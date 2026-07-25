package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun WriterScreen(
    writerOutput: String,
    isGenerating: Boolean,
    onGenerateDraft: (type: String, topic: String, tone: String, language: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf("Email") }
    var selectedTone by remember { mutableStateOf("Professional") }
    var selectedLang by remember { mutableStateOf("English") }
    var topicInput by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    val types = listOf("Email", "Essay", "Story", "Shayari", "Poetry", "Resume", "Cover Letter", "Speech", "Blog", "Grammar Fix")
    val tones = listOf("Professional", "Friendly", "Creative", "Academic", "Persuasive")
    val languages = listOf("English", "Hindi", "Urdu", "Bengali", "Spanish", "French", "German", "Japanese")

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
                Icon(Icons.Default.Edit, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("AI Writer & Translation", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text("Emails, Essays, Stories, Shayari, Resumes & Translations", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Writing Type
        item {
            Text("CONTENT TYPE", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp, color = NeonPurple))
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(types) { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonPurple,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Tone
        item {
            Text("TONE & STYLE", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp, color = NeonPurple))
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tones) { tone ->
                    FilterChip(
                        selected = selectedTone == tone,
                        onClick = { selectedTone = tone },
                        label = { Text(tone) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberPink,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Language
        item {
            Text("TARGET LANGUAGE", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp, color = NeonPurple))
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

        // Input
        item {
            OutlinedTextField(
                value = topicInput,
                onValueChange = { topicInput = it },
                label = { Text("Topic, outline, or text for $selectedType...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("writer_input_field"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Button
        item {
            Button(
                onClick = { onGenerateDraft(selectedType, topicInput, selectedTone, selectedLang) },
                enabled = topicInput.isNotBlank() && !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("generate_writer_draft_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Drafting Content...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compose $selectedType", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Output Card
        if (writerOutput.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "COMPOSED DRAFT ($selectedLang)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonPurple,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            IconButton(onClick = { clipboardManager.setText(AnnotatedString(writerOutput)) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Draft", tint = NeonPurple)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = writerOutput,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
