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
  /** A coordinate axes object
   */

public class Axes extends Object3dAdaptor{
	
  public static int color;

  public Axes() {
    centre = Point3d.o;
  }

  @Override
public double depthBias(View3d v) {
    return 4*FRONTBIAS;
  }
      
  @Override
public void render (View3d v) {
    Color c = v.getColor(color);
    if (c != null) {
      v.g.setColor(c);
      v.drawString("x",Point3d.i);
      v.drawString("y",Point3d.j);
      v.drawString("z",Point3d.k);
      v.drawLine(Point3d.o,Point3d.i);
      v.drawLine(Point3d.o,Point3d.j);
      v.drawLine(Point3d.o,Point3d.k);
    }
  }

  @Override
public void toVRML(VRMLState v){
    int save=-1;
    if (color!=-1) {
      save = v.setDefaultColor(color);
    }
    v.append("Transform{\n"+
    "  translation 0 0.5 0\n"+
    "  children [\n"+
    "    DEF OneAxis Shape {\n"+
    "      appearance Appearance {\n"+
    "	material USE Axes\n"+
    "      }\n"+
    "      geometry  Cylinder {radius 0.01 height 1}\n"+
    "    }\n"+
    "  ]\n"+
    "}\n"+
    "Transform{\n"+
    "  translation 0 1 0\n"+
    "  children [\n"+
    "    Billboard{\n"+
    "      children [\n"+
    "	Shape {\n"+
    "	  appearance Appearance {\n"+
    "	    material USE Axes\n"+
    "	  }\n"+
    "	  geometry Text {	\n"+
    "	    string \"Y\"\n"+
    "	    fontStyle DEF AxisStyle FontStyle {\n"+
    "	      size 0.1\n"+
    "	      justify [\"MIDDLE\", \"END\"]\n"+
    "	    }\n"+
    "	  }\n"+
    "	}\n"+
    "      ]\n"+
    "    }\n"+
    "  ]\n"+
    "}\n"+
    "Transform{\n"+
    "  translation 0.5 0 0\n"+
    "  rotation 0 0 1 1.57\n"+
    "  children [\n"+
    "    USE OneAxis\n"+
    "  ]\n"+
    "}\n"+
    "Transform{\n"+
    "  translation 1 0 0\n"+
    "  children [\n"+
    "    Billboard{\n"+
    "      children [\n"+
    "	Shape {\n"+
    "	  appearance Appearance {\n"+
    "	    material USE Axes\n"+
    "	  }\n"+
    "	  geometry Text {	\n"+
    "	    string \"X\"\n"+
    "	    fontStyle USE AxisStyle\n"+
    "	  }\n"+
    "	}\n"+
    "      ]\n"+
    "    }\n"+
    "  ]\n"+
    "}\n"+
    "Transform{\n"+
    "  translation 0 0 0.5\n"+
    "  rotation 1 0 0 1.57\n"+
    "  children [\n"+
    "    USE OneAxis\n"+
    "  ]\n"+
    "}\n"+
    "Transform{\n"+
    "  translation 0 0 1\n"+
    "  children [\n"+
    "    Billboard{\n"+
    "      children [\n"+
    "	Shape {\n"+
    "	  appearance Appearance {\n"+
    "	    material USE Axes\n"+
    "	  }\n"+
    "	  geometry Text {	\n"+
    "	    string \"Z\"\n"+
    "	    fontStyle USE AxisStyle\n"+
    "	  }\n"+
    "	}\n"+
    "      ]\n"+
    "    }\n"+
    "  ]\n"+
    "}\n"+
    "\n");
    if (color!=-1) {
      v.setDefaultColor(save);
    }
  }
}