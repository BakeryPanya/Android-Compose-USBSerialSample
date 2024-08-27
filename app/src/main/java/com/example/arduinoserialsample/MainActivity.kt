package com.example.arduinoserialsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.arduinoserialsample.ui.theme.ArduinoSerialSampleTheme



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArduinoSerialSampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SerialView()
                }
            }
        }
    }

}

@Composable
fun SerialView() {

    var check by remember { mutableStateOf("NG") }

    var data by remember { mutableStateOf("-1") }

    val context = LocalContext.current

    val usb = remember {
        mutableStateOf(USBSerial { receivedData ->
            // ここで受信したデータ(receivedData) を処理します。
            // 例えば、TextView に表示するなど。
            data = receivedData

        } )
    }

    Column() {
        Button(
            onClick = { check = usb.value.open(context,9600).toString() },
        ) {
            Text("connect")
        }

        Button(
            onClick = { check = usb.value.write("h", 8) },
        ) {
            Text("h write")
        }

        Button(
            onClick = { check = usb.value.write("l", 8) },
        ) {
            Text("l write")
        }

        Button(
            onClick = { check = usb.value.close() },
        ) {
            Text("close")
        }

        Text(text = check)

        Text(text = data)

    }
}