package com.example.yourassistantyora

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yourassistantyora.components.BottomNavigationBar
import com.example.yourassistantyora.utils.NavigationConstants
import com.example.yourassistantyora.screen.TeamColorScheme
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import androidx.compose.ui.tooling.preview.Preview

// ✅ DATA MODEL TEAM
data class Team(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val colorScheme: TeamColorScheme,
    val members: Int,
    val activeTasks: Int,
    val completedTasks: Int,
    val progress: Float,
    val role: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    onNavigateToTeam: () -> Unit = {},
    onCreateTeam: () -> Unit = {},
    onJoinTeam: () -> Unit = {},
    onTeamClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(NavigationConstants.TAB_TEAM) }
    var searchQuery by remember { mutableStateOf("") }

    // ✅ Contoh data dummy
    val teams = remember {
        listOf(
            Team("1", "Mobile Dev Team", "React Native & Flutter Development", "Project", TeamColorScheme.BLUE, 5, 12, 7, 0.60f, "Admin"),
            Team("2", "Design Squad", "UI/UX Design Team", "Project", TeamColorScheme.PINK, 3, 5, 8, 0.47f, "Member"),
            Team("3", "Study Group CS50", "Computer Science Learning", "Project", TeamColorScheme.GREEN, 8, 15, 10, 0.47f, "Admin")
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { index ->
                    when (index) {
                        NavigationConstants.TAB_HOME -> onNavigateToHome()
                        NavigationConstants.TAB_TASK -> onNavigateToTasks()
                        NavigationConstants.TAB_NOTE -> onNavigateToNotes()
                        NavigationConstants.TAB_TEAM -> onNavigateToTeam()
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp, 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ✅ Header
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "My Teams",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D)
                    )
                    Text(
                        "${teams.size} teams",
                        fontSize = 13.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }

            // ✅ Search bar
            item {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text("Search teams...", color = Color(0xFFBDBDBD), fontSize = 14.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, "Search", tint = Color(0xFFBDBDBD))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF6A70D7)
                    ),
                    singleLine = true
                )
            }

            // ✅ Create + Join button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCreateTeam,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF6A70D7),
                            containerColor = Color.White
                        ),
                        border = BorderStroke(1.5.dp, Color(0xFF6A70D7))
                    ) {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Create Team", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }

                    OutlinedButton(
                        onClick = onJoinTeam,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF6A70D7),
                            containerColor = Color.White
                        ),
                        border = BorderStroke(1.5.dp, Color(0xFF6A70D7))
                    ) {
                        Icon(Icons.Filled.PersonAdd, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Join Team", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // ✅ LIST TEAM CARD
            items(teams) { team ->
                TeamCard(
                    team = team,
                    onClick = { onTeamClick(team.id) }
                )
            }
        }
    }
}

@Composable
fun TeamCard(
    team: Team,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // ✅ Header dengan gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(team.colorScheme.gradient))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Team name & description
                        Text(
                            team.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            team.description,
                            color = Color.White.copy(0.85f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        // Badges
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Badge(team.category)
                            Badge(team.role)
                        }
                    }

                    // Arrow icon
                    Icon(
                        Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White.copy(0.9f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // ✅ Body dengan info members & progress
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Members & percentage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Filled.People,
                            contentDescription = null,
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "${team.members} members",
                            fontSize = 13.sp,
                            color = Color(0xFF616161),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        "${(team.progress * 100).toInt()}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = team.colorScheme.gradient.last()
                    )
                }

                // Progress bar
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFFE0E0E0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(team.progress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(Brush.linearGradient(team.colorScheme.gradient))
                        )
                    }

                    // Active & completed tasks
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${team.activeTasks} active tasks",
                            fontSize = 11.sp,
                            color = Color(0xFF9E9E9E)
                        )
                        Text(
                            "${team.completedTasks} completed",
                            fontSize = 11.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Badge(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.25f)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TeamScreenPreview() {
    YourAssistantYoraTheme {
        TeamScreen()
    }
}