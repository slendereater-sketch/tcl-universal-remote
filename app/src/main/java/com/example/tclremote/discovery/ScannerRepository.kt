package com.example.tclremote.discovery

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.InetSocketAddress
import java.net.Socket

enum class TvType { ROKU, ANDROID_TV }
data class TvDevice(val ip: String, val type: TvType, val name: String = "TCL TV")

class ScannerRepository {

    /**
     * Scans the subnet for TVs on ports 8060 (Roku) and 5555 (ADB).
     * @param baseIp The first three octets (e.g., "192.168.1")
     */
    fun scanSubnet(baseIp: String): Flow<TvDevice> = flow {
        coroutineScope {
            val scanJobs = (1..254).map { i ->
                async(Dispatchers.IO) {
                    val ip = "$baseIp.$i"
                    when {
                        isPortOpen(ip, 8060) -> TvDevice(ip, TvType.ROKU)
                        isPortOpen(ip, 5555) -> TvDevice(ip, TvType.ANDROID_TV)
                        else -> null
                    }
                }
            }
            
            scanJobs.awaitAll().filterNotNull().forEach { device ->
                emit(device)
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun isPortOpen(ip: String, port: Int, timeout: Int = 200): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), timeout)
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
