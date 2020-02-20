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

import mgui.interfaces.InterfaceSession;

public class MorphNodeMap2D {

	MorphNode2D N_A;
	MorphNode2D N_B;
	MapWeight weight;
	boolean isReversed;
	
	public MorphNodeMap2D(){
		
	}
	
	public MorphNodeMap2D(boolean isR){
		isReversed = isR;
	}
	
	public MorphNodeMap2D(MorphNode2D s, MorphNode2D t, MapWeight w, boolean isR){
		isReversed = isR;
		N_A = s;
		N_B = t;
		weight = w;
	}
	
	public MorphNodeMap2D(MorphNode2D s, MorphNode2D t, MapWeight w){
		N_A = s;
		N_B = t;
		weight = w;
	}
	
	public int getTarget(){
		if (weight != null) return weight.target;
		return -1;
	}
	
	public void setTarget(int t){
		if (weight != null)
			weight.target = t;
		else{
			weight = new MapWeight();
			weight.target = t;
		}
	}
	
	public int getSource(){
		if (weight != null) return weight.source;
		return -1;
	}
	
	public void setSource(int s){
		if (weight != null)
			weight.source = s;
		else{
			weight = new MapWeight();
			weight.source = s;
		}
	}
	
	public double getWeight(){
		if (weight != null) return weight.weight;
		return -1;
	}
	
	public void setWeight(int w){
		if (weight != null)
			weight.weight = w;
		else{
			weight = new MapWeight();
			weight.weight = w;
		}
	}
	
	public MorphNode2D getNext(){
		//if (isReversed) return N_A;
		return N_B;
	}
	
	public MorphNode2D getPrev(){
		//if (isReversed) return N_B;
		return N_A;
	}
	
	public void printMap(){
		if (weight != null)
			InterfaceSession.log("Map N(A)" + weight.source + " -> N(B)" + weight.target +
				": weight=" + weight.weight);
	}
	
}