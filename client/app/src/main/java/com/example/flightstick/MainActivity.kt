@file:Suppress("DEPRECATION")

package com.example.flightstick

import android.annotation.TargetApi
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
import android.view.MotionEvent
import android.view.View
import com.codility.stick.ListaDispositivos
import com.flight.stick.R
//import androidx.appcompat.app.AppCompatActivity
//import com.example.flightstick.R
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.io.OutputStream
import java.util.*


class MainActivity : AppCompatActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null

    var socketBt : BluetoothSocket? = null
    var myBtadpater : BluetoothAdapter? = null
    val nDaActivity = 0
    val nDaConexao = 1
    var conexao = false
    var MAC: String? = ""
    var UUID_default : UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    var Btconexao : ConnectedThread? = null

    lateinit var reading: FloatArray
    var sensitivity: Int = SensorManager.SENSOR_DELAY_GAME

    override fun onAccuracyChanged(s: Sensor?, i: Int) {
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            reading = event.values
            send("nav", reading)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }

        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        myBtadpater  = BluetoothAdapter.getDefaultAdapter()

        conectbtn.setOnClickListener {
            if (conexao) {
                try {
                    socketBt!!.close()
                    conexao = false
                    conectbtn.text = "connect"
                } catch (erro: IOException) {

                }
            } else {
                val abrirLista = Intent(this@MainActivity, ListaDispositivos::class.java)
                startActivityForResult(abrirLista, nDaConexao)
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

    // ->
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
                        conexao = true
                        Btconexao = ConnectedThread()
                        conectbtn.text = "disconnect"
                        send("cal", reading)
                    } catch (error: IOException) {
                        conexao = false
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
    }

    private fun send(cmd: String, data: String) {
        if (Btconexao != null)
            Btconexao!!.write(Gson().toJson(Command(cmd, data)))
    }
}