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

import org.jogamp.vecmath.Matrix4d;
import org.jogamp.vecmath.Matrix4f;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

import mgui.geometry.util.GeometryFunctions;

/***************************
 * Box in R3 with base point and three orthogonal axis vectors (denoted by S, T, and R, respectively).
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 */

public class Box3D extends Shape3D {

	protected Vector3f sAxis;		//equivalent to X-axis
	protected Vector3f tAxis;		//equivalent to Y-axis
	protected Vector3f rAxis;		//equivalent to Z-axis
	protected Point3f basePt;
	
	public Box3D(){
		basePt = new Point3f();
		sAxis = new Vector3f(1,0,0);
		tAxis = new Vector3f(0,1,0);
		rAxis = new Vector3f(0,0,1);
	}
	
	public Box3D(Point3f basept, Vector3f saxis, Vector3f taxis, Vector3f raxis){
		basePt = basept;
		sAxis = saxis;
		tAxis = taxis;
		rAxis = raxis;
	}
	
	public Box3D(Box3D copy){
		setFromBox(copy);
		
	}
	
	public void setFromBox(Box3D copy){
		basePt = new Point3f(copy.basePt);
		sAxis = new Vector3f(copy.sAxis);
		tAxis = new Vector3f(copy.tAxis);
		rAxis = new Vector3f(copy.rAxis);
	}
	
	/*******************************
	 * Returns a set of edges from this box. Dimension of return array
	 * is [12][2]
	 * 
	 * @return
	 */
	public Point3f[][] getEdges(){
		
		Point3f[][] edges = new Point3f[12][2];
		
		Point3f p = new Point3f(basePt);
		Point3f p2 = new Point3f(p);
		p2.add(sAxis);
		edges[0][0] = p;
		edges[0][1] = p2;
		p = new Point3f(p2);
		p2 = new Point3f(p2);
		p2.add(tAxis);
		edges[1][0] = p;
		edges[1][1] = p2;
		p = new Point3f(p2);
		p2 = new Point3f(p2);
		p2.add(rAxis);
		edges[2][0] = p;
		edges[2][1] = p2;
		p = new Point3f(edges[0][1]);
		p2 = new Point3f(p);
		p2.add(rAxis);
		edges[3][0] = p;
		edges[3][1] = p2;
		
		p = new Point3f(basePt);
		p2 = new Point3f(p);
		p2.add(tAxis);
		edges[4][0] = p;
		edges[4][1] = p2;
		p = new Point3f(p2);
		p2 = new Point3f(p2);
		p2.add(rAxis);
		edges[5][0] = p;
		edges[5][1] = p2;
		p = new Point3f(p2);
		p2 = new Point3f(p2);
		p2.add(sAxis);
		edges[6][0] = p;
		edges[6][1] = p2;
		p = new Point3f(edges[4][1]);
		p2 = new Point3f(p);
		p2.add(sAxis);
		edges[7][0] = p;
		edges[7][1] = p2;
		
		p = new Point3f(basePt);
		p2 = new Point3f(p);
		p2.add(rAxis);
		edges[8][0] = p;
		edges[8][1] = p2;
		p = new Point3f(p2);
		p2 = new Point3f(p2);
		p2.add(sAxis);
		edges[9][0] = p;
		edges[9][1] = p2;
		p = new Point3f(p2);
		p2 = new Point3f(p2);
		p2.add(tAxis);
		edges[10][0] = p;
		edges[10][1] = p2;
		p = new Point3f(edges[8][1]);
		p2 = new Point3f(p);
		p2.add(tAxis);
		edges[11][0] = p;
		edges[11][1] = p2;
		
		return edges;
		
	}
	
	/**@TODO throw exception if basis is non-orthogonal **/
	public void setSAxis(Vector3f axis){
		sAxis = axis;
		
	}
	
	public void setSDim(float d){
		sAxis.normalize();
		sAxis.scale(d);
	}
	
	public void setTAxis(Vector3f axis){
		tAxis = axis;
	}
	
	public void setTDim(float d){
		tAxis.normalize();
		tAxis.scale(d);
	}
	
	public void setRAxis(Vector3f axis){
		rAxis = axis;
	}
	
	public void setRDim(float d){
		rAxis.normalize();
		rAxis.scale(d);
	}
	
	/**************************
	 * Returns the geometric size of the x (S) dimension
	 * 
	 * @return
	 */
	public float getSDim(){
		return sAxis.length();
	}
	
	/**************************
	 * Returns the geometric size of the y (T) dimension
	 * 
	 * @return
	 */
	public float getTDim(){
		return tAxis.length();
	}
	
	/**************************
	 * Returns the geometric size of the z (R) dimension
	 * 
	 * @return
	 */
	public float getRDim(){
		return rAxis.length();
	}
	
	public void setBasePt(Point3f p){
		basePt = p;
	}
	
	
	
	/**************************
	 * Returns a matrix which will transform a vector expressed in world (model) coordinates
	 * to one expressed with respect to this box's coordinate space. Note that basis vectors
	 * must be orthonormal for this to return a valid coordinate.
	 * 
	 * TODO implement a general function to transform into any coordinate space (including
	 * non-orthonormal)
	 * 
	 * @return
	 */
	public Matrix4f getBasisTransform(){
		//Direction cosine representation
		
		//world space axes
		Vector3d xWorld = new Vector3d(1, 0, 0);
		Vector3d yWorld = new Vector3d(0, 1, 0);
		Vector3d zWorld = new Vector3d(0, 0, 1);
		
		//basis s-axis
		double cosSX = Math.cos(new Vector3d(sAxis).angle(xWorld));
		double cosSY = Math.cos(new Vector3d(sAxis).angle(yWorld));
		double cosSZ = Math.cos(new Vector3d(sAxis).angle(zWorld));
		
		//basis t-axis
		double cosTX = Math.cos(new Vector3d(tAxis).angle(xWorld));
		double cosTY = Math.cos(new Vector3d(tAxis).angle(yWorld));
		double cosTZ = Math.cos(new Vector3d(tAxis).angle(zWorld));
		
		//basis r-axis
		double cosRX = Math.cos(new Vector3d(rAxis).angle(xWorld));
		double cosRY = Math.cos(new Vector3d(rAxis).angle(yWorld));
		double cosRZ = Math.cos(new Vector3d(rAxis).angle(zWorld));
		
		Matrix4d t = new Matrix4d();
		t.m00 = cosSX; // * -basePt.x;
		t.m01 = cosSY; // * -basePt.y;
		t.m02 = cosSZ; // * -basePt.z;
		t.m03 = basePt.x;
		t.m10 = cosTX; // * -basePt.x;
		t.m11 = cosTY; // * -basePt.y;
		t.m12 = cosTZ; // * -basePt.z;
		t.m13 = basePt.y;
		t.m20 = cosRX; // * -basePt.x;
		t.m21 = cosRY; // * -basePt.y;
		t.m22 = cosRZ; // * -basePt.z;
		t.m23 = basePt.z;
		t.m33 = 1.0f;
		//Vector3f v = new Vector3f(-basePt.x, -basePt.y, -basePt.z);
		//Transform3D t3d = new Transform3D(t);
		
		return new Matrix4f(t);
	}
	
	/*************************************************
	 * Sets the axes and origin from the specified matrix <code>T</code>, and the three dimensions
	 * 
	 * @param T			Basis transform
	 * @param s_dim		Dimension of box in S
	 * @param t_dim		Dimension of box in T
	 * @param r_dim		Dimension of box in R
	 */
	public void setFromBasisTransform(Matrix4f T, float s_dim, float t_dim, float r_dim){
	
		sAxis = new Vector3f(T.m00, T.m01, T.m02);
		tAxis = new Vector3f(T.m10, T.m11, T.m12);
		rAxis = new Vector3f(T.m20, T.m21, T.m22);
		
		sAxis.normalize();
		sAxis.scale(s_dim);
		tAxis.normalize();
		tAxis.scale(t_dim);
		rAxis.normalize();
		rAxis.scale(r_dim);
		
		basePt = new Point3f(T.m03, T.m13, T.m23);
		
	}
	
	@Override
	public Point3f getCenter(){
		Point3f c = new Point3f(basePt);
		Vector3f offset = new Vector3f(rAxis);
		offset.add(sAxis);
		offset.add(tAxis);
		offset.scale(0.5f);
		c.add(offset);
		return c;
	}
	
	/***************************
	 * Returns 4 points representing a side of this box.
	 * @param which side relative to base point. 0 is same side as b.p., 1 is opposite
	 * @param dir axis normal to the side. 0 = sAxis, 1 = tAxis, 2 = rAxis
	 * @return ArrayList of 4 points
	 */
	public ArrayList<Point3f> getSide(int which, int dir){
		Vector3f offset = new Vector3f();
		Vector3f axis0 = new Vector3f();
		Vector3f axis1 = new Vector3f();
		Vector3f axis2 = new Vector3f();
		if (dir == 0) {
			axis0.set(sAxis);
			axis1.set(rAxis);
			axis2.set(tAxis);
			}
		if (dir == 1) {
			axis0.set(tAxis);
			axis1.set(sAxis);
			axis2.set(rAxis);
			}
		if (dir == 2) {
			axis0.set(rAxis);
			axis1.set(sAxis);
			axis2.set(tAxis);
			}
		if (which == 1) offset.add(axis0);
		ArrayList<Point3f> pts = new ArrayList<Point3f>(4);
		for (int i = 0; i < 4; i++)
			pts.add(new Point3f());
		Point3f thisPt = new Point3f();
		thisPt.set(getBasePt());
		thisPt.add(offset);
		pts.get(0).set(thisPt);
		thisPt.add(axis1);
		pts.get(1).set(thisPt);
		thisPt.add(axis2);
		pts.get(2).set(thisPt);
		thisPt.sub(axis1);
		pts.get(3).set(thisPt);
		return pts;
	}
	
	/************
	 * Returns a copy of this box's base point.
	 * 
	 * @return
	 */
	public Point3f getBasePt(){
		return new Point3f(basePt);
	}
	
	/************
	 * Returns a copy of this box's S-axis (analogous to X-axis).
	 * 
	 * @return
	 */
	public Vector3f getSAxis(){
		return new Vector3f(sAxis);
	}
	
	/************
	 * Returns a copy of this box's T-axis (analogous to Y-axis).
	 * 
	 * @return
	 */
	public Vector3f getTAxis(){
		return new Vector3f(tAxis);
	}
	
	/************
	 * Returns a copy of this box's R-axis (analogous to Z-axis).
	 * 
	 * @return
	 */
	public Vector3f getRAxis(){
		return new Vector3f(rAxis);
	}
	
	//return an array of floats representing the coordinates for this
	//shape
	@Override
	public float[] getCoords(){
		float[] coords = new float[24];
		coords[0] = basePt.x;
		coords[1] = basePt.y;
		coords[2] = basePt.z;
		Point3f p = new Point3f();
		p.add(basePt, sAxis);
		coords[3] = p.x;
		coords[4] = p.y;
		coords[5] = p.z;
		p.add(basePt, tAxis);
		coords[6] = p.x;
		coords[7] = p.y;
		coords[8] = p.z;
		p.add(basePt, rAxis);
		coords[9] = p.x;
		coords[10] = p.y;
		coords[11] = p.z;
		p.add(basePt, rAxis);
		p.add(sAxis);
		coords[12] = p.x;
		coords[13] = p.y;
		coords[14] = p.z;
		p.add(basePt, rAxis);
		p.add(tAxis);
		coords[15] = p.x;
		coords[16] = p.y;
		coords[17] = p.z;
		p.add(basePt, sAxis);
		p.add(tAxis);
		coords[18] = p.x;
		coords[19] = p.y;
		coords[20] = p.z;
		p.add(basePt, rAxis);
		p.add(sAxis);
		p.add(tAxis);
		coords[21] = p.x;
		coords[22] = p.y;
		coords[23] = p.z;
		return coords;
	}
	
	@Override
	public Point3f getVertex(int i) {
		if (i < 0 || i > 7) return null;
		float[] coords = getCoords();
		return new Point3f(coords[(i * 3)], coords[(i * 3) + 1], coords[(i * 3) + 2]);
	}
	
	@Override
	public void setCoords(float[] coords){
		//set the basepoint and vectors from the coords
		basePt.set(new Point3f(coords[0], coords[1], coords[2]));
		sAxis.set(new Vector3f(coords[3], coords[4], coords[5]));
		sAxis.sub(basePt);
		tAxis.set(new Vector3f(coords[6], coords[7], coords[8]));
		tAxis.sub(basePt);
		rAxis.set(new Vector3f(coords[9], coords[10], coords[11]));
		rAxis.sub(basePt);
	}
	
	@Override
	public boolean contains(Point3f p){
		Vector3f pVect = new Vector3f();
		if (GeometryFunctions.isCoincident(p, basePt)) return true;
		pVect.sub(p, basePt);
		float xAngle = pVect.angle(sAxis);
		float yAngle = pVect.angle(tAxis);
		float zAngle = pVect.angle(rAxis);
		float len = pVect.length();
		
		//if angle > 90 deg, point not in box
		if (GeometryFunctions.compareFloat(xAngle, (float)(Math.PI / 2.0)) > 0 ||
			GeometryFunctions.compareFloat(yAngle, (float)(Math.PI / 2.0)) > 0 ||
			GeometryFunctions.compareFloat(zAngle, (float)(Math.PI / 2.0)) > 0 ) return false;
		
		//if any axis component is longer than edge length, point not in box 
		if (Math.cos(xAngle) * len > sAxis.length()) return false;
		if (Math.cos(yAngle) * len > tAxis.length()) return false;
		if (Math.cos(zAngle) * len > rAxis.length()) return false;
		
		return true;
	}
	
	//return non-zero axes; if one (and only one) axis is zero,
	//set it to the cross-product of the other two
	//if more than one axis is zero, return null
	protected Vector3f[] getNonzeroAxes(){
		
		if (GeometryFunctions.isZero(sAxis)){
			if (!GeometryFunctions.isZero(rAxis) &&
				!GeometryFunctions.isZero(tAxis)){
					Vector3f v = new Vector3f();
				v.cross(rAxis, tAxis);
				v.normalize();
				return new Vector3f[]{v, new Vector3f(tAxis), new Vector3f(rAxis)};
			}else{
				return null;	
				}
			}
		
		if (GeometryFunctions.isZero(tAxis)){
			if (!GeometryFunctions.isZero(sAxis) &&
				!GeometryFunctions.isZero(rAxis)){
					Vector3f v = new Vector3f();
				v.cross(sAxis, rAxis);
				v.normalize();
				return new Vector3f[]{new Vector3f(sAxis), v, new Vector3f(rAxis)};
			}else{
				return null;	
				}
			}
		
		if (GeometryFunctions.isZero(rAxis)){
			if (!GeometryFunctions.isZero(sAxis) &&
				!GeometryFunctions.isZero(tAxis)){
					Vector3f v = new Vector3f();
				v.cross(sAxis, tAxis);
				v.normalize();
				return new Vector3f[]{new Vector3f(sAxis), new Vector3f(tAxis), v};
				}
			}
		
		return new Vector3f[]{new Vector3f(sAxis), new Vector3f(tAxis), new Vector3f(rAxis)};
		
	}
	
	
	
	//return list of nodes.. 
	@Override
	public ArrayList<Point3f> getVertices(){
		
		float[] coords = this.getCoords();
		ArrayList<Point3f> vertices = new ArrayList<Point3f>(8);
		for (int i = 0; i < coords.length/3; i++)
			vertices.add(new Point3f(coords[3*i], coords[3*i+1], coords[3*i+2]));
		return vertices;
		
//		ArrayList<Point3f> nodes = new ArrayList<Point3f>(8);
//		//O
//		nodes.add((Point3f)basePt.clone());
//		Point3f p = new Point3f();
//		//P1
//		p.add(basePt, tAxis);
//		nodes.add(new Point3f(p));
//		//P2
//		p.add(sAxis);
//		nodes.add(new Point3f(p));
//		//P3
//		p.sub(tAxis);
//		nodes.add(new Point3f(p));
//		//P4
//		p.add(basePt, rAxis);
//		nodes.add(new Point3f(p));
//		//P5
//		p.add(tAxis);
//		nodes.add(new Point3f(p));
//		//P6
//		p.add(sAxis);
//		nodes.add(new Point3f(p));
//		//P7
//		p.sub(tAxis);
//		nodes.add(new Point3f(p));
//		return nodes;
	}
	
	//set object from a list of 4 nodes
	@Override
	public void setVertices(ArrayList<Point3f> vertices){
		if (vertices.size() != 8) return;
		basePt.set(vertices.get(0));
		sAxis.set(vertices.get(1));
		sAxis.sub(basePt);
		tAxis.set(vertices.get(2));
		tAxis.sub(basePt);
		rAxis.set(vertices.get(3));
		rAxis.sub(basePt);
	}
	
	@Override
	public Object clone(){
		return new Box3D(this);
	}
	
	public String getLocalName() {
		return "Box3D";
	}
	
	public Point3f getOppPt(){
		Point3f p = new Point3f(basePt);
		p.add(sAxis);
		p.add(tAxis);
		p.add(rAxis);
		return p;
	}
	
	public Point3f getMinPt(){
		Point3f p = getOppPt();
		return GeometryFunctions.getMinPt(p, basePt);
	}
	
	public Point3f getMaxPt(){
		Point3f p = getOppPt();
		return GeometryFunctions.getMaxPt(p, basePt);
	}

	@Override
	public String toString(){
		return "Box3D: origin " + basePt.toString() + 
			   ", x-axis " + this.sAxis +
			   ", y-axis " + this.tAxis +
			   ", z-axis " + this.rAxis;
		
	}
	
}