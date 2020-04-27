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

package mgui.interfaces.shapes.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;

import org.jogamp.vecmath.Matrix4d;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3d;

import mgui.geometry.Mesh3D;
import mgui.geometry.Shape3D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.VertexDataColumn;
import mgui.interfaces.util.Engine;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiNumber;

/****************************************************
 * Engine class for general shape functions.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeEngine implements Engine {

	HashMap<String, AttributeList> attributes = new HashMap<String, AttributeList>();
	
	public ShapeEngine(){
		init();
	}
	
	private void init(){
		
		AttributeList thisList = new AttributeList();
		thisList.add(new Attribute<MguiDouble>("x_shift", new MguiDouble(0)));
		thisList.add(new Attribute<MguiDouble>("y_shift", new MguiDouble(0)));
		thisList.add(new Attribute<MguiDouble>("z_shift", new MguiDouble(0)));
		attributes.put("Transform Shape (Translate)", thisList);
		thisList = new AttributeList();
		thisList.add(new Attribute<MguiDouble>("base_pt_x", new MguiDouble(0)));
		thisList.add(new Attribute<MguiDouble>("base_pt_y", new MguiDouble(0)));
		thisList.add(new Attribute<MguiDouble>("base_pt_z", new MguiDouble(0)));
		thisList.add(new Attribute<MguiDouble>("angle_x_yaw", new MguiDouble(0)));
		thisList.add(new Attribute<MguiDouble>("angle_y_pitch", new MguiDouble(0)));
		thisList.add(new Attribute<MguiDouble>("angle_z_roll", new MguiDouble(0)));
		attributes.put("Transform Shape (Rotate YPR)", thisList);
		
		thisList = new AttributeList();
		thisList.add(new Attribute<Matrix4d>("Matrix", new Matrix4d()));
		attributes.put("Transform Shape (Matrix)", thisList);
		
		thisList = new AttributeList();
		thisList.add(new Attribute<String>("surface_name","convex_hull"));
		AttributeSelection<String> attr = new AttributeSelection<String>("target_shape_set", new ArrayList<String>(), String.class);
		thisList.add(attr);
		attributes.put("Convex Hull (Maven)", thisList);
		thisList = new AttributeList();
		thisList.add(new Attribute<String>("surface_name","convex_hull"));
		attr = new AttributeSelection<String>("target_shape_set", new ArrayList<String>(), String.class);
		thisList.add(attr);
		attributes.put("Convex Hull (QuickHull)", thisList);
		thisList = new AttributeList();
		thisList.add(new Attribute<String>("surface_name","convex_hull"));
		attr = new AttributeSelection<String>("target_shape_set", new ArrayList<String>(), String.class);
		thisList.add(attr);
		attributes.put("Convex Hull (GiftWrap)", thisList);
		thisList = new AttributeList();
		thisList.add(new Attribute<String>("surface_name","convex_hull"));
		attr = new AttributeSelection<String>("target_shape_set", new ArrayList<String>(), String.class);
		thisList.add(attr);
		attributes.put("Convex Hull (Divide & Conquer)", thisList);
		
		thisList = new AttributeList();
		attr = new AttributeSelection<String>("source_column", new ArrayList<String>(), String.class);
		thisList.add(attr);
		attr = new AttributeSelection<String>("target_column", new ArrayList<String>(), String.class);
		attr.allowUnlisted(true);
		thisList.add(attr);
		attr = new AttributeSelection<String>("normalize_column", new ArrayList<String>(), String.class);
		thisList.add(attr);
		attr = new AttributeSelection<String>("mask_column", new ArrayList<String>(), String.class);
		thisList.add(attr);
		ArrayList<String> stats = new ArrayList<String>();
		stats.add("mean");
		stats.add("min");
		stats.add("max");
		stats.add("sum");
		attr = new AttributeSelection<String>("statistic", stats, String.class, "mean");
		thisList.add(attr);
		attributes.put("Normalize (Mask)", thisList);
		
		thisList = new AttributeList();
		attr = new AttributeSelection<String>("source_column", new ArrayList<String>(), String.class);
		thisList.add(attr);
		attr = new AttributeSelection<String>("target_column", new ArrayList<String>(), String.class);
		attr.allowUnlisted(true);
		thisList.add(attr);
		thisList.add(new Attribute<String>("source_min", "min"));
		thisList.add(new Attribute<String>("source_max", "max"));
		thisList.add(new Attribute<String>("target_min", "0.0"));
		thisList.add(new Attribute<String>("target_max", "1.0"));
		attributes.put("Normalize (Values)", thisList);
		
	}
	
	public AttributeList getAttributes(String key){
		return attributes.get(key);
	}
	
	@Override
	public AttributeList getAttributes(String operation, String method){
		if (method != null)
			return getAttributes(operation + " (" + method + ")");
		return getAttributes(operation);
	}
	
	@Override
	public ArrayList<String> getOperations(){
		ArrayList<String> ops = new ArrayList<String>(attributes.keySet());
		TreeSet<String> u_ops = new TreeSet<String>();
		for (int i = 0; i < ops.size(); i++){
			String op = ops.get(i);
			if (op.contains(" ("))
				u_ops.add(op.substring(0, op.indexOf(" (")));
			else
				u_ops.add(op);
			}
		ops = new ArrayList<String>(u_ops);
		Collections.sort(ops);
		return ops;
	}
	
	@Override
	public ArrayList<String> getMethods(String operation){
		
		ArrayList<String> ops = new ArrayList<String>(attributes.keySet());
		ArrayList<String> methods = new ArrayList<String>();
		for (int i = 0; i < ops.size(); i++){
			String op = ops.get(i);
			if (op.startsWith(operation) && op.contains(" ("))
				methods.add(op.substring(op.indexOf("(") + 1, op.indexOf(")")));
			}
		Collections.sort(methods);
		return methods;
	}
	
	@Override
	public boolean callMethod(String operation, String method, ProgressUpdater progress){
		return callMethod(operation, method, null, null);
	}
	
	@Override
	public boolean callMethod(String operation, String method, ArrayList<?> params, ProgressUpdater progress){
		
		if (operation.equals("Transform Shape")){
			if (params.size() != 1){
				InterfaceSession.log("ShapeEngine.callMethod: Operation 'Transform Shape' requires one parameter " +
						 "(shape)", 
						 LoggingType.Errors);
				return false;
				}
				
			return transformShape3D((Shape3DInt)params.get(0),
									 method,
									 progress);
			}
		
		if (operation.equals("Compute Convex Hull")){
			if (params.size() != 1){
				InterfaceSession.log("ShapeEngine.callMethod: Operation 'Compute Convex Hull' requires one parameter " +
						 "(shape)", 
						 LoggingType.Errors);
				return false;
				}
			return computeConvexHull((Shape3DInt)params.get(0),
									 method,
								 	 progress);
			}
		
		if (operation.equals("Normalize")){
			if (params.size() != 1){
				InterfaceSession.log("ShapeEngine.callMethod: Operation 'Normalize' requires one parameter " +
						 "(shape)", 
						 LoggingType.Errors);
				return false;
				}
			if (method.equals("Mask")){
				return normalizeColumnWithMask((InterfaceShape)params.get(0),
									 	 		progress);
				}
			if (method.equals("Values")){
				return normalizeColumn((InterfaceShape)params.get(0),
									 	progress);
				}
			}
		
		return false;
	}
	
	/***************************************************
	 * Transforms <code>shape</code> according to <code>method</code>. The parameters are specified in the
	 * attribute list corresponding to the method. This can be one of either:
	 * 
	 * <ol>
	 * <li>Translate: performs a simple translation based upon the attributes
	 * <li>Matrix Transform: performs an affine transform based upon the matrix specified in the attributes
	 * </ol>
	 * 
	 * <p>If <code>shape</code> is an instance of <code>ShapeSet3DInt</code>, this method will transform all
	 * its members (including subsets) recursively.
	 * 
	 * @param shape Shape to be transformed
	 * @param method Method to use; one of either "Translate" or "Matrix Transform"
	 * @param progress Optional progress updater 
	 * @return <code>true</code> if successful
	 */
	public boolean transformShape3D(Shape3DInt shape, String method, ProgressUpdater progress){
		
		boolean success = true;
		
		//if this is a shape set, do some recursion
		if (shape instanceof ShapeSet3DInt){
			ArrayList<Shape3DInt> members = ((ShapeSet3DInt)shape).get3DShapes(false);
			for (int i = 0; i < members.size(); i++)
				success &= transformShape3D(members.get(i), method, progress);
			return success;
			}
		
		if (method.equals("Translate")){
			return translateShape(shape.getShape());
			}
		
		if (method.equals("Matrix")){
			AttributeList attr = attributes.get("Transform Shape (Matrix)");
			Matrix4d matrix = (Matrix4d)attr.getValue("Matrix");
			Shape3D shape3d = ShapeFunctions.transformWithMatrix(shape.getShape(), matrix, progress);
			if (shape3d == null) return false;
			shape.setShape(shape3d);
			return true;
			}
		
		if (method.equals("Rotate YPR")){
			return rotateShapeYPR(shape.getShape());
			}
		
		return false;
	}
	
	protected boolean translateShape(Shape3D shape){
		AttributeList list = attributes.get("Transform Shape (Translate)");
		GeometryFunctions.translate(shape, ((MguiDouble)list.getValue("x_shift")).getValue(),
										   ((MguiDouble)list.getValue("y_shift")).getValue(),
										   ((MguiDouble)list.getValue("z_shift")).getValue());
		InterfaceSession.log("Translated surface [" + ((MguiDouble)list.getValue("x_shift")).getValue() + ", " +
							((MguiDouble)list.getValue("y_shift")).getValue() + ", " +
							((MguiDouble)list.getValue("z_shift")).getValue() + "]",
							LoggingType.Verbose);
		return true;
	}
	
	protected boolean rotateShapeYPR(Shape3D shape){
		AttributeList attr = attributes.get("Transform Shape (Rotate YPR)");
		Point3d base_pt = new Point3d(((MguiDouble)attr.getValue("base_pt_x")).getValue(),
									  ((MguiDouble)attr.getValue("base_pt_y")).getValue(),
									  ((MguiDouble)attr.getValue("base_pt_z")).getValue());
		
		double x_angle_yaw = ((MguiDouble)attr.getValue("angle_x_yaw")).getValue();
		double y_angle_pitch = ((MguiDouble)attr.getValue("angle_y_pitch")).getValue();
		double z_angle_roll = ((MguiDouble)attr.getValue("angle_z_roll")).getValue();
		
		Vector3d T = new Vector3d(0,0,0);
		T.sub(base_pt);
		
		// Translate base pt to origin, rotate, translate back
		GeometryFunctions.rotate(shape, base_pt, new Vector3d(1,0,0) , x_angle_yaw);
		GeometryFunctions.rotate(shape, base_pt, new Vector3d(0,1,0) , y_angle_pitch);
		GeometryFunctions.rotate(shape, base_pt, new Vector3d(0,0,1) , z_angle_roll);
		
		return true;
	}
	
	/***********************************************************
	 * Computes a convex hull for {@code shape}.
	 * 
	 * @param shape
	 * @param progress
	 * @return
	 */
	public boolean computeConvexHull(Shape3DInt shape, String method, ProgressUpdater progress){
		
		AttributeList list = this.getAttributes("Convex Hull", method);
		if (list == null){
			InterfaceSession.log("ShapeEngine.computeConvexHull: Invalid method '" + method + "'",
								 LoggingType.Errors);
			return false;
			}
		String t_set = (String)list.getValue("target_shape_set");
		Shape3DInt target_set = (Shape3DInt)InterfaceSession.getWorkspace().getShapeForName(t_set);
		if (target_set == null || !(target_set instanceof ShapeSet3DInt)){
			InterfaceSession.log("ShapeEngine.computeConvexHull: No/invalid target shape set specified!",
								 LoggingType.Errors);
			return false;
			}
		Mesh3D mesh = ShapeFunctions.getConvexHull(shape, method, progress);
		if (mesh == null){
			InterfaceSession.log("ShapeEngine.computeConvexHull: Convex hull failed or was cancelled; see log.",
					 LoggingType.Errors);
			return false;
			}
		String name = (String)list.getValue("surface_name");
		return ((ShapeSet3DInt)target_set).addShape(new Mesh3DInt(mesh, name));
		
	}
	
	/***************************************************************
	 * Normalizes the values in a column. This method depends on the attribute list
	 * "Normalize (Values)". Attributes are:
	 * 
	 * <ul>
	 * <li>source_column - Column containing the values to normalize
	 * <li>target_column - Column to store results; if this column doesn't exist it will be created
	 * with the same data type as the source column. Can also be the same as the source column (in which
	 * case the original values will be overwritten).
	 * <li>source_min - Minimum of the source; use "min" to compute from the data, or enter a value to specify a
	 * specific minimum.
	 * <li>source_max - Maximum of the source; use "max" to compute from the data, or enter a value to specify a
	 * specific maximum.
	 * <li>source_min - Minimum of the target distribution
	 * <li>source_max - Maximum of the target distribution
	 * </ul>
	 * 
	 * @param shape
	 * @param progress
	 * @return
	 */
	public boolean normalizeColumn(InterfaceShape shape, ProgressUpdater progress){
		AttributeList attr = attributes.get("Normalize (Values)");
		
		String source_column = (String)attr.getValue("source_column");
		if (source_column == null || !shape.hasColumn(source_column)){
			InterfaceSession.log("ShapeEngine.normalizeColumnWithMask: Invalid source column.", 
								 LoggingType.Errors);
			return false;
			}
		String target_column = (String)attr.getValue("target_column");
		if (target_column == null){
			InterfaceSession.log("ShapeEngine.normalizeColumnWithMask: Invalid target column.'", 
								 LoggingType.Errors);
			return false;
			}
		if (!shape.hasColumn(target_column)){
			// Make new column, with same data type as source column
			VertexDataColumn s_column = shape.getVertexDataColumn(source_column);
			int type = s_column.getDataTransferType();
			shape.addVertexData(target_column, type);
			}
		
		String s_min = (String)attr.getValue("source_min");
		double source_min = Double.MAX_VALUE, source_max = -Double.MAX_VALUE;
		boolean compute_min = s_min.toLowerCase().equals("min");
		if (!compute_min)
			source_min = Double.valueOf(s_min);
		String s_max = (String)attr.getValue("source_max");
		boolean compute_max = s_max.toLowerCase().equals("max");
		if (!compute_max)
			source_min = Double.valueOf(s_max);
		double target_min = Double.valueOf((String)attr.getValue("target_min"));
		double target_max = Double.valueOf((String)attr.getValue("target_max"));
		
		if (compute_min || compute_max){
			ArrayList<MguiNumber> values = shape.getVertexData(source_column);
			for (int i = 0; i < values.size(); i++){
				if (compute_min)
					source_min = Math.min(source_min, values.get(i).getValue());
				if (compute_max)
					source_max = Math.max(source_max, values.get(i).getValue());
				}
			}
		
		ArrayList<MguiNumber> values = ShapeFunctions.getNormalizedValues(shape, 
																		  source_column, 
																		  source_min, source_max,
																		  target_min, target_max);
		
		if (values == null) return false;
		
		shape.getVertexDataColumn(target_column).setValues(values, true);
		
		return true;
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
	 * <p> Note that these parameters are contained in the {@code AttributeList} called "Normalize With Mask", which
	 * must be set prior to calling the method.
	 * 
	 * @param volume
	 * @param source_column
	 * @param normalize_column
	 * @param mask_column
	 * @param statistic
	 * @return
	 */
	public boolean normalizeColumnWithMask(InterfaceShape shape, ProgressUpdater progress){
		AttributeList attr = attributes.get("Normalize (Mask)");
		
		String target_column = (String)attr.getValue("target_column");
		if (target_column == null){
			InterfaceSession.log("ShapeEngine.normalizeColumnWithMask: Invalid target column.'", 
								 LoggingType.Errors);
			return false;
			}
		String source_column = (String)attr.getValue("source_column");
		if (source_column == null || !shape.hasColumn(source_column)){
			InterfaceSession.log("ShapeEngine.normalizeColumnWithMask: Invalid source column.", 
								 LoggingType.Errors);
			return false;
			}
		if (!shape.hasColumn(target_column)){
			// Make new column, with same data type as source column
			VertexDataColumn s_column = shape.getVertexDataColumn(source_column);
			int type = s_column.getDataTransferType();
			shape.addVertexData(target_column, type);
			}
		String normalize_column = (String)attr.getValue("normalize_column");
		if (normalize_column == null || !shape.hasColumn(target_column)){
			InterfaceSession.log("ShapeEngine.normalizeColumnWithMask: Invalid normalize column.", 
								 LoggingType.Errors);
			return false;
			}
		String mask_column = (String)attr.getValue("mask_column");
		if (mask_column == null || !shape.hasColumn(target_column)){
			InterfaceSession.log("ShapeEngine.normalizeColumnWithMask: Invalid mask column.", 
								 LoggingType.Errors);
			return false;
			}
		
		String statistic = (String)attr.getValue("statistic");
		
		ArrayList<MguiNumber> values = ShapeFunctions.getMaskNormalizedValues(shape, 
																				source_column, 
																				normalize_column, 
																				mask_column,
																				statistic);
		
		if (values == null) return false;
		shape.getVertexDataColumn(target_column).setValues(values, true);
		//shape.fireShapeModified();
		
		return true;
	}
	
	@Override
	public Attribute<?> getAttribute(String attrName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AttributeList getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return "Shape Engine Instance";
	}

	@Override
	public void setName(String name){}
	
	@Override
	public void setAttribute(String attrName, Object newValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAttributes(AttributeList thisList) {
		// TODO Auto-generated method stub

	}

}