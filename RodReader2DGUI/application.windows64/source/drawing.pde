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
void drawDisplay() {




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

void drawManual() {
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

void drawBackground() {

  background(230);

  stroke(0);
  fill(255);
  rect(30, 30, 540, 540);
}

void drawMask() {
  stroke(0);
  fill(255);
  rect( 585, 5, 210, 590);
}

void drawGrid() {
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

void drawRod() {

  stroke(100);
  noFill();

  //trect(-pixScale*rodDia/2, pixScale*rodStart, pixScale*rodDia/2, pixScale*(rodStart+rodLength));
  rect(rodDia/-2, rodStart, rodDia, (rodLength));
}

void drawData() {
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
void tline(float fx, float fy, float tx, float ty) {
  fx = ((fx+ offx)* scale)  ;
  fy = ((fy + offy)* scale); 
  tx = ((tx+ offx)* scale);
  ty = ((ty + offy)* scale);

  line(x0 + fx, y0 - fy, x0 + tx, y0 - ty);
}
void trect(float fx, float fy, float tx, float ty) {
  tx -= fx;
  ty -= fy;
  fx = ((fx+ offx)* scale)  ;
  fy = ((fy + offy)* scale); 
  tx = (tx* scale);
  ty = (ty* scale);

  rect(x0 + fx, y0 - fy, tx, -ty);
}

