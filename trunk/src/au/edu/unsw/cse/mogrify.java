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
import java.io.IOException;
import java.io.PrintStream;

class mogrify {
  public static void main (String[] args) {
    NumberStream ns = new NumberStream(System.in);
    PrintStream o = System.out;
    double t;
    try {
      int nopts = (int) ns.next();
      o.println(nopts);
      for (int i=0; i<nopts; i++){
	ns.next();
	o.println(ns.next()+" "+ns.next()+" "+ns.next());
      }
      int nopatches = (int) ns.next();
      o.println(nopatches);
      for (int i=0; i<nopatches; i++){
	for (int j=0;j<16;j++){
	  int k = Math.abs((int) ns.next());
	  o.print(k-1+" ");
	}
	o.println();
      }
    } catch (IOException e) {
      System.err.println("IOException " + e);
    }
  }
}