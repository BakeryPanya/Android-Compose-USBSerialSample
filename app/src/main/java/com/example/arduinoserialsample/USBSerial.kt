package com.example.arduinoserialsample

import android.content.Context
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.io.IOException
import java.util.concurrent.Executors


class USBSerial(
    loadData: (String) -> Unit
) {
    private var manager: UsbManager? = null
    private var serialIoManager: SerialInputOutputManager? = null
    private var availableDrivers: MutableList<UsbSerialDriver>? = null
    private val executor = Executors.newSingleThreadExecutor()
    private var driver: UsbSerialDriver? = null
    private var connection: UsbDeviceConnection? = null
    private var port: UsbSerialPort? = null
    private var connected = false
    private var baudRate = 115200

    //read用のコルーチンがないのでリスナーで対応します
    private val mListener: SerialInputOutputManager.Listener =
        object : SerialInputOutputManager.Listener {

            override fun onRunError(e: java.lang.Exception) {

                try {
                    serialIoManager?.stop()
                    serialIoManager = null
                    connected = false
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }//エラー処理

            override fun onNewData(data: ByteArray) {

                val str = String(data)

                loadData(str)

            }//来たデータをloadDataで処理

        }

    fun open(context: Context, rate: Int = 115200): String {

        if(connected) return "AlreadyConnected"
        //接続されているなら接続処理の必要はないのでreturn

        baudRate = rate
        //通信のbaudRateの設定、初期値は115200に設定してます

        manager = context.getSystemService(Context.USB_SERVICE) as UsbManager?
        //USBのmanagerを呼び出し

        if(manager == null){
            return "NoManager"
        }
        //managerがない場合はおかしいのでNoMangerで返却


        availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        //USBのmanagerを使って現在使えるUSB接続デバイス->USBSerialDriverを列挙

        if (availableDrivers?.isEmpty() == true) {
            return "NoDrivers"
        }
        //もしないならnullエラーを防ぐためNoDriversとしてreturn

        driver = availableDrivers?.get(0) ?: return "NoDriver"
        //基本的にはUSB接続しているデバイスは一個しかないので0番を取得することで目的のデバイスと通信ができる。
        //もし選びたい場合はdriverをUIに列挙して選ぶ処理を実装する必要があります。
        //availableDriversはUSBSerialDriverを保持するMutableListなのでdriverをセットするメソッドを用意するのもよいと思います
        //TODO  選べるようにもしたい

        connection = manager?.openDevice(driver?.device ?: return "NoDevice or Driver") ?: return "NoConnection"
        //準備できたのでopenDeviceで開いてあげます。managerを最初はnullという関係上アサーション演算子でnon-nullであることを宣言しないと構文上エラーになります。

        port = driver?.ports?.get(0) ?: return "NoPort"
        //大体は接続ポートが一個しかないので0と決めうってますが違う場合は1かエラーがでないポートを探すように変更してください
        //TODO  動的にportを選択できるようにしたい

        port?.open(connection) ?: return "NoPort or WrongPort"
        //portをopenします,nullだったらまちがえてるので返却

        serialIoManager = SerialInputOutputManager(port, mListener)
        executor.submit(serialIoManager)
        //portに対してListenerを設定します

        port?.dtr = true
        //シリアル通信をする上でDTRをtrueつまり1を送信することで相手に正常動作である事を伝えます
        //これをしないと正常に動きません

        connected = true
        //接続されたことをtrueとして保持します。

        return "connected"
    }

    fun write(send_data: String, data_bits: Int = 8): String {

        if(!connected) return "NotConnected"

        port?.setParameters(baudRate, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)?:return "NG"
        //送るパラメータの設定。色々いじれそうですがあんまり解ってませｎ、、、。
        //baudrate,data長,ストップビット,パリティありなし　だと思います。
        // arduinoはこれで通信可能です。ほかの機器はいじる必要があるかもです。

        port?.write(send_data.toByteArray(), 100)?:return "NG"
        //writeしたいdataをbyteにして渡します。timeoutを持たせる事で値が連続で送られないようにします。
        //timeoutは好きに変えてください
        return "sendOK"

    }

    fun close(): String {

        try {
            serialIoManager?.stop()?: return "close error manager"
            serialIoManager = null
            port?.close() ?: return "close error port"
            connection?.close()?: return "close error connection"
            connected = false
            //いっぱい閉じます
        } catch (e: Exception) {
            e.printStackTrace()
            return "close ng"
        }

        return "close ok"

    }

}

