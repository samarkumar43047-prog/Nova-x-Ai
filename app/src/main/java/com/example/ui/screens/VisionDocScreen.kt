package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun VisionDocScreen(
    visionOutput: String,
    isGenerating: Boolean,
    onAnalyze: (prompt: String, bitmap: Bitmap?, documentText: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var userPrompt by remember { mutableStateOf("Perform detailed analysis and OCR text extraction.") }
    var documentTextInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                selectedBitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = CyberPink, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Vision AI & Document OCR", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text("Analyze photos, extract text from images & summarize documents", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Image Attachment Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("IMAGE INPUT & OCR", style = MaterialTheme.typography.labelSmall.copy(color = CyberPink, fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(10.dp))

                    if (selectedBitmap != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Image(
                                bitmap = selectedBitmap!!.asImageBitmap(),
                                contentDescription = "Preview",
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = { selectedBitmap = null },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.6f))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White)
                            }
                        }
                    } else {
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPink.copy(alpha = 0.2f), contentColor = CyberPink),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Image from Gallery or Camera", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Document Text Section
        item {
            OutlinedTextField(
                value = documentTextInput,
                onValueChange = { documentTextInput = it },
                label = { Text("Paste document / TXT / PDF text here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("doc_text_input_field"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Instruction Prompt
        item {
            OutlinedTextField(
                value = userPrompt,
                onValueChange = { userPrompt = it },
                label = { Text("Analysis Instructions / Question") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Process Button
        item {
            Button(
                onClick = { onAnalyze(userPrompt, selectedBitmap, documentTextInput) },
                enabled = (selectedBitmap != null || documentTextInput.isNotBlank()) && !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analyze_vision_doc_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberPink),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyzing Image & Document...")
                } else {
                    Icon(Icons.Default.FindInPage, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Run Intelligence Analysis", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Output Result
        if (visionOutput.isNotBlank()) {
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
                            Text("INTELLIGENCE ANALYSIS RESULT", style = MaterialTheme.typography.labelSmall.copy(color = CyberPink, fontWeight = FontWeight.Bold))
                            IconButton(onClick = { clipboardManager.setText(AnnotatedString(visionOutput)) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyberPink)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = visionOutput, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
