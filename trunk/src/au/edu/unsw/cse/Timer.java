/*
* Copyright (C) 2014 Andrew Reid and the ModelGUI Project <http://mgui.wikidot.com>
* 
* This file is part of ModelGUI[core] (mgui-core).
* 
* ModelGUI[core] is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* ModelGUI[core] is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with ModelGUI[core]. If not, see <http://www.gnu.org/licenses/>.
*/

package au.edu.unsw.cse;
import java.util.Enumeration;
import java.util.Vector;

public class Timer {

int count =0;
long starttime;
String name;
long cumtime=0;

static Vector timers = new Vector();

public Timer(String s){
  name = s;
  timers.addElement(this);
}

public static void printall(){
  Enumeration e = timers.elements();
  while (e.hasMoreElements()) {
    System.out.println(e.nextElement());
  }
}

public void start() {
  starttime = System.currentTimeMillis();
}

public void stop() {
   cumtime += System.currentTimeMillis() - starttime;
   count++;
}

@Override
public String toString() {
  if (count > 0) {
    String ret = (name + " " + count + " " + cumtime / (double) count);
    count = 0;
    cumtime = 0;
    return ret;
  } else {
    return name;
  }
}
}