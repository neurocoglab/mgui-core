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
/**
 * This class is a checkbox that lets user control visibility of different coloured triangles
 */
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Event;

public class ColorControl extends Checkbox{
	
  protected String id; //name of this for VRML
  protected String tip; //tip to be shown when cursor is over this button
  protected Viewer parent; //parent ViewControl so we can notify it when box is checked
  protected Color c; //colour we are controlling
  protected int i; //index into colour table of View3d object that contains c (or null if the colour
  // has been made invisible
	                   
  public ColorControl(String label, String tip, boolean state, Color c, int i, Viewer parent) {
    super(label);

    // id is label with white space removed
    StringBuffer sb= new StringBuffer(label.length());
    for (int j = 0; j < label.length(); j++){
      if (!Character.isSpace(label.charAt(j))) {
	sb.append(label.charAt(j));
      }
    }
    id = sb.toString();

    if (c != Color.black) { 
      setBackground(c);
    }

    this.tip = tip;
    this.c = c;
    this.i = i;
    this.parent = parent;
    setState(state);
    action(null,null);  
  }
  
  @Override
public boolean action(Event e, Object arg){
    if (getState()) {
      parent.put(i,c);
    } else {
      parent.put(i,null);
    }
    return true;
  }
  
  public String idVRML() {
    return id;
  }

  public String vrmlPROTO(){
    return "DEF "+idVRML()+" Material {\n"+
      "    diffuseColor "+
      ((c.getRed()==0&&c.getGreen()==0&&c.getBlue()==0) ?
      "1 1 1":
        (c.getRed()/255.0)+" "+
	(c.getGreen()/255.0)+" "+
	(c.getBlue()/255.0))+"\n"+
      "}\n";
  }

  // index is total width of preceding boxes
  public void toVRML(VRMLState state, int index){
    state.append(
      "		    Transform{\n"+
      "		      translation "+((0.5*id.length()+index)*0.3333-0.5)+" 1.35 0\n"+
      "		      scale "+id.length()*0.3333+" 1 1\n"+
      "		      children[\n"+
      "			DEF "+id+"Box CheckBox {\n"+
      "			  description \""+tip+"\"\n"+
      "			  textureName \""+id+".png\"\n"+
      "			  textureName2 \""+id+"2.png\"\n"+
      "			}\n"+
      "		      ]\n"+
      "		    }\n");
    state.appendToSuffix(
      "ROUTE "+id+"Box.transparency TO "+id+".set_transparency\n");
  }
}

