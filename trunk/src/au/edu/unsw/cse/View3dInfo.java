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
/** This class represents the device independant part of  a 3D view.
@author Tim Lambert
 */
import java.util.StringTokenizer;

public class View3dInfo{
  protected double wx,wy,wwidth,wheight; //the window on UV plane
  protected double dinverse;  // 1/distance of centre of projection from UV plane
  public Point3d u; // These three vectors are the 
  public Point3d v; // basis of the UVW coordinate
  public Point3d w; // system
 
  public View3dInfo() {
    this(true);
  }

  public View3dInfo(boolean persp) {
    setWindow(-1,-1,2,2);
    setPerspective(persp);
  }
	
  public View3dInfo(Point3d dirn, Point3d up) {
    this();
    setCamera(dirn,up);
  }

  public View3dInfo(Point3d dirn, Point3d up, double dinverse) {
    this(dirn,up);
    this.dinverse = dinverse;
  }
	
  public View3dInfo(Point3d dirn, Point3d up, boolean persp) {
    this(persp);
    setCamera(dirn,up);
  }

  public View3dInfo(Point3d dirn) {
    this(dirn,true);
  }
	
  public View3dInfo(Point3d dirn, boolean persp) {
    this(persp);
    setCamera(dirn);
  }
	
  public View3dInfo(Point3d dirn, Point3d up,double dinverse,
		    double wx,double wy,double wwidth,double wheight) {
    setWindow(wx,wy,wwidth,wheight);
    setCamera(dirn,up);
    this.dinverse = dinverse;
  }

  public void set(View3dInfo vi) {
    setWindow(vi.wx,vi.wy,vi.wwidth,vi.wheight);
    w = vi.w;
    v = vi.v;
    u = vi.u;
    dinverse = vi.dinverse;
  }

  /* is this projection orthographic*/
  public boolean orthographic(){
    return dinverse==0;
  }

  /* set projection to perspective (if arg is true) else orthographic */
  public void setPerspective(boolean persp){
    if (persp) {
      dinverse = 1/wwidth;
    } else {
      dinverse = 0;
    }
  }


  /** Calculate UVW coordinate system, given view direction, and up vector
   */
  public void setCamera(Point3d dirn, Point3d up){
    w = dirn.normalize();
    v = (up.subtract(w.scale(w.dot(up)))).normalize();
    u = v.cross(w);
  }
  
  /** Calculate UVW coordinate system, given view direction, using default up
    vector */
  public void setCamera(Point3d dirn){
    setCamera(dirn,new Point3d(0,1,0));
  }

  /** Adjust the position of the camera, given a displacement of the view
    position relative to U and V vectors adjustment is given in Device Independant coords
    */

  public void adjustCameraDI(double dx,double dy){
    setCamera(w.add(u.scale(dx).add(v.scale(dy))),v);
  }

  public void panDI(double dx,double dy){
    wx += dx*wwidth;
    wy += dy*wheight;
  }
    
  
  public void zoom(double scale){
    setWindow(wx-(scale-1)*wwidth/2,
	      wy-(scale-1)*wheight/2,
	      scale*wwidth,scale*wheight);
  }

  public void dolly(double scale){
    dinverse /= scale;
  }

  /** Set the window on the UV plane
   */
  public void setWindow(double wx,double wy,double wwidth,double wheight){
    this.wx = wx;
    this.wy = wy;
    this.wwidth = wwidth;
    this.wheight = wheight;
    if (!orthographic()){
      dinverse = 1/wwidth;
    }
  }

  /* Depth of a point in UVW system.
     Useful for doing a depth sort
     */
  public double depth(Point3d x){
    if (dinverse == 0){
      return w.dot(x);
    } else {
      return -w.scale(1/dinverse).subtract(x).length();
    }
  }

  public boolean frontFace(Point3d normal, Point3d p) {
    if (dinverse == 0) {
      return normal.dot(w)>0;
    } else {
      return normal.dot(w.scale(1/dinverse).subtract(p))>0;
    }
  }

  @Override
public String toString(){
    return(w+" "+v+" "+(float)dinverse)+" "+(float)wx+" "+(float)wy+" "+
      (float)wwidth+" "+(float)wheight;
  }

  public static View3dInfo fromString(String s) throws NumberFormatException{
    StringTokenizer st = new StringTokenizer(s," ");
    Point3d w =Point3d.fromString(st.nextToken());
    if(!st.hasMoreTokens()){
      return new View3dInfo(w);
    }
    Point3d v =Point3d.fromString(st.nextToken());
    if(!st.hasMoreTokens()){
      return new View3dInfo(w,v);
    }
    double dinverse = Double.valueOf(st.nextToken()).doubleValue();
    if(!st.hasMoreTokens()){
      return new View3dInfo(w,v,dinverse);
    }    
    double wx = Double.valueOf(st.nextToken()).doubleValue();
    double wy = Double.valueOf(st.nextToken()).doubleValue();
    double wwidth = Double.valueOf(st.nextToken()).doubleValue();
    double wheight = Double.valueOf(st.nextToken()).doubleValue();
    return new View3dInfo(w,v,dinverse,wx,wy,wwidth,wheight);
  }

  public String toVRML(String description){
    double d;
    if (dinverse==0.0){
      d = 100;
    } else {
      d = 1/dinverse;
    }
    return "Viewpoint{\n"+
      "orientation "+Quaternion.fromRotMatrix(u,v,w).conjugate().toVRML()+"\n"+
      "position "+w.scale(d).toVRML()+"\n"+
      "fieldOfView "+(float)(2*Math.atan(0.5*wwidth/d))+"\n"+
      "description \""+description+"\"\n"+
      "}\n";
  }
}