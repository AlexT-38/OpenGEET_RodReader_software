float manAPos = 0;
float manBPos = 0;
float manDelta = 0;

boolean showManA = false;
boolean showManB = false;
boolean showManD = false;

boolean pickManA = true;


void pickPoint(float pos){
  
  pos = int(pos * stepsPerMM);
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


