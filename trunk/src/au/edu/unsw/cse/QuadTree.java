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

abstract class QuadTree {
  double max,min; //max and min z coords in this subtree
  Point3d pne, pse, pnw, psw; //corners of quadtree
	
  QuadTree(Point3d ne, Point3d se, Point3d nw, Point3d sw){
    pne = ne;
    pse = se;
    psw = sw;
    pnw = nw;
    max = -Double.MAX_VALUE;
    min = Double.MAX_VALUE;
  }
	
  QuadTree insert(Point3d p){
    if( p.z() > max) {
      max = p.z();
    }
    if( p.z() < min) {
      min = p.z();
    }
    return this;
  }

  static QuadTree build(Point3d[] pts){
    double maxx = -Double.MAX_VALUE;
    double minx = Double.MAX_VALUE;
    double maxy = -Double.MAX_VALUE;
    double miny = Double.MAX_VALUE;
    
    for (int i = 0; i < pts.length; i++){
      Point3d p = pts[i];
      if( p.x() > maxx) {
	maxx = p.x();
      }
      if( p.x() < minx) {
	minx = p.x();
      }
      if( p.y() > maxy) {
	maxy = p.y();
      }
      if( p.y() < miny) {
	miny = p.y();
      }
      
    }
    
    QuadTree q = new QuadTreeLeaf(new Point3d(maxx,maxy,0),
				  new Point3d(maxx,miny,0),
				  new Point3d(minx,maxy,0),
				  new Point3d(minx,miny,0));
    for (int i = 0; i < pts.length; i++){
      q = q.insert(pts[i]);
    }
    return q;
  }


  protected static Point3d cand; //keeps track of candidate vertex during search
  protected static Point3d start; //start of edge we're building face on
  protected static Point3d end; //end of edge
  protected static HalfSpace candh;
  protected Point3d search(Edge3d e) {
    cand = null;
    start = e.start;
    end = e.end;
    candh = new HalfSpace(new Point3d(0,0,min-1),
			  new Point3d(1,0,min-1),
			  new Point3d(0,1,min-1));
    /*every point should lie inside this halfplane */
    
    find();
    return cand;
  }

  /** check all points in this quadtree to see if they're inside candh
            case where we're looking for points above (greater z) */
  protected abstract void findup(double zint);

  /** check all points in this quadtree to see if they're inside candh
            case where we're looking for points above (greater z) */
  protected abstract void finddown(double zint);
  
  /** recursive search for points in candh, updating candh as we go*/
  protected void find() {
  	double zint;
  	if (candh.normal.z() > 0.0) {
  		/* want minimum z of intersection of candg with bbox of tree */
  		if (candh.normal.x() > 0.0) {
	  		if (candh.normal.y() > 0.0) {
	  			zint = candh.zint(pne);
	  		} else {
	  			zint = candh.zint(pse);
	  		}
	  	} else {
	  		if (candh.normal.y() > 0.0) {
	  			zint = candh.zint(pnw);
	  		} else {
	  			zint = candh.zint(psw);
	  		}
	  	}
	  	if (zint <= max) {
	  		findup(zint);
	  	}
	  } else {
  		/* want maximum z of intersection of candg with bbox of tree */
  		if (candh.normal.x() > 0.0) {
	  		if (candh.normal.y() > 0.0) {
	  			zint = candh.zint(psw);
	  		} else {
	  			zint = candh.zint(pnw);
	  		}
	  	} else {
	  		if (candh.normal.y() > 0.0) {
	  			zint = candh.zint(pse);
	  		} else {
	  			zint = candh.zint(pne);
	  		}
	  	}
	  	if (zint >= min) {
	  		finddown(zint);
	  	}
	  }
	}
	  


  void dump(int indent){
    out(indent,pne);
    out(indent,pse);
    out(indent,psw);
    out(indent,pnw);

    out(indent,max);
    out(indent,min);
  }

  
  void dump(){
    dump(0);
  }

  void out(int indent, Object o){
    for(;indent>0;indent--) {
      System.out.print(" ");
    }
    System.out.println(o);
  }

  void out(int indent, double d){
    out(indent,Double.toString(d));
  }
	
}