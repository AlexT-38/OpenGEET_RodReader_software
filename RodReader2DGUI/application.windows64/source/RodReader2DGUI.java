import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import g4p_controls.*; 
import processing.serial.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class RodReader2DGUI extends PApplet {

/** 1D rod reader software
 
 TODO:
 read vector from manual set points
 update gui from params
 read params from reader on startup
 
 */



// Need G4P library

//Need Serial library





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
float step = .1250f; //step distance in mm



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
float magScale = 0.33f; //100 pixels for full range
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
public void mouseDragged(){
  
  dmouseX = mouseX - pmouseX;
  dmouseY = mouseY - pmouseY;
 if(mouseButton == RIGHT){
  
  vpXPos += dmouseX /vpScale;
  vpYPos -= dmouseY /vpScale;
  
 } 
  
}

public void mouseWheel(MouseEvent event) {
  float e = -event.getCount();
  vpScale *= pow(1.05f, e);
  sWeight = lineSize/vpScale;
  mprintln(sWeight);
}

public void mouseClicked(){
  if(mouseX < height && mouseButton == LEFT){
    float pos = ((height - mouseY + vpCentreY )/vpScale) - vpYPos ;
    pickPoint(pos);
    
    
    
  }
  
  
}
public void mprintln(){
 println();
 //textarea1.appendText("\n"); 
}
public void mprintln(float f){
 mprintln(Float.toString(f)); 
}
public void mprintln(int i){
 mprintln(Integer.toString(i)); 
}
public void mprintln(String s){
 println(s);
 //textarea1.appendText(s + '\n'); 
 textarea1.appendText(s); 
  
}

public void mprint(String s){
 print(s);
 
 //String newString = textarea1.getText();
 
 //newString = newString + s;
 //textarea1.setText(newString); 
  
  
}


public void refreshGui(){
  //rodPos, rodLen, rodDia
  //start, stop, step
  //gain, dist
  //pick points
  
  
  
  
}



String[] csvHeader = {"RODREADER DATA 3AXIS 0.1", "", "",
                      "Calib Scan TimeStamp, ",
                    "Data Scan TimeStamp, ",
                  "Calib Gain, ",
                "Data Gain, ",
              "Calib Dist, ",
            "Data Dist, ", "",
          "Rod Start, Rod Length","",
      "Start Pos, End Pos, Step Size","","",
    "Calib.Pos, Calib.x, Calib.y, Calib.z, RawData.x, RawData.y, RawData.z, AdjData.x, AdjData.y, AdjData.z"
  };


int timeoutShort = 200;  //timeout value when we expect an immediate response
int timeoutLong = 10000;  //timeout value when we may be waiting on the rod reader to move around, possibly at very low speed


int sensorGain = 1;
float sensorDist = 6;

float[] gainLevels = {
  1370, 1090, 820, 660, 440, 390, 330, 230 
};



// readings with rod - .x = position, .y = value
float[] dataPosRaw;
PVector[] dataRaw;
int dataRawSize = 0;


String dataTStamp = "";
int dataSensorGain = 1;
float dataSensorDist = 6;


// readings without rod
float[] calibPosRaw;
PVector[] calibRaw;
int calibRawSize = 0;

String calibTStamp = "";
int calibSensorGain = 1;
float calibSensorDist = 6;

//adjusted data
float[] adjPosRaw;
PVector[] adjRaw;
int adjRawSize = 0;

int serialTimeout = 0; //currently this is based upon frames, but should be changed later to milliseconds

int calibState = 0; //0 command, 1 ack, 2 start scan, 3 data, 4 end
public void doCalib() {
  switch(calibState) {
  case 0:
    if (port == null) {
      mprintln("No com port open");
      mode = 0;
      break;
    }
    if (points <2) {
      mprintln("Less than two point to be scanned");
      mode = 0;
      break;
    }
    if (serialBuffer.size() == 0) {    //check to see if we've got serial Data stored
      mprintln("Sending command 'SCAN'; calibState: 1");
      sendCommand("SCAN");          //if not, send the scan command and go to stage two
      calibState = 1;        
      serialTimeout = 0;          //reset timout timer
    }
    else {
      mprintln("Flushing buffer before sending 'SCAN' command");
      serialBuffer.clear();      //otherwise clear the buffer, dont start yet - wait to see if more data comes in
    }                            //this way if the reader is outputing data, we're less likely to clash
    break;
  case 1:
    if (serialBuffer.size() > 0) {  //check for data in buffer
      if (serialBuffer.get(0).equals(">SCAN")) {  //check that the arduino has echoed the command
        mprintln("Got echo; calibState: 2");
        calibState = 2;      //go to next stage (start of scan)
        serialTimeout = 0;    //reset timeout timer
      }
      else {
        calibState = 0;      //got a wrong response to the command, reset the mode
        mode = 0;
        mprintln("Got wrong response to command: " + serialBuffer.get(0) + "; Aborting.");
      }
      serialBuffer.remove(0);    //remove the line from the buffer
    } 
    else {
      serialTimeout++;      //increment timeout
      if (serialTimeout > timeoutShort) {  //if we haven't heard from the arduino in 60 frames, cancel to scan
        mprintln("Timed out waiting for echo");
        calibState = 0;
        mode = 0;
      }
    }   
    break;
  case 2:
    if (serialBuffer.size() > 0) {  //check for data in buffer
      if (serialBuffer.get(0).equals("SCAN")) {  //check that the arduino has started the command
        mprintln("Scan has started; calibState: 3");
        calibState = 3;      //go to next stage (start of scan)
        serialTimeout = 0;    //reset timeout timer
        calibPosRaw = new float[points];
        calibRaw = new PVector[points]; //reset the calib data
        calibRawSize = 0; //reset the position - should be equal to points when finished
        calibSensorGain = sensorGain;
        calibSensorDist = sensorDist;
        calibTStamp = datetime();
      }
      else {
        mprintln("Didn't get 'SCAN'");
        calibState = 0;      //got a wrong response to the command, reset the mode
        mode = 0;
      }
      serialBuffer.remove(0);    //remove the line from the buffer
    } 
    else {
      serialTimeout++;      //increment timeout
      if (serialTimeout > timeoutLong) {  //if we haven't heard from the arduino in 600 frames, cancel to scan
        mprintln("Timed out waiting for SCAN start");
        calibState = 0;
        mode = 0;
      }
    } 
    break;
  case 3:  //collecting data

    if (serialBuffer.size() > 0) {  //check for data in the buffer
      while (serialBuffer.size () > 0) {  //go through all data in the buffer
        //parse the data
        String in = serialBuffer.get(0);
        if (in.equals("END")) {  //this would happen if the buffer collects this message
          //before we've parsed all the data
          // unless we make this loop break out when the buffer is full
          //which we should do so we can check for incomplete buffer
          mprintln("Received 'END' before buffer full - missed data or reader not setup?; calibState: 5");
          calibState = 5;
          serialTimeout = 0;
        }
        else {
          String[] sub1 = split(in, ':');
          if (sub1.length == 2) {
            calibPosRaw[calibRawSize] = PApplet.parseFloat(sub1[0])/stepsPerMM;
              String[] sub2 = split(sub1[1], ',');
            if (sub2.length == 3) {
              PVector vec = new PVector( PApplet.parseFloat(sub2[0]), PApplet.parseFloat(sub2[1]), PApplet.parseFloat(sub2[2]));
              //mprintln(vec);
              calibRaw[calibRawSize++] = vec;
              if (calibRawSize == points) {
                mprintln("Data Buffer full; calibState: 4");
                calibState = 4;  
                serialTimeout = 0;
                serialBuffer.remove(0);    //remove the line from the buffer
                break;              //break out of the loop
              }
            }
          }
          else {
            //we shouldnt ever get here... so what to do? we can abort, or we can ignore
          }
        }
        serialBuffer.remove(0); //remove the parsed datum from the buffer
      }
      //serialBuffer.clear();  //clear the parsed data -- if not all the data in the buffer was parsed, data is lost!
    } 
    else {
      serialTimeout++;      //increment timeout
      if (serialTimeout > timeoutLong) {  //if we haven't heard from the arduino in 600 frames, cancel to scan
        mprintln("Timed out waiting for data");
        calibState = 0;
        mode = 0;
      }
    }


    break;


  case 4:
    if (serialBuffer.size() > 0) {  //check for data in buffer
      if (serialBuffer.get(0).equals("END")) {  //check that the arduino has echoed the command
        mprintln("Received 'END'; calibState: 5");
        calibState = 5;      //go to next stage (end of scan)
        serialTimeout = 0;    //reset timeout timer
      }
      else {
        calibState = 0;      //didnt get end
        mode = 0;
        mprint("Did not receive 'END'... Guru Meditation Error: Got:- ");
        mprintln(serialBuffer.get(0));
      }
      serialBuffer.remove(0);    //remove the line from the buffer
    } 
    else {
      serialTimeout++;      //increment timeout
      if (serialTimeout > timeoutShort) {  //if we haven't heard from the arduino in 60 frames, cancel to scan
        mprintln("Timed out waiting for 'END'");
        calibState = 0;
        mode = 0;
      }
    }
    break;

  case 5:
    mprintln("All is good in the world. Relax and enjoy.");
    mprintln("Now would be a good time to update any buffered graphic data...");
    calibState = 0;
    mode = 0;

    if(checkboxSAuto.isSelected()){
     sMacro(calibRaw, calibRawSize); 
      
      
    }

    break;


  default:              //only occur in the event of a chronic error
    mprintln("ERROR! calibState out of range: " + str(calibState));  //print an error message
    calibState = 0;    //reset the mode and state
    mode = 0;

    break;
  }
}

int dataState = 0; //0 send command, 1 collect data, 2 bleh
public void doScan() {
  switch(dataState) {
  case 0:
    if (port == null) {
      mprintln("No com port open");
      mode = 0;
      break;
    }
    if (points <2) {
      mprintln("Less than two point to be scanned");
      mode = 0;
      break;
    }
    if (serialBuffer.size() == 0) {    //check to see if we've got serial Data stored
      mprintln("Sending command 'SCAN'; dataState: 1");
      sendCommand("SCAN");          //if not, send the scan command and go to stage two
      dataState = 1;        
      serialTimeout = 0;          //reset timout timer
    }
    else {
      mprintln("Flushing buffer before sending 'SCAN' command");
      serialBuffer.clear();      //otherwise clear the buffer, dont start yet - wait to see if more data comes in
    }                            //this way if the reader is outputing data, we're less likely to clash
    break;
  case 1:
    if (serialBuffer.size() > 0) {  //check for data in buffer
      if (serialBuffer.get(0).equals(">SCAN")) {  //check that the arduino has echoed the command
        mprintln("Got echo; dataState: 2");
        dataState = 2;      //go to next stage (start of scan)
        serialTimeout = 0;    //reset timeout timer
      }
      else {
        dataState = 0;      //got a wrong response to the command, reset the mode
        mode = 0;
        mprintln("Got wrong response to command: " + serialBuffer.get(0) + "; Aborting.");
      }
      serialBuffer.remove(0);    //remove the line from the buffer
    } 
    else {
      serialTimeout++;      //increment timeout
      if (serialTimeout > timeoutShort) {  //if we haven't heard from the arduino in 60 frames, cancel to scan
        mprintln("Timed out waiting for echo");
        dataState = 0;
        mode = 0;
      }
    }   
    break;
  case 2:
    if (serialBuffer.size() > 0) {  //check for data in buffer
      if (serialBuffer.get(0).equals("SCAN")) {  //check that the arduino has started the command
        mprintln("Scan has started; dataState: 3");
        dataState = 3;      //go to next stage (start of scan)
        serialTimeout = 0;    //reset timeout timer
        dataPosRaw = new float[points];
        dataRaw = new PVector[points]; //reset the data data
        dataRawSize = 0; //reset the position - should be equal to points when finished
        dataSensorGain = sensorGain;
        dataSensorDist = sensorDist;
        dataTStamp = datetime();
      }
      else {
        mprintln("Didn't get 'SCAN'");
        dataState = 0;      //got a wrong response to the command, reset the mode
        mode = 0;
      }
      serialBuffer.remove(0);    //remove the line from the buffer
    } 
    else {
      serialTimeout++;      //increment timeout
      if (serialTimeout > timeoutLong) {  //if we haven't heard from the arduino in 600 frames, cancel to scan
        mprintln("Timed out waiting for SCAN start");
        dataState = 0;
        mode = 0;
      }
    } 
    break;
  case 3:  //collecting data

    if (serialBuffer.size() > 0) {  //check for data in the buffer
      while (serialBuffer.size () > 0) {  //go through all data in the buffer
        //parse the data
        String in = serialBuffer.get(0);
        if (in.equals("END")) {  //this would happen if the buffer collects this message
          //before we've parsed all the data
          // unless we make this loop break out when the buffer is full
          //which we should do so we can check for incomplete buffer
          mprintln("Received 'END' before buffer full - missed data or reader not setup?; dataState: 5");
          dataState = 5;
          serialTimeout = 0;
        }
        else {
          String[] sub1 = split(in, ':');
          if (sub1.length == 2) {
            dataPosRaw[dataRawSize] = PApplet.parseFloat(sub1[0])/stepsPerMM;
            String[] sub2 = split(sub1[1], ',');
            if (sub2.length == 3) {
              PVector vec = new PVector(PApplet.parseFloat(sub2[0]), PApplet.parseFloat(sub2[1]), PApplet.parseFloat(sub2[2]));
              dataRaw[dataRawSize++] = vec;
              if (dataRawSize == points) {
                mprintln("Data Buffer full; dataState: 4");
                dataState = 4;  
                serialTimeout = 0;
                serialBuffer.remove(0);    //remove the line from the buffer
                break;              //break out of the loop
              }
            }
          }
          else {
            //we shouldnt ever get here... so what to do? we can abort, or we can ignore
          }
        }
        serialBuffer.remove(0); //remove the parsed datum from the buffer
      }
      //serialBuffer.clear();  //clear the parsed data -- if not all the data in the buffer was parsed, data is lost!
    } 
    else {
      serialTimeout++;      //increment timeout
      if (serialTimeout > timeoutLong) {  //if we haven't heard from the arduino in 600 frames, cancel to scan
        mprintln("Timed out waiting for data");
        dataState = 0;
        mode = 0;
      }
    }


    break;


  case 4:
    if (serialBuffer.size() > 0) {  //check for data in buffer
      if (serialBuffer.get(0).equals("END")) {  //check that the arduino has echoed the command
        mprintln("Received 'END'; dataState: 5");
        dataState = 5;      //go to next stage (end of scan)
        serialTimeout = 0;    //reset timeout timer
      }
      else {
        dataState = 0;      //didnt get end
        mode = 0;
        print("Did not receive 'END'... Guru Meditation Error: Got:- ");
        mprintln(serialBuffer.get(0));
      }
      serialBuffer.remove(0);    //remove the line from the buffer
    } 
    else {
      serialTimeout++;      //increment timeout
      if (serialTimeout > timeoutShort) {  //if we haven't heard from the arduino in 60 frames, cancel to scan
        mprintln("Timed out waiting for 'END'");
        dataState = 0;
        mode = 0;
      }
    }
    break;

  case 5:
    mprintln("All is good in the world. Relax and enjoy.");
    dataState = 0;
    mode = 0;
    
    if(checkboxSAuto.isSelected()){
     sMacro(dataRaw, dataRawSize); 
      
      
    }

    calcAdj();
    //calcDelta(adjRaw, adjRawSize);    




    break;


  default:              //only occur in the event of a chronic error
    mprintln("ERROR! dataState out of range: " + str(dataState));  //print an error message
    dataState = 0;    //reset the mode and state
    mode = 0;

    break;
  }
}



/*
PVector[] deltaArray;
 int deltaSize;
 
 void calcADelta() {
 calcDelta(adjRaw, adjRawSize);
 }
 
 void calcDelta(PVector[] vecArray, int vecSize) {  //finds the difference between adjacent points, and plots that difference at the avg position
 if (vecArray != null && vecSize >1) {
 //mprintln("Calculating Delta Curve");
 deltaSize = vecSize-1;
 deltaArray = new PVector[deltaSize];
 for (int n = 0; n<deltaSize; n++) {
 PVector vec = new PVector();
 vec.x = (vecArray[n].x + vecArray[n+1].x)/2;
 vec.y = (vecArray[n+1].y - vecArray[n].y);
 deltaArray[n] = vec;
 }
 }
 }
 */
public void calcAdj() {
  if (calibRawSize == dataRawSize) { //assumption is most likely correct
    int size = dataRawSize;
    adjRawSize = size;
    adjRaw = new PVector[size];
    adjPosRaw = new float[size];
    for (int n = 0; n<size; n++) {
      PVector vec = new PVector();
      vec.y = dataRaw[n].y - calibRaw[n].y;
      vec.x = dataRaw[n].x - calibRaw[n].x;
      vec.z = dataRaw[n].z - calibRaw[n].z;
      if (dataPosRaw[n] != calibPosRaw[n]) {
        mprintln("Houston, we have a problem... data.x != calib.x at datum " + str(n));
        mprintln("data.x: " + str(vec.x) + "; calib.x: " +str(calibRaw[n].x));
        break;
      }
      adjRaw[n] = vec;
      adjPosRaw[n] = dataPosRaw[n];
    }
  }
  else {
    mprintln("Calib and Data are different lengths");
  }
}

/*
//find closest calibration data to datum.x, interpolate calib, subtract from datum and return
PVector adjustValue(PVector datum) {

  PVector adjusted = new PVector();

  return adjusted;
}
*/




StringList serialBuffer = new StringList();
long timeouttime = 2000;

public void findSerial() {

  mprintln("Auto finding port"); 
  port = null;

  String[] slist = Serial.list();

  for (int n = 0; n < slist.length; n++) {

    String tryPortName = slist[n];
    mprint("Trying " + tryPortName);

    try {
      port = new Serial(this, tryPortName, 115200);
      long starttime = millis();
      long timeout = 0;
      while(port.available() == 0 && timeout < timeouttime){
        Thread.sleep(50);
        timeout = millis() - starttime;
        mprint(".");
      }
      mprintln();
      String message = port.readStringUntil('\n');
      if (message != null) {
        if (message.startsWith(">RODREADER")) {
          mprintln(message);
          portName = tryPortName;
          dropListPorts.setItems(slist, n);
          return;
        }
      }
    }
    catch(Exception e) {
      mprintln("Could not open " + dropListPorts.getSelectedText() + ".");
      e.printStackTrace();
    }
  } 

  mprintln("No rod reader found");
  //port.stop();
  port = null;
}


public void readSerial() {

  while (port.available () > 0) {
    String in = port.readStringUntil('\n');
    if (in != null) {
      in = in.trim();
      serialBuffer.append(in); //store the string intothe buffer without whitespaces, ie the newline character
      println(in);
    } else {
      break;
    }
  }
}


public void updateParams() {




  points = 1 + PApplet.parseInt((stop - start) / step);            //the number of points is the difference between the two divided by the step size
  mprintln("Points: " +str(points));

  String[] commands = new String[3];

  commands[0] = "START" + str(startStep);  //send the data to the rod reader
  commands[1] = "POINTS" + str(points);  //send the data to the rod reader
  commands[2] = "STEPS" + str(steps);      

  if (!sendCommands(commands)) {
    //mprintln("Params will be sent when a com port is connected");
  }
}

public boolean sendCommand(String command) {

  if (port != null) {

    port.write(command + "\n");
    return true;
  } else {
    mprintln("No com port open");
    return false;
  }
}
public boolean sendCommands(String[] commands) {

  if (port != null) {
    for (int n = 0; n < commands.length; n++) {
      if (commands[n]!=null) {
        port.write(commands[n] + "\n");
      }
    }
    return true;
  } else {
    mprintln("No com port open");
    return false;
  }
}

float panStrength =600;
float zoomBase = 2;

// drawing offsets
//float x0 = 300;
//float y0 = 550;

float x0 = 00;
float y0 = 0;
float yn = y0 - x0;

float vpCentreY = -300;
float vpCentreX = -vpCentreY;


//viewport stuff

float vpScale = 1;
float vpXPos = 0;
float vpYPos = -150;

float lineSize = 1;
float sWeight = lineSize/vpScale;
public void drawDisplay() {




  drawBackground();

  pushMatrix();

  scale(1, -1);
  translate(vpCentreX, vpCentreY);

  scale(vpScale);
  translate(vpXPos, vpYPos);


  strokeWeight(sWeight);

  drawGrid();
  drawRod();
  drawData();
  drawManual();
  
  strokeWeight(1);

  popMatrix();

  drawMask();
}

public void drawManual() {
  pushStyle();
  if (showManA) {
    stroke(255,128,128,128);
    line(-1000,manAPos, 1000,manAPos);
  }
  if (showManB) {
    stroke(128,255,128,128);
    line(-1000,manBPos, 1000,manBPos);
  }
  if (showManD) {
    stroke(128,128,255,128);
  }
  popStyle();
}

public void drawBackground() {

  background(230);

  stroke(0);
  fill(255);
  rect(30, 30, 540, 540);
}

public void drawMask() {
  stroke(0);
  fill(255);
  rect( 585, 5, 210, 590);
}

public void drawGrid() {
  stroke(0);
  fill(255);

  //strokeWeight( 1 - (sliderVZoom.getStartLimit() + sliderVZoom.getValueF())/4);
  line(-100, 0, 100, 0);
  line(0, 0, 0, 250);
  //strokeWeight(1);

  stroke(128);
  for (int n=0; n<20; n++) {
    //float pos = n*10*pixScale;
    float pos = n*10;//*pixScale;
    line(-10, pos, 10, pos);
  }
}

public void drawRod() {

  stroke(100);
  noFill();

  //trect(-pixScale*rodDia/2, pixScale*rodStart, pixScale*rodDia/2, pixScale*(rodStart+rodLength));
  rect(rodDia/-2, rodStart, rodDia, (rodLength));
}

public void drawData() {
  if (checkboxShowRaw.isSelected()) {
    //calibration
    if (calibRaw != null && calibRawSize > 0) {
      for (int n = 0; n<calibRawSize; n++) {

        //float yy =(calibPosRaw[n] * pixScale); //the x/y swap here is confusing
        float yy =(calibPosRaw[n] ); //the x/y swap here is confusing
        if (checkboxShowX.isSelected()) {
          float xx =(calibRaw[n].x * magScale); //but thats because my graph is vertical
          stroke(150, 0, 0, 32);
          line(0, yy, xx, yy);
        }
        if (checkboxShowY.isSelected()) {
          float xx =(calibRaw[n].y * magScale); //but thats because my graph is vertical
          stroke(50, 100, 0, 32);
          line(0, yy, xx, yy);
        }
        if (checkboxShowZ.isSelected()) {
          float xx =(calibRaw[n].z * magScale); //but thats because my graph is vertical
          stroke(50, 0, 100, 32);
          line(0, yy, xx, yy);
        }
      }
    }
    // rod data
    if (dataRaw != null && dataRawSize > 0) {
      for (int n = 0; n<dataRawSize; n++) {

        //float yy = (dataPosRaw[n] * pixScale); //the x/y swap here is confusing
        float yy = (dataPosRaw[n]); //the x/y swap here is confusing
        if (checkboxShowX.isSelected()) {
          float xx = (dataRaw[n].x * magScale); //but thats because my graph is vertical
          stroke(100, 0, 50, 32); 
          line(0, yy, xx, yy);
        }
        if (checkboxShowY.isSelected()) {
          float xx = (dataRaw[n].y * magScale); //but thats because my graph is vertical
          stroke(0, 100, 50, 32); 
          line(0, yy, xx, yy);
        }
        if (checkboxShowZ.isSelected()) {
          float xx = (dataRaw[n].z * magScale); //but thats because my graph is vertical
          stroke(0, 0, 150, 32); 
          line(0, yy, xx, yy);
        }
      }
    }
  }

  //adjusted data
  if (adjRaw != null && adjRawSize > 1) {
    if (checkboxShowX.isSelected()) {
      stroke(255, 0, 0);
      //float yy0 = (adjPosRaw[0] * pixScale);
      float yy0 = (adjPosRaw[0]);
      float xx0 = (adjRaw[0].x * magScale);
      for (int n = 1; n<adjRawSize; n++) {

        //float yy = (adjPosRaw[n] * pixScale); //the x/y swap here is confusing
        float yy = (adjPosRaw[n]); //the x/y swap here is confusing
        float xx = (adjRaw[n].x * magScale); //but thats because my graph is vertical
        line(xx0, yy0, xx, yy);
        xx0=xx;
        yy0=yy;
      }
    }
    if (checkboxShowY.isSelected()) {
      stroke(0, 255, 0);
      //float yy0 = (adjPosRaw[0] * pixScale);
      float yy0 = (adjPosRaw[0]);
      float xx0 = (adjRaw[0].y * magScale);
      for (int n = 1; n<adjRawSize; n++) {

        //float yy = (adjPosRaw[n] * pixScale); //the x/y swap here is confusing
        float yy = (adjPosRaw[n]); //the x/y swap here is confusing
        float xx = (adjRaw[n].y * magScale); //but thats because my graph is vertical
        line(xx0, yy0, xx, yy);
        xx0=xx;
        yy0=yy;
      }
    }
    if (checkboxShowZ.isSelected()) {
      stroke(0, 0, 255);
      //float yy0 = (adjPosRaw[0] * pixScale);
      float yy0 = (adjPosRaw[0]);
      float xx0 = (adjRaw[0].z * magScale);
      for (int n = 1; n<adjRawSize; n++) {

        //float yy = (adjPosRaw[n] * pixScale); //the x/y swap here is confusing
        float yy = (adjPosRaw[n]); //the x/y swap here is confusing
        float xx = (adjRaw[n].z * magScale); //but thats because my graph is vertical
        line(xx0, yy0, xx, yy);
        xx0=xx;
        yy0=yy;
      }
    }
  }

  //delta function
  /*
  if (deltaArray != null) {
   if (deltaArray.length > 1) {
   float yy0 =(deltaArray[0].x * pixScale);
   float xx0 =(deltaArray[0].y * magScale * deltaScale / step); //large step gives large delta, so reduce scale for large step
   for (int n = 1; n<deltaArray.length; n++) {
   stroke(255, 0, 255, 50); 
   float yy = (deltaArray[n].x * pixScale); //the x/y swap here is confusing
   float xx = (deltaArray[n].y * magScale * deltaScale /step); //but thats because my graph is vertical
   tline(xx0, yy0, xx, yy);
   xx0=xx;
   yy0=yy;
   }
   }
   }
   */

  noFill();
  if (lo != null) {

    stroke(128, 255, 0, 128);
    //lo region
    //trect(loP.y * magScale, lo0.x * pixScale, lo1.y * magScale, lo1.x * pixScale);
    rect(loP.y * magScale, lo0.x, lo1.y * magScale, lo1.x);
    //hi region
    //trect(hiP.y * magScale, hi0.x * pixScale, hi1.y * magScale, hi1.x * pixScale);
    rect(hiP.y * magScale, hi0.x, hi1.y * magScale, hi1.x);

    stroke(255, 128, 0, 150);
    //length region
    //trect(lo.y * magScale, lo.x * pixScale, hi.y * magScale, hi.x * pixScale);
    rect(lo.y * magScale, lo.x, hi.y * magScale, hi.x);
    //tline(lo.y,lo.x,hi.y,lo.x);
    //tline(hi.y,hi.x,lo.y,hi.x);

    stroke(255, 0, 0, 64);
    line(loP.y, 0, loP.y, loP.x);
    line(hiP.y, 0, hiP.y, hiP.x);
  }

  if (zeros.size() > 0) {
    stroke(0, 0, 0, 64);
    for (int n = 0; n< zeros.size (); n++) {
      float zp =zeros.get(n); 
      line(-20, zp, 20, zp);
    }
  }
}

float offx=0, offy=0, scale=1;
public void tline(float fx, float fy, float tx, float ty) {
  fx = ((fx+ offx)* scale)  ;
  fy = ((fy + offy)* scale); 
  tx = ((tx+ offx)* scale);
  ty = ((ty + offy)* scale);

  line(x0 + fx, y0 - fy, x0 + tx, y0 - ty);
}
public void trect(float fx, float fy, float tx, float ty) {
  tx -= fx;
  ty -= fy;
  fx = ((fx+ offx)* scale)  ;
  fy = ((fy + offy)* scale); 
  tx = (tx* scale);
  ty = (ty* scale);

  rect(x0 + fx, y0 - fy, tx, -ty);
}

//manual find

float findPos;
PVector findVal = new PVector();


public void manualFind(){
  
  
  
  
  
}

//peak vectors
PVector lo0; //start of tolerance range
PVector hi0;

PVector lo1; //end of tolerance range
PVector hi1;

PVector hi; //middle of tolerance range
PVector lo;

PVector hiP; //peak reading
PVector loP;

FloatList zeros = new FloatList();

//relative tolerance abandonend
float P2Ptol =1;
int P2PtolMode = 0;

public void calcP2P() {
  int axis = dropListFindAxis.getSelectedIndex();

  lo0 = new PVector();
  hi0 = new PVector();

  lo1 = new PVector();
  hi1 = new PVector();

  hi = new PVector();
  lo = new PVector();
  hiP = new PVector();
  loP = new PVector();
  
  
  float val;
  float pos;
  
  
  
  
  
  //find peaks
  for (int n = 0; n<adjRawSize; n++) {
    val = getAxisVal(adjRaw[n],axis);
    pos = adjPosRaw[n];  
    //peak high
    if (val > hiP.y) {
      hiP.y = val;
      hiP.x = pos;
    }
    //peak low
    if (val < loP.y) {
      loP.y = val;
      loP.x = pos;
    }
  }

  float tolH = P2PtolMode==0?P2Ptol:hiP.y*P2Ptol/100;  //absolute tolerance/ %tolerance
  float tolL = P2PtolMode==0?P2Ptol:loP.y*P2Ptol/100;  //absolute tolerance/ %tolerance
  float tolZ = lerp(tolH,tolL,0.5f);  //use the average tolerance 

  zeros.clear();
  
  PVector zeroStart = new PVector();
  PVector zeroEnd = new PVector();
  boolean zeroStarted = false;
  
  
  //find zeros
  for (int n = 0; n<adjRawSize; n++) {
    val = getAxisVal(adjRaw[n],axis);
    pos = adjPosRaw[n];  
    if(zeroStarted){        //zero range started
      if(abs(val) > tolZ){//find end of range,
       if(signum(zeroStart.y) == signum(val)){  //check that we have crossed over
      
      zeroEnd.x = pos;
       zeroEnd.y = val;
      
      //now find the zero crossing of the line between start and end 
      //y=mx+c
      float dx = zeroEnd.x - zeroStart.x;  //change in x
      float dy = zeroEnd.y - zeroStart.y;  //change in y
      float m = dy/dx;                      //gradient
      float c = zeroEnd.y - zeroEnd.x*m;    //y value at x=0
      float x0 = -c/m;                      //x value at y=0
      
      zeros.append(x0);    //add this position to the list
      
       
         
         
       }
       //reset search variables when out of tol range, regardless of if a zero crossing was found
        //note that a double crossing with a peak less than tolZ will be missed     
       zeroStarted = false;  
      zeroStart = new PVector();
      zeroEnd = new PVector();
      }
    }else{
     if(abs(val) < tolZ){  //find start of range 
       zeroStart.x = pos;  //set the start values
       zeroStart.y = val;
       zeroStarted = true;
     }
    }
        
        
  
  }
  
  
  //find ranges
  for (int n = 0; n<adjRawSize; n++) {
    val = getAxisVal(adjRaw[n],axis);
    pos = adjPosRaw[n];
    //mprintln(val);
    //if a value is greater than the last greatest, it is the 1st greatest
    //if a value is equal to the greatest, it is the 2nd greatest
    //the average position of the 1st and 2nd greates is the ultimate greatest

    //high start
    if (val > hi0.y + tolH) {
      hi0.y = val;  //set the start val
      hi0.x = pos;  //set the start pos
      hi1 = hi0;    //drag the end vec along with the start
      
      //mprintln("Overriding last hi peak");
    }//high end
    else if (val >= hi0.y) {
      hi1.y = val;  //set the end val and pos
      hi1.x = pos;
      //mprintln("Extending last hi peak");
    }
    //low start
    if (val < lo0.y - tolL) {
      lo0.y = val;
      lo1 = lo0;
      //mprintln("Overriding last lo peak");
    }//low end
    else if (val <= lo0.y) {
      lo1.y = val;
      lo1.x = pos;
      //mprintln("Extending last lo peak");
    }
  }
  //set the mean peaks
  hi.y = hi0.y;  //using absolute peak val as mean peak val for convenience
  hi.x = (hi0.x + hi1.x)/2;  //mean position
  lo.y = lo0.y;
  lo.x = (lo0.x + lo1.x)/2;

  float distance = abs(hi.x - lo.x);

  mprintln();
  if(zeros.size()>0){
  print("Zeros found at: ");
  String zerosStr = "";
  for(int n = 0; n<zeros.size()-1; n++){
   zerosStr += strf(zeros.get(n),2) + ", ";  
  }
  zerosStr += strf(zeros.get(zeros.size()-1),2);
  mprintln();
  labelZero.setText(zerosStr);
  
  }else{
    labelZero.setText("No Zeros Found");
    mprintln("No Zeros Found");
  }
  mprintln("   Highest reading from: " +str(hi0.x) + "mm to " + str(hi1.x) +"mm");
  mprintln("    Lowest reading from: " +str(lo0.x) + "mm to " + str(lo1.x)+"mm");
  mprintln(" Average peak positions: " +str(lo.x) + "mm to "+str(hi.x)+"mm");
  mprintln("Length between points: " +str(distance)+"mm");
  labelP2PVal.setText(strf(distance,2));
  labelPHiPos.setText(strf(hi.x,2));
  labelPLoPos.setText(strf(lo.x,2));
  labelPHiVal.setText(strf(hi.y,2));
  labelPLoVal.setText(strf(lo.y,2));
}

public float getAxisVal(PVector vec, int axis){
 float val = 0;
  switch(axis){
  case 0:
  val = vec.x;
  break;
  case 1:
  val = vec.y;
  break;
  case 2:
  val = vec.z;
  break;
  
  
 } 
 return val; 
  
}
/* =========================================================
 * ====                   WARNING                        ===
 * =========================================================
 * The code in this tab has been generated from the GUI form
 * designer and care should be taken when editing this file.
 * Only add/edit code inside the event handlers i.e. only
 * use lines between the matching comment tags. e.g.

 void myBtnEvents(GButton button) { //_CODE_:button1:12356:
     // It is safe to enter your event code here  
 } //_CODE_:button1:12356:
 
 * Do not rename this tab!
 * =========================================================
 */

public void textfield_cli_change(GTextField source, GEvent event) { //_CODE_:textfield_cli:553760:
  //mprintln("textfield1 - GTextField event occured " + System.currentTimeMillis()%10000000 );
  if (event == GEvent.ENTERED) {
    sendCommand(source.getText());
    source.setText("");
  }
} //_CODE_:textfield_cli:553760:

public void buttonCal_click1(GButton source, GEvent event) { //_CODE_:buttonCal:449442:
  //mprintln("buttonCal - GButton event occured " + System.currentTimeMillis()%10000000 );
  if (mode == 0) {
    mode = 1;
    mprintln("Initiating calibration scan");
  }
  else {
    mprintln("Already doing something (MODE " + str(mode) + ")");
  }
} //_CODE_:buttonCal:449442:

public void buttonScan_click1(GButton source, GEvent event) { //_CODE_:buttonScan:243204:
  //mprintln("buttonScan - GButton event occured " + System.currentTimeMillis()%10000000 );
  if (mode == 0) {
    mode = 2;
    mprintln("Initiating data scan");
  }
  else {
    mprintln("Already doing something (MODE " + str(mode) + ")");
  }
} //_CODE_:buttonScan:243204:

public void textfieldRodPos_change1(GTextField source, GEvent event) { //_CODE_:textfieldRodPos:469881:
  //mprintln("textfieldRodPos - GTextField event occured " + System.currentTimeMillis()%10000000 );
  if (event == GEvent.ENTERED) {
    try {
      rodStart = Float.parseFloat(source.getText());  //parse the text

        
    }
    catch(NumberFormatException e) {
      mprintln("Please enter a vaild float value.");
      source.setText(str(rodStart));
    }
  }
} //_CODE_:textfieldRodPos:469881:

public void textfieldRodLen_change1(GTextField source, GEvent event) { //_CODE_:textfieldRodLen:543079:
  //mprintln("textfieldRodLen - GTextField event occured " + System.currentTimeMillis()%10000000 );
  if (event == GEvent.ENTERED) {
    try {
      rodLength = Float.parseFloat(source.getText());  //parse the text
     
    }
    catch(NumberFormatException e) {
      mprintln("Please enter a vaild float value.");
      source.setText(str(rodLength));
    }
  }
} //_CODE_:textfieldRodLen:543079:

public void textfieldStart_change1(GTextField source, GEvent event) { //_CODE_:textfieldStart:238489:
  //mprintln("textfieldStart - GTextField event occured " + System.currentTimeMillis()%10000000 );
  if (event == GEvent.ENTERED) {
    try {
      start = Float.parseFloat(source.getText());  //parse the text

        startStep = PApplet.parseInt(start * stepsPerMM);      //convert mm to steps
      start = PApplet.parseFloat(startStep) / stepsPerMM;  //convert back again for correct resolution
      source.setText(str(start));            //write back into the text field

      updateParams();
    }
    catch(NumberFormatException e) {
      mprintln("Please enter a vaild float value.");
    }
  }
} //_CODE_:textfieldStart:238489:

public void textfieldStop_change1(GTextField source, GEvent event) { //_CODE_:textfieldStop:726272:
  //mprintln("textfieldStop - GTextField event occured " + System.currentTimeMillis()%10000000 );
  if (event == GEvent.ENTERED) {
    try {
      stop = Float.parseFloat(source.getText());

      int stopSteps = PApplet.parseInt(stop * stepsPerMM);      //convert mm to steps
      stop = PApplet.parseFloat(stopSteps) / stepsPerMM;  //convert back again for correct resolution
      source.setText(str(stop));            //write back into the text field

      updateParams();
    }
    catch(NumberFormatException e) {
      mprintln("Please enter a vaild float value.");
    }
  }
} //_CODE_:textfieldStop:726272:

public void textfieldStep_change1(GTextField source, GEvent event) { //_CODE_:textfieldStep:795208:
  //mprintln("textfieldStep - GTextField event occured " + System.currentTimeMillis()%10000000 );
  if (event == GEvent.ENTERED) {
    try {
      step = Float.parseFloat(source.getText());
      steps = PApplet.parseInt(step * stepsPerMM);      //convert mm to steps
      step = PApplet.parseFloat(steps) / stepsPerMM;  //convert back again for correct resolution
      source.setText(str(step));            //write back into the text field

      updateParams();
    }
    catch(NumberFormatException e) {
      mprintln("Please enter a vaild float value.");
    }
  }
} //_CODE_:textfieldStep:795208:

public void buttonUp_click1(GButton source, GEvent event) { //_CODE_:buttonUp:870446:
  //mprintln("buttonUp - GButton event occured " + System.currentTimeMillis()%10000000 );
  sendCommand("MOVE8");
} //_CODE_:buttonUp:870446:

public void buttonDown_click1(GButton source, GEvent event) { //_CODE_:buttonDown:505650:
  //mprintln("buttonDown - GButton event occured " + System.currentTimeMillis()%10000000 );
  sendCommand("MOVE-8");
} //_CODE_:buttonDown:505650:

public void buttonZero_click1(GButton source, GEvent event) { //_CODE_:buttonZero:382134:
  //mprintln("buttonZero - GButton event occured " + System.currentTimeMillis()%10000000 );
  sendCommand("ZERO");
} //_CODE_:buttonZero:382134:

public void dropListPorts_click1(GDropList source, GEvent event) { //_CODE_:dropListPorts:451671:
  //mprintln("dropListPorts - GDropList event occured " + System.currentTimeMillis()%10000000 );
} //_CODE_:dropListPorts:451671:

public void buttonConnect_click1(GButton source, GEvent event) { //_CODE_:buttonConnect:538169:
  //mprintln("buttonConnect - GButton event occured " + System.currentTimeMillis()%10000000 );

  try {
    String newPortName = dropListPorts.getSelectedText();
    if (!portName.equals(newPortName) || port == null) {
      if (port != null) {
        port.stop();
      }
      port = new Serial(this, newPortName, 115200);
      portName = newPortName;
      mprintln("Connected to "+portName);

      //updateParams(); //we could read off the params from the reader, but for now, lets just update
      //this way we know the params are correct when the scan starts
      // we could do this at the start of a scan,
      //  but any change to the params will update the reader anyway, so long as the port is open

      //doesnt work... takes some time for chip to boot before it can accept
      //need to have some extra mode for sending or receiving params, but real low priority just now!
    }
  }
  catch(Exception e) {
    mprintln("Doh! An error occured while attempting to open com port " + dropListPorts.getSelectedText() + ".");
    e.printStackTrace();
  }
} //_CODE_:buttonConnect:538169:

public void buttonRefresh_click1(GButton source, GEvent event) { //_CODE_:buttonRefresh:358820:
  //mprintln("buttonRefresh - GButton event occured " + System.currentTimeMillis()%10000000 );

  dropListPorts.setItems(Serial.list(), min(dropListPorts.getSelectedIndex(), Serial.list().length));
} //_CODE_:buttonRefresh:358820:

public void buttonP2PA_click1(GButton source, GEvent event) { //_CODE_:buttonP2PA:898910:
  //mprintln("buttonP2PA - GButton event occured " + System.currentTimeMillis()%10000000 );
  calcP2P();
} //_CODE_:buttonP2PA:898910:

public void buttonSmooth_click1(GButton source, GEvent event) { //_CODE_:buttonSmooth:614937:
  //mprintln("buttonSmooth - GButton event occured " + System.currentTimeMillis()%10000000 );
  float smoothAmount = PApplet.parseFloat(dropListSWgt.getSelectedText());
  switch(dropListSTarg.getSelectedIndex()) {
  case 0: //calib
    smooth(calibRaw, calibRawSize, smoothAmount);
    calcAdj();
    break;
  case 1: //data
    smooth(dataRaw, dataRawSize, smoothAmount);
    calcAdj();
    break;

  case 2: //adj
    smooth(adjRaw, adjRawSize, smoothAmount);
    break;

  }
} //_CODE_:buttonSmooth:614937:

public void dropListSTarg_click1(GDropList source, GEvent event) { //_CODE_:dropListSTarg:512540:
  //mprintln("dropListSTarg - GDropList event occured " + System.currentTimeMillis()%10000000 );
} //_CODE_:dropListSTarg:512540:

public void dropListSWgt_click1(GDropList source, GEvent event) { //_CODE_:dropListSWgt:832616:
  //mprintln("dropListSWgt - GDropList event occured " + System.currentTimeMillis()%10000000 );

} //_CODE_:dropListSWgt:832616:

public void textfieldP2Ptol_change1(GTextField source, GEvent event) { //_CODE_:textfieldP2Ptol:397514:
  //mprintln("textfieldP2Ptol - GTextField event occured " + System.currentTimeMillis()%10000000 );
  if (event == GEvent.ENTERED) {
    try {
      P2Ptol = Float.parseFloat(source.getText());
    }
    catch(NumberFormatException e) {
      mprintln("Please enter a vaild float value.");
       source.setText(str(P2Ptol));            //write back into the text field
    }
  }
} //_CODE_:textfieldP2Ptol:397514:

public void textfieldRodDia_change1(GTextField source, GEvent event) { //_CODE_:textfieldRodDia:958789:
  //mprintln("textfieldRodDia - GTextField event occured " + System.currentTimeMillis()%10000000 );
  if (event == GEvent.ENTERED) {
    try {
      rodDia = Float.parseFloat(source.getText());
    }
    catch(NumberFormatException e) {
      mprintln("Please enter a vaild float value.");
       source.setText(str(rodDia));            //write back into the text field
    }
  }
} //_CODE_:textfieldRodDia:958789:

public void buttonSaveData_click1(GButton source, GEvent event) { //_CODE_:buttonSaveData:812962:
  //mprintln("buttonSaveData - GButton event occured " + System.currentTimeMillis()%10000000 );
  saveData();
} //_CODE_:buttonSaveData:812962:

public void dropListP2PtolMode_click1(GDropList source, GEvent event) { //_CODE_:dropListP2PtolMode:781944:
  //mprintln("dropListP2PtolMode - GDropList event occured " + System.currentTimeMillis()%10000000 );
  P2PtolMode = source.getSelectedIndex();
} //_CODE_:dropListP2PtolMode:781944:

public void buttonLoad_click1(GButton source, GEvent event) { //_CODE_:buttonLoad:935837:
  //mprintln("buttonSavePeak - GButton event occured " + System.currentTimeMillis()%10000000 );
  loadData();
} //_CODE_:buttonLoad:935837:

public void dropListSMacro_click1(GDropList source, GEvent event) { //_CODE_:dropListSMacro:711082:
  //mprintln("dropListSMacro - GDropList event occured " + System.currentTimeMillis()%10000000 );
} //_CODE_:dropListSMacro:711082:

public void checkboxShowRaw_clicked1(GCheckbox source, GEvent event) { //_CODE_:checkboxShowRaw:583576:
  //mprintln("checkboxShowRaw - GCheckbox event occured " + System.currentTimeMillis()%10000000 );
} //_CODE_:checkboxShowRaw:583576:

public void checkboxShowX_clicked1(GCheckbox source, GEvent event) { //_CODE_:checkboxShowX:719133:
  //mprintln("checkboxShowX - GCheckbox event occured " + System.currentTimeMillis()%10000000 );
} //_CODE_:checkboxShowX:719133:

public void checkboxShowY_clicked1(GCheckbox source, GEvent event) { //_CODE_:checkboxShowY:614547:
  //mprintln("checkboxShowY - GCheckbox event occured " + System.currentTimeMillis()%10000000 );
} //_CODE_:checkboxShowY:614547:

public void checkboxShowZ_clicked1(GCheckbox source, GEvent event) { //_CODE_:checkboxShowZ:332649:
  //mprintln("checkboxShowZ - GCheckbox event occured " + System.currentTimeMillis()%10000000 );
} //_CODE_:checkboxShowZ:332649:

public void checkboxSAuto_clicked1(GCheckbox source, GEvent event) { //_CODE_:checkboxSAuto:467985:
  //mprintln("checkboxSAuto - GCheckbox event occured " + System.currentTimeMillis()%10000000 );
} //_CODE_:checkboxSAuto:467985:

public void textfieldManPos_change1(GTextField source, GEvent event) { //_CODE_:textfieldManPos:384342:
  //mprintln("textfieldManPos - GTextField event occured " + System.currentTimeMillis()%10000000 );
  if (event == GEvent.ENTERED) {
    try {
      float pos = Float.parseFloat(source.getText());
      pickManA = true;
      pickPoint(pos);
    }
    catch(NumberFormatException e) {
      mprintln("Please enter a vaild float value.");
       source.setText(str(findPos));            //write back into the text field
    }
  }

} //_CODE_:textfieldManPos:384342:

public void dropListFindAxis_click1(GDropList source, GEvent event) { //_CODE_:dropListFindAxis:707293:
 // mprintln("dropListFindAxis - GDropList event occured " + System.currentTimeMillis()%10000000 );
} //_CODE_:dropListFindAxis:707293:

public void dropListSensorGain_click1(GDropList source, GEvent event) { //_CODE_:dropListSensorGain:397072:
  //mprintln("dropListSensorGain - GDropList event occured " + System.currentTimeMillis()%10000000 );
  //if(event == GEvent.SELECTION_CHANGED){
    mprintln("gain changed");
    sensorGain = source.getSelectedIndex();
  String command = "SENSORGAIN" + str(sensorGain);
  sendCommand(command);
  //}
} //_CODE_:dropListSensorGain:397072:

public void textfieldSensorDist_change1(GTextField source, GEvent event) { //_CODE_:textfieldSensorDist:702220:
  //mprintln("textfieldSensorDist - GTextField event occured " + System.currentTimeMillis()%10000000 );
  if (event == GEvent.ENTERED) {
    try {
      sensorDist = Float.parseFloat(source.getText());
      
    }
    catch(NumberFormatException e) {
      mprintln("Please enter a vaild float value.");
       source.setText(str(sensorDist));            //write back into the text field
    }
  }
} //_CODE_:textfieldSensorDist:702220:

public void textfieldManPos2_change1(GTextField source, GEvent event) { //_CODE_:textfieldManPos2:450880:
  //println("textfieldManPos2 - GTextField event occured " + System.currentTimeMillis()%10000000 );
  if (event == GEvent.ENTERED) {
    try {
      float pos = Float.parseFloat(source.getText());
      pickManA = false;
      pickPoint(pos);
    }
    catch(NumberFormatException e) {
      mprintln("Please enter a vaild float value.");
       source.setText(str(findPos));            //write back into the text field
    }
  }

} //_CODE_:textfieldManPos2:450880:

public void optionManA_clicked1(GOption source, GEvent event) { //_CODE_:optionManA:355398:
  //println("optionManA - GOption event occured " + System.currentTimeMillis()%10000000 );
  pickManA = true;
} //_CODE_:optionManA:355398:

public void optionManB_clicked1(GOption source, GEvent event) { //_CODE_:optionManB:993602:
  //println("optionManB - GOption event occured " + System.currentTimeMillis()%10000000 );
  pickManA = false;
} //_CODE_:optionManB:993602:

public void cs_magscale_change1(GCustomSlider source, GEvent event) { //_CODE_:cs_magscale:642061:
  //println("cs_magscale - GCustomSlider event occured " + System.currentTimeMillis()%10000000 );
  magScale = source.getValueF();
} //_CODE_:cs_magscale:642061:

public void textarea1_change1(GTextArea source, GEvent event) { //_CODE_:textarea1:974708:
  //println("textarea1 - GTextArea event occured " + System.currentTimeMillis()%10000000 );
} //_CODE_:textarea1:974708:



// Create all the GUI controls. 
// autogenerated do not edit
public void createGUI(){
  G4P.messagesEnabled(false);
  G4P.setGlobalColorScheme(GCScheme.PURPLE_SCHEME);
  G4P.setCursor(ARROW);
  if(frame != null)
    frame.setTitle("Simple Rod Reader");
  textfield_cli = new GTextField(this, 660, 500, 130, 20, G4P.SCROLLBARS_NONE);
  textfield_cli.setLocalColorScheme(GCScheme.BLUE_SCHEME);
  textfield_cli.setOpaque(true);
  textfield_cli.addEventHandler(this, "textfield_cli_change");
  buttonCal = new GButton(this, 660, 30, 130, 20);
  buttonCal.setText("Calibrate");
  buttonCal.addEventHandler(this, "buttonCal_click1");
  buttonScan = new GButton(this, 660, 50, 130, 20);
  buttonScan.setText("Scan");
  buttonScan.addEventHandler(this, "buttonScan_click1");
  textfieldRodPos = new GTextField(this, 740, 300, 50, 20, G4P.SCROLLBARS_NONE);
  textfieldRodPos.setText("50");
  textfieldRodPos.setOpaque(true);
  textfieldRodPos.addEventHandler(this, "textfieldRodPos_change1");
  labelRodPos = new GLabel(this, 660, 300, 80, 20);
  labelRodPos.setText("Rod Pos`n");
  labelRodPos.setOpaque(false);
  textfieldRodLen = new GTextField(this, 740, 320, 50, 20, G4P.SCROLLBARS_NONE);
  textfieldRodLen.setText("100");
  textfieldRodLen.setOpaque(true);
  textfieldRodLen.addEventHandler(this, "textfieldRodLen_change1");
  labelRodLen = new GLabel(this, 660, 320, 80, 20);
  labelRodLen.setText("Rod Length");
  labelRodLen.setOpaque(false);
  textfieldStart = new GTextField(this, 740, 350, 50, 20, G4P.SCROLLBARS_NONE);
  textfieldStart.setText("0");
  textfieldStart.setOpaque(true);
  textfieldStart.addEventHandler(this, "textfieldStart_change1");
  labelStart = new GLabel(this, 660, 350, 80, 20);
  labelStart.setText("Start Pos`n");
  labelStart.setOpaque(false);
  textfieldStop = new GTextField(this, 740, 370, 50, 20, G4P.SCROLLBARS_NONE);
  textfieldStop.setText("200");
  textfieldStop.setOpaque(true);
  textfieldStop.addEventHandler(this, "textfieldStop_change1");
  labelStop = new GLabel(this, 660, 370, 80, 20);
  labelStop.setText("Stop Pos`n");
  labelStop.setOpaque(false);
  textfieldStep = new GTextField(this, 740, 390, 50, 20, G4P.SCROLLBARS_NONE);
  textfieldStep.setText("5");
  textfieldStep.setOpaque(true);
  textfieldStep.addEventHandler(this, "textfieldStep_change1");
  labelStep = new GLabel(this, 660, 390, 80, 20);
  labelStep.setText("Step Size");
  labelStep.setOpaque(false);
  buttonUp = new GButton(this, 750, 430, 20, 30);
  buttonUp.setText("^");
  buttonUp.addEventHandler(this, "buttonUp_click1");
  buttonDown = new GButton(this, 750, 460, 20, 30);
  buttonDown.setText("v");
  buttonDown.addEventHandler(this, "buttonDown_click1");
  buttonZero = new GButton(this, 770, 430, 20, 60);
  buttonZero.setText("0");
  buttonZero.addEventHandler(this, "buttonZero_click1");
  dropListPorts = new GDropList(this, 660, 530, 130, 100, 5);
  dropListPorts.setItems(loadStrings("empty.txt"), 0);
  dropListPorts.addEventHandler(this, "dropListPorts_click1");
  buttonConnect = new GButton(this, 660, 550, 130, 20);
  buttonConnect.setText("Connect");
  buttonConnect.addEventHandler(this, "buttonConnect_click1");
  buttonRefresh = new GButton(this, 660, 570, 130, 20);
  buttonRefresh.setText("Refresh");
  buttonRefresh.addEventHandler(this, "buttonRefresh_click1");
  buttonP2PA = new GButton(this, 660, 160, 130, 20);
  buttonP2PA.setText("Find Peaks & Zeros");
  buttonP2PA.addEventHandler(this, "buttonP2PA_click1");
  buttonSmooth = new GButton(this, 660, 110, 130, 20);
  buttonSmooth.setText("Smooth");
  buttonSmooth.addEventHandler(this, "buttonSmooth_click1");
  dropListSTarg = new GDropList(this, 660, 130, 80, 100, 5);
  dropListSTarg.setItems(loadStrings("empty.txt"), 0);
  dropListSTarg.addEventHandler(this, "dropListSTarg_click1");
  dropListSWgt = new GDropList(this, 740, 130, 50, 200, 10);
  dropListSWgt.setItems(loadStrings("empty.txt"), 0);
  dropListSWgt.addEventHandler(this, "dropListSWgt_click1");
  labelP2PVal = new GLabel(this, 590, 180, 60, 20);
  labelP2PVal.setText("Length");
  labelP2PVal.setTextItalic();
  labelP2PVal.setOpaque(true);
  textfieldP2Ptol = new GTextField(this, 660, 180, 40, 20, G4P.SCROLLBARS_NONE);
  textfieldP2Ptol.setText("10");
  textfieldP2Ptol.setOpaque(true);
  textfieldP2Ptol.addEventHandler(this, "textfieldP2Ptol_change1");
  labelRodDia = new GLabel(this, 660, 280, 80, 20);
  labelRodDia.setText("Rod Dia.");
  labelRodDia.setOpaque(false);
  textfieldRodDia = new GTextField(this, 740, 280, 50, 20, G4P.SCROLLBARS_NONE);
  textfieldRodDia.setOpaque(true);
  textfieldRodDia.addEventHandler(this, "textfieldRodDia_change1");
  buttonSaveData = new GButton(this, 659, 231, 130, 20);
  buttonSaveData.setText("Save Data");
  buttonSaveData.addEventHandler(this, "buttonSaveData_click1");
  dropListP2PtolMode = new GDropList(this, 700, 180, 50, 40, 2);
  dropListP2PtolMode.setItems(loadStrings("empty.txt"), 0);
  dropListP2PtolMode.addEventHandler(this, "dropListP2PtolMode_click1");
  buttonLoad = new GButton(this, 660, 250, 130, 20);
  buttonLoad.setText("Load Data");
  buttonLoad.addEventHandler(this, "buttonLoad_click1");
  dropListSMacro = new GDropList(this, 740, 80, 50, 80, 4);
  dropListSMacro.setItems(loadStrings("empty.txt"), 0);
  dropListSMacro.addEventHandler(this, "dropListSMacro_click1");
  checkboxShowRaw = new GCheckbox(this, 590, 10, 60, 20);
  checkboxShowRaw.setText("Raw");
  checkboxShowRaw.setOpaque(false);
  checkboxShowRaw.addEventHandler(this, "checkboxShowRaw_clicked1");
  checkboxShowRaw.setSelected(true);
  checkboxShowX = new GCheckbox(this, 590, 30, 60, 20);
  checkboxShowX.setText("X");
  checkboxShowX.setOpaque(false);
  checkboxShowX.addEventHandler(this, "checkboxShowX_clicked1");
  checkboxShowX.setSelected(true);
  checkboxShowY = new GCheckbox(this, 590, 50, 60, 20);
  checkboxShowY.setText("Y");
  checkboxShowY.setOpaque(false);
  checkboxShowY.addEventHandler(this, "checkboxShowY_clicked1");
  checkboxShowY.setSelected(true);
  checkboxShowZ = new GCheckbox(this, 590, 70, 60, 20);
  checkboxShowZ.setText("Z");
  checkboxShowZ.setOpaque(false);
  checkboxShowZ.addEventHandler(this, "checkboxShowZ_clicked1");
  checkboxShowZ.setSelected(true);
  checkboxSAuto = new GCheckbox(this, 660, 80, 80, 20);
  checkboxSAuto.setTextAlign(GAlign.LEFT, GAlign.MIDDLE);
  checkboxSAuto.setText("AutoSmooth");
  checkboxSAuto.setOpaque(false);
  checkboxSAuto.addEventHandler(this, "checkboxSAuto_clicked1");
  checkboxSAuto.setSelected(true);
  labelPHiPos = new GLabel(this, 590, 100, 60, 20);
  labelPHiPos.setText("Hi Pos");
  labelPHiPos.setTextItalic();
  labelPHiPos.setOpaque(true);
  labelPLoPos = new GLabel(this, 590, 140, 60, 20);
  labelPLoPos.setText("Lo Pos");
  labelPLoPos.setTextItalic();
  labelPLoPos.setOpaque(true);
  labelZero = new GLabel(this, 590, 200, 200, 20);
  labelZero.setText("Zeros");
  labelZero.setTextItalic();
  labelZero.setOpaque(true);
  labelManual = new GLabel(this, 590, 230, 60, 20);
  labelManual.setText("Manual");
  labelManual.setOpaque(true);
  textfieldManPos = new GTextField(this, 590, 250, 60, 20, G4P.SCROLLBARS_NONE);
  textfieldManPos.setOpaque(true);
  textfieldManPos.addEventHandler(this, "textfieldManPos_change1");
  labelManX = new GLabel(this, 590, 270, 60, 20);
  labelManX.setText("X");
  labelManX.setTextItalic();
  labelManX.setOpaque(true);
  labelManY = new GLabel(this, 590, 290, 60, 20);
  labelManY.setText("Y");
  labelManY.setTextItalic();
  labelManY.setOpaque(true);
  labelManZ = new GLabel(this, 590, 310, 60, 20);
  labelManZ.setText("Z");
  labelManZ.setTextItalic();
  labelManZ.setOpaque(true);
  dropListFindAxis = new GDropList(this, 750, 180, 40, 60, 3);
  dropListFindAxis.setItems(loadStrings("empty.txt"), 0);
  dropListFindAxis.addEventHandler(this, "dropListFindAxis_click1");
  labelPHiVal = new GLabel(this, 590, 120, 60, 20);
  labelPHiVal.setText("Hi Val");
  labelPHiVal.setTextItalic();
  labelPHiVal.setOpaque(true);
  labelPLoVal = new GLabel(this, 590, 160, 60, 20);
  labelPLoVal.setText("Lo Val");
  labelPLoVal.setTextItalic();
  labelPLoVal.setOpaque(true);
  dropListSensorGain = new GDropList(this, 660, 10, 130, 160, 8);
  dropListSensorGain.setItems(loadStrings("empty.txt"), 0);
  dropListSensorGain.addEventHandler(this, "dropListSensorGain_click1");
  textfieldSensorDist = new GTextField(this, 660, 450, 60, 20, G4P.SCROLLBARS_NONE);
  textfieldSensorDist.setText("6.0");
  textfieldSensorDist.setOpaque(true);
  textfieldSensorDist.addEventHandler(this, "textfieldSensorDist_change1");
  labelSensorDist = new GLabel(this, 660, 430, 60, 20);
  labelSensorDist.setText("S. Dist.");
  labelSensorDist.setOpaque(false);
  textfieldManPos2 = new GTextField(this, 590, 340, 60, 20, G4P.SCROLLBARS_NONE);
  textfieldManPos2.setOpaque(true);
  textfieldManPos2.addEventHandler(this, "textfieldManPos2_change1");
  labelMan2X = new GLabel(this, 590, 360, 60, 20);
  labelMan2X.setText("X");
  labelMan2X.setOpaque(true);
  labelManPos2Y = new GLabel(this, 590, 380, 60, 20);
  labelManPos2Y.setText("Y");
  labelManPos2Y.setOpaque(true);
  labelManPos2Z = new GLabel(this, 590, 400, 60, 20);
  labelManPos2Z.setText("Z");
  labelManPos2Z.setOpaque(true);
  labelManDX = new GLabel(this, 590, 450, 60, 20);
  labelManDX.setText("dX");
  labelManDX.setOpaque(true);
  labelManDY = new GLabel(this, 590, 470, 60, 20);
  labelManDY.setText("dY");
  labelManDY.setOpaque(true);
  labelManDZ = new GLabel(this, 590, 490, 60, 20);
  labelManDZ.setText("dZ");
  labelManDZ.setOpaque(true);
  labelManDP = new GLabel(this, 590, 430, 60, 20);
  labelManDP.setText("dP");
  labelManDP.setOpaque(true);
  togGroup1 = new GToggleGroup();
  optionManA = new GOption(this, 590, 520, 60, 20);
  optionManA.setText("Man A");
  optionManA.setOpaque(false);
  optionManA.addEventHandler(this, "optionManA_clicked1");
  optionManB = new GOption(this, 590, 540, 60, 20);
  optionManB.setText("Man B");
  optionManB.setOpaque(false);
  optionManB.addEventHandler(this, "optionManB_clicked1");
  togGroup1.addControl(optionManA);
  optionManA.setSelected(true);
  togGroup1.addControl(optionManB);
  cs_magscale = new GCustomSlider(this, 660, 480, 60, 10, "grey_blue");
  cs_magscale.setLimits(0.5f, 0.0f, 1.0f);
  cs_magscale.setNumberFormat(G4P.DECIMAL, 2);
  cs_magscale.setOpaque(false);
  cs_magscale.addEventHandler(this, "cs_magscale_change1");
  textarea1 = new GTextArea(this, 10, 540, 570, 60, G4P.SCROLLBARS_VERTICAL_ONLY);
  textarea1.setOpaque(true);
  textarea1.addEventHandler(this, "textarea1_change1");
}

// Variable declarations 
// autogenerated do not edit
GTextField textfield_cli; 
GButton buttonCal; 
GButton buttonScan; 
GTextField textfieldRodPos; 
GLabel labelRodPos; 
GTextField textfieldRodLen; 
GLabel labelRodLen; 
GTextField textfieldStart; 
GLabel labelStart; 
GTextField textfieldStop; 
GLabel labelStop; 
GTextField textfieldStep; 
GLabel labelStep; 
GButton buttonUp; 
GButton buttonDown; 
GButton buttonZero; 
GDropList dropListPorts; 
GButton buttonConnect; 
GButton buttonRefresh; 
GButton buttonP2PA; 
GButton buttonSmooth; 
GDropList dropListSTarg; 
GDropList dropListSWgt; 
GLabel labelP2PVal; 
GTextField textfieldP2Ptol; 
GLabel labelRodDia; 
GTextField textfieldRodDia; 
GButton buttonSaveData; 
GDropList dropListP2PtolMode; 
GButton buttonLoad; 
GDropList dropListSMacro; 
GCheckbox checkboxShowRaw; 
GCheckbox checkboxShowX; 
GCheckbox checkboxShowY; 
GCheckbox checkboxShowZ; 
GCheckbox checkboxSAuto; 
GLabel labelPHiPos; 
GLabel labelPLoPos; 
GLabel labelZero; 
GLabel labelManual; 
GTextField textfieldManPos; 
GLabel labelManX; 
GLabel labelManY; 
GLabel labelManZ; 
GDropList dropListFindAxis; 
GLabel labelPHiVal; 
GLabel labelPLoVal; 
GDropList dropListSensorGain; 
GTextField textfieldSensorDist; 
GLabel labelSensorDist; 
GTextField textfieldManPos2; 
GLabel labelMan2X; 
GLabel labelManPos2Y; 
GLabel labelManPos2Z; 
GLabel labelManDX; 
GLabel labelManDY; 
GLabel labelManDZ; 
GLabel labelManDP; 
GToggleGroup togGroup1; 
GOption optionManA; 
GOption optionManB; 
GCustomSlider cs_magscale; 
GTextArea textarea1; 

float manAPos = 0;
float manBPos = 0;
float manDelta = 0;

boolean showManA = false;
boolean showManB = false;
boolean showManD = false;

boolean pickManA = true;


public void pickPoint(float pos){
  
  pos = PApplet.parseInt(pos * stepsPerMM);
  pos /= stepsPerMM;
  
  if(pickManA){
    showManA = true;
      manAPos = pos ;
      textfieldManPos.setText(Float.toString(pos));
    }else{
      showManB = true;
      manBPos = pos;
      textfieldManPos2.setText(Float.toString(pos));
    } 
    manDelta = manAPos - manBPos;
    labelManDP.setText( Float.toString(manDelta));
    pickManA = !pickManA;
    
  
}




boolean doLoad = false;
public void loadData(){
  
  inStrings = null;
  selectInput("Load CSV from:", "setInFile");
  
}

public void doLoad(){
  
 if(inStrings == null) return;
 if(inStrings.length < 15) return;

 int line = 0;
 String[] ss;
 do{
 if(!inStrings[line].equals(csvHeader[line])) {mprintln("fail line: " + str(line) + "; in: " +inStrings[line] + "; header: " + csvHeader[line]); break;}
 mprintln(inStrings[line++]);    //header
 mprintln(inStrings[line++]);  //file timestamp
                               //blank line
 if(!inStrings[++line].startsWith(csvHeader[line])) {mprintln("fail line: " + str(line) + "; in: " +inStrings[line] + "; header: " + csvHeader[line]); break;}
 mprintln(inStrings[line]);    //calib timestamp
 ss = inStrings[line].split(",");
 calibTStamp = ss[1];
 
 if(!inStrings[++line].startsWith(csvHeader[line])) {mprintln("fail line: " + str(line) + "; in: " +inStrings[line] + "; header: " + csvHeader[line]); break;}
 mprintln(inStrings[line]);    //data timestamp
 ss = inStrings[line].split(",");
 dataTStamp = ss[1];
 
 if(!inStrings[++line].startsWith(csvHeader[line])) {mprintln("fail line: " + str(line) + "; in: " +inStrings[line] + "; header: " + csvHeader[line]); break;}
 mprintln(inStrings[line]);    //calib gain
 ss = inStrings[line].split(",");
 calibSensorGain = PApplet.parseInt(ss[1]); 
 
 if(!inStrings[++line].startsWith(csvHeader[line])) {mprintln("fail line: " + str(line) + "; in: " +inStrings[line] + "; header: " + csvHeader[line]); break;}
 mprintln(inStrings[line]);    //data gain
 ss = inStrings[line].split(",");
 dataSensorGain = PApplet.parseInt(ss[1]); 
  
  if(!inStrings[++line].startsWith(csvHeader[line])) {mprintln("fail line: " + str(line) + "; in: " +inStrings[line] + "; header: " + csvHeader[line]); break;}
 mprintln(inStrings[line]);    //calib dist
 ss = inStrings[line].split(",");
 calibSensorDist = PApplet.parseInt(ss[1]); 
 
 if(!inStrings[++line].startsWith(csvHeader[line])) {mprintln("fail line: " + str(line) + "; in: " +inStrings[line] + "; header: " + csvHeader[line]); break;}
 mprintln(inStrings[line]);    //data dist
 ss = inStrings[line].split(",");
 dataSensorDist = PApplet.parseInt(ss[1]);
 
 line++;                        //blank line
 
 if(!inStrings[++line].startsWith(csvHeader[line])) {mprintln("fail line: " + str(line) + "; in: " +inStrings[line] + "; header: " + csvHeader[line]); break;}
 mprintln(inStrings[line++]);    // rod data header
 mprintln(inStrings[line]);      //rod data
 ss = inStrings[line].split(",");
 rodStart = PApplet.parseFloat(ss[0]);
 rodLength = PApplet.parseFloat(ss[1]);
 if(ss.length == 3) rodDia = PApplet.parseFloat(ss[2]);
 
 if(!inStrings[++line].startsWith(csvHeader[line])) {mprintln("fail line: " + str(line) + "; in: " +inStrings[line] + "; header: " + csvHeader[line]); break;}
 mprintln(inStrings[line++]);    // scan settings header
 mprintln(inStrings[line]);      //scan settings
 ss = inStrings[line].split(",");
 start = PApplet.parseFloat(ss[0]);
 stop = PApplet.parseFloat(ss[1]); 
 step = PApplet.parseFloat(ss[2]);
 
 points = 1 + PApplet.parseInt((stop - start) / step);
 
 line++;                         //blank line
 
 
 if(!inStrings[++line].startsWith(csvHeader[line])) {mprintln("fail line: " + str(line) + "; in: " +inStrings[line] + "; header: " + csvHeader[line]); break;}
 mprintln(inStrings[line++]);    // scan settings header
  
  int diff = inStrings.length - points;
  //int end = min( inStrings.length, line + points);
  mprintln("length, points, difference, line");
    mprintln(str(inStrings.length) + ", " + str(points) + ", " + str(diff) + ", " + str(line));
  
 if(diff != line){
  mprintln("data in file wrong length:"); 
    break;
   
 }
  calibPosRaw = new float[points];
  calibRaw = new PVector[points]; //reset the calib data
  calibRawSize = 0; //reset the position - should be equal to points when finished
  
  
  ss = inStrings[line].split(",");
  
  boolean allData = false;
  if(ss.length > 4){
    
   dataPosRaw = new float[points];
  dataRaw = new PVector[points]; //reset the calib data
  dataRawSize = 0; //reset the position - should be equal to points when finished
  
  adjPosRaw = new float[points];
  adjRaw = new PVector[points]; //reset the calib data
  adjRawSize = 0; //reset the position - should be equal to points when finished
   
   allData = true;
  }
  
  println(inStrings[15]);
  println(inStrings[16]);
  println(inStrings[17]);
  println(line);
  
 for(int n = 0; n < points; n++){
   //mprintln(inStrings[++line]);
   ss = inStrings[line].split(",");
   //println(line + "; " + n);
   
 
   
 
   calibPosRaw[n] = PApplet.parseFloat(ss[0]);
   
   float x = PApplet.parseFloat(ss[1]);
   float y = PApplet.parseFloat(ss[2]);
   float z = PApplet.parseFloat(ss[3]);
   
   calibRaw[n] = new PVector(x,y,z);
  
   calibRawSize++;
  
  if(allData){
    
    
   dataPosRaw[n] = calibPosRaw[n];
   adjPosRaw[n] = calibPosRaw[n];
   
   x = PApplet.parseFloat(ss[4]);
   y = PApplet.parseFloat(ss[5]);
   z = PApplet.parseFloat(ss[6]);
   dataRaw[n] = new PVector(x,y,z);
   
   x = PApplet.parseFloat(ss[7]);
   y = PApplet.parseFloat(ss[8]);
   z = PApplet.parseFloat(ss[9]);
   adjRaw[n] = new PVector(x,y,z);
   
   dataRawSize++;
   adjRawSize++;
   line++;
  }
 
 
 
 } 

  if(allData) {
   //move Data metadata to gui 
   sensorDist = dataSensorDist;
   sensorGain = dataSensorGain;
   
  } else{
    //move Calib metadata to gui
   sensorDist = calibSensorDist;
   sensorGain = calibSensorGain; 
    
  }
  
  refreshGui();
 }while(false);

 doLoad = false;

  
  
  
}

String[] inStrings;

public void setInFile(File selection) {
  if (selection == null) {
    mprintln("No input file selected");
  }
  else {
    mprintln("Selected " + selection.getAbsolutePath());
    inStrings = loadStrings(selection.getAbsolutePath());
    doLoad = true;
  }
}



//saves data 

StringList saveData;
public void saveData() {

  saveData = new StringList();
  int line = 0;
  String s = csvHeader[line++];
  saveData.append(s);                                          //0
  
  s = datetime();
  saveData.append(s);                                          //1
  line++;
  saveData.append("");                                         //2
  line++;
  s= csvHeader[line++] + calibTStamp;
  saveData.append(s);                                          //3
  s= csvHeader[line++] + dataTStamp;
  saveData.append(s);                                          //4
  s= csvHeader[line++] + calibSensorGain + ", " + gainLevels[calibSensorGain];
  saveData.append(s);                                          //5
  s= csvHeader[line++] + dataSensorGain + ", " + gainLevels[dataSensorGain];
  saveData.append(s);                                          //6
  s= csvHeader[line++] + calibSensorDist;
  saveData.append(s);                                          //7
  s= csvHeader[line++] + dataSensorDist;
  saveData.append(s);                                          //8
  
  s= csvHeader[line++];
  saveData.append(s);                                          //9
  s= csvHeader[line++];
  saveData.append(s);                                          //10
  s=str(rodStart)+", "+str(rodLength)+", "+str(rodDia);            //rod dia bolt-on
  saveData.append(s);                                          //11
  line++;
  s= csvHeader[line++];
  saveData.append(s);                                          //12
  s=str(start)+", "+str(stop)+", "+str(step);
  saveData.append(s);                                          //13
  line++;
  s= csvHeader[line++];
  saveData.append(s);                                          //14
  
  
  
  s= csvHeader[line++];
  saveData.append(s);
                                          //15

  for (int n = 0; n< calibRawSize; n++) {  //16 -> (16 + size)
    s = str(calibPosRaw[n]) + ", ";

    s += vec2csv(calibRaw[n]);

    if (dataRaw != null) {
      if(dataRawSize == calibRawSize){
      s += vec2csv(dataRaw[n]);
      s += vec2csv(adjRaw[n]);
      }
    }
    saveData.append(s);
  }
  
  savelist = saveData.array();
  selectOutput("Save CSV to:", "setOutFile");
}


String[] savelist;

public void setOutFile(File selection) {
  if (selection == null) {
    mprintln("No output file selected");
  }
  else {
    mprintln("Selected " + selection.getAbsolutePath());
    saveStrings(selection.getAbsolutePath(), savelist);
  }
}

public void smooth(PVector[] vecArray, int vecSize, float smoothAmount) {
  if (vecArray != null && vecSize >2) {
    PVector[] newArrayUp = new PVector[vecSize];
    PVector[] newArrayDn = new PVector[vecSize];


    PVector avg = new PVector();

    //running avg up
    for (int n = 0; n<vecSize; n++) {

      //avg.x = vecArray[n].x*smoothAmount + avg.x*(1-smoothAmount);
      
      //avg.y = vecArray[n].y*smoothAmount + avg.y*(1-smoothAmount);
      
      //avg.z = vecArray[n].z*smoothAmount + avg.z*(1-smoothAmount);
      
      avg = PVector.lerp(vecArray[n],avg,smoothAmount);
      
      newArrayUp[n] = new PVector();
      newArrayUp[n].set(avg);//.copy();
      
    }
    
    avg = new PVector();
    for (int n = vecSize-1; n>=0; n--) {

      //avg.x = vecArray[n].x*smoothAmount + avg.x*(1-smoothAmount);
      
      //avg.y = vecArray[n].y*smoothAmount + avg.y*(1-smoothAmount);
      
      //avg.z = vecArray[n].z*smoothAmount + avg.z*(1-smoothAmount);
      
      avg = PVector.lerp(vecArray[n],avg,smoothAmount);
      
      
      newArrayDn[n] = new PVector();
      newArrayDn[n].set(avg);//.copy();
    }

    for (int n = 0; n<vecSize; n++) {
      vecArray[n] = PVector.lerp(newArrayUp[n], newArrayDn[n], 0.5f);
    }
  }
}

public void sMacro(PVector[] target, int size) {
  switch(  dropListSMacro.getSelectedIndex()) {
  case 0: //low
    for (int n =0; n<3; n++) {
      smooth(target, size, 0.1f);
    }    
    break;
  case 1: //medium
    for (int n =0; n<5; n++) {
      smooth(target, size, 0.2f);
    }
    break;
  case 2: //high
    for (int n =0; n<7; n++) {
      smooth(target, size, 0.3f);
    }
    break;
  case 3: //obscene
    for (int n =0; n<10; n++) {
      smooth(target, size, 0.4f);
    }
    break;
  }
}

public float signum(float f) {
  if(f==0) return(0);
  return(f/abs(f));
}

public String datetime(){
  String s=(str(hour()) + ":" + str(minute())+", "+str(day()) +"/" +str(month())+"/"+str(year())); 
 return s; 
  
  
}


public String vec2csv(PVector vec){
  
String s= (str(vec.x)+", "+str(vec.y)+", "+str(vec.z)+", "); 
 return s; 
}

public String strf(float f, int dp){
  
 String s = str(f);
  int index = s.indexOf(".");
  if(index>0 && dp>0){
   index = min(index+dp,s.length()); 
    s = s.substring(0,+dp);   
  }
  
  
  return s;
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "RodReader2DGUI" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
