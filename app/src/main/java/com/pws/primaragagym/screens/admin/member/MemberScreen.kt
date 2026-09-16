package com.pws.primaragagym.screens.admin.member

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import com.pws.primaragagym.ui.components.shimmerEffect
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Style
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.pws.primaragagym.ui.viewmodel.MemberListViewModel
import com.pws.primaragagym.ui.viewmodel.AuthViewModel
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenAccent
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenLight
import com.pws.primaragagym.screens.admin.member.MemberColors.TextPrimary
import com.pws.primaragagym.screens.admin.member.MemberColors.TextSecondary
import com.pws.primaragagym.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberScreen(
    viewModel: MemberListViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onMemberManagementClick: () -> Unit = {},
    onMembershipManagementClick: () -> Unit = {},
    onMembershipPlanClick: () -> Unit = {},
    onMemberClick: (String) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    val authState by authViewModel.uiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(authState.currentUser) {
        val branchId = authState.currentUser?.branchId
        if (branchId != null) {
            viewModel.loadMembers(branchId, reset = true)
        }
    }

    Scaffold(
        containerColor = MemberColors.BackgroundColor,
        topBar = {
            MemberTopBar(onBackClick = onBackClick)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = if (isTablet) 32.dp else Dimens.screen_padding_horizontal,
                    vertical = Dimens.spacing_5
                )
        ) {
            // Menu Cards
            MemberMenuCard(
                title = "Manajemen Member",
                description = "Kelola data dan informasi member",
                icon = Icons.Filled.Groups,
                onClick = onMemberManagementClick
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_3))

            MemberMenuCard(
                title = "Paket Membership",
                description = "Kelola paket membership gym",
                icon = Icons.Filled.Style,
                onClick = onMembershipPlanClick
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_6))

            // Member Terbaru Section
            Text(
                text = "Member Terbaru",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPrimary,
                modifier = Modifier.padding(bottom = Dimens.spacing_4)
            )

            if (isTablet) {
                if (uiState.isLoading && uiState.members.isEmpty()) {
                    val rows = (1..6).chunked(3)
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
                        ) {
                            rowItems.forEach {
                                MemberHubCardShimmer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(Dimens.spacing_3))
                    }
                } else {
                    val recentMembers = uiState.members.take(6)
                    val rows = recentMembers.chunked(3)
                    rows.forEach { rowMembers ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
                        ) {
                            rowMembers.forEach { member ->
                                MemberHubCard(
                                    member = member,
                                    onClick = { onMemberClick(member.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(3 - rowMembers.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(Dimens.spacing_3))
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_3),
                    contentPadding = PaddingValues(end = Dimens.spacing_4)
                ) {
                    if (uiState.isLoading && uiState.members.isEmpty()) {
                        items(4) {
                            MemberHubCardShimmer(modifier = Modifier.width(200.dp))
                        }
                    } else {
                        items(uiState.members.take(6)) { member ->
                            MemberHubCard(
                                member = member,
                                onClick = { onMemberClick(member.id) },
                                modifier = Modifier.width(200.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_6))
        }
    }
}

// ============================================================================
// TOP APP BAR
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Member",
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
            containerColor = MemberColors.CardBackground
        )
    )
}

// ============================================================================
// MENU CARD
// ============================================================================
@Composable
private fun MemberMenuCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Dimens.card_corner_radius),
        colors = CardDefaults.cardColors(
            containerColor = MemberColors.CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_4),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(GreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(Dimens.spacing_4))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = GreenAccent,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ============================================================================
// MEMBER HUB CARD
// ============================================================================
@Composable
private fun MemberHubCard(
    member: MemberUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBgColor = when (member.status) {
        MemberStatus.EXPIRED -> Color(0xFFFFF5F5)
        MemberStatus.EXPIRING_SOON -> Color(0xFFFFFBEA)
        MemberStatus.SUSPENDED -> Color(0xFFF9FAFB)
        MemberStatus.ACTIVE -> MemberColors.CardBackground
    }

    val cardBorderColor = when (member.status) {
        MemberStatus.EXPIRED -> Color(0xFFFFCDD2)
        MemberStatus.EXPIRING_SOON -> Color(0xFFFFE082)
        MemberStatus.SUSPENDED -> Color(0xFFE5E7EB)
        MemberStatus.ACTIVE -> Color(0xFFE8F5E9)
    }

    val avatarBgColor = when (member.status) {
        MemberStatus.EXPIRED -> Color(0xFFFFE0E0)
        MemberStatus.EXPIRING_SOON -> Color(0xFFFFF3E0)
        MemberStatus.SUSPENDED -> Color(0xFFE5E7EB)
        MemberStatus.ACTIVE -> GreenLight
    }

    val avatarTextColor = when (member.status) {
        MemberStatus.EXPIRED -> Color(0xFFC62828)
        MemberStatus.EXPIRING_SOON -> Color(0xFFE65100)
        MemberStatus.SUSPENDED -> TextSecondary
        MemberStatus.ACTIVE -> GreenAccent
    }

    val expiredTextColor = when (member.status) {
        MemberStatus.EXPIRED -> Color(0xFFD32F2F)
        MemberStatus.EXPIRING_SOON -> Color(0xFFE65100)
        else -> TextSecondary
    }

    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor
        ),
        border = BorderStroke(1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_3)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(avatarBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (!member.photoUrl.isNullOrBlank()) {
                        coil.compose.AsyncImage(
                            model = com.pws.primaragagym.ui.components.normalizeImageUrl(member.photoUrl),
                            contentDescription = "Foto ${member.name}",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = member.avatarInitial,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = avatarTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Dimens.spacing_3))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = member.memberCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_3))

            Text(
                text = member.planName,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_2))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                MemberStatusBadge(status = member.status)
                val formattedExpired = formatExpiredDateDisplay(member.expiredDate, member.createdAt)
                Text(
                    text = "Exp: $formattedExpired",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (member.status == MemberStatus.EXPIRED || member.status == MemberStatus.EXPIRING_SOON) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = expiredTextColor
                )
            }

            val createdText = when {
                member.createdAt != null -> {
                    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale("id", "ID")).apply {
                        timeZone = TimeZone.getTimeZone("Asia/Jakarta")
                    }
                    sdf.format(member.createdAt)
                }
                member.startDate.isNotBlank() -> member.startDate
                else -> null
            }
            if (createdText != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Dibuat: $createdText",
                    style = MaterialTheme.typography.labelSmall,
                    color = MemberColors.TextMuted
                )
            }
        }
    }
}

@Composable
private fun MemberHubCardShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MemberColors.CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_3)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.width(Dimens.spacing_3))

                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.45f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacing_3))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )

            Spacer(modifier = Modifier.height(Dimens.spacing_2))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
        }
    }
}

// ============================================================================
// STATUS BADGE
// ============================================================================
@Composable
fun MemberStatusBadge(status: MemberStatus) {
    val bgColor = when (status) {
        MemberStatus.ACTIVE -> GreenLight
        MemberStatus.EXPIRING_SOON -> Color(0xFFFFF3E0)
        MemberStatus.EXPIRED -> Color(0xFFFFEBEE)
        MemberStatus.SUSPENDED -> Color(0xFFF5F5F5)
    }
    val textColor = when (status) {
        MemberStatus.ACTIVE -> GreenAccent
        MemberStatus.EXPIRING_SOON -> Color(0xFFFF9800)
        MemberStatus.EXPIRED -> Color(0xFFF44336)
        MemberStatus.SUSPENDED -> TextSecondary
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(textColor)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = status.displayName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = textColor
        )
    }
}
