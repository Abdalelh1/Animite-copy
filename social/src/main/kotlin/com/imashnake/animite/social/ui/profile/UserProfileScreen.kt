package com.imashnake.animite.social.ui.profile

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.animation.core.animate
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.unit.Velocity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.TabRow
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import com.imashnake.animite.social.model.AuthState
import com.imashnake.animite.social.ui.auth.AuthContent
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.core.view.WindowCompat

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.imashnake.animite.core.ui.tokens.RoleColors
import com.imashnake.animite.core.ui.tokens.StatusColors
import com.imashnake.animite.core.ui.tokens.SemanticColors
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.imashnake.animite.social.model.AnimiteUser
import com.imashnake.animite.social.model.AvatarShape
import com.imashnake.animite.social.model.FieldVisibility
import com.imashnake.animite.social.model.isVisibleTo
import com.imashnake.animite.social.model.ProfileCustomization
import com.imashnake.animite.social.model.ProfileDesignTier
import com.imashnake.animite.social.model.EliteFeatures
import com.imashnake.animite.social.model.EliteRoleBadge
import com.imashnake.animite.social.model.Tier
import com.imashnake.animite.social.ui.profile.BannerCropDialog
import com.imashnake.animite.social.model.Comment
import com.imashnake.animite.social.model.AnimeListEntry
import com.imashnake.animite.social.model.AnimeListStats
import com.imashnake.animite.social.ui.animelist.AnimeListStatsCard
import com.imashnake.animite.social.model.WatchStatus
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Twitter/X-style social profile, skinned with Animite's own Material theme
 * (dynamic colour scheme + Cairo/Animite typography) — no Twitter branding
 * colours are hardcoded; every colour below is a theme token so the screen
 * follows the app's time-of-day palette, dark/AMOLED and system schemes.
 *
 * Layout (single column, content capped at 600.dp and centred on large
 * screens, mirroring Twitter's profile):
 *  - app bar: display name + post count, back / share / overflow actions
 *  - cover banner with the avatar overlapping its bottom edge
 *  - action row: Edit Profile (own) or Message + Follow/Following (others)
 *  - name, @handle, bio, joined date, Following/Followers stats
 *  - Posts / Replies / Media tabs in a pager with an animated indicator
 *
 * "Posts" are the comments this user authored (see CommentRepository
 * .getCommentsByUser); Media aggregates the anime/episodes they posted about.
 * See docs/profile-redesign/DESIGN.md for tokens, states and QA checklists.
 */
internal enum class ProfileTab(@param:androidx.annotation.StringRes val titleRes: Int) {
    POSTS(com.imashnake.animite.social.R.string.posts),
    REPLIES(com.imashnake.animite.social.R.string.replies),
    MEDIA(com.imashnake.animite.social.R.string.media),
    LIKES(com.imashnake.animite.social.R.string.likes),
    LIBRARY(com.imashnake.animite.social.R.string.library_tab),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onBack: (() -> Unit)? = null,
    onNavigateToFollowers: (userId: String) -> Unit,
    onNavigateToFollowing: (userId: String) -> Unit,
    onNavigateToChat: (userId: String, displayName: String, avatar: String?) -> Unit,
    onNavigateToEditProfile: (() -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null,
    onNavigateToAccountSettings: (() -> Unit)? = null,
    onNavigateToOwnerControlCenter: (() -> Unit)? = null,
    onNavigateToAnimeList: ((userId: String) -> Unit)? = null,
    onNavigateToAnime: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: UserProfileViewModel = hiltViewModel(),
) {
    val authState by viewModel.authState.collectAsState()
    val user by viewModel.userProfile.collectAsState()
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()
    val posts by viewModel.userPosts.collectAsState()
    val animeStats by viewModel.animeStats.collectAsState()
    val libraryEntries by viewModel.libraryEntries.collectAsState()
    val animeListEntries by viewModel.animeListEntries.collectAsState()
    val animeListStats by viewModel.animeListStats.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val isBlocked by viewModel.isBlocked.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()
    val avatarUploadState by viewModel.avatarUploadState.collectAsState()
    val isUploadingAvatar = avatarUploadState is AvatarUploadState.Loading

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onImageSelected(context, uri)
        } else {
            viewModel.onPhotoPickerCancelled()
        }
    }

    var pendingBannerCropUri by remember { mutableStateOf<Uri?>(null) }

    val bannerPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingBannerCropUri = uri
        }
    }

    val liveCoverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onLiveCoverSelected(context, uri)
        } else {
            viewModel.onLiveCoverPickerCancelled()
        }
    }

    val liveCoverUploadState by viewModel.liveCoverUploadState.collectAsState()
    val isUploadingLiveCover = liveCoverUploadState is LiveCoverUploadState.Uploading || liveCoverUploadState is LiveCoverUploadState.Validating

    var showMenu by remember { mutableStateOf(false) }
    var showBlockConfirmDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showUnfollowDialog by remember { mutableStateOf(false) }
    var previewImageUrl by remember { mutableStateOf<String?>(null) }
    var showAvatarOptionsDialog by remember { mutableStateOf(false) }
    var showCoverOptionsDialog by remember { mutableStateOf(false) }
    var showRoleManagementDialog by remember { mutableStateOf(false) }
    var showElitePromotionDialog by remember { mutableStateOf(false) }
    var showEliteRevokeDialog by remember { mutableStateOf(false) }
    var showEliteLockedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(liveCoverUploadState) {
        if (liveCoverUploadState is LiveCoverUploadState.Error) {
            val errorState = liveCoverUploadState as LiveCoverUploadState.Error
            val result = snackbarHostState.showSnackbar(
                message = errorState.message,
                actionLabel = if (errorState.canRetry) "Retry" else null,
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.retryLiveCoverUpload()
            }
        }
    }

    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            when (msg) {
                is ProfileUiMessage.Success -> snackbarHostState.showSnackbar(msg.message)
                is ProfileUiMessage.Error -> snackbarHostState.showSnackbar(msg.message)
            }
            viewModel.clearUiMessage()
        }
    }

    LaunchedEffect(avatarUploadState) {
        if (avatarUploadState is AvatarUploadState.Error) {
            val errorState = avatarUploadState as AvatarUploadState.Error
            val result = snackbarHostState.showSnackbar(
                message = errorState.message,
                actionLabel = if (errorState.canRetry) "Retry" else null,
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.retryAvatarUpload()
            }
        }
    }
    val view = LocalView.current
    DisposableEffect(view) {
        val activity = view.context.findActivity() ?: return@DisposableEffect onDispose {}
        val window = activity.window
        val insetsController = WindowCompat.getInsetsController(window, view)
        val originalStatusBarColor = window.statusBarColor
        val originalAppearanceLightStatusBars = insetsController.isAppearanceLightStatusBars

        // 1. Transparent status bar so cover image flows seamlessly behind it
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        // 2. High-contrast light (white) status bar icons against the cover image
        insetsController.isAppearanceLightStatusBars = false

        onDispose {
            window.statusBarColor = originalStatusBarColor
            insetsController.isAppearanceLightStatusBars = originalAppearanceLightStatusBars
        }
    }

    val bottomPadding = if (onBack == null) {
        dimensionResource(com.imashnake.animite.navigation.R.dimen.navigation_bar_height) +
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp
    } else {
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
    ) { _ ->
        val isOwnProfile by viewModel.isViewingOwnProfile.collectAsState()
        val isSelf = onBack == null || isOwnProfile
        if (isSelf && authState is AuthState.Unauthenticated) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.TopCenter
            ) {
                AuthContent(
                    onSettingsClick = onNavigateToSettings,
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 540.dp)
                        .padding(bottom = bottomPadding)
                )
            }
        } else if (user == null) {
            var isTimeout by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(7000)
                isTimeout = true
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                if (isTimeout && authState !is AuthState.Loading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isSelf) "تعذر مزامنة بيانات حسابك حالياً" else "المستخدم غير موجود أو تعذر تحميل بياناته",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "يرجى التحقق من اتصالك بالإنترنت والمحاولة مجدداً.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (onBack != null) {
                                OutlinedButton(onClick = onBack) {
                                    Text("رجوع")
                                }
                            }
                            Button(onClick = { isTimeout = false }) {
                                Text("إعادة المحاولة")
                            }
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "جاري التحقق والمزامنة...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            key(viewModel.targetUserId) {
                val profile = user!!
                val allPosts = posts
                val topLevelPosts = remember(allPosts) { allPosts?.filter { it.parentId == null } }
                val replies = remember(allPosts) { allPosts?.filter { it.parentId != null } }
                val mediaGroups = remember(allPosts) { allPosts?.let { groupMediaByAnime(it) } }
                val isBasicTier = profile.designTier == ProfileDesignTier.BASIC

                // Banner (cover): max height 200.dp, min height 76.dp
                val maxBannerHeight = 200.dp
                val minBannerHeight = 76.dp
                val tabRowHeight = 48.dp
                val density = LocalDensity.current
                val maxBannerHeightPx = with(density) { maxBannerHeight.toPx() }
                val minBannerHeightPx = with(density) { minBannerHeight.toPx() }

                var bannerHeightPx by remember { mutableFloatStateOf(maxBannerHeightPx) }
                var isBannerExpanded by rememberSaveable { mutableStateOf(true) }
                var isManualToggleAnimating by remember { mutableStateOf(false) }

                // Single `ratio` value (1f = fully expanded, 0f = fully collapsed) computed in one place
                val rawRatio = if (isBannerExpanded) {
                    ((bannerHeightPx - minBannerHeightPx) / (maxBannerHeightPx - minBannerHeightPx))
                        .fastCoerceIn(0f, 1f)
                } else {
                    0f
                }

                val ratio by animateFloatAsState(
                    targetValue = rawRatio,
                    animationSpec = tween(durationMillis = if (isManualToggleAnimating) 350 else 50),
                    finishedListener = { isManualToggleAnimating = false },
                    label = "profile_collapse_ratio"
                )

                val nestedScrollConnection = remember(minBannerHeightPx, maxBannerHeightPx) {
                    object : NestedScrollConnection {
                        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                            // If available.y > 0 do nothing; otherwise reduce banner height by scroll amount,
                            // clamp between min and max, and consume only the portion used by the banner.
                            if (available.y > 0f) return Offset.Zero
                            isManualToggleAnimating = false
                            if (!isBannerExpanded) return Offset.Zero

                            val prevBannerHeight = bannerHeightPx
                            bannerHeightPx = (bannerHeightPx + available.y)
                                .fastCoerceIn(minBannerHeightPx, maxBannerHeightPx)
                            val consumedY = bannerHeightPx - prevBannerHeight
                            return Offset(0f, consumedY)
                        }

                        override fun onPostScroll(
                            consumed: Offset,
                            available: Offset,
                            source: NestedScrollSource,
                        ): Offset {
                            // When scrolling down and the content is at the top, expand the banner back.
                            if (available.y > 0f) {
                                isManualToggleAnimating = false
                                isBannerExpanded = true
                                val prevBannerHeight = bannerHeightPx
                                bannerHeightPx = (bannerHeightPx + available.y)
                                    .fastCoerceIn(minBannerHeightPx, maxBannerHeightPx)
                                val consumedY = bannerHeightPx - prevBannerHeight
                                return Offset(0f, consumedY)
                            }
                            return Offset.Zero
                        }
                    }
                }

                val profileTabs = remember(isBasicTier) {
                    if (isBasicTier) {
                        listOf(ProfileTab.POSTS, ProfileTab.REPLIES, ProfileTab.MEDIA, ProfileTab.LIKES)
                    } else {
                        listOf(ProfileTab.POSTS, ProfileTab.REPLIES, ProfileTab.MEDIA, ProfileTab.LIBRARY)
                    }
                }
                val pagerState = rememberPagerState(pageCount = { profileTabs.size })

                val features = remember(profile.tier, profile.isOwner, profile.role) {
                    profile.features
                }

                val effectiveAccentHex = if (features.canUseCustomAccentColors) {
                    profile.customization.accentColor
                } else {
                    features.standardAccentColor
                }

                val accentColor = try {
                    Color(android.graphics.Color.parseColor(effectiveAccentHex))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }

                val coverType = remember(
                    profile.liveCoverUrl,
                    profile.liveCoverMediaType,
                    profile.liveCoverPosterUrl,
                    profile.bannerUrl,
                    features.canUseLiveCover
                ) {
                    resolveCoverType(
                        liveCoverUrl = profile.liveCoverUrl,
                        mediaType = profile.liveCoverMediaType,
                        posterUrl = profile.liveCoverPosterUrl,
                        bannerUrl = profile.bannerUrl,
                        canUseLiveCover = features.canUseLiveCover
                    )
                }

                val nestedScrollDispatcher = remember {
                    androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher()
                }

                val likedPosts = if (isBasicTier) {
                    viewModel.likedPosts.collectAsState().value
                } else {
                    null
                }

                if (isBasicTier) {
                    BasicProfileContent(
                        profile = profile,
                        viewModel = viewModel,
                        currentUserProfile = currentUserProfile,
                        allPosts = allPosts,
                        topLevelPosts = topLevelPosts,
                        replies = replies,
                        mediaGroups = mediaGroups,
                        likedPosts = likedPosts,
                        isFollowing = isFollowing,
                        isBlocked = isBlocked,
                        isUploadingAvatar = isUploadingAvatar,
                        bottomPadding = bottomPadding,
                        accentColor = accentColor,
                        onBack = onBack,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToEditProfile = onNavigateToEditProfile,
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToFollowers = onNavigateToFollowers,
                        onNavigateToFollowing = onNavigateToFollowing,
                        onShareProfile = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Check out ${profile.displayName} on Animite! @${profile.username}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة الملف الشخصي"))
                        },
                        onPreviewBanner = {
                            if (!profile.bannerUrl.isNullOrBlank()) {
                                previewImageUrl = profile.bannerUrl
                            }
                        },
                        onToggleBlockUser = {
                            if (isBlocked) viewModel.unblockUser() else showBlockConfirmDialog = true
                        },
                        onReportUser = { showReportDialog = true },
                        onAvatarClick = {
                            if (viewModel.isCurrentUser) {
                                if (!profile.photoUrl.isNullOrBlank()) {
                                    showAvatarOptionsDialog = true
                                } else {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            } else {
                                if (!profile.photoUrl.isNullOrBlank()) {
                                    previewImageUrl = profile.photoUrl
                                }
                            }
                        },
                        onBannerClick = {
                            if (viewModel.isCurrentUser) {
                                showCoverOptionsDialog = true
                            } else {
                                if (!profile.bannerUrl.isNullOrBlank()) {
                                    previewImageUrl = profile.bannerUrl
                                }
                            }
                        },
                        onFollowClick = { viewModel.toggleFollow() },
                        onUnfollowClick = { showUnfollowDialog = true },
                        onOwnerControlCenterClick = if (currentUserProfile?.isOwner == true) onNavigateToOwnerControlCenter else null,
                        showMenu = showMenu,
                        onShowMenuChange = { showMenu = it },
                        isViewerOwner = currentUserProfile?.isOwner == true && !viewModel.isCurrentUser,
                        canManageRoles = (currentUserProfile?.isOwner == true || currentUserProfile?.isAdmin == true) && !viewModel.isCurrentUser,
                        onPromoteToElite = { showElitePromotionDialog = true },
                        onRevokeElite = { showEliteRevokeDialog = true },
                        onManageRoles = { showRoleManagementDialog = true }
                    )
                } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(nestedScrollConnection, nestedScrollDispatcher)
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 600.dp)
                    ) {
                        // 1. Collapsing Profile Header (Banner + Top Bar + Avatar + Details) driven by `ratio`
                        ProfileHeader(
                            profile = profile,
                            features = features,
                            stats = animeStats,
                            ratio = ratio,
                            coverType = coverType,
                            accentColor = accentColor,
                            isCurrentUser = viewModel.isCurrentUser,
                            isFollowing = isFollowing,
                            isBlocked = isBlocked,
                            isUploadingAvatar = isUploadingAvatar,
                            isViewerOwner = currentUserProfile?.isOwner == true && !viewModel.isCurrentUser,
                            canManageRoles = (currentUserProfile?.isOwner == true || currentUserProfile?.isAdmin == true) && !viewModel.isCurrentUser,
                            showMenu = showMenu,
                            onShowMenuChange = { showMenu = it },
                            onBack = onBack,
                            onToggleBanner = {
                                isManualToggleAnimating = true
                                val currentlyExpanded = isBannerExpanded && rawRatio > 0.5f
                                if (currentlyExpanded) {
                                    isBannerExpanded = false
                                    bannerHeightPx = minBannerHeightPx
                                    nestedScrollDispatcher.dispatchPreScroll(
                                        Offset(0f, -400f),
                                        NestedScrollSource.UserInput
                                    )
                                } else {
                                    isBannerExpanded = true
                                    bannerHeightPx = maxBannerHeightPx
                                    nestedScrollDispatcher.dispatchPostScroll(
                                        Offset(0f, 400f),
                                        Offset(0f, 400f),
                                        NestedScrollSource.UserInput
                                    )
                                }
                            },
                            onNavigateToSettings = onNavigateToSettings,
                            onShareProfile = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Check out ${profile.displayName} on Animite! @${profile.username}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "مشاركة الملف الشخصي"))
                            },
                            onPreviewBanner = {
                                if (features.canUseLiveCover && !profile.liveCoverPosterUrl.isNullOrBlank()) {
                                    previewImageUrl = profile.liveCoverPosterUrl
                                } else if (!profile.bannerUrl.isNullOrBlank()) {
                                    previewImageUrl = profile.bannerUrl
                                }
                            },
                            onToggleBlockUser = {
                                if (isBlocked) viewModel.unblockUser() else showBlockConfirmDialog = true
                            },
                            onReportUser = { showReportDialog = true },
                            onManageRoles = { showRoleManagementDialog = true },
                            onPromoteToElite = { showElitePromotionDialog = true },
                            onRevokeElite = { showEliteRevokeDialog = true },
                            onAvatarClick = {
                                if (viewModel.isCurrentUser) {
                                    if (!profile.photoUrl.isNullOrBlank()) {
                                        showAvatarOptionsDialog = true
                                    } else {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                } else {
                                    if (!profile.photoUrl.isNullOrBlank()) {
                                        previewImageUrl = profile.photoUrl
                                    }
                                }
                            },
                            onBannerClick = {
                                if (viewModel.isCurrentUser) {
                                    showCoverOptionsDialog = true
                                } else {
                                    if (features.canUseLiveCover && !profile.liveCoverPosterUrl.isNullOrBlank()) {
                                        previewImageUrl = profile.liveCoverPosterUrl
                                    } else if (!profile.bannerUrl.isNullOrBlank()) {
                                        previewImageUrl = profile.bannerUrl
                                    }
                                }
                            },
                            onEditClick = {
                                onNavigateToEditProfile?.invoke()
                            },
                            onFollowClick = { viewModel.toggleFollow() },
                            onUnfollowClick = { showUnfollowDialog = true },
                            onMessageClick = {
                                onNavigateToChat(profile.uid, profile.displayName, profile.photoUrl)
                            },
                            onFollowersClick = { onNavigateToFollowers(profile.uid) },
                            onFollowingClick = { onNavigateToFollowing(profile.uid) },
                            onOwnerControlCenterClick = if (currentUserProfile?.isOwner == true) onNavigateToOwnerControlCenter else null,
                        )

                        // 2. Sticky Tab Row: stays pinned right under the top area and never scrolls away
                        if ("tabs" in profile.customization.visibleSections) {
                        TabRow(
                            selectedTabIndex = pagerState.currentPage,
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            indicator = { tabPositions ->
                                if (pagerState.currentPage < tabPositions.size) {
                                    androidx.compose.material3.TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                        height = 3.dp,
                                        color = accentColor
                                    )
                                }
                            },
                            divider = {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                    thickness = 0.8.dp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tabRowHeight)
                        ) {
                            profileTabs.forEachIndexed { index, tab ->
                                val isSelected = pagerState.currentPage == index
                                Tab(
                                    selected = isSelected,
                                    onClick = {
                                        scope.launch { pagerState.animateScrollToPage(index) }
                                    },
                                    text = {
                                        Text(
                                            text = androidx.compose.ui.res.stringResource(tab.titleRes),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.onSurface
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                            }
                                        )
                                    }
                                )
                            }
                        }

                        // 3. Tab Content Pager (fills remaining space below sticky Tab Row)
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) { page ->
                            val extraBottomPadding = bottomPadding + 32.dp
                            when (profileTabs[page]) {
                                ProfileTab.POSTS -> PostsTabContent(
                                    posts = topLevelPosts,
                                    profile = profile,
                                    emptyText = if (viewModel.isCurrentUser) {
                                        androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.my_posts_empty)
                                    } else {
                                        androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.no_posts_yet)
                                    },
                                    contentPadding = PaddingValues(bottom = extraBottomPadding),
                                    onLikeClick = if (isBasicTier) viewModel::toggleCommentLike else null,
                                    viewModel = viewModel
                                )

                                ProfileTab.REPLIES -> PostsTabContent(
                                    posts = replies,
                                    profile = profile,
                                    emptyText = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.no_replies_yet),
                                    contentPadding = PaddingValues(bottom = extraBottomPadding),
                                    onLikeClick = if (isBasicTier) viewModel::toggleCommentLike else null,
                                    viewModel = viewModel
                                )

                                ProfileTab.MEDIA -> MediaTabContent(
                                    groups = mediaGroups,
                                    emptyText = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.no_media_yet),
                                    contentPadding = PaddingValues(bottom = extraBottomPadding)
                                )

                                ProfileTab.LIKES -> PostsTabContent(
                                    posts = likedPosts,
                                    profile = profile,
                                    emptyText = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.no_likes_yet),
                                    contentPadding = PaddingValues(bottom = extraBottomPadding),
                                    onLikeClick = viewModel::toggleCommentLike,
                                    viewModel = viewModel
                                )

                                ProfileTab.LIBRARY -> LibraryTabContent(
                                    userId = profile.uid,
                                    stats = animeListStats,
                                    entries = animeListEntries,
                                    isCurrentUser = viewModel.isCurrentUser,
                                    onAnimeClick = { animeId -> onNavigateToAnime?.invoke(animeId) },
                                    onProgressUpdate = { entryId, progress, total ->
                                        viewModel.updateAnimeListProgress(entryId, progress, total)
                                    },
                                    onToggleFavorite = { entryId, isFav ->
                                        viewModel.toggleAnimeListFavorite(entryId, isFav)
                                    },
                                    contentPadding = PaddingValues(bottom = extraBottomPadding)
                                )
                            }
                        }
                        }
                    }
                }
                }
            }
        }
    }

    // Unfollow Confirmation Dialog (Twitter asks before unfollowing)
    if (showUnfollowDialog && user != null) {
        AlertDialog(
            onDismissRequest = { showUnfollowDialog = false },
            title = { Text("Unfollow ${user!!.handle}?") },
            text = { Text("Their posts will no longer show up in your home timeline. You can still view their profile.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.toggleFollow()
                        showUnfollowDialog = false
                    }
                ) {
                    Text("Unfollow", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnfollowDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Block Confirmation Dialog
    if (showBlockConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBlockConfirmDialog = false },
            title = { Text("Block ${user?.displayName}?") },
            text = { Text("They will no longer be able to message you or see your comments, and you will unfollow each other.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.blockUser()
                        showBlockConfirmDialog = false
                    }
                ) {
                    Text("Block", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Report Dialog
    if (showReportDialog) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report User") },
            text = {
                Column {
                    Text("Please explain why you are reporting this account:")
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        placeholder = { Text("e.g. Spam, harassment, inappropriate content...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (reason.isNotBlank()) {
                            viewModel.reportUser(reason)
                            showReportDialog = false
                        }
                    }
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Avatar Options Dialog (for current user: View photo vs Change photo)
    if (showAvatarOptionsDialog && user != null) {
        AlertDialog(
            onDismissRequest = { showAvatarOptionsDialog = false },
            title = { Text(user!!.displayName, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = {
                            showAvatarOptionsDialog = false
                            previewImageUrl = user?.photoUrl
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("عرض الصورة", style = MaterialTheme.typography.bodyLarge)
                    }
                    TextButton(
                        onClick = {
                            showAvatarOptionsDialog = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تغيير الصورة", style = MaterialTheme.typography.bodyLarge)
                    }
                    TextButton(
                        onClick = {
                            showAvatarOptionsDialog = false
                            onNavigateToEditProfile?.invoke()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تعديل الملف الشخصي والمظهر", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAvatarOptionsDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Cover Options Dialog (for current user)
    if (showCoverOptionsDialog && user != null) {
        val ownFeatures = user!!.features
        AlertDialog(
            onDismissRequest = { showCoverOptionsDialog = false },
            title = {
                Text(
                    text = "خيارات غلاف الملف الشخصي",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (isUploadingLiveCover) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            Text(
                                text = if (liveCoverUploadState is LiveCoverUploadState.Validating) "جاري التحقق من الوسائط..." else "جاري رفع الغلاف المتحرك...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        TextButton(
                            onClick = {
                                showCoverOptionsDialog = false
                                if (ownFeatures.canUseLiveCover) {
                                    liveCoverPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                    )
                                } else {
                                    showEliteLockedDialog = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (ownFeatures.canUseLiveCover) Icons.Default.PlayArrow else Icons.Default.Lock,
                                    contentDescription = if (ownFeatures.canUseLiveCover) null else androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.elite_locked_label),
                                    tint = if (ownFeatures.canUseLiveCover) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("تعيين غلاف متحرك (فيديو حتى 30 ثانية أو GIF)", style = MaterialTheme.typography.bodyLarge)
                                    if (!ownFeatures.canUseLiveCover) {
                                        Text(
                                            text = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.elite_locked_label),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        TextButton(
                            onClick = {
                                showCoverOptionsDialog = false
                                bannerPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("تعيين غلاف ثابت (صورة مع اقتصاص 3:1)", style = MaterialTheme.typography.bodyLarge)
                            }
                        }

                        if (ownFeatures.canUseLiveCover && !user!!.liveCoverUrl.isNullOrBlank()) {
                            TextButton(
                                onClick = {
                                    showCoverOptionsDialog = false
                                    viewModel.removeLiveCover()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Text("إزالة الغلاف المتحرك", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        if ((ownFeatures.canUseLiveCover && !user!!.liveCoverPosterUrl.isNullOrBlank()) || !user!!.bannerUrl.isNullOrBlank()) {
                            TextButton(
                                onClick = {
                                    showCoverOptionsDialog = false
                                    previewImageUrl = if (ownFeatures.canUseLiveCover) {
                                        user!!.liveCoverPosterUrl ?: user!!.bannerUrl
                                    } else {
                                        user!!.bannerUrl
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.Fullscreen, contentDescription = null)
                                    Text("عرض صورة الغلاف", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showCoverOptionsDialog = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Elite Locked Feature Explanation Dialog (for regular users)
    if (showEliteLockedDialog) {
        AlertDialog(
            onDismissRequest = { showEliteLockedDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.elite_locked_label),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.elite_locked_dialog_title),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.elite_locked_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { showEliteLockedDialog = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.elite_locked_dialog_dismiss),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // Owner-Only Promote to Elite Confirmation Dialog (with optional Role Badges selection)
    if (showElitePromotionDialog && user != null && currentUserProfile?.isOwner == true) {
        var selectedBadges by remember(user!!.uid) {
            mutableStateOf(user!!.eliteBadges.toSet())
        }
        AlertDialog(
            onDismissRequest = { showElitePromotionDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.elite_promote_dialog_title),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(
                            com.imashnake.animite.social.R.string.elite_promote_dialog_message,
                            user!!.displayName
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.elite_select_badges_label),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        EliteRoleBadge.entries.forEach { badge ->
                            val isSelected = badge.id in selectedBadges
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedBadges = if (isSelected) {
                                            selectedBadges - badge.id
                                        } else {
                                            selectedBadges + badge.id
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = badge.labelAr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    androidx.compose.material3.Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = null
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.promoteToElite(
                            targetUid = user!!.uid,
                            eliteBadges = selectedBadges.toList()
                        )
                        showElitePromotionDialog = false
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.elite_promote_confirm),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showElitePromotionDialog = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.cancel))
                }
            }
        )
    }

    // Owner-Only Revoke Elite Confirmation Dialog
    if (showEliteRevokeDialog && user != null && currentUserProfile?.isOwner == true) {
        AlertDialog(
            onDismissRequest = { showEliteRevokeDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.elite_revoke_dialog_title),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = androidx.compose.ui.res.stringResource(
                        com.imashnake.animite.social.R.string.elite_revoke_dialog_message,
                        user!!.displayName
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.revokeElite(user!!.uid)
                        showEliteRevokeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.elite_revoke_confirm),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEliteRevokeDialog = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.cancel))
                }
            }
        )
    }

    // Role Management Dialog (for Owner / Admin)
    if (showRoleManagementDialog && user != null) {
        var selectedRole by remember { mutableStateOf(user!!.role) }
        var customBadge by remember { mutableStateOf(user!!.customBadgeTitle ?: "") }
        var isVerifiedChecked by remember { mutableStateOf(user!!.isVerified) }

        AlertDialog(
            onDismissRequest = { showRoleManagementDialog = false },
            title = {
                Text(
                    text = "إدارة الرتب والشارات",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "المستخدم: ${user!!.displayName} (@${user!!.username})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Role selector
                    Text("الرتبة:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("user" to "مستخدم", "moderator" to "مشرف", "admin" to "أدمن", "owner" to "مالك").forEach { (roleKey, roleTitle) ->
                            val isSelected = selectedRole.equals(roleKey, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedRole = roleKey }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = roleTitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Custom Badge Title
                    Text("لقب الشارة المخصص (اختياري):", style = MaterialTheme.typography.labelMedium)
                    androidx.compose.material3.OutlinedTextField(
                        value = customBadge,
                        onValueChange = { customBadge = it },
                        placeholder = { Text("مثال: مشرف الفعاليات / المطور") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Verification Checkbox / Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isVerifiedChecked = !isVerifiedChecked },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("شارة التوثيق الزرقاء", style = MaterialTheme.typography.bodyMedium)
                        androidx.compose.material3.Checkbox(
                            checked = isVerifiedChecked,
                            onCheckedChange = { isVerifiedChecked = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUserRole(
                            targetUid = user!!.uid,
                            role = selectedRole,
                            customBadgeTitle = customBadge.trim().ifBlank { null },
                            isVerified = isVerifiedChecked
                        )
                        showRoleManagementDialog = false
                    }
                ) {
                    Text("حفظ التغييرات")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRoleManagementDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Full Screen Image Viewer Modal with Smooth Transitions
    previewImageUrl?.let { imageUrl ->
        FullScreenImageViewer(
            imageUrl = imageUrl,
            onDismiss = { previewImageUrl = null }
        )
    }

    if (showSignOutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.sign_out_confirm_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.sign_out_confirm_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirmDialog = false
                        viewModel.signOut()
                        runCatching {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                            GoogleSignIn.getClient(context, gso).signOut()
                        }
                        if (onBack != null) {
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.sign_out),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirmDialog = false }) {
                    Text(androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.cancel))
                }
            }
        )
    }

    // 3:1 Interactive Banner Crop Dialog
    if (pendingBannerCropUri != null) {
        BannerCropDialog(
            imageUri = pendingBannerCropUri!!,
            onDismiss = { pendingBannerCropUri = null },
            onCropConfirmed = { croppedBytes ->
                pendingBannerCropUri = null
                viewModel.uploadBanner(croppedBytes)
            }
        )
    }
}

@Composable
private fun ProfileHeader(
    profile: AnimiteUser,
    features: EliteFeatures,
    stats: UserAnimeStats,
    ratio: Float,
    coverType: CoverType,
    accentColor: Color,
    isCurrentUser: Boolean,
    isFollowing: Boolean,
    isBlocked: Boolean,
    isUploadingAvatar: Boolean,
    isViewerOwner: Boolean,
    canManageRoles: Boolean,
    showMenu: Boolean,
    onShowMenuChange: (Boolean) -> Unit,
    onBack: (() -> Unit)?,
    onToggleBanner: () -> Unit,
    onNavigateToSettings: (() -> Unit)?,
    onShareProfile: () -> Unit,
    onPreviewBanner: () -> Unit,
    onToggleBlockUser: () -> Unit,
    onReportUser: () -> Unit,
    onManageRoles: () -> Unit,
    onPromoteToElite: () -> Unit,
    onRevokeElite: () -> Unit,
    onAvatarClick: () -> Unit,
    onBannerClick: () -> Unit,
    onEditClick: () -> Unit,
    onFollowClick: () -> Unit,
    onUnfollowClick: () -> Unit,
    onMessageClick: () -> Unit,
    onFollowersClick: () -> Unit,
    onFollowingClick: () -> Unit,
    onOwnerControlCenterClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    val designTier = profile.designTier
    val isBasic = designTier == ProfileDesignTier.BASIC
    // Shared Animite header for every tier; only the avatar size/shape and cover height differ.
    val maxBannerHeight = if (designTier == ProfileDesignTier.PREMIUM) 150.dp else 200.dp
    val minBannerHeight = 76.dp
    val currentBannerHeight = minBannerHeight + (maxBannerHeight - minBannerHeight) * ratio
    val statusBarInsets = WindowInsets.statusBars.union(WindowInsets.displayCutout)

    // Driven by ratio:
    // 1. Banner alpha fades from 1f to 0.35f
    val bannerAlpha = 0.35f + 0.65f * ratio

    val customization = profile.customization
    val canSeeBio = !isBasic || FieldVisibility.fromString(profile.privacy.bioVisibility)
        .isVisibleTo(isCurrentUser, isFollowing)
    val canSeeLocation = !isBasic || FieldVisibility.fromString(profile.privacy.locationVisibility)
        .isVisibleTo(isCurrentUser, isFollowing)
    val canSeeWebsite = !isBasic || FieldVisibility.fromString(profile.privacy.websiteVisibility)
        .isVisibleTo(isCurrentUser, isFollowing)

    // 2. Avatar, tier-aware since the profile now differentiates account ranks:
    //    - regular accounts: a 84.dp circle
    //    - elite accounts: the standard 84.dp x 100.dp square photo card
    //    Elite accounts may still pick the circle/square presets. It fades out and slides up
    //    as ratio goes to 0.
    val avatarIsCircle = designTier != ProfileDesignTier.ELITE || customization.resolvedAvatarShape == AvatarShape.CIRCLE
    val avatarWidth = when (designTier) {
        ProfileDesignTier.BASIC -> 84.dp
        ProfileDesignTier.PREMIUM -> 80.dp
        ProfileDesignTier.ELITE -> 84.dp
    }
    val avatarHeight = if (avatarIsCircle) avatarWidth else 100.dp
    val avatarShapeInstance: androidx.compose.ui.graphics.Shape = when {
        avatarIsCircle -> CircleShape
        customization.resolvedAvatarShape == AvatarShape.SQUARE -> RoundedCornerShape(0.dp)
        else -> RoundedCornerShape(ProfileCustomization.PROFILE_CARD_CORNER_RADIUS.dp)
    }
    val avatarSlideUpPx = with(density) { 40.dp.toPx() }

    // 3. User's name appears in the top bar only after the large name has scrolled out of view
    val topBarNameAlpha = ((0.35f - ratio) / 0.35f).fastCoerceIn(0f, 1f)

    // 4. Manual toggle button rotation (180 degrees when collapsed, ~350ms animation)
    val toggleRotation by animateFloatAsState(
        targetValue = if (ratio > 0.5f) 0f else 180f,
        animationSpec = tween(durationMillis = 350),
        label = "banner_toggle_rotation"
    )

    // Frame: BASIC keeps a quiet matte, PREMIUM gets an accent ring (its "more customised"
    // signal), ELITE keeps the editorial matte under its optional glow.
    val avatarFrameWidth = if (designTier == ProfileDesignTier.PREMIUM) 3.dp else 2.dp
    val avatarFrameColor = if (designTier == ProfileDesignTier.PREMIUM) {
        accentColor.copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.background
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Collapsing Banner Slot (200.dp max -> 76.dp min) + Avatar + Top Bar Controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(currentBannerHeight)
                .clipToBounds()
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            // Media slot (CoverType.Image, CoverType.Gif, CoverType.Video)
            // Pauses video when banner is collapsed (ratio <= 0.25f) and resumes when expanded
            CoverMedia(
                coverType = coverType,
                isOwner = isCurrentUser,
                isCoverVisible = ratio > 0.25f,
                showControls = ratio > 0.55f,
                onCoverClick = onBannerClick,
                onAddCoverClick = if (isCurrentUser) onBannerClick else null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = bannerAlpha }
            )



            // Dark gradient scrim behind top-bar name and @username in collapsed state so they stay readable over the cover
            if (topBarNameAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(minBannerHeight)
                        .align(Alignment.TopCenter)
                        .graphicsLayer { alpha = topBarNameAlpha }
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.scrim.copy(alpha = 0.85f),
                                    MaterialTheme.colorScheme.scrim.copy(alpha = 0.62f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            if (!isBasic) {
            // Avatar on Start (Right edge in RTL), flush with the cover's bottom edge:
            // a circle for premium accounts, the square photo card for elite ones.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp)
                    .size(width = avatarWidth, height = avatarHeight)
                    .graphicsLayer {
                        alpha = ratio
                        translationY = -(1f - ratio) * avatarSlideUpPx
                    }
            ) {
                if (features.canUseAvatarNeonGlow && customization.enableDecorativeEffects) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = 1.08f
                                scaleY = 1.08f
                                alpha = 0.45f
                            }
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = 0.65f),
                                        Color.Transparent
                                    )
                                ),
                                shape = avatarShapeInstance
                            )
                    )
                }

                if (!profile.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = profile.photoUrl,
                        contentDescription = "الصورة الشخصية لـ ${profile.displayName}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(avatarShapeInstance)
                            .background(MaterialTheme.colorScheme.surfaceVariant, avatarShapeInstance)
                            .border(avatarFrameWidth, avatarFrameColor, avatarShapeInstance)
                            .clickable(enabled = !isUploadingAvatar && ratio > 0.2f) {
                                onAvatarClick()
                            }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(avatarShapeInstance)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = 0.85f),
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                ),
                                avatarShapeInstance
                            )
                            .border(avatarFrameWidth, avatarFrameColor, avatarShapeInstance)
                            .clickable(enabled = !isUploadingAvatar && ratio > 0.2f) {
                                onAvatarClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (profile.displayName.firstOrNull()?.uppercase() ?: "K"),
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (isUploadingAvatar) {
                    Surface(
                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f),
                        shape = avatarShapeInstance,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
            }

            // Top Bar Row inside the 76.dp min-height region (Start: Back, Center: Name, End: Toggle & Actions)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(minBannerHeight)
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(statusBarInsets)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Start (Right in RTL): Back button + Collapsed User Name
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = "رجوع",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // User's name appears in the top bar only after the large name has scrolled out of view
                    Column(
                        modifier = Modifier
                            .padding(start = if (onBack == null) 8.dp else 0.dp, end = 8.dp)
                            .graphicsLayer { alpha = topBarNameAlpha }
                    ) {
                        Text(
                            text = profile.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        androidx.compose.runtime.CompositionLocalProvider(
                            androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr
                        ) {
                            Text(
                                text = "@${profile.username.removePrefix("@")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // End (Left in RTL): 1. Banner Toggle, 2. Settings / More Options
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isCurrentUser) {
                        if (onNavigateToSettings != null) {
                            IconButton(
                                onClick = onNavigateToSettings,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Settings,
                                            contentDescription = "الإعدادات",
                                            tint = accentColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Manual Banner Collapse/Expand Toggle Button (48.dp touch target, 180 deg rotation, 350ms)
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            onToggleBanner()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(com.imashnake.animite.social.R.drawable.banner_toggle),
                                    contentDescription = if (ratio > 0.5f) "طي الغلاف" else "توسيع الغلاف",
                                    tint = accentColor,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .graphicsLayer { rotationZ = toggleRotation }
                                )
                            }
                        }
                    }
                    if (isCurrentUser) {
                        if (onNavigateToSettings != null) {
                            IconButton(
                                onClick = onNavigateToSettings,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Settings,
                                            contentDescription = "الإعدادات",
                                            tint = accentColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box {
                            IconButton(
                                onClick = { onShowMenuChange(true) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "المزيد من الخيارات",
                                            tint = accentColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { onShowMenuChange(false) }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("مشاركة الملف الشخصي") },
                                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                    onClick = {
                                        onShowMenuChange(false)
                                        onShareProfile()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("عرض الغلاف") },
                                    leadingIcon = { Icon(Icons.Default.Fullscreen, contentDescription = null) },
                                    onClick = {
                                        onShowMenuChange(false)
                                        onPreviewBanner()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isBlocked) "إلغاء الحظر" else "حظر المستخدم") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    onClick = {
                                        onShowMenuChange(false)
                                        onToggleBlockUser()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("الإبلاغ عن المستخدم") },
                                    leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null) },
                                    onClick = {
                                        onShowMenuChange(false)
                                        onReportUser()
                                    }
                                )
                                if (isViewerOwner && !profile.isOwner) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (profile.isElite) {
                                                    androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.elite_revoke_menu_action)
                                                } else {
                                                    androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.elite_promote_menu_action)
                                                },
                                                fontWeight = FontWeight.Bold,
                                                color = if (profile.isElite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (profile.isElite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        onClick = {
                                            onShowMenuChange(false)
                                            if (profile.isElite) {
                                                onRevokeElite()
                                            } else {
                                                onPromoteToElite()
                                            }
                                        }
                                    )
                                }
                                if (canManageRoles) {
                                    DropdownMenuItem(
                                        text = { Text("إدارة الرتب") },
                                        leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                                        onClick = {
                                            onShowMenuChange(false)
                                            onManageRoles()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. User Identity, Action Buttons & Metadata (Collapses smoothly driven by `ratio`)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isBasic) Modifier else Modifier.clipToBounds())
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val currentHeight = (placeable.height * ratio).roundToInt()
                    layout(placeable.width, currentHeight) {
                        val yOffset = -((1f - ratio) * placeable.height).roundToInt()
                        placeable.placeRelative(0, yOffset)
                    }
                }
                .graphicsLayer {
                    alpha = (ratio * 1.15f).fastCoerceIn(0f, 1f)
                }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (isBasic) {
                // 104.dp circle: half over the cover, half over the identity block.
                val avatarOverlap by animateDpAsState(
                    targetValue = 52.dp * ratio,
                    animationSpec = tween(durationMillis = 80),
                    label = "basic_avatar_overlap"
                )
                val avatarAlpha = (ratio * 1.25f).fastCoerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(avatarOverlap)
                ) {
                    Box(
                        modifier = Modifier
                            .offset(x = 0.dp, y = -avatarOverlap)
                            .requiredSize(104.dp)
                            .graphicsLayer {
                                alpha = avatarAlpha
                                scaleX = 0.72f + (0.28f * ratio)
                                scaleY = 0.72f + (0.28f * ratio)
                                translationY = with(density) { (1f - ratio) * 28.dp.toPx() }
                            }
                            .zIndex(1f)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(4.dp, MaterialTheme.colorScheme.background, CircleShape)
                            .clickable(enabled = !isUploadingAvatar && ratio > 0.2f) { onAvatarClick() }
                    ) {
                        if (!profile.photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = profile.photoUrl,
                                contentDescription = "الصورة الشخصية لـ ${profile.displayName}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(accentColor, MaterialTheme.colorScheme.primaryContainer)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile.displayName.firstOrNull()?.uppercase() ?: "A",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Top Row: Large Display Name on Start (Right in RTL) + Action Buttons on End (Left in RTL)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Large Name & Role Badges on Start
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = profile.displayName,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 21.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .semantics { heading() }
                    )
                    if (features.canShowVerifiedBadge && !profile.isOwner && !profile.isAdmin) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "حساب موثق",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                    if (profile.isPrivate) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "حساب خاص",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action Buttons on End (Left in RTL), keeping only "تعديل الملف الشخصي" for current user
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    if (isCurrentUser) {
                        Box(
                            modifier = Modifier.heightIn(min = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.OutlinedButton(
                                onClick = onEditClick,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.7f)),
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.edit_profile),
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    } else {
                        IconButton(
                            onClick = onMessageClick,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Chat,
                                        contentDescription = "إرسال رسالة",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        FollowToggleButton(
                            isFollowing = isFollowing,
                            onFollowClick = onFollowClick,
                            onUnfollowClick = onUnfollowClick
                        )
                    }
                }
            }

                // @handle and Role / Badge Pills (gated strictly on features.canShowRoleBadges)
                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr
                    ) {
                        Text(
                            text = "@${profile.handle.removePrefix("@")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }

                    if (features.canShowRoleBadges) {
                        profile.displayBadges.forEach { label ->
                            val isOwnerBadge = label == "مالك التطبيق"
                            val pillBg = when {
                                isOwnerBadge -> RoleColors.owner(MaterialTheme.colorScheme).copy(alpha = 0.18f)
                                profile.isModerator -> RoleColors.moderator(MaterialTheme.colorScheme).copy(alpha = 0.18f)
                                else -> RoleColors.premium(MaterialTheme.colorScheme).copy(alpha = 0.18f)
                            }
                            val pillBorder = when {
                                isOwnerBadge -> RoleColors.owner(MaterialTheme.colorScheme).copy(alpha = 0.55f)
                                profile.isModerator -> RoleColors.moderator(MaterialTheme.colorScheme).copy(alpha = 0.55f)
                                else -> RoleColors.premium(MaterialTheme.colorScheme).copy(alpha = 0.55f)
                            }
                            val pillFg = when {
                                isOwnerBadge -> RoleColors.owner(MaterialTheme.colorScheme)
                                profile.isModerator -> RoleColors.moderator(MaterialTheme.colorScheme)
                                else -> RoleColors.premium(MaterialTheme.colorScheme)
                            }
                            val pillIcon = when {
                                isOwnerBadge -> Icons.Default.Security
                            else -> Icons.Default.Star
                        }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = pillBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, pillBorder),
                                modifier = if (isCurrentUser && isOwnerBadge && onOwnerControlCenterClick != null) {
                                    Modifier.clickable { onOwnerControlCenterClick() }
                                } else Modifier
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = pillIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = pillFg
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = pillFg
                                    )
                                }
                            }
                        }
                    }
                }

                // Inline Stats row (Following & Followers)
                Row(
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem(
                        count = profile.followingCount,
                        label = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.following),
                        onClick = onFollowingClick
                    )
                    StatItem(
                        count = profile.followersCount,
                        label = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.followers),
                        onClick = onFollowersClick
                    )
                }

                // Bio: 14sp, onSurfaceVariant color
                if (profile.bio.isNotBlank() && canSeeBio) {
                    Text(
                        text = profile.bio,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Meta row: location, website, joined date
                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!profile.location.isNullOrBlank() && canSeeLocation) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = profile.location!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (!profile.websiteUrl.isNullOrBlank() && canSeeWebsite) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .widthIn(max = 210.dp)
                                .clickable {
                                    runCatching {
                                        val rawUrl = profile.websiteUrl!!.trim()
                                        val formattedUrl = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
                                            "https://$rawUrl"
                                        } else rawUrl
                                        val uri = Uri.parse(formattedUrl)
                                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                    }
                                }
                        ) {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = accentColor
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            androidx.compose.runtime.CompositionLocalProvider(
                                androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr
                            ) {
                                Text(
                                    text = profile.websiteUrl!!.removePrefix("https://").removePrefix("http://"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = accentColor,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Optional personal info — each field follows its own privacy scope
                    val viewerFollowsProfile = isFollowing
                    if (profile.canSeeBirthDate(isCurrentUser, viewerFollowsProfile)) {
                        formatBirthDate(profile.birthDate)?.let { formattedBirthday ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Cake,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = formattedBirthday,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    if (profile.canSeeGender(isCurrentUser, viewerFollowsProfile)) {
                        profile.resolvedGender?.let { resolvedGender ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = resolvedGender.labelAr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = androidx.compose.ui.res.stringResource(
                                com.imashnake.animite.social.R.string.joined,
                                joinedDateText(profile.createdAt)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // Linked AniList account
                if (profile.isAnilistLinked) {
                    Card(
                        onClick = {
                            val browserIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://anilist.co/user/${profile.anilistUsername}")
                            )
                            context.startActivity(browserIntent)
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = SemanticColors.anilistCardSurface(MaterialTheme.colorScheme)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("AL", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.linked_anilist_account),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = profile.anilistUsername.orEmpty(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Text(
                                text = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.view_online),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (profile.isPrivate) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.this_account_is_private),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

            }
        }
    }

@Composable
private fun AnimeStatsCard(
    stats: UserAnimeStats,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SemanticColors.statsCardSurface(MaterialTheme.colorScheme)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = accentColor.copy(alpha = 0.25f)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimeStatMetric(
                count = stats.totalEntries,
                label = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.total_entries),
                accentColor = accentColor
            )
            VerticalMetricDivider()
            AnimeStatMetric(
                count = stats.watchingCount,
                label = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.watching_tab),
                accentColor = StatusColors.watching(MaterialTheme.colorScheme)
            )
            VerticalMetricDivider()
            AnimeStatMetric(
                count = stats.completedCount,
                label = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.completed_tab),
                accentColor = StatusColors.completed(MaterialTheme.colorScheme)
            )
            VerticalMetricDivider()
            AnimeStatMetric(
                count = stats.favoritesCount,
                label = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.favorites_tab),
                accentColor = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun VerticalMetricDivider() {
    Box(
        modifier = Modifier
            .height(28.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    )
}

@Composable
private fun AnimeStatMetric(
    count: Int,
    label: String,
    accentColor: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = formatCount(count),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = accentColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AnimeMiniStat(count: Int, label: String, dotColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun StatItem(count: Int, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Text(
            text = formatCount(count),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Follow pill. While following it is outlined; pressing it previews the
 * destructive "Unfollow" state in the error colour (Twitter's hover
 * behaviour, translated to touch), and tapping it asks for confirmation
 * instead of unfollowing immediately.
 */
@Composable
internal fun FollowToggleButton(
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onUnfollowClick: () -> Unit,
) {
    if (!isFollowing) {
        Button(
            onClick = onFollowClick,
            shape = RoundedCornerShape(24.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.follow),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }
    } else {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        OutlinedButton(
            onClick = onUnfollowClick,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.2.dp,
                if (isPressed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                contentColor = if (isPressed) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        ) {
            Text(
                text = if (isPressed) {
                    androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.unfollow)
                } else {
                    androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.following)
                },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
internal fun PostsTabContent(
    posts: List<Comment>?,
    profile: AnimiteUser,
    emptyText: String,
    contentPadding: PaddingValues = PaddingValues(bottom = 16.dp),
    onLikeClick: ((String) -> Unit)? = null,
    viewModel: UserProfileViewModel,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        when {
            posts == null -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(top = 56.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            posts.isEmpty() -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(top = 56.dp, start = 32.dp, end = 32.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = emptyText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            else -> {
                items(posts, key = { it.id }) { comment ->
                    ProfilePostRow(
                        comment = comment,
                        profile = profile,
                        viewModel = viewModel,
                        onLikeClick = onLikeClick
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfilePostRow(
    comment: Comment,
    profile: AnimiteUser,
    viewModel: UserProfileViewModel,
    onLikeClick: ((String) -> Unit)?,
) {
    val currentUserId = viewModel.currentUid
    val isLiked by remember(comment.id, currentUserId) {
        viewModel.isCommentLiked(comment.id)
    }.collectAsState(initial = false)
    val likeTint by animateColorAsState(
        targetValue = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "profile_post_like_tint"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        AsyncImage(
            model = profile.photoUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = profile.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr
                ) {
                    Text(
                        text = "@${profile.handle.removePrefix("@")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = " · ${relativeTimeText(comment.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            if (comment.displayContent.isNotBlank()) {
                Text(
                    text = comment.displayContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (!comment.mediaUrl.isNullOrBlank()) {
                AsyncImage(
                    model = comment.mediaUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            // Show context pill only when linked to an actual anime or episode (hide "Anime #0")
            if (comment.animeId > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = when {
                            comment.targetType == "episode" && !comment.episodeNumber.isNullOrBlank() ->
                                "الحلقة ${comment.episodeNumber} · أنمي #${comment.animeId}"
                            else -> "أنمي #${comment.animeId}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ChatBubbleOutline,
                        contentDescription = "Replies",
                        modifier = Modifier.size(17.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatCount(comment.replyCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onLikeClick != null) {
                        IconButton(
                            onClick = { onLikeClick(comment.id) },
                            enabled = currentUserId != null,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isLiked) "إزالة الإعجاب" else "إعجاب",
                                modifier = Modifier
                                    .size(17.dp)
                                    .graphicsLayer {
                                        val scale = if (isLiked) 1.08f else 1f
                                        scaleX = scale
                                        scaleY = scale
                                    },
                                tint = likeTint
                            )
                        }
                    } else {
                        Icon(
                            Icons.Default.FavoriteBorder,
                            contentDescription = "Likes",
                            modifier = Modifier.size(17.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatCount(comment.likesCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

internal data class MediaGroup(
    val animeId: Int,
    val postsCount: Int,
    val episodes: List<String>,
    val hasEpisodePosts: Boolean,
)

internal fun groupMediaByAnime(posts: List<Comment>): List<MediaGroup> =
    posts.groupBy { it.animeId }
        .map { (animeId, group) ->
            MediaGroup(
                animeId = animeId,
                postsCount = group.size,
                episodes = group.mapNotNull { it.episodeNumber }.distinct().take(3),
                hasEpisodePosts = group.any { it.targetType == "episode" }
            )
        }
        .sortedByDescending { it.postsCount }

@Composable
internal fun MediaTabContent(
    groups: List<MediaGroup>?,
    emptyText: String,
    contentPadding: PaddingValues = PaddingValues(bottom = 16.dp),
) {
    when {
        groups == null -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(top = 56.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        groups.isEmpty() -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(top = 56.dp, start = 32.dp, end = 32.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = emptyText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(groups, key = { it.animeId }) { group ->
                    MediaTile(group = group)
                }
            }
        }
    }
}

@Composable
private fun MediaTile(group: MediaGroup) {
    val gradientPairs = listOf(
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.secondaryContainer,
    )
    val (start, end) = gradientPairs[kotlin.math.abs(group.animeId) % gradientPairs.size]

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(Brush.linearGradient(listOf(start, end)))
            .padding(8.dp)
    ) {
        if (group.hasEpisodePosts) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Episode posts",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(18.dp)
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Text(
                text = "${group.postsCount}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(
                text = "Anime #${group.animeId}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (group.episodes.isNotEmpty()) {
                Text(
                    text = "Ep. ${group.episodes.joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Twitter-style compact counts: 950, 1.2K, 3.4M. */
internal fun formatCount(count: Int): String = when {
    count >= 1_000_000 -> trimTrailingZero(count / 1_000_000.0) + "M"
    count >= 1_000 -> trimTrailingZero(count / 1_000.0) + "K"
    else -> count.toString()
}

private fun trimTrailingZero(value: Double): String {
    val text = String.format(Locale.US, "%.1f", value)
    return text.removeSuffix(".0")
}

internal fun joinedDateText(createdAtMillis: Long): String =
    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(createdAtMillis))

internal fun relativeTimeText(createdAtMillis: Long): String {
    val diffMillis = System.currentTimeMillis() - createdAtMillis
    val minutes = diffMillis / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "now"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}d"
        days < 365 -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(createdAtMillis))
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(createdAtMillis))
    }
}

/**
 * Premium full-screen image viewer with animated zoom & fade transitions,
 * backdrop scrim, tap to dismiss, and close button.
 */
@Composable
private fun FullScreenImageViewer(
    imageUrl: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            visible = true
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.94f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(280)) + scaleIn(initialScale = 0.82f, animationSpec = tween(280)),
                exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.82f, animationSpec = tween(200))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Expanded view",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { /* Prevent dismissing when clicking the image itself */ }
                    )
                }
            }

            // Close button in top end
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .size(48.dp)
                    .align(Alignment.TopEnd)
            ) {
                IconButton(onClick = onDismiss, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}


// ----------------------------------------------------------------------------
// Helpers & New Tab Composables matching profile_expanded.png
// ----------------------------------------------------------------------------

fun Int.toArabicDigits(): String = this.toString().toArabicDigits()
fun Long.toArabicDigits(): String = this.toString().toArabicDigits()

fun String.toArabicDigits(): String {
    val englishToArabic = mapOf(
        '0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤',
        '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩',
        '.' to '٫'
    )
    return this.map { englishToArabic[it] ?: it }.joinToString("")
}

/**
 * Formats an ISO-8601 `yyyy-MM-dd` birthday into the profile's Arabic style,
 * e.g. `١٥ مارس ١٩٩٩`. Returns null when the value is missing or unparsable.
 */
fun formatBirthDate(isoDate: String?): String? {
    if (isoDate.isNullOrBlank()) return null
    val date = runCatching { java.time.LocalDate.parse(isoDate.trim()) }.getOrNull() ?: return null
    val monthNames = arrayOf(
        "كانون الثاني", "شباط", "آذار", "نيسان", "أيار", "حزيران",
        "تموز", "آب", "أيلول", "تشرين الأول", "تشرين الثاني", "كانون الأول"
    )
    val month = monthNames.getOrElse(date.monthValue - 1) { "" }
    return "${date.dayOfMonth.toArabicDigits()} $month ${date.year.toArabicDigits()}"
}

fun formatArabicDate(timestamp: Long): String {
    if (timestamp <= 0) return "كانون الثاني ٢٠٢٤"
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
    val year = cal.get(java.util.Calendar.YEAR).toArabicDigits()
    val monthNames = arrayOf(
        "كانون الثاني", "شباط", "آذار", "نيسان", "أيار", "حزيران",
        "تموز", "آب", "أيلول", "تشرين الأول", "تشرين الثاني", "كانون الأول"
    )
    val month = monthNames.getOrElse(cal.get(java.util.Calendar.MONTH)) { "" }
    return "$month $year"
}

@Composable
private fun ProfileAboutTabContent(
    bio: String,
    animeListStats: AnimeListStats,
    createdAt: Long,
    birthday: String?,
    location: String?,
    genres: List<String>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Bio Quote Card
        if (bio.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = bio,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        // 2. Three Big Anime Stat Cards in a Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileStatCard(
                title = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.stats_completed_anime),
                value = animeListStats.completed.toArabicDigits(),
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                title = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.stats_days_watched),
                value = String.format(java.util.Locale.ROOT, "%.1f", animeListStats.daysWatched).toArabicDigits(),
                modifier = Modifier.weight(1f)
            )
            ProfileStatCard(
                title = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.stats_mean_score),
                value = if (animeListStats.averageScore > 0f) {
                    String.format(java.util.Locale.ROOT, "%.1f", animeListStats.averageScore).toArabicDigits()
                } else {
                    "—"
                },
                modifier = Modifier.weight(1f)
            )
        }

        // 3. Joined Date & Birthday Card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "انضم في ${formatArabicDate(createdAt)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val effectiveBirthday = birthday?.takeIf { it.isNotBlank() } ?: "١٥ مارس"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cake,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "${androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.birthday_prefix)}$effectiveBirthday",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 4. Favorite Genres Card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.favorite_genres_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val displayGenres = if (genres.isNotEmpty()) genres else listOf("دراما", "نفسي", "غموض", "أكشن")
                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    displayGenres.forEach { genre ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        ) {
                            Text(
                                text = genre,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProfileMangaTabContent(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "لا توجد مانغا مضافة بعد",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileFavouritesTabContent(
    entries: List<com.imashnake.animite.social.model.AnimeListEntry>,
    onAnimeClick: (Int) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val favorites = remember(entries) { entries.filter { it.isFavorite } }

    if (favorites.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "لا توجد عناصر في المفضلة بعد",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(favorites, key = { it.id }) { entry ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.7f)
                        .clickable { onAnimeClick(entry.animeId) }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = entry.animeCoverUrl,
                            contentDescription = entry.animeTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                    )
                                )
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = entry.animeTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
