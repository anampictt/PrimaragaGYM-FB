# PRIMARAGA GYM — CLAUDE CODE PROJECT GUIDELINES

## 1. PROJECT OVERVIEW

Project ini adalah aplikasi Android **Primaraga Gym** untuk manajemen operasional gym.

### Platform & stack utama

- Kotlin
- Jetpack Compose
- Material 3
- Clean Architecture
- MVVM
- Hilt
- StateFlow
- Jetpack Compose Navigation
- Firebase Authentication
- Cloud Firestore
- Firebase Storage jika diperlukan

### Role utama

- `SUPER_ADMIN`
- `ADMIN`
- `MEMBER`

Aplikasi harus production-ready, scalable, maintainable, responsive, dan konsisten secara visual.

---

# 2. ATURAN PALING PENTING

Urutan development project:

```text
DESIGN REFERENCE
        ↓
DESIGN SYSTEM
        ↓
UI / UX
        ↓
RESPONSIVE LAYOUT
        ↓
NAVIGATION
        ↓
VIEWMODEL + UI STATE
        ↓
CLEAN ARCHITECTURE
        ↓
FIREBASE AUTH
        ↓
FIRESTORE
        ↓
SECURITY RULES
        ↓
TESTING
```

### Jangan melompat tahap

Pada tahap awal, **fokus utama adalah UI/UX terlebih dahulu**.

Gunakan mock/static data jika diperlukan.

Jangan mengintegrasikan Firestore atau Firebase Authentication hanya untuk membuat UI bekerja.

Firebase baru diintegrasikan setelah struktur UI dan navigation cukup stabil.

---

# 3. WAJIB CEK FOLDER DESIGN

Sebelum membuat atau mengubah UI, **WAJIB memeriksa folder `design/`**.

Folder design berisi referensi UI/UX Primaraga Gym.

Contoh lokasi:

```text
app/src/main/java/com/pws/primaragagym/design/
```

atau lokasi `design/` yang tersedia di project.

### Yang harus dianalisis

Sebelum implementasi UI, periksa seluruh desain yang relevan dan pahami:

- Layout
- Spacing
- Typography
- Warna
- Border radius
- Card
- Button
- Icon
- Header
- Navigation
- Form
- Table/list
- Empty state
- Loading state
- Dialog
- Bottom sheet
- Hierarchy informasi
- Responsive behavior

### Aturan desain

Gunakan desain di folder `design/` sebagai **source of truth visual**.

Jangan membuat desain yang berbeda jauh dari referensi.

Jika suatu screen belum memiliki referensi desain:

1. Gunakan style dari screen yang sudah tersedia.
2. Pertahankan brand identity Primaraga Gym.
3. Buat UI modern, clean, sporty, professional, premium, dan elegant.
4. Jangan menggunakan template admin dashboard generik jika tidak sesuai dengan desain existing.

---

# 4. WAJIB INSPEKSI PROJECT TERLEBIH DAHULU

Sebelum melakukan perubahan besar:

1. Baca struktur project.
2. Baca `build.gradle.kts`.
3. Baca `libs.versions.toml` jika tersedia.
4. Baca `MainActivity`.
5. Baca theme.
6. Baca navigation yang sudah ada.
7. Baca screen yang sudah ada.
8. Baca ViewModel yang sudah ada.
9. Periksa folder `res/font`.
10. Periksa folder `res/drawable`.
11. Periksa folder `design`.
12. Cari component yang sudah ada sebelum membuat component baru.

### Jangan menghancurkan existing work

Jangan menghapus atau memindahkan kode existing secara sembarangan.

Jika existing architecture masih bisa dikembangkan, pertahankan.

Refactor hanya jika memang diperlukan.

---

# 5. BRAND ASSETS

## Font

Font utama aplikasi adalah:

**Plus Jakarta Sans**

Font sudah tersedia di:

```text
app/src/main/res/font/
```

Gunakan file font yang sudah tersedia, misalnya:

```text
plusjakartasans_regular.ttf
plusjakartasans_medium.ttf
plusjakartasans_semibold.ttf
plusjakartasans_bold.ttf
plusjakartasans_extrabold.ttf
```

Jangan menggunakan font default Android untuk typography utama.

Buat typography system yang terpusat.

Contoh:

```text
ui/theme/Type.kt
```

atau ikuti struktur theme existing jika sudah tersedia.

---

# 6. LOGO

Logo Primaraga Gym sudah tersedia di:

```text
app/src/main/res/drawable/
```

Gunakan logo existing.

Jangan menggambar ulang logo menggunakan Text atau Shape.

Logo dapat digunakan pada:

- Splash Screen
- Login Screen
- Navigation / Sidebar
- Header
- Brand section

sesuai desain referensi.

---

# 7. DESIGN SYSTEM

Buat design system yang reusable.

Minimal:

```text
Colors
Typography
Spacing
Shapes
Dimensions
```

Contoh struktur:

```text
ui/theme/
├── Color.kt
├── Type.kt
├── Theme.kt
├── Shape.kt
└── Dimensions.kt
```

Jika project sudah memiliki struktur theme, jangan membuat duplikasi.

### Spacing

Gunakan spacing yang konsisten, misalnya:

```text
4.dp
8.dp
12.dp
16.dp
20.dp
24.dp
32.dp
40.dp
```

Hindari angka random tanpa alasan desain.

---

# 8. RESPONSIVE & ADAPTIVE UI — WAJIB

Aplikasi harus berjalan dengan baik di:

- Small phone
- Normal phone
- Large phone
- Tablet portrait
- Tablet landscape

Target umum:

```text
Compact
< 600dp

Medium
600dp – 839dp

Expanded
>= 840dp
```

Gunakan pendekatan adaptive Compose yang tersedia pada dependency project.

Prioritaskan:

- Window size / available width
- Adaptive navigation
- Responsive grid
- Responsive spacing
- Responsive content width
- Responsive columns

Jangan membuat UI berdasarkan ukuran device tertentu.

Jangan hardcode berdasarkan:

```kotlin
screenWidth == 412.dp
```

atau device tertentu.

---

# 9. PHONE LAYOUT

Untuk phone:

- Gunakan layout compact.
- Gunakan Bottom Navigation jika sesuai desain.
- Gunakan Navigation Drawer jika diperlukan.
- Card dapat menjadi 1 atau 2 kolom sesuai ruang.
- Form dapat menjadi single column.
- List dapat menjadi card/list item.

Contoh konsep:

```text
┌──────────────────────┐
│ Top App Bar          │
├──────────────────────┤
│                      │
│ Content              │
│                      │
│                      │
├──────────────────────┤
│ Home Member More     │
└──────────────────────┘
```

---

# 10. TABLET LAYOUT

Tablet **tidak boleh hanya menjadi versi HP yang diperbesar**.

Tablet harus menggunakan ruang horizontal secara optimal.

Gunakan:

- Navigation Rail
- Permanent Navigation Drawer
- Sidebar
- Multi-column layout
- Dashboard grid
- Split content/detail
- Table jika sesuai

Contoh:

```text
┌──────────────┬─────────────────────────────────┐
│ Sidebar      │ Header                          │
│              │                                 │
│ Dashboard    │ Content                         │
│ Pengguna     │                                 │
│ Role         │                                 │
│ Cabang       │                                 │
│ Member       │                                 │
│ Membership   │                                 │
│ Check-in     │                                 │
└──────────────┴─────────────────────────────────┘
```

Tablet landscape harus memanfaatkan lebar layar.

---

# 11. SUPER ADMIN DASHBOARD

Super Admin Dashboard berfungsi untuk monitoring dan management tingkat sistem.

Informasi utama dapat mencakup:

- Total Users
- Total Members
- Active Members
- Total Branches
- Active Memberships
- Membership Expiring
- Check-in Today
- Recent Activity
- Charts
- Quick Actions

### Phone

Gunakan grid yang compact:

```text
┌────────────┐ ┌────────────┐
│ Members    │ │ Branches   │
└────────────┘ └────────────┘
```

Jika layar terlalu sempit, gunakan satu kolom.

### Tablet

Gunakan layout:

```text
4 statistic cards
+
2-column charts/content
+
full-width activity
```

Jangan membuat dashboard tablet seperti phone yang di-scale.

---

# 12. ADMIN DASHBOARD

Admin fokus pada operasional gym.

Prioritas:

1. Total Member
2. Active Member
3. Check-in Today
4. Membership Expiring
5. Quick Check-in
6. Scan QR
7. Recent Check-in
8. Membership information

Admin Dashboard harus berbeda secara fungsi dari Super Admin Dashboard.

---

# 13. SCREEN YANG PERLU DIKEMBANGKAN

## Authentication

- Splash Screen
- Login
- Forgot Password
- Session Checking

## Super Admin

- Super Admin Dashboard
- Manajemen Pengguna
- Tambah Pengguna
- Detail Pengguna
- Edit Pengguna
- Manajemen Role
- Tambah Role
- Edit Role
- Permission / Hak Akses
- Manajemen Cabang
- Tambah Cabang
- Edit Cabang
- Detail Cabang

## Admin

- Admin Dashboard
- Manajemen Member
- Detail Member
- Membership Plan
- Membership
- Check-in
- Check-out
- QR Scanner
- Riwayat Check-in
- Riwayat Membership

## Member

Jika diperlukan:

- Member Dashboard
- Membership
- QR Member
- Riwayat Check-in
- Profile

Jangan membuat semua screen sekaligus jika belum diperlukan. Implementasikan bertahap.

---

# 14. REUSABLE COMPONENTS

Sebelum membuat component baru, cari component existing terlebih dahulu.

Gunakan reusable component seperti:

```text
AppButton
AppOutlinedButton
AppTextField
AppSearchBar
AppDropdown
AppCard
StatCard
SectionHeader
StatusBadge
MembershipBadge
UserAvatar
EmptyState
LoadingState
ErrorState
AppDialog
AppBottomSheet
AppTopBar
AppNavigationDrawer
AppNavigationRail
AppBottomNavigation
```

Contoh:

```kotlin
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
)
```

Jangan membuat:

```text
CustomButton1
CustomButton2
CustomButtonNew
CustomButtonFinal
```

jika sebenarnya bisa dibuat menjadi satu reusable component.

---

# 15. MVVM

Gunakan MVVM untuk screen yang memiliki state atau business logic.

Pola:

```text
Screen
   ↓
ViewModel
   ↓
UseCase
   ↓
Repository
   ↓
DataSource
```

Contoh:

```text
DashboardScreen
DashboardViewModel
DashboardUiState
DashboardEvent
GetDashboardDataUseCase
DashboardRepository
DashboardRepositoryImpl
```

Composable tidak boleh melakukan business logic kompleks.

Firebase tidak boleh dipanggil langsung dari Composable.

---

# 16. UI STATE

Gunakan UI state yang jelas.

Contoh:

```kotlin
data class DashboardUiState(
    val isLoading: Boolean = false,
    val totalMembers: Int = 0,
    val activeMembers: Int = 0,
    val todayCheckIns: Int = 0,
    val expiringMemberships: Int = 0,
    val error: String? = null
)
```

Screen harus mempertimbangkan:

```text
Loading
Success
Empty
Error
```

---

# 17. STATEFLOW

Gunakan:

```kotlin
StateFlow
MutableStateFlow
```

Untuk Compose, gunakan:

```kotlin
collectAsStateWithLifecycle()
```

jika dependency tersedia.

Hindari memanggil:

```kotlin
StateFlow.value
```

langsung di dalam composition jika menyebabkan state tidak terobservasi dengan benar.

---

# 18. NAVIGATION

Gunakan centralized navigation.

Konsep:

```text
AppNavigation
│
├── AuthGraph
│
├── SuperAdminGraph
│
├── AdminGraph
│
└── MemberGraph
```

Gunakan typed/sealed route jika sesuai dengan versi Navigation yang digunakan project.

Contoh konsep:

```kotlin
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object SuperAdminDashboard : Screen("super_admin_dashboard")
    data object AdminDashboard : Screen("admin_dashboard")
}
```

Jangan menyebarkan route string secara sembarangan.

---

# 19. AUTHENTICATION FLOW

Flow utama:

```text
Splash
   ↓
Check Firebase Auth Session
   ↓
Not Logged In
   ↓
Login
   ↓
Firebase Authentication
   ↓
Get User Profile
   ↓
Get Role
   ↓
Role Based Navigation
```

Hasil:

```text
SUPER_ADMIN
→ Super Admin Dashboard

ADMIN
→ Admin Dashboard

MEMBER
→ Member Dashboard
```

Jangan menentukan role hanya berdasarkan state lokal yang mudah dimanipulasi.

---

# 20. FIREBASE AUTHENTICATION

Gunakan Firebase Authentication.

Untuk tahap awal gunakan:

- Email
- Password

Fitur:

- Login
- Logout
- Forgot Password
- Session
- Current User
- Authentication state

Pisahkan Firebase Authentication dari UI melalui DataSource/Repository.

Contoh:

```text
FirebaseAuthDataSource
AuthRepository
LoginUseCase
LoginViewModel
LoginScreen
```

---

# 21. FIRESTORE

Gunakan Cloud Firestore sebagai database utama.

Collection yang direncanakan:

```text
users
roles
permissions
branches
members
membershipPlans
memberships
checkIns
activities
```

Jangan membuat struktur database secara asal.

Sebelum membuat collection:

1. Identifikasi entity.
2. Identifikasi relationship.
3. Tentukan document ID.
4. Tentukan fields.
5. Tentukan ownership.
6. Tentukan query yang diperlukan.
7. Tentukan index.
8. Tentukan security rules.

---

# 22. ROLE & PERMISSION

Role:

```text
SUPER_ADMIN
ADMIN
MEMBER
```

Contoh:

```text
SUPER_ADMIN
├── Dashboard
├── Pengguna
├── Role
├── Permission
├── Cabang
├── Membership
└── Settings

ADMIN
├── Dashboard
├── Member
├── Membership
├── Check-in
├── Check-out
└── Riwayat

MEMBER
├── Dashboard
├── Membership
├── QR
├── Check-in History
└── Profile
```

### Penting

Menyembunyikan menu di UI **bukan security**.

Authorization tetap harus ditegakkan menggunakan Firebase Security Rules dan/atau mekanisme authorization yang sesuai.

---

# 23. QR SCANNER

QR Scanner digunakan untuk operasional:

```text
Scan QR
   ↓
Validate QR
   ↓
Find Member
   ↓
Validate Membership
   ↓
Validate Check-in Status
   ↓
Check-in / Check-out
   ↓
Save Firestore
   ↓
Show Result
```

Scanner UI harus reusable.

Jangan mengimplementasikan kamera/QR scanner sebelum UI flow-nya jelas.

---

# 24. RESPONSIVE MANAGEMENT SCREEN

Contoh Manajemen Member.

### Phone

```text
┌──────────────────────┐
│ Member Management    │
│ Search               │
│ Filter               │
│                      │
│ Member Card          │
│ Member Card          │
│ Member Card          │
│                      │
├──────────────────────┤
│ Bottom Navigation    │
└──────────────────────┘
```

### Tablet

```text
┌──────────────┬──────────────────────────────────┐
│ Sidebar      │ Member Management                │
│              │                                  │
│ Dashboard    │ Search    Filter     Add Member  │
│ Member       │                                  │
│ Membership   │ ┌──────────────────────────────┐ │
│ Check-in     │ │ Member Table                 │ │
│              │ │                              │ │
│              │ └──────────────────────────────┘ │
└──────────────┴──────────────────────────────────┘
```

Gunakan layout sesuai available width.

---

# 25. FORM DESIGN

Form harus konsisten.

Gunakan:

- Label
- Input
- Validation
- Error message
- Required indicator jika diperlukan
- Keyboard type yang benar
- Password visibility
- Dropdown/select
- Date picker
- Loading state pada submit

Jangan membuat form yang terlalu padat pada phone.

Pada tablet, form dapat menggunakan 2 kolom jika desain dan ruang memungkinkan.

---

# 26. LIST & TABLE

Phone:

- Card/List Item
- Compact information

Tablet:

- Table atau multi-column list jika sesuai desain.

Jangan memaksakan table desktop ke layar phone.

---

# 27. LOADING

Gunakan loading state yang sesuai desain.

Untuk list/dashboard, gunakan skeleton/shimmer jika sesuai.

Hindari hanya:

```text
Loading...
```

jika UI dapat memberikan visual loading yang lebih baik.

---

# 28. EMPTY STATE

Setiap list harus memiliki empty state.

Contoh:

```text
Belum ada member

Belum ada data member yang tersedia.

[ Tambah Member ]
```

Empty state harus tetap mengikuti design system.

---

# 29. ERROR HANDLING

Semua operasi Firebase/API harus memiliki:

```text
Loading
Success
Error
```

Jangan membiarkan exception Firebase menyebabkan aplikasi crash.

Error message harus user-friendly.

Contoh:

```text
Gagal memuat data member.
Silakan coba lagi.
```

Jangan menampilkan stack trace kepada user.

---

# 30. FIREBASE SECURITY

Jangan hardcode credential sensitif.

Gunakan konfigurasi Firebase resmi.

Firestore Security Rules harus berdasarkan role dan ownership.

Konsep:

```text
SUPER_ADMIN
→ Full management access

ADMIN
→ Operational gym access

MEMBER
→ Own data only
```

Security Rules harus dibuat berdasarkan struktur database final.

---

# 31. PERFORMANCE

Hindari:

- Firebase query langsung dari Composable
- Heavy calculation di Composable
- Unnecessary recomposition
- Nested LazyColumn yang tidak diperlukan
- `Column.verticalScroll()` + `LazyColumn` yang tidak diperlukan
- Unnecessary state duplication
- Hardcoded device dimensions
- Loading data berulang tanpa alasan

Gunakan lifecycle-aware state collection.

---

# 32. ACCESSIBILITY

Gunakan:

- Content descriptions
- Proper touch target
- Readable typography
- Sufficient contrast
- Semantic labels jika diperlukan

Jangan menggunakan warna sebagai satu-satunya indikator status.

---

# 33. ICONS

Prioritaskan:

```text
Material Icons
```

Jika icon custom tersedia di asset project, gunakan asset tersebut.

Jangan menggunakan emoji sebagai icon UI production.

---

# 34. CODE QUALITY

Kode harus:

- Idiomatic Kotlin
- Clean
- Readable
- Maintainable
- Testable
- Modular
- Reusable

Gunakan naming yang jelas:

```text
DashboardScreen
DashboardViewModel
DashboardUiState
DashboardEvent
DashboardRepository
DashboardRepositoryImpl
GetDashboardDataUseCase
```

Hindari:

```text
DashboardVM2
DashboardScreenNew
DashboardFinal
DashboardFinal2
```

---

# 35. BEFORE MODIFYING FILE

Sebelum mengubah file:

1. Baca file.
2. Pahami dependency.
3. Cari reference/usage.
4. Pahami state flow.
5. Pastikan perubahan tidak merusak screen lain.
6. Implementasikan perubahan minimal yang diperlukan.
7. Build/check setelah perubahan.

---

# 36. BEFORE CREATING NEW FILE

Pastikan:

- File memang diperlukan.
- Tidak ada file existing dengan fungsi yang sama.
- Tidak membuat duplicate architecture.
- Nama file mengikuti naming convention project.

---

# 37. DEVELOPMENT PHASES

## PHASE 1 — PROJECT & DESIGN ANALYSIS

Periksa:

```text
Project structure
design/
res/font/
res/drawable/
Theme
Navigation
Existing screens
Existing components
```

Output internal:

```text
UI implementation plan
```

---

## PHASE 2 — DESIGN SYSTEM

Implement:

```text
Colors
Typography
Spacing
Shapes
Dimensions
```

Gunakan Plus Jakarta Sans.

---

## PHASE 3 — REUSABLE COMPONENTS

Implement component yang dibutuhkan berdasarkan design reference.

---

## PHASE 4 — UI SCREENS

Implement screen menggunakan mock data.

Urutan:

```text
Splash
↓
Login
↓
Super Admin Dashboard
↓
Super Admin Management
↓
Admin Dashboard
↓
Member Management
↓
Membership
↓
Check-in / Check-out
↓
QR Scanner UI
```

---

## PHASE 5 — RESPONSIVE

Test:

```text
Small Phone
Normal Phone
Large Phone
Tablet Portrait
Tablet Landscape
```

Pastikan:

- Tidak overflow
- Tidak clipped
- Tidak terlalu kecil
- Tidak terlalu besar
- Tidak ada horizontal scrolling yang tidak diperlukan
- Layout berubah secara adaptive

---

## PHASE 6 — NAVIGATION

Implement:

```text
Splash
→ Login
→ Role Based Dashboard
→ Feature Screens
```

---

## PHASE 7 — MVVM

Implement:

```text
UiState
Event
ViewModel
UseCase
Repository
```

---

## PHASE 8 — FIREBASE AUTH

Implement:

```text
Login
Logout
Forgot Password
Session
Role
```

---

## PHASE 9 — FIRESTORE

Implement:

```text
Users
Roles
Permissions
Branches
Members
Membership Plans
Memberships
Check-ins
Activities
```

---

## PHASE 10 — SECURITY & TESTING

Implement:

```text
Firestore Security Rules
Role authorization
Validation
Error handling
Unit tests
UI tests
Navigation tests
```

---

# 38. CURRENT PRIORITY

Untuk kondisi awal project:

## UI FIRST

Checklist:

```text
[ ] Inspect design/
[ ] Inspect existing project
[ ] Inspect existing assets
[ ] Setup Plus Jakarta Sans
[ ] Setup design system
[ ] Setup reusable components
[ ] Build authentication UI
[ ] Build Super Admin UI
[ ] Build Admin UI
[ ] Build management screens
[ ] Build responsive phone layout
[ ] Build responsive tablet layout
[ ] Build tablet landscape layout
[ ] Build navigation
[ ] Verify UI
[ ] Then integrate Firebase
```

**Jangan memulai Firestore sebelum UI/navigation cukup stabil.**

---

# 39. CLAUDE WORKFLOW

Setiap kali user memberikan task:

### STEP 1 — INSPECT

Periksa file yang relevan.

### STEP 2 — DESIGN

Jika task berhubungan dengan UI, periksa `design/`.

### STEP 3 — UNDERSTAND

Pahami existing implementation sebelum mengubahnya.

### STEP 4 — PLAN

Buat rencana singkat perubahan.

### STEP 5 — IMPLEMENT

Implementasikan secara langsung.

### STEP 6 — VERIFY

Lakukan:

- Compile/check
- Inspect imports
- Check state
- Check navigation
- Check responsive layout

### STEP 7 — FIX

Jika ada compile error akibat perubahan, perbaiki.

### STEP 8 — REPORT

Setelah selesai, laporkan secara singkat:

```text
Implemented:
- ...

Files changed:
- ...

Verified:
- ...

Remaining:
- ...
```

---

# 40. DO NOT ASK UNNECESSARY QUESTIONS

Jika requirement sudah jelas, langsung implementasikan.

Jangan menanyakan hal yang sudah dapat diketahui dari:

- Design reference
- Existing project
- Existing assets
- Existing architecture
- CLAUDE.md

Tanyakan hanya jika keputusan tersebut benar-benar berdampak pada:

- Architecture
- Database structure
- Security
- User flow
- Major design decision

---

# 41. DO NOT OVERENGINEER

Jangan membuat abstraction hanya agar project terlihat kompleks.

Hindari abstraction yang tidak diperlukan seperti:

```text
GenericScreenFactory
RepositoryFactory
UseCaseFactory
ViewModelFactory
```

jika tidak memiliki manfaat nyata.

Clean Architecture harus tetap:

```text
Readable
Maintainable
Testable
Scalable
```

---

# 42. IMPORTANT UI PRINCIPLE

Setiap screen harus terasa sebagai bagian dari aplikasi yang sama.

Pertahankan konsistensi:

```text
Typography
Colors
Spacing
Cards
Buttons
Input
Border Radius
Icons
Navigation
Header
Loading
Empty State
Error State
```

Jangan membuat setiap screen memiliki style yang berbeda.

---

# 43. PRIMARAGA GYM BRAND FEEL

Visual aplikasi harus memberikan kesan:

- Modern
- Sporty
- Professional
- Premium
- Clean
- Energetic
- Strong

Gunakan brand asset yang tersedia.

Jangan menghilangkan karakter Primaraga Gym.

---

# 44. FINAL RULE

Jika harus memilih antara:

```text
Cepat selesai
vs
UI konsisten + responsive + maintainable
```

Prioritaskan:

```text
UI konsisten
Responsive
Maintainable
Production-ready
```

Jika harus memilih antara:

```text
Membuat ulang existing code
vs
Mengembangkan existing code
```

Prioritaskan:

```text
Mengembangkan existing code
```

kecuali existing implementation memang rusak atau tidak dapat dipertahankan.

---

# 45. FIRST ACTION WHEN STARTING THIS PROJECT

Ketika Claude pertama kali membuka project ini:

1. Inspect project structure.
2. Inspect `design/`.
3. Inspect `res/font/`.
4. Inspect `res/drawable/`.
5. Inspect existing theme.
6. Inspect existing navigation.
7. Inspect existing screens.
8. Inspect existing ViewModels/components.
9. Jangan langsung membuat Firebase integration.
10. Buat implementation plan.
11. Mulai dari Design System.
12. Lanjutkan ke UI.
13. Pastikan responsive phone + tablet.
14. Setelah UI stabil, lanjut navigation.
15. Setelah navigation stabil, baru lanjut MVVM dan Firebase.

**PRIMARAGA GYM harus dikembangkan sebagai aplikasi production-ready, bukan sekadar prototype UI.**
