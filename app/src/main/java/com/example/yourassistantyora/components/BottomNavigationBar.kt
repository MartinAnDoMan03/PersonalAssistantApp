package com.example.yourassistantyora.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable Bottom Navigation Bar component
 *
 * @param selectedTab Index of currently selected tab (0-3)
 * @param onTabSelected Callback when a tab is clicked, receives tab index
 */
@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = Color.White,
        modifier = modifier.shadow(
            elevation = 10.dp,
            spotColor = Color.Black.copy(alpha = 0.1f)
        )
    ) {
        val items = listOf("Home", "Task", "Note", "Team")
        val icons = listOf(
            Icons.Outlined.Home,
            Icons.Outlined.CheckCircle,
            Icons.Outlined.Description,
            Icons.Outlined.People
        )
        val selectedIcons = listOf(
            Icons.Filled.Home,
            Icons.Filled.CheckCircle,
            Icons.Filled.Description,
            Icons.Filled.People
        )

        items.forEachIndexed { index, item ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) selectedIcons[index] else icons[index],
                        contentDescription = item,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        item,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = isSelected,
                onClick = { onTabSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF6A70D7),
                    selectedTextColor = Color(0xFF6A70D7),
                    unselectedIconColor = Color(0xFF9E9E9E),
                    unselectedTextColor = Color(0xFF9E9E9E),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}