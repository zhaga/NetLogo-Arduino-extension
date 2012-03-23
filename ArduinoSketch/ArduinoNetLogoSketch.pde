//initalize variables
int ServoPos  = 1; //not needed
int Analog[]      = {0, 0, 0, 0, 0, 0};
int Digital[]     = {0, 0, 0, 0, 0, 0};
int MotorPo[]     = {0, 0, 0, 0, 0, 0};
int DigInPorts[]  = {2, 4, 7, 8,12,13};
int DigOutPorts[] = {3, 5, 6, 9,10,11};
int inByte;  //not needed?

boolean readingAnalog  = true;
boolean readingDigital = true;

void setup(){
  //setup serial baud rate
  Serial.begin(57600);      //4800);   //115200);
  //setup the analog reference: do not see any use in making this part of the loop (ie. needs to be set once)
  analogReference(DEFAULT);
  
  //digital input config w/ 20K pullup resistor 
  pinMode(DigInPorts[0], INPUT); digitalWrite(DigInPorts[0], HIGH);
  pinMode(DigInPorts[1], INPUT); digitalWrite(DigInPorts[1], HIGH);
  pinMode(DigInPorts[2], INPUT); digitalWrite(DigInPorts[2], HIGH);
  pinMode(DigInPorts[3], INPUT); digitalWrite(DigInPorts[3], HIGH);
  pinMode(DigInPorts[4], INPUT); digitalWrite(DigInPorts[4], HIGH);
  pinMode(DigInPorts[5], INPUT); digitalWrite(DigInPorts[5], LOW);   //this is the weird channel-see ard. docs
  
  //digital&analog output, aka...motor ports
  pinMode(DigOutPorts[0], OUTPUT);
  pinMode(DigOutPorts[1], OUTPUT);
  pinMode(DigOutPorts[2], OUTPUT);  
  pinMode(DigOutPorts[3], OUTPUT);
  pinMode(DigOutPorts[4], OUTPUT);
  pinMode(DigOutPorts[5], OUTPUT);
  
}

void loop(){
 
  if(Serial.available()>0) {
    readingSerialPort();
  }
    
  if(readingAnalog) {
    readAnalogPorts();
    sendAnalogPacket();
  }
  if(readingDigital){
    readDigitalPorts();
    sendDigitalPacket();
  }
 
  
  /*
  //digital --output
  digitalWrite(13, HIGH);   // set the LED on
  delay(1000);              // wait for a second
  digitalWrite(13, LOW);    // set the LED off
  delay(1000);              // wait for a second
  
  // set the brightness of pin 9:
  analogWrite(9, brightness); 
  // change the brightness for next time through the loop:
  brightness = brightness + fadeAmount;

  // reverse the direction of the fading at the ends of the fade: 
  if (brightness == 0 || brightness == 255) {
    fadeAmount = -fadeAmount ; 
  }     
  // wait for 30 milliseconds to see the dimming effect    
  delay(30); 
  
  */
 
}
void readingSerialPort() {
  int receivedSerialData; //[Serial.available()];
  boolean foundEndChar = false;

  receivedSerialData = Serial.read();
  //Serial.print(receivedSerialData,BYTE);
  //Serial.println(" ");

    if(receivedSerialData == ';')  {
      foundEndChar = true;
      //Serial.println("End Packet Char Found");
    }  
    else if(receivedSerialData == 'C') {
      parceConfigPacket();
    }
    else if(receivedSerialData == 'M') {
      parceMotorPacket();
    }
    //else  {
      //Serial.println("E:not a known packet;");
   // }
  //}
}
//parce the incoming motor packet and execute motor commands 
void parceMotorPacket() {
  int receivedSerialData;    
  String inString = ""; 
  int i = 0;
  boolean flag = true;
  //Serial.println("Here_1");
  if(Serial.available() > 0) {
    receivedSerialData = Serial.read();
    if(receivedSerialData == ':')  {
      //Serial.println("Here");
      while (flag) {
        int inChar = Serial.read();
       // Serial.println(inChar, BYTE);
           
        if (isDigit(inChar)) {
          inString += (char)inChar; 
        }            
        else if (inChar == ',') {
          MotorPo[i] = inString.toInt();
          i++;
          inString = ""; 
        }
        else if (inChar == ';') {
          MotorPo[i] = inString.toInt();
          inString = "";
          flag = false;
          continue;  
        }
       // else {
       //   Serial.println("not a known packet......");
       //   Serial.println(inChar,BYTE);
       // }          
      }
    }
  }
      //Serial.println("Parcing Motor Packet");
      //for(int i =0; i < 6; i++) {
      //  Serial.print("Motor Power  ="); 
      //  Serial.println(MotorPo[i],DEC);
     // }
      //Serial.println("DONE?");
//      M:111,12,13,14,15,116;
//      M:10,60,120,180,200,255;
    for(int i = 0; i < 6; i++) {
      if(MotorPo[i] > 255){
        MotorPo[i] = 255;
      }
      analogWrite(DigOutPorts[i],  MotorPo[i]);
      /*
      pinMode(DigInPorts[5], OUTPUT); 
      digitalWrite(DigInPorts[5], HIGH);
      */
    }
}

//Parce config packet and execute the config properties
//Config Packets--  this actually works C:A0,D1;  (Two commands in one packet.)  
//Didn't expect that.  Anyway,...
void parceConfigPacket() {
//      Serial.println("Parcing Config Packet");
      int receivedSerialData;    
      String inString = "";
      boolean flag = true; 
      int i = 0;
      
      if(Serial.available() > 0) {
        receivedSerialData = Serial.read();
        if(receivedSerialData == ':')  {
//           Serial.println("Parcing Config Packet");
           while (flag) {
             int inChar = Serial.read();
             //Serial.println(inChar, BYTE);
             if(inChar == 'A') {               //Analog config- A1 = read true A0 read false
               int inChar = Serial.read();
               if(inChar == '1') {
                 readingAnalog  = true;
               }
               else if(inChar == '0'){
                 readingAnalog  = false;
               }
             }
             else if(inChar == 'D') {          //Digital config- D1 = read true D0 read false
               int inChar = Serial.read();
               if(inChar == '1') {
                 readingDigital  = true;
               }
               else if(inChar == '0'){
                 readingDigital  = false;
               }
             }
             else if(inChar == ';') {
               flag = false;
             }
           }//while(flag)
        }//if :
      }//if serial
}//parceConfigPAcket


//////////////////////////////////////////////Analog
//read analog ports
void readAnalogPorts() {
  Analog[0] = analogRead(A0); delay(10);
  Analog[1] = analogRead(1);  delay(10);
  Analog[2] = analogRead(2);  delay(10);
  Analog[3] = analogRead(3);  delay(10);
  Analog[4] = analogRead(4);  delay(10);
  Analog[5] = analogRead(5);  delay(10);
}
//write analog ports
void sendAnalogPacket() {
  //Analog packet
  Serial.print("A:");
  for(int i = 0; i < 6; i++) {
    if(i<5){
      Serial.print(Analog[i], DEC); Serial.print(",");
    }
    else if(i == 5) {
       Serial.print(Analog[i], DEC);Serial.print(";;");
    }
  }
}
//////////////////////////////////////////////Digital
//Read Digital ports
void readDigitalPorts() {
  //digital --input
  for(int i = 0; i < 6; i++) {
    Digital[i] = digitalRead(DigInPorts[i]);
  }
}
//send digital packet
void sendDigitalPacket() {
  //Digital packet
  Serial.print("D:");
  for(int i = 0; i < 6; i++) {
    if(i<5){
      Serial.print(Digital[i], DEC); Serial.print(",");
    }
    else if (i == 5) {
      Serial.print(Digital[i], DEC); Serial.print(";;");
    }
  }
}


    
    
  


