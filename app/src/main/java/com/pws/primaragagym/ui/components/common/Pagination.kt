package com.pws.primaragagym.ui.components.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Representasi status pagination list data.
 */
class PaginatedListState<T>(
    val visibleItems: List<T>,
    val isLoadingMore: Boolean,
    val hasMore: Boolean,
    val totalCount: Int,
    private val onLoadNext: () -> Unit,
    private val onReset: () -> Unit
) {
    fun loadNextPage() {
        if (!isLoadingMore && hasMore) {
            onLoadNext()
        }
    }

    fun reset() {
        onReset()
    }
}

/**
 * Hook Compose untuk mengelola data list dengan pagination & infinite scrolling.
 *
 * @param items Seluruh item hasil filter / query
 * @param pageSize Jumlah item yang ditampilkan per halaman (default 10)
 * @param loadDelayMillis Delay simulasi pemuatan halus sebelum batch data berikutnya muncul (default 500ms)
 * @param onLoadMoreFromSource Callback jika ingin memuat data tambahan dari Firestore / remote
 * @param resetKey Kunci yang memicu reset halaman (misal: searchQuery, tab, filter status)
 */
@Composable
fun <T> rememberPaginatedList(
    items: List<T>,
    pageSize: Int = 10,
    loadDelayMillis: Long = 500L,
    onLoadMoreFromSource: (() -> Unit)? = null,
    resetKey: Any? = Unit
): PaginatedListState<T> {
    val scope = rememberCoroutineScope()
    var currentPage by remember(resetKey) { mutableIntStateOf(1) }
    var isLoadingMore by remember(resetKey) { mutableStateOf(false) }

    val totalCount = items.size
    val visibleCount = (currentPage * pageSize).coerceAtMost(totalCount)
    val visibleItems = remember(items, visibleCount) {
        items.take(visibleCount)
    }

    val hasMore = visibleCount < totalCount || (onLoadMoreFromSource != null && !isLoadingMore)

    val onLoadNext = {
        if (!isLoadingMore && hasMore) {
            isLoadingMore = true
            scope.launch {
                if (loadDelayMillis > 0) {
                    delay(loadDelayMillis)
                }
                if (visibleCount < totalCount) {
                    currentPage += 1
                }
                onLoadMoreFromSource?.invoke()
                isLoadingMore = false
            }
        }
    }

    val onReset = {
        currentPage = 1
        isLoadingMore = false
    }

    return remember(visibleItems, isLoadingMore, hasMore, totalCount) {
        PaginatedListState(
            visibleItems = visibleItems,
            isLoadingMore = isLoadingMore,
            hasMore = hasMore,
            totalCount = totalCount,
            onLoadNext = onLoadNext,
            onReset = onReset
        )
    }
}

/**
 * Helper pagination untuk LazyListState (digunakan pada LazyColumn) agar mendeteksi ketika pengguna
 * scroll mendekati akhir list dan secara otomatis memuat data berikutnya.
 */
@Composable
fun BindPagination(
    listState: LazyListState,
    paginatedState: PaginatedListState<*>,
    buffer: Int = 2
) {
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0)
            totalItemsNumber > 0 &&
                    lastVisibleItemIndex >= (totalItemsNumber - 1 - buffer) &&
                    paginatedState.hasMore &&
                    !paginatedState.isLoadingMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            paginatedState.loadNextPage()
        }
    }
}

/**
 * Extension untuk LazyListState (digunakan pada LazyColumn) agar mendeteksi ketika pengguna
 * scroll mendekati akhir list dan secara otomatis memuat data berikutnya.
 */
@JvmName("bindPaginationListExtension")
@Composable
fun LazyListState.BindPagination(
    paginatedState: PaginatedListState<*>,
    buffer: Int = 2
) {
    BindPagination(listState = this, paginatedState = paginatedState, buffer = buffer)
}

/**
 * Helper pagination untuk LazyGridState (digunakan pada LazyVerticalGrid untuk tablet)
 */
@Composable
fun BindPagination(
    gridState: LazyGridState,
    paginatedState: PaginatedListState<*>,
    buffer: Int = 4
) {
    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0)
            totalItemsNumber > 0 &&
                    lastVisibleItemIndex >= (totalItemsNumber - 1 - buffer) &&
                    paginatedState.hasMore &&
                    !paginatedState.isLoadingMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            paginatedState.loadNextPage()
        }
    }
}

/**
 * Extension untuk LazyGridState (digunakan pada LazyVerticalGrid untuk tablet)
 */
@JvmName("bindPaginationGridExtension")
@Composable
fun LazyGridState.BindPagination(
    paginatedState: PaginatedListState<*>,
    buffer: Int = 4
) {
    BindPagination(gridState = this, paginatedState = paginatedState, buffer = buffer)
}

/**
 * Komponen kartu loading pagination yang muncul di bagian bawah daftar saat scrolling.
 */
@Composable
fun PaginationLoadingItem(
    modifier: Modifier = Modifier,
    text: String = "Memuat data lainnya...",
    indicatorColor: Color = Color(0xFF10B981)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF0FDF4))
                .border(BorderStroke(1.dp, Color(0xFFDCFCE7)), RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = indicatorColor,
                strokeWidth = 2.dp
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFF065F46)
            )
        }
    }
}

/**
 * Komponen indikator bahwa semua data telah dimuat.
 * Hanya ditampilkan jika totalCount melebihi 1 halaman data.
 */
@Composable
fun PaginationEndOfListItem(
    totalCount: Int,
    modifier: Modifier = Modifier,
    text: String = "Semua $totalCount data telah ditampilkan"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            ),
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )
    }
}
