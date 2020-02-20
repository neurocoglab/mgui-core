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

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.ProgressUpdater.Mode;
import mgui.interfaces.logs.LoggingType;

public class GiftWrap extends HullAlgorithm{
  public GiftWrap(Point3dObject3d[] pts) {
    super(pts);
  }
	
  int index(Point3d p) {
    for(int i=0; i<pts.length; i++){
      if (p==pts[i]) {
	return i;
      }
    }
    return -1;
  }
  
  protected Point3d search(Edge3d e) {
      int i;
      for(i = 0; pts[i] == e.start || pts[i] == e.end; i++) {
	/* nothing */
      }
      Point3d cand = pts[i];
      HalfSpace candh = new HalfSpace(e.start,e.end,cand);
      for(i=i+1; i < pts.length; i++) {
	if (pts[i] != e.start && pts[i] != e.end && candh.inside(pts[i])) {
	  cand = pts[i];
	  candh = new HalfSpace(e.start,e.end,cand);
	}
      }
      return cand;
}

  protected Point3d search2d(Point3d p) {
      int i;
      i = pts[0] == p?1:0;
      Point3d cand = pts[i];
      HalfSpace candh = new HalfSpace(p,cand);
      for(i=i+1; i < pts.length; i++) {
	if (pts[i] != p && candh.inside(pts[i])) {
	  cand = pts[i];
	  candh = new HalfSpace(p,cand);
	}
      }
      return cand;
}

  /* bottom point */
  protected Point3d bottom(){
    Point3d bot = pts[0];
    for (int i = 1; i < pts.length; i++) {
      if (pts[i].y() < bot.y()) {
	bot = pts[i];
      }
    }
    return bot;
  }
  
  @Override
public Object3dList build () {
	  return build(null);
  }
  
  @Override
public Object3dList build (ProgressUpdater progress) {
    /* First find a hull edge -- just connect bottommost to second from bottom */
    Point3d bot, bot2; /* bottom point and adjacent point*/
    bot = bottom();
    bot2 = search2d(bot);
	
    //no known end point for this algorithm, so we reset it if necessary
    if (progress != null){
    	progress.setMinimum(0);
    	progress.setMaximum(pts.length);
    	progress.setMode(Mode.Indeterminate);
    	}
    
    /* intialize the edge stack */
    EdgeStack es = new EdgeStack();
    es.put(bot,bot2);	
    es.put(bot2,bot);
    Object3dList faces = new Object3dList(20);
    int tcount = 1;
    Edge3d e = new Edge3d(bot,bot2,tcount);
    e.lastFrame = tcount++;
    faces.addElement(e);
	
    int pos = 0;
    
    /* now the main loop -- keep finding faces till there are no more to be found */
    while (! es.isEmpty() ) {
      
      e = es.get();
      Point3d cand = search(e);
      faces.addElement(new Triangle3d(e.start,e.end,cand,tcount++));
      es.putp(e.start,cand);
      es.putp(cand,e.end);
      if (progress.isCancelled()){
    	  InterfaceSession.log("GiftWrap: operation cancelled by user.", 
    			  			   LoggingType.Warnings);
    	  return null;
      	}
    }
    faces.lastFrame = tcount;
    return faces;
  }


  @Override
public Object3dList build2D() {
    /* First find a hull vertex -- just bottommost*/
    Point3d p; /* current hull vertex */
    Point3d bot = bottom(); /* bottom point */
		
    Object3dList faces = new Object3dList(20);
    int tcount = 1;
    faces.addElement(new Point3dObject3d(bot,tcount++));
		
    /* now the main loop -- keep finding edges till we get back */

    p = bot;
    do {
      Point3d cand = search2d(p);
      faces.addElement(new Edge3d(p,cand,tcount++));
      p = cand;
    } while (p!=bot);
    faces.lastFrame = tcount;
    return faces;
  }
	
}

