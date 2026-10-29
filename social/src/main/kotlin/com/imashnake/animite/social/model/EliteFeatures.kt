package com.imashnake.animite.social.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Single source of truth for user account tiers.
 * Existing users without the `tier` field in Firestore default to [REGULAR].
 * Only the application owner can promote an account to [ELITE] or revoke it.
 */
@Serializable
enum class Tier(val id: String) {
    REGULAR("regular"),
    ELITE("elite");

    companion object {
        fun fromId(id: String?): Tier = when (id?.trim()?.lowercase()) {
            "elite" -> ELITE
            else -> REGULAR
        }
    }
}

/**
 * Role badge definitions that the application owner can assign to [Tier.ELITE] accounts.
 */
@Serializable
enum class EliteRoleBadge(
    val id: String,
    val labelAr: String,
    val labelEn: String,
) {
    EVENTS_MODERATOR(
        id = "events_moderator",
        labelAr = "مشرف الفعاليات",
        labelEn = "Events Moderator",
    ),
    NEWS_WRITER(
        id = "news_writer",
        labelAr = "كاتب الأخبار",
        labelEn = "News Writer",
    ),
    MODERATOR(
        id = "moderator",
        labelAr = "مشرف",
        labelEn = "Moderator",
    ),
    COMMUNITY_STAR(
        id = "community_star",
        labelAr = "نجم المجتمع",
        labelEn = "Community Star",
    ),
    TRANSLATOR(
        id = "translator",
        labelAr = "مترجم",
        labelEn = "Translator",
    );

    companion object {
        fun fromId(id: String?): EliteRoleBadge? =
            entries.firstOrNull { it.id.equals(id?.trim(), ignoreCase = true) }

        fun labelArForId(id: String): String =
            fromId(id)?.labelAr ?: id
    }
}

/**
 * Centralized feature matrix defining what each [Tier] is allowed to do and display.
 * Every screen and repository queries [EliteFeatures] (via `user.features` or `EliteFeatures.forUser(user)`)
 * instead of inspecting raw `tier` strings directly.
 *
 * Feature Matrix:
 * - Elite only:
 *   1. Animated cover: GIF/WebP and MP4 video (LIVE badge, play/pause/mute controls)
 *   2. Role badges and the verified check next to the display name
 *   3. Avatar neon glow effect (`enableDecorativeEffects`) and extra theme presets / accent colors
 * - Regular accounts:
 *   1. Static image cover (`bannerUrl` or default gradient fallback)
 *   2. Basic avatar shape and frame
 *   3. Standard accent color and standard theme preset (`DEFAULT_DARK`)
 *   4. No role badges, no verified check, no LIVE controls or animated cover pill
 */
@Immutable
data class EliteFeatures(
    val tier: Tier,
    val canUseLiveCover: Boolean,
    val canShowRoleBadges: Boolean,
    val canShowVerifiedBadge: Boolean,
    val canUseAvatarNeonGlow: Boolean,
    val canUseExtraThemePresets: Boolean,
    val canUseCustomAccentColors: Boolean,
    val allowedThemePresets: Set<ThemePreset>,
    val standardThemePreset: ThemePreset = ThemePreset.DEFAULT_DARK,
    val standardAccentColor: String = "#00D1FF",
) {
    val isElite: Boolean get() = tier == Tier.ELITE

    fun isThemePresetAllowed(preset: ThemePreset): Boolean =
        canUseExtraThemePresets || preset == standardThemePreset

    fun isThemePresetAllowed(presetId: String?): Boolean =
        isThemePresetAllowed(ThemePreset.fromId(presetId))

    fun isAccentColorAllowed(hex: String?): Boolean =
        canUseCustomAccentColors || hex == null || hex.trim().equals(standardAccentColor, ignoreCase = true)

    companion object {
        val REGULAR = EliteFeatures(
            tier = Tier.REGULAR,
            canUseLiveCover = false,
            canShowRoleBadges = false,
            canShowVerifiedBadge = false,
            canUseAvatarNeonGlow = false,
            canUseExtraThemePresets = false,
            canUseCustomAccentColors = false,
            allowedThemePresets = setOf(ThemePreset.DEFAULT_DARK),
        )

        val ELITE = EliteFeatures(
            tier = Tier.ELITE,
            canUseLiveCover = true,
            canShowRoleBadges = true,
            canShowVerifiedBadge = true,
            canUseAvatarNeonGlow = true,
            canUseExtraThemePresets = true,
            canUseCustomAccentColors = true,
            allowedThemePresets = ThemePreset.entries.toSet(),
        )

        fun forTier(tier: Tier): EliteFeatures = when (tier) {
            Tier.ELITE -> ELITE
            Tier.REGULAR -> REGULAR
        }

        fun forUser(user: AnimiteUser?): EliteFeatures {
            if (user == null) return REGULAR
            return forTier(user.resolvedTier)
        }
    }
}
