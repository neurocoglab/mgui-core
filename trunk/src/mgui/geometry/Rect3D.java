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

package mgui.geometry;

import java.util.ArrayList;

import org.jogamp.vecmath.Point3f;

/**************************************************
 * Represents a 3D rectangle shape, defined by four 3D nodes representing BL, BR, TR, TL corners, respectively.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Rect3D extends Shape3D {

	protected Point3f[] nodes = new Point3f[4];
	
	public Rect3D(){
		resetNodes();
	}
	
	public Rect3D(Rect3D copy){
		setCoords(copy.getCoords());
	}
	
	public Rect3D(Point3f[] corners){
		if (corners.length >= 4)
			for (int i = 0; i < 4; i++)
				nodes[i] = corners[i];
		else
			resetNodes();	
	}
	
	public Rect3D(ArrayList<Point3f> corners){
		if (corners.size() >= 4)
			for (int i = 0; i < 4; i++)
				nodes[i] = corners.get(i);
		else
			resetNodes();	
	}
	
	protected void resetNodes(){
		for (int i = 0; i < 4; i++)
			nodes[i] = new Point3f();
	}
	
	@Override
	public ArrayList<Point3f> getVertices(){
		ArrayList<Point3f> array = new ArrayList<Point3f>(4);
		for (int i = 0; i < 4; i++)
			array.add(nodes[i]);
		return array;
	}
	
	@Override
	public void setVertices(ArrayList<Point3f> n){  
		for (int i = 0; i < 4; i++)
			nodes[i] = n.get(i);
	}
	
	public float getWidth(){
		return nodes[0].distance(nodes[1]);
	}
	
	public float getHeight(){
		return nodes[0].distance(nodes[3]);
	}
	
	public Point3f getOrigin(){
		return nodes[1];
	}
	
	@Override
	public float[] getCoords() {
		if (nodes == null) return null;
		float[] coords = new float[12];
		for (int i = 0; i < 4; i++){
			coords[i * 3] = nodes[i].x;
			coords[(i * 3) + 1] = nodes[i].y;
			coords[(i * 3) + 2] = nodes[i].z;
			}
		return coords;
	}

	@Override
	public Point3f getVertex(int i) {
		if (nodes == null) return null;
		return new Point3f(nodes[i]);
	}

	@Override
	public void setCoords(float[] coords) {
		if (coords.length != 12) return;
		nodes = new Point3f[4];
		
		for (int i = 0; i < 4; i++){
			nodes[i] = new Point3f(coords[(i * 3)], coords[(i * 3) + 1], coords[(i * 3) + 2]);
		}
		
	}
	
}