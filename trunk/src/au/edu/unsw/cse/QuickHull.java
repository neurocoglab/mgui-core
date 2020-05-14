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

import mgui.interfaces.ProgressUpdater;

public class QuickHull extends HullAlgorithm{

  public QuickHull(Point3dObject3d[] pts) {
    super(pts);
  }
	

  @Override
int[] extraColors() {
    int[] extra = new int[1];
    extra[0] = Object3dAdaptor.selectColor;
    return extra;
  }

  //find point with min x and make it pts[0]
  //find point with max x and make it pts[1]
  void findmaxmin() {
    for (int i = 0; i < pts.length; i++) {
      if (pts[i].x()>pts[0].x()){
	Point3dObject3d temp = pts[0];
	pts[0] = pts[i];
	pts[i] = temp;
      }
      if (pts[i].x()<pts[1].x()){
	Point3dObject3d temp = pts[1];
	pts[1] = pts[i];
	pts[i] = temp;
      }
    }
  }
	
    
  @Override
public Object3dList build () {
	  return build(null);
  }
  
  @Override
public Object3dList build (ProgressUpdater progress) {
	  
	//no known end point for this algorithm, so we reset it if necessary
    if (progress != null){
    	progress.setMinimum(0);
    	progress.setMaximum(pts.length);
    	}
	  
    Triangle3dPlus face1,face2; //first two faces created
    int frameNo = 1;
    EdgeStack es = new EdgeStack(); //used to find boundary of hole
    Object3dList faces = new Object3dList(20);
    findmaxmin();
    //make p[3] the furthest from p[0]p[1]
    HalfSpace h = new HalfSpace(pts[0],pts[1]);
    for (int i = 3; i < pts.length; i++) {
      if (h.normal.dot(pts[i])>h.normal.dot(pts[2])){
	Point3dObject3d temp = pts[2];
	pts[2] = pts[i];
	pts[i] = temp;
      }
    }      
    faces.addElement(face1 = new Triangle3dPlus(pts[0],pts[1],pts[2],frameNo++));
    faces.addElement(face2 = new Triangle3dPlus(pts[0],pts[2],pts[1],frameNo++));

    /* associate remaining points with one of these two faces */    
    for (int i = 3; i < pts.length; i++) {
      if (!face1.add(pts[i])) {
	face2.add(pts[i]);
      } 
    }
    
    int pos = 0;
    
    /* Each time around the main loop we process one face */
    for (int i = 0; i < faces.size(); i++){
      Object3dList ps = new Object3dList(20); //pts associated with deleted faces
      Object3d o = faces.elementAt(i);
      if (!(o instanceof Triangle3dPlus)) {
	continue;
      }
      Triangle3dPlus selected = (Triangle3dPlus)o;
      if (selected.lastFrame <= frameNo) {
	continue;
      }
      Point3dObject3d newp = selected.extreme();
      if (newp == null) continue;
      /* delete faces that this vertex can see*/
      for (int j = 0; j < faces.size(); j++){
      	o = faces.elementAt(j);
      	if (o instanceof Triangle3dPlus) {
	  Triangle3dPlus t = (Triangle3dPlus)o;
	  if (t.lastFrame>frameNo && t.inside(newp)) {
	    t.lastFrame = frameNo;
	    /* update boundary of hole */
	    es.putp(t.tri[0],t.tri[1]);
	    es.putp(t.tri[1],t.tri[2]);
	    es.putp(t.tri[2],t.tri[0]);
	    /*add the points associated with this face to ps */
	    ps.append(t.getPoints());
	  }
	}
      }
      selected.select(frameNo++);
      
      while (!es.isEmpty()){
	Edge3d e = es.get();
	Triangle3dPlus t =new Triangle3dPlus(e.start,e.end,newp,frameNo++);
	Object3dList ps2 = new Object3dList(ps.size());
	for (int j = ps.size() -1; j >= 0; j--){
	  Point3dObject3d p = (Point3dObject3d)ps.elementAt(j); 
	  if ((p!=newp) && !t.add(p)) {
	    ps2.addElement(p);
	  }
	}
	ps = ps2;
	faces.addElement(t);
	if (progress != null){
		if (progress.isCancelled()) return null;
  	  	pos++;
  	  	if (pos == pts.length) pos = 0;
  	  	progress.update(pos);
    	}
      }
    }
    faces.lastFrame = frameNo;
    return faces;
  }
 

  @Override
public Object3dList build2D() {
    Edge3dPlus face1,face2; 
    int frameNo = 1;
    Object3dList faces = new Object3dList(20);
    findmaxmin();
    
    faces.addElement(face1 = new Edge3dPlus(pts[0],pts[1],frameNo++));
    faces.addElement(face2 = new Edge3dPlus(pts[1],pts[0],frameNo++));

    /* associate remaining points with one of these two faces */    
    for (int i = 2; i < pts.length; i++) {
      if (!face1.add(pts[i])) {
	face2.add(pts[i]);
      } 
    }
    
    /* Each time around the main loop we process one face */
    for (int i = 0; i < faces.size(); i++){
      Object3d o = faces.elementAt(i);
      if (!(o instanceof Edge3dPlus)) {
	continue;
      }
      Edge3dPlus selected = (Edge3dPlus)o;
      if (selected.lastFrame <= frameNo) {
	continue;
      }
      Point3dObject3d newp = selected.extreme();
      if (newp==null){
	continue;
      }
      selected.lastFrame = frameNo;
      selected.select(frameNo++);

      faces.addElement(face1 = new Edge3dPlus(selected.start,newp,frameNo++));
      faces.addElement(face2 = new Edge3dPlus(newp,selected.end,frameNo++));
      for (int k=0; k<selected.pts.size(); k++){
	Point3dObject3d p = (Point3dObject3d)(selected.pts.elementAt(k));
	if (p != newp) {
	  if (!face1.add(p)) {
	    face2.add(p);
	  }
	}
      }
    }
    faces.lastFrame = frameNo;
    return faces;
  }
  
  
}

