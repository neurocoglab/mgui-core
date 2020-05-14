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
import java.util.Vector;
/** This class stores the points that still need to be processed.
  It is not really a stack at all, but its use in 2d is analogous to the use
  of EdgeStack in 3d
  */
public class PointStack {
  private Vector starts; // unmatched start points
  private Vector ends; // unmatched end points
  	
  public PointStack() {
    starts = new Vector();
    ends = new Vector();
  }
  	
  public boolean isEmpty(){
    return starts.isEmpty();
  }

  public void put(Point3d start, Point3d end) {
    if (!ends.removeElement(start)){
      starts.addElement(start);
    }
    if (start==end || !starts.removeElement(end)){
      ends.addElement(end);
    }
  }

  public Point3d getStart(){
    Point3d p = (Point3d)starts.lastElement();
    starts.removeAllElements();
    return p;
  }

  public Point3d getEnd(){
    Point3d p = (Point3d)ends.lastElement();
    ends.removeAllElements();
    return p;
  }



}
  			