package com.imashnake.animite.social.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.imashnake.animite.navigation.UserProfileRoute
import com.imashnake.animite.social.data.AnimeListRepository
import com.imashnake.animite.social.data.AuthRepository
import com.imashnake.animite.social.data.CommentRepository
import com.imashnake.animite.social.data.FirestoreSyncRepository
import com.imashnake.animite.social.data.SocialRelationshipRepository
import com.imashnake.animite.social.data.UserProfileRepository
import com.imashnake.animite.social.model.AnimiteUser
import com.imashnake.animite.social.model.AnimeListEntry
import com.imashnake.animite.social.model.AnimeListStats
import com.imashnake.animite.social.model.AuthState
import com.imashnake.animite.social.model.Comment
import com.imashnake.animite.social.model.EliteFeatures
import com.imashnake.animite.social.model.FirestoreLibraryEntry
import com.imashnake.animite.social.model.ProfileCustomization
import com.imashnake.animite.social.model.ProfilePrivacy
import com.imashnake.animite.social.model.Tier
import com.imashnake.animite.social.util.AvatarValidator
import com.imashnake.animite.social.util.LiveCoverValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class UserAnimeStats(
    val totalEntries: Int = 0,
    val watchingCount: Int = 0,
    val completedCount: Int = 0,
    val planToWatchCount: Int = 0,
    val favoritesCount: Int = 0,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val relationshipRepository: SocialRelationshipRepository,
    private val commentRepository: CommentRepository,
    private val authRepository: AuthRepository,
    private val firestoreSyncRepository: FirestoreSyncRepository,
    private val animeListRepository: AnimeListRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val navRoute = runCatching { savedStateHandle.toRoute<UserProfileRoute>() }.getOrNull()
    val navRouteUserId: String? get() = navRoute?.userId
    val targetUserId: String get() = navRoute?.userId ?: authRepository.currentUid.orEmpty()

    /** Reactive authentication state of the viewer. */
    val authState: StateFlow<AuthState> = authRepository.authState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), AuthState.Loading)

    /** The authenticated current viewer's profile. Used to verify permissions (e.g. Owner/Admin). */
    val currentUserProfile: StateFlow<AnimiteUser?> = authRepository.authState.flatMapLatest { authState ->
        when (authState) {
            is AuthState.Authenticated -> userProfileRepository.getUserProfile(authState.user.uid)
                .map { profile -> profile ?: authState.user }
            else -> flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), null)

    /**
     * Dynamically reactive user profile flow.
     * Prevents infinite loading upon account switching by observing [authState] and dynamically
     * transitioning between own profile and target user without retaining stale nav backstack UIDs.
     */
    val userProfile: StateFlow<AnimiteUser?> = authRepository.authState.flatMapLatest { authState ->
        val currentAuthUid = (authState as? AuthState.Authenticated)?.user?.uid
        if (navRoute?.userId == null || (currentAuthUid != null && navRoute.userId == currentAuthUid)) {
            when (authState) {
                is AuthState.Authenticated -> userProfileRepository.getUserProfile(authState.user.uid)
                    .map { profile -> profile ?: authState.user }
                else -> flowOf(null)
            }
        } else {
            userProfileRepository.getUserProfile(navRoute.userId).flatMapLatest { profile ->
                if (profile != null) {
                    flowOf(profile)
                } else if (currentAuthUid == navRoute.userId && authState is AuthState.Authenticated) {
                    flowOf(authState.user)
                } else {
                    flowOf(null)
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), null)

    val isViewingOwnProfile: StateFlow<Boolean> = kotlinx.coroutines.flow.combine(
        authRepository.authState,
        userProfile
    ) { authState, profile ->
        if (navRoute?.userId == null) {
            true
        } else {
            when (authState) {
                is AuthState.Authenticated -> profile?.uid == authState.user.uid || navRoute.userId == authState.user.uid
                else -> false
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), navRoute?.userId == null)

    val currentUid: String? get() = authRepository.currentUid
    val isCurrentUser: Boolean get() = isViewingOwnProfile.value

    /** The profile's library entries in Firestore. */
    val libraryEntries: StateFlow<List<FirestoreLibraryEntry>> = userProfile.flatMapLatest { profile ->
        val uid = profile?.uid ?: targetUserId
        if (uid.isNotBlank()) {
            firestoreSyncRepository.getLibraryEntriesFlow(uid)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), emptyList())

    /** Reactive anime list entries from Firestore anime_lists collection. */
    val animeListEntries: StateFlow<List<AnimeListEntry>> = userProfile.flatMapLatest { profile ->
        val uid = profile?.uid ?: targetUserId
        if (uid.isNotBlank()) {
            animeListRepository.getUserAnimeList(uid)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), emptyList())

    /** Statistical metrics computed from anime list entries with PieChart & progress breakdown. */
    val animeListStats: StateFlow<AnimeListStats> = animeListEntries.map { entries ->
        AnimeListStats.fromEntries(entries)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), AnimeListStats())

    /** Derived actual anime statistics. Supports both anime_lists and synced library. */
    val animeStats: StateFlow<UserAnimeStats> = animeListEntries.map { entries ->
        if (entries.isNotEmpty()) {
            UserAnimeStats(
                totalEntries = entries.size,
                watchingCount = entries.count { it.watchStatus.equals("WATCHING", ignoreCase = true) },
                completedCount = entries.count { it.watchStatus.equals("COMPLETED", ignoreCase = true) },
                planToWatchCount = entries.count { it.watchStatus.equals("PLAN_TO_WATCH", ignoreCase = true) },
                favoritesCount = entries.count { it.isFavorite },
            )
        } else {
            val syncEntries = libraryEntries.value
            UserAnimeStats(
                totalEntries = syncEntries.size,
                watchingCount = syncEntries.count { it.status.equals("WATCHING", ignoreCase = true) },
                completedCount = syncEntries.count { it.status.equals("COMPLETED", ignoreCase = true) },
                planToWatchCount = syncEntries.count { it.status.equals("PLAN_TO_WATCH", ignoreCase = true) },
                favoritesCount = syncEntries.count { it.isFavorite },
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), UserAnimeStats())

    fun updateAnimeListProgress(entryId: String, progress: Int, totalEpisodes: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            animeListRepository.updateProgress(entryId, progress, totalEpisodes)
        }
    }

    fun toggleAnimeListFavorite(entryId: String, isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            animeListRepository.toggleFavorite(entryId, !isFavorite)
        }
    }

    fun isCommentLiked(commentId: String) =
        commentRepository.isCommentLiked(commentId, currentUid.orEmpty())

    fun toggleCommentLike(commentId: String) {
        val userId = currentUid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            commentRepository.toggleLike(commentId, userId)
        }
    }

    /** The profile's "posts": comments authored by this user. Null while loading. */
    val userPosts: StateFlow<List<Comment>?> = userProfile.flatMapLatest { profile ->
        val uid = profile?.uid ?: targetUserId
        if (uid.isNotBlank()) {
            commentRepository.getCommentsByUser(uid)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), null)

    val likedPosts: StateFlow<List<Comment>?> = userProfile.flatMapLatest { profile ->
        val uid = profile?.uid ?: targetUserId
        if (uid.isNotBlank()) {
            commentRepository.getLikedCommentsByUser(uid)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), null)

    val isFollowing: StateFlow<Boolean> = userProfile.flatMapLatest { profile ->
        val target = profile?.uid ?: targetUserId
        val current = currentUid
        if (!current.isNullOrBlank() && target.isNotBlank() && current != target) {
            relationshipRepository.isFollowing(current, target)
        } else {
            flowOf(false)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), false)

    val isBlocked: StateFlow<Boolean> = userProfile.flatMapLatest { profile ->
        val target = profile?.uid ?: targetUserId
        val current = currentUid
        if (!current.isNullOrBlank() && target.isNotBlank() && current != target) {
            relationshipRepository.isUserBlocked(current, target)
        } else {
            flowOf(false)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), false)

    private val _uiState = MutableStateFlow<ProfileUiMessage?>(null)
    val uiState: StateFlow<ProfileUiMessage?> = _uiState.asStateFlow()
    val uiMessage: StateFlow<ProfileUiMessage?> get() = uiState

    private val _isSavingProfile = MutableStateFlow(false)
    val isSavingProfile: StateFlow<Boolean> = _isSavingProfile.asStateFlow()

    // Avatar upload state
    private val _avatarUploadState = MutableStateFlow<AvatarUploadState>(AvatarUploadState.Idle)
    val avatarUploadState: StateFlow<AvatarUploadState> = _avatarUploadState.asStateFlow()

    private var avatarUploadJob: Job? = null
    private var lastSelectedImage: Pair<ByteArray, String>? = null

    fun onImageSelected(context: Context, uri: Uri) {
        when (val validation = AvatarValidator.validateAndRead(context, uri)) {
            is AvatarValidator.ValidationResult.Invalid -> {
                _avatarUploadState.value = AvatarUploadState.Error(validation.reason, canRetry = false)
                _uiState.value = ProfileUiMessage.Error(validation.reason)
            }
            is AvatarValidator.ValidationResult.Valid -> {
                lastSelectedImage = Pair(validation.bytes, validation.mimeType)
                executeAvatarUpload(validation.bytes, validation.mimeType)
            }
        }
    }

    fun onPhotoPickerCancelled() {
        if (_avatarUploadState.value !is AvatarUploadState.Loading) {
            _avatarUploadState.value = AvatarUploadState.Idle
        }
    }

    fun retryAvatarUpload() {
        val cached = lastSelectedImage
        if (cached != null) {
            executeAvatarUpload(cached.first, cached.second)
        } else {
            _uiState.value = ProfileUiMessage.Error("لا توجد صورة محددة لإعادة المحاولة. يرجى اختيار صورة مجدداً.")
        }
    }

    fun cancelAvatarUpload() {
        avatarUploadJob?.cancel()
        avatarUploadJob = null
        _avatarUploadState.value = AvatarUploadState.Idle
        _uiState.value = ProfileUiMessage.Success("تم إلغاء رفع الصورة الشخصية.")
    }

    private fun executeAvatarUpload(bytes: ByteArray, mimeType: String) {
        val current = currentUid ?: run {
            _avatarUploadState.value = AvatarUploadState.Error("يجب تسجيل الدخول لرفع الصورة الشخصية.", canRetry = false)
            _uiState.value = ProfileUiMessage.Error("يجب تسجيل الدخول لرفع الصورة الشخصية.")
            return
        }

        avatarUploadJob?.cancel()
        avatarUploadJob = viewModelScope.launch(Dispatchers.IO) {
            _avatarUploadState.value = AvatarUploadState.Loading
            userProfileRepository.uploadAvatar(current, bytes, mimeType)
                .onSuccess { secureUrl ->
                    _avatarUploadState.value = AvatarUploadState.Success(secureUrl)
                    _uiState.value = ProfileUiMessage.Success("تم تحديث الصورة الشخصية بنجاح")
                    lastSelectedImage = null
                }
                .onFailure { error ->
                    if (error is kotlinx.coroutines.CancellationException) {
                        _avatarUploadState.value = AvatarUploadState.Idle
                        return@onFailure
                    }
                    val errorMsg = error.localizedMessage ?: "تعذر رفع الصورة الشخصية"
                    _avatarUploadState.value = AvatarUploadState.Error(errorMsg, canRetry = true)
                    _uiState.value = ProfileUiMessage.Error(errorMsg)
                }
        }
    }

    fun removeAvatar() {
        val current = currentUid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _avatarUploadState.value = AvatarUploadState.Loading
            userProfileRepository.removeAvatar(current)
                .onSuccess {
                    _avatarUploadState.value = AvatarUploadState.Idle
                    _uiState.value = ProfileUiMessage.Success("تمت إزالة الصورة الشخصية بنجاح")
                }
                .onFailure { error ->
                    _avatarUploadState.value = AvatarUploadState.Idle
                    _uiState.value = ProfileUiMessage.Error("تعذر إزالة الصورة الشخصية: ${error.localizedMessage}")
                }
        }
    }

    fun toggleFollow() {
        val currentUser = authRepository.currentUid ?: return
        val target = userProfile.value ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val isCurrentlyFollowing = isFollowing.value
            if (isCurrentlyFollowing) {
                relationshipRepository.unfollowUser(currentUser, target.uid)
                    .onFailure { _uiState.value = ProfileUiMessage.Error("تعذر إلغاء المتابعة") }
            } else {
                val currentProfile = userProfileRepository.getUserProfileOnce(currentUser) ?: return@launch
                relationshipRepository.followUser(currentProfile, target)
                    .onFailure { _uiState.value = ProfileUiMessage.Error("تعذر المتابعة: ${it.localizedMessage}") }
            }
        }
    }

    fun blockUser() {
        val current = currentUid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            relationshipRepository.blockUser(current, targetUserId)
                .onSuccess { _uiState.value = ProfileUiMessage.Success("تم حظر المستخدم") }
                .onFailure { _uiState.value = ProfileUiMessage.Error("تعذر حظر المستخدم") }
        }
    }

    fun unblockUser() {
        val current = currentUid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            relationshipRepository.unblockUser(current, targetUserId)
                .onSuccess { _uiState.value = ProfileUiMessage.Success("تم إلغاء حظر المستخدم") }
                .onFailure { _uiState.value = ProfileUiMessage.Error("تعذر إلغاء حظر المستخدم") }
        }
    }

    fun reportUser(reason: String) {
        val current = currentUid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            relationshipRepository.report(
                reporterId = current,
                targetType = "user",
                targetId = targetUserId,
                reason = reason,
            ).onSuccess { _uiState.value = ProfileUiMessage.Success("تم إرسال البلاغ بنجاح.") }
             .onFailure { _uiState.value = ProfileUiMessage.Error("تعذر إرسال البلاغ") }
        }
    }

    fun updateProfile(
        displayName: String,
        bio: String,
        location: String? = null,
        websiteUrl: String? = null,
        isPrivate: Boolean,
        allowMessagesFrom: String,
        username: String? = null,
        birthDate: String? = null,
        gender: String? = null,
    ) {
        val current = currentUid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.updateUserProfile(
                uid = current,
                displayName = displayName,
                bio = bio,
                location = location,
                websiteUrl = websiteUrl,
                isPrivate = isPrivate,
                allowMessagesFrom = allowMessagesFrom,
                username = username,
                birthDate = birthDate,
                gender = gender,
            ).onSuccess { _uiState.value = ProfileUiMessage.Success("تم تحديث الملف الشخصي بنجاح") }
             .onFailure { _uiState.value = ProfileUiMessage.Error("تعذر تحديث الملف الشخصي: ${it.localizedMessage}") }
        }
    }

    fun saveFullProfile(
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
        onSuccess: () -> Unit = {},
    ) {
        val current = currentUid ?: run {
            _uiState.value = ProfileUiMessage.Error("يجب تسجيل الدخول لحفظ التعديلات.")
            return
        }
        if (_isSavingProfile.value) return

        viewModelScope.launch(Dispatchers.IO) {
            _isSavingProfile.value = true
            userProfileRepository.saveFullProfile(
                uid = current,
                displayName = displayName,
                bio = bio,
                location = location,
                websiteUrl = websiteUrl,
                isPrivate = isPrivate,
                allowMessagesFrom = allowMessagesFrom,
                username = username,
                customization = customization,
                privacy = privacy,
                birthDate = birthDate,
                gender = gender,
            ).onSuccess {
                _isSavingProfile.value = false
                _uiState.value = ProfileUiMessage.Success("تم حفظ تعديلات الملف الشخصي بنجاح")
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }.onFailure { error ->
                _isSavingProfile.value = false
                _uiState.value = ProfileUiMessage.Error("تعذر حفظ التعديلات: ${error.localizedMessage ?: "خطأ في الاتصال"}")
            }
        }
    }

    fun updateCustomization(customization: ProfileCustomization) {
        val current = currentUid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.updateCustomization(current, customization)
                .onSuccess { _uiState.value = ProfileUiMessage.Success("تم حفظ تخصيص المظهر بنجاح") }
                .onFailure { _uiState.value = ProfileUiMessage.Error("تعذر حفظ التخصيص: ${it.localizedMessage}") }
        }
    }

    /**
     * Saves the privacy scopes from Account settings (Security & privacy) and optionally the
     * messaging permission, so the settings screen owns the profile's visibility contract.
     */
    fun updatePrivacy(privacy: ProfilePrivacy, allowMessagesFrom: String? = null) {
        val current = currentUid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.updatePrivacy(current, privacy, allowMessagesFrom)
                .onSuccess { _uiState.value = ProfileUiMessage.Success("تم تحديث إعدادات الخصوصية") }
                .onFailure { _uiState.value = ProfileUiMessage.Error("تعذر حفظ إعدادات الخصوصية: ${it.localizedMessage}") }
        }
    }

    fun uploadAvatar(bytes: ByteArray, mimeType: String = "image/jpeg") {
        lastSelectedImage = Pair(bytes, mimeType)
        executeAvatarUpload(bytes, mimeType)
    }

    /** Banner counterpart of [onImageSelected], used by the cover photo tap. */
    fun onBannerImageSelected(context: Context, uri: Uri) {
        when (val validation = AvatarValidator.validateAndRead(context, uri)) {
            is AvatarValidator.ValidationResult.Invalid -> {
                _uiState.value = ProfileUiMessage.Error(validation.reason)
            }
            is AvatarValidator.ValidationResult.Valid -> {
                uploadBanner(validation.bytes)
            }
        }
    }

    fun uploadBanner(bytes: ByteArray) {
        val current = currentUid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _liveCoverUploadState.value = LiveCoverUploadState.Uploading
            userProfileRepository.uploadBanner(current, bytes)
                .onSuccess {
                    _liveCoverUploadState.value = LiveCoverUploadState.Idle
                    _uiState.value = ProfileUiMessage.Success("تم تحديث صورة الغلاف بنجاح")
                }
                .onFailure {
                    _liveCoverUploadState.value = LiveCoverUploadState.Idle
                    _uiState.value = ProfileUiMessage.Error("تعذر رفع صورة الغلاف")
                }
        }
    }

    fun removeCover() {
        val current = currentUid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.removeCover(current)
                .onSuccess {
                    _liveCoverUploadState.value = LiveCoverUploadState.Idle
                    _uiState.value = ProfileUiMessage.Success("تمت إزالة صورة الغلاف بنجاح")
                }
                .onFailure {
                    _uiState.value = ProfileUiMessage.Error("تعذر إزالة الغلاف: ${it.localizedMessage}")
                }
        }
    }

    fun linkAnilist(anilistId: Int, anilistUsername: String) {
        val current = currentUid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.linkAnilistAccount(current, anilistId, anilistUsername)
                .onSuccess { _uiState.value = ProfileUiMessage.Success("تم ربط حساب AniList (@$anilistUsername)") }
                .onFailure { _uiState.value = ProfileUiMessage.Error("تعذر ربط حساب AniList") }
        }
    }

    fun unlinkAnilist() {
        val current = currentUid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.unlinkAnilistAccount(current)
                .onSuccess { _uiState.value = ProfileUiMessage.Success("تم إلغاء ربط حساب AniList") }
                .onFailure { _uiState.value = ProfileUiMessage.Error("تعذر إلغاء ربط حساب AniList") }
        }
    }

    fun updateUserRole(
        targetUid: String,
        role: String,
        customBadgeTitle: String?,
        isVerified: Boolean,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.updateUserRole(
                targetUid = targetUid,
                role = role,
                customBadgeTitle = customBadgeTitle,
                isVerified = isVerified,
            ).onSuccess {
                _uiState.value = ProfileUiMessage.Success("تم تحديث رتبة المستخدم بنجاح")
            }.onFailure {
                _uiState.value = ProfileUiMessage.Error("فشل تحديث الرتبة: ${it.localizedMessage}")
            }
        }
    }

    /**
     * Owner-only action: promotes [targetUid] to Elite tier with optional [eliteBadges].
     * Re-verifies caller authority against the live server document inside the repository.
     */
    fun promoteToElite(targetUid: String, eliteBadges: List<String> = emptyList()) {
        val caller = currentUid ?: run {
            _uiState.value = ProfileUiMessage.Error("يجب تسجيل الدخول أولاً.")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.setUserEliteTier(
                callerUid = caller,
                targetUid = targetUid,
                tier = Tier.ELITE,
                eliteBadges = eliteBadges,
            ).onSuccess {
                _uiState.value = ProfileUiMessage.Success("تمت ترقية الحساب إلى النخبة بنجاح")
            }.onFailure { error ->
                _uiState.value = ProfileUiMessage.Error(error.localizedMessage ?: "تعذر تنفيذ العملية. تأكد من صلاحيات المالك.")
            }
        }
    }

    /**
     * Owner-only action: revokes Elite tier from [targetUid] while preserving their stored elite data.
     */
    fun revokeElite(targetUid: String) {
        val caller = currentUid ?: run {
            _uiState.value = ProfileUiMessage.Error("يجب تسجيل الدخول أولاً.")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            userProfileRepository.setUserEliteTier(
                callerUid = caller,
                targetUid = targetUid,
                tier = Tier.REGULAR,
                eliteBadges = emptyList(),
            ).onSuccess {
                _uiState.value = ProfileUiMessage.Success("تم إلغاء صلاحية النخبة عن الحساب")
            }.onFailure { error ->
                _uiState.value = ProfileUiMessage.Error(error.localizedMessage ?: "تعذر تنفيذ العملية. تأكد من صلاحيات المالك.")
            }
        }
    }

    fun clearUiMessage() {
        _uiState.value = null
    }

    // Live Cover upload state & operations
    private val _liveCoverUploadState = MutableStateFlow<LiveCoverUploadState>(LiveCoverUploadState.Idle)
    val liveCoverUploadState: StateFlow<LiveCoverUploadState> = _liveCoverUploadState.asStateFlow()

    private var liveCoverUploadJob: Job? = null
    private var lastSelectedLiveCover: Triple<ByteArray, String, ByteArray?>? = null

    fun onLiveCoverSelected(context: Context, uri: Uri) {
        val current = currentUid ?: run {
            _uiState.value = ProfileUiMessage.Error("يجب تسجيل الدخول لرفع الغلاف المتحرك.")
            return
        }
        _liveCoverUploadState.value = LiveCoverUploadState.Validating
        viewModelScope.launch(Dispatchers.IO) {
            // Never trust a locally cached tier for permission-sensitive actions; read fresh from user doc
            val freshProfile = userProfileRepository.getUserProfileOnce(current)
            val features = EliteFeatures.forUser(freshProfile)
            if (!features.canUseLiveCover) {
                val msg = "الغلاف المتحرك متاح لحسابات النخبة فقط."
                _liveCoverUploadState.value = LiveCoverUploadState.Error(msg, canRetry = false)
                _uiState.value = ProfileUiMessage.Error(msg)
                return@launch
            }
            when (val validation = LiveCoverValidator.validateAndRead(context, uri, isPremium = true)) {
                is LiveCoverValidator.ValidationResult.Invalid -> {
                    _liveCoverUploadState.value = LiveCoverUploadState.Error(validation.reason, canRetry = false)
                    _uiState.value = ProfileUiMessage.Error(validation.reason)
                }
                is LiveCoverValidator.ValidationResult.Valid -> {
                    lastSelectedLiveCover = Triple(validation.bytes, validation.mimeType, validation.posterBytes)
                    executeLiveCoverUpload(validation.bytes, validation.mimeType, validation.posterBytes)
                }
            }
        }
    }

    fun onLiveCoverPickerCancelled() {
        if (_liveCoverUploadState.value !is LiveCoverUploadState.Uploading &&
            _liveCoverUploadState.value !is LiveCoverUploadState.Validating
        ) {
            _liveCoverUploadState.value = LiveCoverUploadState.Idle
        }
    }

    fun retryLiveCoverUpload() {
        val cached = lastSelectedLiveCover
        if (cached != null) {
            executeLiveCoverUpload(cached.first, cached.second, cached.third)
        } else {
            _uiState.value = ProfileUiMessage.Error("لا يوجد ملف وسائط لإعادة المحاولة. يرجى اختيار فيديو أو GIF مجدداً.")
        }
    }

    fun cancelLiveCoverUpload() {
        liveCoverUploadJob?.cancel()
        liveCoverUploadJob = null
        _liveCoverUploadState.value = LiveCoverUploadState.Idle
        _uiState.value = ProfileUiMessage.Success("تم إلغاء رفع الغلاف المتحرك.")
    }

    private fun executeLiveCoverUpload(bytes: ByteArray, mimeType: String, posterBytes: ByteArray?) {
        val current = currentUid ?: run {
            _liveCoverUploadState.value = LiveCoverUploadState.Error("يجب تسجيل الدخول لرفع الغلاف.", canRetry = false)
            _uiState.value = ProfileUiMessage.Error("يجب تسجيل الدخول لرفع الغلاف.")
            return
        }

        liveCoverUploadJob?.cancel()
        liveCoverUploadJob = viewModelScope.launch(Dispatchers.IO) {
            _liveCoverUploadState.value = LiveCoverUploadState.Uploading
            userProfileRepository.uploadLiveCover(current, bytes, mimeType, posterBytes)
                .onSuccess { (mediaUrl, posterUrl) ->
                    _liveCoverUploadState.value = LiveCoverUploadState.Success(mediaUrl, posterUrl)
                    _uiState.value = ProfileUiMessage.Success("تم تحديث الغلاف المتحرك بنجاح")
                    lastSelectedLiveCover = null
                }
                .onFailure { error ->
                    if (error is kotlinx.coroutines.CancellationException) {
                        _liveCoverUploadState.value = LiveCoverUploadState.Idle
                        return@onFailure
                    }
                    val errorMsg = error.localizedMessage ?: "تعذر رفع الغلاف المتحرك"
                    _liveCoverUploadState.value = LiveCoverUploadState.Error(errorMsg, canRetry = true)
                    _uiState.value = ProfileUiMessage.Error(errorMsg)
                }
        }
    }

    fun removeLiveCover() {
        removeCover()
    }

    fun signOut() {
        authRepository.signOut()
    }
}

sealed interface LiveCoverUploadState {
    data object Idle : LiveCoverUploadState
    data object Validating : LiveCoverUploadState
    data object Uploading : LiveCoverUploadState
    data class Success(val mediaUrl: String, val posterUrl: String?) : LiveCoverUploadState
    data class Error(val message: String, val canRetry: Boolean = true) : LiveCoverUploadState
}

sealed interface AvatarUploadState {
    data object Idle : AvatarUploadState
    data object Loading : AvatarUploadState
    data class Success(val url: String) : AvatarUploadState
    data class Error(val message: String, val canRetry: Boolean = true) : AvatarUploadState
}

sealed interface ProfileUiMessage {
    data class Success(val message: String) : ProfileUiMessage
    data class Error(val message: String) : ProfileUiMessage
}
