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

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/*****
 * Represents a 3 dimensional cube object, using a Point3f, two Vector3f's
 * and a float.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Cube3D extends Shape3D {

	Point3f corner;
	Vector3f[] basis;
	float size;
	
	public Cube3D(){
		
	}
	
	public Cube3D(Point3f cnr, Vector3f[] basis_v, float size){
		corner = cnr;
		/**@todo check for orthogonality **/
		if (basis_v.length == 2 || basis_v.length == 3)
			basis = basis_v;
	}
	
	public Cube3D(Point3f cnr1, Point3f cnr2, Point3f cnr3, float size){
		corner = cnr1;
		basis = new Vector3f[2];
		/**@todo check for orthogonality using isOrthogonal(Vector3f[]) **/
		//dimensions.sub(cnr2, cnr1);
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