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

public class GiftWrapQT extends GiftWrap{
	QuadTree q;
  public GiftWrapQT(Point3dObject3d[] pts) {
    super(pts);
    q = QuadTree.build(pts);
  }
  @Override
protected Point3d search(Edge3d e) {
  	Point3d cand1 = q.search(e);
  	Point3d cand2 = super.search(e);
  	if (cand1 != cand2) {
  		System.out.println("bad"+cand1+cand2);
  	}
  	return cand1;
 }
	
}