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
 * Class controlling painting of animated 3D logo.
 */
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;

public class Paint3d extends Canvas implements Painter{ 
  Viewer viewer;
  Image offscreen; //offscreen bitmap to avoid flickering
  Object3d model;
  Graphics g; //we need this to work around a JDK bug

  
  /** Create a canvas for rendering model in.
   */

  public Paint3d(Viewer viewer){
    this.viewer = viewer;
    this.model=model;
  }
  
  public void setModel(Object3d model) {
    this.model = model;
    viewer.put();
  }

  @Override
public void paint(Graphics g) {
    g.drawImage(offscreen,0,0,null);
  }

  public void run() {
    for (;;) {
      View3d v = viewer.get();
      v.clear();
      model.render(v);
      Graphics g = getParent().getGraphics(); //bug workaround - ought to be getGraphics()
      if (g != null) {
	g.drawImage(offscreen,0,0,null);
      }
    }
  }

  @Override
public void resize(int w, int h) {
    offscreen = createImage(w,h);
    viewer.setGraphics(offscreen.getGraphics(),w,h);
  }

  @Override
public void reshape(int x, int y,int w, int h) {
    resize(w,h);
  }

}
