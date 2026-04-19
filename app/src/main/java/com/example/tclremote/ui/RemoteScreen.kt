package com.example.tclremote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tclremote.RemoteViewModel
import com.example.tclremote.discovery.TvDevice
import com.example.tclremote.protocol.RemoteCommand
import com.example.tclremote.ui.components.HugeButton

@Composable
fun RemoteScreen(viewModel: RemoteViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Dark theme for high contrast
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.selectedDevice == null) {
            DiscoveryView(uiState.discoveredDevices, uiState.isScanning) { 
                viewModel.selectDevice(it) 
            }
        } else {
            ActiveRemoteView(uiState.selectedDevice!!, viewModel::sendCommand)
        }
    }
}

@Composable
fun DiscoveryView(devices: List<TvDevice>, isScanning: Boolean, onSelect: (TvDevice) -> Unit) {
    Text("Select Your TCL TV", style = MaterialTheme.typography.headlineMedium, color = Color.White)
    Spacer(modifier = Modifier.height(16.dp))
    
    if (isScanning) {
        CircularProgressIndicator()
    }

    LazyColumn {
        items(devices) { device ->
            Button(
                onClick = { onSelect(device) },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("${device.name} (${device.ip})", color = Color.White)
            }
        }
    }
}

@Composable
fun ActiveRemoteView(device: TvDevice, onCommand: (RemoteCommand) -> Unit) {
    Text("Connected to ${device.name}", color = Color.Green)
    Spacer(modifier = Modifier.height(32.dp))

    // Power and Home at the top
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        HugeButton(icon = Icons.Default.PowerSettingsNew, backgroundColor = Color.Red) { onCommand(RemoteCommand.POWER) }
        HugeButton(icon = Icons.Default.Home, backgroundColor = Color.Blue) { onCommand(RemoteCommand.HOME) }
    }

    Spacer(modifier = Modifier.height(48.dp))

    // Classic D-Pad
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HugeButton(icon = Icons.Default.KeyboardArrowUp) { onCommand(RemoteCommand.UP) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            HugeButton(icon = Icons.Default.KeyboardArrowLeft) { onCommand(RemoteCommand.LEFT) }
            Spacer(modifier = Modifier.width(16.dp))
            HugeButton(text = "OK", modifier = Modifier.size(100.dp)) { onCommand(RemoteCommand.OK) }
            Spacer(modifier = Modifier.width(16.dp))
            HugeButton(icon = Icons.Default.KeyboardArrowRight) { onCommand(RemoteCommand.RIGHT) }
        }
        HugeButton(icon = Icons.Default.KeyboardArrowDown) { onCommand(RemoteCommand.DOWN) }
    }

    Spacer(modifier = Modifier.height(48.dp))

    // Volume Control at the bottom
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        HugeButton(icon = Icons.Default.VolumeDown) { onCommand(RemoteCommand.VOL_DOWN) }
        HugeButton(icon = Icons.Default.VolumeMute, backgroundColor = Color.DarkGray) { onCommand(RemoteCommand.MUTE) }
        HugeButton(icon = Icons.Default.VolumeUp) { onCommand(RemoteCommand.VOL_UP) }
    }
}
