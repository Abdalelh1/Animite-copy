package com.imashnake.animite.social.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/**
 * Media types supported by the profile cover slot ([CoverMedia]).
 * Allows rendering still images, animated images (GIF/WebP), or video
 * without changing any layout code in the profile screen.
 */
sealed class CoverType {
    data class Image(val url: String?) : CoverType()
    data class Gif(val url: String, val posterUrl: String? = null) : CoverType()
    data class Video(val url: String, val posterUrl: String? = null) : CoverType()
}

/**
 * Resolves a [CoverType] from user profile cover fields and [canUseLiveCover] (from `EliteFeatures`).
 * When [canUseLiveCover] is false (regular tier or demoted user), always falls back to static [bannerUrl].
 */
fun resolveCoverType(
    liveCoverUrl: String?,
    mediaType: String?,
    posterUrl: String?,
    bannerUrl: String?,
    canUseLiveCover: Boolean = true,
): CoverType {
    if (canUseLiveCover && !liveCoverUrl.isNullOrBlank()) {
        val isVideo = mediaType == "video/mp4" || liveCoverUrl.contains(".mp4", ignoreCase = true)
        val isGif = mediaType == "image/gif" ||
            mediaType == "image/webp" ||
            liveCoverUrl.contains(".gif", ignoreCase = true) ||
            liveCoverUrl.contains(".webp", ignoreCase = true)
        return when {
            isVideo -> CoverType.Video(url = liveCoverUrl, posterUrl = posterUrl ?: bannerUrl)
            isGif -> CoverType.Gif(url = liveCoverUrl, posterUrl = posterUrl ?: bannerUrl)
            else -> CoverType.Image(url = liveCoverUrl)
        }
    }
    return CoverType.Image(url = bannerUrl)
}

/**
 * Unified media slot composable for the profile cover banner.
 * Renders [CoverType.Image], [CoverType.Gif], or [CoverType.Video] seamlessly.
 */
@Composable
fun CoverMedia(
    coverType: CoverType,
    modifier: Modifier = Modifier,
    isOwner: Boolean = false,
    isCoverVisible: Boolean = true,
    showControls: Boolean = true,
    onCoverClick: () -> Unit = {},
    onAddCoverClick: (() -> Unit)? = null,
) {
    when (coverType) {
        is CoverType.Image -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onCoverClick
                    )
            ) {
                if (!coverType.url.isNullOrBlank()) {
                    AsyncImage(
                        model = coverType.url,
                        contentDescription = "صورة غلاف الملف الشخصي",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f),
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            )
                    )
                }
            }
        }

        is CoverType.Gif -> {
            LiveCoverPlayer(
                liveCoverUrl = coverType.url,
                posterUrl = coverType.posterUrl,
                mediaType = "image/gif",
                fallbackBannerUrl = coverType.posterUrl,
                isOwner = isOwner,
                isCoverVisible = isCoverVisible,
                showControls = showControls,
                onCoverClick = onCoverClick,
                onAddCoverClick = onAddCoverClick,
                modifier = modifier
            )
        }

        is CoverType.Video -> {
            LiveCoverPlayer(
                liveCoverUrl = coverType.url,
                posterUrl = coverType.posterUrl,
                mediaType = "video/mp4",
                fallbackBannerUrl = coverType.posterUrl,
                isOwner = isOwner,
                isCoverVisible = isCoverVisible,
                showControls = showControls,
                onCoverClick = onCoverClick,
                onAddCoverClick = onAddCoverClick,
                modifier = modifier
            )
        }
    }
}
