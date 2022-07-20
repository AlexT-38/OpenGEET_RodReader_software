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

        startStep = int(start * stepsPerMM);      //convert mm to steps
      start = float(startStep) / stepsPerMM;  //convert back again for correct resolution
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

      int stopSteps = int(stop * stepsPerMM);      //convert mm to steps
      stop = float(stopSteps) / stepsPerMM;  //convert back again for correct resolution
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
      steps = int(step * stepsPerMM);      //convert mm to steps
      step = float(steps) / stepsPerMM;  //convert back again for correct resolution
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
  float smoothAmount = float(dropListSWgt.getSelectedText());
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
  cs_magscale.setLimits(0.5, 0.0, 1.0);
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

