
void smooth(PVector[] vecArray, int vecSize, float smoothAmount) {
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
      vecArray[n] = PVector.lerp(newArrayUp[n], newArrayDn[n], 0.5);
    }
  }
}

void sMacro(PVector[] target, int size) {
  switch(  dropListSMacro.getSelectedIndex()) {
  case 0: //low
    for (int n =0; n<3; n++) {
      smooth(target, size, 0.1);
    }    
    break;
  case 1: //medium
    for (int n =0; n<5; n++) {
      smooth(target, size, 0.2);
    }
    break;
  case 2: //high
    for (int n =0; n<7; n++) {
      smooth(target, size, 0.3);
    }
    break;
  case 3: //obscene
    for (int n =0; n<10; n++) {
      smooth(target, size, 0.4);
    }
    break;
  }
}

