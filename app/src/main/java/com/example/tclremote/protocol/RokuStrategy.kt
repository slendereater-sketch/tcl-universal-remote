package com.example.tclremote.protocol

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class RokuStrategy(
    private val ip: String,
    private val client: OkHttpClient = OkHttpClient()
) : DeviceProtocolStrategy {

    override suspend fun sendCommand(command: RemoteCommand) = withContext(Dispatchers.IO) {
        val rokuKey = mapToRokuKey(command)
        val request = Request.Builder()
            .url("http://$ip:8060/keypress/$rokuKey")
            .post("".toRequestBody())
            .build()
        
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    // Log error or handle failure
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun mapToRokuKey(command: RemoteCommand): String = when (command) {
        RemoteCommand.UP -> "Up"
        RemoteCommand.DOWN -> "Down"
        RemoteCommand.LEFT -> "Left"
        RemoteCommand.RIGHT -> "Right"
        RemoteCommand.OK -> "Select"
        RemoteCommand.BACK -> "Back"
        RemoteCommand.HOME -> "Home"
        RemoteCommand.POWER -> "Power"
        RemoteCommand.VOL_UP -> "VolumeUp"
        RemoteCommand.VOL_DOWN -> "VolumeDown"
        RemoteCommand.MUTE -> "VolumeMute"
    }
}
