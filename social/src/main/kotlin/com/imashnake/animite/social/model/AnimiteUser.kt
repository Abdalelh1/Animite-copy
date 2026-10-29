package com.imashnake.animite.social.model

import androidx.compose.runtime.Immutable
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

/**
 * Visibility scope for specific profile fields.
 */
@Serializable
enum class FieldVisibility {
    PUBLIC,
    FRIENDS,
    PRIVATE;

    companion object {
        fun fromString(value: String?): FieldVisibility = when (value?.trim()?.uppercase()) {
            "FRIENDS", "FOLLOWERS" -> FRIENDS
            "PRIVATE" -> PRIVATE
            else -> PUBLIC
        }
    }
}

/**
 * Profile-wide accessibility mode.
 */
@Serializable
enum class ProfileVisibility {
    PUBLIC,
    FOLLOWERS_ONLY,
    PRIVATE;

    companion object {
        fun fromString(value: String?): ProfileVisibility = when (value?.trim()?.uppercase()) {
            "FOLLOWERS_ONLY", "FRIENDS" -> FOLLOWERS_ONLY
            "PRIVATE" -> PRIVATE
            else -> PUBLIC
        }
    }
}

/**
 * Theme preset customization options.
 */
@Serializable
enum class ThemePreset(val id: String) {
    DEFAULT_DARK("default_dark"),
    CYAN_GLOW("cyan_glow"),
    AMOLED_MIDNIGHT("amoled_midnight"),
    PURPLE_NEBULA("purple_nebula");

    companion object {
        fun fromId(id: String?): ThemePreset =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT_DARK
    }
}

/**
 * Profile layout organization presets.
 */
@Serializable
enum class LayoutPreset(val id: String) {
    STANDARD("standard"),
    COMPACT("compact"),
    EXPANDED("expanded");

    companion object {
        fun fromId(id: String?): LayoutPreset =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: STANDARD
    }
}

/**
 * Optional gender value a user may publish on their profile.
 */
@Serializable
enum class Gender(
    val id: String,
    val labelAr: String,
    val labelEn: String,
) {
    MALE("male", "ذكر", "Male"),
    FEMALE("female", "أنثى", "Female"),
    OTHER("other", "آخر", "Other");

    companion object {
        fun fromId(id: String?): Gender? =
            entries.firstOrNull { it.id.equals(id?.trim(), ignoreCase = true) }
    }
}

/**
 * How much of the profile design an account gets. Distinct from [Tier] (permissions):
 * this drives the layout itself — banner height, avatar treatment, which blocks render.
 */
@Serializable
enum class ProfileDesignTier {
    /** Simple: short flat cover, small circular avatar, stats + bio only. */
    BASIC,

    /** Richer: taller cover, accent-ringed avatar, badges, full info row. */
    PREMIUM,

    /** Full editorial header: collapsing cover, photo card, glow, badges, every block. */
    ELITE,
}

/**
 * Whether a field guarded by [this] visibility may be shown to the current viewer.
 * [FieldVisibility.FRIENDS] means "followers of the profile owner" in Animite's social layer.
 */
fun FieldVisibility.isVisibleTo(isSelf: Boolean, viewerFollowsOwner: Boolean): Boolean = when (this) {
    FieldVisibility.PUBLIC -> true
    FieldVisibility.FRIENDS -> isSelf || viewerFollowsOwner
    FieldVisibility.PRIVATE -> isSelf
}

/**
 * Avatar shape presets for profile customizer.
 */
@Serializable
enum class AvatarShape(val id: String) {
    ROUNDED_SQUARE("ROUNDED_SQUARE"),
    CIRCLE("CIRCLE"),
    SQUARE("SQUARE");

    companion object {
        fun fromId(id: String?): AvatarShape =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ROUNDED_SQUARE
    }
}

/**
 * Theme & display customization preferences for a user profile.
 */
@Immutable
@Serializable
data class ProfileCustomization(
    val themePreset: String = ThemePreset.DEFAULT_DARK.id,
    val accentColor: String = "#00D1FF",
    val layoutPreset: String = LayoutPreset.STANDARD.id,
    val visibleSections: List<String> = listOf("stats", "tabs", "bio"),
    val avatarShape: String = AvatarShape.ROUNDED_SQUARE.id,
    val avatarCornerRadius: Int = 28,
    val showAvatarBorder: Boolean = true,
    val avatarBorderWidth: Int = 4,
    val avatarBorderColor: String = "THEME_BACKGROUND", // "THEME_BACKGROUND", "ACCENT", "THEME_PRIMARY", "THEME_TERTIARY", "THEME_SURFACE"
    val enableDecorativeEffects: Boolean = false,
) {
    val resolvedAvatarShape: AvatarShape
        get() = AvatarShape.fromId(avatarShape)

    /**
     * Corner radius actually rendered for [AvatarShape.ROUNDED_SQUARE].
     *
     * Every profile written before the avatar became a soft rounded card carries
     * [LEGACY_DEFAULT_CORNER_RADIUS] because it was the only stored default, so that value
     * resolves to the standard [PROFILE_CARD_CORNER_RADIUS] card. Any radius the user picked
     * explicitly is rendered unchanged.
     */
    val effectiveAvatarCornerRadius: Int
        get() = if (avatarCornerRadius == LEGACY_DEFAULT_CORNER_RADIUS) {
            PROFILE_CARD_CORNER_RADIUS
        } else {
            avatarCornerRadius.coerceIn(0, 48)
        }

    companion object {
        val DEFAULT = ProfileCustomization()

        /** Radius stored by older builds when the avatar shape was never customised. */
        const val LEGACY_DEFAULT_CORNER_RADIUS = 28

        /** Radius of the standard profile card: square corners, like a printed photo. */
        const val PROFILE_CARD_CORNER_RADIUS = 0

        // Curated safe accent palette (WCAG readable against dark surfaces)
        val ALLOWED_ACCENT_COLORS: Set<String> = setOf(
            "#00D1FF", // Neon Cyan (Standard)
            "#22D3EE", // Sky Cyan
            "#818CF8", // Indigo Accent
            "#A78BFA", // Violet Pastel
            "#34D399", // Emerald Accent
            "#F472B6", // Rose Pastel
            "#FBBF24", // Amber Accent
        )

        fun isValidAccentColor(hex: String?): Boolean {
            if (hex.isNullOrBlank()) return false
            val normalized = hex.trim().uppercase()
            return ALLOWED_ACCENT_COLORS.any { it.equals(normalized, ignoreCase = true) }
        }
    }
}

/**
 * Granular field visibility settings for the user profile.
 */
@Immutable
@Serializable
data class ProfilePrivacy(
    val profileVisibility: String = ProfileVisibility.PUBLIC.name,
    val bioVisibility: String = FieldVisibility.PUBLIC.name,
    val locationVisibility: String = FieldVisibility.PUBLIC.name,
    val websiteVisibility: String = FieldVisibility.PUBLIC.name,
    val libraryVisibility: String = FieldVisibility.PUBLIC.name,
    val birthDateVisibility: String = FieldVisibility.PRIVATE.name,
    val genderVisibility: String = FieldVisibility.PRIVATE.name,
    val emailVisibility: String = FieldVisibility.PRIVATE.name,
    val phoneVisibility: String = FieldVisibility.PRIVATE.name,
) {
    @get:Exclude
    val birthDateField: FieldVisibility
        get() = FieldVisibility.fromString(birthDateVisibility)

    @get:Exclude
    val genderField: FieldVisibility
        get() = FieldVisibility.fromString(genderVisibility)

    companion object {
        val DEFAULT = ProfilePrivacy()
    }
}

/**
 * Fully backward-compatible Animite User model with schema versioning and Elite tier separation.
 *
 * Existing users without the `tier` field in Firestore are treated as `"regular"`.
 * `isOwner`, `tier`, `eliteGrantedAt`, `eliteGrantedBy`, and `eliteBadges` are protected
 * by Firestore security rules so regular users can never self-promote.
 */
@Immutable
@Serializable
data class AnimiteUser(
    val uid: String = "",
    val username: String = "",
    val displayName: String = "",
    val bio: String = "",
    val photoUrl: String? = null,
    val bannerUrl: String? = null,
    val location: String? = null,
    val websiteUrl: String? = null,
    /** Optional birthday in ISO-8601 `yyyy-MM-dd` form; rendered only when privacy allows. */
    val birthDate: String? = null,
    /** Optional [Gender.id]; rendered only when privacy allows. */
    val gender: String? = null,
    @get:PropertyName("isPrivate")
    @field:PropertyName("isPrivate")
    val isPrivate: Boolean = false,
    val allowMessagesFrom: String = "everyone", // "everyone", "followers", "none"
    val anilistUserId: Int? = null,
    val anilistUsername: String? = null,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val schemaVersion: Int = 2,
    val customization: ProfileCustomization = ProfileCustomization.DEFAULT,
    val privacy: ProfilePrivacy = ProfilePrivacy.DEFAULT,
    val role: String = "user", // "owner", "admin", "moderator", "user", "premium", "verified"
    val customBadgeTitle: String? = null, // e.g. "مالك التطبيق", "مشرف عام", "مشرف الفعاليات"
    @get:PropertyName("isVerified")
    @field:PropertyName("isVerified")
    val isVerified: Boolean = false,
    val premiumUntil: Long? = null,
    @get:PropertyName("isSuspended")
    @field:PropertyName("isSuspended")
    val isSuspended: Boolean = false,
    val liveCoverUrl: String? = null,
    val liveCoverPosterUrl: String? = null,
    val liveCoverMediaType: String? = null, // "video/mp4", "image/gif", or "image/webp"
    val tier: String = Tier.REGULAR.id, // "regular" | "elite" (default "regular")
    val eliteGrantedAt: Long? = null,
    val eliteGrantedBy: String? = null,
    val eliteBadges: List<String> = emptyList(), // e.g. ["events_moderator", "news_writer"]
    @get:PropertyName("isOwner")
    @field:PropertyName("isOwner")
    val isOwner: Boolean = role.equals("owner", ignoreCase = true),
) {
    /**
     * Normalizes owner authority when deserialized from Firestore documents that may
     * store owner status via either `isOwner == true` or `role == "owner"`.
     */
    fun withResolvedAuthority(): AnimiteUser =
        if (!isOwner && role.equals("owner", ignoreCase = true)) {
            copy(isOwner = true)
        } else {
            this
        }

    /**
     * Single source of truth for the user's tier.
     * Existing users without `tier == "elite"` (and who are not the app owner) resolve to [Tier.REGULAR].
     */
    @get:Exclude
    val resolvedTier: Tier
        get() = if (isOwner || tier.equals(Tier.ELITE.id, ignoreCase = true)) {
            Tier.ELITE
        } else {
            Tier.REGULAR
        }

    /**
     * Centralized feature matrix for this user's tier.
     */
    @get:Exclude
    val features: EliteFeatures
        get() = EliteFeatures.forTier(resolvedTier)

    /**
     * Which profile *layout* this account renders: simple, richer, or the full editorial one.
     * Elite owns the tier flag; Premium is the paid/verified middle rank.
     */
    @get:Exclude
    val designTier: ProfileDesignTier
        get() = when {
            resolvedTier == Tier.ELITE -> ProfileDesignTier.ELITE
            userRole == UserRole.PREMIUM || userRole == UserRole.VERIFIED -> ProfileDesignTier.PREMIUM
            else -> ProfileDesignTier.BASIC
        }

    @get:Exclude
    val isElite: Boolean
        get() = features.isElite

    // Live cover helpers (gated by features.canUseLiveCover in UI)
    @get:Exclude
    val hasLiveCover: Boolean
        get() = features.canUseLiveCover && !liveCoverUrl.isNullOrBlank()

    @get:Exclude
    val isLiveCoverVideo: Boolean
        get() = mediaIsVideo(liveCoverMediaType, liveCoverUrl)

    @get:Exclude
    val isLiveCoverGif: Boolean
        get() = mediaIsGifOrWebp(liveCoverMediaType, liveCoverUrl)

    // Backward-compatible alias for avatarUrl as specified in requirements
    @get:Exclude
    val avatarUrl: String? get() = photoUrl

    /** Parsed optional gender value (null when unset or unknown). */
    @get:Exclude
    val resolvedGender: Gender?
        get() = Gender.fromId(gender)

    /** Whether the viewer may see the optional birthday (respects [ProfilePrivacy]). */
    fun canSeeBirthDate(isSelf: Boolean, viewerFollowsOwner: Boolean): Boolean =
        !birthDate.isNullOrBlank() && privacy.birthDateField.isVisibleTo(isSelf, viewerFollowsOwner)

    /** Whether the viewer may see the optional gender (respects [ProfilePrivacy]). */
    fun canSeeGender(isSelf: Boolean, viewerFollowsOwner: Boolean): Boolean =
        resolvedGender != null && privacy.genderField.isVisibleTo(isSelf, viewerFollowsOwner)

    @get:Exclude
    val handle: String get() = if (username.isNotBlank()) "@$username" else ""

    @get:Exclude
    val isAnilistLinked: Boolean get() = anilistUserId != null

    // Strongly-typed RBAC role representation
    @get:Exclude
    val userRole: UserRole
        get() {
            if (isOwner) return UserRole.OWNER
            val parsed = UserRole.fromId(role)
            if (parsed == UserRole.PREMIUM && premiumUntil != null && premiumUntil <= System.currentTimeMillis()) {
                return if (isVerified) UserRole.VERIFIED else UserRole.MEMBER
            }
            return parsed
        }

    // Role & permissions helpers
    @get:Exclude
    val isAdmin: Boolean get() = isOwner || role.equals("admin", ignoreCase = true)

    @get:Exclude
    val isModerator: Boolean get() = isOwner || isAdmin || userRole == UserRole.MODERATOR

    @get:Exclude
    val isPremium: Boolean get() = features.isElite

    @get:Exclude
    val isVerifiedUser: Boolean get() = features.canShowVerifiedBadge

    @get:Exclude
    val hasBadge: Boolean get() = features.canShowRoleBadges && displayBadges.isNotEmpty()

    /**
     * All Arabic role badge labels to display for this user.
     * Always empty for regular accounts (`!features.canShowRoleBadges`).
     */
    @get:Exclude
    val displayBadges: List<String>
        get() {
            if (!features.canShowRoleBadges) return emptyList()
            val result = mutableListOf<String>()
            if (isOwner) {
                result.add(customBadgeTitle?.takeIf { it.isNotBlank() } ?: "مالك التطبيق")
            } else if (!customBadgeTitle.isNullOrBlank()) {
                result.add(customBadgeTitle)
            }
            eliteBadges.forEach { badgeId ->
                val label = EliteRoleBadge.labelArForId(badgeId)
                if (label.isNotBlank() && label !in result) {
                    result.add(label)
                }
            }
            if (result.isEmpty()) {
                when {
                    isAdmin -> result.add("مدير النظام")
                    userRole == UserRole.MODERATOR -> result.add("مشرف")
                    else -> result.add("نخبة")
                }
            }
            return result
        }

    /**
     * Primary display label for user badge in Arabic (null for regular accounts).
     */
    @get:Exclude
    val badgeLabel: String?
        get() = displayBadges.firstOrNull()

    companion object {
        private fun mediaIsVideo(mediaType: String?, url: String?): Boolean =
            mediaType == "video/mp4" || url?.contains(".mp4", ignoreCase = true) == true

        private fun mediaIsGifOrWebp(mediaType: String?, url: String?): Boolean =
            mediaType == "image/gif" ||
                mediaType == "image/webp" ||
                url?.contains(".gif", ignoreCase = true) == true ||
                url?.contains(".webp", ignoreCase = true) == true
    }
}

sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(val user: AnimiteUser) : AuthState
}
