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

package mgui.morph.sections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import mgui.geometry.Polygon2D;


public class MappedPolygon2DPath {

	public ArrayList<MappedPolygon2D> maps = new ArrayList<MappedPolygon2D>();
	public Polygon2D startPoly;
	
	protected ArrayList<MappedPolygon2D> buffer;
	
	public MappedPolygon2DPath(Polygon2D startPoly){
		this.startPoly = startPoly;
	}
	
	public MappedPolygon2DPath(MappedPolygon2D map){
		addMap(map);
	}
	
	public MappedPolygon2DPath(ArrayList<MappedPolygon2D> maps){
		setMaps(maps);
	}
	
	public MappedPolygon2DPath(MappedPolygon2DPath path){
		setMaps(path.getPolygons());
	}
	
	public void setMaps(ArrayList<MappedPolygon2D> maps){
		this.maps = new ArrayList<MappedPolygon2D>(maps.size());
		if (maps.size() == 0) return;
		addMaps(maps);
		//sort();
	}
	
	public ArrayList<MappedPolygon2D> getPolygons(){
		return maps;
	}
	
	public int getCount(){
		return maps.size();
	}
	
	public Iterator getIterator(){
		return maps.iterator();
	}
	
	public void addMap(MappedPolygon2D map){
		maps.add(map);
		sort();
	}
	
	public void addMaps(ArrayList<MappedPolygon2D> maps){
		this.maps.addAll(maps);
		sort();
	}
	
	public Polygon2D getLastPolygon(){
		if (getCount() == 0) return startPoly;
		return getLastMap().getOrderedB();
	}
	
	public Polygon2D getStartPolygon(){
		return startPoly;
	}
	
	public MappedPolygon2D getLastMap(){
		if (getCount() == 0) return null;
		return maps.get(maps.size() - 1);
	}
	
	public void sort(){
		Collections.sort(maps, new Comparator<MappedPolygon2D>(){
			public int compare(MappedPolygon2D poly1, MappedPolygon2D poly2){
				if (poly1.prevSect < poly2.prevSect) return -1;
				if (poly1.prevSect == poly2.prevSect) return 0;
				return 1;
				}
			});
		startPoly = maps.get(0).getA();
	}
	
	//fun stuff
	
	public void resetBuffer(){
		buffer = new ArrayList<MappedPolygon2D>();
	}
	
	/*******************
	 * Method adds a map to this path's buffer. Maps in the buffer will be added to this and new paths
	 * by the getBifurcatedPaths() method. 
	 * 
	 * @param map Map to add to buffer
	 */
	public void newMap(MappedPolygon2D map){
		if (buffer == null) resetBuffer();
		buffer.add(map);
	}
	
	/*******************
	 * Adds a map to this path and creates new paths for each additional map in the buffer. Maps are added
	 * to this buffer using the newMap method.
	 * 
	 * @return an ArrayList of bifurcated paths, one for each additional map in the buffer
	 */
	public ArrayList<MappedPolygon2DPath> getBifurcatedPaths(){
		if (buffer == null || buffer.size() == 0) return new ArrayList<MappedPolygon2DPath>();
		
		ArrayList<MappedPolygon2DPath> newPaths = new ArrayList<MappedPolygon2DPath>();
		MappedPolygon2DPath path;
		
		//create new paths for additional maps
		for (int i = 1; i < buffer.size(); i++){
			path = new MappedPolygon2DPath(this);
			path.addMap(buffer.get(i));
			newPaths.add(path);
			}
		
		//add first map to this path
		addMap(buffer.get(0));
		
		//reset the buffer
		resetBuffer();
		
		return newPaths;
		
	}
	
}