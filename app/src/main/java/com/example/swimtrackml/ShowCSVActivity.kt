package com.example.swimtrackml

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import java.io.FileNotFoundException
import java.io.IOException
import java.util.logging.Logger


class ShowCSVActivity : Activity() {

    val Log = Logger.getLogger(MainActivity::class.java.name)
    val CREATE_FILE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.show_data)

        val button=findViewById<Button>(R.id.btn_show_data)
        button.setOnClickListener {

            //Action create pdf
            val intention = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                //type = "application/excel"
                type = "text/comma-separated-values"
                putExtra(Intent.EXTRA_TITLE, "invoice.csv")

                // Optionally, specify a URI for the directory that should be opened in
                // the system file picker before your app creates the document.
                //putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
            }
            startActivityForResult(intention, CREATE_FILE)




            //Action send
        /*    val intent = Intent()
            intent.setAction(Intent.ACTION_SEND)
            intent.setType("text/csv")
            val uri ="test"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)


            startActivity(intent)*/


            //startActivity(Intent(this, ShowDataSeecondTestActivity::class.java))
        }
    }

    private fun saveData() {
        val txtinput=findViewById<EditText>(R.id.txtinput)
        val filename = "ourfile.txt"
        Thread(Runnable {
            try {
                val out = openFileOutput(filename, Context.MODE_PRIVATE)
                out.use {
                    out.write(txtinput.text.toString().toByteArray())
                }
                runOnUiThread(Runnable {
                    Toast.makeText(this,"Saved", Toast.LENGTH_LONG).show()
                })
            }
            catch(ioe:IOException) {
                Log.warning("Error while saving ${filename} : ${ioe}")
            }
        }).start()
    }


    override fun onPause() {
        super.onPause()
        saveData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val txtinput=findViewById<EditText>(R.id.txtinput)

        val filename = "ourfile.txt"
        Thread(Runnable{
            try {
                val input = openFileInput(filename)
                input.use {
                    var buffer = StringBuilder()
                    var bytes_read = input.read()

                    while(bytes_read != -1) {
                        buffer.append(bytes_read.toChar())
                        bytes_read = input.read()
                    }
                    runOnUiThread(Runnable{
                        txtinput.setText(buffer.toString())
                    })
                }
            }
            catch(fnfe:FileNotFoundException) {
                Log.warning("file not found, occurs only once")
            }
            catch(ioe: IOException) {
                Log.warning("IOException : $ioe")
            }
        }).start()
    }
}

