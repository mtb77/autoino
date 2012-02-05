#include <Servo.h> 
#include <LiquidCrystal.h>
#include <MeetAndroid.h>

Servo myservo;  
Servo mymotor;   
int pos = 0;     

String command;
String value;
char mode;

MeetAndroid meetAndroid;
LiquidCrystal lcd(13, 12, 11, 10, 9, 8);

void setup() {  
  lcd.begin(16, 2); 
  
  Serial.begin(115200);  
  myservo.attach(3);   
  mymotor.attach(5);     
  motor(85);
  servo(85);
  meetAndroid.registerFunction(red, 'o'); 
  meetAndroid.registerFunction(debugString, 'd'); 
  meetAndroid.registerFunction(androidMotor, 'm'); 
  meetAndroid.registerFunction(androidServo, 's'); 
  debug("Welcome to", "Autoino");
} 

void loop() {   
  meetAndroid.receive(); 
  /*
  if(Serial.available() > 0) {
    char serial = Serial.read(); 
    
    Serial.write(serial);
    
    switch (serial) {
      case 'c':  
        mode = serial;
        break;
      case 'v': 
        mode = serial;
        break;
      case 'x': 
        mode = ' ';
        debug(command, value);
        if(command == "SERVO") {
          char carray[6];
          value.toCharArray(carray,6);
          servo(atoi(carray));
        }
        if(command == "MOTOR") {
          char carray[6];
          value.toCharArray(carray,6);
          motor(atoi(carray));
        }
        command = "";
        value = "";
      default:
        switch (mode) {
           case 'c':   
             command += serial;
           break;
          case 'v':  
             value += serial; 
           break;
        }

    }
    
    
  } */
   /*
  for(int j = 115; j > 59; j--) {       
    myservo.write(j); 
   // motor.write(j);     
    debug("Servo " + (String)j, "Motor ");
    delay(100);
  }     
  delay(2000);*/
}
void red(byte flag, byte numOfValues) {
  analogWrite(6, meetAndroid.getInt());
}

void debugString(byte flag, byte numOfValues) {
  char f[64];
  meetAndroid.getString(f);
  lcd.clear();
  lcd.print(f); 
}

void androidServo(byte flag, byte numOfValues) {
  servo(meetAndroid.getInt());
}

void androidMotor(byte flag, byte numOfValues) {
  motor(meetAndroid.getInt());
}

void servo(int val) {
  //debug("Servo " + (String)val);
  myservo.write(val); 
}

void motor(int val) {
  //debug("Motor " + (String)val);
  mymotor.write(val); 
}

void debug(String firstLine, String secondLine) { 
  lcd.clear();
  lcd.print(firstLine);
  lcd.setCursor(0,1);
  lcd.print(secondLine);
}

void debug(String val) {
  debug(val, "");
}

