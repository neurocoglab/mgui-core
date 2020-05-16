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

package mgui.geometry.mesh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.jogamp.vecmath.Matrix4d;

import mgui.geometry.Mesh3D;
import mgui.geometry.Plane3D;
import mgui.geometry.Shape3D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.VertexDataColumn;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.util.Engine;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;

/************************************************
 * Engine for performing mesh-based algorithms.
 * 
 * TODO: Specify algorithms as method names using reflection..?
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class MeshEngine implements Engine {

	// Map of attributes for specific function calls
	HashMap<String, AttributeList> attributes = new HashMap<String, AttributeList>();
	
	public static final int OPT_VOLUME_MEAN = 0;
	public static final int OPT_VOLUME_EXPD = 1;
	
	public MeshEngine(){
		init();
	}
	
	private void init(){
		//initiate attribute lists
		//subvide butterfly
		AttributeList thisList = new AttributeList();
		thisList.add(new Attribute<MguiInteger>("NoIterations", new MguiInteger(1)));
		thisList.add(new Attribute<MguiDouble>("Tension (w)", new MguiDouble(1.0 / 16.0)));
		attributes.put("Butterfly Scheme", thisList);
		thisList = new AttributeList();
		//thisList.add(new Attribute("Threshold Radius", new arDouble(0.1)));
		attributes.put("Decimate By Distance", thisList);
		thisList = new AttributeList();
		attributes.put("Decimate Neighbours", thisList);
		thisList = new AttributeList();
		thisList.add(new Attribute<MguiInteger>("Target Size", new MguiInteger(40000)));
		thisList.add(new Attribute<MguiDouble>("Feature Angle", new MguiDouble(0.5)));
		thisList.add(new Attribute<MguiDouble>("Dist to Plane", new MguiDouble(0.0)));
		thisList.add(new Attribute<MguiDouble>("Dist to Edge", new MguiDouble(0.0)));
		thisList.add(new Attribute<MguiDouble>("Min Aspect Ratio", new MguiDouble(0.1)));
		attributes.put("Schroeder Decimate", thisList);
		
		thisList = new AttributeList();
		thisList.add(new Attribute<MguiInteger>("MaxIterations", new MguiInteger(100)));
		thisList.add(new Attribute<MguiDouble>("Lambda", new MguiDouble(0.5)));
		thisList.add(new Attribute<MguiDouble>("Beta", new MguiDouble(0.04)));
		attributes.put("Inflate Mesh TRP", thisList);
		
		thisList = new AttributeList();
		thisList.add(new Attribute<MguiFloat>("X_shift", new MguiFloat(0)));
		thisList.add(new Attribute<MguiFloat>("Y_shift", new MguiFloat(0)));
		thisList.add(new Attribute<MguiFloat>("Z_shift", new MguiFloat(0)));
		attributes.put("Translate", thisList);
		
		thisList = new AttributeList();
		thisList.add(new Attribute<Matrix4d>("Matrix", new Matrix4d()));
		attributes.put("Matrix Transform", thisList);
		
		thisList = new AttributeList();
		thisList.add(new AttributeSelection<String>("input_column", new ArrayList<String>(), String.class));
		//thisList.add(new Attribute<String>("Channel", "default"));
		thisList.add(new Attribute<String>("target_column", "{new}no_name"));
		thisList.add(new Attribute<MguiDouble>("Sigma_normal", new MguiDouble(4)));
		thisList.add(new Attribute<MguiDouble>("Sigma_tangent", new MguiDouble(3)));
		thisList.add(new Attribute<MguiDouble>("Sigma_max_normal", new MguiDouble(2)));
		thisList.add(new Attribute<MguiDouble>("Sigma_max_tangent", new MguiDouble(2)));
		thisList.add(new Attribute<MguiDouble>("Normal_set_max_sigma", new MguiDouble(0)));
		thisList.add(new Attribute<MguiDouble>("Tangent_set_max_sigma", new MguiDouble(0)));
		thisList.add(new Attribute<MguiBoolean>("Normalize values", new MguiBoolean(false)));
		thisList.add(new Attribute<MguiBoolean>("Write matrix file", new MguiBoolean(false)));
		thisList.add(new Attribute<String>("Matrix file", "c:\\matrix_file.mat"));
		ArrayList<String> list = new ArrayList<String>();
		list.add("Parameter");
		list.add("From mean area");
		list.add("From mean length");
		AttributeSelection<String> sel = new AttributeSelection<String>("Set sigma_t from", list, String.class, "Parameter");
		thisList.add(sel);
		attributes.put("Map From Volume (Gaussian)", thisList);
		
		thisList = new AttributeList();
		thisList.add(new Attribute<String>("grid_channel", "default"));
		thisList.add(new Attribute<String>("source_column", "?"));
		thisList.add(new Attribute<MguiDouble>("sigma_normal", new MguiDouble(4)));
		thisList.add(new Attribute<MguiDouble>("sigma_tangent", new MguiDouble(3)));
		thisList.add(new Attribute<MguiDouble>("sigma_max_normal", new MguiDouble(2)));
		thisList.add(new Attribute<MguiDouble>("sigma_max_tangent", new MguiDouble(2)));
		thisList.add(new Attribute<MguiBoolean>("normalize_values", new MguiBoolean(false)));
		thisList.add(new Attribute<MguiBoolean>("is_discrete", new MguiBoolean(false)));
		attributes.put("Map To Volume (Gaussian)", thisList);
		
		thisList = new AttributeList();
		thisList.add(new AttributeSelection<String>("input_column", new ArrayList<String>(), String.class));
		//thisList.add(new Attribute<String>("grid_channel", "default"));
		thisList.add(new Attribute<String>("no_value", "NaN"));
		thisList.add(new Attribute<String>("target_column", "?"));
		thisList.add(new Attribute<MguiFloat>("radius", new MguiFloat(0)));
		list = new ArrayList<String>();
		list.add("Mean");
		list.add("Mode");
		list.add("Median");
		list.add("Max");
		list.add("Min");
		list.add("Abs max");
		list.add("Abs min");
		sel = new AttributeSelection<String>("stat", list, String.class, "Mean");
		thisList.add(sel);
		attributes.put("Map From Volume (Enclosing voxel)", thisList);
		
		
		thisList = new AttributeList();
		ArrayList<String> algorithms = new ArrayList<String>();
		algorithms.add("Maven");
		algorithms.add("QuickHull");
		algorithms.add("GiftWrap");
		algorithms.add("Divide & Conquer");
		sel = new AttributeSelection<String>("Algorithm", algorithms, String.class, "Maven");
		thisList.add(sel);
		attributes.put("Compute Convex Hull", thisList);
		
		thisList = new AttributeList();
		thisList.add(new Attribute<String>("roi_column", "{none}"));
		thisList.add(new Attribute<String>("target_column", "{new}rois"));
		thisList.add(new Attribute<MguiInteger>("min_size", new MguiInteger(1)));
		thisList.add(new Attribute<MguiInteger>("target_rois", new MguiInteger(1)));
		thisList.add(new Attribute<MguiInteger>("min_idx", new MguiInteger(-1)));
		thisList.add(new Attribute<MguiInteger>("max_idx", new MguiInteger(100000)));
		attributes.put("Subdivide ROIs", thisList);
		
		thisList = new AttributeList();
		thisList.add(new AttributeSelection<String>("roi_column", new ArrayList<String>(), String.class));
		thisList.add(new Attribute<MguiInteger>("roi_value", new MguiInteger(0)));
		thisList.add(new Attribute<String>("new_value", "{auto}"));
		ArrayList<SectionSet3DInt> shapes = getAvailableSectionSets();
		thisList.add(new AttributeSelection<SectionSet3DInt>("section_set", shapes, SectionSet3DInt.class));
		thisList.add(new Attribute<MguiInteger>("section", new MguiInteger(0)));
		thisList.add(new Attribute<MguiBoolean>("update_name_map", new MguiBoolean(true)));
		attributes.put("Split ROI with Plane", thisList);
		
		thisList = new AttributeList();
		thisList.add(new AttributeSelection<String>("input_column", new ArrayList<String>(), String.class));
		thisList.add(new Attribute<String>("target_column", "{new}smoothed_values"));
		thisList.add(new Attribute<MguiDouble>("sigma", new MguiDouble(4)));
		thisList.add(new Attribute<MguiDouble>("sigma_max", new MguiDouble(2)));
		
		attributes.put("Smooth vertex values - Isotropic Gaussian", thisList);
		
		thisList = new AttributeList();
		AttributeSelection<String> attr = new AttributeSelection<String>("target_column", new ArrayList<String>(), String.class);
		attr.allowUnlisted(true);
		thisList.add(attr);
		
		attributes.put("Compute Mean Curvature", thisList);
		
		
	}
	
	private ArrayList<SectionSet3DInt> getAvailableSectionSets(){
		
		ArrayList<SectionSet3DInt> sets = new ArrayList<SectionSet3DInt>();
		if (!InterfaceSession.isInit()) return sets;
		
		ArrayList<ShapeModel3D> models = InterfaceSession.getWorkspace().getShapeModels();
		for (int i = 0; i < models.size(); i++){
			ShapeSet3DInt m_set = models.get(i).getModelSet();
			if (m_set != null){
				//ArrayList<InterfaceShape> members = m_set.getShapeType(new SectionSet3DInt(), true).getMembers();
				List<Shape3DInt> members = m_set.getShapeType(new SectionSet3DInt(), true);
				for (Shape3DInt member : members) {
					sets.add((SectionSet3DInt)member);
					}
//				for (int j = 0; j < members.size(); j++)
//					sets.add((SectionSet3DInt)members.get(j));
				}
			}
		
		return sets;
	}
	
	public AttributeList getAttributes(String key){
		// Update stuff if necessary
		if (key.equals("Split ROI with Plane")){
			AttributeList list = attributes.get(key);
			
			AttributeSelection<SectionSet3DInt> selection = (AttributeSelection<SectionSet3DInt>)list.getAttribute("section_set");
			SectionSet3DInt current = selection.getValue();
			selection.setList(getAvailableSectionSets());
			if (current != null)
				selection.setValue(current);
			}
		
		return (AttributeList)attributes.get(key);
	}
	
	@Override
	public Object getAttributeValue(String name) {
		Attribute<?> attribute = getAttribute(name);
		if (attribute == null) return null;
		return attribute.getValue();
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
		
		if (operation.equals("...")){
			if (params.size() != 1){
				InterfaceSession.log("MeshEngine.callMethod: Operation ... requires ... parameter " +
						 "(...)", 
						 LoggingType.Errors);
				return false;
				}
			
			}
		
		
		
		return false;
	}
	
	
	
	
	/************************************************************
	 * Runs the ROI operation specified by {@code method}.
	 * 
	 * @param mesh_int
	 * @param method
	 * @param progress_bar
	 */
	public void doRoiOperation(Mesh3DInt mesh_int, String method, InterfaceProgressBar progress_bar) throws MeshFunctionException{
		
		if (method.equals("Subdivide ROIs")){
			subdivideRois(mesh_int, progress_bar);
			return;
			}
		
		if (method.equals("Split ROI with Plane")){
			splitRoiWithPlane(mesh_int, progress_bar);
			return;
			}
		
	}
	
	/***********************************************
	 * Splits the ROI defined by {@code roi_value} into two parts, on either side
	 * of the plane defined by the section set and section number.
	 * 
	 * @param mesh_int
	 * @param progress_bar
	 * @throws MeshFunctionException
	 */
	public void splitRoiWithPlane(Mesh3DInt mesh_int, InterfaceProgressBar progress_bar) throws MeshFunctionException{
		
		AttributeList attr = (AttributeList)attributes.get("Split ROI with Plane");
		
		String column_name = (String)attr.getValue("roi_column");
		
		if (column_name == null)
			throw new MeshFunctionException("MeshEngine.splitRoiWithPlane: Parameter 'column_name' undefined.");
		
		if (!mesh_int.hasColumn(column_name))
			throw new MeshFunctionException("MeshEngine.splitRoiWithPlane: No column '" + column_name + "' found" +
											" for mesh '" + mesh_int.getName() + ".");
		
		Mesh3D mesh = mesh_int.getMesh();
		ArrayList<MguiNumber> rois = mesh_int.getVertexData(column_name);
		
		SectionSet3DInt s_set = (SectionSet3DInt)attr.getValue("section_set");
		
		if (s_set == null)
			throw new MeshFunctionException("MeshEngine.splitRoiWithPlane: Parameter 'section_set' undefined.");
		
		int section = ((MguiInteger)attr.getValue("section")).getInt();
		Plane3D plane = s_set.getPlaneAt(section);
		
		int roi_value = ((MguiInteger)attr.getValue("roi_value")).getInt();
		String new_value = (String)attr.getValue("new_value");
		
		int new_idx = -1;
		if (new_value.equals("{auto}")){
			new_idx = MeshFunctions.getNextAvailableRoiIndex(rois, 0);
		}else{
			new_idx = Integer.valueOf(new_value);
			}
		
		ArrayList<MguiNumber> new_rois = MeshFunctions.splitRoiWithPlane(mesh, plane, rois, 
																		 roi_value, new_idx, progress_bar);
		
		if (new_rois == null){
			InterfaceSession.log("MeshEngine.splitRoiWithPlane: Plane doesn't appear to intersect ROI '" + roi_value +"'.", 
								 LoggingType.Errors);
		}else{
			mesh_int.getVertexDataColumn(column_name).setValues(new_rois);
			mesh_int.setScene3DObject();
			InterfaceSession.log("MeshEngine.splitRoiWithPlane: Split ROI '" + roi_value +"' -> '" + new_idx + "'.", 
								 LoggingType.Verbose);
			if ( ((MguiBoolean)attr.getValue("update_name_map")).getTrue() ){
				NameMap nmap = mesh_int.getVertexDataColumn(column_name).getNameMap();
				if (nmap == null) return;
				String name = nmap.get(roi_value);
				if (name == null) return;
				nmap.add(new_idx, name + "_" + new_idx);
				}
			
			}
			
		
	}
	
	/*******************************************************
	 * Subdivide the set of ROIs in {@code mesh_int}, by iteratively splitting the largest ROI
	 * in half along its narrowest section.
	 * 
	 * @param mesh_int
	 * @param progress_bar
	 * @throws MeshFunctionException
	 */
	public void subdivideRois(Mesh3DInt mesh_int, InterfaceProgressBar progress_bar) throws MeshFunctionException{
		
		AttributeList attr = (AttributeList)attributes.get("Subdivide ROIs");
		
		ArrayList<MguiNumber> roi_column = null;
		String column_name = (String)attr.getValue("roi_column");
		if (!column_name.equals("{none}")){
			roi_column = mesh_int.getVertexData(column_name);
			if (roi_column == null)
				throw new MeshFunctionException("MeshEngine.subdivideRois: No column named '" + column_name + "'.");
		}else{
			roi_column = new ArrayList<MguiNumber>();
			int n = mesh_int.getVertexCount();
			for (int i = 0; i < n; i++)
				roi_column.add(new MguiInteger(0));
			}
		String target_column = (String)attr.getValue("target_column");
		if (target_column.startsWith("{new}")){
			target_column = target_column.substring(5);
			if (target_column.length() == 0){
				InterfaceSession.log("MeshEngine.subdivideRois: New target_column must have a name. Use '{new}name' to specify.", 
									 LoggingType.Errors);
				return;
				}
			mesh_int.addVertexData(target_column);
		}else{
			if (!mesh_int.hasColumn(target_column))
				throw new MeshFunctionException("MeshEngine.subdivideRois: No column named '" + target_column + 
												"'. Use '{new}name' to create new.");
			}
		
		int min_size = ((MguiInteger)attr.getValue("min_size")).getInt();
		int target_rois = ((MguiInteger)attr.getValue("target_rois")).getInt();
		int min_idx = ((MguiInteger)attr.getValue("min_idx")).getInt();
		int max_idx = ((MguiInteger)attr.getValue("max_idx")).getInt();
		
		ArrayList<MguiNumber> rois = MeshFunctions.subdivideRois(mesh_int.getMesh(), 
																  roi_column, 
																  min_size, 
																  target_rois,
																  min_idx,
																  max_idx,
																  progress_bar);
		
		if (rois == null)
			throw new MeshFunctionException("MeshEngine.subdivideRois: Could not create ROIs.");
			
		mesh_int.getVertexDataColumn(target_column).setValues(rois);
		mesh_int.fireShapeModified();
		
	}
	
	/********************
	 * Calls the function SubdivideButterflyScheme in the MeshSubdivision function class,
	 * using parameters specified in the AttributeList "Butterfly Scheme".
	 * @param mesh Mesh to subdivide
	 * @param newmesh Object to store subdivided mesh 
	 */
	public void SubdivideButterflyScheme(Mesh3DInt mesh){
		//call subdivide function NoIterations times
		AttributeList attr = (AttributeList)attributes.get("Butterfly Scheme");
		int iters = ((MguiInteger)attr.getValue("NoIterations")).getInt();
		
		for (int i = 0; i < iters; i++)
			MeshSubdivision.subdivideButterflyScheme(mesh, 
													 ((MguiDouble)attr.getValue("Tension (w)")).getValue());
		
	}
	
	public void decimate(Mesh3D mesh, String method){
		if (method.equals("Schroeder Decimate")){
			decimateSchroeder(mesh);
			return;
			}
		if (method.equals("Decimate Neighbours")){
			MeshDecimation.decimateNeighbours(mesh);
			return;
			}
	}
	
	/************************************************
	 * Computes the vertex-wise mean curvature of this mesh and stores it in the "target_column" specified
	 * it the "Mean Curvature" attributes. The column will be created if it doesn't exist.
	 * 
	 * @param mesh3d
	 * @param progress
	 * @return
	 */
	public boolean computeMeanCurvature(Mesh3DInt mesh3d, ProgressUpdater progress){
		
		AttributeList attr = (AttributeList)attributes.get("Compute Mean Curvature");
		
		String target_column = (String)attr.getValue("target_column");
		
		return MeshFunctions.computeVertexWiseCurvature(mesh3d, target_column, progress);
		
	}
	
	public void selectBoundaryNodes(Mesh3DInt mesh_int){
		Mesh3D mesh = mesh_int.getMesh();
		ArrayList<Integer> selected = MeshFunctions.getBoundaryNodeIndices(mesh);
		mesh_int.getVertexSelection().select(selected);
	}
	
	public boolean transformMesh(Mesh3D mesh, String method, InterfaceProgressBar progress_bar){
		if (method.equals("Translate")){
			return translateMesh(mesh);
			}
		if (method.equals("Matrix Transform")){
			AttributeList attr = (AttributeList)attributes.get("Matrix Transform");
			Matrix4d matrix = (Matrix4d)attr.getValue("Matrix");
			Mesh3D mesh_t = MeshFunctions.transformWithMatrix(mesh, matrix, progress_bar);
			if (mesh_t == null) return false;
			mesh.setFromMesh(mesh_t);
			return true;
			}
		return false;
	}
	
	public void decimateSchroeder(Mesh3D mesh){
		AttributeList attr = (AttributeList)attributes.get("Schroeder Decimate");
		/*
		MeshDecimation.decimateSchroeder(mesh,
										 ((arInteger)attr.getValue("Target Size")).value, 
										 ((arDouble)attr.getValue("Feature Angle")).value,
										 ((arDouble)attr.getValue("Dist to Plane")).value, 
										 ((arDouble)attr.getValue("Dist to Edge")).value);
		*/
	}
	
	/********************
	 * Calls the function DecimateByDistance in the MeshFunctions function class,
	 * using parameters specified in the AttributeList "Decimate By Distance"
	 * @param mesh Mesh to decimate
	 * @param newmesh Object to store subdivided mesh
	 */
	public void DecimateByDistance(Mesh3D mesh, Mesh3D newmesh){
		AttributeList attr = (AttributeList)attributes.get("Decimate By Distance");
		MeshFunctions.decimateByDistance(mesh, 
										 newmesh, 
										 ((MguiDouble)attr.getValue("Threshold Radius")).getValue());
	}
	
	
	public ArrayList<MguiNumber> mapVolumeToMesh(Mesh3D mesh, Volume3DInt volume, String method){
		return mapVolumeToMesh(mesh, volume, method, null);
	}
	
	
	public ArrayList<MguiNumber> mapVolumeToMesh(Mesh3D mesh, Volume3DInt volume, String method, 
											   InterfaceProgressBar progress_bar){
		
		if (method.equals("Map From Volume (Gaussian)"))
			return mapVolumeToMeshGaussian(mesh, volume, progress_bar);
		
		if (method.equals("Map From Volume (Enclosing voxel)"))
			return mapVolumeToMeshEV(mesh, volume, progress_bar);
		
		return null;
	}
	
	ArrayList<MguiNumber> mapVolumeToMeshGaussian(Mesh3D mesh, Volume3DInt volume, 
												InterfaceProgressBar progress_bar){
		AttributeList attr = (AttributeList)attributes.get("Map From Volume (Gaussian)");
		
		return
			MeshFunctions.mapVolumeToMeshGaussian(mesh, volume,
											(String)attr.getValue("input_column"),
											((MguiDouble)attr.getValue("Sigma_normal")).getValue(),
											((MguiDouble)attr.getValue("Sigma_tangent")).getValue(),
											((MguiDouble)attr.getValue("Sigma_max_normal")).getValue(),
											((MguiDouble)attr.getValue("Sigma_max_tangent")).getValue(),
											   (String)attr.getValue("Set sigma_t from"),
										    ((MguiBoolean)attr.getValue("Normalize values")).getTrue(),
										    progress_bar,
										    ((MguiBoolean)attr.getValue("Write matrix file")).getTrue(),
										    ((MguiDouble)attr.getValue("Normal_set_max_sigma")).getValue(),
											((MguiDouble)attr.getValue("Tangent_set_max_sigma")).getValue(),
										        (String)attr.getValue("Matrix file"));
				
	}
	
	public Volume3DInt mapMeshToVolume(Mesh3DInt mesh, Volume3DInt volume, String method, 
			   					  InterfaceProgressBar progress_bar){

		if (method.equals("Map To Volume (Gaussian)"))
			return mapMeshToVolumeGaussian(mesh, volume, progress_bar);
				
		return null;
	}
	
	public Volume3DInt mapMeshToVolumeGaussian(Mesh3DInt mesh_int, Volume3DInt volume, InterfaceProgressBar progress_bar){
		
		AttributeList attr = (AttributeList)attributes.get("Map To Volume (Gaussian)");
		
		return
			MeshFunctions.mapMeshToVolumeGaussian(mesh_int,
												  volume,
												  (String)attr.getValue("source_column"),
												  (String)attr.getValue("grid_channel"),
												  ((MguiDouble)attr.getValue("sigma_normal")).getValue(),
												  ((MguiDouble)attr.getValue("sigma_tangent")).getValue(),
												  ((MguiDouble)attr.getValue("sigma_max_normal")).getValue(),
												  ((MguiDouble)attr.getValue("sigma_max_tangent")).getValue(),
												  ((MguiBoolean)attr.getValue("normalize_values")).getTrue(),
												  ((MguiBoolean)attr.getValue("is_discrete")).getTrue(),
												  progress_bar);
		
	}
	
	ArrayList<MguiNumber> mapVolumeToMeshEV(Mesh3D mesh, Volume3DInt volume, 
										  InterfaceProgressBar progress_bar){
		
		AttributeList attr = (AttributeList)attributes.get("Map From Volume (Enclosing voxel)");
		
		double no_value = Double.valueOf((String)attr.getValue("no_value"));
		float radius = ((MguiFloat)attr.getValue("radius")).getFloat();
		String stat_str = (String)attr.getValue("stat");
		int stat = -1;
		if (stat_str == null || stat_str.equals("Mean"))
			stat = 0;
		else if (stat_str.equals("Max"))
			stat = 1;
		else if (stat_str.equals("Min"))
			stat = 2;
		else if (stat_str.equals("Abs max"))
			stat = 3;
		else if (stat_str.equals("Abs min"))
			stat = 4;
		else if (stat_str.equals("Mode"))
			stat = 5;
		else if (stat_str.equals("Median"))
			stat = 6;
		
		return MeshFunctions.mapVolumeToMeshEV(mesh, volume, (String)attr.getValue("input_column"), no_value, radius, stat, progress_bar);
	}
	
	/**********************************
	 * 
	 * Creates new mesh objects as a set of non-contiguous meshes from {@code mesh_int}, and adds
	 * these to {@code shape_set}.
	 * 
	 * @param mesh 				Mesh from which to obtain parts
	 * @param new_shape_set		Shape set in which to store new mesh objects
	 *
	 */
	public void getMeshParts(Mesh3DInt mesh_int,
							 ShapeSet3DInt shape_set) {
		
		getMeshParts(mesh_int, shape_set, false, null);
		
	}
							 
	
	/**********************************
	 * 
	 * Creates new mesh objects as a set of non-contiguous meshes from {@code mesh_int}, and adds
	 * these to {@code shape_set}.
	 * 
	 * @param mesh 				Mesh from which to obtain parts
	 * @param new_shape_set		Shape set in which to store new mesh objects
	 * @param retain			Whether to retain the original mesh object
	 * @param copy_data 		Whether to copy vertex data from the original mesh object
	 */
	public void getMeshParts(Mesh3DInt mesh_int,
							 ShapeSet3DInt shape_set,
							 boolean copy_data,
							 ProgressUpdater progress) {
		
		SwingWorker<ArrayList<Object>,Object> worker = new SwingWorker<ArrayList<Object>,Object>(){

			@Override
			protected ArrayList<Object> doInBackground() throws Exception {
				ArrayList<Object> results = new ArrayList<Object>();
				
				ArrayList<HashMap<String,ArrayList<MguiNumber>>> parts_data = null;
				HashMap<String,ArrayList<MguiNumber>> mesh_data = null;
				Mesh3D mesh = mesh_int.getMesh();
				
				if (copy_data) {
					parts_data = new ArrayList<HashMap<String,ArrayList<MguiNumber>>>();
					mesh_data = mesh_int.getVertexDataMap();
					}
				
				ArrayList<Mesh3D> parts = MeshFunctions.getMeshParts(mesh, mesh_data, parts_data);
				results.add(parts);
				results.add(parts_data);
				
				return results;
				
			}
			
		};
		
		if (progress != null) {
			progress.register();
			}
		
		worker.execute();
		
		Thread wait_thread = new Thread() {
			
			@Override
			public void run() {
				
				while(!worker.isDone()) {
					try {
						Thread.sleep(200);
					}catch (Exception ex) {
						
						}
					}
				
				// Add parts to shape set
				try {
					
					ArrayList<Object> results = worker.get();
					ArrayList<Mesh3D> parts = (ArrayList<Mesh3D>)results.get(0);
					ArrayList<HashMap<String,ArrayList<MguiNumber>>> parts_data = (ArrayList<HashMap<String,ArrayList<MguiNumber>>>)results.get(0);
					
					int i = 0;
					for (Mesh3D part : parts) {
						String name = mesh_int.getName() + "_part_" + (i+1);
						Mesh3DInt new_mesh = new Mesh3DInt(part);
						new_mesh.setName(name);
						shape_set.addShape(new_mesh, false);
						
						if (copy_data) {
							new_mesh.setVertexDataMap(parts_data.get(i));
							}
						
						i++;
						}
					
					shape_set.shapeUpdated(new ShapeEvent(shape_set, ShapeEvent.EventType.ShapeSetModified, true));
					
					if (progress != null) {
						progress.deregister();
						}
					
					InterfaceSession.log("Added " + parts.size() + " meshes to '" + shape_set.getName() + "'.", LoggingType.Debug);
					
					SwingUtilities.invokeLater(new Thread() {
						
						public void run() {
							JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
									  "Added " + parts.size()  + " parts of " + mesh_int.getName(),
									  "Get Mesh Parts",
									  JOptionPane.INFORMATION_MESSAGE);
						}
						
					});
					
				} catch (Exception ex) {
					InterfaceSession.handleException(ex);
					}
				
			}
			
			
		};
		
		wait_thread.start();
		
		
	}
	
	
	/**********************************
	 * 
	 * @param mesh
	 * @param newmesh
	 * @param mask
	 * @param value
	 * @param retain
	 */
	public void getSubMesh(Mesh3DInt mesh,
						   Mesh3DInt newmesh,
						   ArrayList<MguiNumber> mask,
						   int value,
						   boolean retain,
						   boolean copydata){
		
		HashMap<String, ArrayList<MguiNumber>> nodeData = new HashMap<String, ArrayList<MguiNumber>>();
		if (copydata)
			newmesh.setMesh(MeshFunctions.getSubMesh(mesh.getMesh(), 
													 mask, 
													 value, 
													 retain,
													 mesh.getVertexDataMap(),
													 nodeData));
		else
			newmesh.setMesh(MeshFunctions.getSubMesh(mesh.getMesh(), 
													 mask, 
													 value, 
													 retain));
								
		InterfaceSession.log("***Submesh operation****");
		InterfaceSession.log("Old mesh: " + mesh.getMesh().n + " nodes, " + 
										  mesh.getMesh().f + " faces.");
		InterfaceSession.log("New mesh: " + newmesh.getMesh().n + " nodes, " + 
				  						  newmesh.getMesh().f + " faces.");
		
		if (copydata)
			newmesh.setVertexDataMap(nodeData);
	}
	
	public void getSubMesh(Mesh3DInt mesh,
						   Mesh3DInt newmesh,
						   boolean retain,
						   boolean copydata){
		
		HashMap<String, ArrayList<MguiNumber>> nodeData = new HashMap<String, ArrayList<MguiNumber>>();
		
		if (mesh.getVertexSelection().getSelectedCount() == 0){
			newmesh.setShape((Shape3D)mesh.getShape().clone());
			if (copydata)
				newmesh.setVertexDataMap(mesh.getVertexDataMap());
		}else{
			if (copydata)
				newmesh.setMesh(MeshFunctions.getSubMesh(mesh.getMesh(),
														 mesh.getVertexSelection(),
														 retain,
														 false,
														 mesh.getVertexDataMap(),
														 nodeData));
			else
				newmesh.setMesh(MeshFunctions.getSubMesh(mesh.getMesh(),
														 mesh.getVertexSelection(),
														 retain));
			
			
			}
		
		InterfaceSession.log("***Submesh operation****");
		InterfaceSession.log("Old mesh: " + mesh.getMesh().n + " nodes, " + 
										  mesh.getMesh().f + " faces.");
		InterfaceSession.log("New mesh: " + newmesh.getMesh().n + " nodes, " + 
				  						  newmesh.getMesh().f + " faces.");
		
		if (copydata)
			newmesh.setVertexDataMap(nodeData);
		
	}
	
	public void SmoothLR(Mesh3D mesh){
		//TODO get parameters from attribute list
		MeshFunctions.smoothLR(mesh);
	}
	
	public void SmoothEM(Mesh3DInt mesh){
		//TODO get parameters from attribute list
		MeshFunctions.smoothEM(mesh);
	}
	
	/*******************************************
	 * Uses the current parameters for "Smooth vertex values - Isotropic Gaussian" to smooth vertex values
	 * with a Gaussian kernel, isotropic in the plane of the surface mesh.
	 * 
	 * @param mesh
	 * @param progress
	 */
	public void smoothVertexValuesIsotropicGaussian(Mesh3DInt mesh_int, ProgressUpdater progress) throws MeshFunctionException{
		
		AttributeList list = attributes.get("Smooth vertex values - Isotropic Gaussian");
		VertexDataColumn column = mesh_int.getVertexDataColumn((String)list.getValue("input_column"));
		if (column == null)
			throw new MeshFunctionException("MeshEngine.smoothVertexValuesIsotropicGaussian: Invalid input column: '" + 
											(String)list.getValue("input_column") + "'.");
		
		Mesh3D mesh = mesh_int.getMesh();
		
		String target_column = (String)list.getValue("target_column");
		if (target_column.startsWith("{new}")){
			target_column = target_column.substring(5);
			if (target_column.length() == 0){
				InterfaceSession.log("MeshEngine.subdivideRois: New target_column must have a name. Use '{new}name' to specify.", 
									 LoggingType.Errors);
				return;
				}
			mesh_int.addVertexData(target_column);
		}else{
			if (!mesh_int.hasColumn(target_column))
				throw new MeshFunctionException("MeshEngine.subdivideRois: No column named '" + target_column + 
												"'. Use '{new}name' to create new.");
			}
		
		ArrayList<MguiNumber> smoothed_values = MeshFunctions.smoothVertexValuesIsotropicGaussian(mesh, 
																								  column.getData(),
																								  ((MguiDouble)list.getValue("sigma")).getValue(),
																								  ((MguiDouble)list.getValue("sigma_max")).getValue(),
																								  progress);
		
		column = mesh_int.getVertexDataColumn(target_column);
		column.setValues(smoothed_values);
		return;
		
	}
	
	public void inflateMesh(Mesh3DInt mesh, String method, ProgressUpdater progress){
		if (method.equals("Inflate Mesh TRP"))
			inflateTRP(mesh, progress);
	}
	
	public void inflateTRP(Mesh3DInt mesh, ProgressUpdater progress){
		AttributeList list = (AttributeList)attributes.get("Inflate Mesh TRP");
		double lambda = ((MguiDouble)list.getValue("Lambda")).getValue();
		double beta = ((MguiDouble)list.getValue("Beta")).getValue();
		int max_itr = ((MguiInteger)list.getValue("MaxIterations")).getInt();
		Mesh3D result = MeshFunctions.inflateMeshTRP(mesh, lambda, beta, max_itr, progress);
		mesh.setMesh(result);
	}
	
	public boolean translateMesh(Mesh3D mesh){
		AttributeList list = (AttributeList)attributes.get("Translate");
		GeometryFunctions.translate(mesh, ((MguiFloat)list.getValue("X_shift")).getValue(),
										  ((MguiFloat)list.getValue("Y_shift")).getValue(),
										  ((MguiFloat)list.getValue("Z_shift")).getValue());
		InterfaceSession.log("Translated surface [" + ((MguiFloat)list.getValue("X_shift")).getValue() + ", " +
		((MguiFloat)list.getValue("Y_shift")).getValue() + ", " +
		((MguiFloat)list.getValue("Z_shift")).getValue() + "]");
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
		return "Mesh Engine Instance";
	}
	
	@Override
	public void setName(String name){}

	@Override
	public void setAttribute(String attrName, Object newValue) {
		
	}

	@Override
	public void setAttributes(AttributeList thisList) {
		
	}
	
}