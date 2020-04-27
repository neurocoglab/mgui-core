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

package mgui.morph.sections;

import org.jogamp.vecmath.Point2f;

public class MorphNode2D implements Cloneable{

	Point2f thisPt;
	double tangentX, tangentY;
	
	public MorphNode2D(){
		
	}
	
	public MorphNode2D(Point2f p){
		thisPt = p;
	}
	
	public void setTangentX(double t){
		tangentX = t;
	}
	
	public void setTangentY(double t){
		tangentY = t;
	}
	
	@Override
	public Object clone(){
		MorphNode2D retObj = new MorphNode2D((Point2f)thisPt.clone());
		retObj.tangentX = tangentX;
		retObj.tangentY = tangentY;
		return retObj;
	}
	
}