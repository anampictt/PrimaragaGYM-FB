package com.pws.primaragagym.screens.admin.notifikasi

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pws.primaragagym.data.repository.MemberRepositoryImpl
import com.pws.primaragagym.domain.model.ChatTemplateCategory
import com.pws.primaragagym.domain.model.FirestoreMember
import com.pws.primaragagym.domain.model.FirestoreNotification
import com.pws.primaragagym.domain.model.formatChatTemplateMessage
import com.pws.primaragagym.domain.repository.MemberRepository
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.BackgroundColor
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.CardBackground
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.GreenAccent
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.GreenLight
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.TextMuted
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.TextPrimary
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.TextSecondary
import com.pws.primaragagym.screens.admin.notifikasi.NotifikasiColors.UnreadBackground
import com.pws.primaragagym.ui.theme.Dimens
import com.pws.primaragagym.ui.viewmodel.ChatTemplateUiState
import com.pws.primaragagym.ui.viewmodel.ChatTemplateViewModel
import com.pws.primaragagym.ui.viewmodel.NotificationDisplayMapper
import com.pws.primaragagym.ui.viewmodel.NotifikasiViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.util.Calendar

// ============================================================================
// FILTER TYPES & CONFIG
// ============================================================================
private enum class NotifikasiFilter(val label: String) {
    ALL("Semua"),
    MEMBERSHIP("Membership"),
    MEMBER("Member"),
    SISTEM("Sistem")
}

private data class ActionButtonConfig(
    val label: String,
    val containerColor: Color
)

// ============================================================================
// MAIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifikasiScreen(
    onBackClick: () -> Unit = {},
    onNotificationClick: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    branchId: String? = null,
    viewModel: NotifikasiViewModel = viewModel(),
    templateViewModel: ChatTemplateViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val uiState by viewModel.uiState.collectAsState()
    val templateState by templateViewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf(NotifikasiFilter.ALL) }
    var processingNotificationId by remember { mutableStateOf<String?>(null) }
    val memberRepository: MemberRepository = remember { MemberRepositoryImpl() }

    // Mulai observasi notifikasi dari Firestore
    LaunchedEffect(branchId) {
        viewModel.observeNotifications(branchId = branchId)
    }

    val filteredNotifications = uiState.notifications.filter { notif ->
        when (selectedFilter) {
            NotifikasiFilter.ALL -> true
            NotifikasiFilter.MEMBERSHIP -> NotificationDisplayMapper.getCategoryLabel(notif.type).equals("Membership", ignoreCase = true)
            NotifikasiFilter.MEMBER -> NotificationDisplayMapper.getCategoryLabel(notif.type).equals("Member", ignoreCase = true)
            NotifikasiFilter.SISTEM -> NotificationDisplayMapper.getCategoryLabel(notif.type).equals("Sistem", ignoreCase = true)
        }
    }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val paginatedNotifications = com.pws.primaragagym.ui.components.common.rememberPaginatedList(
        items = filteredNotifications,
        pageSize = 10,
        resetKey = selectedFilter
    )
    com.pws.primaragagym.ui.components.common.BindPagination(listState, paginatedNotifications)

    Scaffold(
        containerColor = BackgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Notifikasi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Pantau aktivitas dan kondisi member.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    if (uiState.unreadCount > 0) {
                        TextButton(
                            onClick = { viewModel.markAllAsRead(branchId) }
                        ) {
                            Text(
                                text = "Tandai semua dibaca",
                                style = MaterialTheme.typography.labelMedium,
                                color = GreenAccent
                            )
                        }
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Pengaturan",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenLight,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            contentPadding = PaddingValues(
                horizontal = if (isTablet) 32.dp else Dimens.screen_padding_horizontal,
                vertical = Dimens.spacing_4
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing_3)
        ) {
            // Filter Chips (Scrollable for full phone responsiveness)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacing_2)
                ) {
                    NotifikasiFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = filter.label,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenAccent,
                                selectedLabelColor = Color.White,
                                containerColor = CardBackground,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }

            // Loading state
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacing_8),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GreenAccent)
                    }
                }
            } else if (filteredNotifications.isEmpty()) {
                item {
                    EmptyNotificationState()
                }
            } else {
                items(paginatedNotifications.visibleItems, key = { it.notificationId }) { notification ->
                    RealNotificationCard(
                        notification = notification,
                        isActionLoading = processingNotificationId == notification.notificationId,
                        onClick = {
                            if (!notification.isRead) {
                                viewModel.markAsRead(notification.notificationId)
                            }
                        },
                        onActionClick = {
                            handleNotificationAction(
                                context = context,
                                notification = notification,
                                memberRepository = memberRepository,
                                templateState = templateState,
                                scope = scope,
                                onLoadingChange = { loading ->
                                    processingNotificationId = if (loading) notification.notificationId else null
                                },
                                onMarkAsRead = {
                                    if (!notification.isRead) {
                                        viewModel.markAsRead(notification.notificationId)
                                    }
                                }
                            )
                        }
                    )
                }

                if (paginatedNotifications.isLoadingMore) {
                    item(key = "pagination_loading") {
                        com.pws.primaragagym.ui.components.common.PaginationLoadingItem()
                    }
                } else if (!paginatedNotifications.hasMore && paginatedNotifications.totalCount > 10) {
                    item(key = "pagination_end") {
                        com.pws.primaragagym.ui.components.common.PaginationEndOfListItem(totalCount = paginatedNotifications.totalCount)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(Dimens.spacing_4))
            }
        }
    }
}

// ============================================================================
// REAL NOTIFICATION CARD (dari Firestore)
// ============================================================================
@Composable
private fun RealNotificationCard(
    notification: FirestoreNotification,
    isActionLoading: Boolean = false,
    onClick: () -> Unit,
    onActionClick: () -> Unit
) {
    val (icon, iconBg, iconColor) = NotificationDisplayMapper.getIconAndColors(notification.type)
    val timeLabel = NotificationDisplayMapper.formatTime(notification.createdAt)
    val isUnread = !notification.isRead

    val actionInfo = when (notification.type) {
        "MEMBER_BIRTHDAY" -> ActionButtonConfig("Kirim Ucapan", GreenAccent)
        "MEMBER_INACTIVE", "NEVER_CHECKIN", "MEMBER_NEVER_CHECKIN" -> ActionButtonConfig("Hubungi Member", Color(0xFFEF6C00))
        "MEMBERSHIP_EXPIRING" -> ActionButtonConfig("Ingatkan Member", Color(0xFFD32F2F))
        else -> null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Dimens.card_corner_radius),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) UnreadBackground else CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnread) 2.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacing_4),
            verticalAlignment = Alignment.Top
        ) {
            // Unread indicator dot
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(GreenAccent)
                        .align(Alignment.Top)
                )
                Spacer(modifier = Modifier.width(Dimens.spacing_2))
            }

            // Icon circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(Dimens.spacing_3))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                // Action button if applicable
                if (actionInfo != null) {
                    Spacer(modifier = Modifier.height(Dimens.spacing_3))
                    Button(
                        onClick = onActionClick,
                        shape = RoundedCornerShape(Dimens.button_corner_radius),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = actionInfo.containerColor,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp),
                        enabled = !isActionLoading
                    ) {
                        if (isActionLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = actionInfo.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// EMPTY STATE
// ============================================================================
@Composable
private fun EmptyNotificationState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.spacing_8),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_4))
            Text(
                text = "Belum ada notifikasi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(Dimens.spacing_2))
            Text(
                text = "Notifikasi akan muncul di sini.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

// ============================================================================
// HELPER: CALCULATE AGE & HANDLE WHATSAPP ACTION
// ============================================================================
private fun calculateMemberAge(dob: String): Int? {
    if (dob.isBlank()) return null
    val yearRegex = Regex("""\b(19\d\d|20\d\d)\b""")
    val match = yearRegex.find(dob) ?: return null
    val birthYear = match.value.toIntOrNull() ?: return null
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val age = currentYear - birthYear
    return if (age in 1..120) age else null
}

private fun handleNotificationAction(
    context: Context,
    notification: FirestoreNotification,
    memberRepository: MemberRepository,
    templateState: ChatTemplateUiState,
    scope: CoroutineScope,
    onLoadingChange: (Boolean) -> Unit,
    onMarkAsRead: () -> Unit
) {
    onMarkAsRead()
    onLoadingChange(true)

    scope.launch {
        try {
            // 1. Resolve member
            var member: FirestoreMember? = null

            // First try memberId if available
            if (!notification.memberId.isNullOrBlank()) {
                member = memberRepository.getMemberById(notification.memberId).getOrNull()
            }

            // Fallback: search by name extracted from message
            if (member == null) {
                val candidateName = when (notification.type) {
                    "MEMBER_BIRTHDAY" -> {
                        notification.message
                            .substringBefore(" berulang")
                            .substringBefore(" ulang tahun")
                            .trim()
                    }
                    "MEMBER_INACTIVE", "NEVER_CHECKIN", "MEMBER_NEVER_CHECKIN" -> {
                        notification.message
                            .substringBefore(" tidak check-in")
                            .substringBefore(" belum check-in")
                            .trim()
                    }
                    "MEMBERSHIP_EXPIRING" -> {
                        notification.message
                            .substringBefore(":")
                            .substringBefore(" membership")
                            .trim()
                    }
                    else -> ""
                }

                if (candidateName.isNotBlank() && !candidateName.equals("Member", ignoreCase = true)) {
                    val searchResult = memberRepository.searchMembers(candidateName, limit = 5).getOrNull()
                    member = searchResult?.firstOrNull {
                        it.fullName.equals(candidateName, ignoreCase = true)
                    } ?: searchResult?.firstOrNull()

                    // If still null, try listing members to find match
                    if (member == null) {
                        val allMembers = memberRepository.getMembers(limit = 100).getOrNull()
                        member = allMembers?.firstOrNull {
                            it.fullName.contains(candidateName, ignoreCase = true) ||
                            candidateName.contains(it.fullName, ignoreCase = true)
                        }
                    }
                }
            }

            // 2. Validate member & phone
            if (member == null) {
                Toast.makeText(context, "Data member tidak ditemukan", Toast.LENGTH_SHORT).show()
                onLoadingChange(false)
                return@launch
            }

            val digits = member.phoneNumber.filter { it.isDigit() }
            if (digits.isBlank()) {
                Toast.makeText(context, "Nomor WhatsApp untuk ${member.fullName} belum terisi", Toast.LENGTH_SHORT).show()
                onLoadingChange(false)
                return@launch
            }

            val formattedPhone = when {
                digits.startsWith("0") -> "62" + digits.substring(1)
                digits.startsWith("62") -> digits
                digits.startsWith("8") -> "62$digits"
                else -> digits
            }

            // 3. Resolve template message according to notification type
            val messageToSend = when (notification.type) {
                "MEMBER_BIRTHDAY" -> {
                    val template = templateState.templates.find { it.categoryEnum == ChatTemplateCategory.BIRTHDAY && it.isDefault }?.message
                        ?: templateState.templates.find { it.categoryEnum == ChatTemplateCategory.BIRTHDAY }?.message
                        ?: "Halo Kak {nama}, segenap keluarga {gym} mengucapkan Selamat Ulang Tahun! 🎂🎉 Semoga sehat selalu, panjang umur, dan semakin bersemangat berolahraga bersama {gym}! 💪"

                    val age = calculateMemberAge(member.dateOfBirth)
                    formatChatTemplateMessage(
                        template = template,
                        memberName = member.fullName,
                        memberCode = member.memberCode,
                        age = age,
                        planName = member.planName,
                        daysRemaining = 0,
                        expiredDate = member.expiredDate
                    )
                }
                "MEMBER_INACTIVE", "NEVER_CHECKIN", "MEMBER_NEVER_CHECKIN" -> {
                    val template = templateState.templates.find { it.categoryEnum == ChatTemplateCategory.NEVER_CHECKIN && it.isDefault }?.message
                        ?: templateState.templates.find { it.categoryEnum == ChatTemplateCategory.NEVER_CHECKIN }?.message
                        ?: templateState.templates.find { it.categoryEnum == ChatTemplateCategory.INACTIVE && it.isDefault }?.message
                        ?: templateState.templates.find { it.categoryEnum == ChatTemplateCategory.INACTIVE }?.message
                        ?: "Halo Kak {nama}, kami dari tim {gym} melihat Kakak sudah cukup lama tidak datang latihan nih. Ada yang bisa kami bantu? Ditunggu kedatangannya ya Kak! 💪🔥"

                    formatChatTemplateMessage(
                        template = template,
                        memberName = member.fullName,
                        memberCode = member.memberCode,
                        planName = member.planName,
                        expiredDate = member.expiredDate
                    )
                }
                "MEMBERSHIP_EXPIRING" -> {
                    val template = templateState.templates.find { it.categoryEnum == ChatTemplateCategory.INACTIVE && it.isDefault }?.message
                        ?: templateState.templates.find { it.categoryEnum == ChatTemplateCategory.INACTIVE }?.message
                        ?: "Halo Kak {nama}, masa membership Kakak di {gym} akan segera berakhir. Yuk perpanjang sekarang agar tetap rutin berolahraga! 💪"

                    formatChatTemplateMessage(
                        template = template,
                        memberName = member.fullName,
                        memberCode = member.memberCode,
                        planName = member.planName,
                        expiredDate = member.expiredDate
                    )
                }
                else -> {
                    notification.message
                }
            }

            // 4. Open WhatsApp
            val encoded = URLEncoder.encode(messageToSend, "UTF-8")
            val uri = Uri.parse("https://wa.me/$formattedPhone?text=$encoded")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)

        } catch (e: Exception) {
            Toast.makeText(context, "Tidak dapat membuka WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            onLoadingChange(false)
        }
    }
}
