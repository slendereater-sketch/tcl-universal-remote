package com.example.tclremote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tclremote.discovery.ScannerRepository
import com.example.tclremote.discovery.TvDevice
import com.example.tclremote.discovery.TvType
import com.example.tclremote.protocol.AndroidTvStrategy
import com.example.tclremote.protocol.DeviceProtocolStrategy
import com.example.tclremote.protocol.RemoteCommand
import com.example.tclremote.protocol.RokuStrategy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UiState(
    val discoveredDevices: List<TvDevice> = emptyList(),
    val selectedDevice: TvDevice? = null,
    val isScanning: Boolean = false
)

class RemoteViewModel(
    private val scannerRepository: ScannerRepository = ScannerRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private var activeStrategy: DeviceProtocolStrategy? = null

    fun startDiscovery(baseIp: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, discoveredDevices = emptyList())
            scannerRepository.scanSubnet(baseIp).collect { device ->
                _uiState.value = _uiState.value.copy(
                    discoveredDevices = _uiState.value.discoveredDevices + device
                )
            }
            _uiState.value = _uiState.value.copy(isScanning = false)
        }
    }

    fun selectDevice(device: TvDevice) {
        activeStrategy = when (device.type) {
            TvType.ROKU -> RokuStrategy(device.ip)
            TvType.ANDROID_TV -> AndroidTvStrategy(device.ip)
        }
        _uiState.value = _uiState.value.copy(selectedDevice = device)
    }

    fun sendCommand(command: RemoteCommand) {
        viewModelScope.launch {
            activeStrategy?.sendCommand(command)
        }
    }
}
