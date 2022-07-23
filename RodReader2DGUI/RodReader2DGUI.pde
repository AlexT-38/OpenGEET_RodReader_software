/** 

 OpenGEET Rodreader GUI v0.3
 
 Released under GNU GPL v3
 
 written by Alexander Timiney
 
 Created April/May 2014
 
 updated 20/07/2022
 
 -update
     added sync command when scanning ports to stimulate rodreader emulator into responding 
     (arduino auto resets when connecting to its com port)

 This code is a mess, the gui library is inflexible, and the project lacks adequate documentation.
 The intention is to switch to a Eclipse/Java environment and rebuild to whole project,
 preferably without requiring Processing.
 
 
 TODO (2014):
 read vector from manual set points
 update gui from params
 read params from reader on startup
 
 */



// Need G4P library
import g4p_controls.*;
//Need Serial library
import processing.serial.*;




//Serial port for comunicating with Rod Reader
Serial port;
String portName = "";

//hardware INT settings

//number of sample points to take
int points;
int steps;
int startStep;
// number of steps for a 1mm traversal
float stepsPerMM = 8;


//hardware FLOAT settings

// total distance to scan over
float distance;

float start = 0; //start position in mm
float stop = 200; //stop position in mm
float step = .1250; //step distance in mm



// rod data
float rodStart = 52;
float rodLength = 100;
float rodDia = 10;




//data storage

float[] calData; //data - calib

float[] drawData;
float[] drawCalib;

//data scaling

// number of pixels per LSb
float magScale = 0.33; //100 pixels for full range
float pixScale = 2; //1 pix = 0.5mm
float deltaScale =2;

// point at which reading is highest
int point_highest;
// point at which reading is lowest
int point_lowest;
// which axis to read when collecting data
//int scan_axis = 2; // 0=X, 1=Y, 2=Z


//program state
int mode = 0; //0 idle, 1 collecting calib data, 2 collecting scan data, 3 seeking min max




public void setup() {
  size(800, 600, JAVA2D);
  createGUI();
  customGUI();
  // Place your setup code here
  findSerial();
}

public void draw() {
  

  drawDisplay();


  if (port != null) {    //check for an open com port before doing anything
    readSerial();        //collect data from the serial port
    switch(mode) {        //act on that data according to the current mode
    case 0:
      if(doLoad){
        doLoad();
        doLoad = false;
      }
      break;
    case 1:
      doCalib();          //scan for calibration data
      break;   
    case 2:
      doScan();          //scan for rod data
      break;   
    
    }
  }
  else {                //if there's no serial port open, reset all modes to 0
    mode = 0;
    calibState = 0;
    dataState = 0;

  }
}

// Use this method to add additional statements
// to customise the GUI controls
public void customGUI() {
  String[] axis = {
    "X", "Y", "Z"
  };

  String[] sTarg = {
    "Calib", "Data", "Adjusted"
  };

  String[] sWgt = {
    "0.10", "0.20", "0.30", "0.40", "0.50"
  };

  String[] P2Ptmode = {
    "LSb", "%"
  };

  String[] SMacros = {
    "Light", "Medium", "Heavy", "Obscene"
  };
  
  String[] SensGain = {
   "0: 1370", "1: 1090", "2: 820", "3: 660", "4: 440", "5: 390", "6: 330", "7: 230" 
  };

  dropListPorts.setItems(Serial.list(), 0);
  dropListFindAxis.setItems(axis, 2);
  dropListSTarg.setItems(sTarg, 0);
  dropListSWgt.setItems(sWgt, 2);
  dropListP2PtolMode.setItems(P2Ptmode, 2);
  dropListSMacro.setItems(SMacros, 1);
  dropListSensorGain.setItems(SensGain,1);


  textfieldStart.setText(str(start));
  textfieldStop.setText(str(stop));
  textfieldStep.setText(str(step));

  textfieldRodPos.setText(str(rodStart));
  textfieldRodLen.setText(str(rodLength));
  textfieldRodDia.setText(str(rodDia));
}

float dmouseX = 0;
float dmouseY = 0;
void mouseDragged(){
  
  dmouseX = mouseX - pmouseX;
  dmouseY = mouseY - pmouseY;
 if(mouseButton == RIGHT){
  
  vpXPos += dmouseX /vpScale;
  vpYPos -= dmouseY /vpScale;
  
 } 
  
}

void mouseWheel(MouseEvent event) {
  float e = -event.getCount();
  vpScale *= pow(1.05, e);
  sWeight = lineSize/vpScale;
  mprintln(sWeight);
}

void mouseClicked(){
  if(mouseX < height && mouseButton == LEFT){
    float pos = ((height - mouseY + vpCentreY )/vpScale) - vpYPos ;
    pickPoint(pos);
    
    
    
  }
  
  
}
void mprintln(){
 println();
 //textarea1.appendText("\n"); 
}
void mprintln(float f){
 mprintln(Float.toString(f)); 
}
void mprintln(int i){
 mprintln(Integer.toString(i)); 
}
void mprintln(String s){
 println(s);
 //textarea1.appendText(s + '\n'); 
 textarea1.appendText(s); 
  
}

void mprint(float f){
 mprint(Float.toString(f)); 
}
void mprint(int i){
 mprint(Integer.toString(i)); 
}void mprint(String s){
 print(s);
 
 //String newString = textarea1.getText();
 
 //newString = newString + s;
 //textarea1.setText(newString); 
  
  
}


void refreshGui(){
  //rodPos, rodLen, rodDia
  //start, stop, step
  //gain, dist
  //pick points
  
  
  
  
}

