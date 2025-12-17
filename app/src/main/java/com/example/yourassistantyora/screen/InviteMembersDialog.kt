package com.example.yourassistantyora.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteMembersDialog(
    inviteCode: String,
    teamColor: Color,
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onCopyCode: () -> Unit,
    // We pass the invite code to the logic so we can generate the message
    onSendEmail: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isEmailValid by remember { mutableStateOf(true) }

    // Regex for basic email validation
    fun validateEmail(input: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()
    }

    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
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
                        Text(
                            "Invite Members",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D2D2D)
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                "Close",
                                tint = Color(0xFF757575)
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Invite Code Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Invite Code",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF757575)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(teamColor.copy(alpha = 0.1f)),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    inviteCode,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = teamColor,
                                    letterSpacing = 2.sp
                                )
                                IconButton(
                                    onClick = onCopyCode,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(teamColor, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(
                                        Icons.Filled.ContentCopy,
                                        "Copy",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Divider with "or"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
                        Text(
                            "or",
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
                    }

                    Spacer(Modifier.height(20.dp))

                    // Email Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Invite Via Email",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF757575)
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                isEmailValid = validateEmail(it) || it.isEmpty()
                            },
                            placeholder = {
                                Text(
                                    "member@example.com",
                                    color = Color(0xFFBDBDBD),
                                    fontSize = 14.sp
                                )
                            },
                            isError = !isEmailValid,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = teamColor,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                errorBorderColor = Color.Red,
                                cursorColor = teamColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Send
                            ),
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Email,
                                    null,
                                    tint = if(!isEmailValid) Color.Red else Color(0xFF9E9E9E),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                        )

                        if (!isEmailValid) {
                            Text(
                                text = "Please enter a valid email address",
                                color = Color.Red,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        Button(
                            onClick = {
                                if (validateEmail(email)) {
                                    onSendEmail(email)
                                    // Don't verify here, let the external logic launch the app
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = email.isNotBlank() && isEmailValid,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = teamColor,
                                disabledContainerColor = Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Filled.Send,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Send Invitation",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
