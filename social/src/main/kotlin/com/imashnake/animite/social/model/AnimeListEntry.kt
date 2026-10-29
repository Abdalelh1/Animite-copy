package com.imashnake.animite.social.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

/**
 * حالات مشاهدة الأنمي
 */
enum class WatchStatus {
    @PropertyName("watching")
    WATCHING,        // يشاهدها حالياً

    @PropertyName("completed")
    COMPLETED,       // مكتملة

    @PropertyName("on_hold")
    ON_HOLD,        // متوقفة مؤقتاً

    @PropertyName("dropped")
    DROPPED,        // متروكة

    @PropertyName("plan_to_watch")
    PLAN_TO_WATCH;  // يخطط لمشاهدتها

    fun toArabicString(): String = when (this) {
        WATCHING -> "يشاهدها حالياً"
        COMPLETED -> "مكتملة"
        ON_HOLD -> "متوقفة مؤقتاً"
        DROPPED -> "متروكة"
        PLAN_TO_WATCH -> "يخطط لمشاهدتها"
    }
}

/**
 * نموذج بيانات لمدخل في قائمة الأنمي الخاصة بالمستخدم
 */
data class AnimeListEntry(
    @PropertyName("id")
    val id: String = "",

    @PropertyName("userId")
    val userId: String = "",

    @PropertyName("animeId")
    val animeId: Int = 0,

    @PropertyName("animeTitle")
    val animeTitle: String = "",

    @PropertyName("animeCoverUrl")
    val animeCoverUrl: String? = null,

    @PropertyName("watchStatus")
    val watchStatus: String = WatchStatus.PLAN_TO_WATCH.name,

    @PropertyName("progress")
    val progress: Int = 0,  // عدد الحلقات المشاهدة

    @PropertyName("totalEpisodes")
    val totalEpisodes: Int? = null,

    @PropertyName("score")
    val score: Float? = null,  // التقييم من 1-10

    @PropertyName("isFavorite")
    val isFavorite: Boolean = false,

    @PropertyName("startedAt")
    val startedAt: Timestamp? = null,

    @PropertyName("completedAt")
    val completedAt: Timestamp? = null,

    @PropertyName("updatedAt")
    val updatedAt: Timestamp = Timestamp.now(),

    @PropertyName("notes")
    val notes: String? = null
) {
    fun getWatchStatus(): WatchStatus = try {
        WatchStatus.valueOf(watchStatus)
    } catch (e: IllegalArgumentException) {
        WatchStatus.PLAN_TO_WATCH
    }

    fun getProgressPercentage(): Float {
        return if (totalEpisodes != null && totalEpisodes > 0) {
            (progress.toFloat() / totalEpisodes) * 100f
        } else {
            0f
        }
    }
}

/**
 * إحصائيات قائمة الأنمي
 */
data class AnimeListStats(
    val totalAnime: Int = 0,
    val watching: Int = 0,
    val completed: Int = 0,
    val onHold: Int = 0,
    val dropped: Int = 0,
    val planToWatch: Int = 0,
    val totalEpisodesWatched: Int = 0,
    val averageScore: Float = 0f,
    val daysWatched: Float = 0f  // تقدير بناءً على عدد الحلقات
) {
    companion object {
        fun fromEntries(entries: List<AnimeListEntry>): AnimeListStats {
            val grouped = entries.groupBy { it.getWatchStatus() }
            val totalEpisodes = entries.sumOf { it.progress }
            val scores = entries.mapNotNull { it.score }

            return AnimeListStats(
                totalAnime = entries.size,
                watching = grouped[WatchStatus.WATCHING]?.size ?: 0,
                completed = grouped[WatchStatus.COMPLETED]?.size ?: 0,
                onHold = grouped[WatchStatus.ON_HOLD]?.size ?: 0,
                dropped = grouped[WatchStatus.DROPPED]?.size ?: 0,
                planToWatch = grouped[WatchStatus.PLAN_TO_WATCH]?.size ?: 0,
                totalEpisodesWatched = totalEpisodes,
                averageScore = if (scores.isNotEmpty()) scores.average().toFloat() else 0f,
                daysWatched = (totalEpisodes * 24f) / (60f * 24f)  // متوسط 24 دقيقة للحلقة
            )
        }
    }
}
