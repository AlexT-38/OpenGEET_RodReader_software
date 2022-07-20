float signum(float f) {
  if(f==0) return(0);
  return(f/abs(f));
}

String datetime(){
  String s=(str(hour()) + ":" + str(minute())+", "+str(day()) +"/" +str(month())+"/"+str(year())); 
 return s; 
  
  
}


String vec2csv(PVector vec){
  
String s= (str(vec.x)+", "+str(vec.y)+", "+str(vec.z)+", "); 
 return s; 
}

String strf(float f, int dp){
  
 String s = str(f);
  int index = s.indexOf(".");
  if(index>0 && dp>0){
   index = min(index+dp,s.length()); 
    s = s.substring(0,+dp);   
  }
  
  
  return s;
}
