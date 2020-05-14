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

package mgui.geometry.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;

import org.jogamp.java3d.Transform3D;
import org.jogamp.vecmath.AxisAngle4f;
import org.jogamp.vecmath.Matrix4d;
import org.jogamp.vecmath.Matrix4f;
import org.jogamp.vecmath.Point2d;
import org.jogamp.vecmath.Point2f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Quat4d;
import org.jogamp.vecmath.Tuple3d;
import org.jogamp.vecmath.Tuple3f;
import org.jogamp.vecmath.Vector2d;
import org.jogamp.vecmath.Vector2f;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;
import org.jogamp.vecmath.Vector4d;
import org.jogamp.vecmath.Vector4f;

import mgui.geometry.Box3D;
import mgui.geometry.Cube3D;
import mgui.geometry.LineSegment2D;
import mgui.geometry.LineSegment3D;
import mgui.geometry.Plane3D;
import mgui.geometry.Polygon2D;
import mgui.geometry.Polygon3D;
import mgui.geometry.Rect2D;
import mgui.geometry.Shape;
import mgui.geometry.Shape2D;
import mgui.geometry.Shape3D;
import mgui.geometry.Sphere3D;
import mgui.geometry.Triangle2D;
import mgui.geometry.Triangle3D;
import mgui.geometry.Vector2D;
import mgui.geometry.Vector3D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.Utility;
import mgui.numbers.MguiFloat;
import Jama.Matrix;
import Jama.SingularValueDecomposition;

/****************************
 * Utility class providing a library of common geometric functions. All functions in this class are blocking,
 * meaning that if they need to run in a worker thread, they should be called from that thread.
 * 
 * <p>TODO: All functions should use <code>double</code> for calculations, to minimize rounding errors.
 *  
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * 
 */

public class GeometryFunctions extends Utility {
	
	/** Acceptable margin of error ***/
	public static double error = 0.000000001;	
	
	/**************************
	 * Transforms <code>vector</code> with the given affine transformation matrix <code>matrix</code>.
	 * Ignores the last column.
	 * 
	 * @status experimental
	 * @param vector
	 * @param matrix
	 * @return the result
	 */
	public static Vector3d transform(Tuple3d vector, Matrix4d matrix){
		Vector3d v = new Vector3d(vector);
		v.x = matrix.m00 * vector.x + matrix.m01 * vector.y + matrix.m02 * vector.z ;
		v.y = matrix.m10 * vector.x + matrix.m11 * vector.y + matrix.m12 * vector.z ;
		v.z = matrix.m20 * vector.x + matrix.m21 * vector.y + matrix.m22 * vector.z ;
		return v;
	}
	
	/**************************
	 * Transforms <code>vector</code> with the given affine transformation matrix <code>matrix</code>.
	 * 
	 * @status experimental
	 * @param vector
	 * @param matrix
	 * @return the result
	 */
	public static Vector3f transform(Tuple3f tuple, Matrix4d matrix){
		return new Vector3f(transform(new Vector3d(tuple), matrix));
	}
	
	/**************************
	 * Transforms <code>vector</code> with the given affine transformation matrix <code>matrix</code>.
	 * 
	 * @status experimental
	 * @param vector
	 * @param matrix
	 * @return the result
	 */
	public static Vector3f transform(Tuple3f vector, Matrix4f matrix){
		return new Vector3f(transform(new Vector3d(vector), new Matrix4d(matrix)));
	}
	
	/**************************
	 * Transforms <code>shape</code> with the given affine transformation matrix <code>matrix</code>.
	 * 
	 * @status approved
	 * @param shape
	 * @param matrix
	 * @return
	 */
	public static boolean transform(Shape shape, Matrix4d matrix){
		return transform(shape, matrix, null);
	}
	
	/**************************
	 * Transforms <code>shape</code> with the given affine transformation matrix <code>matrix</code>.
	 * 
	 * @status approved
	 * @param shape
	 * @param matrix
	 * @return
	 */
	public static boolean transform(Shape shape, Matrix4f matrix){
		return transform(shape, matrix, null);
	}
	
	/**************************
	 * Transforms <code>shape</code> with the given affine transformation matrix <code>matrix</code>.
	 * Updates <code>progress_bar</code> with its progress.
	 * 
	 * @status approved
	 * @param shape
	 * @param matrix
	 * @param progress_bar
	 * @return
	 */
	public static boolean transform(Shape shape, Matrix4f matrix, ProgressUpdater progress){
		Matrix4d matrix2 = new Matrix4d(matrix);
		return transform(shape, matrix2, progress);
	}
	
	/**************************
	 * Transforms <code>shape</code> with the given affine transformation matrix <code>matrix</code>.
	 * Updates <code>progress_bar</code> with its progress.
	 * 
	 * @status approved
	 * @param shape
	 * @param matrix
	 * @param progress_bar
	 * @return
	 */
	public static boolean transform(Shape shape, Matrix4d matrix, ProgressUpdater progress){
		
		float[] coords = shape.getCoords();
		if (progress != null){
			progress.setMinimum(0);
			progress.setMaximum(coords.length);
			}
		
		Transform3D T = new Transform3D(matrix);
		
		//try to use special transform if implemented
		if (shape.transform(matrix)) return true;
		
		for (int i = 0; i < coords.length; i += 3){
			Point3d p = new Point3d(coords[i], coords[i + 1], coords[i + 2]);
			T.transform(p);
			coords[i] = (float)p.x;
			coords[i + 1] = (float)p.y;
			coords[i + 2] = (float)p.z;
			if (progress != null)
				progress.update(i);
			}
		
		shape.setCoords(coords);
		return true;
	}
	
	/**************************
	 * Transforms <code>shape</code> with the given affine transformation Jama matrix <code>matrix</code>.
	 * 
	 * @status approved
	 * @param shape
	 * @param matrix
	 * @return
	 */
	public static boolean transform(Shape shape, Matrix matrix){
		return transform(shape, getJamaMatrixAsMatrix4d(matrix), null);
	}
	
	/**************************
	 * Transforms <code>shape</code> with the given affine transformation Jama matrix <code>matrix</code>.
	 * Updates <code>progress_bar</code> with its progress.
	 * 
	 * @status approved
	 * @param shape
	 * @param matrix
	 * @param progress_bar
	 * @return
	 */
	public static boolean transform(Shape shape, Matrix matrix, ProgressUpdater progress){
		return transform(shape, getJamaMatrixAsMatrix4d(matrix), progress);
	}
	
	public static Matrix4d getJamaMatrixAsMatrix4d(Matrix matrix){
		
		Matrix4d m = new Matrix4d();
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++)
				m.setElement(i, j, matrix.get(i, j));
		
		return m;
	}
	
	
	/***************************
	 * Determines whether <code>tuple</code> is of length zero.
	 * 
	 * @param tuple
	 * @return
	 */
	public static boolean isZero(Tuple3f tuple){
		return compareFloat(tuple.x, 0) == 0 &&
		   	   compareFloat(tuple.y, 0) == 0 &&
		   	   compareFloat(tuple.z, 0) == 0;
	}
	
	/********************************
	 * Returns the angle that a 2D vector makes with the x-axis. Angle will be expressed on the range
	 * [0, 2Pi].
	 * 
	 * @status approved
	 * @param vector - 2D vector to test
	 * @return the angle that <code>vector</code> makes with the x-axis
	 */
	public static float getVectorAngle(Vector2f vector){
		
		//special cases
		if (compareFloat(vector.x, 0) == 0){
			int c = compareFloat(vector.y, 0);
			if (c == 0) return Float.NaN;
			if (c > 0) return (float)(Math.PI / 2.0);
			return (float)(3 * Math.PI / 2.0);
			}
		
		if (compareFloat(vector.y, 0) == 0){
			int c = compareFloat(vector.x, 0);
			if (c == 0) return Float.NaN;
			if (c > 0) return (0);
			return (float)Math.PI;
			}
		
		float angle = (float)Math.atan(vector.y / vector.x);
		if (vector.x < 0) angle += Math.PI;
		if (angle < 0) angle += 2.0 * Math.PI;
		return angle;
		
	}
	
	/********************************
	 * Projects <code>vector</code> onto plane <code>plane</code> along its normal. Returns a 2D point in the
	 * coordinate system of <code>plane</code>.
	 * 
	 * See http://www.euclideanspace.com/maths/geometry/elements/plane/lineOnPlane/index.htm
	 * 
	 * @status experimental
	 * @param vector
	 * @param normal
	 * @return the resulting projected vector 2D
	 */
	public static Vector2f getProjectedToPlane2D(Vector3f vector, Plane3D plane){
		return getProjectedToPlane2D(vector, plane.getNormal(), plane);
	}
	
	/********************************
	 * Projects <code>vector</code> onto plane <code>plane</code> along its normal. Returns a 2D point in the
	 * coordinate system of <code>plane</code>.
	 * 
	 * See http://www.euclideanspace.com/maths/geometry/elements/plane/lineOnPlane/index.htm
	 * 
	 * @status experimental
	 * @param vector
	 * @param normal
	 * @return the resulting projected vector 2D
	 */
	public static Vector2f getProjectedToPlane2D(Vector3f vector, Vector3f proj, Plane3D plane){
		
		Point3f P = new Point3f(plane.origin);
		P.add(vector);
		return new Vector2f(getProjectedPoint(P, proj, plane));
		
	}
	
	/********************************
	 * Projects <code>vector</code> onto a plane with normal <code>normal</code>.
	 * 
	 * See http://www.euclideanspace.com/maths/geometry/elements/plane/lineOnPlane/index.htm
	 * 
	 * @status experimental
	 * @param vector
	 * @param normal
	 * @return the resulting projection
	 */
	public static Vector3f getProjectedToPlane(Vector3f vector, Vector3f normal){
		
		Vector3f B = new Vector3f(normal);
		B.normalize();
		Vector3f AxB = new Vector3f();
		AxB.cross(normal, vector);
		AxB.normalize();
		Vector3f V = new Vector3f();
		V.cross(B, AxB);
		V.scale(vector.length() * (float)Math.sin(vector.angle(normal)));
		return V;
		
	}
	
	/********************************
	 * Projects <code>nodes</code> onto <code>plane</code> and returns the 2D points.
	 * 
	 */
	public static ArrayList<Point2f> getProjectedToPlane(ArrayList<Point3f> nodes, Plane3D plane){
		ArrayList<Point2f> nodes2D = new ArrayList<Point2f>();
		for (Point3f p : nodes)
			nodes2D.add(getProjectedPoint(p, plane));
		return nodes2D;
	}
	
	/********************************
	 * Projects <code>tri</code> onto <code>plane</code> and returns the 2D triangle.
	 * 
	 */
	public static Triangle2D getProjectedToPlane(Triangle3D tri, Plane3D plane){
		ArrayList<Point2f> nodes2D = new ArrayList<Point2f>();
		for (Point3f p : tri.getVertices())
			nodes2D.add(getProjectedPoint(p, plane));
		return new Triangle2D(nodes2D);
	}
	
	/*********************************
	 * Computes the mid-point of the line segment defined by <code>p1</code> and
	 * <code>p2</code>.
	 * 
	 * @status approved
	 * @param p1
	 * @param p2
	 * @return the mid-point
	 */
	public static Point3f getMidPt(Point3f p1, Point3f p2){
		Vector3f v = new Vector3f(p2);
		v.sub(p1);
		v.scale(0.5f);
		Point3f p = new Point3f(p1);
		p.add(v);
		return p;
	}
	
	/**************************
	 * Determines whether <code>tri</code> is an obtuse triangle.
	 * 
	 * @status experimental
	 * @param tri
	 * @return <code>true</code> if <code>tri</code> is obtuse, <code>false</code> otherwise. 
	 */
	public static boolean isObtuse(Triangle3D tri){
		Vector3f v1 = new Vector3f(tri.B);
		v1.sub(tri.A);
		Vector3f v2 = new Vector3f(tri.C);
		v1.sub(tri.A);
		if (v1.angle(v2) > Math.PI) return true;
		v1.set(tri.A);
		v1.sub(tri.B);
		v2.set(tri.C);
		v2.sub(tri.B);
		if (v1.angle(v2) > Math.PI) return true;
		v1.set(tri.A);
		v1.sub(tri.C);
		v2.set(tri.B);
		v2.sub(tri.C);
		if (v1.angle(v2) > Math.PI) return true;
		return false;
	}
	
	/************************
	 * Translates <code>shape</code> by the specified vector.
	 * 
	 * @status approved
	 * @param shape
	 * @param x
	 * @param y
	 * @param z
	 */
	public static void translate(Shape3D shape, Vector3d v){
		translate(shape, v.x, v.y, v.z);
	}
	
	/************************
	 * Translates <code>shape</code> by the specified x, y, and z distances.
	 * 
	 * @status approved
	 * @param shape
	 * @param x
	 * @param y
	 * @param z
	 */
	public static void translate(Shape3D shape, double x, double y, double z){
		float[] nodes = shape.getCoords();
		for (int i = 0; i < nodes.length / 3; i++){
			nodes[(i * 3)] += x;
			nodes[(i * 3) + 1] += y;
			nodes[(i * 3) + 2] += z;
			}
		shape.setCoords(nodes);
	}
	
	/****************************
	 * Rotates this shape about a base point <code>base_point</code> and an axis <code>axis</code> 
	 * by <code>angle</code> radians.
	 * 
	 * @param shape 		Shape to rotate
	 * @param base_point	Point about which to rotate
	 * @param axis			Rotation axis
	 * @param angle			Angle, in radians
	 */
	public static void rotate(Shape3D shape, Point3f base_point, Vector3f axis, double angle){
		rotate(shape, new Point3d(base_point), new Vector3d(axis), angle);
	}
	
	/****************************
	 * Rotates this shape about a base point <code>base_point</code> and an axis <code>axis</code> 
	 * by <code>angle</code> radians.
	 * 
	 * @param shape 		Shape to rotate
	 * @param base_point	Point about which to rotate
	 * @param axis			Rotation axis
	 * @param angle			Angle, in radians
	 */
	public static void rotate(Shape3D shape, Point3d base_point, Vector3d axis, double angle){
		Matrix4d transform = getRotationTransform(new Vector3d(axis), angle);
		Vector3d t = new Vector3d(base_point);
		t.scale(-1);
		translate(shape, t);
		transform(shape, transform);
		t.scale(-1);
		translate(shape, t);
	}
	
	/*******************************
	 * Rotates <code>shape</code> about <code>base_point</code> by <code>angle</code> radians.
	 * 
	 * @param shape
	 * @param base_point
	 * @param angle
	 */
	public static void rotate(Shape2D shape, Point2f base_point, double angle){
		rotate(shape, new Point2d(base_point), angle);
	}
	
	/*******************************
	 * Rotates <code>shape</code> about <code>base_point</code> by <code>angle</code> radians.
	 * 
	 * @param shape
	 * @param base_point
	 * @param angle
	 */
	public static void rotate(Shape2D shape, Point2d base_point, double angle){
		ArrayList<Point2f> vertices = shape.getVertices();
		for (int i = 0; i < vertices.size(); i++){
			Point2d p = new Point2d(vertices.get(i));
			Point2d p2 = new Point2d();
			p2.x = p.x * Math.cos(angle) - p.y * Math.sin(angle);
			p2.y = p.x * Math.sin(angle) + p.y * Math.cos(angle);
			vertices.set(i, new Point2f(p2));
			}
		shape.setVertices(vertices);
	}
	
	/***********************
	 * Returns the endpoint of a vector starting at <code>startPt</code>.
	 * 
	 * @status approved
	 * @param startPt
	 * @param vector
	 * @return the calculated endpoint.
	 */
	public static Point2f getVectorEndpoint(Point2f startPt, Vector2f vector){
		return new Point2f(startPt.x + vector.x, startPt.y + vector.y);
	}
	
	/************************
	 * Flips <code>v</code>.
	 * 
	 * @status approved
	 * @param v
	 * @return the flipped vector
	 */
	public static Vector3f flipVector(Vector3f v){
		Vector3f r = new Vector3f();
		r.scale(-1);
		//r.x = -v.x;
		//r.y = -v.y;
		//r.z = -v.z;
		return r;
	}

	/*************************
	 * Determines the signed angle that <code>v1</code> makes with <code>v2</code>.
	 * 
	 * @status experimental
	 * @param v1
	 * @param v2
	 * @return the signed angle.
	 */
	public static double getSignedAngle(Vector3d v1, Vector3d v2){
		
		Vector3d axis = new Vector3d();
		axis.cross(v1, v2);
		double perp_dot = axis.length();
		
		return Math.atan2(perp_dot, v1.dot(v2));
		
	}
	
	public static Quat4d getQuaternion(Vector3d axis, double angle){
		double sin_a = Math.sin( angle / 2 );
		double cos_a = Math.cos( angle / 2 );
		//use a vector so we can call normalize
		Vector4d q = new Vector4d();
		q.x = (axis.x * sin_a);
		q.y = (axis.y * sin_a);
		q.z = (axis.z * sin_a);
		q.w = cos_a;
		//It is best to normalize the quaternion
		//so that only rotation information is used
		q.normalize();
		//convert to a Quat4f and return
		return new Quat4d( q );
		}
	
	/************************
	 * Returns a list of cross-products corresponding the the vertices of <code>thisPoly</code>.
	 * 
	 * @status approved
	 * @param thisPoly
	 * @return list of cross-products
	 */
	public static ArrayList<MguiFloat> getCrossProducts(Polygon2D thisPoly){
		if (thisPoly.vertices.size() < 3) return null;
		ArrayList<Point2f> nodes = thisPoly.vertices;
		ArrayList<MguiFloat> crosses = new ArrayList<MguiFloat>(nodes.size());
		
		for (int i = 0; i < nodes.size(); i++){
			int i0 = i - 1;
			if (i0 < 0) i0 = nodes.size() - 1;
			int i2 = i + 1;
			if (i2 == nodes.size()) i2 = 0;
			crosses.add(new MguiFloat((nodes.get(i0).x - nodes.get(i).x) * (nodes.get(i2).y - nodes.get(i).y) -
					 				(nodes.get(i0).y - nodes.get(i).y) * (nodes.get(i2).x - nodes.get(i).x)));
			}
		return crosses;
	}
	
	
	/*****************************************
	 * Returns the projection of v onto u. Result is a scalar multiple of u.
	 * <p>
	 * See http://everything2.com/index.pl?node_id=1390247
	 * </p>
	 * 
	 * @status approved
	 * @param u
	 * @param v
	 * @return the projected vector.
	 */
	public static Vector3f getProjectedVector(Vector3f v, Vector3f u){
		
		float scale = u.dot(v) / u.lengthSquared();
		Vector3f v2 = new Vector3f(u);
		v2.scale(scale);
		return v2;
		
	}
	
	/*******************
	 * Determines a maximum point max_x, max_y, max_z from <code>shape</code>. 
	 * 
	 * @status approved
	 * @param shape
	 * @return the maximum point
	 */
	public static Point3f getMaxPt(Shape3D shape){
		Box3D box = shape.getBoundBox();
		return box.getMaxPt();
	}
	
	/*******************
	 * Determines a minimum point min_x, min_y, min_z from <code>shape</code>. 
	 * 
	 * @status approved
	 * @param shape
	 * @return the minimum point
	 */
	public static Point3f getMinPt(Shape3D shape){
		Box3D box = shape.getBoundBox();
		return box.getMinPt();
	}
	
	/*******************
	 * Determines a minimum point min_x, min_y, min_z from two points. 
	 * 
	 * @status approved
	 * @param p1
	 * @param p2
	 * @return the minimum point
	 */
	public static Point3f getMinPt(Point3f p1, Point3f p2){
		Point3f p = new Point3f();
		p.x = Math.min(p1.x, p2.x);
		p.y = Math.min(p1.y, p2.y);
		p.z = Math.min(p1.z, p2.z);
		return p;
	}
	
	/*******************
	 * Determines a maximum point max_x, max_y, max_z from two points. 
	 * 
	 * @status approved
	 * @param p1
	 * @param p2
	 * @return the maximum point
	 */
	public static Point3f getMaxPt(Point3f p1, Point3f p2){
		Point3f p = new Point3f();
		p.x = Math.max(p1.x, p2.x);
		p.y = Math.max(p1.y, p2.y);
		p.z = Math.max(p1.z, p2.z);
		return p;
	}
	
	/********************
	 * Determines the area of triangle <code>tri</code>.
	 * 
	 * @status approved
	 * @param tri
	 * @return the computed area.
	 */
	public static float getArea(Triangle3D tri){
		//is half the magnitude of the cross product of two edges
		Vector3f v1 = new Vector3f(tri.B);
		v1.sub(tri.A);
		Vector3f v2 = new Vector3f(tri.C);
		v2.sub(tri.A);
		v1.cross(v1, v2);
		return 0.5f * v1.length();
	}
	
	/********************
	 * Determines the area of triangle defined by three points.
	 * 
	 * @status approved
	 * @param p1
	 * @param p2
	 * @param p3
	 * @return the computed area.
	 */
	public static float getArea(Point3f p1, Point3f p2, Point3f p3){
		//is half the magnitude of the cross product of two edges
		Vector3f v1 = new Vector3f(p2);
		v1.sub(p1);
		Vector3f v2 = new Vector3f(p3);
		v2.sub(p1);
		v1.cross(v1, v2);
		return 0.5f * v1.length();
	}
	
	/*************************
	 * Determines from a list of vertex-wise cross-products whether a polygon is
	 * convex or not (i.e., concave).    
	 * 
	 * @param experimental
	 * @param crossProducts
	 * @return <code>true</code> if polygon is convex, <code>false</code> if it is concave.
	 */
	public static boolean isConvex(ArrayList<MguiFloat> crossProducts){
		if (crossProducts.size() < 3) return false;
		boolean firstPos = (crossProducts.get(0).getValue() > 0);
		boolean isPos = (crossProducts.get(1).getValue() > 0);
		if (isPos != firstPos) return false;
		
		for (int i = 0; i < crossProducts.size(); i++){
			isPos = (crossProducts.get(1).getValue() > 0);
			if (isPos != firstPos) return false;
			}
		return true;
	}
	
	/**************************
	 * Determines from a list of vertex-wise cross-products whether a polygon is
	 * clockwise or not (i.e., counter-clockwise).
	 * 
	 * @status experimental
	 * @param crossProducts
	 * @return <code>true</code> if polygon is clockwise, <code>false</code> if it is counter-clockwise.
	 */
	public static boolean isClockwise(ArrayList<MguiFloat> crossProducts){
		if (crossProducts.size() < 3) return false;
		int posCount = 0;
		int negCount = 0;
		
		for (int i = 0; i < crossProducts.size(); i++){
			if (crossProducts.get(i).getValue() > 0)
				posCount += 1;
			if (crossProducts.get(i).getValue() < 0)
				negCount += 1;
			}
		return posCount > negCount;
	}
	
	/**********************************
	 * Determines whether a polygon is clockwise or not (i.e., counter-clockwise).
	 * 
	 * @status experimental
	 * @param thisPoly
	 * @return <code>true</code> if polygon is clockwise, <code>false</code> if it is counter-clockwise.
	 */
	public static boolean isClockwise(Polygon2D thisPoly){
		return getArea(thisPoly) > 0;
	}
	
	/**********************************
	 * Determines whether a triangle defined by three 3D points is clockwise or not (i.e., counter-clockwise).
	 * 
	 * <p>NB --NOT YET IMPLEMENTED--
	 * 
	 * @status not implemented
	 * @param p1
	 * @param p2
	 * @param p3
	 * @return <code>true</code> if polygon is clockwise, <code>false</code> if it is counter-clockwise.
	 */
	public static boolean isClockwise(Point3f p1, Point3f p2, Point3f p3){
		/**@TODO implement this **/
		return false;
	}
	
	/**********************************
	 * Determines whether a triangle defined by three 2D points is clockwise or not (i.e., counter-clockwise).
	 * 
	 * @status experimental
	 * @param p1
	 * @param p2
	 * @param p3
	 * @return <code>true</code> if polygon is clockwise, <code>false</code> if it is counter-clockwise.
	 */
	public static boolean isClockwise(Point2f p1, Point2f p2, Point2f p3){
		//return getCrossProductDist(p1, p2, p3) > 0;
		Polygon2D poly = new Polygon2D();
		poly.vertices.add(p1);
		poly.vertices.add(p2);
		poly.vertices.add(p3);
		return isClockwise(poly);
	}
	
	/***********************************
	 * Determines the area of <code>thisPoly</code>. Area will be signed, depending on whether
	 * polygon is clockwise or not.
	 * 
	 * @status experimental
	 * @param thisPoly
	 * @return the signed area of polygon
	 */
	public static float getArea(Polygon2D thisPoly){
		if (thisPoly.vertices.size() < 3) return 0;
		float area = 0;
		int j;
		
		for (int i = 0; i < thisPoly.vertices.size(); i++){
			j = i + 1;
			if (j == thisPoly.vertices.size()) j = 0;
			area += (thisPoly.vertices.get(i).x * thisPoly.vertices.get(j).y);
			area -= (thisPoly.vertices.get(j).x * thisPoly.vertices.get(i).y);
		}
		
		return 0.5f * area;
	}
	
	/****
	 * Reverses the direction of this polygon (CW -> CCW or vice versa)
	 * 
	 * @status approved
	 * @param thisPoly polygon to reverse
	 * @return new Polygon2D object which is the reversed version of thisPoly
	 */
	
	public static Polygon2D getReversePolygon(Polygon2D thisPoly){
		Polygon2D retPoly = new Polygon2D();
		int j;
		
		for (int i = 0; i < thisPoly.vertices.size(); i++){
			j = thisPoly.vertices.size() - i;
			if (i == 0)
				retPoly.vertices.add((Point2f)thisPoly.vertices.get(i).clone());
			else
				retPoly.vertices.add((Point2f)thisPoly.vertices.get(j).clone());
		}
		return retPoly;
	}
	
	/****************************
	 * Determines the nearest neighbour of <code>p</code> in the list <code>nodes</code>.
	 * 
	 * @status approved
	 * @param p
	 * @param nodes
	 * @return the index of the nearest neighbour
	 */
	public static int getNearestNeighbour(Point2f p, ArrayList<Point2f> nodes){
		float min = Float.MAX_VALUE;
		float dist;
		int index = -1;
		for (int i = 0; i < nodes.size(); i++){
			dist = p.distance(nodes.get(i)); 
			if (dist < min){
				index = i;
				min = dist;
				}
			}
		return index;
	}
	
	public static double getDistance(Point2f pt1, Point2f pt2){
		if (pt1 == null || pt2 == null) return Double.NaN;
		return pt1.distance(pt2);
		//return (Math.sqrt(Math.pow((pt1.x - pt2.x), 2) + Math.pow((pt1.y - pt2.y), 2)));
	}
	
	/***************************
	 * Determines the angle between vectors AB and BC.
	 * 
	 * @param approved
	 * @param A
	 * @param B
	 * @param C
	 * @return the calculated angle
	 */
	public static double getAngle(Point2f A, Point2f B, Point2f C){
		if (A == null || B == null || C == null) return 0;
		//pt1 is central node
		Vector2d v1 = new Vector2d(A.x - B.x, A.y - B.y);
		Vector2d v2 = new Vector2d(C.x - B.x, C.y - B.y);
		return v1.angle(v2);
	}
	
	public static double getCrossProductDist(Point2f pt1, Point2f pt2, Point2f pt3){
		if (pt1 == null || pt2 == null || pt3 == null) return 0;
		//pt1 is central node
		return ((pt1.x - pt2.x) * (pt3.y - pt2.y) - (pt3.x - pt2.x) * (pt1.y - pt2.y));
	}
	
	public static double getDotProduct(Point2f pt1, Point2f pt2, Point2f pt3){
		if (pt1 == null || pt2 == null || pt3 == null) return 0;
		//pt1 is central node
		return ((pt1.x - pt2.x) * (pt3.x - pt2.x) + (pt1.y - pt2.y) * (pt3.y - pt2.y));
	}
	
	public static boolean isOblique(double thisAngle){
		thisAngle = getMinimalAngle(thisAngle);
		//return thisAngle > Math.PI / 2;
		return thisAngle > Math.PI / 2 && thisAngle < Math.PI * 3 / 2;
	}
	
	public static double getMinimalAngle(double thisAngle){
		while (thisAngle > Math.PI * 2)
			thisAngle -= Math.PI * 2;
		while (thisAngle < 0)
			thisAngle += Math.PI * 2;
		return thisAngle;
	}
	
	public static Vector3d getCrossProduct(Vector3d v1, Vector3d v2){
		return new Vector3d(v1.y * v2.z - v1.z * v2.y,
							v1.z * v2.x - v1.x * v2.z,
							v1.x * v2.y - v1.y * v2.x);
	}
	
	public static Vector3f getCrossProduct(Point3f p1, Point3f p2, Point3f p3){
		Vector3f v1 = new Vector3f();
		Vector3f v2 = new Vector3f();
		v1.sub(p2, p1);
		v2.sub(p3, p2);
		return getCrossProduct(v1, v2);
	}
	
	public static Vector3f getCrossProduct(Vector3f v1, Vector3f v2){
		return new Vector3f(v1.y * v2.z - v1.z * v2.y,
							v1.z * v2.x - v1.x * v2.z,
							v1.x * v2.y - v1.y * v2.x);
	}
	
	public static Rect2D getScaledShape(Point2f scalePt, Rect2D thisRect, double scaleVal){
		Rect2D retRect = new Rect2D();
		retRect.corner1.x = (float)(scalePt.x + ((thisRect.corner1.x - scalePt.x) * scaleVal));
		retRect.corner1.y = (float)(scalePt.y + ((thisRect.corner1.y - scalePt.y) * scaleVal));
		retRect.corner2.x = (float)(scalePt.x + ((thisRect.corner2.x - scalePt.x) * scaleVal));
		retRect.corner2.y = (float)(scalePt.y + ((thisRect.corner2.y - scalePt.y) * scaleVal));
		return retRect;
	}
	
	public static double getAngleDiff(double a1, double a2){
		double thisDiff = getMinimalAngle(Math.abs(a1 - a2));
		if (thisDiff > Math.PI) thisDiff = (2 * Math.PI) - thisDiff;
		return thisDiff;
	}
	
	public static Point2f getCenterOfGravity(Shape2D s){
		//sum x's and y's
		float sumX = 0, sumY = 0;
		ArrayList<Point2f> nodes = s.getVertices();
		if (nodes.size() < 1) return null;
		
		for (int i = 0; i < nodes.size(); i++){
			if (!Float.isNaN(nodes.get(i).x)){
				sumX += nodes.get(i).x;
				sumY += nodes.get(i).y;
				}
		}
		//divide by no. nodes
		return new Point2f(sumX / nodes.size(), sumY / nodes.size());
	}
	
	
	/****
	 * Returns an angle tangent to the given node angles in a polygon 
	 * @param a1 angle at previous node
	 * @param a2 angle at this node
	 * @return tangential angle
	 */
	
	public static double getAngleTangent(double a1, double a2){
		double theta1 = getMinimalAngle(a1);
		double theta2 = getMinimalAngle(a2);
		double theta_bisect = (theta1 + theta2) / 2;
		if (Math.abs(theta1 - theta2) > Math.PI)
			theta_bisect -= Math.PI;
	
		if (theta1 > theta2)
			return theta_bisect + (Math.PI / 2);
		return theta_bisect - (Math.PI / 2);
	}

	/*******************************
	 * Determines the mid-point of the line segment defined by <code>pt1</code> and
	 * <code>pt2</code>, at the position <code>pos</code>, which must range from 0.0
	 * (i.e., at <code>pt1<code> to 1.0 (i.e., at <code>pt2</code>).
	 * 
	 * @status approved
	 * @param pt1
	 * @param pt2
	 * @param pos
	 * @return <code>null</code> if <code>pos</code> is not in the range [0,1]; 
	 * the mid-point otherwise.
	 */
	public static Point2f getMidPoint(Point2f pt1, Point2f pt2, double pos){
		if (pos < 0 || pos > 1) return null;
		Point2f midPt = new Point2f();
		double deltaX = pt2.x - pt1.x;
		double deltaY = pt2.y - pt1.y;
		//midPt.x = (pt1.x + pt2.x + deltaX * pos) / 2;
		//midPt.y = (pt1.y + pt2.y + deltaY * pos) / 2;
		midPt.x = (float)(pt1.x + deltaX * pos);
		midPt.y = (float)(pt1.y + deltaY * pos);
		return midPt;
	}
	
	public static Point2f getEndpoint(Point2f startPt, double angle, double length){
		return new Point2f((float)(startPt.x + Math.cos(angle) * length),
						   (float)(startPt.y + Math.sin(angle) * length));
	}
	
	/*********************
	 * Given two nodes N1 and N2 and their control points C1, C2, return a point along
	 * the cubic spline function defined by these points, at distance t (where t = 0 at
	 * N1 and t = 1 at N2) along the curve.
	 * 
	 * @status experimental
	 * @param N1 first node
	 * @param C1 first control point
	 * @param N2 second node
	 * @param C2 second control point
	 * @param t distance parameter
	 * @return Point2f representing the point on the cubic spline curve defined by these
	 * parameters.
	 */
	public static Point2f getCubicSplinePt(Point2f N1, Point2f C1, 
										   Point2f N2, Point2f C2,
										   double t){
		
		Point2f retPoint = new Point2f();
	
		retPoint.x = (float)(Math.pow((1 - t), 3) * N1.x + 
							 3 * t * Math.pow((1 - t), 2) * C1.x +
 		 					 3 * Math.pow(t, 2) * (1 - t) * C2.x + 
 		 					 Math.pow(t, 3) * N2.x);
		retPoint.y = (float)(Math.pow((1 - t), 3) * N1.y + 
							 3 * t * Math.pow((1 - t), 2) * C1.y +
							 3 * Math.pow(t, 2) * (1 - t) * C2.y + 
							 Math.pow(t, 3) * N2.y);
		return retPoint;
	}
	
	public static double getAngle(Point2f p1, Point2f p2){
		double theta = Math.atan((p2.y - p1.y) / (p2.x - p1.x));
		if (p2.x < p1.x) theta += Math.PI;
		return getMinimalAngle(theta);
	}
	
	/************************
	 * Returns the angle between two triangular faces, defined such that AB is a common edge.
	 * 
	 * @status experimental
	 * @param A
	 * @param B
	 * @param C
	 * @param D
	 * @return angle between faces ABC and ABD
	 */
	public static double getAngle(Point3f A, Point3f B, Point3f C, Point3f D){
		//strategy is to find the perpendicular vectors E1C and E2D
		//and return the angle between them
		
		Point3d E1, E2;
		double theta1, theta2;
		double BE1, AE2;
		Vector3d AB, BA, BC, AD;
		Vector3d E1C, E2D;
		
		AB = new Vector3d(B);
		AB.sub(new Vector3d(A));
		BA = new Vector3d(A);
		BA.sub(new Vector3d(B));
		BC = new Vector3d(C);
		BC.sub(new Vector3d(B));
		AD = new Vector3d(D);
		AD.sub(new Vector3d(A));
		
		theta1 = BA.angle(BC);
		theta2 = AB.angle(AD);
		
		BE1 = BC.length() * (float)Math.cos(theta1);
		AE2 = AD.length() * (float)Math.cos(theta2);
		
		BA.normalize();
		BA.scale(BE1);
		
		E1 = new Point3d(B);
		//if angle is obtuse, go the other way
		if (theta1 <= Math.PI / 2.0)
			E1.add(BA);
		else
			E1.sub(BA);
		
		E1C = new Vector3d(C);
		E1C.sub(E1);
		
		AB.normalize();
		AB.scale(AE2);
		
		E2 = new Point3d(A);
		//if angle is obtuse, go the other way
		if (theta1 <= Math.PI / 2.0)
			E2.add(AB);
		else
			E2.sub(AB);
		
		E2D = new Vector3d(D);
		E2D.sub(E2);
		
		return E1C.angle(E2D);
	}
	
	//returns the normalized vector represented by x- and y-axis rotation angles
	public static Vector3d getVectorFromAngles(double angleX, double angleY) {
		//set x = 1
		double x = 1, y, z;
		z = Math.tan(angleY) * x;
		y = Math.tan(angleX) * z;
		Vector3d retVect = new Vector3d(x, y, z);
		retVect.normalize();
		return retVect;
	}
	
	
	//returns angle of this vector about x-axis
	public static double getXAngle(Vector3d v){
		double theta = Math.atan(v.y/v.z);
		if (v.z < 0) theta += Math.PI;
		if (v.z == 0)
			if (v.y > 0)
				theta = Math.PI / 2.0;
			else
				theta = 3.0 * Math.PI / 2.0;
		else if (v.y == 0)
			if (v.z > 0) 
				theta = 0;
			else
				theta = Math.PI;
		if (v.y == 0 && v.z == 0) theta = 0;
		return getMinimalAngle(theta);
	}
	
	//returns angle of this vector about y-axis
	//note Z-axis is zero
	public static double getYAngle(Vector3d v){
		double theta = Math.atan(v.x/v.z);
		if (v.z < 0) theta += Math.PI;
		if (v.z == 0)
			if (v.x > 0)
				theta = Math.PI / 2.0;
			else
				theta = 3 * Math.PI / 2.0;
		else if (v.x == 0)
			if (v.z > 0) 
				theta = 0;
			else
				theta = Math.PI;
		if (v.x == 0 && v.z == 0) theta = 0;
		return getMinimalAngle(theta);
	}
	
	//returns angle of this vector about z-axis
	public static double getZAngle(Vector3d v){
		double theta = Math.atan(v.x/v.y);
		if (v.y < 0) theta += Math.PI;
		if (v.y == 0)
			if (v.x > 0)
				theta = Math.PI / 2.0;
			else
				theta = 3.0 * Math.PI / 2.0;
		else if (v.x == 0)
			if (v.y > 0) 
				theta = 0;
			else
				theta = Math.PI;
		if (v.y == 0 && v.x == 0) theta = 0;
		return getMinimalAngle(theta);
	}
	
	/***********************************
	 * Returns angle of <code>vector</code> with x-axis.
	 * 
	 * @status experimental
	 * @param vector
	 * @return
	 */
	public static float getXAngle(Vector3f vector){
		double theta = Math.atan(vector.y/vector.z);
		if (vector.z < 0) theta += Math.PI;
		if (vector.z == 0) 
			theta = 0;
		else if (vector.y == 0)
			if (vector.z < 0) 
				theta = Math.PI;
			else
				theta = 0;
		return (float)getMinimalAngle(theta);
	}
	
	/***********************************
	 * Returns angle of <code>vector</code> with y-axis.
	 * 
	 * @status experimental
	 * @param vector
	 * @return
	 */
	public static float getYAngle(Vector3f vector){
		double theta = Math.atan(vector.z/vector.x);
		if (vector.x < 0) theta += Math.PI;
		if (vector.x == 0) 
			theta = 0;
		else if (vector.z == 0)
			if (vector.x > 0) 
				theta = Math.PI / 2.0;
			else
				theta = 3 * Math.PI / 2.0;
		return (float)getMinimalAngle(theta);
	}
	
	/***********************************
	 * Returns angle of <code>vector</code> with z-axis.
	 * 
	 * @status experimental
	 * @param vector
	 * @return
	 */
	public static float getZAngle(Vector3f vector){
		double theta = Math.atan(vector.x/vector.y);
		if (vector.y < 0) theta += Math.PI;
		if (vector.y == 0) 
			theta = 0;
		else if (vector.x == 0)
			if (vector.y > 0) 
				theta = Math.PI / 2.0;
			else
				theta = 3 * Math.PI / 2.0;
		return (float)getMinimalAngle(theta);
	}
	
	/*********************************************
	 * Returns a list of the lengths of each segment in <code>polygon</code>.
	 * 
	 * @status approved
	 * @param polygon
	 * @return a list of segment lengths, starting with segment [0,1]
	 */
	public static float[] getSegmentLengths(Polygon2D polygon){
		float[] retArray = new float[polygon.vertices.size()];
		int j;
		for (int i = 0; i < polygon.vertices.size(); i++){
			j = i + 1;
			if (j == polygon.vertices.size()) j = 0;
			retArray[i] = polygon.vertices.get(i).distance(polygon.vertices.get(j));
		}
		return retArray;	
	}
	
	public static void getVectorTransform(Matrix4d M, Vector3d v){
		double x, y, z;
		x = M.m00 * v.x + M.m01 * v.y + M.m02 * v.z + M.m03;
		y = M.m10 * v.x + M.m11 * v.y + M.m12 * v.z + M.m13;
		z = M.m20 * v.x + M.m21 * v.y + M.m22 * v.z + M.m23;
		v.x = x;
		v.y = y;
		v.z = z;
	}
	
	/********************************
	 * Creates a transformation matrix representing a rotation about <code>axis</code> by
	 * <code>angle</code> radians. 
	 * 
	 * <p>See <a href="http://en.wikipedia.org/wiki/Rotation_matrix">http://en.wikipedia.org/wiki/Rotation_matrix</a>
	 * 
	 * @param axis
	 * @param angle
	 * @return
	 */
	public static Matrix4d getRotationTransform(Vector3d axis, double angle){
		
		Vector3d u = new Vector3d(axis);
		u.normalize();
		
		double c = Math.cos(angle);
		double s = Math.sin(angle);
		
		Matrix4d matrix = new Matrix4d();
		matrix.m00 = u.x * u.x + ((1 - u.x * u.x) * c);
		matrix.m01 = (u.x * u.y * (1 - c)) - (u.z * s); 
		matrix.m02 = (u.x * u.z * (1 - c)) + (u.y * s);
		matrix.m03 = 0;
		matrix.m10 = (u.x * u.y * (1 - c)) + (u.z * s);
		matrix.m11 = u.y * u.y + ((1 - u.y * u.y) * c);
		matrix.m12 = (u.y * u.z * (1 - c)) - (u.x * s); 
		matrix.m13 = 0;
		matrix.m20 = (u.x * u.z * (1 - c)) - (u.y * s);
		matrix.m21 = (u.y * u.z * (1 - c)) + (u.x * s);
		matrix.m22 = u.z * u.z + ((1 - u.z * u.z) * c);
		matrix.m23 = 0;
		matrix.m30 = 0;
		matrix.m31 = 0;
		matrix.m32 = 0;
		matrix.m33 = 1;
		
		return matrix;
	}
	
	/********************************
	 * Determines the rotation that converts [1, 0, 0] to <code>vector</code>. 
	 * 
	 * @status experimental
	 * @param v
	 * @return
	 */
	public static Transform3D getVectorRotation(Vector3D vector){
		
		Vector3f v = new Vector3f(vector.vector);
		if (!isValidVector(v)) return null;
		v.normalize();
		v.scale(-1f);
		
        //projection of ab to xy-plane
		double abxy = Math.sqrt(v.x * v.x + v.y * v.y);
		
		//degenerate case: this is z-axis, rotate pi / 2 about y-axis
		if (compareDouble(abxy, 0) == 0){
			double angle = Math.PI / 2.0;
			if (v.z > 0) angle *= -1.0;
			Transform3D rotate = new Transform3D();
			rotate.rotX(angle);
			return rotate;
			}
		
        double ab = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
        double alphaz = 0.0;
        double x_ratio = v.x / abxy;
        
        //handle rounding errors
        if (x_ratio <= -1.0){
        	Transform3D t = new Transform3D();
        	t.rotZ(-Math.PI / 2f);
        	return t;
        }else if (x_ratio >= 1.0){
        	Transform3D t = new Transform3D();
        	t.rotZ(Math.PI / 2f);
        	return t;
        	}
        	 
        if (v.y > 0)
           alphaz = Math.PI - Math.asin(x_ratio);          
        else
           alphaz = Math.asin(x_ratio);
        
        //handle rounding errors
        double z_ratio = v.z / ab;
        if (z_ratio > 1.0)
        	z_ratio = 1.0;
        else if (z_ratio < -1.0)
        	z_ratio = -1.0;
        double alphax = -Math.asin(z_ratio);
        Transform3D temp = new Transform3D();
        Transform3D rotate = new Transform3D();      
        
        if (Double.isNaN(alphaz) || Double.isNaN(alphax))
        	x_ratio = x_ratio + 0;
       
        if (compareDouble(alphaz, 0) != 0)
        	temp.rotZ(alphaz);
        rotate.set(temp);
        if (compareDouble(alphax, 0) != 0)
        	temp.rotX(alphax);
        rotate.mul(temp);
       
        return rotate;
		
	}
	
	/***********************************************
	 * Returns an array of angles made by each node of <code>polygon</code>
	 * 
	 * @status experimental
	 * @param polygon The polygon for which to determine angles
	 * @return a list of angles, starting at the angle made for node 0
	 */
	public static float[] getNodeAngles(Polygon2D polygon){
		if (polygon.vertices.size() < 3) return null;
		float[] retArray = new float[polygon.vertices.size()];
		
		int j, k;
		for (int i = 0; i < polygon.vertices.size(); i++){
			j = i + 1;
			k = i - 1;
			if (j == polygon.vertices.size()) j = 0;
			if (k == -1) k = polygon.vertices.size() - 1;
			retArray[i] = (float)getAngle(polygon.vertices.get(k), polygon.vertices.get(i),
					polygon.vertices.get(j));
		}
		return retArray;	
	}
	
	/*************************************
	 * Determines whether line segments <code>l1</code> and <code>l2</code> intersect.
	 * 
	 * @status experimental
	 * @param l1
	 * @param l2
	 * @return <code>true</code> if they intersect, </code>false</code> if not.
	 */
	public static boolean getSegmentsIntersect(LineSegment2D l1, LineSegment2D l2){
		//segments intersect if and only if:
		//only one of acd, bcd is clockwise and
		//only one of abc, abd is clockwise
		if (isCoincident(l1.pt1, l2.pt1) || isCoincident(l1.pt1, l2.pt2) ||
			isCoincident(l1.pt2, l2.pt1) || isCoincident(l1.pt2, l2.pt2)) return false;
		
		return (isClockwise(l1.pt1, l2.pt1, l2.pt2) != isClockwise(l1.pt2, l2.pt1, l2.pt2)) &&
			   (isClockwise(l1.pt1, l1.pt2, l2.pt1) != isClockwise(l1.pt1, l1.pt2, l2.pt2));
		
	}
	
	/***********************
	 * Converts degrees to radians.
	 * 
	 * @status approved
	 * @param degAngle
	 * @return the converted value.
	 */
	public static double getDegToRad(double degAngle){
		return getMinimalAngle(Math.toRadians(degAngle));
	}
	
	/***********************
	 * Converts radians to degrees.
	 * 
	 * @status approved
	 * @param radAngle
	 * @return the converted value.
	 */
	public static double getRadToDeg(double radAngle){
		return Math.toDegrees(getMinimalAngle(radAngle));
	}
	
	/***********************
	 * Rotates <code>vector</code> by <code>angle</code>, specified in radians.
	 * 
	 * @status approved
	 * @param vector vector to rotate
	 * @param angle in radians
	 * @return rotated vector
	 */
	public static Vector2f getRotatedVector2D(Vector2f vector, double angle){
		return new Vector2f(getRotatedVector2D(new Vector2d(vector), angle));
	}
	
	/***********************
	 * Rotates <code>vector</code> by <code>angle</code>, specified in radians.
	 * 
	 * @status approved
	 * @param vector vector to rotate
	 * @param angle in radians
	 * @return rotated vector
	 */
	public static Vector2d getRotatedVector2D(Vector2d vector, double angle){
		Vector2d R = new Vector2d();
		R.x = (Math.cos(angle) * vector.x - Math.sin(angle) * vector.y);
		R.y = (Math.sin(angle) * vector.x + Math.cos(angle) * vector.y);
		return R;
	}
	
	/*************************
	 * Determines whether <code>p1</code> and </code>p2</code> coincide, within the given
	 * tolerance distance.
	 * 
	 * @status approved
	 * @param p1
	 * @param p2
	 * @param tolerance
	 * @return
	 */
	public static boolean isCoincident(Point2f p1, Point2f p2, float tolerance){
		return compareFloat(p1.x, p2.x) == 0 && compareFloat(p1.y, p2.y) == 0;
	}
	
	/*************************
	 * Determines whether <code>p1</code> and </code>p2</code> coincide, within the default
	 * tolerance distance <code>GeometryFunctions.error</code>.
	 * 
	 * @status approved
	 * @param p1
	 * @param p2
	 * @param tolerance
	 * @return
	 */
	public static boolean isCoincident(Point2f p1, Point2f p2){
		return isCoincident(p1, p2, (float)error);
	}
	
	/*************************
	 * Determines whether <code>p1</code> and </code>p2</code> coincide, within the default
	 * tolerance distance <code>GeometryFunctions.error</code>.
	 * 
	 * @status approved
	 * @param p1
	 * @param p2
	 * @param tolerance
	 * @return
	 */
	public static boolean isCoincident(Point3f p1, Point3f p2){
		return compareFloat(p1.x, p2.x) == 0 && compareFloat(p1.y, p2.y) == 0 && compareFloat(p1.z, p2.z) == 0;
	}
	
	/*************************
	 * Determines whether <code>p1</code> and </code>p2</code> coincide, within the default
	 * tolerance distance <code>GeometryFunctions.error</code>.
	 * 
	 * @status approved
	 * @param p1
	 * @param p2
	 * @param tolerance
	 * @return
	 */
	public static boolean isCoincident(Point3d p1, Point3d p2){
		return compareDouble(p1.x, p2.x) == 0 && compareDouble(p1.y, p2.y) == 0 && compareDouble(p1.z, p2.z) == 0;
	}
	
	//is angle a_i between a_1 and a_2 looking clockwise from a_1 -> a_i -> a_2?
	public static boolean isIntermediateAngleCW(double a_i, double a_1, double a_2){
		double d1 = getAngleDiffCW(a_1, a_i);
		double d2 = getAngleDiffCW(a_i, a_2);
		return (d1 + d2 < (2 * Math.PI));
	}

	public static double getAngleDiffCW(double a1, double a2){
		double d1 = a1 - a2;
		if (d1 < 0) d1 += 2 * Math.PI;
		return d1;
	}
	
	public static Box3D getUnionBounds(Box3D b1, Box3D b2){
		if (b1 == null) return b2;
		if (b2 == null) return b1;
		Point3f minPt = new Point3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		Point3f maxPt = new Point3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		ArrayList<Point3f> nodes = b1.getVertices();
		for (int j = 0; j < 2; j++){
			for (int i = 0; i < nodes.size(); i++){
				minPt.x = Math.min(minPt.x, nodes.get(i).x);
				minPt.y = Math.min(minPt.y, nodes.get(i).y);
				minPt.z = Math.min(minPt.z, nodes.get(i).z);
				maxPt.x = Math.max(maxPt.x, nodes.get(i).x);
				maxPt.y = Math.max(maxPt.y, nodes.get(i).y);
				maxPt.z = Math.max(maxPt.z, nodes.get(i).z);
				}
			nodes = b2.getVertices();
			}
		return new Box3D(minPt, new Vector3f(maxPt.x - minPt.x, 0, 0),
								new Vector3f(0, maxPt.y - minPt.y, 0),
								new Vector3f(0, 0, maxPt.z - minPt.z));
	}
	
	public static Rect2D getUnionBounds(Rect2D r1, Rect2D r2){
		//iterate through all nodes
		Rect2D bounds = new Rect2D();
		bounds.corner1.x = Float.MAX_VALUE;
		bounds.corner1.y = Float.MAX_VALUE;
		bounds.corner2.x = Float.MIN_VALUE;
		bounds.corner2.y = Float.MIN_VALUE;
		if (r1 == null && r2 == null) return bounds;
		if (r1 == null) return r2;
		if (r2 == null) return r1;
		bounds.corner1.x = Math.min(r1.getCorner(Rect2D.CNR_BL).x, 
									r2.getCorner(Rect2D.CNR_BL).x);
		bounds.corner1.y = Math.min(r1.getCorner(Rect2D.CNR_BL).y, 
									r2.getCorner(Rect2D.CNR_BL).y);
		bounds.corner2.x = Math.max(r1.getCorner(Rect2D.CNR_TR).x, 
									r2.getCorner(Rect2D.CNR_TR).x);
		bounds.corner2.y = Math.max(r1.getCorner(Rect2D.CNR_TR).y, 
									r2.getCorner(Rect2D.CNR_TR).y);
		return bounds;
	}
	
	/***********
	 * Return a 3D Cartesian coordinate for the specified 2D point in a plane parallel to
	 * refPlane, and separated from it by <dist> along its normal vector.
	 * 
	 * @param refPlane 	reference plane
	 * @param dist 		distance of this point's plane from reference plane
	 * @param point 	2D point to determine 3D coordinates for
	 * @return Point3d 	representing the location of this 2D point in 3D space
	 */
	
	public static Point3f getPointFromSection(Plane3D refPlane, float dist, Point2f point){
		
		Vector3f R = new Vector3f();
		R.scale(dist, refPlane.getNormal());
		Plane3D plane = (Plane3D)refPlane.clone();
		plane.origin.add(R);
		return getPointFromPlane(point, plane);
	}
	
	/**********
	 * @deprecated Use getPointFromPlane
	 * @param point
	 * @param plane
	 * @return
	 */
	@Deprecated
	public static Point3f getPointFromSection(Point2f point, Plane3D plane){
		return getPointFromPlane(point, plane);
	}
	
	/***********
	 * Return a set of 3D Cartesian coordinates for the specified 2D points in a plane parallel to
	 * <code>plane</code>.
	 * 
	 * @status approved
	 * @param plane reference plane
	 * @param points 2D points to determine 3D coordinates for
	 * @return array of <code>Point3f</code> representing the location of this 2D point in R3
	 */
	public static ArrayList<Point3f> getPointsFromPlane(ArrayList<Point2f> points, Plane3D plane){
		ArrayList<Point3f> points3D = new ArrayList<Point3f>();
		for (int i = 0; i < points.size(); i++)
			points3D.add(getPointFromPlane(points.get(i), plane));
		return points3D;
	}
	
	
	/*************
	 * Returns a 3D polygon which is the representation of a 2D polygon expressed on the given plane.
	 * 
	 * @status experimental
	 * @param poly2d
	 * @param plane
	 * @return
	 */
	public static Polygon3D getPolygonFromPlane(Polygon2D poly2d, Plane3D plane){
		
		ArrayList<Point3f> points = getPointsFromPlane(poly2d.getVertices(), plane);
		
		Polygon3D poly3d = new Polygon3D();
		poly3d.setVertices(points);
		
		return poly3d;
		
	}
	
	/***********
	 * Return a 3D Cartesian coordinate for the specified 2D point in a plane parallel to
	 * <code>plane</code>.
	 * 
	 * @status approved
	 * @param plane reference plane
	 * @param point 2D point to determine 3D coordinates for
	 * @return <code>Point3f</code> representing the location of this 2D point in R3
	 */
	public static Point3f getPointFromPlane(Point2f point, Plane3D plane){
		Vector3d R = new Vector3d();
		Vector3d X = new Vector3d();
		X.scale(point.x, new Vector3d(plane.xAxis));
		Vector3d Y = new Vector3d();
		Y.scale(point.y, new Vector3d(plane.yAxis));
		R.add(X);
		R.add(Y);
		Point3f retPoint = new Point3f();
		retPoint.set(plane.origin);
		retPoint.add(new Vector3f(R));
		return retPoint;
	}
	
	public static ArrayList<Point3f> getVerticesFromSection(ArrayList<Point2f> vertices, Plane3D plane){
		ArrayList<Point3f> vertices_3D = new ArrayList<Point3f>(vertices.size());
		for (int i = 0; i < vertices.size(); i++)
			vertices_3D.add(GeometryFunctions.getPointFromPlane(vertices.get(i), plane));
		return vertices_3D;
	}
	
	/************************************
	 * Returns the center of gravity of this shape, which is the mean of its coordinates. Compare to
	 * {@link getGeometricCenter}.
	 * 
	 * @param vertices
	 * @status Experimental
	 * @return
	 */
	public static Point3d getCenterOfGravity(Cube3D thisCube){
		//x = corner.x + Vector.x / 2
		if (thisCube == null) return null;
		Point3d retObj = new Point3d();
		//retObj.x = thisCube.corner.x + (thisCube.dimensions.x / 2.0);
		//retObj.y = thisCube.corner.y + (thisCube.dimensions.y / 2.0);
		//retObj.z = thisCube.corner.z + (thisCube.dimensions.z / 2.0);
		return retObj;
		
	}
	
	/************************************
	 * Returns the center of gravity of this shape, which is the mean of its coordinates. Compare to
	 * {@link getGeometricCenter}.
	 * 
	 * @param nodes
	 * @status Experimental
	 * @return
	 */
	public static Point3f getCenterOfGravity(ArrayList<Point3f> nodes){
		Point3f avPt = new Point3f(0,0,0);
		for (int i = 0; i < nodes.size(); i++)
			avPt.add(nodes.get(i));
		avPt.scale(1.0f/nodes.size());
		return avPt;
	}
	
	/************************************
	 * Returns the geometric center of this list of points, which is the center of its bounding box.
	 * 
	 * @param nodes
	 * @status Experimental
	 * @return
	 */
	public static Point3f getGeometricCenter(ArrayList<Point3f> nodes){
		if (nodes.size() == 0) return null;
		if (nodes.size() == 1) return nodes.get(1);
		Box3D box = getBoundingBox(nodes);
		return box.getCenter();
	}
	
	/************************************
	 * Returns the geometric center of this list of points, which is the center of its bounding box.
	 * 
	 * @param shape
	 * @status Experimental
	 * @return
	 */
	public static Point3f getGeometricCenter(Shape3D shape){
		return shape.getBoundBox().getCenter();
	}
	
	/************************************
	 * Returns the box (having standard axes) that bounds this set of points.
	 * 
	 * @param nodes
	 * @status Approved
	 * @return the box, or <code>null</code> if <code>nodes</code> has less than two nodes.
	 */
	public static Box3D getBoundingBox(ArrayList<Point3f> nodes){
		
		if (nodes == null) return null;
		if (nodes.size() < 2) return null;
		
		Point3f p;
		float xMin = Float.MAX_VALUE, yMin = Float.MAX_VALUE, zMin = Float.MAX_VALUE;
		float xMax = -Float.MAX_VALUE, yMax = -Float.MAX_VALUE, zMax = -Float.MAX_VALUE;
		for (int i = 0; i < nodes.size(); i++){
			p = nodes.get(i);
			if (p.x < xMin) xMin = p.x;
			if (p.y < yMin) yMin = p.y;
			if (p.z < zMin) zMin = p.z;
			if (p.x > xMax) xMax = p.x;
			if (p.y > yMax) yMax = p.y;
			if (p.z > zMax) zMax = p.z;
			}
		p = new Point3f(xMin, yMin, zMin);
		Vector3f xAxis = new Vector3f(xMax - xMin, 0 , 0);
		Vector3f yAxis = new Vector3f(0, yMax - yMin, 0);
		Vector3f zAxis = new Vector3f(0, 0, zMax - zMin);
		return new Box3D(p, xAxis, yAxis, zAxis);
		
		
	}
	
	/********************
	 * Returns a sphere that is the union of the two spheres s1 and s2
	 * 
	 * @status experimental
	 * @param s1
	 * @param s2
	 * @return
	 */
	
	public static Sphere3D getUnionSphere(Sphere3D s1, Sphere3D s2){
		if (s1 == null) return s2;
		if (s2 == null) return s1;
		//handle degenerate cases
		if (!isValidSphere(s1) && !isValidSphere(s2)) return null;
		if (!isValidSphere(s1)) return s2;
		if (!isValidSphere(s2)) return s1;
		if (isCoincident(s1.center, s2.center))
			if (s1.radius > s2.radius)
				return s1;
			else
				return s2;
		Vector3f cVect = new Vector3f();
		cVect.sub(s1.center, s2.center);	//will be pointing at s1.center
		float disp = cVect.length();
		if (s1.radius > disp + s2.radius) return s1;
		if (s2.radius > disp + s1.radius) return s2;
		cVect.normalize();
		cVect.scale(s1.radius);
		Point3f p1 = new Point3f();
		p1.add(s1.center, cVect);
		cVect.normalize();
		cVect.scale(-(s1.radius + disp + s2.radius) / 2.0f);
		p1.add(cVect);
		return new Sphere3D(p1, (s1.radius + disp + s2.radius) / 2.0f);
	}
	
	public static boolean isValidPoint(Point3f p){
		if (p == null) return false;
		return !(Float.isNaN(p.x) || Float.isNaN(p.y) || Float.isNaN(p.z) ||
				 Float.isInfinite(p.x) || Float.isInfinite(p.y) || Float.isInfinite(p.z));
	}
	
	public static boolean isValidPoint(Point2f p){
		if (p == null) return false;
		return !(Float.isNaN(p.x) || Float.isNaN(p.y) || Float.isInfinite(p.x) || Float.isInfinite(p.y));
	}
	
	public static boolean isValidVector(Vector3d v){
		if (v == null) return false;
		return !(Double.isNaN(v.x) || Double.isNaN(v.y) || Double.isNaN(v.z));
	}
	
	public static boolean isValidVector(Vector3f v){
		if (v == null) return false;
		return !(Float.isNaN(v.x) || Float.isNaN(v.y) || Float.isNaN(v.z));
	}
	
	public static boolean isValidVector(Vector2f v){
		if (v == null) return false;
		return !(Float.isNaN(v.x) || Float.isNaN(v.y));
	}
	
	public static boolean isNonZeroVector(Vector3d v){
		if (!isValidVector(v)) return false;
		return !(compareDouble(v.x, 0) == 0 && compareDouble(v.y, 0) == 0 && compareDouble(v.z, 0) == 0);
	}
	
	public static boolean isNonZeroVector(Vector3f v){
		if (!isValidVector(v)) return false;
		return !(compareFloat(v.x, 0) == 0 && compareFloat(v.y, 0) == 0 && compareFloat(v.z, 0) == 0);
	}
	
	public static boolean isValidSphere(Sphere3D s){
		if (s == null) return false;
		if (!isValidPoint(s.center)) return false;
		return !(Float.isNaN(s.radius));
	}
	
	public static boolean isValidTriangle(Triangle3D t){
		if (isCoincident(t.A, t.B) ||
			isCoincident(t.B, t.C) ||
			isCoincident(t.A, t.C) ) return false;
		return isValidPoint(t.A) &&
			   isValidPoint(t.B) &&
			   isValidPoint(t.C);
	}
	
	/********************
	 * Return a plane representing the average plane of a set of nodes
	 * note, in the degenerative case the average cross product will be
	 * zero; thus no plane will be defined. This should be handled.
	 * 
	 * @status experimental
	 * @param nodes an ArrayList of Point3f nodes for which to calculate the plane
	 * @return the average plane specified as a Vector4f
	 */
	public static Vector4f getAveragePlane(ArrayList<Point3f> nodes){
		//average point
		Point3f avPt = getCenterOfGravity(nodes);
		
		//average cross-products
		int k, m;
		Vector3f thisProd;
		Vector3f normal = new Vector3f(0,0,0);
		for (int i = 0; i < nodes.size(); i++){
			k = i + 1;
			if (k == nodes.size()) k = 0;
			m = k + 1;
			if (m == nodes.size()) m = 0;
			thisProd = getCrossProduct(nodes.get(i), nodes.get(k), nodes.get(m));
			normal.add(thisProd);
		}
		normal.scale(1.0f/nodes.size());
		
		//distance along N from origin
		Vector3f originVect = new Vector3f();
		originVect.set(avPt);
		float alpha = originVect.angle(normal);
		float d = (float)Math.cos(alpha) * originVect.length();
		return new Vector4f(avPt.x, avPt.y, avPt.z, d);
	}

	/*****************
	 * Rotate this plane about the Z axis
	 * 
	 * @status experimental
	 * @param plane to be rotated
	 * @return Vector4f representing rotated plane
	 */
	public static Vector4f getZRotation(Vector4f plane){
		//given normal vector N and Z-axis Z, calculate N' = ptZ - N
		Vector3f N = new Vector3f(plane.x, plane.y, plane.z);
		N.normalize();
		N.scale(plane.w);
		Point3f P = new Point3f();
		P.set(N);
		N.normalize();
		Vector3f Z = new Vector3f(0,0,1);
		Z.scale(1.0f / (float)Math.cos(Z.angle(N)));
		Point3f P2 = new Point3f(); 
		P2.add(P, Z);
		P2.sub(N);
		N.sub(P2, P);
		return new Vector4f(N.x, N.y, N.z, 0.0f);
	}
	
	/*****************
	 * Rotate this plane about the Y axis
	 * 
	 * @status experimental
	 * @param plane to be rotated
	 * @return Vector4f representing rotated plane
	 */
	public static Vector4f getYRotation(Vector4f plane){
		//given normal vector N and Z-axis Z, calculate N' = ptZ - N
		Vector3f N = new Vector3f(plane.x, plane.y, plane.z);
		N.normalize();
		N.scale(plane.w);
		Point3f P = new Point3f();
		P.set(N);
		N.normalize();
		Vector3f Z = new Vector3f(0,1,0);
		Z.scale(1.0f / (float)Math.cos(Z.angle(N)));
		Point3f P2 = new Point3f(); 
		P2.add(P, Z);
		P2.sub(N);
		N.sub(P2, P);
		return new Vector4f(N.x, N.y, N.z, 0.0f);
	}
	
	public static Vector4f getYRotation(Vector4f plane, float r){
		Vector3f N = new Vector3f(plane.x, plane.y, plane.z);
		Vector3f Y = new Vector3f(0,1,0);
		Vector3f A = new Vector3f();
		AxisAngle4f axis = new AxisAngle4f();
		A.cross(N, Y);
		axis.set(A, r);
		Transform3D R = new Transform3D();
		R.set(axis);
		R.transform(N);
		return new Vector4f(N.x, N.y, N.z, 0.0f); 
	}
	
	public static Vector4f getXRotation(Vector4f plane, float r){
		Vector3f N = new Vector3f(plane.x, plane.y, plane.z);
		Vector3f Y = new Vector3f(1,0,0);
		Vector3f A = new Vector3f();
		AxisAngle4f axis = new AxisAngle4f();
		A.cross(N, Y);
		axis.set(A, r);
		Transform3D R = new Transform3D();
		R.set(axis);
		R.transform(N);
		return new Vector4f(N.x, N.y, N.z, 0.0f); 
	}
	
	public static Vector4f getZRotation(Vector4f plane, float r){
		Vector3f N = new Vector3f(plane.x, plane.y, plane.z);
		Vector3f Y = new Vector3f(0,0,1);
		Vector3f A = new Vector3f();
		AxisAngle4f axis = new AxisAngle4f();
		A.cross(N, Y);
		axis.set(A, r);
		Transform3D R = new Transform3D();
		R.set(axis);
		R.transform(N);
		return new Vector4f(N.x, N.y, N.z, 0.0f); 
	}
	
	/****************************
	 * Determines whether plane <code>p</code> crosses box <code>b</code>.
	 * 
	 * @status approved
	 * @param s
	 * @param p
	 * @return <code>true</code> if it crosses, <code>false</code> otherwise
	 */
	public static boolean crossesPlane(Shape3D b, Plane3D p){
		if (b == null) return false;
		ArrayList<Point3f> nodes = b.getVertices();
		boolean isAbove = compareToPlane(nodes.get(0), p) > 0;
		//if at least one node is on opposite side of plane, this shape
		//crosses p, return true
		for (int i = 0; i < nodes.size(); i++)
			if (compareToPlane(nodes.get(i), p) > 0 != isAbove) return true;
		return false;
	}
	
	/****************************
	 * Determines whether point pt is located above plane p.
	 * 
	 * @deprecated use compareToPlane
	 * @param pt
	 * @param p
	 * @return
	 */
	@Deprecated
	public static boolean isAbovePlane(Point3f pt, Plane3D p){
		
		return compareToPlane(pt, p) > 0;
		
	}
	
	/***********************************
	 * Determines the position of <code>pt</code> with respect to <code>plane</code>.
	 * 
	 * @status approved
	 * @param pt
	 * @param plane
	 * @return -1 if <code>pt</code> is below plane, 0 if it is on plane (within the error limits), 
	 * and 1 if it is above plane
	 */
	public static int compareToPlane(Point3f pt, Plane3D plane){
		return compareToPlane(new Point3d(pt), plane);
	}
	
	/***********************************
	 * Determines the position of <code>pt</code> with respect to <code>plane</code>.
	 * 
	 * @status approved
	 * @param pt
	 * @param plane
	 * @return -1 if <code>pt</code> is below plane, 0 if it is on plane, and 1 if it is above plane
	 */
	public static int compareToPlane(Point3d pt, Plane3D plane){
		Vector3d to_point = new Vector3d(pt);
		to_point.sub(new Point3d(plane.origin));
		double distance = to_point.dot(new Vector3d(plane.getNormal()));
		return compareDouble(distance , 0);
			
	}
	
	/******************************************
	 * Multiplies <code>v</code> by <code>M</code>, using only the first 3 x 3 subset of <code>M</code>.
	 * 
	 * @status Experimental
	 * @param v
	 * @param M
	 * @return
	 */
	public static Vector3f getMatrixProduct(Vector3f v, Matrix4f M){
		Vector3f r = new Vector3f();
		r.x = v.x * M.m00 + r.y * M.m01 + r.z * M.m02;
		r.y = v.x * M.m10 + r.y * M.m11 + r.z * M.m12;
		r.z = v.x * M.m20 + r.y * M.m21 + r.z * M.m22;
		return r;
	}
	
	/*****************************
	 * Returns a plane which is orthogonal to <code>plane</code>, with a y-axis defined by
	 * <code>line</code>.
	 * 
	 * @status experimental
	 * @param plane
	 * @param line
	 * @return
	 */
	public Plane3D getOrthogonalPlane(Plane3D plane, LineSegment3D line) {
		Vector3f yAxis = new Vector3f(line.getVertex(0));
		yAxis.sub(line.getVertex(1));
		Vector3f normal = new Vector3f();
		normal.cross(plane.getNormal(), yAxis);
		normal.normalize();
		yAxis.normalize();

		return Plane3D.getPlaneFromNormalAndY(line.getVertex(0), normal, yAxis);
	}
	
	/***************************
	 * Determines whether the given line segment lies in <code>plane</code>.
	 * 
	 * @status experimental
	 * @param pt
	 * @param v
	 * @param p
	 * @return
	 */
	public static boolean isInPlane(Point3f pt, Vector3f v, Plane3D plane){
		if (!isInPlane(pt, plane)) return false;
		Point3f pt2 = new Point3f();
		pt2.add(pt, v);
		return isInPlane(pt2, plane);
	}
	
	/***************************
	 * Determines whether the given line segment lies in <code>plane</code>.
	 * 
	 * @status experimental
	 * @param pt
	 * @param v
	 * @param p
	 * @return
	 */
	public static boolean isInPlane(Point3d pt, Vector3d v, Plane3D plane){
		if (!isInPlane(pt, plane)) return false;
		Point3d pt2 = new Point3d();
		pt2.add(pt, v);
		return isInPlane(pt2, plane);
	}
	
	/*****************************
	 * Determines whether point <code>pt</code> lies in plane <code>plane</code>
	 * 
	 * @status approved
	 * @param pt
	 * @param plane
	 * @return <code>true</code> if <code>pt</code> lies in <code>plane</code>, 
	 * <code>false</code> otherwise.
	 */
	public static boolean isInPlane(Point3f pt, Plane3D plane){
		return getDistance(pt, plane) < error;	
	}
	
	/*****************************
	 * Determines whether point <code>pt</code> lies in <code>plane</code>
	 * 
	 * @status approved
	 * @param pt
	 * @param plane
	 * @return <code>true</code> if <code>pt</code> lies in <code>plane</code>, 
	 * <code>false</code> otherwise.
	 */
	public static boolean isInPlane(Point3d pt, Plane3D plane){
		return getDistance(pt, plane) < error;	
	}
	
	/*****************************
	 * Determines whether shape s lies in plane p
	 * 
	 * @status experimental
	 * @param pt
	 * @param p
	 * @return
	 */
	public static boolean isInPlane(Shape3D s, Plane3D p){
		ArrayList<Point3f> nodes = s.getVertices();
		if (nodes.size() == 0) return false;
		for (int i = 0; i < nodes.size(); i++)
			if (!isInPlane(nodes.get(i), p)) return false;
		return true;	
	}
	
	/***************************
	 * Determines distance from point <code>pt</code> to plane <code>plane</code>. The sign of the result
	 * specifies on which side of the plane the point is located; i.e., with respect to the direction of its 
	 * normal vector.
	 * 
	 * @status experimental
	 * @param pt
	 * @param plane
	 * @return the calculated distance.
	 */	
	public static float getSignedDistance(Point3f pt, Plane3D plane){
		
		if (GeometryFunctions.isCoincident(pt, plane.origin)) return 0;
		Vector3f R = new Vector3f(pt);
		R.sub(plane.origin);
		return R.dot(plane.getNormal());
		
	}
	
	/***************************
	 * Determines distance from point <code>pt</code> to plane <code>plane</code>.
	 * 
	 * @status experimental
	 * @param pt
	 * @param plane
	 * @return the calculated distance.
	 */	
	public static double getSignedDistance(Point3d pt, Plane3D plane){
		
		if (GeometryFunctions.isCoincident(pt, new Point3d(plane.origin))) return 0;
		Vector3d R = new Vector3d(pt);
		R.sub(new Point3d(plane.origin));
		return R.dot(new Vector3d(plane.getNormal()));
		
	}
	
	/****************************************
	 * Calculates the absolute distance from <code>pt</code> to <code>plane</code>
	 * 
	 * @param pt
	 * @param plane
	 * @status Experimental
	 * @return
	 */
	public static float getDistance(Point3f pt, Plane3D plane){
		return Math.abs(getSignedDistance(pt, plane));
		//return compareToPlane(pt, plane) * getDistance(pt, plane);
	}
	
	/****************************************
	 * Calculates the distance from <code>pt</code> to <code>plane</code>, signed with respect to its
	 * normal.
	 * 
	 * @param pt
	 * @param plane
	 * @status Experimental
	 * @return
	 */
	public static double getDistance(Point3d pt, Plane3D plane){
		return Math.abs(getSignedDistance(pt, plane));
		//return compareToPlane(pt, plane) * getDistance(pt, plane);
	}
	
	/*************************
	 * Project point <code>pt</code> onto plane <code>plane</code>, along <code>plane</code>'s 
	 * normal vector. Return a Point2f whose coordinates are relative to <code>plane</code>'s base 
	 * point and x- and y-axes.
	 * 
	 * @status approved
	 * @param pt
	 * @param plane
	 * @return
	 */
	public static Point2f getProjectedPoint(Point3f pt, Plane3D plane){
		if (isCoincident(pt, plane.origin)) return new Point2f(0, 0);
		Vector3f R = new Vector3f();
		R.sub(pt, plane.origin);
		float deltaX = (float)Math.cos(R.angle(plane.xAxis)) * R.length();
		float deltaY = (float)Math.cos(R.angle(plane.yAxis)) * R.length();
		if (Double.isNaN(deltaX) || Double.isNaN(deltaY))
			return null;
		return new Point2f(deltaX/plane.getAxisX().length(), deltaY/plane.getAxisY().length());
		//return new Point2f(deltaX, deltaY);
	}
	
	/*************************
	 * Project point <code>pt</code> onto plane <code>plane</code>, along <code>plane</code>'s 
	 * normal vector. Return a Point2f whose coordinates are relative to <code>plane</code>'s base 
	 * point and x- and y-axes.
	 * 
	 * @status approved
	 * @param pt
	 * @param plane
	 * @return
	 */
	public static Point2d getProjectedPoint(Point3d pt, Plane3D plane){
		//if (pt.equals(p.planePt)) return new Point2f(0, 0);
		Point3d origin = new Point3d(plane.origin);
		if (isCoincident(pt, origin)) return new Point2d(0, 0);
		Vector3d R = new Vector3d();
		R.sub(pt, origin);
		double deltaX = Math.cos(R.angle(new Vector3d(plane.xAxis))) * R.length();
		double deltaY = Math.cos(R.angle(new Vector3d(plane.yAxis))) * R.length();
		if (Double.isNaN(deltaX) || Double.isNaN(deltaY))
			return null;
		return new Point2d(deltaX, deltaY);
	}
	
	/*************************
	 * Project point <code>pt</code> onto plane <code>plane</code>, along <code>plane</code>'s 
	 * normal vector. Return a Point2f whose coordinates are relative to <code>plane</code>'s base 
	 * point and x- and y-axes.
	 * 
	 * @status experimental
	 * @param pt
	 * @param plane
	 * @return
	 */
	public static Point2f getProjectedPoint(Point3f pt, Vector3f proj, Plane3D plane){
		Point3f p3d = getProjectedPoint3D(pt, proj, plane);
		Vector3f v = new Vector3f(p3d);
		v.sub(plane.origin);
		float x_dist = v.dot(plane.xAxis);
		float y_dist = v.dot(plane.yAxis);
		return new Point2f(x_dist, y_dist);
	}

	/*************************
	 * Project point <code>pt</code> onto plane <code>plane</code>, along <code>plane</code>'s 
	 * normal vector.
	 * 
	 * @status experimental
	 * @param pt
	 * @param plane
	 * @return Point3f
	 */
	public static Point3f getProjectedPoint3D(Point3f pt, Plane3D plane){
		
		return getProjectedPoint3D(pt, plane.getNormal(), plane);
		
	}
	
	/*************************
	 * Project point <code>pt</code> onto plane <code>plane</code>, along vector <code>v</code>.
	 * 
	 * @status experimental
	 * @param pt
	 * @param plane
	 * @return Point3f
	 */
	public static Point3f getProjectedPoint3D(Point3f pt, Vector3f v, Plane3D plane){
		
		Vector3f P = new Vector3f(pt);
		P.sub(plane.origin);
		Vector3f proj = new Vector3f(v);
		proj.normalize();
		
		//flip projection vector if necessary
		if (P.angle(proj) > Math.PI / 2.0) proj.scale(-1);
		
		//project P onto projection vector to get projection distance
		proj.scale(P.dot(proj));
		Point3f proj_pt = new Point3f(pt);
		proj_pt.add(proj);
		
		return pt;
	}
	
	/*******************
	 * Returns -1 if a < b, 0 if a = b, +1 if a > b,  within the limits of {@code error}.
	 * @param a
	 * @param b
	 * @return
	 */
	public static int compareFloat(float a, float b){
		return compareFloat(a, b, error);
	}
	
	/*******************
	 * Returns -1 if a < b, 0 if a = b, +1 if a > b,  within the limits of {@code error}.
	 * @param a
	 * @param b
	 * @return
	 */
	public static int compareFloat(float a, float b, double error){
		if (Math.abs(a - b) < error) 
			return 0;
		if (a > b) return 1;
		return -1;
	}
	
	/*******************
	 * Returns -1 if a < b, 0 if a = b, +1 if a > b, within the limits of {@code error}.
	 * @param a
	 * @param b
	 * @return
	 */
	public static int compareDouble(double a, double b){
		return compareDouble(a,b,error);
	}
	
	/*******************
	 * Returns -1 if a < b, 0 if a = b, +1 if a > b, within the limits of {@code error}.
	 * @param a
	 * @param b
	 * @return
	 */
	public static int compareDouble(double a, double b, double error){
		if (Math.abs(a - b) < error) 
			return 0;
		if (a > b) return 1;
		return -1;
	}
	
	//TODO: make more efficient, i.e., plane sweep algorithm...
	public static boolean[][] getSegmentsIntersect(Polygon2D poly1, Polygon2D poly2){
		
		boolean[][] intersects = new boolean[poly1.vertices.size()][poly2.vertices.size()];
		
		//currently doing this by brute force
		for (int i = 0; i < poly1.vertices.size(); i++){
			int i1 = i + 1; if (i1 == poly1.vertices.size()) i1 = 0;
			for (int j = 0; j < poly2.vertices.size(); j++){
				int j1 = j + 1; if (j1 == poly2.vertices.size()) j1 = 0;
				intersects[i][j] = crosses(new LineSegment2D(poly1.vertices.get(i), poly1.vertices.get(i1)),
										   new LineSegment2D(poly2.vertices.get(j), poly2.vertices.get(j1)));
				}
			}
		
		return intersects;
	}
	
	/*************************************************************
	 * Calculates a line segment where <code>vector</code> crosses <code>rect</code>. Treats
	 * <code>vector</code> as an infinite line. Returns <code>null</code> if </code>vector</code>
	 * does not cross <code>rect</code>.
	 * 
	 * @param vector
	 * @param rect
	 * @return
	 */
	public static LineSegment2D getIntersectionSegment(Vector2D vector, Rect2D rect){
		
		if (!crosses(vector, rect, true)) return null;
		
		//intersects at two sides
		Point2f p0 = null;
		LineSegment2D line = vector.asLineSegment(); 
		ArrayList<LineSegment2D> sides = rect.getSides();
		
		for (int i = 0; i < 4; i++){
			Point2f p = getIntersectionPoint(line, sides.get(i), false, true);
			if (p != null)
				if (p0 == null)
					p0 = p;
				else
					return new LineSegment2D(p0, p);
			}
		
		//if we get here we did something wrong...
		return null;
	}
	
	/************************************************************
	 * Determines whether <code>vector</code> crosses <code>rect</code>. Vector is treated as a line segment.
	 * 
	 * @param vector
	 * @param rect
	 * @return
	 */
	public static boolean crosses(Vector2D vector, Rect2D rect){
		return crosses(vector, rect, false);
	}
	
	/************************************************************
	 * Determines whether <code>vector</code> crosses <code>rect</code>. The <code>is_infinite</code> flag
	 * indicates whether <code>vector</code> should be treated as a line or a segment.
	 * 
	 * @param vector
	 * @param rect
	 * @return
	 */
	public static boolean crosses(Vector2D vector, Rect2D rect, boolean is_infinite){
		
		Vector2D v2 = new Vector2D(vector);
		
		if (!is_infinite) return crosses(v2.asLineSegment(), rect);
		
		Point3f O = new Point3f(vector.start.x, vector.start.y, 0);
		Vector3f L = new Vector3f(vector.vector.x, vector.vector.y, 0);
		Vector3f A = new Vector3f(1, 0, 0);
		if (A.equals(L)) A.set(0, 1, 0);
		
		ArrayList<Point2f> corners = rect.getVertices();
		Vector3f[] CPs = new Vector3f[4];
		
		for (int i = 0; i < 4; i++){
			Vector3f P = new Vector3f(corners.get(i).x, corners.get(i).y, 0);
			P.sub(O);
			if (!isNonZeroVector(P)) return true;
			if (L.equals(O)) return true;
			Vector3f CP = new Vector3f();
			CP.cross(L, P);
			//if CP = (0, 0 ,0), this line intersects point 
			if (!isNonZeroVector(CP)) return true;
			//is this CP on different side of L?
			for (int j = 0; j < i; j++)
				if (CP.angle(CPs[j]) > Math.PI / 2.0) return true;
			CPs[i] = CP;
			
			}
	
		return false;
		
	}
	
	public static boolean crosses(LineSegment2D segment, Rect2D rect){
		
		ArrayList<LineSegment2D> sides = rect.getSides();
		for (int i = 0; i < 4; i++)
			if (crosses(sides.get(i), segment)) return true;
		
		return false;
	}
	
	/*************************************************************************
	 * Determines whether the spaces defined by these boxes intersect.
	 * 
	 * @param box1
	 * @param box2
	 * @return
	 */
	public static boolean crosses(Box3D box1, Box3D box2){
		
		ArrayList<Point3f> nodes = box1.getVertices();
		for (int i = 0; i < nodes.size(); i++)
			if (box2.contains(nodes.get(i))) return true;
		nodes = box2.getVertices();
		for (int i = 0; i < nodes.size(); i++)
			if (box1.contains(nodes.get(i))) return true;
		
		return false;
	}
	
	/*************************************************************
	 * Determines whether the two planes are parallel.
	 * 
	 * @status experimental
	 * @param plane1
	 * @param plane2
	 * @return
	 */
	public static boolean isParallel(Plane3D plane1, Plane3D plane2){
		return compareFloat(plane1.getNormal().angle(plane2.getNormal()), 0) == 0;
	}
	
	/*************************************************************
	 * Determines the line of intersection between two planes, and returns the result in
	 * the 2D coordinate system of <code>plane2</code>. If the planes are parallel, returns
	 * <code>null</code>. 
	 * 
	 * @param plane1
	 * @param plane2
	 * @return
	 */
	public static Vector2D getIntersectionLine(Plane3D plane1, Plane3D plane2){
		
		Vector3f N1 = plane1.getNormal();
		Vector3f N2 = plane2.getNormal();
		
		//degenerate case: planes are parallel
		if (compareFloat(N1.angle(N2), 0) == 0) return null;
		
		Vector3f A = new Vector3f();
		A.cross(N1, N2);
		Vector2f a = getProjectedToPlane2D(A, plane2);
		
		//project origin1 to plane2 for a point in plane
		Point2f X = getProjectedPoint(plane1.getOrigin(), plane2);
		if (X == null || a == null) return null;
		return new Vector2D(X, a);
		
	}
	
	public static Point2f getIntersectionPoint(LineSegment2D l1, LineSegment2D l2){
		return getIntersectionPoint(l1, l2, true, false);
	}
	
	/********************************************************************
	 * Returns the intersection point between two lines or line segments.
	 * <p>See <a href='http://en.wikipedia.org/wiki/Line-line_intersection'>http://en.wikipedia.org/wiki/Line-line_intersection</a>
	 * 
	 * @param l1
	 * @param l2
	 * @param test_crossing
	 * @param is_infinite
	 * @return
	 */
	public static Point2f getIntersectionPoint(LineSegment2D l1, LineSegment2D l2, boolean test_crossing, boolean is_infinite){
		if (test_crossing && !crosses(l1.getBounds(), l2.getBounds())) return null;
		
		Point2f pt1 = l1.pt1;
		Point2f pt2 = l1.pt2;
		Point2f pt3 = l2.pt1;
		Point2f pt4 = l2.pt2;
	
		float denom = ((pt1.x - pt2.x) * (pt3.y - pt4.y)) - ((pt1.y - pt2.y) * (pt3.x - pt4.x));
		
		//lines are parallel if denom = 0
		if (compareFloat(denom, 0, 0.0001) == 0) return null;
		
		float p_x = ((pt1.x*pt2.y - pt1.y*pt2.x) * (pt3.x - pt4.x) - (pt1.x - pt2.x) * (pt3.x*pt4.y - pt3.y*pt4.x)) / denom;
		float p_y = ((pt1.x*pt2.y - pt1.y*pt2.x) * (pt3.y - pt4.y) - (pt1.y - pt2.y) * (pt3.x*pt4.y - pt3.y*pt4.x)) / denom;
		
		Point2f int_pt = new Point2f(p_x, p_y);
		
		if (is_infinite || (l1.getBounds().contains(int_pt) && l2.getBounds().contains(int_pt))) return int_pt;
		
		return null;
	}
	
	/***************************
	 * Returns the vertex of <code>shape</code> which is closest to <code>point</code>.
	 * 
	 * @param shape
	 * @param point
	 * @return
	 */
	public static int getClosestVertex(Shape2D shape, Point2f point){
		
		float min = Float.MAX_VALUE;
		int index = -1;
		ArrayList<Point2f> vertices = shape.getVertices();
		
		for (int i = 0; i < vertices.size(); i++){
			Point2f vertex = vertices.get(i);
			float d = vertex.distance(point);
			if (d < min){
				index = i;
				min = d;
				}
			}
		
		return index;
	}
	
	/***************************
	 * Returns the vertex of <code>shape</code> which is closest to <code>point</code>.
	 * 
	 * @param shape
	 * @param point
	 * @return
	 */
	public static int getClosestVertex(Shape3D shape, Point3f point){
		
		float min = Float.MAX_VALUE;
		int index = -1;
		ArrayList<Point3f> vertices = shape.getVertices();
		
		for (int i = 0; i < vertices.size(); i++){
			Point3f vertex = vertices.get(i);
			float d = vertex.distance(point);
			if (d < min){
				index = i;
				min = d;
				}
			}
		
		return index;
	}
	
	/***************************
	 * Determine the point at which the line segment described by pt and v intersects
	 * plane p. Return intersection point, or null if segment does not intersect plane.
	 * 
	 * @status approved
	 * @param pt
	 * @param v
	 * @param p
	 * @return
	 */
	public static Point2f getIntersectionPoint(Point3f pt, Vector3f vector, Plane3D plane){
		Point2d p2d = getIntersectionPoint(new Point3d(pt), new Vector3d(vector), plane);
		if (p2d == null) return null;
		return new Point2f(p2d);
	}
	
	/***************************
	 * Determine the point at which the line segment described by {@code p1} and {@code p2} intersects
	 * {@code plane}. Returns intersection point in plane coordinates, or {@code null} if segment does not 
	 * intersect {@code plane}.
	 * 
	 * @status approved
	 * @param pt
	 * @param v
	 * @param p
	 * @return
	 */
	public static Point2f getIntersectionPoint(Point3f p1, Point3f p2, Plane3D plane){
		Point2d p2d = getIntersectionPoint(new Point3d(p1), new Point3d(p2), plane);
		if (p2d == null) return null;
		return new Point2f(p2d);
	}
	
	/***************************
	 * Determine the point at which the line segment described by {@code p1} and {@code p2} intersects
	 * {@code plane}. Returns intersection point in plane coordinates, or {@code null} if segment does not 
	 * intersect {@code plane}.
	 * 
	 * @status approved
	 * @param pt
	 * @param v
	 * @param p
	 * @return
	 */
	public static Point2d getIntersectionPoint(Point3d p1, Point3d p2, Plane3D plane){
		
		Vector3d v = new Vector3d(p2);
		v.sub(p1);
		return getIntersectionPoint(p1, v, plane);
		
	}
	
	/***************************
	 * Determine the point at which the line segment described by pt and v intersects
	 * plane p. Return intersection point, or null if segment does not intersect plane.
	 * 
	 * @status approved
	 * @param pt
	 * @param v
	 * @param p
	 * @return
	 */
	public static Point2d getIntersectionPoint(Point3d pt, Vector3d v, Plane3D p){
		//get second point
		Point3d pt2 = new Point3d(pt);
		pt2.add(v);
		
		//ensure the segment crosses plane; if not, return null
		if (compareToPlane(pt, p) == compareToPlane(pt2, p)) return null;
		
		//get ratio of distances
		double dist1 = Math.abs(getDistance(pt, p));
		double dist2 = Math.abs(getDistance(pt2, p));
		double ratio = dist1 / (dist1 + dist2);
		Vector3d v2 = new Vector3d(v);
		v2.scale((float)ratio);
		
		//and return mid point at this value 
		pt2.add(pt, v2);
		
		//projection simply converts it to a Point2f
		//it should already be in the plane
		return getProjectedPoint(pt2, p);
	}
	
	/************************************
	 * Determines the point at which the line segment defined by <code>pt</code> and <code>v</code>
	 * intersects triangle <code>tri</code>. Returns <code>null</code> if there is no intersection.
	 * 
	 * @status approved
	 * @param pt
	 * @param v
	 * @param tri
	 * @return point of intersection, or <code>null</code> if there is no intersection
	 */
	public static Point3f getIntersectionPoint3D(Point3f pt, Vector3f v, Triangle3D tri){
		Point3f p = getIntersectionPoint3D(pt, v, tri.getPlane());
		if (p == null) return null;
		if (tri.contains(p)) return p;
		return null;
	}
	
	/************************************
	 * Returns point at which the line segment defined by <code>pt1</code> and
	 * <code>v</code> intersects <code>plane</code>.
	 * Returns <code>null</code> if segment does not intersect the plane. 
	 * 
	 * @param pt1
	 * @param pt2
	 * @param plane
	 * @return
	 */
	public static Point3f getIntersectionPoint3D(Point3f pt, Vector3f v, Plane3D plane){
		//get second point
		Point3f pt2 = new Point3f(pt);
		pt2.add(v);
		return getIntersectionPoint3D(pt, pt2, plane);
	}
	
	/************************************
	 * Returns point at which the line segment <code>pt1</code>-<code>pt2</code> intersects <code>plane</code>.
	 * Returns <code>null</code> if segment does not intersect the plane. 
	 * 
	 * @param pt1
	 * @param pt2
	 * @param plane
	 * @status experimental
	 * @return
	 */
	public static Point3f getIntersectionPoint3D(Point3f pt1, Point3f pt2, Plane3D plane){
		Point3d p = getIntersectionPoint3D(new Point3d(pt1), new Point3d(pt2), plane);
		if (p == null) return null;
		return new Point3f(p);
	}
		
	/************************************
	 * Returns point at which the line segment <code>pt1</code>-<code>pt2</code> intersects <code>plane</code>.
	 * Returns <code>null</code> if segment does not intersect the plane. 
	 * 
	 * @param pt1
	 * @param pt2
	 * @param plane
	 * @status experimental
	 * @return
	 */
	public static Point3d getIntersectionPoint3D(Point3d pt1, Point3d pt2, Plane3D plane){
		
		//ensure the segment crosses plane; if not, return null
		if (compareToPlane(pt1, plane) == compareToPlane(pt2, plane))
			return null;
		
		//cases where one of the endpoints is on the plane itself
		double dist1 = Math.abs(getDistance(pt1, plane));
		if (compareFloat((float)dist1, 0) == 0)
			return new Point3d(pt1);
		double dist2 = Math.abs(getDistance(pt2, plane));
		if (compareFloat((float)dist2, 0) == 0)
			return new Point3d(pt2);
		
		//get ratio of distances
		double ratio = dist1 / (dist1 + dist2);
		Vector3d v2 = new Vector3d(pt2);
		v2.sub(pt1);
		v2.scale(ratio);
		
		//and return intermediate point at this value
		Point3d p_int = new Point3d();
		p_int.add(pt1, v2);
		return p_int;
		
	}
	
	public static float getIntersectionEdgeRatio(Point3f pt, Vector3f v, Plane3D p){
		//get second point
		Point3f pt2 = new Point3f(pt);
		pt2.add(v);
		
		return getIntersectionEdgeRatio(pt, pt2, p);
		
	}
	
	/***********************************************
	 * Determines the ratio along the line segment <code>pt1-pt2</code> at which it intersects
	 * <code>plane</code>. Returns -1 if this segment does not cross <code>plane</code>.
	 * 
	 * @param 	pt1
	 * @param 	pt2
	 * @param 	plane
	 * @return 	the ratio along the line segment <code>pt1-pt2</code> at which it intersects
	 * 			<code>plane</code>. Returns -1 if this segment does not cross <code>plane</code>. 
	 */
	public static float getIntersectionEdgeRatio(Point3f pt1, Point3f pt2, Plane3D plane){
	
		if (isInPlane(pt1, plane)) return 0;
		if (isInPlane(pt2, plane)) return 1;
		
		//ensure the segment crosses plane; if not, return null
		//boolean isAbove = isAbovePlane(pt1, plane);
		if (compareToPlane(pt2, plane) == compareToPlane(pt1, plane)) return -1;
		
		//get ratio of distances
		float dist1 = Math.abs(getDistance(pt1, plane));
		float dist2 = Math.abs(getDistance(pt2, plane));
		
		return dist1 / (dist1 + dist2);
	}
	
	/***********************************************
	 * Determines the ratio along the line segment <code>pt1-pt2</code> at which it intersects
	 * <code>plane</code>. Returns -1 if this segment does not cross <code>plane</code>.
	 * 
	 * @param 	pt1
	 * @param 	pt2
	 * @param 	plane
	 * @return 	the ratio along the line segment <code>pt1-pt2</code> at which it intersects
	 * 			<code>plane</code>. Returns -1 if this segment does not cross <code>plane</code>. 
	 */
	public static double getIntersectionEdgeRatio(Point3d pt1, Point3d pt2, Plane3D plane){
	
		if (isInPlane(pt1, plane)) return 0;
		if (isInPlane(pt2, plane)) return 1;
		
		//ensure the segment crosses plane; if not, return null
		//boolean isAbove = isAbovePlane(pt1, plane);
		if (compareToPlane(pt2, plane) == compareToPlane(pt1, plane)) return -1;
		
		//get ratio of distances
		double dist1 = Math.abs(getDistance(pt1, plane));
		double dist2 = Math.abs(getDistance(pt2, plane));
		
		return dist1 / (dist1 + dist2);
	}
	
	/*************************
	 * Sets the edge of intersection for triangle tri with plane p. Returns
	 * a two-element integer array indicating which sides intersected the plane, and sets
	 * the <code>edge</code> parameter to the 2D planar coordinates of the intersection 
	 * @param tri
	 * @param p
	 * @param edge
	 * @return two-element integer array indicating which sides intersected the plane
	 */
	public static int[] getIntersectionEdge(Triangle3D tri, Plane3D p, Point2f[] edge){
		if (!isValidTriangle(tri)) return null;
		int[] used = new int[2];
		//ArrayList<Point2f> pts = new ArrayList<Point2f>(2);
		if (isInPlane(tri.A, p)){
			if (isInPlane(tri.B, p)){
				edge[0] = getProjectedPoint(tri.A, p);
				edge[1] = getProjectedPoint(tri.B, p);
				used[0] = 0;
				used[1] = 1;
				return used;
				}
			if (isInPlane(tri.C, p)){
				edge[0] = getProjectedPoint(tri.A, p);
				edge[1] = getProjectedPoint(tri.C, p);
				used[0] = 0;
				used[1] = 2;
				return used;
				}
			}
		
		if (isInPlane(tri.B, p) && isInPlane(tri.C, p)){
			edge[0] = getProjectedPoint(tri.B, p);
			edge[1] = getProjectedPoint(tri.C, p);
			used[0] = 1;
			used[1] = 2;
			return used;
			}
		int index = 0;
		Vector3f v = new Vector3f();
		v.sub(tri.B, tri.A);
		Point2f pt = getIntersectionPoint(tri.A, v, p);
		if (pt != null){
			edge[index] = pt;
			used[index] = 0;
			index++;
			}
		v.sub(tri.C, tri.B);
		pt = getIntersectionPoint(tri.B, v, p);
		if (pt != null){
			edge[index] = pt;
			used[index] = 1;
			index++;
			if (index > 1) return used;
			}
		v.sub(tri.A, tri.C);
		pt = getIntersectionPoint(tri.C, v, p);
		if (pt != null){
			edge[index] = pt;
			used[index] = 2;
			index++;
			if (index > 1) return used;
			}
		
		//if we get here, no edge was made
		return null;
	}
	
	/*******************************
	 * Tests whether a point pt is contained within the boundary of a convex polygon poly. If polygon is concave,
	 * or its concavity is unknown, the method isInternalConcave() should be used.
	 * 
	 * @param poly
	 * @param pt
	 * @return
	 */
	public static boolean isInternalConvex(Polygon2D poly, Point2f pt){
		//iff angles sum to 2 * PI, point is internal
		
		ArrayList<Point2f> nodes = poly.getVertices();
		float angleSum = 0;
		
		//for each node
		for (int i = 0; i < nodes.size(); i++){
			int i2 = i + 1;
			if (i2 == nodes.size()) i2 = 0;
			angleSum += getVector(pt, nodes.get(i)).angle(getVector(pt, nodes.get(i2)));
			}
	
		boolean b = compareFloat(Math.abs(angleSum), 2 * (float)Math.PI) == 0;
		
		
		//return compareFloat(angleSum, dir * 2 * (float)Math.PI) == 0;
		return b;
	}
	
	public static Vector2f getVector(Point2f p1, Point2f p2){
		return new Vector2f(p2.x - p1.x, p2.y - p1.y);
	}
	
	/*******************************
	 * Tests whether a point pt is contained within the boundary of a polygon poly. Polygon may be convex or
	 * concave; if it is known to be convex, the method isInternalConvex() should be used.
	 * 
	 * @param poly
	 * @param pt
	 * @return
	 */
	public static boolean isInternalConcave(Polygon2D poly, Point2f pt){
		return getIsInternalConcavePoly(poly, pt) != null;
	}
	
		
	public static Polygon2D getIsInternalConcavePoly(Polygon2D poly, Point2f pt){
		//first determine whether point is within bounding rectangle
		if (!(poly.getBounds().contains(pt))) return null;
		
		//get cross products
		ArrayList<MguiFloat> concavities = getCrossProducts(poly);
		
		//if polygon is counter-clockwise, reverse signs
		//inefficient, but good for now...
		if(isClockwise(poly))
			for (int i = 0; i < concavities.size(); i++)
				concavities.get(i).multiply(-1);
		
		int offset = -1, i = 0, count = 0;
		
		while (i < concavities.size() && offset < 0){
			if (concavities.get(i).getValue() < 0){
				offset = i;
				count++;
				}
			i++;
			}
		
		//if polygon is already convex, simple
		if (offset < 0)
			if (isInternalConvex(poly, pt))
				return poly;
			else
				return null;
			
		boolean isDegen = false, isDegenTested = false;
		
		//if count < 3, degenerative case
		if (count < 3) isDegen = true;
		
		//otherwise divide it into convex subpolygons and analyze individually
		Polygon2D subpoly = new Polygon2D();
		
		//final internal polygon whose nodes are the concave nodes
		Polygon2D intpoly = new Polygon2D();
		
		for (i = 0; i <= concavities.size(); i++){
			int j = i + offset;
			if (j >= concavities.size()) j -= concavities.size();
			int k = j - 1;
			if (k < 0) k = concavities.size() - 1;
			
			subpoly.addVertex(poly.getVertex(j));
			
			if (isDegenTested){
				intpoly.addVertex(poly.getVertex(j));
			}else{
				if (isDegen){
					if (subpoly.vertices.size() > 2) {
						if (isInternalConvex(subpoly, pt)){
							return subpoly;
						}else{
							isDegenTested = true;
							intpoly = new Polygon2D();
							intpoly.addVertex(poly.getVertex(j));
							}
					}
				}else{ 
					if (concavities.get(j).getValue() < 0){
						subpoly.addVertex(poly.getVertex(j));
						if (subpoly.getVertices().size() > 2 && concavities.get(k).getValue() > 0)
							//test for internality with new convex polygon
							if (isInternalConvex(subpoly, pt)) return subpoly;
							
						intpoly.addVertex(poly.getVertex(j));
						subpoly = new Polygon2D();
						}
					}
				}
			}
		
		//recursively test internal polygon
		return getIsInternalConcavePoly(intpoly, pt);
	}
	
	/*************************
	 * Returns the edge of intersection for triangle tri with plane p. Returns
	 * null if triangle does not intersect the plane.
	 * @param tri
	 * @param p
	 * @return array of two Point2f objects representing the planar coordinates of the edge
	 */
	public static Point2f[] getIntersectionEdge(Triangle3D tri, Plane3D p){
		if (!isValidTriangle(tri)) return null;
		ArrayList<Point2f> pts = new ArrayList<Point2f>(2);
		if (isInPlane(tri.A, p)){
			if (isInPlane(tri.B, p)){
				pts.add(getProjectedPoint(tri.A, p));
				pts.add(getProjectedPoint(tri.B, p));
				return pts.toArray(new Point2f[0]);
				}
			if (isInPlane(tri.C, p)){
				pts.add(getProjectedPoint(tri.A, p));
				pts.add(getProjectedPoint(tri.C, p));
				return pts.toArray(new Point2f[0]);
				}
			}
		
		if (isInPlane(tri.B, p) && isInPlane(tri.C, p)){
			pts.add(getProjectedPoint(tri.B, p));
			pts.add(getProjectedPoint(tri.C, p));
			return pts.toArray(new Point2f[0]);
			}
			
		Vector3f v = new Vector3f();
		v.sub(tri.B, tri.A);
		Point2f pt = getIntersectionPoint(tri.A, v, p);
		if (pt != null) pts.add(pt);
		v.sub(tri.C, tri.B);
		pt = getIntersectionPoint(tri.B, v, p);
		if (pt != null) pts.add(pt);
		v.sub(tri.A, tri.C);
		pt = getIntersectionPoint(tri.C, v, p);
		if (pt != null) pts.add(pt);
		if (pts.size() != 2) return null;
		if (isCoincident(pts.get(0), pts.get(1))) return null;
		
		return pts.toArray(new Point2f[0]);
	}
	
	public static Point3fComp getPoint3fComp(){
		return new Point3fComp();
	}
	
	/***********************
	 * Determines whether triangle intersects plane
	 * @param tri
	 * @param plane
	 * @return true if it does, false otherwise
	 */
	public static boolean intersects(Triangle3D tri, Plane3D plane){
		boolean b1 = compareToPlane(tri.A, plane) > 0;
		boolean b2 = compareToPlane(tri.B, plane) > 0;
		boolean b3 = compareToPlane(tri.C, plane) > 0;
		return !((b1 == b2) && (b2 == b3));
	}
	
	/*************************
	 * Determines whether <code>tri1</code> and <code>tri2</code> intersect.
	 * 
	 * @param tri1
	 * @param tri2
	 * @return
	 */
	public static boolean intersects(Triangle3D tri1, Triangle3D tri2){
		//if bounds don't intersect, neither do triangles
		//if (!crosses(tri1.getBoundBox(), tri2.getBoundBox())) return false;
		
		Point3f[] p = getIntersectionSegment(tri1, tri2);
		if (p == null || compareDouble(p[0].distance(p[1]), 0) == 0) return false;
		
		return true;
		
		/*
		
		Plane3D plane1 = getPlane(tri1);
		Plane3D plane2 = getPlane(tri2);
		
		//Get points where triangles intersection each other's plane
		//If either is null, we know they do not intersect 
		Point3f[] int_pts1 = getIntersectionPoints(tri1, plane2);
		if (int_pts1 == null || int_pts1[0].distance(int_pts1[1]) < error) 
			return false;
		Point3f[] int_pts2 = getIntersectionPoints(tri2, plane1);
		if (int_pts2 == null || int_pts2[0].distance(int_pts2[1]) < error) 
			return false;
		
		//They intersect if and only if the bounds of their intersection points intersect
		Box3D bounds = getBox3D(int_pts1[0], int_pts1[1]);
		if (!(bounds.contains(int_pts2[0]) || bounds.contains(int_pts2[1]))){
			bounds = getBox3D(int_pts2[0], int_pts2[1]);
			if (!(bounds.contains(int_pts1[0]) || bounds.contains(int_pts1[1])))
				return false;
			}
		
		return true;
		*/
		
	}
	
	/*************************
	 * Determines the line segment defining the seam of where <code>tri1</code> and <code>tri2</code> 
	 * intersect. Returns null if they do not intersect.
	 * 
	 * @status Experimental
	 * @param tri1
	 * @param tri2
	 * @return
	 */
	public static Point3f[] getIntersectionSegment(Triangle3D tri1, Triangle3D tri2){
		//if bounds don't intersect, neither do triangles
		//if (!crosses(tri1.getBoundBox(), tri2.getBoundBox())) return null;
		
		//following Moeller 1997:
		//get plane equation N2, d2 for tri2
		
		//Get plane intersection points 
		Plane3D plane1 = getPlane(tri1);
		Plane3D plane2 = getPlane(tri2);
		
		//Get points where triangles intersection each other's plane
		//If either is null, we know they do not intersect 
		Point3f[] int_pts1 = getIntersectionPoints(tri1, plane2);
		if (int_pts1 == null) 
			return null;
		Point3f[] int_pts2 = getIntersectionPoints(tri2, plane1);
		if (int_pts2 == null) 
			return null;
		
		//They intersect if the bounds of their intersection points intersect
		Box3D bounds = getBox3D(int_pts1[0], int_pts1[1]);
		if (!(bounds.contains(int_pts2[0]) || bounds.contains(int_pts2[1]))){
			bounds = getBox3D(int_pts2[0], int_pts2[1]);
			if (!(bounds.contains(int_pts1[0]) || bounds.contains(int_pts1[1])))
				return null;
		}
		
		//Compare distances between test points, which must lie on the same line
		int[] max_idx = new int[2];
		float max = -Float.MAX_VALUE;
		float d1 = int_pts1[0].distance(int_pts1[1]);
		float d2 = int_pts2[0].distance(int_pts2[1]);
		
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 2; j++){
				float d = int_pts1[i].distance(int_pts2[j]);
				if (d > max){
					max = d;
					max_idx[0] = i;
					max_idx[1] = j;
					}
				}
		
		//Intersection segment will be innermost points; thus, invert maximum (outermost points)
		int i = max_idx[0] + 1;
		if (i > 1) i = 0;
		int j = max_idx[1] + 1;
		if (j > 1) j = 0;
		
		Point3f p1 = int_pts1[i];
		Point3f p2 = int_pts2[j];
		
		//if outer points are on same triangle, use only inner triangle for edge:
		if (d1 > max){
			j += 1;
			if (j > 1) j = 0;
			p1 = int_pts2[j];
			}
		if (d2 > max){
			i += 1;
			if (i > 1) i = 0;
			p2 = int_pts1[i];
			}
		
		//Otherwise they intersect, return intersection segment
		return new Point3f[]{p1, p2};
		
	}
	
	/********************************************
	 * Returns a Box3D defined by two points
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static Box3D getBox3D(Point3f p1, Point3f p2){
		
		Vector3f v = new Vector3f(p2);
		v.sub(p1);
		
		return new Box3D(p1, new Vector3f(v.x, 0, 0),
							 new Vector3f(0, v.y, 0),
							 new Vector3f(0, 0, v.z));
		
	}
	
	/*****************************
	 * Determines the points at which <code>tri</code> intersects <code>plane</code>. Returns
	 * <code>null</code> if it does not intersect; this includes the case where an edge of one triangle
	 * is exactly on the plane of (kissing) the other.
	 * 
	 * @param tri
	 * @param plane
	 * @return
	 */
	public static Point3f[] getIntersectionPoints(Triangle3D tri, Plane3D plane){
		return getIntersectionPoints(tri, plane, false);
	}
	
	/*****************************
	 * Determines the points at which <code>tri</code> intersects <code>plane</code>. Returns
	 * <code>null</code> if it does not intersect; {@code include_kiss} specifies whether to include the 
	 * case where an edge of one triangle is exactly on the plane of (kissing) the other.
	 * 
	 * @param tri
	 * @param plane
	 * @return
	 */
	public static Point3f[] getIntersectionPoints(Triangle3D tri, Plane3D plane, boolean include_kiss){
		
		boolean[] is_above = new boolean[3];
		int above_count = 0;
		int ABOVE = 1;
		int on_plane = -1;
		int on_plane2 = -1;
		int on_count = 0;
		
		for (int k = 0; k < 3; k++){
			int c = GeometryFunctions.compareToPlane(tri.getVertex(k), plane);
			if (c == 0){
				on_count++;
				if (on_count == 1)
					on_plane = k;
				else
					on_plane = k;
			}else{
				is_above[k] = (c != ABOVE);
				if (is_above[k]) above_count++;
				}
			}
		
		//Triangles are coplanar
		if (on_count > 2){
			return null;
			}
		
		//Triangle has an edge on plane of second
		if (on_count == 2){
			if (!include_kiss)
				return null;	//just on plane, no intersection
			//TODO: find intersection of two triangles
			return null;
		}
		
		int v1 = -1, v2 = -1, v3 = -1;
		if (on_count == 1){
			//Special case: one vertex on plane
			if (above_count != 1) return null;
			Point3f[] pts = new Point3f[2];
			pts[0] = tri.getVertex(on_plane);
			switch (on_plane){
				case 0:
					v1 = 1;
					v2 = 2;
					break;
				case 1:
					v1 = 0;
					v2 = 2;
					break;
				case 2:
					v1 = 0;
					v2 = 1;
					break;
					
				}
				pts[1] = GeometryFunctions.getIntersectionPoint3D(tri.getVertex(v1), tri.getVertex(v2), plane);
				return pts;
			}
		
		//if none or all points are above plane, there is no intersection
		if (above_count != 1 && above_count != 2) return null;
		
		if (above_count == 1){
			for (int i = 0; i < 3; i++)
				if (is_above[i])
					v1 = i;
				else if (v2 < 0)
					v2 = i;
				else
					v3 = i;
		}else{
			for (int i = 0; i < 3; i++)
				if (!is_above[i])
					v1 = i;
				else if (v2 < 0)
					v2 = i;
				else
					v3 = i;
			}
		
		//return intersection points between v1 (odd vertex wrt. plane) and v2, v3
		Point3f[] pts = new Point3f[2];
		pts[0] = GeometryFunctions.getIntersectionPoint3D(tri.getVertex(v1), tri.getVertex(v2), plane);
		pts[1] = GeometryFunctions.getIntersectionPoint3D(tri.getVertex(v1), tri.getVertex(v3), plane);
		
		return pts;
	}
	
	public static boolean intersects(Point3f p, Vector3f v, Triangle3D tri){
		Point3f int_pt = getIntersectionPoint3D(p, v, tri.getPlane());
		if (int_pt == null) return false;
		return tri.getBoundBox().contains(int_pt);
	}
	
	/***********************************
	 * Determines whether <code>p</code> intersects <code>vector</code>, within the tolerance set for this
	 * session instance (<code>GeometryFunctions.error</code>).
	 * 
	 * @param vector
	 * @param p
	 * @status Experimental
	 * @return
	 */
	public static boolean intersects(Vector3D vector, Point3f p){
		return intersects(vector, p, error);
	}
	
	/***********************************
	 * Determines whether <code>p</code> intersects <code>vector</code>, within the given tolerance.
	 * 
	 * @param vector
	 * @param p
	 * @param tolerance
	 * @status Experimental
	 * @return
	 */
	public static boolean intersects(Vector3D vector, Point3f p, double tolerance){
		
		Point3f start = vector.getStart();
		Vector3f disp = new Vector3f(p);
		disp.sub(start);
		
		return Math.sin(disp.angle(vector.getVector())) * start.distance(p) < tolerance;
		
	}
	
	/************************************
	 * Returns a plane equation as a vector {a, b, c, d}, given <code>p</code>, a point in the plane,
	 * and <code>normal</code>, its normal vector.
	 * 
	 * <p>See also the <a href="http://mathworld.wolfram.com/Plane.html">Wolfram</a> definition.
	 * 
	 * @param p
	 * @param normal
	 * @return
	 */
	public static Vector4f getPlaneEquation(Point3f p, Vector3f normal){
		float d = -(p.x * normal.x + p.y * normal.y + p.z * normal.z);
		return new Vector4f(normal.x, normal.y, normal.z, d);
	}
	
	/***********************************
	 * Returns the convex hull of <code>points</code>.
	 * 
	 * @status experimental
	 * @param points
	 * @return
	 */
	public static Polygon2D getConvexHull2f(ArrayList<Point2f> points){
		ArrayList<Point2d> points_2d = new ArrayList<Point2d>(points.size());
		for (int i = 0; i < points.size(); i++)
			points_2d.add(new Point2d(points.get(i)));
		return getConvexHull(points_2d);
	}
	
	/***********************************
	 * Returns the convex hull of <code>points</code>.
	 * 
	 * @status approved
	 * @param points the points from which to construct a convex hull
	 * @return the 2D convex hull
	 */
	public static Polygon2D getConvexHull(ArrayList<Point2d> points){
		
		if (points.size() < 3){
			InterfaceSession.log("GeometryFunctions: at least three points needed for convex hull..");
			return null;
			}
		
		/***
		 * 
		 * From: http://softsurfer.com/Archive/algorithm_0109/algorithm_0109.htm
		  
		Input: a set of points S = {P = (P.x,P.y)}

	    Select the rightmost lowest point P0 in S.
	    Sort S angularly about P0 as a center.
	        For ties, discard the closer points.
	    Let P[N] be the sorted array of points.

	    Push P[0]=P0 and P[1] onto a stack W.

	    while i < N
	    {
	        Let PT1 = the top point on W
	        Let PT2 = the second top point on W
	        if (P[i] is strictly left of the line PT2 to PT1) {
	            Push P[i] onto W
	            i++    // increment i
	        }
	        else
	            Pop the top point PT1 off the stack
	       
	    }

	    Output: W = the convex hull of S.
	    
		 */
		
		ArrayList<AnglePoint2D> angle_points = new ArrayList<AnglePoint2D> ();
		Point2d P0 = new Point2d(Double.MAX_VALUE, Double.MAX_VALUE);
		int min_idx = -1;
		for (int i = 0; i < points.size(); i++){
			Point2d p = points.get(i);
			angle_points.add(new AnglePoint2D(p));
			if (p.y < P0.y){
				P0 = p;
				min_idx = i;
				}
			}
		angle_points.remove(min_idx);
		
		for (AnglePoint2D ap : angle_points)
			ap.setAngle(P0);
		Collections.sort(angle_points);
		Stack<Point2d> W = new Stack<Point2d>();
		W.push(P0);
		W.push(angle_points.get(0).point);
		int i = 1;
		
		while (i < angle_points.size()){
			Point2d p = angle_points.get(i).point;
			Point2d p2 = W.peek();
			Point2d p1 = W.elementAt(W.size() - 2);
			
			if (isLeftOfLine(p1, p2, p)){
				W.push(p);
				i++;
			}else{
				//remove previous point as it is no longer
				//part of the hull
				if (W.size() > 2)
					W.pop();
				else
					i++;
				}
			}
		
		Polygon2D poly = new Polygon2D();
		for (Point2d p : W){
			poly.addVertex(new Point2f(p));
			}
		
		//close it
		poly.addVertex(new Point2f(P0));
		
		return poly;
	}
	
	protected static class AnglePoint2D implements Comparable<AnglePoint2D>{
		//assigns an angle to a point for sorting purposes
		
		public double angle;
		public Point2d point;
		
		public AnglePoint2D(Point2d point){
			this.point = point;
		}
		
		public void setAngle(Point2d p2){
			angle = getAngle(p2, point);
		}
		
		public int compareTo(AnglePoint2D ap2){
			if (angle < ap2.angle) return -1;
			if (angle > ap2.angle) return 1;
			return 0;
		}
		
	}
	
	public static double getAngle(Point2d p1, Point2d p2){
		Vector2d x = new Vector2d(1,0);
		Vector2d v = new Vector2d(p2);
		v.sub(p1);
		return x.angle(v);
		//double d = p1.distance(p2);
		//return Math.asin((p2.y - p1.y) / d);
	}
	
	//is positive when test_pt is left of line AB
	private static boolean isLeftOfLine(Point2d P0, Point2d P1, Point2d P2 ){
	    return ((P1.x - P0.x)*(P2.y - P0.y) - (P2.x - P0.x)*(P1.y - P0.y)) > 0;
	}
	
	/************************
	 * Returns a plane containing <code>tri</code>.
	 * 
	 * @status approved
	 * @param tri
	 * @return
	 */
	public static Plane3D getPlane(Triangle3D tri){
		
		Plane3D plane = new Plane3D();
		plane.origin = tri.getVertex(0);
		Vector3f v = new Vector3f(tri.getVertex(1));
		v.sub(plane.origin);
		v.normalize();
		plane.xAxis = v;
		v = new Vector3f(tri.getVertex(2));
		v.sub(plane.origin);
		Vector3f norm = new Vector3f();
		norm.cross(v, plane.xAxis);
		v.cross(norm, plane.xAxis);
		v.normalize();
		plane.yAxis = v;
		return plane;
		
	}
	
	/**********************************************************
	 * Determines whether the line segments <code>l1</code> and <code>l2</code> cross.
	 * 
	 * @status experimental
	 * @param l1
	 * @param l2
	 * @return
	 */
	public static boolean crosses(LineSegment2D l1, LineSegment2D l2){
		
		if (!crosses(l1.getBounds(), l2.getBounds())) return false;
		
		return getIntersectionPoint(l1, l2) != null;
		
		/*
		//crosses if x ranges & y ranges intersect
		float xMin1 = Math.min(l1.pt1.x, l1.pt2.x);
		float yMin1 = Math.min(l2.pt1.x, l1.pt2.x);
		float xMax2 = Math.max(l1.pt1.y, l1.pt2.y);
		float yMax2 = Math.max(l2.pt1.y, l1.pt2.y);
		
		return (xMin1 < xMax2 && yMin1 < yMax2);
		*/
	}
	
	public static boolean crosses(Rect2D rect1, Rect2D rect2){
		Point2f BL1 = rect1.getCorner(Rect2D.CNR_BL);
		Point2f BL2 = rect2.getCorner(Rect2D.CNR_BL);
		Point2f TR1 = rect1.getCorner(Rect2D.CNR_TR);
		Point2f TR2 = rect2.getCorner(Rect2D.CNR_TR);
		
		float maxB = Math.max(BL1.y, BL2.y);
		float minT = Math.min(TR1.y, TR2.y);
		float maxL = Math.max(BL1.x, BL2.x);
		float minR = Math.min(TR1.x, TR2.x);
		
		return (minR > maxL && minT > maxB);
		
	}
	
	/*****************************
	 * Calculates and returns the orthogonal distance regression ("best-fit") plane for a set of points in R3.
	 * See http://mathforum.org/library/drmath/view/63765.html for a description of this algorithm.
	 * 
	 * @status experimental
	 * @param points	Array of points, where n > 1 (otherwise returns <code>null</code>)
	 * @return Plane3D corresponding to the regression plane
	 */
	public static Plane3D getOrthogonalRegressionPlane(ArrayList<Point3f> points){
		if (points.size() < 2) return null;
		
		//1. compute centroid
		Point3f centroid = GeometryFunctions.getCenterOfGravity(points);
		
		//2. construct M (of deviations)
		Matrix M = new Matrix(points.size(), 3);
		for (int i = 0; i < points.size(); i++){
			M.set(i, 0, points.get(i).x);
			M.set(i, 1, points.get(i).y);
			M.set(i, 2, points.get(i).z);
			}
		
		//3. SVD of M
		SingularValueDecomposition SVD = new SingularValueDecomposition(M); 
		
		//4. extract normal from V
		Vector3f normal = new Vector3f();
		normal.x = (float)SVD.getV().get(0, SVD.getV().getColumnDimension() - 1);
		normal.y = (float)SVD.getV().get(1, SVD.getV().getColumnDimension() - 1);
		normal.z = (float)SVD.getV().get(2, SVD.getV().getColumnDimension() - 1);
		
		//5. return plane defined by centroid and normal 
		return Plane3D.getPlaneFromNormalAndY(centroid, normal);
	}
	
	/**********************************************************
	 * Determines which points in <code>nodes</code> lie within the projection limits specified by
	 * <code>plane</code>, <code>above_limit</code>, and <code>below_limit</code>. 
	 * Vectors are normalized to unit vectors.
	 * 
	 * @status experimental
	 * @param nodes
	 * @param plane
	 * @param above_limit distance along this plane's normal
	 * @param below_limit distance along the flipped normal
	 * @return list of <code>Integer</code>s specifying which nodes are inside the limits (0),
	 * 		   above the limits (1), or below the limits (-1)
	 */
	public static ArrayList<Integer> getNodesInsideProjectionLimits(ArrayList<Point3f> nodes,
																	Plane3D plane,
																	float above_limit,
																	float below_limit){
		
		ArrayList<Integer> inside = new ArrayList<Integer>();
		
		for (int i = 0; i < nodes.size(); i++){
			float dist = GeometryFunctions.getSignedDistance(nodes.get(i), plane);
			if (dist < -below_limit)
				inside.add(-1);
			else if (dist > above_limit)
				inside.add(1);
			else
				inside.add(0);
			}
			
		return inside;
		
	}
	
	/*************************************
	 * Returns a list of vectors which are the normals to the nodes of <code>polygon</code>, given a
	 * direction of normal, indicated by <code>CW</code> (clockwise if <code>true</code>, counterclockwise
	 * if <code>false</code>). Vectors are normalized to unit vectors.
	 * 
	 * @param polygon 	polygon for which to determine normals
	 * @param CW		direction of normal (clockwise if <code>true</code>, counterclockwise
	 * 						if <code>false</code>)
	 * @return			list of normals
	 */
	public static ArrayList<Vector2f> getNormals(Polygon2D polygon, boolean CW){
		
		ArrayList<Point2f> nodes = polygon.getVertices();
		ArrayList<Vector2f> normals = new ArrayList<Vector2f>();
		
		for (int i = 0; i < nodes.size(); i++){
			Point2f p0 = null;
			if (i > 0) p0 = nodes.get(i - 1);
			Point2f p1 = null;
			if (i < nodes.size() - 1) p1 = nodes.get(i + 1);
			normals.add(getNormal(p0, nodes.get(i), p1, CW));
			}
		
		return normals;
	}
	
	/*********************
	 * Determines the vector normal to the 2D curve at point <code>p</code>, where <code>p0</code> and
	 * <code>p1</code> are the point before and after <code>p</code>, respectively. One of <code>p0</code> or 
	 * <code>p1</code> (but not both) can be <code>null</code>, indicating the start or end of a curve; the
	 * remaining point will be used to determine the normal. Vector is normalized to a unit vector.
	 * 
	 * @param p0	point in curve before <code>p</code>
	 * @param p		point in curve at which to determine normal
	 * @param p1	point in curve after <code>p</code>
	 * @param CW 	indicates direction of normal (clockwise if <code>true</code>, counterclockwise
	 * 					if <code>false</code>)
	 * @return the normal at <code>p</code>
	 */
	public static Vector2f getNormal(Point2f p0, Point2f p, Point2f p1, boolean CW){
		return new Vector2f(getNormal(new Point2d(p0), new Point2d(p), new Point2d(p1), CW));
	}
	
	/*********************
	 * Determines the vector normal to the 2D curve at point <code>p</code>, where <code>p0</code> and
	 * <code>p1</code> are the point before and after <code>p</code>, respectively. One of <code>p0</code> or 
	 * <code>p1</code> (but not both) can be <code>null</code>, indicating the start or end of a curve; the
	 * remaining point will be used to determine the normal. Vector is normalized to a unit vector.
	 * 
	 * @param p0	point in curve before <code>p</code>
	 * @param p		point in curve at which to determine normal
	 * @param p1	point in curve after <code>p</code>
	 * @param CW 	indicates direction of normal (clockwise if <code>true</code>, counterclockwise
	 * 					if <code>false</code>)
	 * @return the normal at <code>p</code>
	 */
	public static Vector2d getNormal(Point2d p0, Point2d p, Point2d p1, boolean CW){
		
		double angle = Math.PI / 2.0;
		if (CW) angle = -angle;
		Vector2d S0 = new Vector2d(p);
		S0.sub(p0);
		Vector2d V0 = getRotatedVector2D(S0, angle);
		Vector2d S1 = new Vector2d(p1);
		S1.sub(p);
		Vector2d V1 = getRotatedVector2D(S1, angle);
		V0.add(V1);
		V0.normalize();
		
		return V0;
	}
	
	public static class Point3fComp implements Comparator{
		public int compare(Object o1, Object o2){
			Point3f p1 = (Point3f)o1;
			Point3f p2 = (Point3f)o2;
			if (isCoincident(p1, p2)) return 0;
			if (p1.x > p2.x) return 1;
			if (p1.x < p2.x) return -1;
			if (p1.y > p2.y) return 1;
			if (p1.y < p2.y) return -1;
			if (p1.z > p2.z) return 1;
			return -1;
			}
	}
	
	

	
	
}