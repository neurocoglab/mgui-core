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
import java.awt.Color;
import java.io.IOException;

public class BezierToVRML {
  public static void main (String[] args) {
    int steps = 6;
    if(args.length > 0){
      try {
	steps = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
	System.err.println("Arguments must be an integer to use for no of steps.");
	System.exit(1);
      }
    } 
    NumberStream ns = new NumberStream(System.in);
    try {
      int nopts = (int) ns.next();
      Point3d[] pts = new Point3d[nopts];
      for (int i=0; i<nopts; i++){
	pts[i] = new Point3d(ns.next(),ns.next(),ns.next());
      }
      
      System.out.println("#VRML V2.0 utf8\nGroup {\nchildren [");
      Point3d[][] ctl = new Point3d[4][4];
      int nopatches = (int) ns.next();
      for (int i=0; i<nopatches; i++){
	for (int j=0;j<4;j++){
	  for (int k=0;k<4;k++){
	    ctl[j][k] = pts[(int) ns.next()];
	  }
	}
	Bezier3d b = new Bezier3d(ctl,Color.red,Color.green,steps);
	if (i!=0) {
	  System.out.println(",");
	}
	//b.toVRML(System.out);
      }
      System.out.println("]\n}");
    } catch (IOException e) {
      System.err.println("IOException " + e);
    }
  }
}