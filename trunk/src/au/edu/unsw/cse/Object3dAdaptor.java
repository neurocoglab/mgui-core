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
/** Adaptor class for Object3d - provides default implementation for all the methods */
public class Object3dAdaptor implements Object3d{
  protected Point3d centre;

  static int normalColor; //colour for normal objects
  static int addColor; //colour for objects just added
  static int deleteColor; //colour for deleted objects
  static int selectColor; //colour for selected objects


  static int idCount; //Total no of these created
  private int id;
  
  public Object3dAdaptor(){
    id = idCount++;
  }

  //name of this object for VRML purposes
  public String id(){
    return "O"+id;
  }
  
  public void setCentre(Point3d c){
    centre = c;
  }
  public Point3d centre() {
    return centre;
  }
  protected int firstFrame=0;
  protected int lastFrame=Integer.MAX_VALUE;
  public void setFirstFrame(int f){
    firstFrame = f;
  }
  public int getFirstFrame(){
    return firstFrame;
  }
  public void setLastFrame(int f){
    lastFrame = f;
  }
  public int getLastFrame(){
    return lastFrame;
  }
  
  int selectFrameNo=-1; //frame no this was selected 

  /* set frameno that this was selected for processing */
  public void select(int n){
    selectFrameNo=n;
  }
  public int getSelectFrame(){
    return selectFrameNo;
  }

  public boolean visible(int frame){
    return firstFrame <= frame && frame <= lastFrame;
  }
  
  public double depthBias(View3d v){
    return 0.0;
  }

  public int getColorIndex(View3d v,int col) {
    return (firstFrame==v.getFrameNo()) ? addColor :
      (lastFrame==v.getFrameNo())  ? deleteColor: col;
  }


  public void render(View3d v){// do nothing
  }
  public void transform(Matrix3D T){ // do nothing
  }

  //PROTO for this object in VRML
  public String vrmlPROTO(){
    return
      "PROTO "+vrmlPROTOName()+"IN [\n"+
      vrmlPROTOINFields()+
      "] {\n"+
      "   Group {\n"+
      "    children[\n"+
      vrmlPROTOINBody()+
      "    ]}\n"+
      "}\n"+
      "PROTO "+vrmlPROTOName()+" [\n"+
      vrmlPROTOFields()+
      "] {\n"+
      vrmlPROTOBody()+
      "}\n";
  }

  public String vrmlPROTOName() {
    return "Object3d";
  }

  //fields for the inner prototype
  public String vrmlPROTOINFields() {
    return
      "  field SFNode material NULL\n"+
      vrmlPROTOExtraFields();
  }


  //body of the inner prototype
  public String vrmlPROTOINBody() {
    return "";
  }

  //extra fields to add - normally overidden by subclasses
  public String vrmlPROTOExtraFields() {
    return "";
  }


  public String vrmlPROTOFields() {
    return 
      "  eventIn SFInt32 set_whichChoice\n"+
      "  field SFNode normal NULL\n"+
      "  field SFNode added NULL\n"+
      "  field SFNode deleted NULL\n"+
      "  field SFNode selected NULL\n"+
      vrmlPROTOExtraFields();
  }

  //these are supposed to be overridden by children
  public String[] vrmlPROTOMaterials() {
    String[] result = {"material IS normal\n",
		       "material IS added\n",
		       "material IS deleted\n",
		       "material IS selected\n"};
    return result;
  }

  public String vrmlPROTOBody() {
    StringBuffer sb = new StringBuffer();
    String[] materials = vrmlPROTOMaterials();
    sb.append("Switch{\n");
    sb.append("set_whichChoice IS set_whichChoice\n");
    sb.append("choice[\n");
    for (int i = 0; i < materials.length; i++){
      sb.append(vrmlPROTOName()+"IN {\n");
      sb.append(materials[i]);
      sb.append("}\n");
    }
    sb.append("]}\n");
    return sb.toString();
  }

  public String toVRMLBody(VRMLState v){ // do nothing - subclasses will override
    return null;
  }

  public void toVRML(VRMLState v) {
    //NumberFormat nf =  NumberFormat.getInstance();
    //nf.setMaximumFractionDigits(3);
    
    String body = toVRMLBody(v);
    if (body!=null) {
      if (v.getPROTO(vrmlPROTOName()) == null) {
	v.putPROTO(vrmlPROTOName(),vrmlPROTO());
      }

      v.append("DEF "+id()+" "+vrmlPROTOName()+" {\n");
      v.append("normal USE "+v.idVRML(-1)+"\n");
      v.addAppear(this);
      v.append("added USE "+v.idVRML(addColor)+"\n");
      if (this.lastFrame < v.getLastFrame()) {
	v.addDisappear(this);
	v.append("deleted USE "+v.idVRML(deleteColor)+"\n");
      }
      if (this.selectFrameNo >= 0) {
	v.addSelect(this);
	v.append("selected USE "+v.idVRML(selectColor)+"\n");
      }
      v.append(body);
      v.append("}\n");
    }
  }
}