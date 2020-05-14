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
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.MalformedURLException;
import java.net.URL;
/** Fake AppletStub so that an Applet can run as as application */
public class FakeAppletStub implements AppletStub {

  String docBase;

  public FakeAppletStub(String docBase){
    this.docBase = docBase;
  }

  public boolean isActive(){
    return false;
  }

  public URL getDocumentBase(){
    try {
      return new URL("file://"+docBase);
    } catch (MalformedURLException e) {
      return null;
    }
  }
  public URL getCodeBase(){
    return null;
  }

  public String getParameter(String name){
    return null;
  }
  
  public AppletContext getAppletContext(){
    return null;
  }
  
  public void appletResize(int width,
                                   int height){
  }
}