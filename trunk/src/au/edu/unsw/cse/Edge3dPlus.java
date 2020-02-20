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

public class Edge3dPlus extends Edge3d {
  /* the points associated with this. inside() is true for each point in here */
  Object3dList pts;
  int selectFrameNo=-1; //frame no this tri was selected 
  HalfSpace h;

  public Edge3dPlus(Point3d a, Point3d b, int frameNo){
    super	(a,b,frameNo);
    h = new HalfSpace(a,b);

    pts = new Object3dList(10);
  }
  
  /** try to add a point to association list.  Return true if succesful */
  public boolean add(Point3dObject3d p) {
    if (inside(p)) {
      pts.addElement(p);
      return true;
    } else {
      return false;
    }
  }
  
  /* set frameno that this tri wasselected for processing */
  @Override
public void select(int n){
    selectFrameNo=n;
    extreme().select(n);
  }
  
  /** return list of points associated with this triangle */
  public Object3dList getPoints() {
    return pts;
  }
  
  /** return point farthest from support plane of this triangl */
  public Point3dObject3d extreme() {
    Point3dObject3d res = null;
    double maxd = Double.MIN_VALUE;
    for (int i = 0; i < pts.size(); i++) {
      double d = h.normal.dot((Point3d)pts.elementAt(i));
      if ( d > maxd){
	res = (Point3dObject3d)pts.elementAt(i);
	maxd = d;
      }
    }
    return res;
  }
  
  
  /** Should we show the points associated with this triangle ? */
  boolean shouldShowPts(View3d v) {
    return firstFrame==v.getFrameNo() || selectFrameNo==v.getFrameNo();
	}
  
  /** render the triangle, given a 3D view */
  @Override
public void render(View3d v){
    if (shouldShowPts(v)) {
      super.render(v);
      pts.render(v);
    } else {
      super.render(v);
    }
  }
  
  @Override
public int getColorIndex(View3d v,int col) {
    return (selectFrameNo==v.getFrameNo()) ? selectColor : super.getColorIndex(v,col);
  }				
  
  
}