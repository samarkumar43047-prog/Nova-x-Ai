package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.NovaXApp
import com.example.ui.NovaXViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: NovaXViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NovaXApp(viewModel = viewModel)
        }
    }
}

