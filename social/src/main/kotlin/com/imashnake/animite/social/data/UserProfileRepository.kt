package com.imashnake.animite.social.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.imashnake.animite.api.preferences.PreferencesRepository
import com.imashnake.animite.social.model.AnimiteUser
import com.imashnake.animite.social.model.EliteFeatures
import com.imashnake.animite.social.model.ProfileCustomization
import com.imashnake.animite.social.model.ProfilePrivacy
import com.imashnake.animite.social.model.Tier
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val cloudinaryUploader: CloudinaryUploader,
    private val preferencesRepository: PreferencesRepository,
) : IUserProfileRepository {

    override fun getUserProfile(uid: String): Flow<AnimiteUser?> = callbackFlow {
        if (uid.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val docRef = firestore.collection("users").document(uid)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }
            val user = snapshot?.toObject(AnimiteUser::class.java)?.withResolvedAuthority()
            trySend(user)
        }
        awaitClose { registration.remove() }
    }

    /**
     * Fetches the user document directly from Firestore (preferring [Source.SERVER] so
     * permission-sensitive actions never trust a stale locally cached tier or owner status).
     */
    override suspend fun getUserProfileOnce(uid: String): AnimiteUser? {
        if (uid.isBlank()) return null
        val docRef = firestore.collection("users").document(uid)
        val snapshot = runCatching {
            docRef.get(Source.SERVER).await()
        }.getOrElse {
            docRef.get().await()
        }
        return snapshot.toObject(AnimiteUser::class.java)?.withResolvedAuthority()
    }

    override suspend fun updateUserProfile(
        uid: String,
        displayName: String,
        bio: String,
        location: String?,
        websiteUrl: String?,
        isPrivate: Boolean,
        allowMessagesFrom: String,
        username: String?,
        birthDate: String?,
        gender: String?,
    ): Result<Unit> = runCatching {
        val updates = mutableMapOf<String, Any?>(
            "displayName" to displayName.trim(),
            "bio" to bio.trim(),
            "location" to location?.trim(),
            "websiteUrl" to websiteUrl?.trim(),
            "isPrivate" to isPrivate,
            "allowMessagesFrom" to allowMessagesFrom,
            "birthDate" to birthDate?.trim()?.ifBlank { null },
            "gender" to gender?.trim()?.ifBlank { null },
            "updatedAt" to System.currentTimeMillis(),
        )
        if (!username.isNullOrBlank()) updates["username"] = username.trim().removePrefix("@")

        firestore.collection("users").document(uid).update(updates).await()
    }

    override suspend fun saveFullProfile(
        uid: String,
        displayName: String,
        bio: String,
        location: String?,
        websiteUrl: String?,
        isPrivate: Boolean,
        allowMessagesFrom: String,
        username: String?,
        customization: ProfileCustomization,
        privacy: ProfilePrivacy,
        birthDate: String?,
        gender: String?,
    ): Result<Unit> = runCatching {
        val freshUser = getUserProfileOnce(uid)
            ?: throw IllegalStateException("تعذر التحقق من بيانات الحساب.")
        val sanitizedCustomization = sanitizeCustomizationForUser(freshUser, customization)

        val updates = mutableMapOf<String, Any?>(
            "displayName" to displayName.trim(),
            "bio" to bio.trim(),
            "location" to location?.trim(),
            "websiteUrl" to websiteUrl?.trim(),
            "isPrivate" to isPrivate,
            "allowMessagesFrom" to allowMessagesFrom,
            "birthDate" to birthDate?.trim()?.ifBlank { null },
            "gender" to gender?.trim()?.ifBlank { null },
            "customization" to sanitizedCustomization,
            "privacy" to privacy,
            "updatedAt" to System.currentTimeMillis(),
        )
        if (!username.isNullOrBlank()) {
            updates["username"] = username.trim().removePrefix("@")
        }
        firestore.collection("users").document(uid).update(updates).await()
    }

    override suspend fun updateCustomization(
        uid: String,
        customization: ProfileCustomization,
    ): Result<Unit> = runCatching {
        val freshUser = getUserProfileOnce(uid)
            ?: throw IllegalStateException("تعذر التحقق من بيانات الحساب.")
        val sanitizedCustomization = sanitizeCustomizationForUser(freshUser, customization)

        val updates = mapOf<String, Any>(
            "customization" to sanitizedCustomization,
            "updatedAt" to System.currentTimeMillis(),
        )
        firestore.collection("users").document(uid).update(updates).await()
    }

    /**
     * Ensures regular accounts cannot newly enable elite-only customization options,
     * while preserving any previously stored customization if the user was demoted from elite.
     */
    private fun sanitizeCustomizationForUser(
        freshUser: AnimiteUser,
        requested: ProfileCustomization,
    ): ProfileCustomization {
        val features = EliteFeatures.forUser(freshUser)
        if (features.isElite) return requested

        val existing = freshUser.customization
        return requested.copy(
            themePreset = if (features.isThemePresetAllowed(requested.themePreset)) {
                requested.themePreset
            } else {
                existing.themePreset
            },
            accentColor = if (features.isAccentColorAllowed(requested.accentColor)) {
                requested.accentColor
            } else {
                existing.accentColor
            },
            enableDecorativeEffects = if (features.canUseAvatarNeonGlow) {
                requested.enableDecorativeEffects
            } else {
                existing.enableDecorativeEffects
            },
        )
    }

    override suspend fun updatePrivacy(
        uid: String,
        privacy: ProfilePrivacy,
        allowMessagesFrom: String?,
    ): Result<Unit> = runCatching {
        val updates = mutableMapOf<String, Any>(
            "privacy" to privacy,
            "isPrivate" to (privacy.profileVisibility == "PRIVATE"),
            "updatedAt" to System.currentTimeMillis(),
        )
        if (!allowMessagesFrom.isNullOrBlank()) {
            updates["allowMessagesFrom"] = allowMessagesFrom
        }
        firestore.collection("users").document(uid).update(updates).await()
    }

    override suspend fun uploadAvatar(
        uid: String,
        imageBytes: ByteArray,
        mimeType: String,
    ): Result<String> = runCatching {
        val downloadUrl = cloudinaryUploader.uploadImage(imageBytes, mimeType = mimeType)
            .getOrThrow()

        try {
            firestore.collection("users").document(uid).update(
                mapOf(
                    "photoUrl" to downloadUrl,
                    "updatedAt" to System.currentTimeMillis(),
                )
            ).await()
        } catch (e: Exception) {
            throw Exception("Failed to update profile in Firestore: ${e.localizedMessage}", e)
        }

        runCatching {
            preferencesRepository.setViewerAvatar(downloadUrl)
        }

        downloadUrl
    }

    override suspend fun removeAvatar(uid: String): Result<Unit> = runCatching {
        firestore.collection("users").document(uid).update(
            mapOf(
                "photoUrl" to null,
                "updatedAt" to System.currentTimeMillis(),
            )
        ).await()

        runCatching {
            preferencesRepository.setViewerAvatar(null)
        }
    }

    override suspend fun uploadBanner(
        uid: String,
        imageBytes: ByteArray,
        mimeType: String,
    ): Result<String> = runCatching {
        val freshUser = getUserProfileOnce(uid)
        val features = EliteFeatures.forUser(freshUser)

        val downloadUrl = cloudinaryUploader.uploadImage(imageBytes, mimeType = mimeType)
            .getOrThrow()

        // Regular users only update bannerUrl (never touching liveCoverUrl so Firestore rules
        // allow the update and any demoted elite user's previous animated cover stays preserved).
        val updates = if (features.canUseLiveCover) {
            mapOf(
                "bannerUrl" to downloadUrl,
                "liveCoverUrl" to null,
                "liveCoverPosterUrl" to null,
                "liveCoverMediaType" to null,
                "updatedAt" to System.currentTimeMillis(),
            )
        } else {
            mapOf(
                "bannerUrl" to downloadUrl,
                "updatedAt" to System.currentTimeMillis(),
            )
        }

        firestore.collection("users").document(uid).update(updates).await()

        downloadUrl
    }

    override suspend fun uploadLiveCover(
        uid: String,
        mediaBytes: ByteArray,
        mimeType: String,
        posterBytes: ByteArray?,
    ): Result<Pair<String, String?>> = runCatching {
        // Never trust locally cached tier: verify from the user document before uploading
        val freshUser = getUserProfileOnce(uid)
            ?: throw IllegalStateException("تعذر التحقق من صلاحيات الحساب.")
        val features = EliteFeatures.forUser(freshUser)
        if (!features.canUseLiveCover) {
            throw SecurityException("ميزة الغلاف المتحرك متاحة لحسابات النخبة فقط.")
        }

        val mediaUrl = cloudinaryUploader.uploadMedia(mediaBytes, mimeType = mimeType).getOrThrow()

        val posterUrl: String? = if (posterBytes != null && posterBytes.isNotEmpty()) {
            cloudinaryUploader.uploadImage(posterBytes, mimeType = "image/jpeg").getOrNull()
                ?: cloudinaryUploader.derivePosterUrl(mediaUrl)
        } else {
            cloudinaryUploader.derivePosterUrl(mediaUrl)
        }

        val updates = mapOf(
            "liveCoverUrl" to mediaUrl,
            "liveCoverPosterUrl" to posterUrl,
            "liveCoverMediaType" to mimeType,
            "updatedAt" to System.currentTimeMillis(),
        )

        firestore.collection("users").document(uid).update(updates).await()

        Pair(mediaUrl, posterUrl)
    }

    override suspend fun removeLiveCover(uid: String): Result<Unit> = runCatching {
        val freshUser = getUserProfileOnce(uid)
            ?: throw IllegalStateException("تعذر التحقق من صلاحيات الحساب.")
        val features = EliteFeatures.forUser(freshUser)
        if (!features.canUseLiveCover) {
            throw SecurityException("ميزة الغلاف المتحرك متاحة لحسابات النخبة فقط.")
        }

        val updates = mapOf<String, Any?>(
            "liveCoverUrl" to null,
            "liveCoverPosterUrl" to null,
            "liveCoverMediaType" to null,
            "updatedAt" to System.currentTimeMillis(),
        )
        firestore.collection("users").document(uid).update(updates).await()
    }

    override suspend fun removeCover(uid: String): Result<Unit> = runCatching {
        val freshUser = getUserProfileOnce(uid)
        val features = EliteFeatures.forUser(freshUser)

        val updates = if (features.canUseLiveCover) {
            mapOf<String, Any?>(
                "bannerUrl" to null,
                "liveCoverUrl" to null,
                "liveCoverPosterUrl" to null,
                "liveCoverMediaType" to null,
                "updatedAt" to System.currentTimeMillis(),
            )
        } else {
            mapOf<String, Any?>(
                "bannerUrl" to null,
                "updatedAt" to System.currentTimeMillis(),
            )
        }
        firestore.collection("users").document(uid).update(updates).await()
    }

    override suspend fun linkAnilistAccount(
        uid: String,
        anilistUserId: Int,
        anilistUsername: String,
    ): Result<Unit> = runCatching {
        firestore.collection("users").document(uid).update(
            mapOf(
                "anilistUserId" to anilistUserId,
                "anilistUsername" to anilistUsername,
                "updatedAt" to System.currentTimeMillis(),
            )
        ).await()
    }

    override suspend fun unlinkAnilistAccount(uid: String): Result<Unit> = runCatching {
        firestore.collection("users").document(uid).update(
            mapOf(
                "anilistUserId" to null,
                "anilistUsername" to null,
                "updatedAt" to System.currentTimeMillis(),
            )
        ).await()
    }

    override suspend fun updateUserRole(
        targetUid: String,
        role: String,
        customBadgeTitle: String?,
        isVerified: Boolean,
    ): Result<Unit> = runCatching {
        val updates = mutableMapOf<String, Any?>(
            "role" to role.trim().lowercase(Locale.ROOT),
            "isVerified" to isVerified,
            "updatedAt" to System.currentTimeMillis(),
        )
        if (customBadgeTitle != null) {
            updates["customBadgeTitle"] = customBadgeTitle.trim()
        }
        firestore.collection("users").document(targetUid).update(updates).await()
    }

    /**
     * Grants or revokes the [Tier.ELITE] tier on [targetUid].
     * Strictly verifies from the live Firestore user document that [callerUid] is the app owner.
     * On demotion ([Tier.REGULAR]), preserves the user's previous animated cover, badges, and customization
     * in Firestore so they return if the user is promoted again later.
     */
    override suspend fun setUserEliteTier(
        callerUid: String,
        targetUid: String,
        tier: Tier,
        eliteBadges: List<String>,
    ): Result<Unit> = runCatching {
        require(callerUid.isNotBlank() && targetUid.isNotBlank()) {
            "معرّف المستخدم غير صالح."
        }
        val callerProfile = getUserProfileOnce(callerUid)
            ?: throw SecurityException("تعذر التحقق من صلاحيات حسابك.")
        if (!callerProfile.isOwner) {
            throw SecurityException("غير مصرح لك بترقية أو إلغاء النخبة. هذه الصلاحية لمالك التطبيق فقط.")
        }

        val now = System.currentTimeMillis()
        val updates = when (tier) {
            Tier.ELITE -> mapOf<String, Any?>(
                "tier" to Tier.ELITE.id,
                "eliteGrantedAt" to now,
                "eliteGrantedBy" to callerUid,
                "eliteBadges" to eliteBadges.map { it.trim() }.filter { it.isNotEmpty() }.distinct(),
                "updatedAt" to now,
            )
            Tier.REGULAR -> mapOf<String, Any?>(
                "tier" to Tier.REGULAR.id,
                "updatedAt" to now,
            )
        }

        firestore.collection("users").document(targetUid).update(updates).await()
    }

    override fun searchUsers(query: String, limit: Long): Flow<List<AnimiteUser>> = callbackFlow {
        val clean = query.trim().lowercase(Locale.ROOT)
        if (clean.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val queryRef = firestore.collection("users")
            .orderBy("username")
            .startAt(clean)
            .endAt(clean + "\uf8ff")
            .limit(limit)

        val registration = queryRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val users = snapshot?.documents
                ?.mapNotNull { it.toObject(AnimiteUser::class.java)?.withResolvedAuthority() }
                .orEmpty()
            trySend(users)
        }
        awaitClose { registration.remove() }
    }
}
