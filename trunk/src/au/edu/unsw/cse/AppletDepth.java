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
import java.awt.Choice;
import java.awt.Color;
import java.awt.Event;

public
class AppletDepth extends Applet3d{
    static double[][][] tridata = { // initial positions of tris
    {{-2,-2,-1},{2,1,1},{-2,-1,-1}},
    {{1,-2,-1},{2,-1,-1},{-2,2,1}},
    {{-1,2,-1},{0,2,-1},{-1,-2,1}}
  };
  static Color[] tricol = {Color.red,Color.green,Color.blue};

  static double[][][] cutdata = { // initial positions of tris
    {{0,-0.5,0},{2,1,1},{0,0,0}},
    {{-2,-2,-1},{0,-0.5,0},{0,0,0},{-2,-1,-1}},
    {{-0.5,0,0},{0,0.5,0},{-2,2,1}},
    {{1,-2,-1},{2,-1,-1},{0,0.5,0},{-0.5,0,0}},
    {{-1,0,0},{-0.5,0,0},{-1,-2,1}},
    {{-1,2,-1},{0,2,-1},{-0.5,0,0},{-1,0,0}}
  };
  static Color[] cutcol = {Color.red,Color.red,Color.green,Color.green,Color.blue,Color.blue};


  private Object3dList polygons(double[][][] data, Color[] col) {
    Object3dList model = new Object3dList(data.length);
    for (int i=0; i < data.length; i++){
      Point3d[] tri = new Point3d[data[i].length];
      for (int j=0; j < data[i].length; j++){
	tri[j] = new Point3d(data[i][j][0],data[i][j][1],data[i][j][2]);
      }
      Polygon3d t = new Polygon3d(tri,col[i]);
      model.addElement(t);
    }
    return model;
  }    

  @Override
public boolean mouseEnter(Event e, int x, int y){
    showStatus("Hold the mouse button down and move the mouse to rotate shapes");
    return true;
  }

  @Override
public Choice createModelChoice() {
    Choice modelChoice = new Choice();
    modelChoice.addItem("Logo");
    modelChoice.addItem("Triangles");
    modelChoice.addItem("Cut Triangles");
    return modelChoice;
  }

  /** override this method if we want a choice of models */
  @Override
public Object3dList selectModel(String choice) {
    if (choice.equals("Logo")) {
      return new CSELogo();
    } else if (choice.equals("Triangles")) {
      return polygons(tridata,tricol);
    } else if (choice.equals("Cut Triangles")) {
      return polygons(cutdata,cutcol);
    }
    return null;
  }

  /** defaultModel to display */
  @Override
public Object3dList defaultModel() {
      return new CSELogo();
  }

    // Return information suitable for display in an About dialog box.
    @Override
	public String getAppletInfo() {
        return "3D Spline Patch Applet.\nWritten by Tim Lambert.";
    }
 
}