package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CodeStudioScreen(
    outputCode: String,
    isGenerating: Boolean,
    onProcessCode: (mode: String, language: String, input: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf("Generate") }
    var selectedLang by remember { mutableStateOf("Python") }
    var codeInputText by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    val modes = listOf("Generate", "Debug", "Explain", "Optimize")
    val languages = listOf("Python", "Kotlin", "JavaScript", "C++", "Java", "SQL", "PHP", "HTML/CSS")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        // Title
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Code Studio", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text("Multi-language AI Code Generator & Debugger", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Mode Selector
        item {
            Text("SELECT ACTION MODE", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp, color = NeonBlue))
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(modes) { mode ->
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = { selectedMode = mode },
                        label = { Text(mode) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Language Selector
        item {
            Text("TARGET LANGUAGE", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp, color = NeonBlue))
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(languages) { lang ->
                    FilterChip(
                        selected = selectedLang == lang,
                        onClick = { selectedLang = lang },
                        label = { Text(lang) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricViolet,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Code Input Field
        item {
            OutlinedTextField(
                value = codeInputText,
                onValueChange = { codeInputText = it },
                label = { Text("Describe task or paste code to $selectedMode...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .testTag("code_input_text_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }

        // Process Button
        item {
            Button(
                onClick = { onProcessCode(selectedMode, selectedLang, codeInputText) },
                enabled = codeInputText.isNotBlank() && !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("run_code_studio_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Processing $selectedLang...")
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Execute $selectedMode ($selectedLang)", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Code Output Card
        if (outputCode.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CodeBg),
                    border = BorderStroke(1.dp, NeonBlue.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "OUTPUT RESULT ($selectedLang)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            IconButton(onClick = { clipboardManager.setText(AnnotatedString(outputCode)) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonCyan)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = outputCode,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE0F7FA)
                            )
                        )
                    }
                }
            }
        }
    }
}
