package com.pws.primaragagym.ui.components.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Settings

import com.pws.primaragagym.ui.theme.DashboardPrimary
import com.pws.primaragagym.ui.theme.DashboardTextSecondary
import com.pws.primaragagym.ui.theme.Dimens
import com.pws.primaragagym.ui.theme.GreenPrimary
import com.pws.primaragagym.ui.theme.GreenPrimaryDark

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val isSelected: Boolean = false,
    val onClick: () -> Unit = {}
)

@Composable
fun SuperAdminNavigationRail(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navigationItems = listOf(
        NavigationItemData.DASHBOARD,
        NavigationItemData.USERS,
        NavigationItemData.ROLE,
        NavigationItemData.BRANCH,
        NavigationItemData.MEMBER,
        NavigationItemData.MEMBERSHIP,
        NavigationItemData.SCHEDULE,
        NavigationItemData.CHECK_IN_OUT,
        NavigationItemData.REPORTS,
        NavigationItemData.SETTINGS
    )

    Column(
        modifier = modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(Color.White)
            .padding(vertical = Dimens.spacing_4),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo section
        Column(
            modifier = Modifier
                .padding(horizontal = Dimens.spacing_4)
                .padding(bottom = Dimens.spacing_4),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PRIMARAGA",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = GreenPrimary
            )
            Text(
                text = "GYM",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GreenPrimaryDark
            )
        }

        Spacer(modifier = Modifier.height(Dimens.spacing_2))

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = Dimens.spacing_4),
            thickness = 1.dp,
            color = Color(0xFFE8E8E8)
        )

        Spacer(modifier = Modifier.height(Dimens.spacing_4))

        // Navigation items
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            navigationItems.forEach { item ->
                NavItem(
                    item = item,
                    isSelected = selectedItem == item.title,
                    onClick = { onItemSelected(item.title) }
                )
            }
        }
    }
}

@Composable
fun SuperAdminBottomNavigation(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navigationItems = listOf(
        NavigationItemData.DASHBOARD,
        NavigationItemData.USERS,
        NavigationItemData.ROLE,
        NavigationItemData.BRANCH,
        NavigationItemData.MEMBER,
        NavigationItemData.MEMBERSHIP
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = Dimens.spacing_2),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        navigationItems.forEach { item ->
            NavItemCompact(
                item = item,
                isSelected = selectedItem == item.title,
                onClick = { onItemSelected(item.title) }
            )
        }
    }
}

@Composable
private fun NavItem(
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        DashboardPrimary.copy(alpha = 0.1f)
    } else {
        Color.Transparent
    }

    val contentColor = if (isSelected) {
        DashboardPrimary
    } else {
        DashboardTextSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.spacing_3)
            .clip(RoundedCornerShape(Dimens.spacing_2))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.spacing_4, vertical = Dimens.spacing_3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(Dimens.spacing_3))
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor
        )
    }
}

@Composable
private fun NavItemCompact(
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (isSelected) {
        DashboardPrimary
    } else {
        DashboardTextSecondary
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor
        )
    }
}

object NavigationItemData {
    val DASHBOARD = NavigationItem(
        title = "Dashboard",
        icon = Icons.Filled.Home
    )
    val USERS = NavigationItem(
        title = "Pengguna",
        icon = Icons.Filled.People
    )
    val ROLE = NavigationItem(
        title = "Role",
        icon = Icons.Filled.Badge
    )
    val BRANCH = NavigationItem(
        title = "Cabang",
        icon = Icons.Filled.Store
    )
    val MEMBER = NavigationItem(
        title = "Member",
        icon = Icons.Filled.Person
    )
    val MEMBERSHIP = NavigationItem(
        title = "Membership",
        icon = Icons.Filled.CreditCard
    )
    val SCHEDULE = NavigationItem(
        title = "Jadwal",
        icon = Icons.Filled.CalendarMonth
    )
    val CHECK_IN_OUT = NavigationItem(
        title = "Check In/Out",
        icon = Icons.Filled.QrCodeScanner
    )
    val REPORTS = NavigationItem(
        title = "Laporan",
        icon = Icons.Filled.Assessment
    )
    val SETTINGS = NavigationItem(
        title = "Pengaturan",
        icon = Icons.Filled.Settings
    )
}
