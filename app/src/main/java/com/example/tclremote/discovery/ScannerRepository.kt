package com.example.tclremote.discovery

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

enum class TvType { ROKU, ANDROID_TV }
data class TvDevice(val ip: String, val type: TvType, val name: String = "TCL TV")

class ScannerRepository {

    /**
     * Combines SSDP discovery and subnet scanning.
     */
    fun scanSubnet(localIp: String): Flow<TvDevice> = flow {
        val discoveredIps = mutableSetOf<String>()

        coroutineScope {
            // 1. SSDP Discovery (Roku standard)
            launch {
                discoverRokuViaSsdp().collect { device ->
                    if (discoveredIps.add(device.ip)) emit(device)
                }
            }

            // 2. Optimized Subnet Scan (Android TV / Fallback)
            val baseIp = localIp.substringBeforeLast(".")
            val scanJobs = (1..254).map { i ->
                async(Dispatchers.IO) {
                    val ip = "$baseIp.$i"
                    if (ip == localIp || discoveredIps.contains(ip)) return@async null
                    
                    when {
                        // Scan ADB for Android TV
                        isPortOpen(ip, 5555, 500) -> TvDevice(ip, TvType.ANDROID_TV)
                        // Fallback Roku check
                        isPortOpen(ip, 8060, 500) -> TvDevice(ip, TvType.ROKU)
                        else -> null
                    }
                }
            }
            
            scanJobs.awaitAll().filterNotNull().forEach { device ->
                if (discoveredIps.add(device.ip)) emit(device)
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun discoverRokuViaSsdp(): Flow<TvDevice> = flow {
        try {
            val ssdpRequest = "M-SEARCH * HTTP/1.1\r\n" +
                    "HOST: 239.255.255.250:1900\r\n" +
                    "MAN: \"ssdp:discover\"\r\n" +
                    "ST: roku:ecp\r\n" +
                    "MX: 3\r\n\r\n"

            val socket = DatagramSocket()
            socket.soTimeout = 3000
            val group = InetAddress.getByName("239.255.255.250")
            val packet = DatagramPacket(ssdpRequest.toByteArray(), ssdpRequest.length, group, 1900)
            
            socket.send(packet)

            val buffer = ByteArray(1024)
            val receivePacket = DatagramPacket(buffer, buffer.size)

            // Listen for responses for 3 seconds
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 3000) {
                try {
                    socket.receive(receivePacket)
                    val response = String(receivePacket.data, 0, receivePacket.length)
                    if (response.contains("LOCATION: http://")) {
                        val ip = receivePacket.address.hostAddress
                        emit(TvDevice(ip, TvType.ROKU, "Roku TV"))
                    }
                } catch (e: Exception) {
                    // Timeout or error
                }
            }
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isPortOpen(ip: String, port: Int, timeout: Int): Boolean {
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
