char data;

void setup() {
  Serial.begin(9600);//シリアル通信開始、転送速度は115200ビット/秒
  pinMode(LED_BUILTIN, OUTPUT);
}

void loop() {


  if (Serial.available() > 0) {//シリアル通信受け取り
    data = Serial.read();//シリアル通信で受け取ったデータを読み込む(半角一文字)
    //配列にする事で複数文字の送信も対応します。

    if (data == 'h') {//1が送られてきたらLEDをON
      digitalWrite(LED_BUILTIN, HIGH);
      Serial.write('h');//hを返す
    } 
    if (data == 'l') {//1が送られてきたらLEDをON
      digitalWrite(LED_BUILTIN, LOW);
      Serial.write('l');//lを返す
    } 


  }


}