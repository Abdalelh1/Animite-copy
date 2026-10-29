package com.imashnake.animite.social.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.material3.TabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.imashnake.animite.core.ui.tokens.RoleColors
import com.imashnake.animite.core.ui.tokens.SemanticColors
import com.imashnake.animite.social.R
import com.imashnake.animite.social.model.AnimiteUser
import com.imashnake.animite.social.model.Comment
import com.imashnake.animite.social.model.FieldVisibility


import com.imashnake.animite.social.model.isVisibleTo

import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * High-fidelity Twitter/X-style profile experience tailored for Animite's Basic tier.
 * Features:
 * - Fluid synchronized collapsing header with nested scrolling.
 * - Sticky TabRow that pins neatly under the top bar.
 * - Circular avatar overlapping banner by ~50% with smooth scale/fade animation on scroll.
 * - X layout: Action buttons positioned on the opposite side of the avatar row.
 * - Display name, @handle, role badge, bio, metadata (location, website, birthday, gender, joined),
 *   AniList card, and stats in Twitter order (Following first, then Followers).
 * - Full RTL and Arabic support, leveraging dynamic MaterialTheme tokens.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun BasicProfileContent(
    profile: AnimiteUser,
    viewModel: UserProfileViewModel,
    currentUserProfile: AnimiteUser?,
    allPosts: List<Comment>?,
    topLevelPosts: List<Comment>?,
    replies: List<Comment>?,
    mediaGroups: List<MediaGroup>?,
    likedPosts: List<Comment>?,
    isFollowing: Boolean,
    isBlocked: Boolean,
    isUploadingAvatar: Boolean,
    bottomPadding: Dp,
    accentColor: Color,
    onBack: (() -> Unit)?,
    onNavigateToSettings: (() -> Unit)?,
    onNavigateToEditProfile: (() -> Unit)?,
    onNavigateToChat: (userId: String, displayName: String, avatar: String?) -> Unit,
    onNavigateToFollowers: (userId: String) -> Unit,
    onNavigateToFollowing: (userId: String) -> Unit,
    onShareProfile: () -> Unit,
    onPreviewBanner: () -> Unit,
    onToggleBlockUser: () -> Unit,
    onReportUser: () -> Unit,
    onAvatarClick: () -> Unit,
    onBannerClick: () -> Unit,
    onFollowClick: () -> Unit,
    onUnfollowClick: () -> Unit,
    onOwnerControlCenterClick: (() -> Unit)?,
    showMenu: Boolean,
    onShowMenuChange: (Boolean) -> Unit,
    isViewerOwner: Boolean,
    canManageRoles: Boolean,
    onPromoteToElite: () -> Unit,
    onRevokeElite: () -> Unit,
    onManageRoles: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val statusBarInsets = WindowInsets.statusBars.union(WindowInsets.displayCutout)
    val statusBarHeight = statusBarInsets.asPaddingValues().calculateTopPadding()

    // Dimensions
    val topBarHeight = 56.dp + statusBarHeight
    val topBarHeightPx = with(density) { topBarHeight.toPx() }
    val bannerHeight = 145.dp + statusBarHeight
    val bannerHeightPx = with(density) { bannerHeight.toPx() }
    val tabRowHeight = 48.dp
    val tabRowHeightPx = with(density) { tabRowHeight.toPx() }
    val avatarSize = 82.dp
    val avatarSizePx = with(density) { avatarSize.toPx() }
    val avatarOverlap = 41.dp

    // Measured total scrollable header height (from top of banner to bottom of identity block)
    var headerHeightPx by remember { mutableFloatStateOf(bannerHeightPx + with(density) { 260.dp.toPx() }) }

    // Scrollable range before the TabRow reaches the top bar:
    val maxScrollPx by remember {
        derivedStateOf {
            (headerHeightPx - topBarHeightPx).coerceAtLeast(0f)
        }
    }

    // Scroll offset ranges from 0f (fully expanded) down to -maxScrollPx (fully collapsed)
    var scrollOffsetPx by remember { mutableFloatStateOf(0f) }

    // Collapse fraction: 0f = expanded, 1f = collapsed
    val collapseFraction by remember {
        derivedStateOf {
            if (maxScrollPx > 0f) {
                (-scrollOffsetPx / maxScrollPx).coerceIn(0f, 1f)
            } else 0f
        }
    }

    // Smooth NestedScrollConnection driving the collapsing header
    val nestedScrollConnection = remember(maxScrollPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val deltaY = available.y
                // When scrolling UP (deltaY < 0, finger moves up):
                // Consume scroll to collapse the header first before child list scrolls
                if (deltaY < 0f && maxScrollPx > 0f) {
                    val prevOffset = scrollOffsetPx
                    scrollOffsetPx = (scrollOffsetPx + deltaY).coerceIn(-maxScrollPx, 0f)
                    val consumedY = scrollOffsetPx - prevPrevOffset(prevOffset)
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val deltaY = available.y
                // When scrolling DOWN (deltaY > 0, finger moves down) and child list reached the top:
                // Consume unconsumed scroll to expand the header back
                if (deltaY > 0f && maxScrollPx > 0f) {
                    val prevOffset = scrollOffsetPx
                    scrollOffsetPx = (scrollOffsetPx + deltaY).coerceIn(-maxScrollPx, 0f)
                    val consumedY = scrollOffsetPx - prevOffset
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }

            private fun prevPrevOffset(p: Float) = p
        }
    }

    // Tabs for Basic: Posts, Replies, Media, Likes
    val tabs = remember {
        listOf(ProfileTab.POSTS, ProfileTab.REPLIES, ProfileTab.MEDIA, ProfileTab.LIKES)
    }
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    // Privacy visibility checks
    val isCurrentUser = viewModel.isCurrentUser
    val canSeeBio = FieldVisibility.fromString(profile.privacy.bioVisibility)
        .isVisibleTo(isCurrentUser, isFollowing)
    val canSeeLocation = FieldVisibility.fromString(profile.privacy.locationVisibility)
        .isVisibleTo(isCurrentUser, isFollowing)
    val canSeeWebsite = FieldVisibility.fromString(profile.privacy.websiteVisibility)
        .isVisibleTo(isCurrentUser, isFollowing)
    val postsCount = allPosts?.size ?: 0

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        val screenHeight = maxHeight
        val pagerHeight = (screenHeight - topBarHeight - tabRowHeight).coerceAtLeast(100.dp)

        // 1. Scrollable Container (Banner + Avatar Row + Details + TabRow + Pager)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .offset { IntOffset(0, scrollOffsetPx.roundToInt()) }
        ) {
            // Header Section (Measured dynamically to set maxScrollPx)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { size ->
                        headerHeightPx = size.height.toFloat()
                    }
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { deltaY ->
                            if (maxScrollPx > 0f) {
                                scrollOffsetPx = (scrollOffsetPx + deltaY).coerceIn(-maxScrollPx, 0f)
                            }
                        }
                    )
            ) {
                // A. Full-Bleed Cover Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bannerHeight)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .clipToBounds()
                        .clickable(onClick = onBannerClick)
                ) {
                    if (!profile.bannerUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = profile.bannerUrl,
                            contentDescription = stringResource(R.string.change_cover),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    // Subtle parallax translation
                                    translationY = -scrollOffsetPx * 0.35f
                                }
                        )
                    } else {
                        // Brand gradient fallback
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            accentColor.copy(alpha = 0.85f),
                                            MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                )
                        )
                    }

                    // Scrim gradient for contrast against top bar icons
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(topBarHeight + 20.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.45f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                // B. Avatar & Action Buttons Row (Twitter/X layout)
                // The avatar overlaps the banner bottom edge by ~50% (avatarOverlap)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Start (Right in RTL): Circular Avatar overlapping the banner
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(y = -avatarOverlap)
                            .size(avatarSize)
                            .graphicsLayer {
                                // Smooth scale down & dissolve as header approaches top bar
                                val scale = (1f - 0.4f * collapseFraction).coerceIn(0.6f, 1f)
                                scaleX = scale
                                scaleY = scale
                                alpha = ((0.85f - collapseFraction) / 0.35f).coerceIn(0f, 1f)
                            }
                            .zIndex(2f)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(3.5.dp, MaterialTheme.colorScheme.background, CircleShape)
                            .clickable(enabled = !isUploadingAvatar) { onAvatarClick() }
                    ) {
                        if (!profile.photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = profile.photoUrl,
                                contentDescription = profile.displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            // Monogram avatar fallback
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

                        if (isUploadingAvatar) {
                            Surface(
                                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f),
                                shape = CircleShape,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.5.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }

                    // End (Left in RTL): Action Buttons (Aligned with avatar row, below banner)
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isCurrentUser) {
                            OutlinedButton(
                                onClick = { onNavigateToEditProfile?.invoke() },
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.edit_profile),
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        } else {
                            // Message Icon Button
                            IconButton(
                                onClick = {
                                    onNavigateToChat(profile.uid, profile.displayName, profile.photoUrl)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Chat,
                                            contentDescription = "Chat",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }

                            // Follow / Following Toggle Button
                            FollowToggleButton(
                                isFollowing = isFollowing,
                                onFollowClick = onFollowClick,
                                onUnfollowClick = onUnfollowClick
                            )
                        }
                    }
                }

                // C. Identity & Details Block
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = (avatarSize - avatarOverlap) - 34.dp, bottom = 12.dp)
                ) {
                    // 1. Display Name + Verified Badge + Private Lock
                    Row(
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
                            modifier = Modifier.semantics { heading() }
                        )

                        if (profile.isVerified) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Verified",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        if (profile.isPrivate) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = stringResource(R.string.private_account),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 2. @Handle (always LTR)
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = "@${profile.handle.removePrefix("@")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }

                    // Role / Custom Badges (if any)
                    if (profile.features.canShowRoleBadges && profile.displayBadges.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            profile.displayBadges.forEach { label ->
                                val isOwnerBadge = label == "مالك التطبيق"
                                val pillBg = when {
                                    isOwnerBadge -> RoleColors.owner(MaterialTheme.colorScheme).copy(alpha = 0.18f)
                                    profile.isModerator -> RoleColors.moderator(MaterialTheme.colorScheme).copy(alpha = 0.18f)
                                    else -> RoleColors.premium(MaterialTheme.colorScheme).copy(alpha = 0.18f)
                                }
                                val pillFg = when {
                                    isOwnerBadge -> RoleColors.owner(MaterialTheme.colorScheme)
                                    profile.isModerator -> RoleColors.moderator(MaterialTheme.colorScheme)
                                    else -> RoleColors.premium(MaterialTheme.colorScheme)
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = pillBg,
                                    border = BorderStroke(1.dp, pillFg.copy(alpha = 0.45f)),
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
                                            imageVector = if (isOwnerBadge) Icons.Default.Security else Icons.Default.Star,
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

                    // 3. Bio
                    if (profile.bio.isNotBlank() && canSeeBio) {
                        Text(
                            text = profile.bio,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.5.sp, lineHeight = 20.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // 4. Metadata FlowRow (Location, Website, Birthday, Gender, Joined)
                    FlowRow(
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
                                    .widthIn(max = 220.dp)
                                    .clickable {
                                        runCatching {
                                            val rawUrl = profile.websiteUrl!!.trim()
                                            val formattedUrl = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
                                                "https://$rawUrl"
                                            } else rawUrl
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl)))
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
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
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

                        val viewerFollows = isFollowing
                        if (profile.canSeeBirthDate(isCurrentUser, viewerFollows)) {
                            formatBirthDate(profile.birthDate)?.let { birthday ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Cake,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = birthday,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        if (profile.canSeeGender(isCurrentUser, viewerFollows)) {
                            profile.resolvedGender?.let { gender ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = gender.labelAr,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Joined Date
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = stringResource(R.string.joined, joinedDateText(profile.createdAt)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    // 5. Following & Followers Stats Row (Twitter/X format)
                    Row(
                        modifier = Modifier.padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(
                            count = profile.followingCount,
                            label = stringResource(R.string.following),
                            onClick = { onNavigateToFollowing(profile.uid) }
                        )
                        StatItem(
                            count = profile.followersCount,
                            label = stringResource(R.string.followers),
                            onClick = { onNavigateToFollowers(profile.uid) }
                        )
                    }

                    // 6. Linked AniList account card (if available)
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
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "AL",
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = stringResource(R.string.linked_anilist_account),
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
                                    text = stringResource(R.string.view_online),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // 7. Private account banner (if private)
                    if (profile.isPrivate) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.this_account_is_private),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // D. Sticky TabRow (Pins neatly at topBarHeight as the header collapses)
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tabRowHeight)
            ) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    indicator = { tabPositions ->
                        if (pagerState.currentPage < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
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
                    modifier = Modifier.fillMaxSize()
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val isSelected = pagerState.currentPage == index
                        Tab(
                            selected = isSelected,
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(index) }
                            },
                            text = {
                                Text(
                                    text = stringResource(tab.titleRes),
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
            }

            // E. Tab Content Pager (Posts, Replies, Media, Likes)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(pagerHeight)
            ) { page ->
                val extraBottomPadding = bottomPadding + 32.dp
                when (tabs[page]) {
                    ProfileTab.POSTS -> PostsTabContent(
                        posts = topLevelPosts,
                        profile = profile,
                        emptyText = if (isCurrentUser) {
                            stringResource(R.string.my_posts_empty)
                        } else {
                            stringResource(R.string.no_posts_yet)
                        },
                        contentPadding = PaddingValues(bottom = extraBottomPadding),
                        onLikeClick = viewModel::toggleCommentLike,
                        viewModel = viewModel
                    )

                    ProfileTab.REPLIES -> PostsTabContent(
                        posts = replies,
                        profile = profile,
                        emptyText = stringResource(R.string.no_replies_yet),
                        contentPadding = PaddingValues(bottom = extraBottomPadding),
                        onLikeClick = viewModel::toggleCommentLike,
                        viewModel = viewModel
                    )

                    ProfileTab.MEDIA -> MediaTabContent(
                        groups = mediaGroups,
                        emptyText = stringResource(R.string.no_media_yet),
                        contentPadding = PaddingValues(bottom = extraBottomPadding)
                    )

                    ProfileTab.LIKES -> PostsTabContent(
                        posts = likedPosts,
                        profile = profile,
                        emptyText = stringResource(R.string.no_likes_yet),
                        contentPadding = PaddingValues(bottom = extraBottomPadding),
                        onLikeClick = viewModel::toggleCommentLike,
                        viewModel = viewModel
                    )

                    else -> Unit
                }
            }
        }

        // 2. Compressed Collapsing Top Bar (Always pinned at top; fades in title and surface background)
        val topBarBgAlpha = (collapseFraction * 1.5f - 0.5f).coerceIn(0f, 1f)
        val topBarTitleAlpha = ((collapseFraction - 0.55f) / 0.45f).coerceIn(0f, 1f)

        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = topBarBgAlpha),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .height(topBarHeight)
                .zIndex(10f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .windowInsetsPadding(statusBarInsets)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Start (Right in RTL): Back Button + Collapsed Profile Title
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
                                    color = MaterialTheme.colorScheme.surfaceContainer
                                        .copy(alpha = (1f - topBarBgAlpha) * 0.75f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                            contentDescription = stringResource(R.string.back),
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Collapsed Title (Display Name + Post Count)
                        Column(
                            modifier = Modifier
                                .padding(start = if (onBack == null) 8.dp else 2.dp, end = 8.dp)
                                .graphicsLayer {
                                    alpha = topBarTitleAlpha
                                    translationY = (1f - topBarTitleAlpha) * with(density) { 8.dp.toPx() }
                                }
                        ) {
                            Text(
                                text = profile.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$postsCount ${stringResource(R.string.posts)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    // End (Left in RTL): Settings / Menu Options
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
                                        color = MaterialTheme.colorScheme.surfaceContainer
                                            .copy(alpha = (1f - topBarBgAlpha) * 0.75f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.Settings,
                                                contentDescription = "Settings",
                                                tint = accentColor,
                                                modifier = Modifier.size(19.dp)
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
                                        color = MaterialTheme.colorScheme.surfaceContainer
                                            .copy(alpha = (1f - topBarBgAlpha) * 0.75f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Menu",
                                                tint = accentColor,
                                                modifier = Modifier.size(19.dp)
                                            )
                                        }
                                    }
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { onShowMenuChange(false) }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.share_profile)) },
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
                                        text = { Text(if (isBlocked) stringResource(R.string.unblock_user) else stringResource(R.string.block_user)) },
                                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                        onClick = {
                                            onShowMenuChange(false)
                                            onToggleBlockUser()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.report_user)) },
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
                                                        stringResource(R.string.elite_revoke_menu_action)
                                                    } else {
                                                        stringResource(R.string.elite_promote_menu_action)
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
                                                if (profile.isElite) onRevokeElite() else onPromoteToElite()
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

                // Subtle bottom border/divider on TopBar when collapsed
                if (topBarBgAlpha > 0.8f) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        thickness = 0.8.dp
                    )
                }
            }
        }
    }
}
