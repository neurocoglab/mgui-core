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

package mgui.interfaces.shapes;

/*************************
 * GeometryUpdater implementation for Shape3DInt objects. Takes a node array which
 * can then be modified by 
 */

import org.jogamp.java3d.Geometry;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.GeometryUpdater;
import org.jogamp.vecmath.Point3f;

public class NodesUpdater implements GeometryUpdater {

	private Point3f[] buffer;
	private Shape3DInt thisShape;
	public boolean newArray;
	
	public NodesUpdater (Shape3DInt s){
		System.arraycopy(s.shape3d.getCoords(), 0, buffer, 0, s.shape3d.getCoords().length);
		thisShape = s;
	}
	
	//called by GeometryArray's updateData function
	public void updateData(Geometry g) {
		thisShape.shape3d.setVertices(buffer);
		if (newArray && GeometryArray.class.isInstance(g)){
			GeometryArray ga = (GeometryArray)g;
			ga.setCoordRef3f(buffer);
			
			
			}
	}
	
	public void setNodes(Point3f[] array){
		buffer = array;
	}
	
	public Point3f[] getNodes(){
		return buffer;
	}

}