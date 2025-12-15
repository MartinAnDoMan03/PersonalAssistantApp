package com.example.yourassistantyora.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TaskFilterRow(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    categories: List<String>,
    statuses: List<String> = listOf("All", "Waiting", "To do", "In Progress", "Hold On", "Done"),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ✅ Status badge dropdown kecil
        FilterBadgeDropdown(
            value = selectedStatus,
            options = statuses,
            onSelect = onStatusSelected,
            backgroundColor = Color(0xFFF5F5F5),
            textColor = Color(0xFF616161)
        )

        Spacer(Modifier.width(10.dp))

        // ✅ Category badge dropdown kecil
        FilterBadgeDropdown(
            value = selectedCategory,
            options = categories,
            onSelect = onCategorySelected,
            backgroundColor = Color(0xFFE8EAF6),
            textColor = Color(0xFF3949AB)
        )
    }
}

@Composable
private fun FilterBadgeDropdown(
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            color = backgroundColor,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                Spacer(Modifier.width(2.dp))
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier
                        .width(12.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = opt,
                            fontSize = 12.sp
                        )
                    },
                    onClick = {
                        onSelect(opt)
                        expanded = false
                    }
                )
            }
        }
    }
}
