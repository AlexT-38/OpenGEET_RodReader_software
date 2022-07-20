//manual find

float findPos;
PVector findVal = new PVector();


void manualFind(){
  
  
  
  
  
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

void calcP2P() {
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
  float tolZ = lerp(tolH,tolL,0.5);  //use the average tolerance 

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

float getAxisVal(PVector vec, int axis){
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
