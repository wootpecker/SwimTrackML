package com.example.wear.presentation

import android.util.Log
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService

private const val TAG = "DataLayerSample"
private const val START_ACTIVITY_PATH = "/start-activity"
private const val DATA_ITEM_RECEIVED_PATH = "/data-item-received"

class WearListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onDataChanged: $dataEvents")
        }

        // Loop through the events and send a message
        // to the node that created the data item.
        dataEvents.map { it.dataItem.uri }
            .forEach { uri ->
                // Get the node ID from the host value of the URI.
                val nodeId: String = uri.host.toString()
                // Set the data of the message to be the bytes of the URI.
                val payload: ByteArray = uri.toString().toByteArray()

                // Send the RPC.
                Wearable.getMessageClient(this)
                    .sendMessage(nodeId, DATA_ITEM_RECEIVED_PATH, payload)
            }
    }
}
