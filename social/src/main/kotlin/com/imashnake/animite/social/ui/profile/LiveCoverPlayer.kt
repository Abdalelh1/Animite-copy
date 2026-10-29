package com.imashnake.animite.social.ui.profile

import android.content.Context
import android.graphics.Matrix
import android.net.ConnectivityManager
import android.provider.Settings
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import coil3.compose.AsyncImage

/**
 * Premium, lifecycle-aware Live Cover Player supporting MP4 videos, animated GIFs,
 * and high-fidelity poster fallbacks with smooth crossfade and responsive 3:1 ratio.
 */
@OptIn(UnstableApi::class)
@Composable
fun LiveCoverPlayer(
    liveCoverUrl: String?,
    posterUrl: String?,
    mediaType: String?,
    fallbackBannerUrl: String?,
    isOwner: Boolean,
    modifier: Modifier = Modifier,
    isCoverVisible: Boolean = true,
    showControls: Boolean = true,
    parallaxOffset: Float = 0f,
    onCoverClick: () -> Unit = {},
    onAddCoverClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val isVideo = remember(liveCoverUrl, mediaType) {
        mediaType == "video/mp4" || (liveCoverUrl != null && liveCoverUrl.contains(".mp4", ignoreCase = true))
    }
    val isGif = remember(liveCoverUrl, mediaType) {
        mediaType == "image/gif" || (liveCoverUrl != null && liveCoverUrl.contains(".gif", ignoreCase = true))
    }
    val hasLiveCover = !liveCoverUrl.isNullOrBlank()

    // System Data-Saver and Reduced-Motion detection
    val isDataSaverActive = remember(context) { checkDataSaver(context) }
    val isReducedMotionActive = remember(context) { checkReducedMotion(context) }

    // Playback state
    var isPlaying by remember { mutableStateOf(!isDataSaverActive && !isReducedMotionActive) }
    var isMuted by remember { mutableStateOf(true) }
    var isVideoBuffering by remember { mutableStateOf(false) }
    var isVideoReady by remember { mutableStateOf(false) }
    var hasPlaybackError by remember { mutableStateOf(false) }
    var videoWidth by remember { mutableIntStateOf(0) }
    var videoHeight by remember { mutableIntStateOf(0) }

    // ExoPlayer management
    val exoPlayer = remember(context, liveCoverUrl, isVideo) {
        if (isVideo && !liveCoverUrl.isNullOrBlank()) {
            ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_ALL
                volume = 0f
                setMediaItem(MediaItem.fromUri(liveCoverUrl))
                prepare()
            }
        } else {
            null
        }
    }

    // Attach ExoPlayer listeners
    DisposableEffect(exoPlayer) {
        val player = exoPlayer ?: return@DisposableEffect onDispose {}

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        isVideoBuffering = true
                    }
                    Player.STATE_READY -> {
                        isVideoBuffering = false
                        isVideoReady = true
                        hasPlaybackError = false
                    }
                    Player.STATE_ENDED -> {
                        isVideoBuffering = false
                    }
                    Player.STATE_IDLE -> {}
                }
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    videoWidth = videoSize.width
                    videoHeight = videoSize.height
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                isVideoBuffering = false
                hasPlaybackError = true
            }
        }

        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    // Play/Pause control based on visibility, user preference, and screen lifecycle
    LaunchedEffect(exoPlayer, isPlaying, isCoverVisible, isMuted) {
        exoPlayer?.let { player ->
            val shouldPlay = isPlaying && isCoverVisible
            player.playWhenReady = shouldPlay
            if (shouldPlay) {
                player.play()
            } else {
                player.pause()
            }
            player.volume = if (isMuted) 0f else 1f
        }
    }

    // Observe Activity / Screen lifecycle to pause media in background
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val player = exoPlayer ?: return@DisposableEffect onDispose {}

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    player.playWhenReady = false
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (isPlaying && isCoverVisible) {
                        player.playWhenReady = true
                    }
                }
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Subtle Live indicator pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "LivePulse")
    val livePulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LivePulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .graphicsLayer {
                translationY = parallaxOffset * 0.35f
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCoverClick() }
    ) {
        // LAYER 1: Base static fallback / Static banner / Gradient
        val effectivePoster = if (hasLiveCover) (posterUrl ?: fallbackBannerUrl) else fallbackBannerUrl
        if (!effectivePoster.isNullOrBlank()) {
            AsyncImage(
                model = effectivePoster,
                contentDescription = "Profile Cover Poster",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (!hasLiveCover) {
            // Modern Anime Glow Gradient fallback
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

        // LAYER 2: Animated Media (GIF or MP4 Video via TextureView for clean Compose clipping & scrolling)
        if (hasLiveCover && !hasPlaybackError) {
            if (isGif) {
                AsyncImage(
                    model = liveCoverUrl,
                    contentDescription = "Animated Live Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (isVideo && exoPlayer != null) {
                AndroidView(
                    factory = { ctx ->
                        TextureView(ctx).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            exoPlayer.setVideoTextureView(this)
                            addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
                                val tv = v as? TextureView ?: return@addOnLayoutChangeListener
                                val vw = exoPlayer.videoSize.width.takeIf { it > 0 } ?: videoWidth
                                val vh = exoPlayer.videoSize.height.takeIf { it > 0 } ?: videoHeight
                                applyCenterCropTransform(tv, vw, vh)
                            }
                        }
                    },
                    update = { textureView ->
                        val vw = exoPlayer.videoSize.width.takeIf { it > 0 } ?: videoWidth
                        val vh = exoPlayer.videoSize.height.takeIf { it > 0 } ?: videoHeight
                        applyCenterCropTransform(textureView, vw, vh)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .graphicsLayer {
                            alpha = if (isVideoReady) 1f else 0f
                        }
                )
            }
        }

        // LAYER 3: Tasteful loading indicator during buffering
        AnimatedVisibility(
            visible = isVideoBuffering,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(250)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                }
            }
        }

        // LAYER 4: Subtle Scrim Gradients for Readability (without washing out or hiding the banner)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.42f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.35f)
                        )
                    )
                )
        )

        // Subtle bottom border line separating banner from profile content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.BottomCenter)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        )

        // LAYER 5: Live Cover Badge & Controls (Positioned cleanly at Bottom-End corner of banner)
        if (hasLiveCover && showControls) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (showControls) {
                    // Play / Pause Toggle Button (48.dp touch target)
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "إيقاف الغلاف المتحرك مؤقتاً" else "تشغيل الغلاف المتحرك",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Audio Mute / Unmute Button (for videos, 48.dp touch target)
                    if (isVideo) {
                        IconButton(
                            onClick = { isMuted = !isMuted },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = if (isMuted) "تشغيل صوت الغلاف" else "كتم صوت الغلاف",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // "LIVE" Pill Indicator
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f),
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .height(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isPlaying) MaterialTheme.colorScheme.tertiary.copy(alpha = livePulseAlpha)
                                    else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                                )
                        )
                        Text(
                            text = "LIVE",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Applies a center-crop (ContentScale.Crop) matrix transformation to a [TextureView]
 * so the video fills the 3:1 banner container without stretching or leaving black bars.
 */
private fun applyCenterCropTransform(
    textureView: TextureView,
    videoWidth: Int,
    videoHeight: Int,
) {
    val viewWidth = textureView.width.toFloat()
    val viewHeight = textureView.height.toFloat()
    if (viewWidth <= 0f || viewHeight <= 0f || videoWidth <= 0 || videoHeight <= 0) return

    val viewAspect = viewWidth / viewHeight
    val videoAspect = videoWidth.toFloat() / videoHeight.toFloat()

    val scaleX: Float
    val scaleY: Float
    if (videoAspect > viewAspect) {
        scaleX = videoAspect / viewAspect
        scaleY = 1f
    } else {
        scaleX = 1f
        scaleY = viewAspect / videoAspect
    }

    val matrix = Matrix()
    matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
    textureView.setTransform(matrix)
}

/**
 * Checks whether Android Data Saver or metered network constraints are active.
 */
private fun checkDataSaver(context: Context): Boolean {
    return runCatching {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (cm != null && cm.isActiveNetworkMetered) {
            cm.restrictBackgroundStatus == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED
        } else {
            false
        }
    }.getOrDefault(false)
}

/**
 * Checks whether the user has enabled Reduced Motion in Android Accessibility Settings.
 */
private fun checkReducedMotion(context: Context): Boolean {
    return runCatching {
        val durationScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f
        )
        durationScale == 0f
    }.getOrDefault(false)
}
