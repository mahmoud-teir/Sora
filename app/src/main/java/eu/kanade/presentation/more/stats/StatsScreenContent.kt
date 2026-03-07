package eu.kanade.presentation.more.stats

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import eu.kanade.presentation.more.stats.data.StatsData
import eu.kanade.presentation.util.toDurationString
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import java.util.Locale
import kotlin.time.DurationUnit
import kotlin.time.toDuration

// Sora Theme Colors
private val SoraBlue = Color(0xFF2D7CFF)
private val SurfaceDark = Color(0xFF141414)
private val SurfaceLight = Color(0xFF1E1E1E)
private val TextGray = Color(0xFFAAAAAA)
private val GrowthGreen = Color(0xFF4ADE80)
private val FireOrange = Color(0xFFFFA000)

@Composable
fun StatsScreenContent(
    state: StatsScreenState.Success,
    paddingValues: PaddingValues,
    navigator: Navigator,
) {
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(paddingValues)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .padding(top = systemBarsPadding.calculateTopPadding()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.IconButton(
                        onClick = navigator::pop,
                        modifier = Modifier
                            .size(40.dp)
                            .background(SurfaceLight, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Reading Statistics",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    androidx.compose.material3.IconButton(
                        onClick = { /* Share mock */ },
                        modifier = Modifier
                            .size(40.dp)
                            .background(SurfaceLight, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }
                }
            }
            item {
                TimeFilterSelector()
            }
            item {
                ReadingActivitySection(state.overview)
            }
            item {
                WeeklyChartSection()
            }
            item {
                StatsMatrix(state)
            }
            item {
                GenreDonutChart()
            }
            item {
                MostReadSection()
            }
        }
    }
}

@Composable
private fun TimeFilterSelector() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .height(44.dp)
            .background(SurfaceDark, RoundedCornerShape(22.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterPill("Week", true)
        FilterPill("Month", false)
        FilterPill("Year", false)
    }
}

@Composable
private fun FilterPill(text: String, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) SoraBlue else Color.Transparent)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else TextGray,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
private fun ReadingActivitySection(overview: StatsData.Overview) {
    val context = LocalContext.current
    val readDurationString = remember(overview.totalReadDuration) {
        val totalMs = overview.totalReadDuration
        val hours = totalMs.toDuration(DurationUnit.MILLISECONDS).inWholeHours
        val minutes = totalMs.toDuration(DurationUnit.MILLISECONDS).inWholeMinutes % 60
        "${hours}h ${minutes}m"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = FireOrange,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Reading Activity",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = readDurationString,
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 48.sp
            )
            
            Box(
                modifier = Modifier
                    .background(Color(0xFF1A2E1C), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "+12% from last week",
                    color = GrowthGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun WeeklyChartSection() {
    val heights = listOf(0.4f, 0.7f, 0.9f, 0.6f, 0.8f, 0.3f, 0.5f)
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    
    // MOCK DATA: Custom Bar Chart
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        heights.forEachIndexed { index, fillPercent ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.height(180.dp)
            ) {
                val isToday = index == 2 // Mocking Wednesday as today, tallest
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height((140 * fillPercent).dp)
                        .background(
                            if (isToday) SoraBlue else SurfaceLight,
                            RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = days[index],
                    color = if (isToday) Color.White else TextGray,
                    fontSize = 12.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun StatsMatrix(state: StatsScreenState.Success) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Total Chapters",
                value = state.chapters.readChapterCount.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Time Spent",
                value = "${state.overview.totalReadDuration.toDuration(DurationUnit.MILLISECONDS).inWholeHours} Hrs",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Current Streak",
                value = "14 Days", // Mock
                modifier = Modifier.weight(1f)
            )
            val avg = if (state.overview.libraryMangaCount > 0) {
                state.chapters.readChapterCount / state.overview.libraryMangaCount
            } else 0
            StatCard(
                title = "Average Read",
                value = "$avg Ch/Title",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(SurfaceDark, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Text(
            text = title,
            color = TextGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GenreDonutChart() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .background(SurfaceDark, RoundedCornerShape(20.dp))
            .padding(24.dp)
    ) {
        Text(
            text = "Genre Breakdown",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mock Donut Chart
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFF1E1E1E), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Inner circle to make it a donut
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(SurfaceDark, CircleShape)
                )
                // In a real implementation this would be a canvas drawing arcs
            }
            
            Spacer(modifier = Modifier.width(24.dp))
            
            // Legend
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GenreLegendItem("Action", "45%", Color(0xFF2D7CFF))
                GenreLegendItem("Romance", "30%", Color(0xFFFF4D4D))
                GenreLegendItem("Fantasy", "15%", Color(0xFFA855F7))
                GenreLegendItem("Other", "10%", Color(0xFF6B7280))
            }
        }
    }
}

@Composable
private fun GenreLegendItem(name: String, percentage: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = percentage,
            color = TextGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MostReadSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Text(
            text = "Most Read Series",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(4) { index ->
                MockSeriesCard(index)
            }
        }
    }
}

@Composable
private fun MockSeriesCard(index: Int) {
    val titles = listOf("Solo Leveling", "One Piece", "Berserk", "Vagabond")
    val hours = listOf(45, 32, 28, 15)
    
    Column(modifier = Modifier.width(120.dp)) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceLight) // Placeholder for cover art
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = titles[index],
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = "${hours[index]} hrs read",
            color = TextGray,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}
