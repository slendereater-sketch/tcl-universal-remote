package com.example.tclremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.tclremote.ui.RemoteScreen

class MainActivity : ComponentActivity() {
    
    private val viewModel: RemoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Trigger discovery on start (assuming a common subnet for demo)
        // In a real app, you'd fetch the actual local IP base.
        viewModel.startDiscovery("192.168.1")

        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    RemoteScreen(viewModel)
                }
            }
        }
    }
}
