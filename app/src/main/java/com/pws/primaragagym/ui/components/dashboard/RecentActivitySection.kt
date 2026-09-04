package com.pws.primaragagym.ui.components.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pws.primaragagym.ui.theme.DashboardCardBackground
import com.pws.primaragagym.ui.theme.DashboardPrimary
import com.pws.primaragagym.ui.theme.DashboardTextPrimary
import com.pws.primaragagym.ui.theme.DashboardTextSecondary
import com.pws.primaragagym.ui.theme.Dimens
import com.pws.primaragagym.ui.theme.GreenPrimary
import com.pws.primaragagym.ui.theme.StatusActive

data class ActivityItem(
    val userName: String,
    val userInitial: String,
    val role: String,
    val activity: String,
    val date: String,
    val avatarColor: androidx.compose.ui.graphics.Color = GreenPrimary
)

@Composable
fun RecentActivitySection(
    activities: List<ActivityItem>,
    onViewAllClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.card_corner_radius))
            .background(DashboardCardBackground)
            .padding(Dimens.spacing_5)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Aktivitas Terbaru",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = DashboardTextPrimary
            )
            TextButton(onClick = onViewAllClick) {
                Text(
                    text = "Lihat Semua",
                    style = MaterialTheme.typography.bodySmall,
                    color = DashboardPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spacing_4))

        if (isCompact) {
            // Compact list for phone
            activities.forEachIndexed { index, activity ->
                ActivityListItem(activity = activity)
                if (index < activities.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = Dimens.spacing_3),
                        thickness = 1.dp,
                        color = DashboardTextSecondary.copy(alpha = 0.1f)
                    )
                }
            }
        } else {
            // Table header
            ActivityTableHeader()

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Dimens.spacing_3),
                thickness = 1.dp,
                color = DashboardTextSecondary.copy(alpha = 0.15f)
            )

            // Table rows
            activities.forEach { activity ->
                ActivityTableRow(activity = activity)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = Dimens.spacing_3),
                    thickness = 1.dp,
                    color = DashboardTextSecondary.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
private fun ActivityTableHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "User",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = DashboardTextSecondary,
            modifier = Modifier.weight(1.5f)
        )
        Text(
            text = "Role",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = DashboardTextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Aktivitas",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = DashboardTextSecondary,
            modifier = Modifier.weight(2f)
        )
        Text(
            text = "Tanggal",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = DashboardTextSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActivityTableRow(activity: ActivityItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // User column
        Row(
            modifier = Modifier.weight(1.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(activity.avatarColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activity.userInitial,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = activity.avatarColor
                )
            }
            Spacer(modifier = Modifier.width(Dimens.spacing_2))
            Text(
                text = activity.userName,
                style = MaterialTheme.typography.bodySmall,
                color = DashboardTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Role column
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = activity.role,
                style = MaterialTheme.typography.bodySmall,
                color = DashboardTextSecondary
            )
        }

        // Activity column
        Text(
            text = activity.activity,
            style = MaterialTheme.typography.bodySmall,
            color = DashboardTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(2f)
        )

        // Date column
        Text(
            text = activity.date,
            style = MaterialTheme.typography.bodySmall,
            color = DashboardTextSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActivityListItem(activity: ActivityItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(activity.avatarColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = activity.userInitial,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = activity.avatarColor
            )
        }

        Spacer(modifier = Modifier.width(Dimens.spacing_3))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = activity.userName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = DashboardTextPrimary
            )
            Text(
                text = activity.activity,
                style = MaterialTheme.typography.bodySmall,
                color = DashboardTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = activity.date,
            style = MaterialTheme.typography.labelSmall,
            color = DashboardTextSecondary
        )
    }
}
