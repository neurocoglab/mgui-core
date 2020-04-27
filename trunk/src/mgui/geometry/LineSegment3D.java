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

import org.jogamp.vecmath.Point3f;

/**********************************************
 * Represents a line segment in R3.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class LineSegment3D extends Shape3D {

	protected Point3f pt1;
	protected Point3f pt2;
	
	public LineSegment3D(){
		pt1 = new Point3f();
		pt2 = new Point3f();
	}
	
	public LineSegment3D(Point3f thisPt1, Point3f thisPt2){
		pt1 = thisPt1;
		pt2 = thisPt2;
	}
	
	@Override
	public float[] getCoords() {
		
		return new float[]{pt1.x, pt1.y, pt1.z,
						   pt2.x, pt2.y, pt2.z};
	}

	@Override
	public Point3f getVertex(int index) {
		if (index == 0) return new Point3f(pt1);
		if (index == 0) return new Point3f(pt2);
		return null;
	}

	@Override
	public ArrayList<Point3f> getVertices() {
		ArrayList<Point3f> nodes = new ArrayList<Point3f>(2);
		nodes.add(new Point3f(pt1));
		nodes.add(new Point3f(pt2));
		return nodes;
	}

	@Override
	public void setCoords(float[] f) {
		if (f.length != 6) return;
		pt1 = new Point3f(f[0], f[1], f[2]);
		pt2 = new Point3f(f[3], f[4], f[5]);
	}

	@Override
	public void setVertices(ArrayList<Point3f> nodes) {
		if (nodes.size() != 2) return;
		pt1 = new Point3f(nodes.get(0));
		pt2 = new Point3f(nodes.get(1));
	}
	
	
}