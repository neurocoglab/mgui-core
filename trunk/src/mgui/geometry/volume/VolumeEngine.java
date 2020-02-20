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

package mgui.geometry.volume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;

import mgui.geometry.Mesh3D;
import mgui.geometry.mesh.MeshFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.VertexDataColumn;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.util.Engine;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;

/**********************************************************
 * Engine to perform built-in operations on volumes.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class VolumeEngine implements Engine {

	protected HashMap<String, AttributeList> attributes = new HashMap<String, AttributeList>();
	
	public VolumeEngine(){
		init();
	}
	
	private void init(){
		AttributeList list = new AttributeList();
		AttributeSelection<String> attr = new AttributeSelection<String>("source_column", new ArrayList<String>(), String.class);
		attr.allowUnlisted(false);
		list.add(attr);
		attr = new AttributeSelection<String>("target_column", new ArrayList<String>(), String.class);
		attr.allowUnlisted(true);
		list.add(attr);
		list.add(new Attribute<MguiDouble>("default_value", new MguiDouble(0)));
		
		attributes.put("Map Volume to Volume (Enclosing Voxel)", list);
		
		list = new AttributeList();
		attr = new AttributeSelection<String>("source_column", new ArrayList<String>(), String.class);
		attr.allowUnlisted(false);
		list.add(attr);
		attr = new AttributeSelection<String>("target_column", new ArrayList<String>(), String.class);
		attr.allowUnlisted(true);
		list.add(attr);
		list.add(new Attribute<MguiDouble>("default_value", new MguiDouble(0)));
		list.add(new Attribute<MguiInteger>("search_radius", new MguiInteger(1)));
		attributes.put("Map Volume to Volume (Tri-linear)", list);
		
		list = new AttributeList();
		attr = new AttributeSelection<String>("source_column", new ArrayList<String>(), String.class);
		attr.allowUnlisted(false);
		list.add(attr);
		attr = new AttributeSelection<String>("target_column", new ArrayList<String>(), String.class);
		attr.allowUnlisted(true);
		list.add(attr);
		list.add(new Attribute<MguiDouble>("default_value", new MguiDouble(0)));
		attributes.put("Map Volume to Volume (Tri-cubic)", list);
		
		list = new AttributeList();
		attr = new AttributeSelection<String>("source_column", new ArrayList<String>(), String.class);
		attr.allowUnlisted(false);
		list.add(attr);
		attr = new AttributeSelection<String>("target_column", new ArrayList<String>(), String.class);
		attr.allowUnlisted(true);
		list.add(attr);
		list.add(new Attribute<MguiDouble>("default_value", new MguiDouble(0)));
		attributes.put("Map Volume to Volume (Sinc)", list);
		
		list = new AttributeList();
		attr = new AttributeSelection<String>("source_column", new ArrayList<String>(), String.class);
		attr.allowUnlisted(false);
		list.add(attr);
		attr = new AttributeSelection<String>("target_column", new ArrayList<String>(), String.class);
		attr.allowUnlisted(true);
		list.add(attr);
		list.add(new Attribute<MguiDouble>("default_value", new MguiDouble(0)));
		list.add(new Attribute<MguiDouble>("fwhm", new MguiDouble(2.0)));
		list.add(new Attribute<MguiDouble>("max_radius", new MguiDouble(6.0)));
		attributes.put("Smooth Volume (Isotropic Gaussian)", list);
		
		list = new AttributeList();
		attr = new AttributeSelection<String>("source_column", new ArrayList<String>(), String.class);
		attr.allowUnlisted(false);
		list.add(attr);
		attr = new AttributeSelection<String>("target_column", new ArrayList<String>(), String.class);
		attr.allowUnlisted(true);
		list.add(attr);
		list.add(new Attribute<MguiDouble>("threshold", new MguiDouble(0)));
		list.add(new Attribute<MguiInteger>("min_blob", new MguiInteger(1)));
		attributes.put("Get Blobs from Volume", list);
		
		list = new AttributeList();
		list.add(new Attribute<String>("source_column", ""));
		list.add(new Attribute<MguiDouble>("isovalue", new MguiDouble(0.5)));
		list.add(new Attribute<String>("surface_name", "no-name"));
		list.add(new Attribute<ShapeSet3DInt>("shape_set", (ShapeSet3DInt)null));
		//list.add(attr);
		
		attributes.put("Get Isosurface from Volume", list);
	}
	
	/*************************************************
	 * Returns the attributes corresponding to {@code key}, where {@code key} is of the form
	 * "operation (method)".
	 * 
	 * @param key
	 * @return
	 */
	public AttributeList getAttributes(String key){
		return (AttributeList)attributes.get(key);
	}
	
	@Override
	public AttributeList getAttributes(String operation, String method){
		if (method != null)
			return getAttributes(operation + "(" + method + ")");
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
		
		if (operation.equals("Map Volume to Volume")){
			if (params.size() != 2){
				InterfaceSession.log("VolumeEngine.callMethod: Operation 'Map Volume to Volume' requires two parameters " +
									 "(source_volume, target_volume)", 
									 LoggingType.Errors);
				return false;
				}
				
			return mapVolumeToVolume((Volume3DInt)params.get(0),
									 (Volume3DInt)params.get(0),
									 method,
									 progress);
			}
		
		if (operation.equals("Smooth Volume")){
			if (params.size() != 1){
				InterfaceSession.log("VolumeEngine.callMethod: Operation 'Smooth Volume' requires one parameter " +
									 "(source_volume)", 
									 LoggingType.Errors);
				return false;
				}
				
			return smoothVolume((Volume3DInt)params.get(0),
								 method,
								 progress);
			}
		
		return false;
	}
	
	/*****************************************************
	 * Maps a column of {@code source} to a column of {@code target}, given the attributes corresponding
	 * to {@code method}.
	 * 
	 * @param source
	 * @param target
	 * @param method
	 * @param progress_bar
	 * @return
	 */
	public boolean mapVolumeToVolume(Volume3DInt source,
									 Volume3DInt target, 
									 String method, 
									 ProgressUpdater progress_bar){
		
		String op_method = "Map Volume to Volume (" + method + ")";
		
		AttributeList attr = attributes.get(op_method);
		double default_value = ((MguiDouble)attr.getValue("default_value")).getValue();
		String source_column = (String)attr.getValue("source_column");
		if (source_column == null){
			InterfaceSession.log("VolumeEngine.mapVolumeToVolume: Source column not set.", 
								 LoggingType.Errors);
			return false;
			}
		String target_column = (String)attr.getValue("target_column");
		if (target_column == null){
			InterfaceSession.log("VolumeEngine.mapVolumeToVolume: Target column not set.", 
								 LoggingType.Errors);
			return false;
			}
		// If target column doesn't exist, create it with same data type as source column
		if (!target.hasColumn(target_column)){
			int type = source.getVertexDataColumn(source_column).getDataTransferType();
			target.addVertexData(target_column, type);
			VertexDataColumn t_column = target.getVertexDataColumn(target_column);
			VertexDataColumn s_column = source.getVertexDataColumn(source_column);
			t_column.setColourMap(s_column.getColourMap());
			t_column.setColourLimits(s_column.getColourMin(), s_column.getColourMax());
			}
		
		if (method.equals("Enclosing Voxel")){
			
			boolean success = VolumeFunctions.mapVolumeToVolumeEV(source, target, source_column, target_column, default_value, progress_bar);
			if (!success) return false;
			target.getVertexDataColumn(target_column).updateDataLimits(false);
			return true;
			}
		
		ArrayList<Object> parameters = null;
		if (method.equals("Tri-linear")){
			parameters = new ArrayList<Object>();
			parameters.add(attr.getValue("search_radius"));
			}
		
		boolean success = VolumeFunctions.mapVolumeToVolumeInterp(source, target, source_column, target_column, 
																  method, parameters, default_value, progress_bar);
		if (!success) return false;
		target.getVertexDataColumn(target_column).updateDataLimits(false);
		return true;

	}
	
	/*************************************************************
	 * Geometrically smooths values from {@code source_column} in {@code volume} with the
	 * smoothing kernel specified by {@code method}. Writes the results to {@code target_column}. If
	 * {@code target_column} doesn't exist, it will be created with the same data type as
	 * {@code source_column}. If it exists, it will maintain its current data type and its
	 * values will be overwritten. {@code target_column} can be the same as {@code source_column}.
	 * 
	 * <p>Valid methods are:
	 * 
	 * <ul>
	 * <li>'Smooth Volume (Isotropic Gaussian)' - 
	 * </ul>
	 * 
	 * @param volume
	 * @param method
	 * @param progress_bar
	 * @return {@code true} if successful
	 */
	public boolean smoothVolume(Volume3DInt volume,
								String method,
								ProgressUpdater progress_bar){
		
		method = "Smooth Volume (" + method + ")";
		
		if (method.equals("Smooth Volume (Isotropic Gaussian)")){
			AttributeList attr = attributes.get(method);
			double default_value = ((MguiDouble)attr.getValue("default_value")).getValue();
			double fwhm = ((MguiDouble)attr.getValue("fwhm")).getValue();
			double max_radius = ((MguiDouble)attr.getValue("max_radius")).getValue();
			String source_column = (String)attr.getValue("source_column");
			String target_column = (String)attr.getValue("target_column");
			
			if (!VolumeFunctions.smoothVolumeGaussian(volume, 
														source_column, 
														target_column, 
														fwhm,
														max_radius,
														progress_bar,
														default_value)){
				return false;
				}
			
			volume.fireShapeModified();
			return true;
			}
		
		InterfaceSession.log("VolumeEngine.smoothVolume: No such method '" + method + "'", 
	 			 LoggingType.Errors);
		return false;
	}
	
	/*********************************
	 * Extract blobs from a thresholded volume
	 * 
	 * @param volume
	 * @return
	 */
	public boolean getBlobsFromVolume(final Volume3DInt volume){
		
		AttributeList attr = attributes.get("Get Blobs from Volume");
		final double threshold = ((MguiDouble)attr.getValue("threshold")).getValue();
		final int min_blob = ((MguiInteger)attr.getValue("min_blob")).getInt();
		final String source_column = (String)attr.getValue("source_column");
		final String target_column = (String)attr.getValue("target_column");
		
		InterfaceProgressBar progress = new InterfaceProgressBar("Extracting blobs from volume: ");
		
		return VolumeFunctions.getBlobsFromVolume(volume, source_column, target_column, progress, min_blob, threshold);
	}
	
	/***************************************
	 * Creates an isosurface at a specified isovalue in {@code volume}. Creates a new surface and stores it
	 * in {@code shape_set}.
	 * 
	 * @param volume
	 * @return
	 */
	public boolean getIsosurfaceFromVolume(Volume3DInt volume){
		
		AttributeList attr = attributes.get("Get Isosurface from Volume");
		double isovalue = ((MguiDouble)attr.getValue("isovalue")).getValue();
		String source_column = (String)attr.getValue("source_column");
		String surface_name = (String)attr.getValue("surface_name");
		ShapeSet3DInt shape_set = (ShapeSet3DInt)attr.getValue("shape_set");
		
		Mesh3D mesh = MeshFunctions.getIsosurfaceFromVolume(volume, source_column, isovalue);
		
		if (mesh == null){
			return false;
			}
		
		Mesh3DInt mesh3d = new Mesh3DInt(mesh, surface_name);
		shape_set.addShape(mesh3d);
		
		return true;
	}
	
	
	@Override
	public AttributeList getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Attribute<?> getAttribute(String attrName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttributes(AttributeList thisList) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAttribute(String attrName, Object newValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub

	}

}