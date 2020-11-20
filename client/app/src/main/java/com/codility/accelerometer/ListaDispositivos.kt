package com.codility.accelerometer

import android.app.Activity
import android.app.ListActivity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import org.w3c.dom.Text

class ListaDispositivos : ListActivity() {
    var myBtAdapter : BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var blueArray = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
        myBtAdapter = BluetoothAdapter.getDefaultAdapter()
        val pareados: Set<BluetoothDevice>? = myBtAdapter!!.bondedDevices
        pareados?.forEach { dispositivos ->
            blueArray.add("${dispositivos.name} \n ${dispositivos.address}")
        }
        listAdapter = blueArray
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)

        val Nome = listView.getItemAtPosition(position) as String
        //Toast.makeText(applicationContext, Nome, Toast.LENGTH_LONG).show()
        var endercoMacsub = Nome.subSequence(Nome.indexOf("\n")+2, Nome.length).toString()
        Toast.makeText(applicationContext, endercoMacsub, Toast.LENGTH_LONG).show()
        val enviarMac : Intent = Intent()
        enviarMac.putExtra("MAC", endercoMacsub)
        setResult(Activity.RESULT_OK, enviarMac)
        finish()
    }
}