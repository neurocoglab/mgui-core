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

class QuadTreeInternal extends QuadTree{
  QuadTree ne, nw, sw, se; //children
  double midx, midy;

  QuadTreeInternal(Point3d pne, Point3d pse, Point3d pnw, Point3d psw,
		   QuadTree ne, QuadTree se, QuadTree nw, QuadTree sw){
    super(pne,pse,pnw,psw);
    this.ne = ne;
    this.se = se;
    this.nw = nw;
    this.sw = sw;
    midx = (pne.x() + pnw.x())/2;
    midy = (pne.y() + pse.y())/2;
  }
	
  @Override
QuadTree insert(Point3d p){
    super.insert(p);
    if (p.x() > midx) {
      if (p.y() > midy) {
	ne = ne.insert(p);
      } else {
	se = se.insert(p);
      }
    } else {
      if (p.y() > midy) {
	nw = nw.insert(p);
      } else {
	sw = sw.insert(p);
      }
    }
    return this;
  }

 /** check all points in this quadtree to see if they're inside candh
            case where we're looking for points above (greater z) */
  @Override
protected void findup(double zint) {
  	ne.find();
  	se.find();
  	nw.find();
  	sw.find();
  }
  	

  /** check all points in this quadtree to see if they're inside candh
            case where we're looking for points above (greater z) */
  @Override
protected  void finddown(double zint){
  	findup(zint);
  }

  @Override
void dump(int indent){
    super.dump(indent);

    out(indent,midx);
    out(indent,midy);
    out(indent,"ne");
    ne.dump(indent+2);
    out(indent,"se");
    se.dump(indent+2);
    out(indent,"nw");
    nw.dump(indent+2);
    out(indent,"sw");
    sw.dump(indent+2);
  }
			

}