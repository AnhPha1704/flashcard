package com.example.flashcard.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashcard.R
import com.example.flashcard.ui.viewmodel.AuthViewModel
import com.example.flashcard.ui.theme.*

@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // --- Full-screen backdrop (kể cả status bar) ---
    Box(modifier = Modifier.fillMaxSize().background(NeoBackgroundPink)) {
        // Full-screen solid background

        // Nội dung bên trong mới bị đẩy xuống dưới status bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Box { // Box to stack shadow behind Surface
                // Shadow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 7.dp, y = 7.dp)
                        .background(NeoNavy, RoundedCornerShape(16.dp))
                )
                // Main card
                Surface(
                    color = NeoBackgroundPink,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(3.dp, NeoNavy),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isSignUpMode) "Đăng ký" else "Đăng nhập",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = NeoNavy
                        )
                        Text(
                            text = "FLIP Flashcard",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeoNavy.copy(alpha = 0.6f),
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Email field
                        NeoTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password field
                        NeoTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Mật khẩu",
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )

                        // Error message
                        if (error != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = error!!,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            LaunchedEffect(error) {
                                kotlinx.coroutines.delay(3000)
                                viewModel.clearError()
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Submit Button
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .offset(x = 5.dp, y = 5.dp)
                                    .background(NeoNavy, RoundedCornerShape(12.dp))
                            )
                            Surface(
                                onClick = {
                                    if (isSignUpMode) viewModel.signUp(email, password)
                                    else viewModel.signIn(email, password)
                                },
                                enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                                color = NeoNavy,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = NeoWhite,
                                            strokeWidth = 3.dp
                                        )
                                    } else {
                                        Text(
                                            text = if (isSignUpMode) "Đăng ký" else "Đăng nhập",
                                            fontWeight = FontWeight.Black,
                                            color = NeoWhite,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
                            Text(
                                text = if (isSignUpMode) "Đã có tài khoản? Đăng nhập" else "Chưa có tài khoản? Đăng ký",
                                color = NeoNavy,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NeoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontWeight = FontWeight.Bold) },
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = NeoWhite,
            unfocusedContainerColor = NeoWhite,
            focusedBorderColor = NeoNavy,
            unfocusedBorderColor = NeoNavy,
            focusedTextColor = NeoNavy,
            unfocusedTextColor = NeoNavy,
            focusedLabelColor = NeoNavy,
            unfocusedLabelColor = NeoNavy.copy(alpha = 0.6f)
        )
    )
}
