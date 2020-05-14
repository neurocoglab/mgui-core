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

import java.awt.Graphics;

public class PaintVRML implements Painter{  

  Applet3d applet; //parent applet
  Object3d model;

  public PaintVRML(Applet3d applet){
    this.applet = applet;
  }

  public void setModel(Object3d model){
    // get the handle to the VRML Browser.
    // we must do this first to ensure that vrmlAllPROTOs works

    VRMLState state = new VRMLState(applet.viewer);
    model.toVRML(state);
    AnimationWidgetVRML.toVRML(state);
    String s = "NavigationInfo{type [\"EXAMINE\",\"ANY\"]}\n"+
      state.toString();
    if (applet.getParameter("cgi")!=null){
      System.out.println("#VRML V2.0 utf8\n"+s);
    } else {
    //  try {
	//Browser browser = (Browser) vrml.external.Browser.getBrowser(applet);
	//Node[] scene = browser.createVrmlFromString(s);
	//browser.replaceWorld(scene);
   //   }
    //  catch (InvalidVrmlException ie) {
	//System.err.println("Bad VRML! " + ie);
	//System.err.println(s);
    //  }
   //   catch (NoClassDefFoundError e) {
	//System.err.println("Cannot communicate with VRML browser");
   //   }
    }
  }
  
  
  public void paint(Graphics g){
    //nothing to do - VRML browser does it all
  }
  
  public void run(){
  }
}
