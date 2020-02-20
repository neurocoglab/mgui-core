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

package mgui.geometry;

import java.util.ArrayList;

import javax.media.j3d.BoundingSphere;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/*********************************************
 * Represents a sphere in R3.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Sphere3D extends Shape3D {

	public Point3f center = new Point3f(0,0,0);
	public float radius = 0;
	
	public Sphere3D(){
		
	}
	
	public Sphere3D(Point3f c, float r){
		center = c;
		radius = r;
	}
	
	public Sphere3D(BoundingSphere bs){
		Point3d tempPt = new Point3d();
		bs.getCenter(tempPt);
		center.set(tempPt);
		radius = (float)bs.getRadius();
	}
	
	@Override
	public Box3D getBoundBox(){
		if (center == null || radius <= 0) return null;
		Point3f base_pt = new Point3f(center);
		base_pt.sub(new Vector3f(radius, radius, radius));
		
		return new Box3D(base_pt, new Vector3f(radius * 2f, 0, 0),
								  new Vector3f(0, radius * 2f, 0),
								  new Vector3f(0, 0, radius * 2f));
		
	}
	
	@Override
	public float[] getCoords() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point3f getVertex(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Point3f> getVertices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCoords(float[] f) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setVertices(ArrayList<Point3f> n) {
		// TODO Auto-generated method stub
		
	}
	
}