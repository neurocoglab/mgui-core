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
/**
 * This class is used for communication between the thread that responds to
 * user input and the one that does the drawing
 */
import java.awt.Event;

public class Mousedelta {
  private int deltax=0;
  private int deltay=0;
  private int deltax2=0;
  private int deltay2=0;
  private int deltax3=0;
  private int deltay3=0;
  private int frameNo=0;
  private boolean repaint=false;
  private boolean animating=false;

  public synchronized void put (int deltax, int deltay, int modifiers){
  	if ((Event.ALT_MASK & modifiers) !=0) {
  		deltax2 += deltax;
  		deltay2 += deltay;
  	} else if ((Event.META_MASK & modifiers) !=0) {
   		deltax3 += deltax;
  		deltay3 += deltay;
    } else if (modifiers == 0) {
    	this.deltax += deltax;
        this.deltay += deltay;
    }
    repaint = true;
    notify();
  }

  public synchronized void put (){
    repaint = true;
    notify();
  }

  public synchronized void putFrameNo(int frameNo) {
    this.frameNo = frameNo;
    repaint = true;
    notify();
  }

  public synchronized void startAnimation (){
    animating = true;
    repaint = true;
    notify();
  }

  public synchronized void stopAnimation (){
    animating = false;
  }


  public synchronized MouseInfo get(){
    while (!repaint) {
      try {
	wait();
      } catch (InterruptedException e) {
      }
    }
    MouseInfo m = new MouseInfo(deltax,deltay,deltax2,deltay2,deltax3,deltay3,frameNo);
    deltax = 0;
    deltay = 0;
    deltax2 = 0;
    deltay2 = 0;
    deltax3 = 0;
    deltay3 = 0;
    repaint = animating; //only need to repaint again if we're animating
    return m;
  }

} 