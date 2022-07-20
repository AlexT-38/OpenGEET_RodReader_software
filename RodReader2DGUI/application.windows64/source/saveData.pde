
//saves data 

StringList saveData;
void saveData() {

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

void setOutFile(File selection) {
  if (selection == null) {
    mprintln("No output file selected");
  }
  else {
    mprintln("Selected " + selection.getAbsolutePath());
    saveStrings(selection.getAbsolutePath(), savelist);
  }
}
