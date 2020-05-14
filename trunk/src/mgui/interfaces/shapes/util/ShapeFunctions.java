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

package mgui.interfaces.shapes.util;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.CapabilityNotSetException;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.ModelClip;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.PickInfo;
import org.jogamp.java3d.PickInfo.IntersectionInfo;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Matrix4d;
import org.jogamp.vecmath.Matrix4f;
import org.jogamp.vecmath.Point2d;
import org.jogamp.vecmath.Point2f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector2f;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;
import org.jogamp.vecmath.Vector4d;

import mgui.geometry.Box3D;
import mgui.geometry.Graph2D;
import mgui.geometry.Grid3D;
import mgui.geometry.LineSegment2D;
import mgui.geometry.Mesh3D;
import mgui.geometry.Plane3D;
import mgui.geometry.PointSet2D;
import mgui.geometry.PointSet3D;
import mgui.geometry.Polygon2D;
import mgui.geometry.Polygon3D;
import mgui.geometry.Triangle3D;
import mgui.geometry.Vector2D;
import mgui.geometry.Vector3D;
import mgui.geometry.mesh.MeshFunctionException;
import mgui.geometry.util.ConvexHullFunctions;
import mgui.geometry.util.GeometryFunctions;
import mgui.geometry.util.K3DBinaryTree;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.Utility;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.graphs.AbstractGraphEdge;
import mgui.interfaces.graphs.AbstractGraphNode;
import mgui.interfaces.graphs.DefaultGraph;
import mgui.interfaces.graphs.InterfaceAbstractGraph;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.shapes.BoolPolygon2DInt;
import mgui.interfaces.shapes.IntPolygon2DInt;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.Mesh2DInt;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.PointSet2DInt;
import mgui.interfaces.shapes.Polygon2DInt;
import mgui.interfaces.shapes.Shape2DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.ShapeSet2DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.Vector2DInt;
import mgui.interfaces.shapes.Vector3DInt;
import mgui.interfaces.shapes.VertexDataColumn;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.graphs.Graph2DInt;
import mgui.interfaces.shapes.graphs.util.Graph2DLayout;
import mgui.interfaces.shapes.mesh.MeshDataMaskOptions;
import mgui.interfaces.shapes.mesh.VertexSelection;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;
import mgui.util.Colour;
import mgui.util.Colours;

import org.jogamp.java3d.utils.geometry.Cylinder;
import org.jogamp.java3d.utils.geometry.Primitive;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.pickfast.PickCanvas;
import org.jogamp.java3d.utils.pickfast.PickIntersection;

import foxtrot.Job;
import foxtrot.Worker;

/***********************************************************
 * Utility class which provides functions related to {@code InterfaceShape} objects generally.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeFunctions extends Utility {
	
	public static final double tolerance = 0.0000001;

	public static Shape3DInt getShape(Shape3D shape){
		Object obj = shape.getUserData();
		if (obj instanceof Shape3DInt) return (Shape3DInt)obj;
		return null;
	}
	
	public static Shape3DInt getShapeVertexObject(Shape3D shape, MguiInteger index){
		Object obj = shape.getUserData();
		if (!(obj instanceof ShapeVertexObject)) return null;
		ShapeVertexObject vertex = (ShapeVertexObject)obj;
		if (index != null)
			index.setValue(vertex.getIndex());
		return vertex.getShape();
	}
	
	/**********************************************************************
	 * Sets a 2D child's attributes from its parent. If {@code inherit} is {@code true},
	 * Sets all 2D attributes to the 3D attribute values.
	 * 
	 * @param child_attributes
	 * @param parent_attributes
	 * @param inherit
	 */
	public static void setAttributesFrom3DParent(Shape2DInt child, Shape3DInt parent, boolean inherit){
				
		AttributeList child_attributes = child.getAttributes();
		AttributeList parent_attributes = parent.getAttributes();
		
		if (!inherit){
			child_attributes.setIntersection(parent_attributes, false);
			child.updateShape();
			return;
			}
		
		child_attributes.setIntersection(parent_attributes, false);
		ArrayList<String> keys = parent_attributes.getKeys();
		
		for (int i = 0; i < keys.size(); i++){
			String name = keys.get(i);
			Attribute<?> parent_attribute = parent_attributes.getAttribute(name);
			
			if (parent.isHeritableAttribute(parent_attribute)){
				Attribute<?> inheriting_attribute = parent.getInheritingAttribute(parent_attribute);
				String name2 = inheriting_attribute.getName();
				if (child_attributes.hasAttribute(name2))
					child_attributes.getAttribute(name2).setValue(parent_attribute.getValue(), false);
			}else if (child.isInheritingAttribute(parent_attribute) && child_attributes.hasAttribute(name)){
				child_attributes.getAttribute(name).setValue(parent_attribute.getValue(), false);
				}
			}
		
		child.updateShape();
	}
	
	/*************************************************
	 * Transform <code>shape_in</code> with <code>matrix</code> and returns the result. Does not alter
	 * <code>shape_in</code>. If <code>progress</code> is not <code>null</code>, this method starts a worker 
	 * thread; otherwise, it blocks.
	 * 
	 * @param shape_in the shape to transform
	 * @param matrix matrix specifying an affine transformation
	 * @param progress option progress monitor
	 * @return a shape which is the transformed copy of <code>shape_in</code>
	 */
	public static mgui.geometry.Shape3D transformWithMatrix(final mgui.geometry.Shape3D shape_in, 
															final Matrix4d matrix, 
															final ProgressUpdater progress){
		
		if (progress == null)
			return transformWithMatrixBlocking(shape_in, matrix, progress);
		
		mgui.geometry.Shape3D shape_out =
			((mgui.geometry.Shape3D)Worker.post(new Job(){
			@Override
			public mgui.geometry.Shape3D run(){
				return transformWithMatrixBlocking(shape_in, matrix, progress);
			}
		}));
		
		return shape_out;
		
	}
	
	/*************************************************
	 * Transform <code>shape_in</code> with <code>matrix</code> and returns the result. Does not alter
	 * <code>shape_in</code>. This method blocks until complete; to run in a worker thread, call
	 * <code>transformWithMatrix(..)</code>
	 * 
	 * @param shape_in the shape to transform
	 * @param matrix matrix specifying an affine transformation
	 * @param progress option progress monitor
	 * @return a shape which is the transformed copy of <code>shape_in</code>
	 */
	protected static mgui.geometry.Shape3D transformWithMatrixBlocking(mgui.geometry.Shape3D shape_in, 
																	   Matrix4d matrix, 
																	   ProgressUpdater progress){
		
		mgui.geometry.Shape3D shape_new = (mgui.geometry.Shape3D)shape_in.clone();
		GeometryFunctions.transform(shape_new, matrix, progress);
		return shape_new;
		
	}
	
	/******************
	 * Returns a new mesh which is the convex hull of the given mesh. Defaults to
	 * the "Giftwrap" algorithm, by Tim Lambert:
	 * 
	 * <p>http://www.cse.unsw.edu.au/~lambert/java/3d/implementation.html
	 * 
	 * @param mesh Mesh for which to compute a convex hull.
	 * @param method The method to use
	 */
	public static Mesh3D getConvexHull(Shape3DInt shape){
		return getConvexHull(shape, "GiftWrap", null);
	}
	
	public static Mesh3D getConvexHull(Shape3DInt shape, ProgressUpdater progress){
		return getConvexHull(shape, "GiftWrap", progress);
	}
	
	public static Mesh3D getConvexHull(Shape3DInt shape, String method){
		return getConvexHull(shape, method, null);
	}
	
	/******************
	 * Returns a new mesh which is the convex hull of the given mesh. Algorithms
	 * provided by Joseph O'Rourke et al:
	 * 
	 * <p>http://maven.smith.edu/~orourke/books/ftp.html
	 * 
	 * <p>and Tim Lambert:
	 * 
	 * <p>http://www.cse.unsw.edu.au/~lambert/java/3d/implementation.html
	 * 
	 * @status Approved
	 * @param mesh Mesh for which to compute a convex hull
	 * @param method The method to use
	 * @param bar Optional progress bar
	 * @return the convex hull, or <code>null</code> if method failed or was cancelled.
	 */
	public static Mesh3D getConvexHull(final Shape3DInt shape, 
									   final String method, 
									   final ProgressUpdater progress){
		if (progress == null || !(progress instanceof InterfaceProgressBar)) 
			return getConvexHullBlocking(shape, method, progress);
		
		Mesh3D hull = (Mesh3D)Worker.post(new Job(){
				@Override
				public Mesh3D run(){
					return getConvexHullBlocking(shape, method, progress);
				}
			});
		
		return hull;
	}
	
	protected static Mesh3D getConvexHullBlocking(Shape3DInt shape, 
												  String method, 
												  ProgressUpdater progress){
		
		try{
			return ConvexHullFunctions.getConvexHull(shape.getVertices(), method, progress);
		}catch (MeshFunctionException e){
			e.printStackTrace();
			}
		
		return null;
		
	}
	
	/****************************************
	 * Return the shape picked by a mouse click at the screen location defined by <code>click_point</code>.
	 * Optionally sets the value of <code>node</code> to the selected node index of the picked shape, if
	 * <code>node</code> is not null.
	 * 
	 * <p>TODO: return a list of picked shapes, ordered from closest to farthest
	 * 
	 * @param pickNode
	 * @param pickGeom
	 * @param click_point
	 * @param node
	 * @return
	 */
	public static Shape3DInt getPickedShape(PickCanvas pickNode, PickCanvas pickGeom, Point click_point, MguiInteger node){
		if (pickGeom == null || pickNode == null || click_point == null) return null;
		
		pickNode.setShapeLocation(click_point.x, click_point.y);
		//pickGeom.setShapeLocation(click_point.x, click_point.y);
		PickInfo infoNode = pickNode.pickClosest();
		//PickInfo infoNode = pickGeom.pickClosest();
		if (infoNode == null) return null;
		Node n = infoNode.getNode();			
		if (n == null) return null;
		
		Shape3DInt shape = null;
		if (n instanceof Shape3D){
			shape = ShapeFunctions.getShape((Shape3D)n);
		
			if (shape == null){
				shape = ShapeFunctions.getShapeVertexObject((Shape3D)n, node);
				return shape;
				}
			}
		
		if (shape == null) return null;
		
		pickGeom.setShapeLocation(click_point.x, click_point.y);
		PickInfo infoGeom = pickGeom.pickClosest();
		if (infoGeom == null) return shape;
		
		Transform3D t3d = new Transform3D();
		infoNode.getNode().getLocalToVworld(t3d);
		IntersectionInfo[] infos = infoGeom.getIntersectionInfos();
		if (infos == null){
			if (node != null) node.setValue(-1);
			return shape;
			}
		
		PickInfo.IntersectionInfo intInfo = infos[0];
		PickIntersection intersect = new PickIntersection(t3d, intInfo);
		int[] indices = intersect.getPrimitiveCoordinateIndices();
		if (indices == null){
			if (node != null) node.setValue(-1);
			return shape;
			}
		
		int index = intersect.getClosestVertexIndex();
		if (node != null) node.setValue(indices[index]);
		return shape;
		
	}
	
	/**********************************************
	 * Returns a {@link TransformGroup} with a sphere at the specified point, with the specified
	 * radius.
	 * 
	 * @param radius
	 * @param point
	 * @return
	 */
	public static TransformGroup getSphereAtPoint(float radius, Point3f point){
		return getSphereAtPoint(radius, new Point3d(point), false);
	}
	
	/**********************************************
	 * Returns a {@link TransformGroup} with a sphere at the specified point, with the specified
	 * radius. Uses a default {@link Appearance} with red colour.
	 * 
	 * @param radius
	 * @param point
	 * @return
	 */
	public static TransformGroup getSphereAtPoint(float radius, Point3d point){
		return getSphereAtPoint(radius, point, false);
	}
	
	/**********************************************
	 * Returns a {@link TransformGroup} with a sphere at the specified point, with the specified
	 * radius. Uses a default {@link Appearance} with red colour.
	 * 
	 * @param radius	Radius of the sphere
	 * @param point		Center point of the sphere
	 * @param pickable	Determines whether the sphere is pickable
	 * @return
	 */
	public static TransformGroup getSphereAtPoint(float radius, Point3d point, boolean pickable){
		Appearance app = new Appearance();
		Material m = new Material();
		m.setSpecularColor(Colours.getColor3f(Color.red));
		m.setDiffuseColor(Colours.getColor3f(Color.red));
		app.setMaterial(m);
		return getSphereAtPoint(radius, point, app, pickable);
	}
	
	/**********************************************
	 * Returns a {@link TransformGroup} with a sphere at the specified point, with the specified
	 * radius and {@link Appearance}.
	 * 
	 * @param radius	Radius of the sphere
	 * @param point		Center point of the sphere
	 * @param pickable	Determines whether the sphere is pickable
	 * @return
	 */
	public static TransformGroup getSphereAtPoint(float radius, Point3d point, Appearance app, boolean pickable){
		Vector3f v = new Vector3f(point);
		Transform3D t3d = new Transform3D();
		t3d.setTranslation(v);
		TransformGroup tg = new TransformGroup(t3d);
		Sphere sphere = new Sphere(radius);
		sphere.setAppearance(app);
		sphere.setPickable(pickable);
		tg.addChild(sphere);
		return tg;
	}
	
	public static BoolPolygon2DInt getBoolCornerPoints(Polygon2DInt thePoly, 
									   double length_threshold,
									   double angle_threshold){
		
		BoolPolygon2DInt thisPoly = new BoolPolygon2DInt(thePoly);
		double avrL = 0;
		int n = thisPoly.getPolygon().vertices.size();
		double[] lengths = new double[n];
		//determine relative segment lengths
		for (int i = 0; i < n; i++){
			if (i == n - 1)
				lengths[i] = GeometryFunctions.getDistance(thisPoly.getPolygon().vertices.get(i),
												   		   thisPoly.getPolygon().vertices.get(0));
			else
				lengths[i] = GeometryFunctions.getDistance(thisPoly.getPolygon().vertices.get(i),
						   						   		   thisPoly.getPolygon().vertices.get(i + 1));
			avrL += lengths[i];
		}
		
		avrL /= n;
		boolean lengthPassed, prevPassed;
		double angle1, angle2, angle3;
		int j, k, l, m;
		prevPassed = (lengths[n - 1] / avrL > length_threshold);
		
		//for each node in polygon
		for (int i = 0; i < n; i++){
			//if segment length > length_threshold
			lengthPassed = lengths[i] / avrL > length_threshold;
			if (lengthPassed || prevPassed){	
				//and Ni.angle < angle_threshold or Ni-1.angle < angle_threshold
				j = i - 1;
				if (j < 0) j = n - 1;
				k = i + 1;
				if (k == n) k = 0;
				l = i + 2;
				if (l == n) l = 0;
				if (l == n + 1) l = 1;
				m = i - 2;
				if (m == -1) m = n - 1;
				if (m == -2) m = n - 2;
				angle1 = GeometryFunctions.getAngle(thisPoly.getPolygon().vertices.get(j),
													thisPoly.getPolygon().vertices.get(i), 
													thisPoly.getPolygon().vertices.get(k));
				angle2 = Double.MAX_VALUE;
				if (lengthPassed)
					angle2 = GeometryFunctions.getAngle(thisPoly.getPolygon().vertices.get(i),
														thisPoly.getPolygon().vertices.get(k), 
														thisPoly.getPolygon().vertices.get(l));
				angle3 = Double.MAX_VALUE;
				if (prevPassed)
					angle2 = GeometryFunctions.getAngle(thisPoly.getPolygon().vertices.get(m),
														thisPoly.getPolygon().vertices.get(j), 
														thisPoly.getPolygon().vertices.get(i));
					
					
				if (angle1 < angle_threshold || angle2 < angle_threshold || angle3 < angle_threshold)
					//set node to true;
					thisPoly.setNodeBool(i, true);
				}
			prevPassed = lengthPassed;
			}
		return thisPoly;
	}
	
	public static IntPolygon2DInt getIntCornerPoints(Polygon2DInt thePoly, 
													 double length_threshold,
													 double angle_threshold){
		BoolPolygon2DInt boolPoly = getBoolCornerPoints(thePoly, length_threshold, angle_threshold);
		IntPolygon2DInt intPoly = new IntPolygon2DInt(thePoly);
		int c = 1;
		for (int i = 0; i < boolPoly.getPolygon().vertices.size(); i++)
			if (boolPoly.getNodeBool(i)){
				intPoly.setNodeVal(i, c);
				c++;
			}
		
		return intPoly;
	}
	
	/****************************************************************
	 * Determines whether {@code child} is a child of {@code parent} in the Scene Graph
	 * 
	 * @param parent
	 * @param child
	 * @return
	 * @throws CapabilityNotSetException
	 */
	public static boolean nodeHasChild(BranchGroup parent, Node child) throws CapabilityNotSetException{
		
		Iterator<Node> children = parent.getAllChildren();
		
		while (children.hasNext())
			if (children.next().equals(child))
				return true;
		
		return false;
	}
	
	public static Polygon2DInt getGroomedPolygon(Polygon2DInt thisPoly, 
												 float minLen, boolean blnMin,
												 float maxLen, boolean blnMax,
												 float minAngle){
		Polygon2DInt retPoly = (Polygon2DInt)thisPoly.clone();
		Polygon2D thePoly = retPoly.getPolygon();
		
		//get node lengths array
		float[] lengths = GeometryFunctions.getSegmentLengths(thePoly);
		ArrayList<MguiFloat> length = new ArrayList<MguiFloat>(lengths.length);
		for (int i = 0; i < lengths.length; i++)
			length.add(new MguiFloat(lengths[i]));
		
		//get node angles array
		float[] angles = GeometryFunctions.getNodeAngles(thePoly);
		ArrayList<MguiFloat> angle = new ArrayList<MguiFloat>(lengths.length);
		for (int i = 0; i < lengths.length; i++)
			angle.add(new MguiFloat(angles[i]));
		
		boolean blnAltered = true;
		
		while (blnAltered){
			blnAltered = false;
			//for each node
			//int n = thePoly.nodes.size();
			//Polygon2D newPoly = (Polygon2D)thePoly.clone();
			for (int i = 0; i < thePoly.vertices.size(); i++){
				//if blnMin && length[i] < minLen && angle[i] > minAngle
				if (blnMin && length.get(i).getValue() < minLen && angle.get(i).getValue() > minAngle){
					//if next len > minLen, move this node to midpoint between
					//Ni-1 and Ni+1; alter length[i] and neighbour length to
					//reflect this change
					
					int j = i + 1;
					if (j == thePoly.vertices.size()) j = 0;
					int k = i - 1;
					if (k == -1) k = thePoly.vertices.size() - 1;
					int l = i + 2;
					if (l >= thePoly.vertices.size()) l -= thePoly.vertices.size();
					int m = i - 2;
					if (m < 0) m = thePoly.vertices.size() - m;
					
					if (thePoly.vertices.get(k).distance(thePoly.vertices.get(j)) >= minLen * 2){
						blnAltered = true;
						//move this node to midway between its neighbours
						thePoly.vertices.set(i, GeometryFunctions.getMidPoint(thePoly.vertices.get(k),
								thePoly.vertices.get(j), 0.5));
						
//						get node lengths array
						lengths = GeometryFunctions.getSegmentLengths(thePoly);
						length = new ArrayList<MguiFloat>(lengths.length);
						for (int a = 0; a < lengths.length; a++)
							length.add(new MguiFloat(lengths[a]));
						
						//get node angles array
						angles = GeometryFunctions.getNodeAngles(thePoly);
						angle = new ArrayList<MguiFloat>(lengths.length);
						for (int a = 0; a < lengths.length; a++)
							angle.add(new MguiFloat(angles[a]));
						
						/**
						//adjust arrays
						length.get(k).value = thePoly.nodes.get(k).distance(thePoly.nodes.get(i));
						length.get(i).value = thePoly.nodes.get(i).distance(thePoly.nodes.get(j));
						angle.get(k).value = GeometryFunctions.getAngle(thePoly.nodes.get(m),
																		thePoly.nodes.get(k), 
																		thePoly.nodes.get(i));
						angle.get(i).value = GeometryFunctions.getAngle(thePoly.nodes.get(k),
																		thePoly.nodes.get(i), 
																		thePoly.nodes.get(j));
						angle.get(j).value = GeometryFunctions.getAngle(thePoly.nodes.get(i),
																		thePoly.nodes.get(j), 
																		thePoly.nodes.get(l));
																		**/
						}else{
						//delete this node
						blnAltered = true;
						thePoly.vertices.remove(i);
						length.remove(i);
						angle.remove(i);
						}
					}
				
				
			//if blnMax && length[i] > maxLen && angle[i] > minAngle
			if (i < thePoly.vertices.size())
			if (blnMax && length.get(i).getValue() > maxLen && angle.get(i).getValue() > minAngle){
				//insert node at midpoint between Ni and Ni+1
				//insert and adjust lengths array
				blnAltered = true;
				int j = i + 1;
				if (j == thePoly.vertices.size()) j = 0;
				Point2f newPt = GeometryFunctions.getMidPoint(thePoly.vertices.get(i),
						thePoly.vertices.get(j), 0.5);
				//length.add(j, new arDouble(newPt.distance(thePoly.nodes.get(j))));
				//length.get(i).value = newPt.distance(thePoly.nodes.get(i));
				//angle.add(j, new arDouble(GeometryFunctions.getAngle(thePoly.nodes.get(i),
				//						  newPt, 
				//						  thePoly.nodes.get(j))));
				thePoly.vertices.add(j, newPt);
				
				//get node lengths array
				lengths = GeometryFunctions.getSegmentLengths(thePoly);
				length = new ArrayList(lengths.length);
				for (int a = 0; a < lengths.length; a++)
					length.add(new MguiFloat(lengths[a]));
				
				//get node angles array
				angles = GeometryFunctions.getNodeAngles(thePoly);
				angle = new ArrayList(lengths.length);
				for (int a = 0; a < lengths.length; a++)
					angle.add(new MguiFloat(angles[a]));
				
				}
			}
			//repeat until polygon is valid
		}
		
		return retPoly;
	}
	
	public static ShapeSet3DInt getShapeSet3DFromSection(Plane3D refPlane, 
														 float dist,
														 ShapeSet2DInt shapes,
														 ShapeSelectionSet selSet){
		Shape3DInt thisShape;
		ShapeSet3DInt retSet = new ShapeSet3DInt();
		boolean blnInclude;
		for (int i = 0; i < shapes.members.size(); i++){
			blnInclude = true;
			if (selSet != null)
				blnInclude = selSet.hasShape(shapes.members.get(i));
			if (blnInclude){
				thisShape = ShapeFunctions.getShape3DIntFromSection(refPlane,
																	dist,
																	shapes.members.get(i));
				if (thisShape != null)
					retSet.addShape(thisShape, false);
				}
			}
		
		return retSet;
	}
	
	public static Shape3DInt getShape3DIntFromSection(Plane3D refPlane, 
													  float dist,
													  Shape2DInt shape2D){
		return getShape3DIntFromSection(refPlane, dist, shape2D, false);
	}
	
	public static Shape3DInt getShape3DIntFromSection(Plane3D refPlane, 
													  float dist,
													  Shape2DInt shape2D,
													  boolean set_parent){
		
		/*
		if (shape2D instanceof ShapeSet2DInt){
			ShapeSet3DInt shape_set_3d = new ShapeSet3DInt();
			if (set_parent){
				shape2D.setChild3D(shape_set_3d);
				//shape_set_3d.setParent2D(shape2D);
				}
			
			for (Shape2DInt shape : ((ShapeSet2DInt)shape2D).members){
				Shape3DInt shape3d = getShape3DIntFromSection(refPlane, dist, shape, set_parent);
				if (shape3d != null)
					shape_set_3d.addShape(shape3d);
				}
			return shape_set_3d;
		}
		
		//must be done case by case (for different shape ints)...?
		//or can these attributes be made general? Maybe with a getShape3D() function?
		if (shape2D instanceof Polygon2DInt){
			Polygon3D newPoly = new Polygon3D();
			ArrayList<Point2f> nodes = shape2D.thisShape.getNodes();
			
			//for each 2D node, determine 3D node
			for (int i = 0; i < nodes.size(); i++)
				newPoly.addNode(GeometryFunctions.getPointFromSection(refPlane, dist, nodes.get(i)));
			Polygon3DInt retPoly = new Polygon3DInt(newPoly);
			//temp for texture test
			//TestTextureInt retPoly = new TestTextureInt(newPoly);
			
			//set attributes from 2D polygon
			retPoly.getAttributes().setAttribute(shape2D.getAttributes().getAttribute("LineColour"));
			retPoly.getAttributes().setAttribute(shape2D.getAttributes().getAttribute("IsVisible"));
			retPoly.getAttributes().setAttribute(shape2D.getAttributes().getAttribute("HasFill"));
			retPoly.getAttributes().setAttribute(shape2D.getAttributes().getAttribute("FillColour"));
			retPoly.getAttributes().setAttribute(shape2D.getAttributes().getAttribute("HasTransparency"));
			retPoly.getAttributes().setAttribute(shape2D.getAttributes().getAttribute("Alpha"));
			//retPoly.attributes.setValue("AsCylinder", new arBoolean(true));
			
			if (set_parent){
				shape2D.setChild3D(retPoly);
				//retPoly.setParent2D(shape2D);
				}
			
			return retPoly;
			}
		
		if (shape2D instanceof Rect2DInt){
			Rect3D newRect = new Rect3D();
			ArrayList<Point2f> nodes = shape2D.thisShape.getNodes();
			
			//for each 2D node, determine 3D node
			for (int i = 0; i < nodes.size(); i++)
				newRect.nodes[i] = (GeometryFunctions.getPointFromSection(refPlane, dist, nodes.get(i)));
			Rect3DInt retRect = new Rect3DInt(newRect);
			
			//set attributes from 2D rect
			retRect.getAttributes().setAttribute(shape2D.getAttributes().getAttribute("LineColour"));
			retRect.getAttributes().setAttribute(shape2D.getAttributes().getAttribute("IsVisible"));
			retRect.getAttributes().setAttribute(shape2D.getAttributes().getAttribute("HasFill"));
			retRect.getAttributes().setAttribute(shape2D.getAttributes().getAttribute("FillColour"));
			retRect.getAttributes().setAttribute(shape2D.getAttributes().getAttribute("HasTransparency"));
			retRect.getAttributes().setAttribute(shape2D.getAttributes().getAttribute("Alpha"));
			
			if (set_parent){
				shape2D.setChild3D(retRect);
				//retRect.setParent2D(shape2D);
				}
			
			return retRect;
		}
		
		if (shape2D instanceof Image2DInt){
			Image2DInt image = (Image2DInt)shape2D;
			ArrayList<Point2f> nodes = shape2D.thisShape.getNodes();
			ArrayList<Point3f> newNodes = new ArrayList<Point3f>(4);
			
			for (int i = 0; i < 4; i++)
				newNodes.add(GeometryFunctions.getPointFromSection(refPlane, dist, nodes.get(i)));
			
			Image3DInt image3D = new Image3DInt(new Rect3D(newNodes), image.image, image.getHasTransparency());
			
			//set attributes from 2D image
			image3D.getAttributes().setAttribute(shape2D.getAttributes().getAttribute("LineColour"));
			image3D.getAttributes().setAttribute(shape2D.getAttributes().getAttribute("IsVisible"));
			//image3D.getAttributes().setAttribute(shape2D.getAttributes().getAttribute("HasFill"));
			//image3D.getAttributes().setAttribute(shape2D.getAttributes().getAttribute("FillColour"));
			image3D.getAttributes().setAttribute(shape2D.getAttributes().getAttribute("HasTransparency"));
			image3D.getAttributes().setAttribute(shape2D.getAttributes().getAttribute("Alpha"));
			image3D.getAttributes().setAttribute(shape2D.getAttributes().getAttribute("HasBorder"));
			
			if (set_parent){
				shape2D.setChild3D(image3D);
				//image3D.setParent2D(shape2D);
				}
			
			return image3D;
		}
		*/
		
		return shape2D.getShape3DInt(refPlane);
		
	}
	
	
	/*****************************
	 * Returns a 2D polygon in section coordinates representing the intersection of
	 * a Box3D object with the plane. Returns <code>null</code> if the box does not intersect the plane. 
	 * 
	 * @param box
	 * @param plane
	 * @status Experimental
	 * @return Polygon2D object, in plane coordinates relative to its reference point
	 */
	public static Polygon2D getIntersectionPolygon(Box3D box, Plane3D plane){
		
		// 1. Get intersection points
		
		// For each edge in the box
		Point3f[][] edges = box.getEdges();
		ArrayList<Point2f> vertices = new ArrayList<Point2f>();
		for (int i = 0; i < 12; i++){
			Point2f p2f = GeometryFunctions.getIntersectionPoint(edges[i][0], edges[i][1], plane);
			if (p2f != null) vertices.add(p2f);
			}
		
		if (vertices.size() < 3) return null;
		
		// 2. Get convex hull
		return GeometryFunctions.getConvexHull2f(vertices);
		
	}
	
	/*****************************
	 * Returns a 2D polygon in section coordinates representing the intersection of
	 * a Box3D object with the plane. Returns <code>null</code> if the box does not intersect the plane. 
	 * 
	 * @param box
	 * @param plane
	 * @status Experimental
	 * @return Polygon2D object, in plane coordinates relative to its reference point
	 */
	public static Polygon2D getIntersectionPolygon2(Box3D box, Plane3D plane){
		ArrayList<Point2f> nodes = new ArrayList<Point2f>();
		ArrayList<Point3f> side;
		//for each edge (12 total)
		Vector3d edge = new Vector3d();
		Point3d point = new Point3d();
		Point2d point2D = new Point2d();
		//x edges
		for (int dir = 0; dir < 3; dir++){
			side = box.getSide(0, dir);
			//point.set(box.getBasePt());
			if (dir == 0) edge.set(box.getSAxis());
			if (dir == 1) edge.set(box.getTAxis());
			if (dir == 2) edge.set(box.getRAxis());
			
			for (int i = 0; i < 4; i++){
				point = new Point3d(side.get(i));
				if (GeometryFunctions.isInPlane(point, edge, plane)){
					point2D = GeometryFunctions.getProjectedPoint(point, plane);
					Point2f point2Df = new Point2f(point2D);
					if (!isInList(point2Df, nodes)){
						nodes.add(point2Df);
						//if (edges != null)
						//	edges.add(new int[]{dir, i});
						}
					point.add(edge);
					point2D = GeometryFunctions.getProjectedPoint(point, plane);
					point2Df = new Point2f(point2D);
					if (!isInList(point2Df, nodes)){
						nodes.add(point2Df);
						//if (edges != null)
						//	edges.add(new int[]{dir, i});
						}
				}else{
					point2D = GeometryFunctions.getIntersectionPoint(point, edge, plane);
					if (point2D != null){
						Point2f point2Df = new Point2f(point2D);
					 	if (!isInList(point2Df, nodes)){
					 		nodes.add(point2Df);
					 		//if (edges != null)
							//	edges.add(new int[]{dir, i});
					 		}
						}
					}
				}
			
			}
		
		if (nodes.size() < 3) return null;
		
		//return convex hull of points
		return GeometryFunctions.getConvexHull2f(nodes);
		//return GeometryFunctions.getNearestNeighbourPolygon(nodes);
	}
	
	/*****************************
	 * Returns a 2D polygon in section coordinates representing the projection of <code>polygon</code>
	 * onto <code>plane</code>, within the specified clipping distances.
	 *  
	 * @param box
	 * @param plane
	 * @return Polygon2D object, in plane coordinates relative to its reference point
	 */
	public static Polygon2DInt getProjectedPolygon(Polygon3D polygon, Plane3D plane, 
												   float above_dist, float below_dist){
		return getProjectedPolygon(polygon, plane, above_dist, below_dist, null);
	}
	
	/*****************************
	 * Returns a 2D polygon in section coordinates representing the projection of <code>polygon</code>
	 * onto <code>plane</code>, within the specified clipping distances.
	 *  
	 * @param polygon 			The shape to project
	 * @param plane
	 * @param above_dist
	 * @param below_dist
	 * @param v_column   		Vertex data column from which to derive vertex-wise values for the
	 * 							new polygon shape
	 * 
	 * @return Polygon2DInt object, in plane coordinates relative to its reference point
	 */
	public static Polygon2DInt getProjectedPolygon(Polygon3D polygon, Plane3D plane, 
												   float above_dist, float below_dist,
												   VertexDataColumn v_column){
		
		ArrayList<MguiNumber> values = null;
		if (v_column != null){
			values = v_column.getData();
			}
		
		ArrayList<Point3f> vertices = polygon.getVertices();
		ArrayList<MguiNumber> values2d = new ArrayList<MguiNumber>();
		
		Plane3D plane_up = (Plane3D)plane.clone();
		Point3f o = plane.getOrigin();
		Vector3f offset = plane.getNormal();
		offset.scale(above_dist);
		o.add(offset);
		plane_up.setOrigin(o);
		
		Plane3D plane_down = (Plane3D)plane.clone();
		o = plane.getOrigin();
		offset = plane.getNormal();
		offset.scale(below_dist);
		o.sub(offset);
		plane_down.setOrigin(o);
		
		boolean[] contained = new boolean[vertices.size()];
		boolean[] above = new boolean[vertices.size()];
		ArrayList<Point2f> points2d = new ArrayList<Point2f>();
		boolean is_out = false;
		for (int i = 0; i < vertices.size(); i++){
			float dist = GeometryFunctions.getSignedDistance(vertices.get(i), plane);
			above[i] = dist >=0;
			contained[i] = (dist >= 0 && dist <= above_dist) || 
						   (dist < 0 && Math.abs(dist) <= below_dist);
			points2d.add(GeometryFunctions.getProjectedPoint(vertices.get(i), plane));
			}
		
		Polygon2D poly2d = new Polygon2D();
		Plane3D proj_plane;
		Point2f p2d = new Point2f();
		Point3f p3d = new Point3f();
		Point3f v_i = new Point3f();
		Point3f v_j = new Point3f();
		for (int i = 0, j = 1; j < vertices.size(); i++, j++){
			v_i.set(vertices.get(i));
			v_j.set(vertices.get(j));
			
			boolean is_last = (j == vertices.size() - 1);
			
			// Case 1: both in
			if (contained[i] && contained[j]){
				p2d.set(GeometryFunctions.getProjectedPoint(vertices.get(i), plane));
				poly2d.addVertex(p2d, true, true);
				is_out = false;
				// Add last vertex if we're there
				if (is_last){
					p2d.set(GeometryFunctions.getProjectedPoint(vertices.get(j), plane));
					poly2d.addVertex(p2d, true, true);
					}
			
			// Case 2: i in, j out
			}else if (contained[i] && !contained[j]){
				// Get intersection point and project
				p2d.set(GeometryFunctions.getProjectedPoint(vertices.get(i), plane));
				poly2d.addVertex(p2d, true, true);
				is_out = true;
				
				if (is_last){
					proj_plane = above[j] ? plane_up : plane_down;
					p3d.set(GeometryFunctions.getIntersectionPoint3D(v_i, v_j, proj_plane));
					p2d.set(GeometryFunctions.getProjectedPoint(p3d, plane));
					poly2d.addVertex(p2d, true, false);
					}
				
			// Case 2: i out, j in
			}else if (!contained[i] && contained[j]){
				proj_plane = above[i] ? plane_up : plane_down;
				p3d.set(GeometryFunctions.getIntersectionPoint3D(v_i, v_j, proj_plane));
				p2d.set(GeometryFunctions.getProjectedPoint(p3d, plane));
				
				// If previous vertex was out, don't include the previous segment
				if (is_out && poly2d.segments.size() > 0){
					poly2d.segments.set(poly2d.segments.size()-1, false);
					}
				poly2d.addVertex(p2d, true, false);
				is_out = false;
				
				if (is_last){
					p2d.set(GeometryFunctions.getProjectedPoint(vertices.get(j), plane));
					poly2d.addVertex(p2d, true, true);
					}
				
			// Case 2: both out
			}else{
				is_out = true;
				}
			
			}
		
		if (poly2d.getSize() == 0) return null;
		Polygon2DInt poly2dint = new Polygon2DInt(poly2d);
		
		return poly2dint;
	}
	
	
	
	/*****************************
	 * Returns a 2D polygon in section coordinates representing the projection of <code>polygon</code>
	 * onto <code>plane</code>, within the specified clipping distances.
	 *  
	 * @param polygon 			The shape to project
	 * @param plane
	 * @param above_dist
	 * @param below_dist
	 * @param v_column   		Vertex data column from which to derive vertex-wise values for the
	 * 							new polygon shape
	 * 
	 * @return Polygon2DInt object, in plane coordinates relative to its reference point
	 */
	public static Polygon2DInt getProjectedPolygon_bak(Polygon3D polygon, Plane3D plane, 
												   float above_dist, float below_dist,
												   VertexDataColumn v_column){
		
		// For each segment in polygon
		// If segment is contained in plane corridor, add it
		// Else if segment intersects plane corridor, determine intersection points and add it
		
		ArrayList<MguiNumber> values = null;
		if (v_column != null){
			values = v_column.getData();
			}
		
		ArrayList<Point3f> vertices = polygon.getVertices();
		ArrayList<MguiNumber> values2d = new ArrayList<MguiNumber>();
		Point3f s1 = new Point3f();
		Point3f s2 = new Point3f();
		
		int[] contained = new int[vertices.size()];
		ArrayList<Point2f> points2d = new ArrayList<Point2f>();
		for (int i = 0; i < vertices.size(); i++){
			float dist = GeometryFunctions.getSignedDistance(vertices.get(i), plane);
			if (dist >= 0 && dist < above_dist){
				contained[i] = 1;
			}else if (dist < 0 && Math.abs(dist) < below_dist){
				contained[i] = 2;
				}
			points2d.add(GeometryFunctions.getProjectedPoint(vertices.get(i), plane));
			}
		
		Polygon2D poly2d = new Polygon2D();
		boolean include_last = true;
		for (int i = 0; i < vertices.size()-1; i++){
			s1.set(vertices.get(i));
			int j = i + 1;
			s2.set(vertices.get(j));
			
			if (contained[i] > 0){
				poly2d.addVertex(points2d.get(i), true);
				if (values != null){
					values2d.add((MguiNumber)values.get(i).clone());
					}
				
				if (contained[j]==0){
					// i is contained but j isn't, add another vertex for ij intersection
					Plane3D plane2 = (Plane3D)plane.clone();
					Point3f o = plane2.getOrigin();
					float d = GeometryFunctions.getSignedDistance(s2, plane);
					if (d>0){
						// Above plane
						Vector3f offset = plane.getNormal();
						offset.scale(above_dist);
						o.add(offset);
						plane2.setOrigin(o);
					}else{
						// Below plane
						Vector3f offset = plane.getNormal();
						offset.scale(below_dist);
						o.sub(offset);
						plane2.setOrigin(o);
						}
					
					if (values != null) {
						// Interpolate value here
						Point3f p3_int = GeometryFunctions.getIntersectionPoint3D(s1, s2, plane2);
						float delta = s1.distance(p3_int) / s1.distance(s2);
						double v = (values.get(i).getValue() * delta) + (values.get(j).getValue() * (delta-1));
						MguiNumber newval = (MguiNumber)values.get(i).clone();
						newval.setValue(v);
						values2d.add(newval);
						}
					
					Point2f p2_int = GeometryFunctions.getIntersectionPoint(s1, s2, plane2);
					if (p2_int != null){
						poly2d.addVertex(p2_int, true, false);
						}
					}
				include_last = true;
			}else{
				
				if (contained[j]>0){
					// i is not contained but j is, compute intersection point of segment s_ij with the
					// offset plane
					
					Plane3D plane2 = (Plane3D)plane.clone();
					Point3f o = plane2.getOrigin();
					if (contained[j]==1){
						// Above plane
						Vector3f offset = plane.getNormal();
						offset.scale(above_dist);
						o.add(offset);
						plane2.setOrigin(o);
					}else{
						// Below plane
						Vector3f offset = plane.getNormal();
						offset.scale(below_dist);
						o.sub(offset);
						plane2.setOrigin(o);
						}
					
					if (values != null) {
						// Interpolate value here
						Point3f p3_int = GeometryFunctions.getIntersectionPoint3D(s1, s2, plane2);
						float delta = s1.distance(p3_int) / s1.distance(s2);
						double v = (values.get(i).getValue() * delta) + (values.get(j).getValue() * (delta-1));
						MguiNumber newval = (MguiNumber)values.get(i).clone();
						newval.setValue(v);
						values2d.add(newval);
						}
					
					Point2f p2_int = GeometryFunctions.getIntersectionPoint(s1, s2, plane2);
					// Segment before i is not included
					if (p2_int != null){
						poly2d.addVertex(p2_int, false, false);
						include_last = true;
					}else{
						include_last = true;
						}
					
				}else{
					// Neither i nor j are contained, but segment may still cross
					Plane3D plane_up = (Plane3D)plane.clone();
					Point3f o = plane_up.getOrigin();
					Vector3f offset = plane.getNormal();
					offset.scale(above_dist);
					o.add(offset);
					plane_up.setOrigin(o);
					Plane3D plane_down = (Plane3D)plane.clone();
					o = plane_up.getOrigin();
					offset = plane.getNormal();
					offset.scale(below_dist);
					o.sub(offset);
					plane_down.setOrigin(o);
					
					Point3f x_up = GeometryFunctions.getIntersectionPoint3D(s1, s2, plane_up);
					Point3f x_down = GeometryFunctions.getIntersectionPoint3D(s1, s2, plane_down);
					
					if (x_up == null && x_down == null){
						// No crossing, skip this vertex
						include_last = false;
					}else{ 
						if (x_up != null){
							// segment enters corridor, add intersection point
							poly2d.addVertex(GeometryFunctions.getProjectedPoint(x_up, plane), true, false);
							if (values != null) {
								// Interpolate value here
								Point3f p3_int = GeometryFunctions.getIntersectionPoint3D(s1, s2, plane_up);
								float delta = s1.distance(p3_int) / s1.distance(s2);
								double v = (values.get(i).getValue() * delta) + (values.get(j).getValue() * (delta-1));
								MguiNumber newval = (MguiNumber)values.get(i).clone();
								newval.setValue(v);
								values2d.add(newval);
								}
							include_last = true;
							} 
						if (x_down != null){
							// segment enters corridor, add intersection point
							poly2d.addVertex(GeometryFunctions.getProjectedPoint(x_down, plane), true, false);
							if (values != null) {
								// Interpolate value here
								Point3f p3_int = GeometryFunctions.getIntersectionPoint3D(s1, s2, plane_down);
								float delta = s1.distance(p3_int) / s1.distance(s2);
								double v = (values.get(i).getValue() * delta) + (values.get(j).getValue() * (delta-1));
								MguiNumber newval = (MguiNumber)values.get(i).clone();
								newval.setValue(v);
								values2d.add(newval);
								}
							include_last = true;
							}
						}
					}
				}
			}
		
		if (poly2d.getSize() == 0) return null;
		
		Polygon2DInt poly2dint = new Polygon2DInt(poly2d);
		if (v_column != null){
			// Set the new data column to match the parent one
			poly2dint.addVertexData(v_column.getName(), values2d);
			poly2dint.setCurrentColumn(v_column.getName());
			VertexDataColumn column = poly2dint.getCurrentDataColumn();
			column.setColourMap(v_column.getColourMap(), false);
			column.setDataLimits(v_column.getDataMin(), v_column.getDataMax(), true);
			column.setColourLimits(v_column.getColourMin(), v_column.getColourMax(), true);
			}
		
		return poly2dint;
		
	}
	
	//dirty search for point in list
	private static boolean isInList(Point2f p, ArrayList<Point2f> list){
		for (int i = 0; i < list.size(); i++)
			if (Math.abs(list.get(i).x - p.x) < GeometryFunctions.error &&
				Math.abs(list.get(i).y - p.y) < GeometryFunctions.error) 
				return true;
		return false;
	}
	
	/*****************************
	 * Returns a Mesh2DInt object representing the intersection of the given 3D mesh
	 * with plane p. Returns null if this mesh does not intersect the plane. TODO: add
	 * functionality to project mesh onto plane within a given spacing.
	 * @param mesh mesh to intersect
	 * @param plane plane of intersection
	 * @return Mesh2DInt object representing the intersection of the surface with the plane
	 */
	public static Mesh2DInt getIntersectionMesh(Mesh3D mesh, 
												Plane3D plane){
		return getIntersectionMesh(mesh, plane, null, null);
	}
	
	/*****************************
	 * Returns a Mesh2DInt object representing the intersection of the given 3D mesh
	 * with plane p. Returns null if this mesh does not intersect the plane. If data and
	 * cmap are not null, also assigns colours to the segments based upon their interpolated
	 * values.
	 * TODO: add functionality to project mesh onto plane within a given spacing.
	 * @param mesh mesh to intersect
	 * @param plane plane of intersection
	 * @param data list of node data from which to colour the lines. If null, lines are
	 * 			   coloured according to surface fill colour.
	 * @param cmap colour map from which to set the interpolated line colours
	 * @return Mesh2DInt object representing the intersection of the surface with the plane,
	 * 					 or {@code null} if the plane doesn't intersect the mesh
	 */
	public static Mesh2DInt getIntersectionMesh(Mesh3D mesh, 
												Plane3D plane, 
												ArrayList<MguiNumber> data,
												ColourMap cmap){
		
		//TODO: connect the dots and allow for a fill...
		
		//1. Get set of faces
		ArrayList<Mesh3D.MeshFace3D> faces = mesh.getFaces();
		if (faces == null) return null;
		Mesh3D.MeshFace3D face;
		Triangle3D tri;
		ArrayList<Edge> edges = new ArrayList<Edge>();
		Point2f[] edge;
		boolean getColours = (data != null && cmap != null);
		
		ArrayList<Colour> colours = null;
		if (getColours)
			colours = new ArrayList<Colour>();
		
		ArrayList<Integer[]> indices = new ArrayList<Integer[]>();
		
		float d1;
		int index = 0;
		
		//2. for each face in faces, get intersection edge if it exists
		for (int i = 0; i < faces.size(); i++){
			face = faces.get(i);
			tri = new Triangle3D(mesh.getVertex(face.A),
								 mesh.getVertex(face.B),
								 mesh.getVertex(face.C));
			if (GeometryFunctions.isInPlane(tri, plane)){
				Point2f A = GeometryFunctions.getProjectedPoint(tri.A, plane);
				Point2f B = GeometryFunctions.getProjectedPoint(tri.B, plane);
				Point2f C = GeometryFunctions.getProjectedPoint(tri.C, plane);
				//add three edges
				//InterfaceSession.log("Face in plane...");
				if (A != null && B != null){
					edges.add(new Edge(index++, A, B));
					indices.add(new Integer[]{face.A, face.B});
					}
				if (C != null && B != null){
					edges.add(new Edge(index++, B, C));
					indices.add(new Integer[]{face.B, face.C});
					}
				if (A != null && C != null){
					edges.add(new Edge(index++, C, A));
					indices.add(new Integer[]{face.C, face.A});
					}
				//colour edge points if necessary
				if (getColours){
					if (A != null){
						colours.add(cmap.getColour(data.get(face.A)));
						colours.add(cmap.getColour(data.get(face.A)));
						}
					if (B != null){
						colours.add(cmap.getColour(data.get(face.B)));
						colours.add(cmap.getColour(data.get(face.B)));
						}
					if (C != null){
						colours.add(cmap.getColour(data.get(face.C)));
						colours.add(cmap.getColour(data.get(face.C)));
						}
					}
			}else{
				edge = new Point2f[2];
				int[] used = GeometryFunctions.getIntersectionEdge(tri, plane, edge);
				//add regular edge
				if (used != null){
					edges.add(new Edge(index++, edge));
					indices.add(new Integer[]{used[0], used[1]});
						
					//add colour if necessary
					if (getColours){
						for (int k = 0; k < 2; k++){
							int j = used[k] + 1;
							if (j > 2) j = 0;
							
							d1 = GeometryFunctions.getIntersectionEdgeRatio(tri.getVertex(used[k]),
																			tri.getVertex(j), 
																			plane);
							
							colours.add(Colours.interpolate(cmap.getColour(data.get(face.getNode(used[k]))),
															cmap.getColour(data.get(face.getNode(j))),
															d1));
							}
						}
					}else{
						//InterfaceSession.log("Null used array...");
						
						}
				}
			}
		
		if (edges.size() == 0){
			//InterfaceSession.log("No edges!");
			return null;
		}
		
		//remove duplicate edges
		Comparator<Edge> edgeComp = new Comparator<Edge>(){
			// Non-zero tolerance violates contract; simply use 0 tolerance
			float tolerance = 0f; // 0.01f;
			public int compare(Edge e1, Edge e2){
				int r = comp(e1.A.x, e2.A.x);
				if (r != 0) return r;
				r = comp(e1.A.y, e2.A.y);
				if (r != 0) return r;
				r = comp(e1.B.x, e2.B.x);
				if (r != 0) return r;
				return comp(e1.B.y, e2.B.y);
				
				}
			int comp(float a, float b){
				float r = a - b;
				if (r > tolerance) return 1;
				if (r < -tolerance) return -1;
				return 0;
				}
			}; 
			
		//sort edge list (takes too long maybe?)
		try{
			Collections.sort(edges, edgeComp);
		}catch (Exception ex){
			//ex.printStackTrace();
			InterfaceSession.log("ShapeFunctions.getIntersectionMesh: Error sorting edges..",
								 LoggingType.Errors);
			return null;
			}
		
		ArrayList<Colour> retColours = new ArrayList<Colour>();
		ArrayList<MguiNumber> retData = new ArrayList<MguiNumber>();
		
		//eliminate duplicates
		int j;
		for (int i = 0; i < edges.size(); i++){
			j = i + 1;
			while (j < edges.size() && edgesEqual(edges.get(i), edges.get(j))){
				edges.remove(j);
				}
			
			}
		
		ArrayList<Point2f[]> pts = new ArrayList<Point2f[]>(edges.size());
		ArrayList<Integer[]> inds = new ArrayList<Integer[]>(edges.size());
		Point2f[] thisEdge;
		for (int i = 0; i < edges.size(); i++){
			thisEdge = new Point2f[2];
			thisEdge[0] = edges.get(i).A;
			thisEdge[1] = edges.get(i).B;
			pts.add(thisEdge);
			inds.add(indices.get(edges.get(i).index));
			
			if (getColours){
				if (edges.get(i).reversed){
					retColours.add(colours.get((edges.get(i).index * 2) + 1));
					retColours.add(colours.get(edges.get(i).index * 2));
				}else{
					retColours.add(colours.get(edges.get(i).index * 2));
					retColours.add(colours.get((edges.get(i).index * 2) + 1));
					}
				}
			}
		
		Mesh2DInt retMesh = new Mesh2DInt(pts, inds);
		if (getColours){
			retMesh.setColours(retColours);
			}

		return retMesh;
	}
	
	/*******************************
	 * Returns a {@linkplain Graph2DInt} object derived from the plane and 3D
	 * graph.
	 * 
	 * @param graph3d
	 * @param plane
	 * @param above_dist
	 * @param below_dist
	 * @return
	 */
	public static Graph2DInt getIntersectedGraph(InterfaceAbstractGraph graph3d,
												 HashMap<AbstractGraphNode, Integer> node_map,
												 Plane3D plane,
												 float above_dist,
												 float below_dist){
		return getIntersectedGraph(graph3d, node_map, plane, above_dist, below_dist, null);
	}
	
	/*******************************
	 * Returns a {@linkplain Graph2DInt} object derived from the plane and 3D
	 * graph.
	 * 
	 * @param graph3d
	 * @param plane
	 * @param above_dist
	 * @param below_dist
	 * @return
	 */
	public static Graph2DInt getIntersectedGraph(InterfaceAbstractGraph graph3d,
												 HashMap<AbstractGraphNode, Integer> node_map,
												 Plane3D plane,
												 float above_dist,
												 float below_dist,
												 Matrix4d transform){
		
		try{
			InterfaceAbstractGraph new_graph = new DefaultGraph();
			
			ArrayList<AbstractGraphNode> vertices = new ArrayList<AbstractGraphNode>(graph3d.getVertices());
			HashMap<AbstractGraphNode,Point2f> pts2d = new HashMap<AbstractGraphNode,Point2f>();
			HashMap<AbstractGraphNode,AbstractGraphNode> vmap = new HashMap<AbstractGraphNode,AbstractGraphNode>();
			// Index map is used to maintain vertex correspondence between the 2D graph and 3D parent
			HashMap<AbstractGraphNode,Integer> index_map = new HashMap<AbstractGraphNode,Integer>();
			
			// Add vertices
			for (int i = 0; i < vertices.size(); i++){
				AbstractGraphNode vertex = vertices.get(i);
				Point3f p = vertex.getLocation();
				if (transform != null){
					transform.transform(p);
					}
				
				float test_dist = above_dist;
				
				if (GeometryFunctions.compareToPlane(p, plane) < 0) test_dist = below_dist;
				if (GeometryFunctions.getDistance(p, plane) <= test_dist){
					AbstractGraphNode new_vertex = (AbstractGraphNode)vertex.clone();
					vmap.put(vertex,new_vertex);
					new_graph.addVertex(new_vertex);
					pts2d.put(new_vertex,GeometryFunctions.getProjectedPoint(p, plane));
					index_map.put(new_vertex, node_map.get(vertex));
					}
				}
			
			// Add Edges
			ArrayList<AbstractGraphEdge> edges = new ArrayList<AbstractGraphEdge>(graph3d.getEdges());
			for (int i = 0; i < edges.size(); i++){
				AbstractGraphEdge edge = edges.get(i);
				AbstractGraphNode from = edge.getFrom();
				AbstractGraphNode to = edge.getTo();
				from = vmap.get(from);
				to = vmap.get(to);
				if (from != null && to != null){
					new_graph.addEdge((AbstractGraphEdge)edge.clone(), from, to);
					}
				}
			
			Graph2D g2d = new Graph2D(new_graph, pts2d);
			
			Graph2DLayout<AbstractGraphNode,AbstractGraphEdge> layout = 
								new Graph2DLayout<AbstractGraphNode,AbstractGraphEdge>(new_graph, plane);
			
			return new Graph2DInt(g2d, layout, index_map);
		}catch (CloneNotSupportedException ex){
			InterfaceSession.handleException(ex);
			return null;
			}
		
		
	}
	
	/*****************************
	 * Returns a PointSet2DInt object representing the points within <code>spacing</code> of the
	 * plane <code>plane</code>. Returns null if this set does not intersect the plane. If data and
	 * cmap are not null, also sets these attributes.
	 * 
	 * @param point_set point set to intersect
	 * @param plane plane of intersection
	 * @param data list of node data from which to colour the lines. If null, lines are
	 * 			   coloured according to surface fill colour.
	 * @param cmap colour map from which to set the interpolated line colours
	 * @return Mesh2DInt object representing the intersection of the surface with the plane
	 */
	public static PointSet2DInt getIntersectionPointSet(PointSet3D point_set, 
														Plane3D plane,
														float above_dist,
														float below_dist,
														HashMap<String, ArrayList<MguiNumber>> data,
														ColourMap cmap){
		
		
		PointSet2D set2D = new PointSet2D();
		PointSet2DInt set2Dint = new PointSet2DInt(set2D);
		HashMap<Integer,Integer> map_idx_to_parent = new HashMap<Integer,Integer>();
		
		HashMap<String, ArrayList<MguiNumber>> data2 = null;
		if (data != null)
			data2 = new HashMap<String, ArrayList<MguiNumber>>();
		
		int index = 0;
		float[] nodes = new float[point_set.n * 2];
		int itr = 0;
		
		for (int i = 0; i < point_set.n; i++){
			
			Point3f p = point_set.getVertex(i);
			float test_dist = above_dist;
			if (GeometryFunctions.compareToPlane(p, plane) < 0) test_dist = below_dist;
			if (GeometryFunctions.getDistance(p, plane) <= test_dist){ // / 2.0){
				//project point and add it
				map_idx_to_parent.put(itr++, i);
				Point2f p2d = GeometryFunctions.getProjectedPoint(p, plane);
				//set2D.addNode(p2d);
				nodes[(index * 2)] = p2d.x;
				nodes[(index * 2) + 1] = p2d.y;
				index++;
				
				//add its data if necessary
				if (data != null){
					Iterator<String> keys = data.keySet().iterator(); 
					while (keys.hasNext()){
						String key = keys.next();
						if (data2.get(key) == null)
							data2.put(key, new ArrayList<MguiNumber>());
						ArrayList<MguiNumber> d = data2.get(key);
						d.add(data.get(key).get(i));
						}
					//set2Dint.nodeData = data2;
					}
				
				}
			}
		
		set2D.nodes = nodes;
		set2D.n = index;
		set2D.finalize();
		set2Dint.setMapIdxToParent(map_idx_to_parent);
		
		if (data2 != null){
			Iterator<String> keys = data2.keySet().iterator(); 
			while (keys.hasNext()){
				String key = keys.next();
				set2Dint.addVertexData(key, data2.get(key));
				}
			}
		//InterfaceSession.log("get int point set: + " + index  + " nodes of " + point_set.n);
		
		return set2Dint;
		
	}
	
	static boolean edgesEqual(Edge e1, Edge e2){
		boolean first, out, in, last;
		float tolerance = 0.001f;
		first = GeometryFunctions.isCoincident(e1.A, e2.A, tolerance);
		out = GeometryFunctions.isCoincident(e1.A, e2.B, tolerance);
		in = GeometryFunctions.isCoincident(e1.B, e2.A, tolerance);
		last = GeometryFunctions.isCoincident(e1.B, e2.B, tolerance);
		return (first && last) || (in && out);
	}
	
	static class EdgeNode{
		public Point2f node;
		public int edge;
		public EdgeNode(Point2f p, int e){
			node = p;
			edge = e;
		}
	}
	
	static class Edge{
		public int index;
		public boolean reversed = false;
		Point2f A, B;
		static float tolerance = 0.01f;
		public Edge(){}
		public Edge(int index, Point2f a, Point2f b){
			this.index = index;
			A = a;
			B = b;
			sort();
		}
		public Edge(int index, Point2f[] pts){
			this.index = index;
			A = pts[0];
			B = pts[1];
			sort();
		}
		void sort(){
			if (comp(A.x, B.x) <= 0) return;
			reversed = true;
			Point2f T = B;
			B = A;
			A = T;
			}
		int comp(float a, float b){
			float r = a - b;
			if (r > tolerance) return 1;
			if (r < -tolerance) return -1;
			return 0;
			}
		@Override
		public String toString(){
			return "A: (" + A.x + ", " + A.y + ") B: (" + B.x + ", " + B.y + ")";
		}
	}
	
	
	
	/***************************
	 * Returns a Vector2DInt object which is the projection of <code>vector</code> onto <code>plane</code>,
	 * if and only if some part of <code>vector</code> is within the distance boundary specified by 
	 * <code>above_dist</code> and <code>below_dist</code>. Truncates <code>vector</code> at this boundary if it does not lie completely
	 * within it.
	 * 
	 * @param volume
	 * @param plane
	 * @param spacing
	 * @return Image3DInt object, in plane coordinates relative to its reference point
	 */
	
	public static Vector2DInt getIntersectionVector(Vector3DInt v_int, Plane3D plane, float above_dist, float below_dist){
		return getIntersectionVector(v_int, plane, above_dist, below_dist, null);
	}
	
	/***************************
	 * Returns a Vector2DInt object which is the projection of <code>vector</code> onto <code>plane</code>,
	 * if and only if some part of <code>vector</code> is within the distance boundary specified by 
	 * <code>above_dist</code> and <code>below_dist</code>. Truncates <code>vector</code> at this boundary if it does not lie completely
	 * within it.
	 * 
	 * @param volume
	 * @param plane
	 * @param spacing
	 * @return Image3DInt object, in plane coordinates relative to its reference point
	 */
	
	public static Vector2DInt getIntersectionVector(Vector3DInt v_int, Plane3D plane, float above_dist, float below_dist, Matrix4d transform){
		
		Vector3D vector = v_int.getVector();
		if (transform != null){
			GeometryFunctions.transform(vector, transform);
			}
		Point3f start_pt = new Point3f(vector.getStart());
		Point3f end_pt = new Point3f(vector.getStart());
		end_pt.add(vector.getVector());
		float test_dist_start = above_dist; 
		if (GeometryFunctions.compareToPlane(start_pt, plane) < 0) test_dist_start = -below_dist;
		boolean start_near = GeometryFunctions.getDistance(start_pt, plane) <= Math.abs(test_dist_start);
		float test_dist_end = above_dist; 
		if (GeometryFunctions.compareToPlane(end_pt, plane) < 0) test_dist_end = -below_dist;
		boolean end_near = GeometryFunctions.getDistance(end_pt, plane) <= Math.abs(test_dist_end);
		
		if (!start_near && !end_near)
			if (!GeometryFunctions.crossesPlane(vector, plane))
				if (vector.start.z < 20 && vector.start.z > 10)
					return null;	//debug point
				else
					return null;
		
		Vector3f normal = plane.getNormal();
		Point2f start_2d = new Point2f();
		
		//ratios for resulting shape
		float start_ratio = 0, end_ratio = 1;
		
		//get start point
		if (start_near){
			start_2d.set(GeometryFunctions.getProjectedPoint(start_pt, plane));
		}else{
			//outside spacing window, need to find intersection with boundary plane
			Vector3f n = new Vector3f(normal);
			Plane3D b_plane = (Plane3D)plane.clone();
			n.scale(test_dist_start);
			//flips normal if this point is below the plane
			//n.scale(GeometryFunctions.compareToPlane(start_pt, plane));
			//move plane to boundary
			b_plane.origin.add(n);
			start_2d.set(GeometryFunctions.getIntersectionPoint(start_pt, vector.getVector(), b_plane));
			Point3f int_pt = GeometryFunctions.getPointFromPlane(start_2d, b_plane);
			float dist = int_pt.distance(start_pt);
			start_ratio = dist / vector.vector.length(); 
			}
		
		Point2f end_2d = new Point2f();
		
		//get end point
		if (end_near){
			end_2d.set(GeometryFunctions.getProjectedPoint(end_pt, plane));
		}else{
			//outside spacing window, need to find intersection with boundary plane
			Vector3f n = new Vector3f(normal);
			Plane3D b_plane = (Plane3D)plane.clone();
			n.scale(test_dist_end);
			//flips normal if this point is below the plane
			//n.scale(GeometryFunctions.compareToPlane(end_pt, plane));
			//move plane to boundary
			b_plane.origin.add(n);
			end_2d.set(GeometryFunctions.getIntersectionPoint(start_pt, vector.getVector(), b_plane));
			Point3f int_pt = GeometryFunctions.getPointFromPlane(end_2d, b_plane);
			float dist = int_pt.distance(start_pt);
			end_ratio = dist / vector.vector.length(); 
			}
		
		Vector2f vector_2d = new Vector2f(end_2d);
		vector_2d.sub(start_2d);
		
		Vector2DInt vector2d_int = new Vector2DInt(new Vector2D(start_2d, vector_2d));
		vector2d_int.getAttributes().setIntersection(v_int.getAttributes());
		
		vector2d_int.setParentShape(v_int);
		vector2d_int.setStartRatio(start_ratio);
		vector2d_int.setEndRatio(end_ratio);
		
		return vector2d_int;
	}
	
	
	
	
	public static void setMeshConstraints(Mesh3DInt mesh, ArrayList<Point3f> constraints){
		//sort nodes
		ArrayList<Point3f> nodes = mesh.getMesh().getVertices();
		GeometryFunctions.Point3fComp PointComp = GeometryFunctions.getPoint3fComp();
		Collections.sort(nodes, PointComp);
		
		int index;
		for (int i = 0; i < constraints.size(); i++){
			index = Collections.binarySearch(nodes, constraints.get(i), PointComp);
			if (index >= 0)
				mesh.setConstraint(index, true);
			}
			
	}
	
	public static ArrayList<Polygon2D> getMergedPolygons(ArrayList<Polygon2D> polygons){
		
		ArrayList<IntPt> intPts = new ArrayList<IntPt>();
		boolean[] hasInt = new boolean[polygons.size()];
		int nTotal = 0;
		int[] offsets = new int[polygons.size()];
		for (int i = 0; i < polygons.size(); i++){
			offsets[i] = nTotal;
			nTotal += polygons.get(i).vertices.size();
			}
		IntPt[] joinPts = new IntPt[nTotal];
		
		ArrayList<Polygon2D> mergedPolys = new ArrayList<Polygon2D>();
		
		//get list of intersection points
		for (int i = 0; i < polygons.size(); i++){
			Polygon2D poly1 = polygons.get(i);
			for (int j = 0; j < polygons.size(); j++)
				if (i != j){
					Polygon2D poly2 = polygons.get(j);
					if (GeometryFunctions.crosses(poly1.getBounds(), poly2.getBounds())){
						//get list of intersecting edges
						boolean[][] intersects = GeometryFunctions.getSegmentsIntersect(poly1, poly2);
						
						for (int p = 0; p < poly1.vertices.size(); p++)
							for (int q = 0; q < poly2.vertices.size(); q++)
								if (intersects[p][q]){
									IntPt intPt = new IntPt();
									intPt.poly1 = i;
									intPt.poly2 = j;
									intPt.node1 = p;
									intPt.node2 = q;
									
									int p2 = p + 1; if (p2 == poly1.vertices.size()) p2 = 0;
									int q2 = q + 1; if (q2 == poly2.vertices.size()) q2 = 0;
									
									intPt.point = GeometryFunctions.getIntersectionPoint(
													new LineSegment2D(poly1.getVertex(p), poly1.getVertex(p2)),
													new LineSegment2D(poly2.getVertex(q), poly2.getVertex(q2)));
									
									intPts.add(intPt);
									joinPts[offsets[i] + p] = intPt;
									joinPts[offsets[j] + q] = intPt;
									}
						}
					}
			}
		
		//add non-intersecting polygons unless they are contained within other polygons
		for (int i = 0; i < polygons.size(); i++)
			if (!hasInt[i]){
				boolean isContained = false;
				for (int j = 0; j < polygons.size(); j++)
					isContained &= (i != j &&
									GeometryFunctions.crosses(polygons.get(i).getBounds(),
								  							  polygons.get(j).getBounds()) &&
								  	polygons.get(j).contains(polygons.get(i).getVertex(0)));
				if (!isContained)
					mergedPolys.add(polygons.get(i));
				}
		
		//construct merged polygons
		Point2f startPt;
		Polygon2D thisPoly, newPoly = null;
		int thisNode;
		boolean reversed;
		IntPt intPt;
		
		for (int i = 0; i < intPts.size(); i++){
			intPt = intPts.get(i);
			
			if (!intPt.isProcessed)
				newPoly = new Polygon2D();
			
			while (!intPt.isProcessed){
				reversed = true;
				startPt = intPt.point;
				newPoly.addVertex(startPt);
				thisPoly = polygons.get(intPt.poly1);
				thisNode = intPt.node1;
				
				if (polygons.get(intPt.poly2).contains(thisPoly.getVertex(thisNode))){
					reversed = false;
					thisNode++;
					if (thisNode == thisPoly.vertices.size()) thisNode = 0;
					}
				
				do{
					newPoly.addVertex(thisPoly.getVertex(thisNode));
					
					if (reversed){
						thisNode--;
						if (thisNode < 0) thisNode = thisPoly.vertices.size() - 1;
					}else{
						thisNode++;
						if (thisNode == thisPoly.vertices.size()) thisNode = 0;
						}
					
					intPt = joinPts[offsets[intPt.poly1] + thisNode];
						
					} while (intPt == null && !intPt.isProcessed);
				}
			
			mergedPolys.add(newPoly);
			}
		
		return mergedPolys;
	}
	
	static class IntPt{
		public int poly1, poly2;
		public int node1, node2;
		public Point2f point;
		public boolean isProcessed;
	}
	
	public static ArrayList<Polygon2D> getMergedPolygonsbak(ArrayList<Polygon2D> polygons){
		
		//for each polygon pair,
		//1. find intersection points
		//2. find internal nodes
		//3. delete internal nodes
		int nTotal = 0;
		int[] offsets = new int[polygons.size()];
		//int[] leftmost = new int[]{-1,-1};
		//float minX = -Float.MAX_VALUE;
		for (int i = 0; i < polygons.size(); i++){
			offsets[i] = nTotal;
			nTotal += polygons.get(i).vertices.size();
			//for (int p = 0; p < polygons.get(i).nodes.size(); p++)
			//	if (polygons.get(i).nodes.get(p).x < minX){
			//		leftmost[0] = i;
			//		leftmost[1] = p;
			//		minX = polygons.get(i).nodes.get(p).x;
			//		}
			}
		
		boolean[] remNodes = new boolean[nTotal];
		int[] joinNodes = new int[nTotal];
		int[] nodePolys = new int[nTotal];
		int o = 0;
		for (int i = 0; i < nTotal; i++){
			if (i == offsets[o]) o++;
			nodePolys[i] = o;
			joinNodes[i] = -1;
			}
		Point2f[] intNodes = new Point2f[nTotal];
		
		for (int i = 0; i < polygons.size(); i++){
			Polygon2D poly1 = polygons.get(i);
			for (int j = 0; j < polygons.size(); j++)
				if (i != j){
					Polygon2D poly2 = polygons.get(j);
					if (GeometryFunctions.crosses(poly1.getBounds(), poly2.getBounds())){
						//get list of intersecting edges
						boolean[][] intersects = GeometryFunctions.getSegmentsIntersect(poly1, poly2);
						//for each intersection pair, get intersection point
						for (int p = 0; p < poly1.vertices.size(); p++)
							for (int q = 0; q < poly2.vertices.size(); q++)
								if (intersects[p][q]){
									int p2 = p + 1; if (p2 == poly1.vertices.size()) p2 = 0;
									int q2 = q + 1; if (q2 == poly2.vertices.size()) q2 = 0;
									int i_p = offsets[i] + p;
									int j_q = offsets[j] + q;
 									Point2f pt = GeometryFunctions.getIntersectionPoint(
																new LineSegment2D(poly1.getVertex(p), poly1.getVertex(p2)),
																new LineSegment2D(poly2.getVertex(q), poly2.getVertex(q2)));
									intNodes[i_p] = pt;
									intNodes[j_q] = pt;
									joinNodes[i_p] = j_q;
									joinNodes[j_q] = i_p;
									}
								
						}
					}
			
			
			}
		
		//with this info:
		//1. build new polygon (possibly with holes)
		
		//start at leftmost node, which must be external
		//start new polygon thisPoly
		Polygon2D thisPoly = new Polygon2D();
		Point2f thisNode, startNode = null;
		
		//set startNode
		//loop through nodes
		//if thisNode = startNode, 
		//		add thisPoly to list 
		//		start new thisPoly
		//if thisNode is already processed
		//		break
		//if node's edge has no intersect
		//		add to thisPoly
		//otherwise
		//		add intersection point
		//		jump to connected node
		
		boolean isDone = false, isNew = false, isAdding = false;
		boolean[] isProcessed = new boolean[nTotal];
		//find first node with intersecting edge
		int currentPoly = 0;
		int currentNode = 0;
		int currentIndex = 0;
		int startIndex = -1;
		
		while (startIndex < -1 && currentIndex < nTotal)
			if (joinNodes[currentIndex] >= 0)
				startIndex = currentIndex;
				
		//in this case, no intersections occur
		if (startIndex < 0) return polygons;
		
		currentPoly = nodePolys[startIndex];
		currentNode = startIndex - offsets[currentPoly];
		currentIndex = startIndex;
		
		//test next node
		int nextNode = currentNode + 1;
		if (nextNode == polygons.get(currentPoly).vertices.size())
			nextNode = 0;
		
		Point2f testNode = polygons.get(currentPoly).getVertex(nextNode);
		
		//if next node is internal, switch polygons
		if (polygons.get(nodePolys[joinNodes[currentIndex]]).contains(testNode)){
			currentPoly = nodePolys[joinNodes[currentIndex]];
			currentIndex = joinNodes[currentIndex];
			currentNode = currentIndex - offsets[currentPoly];
			}
		
		//startNode = polygons.get(currentPoly).nodes.get(currentNode);
		//thisPoly.addNode(startNode);
		ArrayList<Polygon2D> returnPolys = new ArrayList<Polygon2D>();
		
		do{
			thisNode = polygons.get(currentPoly).getVertex(currentNode);
			if (thisNode == startNode){
				returnPolys.add(thisPoly);
				thisPoly = new Polygon2D();
				isNew = true;
				}
			
			if (!(isNew || isDone)){
				//if this node has no intersect, add it
				if (joinNodes[currentIndex] < 0){
					if (isAdding){
						
					}else{
						
						}
					
				}else{
					//otherwise add intersection point and 
					
					}
				
				
				
				}
			
			currentNode++;
			if (currentNode > polygons.get(currentPoly).vertices.size()){
				currentPoly++;
				if (currentPoly == polygons.size()) currentPoly = 0;
				currentNode = 0;
				}
			currentIndex = offsets[currentPoly] + currentNode;
			
			//search for next unprocessed node
			while (currentIndex != startIndex && isProcessed[currentIndex]){
				currentNode++;
				if (currentNode > polygons.get(currentPoly).vertices.size()){
					currentPoly++;
					if (currentPoly == polygons.size()) currentPoly = 0;
					currentNode = 0;
					}
				currentIndex = offsets[currentPoly] + currentNode;
				}
			
			//in this case all nodes have been processed
			if (currentIndex == startIndex) break;
			
			isNew = false;
			} while(!isDone);
		
		return returnPolys;
	}
	
	/**************************************
	 * Returns a set of cylindrical segments which follow the path of the
	 * given polygon. Parameters specify its appearance.
	 * TODO: fix junction artifacts 
	 * 
	 * @param poly
	 * @param radius
	 * @param res
	 * @param closed
	 * @param app
	 * @return
	 */
	public static BranchGroup getCylinderPolygon(Polygon3D poly, 
												 float radius, 
												 int res,
												 Appearance app){
		
		//get cylinder for each segment
		BranchGroup group = new BranchGroup();
		group.setCapability(BranchGroup.ALLOW_DETACH);
		ArrayList<Point3f> nodes = poly.getVertices();
		
		for (int i = 0; i < nodes.size() - 1; i++)
			group.addChild(getCylinderSegment(nodes.get(i), nodes.get(i+1),
											  radius, res, app));
		
		/*
		if (closed)
			group.addChild(getCylinderSegment(nodes.get(nodes.size() - 1), nodes.get(0),
											  radius, res, app));
		*/
		
		return group;
	}
	
	public static BranchGroup getCylinderSegment(Point3f p1, Point3f p2, 
										  float radius, int res,
										  Appearance app){
		
		CylinderCreator creator = new CylinderCreator();
		creator.setResolution(res);
		return creator.create(p1, p2, radius, app);
	}
	
	
	static class CylinderCreator {

		  int edges;

		  /**
		   * Constructs a cylinder with 7 edges by default.
		   */
		  public CylinderCreator() {
		    edges = 7;    // e.g. "edges = 8" would look like a stop sign
		  }

		  /**
		   * Sets the resolution (number of edges) of the Cylinder.
		   * @param e Number of edges (e.g. 8 would look like a stop sign).
		   */
		  public void setResolution(int e) {
		    edges = e;
		  }
		  

		  /**
		   * Creates a cylinder.
		   * @return A BranchGroup containing the cylinder in the desired orientation.
		   * @param b coordinates of the base of the cylinder.
		   * @param a coordinates of the top of the cylinder.
		   * @param radius radius of the cylinder.
		   * @param cylApp cylinder Appearance.
		   * @author Scott Teresi, March 1999, www.teresi.us
		   */
		  public BranchGroup create (Point3f b, Point3f a, float radius,
					     				Appearance cylApp) {

		    Vector3f base = new Vector3f();
		    base.x = b.x;
		    base.y = b.y;
		    base.z = b.z;
		    Vector3f apex = new Vector3f();
		    apex.x = a.x;
		    apex.y = a.y;
		    apex.z = a.z;

		    // calculate center of object
		    Vector3f center = new Vector3f();
		    center.x = (apex.x - base.x) / 2.0f + base.x;
		    center.y = (apex.y - base.y) / 2.0f + base.y;
		    center.z = (apex.z - base.z) / 2.0f + base.z;

		    // calculate height of object and unit vector along cylinder axis
		    Vector3f unit = new Vector3f();
		    unit.sub(apex, base);  // unit = apex - base;
		    double height = unit.length();
		    unit.normalize();

		    /* A Java3D cylinder is created lying on the Y axis by default.
		       The idea here is to take the desired cylinder's orientation
		       and perform a tranformation on it to get it ONTO the Y axis.
		       Then this transformation matrix is inverted and used on a
		       newly-instantiated Java 3D cylinder. */

		    // calculate vectors for rotation matrix
		    // rotate object in any orientation, onto Y axis (exception handled below)
		    // (see page 418 of _Computer Graphics_ by Hearn and Baker)
		    Vector3f uX = new Vector3f();
		    Vector3f uY = new Vector3f();
		    Vector3f uZ = new Vector3f();
		    float magX;
		    Transform3D rotateFix = new Transform3D();

		    uY = new Vector3f(unit);
		    uX.cross(unit, new Vector3f(0, 0, 1));
		    magX = uX.length();
		    // magX == 0 if object's axis is parallel to Z axis
		    if (magX != 0) {
		      uX.z = uX.z / magX;
		      uX.x = uX.x / magX;
		      uX.y = uX.y / magX;
		      uZ.cross(uX, uY);
		    }
		    else {
		      // formula doesn't work if object's axis is parallel to Z axis
		      // so rotate object onto X axis first, then back to Y at end
		      float magZ;
		      // (switched z -> y, y -> x, x -> z from code above)
		      uX = new Vector3f(unit);
		      uZ.cross(unit, new Vector3f(0, 1, 0));
		      magZ = uZ.length();
		      uZ.x = uZ.x / magZ;
		      uZ.y = uZ.y / magZ;
		      uZ.z = uZ.z / magZ;
		      uY.cross(uZ, uX);
		      // rotate object 90 degrees CCW around Z axis--from X onto Y
		      rotateFix.rotZ(Math.PI / 2.0);
		    }

		    // create the rotation matrix
		    Transform3D transMatrix = new Transform3D();
		    Transform3D rotateMatrix =
			new Transform3D(new Matrix4f(uX.x, uX.y, uX.z, 0,
						     uY.x, uY.y, uY.z, 0,
						     uZ.x, uZ.y, uZ.z, 0,
						     0,  0,  0,  1));
		    // invert the matrix; need to rotate it off of the Z axis
		    rotateMatrix.invert();
		    // rotate the cylinder into correct orientation
		    transMatrix.mul(rotateMatrix);
		    transMatrix.mul(rotateFix);
		    // translate the cylinder away
		    transMatrix.setTranslation(center);
		    // create the transform group
		    TransformGroup tg = new TransformGroup(transMatrix);

		    Cylinder cyl = new Cylinder(radius, (float) height,
										Primitive.GENERATE_NORMALS, 
										edges, 1, 
										cylApp);
		    tg.addChild(cyl);
		    BranchGroup cylBg = new BranchGroup();
		    cylBg.addChild(tg);
		    return cylBg;
		  }

		}
	
	public static ArrayList<MguiNumber> getDataMaskedDoubleValues(ArrayList<MguiNumber> input, MeshDataMaskOptions options){
		return getDataMaskedDoubleValues(input, options, null);
	}
		
		
	public static ArrayList<MguiNumber> getDataMaskedDoubleValues(ArrayList<MguiNumber> input, 
																  MeshDataMaskOptions options,
																  VertexSelection selection){
		ArrayList<MguiNumber> values = new ArrayList<MguiNumber>();
		
		for (int i = 0; i < input.size(); i++){
			double d = options.out;
			if (selection == null || selection.isSelected(i)){
				double v = input.get(i).getValue(); 
				for (int j = 0; j < options.vals.length; j++)
					if (v == options.vals[j]) d = options.in;
				}
			values.add(new MguiDouble(d));
			}
		return values;
	}
	
	public static ArrayList<MguiNumber> getDataMaskedIntegerValues(ArrayList<MguiNumber> input, 
			 													   MeshDataMaskOptions options){
		return getDataMaskedIntegerValues(input, options, null);
	}
	
	public static ArrayList<MguiNumber> getDataMaskedIntegerValues(ArrayList<MguiNumber> input, 
																   MeshDataMaskOptions options,
																   VertexSelection selection){
		
		ArrayList<MguiNumber> values = new ArrayList<MguiNumber>();
		
		for (int i = 0; i < input.size(); i++){
			int d = (int)options.out;
			if (selection == null || selection.isSelected(i)){
				int v = (int)input.get(i).getValue(); 
				for (int j = 0; j < options.vals.length; j++)
					if (v == options.vals[j]) d = (int)options.in;
				}
			values.add(new MguiInteger(d));
			}
		return values;
	}
	
	/***********************************************************************
	 * Normalize values in {@code source_column} to from the range {@code [source_min, source_max]} to
	 * the range {@code [target_min, target_max]}
	 * 
	 * @param volume
	 * @param source_column
	 * @param source_min
	 * @param source_max
	 * @param target_min
	 * @param target_max
	 * @return
	 */
	public static ArrayList<MguiNumber> getNormalizedValues(InterfaceShape shape, 
															String source_column,
															double source_min, double source_max,
															double target_min, double target_max){
	
		ArrayList<MguiNumber> s_column = shape.getVertexData(source_column);
		if (s_column == null){
			InterfaceSession.log("ShapeFunctions.getNormalizedValues: column '" + source_column + "' doesn't exist.", 
								 LoggingType.Errors);
			return null;
			}
		
		double s_range = source_max - source_min;
		if (s_range <= 0){
			InterfaceSession.log("ShapeFunctions.getNormalizedValues: source_min must be less than source_max", 
					 LoggingType.Errors);
			return null;
			}
		double t_range = target_max - target_min;
		if (t_range <= 0){
			InterfaceSession.log("ShapeFunctions.getNormalizedValues: target_min must be less than target_max", 
					 LoggingType.Errors);
			return null;
			}
		
		ArrayList<MguiNumber> normalized = new ArrayList<MguiNumber>();
		for (int i = 0; i < s_column.size(); i++){
			MguiNumber value = (MguiNumber)s_column.get(i);
			MguiNumber new_value = (MguiNumber)value.clone();
			new_value.subtract(source_min);
			new_value.divide(s_range);
			new_value.add(target_min);
			new_value.multiply(t_range);
			if (i == 144816)
				i+=0;
			normalized.add(new_value);
			}
		
		
		return normalized;
	}
	
	/***********************************************************************
	 * Normalize values in {@code source_column} by the mean of values in {@code normalize_column} which
	 * lie in the mask {@code mask_column}. {@code source_column} and {@code normalize_column} can be the same.
	 * Elements are considered to in the mask if their value in {@code mask_column} is >= zero.
	 * 
	 * <p> Returned values are of the same type as {@code source_column}.
	 * 
	 * @param volume
	 * @param source_column
	 * @param normalize_column
	 * @param mask_column
	 * @return
	 */
	public static ArrayList<MguiNumber> getMaskNormalizedValues(InterfaceShape shape, 
																String source_column, 
																String normalize_column,
																String mask_column){
		
		return getMaskNormalizedValues(shape, source_column, normalize_column, mask_column, "mean");
		
	}
	
	/***********************************************************************
	 * Normalize values in {@code source_column} by the {@code statistic} of values in {@code normalize_column} which
	 * lie in the mask {@code mask_column}. {@code source_column} and {@code normalize_column} can be the same.
	 * Elements are considered to in the mask if their value in {@code mask_column} is >= zero.
	 * 
	 * <p> Returned values are of the same type as {@code source_column}.
	 * 
	 * <p> Valid values of {@code statistic} are:
	 * 
	 * <ul>
	 * <li> mean
	 * <li> max
	 * <li> min
	 * <li> sum
	 * </ul>
	 * 
	 * @param volume
	 * @param source_column
	 * @param normalize_column
	 * @param mask_column
	 * @param statistic
	 * @return
	 */
	public static ArrayList<MguiNumber> getMaskNormalizedValues(InterfaceShape shape, 
																String source_column, 
																String normalize_column,
																String mask_column,
																String statistic){
		
		ArrayList<MguiNumber> s_column = shape.getVertexData(source_column);
		if (s_column == null){
			InterfaceSession.log("ShapeFunctions.getMaskNormalizedValues: column '" + source_column + "' doesn't exist.", 
								 LoggingType.Errors);
			return null;
			}
		ArrayList<MguiNumber> n_column = shape.getVertexData(normalize_column);
		if (n_column == null){
			InterfaceSession.log("ShapeFunctions.getMaskNormalizedValues: column '" + normalize_column + "' doesn't exist.", 
								 LoggingType.Errors);
			return null;
			}
		ArrayList<MguiNumber> m_column = shape.getVertexData(mask_column);
		if (m_column == null){
			InterfaceSession.log("VolumeFunctions.getMaskNormalizedValues: column '" + mask_column + "' doesn't exist.", 
								 LoggingType.Errors);
			return null;
			}
		ArrayList<MguiNumber> out_column = new ArrayList<MguiNumber>(s_column.size());
		
		statistic = statistic.toLowerCase();
		if (!(statistic.equals("mean") || statistic.equals("min") || statistic.equals("max") || statistic.equals("sum"))){
			InterfaceSession.log("ShapeFunctions.getMaskNormalizedValues: statistic must be one of: mean, min, max, sum", 
					 			 LoggingType.Errors);
			return null;
			}
		
		double stat = 0;
		double denom = 0;
		
		// Get statistic from masked values
		for (int i = 0; i < n_column.size(); i++){
			if (m_column.get(i).getValue() > 0){
				double v = n_column.get(i).getValue();
				if (statistic.equals("mean")){
					stat += v;
					denom++;
				}else if (statistic.equals("min")){
					stat = Math.min(v, stat);
				}else if (statistic.equals("max")){
					stat = Math.max(v, stat);
				}else {
					stat += v;
					}
				}
			}
		
		if (statistic.equals("mean")){
			stat /= denom;
			}
		
		// Normalize values
		if (stat == 0){
			InterfaceSession.log("ShapeFunctions.getMaskNormalizedValues: normalizing stat is zero!", 
		 			 			 LoggingType.Errors);
			return null;
			}
		
		for (int i = 0; i < s_column.size(); i++){
			MguiNumber val = (MguiNumber)s_column.get(i).clone();
			val.divide(stat);
			out_column.add(val);
			}
		
		return out_column;
		
	}
	
	/******************************************
	 * Returns the up and down clipping planes for this plane
	 * @param plane
	 * @return
	 */
	public static ModelClip getModelClip(Plane3D plane, float up, float down, boolean invert){
		
		ModelClip clip = new ModelClip();
		setModelClip(clip, plane, up, down, invert);
		
		//two planes enabled
		clip.setEnables(new boolean[]{true, true, false, false, false, false});
		
		//TODO: set capabilities here if plane is to be read/written
		clip.setCapability(ModelClip.ALLOW_PLANE_WRITE);
		clip.setInfluencingBounds(new BoundingSphere(new Point3d(), Double.POSITIVE_INFINITY));
		clip.setCapability(ModelClip.ALLOW_SCOPE_WRITE);
		clip.setCapability(ModelClip.ALLOW_SCOPE_READ);
		
		return clip;
		
	}
	
	/********************************************************************
	 * Sets {@code clip} to match the specified plane and clipping distances.
	 * 
	 * @param clip
	 * @param plane
	 * @param up
	 * @param down
	 * @param invert
	 */
	public static void setModelClip(ModelClip clip, Plane3D plane, float up, float down, boolean invert){
		
		float flip = 1;
		if (invert) flip = -1;
		
		Vector3f normal = new Vector3f(plane.getNormal());
		
		//up clip
		Plane3D _plane = new Plane3D(plane);
		if (invert)
			_plane.flip_normal = !_plane.flip_normal;
		Vector3f d = _plane.getNormal();
		d.scale(up);
		Point3f origin = _plane.getOrigin();
		origin.add(d);
		_plane.setOrigin(origin);
		Vector4d p1 = _plane.getAsVector4d();
		//p1.scale(flip);
		clip.setPlane(0, p1);
		
		//plane.getOrigin().sub(d);
		_plane = new Plane3D(plane);
		if (!invert)
			_plane.flip_normal = !_plane.flip_normal;
		d = _plane.getNormal(); // new Vector3f(normal);
		d.scale(down);
		origin = _plane.getOrigin();
		origin.add(d);
		_plane.setOrigin(origin);
		//plane.getOrigin().sub(d);
		p1 = _plane.getAsVector4d();
		//p1.scale(-flip);
		clip.setPlane(1, p1);
		
	}
	
	/****************************************
	 * Returns a mask for <code>volume</code> which is above or below <code>plane</code>.
	 * 
	 * @param volume
	 * @param plane
	 * @param is_above
	 * @return
	 */
	public static void unionMaskVolumeWithPlane(boolean[][][] mask,
												Volume3DInt volume,
												Plane3D plane,
												boolean is_above){
		
		Grid3D grid = volume.getGrid();
		int count = 0;
		int x_size = grid.getSizeS();
		int y_size = grid.getSizeT();
		int z_size = grid.getSizeR();
		
		for (int i = 0; i < x_size; i++)
			for (int j = 0; j < y_size; j++)
				for (int k = 0; k < z_size; k++){
					if (!mask[i][j][k])
						mask[i][j][k] = (GeometryFunctions.isAbovePlane(volume.getCenterOfVoxel(i, j, k), plane) == is_above);
					if (mask[i][j][k]) count++;
					}
		
		int n = x_size * y_size * z_size - count;
		InterfaceSession.log("Union mask: " + count + " masked, " + n + " unmasked..");
	}
	
	/*********************************************************************
	 * Returns a list of vertex indices from {@code source} which are nearest neighbours of the corresponding vertices
	 * of {@code target}.  
	 * 
	 * @param source
	 * @param target
	 * @param search_max The maximum search radius; if no neighbours are found, the corresponding value is -1
	 * @return a list of indices, of size {@code target.size()} corresponding to the nearest neighbours in {@code source}.
	 */
	public static ArrayList<Integer> getNearestNeighbours(InterfaceShape source, InterfaceShape target){
		
		if (source instanceof Shape3DInt && target instanceof Shape3DInt){
			
			return getNearestNeighbour3DBlocking((Shape3DInt)source, (Shape3DInt)target);
			
			}
		
		return null;
	}
	
	protected static ArrayList<Integer> getNearestNeighbour3DBlocking(Shape3DInt source, Shape3DInt target){
		
		ArrayList<Integer> neighbours = new ArrayList<Integer>();
				
		ArrayList<Point3f> source_vertices = source.getVertices();
		ArrayList<Point3f> target_vertices = target.getVertices();
		
		K3DBinaryTree kd_tree = new K3DBinaryTree(source_vertices);
		
		//temp debug
		//return kd_tree.getTreeValues(3);
		
		
		for (int i = 0; i < target_vertices.size(); i++){
			Point3f point = target_vertices.get(i);
			
			neighbours.add(kd_tree.getNearestNeighbour(point));
			}
		
		return neighbours;
		
	}
	
	public static class Point3fComparatorX implements Comparator<Point3f>{
		public int compare(Point3f n1, Point3f n2) {
			return GeometryFunctions.compareFloat(n1.x, n2.x);
		}
	}
	
	public static class Point3fComparatorY implements Comparator<Point3f>{
		public int compare(Point3f n1, Point3f n2) {
			return GeometryFunctions.compareFloat(n1.y, n2.y);
		}
	}
	
	public static class Point3fComparatorZ implements Comparator<Point3f>{
		public int compare(Point3f n1, Point3f n2) {
			return GeometryFunctions.compareFloat(n1.z, n2.z);
		}
	}
	
	public static AttributeList getDefaultShapeAttributes2D(){
		Shape2DInt X = new Shape2DInt(){};
		return X.getAttributes();
	}
	
	public static AttributeList getDefaultShapeAttributes3D(){
		Shape3DInt X = new Shape3DInt(){};
		return X.getAttributes();
	}
	
	
}