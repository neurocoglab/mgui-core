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
import java.util.Enumeration;
import java.util.Hashtable;
/** This class stores state information needed to convert an Object3d
 to VRML. */
public class VRMLState {
  private View3d view;
  private Viewer viewer;
  //Stores all prototypes required for all object3d
  private Hashtable vrmlPROTOs = new Hashtable();
  private StringBuffer sb = new StringBuffer();
  private Object3dList appear = new Object3dList(10);
  private Object3dList disappear = new Object3dList(10);
  private Object3dList select = new Object3dList(10);
  private StringBuffer suffix = new StringBuffer(); //stuff to add to end

  public VRMLState(Viewer viewer){
    this.viewer = viewer;
    view = viewer.getView();
  }

  public int getLastFrame() {
    return view.getLastFrame();
  }

  public Viewer getViewer() {
    return viewer;
  }

  //VRML identifier for a colour
  public String idVRML(int col){
    return viewer.idVRML(col);
  }

  public String getPROTO(String name){
    return (String) vrmlPROTOs.get(name);
  }

  public void putPROTO(String name, String body){
    vrmlPROTOs.put(name,body);
  }

  public void append(String s){
    sb.append(s);
  }

  public void appendToSuffix(String s){
    suffix.append(s);
  }

  /* add to list of objects that appear */
  public void addAppear(Object3d o){
    appear.addElement(o);
  }

  public Object3dList getAppear() {
    return appear;
  }

  public Object3dList getDisappear() {
    return disappear;
  }

  public Object3dList getSelect() {
    return select;
  }

  public void addDisappear(Object3d o){
    disappear.addElement(o);
  }

  public void addSelect(Object3d o){
    select.addElement(o);
  }

  public int setDefaultColor(int col) {
    return view.setDefaultColor(col);
  }

  public String getVRMLColor(int col){
    return view.getVRMLColor(col);
  }

  //Protypes of all objects defined in this State
  public String vrmlAllPROTOs(){
    StringBuffer s = new StringBuffer();
    for (Enumeration e = vrmlPROTOs.elements() ; e.hasMoreElements() ;) {
      s.append((String)e.nextElement());
    }
    return s.toString();
  }

  @Override
public String toString() {
    return vrmlAllPROTOs() + sb.toString() + suffix.toString();
  }

}