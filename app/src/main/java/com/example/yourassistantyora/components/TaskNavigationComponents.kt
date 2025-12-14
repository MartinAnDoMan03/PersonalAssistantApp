package com.example.yourassistantyora.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------- VIEW MODE NAVIGATION (REUSABLE) ----------
@Composable
fun TaskViewModeNavigation(
    selectedViewMode: String,
    onViewModeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    // Callback untuk navigasi ke halaman berbeda
    onNavigateToList: (() -> Unit)? = null,
    onNavigateToDaily: (() -> Unit)? = null,
    onNavigateToWeekly: (() -> Unit)? = null,
    onNavigateToMonthly: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ViewModeTab(
            icon = Icons.Outlined.List,
            text = "List",
            isSelected = selectedViewMode == "List",
            onClick = {
                onViewModeChange("List")
                onNavigateToList?.invoke()
            },
            modifier = Modifier.weight(1f)
        )
        ViewModeTab(
            icon = Icons.Outlined.DateRange,
            text = "Daily",
            isSelected = selectedViewMode == "Daily",
            onClick = {
                onViewModeChange("Daily")
                onNavigateToDaily?.invoke()
            },
            modifier = Modifier.weight(1f)
        )
        ViewModeTab(
            icon = Icons.Outlined.CalendarMonth,
            text = "Weekly",
            isSelected = selectedViewMode == "Weekly",
            onClick = {
                onViewModeChange("Weekly")
                onNavigateToWeekly?.invoke()
            },
            modifier = Modifier.weight(1f)
        )
        ViewModeTab(
            icon = Icons.Outlined.CalendarToday,
            text = "Monthly",
            isSelected = selectedViewMode == "Monthly",
            onClick = {
                onViewModeChange("Monthly")
                onNavigateToMonthly?.invoke()
            },
            modifier = Modifier.weight(1f)
        )
    }
}

// ---------- VIEW MODE TAB ----------
@Composable
private fun ViewModeTab(
    icon: ImageVector,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) Color(0xFF6A70D7) else Color(0xFFF5F7FA),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (isSelected) Color.White else Color(0xFF666666),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = text,
                color = if (isSelected) Color.White else Color(0xFF666666),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

// ---------- FILTER ROW (STATUS + CATEGORY) - REUSABLE ----------
@Composable
fun TaskFilterRow(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    categories: List<String> = listOf("All", "Work", "Study", "Project")
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status Dropdown: Weight 1f untuk align dengan tab pertama
        StatusDropdown(
            selectedStatus = selectedStatus,
            onStatusSelected = onStatusSelected,
            modifier = Modifier.weight(1f)
        )

        // Category chips: Weight 3f untuk fill sisa space.
        // Hapus weight(1f) dari chip di dalam agar bisa scroll.
        Row(
            modifier = Modifier
                .weight(3f) // Memastikan Row ini mengambil 3/4 lebar
                .horizontalScroll(rememberScrollState()), // Mengaktifkan scroll
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            categories.forEach { category ->
                FilterChipCompact(
                    text = category,
                    isSelected = selectedCategory == category,
                    onClick = {
                        onCategorySelected(
                            if (category == "All" || selectedCategory != category) category else "All"
                        )
                    },
                    // ✅ PERBAIKAN: Hapus Modifier.weight(1f) di sini.
                    // Ini memungkinkan chip untuk menentukan lebarnya berdasarkan teks, bukan dipaksa stretch.
                    modifier = Modifier
                )
            }
        }
    }
}

// ---------- STATUS DROPDOWN (UKURAN MEDIUM) ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusDropdown(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val statusOptions = listOf(
        StatusOption("All", null),
        StatusOption("Waiting", Color(0xFFE1BEE7)),
        StatusOption("To do", Color(0xFFBBDEFB)),
        StatusOption("Hold On", Color(0xFFFFF9C4)),
        StatusOption("In Progress", Color(0xFFB2DFDB)),
        StatusOption("Done", Color(0xFFC8E6C9))
    )

    // Mapping warna untuk button berdasarkan selectedStatus (background)
    val buttonColor = when (selectedStatus) {
        "Waiting" -> Color(0xFFE1BEE7)
        "To do" -> Color(0xFFBBDEFB)
        "Hold On" -> Color(0xFFFFF9C4)
        "In Progress" -> Color(0xFFB2DFDB)
        "Done" -> Color(0xFFC8E6C9)
        else -> Color(0xFF6A70D7) // Default untuk "All"
    }

    // Mapping warna teks/icon untuk button - match dengan warna di menu
    val textColor = when (selectedStatus) {
        "Waiting" -> Color(0xFF7B1FA2)
        "To do" -> Color(0xFF1976D2)
        "Hold On" -> Color(0xFFF57F17)
        "In Progress" -> Color(0xFF00796B)
        "Done" -> Color(0xFF388E3C)
        else -> Color.White // Default untuk "All"
    }
    val iconTint = textColor // Icon arrow ikut warna teks

    Box(modifier = modifier) {
        // Tombol Dropdown
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = !expanded },
            color = buttonColor,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Tambah icon FilterList di depan (seperti icon di tabs)
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = "Filter",
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (selectedStatus == "All") "Status" else selectedStatus,
                    color = textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(4.dp))
                // Arrow di kanan, size 16.dp
                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Dropdown Menu: Lebar disesuaikan konten (tidak ada width(220.dp)), ukuran medium
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            // Menghapus .width(220.dp) agar lebar menyesuaikan konten
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp), // Ukuran Medium
                verticalArrangement = Arrangement.spacedBy(8.dp) // Ukuran Medium
            ) {
                statusOptions.forEach { option ->
                    val isSelected = selectedStatus == option.name

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onStatusSelected(option.name)
                                expanded = false
                            },
                        color = option.color ?: Color(0xFFF5F7FA),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp), // Ukuran Medium
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option.name,
                                fontSize = 13.sp, // Ukuran Medium
                                color = if (option.color != null) {
                                    when(option.name) {
                                        "Waiting" -> Color(0xFF7B1FA2)
                                        "To do" -> Color(0xFF1976D2)
                                        "Hold On" -> Color(0xFFF57F17)
                                        "In Progress" -> Color(0xFF00796B)
                                        "Done" -> Color(0xFF388E3C)
                                        else -> Color(0xFF666666)
                                    }
                                } else Color(0xFF666666),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF6A70D7),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class StatusOption(
    val name: String,
    val color: Color?
)

// ---------- FILTER CHIP COMPACT ----------
@Composable
private fun FilterChipCompact(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        // Catatan: Modifier hanya berisi yang dilewatkan dari luar (tidak ada weight)
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) Color(0xFF6A70D7) else Color(0xFFF5F7FA),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp), // Padding horizontal sedikit ditambah agar chip tidak terlalu mepet
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) Color.White else Color(0xFF666666),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                // Pastikan teks tidak dibungkus, ini akan memaksa chip melebar
                maxLines = 1
            )
        }
    }
}