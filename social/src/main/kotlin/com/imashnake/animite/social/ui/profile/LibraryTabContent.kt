package com.imashnake.animite.social.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.imashnake.animite.social.model.AnimeListEntry
import com.imashnake.animite.social.model.AnimeListStats
import com.imashnake.animite.social.model.WatchStatus
import com.imashnake.animite.social.ui.animelist.AnimeListStatsCard
import java.util.Locale

@Composable
fun LibraryTabContent(
    userId: String,
    stats: AnimeListStats,
    entries: List<AnimeListEntry>,
    isCurrentUser: Boolean,
    onAnimeClick: (Int) -> Unit,
    onProgressUpdate: (String, Int, Int?) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    var selectedStatus by remember { mutableStateOf<WatchStatus?>(null) }

    val filterStatuses = listOf(
        "الكل" to null,
        "يشاهد" to WatchStatus.WATCHING,
        "مكتمل" to WatchStatus.COMPLETED,
        "متوقف" to WatchStatus.ON_HOLD,
        "متروك" to WatchStatus.DROPPED,
        "مخطط" to WatchStatus.PLAN_TO_WATCH
    )

    val filteredEntries = remember(entries, selectedStatus) {
        if (selectedStatus == null) {
            entries
        } else {
            entries.filter { it.getWatchStatus() == selectedStatus }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. الإحصائيات مع الرسم البياني الدائري (PieChart)
        if (stats.totalAnime > 0) {
            item {
                AnimeListStatsCard(
                    stats = stats,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp)
                )
            }
        }

        // 2. شريط فلاتر الحالات (Filter Chips)
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterStatuses) { (title, status) ->
                    val isSelected = selectedStatus == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatus = status },
                        label = {
                            val count = when (status) {
                                null -> stats.totalAnime
                                WatchStatus.WATCHING -> stats.watching
                                WatchStatus.COMPLETED -> stats.completed
                                WatchStatus.ON_HOLD -> stats.onHold
                                WatchStatus.DROPPED -> stats.dropped
                                WatchStatus.PLAN_TO_WATCH -> stats.planToWatch
                            }
                            Text(
                                text = if (count > 0) "$title ($count)" else title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }

        // 3. عرض قائمة الأنمي
        if (filteredEntries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.imashnake.animite.social.R.string.empty_section_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "لا توجد أعمال في هذه القائمة حالياً",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredEntries, key = { it.id }) { entry ->
                LibraryEntryCard(
                    entry = entry,
                    isCurrentUser = isCurrentUser,
                    onClick = { onAnimeClick(entry.animeId) },
                    onIncrementProgress = {
                        val currentProgress = entry.progress
                        val total = entry.totalEpisodes
                        if (total == null || currentProgress < total) {
                            onProgressUpdate(entry.id, currentProgress + 1, total)
                        }
                    },
                    onToggleFavorite = {
                        onToggleFavorite(entry.id, entry.isFavorite)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun LibraryEntryCard(
    entry: AnimeListEntry,
    isCurrentUser: Boolean,
    onClick: () -> Unit,
    onIncrementProgress: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // صورة الغلاف
            Box(
                modifier = Modifier
                    .size(width = 68.dp, height = 96.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = entry.animeCoverUrl,
                    contentDescription = entry.animeTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.animeTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (entry.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (entry.isFavorite) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // شارة حالة المشاهدة
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = getLibraryStatusColor(entry.getWatchStatus()).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = entry.getWatchStatus().toArabicString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = getLibraryStatusColor(entry.getWatchStatus()),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // التقييم إذا وجد
                    if (entry.score != null && entry.score > 0f) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f", entry.score),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // شريط التقدم + زر الزيادة السريع (+1)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val total = entry.totalEpisodes
                    val progressFraction = if (total != null && total > 0) {
                        (entry.progress.toFloat() / total).coerceIn(0f, 1f)
                    } else null

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (total != null && total > 0) {
                                "الحلقة ${entry.progress} من $total"
                            } else {
                                "الحلقة ${entry.progress}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (progressFraction != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = getLibraryStatusColor(entry.getWatchStatus()),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }

                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            IconButton(
                                onClick = onIncrementProgress,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "+1 Episode",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getLibraryStatusColor(status: WatchStatus): Color {
    return when (status) {
        WatchStatus.WATCHING -> Color(0xFF2196F3)
        WatchStatus.COMPLETED -> Color(0xFF4CAF50)
        WatchStatus.ON_HOLD -> Color(0xFFFF9800)
        WatchStatus.DROPPED -> Color(0xFFF44336)
        WatchStatus.PLAN_TO_WATCH -> Color(0xFF9C27B0)
    }
}
