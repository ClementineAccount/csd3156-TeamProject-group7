package com.google.ar.core.examples.kotlin.helloar

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.csd3156.team7.ItemViewHolder
import com.csd3156.team7.ShopActivity
import com.csd3156.team7.ShopListAdaptor
import kotlinx.coroutines.flow.collect
import java.lang.StringBuilder

//https://abhishekbagdare.medium.com/reading-nfc-tags-with-android-kotlin-9ee8f82223b8
class NFCActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }

    private fun createNFCIntentFilter(): Array<IntentFilter> {
        val intentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        try {
            intentFilter.addDataType("*/*")
        } catch (e: IntentFilter.MalformedMimeTypeException) {
            throw RuntimeException("Failed to add MIME type.", e)
        }
        return arrayOf(intentFilter)
    }

    override fun onResume() {
        super.onResume()
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE
        )
        val intentFilters = arrayOf<IntentFilter>(
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        )
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, null)
    }

    override fun onPause() {
        super.onPause()
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }
            tag?.id?.let {
                val tagValue = it.toHexString()


                val last6 : String= tagValue.takeLast(6)
                val colorString : String = "#$last6"

                Log.d("DebugColor", "$colorString");
                val color: Int = Color.parseColor(colorString)
                Log.d("DebugColor", "$color");


                Toast.makeText(this, "NFC tag detected: $tagValue", Toast.LENGTH_SHORT).show()
                ShopActivity.playerViewModel.updateItemColor(ShopListAdaptor.selectedID, color )

                onBackPressed()

            }
        }
    }

    fun ByteArray.toHexString() : String
    {
        val hexChars = "0123456789ABCDEF"
        val result = StringBuilder(size * 2)

        map{byte ->
            val value = byte.toInt()
            val hexChar1 = hexChars[value shr 4 and 0x0F]
            val hexChar2 = hexChars[value and 0x0F]
            result.append(hexChar1)
            result.append(hexChar2)
        }
        return result.toString();

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}