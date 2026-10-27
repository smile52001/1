package com.example.bleadvertiser

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : Activity() {
    private var advertiser: BluetoothLeAdvertiser? = null
    private var callback: AdvertiseCallback? = null
    private lateinit var status: TextView
    private lateinit var name: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.bleadvertiser.R.layout.activity_main)
        status=findViewById(com.example.bleadvertiser.R.id.status)
        name=findViewById(com.example.bleadvertiser.R.id.name)

        findViewById<Button>(com.example.bleadvertiser.R.id.start).setOnClickListener { startBle() }
        findViewById<Button>(com.example.bleadvertiser.R.id.stop).setOnClickListener { stopBle() }
    }

    private fun startBle() {
        if (Build.VERSION.SDK_INT >= 31 &&
            checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_ADVERTISE), 100)
            return
        }
        val manager=getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val adapter=manager.adapter
        if (adapter == null || !adapter.isEnabled) {
            status.text="状态：请先打开手机蓝牙"
            return
        }
        advertiser=adapter.bluetoothLeAdvertiser
        if (advertiser == null) {
            status.text="状态：此手机不支持 BLE 广播"
            return
        }

        val settings=AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(false)
            .build()

        val data=AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addManufacturerData(0xFFFF, name.text.toString().toByteArray(Charsets.UTF_8))
            .build()

        callback=object: AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                status.text="状态：正在 BLE 广播"
            }
            override fun onStartFailure(errorCode: Int) {
                status.text="状态：启动失败，错误码 $errorCode"
            }
        }
        advertiser?.startAdvertising(settings, data, callback)
    }

    private fun stopBle() {
        callback?.let { advertiser?.stopAdvertising(it) }
        status.text="状态：已停止"
    }

    override fun onDestroy() {
        stopBle()
        super.onDestroy()
    }
}
