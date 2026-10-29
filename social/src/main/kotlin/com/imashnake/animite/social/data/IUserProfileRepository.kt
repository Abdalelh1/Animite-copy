package com.imashnake.animite.social.data

import com.imashnake.animite.social.model.AnimiteUser
import com.imashnake.animite.social.model.ProfileCustomization
import com.imashnake.animite.social.model.ProfilePrivacy
import com.imashnake.animite.social.model.Tier
import kotlinx.coroutines.flow.Flow

/**
 * Clean interface separating the UI and presentation layer from Firestore and network storage.
 * Follows repository pattern with versioned and non-destructive update contracts.
 */
interface IUserProfileRepository {
    fun getUserProfile(uid: String): Flow<AnimiteUser?>
    suspend fun getUserProfileOnce(uid: String): AnimiteUser?

    suspend fun updateUserProfile(
        uid: String,
        displayName: String,
        bio: String,
        location: String? = null,
        websiteUrl: String? = null,
        isPrivate: Boolean = false,
        allowMessagesFrom: String = "everyone",
        username: String? = null,
        birthDate: String? = null,
        gender: String? = null,
    ): Result<Unit>

    suspend fun saveFullProfile(
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
        birthDate: String? = null,
        gender: String? = null,
    ): Result<Unit>

    suspend fun updateCustomization(
        uid: String,
        customization: ProfileCustomization,
    ): Result<Unit>

    suspend fun updatePrivacy(
        uid: String,
        privacy: ProfilePrivacy,
        allowMessagesFrom: String? = null,
    ): Result<Unit>

    suspend fun uploadAvatar(
        uid: String,
        imageBytes: ByteArray,
        mimeType: String = "image/jpeg",
    ): Result<String>

    suspend fun removeAvatar(uid: String): Result<Unit>

    suspend fun uploadBanner(
        uid: String,
        imageBytes: ByteArray,
        mimeType: String = "image/jpeg",
    ): Result<String>

    suspend fun uploadLiveCover(
        uid: String,
        mediaBytes: ByteArray,
        mimeType: String,
        posterBytes: ByteArray? = null,
    ): Result<Pair<String, String?>>

    suspend fun removeLiveCover(uid: String): Result<Unit>

    suspend fun removeCover(uid: String): Result<Unit>

    suspend fun linkAnilistAccount(
        uid: String,
        anilistUserId: Int,
        anilistUsername: String,
    ): Result<Unit>

    suspend fun unlinkAnilistAccount(uid: String): Result<Unit>

    suspend fun updateUserRole(
        targetUid: String,
        role: String,
        customBadgeTitle: String? = null,
        isVerified: Boolean = false,
    ): Result<Unit>

    suspend fun setUserEliteTier(
        callerUid: String,
        targetUid: String,
        tier: Tier,
        eliteBadges: List<String> = emptyList(),
    ): Result<Unit>

    fun searchUsers(query: String, limit: Long = 20): Flow<List<AnimiteUser>>
}
