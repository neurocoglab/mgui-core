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
public class CSELogo extends Object3dList{
    static double[][] logodata = { // initial positions of cubes
    {2,2,2},{1,2,2},{2,2,1},{2,0,2},{0,2,2},{2,2,0},{1,0,2},{-1,2,2},{2,-1,2},
    {2,0,1},{2,2,-1},{2,-2,2},{0,0,2},{-2,2,2},{2,0,0},{2,2,-2},{1,2,-2},
    {1,-2,2},{-1,0,2},{-2,1,2},{2,-2,1},{2,0,-1},{-2,2,1},{0,-2,2},{-2,0,2},
    {2,-2,0},{2,0,-2},{-2,2,0},{2,-2,-1},{-2,2,-1},{-1,2,-2},{-1,-2,2},
    {-2,-2,2},{2,-2,-2},{-2,2,-2}};

  static double cuberadius = 0.35; // radius of cubes in logo
  public CSELogo() {
    super(logodata.length);
    for (int i=0; i < logodata.length; i++){
      Cube c = new Cube(new Point3d(logodata[i][0],
					    logodata[i][1],
					    logodata[i][2]),cuberadius);
      addElement(c);      
    }
  }    
}