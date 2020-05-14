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
/** A Point3d that is also an Object 3d so that it can be rendered*/
public class Point3dObject3d extends Point3d implements Object3d{

  private Site3d site;

  public Point3dObject3d(Point3d p) {
    super();
    site = new Site3d(p);
    setCentre(p);
  }
    
  public Point3dObject3d(Point3d p, int frame) {
    this(p);
    setLastFrame(frame);
    setFirstFrame(frame);
  }
    
  //name of this object for VRML purposes
  public String id(){
    return site.id();
  }

  public void setCentre(Point3d c){
    this.v = c.v;
  }
  
  public Point3d centre() {
    return this;
  }
  

  public void setFirstFrame(int f){
    site.setFirstFrame(f);
  }
  
  public void setLastFrame(int f){
    site.setLastFrame(f);
  }

  /* set frameno that this point was selected for processing */
  public void select(int n){
    site.select(n);
  }
  public int getSelectFrame(){
    return site.getSelectFrame();
  }

  public int getFirstFrame(){
    return site.getFirstFrame();
  }
  public int getLastFrame(){
    return site.getLastFrame();
  }
  
  public boolean visible(int frame){
    return site.visible(frame);
  }
  
  public double depthBias(View3d v){
    return FRONTBIAS;
  }

  public void render(View3d v){
    site.render(v);
  }

  public void transform(Matrix3D T){ 
    T.transform(this);
  }

  public void toVRML(VRMLState state){
    site.toVRML(state);
  }
}