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
 * This class is used to store the information about a collection of mouse movements
 */

public class MouseInfo {
  public int deltax=0;
  public int deltay=0;
  public int deltax2=0;
  public int deltay2=0;
  public int deltax3=0;
  public int deltay3=0;
  public int frameNo=0;
  
  public MouseInfo(int x, int y, int x2, int y2, int x3, int y3, int frameNo) {
  	deltax = x;
  	deltay = y;
  	deltax2 = x2;
  	deltay2 = y2;
  	deltax3 = x3;
  	deltay3 = y3;
  	this.frameNo = frameNo;
  }

} 