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
import java.util.Enumeration;
import java.util.Stack;
/** This class stores the edges that still need to be processed.
  It works like a regular stack except that putting AB on the stack when
  BA is already there causes both edges to be eliminated. 
  */
public class EdgeStack {
  private Stack data; // contents of the stack
  	
  public EdgeStack() {
    data = new Stack();
  }
  	
  public boolean isEmpty() {
    return data.isEmpty();
  }
  	
  public Edge3d get() {
    return (Edge3d) data.pop();
  }
  	
  public void put(Edge3d e) {
    data.push(e);
  }

  public void put(Point3d a, Point3d b) {
    put(new Edge3d(a,b));
  }
  	
  public void putp(Edge3d e) {
    int ind = data.indexOf(e);
    if (ind == -1) {
      data.push(e);
    } else {
      data.removeElementAt(ind);
    }
  }

  public void putp(Point3d a, Point3d b) {
    putp(new Edge3d(a,b));
  }
  	
  public void dump() {
    Enumeration e = data.elements();
    System.out.println(data.size());
    while (e.hasMoreElements()) {
      System.out.println(e.nextElement());
    }
    System.out.println();
  }

}
  			