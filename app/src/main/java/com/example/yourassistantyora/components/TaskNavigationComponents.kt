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
import androidx.compose.foundation.shape.CircleShape
// ---------- VIEW MODE NAVIGATION ----------
@Composable
fun TaskViewModeNavigation(
    selectedViewMode: String,
    onViewModeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
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
        val tabs = listOf(
            Triple(Icons.Outlined.List, "List", onNavigateToList),
            Triple(Icons.Outlined.DateRange, "Daily", onNavigateToDaily),
            Triple(Icons.Outlined.CalendarMonth, "Weekly", onNavigateToWeekly),
            Triple(Icons.Outlined.CalendarToday, "Monthly", onNavigateToMonthly)
        )

        tabs.forEach { (icon, text, action) ->
            ViewModeTab(
                icon = icon,
                text = text,
                isSelected = selectedViewMode == text,
                onClick = {
                    onViewModeChange(text)
                    action?.invoke()
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

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
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
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
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}

// ---------- FILTER ROW (STATUS + CATEGORY) ----------
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
        // ✅ PERBAIKAN: Weight dinaikkan ke 1.4f agar teks "In Progress" punya ruang cukup
        StatusDropdown(
            selectedStatus = selectedStatus,
            onStatusSelected = onStatusSelected,
            modifier = Modifier.weight(1.4f)
        )

        // Category chips: Weight dikurangi sedikit untuk memberi ruang pada dropdown
        Row(
            modifier = Modifier
                .weight(2.6f)
                .horizontalScroll(rememberScrollState()),
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
                    }
                )
            }
        }
    }
}

// ---------- STATUS DROPDOWN ----------
@Composable
private fun StatusDropdown(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Konfigurasi warna dropdown (Match dengan gambar referensi)
    val statusOptions = listOf(
        StatusOption("All", null, Color.White, Color(0xFF6A70D7)),
        StatusOption("Waiting", Color(0xFFFDF2F9), Color(0xFF7B1FA2), Color(0xFFE1BEE7)),
        StatusOption("To do", Color(0xFFE3F2FD), Color(0xFF1976D2), Color(0xFFBBDEFB)),
        StatusOption("Hold On", Color(0xFFFFFDE7), Color(0xFFF57F17), Color(0xFFFFF9C4)),
        StatusOption("In Progress", Color(0xFFE0F2F1), Color(0xFF00796B), Color(0xFFB2DFDB)),
        StatusOption("Done", Color(0xFFE8F5E9), Color(0xFF388E3C), Color(0xFFC8E6C9))
    )

    val currentOption = statusOptions.find { it.name == selectedStatus } ?: statusOptions[0]

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = !expanded },
            color = currentOption.chipBgColor,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = null,
                    tint = currentOption.textColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                // ✅ PERBAIKAN: MaxLines dan SoftWrap ditambahkan agar tetap satu baris
                Text(
                    text = if (selectedStatus == "All") "Status" else selectedStatus,
                    color = currentOption.textColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = currentOption.textColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
        ) {
            statusOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(option.name, fontSize = 13.sp, color = option.textColor)
                    },
                    onClick = {
                        onStatusSelected(option.name)
                        expanded = false
                    },
                    leadingIcon = {
                        Box(Modifier.size(12.dp).background(option.chipBgColor, CircleShape))
                    }
                )
            }
        }
    }
}

private data class StatusOption(
    val name: String,
    val bgColor: Color?, // Untuk item di list
    val textColor: Color,
    val chipBgColor: Color // Untuk tampilan tombol utama
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
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) Color(0xFF6A70D7) else Color(0xFFF5F7FA),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) Color.White else Color(0xFF666666),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}