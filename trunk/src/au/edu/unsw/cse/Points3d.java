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
import java.awt.Color;
import java.awt.Point;
import java.util.Vector;


public class Points3d extends Object3dAdaptor{
  
  public static int color;
  protected Point3dObject3d[] pts;
  protected Vector labels;
  private Object3dList model; //display this one at frame zero
  
  public Points3d() {
    centre = Point3d.o;
    pts = null;
    labels = null;
  }
  
  @Override
public double depthBias(View3d v) {
    return 4*FRONTBIAS;
  }
  
  public Points3d set(Point3dObject3d[] pts){
    this.pts = pts;
    labels = null;
    model = new Object3dList(pts.length);
    for (int i=0; i <pts.length; i++) {
      model.addElement(pts[i]);
    }
    model.lastFrame = 0;
    return this;
  }

  public void setLabels(Vector labels){
    this.labels = labels;
  }
  
  @Override
public void render (View3d v) {
    if (v.getFrameNo()==0 && pts != null) {
      model.render(v);
    } else {
      Color c = v.getColor(color);
      if (c != null && pts != null) {
	v.g.setColor(c);
	for (int i = 0; i < pts.length; i++) {
	  Point s = v.toPoint(pts[i]);
	  v.g.drawRect(s.x-1,s.y-1,3,3);
	}
      }
      if (labels != null){
	v.g.setColor(Color.black);
	StringBuffer sb = new StringBuffer(" ");
	for (int j = 0; j < labels.size(); j++) {
	  int i = ((Integer) labels.elementAt(j)).intValue();
	  if (i>=0&&i<pts.length){
	    sb.setCharAt(0,(char)('A'+j));
	    v.drawStringBelow(sb.toString(),pts[i]);
	  }
	}
      }
    }
  }

  @Override
public void toVRML(VRMLState v){
    int save=-1;
    if (color!=-1) {
      save = v.setDefaultColor(color);
    }
    for (int i = pts.length-1; i>= 0; i--){
      pts[i].toVRML(v);
    }
    if (color!=-1) {
      v.setDefaultColor(save);
    }
  }
}