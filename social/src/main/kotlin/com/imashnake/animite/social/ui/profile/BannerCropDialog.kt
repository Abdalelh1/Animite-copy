package com.imashnake.animite.social.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Interactive Image Cropping and Repositioning Dialog.
 *
 * Supports both:
 * - 3:1 aspect ratio framing viewport for Cover Banner (`aspectRatio = 3f`, `showAvatarOverlay = true`)
 * - 1:1 / custom aspect ratio framing viewport for Profile Avatar (`aspectRatio = 1f`, `showAvatarOverlay = false`)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BannerCropDialog(
    imageUri: Uri,
    onDismiss: () -> Unit,
    onCropConfirmed: (ByteArray) -> Unit,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 3f,
    title: String = "ضبط أبعاد وموضع الغلاف",
    showAvatarOverlay: Boolean = true,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Transform state (hoisted so TopAppBar Apply button uses exact user pan & zoom)
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var activeViewportWidthPx by remember { mutableFloatStateOf(1080f) }
    var activeViewportHeightPx by remember { mutableFloatStateOf(1080f / aspectRatio) }

    // Load source bitmap asynchronously with high fidelity
    LaunchedEffect(imageUri) {
        isLoading = true
        withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(imageUri)?.use { stream ->
                    val options = BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    BitmapFactory.decodeStream(stream, null, options)
                }
            }.getOrNull()
        }?.let { bmp ->
            sourceBitmap = bmp
        }
        isLoading = false
    }

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isSaving,
            dismissOnClickOutside = false,
        )
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onDismiss,
                            enabled = !isSaving,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع"
                            )
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                val bmp = sourceBitmap ?: return@Button
                                isSaving = true
                                val currentScale = scale
                                val currentOffsetX = offsetX
                                val currentOffsetY = offsetY
                                val vwPx = activeViewportWidthPx
                                val vhPx = activeViewportHeightPx
                                scope.launch(Dispatchers.IO) {
                                    val croppedBytes = cropAndEncodeImage(
                                        source = bmp,
                                        scale = currentScale,
                                        offsetX = currentOffsetX,
                                        offsetY = currentOffsetY,
                                        viewportWidthPx = vwPx,
                                        viewportHeightPx = vhPx
                                    )
                                    withContext(Dispatchers.Main) {
                                        isSaving = false
                                        onCropConfirmed(croppedBytes)
                                    }
                                }
                            },
                            enabled = !isLoading && !isSaving && sourceBitmap != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text("تطبيق", fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                if (isLoading || sourceBitmap == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "جاري تحميل الصورة بجودة عالية...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    val bitmap = sourceBitmap!!

                    // Container area
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.scrim),
                        contentAlignment = Alignment.Center
                    ) {
                        val viewportWidth = if (aspectRatio <= 1.2f) maxWidth * 0.8f else maxWidth
                        val viewportHeight = viewportWidth / aspectRatio
                        val density = LocalDensity.current

                        val viewportWidthPx = with(density) { viewportWidth.toPx() }
                        val viewportHeightPx = with(density) { viewportHeight.toPx() }
                        activeViewportWidthPx = viewportWidthPx
                        activeViewportHeightPx = viewportHeightPx

                        // Base scale so that image fills viewport at min zoom
                        val baseScale = max(
                            viewportWidthPx / bitmap.width.toFloat(),
                            viewportHeightPx / bitmap.height.toFloat()
                        )

                        val currentScaledWidth = bitmap.width * baseScale * scale
                        val currentScaledHeight = bitmap.height * baseScale * scale

                        // Clamp offsets so image cannot be dragged outside viewport boundaries
                        val maxOffsetX = max(0f, (currentScaledWidth - viewportWidthPx) / 2f)
                        val maxOffsetY = max(0f, (currentScaledHeight - viewportHeightPx) / 2f)

                        fun clampOffsets(newX: Float, newY: Float) {
                            offsetX = newX.coerceIn(-maxOffsetX, maxOffsetX)
                            offsetY = newY.coerceIn(-maxOffsetY, maxOffsetY)
                        }

                        val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)

                        // Framing Window Box
                        Box(
                            modifier = Modifier
                                .size(width = viewportWidth, height = viewportHeight)
                                .clipToBounds()
                                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                                .pointerInput(scale, baseScale) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(1f, 3.5f)
                                        val newX = offsetX + pan.x
                                        val newY = offsetY + pan.y
                                        clampOffsets(newX, newY)
                                    }
                                }
                        ) {
                            // The Image being transformed
                            androidx.compose.foundation.Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = title,
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier
                                    .size(
                                        width = with(density) { (bitmap.width * baseScale).toDp() },
                                        height = with(density) { (bitmap.height * baseScale).toDp() }
                                    )
                                    .align(Alignment.Center)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        translationX = offsetX
                                        translationY = offsetY
                                    }
                            )

                            // Rule-of-thirds grid overlay
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val thirdW = size.width / 3f
                                val thirdH = size.height / 3f
                                val stroke = Stroke(width = 1f)

                                drawLine(gridColor, Offset(thirdW, 0f), Offset(thirdW, size.height), stroke.width)
                                drawLine(gridColor, Offset(thirdW * 2f, 0f), Offset(thirdW * 2f, size.height), stroke.width)
                                drawLine(gridColor, Offset(0f, thirdH), Offset(size.width, thirdH), stroke.width)
                                drawLine(gridColor, Offset(0f, thirdH * 2f), Offset(size.width, thirdH * 2f), stroke.width)
                            }

                            if (showAvatarOverlay) {
                                // Avatar cutout overlay preview (shows where the avatar will overlap the banner)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .offset(x = 18.dp, y = 30.dp)
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f))
                                        .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), RoundedCornerShape(20.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "الصورة",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Controls Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Instruction & Reset
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Crop,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (aspectRatio > 1.5f) "اسحب وحرّك للتكبير والقص بنسبة 3:1" else "اسحب وحرّك لضبط إطار الصورة الشخصية",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        scale = 1f
                                        offsetX = 0f
                                        offsetY = 0f
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "إعادة تعيين",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("افتراضي", fontSize = 12.sp)
                                }
                            }

                            // Zoom Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomOut,
                                    contentDescription = "تصغير",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )

                                Slider(
                                    value = scale,
                                    onValueChange = { newScale ->
                                        scale = newScale
                                        val vw = activeViewportWidthPx
                                        val vh = activeViewportHeightPx
                                        val baseScale = max(
                                            vw / bitmap.width.toFloat(),
                                            vh / bitmap.height.toFloat()
                                        )
                                        val currentScaledWidth = bitmap.width * baseScale * scale
                                        val currentScaledHeight = bitmap.height * baseScale * scale
                                        val maxOffsetX = max(0f, (currentScaledWidth - vw) / 2f)
                                        val maxOffsetY = max(0f, (currentScaledHeight - vh) / 2f)
                                        offsetX = offsetX.coerceIn(-maxOffsetX, maxOffsetX)
                                        offsetY = offsetY.coerceIn(-maxOffsetY, maxOffsetY)
                                    },
                                    valueRange = 1f..3.5f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = "تكبير",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Crops the bitmap to the target aspect ratio respecting the user's exact pan (offsetX, offsetY)
 * and zoom (scale) inside the viewport, preserving maximum resolution and quality.
 */
private fun cropAndEncodeImage(
    source: Bitmap,
    scale: Float = 1f,
    offsetX: Float = 0f,
    offsetY: Float = 0f,
    viewportWidthPx: Float = 1080f,
    viewportHeightPx: Float = 360f,
): ByteArray {
    val baseScale = max(
        viewportWidthPx / source.width.toFloat(),
        viewportHeightPx / source.height.toFloat()
    )
    val totalScale = (baseScale * scale).coerceAtLeast(0.0001f)

    val cropWidth = (viewportWidthPx / totalScale).roundToInt().coerceIn(1, source.width)
    val cropHeight = (viewportHeightPx / totalScale).roundToInt().coerceIn(1, source.height)

    val centerX = (source.width / 2f) - (offsetX / totalScale)
    val centerY = (source.height / 2f) - (offsetY / totalScale)

    val startX = (centerX - cropWidth / 2f).roundToInt().coerceIn(0, (source.width - cropWidth).coerceAtLeast(0))
    val startY = (centerY - cropHeight / 2f).roundToInt().coerceIn(0, (source.height - cropHeight).coerceAtLeast(0))

    val cropped = Bitmap.createBitmap(source, startX, startY, cropWidth, cropHeight)
    val stream = ByteArrayOutputStream()
    // 95% JPEG preserves high detail without unnecessary pixel degradation
    cropped.compress(Bitmap.CompressFormat.JPEG, 95, stream)
    return stream.toByteArray()
}
