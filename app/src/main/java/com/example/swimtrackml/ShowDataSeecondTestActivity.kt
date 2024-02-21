package com.example.swimtrackml

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText


class ShowDataSeecondTestActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.show_data)
        val button=findViewById<Button>(R.id.btn_show_data)
        button.setOnClickListener {
            startActivity(Intent(this, ShowDataActivity::class.java))
        }
    }

    private fun loadData() {
        val txtoutput=findViewById<EditText>(R.id.txtinput)
        val filename = "ourfile.txt"

        Thread(Runnable {

            val input = openFileInput(filename)

            input.use {
                var buffer = StringBuilder()
                var bytes_read = input.read()
                while(bytes_read != -1) {
                    buffer.append(bytes_read.toChar())
                    bytes_read = input.read()
                }
                runOnUiThread(Runnable{
                    txtoutput.setText(buffer.toString())
                })
            }
        }).start()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
}
