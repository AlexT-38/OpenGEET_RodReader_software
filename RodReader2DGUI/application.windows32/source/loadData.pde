

boolean doLoad = false;
void loadData(){
  
  inStrings = null;
  selectInput("Load CSV from:", "setInFile");
  
}

void doLoad(){
  
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
 calibSensorGain = int(ss[1]); 
 
 if(!inStrings[++line].startsWith(csvHeader[line])) {mprintln("fail line: " + str(line) + "; in: " +inStrings[line] + "; header: " + csvHeader[line]); break;}
 mprintln(inStrings[line]);    //data gain
 ss = inStrings[line].split(",");
 dataSensorGain = int(ss[1]); 
  
  if(!inStrings[++line].startsWith(csvHeader[line])) {mprintln("fail line: " + str(line) + "; in: " +inStrings[line] + "; header: " + csvHeader[line]); break;}
 mprintln(inStrings[line]);    //calib dist
 ss = inStrings[line].split(",");
 calibSensorDist = int(ss[1]); 
 
 if(!inStrings[++line].startsWith(csvHeader[line])) {mprintln("fail line: " + str(line) + "; in: " +inStrings[line] + "; header: " + csvHeader[line]); break;}
 mprintln(inStrings[line]);    //data dist
 ss = inStrings[line].split(",");
 dataSensorDist = int(ss[1]);
 
 line++;                        //blank line
 
 if(!inStrings[++line].startsWith(csvHeader[line])) {mprintln("fail line: " + str(line) + "; in: " +inStrings[line] + "; header: " + csvHeader[line]); break;}
 mprintln(inStrings[line++]);    // rod data header
 mprintln(inStrings[line]);      //rod data
 ss = inStrings[line].split(",");
 rodStart = float(ss[0]);
 rodLength = float(ss[1]);
 if(ss.length == 3) rodDia = float(ss[2]);
 
 if(!inStrings[++line].startsWith(csvHeader[line])) {mprintln("fail line: " + str(line) + "; in: " +inStrings[line] + "; header: " + csvHeader[line]); break;}
 mprintln(inStrings[line++]);    // scan settings header
 mprintln(inStrings[line]);      //scan settings
 ss = inStrings[line].split(",");
 start = float(ss[0]);
 stop = float(ss[1]); 
 step = float(ss[2]);
 
 points = 1 + int((stop - start) / step);
 
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
   
 
   
 
   calibPosRaw[n] = float(ss[0]);
   
   float x = float(ss[1]);
   float y = float(ss[2]);
   float z = float(ss[3]);
   
   calibRaw[n] = new PVector(x,y,z);
  
   calibRawSize++;
  
  if(allData){
    
    
   dataPosRaw[n] = calibPosRaw[n];
   adjPosRaw[n] = calibPosRaw[n];
   
   x = float(ss[4]);
   y = float(ss[5]);
   z = float(ss[6]);
   dataRaw[n] = new PVector(x,y,z);
   
   x = float(ss[7]);
   y = float(ss[8]);
   z = float(ss[9]);
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

void setInFile(File selection) {
  if (selection == null) {
    mprintln("No input file selected");
  }
  else {
    mprintln("Selected " + selection.getAbsolutePath());
    inStrings = loadStrings(selection.getAbsolutePath());
    doLoad = true;
  }
}


