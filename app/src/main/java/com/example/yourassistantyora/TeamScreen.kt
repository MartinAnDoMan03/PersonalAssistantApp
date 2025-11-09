package com.example.yourassistantyora

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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import com.example.yourassistantyora.components.BottomNavigationBar
import com.example.yourassistantyora.utils.NavigationConstants
import com.example.yourassistantyora.TeamColorScheme // IMPORT DARI CREATE TEAM SCREEN

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
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(NavigationConstants.TAB_TEAM) }
    var searchQuery by remember { mutableStateOf("") }

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
            modifier = modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(20.dp, 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("My Teams", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D2D2D))
                    Text("${teams.size} teams", fontSize = 13.sp, color = Color(0xFF9E9E9E))
                }
            }

            item {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search teams...", color = Color(0xFFBDBDBD), fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, "Search", tint = Color(0xFFBDBDBD), modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(12.dp)),
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

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onCreateTeam,
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6A70D7), containerColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp, brush = SolidColor(Color(0xFF6A70D7))),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp), tint = Color(0xFF6A70D7))
                        Spacer(Modifier.width(6.dp))
                        Text("Create Team", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6A70D7))
                    }
                    OutlinedButton(
                        onClick = onJoinTeam,
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6A70D7), containerColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp, brush = SolidColor(Color(0xFF6A70D7))),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.PersonAdd, null, modifier = Modifier.size(18.dp), tint = Color(0xFF6A70D7))
                        Spacer(Modifier.width(6.dp))
                        Text("Join Team", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6A70D7))
                    }
                }
            }

            items(teams) { team -> TeamCard(team = team) }
        }
    }
}

@Composable
fun TeamCard(team: Team, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().clickable { },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(team.colorScheme.gradient))
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(team.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Text(team.description, fontSize = 12.sp, color = Color.White.copy(0.8f))
                        }
                        Icon(Icons.Filled.KeyboardArrowRight, null, tint = Color.White.copy(0.9f), modifier = Modifier.size(24.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Badge(team.category, Color.White.copy(0.2f))
                        Badge(team.role, Color.White.copy(0.2f))
                    }
                }
            }
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.People, null, tint = Color(0xFF616161), modifier = Modifier.size(18.dp))
                        Text("${team.members} members", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF616161))
                    }
                    Text("${(team.progress * 100).toInt()} %", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = team.colorScheme.gradient.last())
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFE0E0E0))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(team.progress).fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(Brush.linearGradient(team.colorScheme.gradient))
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${team.activeTasks} active tasks", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                        Text("${team.completedTasks} completed", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                    }
                }
            }
        }
    }
}

@Composable
fun Badge(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = color, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TeamScreenPreview() {
    YourAssistantYoraTheme { TeamScreen() }
}