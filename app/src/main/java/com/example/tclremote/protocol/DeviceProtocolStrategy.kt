package com.example.tclremote.protocol

enum class RemoteCommand {
    UP, DOWN, LEFT, RIGHT, OK, BACK, HOME, POWER, VOL_UP, VOL_DOWN, MUTE
}

interface DeviceProtocolStrategy {
    suspend fun sendCommand(command: RemoteCommand)
}
