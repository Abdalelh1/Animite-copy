package com.imashnake.animite.social.ui.animelist

import android.graphics.Color
import android.graphics.Typeface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.imashnake.animite.social.model.AnimeListStats
import com.imashnake.animite.social.model.WatchStatus

@Composable
fun AnimeListStatsCard(
    stats: AnimeListStats,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary.toArgb()
    val secondary = MaterialTheme.colorScheme.secondary.toArgb()
    val tertiary = MaterialTheme.colorScheme.tertiary.toArgb()
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant.toArgb()
    val onSurface = MaterialTheme.colorScheme.onSurface.toArgb()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "إحصائيات القائمة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pie Chart
            if (stats.totalAnime > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { context ->
                            PieChart(context).apply {
                                description.isEnabled = false
                                setUsePercentValues(true)
                                setDrawEntryLabels(false)
                                setHoleColor(Color.TRANSPARENT)
                                holeRadius = 58f
                                transparentCircleRadius = 61f
                                setDrawCenterText(true)
                                centerText = "${stats.totalAnime}\nأنمي"
                                setCenterTextColor(onSurface)
                                setCenterTextSize(18f)
                                setCenterTextTypeface(Typeface.DEFAULT_BOLD)
                                legend.isEnabled = false
                                setTouchEnabled(false)
                                setExtraOffsets(0f, 0f, 0f, 0f)
                            }
                        },
                        update = { chart ->
                            val entries = mutableListOf<PieEntry>()
                            val colors = mutableListOf<Int>()

                            if (stats.watching > 0) {
                                entries.add(PieEntry(stats.watching.toFloat(), "يشاهد"))
                                colors.add(primary)
                            }
                            if (stats.completed > 0) {
                                entries.add(PieEntry(stats.completed.toFloat(), "مكتمل"))
                                colors.add(android.graphics.Color.parseColor("#4CAF50"))
                            }
                            if (stats.onHold > 0) {
                                entries.add(PieEntry(stats.onHold.toFloat(), "متوقف"))
                                colors.add(android.graphics.Color.parseColor("#FFC107"))
                            }
                            if (stats.dropped > 0) {
                                entries.add(PieEntry(stats.dropped.toFloat(), "متروك"))
                                colors.add(android.graphics.Color.parseColor("#F44336"))
                            }
                            if (stats.planToWatch > 0) {
                                entries.add(PieEntry(stats.planToWatch.toFloat(), "مخطط"))
                                colors.add(surfaceVariant)
                            }

                            val dataSet = PieDataSet(entries, "").apply {
                                setColors(colors)
                                sliceSpace = 2f
                                selectionShift = 5f
                                valueTextColor = Color.WHITE
                                valueTextSize = 12f
                                valueTypeface = Typeface.DEFAULT_BOLD
                            }

                            val data = PieData(dataSet).apply {
                                setValueFormatter(PercentFormatter(chart))
                            }

                            chart.data = data
                            chart.animateY(800, Easing.EaseInOutQuad)
                            chart.invalidate()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Stats Grid
            StatsGrid(
                stats = stats,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatsGrid(
    stats: AnimeListStats,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            StatItem(
                label = "يشاهد",
                value = stats.watching.toString(),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatItem(
                label = "مكتمل",
                value = stats.completed.toString(),
                color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            StatItem(
                label = "متوقف",
                value = stats.onHold.toString(),
                color = androidx.compose.ui.graphics.Color(0xFFFFC107),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatItem(
                label = "متروك",
                value = stats.dropped.toString(),
                color = androidx.compose.ui.graphics.Color(0xFFF44336),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            StatItem(
                label = "مخطط",
                value = stats.planToWatch.toString(),
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatItem(
                label = "حلقات",
                value = stats.totalEpisodesWatched.toString(),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        if (stats.averageScore > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                StatItem(
                    label = "متوسط التقييم",
                    value = String.format("%.1f", stats.averageScore),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatItem(
                    label = "أيام المشاهدة",
                    value = String.format("%.1f", stats.daysWatched),
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
