@file:Suppress("DEPRECATION")

package com.codility.accelerometer

//import android.R
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
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
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
    lateinit var listener: SensorEventListener

    override fun onAccuracyChanged(s: Sensor?, i: Int) {
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER && Btconexao != null) {
            getAccelerometer(event)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        myBtadpater  = BluetoothAdapter.getDefaultAdapter()

        conectbtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (conexao) {
                    try {
                        socketBt!!.close()
                        conexao = false
                        BtStatusTxt.text = "disconnected"
                        conectbtn.text = "connect"
                    } catch (erro: IOException) {
                        BtStatusTxt.text = "error"
                    }
                } else {
                    val abrirLista = Intent(this@MainActivity, ListaDispositivos::class.java)
                    startActivityForResult(abrirLista, nDaConexao)
                }
            }
        })

        keyup.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                Btconexao!!.write(Gson().toJson(Command("key", "Page_Up")))
            }
        })

        keydown.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                Btconexao!!.write(Gson().toJson(Command("key", "Page_Down")))
            }
        })

        keyg.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                Btconexao!!.write(Gson().toJson(Command("key", "g")))
            }
        })

        keyw.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                Btconexao!!.write(Gson().toJson(Command("key", "w")))
            }
        })

        keyspace.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                Btconexao!!.write(Gson().toJson(Command("key", "space")))
            }
        })

        keyenter.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                Btconexao!!.write(Gson().toJson(Command("key", "KP_Enter")))
            }
        })

        pressdown.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        Btconexao!!.write(Gson().toJson(Command("down", "minus")))
                    }
                    MotionEvent.ACTION_UP -> {
                        Btconexao!!.write(Gson().toJson(Command("up", "minus")))
                    }
                }
                return v?.onTouchEvent(event) ?: true
            }
        })
        pressup.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        Btconexao!!.write(Gson().toJson(Command("down", "plus")))
                    }
                    MotionEvent.ACTION_UP -> {
                        Btconexao!!.write(Gson().toJson(Command("up", "plus")))
                    }
                }
                return v?.onTouchEvent(event) ?: true
            }
        })

        val radioGroup = findViewById<RadioGroup>(R.id.refresh)
        radioGroup.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {


                val rb: RadioButton = findViewById(checkedId) as RadioButton
                var items = HashMap<String, Int>()

                items["NORMAL"] = SensorManager.SENSOR_DELAY_NORMAL
                items["GAME"] = SensorManager.SENSOR_DELAY_GAME
                items["UI"] = SensorManager.SENSOR_DELAY_UI
                items["FASTEST"] = SensorManager.SENSOR_DELAY_FASTEST
                Log.d("REFRESH", rb.text.toString())
                val delay: Int = items.get(rb.text)!!

                sensorManager!!.registerListener(listener, sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), delay)


                //val speed: + rb.getText()
            }
        })

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

    data class Coord(
            @SerializedName("ts") val ts: Long,
            @SerializedName("x") val x: Float,
            @SerializedName("y") val y: Float,
            @SerializedName("z") val z: Float
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAccelerometer(event: SensorEvent) {

        val ts: Long = System.currentTimeMillis()

        Btconexao!!.write(Gson().toJson(XYZ("xyz", event.values)))

    }

    override fun onResume() {
        super.onResume()
        listener = this
        sensorManager!!.registerListener(listener, sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME)
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
                        BtStatusTxt.text = "connected"
                        Btconexao = ConnectedThread()

                        conectbtn.text = "disconnect"
                    } catch (erro: IOException) {
                        conexao = false
                        BtStatusTxt.text = "error"
                    }
                } else {
                    BtStatusTxt.text = "failed to obtain mac"
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

}