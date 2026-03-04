package eu.kanade.tachiyomi.ui.download

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.components.DropdownMenu
import eu.kanade.presentation.components.NestedMenuItem
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.data.download.model.Download
import kotlinx.collections.immutable.persistentListOf
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.Scaffold
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.EmptyScreen

private val SoraBlue = Color(0xFF2977FF)

object DownloadQueueScreen : Screen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { DownloadQueueScreenModel() }
        val downloadList by screenModel.state.collectAsState()
        val isRunning by screenModel.isDownloaderRunning.collectAsState()

        var searchQuery by remember { mutableStateOf("") }
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

        // Flatten all items from all headers
        val allDownloads = remember(downloadList) {
            downloadList.flatMap { header -> header.subItems.map { it.download } }
        }

        val filteredDownloads = remember(allDownloads, searchQuery) {
            if (searchQuery.isBlank()) allDownloads
            else allDownloads.filter {
                it.manga.title.contains(searchQuery, ignoreCase = true) ||
                    it.chapter.name.contains(searchQuery, ignoreCase = true)
            }
        }

        Scaffold(
            topBar = {
                AppBar(
                    title = stringResource(MR.strings.label_download_queue),
                    navigateUp = navigator::pop,
                    actions = {
                        if (downloadList.isNotEmpty()) {
                            // Pause / Resume
                            IconButton(onClick = {
                                if (isRunning) screenModel.pauseDownloads() else screenModel.startDownloads()
                            }) {
                                Icon(
                                    imageVector = if (isRunning) Icons.Outlined.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = SoraBlue,
                                )
                            }

                            var sortExpanded by remember { mutableStateOf(false) }
                            DropdownMenu(
                                expanded = sortExpanded,
                                onDismissRequest = { sortExpanded = false },
                            ) {
                                NestedMenuItem(
                                    text = { Text(text = stringResource(MR.strings.action_order_by_upload_date)) },
                                    children = { closeMenu ->
                                        DropdownMenuItem(
                                            text = { Text(text = stringResource(MR.strings.action_newest)) },
                                            onClick = {
                                                screenModel.reorderQueue({ it.download.chapter.dateUpload }, true)
                                                closeMenu()
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text(text = stringResource(MR.strings.action_oldest)) },
                                            onClick = {
                                                screenModel.reorderQueue({ it.download.chapter.dateUpload }, false)
                                                closeMenu()
                                            },
                                        )
                                    },
                                )
                                NestedMenuItem(
                                    text = { Text(text = stringResource(MR.strings.action_order_by_chapter_number)) },
                                    children = { closeMenu ->
                                        DropdownMenuItem(
                                            text = { Text(text = stringResource(MR.strings.action_asc)) },
                                            onClick = {
                                                screenModel.reorderQueue({ it.download.chapter.chapterNumber }, false)
                                                closeMenu()
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text(text = stringResource(MR.strings.action_desc)) },
                                            onClick = {
                                                screenModel.reorderQueue({ it.download.chapter.chapterNumber }, true)
                                                closeMenu()
                                            },
                                        )
                                    },
                                )
                            }

                            AppBarActions(
                                persistentListOf(
                                    AppBar.Action(
                                        title = stringResource(MR.strings.action_sort),
                                        icon = Icons.AutoMirrored.Outlined.Sort,
                                        onClick = { sortExpanded = true },
                                    ),
                                    AppBar.OverflowAction(
                                        title = stringResource(MR.strings.action_cancel_all),
                                        onClick = { screenModel.clearQueue() },
                                    ),
                                ),
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { contentPadding ->
            if (downloadList.isEmpty()) {
                EmptyScreen(
                    stringRes = MR.strings.information_no_downloads,
                    modifier = Modifier.padding(contentPadding),
                )
                return@Scaffold
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp),
            ) {
                // Search bar
                item(key = "search") {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                // Active Downloads section
                item(key = "active_header") {
                    SectionHeader(
                        title = "Active Downloads",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                items(
                    items = filteredDownloads,
                    key = { it.chapter.id },
                ) { download ->
                    DownloadQueueItem(
                        download = download,
                        onCancel = { screenModel.cancel(listOf(download)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search downloads",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    cursorBrush = SolidColor(SoraBlue),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (query.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(20.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp,
        ),
        modifier = modifier,
    )
}

@Composable
private fun DownloadQueueItem(
    download: Download,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress by download.progressFlow.collectAsState(initial = download.progress)
    val progressFloat by animateFloatAsState(
        targetValue = progress / 100f,
        label = "progress",
    )
    val pages = download.pages
    val pageText = if (pages != null) "${download.downloadedImages}/${pages.size}" else "Queued"
    val isQueued = download.status == Download.State.QUEUE

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Circular progress indicator
            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    progress = { if (isQueued) 0f else progressFloat },
                    modifier = Modifier.size(52.dp),
                    color = SoraBlue,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeWidth = 4.dp,
                )
                Text(
                    text = if (isQueued) "–" else "${(progressFloat * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Manga title + chapter name + progress text
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = download.manga.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = download.chapter.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Progress bar
                if (!isQueued && pages != null) {
                    LinearProgressIndicator(
                        progress = { progressFloat },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(50)),
                        color = SoraBlue,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = pageText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Cancel button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel download",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}
