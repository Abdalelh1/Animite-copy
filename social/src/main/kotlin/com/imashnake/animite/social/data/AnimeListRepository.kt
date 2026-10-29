package com.imashnake.animite.social.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.imashnake.animite.social.model.AnimeListEntry
import com.imashnake.animite.social.model.WatchStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnimeListRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val animeListCollection = firestore.collection("anime_lists")

    /**
     * مراقبة قائمة الأنمي الخاصة بمستخدم
     */
    fun getUserAnimeList(userId: String): Flow<List<AnimeListEntry>> = callbackFlow {
        val listener = animeListCollection
            .whereEqualTo("userId", userId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val entries = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(AnimeListEntry::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(entries)
            }

        awaitClose { listener.remove() }
    }

    /**
     * الحصول على قائمة بحالة محددة
     */
    fun getUserAnimeListByStatus(
        userId: String,
        status: WatchStatus
    ): Flow<List<AnimeListEntry>> = callbackFlow {
        val listener = animeListCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("watchStatus", status.name)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val entries = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(AnimeListEntry::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(entries)
            }

        awaitClose { listener.remove() }
    }

    /**
     * إضافة أنمي للقائمة
     */
    suspend fun addAnimeToList(
        userId: String,
        animeId: Int,
        animeTitle: String,
        animeCoverUrl: String?,
        watchStatus: WatchStatus = WatchStatus.PLAN_TO_WATCH
    ): Result<String> = try {
        // التحقق من عدم وجود الأنمي مسبقاً
        val existing = animeListCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("animeId", animeId)
            .get()
            .await()

        if (!existing.isEmpty) {
            return Result.failure(Exception("الأنمي موجود بالفعل في القائمة"))
        }

        val entry = AnimeListEntry(
            userId = userId,
            animeId = animeId,
            animeTitle = animeTitle,
            animeCoverUrl = animeCoverUrl,
            watchStatus = watchStatus.name,
            updatedAt = Timestamp.now()
        )

        val docRef = animeListCollection.add(entry).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * تحديث حالة المشاهدة
     */
    suspend fun updateWatchStatus(
        entryId: String,
        newStatus: WatchStatus
    ): Result<Unit> = try {
        val updates = hashMapOf<String, Any>(
            "watchStatus" to newStatus.name,
            "updatedAt" to Timestamp.now()
        )

        // إذا تم الانتهاء من المشاهدة، نضيف تاريخ الإنهاء
        if (newStatus == WatchStatus.COMPLETED) {
            updates["completedAt"] = Timestamp.now()
        }

        animeListCollection.document(entryId).update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * تحديث التقدم (عدد الحلقات المشاهدة)
     */
    suspend fun updateProgress(
        entryId: String,
        progress: Int,
        totalEpisodes: Int?
    ): Result<Unit> = try {
        val updates = hashMapOf<String, Any>(
            "progress" to progress,
            "updatedAt" to Timestamp.now()
        )

        totalEpisodes?.let {
            updates["totalEpisodes"] = it

            // إذا أكمل جميع الحلقات، نغير الحالة تلقائياً
            if (progress >= it) {
                updates["watchStatus"] = WatchStatus.COMPLETED.name
                updates["completedAt"] = Timestamp.now()
            }
        }

        animeListCollection.document(entryId).update(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * تحديث التقييم
     */
    suspend fun updateScore(
        entryId: String,
        score: Float
    ): Result<Unit> = try {
        animeListCollection.document(entryId).update(
            mapOf(
                "score" to score.coerceIn(0f, 10f),
                "updatedAt" to Timestamp.now()
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * تبديل حالة المفضلة
     */
    suspend fun toggleFavorite(
        entryId: String,
        isFavorite: Boolean
    ): Result<Unit> = try {
        animeListCollection.document(entryId).update(
            mapOf(
                "isFavorite" to isFavorite,
                "updatedAt" to Timestamp.now()
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * حذف أنمي من القائمة
     */
    suspend fun removeAnimeFromList(entryId: String): Result<Unit> = try {
        animeListCollection.document(entryId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * الحصول على مدخل واحد
     */
    suspend fun getAnimeListEntry(entryId: String): Result<AnimeListEntry?> = try {
        val doc = animeListCollection.document(entryId).get().await()
        val entry = doc.toObject(AnimeListEntry::class.java)?.copy(id = doc.id)
        Result.success(entry)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
