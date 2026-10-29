package com.imashnake.animite.social.ui.profile

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.imashnake.animite.social.R
import com.imashnake.animite.social.model.AvatarShape
import com.imashnake.animite.social.model.EliteFeatures
import com.imashnake.animite.social.model.FieldVisibility
import com.imashnake.animite.social.model.ProfileCustomization
import com.imashnake.animite.social.model.ProfilePrivacy
import com.imashnake.animite.social.model.ThemePreset
import com.imashnake.animite.social.validation.ProfileValidator
import com.imashnake.animite.social.validation.ValidationResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Unified, single-page Edit Profile & Appearance Customization screen.
 *
 * Combines:
 * - Real-time Live Preview at the top (CoverMedia + Avatar with custom shape/border/glow + Name + @username + Theme/Accent)
 * - Cover management (Image with 3:1 crop, GIF/MP4 Video with duration & size validation, Remove)
 * - Avatar management (Image picker with 1:1 crop, Remove)
 * - Profile fields (Display Name, Username, Bio with counter, Location, Website URL)
 * - Profile color / theme presets & Avatar frame customization
 * - Visibility & Privacy options (visible profile sections, field visibility, DM permissions, private account)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserProfileViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val user by viewModel.userProfile.collectAsState()
    val avatarUploadState by viewModel.avatarUploadState.collectAsState()
    val liveCoverUploadState by viewModel.liveCoverUploadState.collectAsState()
    val isSavingProfile by viewModel.isSavingProfile.collectAsState()

    val isUploadingAvatar = avatarUploadState is AvatarUploadState.Loading
    val isUploadingLiveCover = liveCoverUploadState is LiveCoverUploadState.Uploading ||
        liveCoverUploadState is LiveCoverUploadState.Validating
    val snackbarHostState = remember { SnackbarHostState() }

    // Basic profile fields state
    var username by remember(user?.uid) { mutableStateOf(user?.username.orEmpty()) }
    var displayName by remember(user?.uid) { mutableStateOf(user?.displayName.orEmpty()) }
    var bio by remember(user?.uid) { mutableStateOf(user?.bio.orEmpty()) }
    var location by remember(user?.uid) { mutableStateOf(user?.location.orEmpty()) }
    var websiteUrl by remember(user?.uid) { mutableStateOf(user?.websiteUrl.orEmpty()) }
    // Optional personal info — stored as ISO-8601 date / Gender.id
    var birthDate by remember(user?.uid) { mutableStateOf(user?.birthDate.orEmpty()) }
    var gender by remember(user?.uid) { mutableStateOf(user?.gender.orEmpty()) }
    var isBirthDatePickerOpen by remember { mutableStateOf(false) }
    var isPrivate by remember(user?.uid) { mutableStateOf(user?.isPrivate ?: false) }
    var allowMessagesFrom by remember(user?.uid) { mutableStateOf(user?.allowMessagesFrom ?: "everyone") }

    // Single source of truth for Elite vs Regular capabilities
    val features = remember(user?.tier, user?.isOwner, user?.role) {
        EliteFeatures.forUser(user)
    }

    // Appearance & customization state
    val initialCustom = user?.customization ?: ProfileCustomization.DEFAULT
    var selectedAccentColor by remember(user?.uid) { mutableStateOf(initialCustom.accentColor) }
    var selectedThemePreset by remember(user?.uid) { mutableStateOf(initialCustom.themePreset) }
    var selectedShape by remember(user?.uid) { mutableStateOf(initialCustom.resolvedAvatarShape) }
    var cornerRadiusDp by remember(user?.uid) {
        mutableFloatStateOf(initialCustom.effectiveAvatarCornerRadius.toFloat().coerceIn(4f, 38f))
    }
    var showBorder by remember(user?.uid) { mutableStateOf(initialCustom.showAvatarBorder) }
    var borderWidthDp by remember(user?.uid) {
        mutableFloatStateOf(initialCustom.avatarBorderWidth.toFloat().coerceIn(1f, 6f))
    }
    var borderColorOption by remember(user?.uid) { mutableStateOf(initialCustom.avatarBorderColor) }
    var enableDecorativeEffects by remember(user?.uid) { mutableStateOf(initialCustom.enableDecorativeEffects) }
    var visibleSections by remember(user?.uid) {
        mutableStateOf(initialCustom.visibleSections.ifEmpty { listOf("stats", "tabs", "bio") })
    }

    // Granular field privacy state
    val initialPrivacy = user?.privacy ?: ProfilePrivacy.DEFAULT
    var bioVisibility by remember(user?.uid) { mutableStateOf(initialPrivacy.bioVisibility) }
    var locationVisibility by remember(user?.uid) { mutableStateOf(initialPrivacy.locationVisibility) }
    var websiteVisibility by remember(user?.uid) { mutableStateOf(initialPrivacy.websiteVisibility) }
    var libraryVisibility by remember(user?.uid) { mutableStateOf(initialPrivacy.libraryVisibility) }
    var birthDateVisibility by remember(user?.uid) { mutableStateOf(initialPrivacy.birthDateField.name) }
    var genderVisibility by remember(user?.uid) { mutableStateOf(initialPrivacy.genderField.name) }

    var isMessageMenuExpanded by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showCoverOptionsDialog by remember { mutableStateOf(false) }
    var showEliteLockedDialog by remember { mutableStateOf(false) }
    var pendingBannerCropUri by remember { mutableStateOf<Uri?>(null) }
    var pendingAvatarCropUri by remember { mutableStateOf<Uri?>(null) }

    // Media Pickers
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingAvatarCropUri = uri
        } else {
            viewModel.onPhotoPickerCancelled()
        }
    }

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

    val uiMessage by viewModel.uiMessage.collectAsState()

    // Collect ViewModel snackbar messages
    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            val messageText = when (msg) {
                is ProfileUiMessage.Success -> msg.message
                is ProfileUiMessage.Error -> msg.message
            }
            snackbarHostState.showSnackbar(
                message = messageText,
                duration = SnackbarDuration.Short
            )
            viewModel.clearUiMessage()
        }
    }

    // Collect Live Cover validation/upload errors with retry support
    LaunchedEffect(liveCoverUploadState) {
        val state = liveCoverUploadState
        if (state is LiveCoverUploadState.Error) {
            val result = snackbarHostState.showSnackbar(
                message = state.message,
                actionLabel = if (state.canRetry) context.getString(R.string.retry) else null,
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.retryLiveCoverUpload()
            }
        }
    }

    // Collect Avatar upload errors
    LaunchedEffect(avatarUploadState) {
        val state = avatarUploadState
        if (state is AvatarUploadState.Error) {
            snackbarHostState.showSnackbar(
                message = state.message,
                duration = SnackbarDuration.Long
            )
        }
    }

    // Validation using production ProfileValidator
    val usernameValidation = ProfileValidator.validateUsername(username)
    val displayNameValidation = ProfileValidator.validateDisplayName(displayName)
    val bioValidation = ProfileValidator.validateBio(bio)
    val locationValidation = ProfileValidator.validateLocation(location)
    val websiteValidation = ProfileValidator.validateWebsite(websiteUrl)

    val canSave = usernameValidation.isValid &&
        displayNameValidation.isValid &&
        bioValidation.isValid &&
        locationValidation.isValid &&
        websiteValidation.isValid &&
        user != null

    val currentCustomization = remember(
        selectedThemePreset,
        selectedAccentColor,
        visibleSections,
        selectedShape,
        cornerRadiusDp,
        showBorder,
        borderWidthDp,
        borderColorOption,
        enableDecorativeEffects,
        user?.customization?.layoutPreset
    ) {
        ProfileCustomization(
            themePreset = selectedThemePreset,
            accentColor = selectedAccentColor,
            layoutPreset = user?.customization?.layoutPreset ?: "standard",
            visibleSections = visibleSections,
            avatarShape = selectedShape.id,
            avatarCornerRadius = cornerRadiusDp.toInt(),
            showAvatarBorder = showBorder,
            avatarBorderWidth = borderWidthDp.toInt(),
            avatarBorderColor = borderColorOption,
            enableDecorativeEffects = enableDecorativeEffects,
        )
    }

    val currentPrivacy = remember(
        isPrivate,
        bioVisibility,
        locationVisibility,
        websiteVisibility,
        libraryVisibility,
        birthDateVisibility,
        genderVisibility,
        user?.privacy
    ) {
        (user?.privacy ?: ProfilePrivacy.DEFAULT).copy(
            profileVisibility = if (isPrivate) "PRIVATE" else "PUBLIC",
            bioVisibility = bioVisibility,
            locationVisibility = locationVisibility,
            websiteVisibility = websiteVisibility,
            libraryVisibility = libraryVisibility,
            birthDateVisibility = birthDateVisibility,
            genderVisibility = genderVisibility,
        )
    }

    // Track unsaved changes across all profile fields, customization, and privacy options
    val hasUnsavedChanges = remember(
        username,
        displayName,
        bio,
        location,
        websiteUrl,
        birthDate,
        gender,
        isPrivate,
        allowMessagesFrom,
        currentCustomization,
        currentPrivacy,
        user
    ) {
        val currentUser = user ?: return@remember false
        username.trim().lowercase() != currentUser.username.trim().lowercase() ||
            displayName.trim() != currentUser.displayName.trim() ||
            bio.trim() != currentUser.bio.trim() ||
            location.trim() != currentUser.location.orEmpty().trim() ||
            websiteUrl.trim() != currentUser.websiteUrl.orEmpty().trim() ||
            birthDate.trim() != currentUser.birthDate.orEmpty().trim() ||
            gender.trim() != currentUser.gender.orEmpty().trim() ||
            isPrivate != currentUser.isPrivate ||
            allowMessagesFrom != currentUser.allowMessagesFrom ||
            currentCustomization != currentUser.customization ||
            currentPrivacy != currentUser.privacy
    }

    val handleBack = {
        if (hasUnsavedChanges && !isSavingProfile) {
            showUnsavedDialog = true
        } else if (!isSavingProfile) {
            onBack()
        }
    }

    BackHandler(enabled = hasUnsavedChanges || isSavingProfile) {
        handleBack()
    }

    // Resolve live preview styling gated by EliteFeatures
    val effectiveAccentHex = if (features.canUseCustomAccentColors) {
        selectedAccentColor
    } else {
        features.standardAccentColor
    }
    val effectiveThemePreset = if (features.canUseExtraThemePresets) {
        selectedThemePreset
    } else {
        features.standardThemePreset.id
    }
    val effectiveNeonGlow = features.canUseAvatarNeonGlow && enableDecorativeEffects

    val parsedAccent = remember(effectiveAccentHex) {
        try {
            Color(android.graphics.Color.parseColor(effectiveAccentHex))
        } catch (_: Exception) {
             null
        }
    } ?: MaterialTheme.colorScheme.primary

    // The header renders a circle for the simple/premium tiers and the square photo card for
    // the elite tier (elite may still switch to the circle/square presets).
    val previewDesignTier = user?.designTier ?: com.imashnake.animite.social.model.ProfileDesignTier.BASIC
    val liveAvatarShape: Shape = if (previewDesignTier != com.imashnake.animite.social.model.ProfileDesignTier.ELITE || selectedShape == AvatarShape.CIRCLE) {
        CircleShape
    } else when (selectedShape) {
        AvatarShape.SQUARE -> RoundedCornerShape(0.dp)
        else -> RoundedCornerShape(ProfileCustomization.PROFILE_CARD_CORNER_RADIUS.dp)
    }

    val liveBorderColor: Color = when (borderColorOption.uppercase()) {
        "THEME_PRIMARY", "PRIMARY" -> MaterialTheme.colorScheme.primary
        "THEME_SECONDARY", "SECONDARY" -> MaterialTheme.colorScheme.secondary
        "THEME_TERTIARY", "TERTIARY" -> MaterialTheme.colorScheme.tertiary
        "THEME_SURFACE", "SURFACE" -> MaterialTheme.colorScheme.surfaceContainerHigh
        "ACCENT" -> parsedAccent
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    // Mirrors the profile header exactly: a square photo with a thin matte in the page colour.
    val previewBorderWidth = 2.dp
    val previewBorderColor = MaterialTheme.colorScheme.background

    val themeSurfaceTint: Color = when (ThemePreset.fromId(effectiveThemePreset)) {
        ThemePreset.CYAN_GLOW -> parsedAccent.copy(alpha = 0.14f)
        ThemePreset.DEFAULT_DARK -> MaterialTheme.colorScheme.surfaceContainerHigh
        ThemePreset.AMOLED_MIDNIGHT -> MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f)
        ThemePreset.PURPLE_NEBULA -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
    }

    if (isBirthDatePickerOpen) {
        BirthdayPickerDialog(
            initialIsoDate = birthDate,
            onDismiss = { isBirthDatePickerOpen = false },
            onConfirm = { iso ->
                birthDate = iso.orEmpty()
                isBirthDatePickerOpen = false
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.edit_profile),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = handleBack,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    val saveEnabled = canSave && hasUnsavedChanges && !isSavingProfile
                    TextButton(
                        onClick = {
                            if (saveEnabled) {
                                viewModel.saveFullProfile(
                                    displayName = displayName.trim(),
                                    bio = bio.trim(),
                                    location = location.trim().ifEmpty { null },
                                    websiteUrl = websiteUrl.trim().ifEmpty { null },
                                    isPrivate = isPrivate,
                                    allowMessagesFrom = allowMessagesFrom,
                                    username = username.trim().removePrefix("@").lowercase(),
                                    customization = currentCustomization,
                                    privacy = currentPrivacy,
                                    birthDate = birthDate.trim().ifEmpty { null },
                                    gender = gender.trim().ifEmpty { null },
                                    onSuccess = {
                                        coroutineScope.launch {
                                            delay(650)
                                            onBack()
                                        }
                                    }
                                )
                            }
                        },
                        enabled = saveEnabled,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        if (isSavingProfile) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.saving),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.save),
                                fontWeight = FontWeight.Bold,
                                color = if (saveEnabled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                }
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        val currentUser = user
        if (currentUser == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val coverType = remember(
                currentUser.liveCoverUrl,
                currentUser.liveCoverMediaType,
                currentUser.liveCoverPosterUrl,
                currentUser.bannerUrl,
                features.canUseLiveCover
            ) {
                resolveCoverType(
                    liveCoverUrl = currentUser.liveCoverUrl,
                    mediaType = currentUser.liveCoverMediaType,
                    posterUrl = currentUser.liveCoverPosterUrl,
                    bannerUrl = currentUser.bannerUrl,
                    canUseLiveCover = features.canUseLiveCover
                )
            }
            val hasAnyCover = !currentUser.bannerUrl.isNullOrBlank() ||
                (features.canUseLiveCover && !currentUser.liveCoverUrl.isNullOrBlank())
            val hasAvatar = !currentUser.photoUrl.isNullOrBlank()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // =========================================================================
                // 0. LIVE PREVIEW AT THE TOP OF THE SCREEN
                // =========================================================================
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, parsedAccent.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(themeSurfaceTint)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.live_preview_title),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = parsedAccent
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = parsedAccent.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, parsedAccent.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = when (ThemePreset.fromId(effectiveThemePreset)) {
                                        ThemePreset.CYAN_GLOW -> stringResource(R.string.theme_preset_cyan_glow)
                                        ThemePreset.DEFAULT_DARK -> stringResource(R.string.theme_preset_default_dark)
                                        ThemePreset.AMOLED_MIDNIGHT -> stringResource(R.string.theme_preset_amoled_midnight)
                                        ThemePreset.PURPLE_NEBULA -> stringResource(R.string.theme_preset_purple_nebula)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = parsedAccent,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Live Header Preview (CoverMedia + Avatar + Name + @Username)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(166.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            ) {
                                CoverMedia(
                                    coverType = coverType,
                                    isOwner = true,
                                    isCoverVisible = true,
                                    showControls = false,
                                    onCoverClick = { showCoverOptionsDialog = true },
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Theme accent gradient overlay on cover bottom
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f)
                                                )
                                            )
                                        )
                                )
                            }

                            // Avatar in Live Preview
                            val previewAvatarSize = 76.dp
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = 16.dp)
                                    .size(previewAvatarSize),
                                contentAlignment = Alignment.Center
                            ) {
                                if (effectiveNeonGlow) {
                                    Box(
                                        modifier = Modifier
                                            .size(previewAvatarSize + 8.dp)
                                            .shadow(
                                                elevation = 10.dp,
                                                shape = liveAvatarShape,
                                                ambientColor = parsedAccent,
                                                spotColor = parsedAccent
                                            )
                                            .background(parsedAccent.copy(alpha = 0.28f), liveAvatarShape)
                                    )
                                }

                                AsyncImage(
                                    model = currentUser.photoUrl,
                                    contentDescription = stringResource(R.string.change_avatar),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(liveAvatarShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, liveAvatarShape)
                                        .border(previewBorderWidth, previewBorderColor, liveAvatarShape)
                                )
                            }

                            // Live Display Name & Handle next to Avatar
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = 104.dp, end = 16.dp, bottom = 4.dp)
                            ) {
                                Text(
                                    text = displayName.ifBlank { currentUser.displayName.ifBlank { "مستخدم" } },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (username.isNotBlank()) {
                                    Text(
                                        text = "@${username.trim().removePrefix("@")}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            textDirection = TextDirection.Ltr
                                        ),
                                        color = parsedAccent,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Live Bio snippet inside preview if enabled
                        if (bio.isNotBlank() && "bio" in visibleSections) {
                            Text(
                                text = bio,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                // =========================================================================
                // 1. COVER SECTION (Image / GIF / Video + Change & Remove buttons)
                // =========================================================================
                SectionCard(title = stringResource(R.string.cover_section_title)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(128.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        CoverMedia(
                            coverType = coverType,
                            isOwner = true,
                            isCoverVisible = true,
                            showControls = false,
                            onCoverClick = { showCoverOptionsDialog = true },
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isUploadingLiveCover) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.5.dp
                                    )
                                    Text(
                                        text = if (liveCoverUploadState is LiveCoverUploadState.Validating) {
                                            stringResource(R.string.validating_media)
                                        } else {
                                            stringResource(R.string.uploading_live_cover)
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = stringResource(R.string.video_cover_limits_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                bannerPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !isUploadingLiveCover,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.change_cover),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.removeCover() },
                            enabled = hasAnyCover && !isUploadingLiveCover,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = if (hasAnyCover) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.remove_cover),
                                color = if (hasAnyCover) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Elite Animated Cover Tool (Visible for all; locked with explanation dialog for Regular accounts)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (features.canUseLiveCover) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (features.canUseLiveCover) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(enabled = !isUploadingLiveCover) {
                                if (features.canUseLiveCover) {
                                    liveCoverPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                    )
                                } else {
                                    showEliteLockedDialog = true
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (features.canUseLiveCover) Icons.Default.PlayArrow else Icons.Default.Lock,
                                    contentDescription = if (features.canUseLiveCover) {
                                        stringResource(R.string.set_live_cover)
                                    } else {
                                        stringResource(R.string.elite_locked_label)
                                    },
                                    tint = if (features.canUseLiveCover) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = stringResource(R.string.set_live_cover),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!features.canUseLiveCover) {
                                        Text(
                                            text = stringResource(R.string.elite_locked_label),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            if (!features.canUseLiveCover) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = stringResource(R.string.elite_locked_label),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.elite_locked_label),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // 2. AVATAR SECTION (Preview + Change with 1:1 Crop + Remove)
                // =========================================================================
                SectionCard(title = stringResource(R.string.avatar_section_title)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = currentUser.photoUrl,
                                contentDescription = stringResource(R.string.change_avatar),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(liveAvatarShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, liveAvatarShape)
                                    .border(previewBorderWidth, previewBorderColor, liveAvatarShape)
                            )

                            if (isUploadingAvatar) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(liveAvatarShape)
                                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.5.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    avatarPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                enabled = !isUploadingAvatar,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.change_avatar),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.removeAvatar() },
                                enabled = hasAvatar && !isUploadingAvatar,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = if (hasAvatar) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.remove_avatar),
                                    color = if (hasAvatar) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // =========================================================================
                // 3. PROFILE FIELDS (Display name, Username, Bio with counter, Location, Link)
                // =========================================================================
                SectionCard(title = stringResource(R.string.basic_info_section_title)) {
                    // Display Name
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { if (it.length <= 40) displayName = it },
                        label = { Text(stringResource(R.string.display_name)) },
                        supportingText = { Text("${displayName.length}/40") },
                        isError = !displayNameValidation.isValid,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = { if (it.length <= 30) username = it.lowercase().trim() },
                        label = { Text(stringResource(R.string.username)) },
                        leadingIcon = {
                            Text(
                                text = "@",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            textDirection = TextDirection.Ltr
                        ),
                        supportingText = {
                            if (!usernameValidation.isValid) {
                                Text(
                                    text = (usernameValidation as ValidationResult.Invalid).reason,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text("${username.length}/30")
                            }
                        },
                        isError = !usernameValidation.isValid,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Bio (with character counter)
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { if (it.length <= 300) bio = it },
                        label = { Text(stringResource(R.string.bio)) },
                        placeholder = { Text(stringResource(R.string.bio_placeholder)) },
                        supportingText = { Text("${bio.length}/300") },
                        isError = !bioValidation.isValid,
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Location
                    OutlinedTextField(
                        value = location,
                        onValueChange = { if (it.length <= 100) location = it },
                        label = { Text(stringResource(R.string.location)) },
                        placeholder = { Text(stringResource(R.string.location_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        supportingText = { Text("${location.length}/100") },
                        isError = !locationValidation.isValid,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Website Link
                    OutlinedTextField(
                        value = websiteUrl,
                        onValueChange = { websiteUrl = it },
                        label = { Text(stringResource(R.string.website)) },
                        placeholder = { Text(stringResource(R.string.website_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        isError = !websiteValidation.isValid,
                        supportingText = {
                            if (!websiteValidation.isValid) {
                                Text(
                                    text = stringResource(R.string.website_error),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // Optional personal info — birth date & gender (visibility is per field)
                    Text(
                        text = stringResource(R.string.personal_info_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.personal_info_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        onClick = { isBirthDatePickerOpen = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cake,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.birth_date_label),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatBirthDate(birthDate) ?: stringResource(R.string.birth_date_placeholder),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (birthDate.isBlank()) {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    fontWeight = if (birthDate.isBlank()) FontWeight.Normal else FontWeight.SemiBold
                                )
                            }
                            if (birthDate.isNotBlank()) {
                                IconButton(onClick = { birthDate = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.birth_date_clear),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = stringResource(R.string.gender_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.imashnake.animite.social.model.Gender.entries.forEach { option ->
                            FilterChip(
                                selected = gender == option.id,
                                onClick = { gender = if (gender == option.id) "" else option.id },
                                label = { Text(option.labelAr) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                            )
                        }
                    }
                }

                // =========================================================================
                // 4. PROFILE COLOR / THEME & AVATAR APPEARANCE
                // =========================================================================
                SectionCard(title = stringResource(R.string.theme_customization)) {
                    // 4a. Accent Color Swatches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.accent_color),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (!features.canUseCustomAccentColors) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = stringResource(R.string.elite_locked_label),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.elite_locked_label),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = stringResource(R.string.accent_color_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ProfileCustomization.ALLOWED_ACCENT_COLORS.forEach { hex ->
                            val isStandardColor = hex.equals(features.standardAccentColor, ignoreCase = true)
                            val isLockedSwatch = !features.canUseCustomAccentColors && !isStandardColor
                            val isSelected = effectiveAccentHex.equals(hex, ignoreCase = true)
                            val swatchColor = remember(hex) {
                                try {
                                    Color(android.graphics.Color.parseColor(hex))
                                } catch (_: Exception) {
                                    null
                                }
                            } ?: MaterialTheme.colorScheme.primary

                            val animatedSize by animateDpAsState(
                                targetValue = if (isSelected) 38.dp else 32.dp,
                                label = "AccentSwatchSize"
                            )

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        if (isLockedSwatch) {
                                            showEliteLockedDialog = true
                                        } else {
                                            selectedAccentColor = hex
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(animatedSize)
                                        .clip(CircleShape)
                                        .background(if (isLockedSwatch) swatchColor.copy(alpha = 0.45f) else swatchColor)
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(
                                                    width = 2.5.dp,
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    shape = CircleShape
                                                )
                                            } else {
                                                Modifier.border(
                                                    width = 1.dp,
                                                    color = MaterialTheme.colorScheme.outlineVariant,
                                                    shape = CircleShape
                                                )
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isLockedSwatch) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = stringResource(R.string.elite_locked_label),
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = hex,
                                            tint = MaterialTheme.colorScheme.scrim,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // 4b. Theme Preset Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.theme_preset_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (!features.canUseExtraThemePresets) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = stringResource(R.string.elite_locked_label),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.elite_locked_label),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf(
                            ThemePreset.DEFAULT_DARK to stringResource(R.string.theme_preset_default_dark),
                            ThemePreset.CYAN_GLOW to stringResource(R.string.theme_preset_cyan_glow),
                            ThemePreset.AMOLED_MIDNIGHT to stringResource(R.string.theme_preset_amoled_midnight),
                            ThemePreset.PURPLE_NEBULA to stringResource(R.string.theme_preset_purple_nebula),
                        )
                        presets.forEach { (preset, label) ->
                            val isStandardPreset = preset.id.equals(features.standardThemePreset.id, ignoreCase = true)
                            val isLockedPreset = !features.canUseExtraThemePresets && !isStandardPreset
                            val selected = effectiveThemePreset.equals(preset.id, ignoreCase = true)
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    if (isLockedPreset) {
                                        showEliteLockedDialog = true
                                    } else {
                                        selectedThemePreset = preset.id
                                    }
                                },
                                label = { Text(label) },
                                leadingIcon = if (isLockedPreset) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = stringResource(R.string.elite_locked_label),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else if (selected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // 4c. Avatar Shape Controls
                    Text(
                        text = stringResource(R.string.avatar_shape_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedShape == AvatarShape.CIRCLE,
                            onClick = { selectedShape = AvatarShape.CIRCLE },
                            label = { Text(stringResource(R.string.shape_circle)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        )

                        FilterChip(
                            selected = selectedShape == AvatarShape.ROUNDED_SQUARE,
                            onClick = { selectedShape = AvatarShape.ROUNDED_SQUARE },
                            label = { Text(stringResource(R.string.shape_rounded_square)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CropSquare,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .weight(1.25f)
                                .heightIn(min = 48.dp)
                        )

                        FilterChip(
                            selected = selectedShape == AvatarShape.SQUARE,
                            onClick = { selectedShape = AvatarShape.SQUARE },
                            label = { Text(stringResource(R.string.shape_square)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Square,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        )
                    }

                    AnimatedVisibility(visible = selectedShape == AvatarShape.ROUNDED_SQUARE) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.corner_radius_label),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${cornerRadiusDp.toInt()} dp",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Slider(
                                value = cornerRadiusDp,
                                onValueChange = { cornerRadiusDp = it },
                                valueRange = 4f..38f,
                                steps = 5,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // 4d. Avatar Border Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.avatar_border_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.avatar_border_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showBorder,
                            onCheckedChange = { showBorder = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    AnimatedVisibility(visible = showBorder) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(R.string.border_width_label),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${borderWidthDp.toInt()} dp",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = borderWidthDp,
                                    onValueChange = { borderWidthDp = it },
                                    valueRange = 1f..6f,
                                    steps = 4,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Text(
                                text = stringResource(R.string.border_color_label),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BorderColorOptionChip(
                                    label = stringResource(R.string.border_color_background),
                                    color = MaterialTheme.colorScheme.background,
                                    isSelected = borderColorOption == "THEME_BACKGROUND",
                                    onClick = { borderColorOption = "THEME_BACKGROUND" },
                                    modifier = Modifier.weight(1f)
                                )
                                BorderColorOptionChip(
                                    label = stringResource(R.string.border_color_primary),
                                    color = MaterialTheme.colorScheme.primary,
                                    isSelected = borderColorOption == "THEME_PRIMARY",
                                    onClick = { borderColorOption = "THEME_PRIMARY" },
                                    modifier = Modifier.weight(1f)
                                )
                                BorderColorOptionChip(
                                    label = stringResource(R.string.border_color_secondary),
                                    color = MaterialTheme.colorScheme.tertiary,
                                    isSelected = borderColorOption == "THEME_TERTIARY",
                                    onClick = { borderColorOption = "THEME_TERTIARY" },
                                    modifier = Modifier.weight(1f)
                                )
                                BorderColorOptionChip(
                                    label = stringResource(R.string.border_color_accent),
                                    color = parsedAccent,
                                    isSelected = borderColorOption == "ACCENT",
                                    onClick = { borderColorOption = "ACCENT" },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // 4e. Decorative Aura Glow (Elite-only tool: locked with explanation dialog for Regular accounts)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (features.canUseAvatarNeonGlow) {
                                    enableDecorativeEffects = !enableDecorativeEffects
                                } else {
                                    showEliteLockedDialog = true
                                }
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (features.canUseAvatarNeonGlow) Icons.Default.AutoAwesome else Icons.Default.Lock,
                                contentDescription = if (features.canUseAvatarNeonGlow) null else stringResource(R.string.elite_locked_label),
                                tint = if (features.canUseAvatarNeonGlow) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.aura_glow_title),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (!features.canUseAvatarNeonGlow) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.elite_locked_label),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = stringResource(R.string.aura_glow_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = effectiveNeonGlow,
                            onCheckedChange = {
                                if (features.canUseAvatarNeonGlow) {
                                    enableDecorativeEffects = it
                                } else {
                                    showEliteLockedDialog = true
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    // 4f. Reset appearance defaults
                    OutlinedButton(
                        onClick = {
                            selectedShape = AvatarShape.ROUNDED_SQUARE
                            cornerRadiusDp = ProfileCustomization.PROFILE_CARD_CORNER_RADIUS.toFloat()
                            showBorder = true
                            borderWidthDp = 4f
                            borderColorOption = "THEME_BACKGROUND"
                            selectedAccentColor = features.standardAccentColor
                            selectedThemePreset = if (features.canUseExtraThemePresets) {
                                ThemePreset.CYAN_GLOW.id
                            } else {
                                features.standardThemePreset.id
                            }
                            enableDecorativeEffects = features.canUseAvatarNeonGlow
                            visibleSections = listOf("stats", "tabs", "bio")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.reset_appearance_defaults))
                    }
                }

                // =========================================================================
                // 5. VISIBILITY & PRIVACY OPTIONS
                // =========================================================================
                SectionCard(title = stringResource(R.string.visibility_section_title)) {
                    // 5a. Visible Profile Sections (stats, tabs, bio)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.visible_sections_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.visible_sections_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val sectionOptions = listOf(
                            "stats" to stringResource(R.string.section_stats),
                            "tabs" to stringResource(R.string.section_tabs),
                            "bio" to stringResource(R.string.section_bio)
                        )
                        sectionOptions.forEach { (sectionKey, sectionLabel) ->
                            val isChecked = sectionKey in visibleSections
                            FilterChip(
                                selected = isChecked,
                                onClick = {
                                    visibleSections = if (isChecked) {
                                        if (visibleSections.size > 1) visibleSections - sectionKey else visibleSections
                                    } else {
                                        visibleSections + sectionKey
                                    }
                                },
                                label = { Text(sectionLabel) },
                                leadingIcon = if (isChecked) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // 5b. Granular Field Visibility Controls
                    Text(
                        text = stringResource(R.string.field_visibility_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    FieldVisibilityRow(
                        label = stringResource(R.string.bio_visibility_label),
                        selectedValue = bioVisibility,
                        onValueChange = { bioVisibility = it }
                    )
                    FieldVisibilityRow(
                        label = stringResource(R.string.location_visibility_label),
                        selectedValue = locationVisibility,
                        onValueChange = { locationVisibility = it }
                    )
                    FieldVisibilityRow(
                        label = stringResource(R.string.website_visibility_label),
                        selectedValue = websiteVisibility,
                        onValueChange = { websiteVisibility = it }
                    )
                    FieldVisibilityRow(
                        label = stringResource(R.string.library_visibility_label),
                        selectedValue = libraryVisibility,
                        onValueChange = { libraryVisibility = it }
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // 5c. Direct Messages Permission
                    ExposedDropdownMenuBox(
                        expanded = isMessageMenuExpanded,
                        onExpandedChange = { isMessageMenuExpanded = !isMessageMenuExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = when (allowMessagesFrom) {
                                "followers" -> stringResource(R.string.messages_followers)
                                "none" -> stringResource(R.string.messages_none)
                                else -> stringResource(R.string.messages_everyone)
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.allow_messages_from)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMessageMenuExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = isMessageMenuExpanded,
                            onDismissRequest = { isMessageMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.messages_everyone)) },
                                onClick = {
                                    allowMessagesFrom = "everyone"
                                    isMessageMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.messages_followers)) },
                                onClick = {
                                    allowMessagesFrom = "followers"
                                    isMessageMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.messages_none)) },
                                onClick = {
                                    allowMessagesFrom = "none"
                                    isMessageMenuExpanded = false
                                }
                            )
                        }
                    }

                    // 5d. Private Account Toggle
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.private_account),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.private_account_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isPrivate,
                                onCheckedChange = { isPrivate = it }
                            )
                        }
                    }
                }
            }
        }
    }

    // Cover Options Dialog (Static Image with 3:1 Crop, or GIF/MP4 Video, or Remove)
    if (showCoverOptionsDialog && user != null) {
        AlertDialog(
            onDismissRequest = { showCoverOptionsDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.cover_options_title),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
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
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.set_static_cover),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            showCoverOptionsDialog = false
                            if (features.canUseLiveCover) {
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
                                imageVector = if (features.canUseLiveCover) Icons.Default.PlayArrow else Icons.Default.Lock,
                                contentDescription = if (features.canUseLiveCover) null else stringResource(R.string.elite_locked_label),
                                tint = if (features.canUseLiveCover) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.set_live_cover),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (!features.canUseLiveCover) {
                                    Text(
                                        text = stringResource(R.string.elite_locked_label),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (!user!!.bannerUrl.isNullOrBlank() || (features.canUseLiveCover && !user!!.liveCoverUrl.isNullOrBlank())) {
                        TextButton(
                            onClick = {
                                showCoverOptionsDialog = false
                                viewModel.removeCover()
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
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = stringResource(R.string.remove_cover),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
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
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Elite Locked Feature Explanation Dialog (shown when a regular user taps a locked tool)
    if (showEliteLockedDialog) {
        AlertDialog(
            onDismissRequest = { showEliteLockedDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(R.string.elite_locked_label),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.elite_locked_dialog_title),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.elite_locked_dialog_message),
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
                        text = stringResource(R.string.elite_locked_dialog_dismiss),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // Unsaved Changes Confirmation Dialog
    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.unsaved_changes_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = stringResource(R.string.unsaved_changes_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnsavedDialog = false
                        onBack()
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.discard),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUnsavedDialog = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(text = stringResource(R.string.stay_in_editor))
                }
            }
        )
    }

    // Banner 3:1 Crop Dialog
    if (pendingBannerCropUri != null) {
        BannerCropDialog(
            imageUri = pendingBannerCropUri!!,
            onDismiss = { pendingBannerCropUri = null },
            onCropConfirmed = { croppedBytes ->
                pendingBannerCropUri = null
                viewModel.uploadBanner(croppedBytes)
            },
            aspectRatio = 3f,
            showAvatarOverlay = true,
            title = stringResource(R.string.crop_cover_title)
        )
    }

    // Avatar 1:1 Crop Dialog
    if (pendingAvatarCropUri != null) {
        BannerCropDialog(
            imageUri = pendingAvatarCropUri!!,
            onDismiss = { pendingAvatarCropUri = null },
            onCropConfirmed = { croppedBytes ->
                pendingAvatarCropUri = null
                viewModel.uploadAvatar(croppedBytes)
            },
            aspectRatio = 1f,
            showAvatarOverlay = false,
            title = stringResource(R.string.crop_avatar_title)
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun BorderColorOptionChip(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FieldVisibilityRow(
    label: String,
    selectedValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val options = listOf(
                FieldVisibility.PUBLIC.name to stringResource(R.string.visibility_public),
                FieldVisibility.FRIENDS.name to stringResource(R.string.visibility_friends),
                FieldVisibility.PRIVATE.name to stringResource(R.string.visibility_private),
            )
            options.forEach { (value, optionLabel) ->
                val selected = selectedValue.equals(value, ignoreCase = true)
                FilterChip(
                    selected = selected,
                    onClick = { onValueChange(value) },
                    label = { Text(optionLabel) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthdayPickerDialog(
    initialIsoDate: String,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit,
) {
    val initialMillis = remember(initialIsoDate) {
        runCatching {
            java.time.LocalDate.parse(initialIsoDate.trim())
                .atStartOfDay(java.time.ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
    }
    val state = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val iso = state.selectedDateMillis?.let { millis ->
                        java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                            .toString()
                    }
                    onConfirm(iso)
                }
            ) {
                Text(text = stringResource(R.string.save), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    ) {
        androidx.compose.material3.DatePicker(
            state = state,
            showModeToggle = false,
            title = {
                Text(
                    text = stringResource(R.string.birth_date_label),
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        )
    }
}
