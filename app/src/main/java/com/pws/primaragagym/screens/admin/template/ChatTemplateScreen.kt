package com.pws.primaragagym.screens.admin.template

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pws.primaragagym.domain.model.ChatTemplateCategory
import com.pws.primaragagym.domain.model.FirestoreChatTemplate
import com.pws.primaragagym.domain.model.formatChatTemplateMessage
import com.pws.primaragagym.ui.viewmodel.ChatTemplateViewModel
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Phone
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.LaunchedEffect
import com.pws.primaragagym.screens.admin.member.MemberUiModel
import com.pws.primaragagym.screens.admin.member.openWhatsApp
import com.pws.primaragagym.ui.viewmodel.MemberListViewModel

private val GreenAccent = Color(0xFF32A060)
private val GreenLight = Color(0xFFE8F5E9)
private val TextPrimary = Color(0xFF1F2937)
private val TextSecondary = Color(0xFF6B7280)
private val TextMuted = Color(0xFF9CA3AF)
private val CardBackground = Color.White
private val BackgroundColor = Color(0xFFF8FAFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTemplateScreen(
    viewModel: ChatTemplateViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val categories = listOf(
        null, // Semua
        ChatTemplateCategory.BIRTHDAY,
        ChatTemplateCategory.NEVER_CHECKIN,
        ChatTemplateCategory.INACTIVE,
        ChatTemplateCategory.GENERAL
    )

    var templateToEdit by remember { mutableStateOf<FirestoreChatTemplate?>(null) }
    var isAddDialogOpen by remember { mutableStateOf(false) }
    var templateToDelete by remember { mutableStateOf<FirestoreChatTemplate?>(null) }
    var templateToSend by remember { mutableStateOf<FirestoreChatTemplate?>(null) }

    val filteredTemplates = remember(selectedTab, uiState.templates) {
        val targetCat = categories[selectedTab]
        if (targetCat == null) {
            uiState.templates
        } else {
            uiState.templates.filter { it.category == targetCat.code }
        }
    }

    if (templateToDelete != null) {
        AlertDialog(
            onDismissRequest = { templateToDelete = null },
            title = { Text("Hapus Template", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus template \"${templateToDelete?.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = templateToDelete
                        templateToDelete = null
                        if (target != null) {
                            viewModel.deleteTemplate(target.id) { success ->
                                if (success) {
                                    Toast.makeText(context, "Template berhasil dihapus", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Gagal menghapus template", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { templateToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    if (templateToSend != null) {
        KirimTemplateToMemberDialog(
            template = templateToSend!!,
            onDismiss = { templateToSend = null }
        )
    }

    if (isAddDialogOpen || templateToEdit != null) {
        TambahEditChatTemplateDialog(
            template = templateToEdit,
            initialCategory = categories[selectedTab] ?: ChatTemplateCategory.BIRTHDAY,
            onDismiss = {
                isAddDialogOpen = false
                templateToEdit = null
            },
            onSave = { id, title, message, category, isDefault ->
                viewModel.saveTemplate(id, title, message, category, isDefault) { success, error ->
                    if (success) {
                        Toast.makeText(context, "Template berhasil disimpan", Toast.LENGTH_SHORT).show()
                        isAddDialogOpen = false
                        templateToEdit = null
                    } else {
                        Toast.makeText(context, error ?: "Gagal menyimpan", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    Scaffold(
        containerColor = BackgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Template Pesan WhatsApp",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddDialogOpen = true },
                containerColor = GreenAccent,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Tambah Template")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            // Category Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = CardBackground,
                contentColor = GreenAccent,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    if (selectedTab in tabPositions.indices) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = GreenAccent
                        )
                    }
                }
            ) {
                val tabLabels = listOf("Semua", "Ulang Tahun", "Belum Check-in", "Belum Perpanjang", "Umum")
                tabLabels.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = label,
                                color = if (selectedTab == index) GreenAccent else TextSecondary,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            if (uiState.isLoading && uiState.templates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            } else if (filteredTemplates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Chat,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Template",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ketuk tombol + di kanan bawah untuk membuat template pesan baru.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (isTablet) 32.dp else 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (isTablet) {
                        val chunks = filteredTemplates.chunked(2)
                        items(chunks) { rowTemplates ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowTemplates.forEach { template ->
                                    ChatTemplateCard(
                                        template = template,
                                        onSendToMember = { templateToSend = template },
                                        onEdit = { templateToEdit = template },
                                        onDelete = { templateToDelete = template },
                                        onSetDefault = {
                                            viewModel.setDefaultTemplate(template.id, template.category)
                                            Toast.makeText(context, "Disetel sebagai template default", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(2 - rowTemplates.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    } else {
                        items(filteredTemplates, key = { it.id }) { template ->
                            ChatTemplateCard(
                                template = template,
                                onSendToMember = { templateToSend = template },
                                onEdit = { templateToEdit = template },
                                onDelete = { templateToDelete = template },
                                onSetDefault = {
                                    viewModel.setDefaultTemplate(template.id, template.category)
                                    Toast.makeText(context, "Disetel sebagai template default", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatTemplateCard(
    template: FirestoreChatTemplate,
    onSendToMember: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = template.categoryEnum
    val (catBgColor, catTextColor, catIcon) = when (category) {
        ChatTemplateCategory.BIRTHDAY -> Triple(Color(0xFFFCE4EC), Color(0xFFC2185B), Icons.Filled.Cake)
        ChatTemplateCategory.NEVER_CHECKIN -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), Icons.Filled.FitnessCenter)
        ChatTemplateCategory.INACTIVE -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), Icons.Filled.History)
        ChatTemplateCategory.GENERAL -> Triple(Color(0xFFE8F5E9), GreenAccent, Icons.Filled.Chat)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Category Badge + Default Badge + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(catBgColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = catIcon,
                                contentDescription = null,
                                tint = catTextColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = catTextColor
                            )
                        }
                    }

                    if (template.isDefault) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GreenLight)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = GreenAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Default",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = GreenAccent
                                )
                            }
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = TextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = template.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Message Bubble Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF9FAFB))
                    .padding(12.dp)
            ) {
                Text(
                    text = template.message,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 18.sp),
                    color = TextPrimary,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom row: Send to member button & Set as default button or status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onSendToMember,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kirim ke Member",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (!template.isDefault) {
                    OutlinedButton(
                        onClick = onSetDefault,
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, GreenAccent.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenAccent),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Text("Jadikan Default", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Text(
                        text = "Template Default",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                        color = GreenAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun TambahEditChatTemplateDialog(
    template: FirestoreChatTemplate?,
    initialCategory: ChatTemplateCategory,
    onDismiss: () -> Unit,
    onSave: (id: String?, title: String, message: String, category: ChatTemplateCategory, isDefault: Boolean) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    var title by remember { mutableStateOf(template?.title ?: "") }
    var message by remember { mutableStateOf(template?.message ?: "") }
    var selectedCategory by remember { mutableStateOf(template?.categoryEnum ?: initialCategory) }
    var isDefault by remember { mutableStateOf(template?.isDefault ?: false) }
    val isEditing = template != null

    val placeholders = listOf(
        Pair("{nama}", "Nama Member"),
        Pair("{kode}", "Kode Member"),
        Pair("{gym}", "Nama Gym"),
        Pair("{paket}", "Paket Gym"),
        Pair("{expired}", "Tgl Expired"),
        Pair("{usia}", "Usia Member"),
        Pair("{sisa_hari}", "Sisa Hari")
    )

    // Real-time preview calculation with dummy data
    val previewText = remember(message) {
        formatChatTemplateMessage(
            template = message.ifBlank { "Halo Kak {nama}, terima kasih telah bergabung di {gym}!" },
            memberName = "Budi Santoso",
            memberCode = "PRMG-013",
            age = 24,
            planName = "Gold 1 Bulan",
            daysRemaining = 3,
            expiredDate = "25 Okt 2026",
            gymName = "Primaraga Gym"
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 320.dp, max = if (isTablet) 600.dp else 420.dp)
                .fillMaxWidth(if (isTablet) 0.85f else 0.94f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Template Chat" else "Tambah Template Chat",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                Text(text = "Judul Template", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Contoh: Promo Spesial Member", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenAccent,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selection
                Text(text = "Kategori Pesan", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ChatTemplateCategory.entries) { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) GreenAccent else Color(0xFFF3F4F6))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.5.sp
                                ),
                                color = if (isSelected) Color.White else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Data otomatis member (penjelasan ramah pengguna awam)
                Column {
                    Text(
                        text = "Data Otomatis Member (Klik untuk sisipkan):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Akan otomatis terisi dengan data asli member saat chat dikirim.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(placeholders) { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9))
                                .clickable {
                                    message = if (message.isBlank() || message.endsWith(" ")) message + tag.first else "$message " + tag.first
                                }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = GreenAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = tag.second,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    ),
                                    color = GreenAccent
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Message Input
                Text(text = "Isi Pesan Template", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = { Text("Ketik pesan Anda di sini. Gunakan tombol data otomatis di atas agar data member terisi otomatis.", fontSize = 13.sp) },
                    minLines = 4,
                    maxLines = 7,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenAccent,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Switch Set as Default
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gunakan sebagai Default",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Pesan ini akan langsung dipakai saat menekan tombol WhatsApp di dashboard.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GreenAccent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // WhatsApp Live Preview
                Text(text = "Pratinjau Pesan WhatsApp:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEFEAE2)) // WhatsApp chat background
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .align(Alignment.CenterStart)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFDCF8C6)) // WhatsApp outgoing bubble
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = previewText,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                                color = Color(0xFF111B21)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "10:30 ✓✓",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = Color(0xFF667781),
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                return@Button
                            }
                            if (message.isBlank()) {
                                return@Button
                            }
                            onSave(template?.id, title, message, selectedCategory, isDefault)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                        modifier = Modifier.height(40.dp),
                        enabled = title.isNotBlank() && message.isNotBlank()
                    ) {
                        Text("Simpan Template", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun KirimTemplateToMemberDialog(
    template: FirestoreChatTemplate,
    onDismiss: () -> Unit,
    memberViewModel: MemberListViewModel = viewModel()
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val memberUiState by memberViewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedMember by remember { mutableStateOf<MemberUiModel?>(null) }

    LaunchedEffect(Unit) {
        if (memberUiState.members.isEmpty()) {
            memberViewModel.loadMembers()
        }
    }

    val filteredMembers = remember(searchQuery, memberUiState.members) {
        if (searchQuery.isBlank()) {
            memberUiState.members
        } else {
            memberUiState.members.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.memberCode.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery)
            }
        }
    }

    val formattedMessage = remember(selectedMember, template.message) {
        if (selectedMember != null) {
            formatChatTemplateMessage(
                template = template.message,
                memberName = selectedMember!!.name,
                memberCode = selectedMember!!.memberCode,
                planName = selectedMember!!.planName,
                expiredDate = selectedMember!!.expiredDate
            )
        } else ""
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 320.dp, max = if (isTablet) 580.dp else 420.dp)
                .fillMaxWidth(if (isTablet) 0.85f else 0.94f)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kirim Template ke Member",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = template.title,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = GreenAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedMember == null) {
                    // Step 1: Pilih Member
                    Text(
                        text = "Pilih member tujuan:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari nama atau kode member...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Filled.Close, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenAccent,
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (memberUiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Memuat data member...", color = TextSecondary, fontSize = 13.sp)
                        }
                    } else if (filteredMembers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (searchQuery.isNotBlank()) "Member tidak ditemukan" else "Belum ada member terdaftar",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = if (isTablet) 360.dp else 280.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredMembers, key = { it.id }) { m ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedMember = m },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = m.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${m.memberCode} • ${m.phone.ifBlank { "No HP -" }}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                                color = TextSecondary
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Filled.ChevronRight,
                                            contentDescription = null,
                                            tint = GreenAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Step 2: Member Selected -> Preview & Send
                    val m = selectedMember!!

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        border = BorderStroke(1.dp, GreenAccent.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Penerima: ${m.name}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "No. HP: ${m.phone.ifBlank { "-" }} • Kode: ${m.memberCode}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                    color = TextSecondary
                                )
                            }
                            TextButton(onClick = { selectedMember = null }) {
                                Text("Ganti", color = GreenAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Pratinjau Pesan yang Akan Dikirim:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // WhatsApp bubble preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFEFEAE2))
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 14.dp,
                                        topEnd = 14.dp,
                                        bottomStart = 2.dp,
                                        bottomEnd = 14.dp
                                    )
                                )
                                .background(Color(0xFFDCF8C6))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = formattedMessage,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp, lineHeight = 18.sp),
                                color = Color(0xFF111B21)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            openWhatsApp(context, m.phone, m.name, customMessage = formattedMessage)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Buka WhatsApp & Kirim Pesan",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

