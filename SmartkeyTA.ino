#include<SoftwareSerial.h>
#include<EEPROM.h>
#define sizeOfData 200
#define btnStarter 12
#define RelayKunci 3
#define RelayStarter 4

SoftwareSerial bluetooth(8,9);

//default global variable (bisa diakses dimana2)
boolean flagCon = false;    //true saat ada yg login
String conn = "conn";       //string yg terusmenerus dikirim android utk indikator koneksi
long timeConn = 0;          //variable penghitung waktu conn 
long timeLimitConn = 0;  //waktu utk mesin mati otomatis stelah conn tidak diterima
long timeToleranceConn = 16000; // waktu indikasi masih connect atau tidak
boolean flagBtn = false;    //untuk kebutuhan multitasking button
int state = 1;              //state motor dimulai dari state1
int whichBtn = 0;           // Untuk membaca tombol mana yang tertekan. Ditaruh di global, agar tidak terjadi inisialisasi terus menerus dalam looping

void setup() {
  // inisialisasi
  bluetooth.begin(9600);
  Serial.begin(9600);
  EEPROM.begin();
  pinMode(RelayKunci, OUTPUT);
  pinMode(RelayStarter, OUTPUT);
  pinMode(btnStarter, INPUT_PULLUP);

  // Kondisi Relay awal non-aktif, dengan menggunakan Relay active LOW
  digitalWrite(RelayKunci, HIGH);
  digitalWrite(RelayStarter, HIGH);
}


void loop() {
  // Kalau belum Login, flagCon = false
  //===============================LOGIN===================================
  if (flagCon == false){
    if(bluetooth.available()){
      String message = "";
      char a;
      while(bluetooth.available() &&  (a = (char)bluetooth.read()) != '\n'){
        message = message + a;
        delay(1);
      }
      Serial.println(message);
      
      String command = message.substring(0, message.indexOf(";")); //mengambil perintah
      String payload = message.substring(message.indexOf(";")+1, message.indexOf(".")); //mengambil isi pesan perintah

      
      if(command.equals("login")){
        int pos = payload.indexOf("+");
        String ID = payload.substring(0, pos);
        String PIN = payload.substring(pos+1);
        if(ID.length()>0 && PIN.length()==6){
          if(isUser(ID, PIN) || isAdmin(ID, PIN)){
            flagCon = true;
            bluetooth.println("success");
            Serial.println("Login Success\n");
            timeConn = millis(); //timeCoon dimulai pada saat berhasil login
          }
          else{
            bluetooth.println("failed");
            Serial.println("Login Failed");
          }
        }
        else{
          bluetooth.println("failed");
          Serial.println("Login Failed");
        }
      }
    }
    
  }
  //=================================Setelah Login======================
  else if(flagCon==true){
    if(bluetooth.available()){
      String message = "";
      char a;
      while(bluetooth.available() &&  ( (a = (char)bluetooth.read()) != '\n' )){
        message = message + a;
        delay(1);
      }
      message.trim();
      Serial.println("command :" + message);
      
      //format pengambilan pesan secara umum
      String command = message.substring(0, message.indexOf(";"));
      String payload = message.substring(message.indexOf(";")+1, message.indexOf(".")+1);

      // ========================= COMMAND "COMMAND" =============================
      if(command.equals("command")){
        // Command untuk mengontrol relay dan berapa lama timeLimitConn 
        payload = payload.substring(0, payload.indexOf("."));

        String b = payload.substring(0, payload.indexOf(","));
        // Pengaturan untuk setting-an timeLimitConn
        if(b.equals("sec")){
          timeLimitConn = payload.substring(payload.indexOf(",")+1).toInt()*1000;
          Serial.println("Time Limit = " + String(timeLimitConn) + " ms");
        }

        // Kontrol untuk Relay
        if(state == 1){                       //pada state 1 hanya tersedia on1 dan on2
          if(payload.equals("on1")){
            digitalWrite(RelayKunci, LOW);
            state = 2;
            Serial.println("state = 2");
          }
          else if(payload.equals("on2")){
            digitalWrite(RelayKunci, LOW);
            delay(3000);
            digitalWrite(RelayStarter, LOW);
            delay(1500);
            digitalWrite(RelayStarter, HIGH);
            state = 3;
            Serial.println("state = 3");
          }
          else {
            bluetooth.println("invalid input");
          }
        }
        else if(state == 2){                  //pada state 2 hanya tersedia on2 dan off
          if(payload.equals("on2")){
            digitalWrite(RelayStarter, LOW);
            delay(1500);
            digitalWrite(RelayStarter, HIGH);
            state = 3;
            Serial.println("state = 3");
          }
          else if(payload.equals("off")){                   
            digitalWrite(RelayKunci, HIGH);
            state = 1;
            Serial.println("state = 1");
          }
          else {
            bluetooth.println("invalid input");
          }
        }
        else if(state == 3){                    //pada state 3 hanya tersedia off
          if(payload.equals("off")){
            digitalWrite(RelayKunci, HIGH);
            state = 1;
            Serial.println("state = 1");
          }
          else {
            bluetooth.println("invalid input");
          }
        }
      }
      // ================== Command ADD ===========================
      else if(command.equals("add")){
        bool flagFormatOK = true;
        int pos1 = payload.indexOf(",");
        int pos2;
        String Admin = payload.substring(0, pos1);
        pos2 = pos1;
        pos1 = payload.indexOf("+", pos2+1);
        if(pos2<0 || pos1<0){
          flagFormatOK = false;
        }
        String idToAdd = payload.substring(pos2+1, pos1);
        pos2 = pos1;
        pos1 = payload.indexOf(".", pos2+1);
        if(pos2<0 || pos1<0){
          flagFormatOK = false;
        }
        String pinToAdd = payload.substring(pos2+1, pos1);
        String IDAdmin = Admin.substring(0, Admin.indexOf("+"));
        String pinAdmin = Admin.substring(Admin.indexOf("+")+1);
        IDAdmin.trim();
        pinAdmin.trim();
        if(idToAdd.length()>0 && pinToAdd.length() > 0 && flagFormatOK){
          if(isAdmin(IDAdmin, pinAdmin)){
            String data = getEEPROM();
            Serial.println("before: " + data);
            String data2 = data;
            data = data.substring(0, data.indexOf("#"));
            if((data2 = data2.substring(data2.indexOf("#")+1, data2.indexOf("."))).length()>0){
              data2 += ",";
            }
            String dataFinal = data + "#" + data2 + idToAdd  + "+" + pinToAdd + ".";
            
            writeEEPROM(dataFinal);
            data = getEEPROM();
            Serial.println("after : " + data + "\n");
            bluetooth.println("addsuccess");
          }
          // Kalau bukan admin
          else{
            bluetooth.println("addfailed");
          }
        }
        else{
          bluetooth.println("addfailed");
        }
      }
      // ================== Command Delete ===========================
      else if(command.equals("delete")){
        bool flagFormatOK = true;
        int pos1 = payload.indexOf(",");
        int pos2;
        if(pos2<0 || pos1<0){
          flagFormatOK = false;
        }
        String Admin = payload.substring(0, pos1);
        pos2 = pos1;
        pos1 = payload.indexOf(".", pos2+1);
        if(pos2<0 || pos1<0){
          flagFormatOK = false;
        }
        String idToDelete = payload.substring(pos2+1, pos1);
        String IDAdmin = Admin.substring(0, Admin.indexOf("+"));
        String pinAdmin = Admin.substring(Admin.indexOf("+")+1);
        
        if(idToDelete.length()>0 && flagFormatOK){
          if(isAdmin(IDAdmin, pinAdmin)){
            String data = getEEPROM();
            String data2 = data;
            String awal;
            String akhir = "";
            bool flagisThere = false;
            String bacaID;
            int pos2 = data.indexOf("#");
            int pos1 = data.indexOf("+", pos2);
            while(!flagisThere){
              // kalau hasil dari pos1 -1, langsung keluar dari while
              if(pos1<0 || pos2<0){
                break;
              }
              bacaID = data.substring(pos2 + 1, pos1);
              if(bacaID.equals(idToDelete)){
                awal = data.substring(0, pos2+1);
                pos1 = data.indexOf(",", pos2+1);
                if(pos1>0){
                  akhir = data.substring(pos1+1);
                }
                else{
                  awal.remove(awal.length()-1);
                  if(pos2 == data.indexOf("#")){
                    awal += "#.";
                  }
                  else{
                    awal += ".";
                  }
                }
                flagisThere = true;
              }
              pos2 = data.indexOf(",", pos2+1);
              pos1 = data.indexOf("+", pos2+1);
            }
            if(flagisThere){
              Serial.println("before: " + data2);
              String dataFinal = awal + akhir;
              writeEEPROM(dataFinal);
              data = getEEPROM();
              Serial.println("after : " + data + "\n");
              bluetooth.println("deletesuccess");
            }
            else{
              bluetooth.println("deletefailed");
            }
          }
          // Kalau bukan admin
          else{
            bluetooth.println("deletefailed");
          }
        }
        else{
          bluetooth.println("deletefailed");
        }
      }
      // ================= EDIT ADMIN =======================
      else if (command.equals("editadmin")){
        bool flagFormatOK = true;
        String eeprom = getEEPROM();
        String dataAkhir = eeprom.substring(eeprom.indexOf("#"));
        int pos2 = 0;
        int pos1 = payload.indexOf(",");
        if(pos1<0){
          flagFormatOK = false;
        }
        String Admin = payload.substring(pos2, pos1);
        String IDAdmin = Admin.substring(0, Admin.indexOf("+"));
        String pinAdmin = Admin.substring(Admin.indexOf("+")+1);
        pos2 = pos1;
        pos1 = payload.indexOf(".");
        if(pos1<0){
          flagFormatOK = false;
        }
        String AdminToReplace = payload.substring(pos2+1, pos1);
        String IDToReplace = AdminToReplace.substring(0, AdminToReplace.indexOf("+"));
        String pinToReplace = AdminToReplace.substring(AdminToReplace.indexOf("+")+1);
        if(pinToReplace.length()!=6){
          flagFormatOK = false;
        }
        if(isAdmin(IDAdmin, pinAdmin) && flagFormatOK){
          String dataAwal = IDToReplace + "+" + pinToReplace;
          String dataFinal = dataAwal + dataAkhir;
          writeEEPROM(dataFinal);
          Serial.println("before: " + eeprom);
          Serial.println("after : " + getEEPROM() + "\n");
          bluetooth.println("editadminsuccess");
        }else {
          bluetooth.println("editadminfailed");
        }
      }
      // ===================== GET ALL USERS =====================
      else if(command.equals("getallusers")){
        String Admin = payload.substring(0, payload.indexOf("."));
        String IDAdmin = Admin.substring(0, Admin.indexOf("+"));
        String pinAdmin = Admin.substring(Admin.indexOf("+")+1);
        if(isAdmin(IDAdmin, pinAdmin)){
          Serial.println("Users Sent: ");
          String users = getAllUsers();
          bluetooth.println(users);
          Serial.println(users);
        }
        else{
          bluetooth.println("getallusersfailed");
        }
      }
      // ============= COMMAND LOGOUT ====================
      else if(command.equals("logout")){
        digitalWrite(RelayKunci, HIGH);
        flagCon = false;
        Serial.println("User has Logged out");
      }
      else if(command.equals(conn)){
        timeConn = millis();
      }
    }

    // ==================== FLAG BUTTON ======================
    int whichBtn = buttonPushed();
    if(whichBtn>0){                                //jika button tertekan
                                            // btncommand 1: tekan1x; 2: tekan 2x; 3: tekan 1xtahan; 
      if(state == 1){                       //pada state 1 hanya tersedia btncommand 1 dan 2
        if(whichBtn == 1){
          digitalWrite(RelayKunci, LOW);
          state = 2;
          Serial.println("state = 2");
        }
        else if(whichBtn == 2){
          digitalWrite(RelayKunci, LOW);
          delay(3000);
          digitalWrite(RelayStarter, LOW);
          delay(1500);
          digitalWrite(RelayStarter, HIGH);
          state = 3;
          Serial.println("state = 3");
        }
      }
      else if(state == 2){                  //pada state 2 hanya tersedia btncommand 1 dan 3
        if(whichBtn == 1){
          digitalWrite(RelayStarter, LOW);
          delay(1500);
          digitalWrite(RelayStarter, HIGH);
          state = 3;
          Serial.println("state = 3");
        }
        else if(whichBtn == 3){                   
          digitalWrite(RelayKunci, HIGH);
          state = 1;
          Serial.println("state = 1");
        }
      }
      else if(state == 3){                    //pada state 3 hanya tersedia btncommand 3
        if(whichBtn == 3){
          digitalWrite(RelayKunci, HIGH);
          state = 1;
          Serial.println("state = 1");
        }
      }
    }
    if(flagBtn){
      if(digitalRead(btnStarter)){
        flagBtn = false;
        delay(200); // Pengaman untuk Button tidak tertekan lagi setelahnya
      }
    }
    if(millis() - timeConn>timeToleranceConn){
      Serial.println("disconnected");
      flagCon = false;
    }
  }

  // ====================================== Security ========================================
  
  if(!flagCon){
    if(buttonPushed()>0){
      digitalWrite(RelayKunci, HIGH);digitalWrite(RelayStarter, HIGH);
      Serial.println("Engine turned off");
      state = 1;
    }
    if(timeLimitConn>0){  
      if(millis()-timeConn>timeLimitConn){
        digitalWrite(RelayKunci, HIGH);digitalWrite(RelayStarter, HIGH);
        Serial.println("Engine turned off");
        state = 1;
      } 
    }
  }
}

// =============== BTN ========================
long pastBtn;                               //penghitung lama button ditekan
int buttonPushed(){
  if(flagBtn){
    return 0;                             //selama flagbtn true atau tombol masih tertekan dari aktifitas sblmnya, maka tidak boleh membaca btn
  }
  if(!digitalRead(btnStarter)){
    flagBtn = true;
    pastBtn = millis();
    delay(10);
    while(!digitalRead(btnStarter) && millis()-pastBtn<1500);  //selama button ditekan selama 1.5detik akan terbaca sbg btncommand 3
    if(millis()-pastBtn>=1500){
      Serial.println("Button hold");
      return 3;
    }
    delay(50);
    while(millis()-pastBtn<500){                //selama 0.5detik tombol ditekan lagi akan terbaca sbg btncommand 2
      if(!digitalRead(btnStarter)){
        delay(20);
        Serial.println("Button twice");
        return 2;
      }
    }
    Serial.println("Button once");              //selama 0.5detik tombol TIDAK ditekan lagi akan terbaca sbg btncommand 1
    return 1;
  }
  return 0;
}

// ======================== EEPROM ==================================
void clearEEPROM(){
  for(int i=0; i<200; i++){
    EEPROM.write(i, 0);
  }
}

String getEEPROM(){
  String dataEEPROM = "";
  char dummy;
  for(int i=0; i<200; i++){
    dummy = (char)EEPROM.read(i);
    dataEEPROM += dummy;
    if( dummy == '.' ){       //mengambil data EEPROM sampai titik
      break;  
    }
  }
  dataEEPROM = dataEEPROM.substring(0, dataEEPROM.indexOf(".")+1);
  return dataEEPROM;
}

//========================== GET ALL USERS ======================================

String getAllUsers(){
  String dataEEPROM = getEEPROM();
  dataEEPROM = dataEEPROM.substring(dataEEPROM.indexOf("#")+1);
  //get all users ini hanya akan mengambil ID dari keseluruhan pesan
  int pos1 = dataEEPROM.indexOf(",");
  int pos2 = 0;
  String dataFinal = "";
  while(dataEEPROM.indexOf(".", pos2)>0){
    if(dataEEPROM.indexOf(",", pos2)>0){
      dataFinal += dataEEPROM.substring(pos2, dataEEPROM.indexOf("+", pos2)) + ",";
    }
    else{
      pos1 = dataEEPROM.indexOf(".");
      dataFinal += dataEEPROM.substring(pos2, dataEEPROM.indexOf("+", pos2)) + ".";
      break;
    }
    pos2 = pos1+1;
    pos1 = dataEEPROM.indexOf(",", pos2);
  }
  return dataFinal;
}
//cara kerja EEPROM dgn menghapus semua data dan write kembali
void writeEEPROM(String data){
  clearEEPROM();
  for(int i=0; i<data.length(); i++){
    EEPROM.write(i, data.charAt(i));
  }
}

// ========================= Authentication =================================
boolean isAdmin(String IDInput, String pinInput){ //melihat apakah data yg masuk merupakan admin atau bukan
  String ID;
  String pin;
  String eeprom;
  eeprom = getEEPROM();
  ID = eeprom.substring(0, eeprom.indexOf("+"));
  pin = eeprom.substring(eeprom.indexOf("+")+1, eeprom.indexOf("#"));
  if(ID.equals(IDInput) && pin.equals(pinInput)){
    return true;
  }
  return false;
}

boolean isUser(String IDInput, String pinInput){
  String user;
  String ID;
  String pin;
  String eeprom;
  eeprom = getEEPROM();
  int pos2 = eeprom.indexOf("#");
  int pos1 = eeprom.indexOf(",", pos2+1);
  if(pos1<0){
    pos1 = eeprom.indexOf(".");
  }
  if(eeprom.substring(pos2+1, pos1).length()>0){
    while(true){
      user = eeprom.substring(pos2+1, pos1);
      ID = user.substring(0, user.indexOf("+"));
      pin = user.substring(user.indexOf("+")+1);
      if(ID.equals(IDInput) && pin.equals(pinInput)){
        return true;
      }
      pos2 = pos1;
      pos1 = eeprom.indexOf(",", pos2+1);
      if(pos1<0){
        pos1 = eeprom.indexOf(".");
      }
      if(pos2 == eeprom.indexOf(".")){
        break;
      }
    }
  }
  else{
    return false;
  }
  return false;
}
