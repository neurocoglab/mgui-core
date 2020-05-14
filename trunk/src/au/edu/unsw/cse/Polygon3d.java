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
import java.awt.Color;
import java.awt.Point;

public class Polygon3d extends Object3dAdaptor {

  Point3d[] pol ;
  Color col;
  
  /** Create a polygon with given colour
   */
  public Polygon3d(Point3d[] pol,Color col){
    centre = pol[0].add(pol[1]).add(pol[2]).scale(1.0/3.0);
    this.pol = pol;
    this.col = col;
  }


  /** render the poly, given a 3D view */
  @Override
public void render(View3d v){
    int[] ix = new int[pol.length]; // pol in
    int[] iy = new int[pol.length]; // screen space
      
    // first transform corners to screen space
    Point p;
    for (int i = 0; i < pol.length; i++){
      p = v.toPoint(pol[i]);
      ix[i] = p.x;
      iy[i] = p.y;
    }
    v.g.setColor(col);
    v.g.fillPolygon(ix,iy,pol.length);
    v.g.setColor(Color.black);
    v.g.drawPolygon(ix,iy,pol.length);
  }

  /** compute new position of pol
   */
  @Override
public void transform(Matrix3D T){
      T.transform(pol);
      centre = pol[0].add(pol[1]).add(pol[2]).scale(1.0/3.0);
  }

}