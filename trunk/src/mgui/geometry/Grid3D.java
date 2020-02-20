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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.volume.VolumeMetadata;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/*************************
 * Holds a 3D array of values and a Box3D defining the distribution of these
 * values in R3. For use as a voxel set or volume 3D texture. Values are designated
 * in terms of their size in bytes using the function setDataSize
 * 
 * <p>To avoid confusion, dimensions in a <code>Grid3D</code> shape, following the Java3D convention, 
 * are specified as S (analogous to X), T (analogous to Y), and R (analogous to Z). 
 * 
 * <p>Voxels are referred to as integer arrays: {s, t, r}; and with an absolute index, calculated
 * as: r * s_size * t_size + t * s_size + s.
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */

public class Grid3D extends Box3D {
	
	protected int s_size, t_size, r_size, v_size;
	VolumeMetadata metadata;
	
	public Grid3D(){
		this(1,1,1,new Box3D());
	}
	
	public Grid3D(Grid3D grid){
		setFromGrid(grid);
	}
	
	public Grid3D(int s_size, int t_size, int r_size, Box3D box){
		this(s_size, t_size, r_size, 1, box);
	}
	
	public Grid3D(int s_size, int t_size, int r_size, int v_size, Box3D box){
		this.s_size = s_size;
		this.t_size = t_size;
		this.r_size = r_size;
		this.v_size = v_size;
		this.setFromBox(box);
	}
	
	/******************************
	 * Returns the S, T, R, V dimensions, as an int array
	 * 
	 * @return
	 */
	public int[] getDims(){
		return new int[]{s_size, t_size, r_size, v_size};
	}
	
	public void setDims(int[] dims){
		s_size = dims[0];
		t_size = dims[1];
		r_size = dims[2];
		if (dims.length > 3)
			v_size = dims[3];
	}
	
	/******************************
	 * Returns the midpoint of the voxel identified by <code>i</code>, where i is determined by the order
	 * s < t < r. I.e., 
	 * 
	 * @param index The absolute index of the voxel
	 */
	@Override
	public Point3f getVertex(int index){
		
		int[] voxel = getIndexAsVoxel(index);
		return getVoxelMidPoint(voxel[0], voxel[1], voxel[2]);
	}
	
	
	/*****************************************
	 * Returns the absolute index of the voxel specified by the integer array {@code [s,t,r]}.
	 * Calculated as: {@code r * s_size * t_size + t * s_size + s}.
	 * 
	 * @param voxel
	 * @return
	 */
	public int getAbsoluteIndex(int[] voxel){
		
		return voxel[2] * s_size * t_size + voxel[1] * s_size + voxel[0];
		
	}
	
	/****************************************
	 * Returns the immediate neighbours of the voxel at {@code idx}. Diagonal neighbours are
	 * not returned.
	 * 
	 * @param idx
	 * @return
	 */
	public int[] getNeighbours(int idx){
		
		int[] voxel = this.getIndexAsVoxel(idx);
		ArrayList<Integer> nbrs = new ArrayList<Integer>();
		
		// X-
		if (voxel[0] > 0)
			nbrs.add(this.getAbsoluteIndex(voxel[0]-1, voxel[1], voxel[2]));
		// X+
		if (voxel[0] < s_size-1)
			nbrs.add(this.getAbsoluteIndex(voxel[0]+1, voxel[1], voxel[2]));
		// Y-
		if (voxel[1] > 0)
			nbrs.add(this.getAbsoluteIndex(voxel[0], voxel[1]-1, voxel[2]));
		// Y+
		if (voxel[1] < t_size-1)
			nbrs.add(this.getAbsoluteIndex(voxel[0], voxel[1]+1, voxel[2]));
		// Z-
		if (voxel[2] > 0)
			nbrs.add(this.getAbsoluteIndex(voxel[0], voxel[1], voxel[2]-1));
		// Z+
		if (voxel[2] < r_size-1)
			nbrs.add(this.getAbsoluteIndex(voxel[0], voxel[1], voxel[2]+1));
		
		int[] n_array = new int[nbrs.size()];
		for (int i = 0; i < nbrs.size(); i++)
			n_array[i] = nbrs.get(i);
		
		return n_array;
	}
	
	/*****************************************
	 * Returns a voxel as an integer array of the form {s, t, r}, from the absolute index <code>index</code>.
	 * 
	 * <p>A voxel {s, t, r} is determined by:
	 * 
	 * <p>r = ceil(index / (s_size * t_size))
	 * <br>residual = index - (r - 1) * (s_size * t_size)
	 * <br>t = ceil(residual / (s_size))
	 * <br>s = residual - (t - 1) * (s_size)
	 * 
	 * @param index The absolute index of the voxel
	 * @return the voxel as an integer array
	 */
	public int[] getIndexAsVoxel(int index){
		
		double r = Math.floor((double)index / ((double)(s_size * t_size)));
		double residual = index - r * (double)(s_size * t_size);
		double t = Math.floor(residual / (double)s_size);
		double s = residual - t * (double)s_size;
		return new int[]{(int)s, (int)t, (int)r};
		
	}
	
	
	/************************
	 * Returns the data dimensions in the x (S) dimension
	 * 
	 * @return
	 */
	public int getSizeS(){
		return s_size;
	}
	
	/************************
	 * Returns the data dimensions in the y (T) dimension
	 * 
	 * @return
	 */
	public int getSizeT(){
		return t_size;
	}
	
	/************************
	 * Returns the data dimensions in the z (R) dimension
	 * 
	 * @return
	 */
	public int getSizeR(){
		return r_size;
	}
	
	public int getSizeV(){
		return v_size;
	}
	
	/**********************************************
	 * Returns the geometric dimension in the S-axis. Note that this is not equivalent to the
	 * bounds dimension, but the displacement from the center-points of the voxels; i.e., the
	 * bounds dimension minus a voxel.
	 * 
	 * @return
	 */
	public float getGeomS(){
		return getSDim();
//		double length = getSDim();
//		double dim = (float)s_size;
//		double step = length / (dim + 1);
//		return (float)(step * dim);
	}
	
	/**********************************************
	 * Returns the geometric dimension in the T-axis. Note that this is not equivalent to the
	 * bounds dimension, but the displacement from the center-points of the voxels; i.e., the
	 * bounds dimension minus a voxel.
	 * 
	 * @return
	 */
	public float getGeomT(){
		return getTDim();
//		double length = getTDim();
//		double dim = (double)t_size;
//		double step = length / (dim + 1);
//		return (float)(step * dim);
	}
	
	/**********************************************
	 * Returns the geometric dimension in the R-axis. Note that this is not equivalent to the
	 * bounds dimension, but the displacement from the center-points of the voxels; i.e., the
	 * bounds dimension minus a voxel.
	 * 
	 * @return
	 */
	public float getGeomR(){
		return getRDim();
//		double length = getRDim();
//		double dim = (float)r_size;
//		double step = length / (dim + 1);
//		return (float)(step * dim);
	}
	
	/**********************************************
	 * Returns the geometric dimensions of this grid. Note that this is not equivalent to the
	 * bounds dimensions, but the displacement from the center-points of the voxels; i.e., the
	 * bounds dimensions minus a voxel.
	 * 
	 * @return
	 */
	public float[] getGeomDims(){
		return new float[]{getGeomS(), getGeomT(), getGeomR()};
	}
	
	/**********************************************
	 * Returns the total number of data elements in this grid.
	 * 
	 * @return
	 */
	public int getSize(){
		return getSizeR() * getSizeS() * getSizeT() * getSizeV();
	}
	
	/**********************************************
	 * Returns the origin of this grid. Note that this is not equivalent to the base point of its bounds,
	 * but rather the center point of the origin voxel; i.e., the base point plus half a voxel.
	 * 
	 * @return
	 */
	public Point3f getOrigin(){
		float[] geom = getGeomDims();
		Point3f origin = this.getBasePt();
		float s_s = geom[0] / (float)s_size;
		float s_t = geom[1] / (float)t_size;
		float s_r = geom[2] / (float)r_size;
		
		Vector3f v_s = new Vector3f(sAxis);
		Vector3f v_t = new Vector3f(tAxis);
		Vector3f v_r = new Vector3f(rAxis);
		
		v_s.normalize();
		v_s.scale(s_s * 0.5f);
		origin.add(v_s);
		v_t.normalize();
		v_t.scale(s_t * 0.5f);
		origin.add(v_t);
		v_r.normalize();
		v_r.scale(s_r * 0.5f);
		origin.add(v_r);
		
//		Vector3f offset = new Vector3f(geom[0] / (float)s_size * 0.5f,
//									   geom[1] / (float)t_size * 0.5f,
//									   geom[2] / (float)r_size * 0.5f);
//		origin.add(offset);
		return origin;
	}
	
	
	public void setFromGrid(Grid3D grid){
		
		setFromBox(grid);
		
		this.s_size = grid.s_size;
		this.t_size = grid.t_size;
		this.r_size = grid.r_size;
		this.v_size = grid.v_size;
		
	}
	
	@Override
	public Object clone(){
		return new Grid3D(this);
	}
	
//	/************
//	 * Returns a copy of this box's S-axis (analogous to X-axis), plus one voxel. This is useful for
//	 * determining bounds dimensions, by adding half a voxel to each end.
//	 * 
//	 * @return
//	 */
//	public Vector3f getSBoundsAxis(){
//		Vector3f v = new Vector3f(sAxis);
//		v.scale((float)(getSizeS() + 1) / (float)getSizeS());
//		return v;
//	}
//	
//	/************
//	 * Returns a copy of this box's T-axis (analogous to Y-axis), plus one voxel. This is useful for
//	 * determining bounds dimensions, by adding half a voxel to each end.
//	 * 
//	 * @return
//	 */
//	public Vector3f getTBoundsAxis(){
//		Vector3f v = new Vector3f(tAxis);
//		v.scale((float)(getSizeT() + 1) / (float)getSizeT());
//		return v;
//	}
//	
//	/************
//	 * Returns a copy of this box's R-axis (analogous to Z-axis), plus one voxel. This is useful for
//	 * determining bounds dimensions, by adding half a voxel to each end.
//	 * 
//	 * @return
//	 */
//	public Vector3f getRBoundsAxis(){
//		Vector3f v = new Vector3f(rAxis);
//		v.scale((float)(getSizeR() + 1) / (float)getSizeR());
//		return v;
//	}
	
	
	public int getY(int y){
		return t_size - y - 1;
	}
	
	public void setBounds(Box3D bounds){
		//this.bounds = new Box3D(bounds);
		setFromBox(bounds);
	}
	
	/**********************************
	 * Determines the grid coordinate of point <code>p</code>, expressed in world coordinates.
	 * 
	 * @param p
	 * @return
	 */
	public Point3f getGridCoordinate(Point3f p){
		Vector3f v = new Vector3f(p);
		Point3f voxel_base_point = this.getOrigin();
		v.sub(p, voxel_base_point);
		Matrix4f M = this.getBasisTransform();
		v = GeometryFunctions.transform(v, M);
		float x_geom = getGeomS();
		float y_geom = getGeomT();
		float z_geom = getGeomR();
		double vox_x = x_geom / (double)(s_size);
		double vox_y = y_geom / (double)(t_size);
		double vox_z = z_geom / (double)(r_size);
		
		return new Point3f((float)(v.x / vox_x),
						   (float)(v.y / vox_y),
						   (float)(v.z / vox_z));
		
	}
	
	/**************************
	 * Returns a matrix which will transform a vector expressed in world (model) coordinates
	 * to one expressed with respect to this box's coordinate space. Note that basis vectors
	 * must be orthonormal for this to return a valid coordinate.
	 * 
	 * This function also applies a scale to the transformation, defined as the voxel size; thus
	 * scale_r = geom_r / size_r.
	 * 
	 * @return
	 */
	public Matrix4f getScaledBasisTransform(){
		Matrix4f M = getBasisTransform();
		Matrix4f S = new Matrix4f();
		
		float scale_s = this.getGeomS() / this.getSizeS();
		float scale_t = this.getGeomT() / this.getSizeT();
		float scale_r = this.getGeomR() / this.getSizeR();
		
		S.m00 = scale_s;
		S.m11 = scale_t;
		S.m22 = scale_r;
		S.m33 = 1;
		
		M.mul(S);
		return M;
	}
	
	/**********************************
	 * Determines the voxel which encloses point <code>p</code>. Returns an array containing
	 * the coordinates of the voxel (i, j, k), or <code>null</code> if <code>p</code> is
	 * not contained by this volume.
	 * 
	 * @param p
	 * @return
	 */
	public int[] getEnclosingVoxel(Point3f p){
		Vector3f v = new Vector3f(p);
		Point3f voxel_base_point = this.getBasePt(); // getVoxelMidPoint(0, 0, 0);
		
		v.sub(p, voxel_base_point);
		//voxel_base_point.sub(this.getOrigin());
		//v.sub(voxel_base_point);
		
		Matrix4f M = this.getBasisTransform();
		
		v = GeometryFunctions.transform(v, M);
		
		float x_geom = getGeomS();
		float y_geom = getGeomT();
		float z_geom = getGeomR();
		
		double vox_x = x_geom / (double)(s_size);
		double vox_y = y_geom / (double)(t_size);
		double vox_z = z_geom / (double)(r_size);
		
		int i = (int)Math.floor(v.x / vox_x);
		if (i < 0 && ++i < 0) 
			return null; 
		if (i >= s_size && --i >= s_size) 
			return null; 
		int j = (int)Math.floor(v.y / vox_y);
		if (j < 0 && ++j < 0) 
			return null;
		if (j >= t_size && --j >= t_size) 
			return null;
		int k = (int)Math.floor(v.z / vox_z);
		if (k < 0 && ++k < 0) 
			return null; 
		if (k >= r_size && --k >= r_size) 
			return null; 
		
		return new int[]{i, j, k};
	}
		
//	/******************************
//	 * Returns the midpoint at the specified voxel. 
//	 * 
//	 * @param x
//	 * @param y
//	 * @param z
//	 */
//	public Point3f getVoxelMidPoint(int x, int y, int z){
//		
//		float x_voxel = getSDim() / s_size;
//		float y_voxel = getTDim() / t_size;
//		float z_voxel = getRDim() / r_size;
//		
//		Vector3f v = new Vector3f(sAxis);
//		v.normalize();
//		v.scale(x_voxel * (0.5f + x));
//		Vector3f offset = new Vector3f(v); 
//		v = new Vector3f(tAxis);
//		v.normalize();
//		v.scale(y_voxel * (0.5f + y));
//		offset.add(v); 
//		v = new Vector3f(rAxis);
//		v.normalize();
//		v.scale(z_voxel * (0.5f + z));
//		offset.add(v);
//		
//		Point3f p = new Point3f(basePt);
//		p.add(offset);
//		return p;
//		
//	}
	
	/*********************************
	 * Returns the geometric midpoint of {@code voxel}
	 * 
	 * @param i
	 * @param j
	 * @param k
	 * @return
	 */
	public Point3f getVoxelMidPoint(int[] voxel){
		return getVoxelMidPoint(voxel[0], voxel[1], voxel[2]);
	}
	
	/*********************************
	 * Returns the geometric midpoint of the grid coordinate i, j, k
	 * 
	 * @param i
	 * @param j
	 * @param k
	 * @return
	 */
	public Point3f getVoxelMidPoint(int i, int j, int k){
		float voxel_x = getGeomS() / (s_size);
		float voxel_y = getGeomT() / (t_size);
		float voxel_z = getGeomR() / (r_size);
		
		// Add 1/2 voxel, then add full voxel widths along all three
		// dimensions.
		
		Point3f p = this.getOrigin();
		Vector3f v1 = new Vector3f(sAxis);
		v1.normalize();
		v1.scale(i * voxel_x);
		p.add(v1);
		v1.set(tAxis);
		v1.normalize();
		v1.scale(j * voxel_y);
		p.add(v1);
		v1.set(rAxis);
		v1.normalize();
		v1.scale(k * voxel_z);
		p.add(v1);
		return p;
	}
	
	/**********************************************
	 * Returns the grid coordinate at the absolute index {@code idx}
	 * 
	 * @param idx
	 * @return
	 */
	public Point3f getVoxelMidPoint(int idx){
		int[] voxel = getIndexAsVoxel(idx);
		if (voxel == null) return null;
		return getVoxelMidPoint(voxel[0], voxel[1], voxel[2]);
	}
	
	/********************
	 * Finds transform using the origin (center point of first voxel), rather than the base point.
	 * 
	 * @return
	 */
	public Matrix4f getGridBasisTransform(){
		
		Point3f origin = this.getOrigin();
		
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
		t.m03 = origin.x;
		t.m10 = cosTX; // * -basePt.x;
		t.m11 = cosTY; // * -basePt.y;
		t.m12 = cosTZ; // * -basePt.z;
		t.m13 = origin.y;
		t.m20 = cosRX; // * -basePt.x;
		t.m21 = cosRY; // * -basePt.y;
		t.m22 = cosRZ; // * -basePt.z;
		t.m23 = origin.z;
		t.m33 = 1.0f;
		//Vector3f v = new Vector3f(-basePt.x, -basePt.y, -basePt.z);
		//Transform3D t3d = new Transform3D(t);
		
		return new Matrix4f(t);
		
	}
	
	/*******************************
	 * Test whether p is contained by this grid's bounds.
	 *
	public boolean contains(Point3f p){
		return bounds.contains(p);
	}
	*/
	
	/*****************************************
	 * Returns two points (min coords, max coords) which represent a subset of this grid which 
	 * is bounded by the geometric box specified by p1 and p2 (i.e., a voxel must be contained 
	 * within the box, or must enclose its boundary). Adds 1 to the max indices, such
	 * that the includes indices are < max.
	 * 
	 * @param p1 min point
	 * @param p2 max point
	 * @return
	 */
	public int[] getSubGrid(Point3f p1, Point3f p2){
		
		boolean in1 = this.contains(p1);
		boolean in2 = this.contains(p2);
		
		if (!in1 && !in2) return null;
		
		if (!in1){
			p1 = GeometryFunctions.getMaxPt(p1, getMinVoxelMidPt());
			p1 = GeometryFunctions.getMinPt(p1, getMaxVoxelMidPt());
		}
		
		if (!in2){
			p2 = GeometryFunctions.getMaxPt(p2, getMinVoxelMidPt());
			p2 = GeometryFunctions.getMinPt(p2, getMaxVoxelMidPt());
		}
		
		//Given correct p1, p2, find points in grid
		int[] c1 = getEnclosingVoxel(p1);
		int[] c2 = getEnclosingVoxel(p2);
		
		if (c1 == null || c2 == null){
			InterfaceSession.log("Error getting subgrid for points " + p1.toString() + " and  " + p2.toString() + ".",
								 LoggingType.Debug);
			return null;
		}
		
		//return as min_x,y,z, max_x+1,y+1,z+1
		return new int[]{Math.min(c1[0], c2[0]), 
						 Math.min(c1[1], c2[1]), 
						 Math.min(c1[2], c2[2]),
						 Math.min(s_size, Math.max(c1[0], c2[0]) + 1), 
						 Math.min(t_size, Math.max(c1[1], c2[1]) + 1), 
						 Math.min(r_size, Math.max(c1[2], c2[2]) + 1)};
		
	}
	
	public Point3f getMinVoxelMidPt(){
		return getVoxelMidPoint(0, 0 ,0);
	}
	
	public Point3f getMaxVoxelMidPt(){
		return getVoxelMidPoint(s_size - 1, t_size - 1, r_size - 1);
	}
	
	/**************************************
	 * Returns absolute index of this coordinate, which is given by:
	 * k * xSize * ySize + j * xSize + i (x changes fastest)
	 * @param i
	 * @param j
	 * @param k
	 * @return
	 */
	public int getAbsoluteIndex(int i, int j, int k){
		return k * s_size * t_size + j * s_size + i;
		
	}
	
	// ******************************* XML STUFF *************************************
	
	
	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) throws SAXException{
		
		if (localName.equals(this.getLocalName())){
			xml_encoding = XMLFunctions.getEncodingForStr(attributes.getValue("encoding"));
			s_size = Integer.valueOf(attributes.getValue("s_size"));
			t_size = Integer.valueOf(attributes.getValue("t_size"));
			r_size = Integer.valueOf(attributes.getValue("r_size"));
			v_size = Integer.valueOf(attributes.getValue("v_size"));
			return;
		}
		
		super.handleXMLElementStart(localName, attributes, type);
		
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
	
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab+1);
		
		writer.write(_tab + "<" + getLocalName() + "\n" + 
					 _tab2 + "encoding='" + XMLFunctions.getEncodingStr(options.encoding) + "'\n" +
					 _tab2 + "s_size='" + getSizeS() + "'\n" +
					 _tab2 + "t_size='" + getSizeT() + "'\n" +
					 _tab2 + "r_size='" + getSizeR() + "'\n" +
					 _tab2 + "v_size='" + getSizeV() + "'\n" +
					 _tab + ">\n");
		writeCoords(tab + 1, writer, options, progress_bar);
		writer.write("\n" + _tab + "</" + getLocalName() + ">");
		
	}
	
	@Override
	protected void writeCoords(int tab, Writer writer, XMLOutputOptions options, 
							   ProgressUpdater progress_bar) throws IOException{

		// Write coords as a box, not as a grid
		Box3D box = new Box3D(this);
		box.writeCoords(tab, writer, options, progress_bar);
		
	}
	
	@Override
	public String getLocalName() {
		return "Grid3D";
	}
	
	
}