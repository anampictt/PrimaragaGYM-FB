package com.pws.primaragagym.screens.admin.member

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pws.primaragagym.screens.admin.member.MemberColors.BackgroundColor
import com.pws.primaragagym.screens.admin.member.MemberColors.CardBackground
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenAccent
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenLight
import com.pws.primaragagym.screens.admin.member.MemberColors.TextMuted
import com.pws.primaragagym.screens.admin.member.MemberColors.TextPrimary
import com.pws.primaragagym.screens.admin.member.MemberColors.TextSecondary
import com.pws.primaragagym.ui.theme.Dimens

// ============================================================================
// FILTER TYPES
// ============================================================================
private enum class MemberFilter(val displayName: String) {
    ALL("Semua"),
    ACTIVE("Active"),
    EXPIRING("Expiring Soon"),
    EXPIRED("Expired"),
    SUSPENDED("Suspended")
}

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberManagementScreen(
    onBackClick: () -> Unit = {},
    onAddMemberClick: () -> Unit = {},
    onMemberClick: (String) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(MemberFilter.ALL) }

    // Filter members
    val filteredMembers = remember(searchQuery, selectedFilter) {
        dummyMembers.filter { member ->
            val matchesSearch = searchQuery.isBlank() ||
                    member.name.contains(searchQuery, ignoreCase = true) ||
                    member.memberCode.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                MemberFilter.ALL -> true
                MemberFilter.ACTIVE -> member.status == MemberStatus.ACTIVE
                MemberFilter.EXPIRING -> member.status == MemberStatus.EXPIRING_SOON
                MemberFilter.EXPIRED -> member.status == MemberStatus.EXPIRED
                MemberFilter.SUSPENDED -> member.status == MemberStatus.SUSPENDED
            }

            matchesSearch && matchesFilter
        }
    }

    val horizontalPadding = if (isTablet) 32.dp else Dimens.screen_padding_horizontal

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            MemberManagementTopBar(onBackClick = onBackClick)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMemberClick,
                containerColor = GreenAccent,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Registrasi Member Baru"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Field
            MemberSearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = horizontalPadding,
                        vertical = Dimens.spacing_4
                    )
            )

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(
                    horizontal = horizontalPadding
                ),
                modifier = Modifier.padding(bottom = Dimens.spacing_4)
            ) {
                items(MemberFilter.entries) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter.displayName,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenAccent,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Member Count
            Text(
                text = "${filteredMembers.size} member",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(
                    horizontal = horizontalPadding,
                    vertical = Dimens.spacing_2
                )
            )

            // Member List
            if (filteredMembers.isEmpty()) {
                MemberEmptyState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        bottom = 80.dp
                    )
                ) {
                    items(
                        items = filteredMembers,
                        key = { it.id }
                    ) { member ->
                        MemberManagementCard(
                            member = member,
                            onClick = { onMemberClick(member.id) }
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// TOP APP BAR
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberManagementTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Manajemen Member",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPrimary
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = TextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CardBackground
        )
    )
}

// ============================================================================
// SEARCH FIELD
// ============================================================================
@Composable
private fun MemberSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Cari nama / ID member...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = TextPrimary
                    ),
                    cursorBrush = SolidColor(GreenAccent),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Search
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ============================================================================
// MEMBER CARD
// ============================================================================
@Composable
private fun MemberManagementCard(
    member: MemberUiModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(GreenLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.avatarInitial,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = GreenAccent
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = member.memberCode,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = member.planName,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    MemberStatusBadge(status = member.status)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Expired: ${member.expiredDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

// ============================================================================
// EMPTY STATE
// ============================================================================
@Composable
private fun MemberEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Belum ada member",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = TextSecondary
            )
        }
    }
}
