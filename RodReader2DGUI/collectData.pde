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
void doCalib() {
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
    /* 2022: 'SCAN' from emulator is missing here. Adding some debug reporting to find out why */
    /* ok, so .size() is reporting '4' but .get(0) returns an empty string? 
        yes, size is the number of lines received
        so if I'm getting empty strings I should just drop them until I run out of strings
        or one of them is not empty */
      String buffer = serialBuffer.get(0);
      
      while (buffer.length() == 0 && serialBuffer.size() > 2) {
        mprintln("empty buffer");
        serialBuffer.remove(0);    //remove the line from the buffer
        buffer = serialBuffer.get(0);
      }
      /* final length check */
      if (buffer.length() > 0)
      {
        if (buffer.equals("SCAN")) {  //check that the arduino has started the command
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
          mprint("Didn't get 'SCAN': '");
          mprint(buffer);    //print the received buffer
          mprintln("'");
          mprintln(serialBuffer.size());
          
          calibState = 0;      //got a wrong response to the command, reset the mode
          mode = 0;
        }
      } /* end final length check */
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
            calibPosRaw[calibRawSize] = float(sub1[0])/stepsPerMM;
              String[] sub2 = split(sub1[1], ',');
            if (sub2.length == 3) {
              PVector vec = new PVector( float(sub2[0]), float(sub2[1]), float(sub2[2]));
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
void doScan() {
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
            dataPosRaw[dataRawSize] = float(sub1[0])/stepsPerMM;
            String[] sub2 = split(sub1[1], ',');
            if (sub2.length == 3) {
              PVector vec = new PVector(float(sub2[0]), float(sub2[1]), float(sub2[2]));
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
void calcAdj() {
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




