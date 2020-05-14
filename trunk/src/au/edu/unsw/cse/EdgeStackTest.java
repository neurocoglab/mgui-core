/*
* Copyright (C) 2020 Andrew Reid and the ModelGUI Project <http://www.modelgui.org>
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

public class EdgeStackTest {
  public static void main (String[] args) {
  	Point3d[] pts = new Point3d[10];
  	for(int i = 0; i<pts.length; i++) {
  		pts[i] = (Point3d.randomOnSphere());
  	}
	//GiftWrap g = new GiftWrap(pts);
	//Vector v = g.build();
	//Enumeration e = v.elements();
	//while (e.hasMoreElements()) {
	//	System.out.println((Triangle3d)e.nextElement());
	//}
}
}