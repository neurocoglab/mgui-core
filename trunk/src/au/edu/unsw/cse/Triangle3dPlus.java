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
/** A 3d triangle with a list of associated points */

public class Triangle3dPlus extends Triangle3d {
  /* the points associated with this. inside() is true for each point in here */

  Object3dList pts;

  public Triangle3dPlus(Point3d a, Point3d b, Point3d c, int frameNo){
    super(a,b,c,frameNo);
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
    
  /* set frameno that this tri was selected for processing */
  @Override
public void select(int n){
    super.select(n);
    extreme().select(n);
  }
    
  /** return list of points associated with this triangle */
  public Object3dList getPoints() {
    return pts;
  }

  /** return point farthest from support plane of this triangle */
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
      if (v.frontFace(h.normal,tri[0])) {
	super.render(v);
	pts.render(v);
      } else {
	pts.render(v);
	super.render(v);
      }
    } else {
      super.render(v);
    }
  }

  @Override
public int getColorIndex(View3d v,int col) {
    return (selectFrameNo==v.getFrameNo()) ? selectColor : super.getColorIndex(v,col);
  }				

  /* hack hack hack */
  @Override
public double depthBias(View3d v) {
    if (shouldShowPts(v)) {
      return 2*super.depthBias(v);
    } else {
      return super.depthBias(v);
    }
  }


  @Override
public String vrmlPROTOName() {
    return "Triangle3dPlus";
  }

  @Override
public String vrmlPROTOExtraFields() {
    return 
      super.vrmlPROTOExtraFields()+
      "  field MFVec3f points [] \n"+
      "  field MFInt32 lines []\n";
  }

  @Override
public String[] vrmlPROTOMaterials() {
    String[] result = super.vrmlPROTOMaterials();
    for (int i=0; i < result.length; i++){
      result[i] += "points IS points\nlines IS lines\n";
    }
    return result;
  }

  @Override
public String vrmlPROTOINBody() {
    return
      super.vrmlPROTOINBody()+
      " Shape {\n"+
      "	appearance Appearance {\n"+
      "	  material Material {\n"+
      "	    emissiveColor 1 1 1\n"+
      "	  }\n"+
      "	}\n"+
      "	geometry IndexedLineSet {\n"+
      "  coord Coordinate {\n"+
      "    point IS points\n"+
      "      }\n"+
      "  coordIndex IS lines\n"+
      "  }}\n";
  }


  @Override
public String toVRMLBody(VRMLState v) {
    StringBuffer s = new StringBuffer();
    s.append(super.toVRMLBody(v));
    if (pts.size()>0){
      s.append("points [\n");
      s.append(centre.toVRML()+",\n");
      for (int i = 0; i < pts.size(); i++){
	s.append(((Point3d)pts.elementAt(i)).toVRML()+",\n");
      }
      s.append("]\n");
      s.append("lines [\n");
      for (int i = 1; i <= pts.size(); i++){
	s.append("0,"+i+",-1,\n");
      }
      s.append("]\n");
    }
    return s.toString();
  }


}