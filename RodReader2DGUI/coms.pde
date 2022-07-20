StringList serialBuffer = new StringList();
long timeouttime = 2000;

void findSerial() {

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
      port.write(0x55);
      port.write("\n");
      while(port.available() == 0 && timeout < timeouttime){
        Thread.sleep(50);
        timeout = millis() - starttime;
        mprint(".");
      }
      mprintln();
      
      
      String message = port.readStringUntil('\n');
      mprintln(message);
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


void readSerial() {

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


void updateParams() {




  points = 1 + int((stop - start) / step);            //the number of points is the difference between the two divided by the step size
  mprintln("Points: " +str(points));

  String[] commands = new String[3];

  commands[0] = "START" + str(startStep);  //send the data to the rod reader
  commands[1] = "POINTS" + str(points);  //send the data to the rod reader
  commands[2] = "STEPS" + str(steps);      

  if (!sendCommands(commands)) {
    //mprintln("Params will be sent when a com port is connected");
  }
}

boolean sendCommand(String command) {

  if (port != null) {

    port.write(command + "\n");
    return true;
  } else {
    mprintln("No com port open");
    return false;
  }
}
boolean sendCommands(String[] commands) {

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

