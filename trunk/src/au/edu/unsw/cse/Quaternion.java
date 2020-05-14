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
/** A quaternion.  Really handy for representing a 3D rotation.  */
public class Quaternion{
  private double scalar;  //scalar part
  private Point3d vector; //vector part
  

  private Quaternion(){
  }

  /** Create the quaternion ix+jy+kz+w */
  public Quaternion(double x, double y, double z, double w){
    scalar = w;
    vector = new Point3d(x,y,z);
  }

  /** Create a unit quaternion that represents the rotation about axis
    by theta */
  public Quaternion(Point3d axis, double theta){
    scalar = Math.cos(theta/2);
    vector = axis.normalize().scale(Math.sin(theta/2));
  }

  public Quaternion add(Quaternion x){
    Quaternion a = new Quaternion();
    a.scalar = scalar + x.scalar;
    a.vector = vector.add(x.vector);
    return a;
  }

  public Quaternion multiply(Quaternion x){
    Quaternion a = new Quaternion();
    a.scalar = scalar * x.scalar - vector.dot(x.vector);
    a.vector = vector.cross(x.vector).add(x.vector.scale(scalar)).add(vector.scale(x.scalar));
    return a;
  }
  
  public Quaternion scale(double x){
    Quaternion a = new Quaternion();
    a.scalar = x * scalar;
    a.vector = vector.scale(x);
    return a;
  }

  public Quaternion conjugate(){
    Quaternion a = new Quaternion();
    a.scalar = scalar;
    a.vector = vector.scale(-1);
    return a;
  }
    
  public double norm(){
    return scalar*scalar + vector.dot(vector);
  }

  public Quaternion inverse(){
    return conjugate().scale(1/norm());
  }

  /** construct Quaternion from a rotation matrix expressed as a triple
    of vectors, each one a row of the matrix.
    Code adapted from Shoemake's paper "Quaternions".
    */
  public static Quaternion fromRotMatrix(Point3d u,Point3d v,Point3d w){
    double tr,s,sinv;
    tr = u.x() + v.y() + w.z();
    if (tr >= 0.0) {
      s = Math.sqrt(tr + 1);
      sinv = 0.5 / s;
      return new Quaternion((w.y()-v.z())*sinv,
			    (u.z()-w.x())*sinv,
			    (v.x()-u.y())*sinv,
			    s*0.5);
    } else if (u.x() > v.y() && u.x() > w.z()) {
      s = Math.sqrt (u.x() - (v.y()+w.z()) + 1);
      sinv = 0.5 / s;
      return new Quaternion(s*0.5,
			    (u.y()+v.x())*sinv,
			    (w.x()+u.z())*sinv,
			    (w.y()-v.z())*sinv);
    } else if (v.y() > w.z()) {
      s = Math.sqrt (v.y() - (w.z()+u.x()) + 1);
      sinv = 0.5 / s;
      return new Quaternion((u.y()+v.x())*sinv,
			    s*0.5,
			    (v.z()+w.y())*sinv,
			    (u.z()-w.x())*sinv);
    } else {
      s = Math.sqrt (w.z() - (u.x()+v.y()) + 1);
      sinv = 0.5 / s;
      return new Quaternion((w.x()+u.z())*sinv,
			    (v.z()+w.y())*sinv,
			    s*0.5,
			    (v.x()-u.y())*sinv);
    }
  }

  @Override
public String toString(){
    return (float)vector.x()+"i+"+(float)vector.y()+"j+"+
      (float)vector.z()+"k+"+(float)scalar;
  }

  /**Convert to VRML representation: axis + rotation angle*/
  public String toVRML(){
    return (float)vector.x()+" "+(float)vector.y()+" "+
      (float)vector.z()+" "+(float)(2*Math.acos(scalar/norm()));    
  }


  public static void main(String[] arg){
    Quaternion a,b;
    a = new Quaternion(1,2,3,4);
    b = new Quaternion(1,1,0,3);
    System.out.println(a.multiply(b));
    System.out.println(a.multiply(b).multiply(b.inverse()));
    View3dInfo v = new View3dInfo(Point3d.ijk,Point3d.j);
    System.out.println(v.toVRML("new"));
    v = new View3dInfo(Point3d.ijk,Point3d.j,false);
    System.out.println(v.toVRML("new2"));
  }
}