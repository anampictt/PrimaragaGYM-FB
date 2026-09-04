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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pws.primaragagym.screens.admin.member.MemberColors.BackgroundColor
import com.pws.primaragagym.screens.admin.member.MemberColors.CardBackground
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenAccent
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenLight
import com.pws.primaragagym.screens.admin.member.MemberColors.TextMuted
import com.pws.primaragagym.screens.admin.member.MemberColors.TextPrimary
import com.pws.primaragagym.screens.admin.member.MemberColors.TextSecondary
import com.pws.primaragagym.ui.theme.Dimens

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipManagementScreen(
    onBackClick: () -> Unit = {},
    onMembershipClick: (String) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val horizontalPadding = if (isTablet) 32.dp else Dimens.screen_padding_horizontal

    var searchQuery by remember { mutableStateOf("") }

    val filteredMembers = remember(searchQuery) {
        dummyMembers.filter { member ->
            searchQuery.isBlank() ||
                    member.name.contains(searchQuery, ignoreCase = true) ||
                    member.memberCode.contains(searchQuery, ignoreCase = true) ||
                    member.planName.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Manajemen Membership",
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Field
            MembershipSearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = horizontalPadding,
                        vertical = Dimens.spacing_4
                    )
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = Dimens.spacing_2,
                    bottom = Dimens.spacing_5
                )
            ) {
                if (filteredMembers.isEmpty()) {
                    item {
                        EmptyMembershipState()
                    }
                } else {
                    items(
                        items = filteredMembers,
                        key = { it.id }
                    ) { member ->
                        MembershipMemberCard(
                            member = member,
                            onClick = { onMembershipClick(member.id) }
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// MEMBERSHIP SEARCH FIELD
// ============================================================================
@Composable
private fun MembershipSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = TextPrimary
            ),
            cursorBrush = SolidColor(GreenAccent),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "Cari nama / ID member...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }

                innerTextField()
            }
        )
    }
}

// ============================================================================
// EMPTY STATE
// ============================================================================
@Composable
private fun EmptyMembershipState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.spacing_8),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Membership tidak ditemukan",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Coba kata kunci lain",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
    }
}
// ============================================================================
@Composable
private fun MembershipMemberCard(
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    text = member.planName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${member.startDate} - ${member.expiredDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    MemberStatusBadge(status = member.status)
                }
            }
        }
    }
}
