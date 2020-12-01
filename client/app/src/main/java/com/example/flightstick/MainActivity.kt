@file:Suppress("DEPRECATION")

package com.example.flightstick

import android.annotation.TargetApi
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ToggleButton
import com.codility.stick.ListaDispositivos
import com.flight.stick.R
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.util.*


class MainActivity : AppCompatActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null

    var socketBt : BluetoothSocket? = null
    var myBtadpater : BluetoothAdapter? = null
    val nDaActivity = 0
    val nDaConexao = 1
    var MAC: String? = ""
    var UUID_default : UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    var Btconexao : ConnectedThread? = null
    var isWS: Boolean = true
    var isConnected: Boolean = false
    lateinit var socket: Socket

    lateinit var reading: FloatArray
    var sensitivity: Int = SensorManager.SENSOR_DELAY_GAME

    override fun onAccuracyChanged(s: Sensor?, i: Int) {
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            reading = event.values
            if (isConnected)
                send("nav", reading)
        }
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }

        setContentView(R.layout.activity_main)






        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        val toggle: ToggleButton = findViewById(R.id.onoff)
        toggle.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {

                if (isWS) {

                    val context = this
                    val builder = AlertDialog.Builder(context)
                    val view = layoutInflater.inflate(R.layout.prompts, null)
                    val categoryEditText = view.findViewById(R.id.socketIP) as EditText

                    builder.setTitle("Server Address")
                    builder.setView(view)
                    builder.setPositiveButton(android.R.string.ok) { dialog, p1 ->
                        val socketIP = categoryEditText.text.toString()
                        val opts = IO.Options()
                        opts.transports = arrayOf(WebSocket.NAME)
                        socket = IO.socket(socketIP, opts)
                        Log.d("emit", socketIP)
                        socket?.on(Socket.EVENT_CONNECT) {
                            Log.d("emit", "connected")
                            isConnected = true
                        }?.on(Socket.EVENT_DISCONNECT) {parameters ->
                            isConnected = false
                           // toggle.isChecked = false
                            Log.d("emit", "dis")
                            for (obj in parameters)
                                Log.v("emit", "ERROR:: $obj")
                        }?.on(Socket.EVENT_CONNECTING) {
                            Log.d("emit", "CONNECTING")
                        }?.on(Socket.EVENT_CONNECT_TIMEOUT) {
                            Log.d("emit", "TIMEOUT")
                        }?.on(Socket.EVENT_CONNECT_ERROR) {parameters ->
                            for (obj in parameters)
                                Log.v("emit", "ERROR:: $obj")
                            Log.d("emit", "CONNECT ERROR")
                        }?.on(Socket.EVENT_PING) { parameters ->
                            for (obj in parameters)
                                Log.v("emit", "PING:: $obj")
                            Log.d("emit", "PING")
                        }
                        socket.connect()
                        dialog.dismiss()

                    }

                    //builder.setNegativeButton(android.R.string.cancel) { dialog, p1 ->
                      //  dialog.cancel()
                    //}

                    builder.show();

                } else {

                    myBtadpater = BluetoothAdapter.getDefaultAdapter()
                    startActivityForResult(
                        Intent(this@MainActivity, ListaDispositivos::class.java),
                        nDaConexao
                    )

                }

            } else {

                isConnected = false

                if (isWS)

                    socket.disconnect()

                else {

                    try {
                        socketBt!!.close()
                    } catch (error: Exception) {}

                }

            }



        }

        keyup.setOnClickListener { send("key", "Page_Up") }

        keydown.setOnClickListener { send("key", "Page_Down") }

        keyg.setOnClickListener { send("key", "g") }

        keyw.setOnClickListener { send("key", "w") }

        keyenter.setOnClickListener { send("key", "KP_Enter") }

        keyr.setOnClickListener { send("key", "r") }

        keye.setOnClickListener { send("key", "e") }

        key1.setOnClickListener { send("key", "1") }

        key2.setOnClickListener { send("key", "2") }

        key3.setOnClickListener { send("key", "3") }

        keyesc.setOnClickListener { send("key", "Escape") }

        keyx.setOnClickListener { send("key", "x") }

        keyc.setOnClickListener { send("key", "c") }

        keyb.setOnClickListener { send("key", "b") }

        flare.setOnClickListener { send("key", "Delete") }

        chaff.setOnClickListener { send("key", "Insert") }

        calibratebtn.setOnClickListener { send("cal", reading) }

        pressspace.setOnTouchListener { v, event ->
            v.performClick()
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    send("down", "space")
                }
                MotionEvent.ACTION_UP -> {
                    send("up", "space")
                }
            }
            v?.onTouchEvent(event) ?: true
        }
        pressdown.setOnTouchListener { v, event ->
            v.performClick()
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    send("down", "minus")
                }
                MotionEvent.ACTION_UP -> {
                    send("up", "minus")
                }
            }
            v?.onTouchEvent(event) ?: true
        }
        pressup.setOnTouchListener { v, event ->
            v.performClick()
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    send("down", "plus")
                }
                MotionEvent.ACTION_UP -> {
                    send("up", "plus")
                }
            }
            v?.onTouchEvent(event) ?: true
        }

        val radioGroup = findViewById<RadioGroup>(R.id.refresh)
        radioGroup.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {

                isConnected = false
                toggle.isChecked = false

                val rb: RadioButton = findViewById(checkedId) as RadioButton

                isWS = rb.text == "wifi"

            }



        })

/*
        val radioGroup = findViewById<RadioGroup>(R.id.refresh)
        radioGroup.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {


                val rb: RadioButton = findViewById(checkedId) as RadioButton
                val items = HashMap<String, Int>()

                items["NORMAL"] = SensorManager.SENSOR_DELAY_NORMAL
                items["GAME"] = SensorManager.SENSOR_DELAY_GAME
                items["UI"] = SensorManager.SENSOR_DELAY_UI
                items["FASTEST"] = SensorManager.SENSOR_DELAY_FASTEST

                sensitivity = items.get(rb.text)!!

                finish()
                startActivity(intent)


            }
        })
*/
    }

    data class Command(
            @SerializedName("cmd") val ts: String,
            @SerializedName("data") val data: String
    )

    data class XYZ(
            @SerializedName("cmd") val ts: String,
            @SerializedName("data") val data: FloatArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as XYZ

            if (ts != other.ts) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = ts.hashCode()
            result = 31 * result + data.contentHashCode()
            return result
        }
    }


    override fun onResume() {
        super.onResume()
        sensorManager!!.registerListener(this, sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensitivity)
    }

    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(this)
    }

    @TargetApi(23)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            nDaActivity -> {
                if (resultCode == RESULT_CANCELED) {
                    finish()
                    moveTaskToBack(true)
                }
            }
            nDaConexao -> {
                if (resultCode == RESULT_OK) {
                    MAC = data!!.getStringExtra("MAC")
                    try {
                        val myBtDevice: BluetoothDevice = myBtadpater!!.getRemoteDevice(MAC)
                        socketBt = myBtDevice.createRfcommSocketToServiceRecord(UUID_default)
                        socketBt!!.connect()
                        Btconexao = ConnectedThread()
                        isConnected = true
                        send("cal", reading)
                    } catch (error: IOException) {
                        isConnected = false
                    }
                }
            }
        }
    }

    inner class ConnectedThread : Thread() {
        private val envio : OutputStream = socketBt!!.outputStream
        fun write(info: String) {
            val msg : ByteArray = info.toByteArray()
            try {
                envio.write(msg)
            } catch (e: IOException) {
                return
            }
        }
    }

    private fun send(cmd: String, data: FloatArray) {
        if (Btconexao != null)
            Btconexao!!.write(Gson().toJson(XYZ(cmd, data)))
        if (isWS)
            socket.emit("data", Gson().toJson(XYZ(cmd, data)))
    }

    private fun send(cmd: String, data: String) {
        if (Btconexao != null)
            Btconexao!!.write(Gson().toJson(Command(cmd, data)))
        if (isWS)
            socket.emit("data", Gson().toJson(Command(cmd, data)))
    }
}