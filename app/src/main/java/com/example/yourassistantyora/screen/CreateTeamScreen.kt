package com.example.yourassistantyora.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import com.example.yourassistantyora.models.TeamColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.FlowRow
import android.widget.Toast
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateTeamScreen(
    onBackClick: () -> Unit = {},
    viewModel: com.example.yourassistantyora.viewModel.TeamViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onCreateClick: (String, String, List<String>, TeamColorScheme) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    LaunchedEffect(viewModel.isSuccess.value) {
        if(viewModel.isSuccess.value) {
            viewModel.resetState()
            onBackClick()
        }
    }

    val context= LocalContext.current
    LaunchedEffect(viewModel.errorMessage.value) {
        viewModel.errorMessage.value?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    var teamName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf<TeamColorScheme?>(TeamColorScheme.BLUE) }
    var selectedCategories by remember { mutableStateOf(listOf<String>()) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    val predefinedCategories = listOf(
        "Project" to Color(0xFF667EEA),
        "Study" to Color(0xFF64B5F6),
        "Organization" to Color(0xFF4DB6AC)
    )

    var customCategories by remember { mutableStateOf(listOf<Pair<String, Color>>()) }

    val allCategories = predefinedCategories + customCategories

    val isFormValid = teamName.isNotBlank() && description.isNotBlank() &&
            selectedColor != null && selectedCategories.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFF8F9FA),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Create New Team",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D2D2D)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Filled.Close, "Close", tint = Color(0xFF2D2D2D))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFFF8F9FA)
                    )
                )
            }
        ) { paddingValues ->
            Column(modifier = modifier.fillMaxSize().padding(paddingValues)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // TEAM ICON CARD
                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Team Icon",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2D2D2D)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                TeamColorScheme.values().forEach { scheme ->
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Brush.linearGradient(scheme.gradient))
                                            .clickable { selectedColor = scheme }
                                            .then(
                                                if (selectedColor == scheme)
                                                    Modifier.padding(4.dp)
                                                else
                                                    Modifier
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (selectedColor == scheme) {
                                            Icon(
                                                Icons.Filled.Check,
                                                "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TEAM DETAILS CARD
                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "Team Details",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2D2D2D)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Team Name", fontSize = 12.sp, color = Color(0xFF9E9E9E))
                                OutlinedTextField(
                                    value = teamName,
                                    onValueChange = { teamName = it },
                                    placeholder = {
                                        Text("e.g., Mobile Dev Team", color = Color(0xFFBDBDBD), fontSize = 14.sp)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        cursorColor = Color(0xFF6A70D7),
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Description", fontSize = 12.sp, color = Color(0xFF9E9E9E))
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    placeholder = {
                                        Text("What is this team about?", color = Color(0xFFBDBDBD), fontSize = 14.sp)
                                    },
                                    modifier = Modifier.heightIn(min = 90.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        cursorColor = Color(0xFF6A70D7),
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    maxLines = 4,
                                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                                )
                            }
                        }
                    }

                    // CATEGORY CARD (Updated dengan FlowRow)
                    Card(
                        modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Category",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2D2D2D)
                            )

                            // Category chips dengan FlowRow
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Predefined & Custom Categories
                                allCategories.forEach { (category, color) ->
                                    CreateTeamCategoryChip(
                                        text = category,
                                        isSelected = selectedCategories.contains(category),
                                        color = color,
                                        onClick = {
                                            selectedCategories = if (selectedCategories.contains(category)) {
                                                selectedCategories - category
                                            } else {
                                                selectedCategories + category
                                            }
                                        }
                                    )
                                }

                                // Add New Button
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { showAddCategoryDialog = true },
                                    color = Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = "Add Category",
                                            tint = Color(0xFF6A70D7),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Add New",
                                            color = Color(0xFF6A70D7),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF5350)
                        ),
                        border = BorderStroke(1.5.dp, Color(0xFFEF5350)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Cancel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFEF5350)
                        )
                    }
                    Button(
                        onClick = {
                            if (teamName.isNotBlank() && selectedColor != null) {
                                viewModel.createTeam(
                                    name = teamName,
                                    description = description,
                                    categories = selectedCategories,
                                    colorScheme = selectedColor!!
                                )
                            } else {
                                Toast.makeText(context, "Please enter a name and pick a color", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(46.dp),
                        enabled = !viewModel.isLoading.value,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6A70D7),
                            disabledContainerColor = Color(0xFFE0E0E0)
                        ),
                    ) {
                        if (viewModel.isLoading.value) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Create Team",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Add Category Dialog dengan Blur Background
        AnimatedVisibility(
            visible = showAddCategoryDialog,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            // Blur Background Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) { }
            )
        }

        // Dialog Content
        if (showAddCategoryDialog) {
            Dialog(
                onDismissRequest = {
                    showAddCategoryDialog = false
                    newCategoryName = ""
                },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                AnimatedVisibility(
                    visible = showAddCategoryDialog,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            // Title
                            Text(
                                "New Category",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2D2D2D)
                            )

                            // Input Field
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "Category Name",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF9E9E9E)
                                )

                                TextField(
                                    value = newCategoryName,
                                    onValueChange = { newCategoryName = it },
                                    placeholder = {
                                        Text(
                                            "Marketing",
                                            color = Color(0xFFBDBDBD),
                                            fontSize = 14.sp
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = Color(0xFF6A70D7)
                                    ),
                                    textStyle = LocalTextStyle.current.copy(
                                        fontSize = 14.sp,
                                        color = Color(0xFF2D2D2D)
                                    ),
                                    singleLine = true
                                )

                                HorizontalDivider(
                                    color = Color(0xFFE0E0E0),
                                    thickness = 0.5.dp
                                )
                            }

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Cancel Button
                                OutlinedButton(
                                    onClick = {
                                        showAddCategoryDialog = false
                                        newCategoryName = ""
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFFEF5350)
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        width = 1.5.dp,
                                        brush = SolidColor(Color(0xFFEF5350))
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        "Cancel",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFEF5350)
                                    )
                                }

                                // Create Button
                                Button(
                                    onClick = {
                                        if (newCategoryName.isNotEmpty()) {
                                            // Generate random color for new category
                                            val randomColor = listOf(
                                                Color(0xFFE91E63),
                                                Color(0xFF9C27B0),
                                                Color(0xFF3F51B5),
                                                Color(0xFF00BCD4),
                                                Color(0xFF4CAF50),
                                                Color(0xFFFF9800),
                                                Color(0xFFFF5722)
                                            ).random()

                                            customCategories = customCategories + (newCategoryName to randomColor)
                                            selectedCategories = selectedCategories + newCategoryName
                                            showAddCategoryDialog = false
                                            newCategoryName = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp),
                                    enabled = newCategoryName.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF6A70D7),
                                        disabledContainerColor = Color(0xFFE0E0E0)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        "Create",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateTeamCategoryChip(
    text: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        color = if (isSelected) color else color.copy(alpha = 0.15f),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else color,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreateTeamScreenPreview() {
    YourAssistantYoraTheme { CreateTeamScreen() }
}