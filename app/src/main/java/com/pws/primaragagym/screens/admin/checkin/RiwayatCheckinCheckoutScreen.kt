package com.pws.primaragagym.screens.admin.checkin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pws.primaragagym.domain.model.FirestoreCheckin
import com.pws.primaragagym.ui.viewmodel.AuthViewModel
import com.pws.primaragagym.ui.viewmodel.CheckinFirestoreViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val BackgroundColor = Color(0xFFF5F7FA)
private val CardBackground = Color.White
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B6B6B)
private val TextMuted = Color(0xFF9E9E9E)
private val IconBackground = Color(0xFFE8F5E9)
private val GreenAccent = Color(0xFF32A060)
private val OrangeAccent = Color(0xFFF57C00)
private val BlueAccent = Color(0xFF1976D2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatCheckinCheckoutScreen(
    viewModel: CheckinFirestoreViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onBackClick: () -> Unit,
    onMemberClick: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val authState by authViewModel.uiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val branchId = authState.currentUser?.branchId ?: ""
                viewModel.loadAllCheckins(branchId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(authState.currentUser) {
        val branchId = authState.currentUser?.branchId ?: ""
        viewModel.loadAllCheckins(branchId)
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") }

    val rawCheckins = uiState.allCheckins
    val calendarToday = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    // Filter data
    val filteredCheckins = rawCheckins.filter { checkin ->
        val date = checkin.checkInAt ?: checkin.createdAt
        val matchesFilter = when (selectedFilter) {
            "Hari Ini" -> date != null && !date.before(calendarToday)
            "Sedang di Gym" -> checkin.status.equals("CHECKED_IN", ignoreCase = true)
            "Sudah Check-out" -> checkin.status.equals("CHECKED_OUT", ignoreCase = true)
            else -> true
        }

        val q = searchQuery.trim()
        val matchesSearch = if (q.isEmpty()) {
            true
        } else {
            checkin.memberName.contains(q, ignoreCase = true) ||
            checkin.memberCode.contains(q, ignoreCase = true)
        }

        matchesFilter && matchesSearch
    }

    // Counters
    val totalCheckinToday = rawCheckins.count {
        val d = it.checkInAt ?: it.createdAt
        d != null && !d.before(calendarToday)
    }
    val totalActiveNow = rawCheckins.count { it.status.equals("CHECKED_IN", ignoreCase = true) }
    val totalCheckedOut = rawCheckins.count { it.status.equals("CHECKED_OUT", ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Riwayat Check-in & Check-out",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        containerColor = BackgroundColor
    ) { paddingValues ->
        val contentPadding = if (isTablet) 24.dp else 16.dp

        if (uiState.isLoading && rawCheckins.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenAccent)
            }
        } else {
            if (isTablet) {
                // TABLET LAYOUT: 2-Column Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(contentPadding),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header span across 2 columns
                    item(span = { GridItemSpan(2) }) {
                        Column {
                            HeaderSummarySection(
                                totalToday = totalCheckinToday,
                                totalActive = totalActiveNow,
                                totalOut = totalCheckedOut
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            SearchAndFilterBar(
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                selectedFilter = selectedFilter,
                                onFilterSelected = { selectedFilter = it }
                            )
                        }
                    }

                    if (filteredCheckins.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            EmptyHistoryCard(searchQuery = searchQuery, selectedFilter = selectedFilter)
                        }
                    } else {
                        items(filteredCheckins, key = { it.checkinId }) { checkin ->
                            RiwayatVisitCard(
                                checkin = checkin,
                                onClick = {
                                    val idToOpen = checkin.memberCode.ifBlank { checkin.memberId }
                                    if (idToOpen.isNotBlank()) onMemberClick(idToOpen)
                                }
                            )
                        }
                    }
                }
            } else {
                // PHONE LAYOUT: Single Column
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(contentPadding),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        HeaderSummarySection(
                            totalToday = totalCheckinToday,
                            totalActive = totalActiveNow,
                            totalOut = totalCheckedOut
                        )
                    }

                    item {
                        SearchAndFilterBar(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            selectedFilter = selectedFilter,
                            onFilterSelected = { selectedFilter = it }
                        )
                    }

                    if (filteredCheckins.isEmpty()) {
                        item {
                            EmptyHistoryCard(searchQuery = searchQuery, selectedFilter = selectedFilter)
                        }
                    } else {
                        items(filteredCheckins, key = { it.checkinId }) { checkin ->
                            RiwayatVisitCard(
                                checkin = checkin,
                                onClick = {
                                    val idToOpen = checkin.memberCode.ifBlank { checkin.memberId }
                                    if (idToOpen.isNotBlank()) onMemberClick(idToOpen)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// HEADER SUMMARY METRICS
// ============================================================================
@Composable
private fun HeaderSummarySection(
    totalToday: Int,
    totalActive: Int,
    totalOut: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryStatCard(
            modifier = Modifier.weight(1f),
            label = "Check-in Hari Ini",
            value = totalToday.toString(),
            icon = Icons.Filled.Login,
            iconTint = GreenAccent,
            bgColor = Color(0xFFE8F5E9)
        )
        SummaryStatCard(
            modifier = Modifier.weight(1f),
            label = "Sedang di Gym",
            value = totalActive.toString(),
            icon = Icons.Filled.FitnessCenter,
            iconTint = OrangeAccent,
            bgColor = Color(0xFFFFF3E0)
        )
        SummaryStatCard(
            modifier = Modifier.weight(1f),
            label = "Selesai Keluar",
            value = totalOut.toString(),
            icon = Icons.Filled.Logout,
            iconTint = BlueAccent,
            bgColor = Color(0xFFE3F2FD)
        )
    }
}

@Composable
private fun SummaryStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    bgColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ============================================================================
// SEARCH AND FILTER BAR
// ============================================================================
@Composable
private fun SearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Cari nama atau ID member (misal: PRG128910)...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = GreenAccent) },
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedBorderColor = GreenAccent,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.8f)
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filterOptions = listOf("Semua", "Hari Ini", "Sedang di Gym", "Sudah Check-out")
            filterOptions.forEach { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { onFilterSelected(filter) },
                    label = {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenAccent.copy(alpha = 0.15f),
                        selectedLabelColor = GreenAccent
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) GreenAccent else Color.LightGray.copy(alpha = 0.7f),
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }
    }
}

// ============================================================================
// VISIT HISTORY CARD
// ============================================================================
@Composable
private fun RiwayatVisitCard(
    checkin: FirestoreCheckin,
    onClick: () -> Unit
) {
    val isCurrentlyInGym = checkin.status.equals("CHECKED_IN", ignoreCase = true)
    val checkInDate = checkin.checkInAt ?: checkin.createdAt
    val checkOutDate = checkin.checkOutAt

    val dateFormatter = remember { SimpleDateFormat("EEEE, dd MMM yyyy", Locale("id", "ID")) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm 'WIB'", Locale("id", "ID")) }

    val formattedDate = checkInDate?.let { dateFormatter.format(it) } ?: "Tanggal tidak diketahui"
    val formattedCheckInTime = checkInDate?.let { timeFormatter.format(it) } ?: "-"
    val formattedCheckOutTime = checkOutDate?.let { timeFormatter.format(it) } ?: "Sedang Gym"

    // Hitung durasi kunjungan
    val durationText = remember(checkInDate, checkOutDate, isCurrentlyInGym) {
        val start = checkInDate
        if (start == null) {
            "-"
        } else {
            val end = checkOutDate ?: Date()
            val diffMillis = (end.time - start.time).coerceAtLeast(0)
            val minutes = diffMillis / (1000 * 60)
            val hours = minutes / 60
            val remMinutes = minutes % 60

            when {
                hours > 0 && remMinutes > 0 -> "$hours Jam $remMinutes Menit"
                hours > 0 -> "$hours Jam"
                minutes > 0 -> "$minutes Menit"
                else -> "< 1 Menit"
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Member Avatar, Name, Code, and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isCurrentlyInGym) Color(0xFFFFF3E0) else IconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = checkin.memberName.take(2).uppercase().ifBlank { "MB" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isCurrentlyInGym) OrangeAccent else GreenAccent
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = checkin.memberName.ifBlank { "Member Gym" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = checkin.memberCode.ifBlank { checkin.memberId },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = GreenAccent
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Badge
                if (isCurrentlyInGym) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFF3E0))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Sedang di Gym",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = OrangeAccent
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Selesai",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GreenAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Date Row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Timing Box (Check-in time vs Check-out time)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF9FAFB))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Jam Masuk
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Login,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Jam Masuk",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedCheckInTime,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                // Divider vertical
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(Color.LightGray.copy(alpha = 0.5f))
                )

                // Jam Keluar
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Logout,
                            contentDescription = null,
                            tint = if (isCurrentlyInGym) OrangeAccent else BlueAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Jam Keluar",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedCheckOutTime,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrentlyInGym) OrangeAccent else TextPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Duration Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Durasi Kunjungan:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCurrentlyInGym) "$durationText (berjalan)" else durationText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isCurrentlyInGym) OrangeAccent else TextPrimary
                    )
                }

                Text(
                    text = "Lihat Detail →",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = GreenAccent
                )
            }
        }
    }
}

// ============================================================================
// EMPTY STATE
// ============================================================================
@Composable
private fun EmptyHistoryCard(
    searchQuery: String,
    selectedFilter: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F7FA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (searchQuery.isNotBlank()) "Member Tidak Ditemukan" else "Belum Ada Riwayat Kunjungan",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (searchQuery.isNotBlank()) {
                    "Tidak ada catatan check-in untuk kata kunci '$searchQuery'."
                } else {
                    "Data check-in dan check-out untuk filter '$selectedFilter' akan tercatat di sini secara otomatis."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
