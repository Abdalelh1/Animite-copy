package com.imashnake.animite.social.model

import com.imashnake.animite.social.ui.profile.CoverType
import com.imashnake.animite.social.ui.profile.resolveCoverType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EliteFeaturesTest {

    @Test
    fun `missing or unknown tier defaults to REGULAR`() {
        assertEquals(Tier.REGULAR, Tier.fromId(null))
        assertEquals(Tier.REGULAR, Tier.fromId(""))
        assertEquals(Tier.REGULAR, Tier.fromId("regular"))
        assertEquals(Tier.REGULAR, Tier.fromId("unknown_tier"))
        assertEquals(Tier.ELITE, Tier.fromId("elite"))
        assertEquals(Tier.ELITE, Tier.fromId("ELITE"))
    }

    @Test
    fun `regular user has all elite features disabled`() {
        val regularUser = AnimiteUser(
            uid = "user_1",
            username = "regular_user",
            displayName = "Regular User",
            tier = "regular",
            isVerified = true, // Even if legacy flag is set, regular tier cannot show verified check or badges
            eliteBadges = listOf("events_moderator"),
            liveCoverUrl = "https://cdn.example.com/live.mp4",
            liveCoverMediaType = "video",
            bannerUrl = "https://cdn.example.com/static.jpg"
        )

        val features = regularUser.features
        assertEquals(Tier.REGULAR, regularUser.resolvedTier)
        assertFalse(features.canUseLiveCover)
        assertFalse(features.canShowRoleBadges)
        assertFalse(features.canShowVerifiedBadge)
        assertFalse(features.canUseAvatarNeonGlow)
        assertFalse(features.canUseExtraThemePresets)
        assertFalse(features.canUseCustomAccentColors)
        assertTrue(regularUser.displayBadges.isEmpty())
        assertNull(regularUser.badgeLabel)

        val cover = resolveCoverType(
            liveCoverUrl = regularUser.liveCoverUrl,
            mediaType = regularUser.liveCoverMediaType,
            posterUrl = regularUser.liveCoverPosterUrl,
            bannerUrl = regularUser.bannerUrl,
            canUseLiveCover = features.canUseLiveCover
        )
        assertEquals(CoverType.Image(url = "https://cdn.example.com/static.jpg"), cover)
    }

    @Test
    fun `elite user unlocks animated cover, badges, verified check, glow and presets`() {
        val eliteUser = AnimiteUser(
            uid = "user_2",
            username = "elite_user",
            displayName = "Elite User",
            tier = "elite",
            eliteGrantedBy = "owner_uid",
            eliteGrantedAt = 1700000000000L,
            eliteBadges = listOf("events_moderator", "news_writer"),
            liveCoverUrl = "https://cdn.example.com/live.mp4",
            liveCoverPosterUrl = "https://cdn.example.com/poster.jpg",
            liveCoverMediaType = "video",
            bannerUrl = "https://cdn.example.com/static.jpg"
        )

        val features = eliteUser.features
        assertEquals(Tier.ELITE, eliteUser.resolvedTier)
        assertTrue(features.canUseLiveCover)
        assertTrue(features.canShowRoleBadges)
        assertTrue(features.canShowVerifiedBadge)
        assertTrue(features.canUseAvatarNeonGlow)
        assertTrue(features.canUseExtraThemePresets)
        assertTrue(features.canUseCustomAccentColors)
        assertEquals(listOf("مشرف الفعاليات", "كاتب الأخبار"), eliteUser.displayBadges)
        assertEquals("مشرف الفعاليات", eliteUser.badgeLabel)

        val cover = resolveCoverType(
            liveCoverUrl = eliteUser.liveCoverUrl,
            mediaType = eliteUser.liveCoverMediaType,
            posterUrl = eliteUser.liveCoverPosterUrl,
            bannerUrl = eliteUser.bannerUrl,
            canUseLiveCover = features.canUseLiveCover
        )
        assertEquals(
            CoverType.Video(
                url = "https://cdn.example.com/live.mp4",
                posterUrl = "https://cdn.example.com/poster.jpg"
            ),
            cover
        )
    }

    @Test
    fun `demotion preserves data and falls back to regular features, re-promotion restores them`() {
        val promoted = AnimiteUser(
            uid = "user_3",
            username = "member",
            displayName = "Member",
            tier = "elite",
            eliteBadges = listOf("news_writer"),
            liveCoverUrl = "https://cdn.example.com/anim.gif",
            liveCoverMediaType = "gif",
            bannerUrl = "https://cdn.example.com/fallback.jpg"
        )

        // Demote to regular while keeping stored data
        val demoted = promoted.copy(tier = "regular")
        assertFalse(demoted.features.canUseLiveCover)
        assertFalse(demoted.features.canShowRoleBadges)
        assertTrue(demoted.displayBadges.isEmpty())
        assertEquals(
            CoverType.Image(url = "https://cdn.example.com/fallback.jpg"),
            resolveCoverType(
                liveCoverUrl = demoted.liveCoverUrl,
                mediaType = demoted.liveCoverMediaType,
                posterUrl = demoted.liveCoverPosterUrl,
                bannerUrl = demoted.bannerUrl,
                canUseLiveCover = demoted.features.canUseLiveCover
            )
        )

        // Re-promote back to elite: stored animated cover and badges return immediately
        val repromoted = demoted.copy(tier = "elite")
        assertTrue(repromoted.features.canUseLiveCover)
        assertEquals(listOf("كاتب الأخبار"), repromoted.displayBadges)
        assertEquals(
            CoverType.Gif(url = "https://cdn.example.com/anim.gif", posterUrl = "https://cdn.example.com/fallback.jpg"),
            resolveCoverType(
                liveCoverUrl = repromoted.liveCoverUrl,
                mediaType = repromoted.liveCoverMediaType,
                posterUrl = repromoted.liveCoverPosterUrl,
                bannerUrl = repromoted.bannerUrl,
                canUseLiveCover = repromoted.features.canUseLiveCover
            )
        )
    }

    @Test
    fun `firestore rules block regular account from writing tier, isOwner, eliteBadges or liveCoverUrl directly`() {
        val forbiddenPrivilegeKeys = setOf(
            "email", "phoneNumber", "roles", "role", "isVerified", "isBanned", "isSuspended",
            "premiumUntil", "tier", "eliteGrantedAt", "eliteGrantedBy", "eliteBadges", "isOwner", "customBadgeTitle"
        )
        val eliteOnlyCoverKeys = setOf("liveCoverUrl", "liveCoverPosterUrl", "liveCoverMediaType")

        fun evaluateUserUpdateRule(
            callerUid: String,
            callerIsOwner: Boolean,
            targetUid: String,
            existingData: Map<String, Any?>,
            newData: Map<String, Any?>,
        ): String {
            val affectedKeys = (existingData.keys + newData.keys).filter { existingData[it] != newData[it] }.toSet()
            val isSelf = callerUid == targetUid
            val isTargetCurrentlyElite = existingData["tier"] == "elite" ||
                existingData["isOwner"] == true ||
                existingData["role"] == "owner"

            val allowedBySelfRule = isSelf &&
                newData["uid"] == targetUid &&
                affectedKeys.intersect(forbiddenPrivilegeKeys).isEmpty() &&
                (isTargetCurrentlyElite || callerIsOwner || affectedKeys.intersect(eliteOnlyCoverKeys).isEmpty())

            val allowedByOwnerRule = callerIsOwner &&
                "isOwner" !in affectedKeys &&
                (newData["tier"] == null || newData["tier"] in setOf("regular", "elite")) &&
                (newData["eliteBadges"] == null || newData["eliteBadges"] is List<*>)

            return if (allowedBySelfRule || allowedByOwnerRule) {
                "ALLOW"
            } else {
                "PERMISSION_DENIED: Missing or insufficient permissions (affectedKeys=$affectedKeys)"
            }
        }

        val baseDoc = mapOf(
            "uid" to "reg_user_1",
            "username" to "reg_user",
            "displayName" to "Regular User",
            "tier" to "regular",
            "isOwner" to false,
            "role" to "user",
            "eliteBadges" to emptyList<String>(),
            "liveCoverUrl" to null
        )

        // (1) Regular user attempts to self-promote tier = "elite" -> DENIED
        val selfPromoteTierResult = evaluateUserUpdateRule(
            callerUid = "reg_user_1",
            callerIsOwner = false,
            targetUid = "reg_user_1",
            existingData = baseDoc,
            newData = baseDoc + ("tier" to "elite")
        )
        println("Test (d.1) Regular user writing tier='elite': $selfPromoteTierResult")
        assertTrue(selfPromoteTierResult.startsWith("PERMISSION_DENIED"))

        // (2) Regular user attempts to write isOwner = true -> DENIED
        val selfSetOwnerResult = evaluateUserUpdateRule(
            callerUid = "reg_user_1",
            callerIsOwner = false,
            targetUid = "reg_user_1",
            existingData = baseDoc,
            newData = baseDoc + ("isOwner" to true)
        )
        println("Test (d.2) Regular user writing isOwner=true: $selfSetOwnerResult")
        assertTrue(selfSetOwnerResult.startsWith("PERMISSION_DENIED"))

        // (3) Regular user attempts to write eliteBadges -> DENIED
        val selfSetBadgesResult = evaluateUserUpdateRule(
            callerUid = "reg_user_1",
            callerIsOwner = false,
            targetUid = "reg_user_1",
            existingData = baseDoc,
            newData = baseDoc + ("eliteBadges" to listOf("events_moderator"))
        )
        println("Test (d.3) Regular user writing eliteBadges: $selfSetBadgesResult")
        assertTrue(selfSetBadgesResult.startsWith("PERMISSION_DENIED"))

        // (4) Regular user attempts to write liveCoverUrl directly -> DENIED
        val selfSetLiveCoverResult = evaluateUserUpdateRule(
            callerUid = "reg_user_1",
            callerIsOwner = false,
            targetUid = "reg_user_1",
            existingData = baseDoc,
            newData = baseDoc + ("liveCoverUrl" to "https://cdn.example.com/hack.mp4")
        )
        println("Test (d.4) Regular user writing liveCoverUrl: $selfSetLiveCoverResult")
        assertTrue(selfSetLiveCoverResult.startsWith("PERMISSION_DENIED"))

        // (5) Owner promotes regular user -> ALLOW
        val ownerPromoteResult = evaluateUserUpdateRule(
            callerUid = "app_owner_uid",
            callerIsOwner = true,
            targetUid = "reg_user_1",
            existingData = baseDoc,
            newData = baseDoc + mapOf(
                "tier" to "elite",
                "eliteGrantedBy" to "app_owner_uid",
                "eliteGrantedAt" to 1700000000000L,
                "eliteBadges" to listOf("events_moderator")
            )
        )
        println("Test (d.5) Owner promoting user to elite: $ownerPromoteResult")
        assertEquals("ALLOW", ownerPromoteResult)
    }
}
