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

package mgui.interfaces.maps;

import java.util.ArrayList;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.xml.XMLObject;

/*********************************************************
 * Represents a coordinate mapping from one coordinate system to another (typically a model
 * system to screen or printer coordinates) 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class Map extends AbstractInterfaceObject implements XMLObject {
	
	public static final int MAP_2D = 0;
	public static final int MAP_3D = 1;
	
	protected ArrayList<MapListener> listeners = new ArrayList<MapListener>();
	
	public int getType(){ return -1; }
	public double getZoom() { return -1; }
	public void setZoom(double z){}
	
	public void fireMapListeners(){
		MapEvent e = new MapEvent(this);
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).mapUpdated(e);
	}
	
	public void addMapListener(MapListener l){
		listeners.add(l);
	}
	
	public void removeMapListener(MapListener l){
		listeners.remove(l);
	}
	
}