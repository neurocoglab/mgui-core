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

package mgui.geometry.mesh;

import java.awt.image.DataBuffer;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.jogamp.java3d.IndexedTriangleArray;
import org.jogamp.java3d.utils.geometry.GeometryInfo;
import org.jogamp.java3d.utils.geometry.NormalGenerator;
import org.jogamp.vecmath.Matrix4d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import Jama.Matrix;
import foxtrot.Job;
import foxtrot.Worker;
import mgui.geometry.Box3D;
import mgui.geometry.Grid3D;
import mgui.geometry.Mesh3D;
import mgui.geometry.Mesh3D.MeshFace3D;
import mgui.geometry.Plane3D;
import mgui.geometry.Polygon3D;
import mgui.geometry.Triangle3D;
import mgui.geometry.util.ConvexHullFunctions;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.Utility;
import mgui.interfaces.graphs.InterfaceAbstractGraph;
import mgui.interfaces.graphs.util.GraphFunctions;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.mesh.VertexSelection;
import mgui.io.domestic.variables.DefaultMatrixFileWriter;
import mgui.io.domestic.variables.MatrixOutOptions;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;
import mgui.stats.StatFunctions;

/************************
 * Utility class containing static methods that perform some operation upon Mesh3D
 * shape objects. This class should contain all methods for mesh functions, which may
 * or may not reference more specialized mesh algorithm classes. I.e., it is intended
 * as the standard interface for mesh operations within mgui.
 * 
 * <p><b>Policy</b>: See {@link ProgressUpdater} for details on how to incorporate progress
 * updaters into your methods. In general, if progress updating is desired, functions must be
 * able to handle <code>null</code> updater arguments, and preferably provide a second method
 * with no updater which calls the first with a <code>null</code> updater (thus allowing
 * stand-alone calls to blocking methods).
 * 
 * TODO: Remove all calls to {@code Worker} in this class. All functions should block.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * @see mgui.interfaces.ProgressUpdater
 * @see mgui.interfaces.gui.InterfaceProgressBar
 *
 */
public class MeshFunctions extends Utility {
	
	public static double tolerance = 10e-8;

	public static JProgressBar progressBar;
		
	/******************
	 * Returns a new mesh which is the convex hull of the given mesh. Defaults to
	 * the "Giftwrap" algorithm, by Tim Lambert:
	 * 
	 * <p>http://www.cse.unsw.edu.au/~lambert/java/3d/implementation.html
	 * 
	 * @param mesh Mesh for which to compute a convex hull.
	 * @param method The method to use
	 */
	public static Mesh3D getConvexHull(Mesh3D mesh){
		return getConvexHull(mesh, "GiftWrap", null);
	}
	
	public static Mesh3D getConvexHull(Mesh3D mesh, ProgressUpdater progress){
		return getConvexHull(mesh, "GiftWrap", progress);
	}
	
	public static Mesh3D getConvexHull(Mesh3D mesh, String method){
		return getConvexHull(mesh, method, null);
	}
	
	/**************************************************
	 * Combines <code>mesh1</code> and <code>mesh2</code> and returns the result.
	 * 
	 * @param mesh1
	 * @param mesh2
	 * @return
	 */
	public static Mesh3D combine(Mesh3D mesh1, Mesh3D mesh2){
		return (combineBlocking(mesh1, mesh2, null));
	}
	
	static Mesh3D combineBlocking(Mesh3D mesh1, Mesh3D mesh2, ProgressUpdater progress){
		
		int[] faces_new = new int[(mesh1.f * 3) + (mesh2.f * 3)];
		int[] faces_append = new int[mesh2.faces.length];
		System.arraycopy(mesh2.faces, 0, faces_append, 0, mesh2.faces.length);
		for (int i = 0; i < mesh2.faces.length; i++)
			faces_append[i] += mesh1.n;
		
		System.arraycopy(mesh1.faces, 0, faces_new, 0, mesh1.f * 3);
		System.arraycopy(faces_append, 0, faces_new, mesh1.f * 3, mesh2.f * 3);
		float[] nodes_new = new float[(mesh1.n * 3) + (mesh2.n * 3)];
		System.arraycopy(mesh1.nodes, 0, nodes_new, 0, mesh1.n * 3);
		System.arraycopy(mesh2.nodes, 0, nodes_new, mesh1.n * 3, mesh2.n * 3);
		
		Mesh3D result = new Mesh3D();
		result.faces = faces_new;
		result.nodes = nodes_new;
		result.f = mesh1.f + mesh2.f;
		result.n = mesh1.n + mesh2.n;
		
		return result;
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
	public static Mesh3D getConvexHull(final Mesh3D mesh, final String method, final ProgressUpdater progress){
		if (progress == null || !(progress instanceof InterfaceProgressBar)) return getConvexHullBlocking(mesh, method, progress);
		
		Mesh3D hull = (Mesh3D)Worker.post(new Job(){
				@Override
				public Mesh3D run(){
					return getConvexHullBlocking(mesh, method, progress);
				}
			});
		
		return hull;
	}
	
	static Mesh3D getConvexHullBlocking(Mesh3D mesh, String method, ProgressUpdater progress){
		
		try{
			return ConvexHullFunctions.getConvexHull(mesh.getVertices(), method, progress);
		}catch (MeshFunctionException e){
			e.printStackTrace();
			}
		
		return null;
		
	}
	
	/***********************************
	 * 
	 * Returns a sub-mesh of {@code mesh_old} containing all vertices and associated faces
	 * in {@code selection}.
	 * 
	 * @param mesh_old 			Mesh from which to obtain sub-mesh
	 * @param selection			Selected vertices for sub-mesh
	 * @param retain			Whether to retain the selection (otherwise it is removed)
	 * 
	 * @return Newly created sub-mesh
	 */
	public static Mesh3D getSubMesh(Mesh3D mesh_old, 
									VertexSelection selection,
									boolean retain){
		return getSubMesh(mesh_old, selection, retain, false, null, null);
	}
	
	/***********************************
	 * 
	 * Returns a sub-mesh of {@code mesh_old} containing all vertices and associated faces
	 * in {@code selection}.
	 * 
	 * @param mesh_old 			Mesh from which to obtain sub-mesh
	 * @param selection			Selected vertices for sub-mesh
	 * @param retain			Whether to retain the selection (otherwise it is removed)
	 * @param any_in_face 		Include faces with any vertices in selection? Otherwise, requires
	 * 							all vertices to be in the selection.
	 * @param data_old			Data from {@code mesh_old} to transfer to {@code mesh_new} (can be {@code null})
	 * @param data_new 			New data for the sub-mesh; this should be a reference to an empty
	 * 							{@code HashMap}
	 * 
	 * @return Newly created sub-mesh
	 */
	public static Mesh3D getSubMesh(Mesh3D mesh_old, 
									VertexSelection selection,
									boolean retain,
									boolean any_in_face,
									HashMap<String, ArrayList<MguiNumber>> data_old,
									HashMap<String, ArrayList<MguiNumber>> data_new){
		
		//add all retained vertices to new mesh
		//store index map
		Mesh3D mesh_new = new Mesh3D();
		ArrayList<MguiInteger> indexMap = new ArrayList<MguiInteger>(mesh_old.n);
		
		int k = 0;
		for (int i = 0; i < mesh_old.n; i++){
			if (selection.isSelected(i) != retain){
				indexMap.add(new MguiInteger(-1));
			}else{
				mesh_new.addVertex(mesh_old.getVertex(i));
				indexMap.add(new MguiInteger(k++));
				}
			}
		
		//for each face, add if each of its vertices is non zero
		for (int i = 0; i < mesh_old.f; i++){
			Mesh3D.MeshFace3D face = mesh_old.getFace(i);
			
			boolean accept = false;
			if (any_in_face)
				accept = (selection.isSelected(face.A) == retain || 
						  selection.isSelected(face.B) == retain ||
						  selection.isSelected(face.C) == retain);
			else
				accept = (selection.isSelected(face.A) == retain && 
						  selection.isSelected(face.B) == retain &&
						  selection.isSelected(face.C) == retain);
			
			if (accept){
				//add extra vertices if necessary 
				if (any_in_face){
					
					if (indexMap.get(face.A).getInt() < 0){
						mesh_new.addVertex(mesh_old.getVertex(face.A));
						indexMap.get(face.A).setValue(k++);
						}
					if (indexMap.get(face.B).getInt() < 0){
						mesh_new.addVertex(mesh_old.getVertex(face.B));
						indexMap.get(face.B).setValue(k++);
						}
					if (indexMap.get(face.C).getInt() < 0){
						mesh_new.addVertex(mesh_old.getVertex(face.C));
						indexMap.get(face.C).setValue(k++);
						}
					}
				
				mesh_new.addFace(indexMap.get(face.A).getInt(),
								 indexMap.get(face.B).getInt(),
								 indexMap.get(face.C).getInt());
				
				}
			}
		
		//set node data if not null
		//TODO: make this work for any_in_face
		if (data_old != null && data_new != null){
			Iterator<String> itr = data_old.keySet().iterator();
			
			while (itr.hasNext()){
				String c = itr.next();
				ArrayList<MguiNumber> col = data_old.get(c); 
				ArrayList<MguiNumber> new_col = new ArrayList<MguiNumber>(mesh_new.n);
				for (int i = 0; i < col.size(); i++)
					if (selection.isSelected(i) == retain)
						new_col.add((MguiNumber)col.get(i).clone());
				data_new.put(c, new_col);
				}
			}
		
		removeStrandedNodes(mesh_new);
		return mesh_new;
		
	}
	
	/*****************************************************************
	 * Retains only the largest contiguous ROIs in the given set of values; all others
	 * are set to {@code out_value}.
	 * 
	 * @param mesh
	 * @param rois
	 * @param out_value
	 * @return
	 */
	public static ArrayList<MguiNumber> getLargestContiguousRois(Mesh3D mesh, 
																 ArrayList<MguiNumber> rois, 
																 int out_value){
		
		ArrayList<MguiNumber> new_rois = new ArrayList<MguiNumber>();
		TreeSet<Integer> _keys = new TreeSet<Integer>();
		for (int i = 0; i < rois.size(); i++){
			_keys.add((int)rois.get(i).getValue());
			new_rois.add(new MguiInteger(rois.get(i).getValue()));
			}
		
		// For all keys, search for subregions
		ArrayList<Integer> keys = new ArrayList<Integer>(_keys);
		for (int i = 0; i < keys.size(); i++){
			int roi = keys.get(i);
			
			ArrayList<ArrayList<Integer>> islands = getContiguousRoiRegions(mesh, rois, roi);
			
			if (islands.size() > 1){
				int max = -1, max_idx = -1;
				for (int j = 0; j < islands.size(); j++){
					if (islands.get(j).size() > max){
						max = islands.get(j).size();
						max_idx = j;
						}
					}
				
				// Set 
				for (int j = 0; j < islands.size(); j++){
					if (j != max_idx){
						for (int k = 0; k < islands.get(j).size(); k++)
							new_rois.set(islands.get(j).get(k), new MguiInteger(out_value));
						}
					}
				}
			
			}
		
		return new_rois;
	}
	
	
	static ArrayList<ArrayList<Integer>> getContiguousRoiRegions(Mesh3D mesh, 
																 ArrayList<MguiNumber> rois, 
																 int roi){
		ArrayList<ArrayList<Integer>> regions = new ArrayList<ArrayList<Integer>>();
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		boolean[] searched = new boolean[mesh.n];
		boolean[] is_roi = new boolean[mesh.n];
		
		for (int i = 0; i < rois.size(); i++){
			is_roi[i] = (int)rois.get(i).getValue() == roi;
			}
		
		for (int i = 0; i < rois.size(); i++){
			if (!searched[i] && is_roi[i]){
				ArrayList<Integer> all_nbrs = getAllRoiNeighbours(n_mesh, i, is_roi, searched);
				all_nbrs.add(i);
				regions.add(all_nbrs);
				}
			}
		
		return regions;
	}
	
	static ArrayList<Integer> getAllRoiNeighbours(NeighbourhoodMesh n_mesh,  
												  int idx,
												  boolean[] is_roi, 
												  boolean[] searched){
		
		searched[idx] = true;
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		
		int[] nbrs = n_mesh.getNeighbourhood(idx).getNeighbourList();
		
		for (int i = 0; i < nbrs.length; i++){
			if (is_roi[nbrs[i]] && !searched[nbrs[i]]){
				indexes.add(nbrs[i]);
				indexes.addAll(getAllRoiNeighbours(n_mesh, nbrs[i], is_roi, searched));
				}
			}
		
		return indexes;
		
	}
	
	
	/*****************************************************************
	 * Splits the ROI defined by {@code roi} into two parts, on either side of
	 * {@code plane}.
	 * 
	 * @param mesh
	 * @param plane
	 * @param rois
	 * @param roi
	 * @param new_roi
	 * @param progress
	 * @return The new ROIs; or {@code null} if the plane does not intersect the ROI
	 */
	public static ArrayList<MguiNumber> splitRoiWithPlane(Mesh3D mesh, Plane3D plane, ArrayList<MguiNumber> rois,
										 				  int roi, int new_roi, ProgressUpdater progress){
		
		ArrayList<MguiNumber> new_rois = new ArrayList<MguiNumber>(rois);
		
		// Go through all vertices in ROI; if above plane, leave; if below, set to new_roi
		boolean found = false;
		for (int i = 0; i < rois.size(); i++){
			
			if (rois.get(i).equals(roi)){
				Point3f p = mesh.getVertex(i);
				if (GeometryFunctions.compareToPlane(p, plane) < 0){
					new_rois.set(i, new MguiInteger(new_roi));
					found = true;
					}
				}
			
			}
		
		if (!found) return null;
		return new_rois;
	}
	
	/*****************************************************************
	 * Subdivides a mesh into (roughly) equally sized ROIs, with the final number equal or
	 * close to {@code target_rois}. Generation is constrained by the min/max parameters.
	 * 
	 * <p>A starting parcellation can be specified using the {@code old_rois} parameter; this
	 * can also be {@code null} to start from a random seed.
	 * 
	 * @param mesh
	 * @param old_rois
	 * @param min_rois
	 * @param max_rois
	 * @param max_size
	 * @param min_size
	 * @param target_rois

	 * @return
	 */
	public static ArrayList<MguiNumber> subdivideRois(Mesh3D mesh, 
												   	  ArrayList<MguiNumber> old_rois,
												   	  int min_size, 
												   	  int target_rois,
												   	  ProgressUpdater progress)
												   	  throws MeshFunctionException{
		return subdivideRois(mesh, old_rois, min_size, target_rois, 
							 Integer.MIN_VALUE, Integer.MAX_VALUE,
							 progress);
		
	}
	
	/*****************************************************************
	 * Subdivides a mesh into (roughly) equally sized ROIs, with the final number equal or
	 * close to {@code target_rois}. Generation is constrained by the min/max parameters.
	 * 
	 * <p>A starting parcellation can be specified using the {@code old_rois} parameter; this
	 * can also be {@code null} to start from a random seed.
	 * 
	 * @param mesh
	 * @param old_rois
	 * @param min_rois
	 * @param max_rois
	 * @param max_size
	 * @param min_size
	 * @param target_rois
	 * @param min_roi 				Minimum roi value to include
	 * @param max_roi 				Maximum roi value to include
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<MguiNumber> subdivideRois(final Mesh3D mesh, 
														final ArrayList<MguiNumber> old_rois,
														final int min_size, 
														final int target_rois,
														final int min_roi,
														final int max_roi,
														final ProgressUpdater progress) 
																throws MeshFunctionException{
		
		if (progress == null || !(progress instanceof InterfaceProgressBar))
			return subdivideRoisBlocking(mesh, 
									   	  old_rois,
									   	  min_size, 
									   	  target_rois,
									   	  min_roi,
									   	  max_roi,
									   	  progress);
		
		
		ArrayList<MguiNumber> list = (ArrayList<MguiNumber>)Worker.post(new Job(){
			@Override
			public ArrayList<MguiNumber> run(){
				try{
					return subdivideRoisBlocking(mesh, 
											   	  old_rois,
											   	  min_size, 
											   	  target_rois,
											   	  min_roi,
											   	  max_roi,
											   	  progress);
				}catch (final Exception ex){
					SwingUtilities.invokeLater(new Runnable(){
						public void run(){
							InterfaceSession.handleException(ex);
						}
					});
					return null;
					}
				}
				
			});
		
		if (list == null)
			throw new MeshFunctionException("MeshFunctions.subdivideRois: Exception encountered (see log).");
		
		return list;
		
	}
	
	/*****************************************************************
	 * Subdivides a mesh into (roughly) equally sized ROIs, with the final number equal or
	 * close to {@code target_rois}. Generation is constrained by the min/max parameters.
	 * 
	 * <p>A starting parcellation can be specified using the {@code old_rois} parameter; this
	 * can also be {@code null}, in which case the mesh will first be split by the median x coordinate
	 * and then subdivided.
	 * 
	 * @param mesh
	 * @param old_rois
	 * @param min_rois
	 * @param max_rois
	 * @param max_size
	 * @param min_size
	 * @param target_rois
	 * @param min_roi 				Minimum roi value to include
	 * @param max_roi 				Maximum roi value to include
	 * @return
	 */
	public static ArrayList<MguiNumber> subdivideRoisBlocking(Mesh3D mesh, 
														   	  ArrayList<MguiNumber> old_rois,
														   	  int min_size, 
														   	  int target_rois,
														   	  int min_roi,
														   	  int max_roi,
														   	  ProgressUpdater progress)
														   	  throws MeshFunctionException{
		
		// Validate constraints
		if (min_size < 1 || 
				target_rois * min_size >= mesh.n){
			InterfaceSession.log("MeshFuntions.subdivideRois: Invalid constraints..", 
								 LoggingType.Errors);
			return null;
			}
		
		// Initiate values
		ArrayList<MguiNumber> rois = null; 
		
		if (old_rois == null){
			rois = new ArrayList<MguiNumber>(mesh.n);
			for (int i = 0; i < mesh.n; i++){
					rois.add(new MguiInteger(1));
				}
		}else{
			rois = getLargestContiguousRois(mesh, old_rois, Math.min(-1, min_roi));
			}
		
		
		PriorityQueue<Roi> roi_q = getRoiQueue(rois, min_roi, max_roi);
		int size = roi_q.size();
		if (size >= target_rois){
			InterfaceSession.log("MeshFunctions.subdivideRois: Initial size is >= target size.", 
					 			 LoggingType.Errors);
			return null;
			}
		
		if (size == 1){
			// We'll get no boundary nodes so it needs to be split into two ROIs first
			// So let's split by middle x-coordinate
			
			ArrayList<Point3f> vertices = mesh.getVertices();
			Collections.sort(vertices, new Comparator<Point3f>(){
				public int compare (Point3f v1, Point3f v2){
					return Float.compare(v1.getX(), v2.getX());
				}
			});
			
			float x_split = vertices.get((int)((float)vertices.size() / 2f)).getX();
			int roi_1 = (int)rois.get(0).getValue();
			int roi_2 = roi_1 + 1;
			
			vertices = mesh.getVertices();
			for (int i = 0; i < vertices.size(); i++){
				if (vertices.get(i).getX() > x_split)
					rois.set(i, new MguiInteger(roi_1));
				else
					rois.set(i, new MguiInteger(roi_2));
				}
			
			roi_q = getRoiQueue(rois, min_roi, max_roi);
			}
		
		if (progress != null){
			progress.setMinimum(size);
			progress.setMaximum(target_rois);
			progress.update(0);
			}
		
		String error_str = null;
		int last_size = -1, last_roi = -1;
		
		while (roi_q.size() < target_rois){
			
			Roi biggest_roi = roi_q.peek();
			
			// Test constraints
			if (biggest_roi.size < min_size){
				error_str = "Biggest roi is < min_size..";
				break;
				}
			
			// Prevent infinite loop
			if (biggest_roi.index == last_roi && biggest_roi.size == last_size){
				InterfaceSession.log("MeshFunctions.subdivideRois: Biggest roi did not change.. finishing to prevent infinite loop..", 
						 LoggingType.Errors);
				break;
				}
			
			// subdivide this ROI
			rois = subdivideRoi(mesh, rois, biggest_roi);
			rois = getLargestContiguousRois(mesh, rois, Math.min(-1, min_roi));
			roi_q = getRoiQueue(rois, min_roi, max_roi);
			
			if (progress != null){
				if (progress.isCancelled()){
					InterfaceSession.log("MeshFunctions.subdivideRois: User cancelled process.", 
							 LoggingType.Errors);
					break;
					}
				progress.update(roi_q.size());
				}
			
			last_roi = biggest_roi.index;
			last_size = biggest_roi.size;
			}
		
		if (error_str != null){
			InterfaceSession.log("MeshFunctions.subdivideRois: " + error_str, 
								 LoggingType.Errors);
			return null;
			}
		
		// Return the new rois
		return rois;
		
	}
	
	/*************************************************
	 * Subdivides {@code roi} into two equal subparts; assigns max + 1 as the new roi
	 * index.
	 * 
	 * @param mesh
	 * @param rois
	 * @param roi
	 * @return
	 */
	static ArrayList<MguiNumber> subdivideRoi(Mesh3D mesh, ArrayList<MguiNumber> rois, Roi roi){
		
		int new_roi = getNextAvailableRoiIndex(rois, 0);
		ArrayList<MguiNumber> new_rois = new ArrayList<MguiNumber>(rois);
		ArrayList<MguiNumber> weights = new ArrayList<MguiNumber>();
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		
		for (int i = 0; i < mesh.n; i++)
			weights.add(new MguiDouble(0));
		
		//get its boundary nodes
		int idx = roi.index;
		ArrayList<Integer> b_nodes = MeshFunctions.getRegionBoundaryNodeIndices(mesh, rois, idx);
		boolean[] processed = new boolean[mesh.n];
		
		ArrayList<Integer> new_nodes = new ArrayList<Integer>(b_nodes);
		for (Integer i : new_nodes)
			processed[i] = true;
		
		ArrayList<Integer> closest = new ArrayList<Integer>();
		for (int i = 0; i < mesh.n; i++)
			closest.add(-1);
		new_nodes = getVertexWeights(weights, rois, mesh, n_mesh, b_nodes, idx, processed, closest);
		while (new_nodes.size() > 0)
			new_nodes = getVertexWeights(weights, rois, mesh, n_mesh, new_nodes, idx, processed, closest);
		
		// Start at extreme vertex and expand along neighbourhoods until n = N / 2
		//int r = (int)((double)b_nodes.size() * Math.random());
		// Gets a vertex with the highest weighted path length to another 
		// boundary vertex (i.e., 'extreme' vertex)
		int r = getExtremeVertex(mesh, rois, roi.index);
		b_nodes = getBoundaryNeighbours(mesh, n_mesh, b_nodes, r);
		
		if (b_nodes.size() == 1){
			InterfaceSession.log("MeshFunctions.subdivideRoi: Only one boundary node in ordered list [" + r + "']...", 
								 LoggingType.Errors);
			// TEMP DEBUG
			
			Mesh3DInt mesh_int = new Mesh3DInt((Mesh3D)mesh.clone(), "temp_debug_mesh_" + r);
			ArrayList<MguiNumber> values = new ArrayList<MguiNumber>();
			for (int i = 0; i < mesh.n; i++){
				if (i == r)
					values.add(new MguiDouble(1));
				else
					values.add(new MguiDouble(0));
				}
			mesh_int.addVertexData("debug_boundary_node", values);
			InterfaceSession.getDisplayPanel().getCurrentShapeSet().addShape(mesh_int);
			
			// END DEBUG
			}
		
		int n2 = (int)((double)b_nodes.size() / 2.0);
		
		for (int i = 0; i < b_nodes.size(); i++){
			if (i < n2)
				new_rois.set(b_nodes.get(i), new MguiInteger(roi.index));
			else
				new_rois.set(b_nodes.get(i), new MguiInteger(new_roi));
			}
		
		// Now for each middle vertex, assign the value of its closest boundary vertex
		for (int i = 0; i < closest.size(); i++){
			if (closest.get(i) > 0)
				new_rois.set(i, new_rois.get(closest.get(i)));
			}
		
		return new_rois;
		
	}
	
	private static int getExtremeVertex(Mesh3D mesh, ArrayList<MguiNumber> rois, int roi){
		
		Mesh3D roi_mesh = getRoiSubmesh(mesh, rois, roi);
		ArrayList<Integer> idx_map = getRoiIndices(mesh, rois, roi);
		ArrayList<Double> distances = new ArrayList<Double>();
		ArrayList<Integer> b_nodes = getBoundaryNodeIndices(roi_mesh);
		
		getFarthestVertices(roi_mesh, distances, b_nodes);
		
		int max_idx = -1;
		double max_dist = -Double.MAX_VALUE;
		
		// Get vertex with farthest separation (extreme vertex) 
		for (int i = 0; i < b_nodes.size(); i++){
			double dist = distances.get(i);
			if (dist > max_dist){
				max_idx = b_nodes.get(i);
				max_dist = dist;
				}
			}
		
		return idx_map.get(max_idx);
		
	}
	
	/************************************************************
	 * Returns a list of size {@code mesh.n} integers, such that list(i) = the index of 
	 * the farthest vertex j from vertex i.
	 * 
	 * @param mesh
	 * @return
	 */
	public static ArrayList<Integer> getFarthestVertices(Mesh3D mesh){
		return getFarthestVertices(mesh, null, null);
	}
	
	/************************************************************
	 * Returns a list of size {@code mesh.n} integers, such that list(i) = the index of 
	 * the farthest vertex j from vertex i.
	 * 
	 * @param mesh
	 * @param distances 		Stores the corresponding distances; can be {@code null}.
	 * @return
	 */
	public static ArrayList<Integer> getFarthestVertices(Mesh3D mesh, ArrayList<Double> distances){
		return getFarthestVertices(mesh, distances, null);
	}
	
	/************************************************************
	 * Returns a list of size {@code mesh.n} integers, such that list(i) = the index of 
	 * the farthest vertex j from vertex i. 
	 * 
	 * @param mesh
	 * @param distances 		Stores the corresponding distances; can be {@code null}.
	 * @param n_vertex 			Number of vertices to sample from {@code mesh}. n_vertex <= 0
	 * 							samples from entire mesh (default). This should be specified
	 * 							for very large graphs to avoid resource issues.
	 * @return
	 */
	public static ArrayList<Integer> getFarthestVertices(Mesh3D mesh, ArrayList<Double> distances, ArrayList<Integer> vertices){
		
		InterfaceAbstractGraph graph = GraphFunctions.getDistanceWeightedGraphForMesh(mesh);
		
		ArrayList<Integer> maxes = new ArrayList<Integer>(mesh.n);
		if (distances != null)
			distances.clear();
		
		if (vertices == null){
			vertices = new ArrayList<Integer> (mesh.n);
			for (int i = 0; i < mesh.n; i++)
				vertices.add(i);
			}
		
		Matrix M = GraphFunctions.getShortestPaths(graph, vertices);
		
		for (int i = 0; i < vertices.size(); i++){
			double max = -Double.MAX_VALUE;
			int max_idx = -1;
			for (int j = 0; j < vertices.size(); j++){
				if (M.get(i, j) > max){
					max = M.get(i, j);
					max_idx = j;
					}
				}
			maxes.add(vertices.get(max_idx));
			if (distances != null)
				distances.add(M.get(i, max_idx));
			}
		
		return maxes;
	}
	
	/************************************************************
	 *  Returns a list of size {@code mesh.n} integers, such that list(i) = the index of 
	 * the closest vertex j from vertex i.
	 * 
	 * @param mesh
	 * @return
	 */
	public static ArrayList<Integer> getClosestVertices(Mesh3D mesh){
		return getClosestVertices(mesh, null);
	}
	
	/************************************************************
	 * Returns a list of size {@code mesh.n} integers, such that list(i) = the index of 
	 * the closest vertex j from vertex i.
	 * 
	 * @param mesh
	 * @param distances 		Stores the corresponding distances; can be {@code null}.
	 * @return
	 */
	public static ArrayList<Integer> getClosestVertices(Mesh3D mesh, ArrayList<Double> distances){
		
		InterfaceAbstractGraph graph = GraphFunctions.getDistanceWeightedGraphForMesh(mesh);
		Matrix M = GraphFunctions.getShortestPaths(graph);
		ArrayList<Integer> mins = new ArrayList<Integer>(mesh.n);
		if (distances != null)
			distances.clear();
		
		for (int i = 0; i < mesh.n; i++){
			double min = Double.MAX_VALUE;
			int min_idx = -1;
			for (int j = 0; j < mesh.n; j++){
				if (M.get(i, j) < min){
					min = M.get(i, j);
					min_idx = j;
					}
				}
			mins.add(min_idx);
			if (distances != null)
				distances.add(M.get(i, min_idx));
			}
		
		return mins;
	}
	
	
	/******************************************************
	 * Returns a submesh of {@code mesh} including all vertices corresponding to
	 * {@code rois == roi}.
	 * 
	 * @param mesh
	 * @param rois
	 * @param roi
	 * @return
	 */
	public static Mesh3D getRoiSubmesh(Mesh3D mesh, ArrayList<MguiNumber> rois, int roi){
		
		return getSubMesh(mesh, getRoiSelection(mesh, rois, roi), true);
		
	}
	
	/*******************************************************
	 * Returns a vertex selection corresponding to
	 * {@code rois == roi}.
	 * 
	 * @param mesh
	 * @param rois
	 * @param roi
	 * @return
	 */
	public static VertexSelection getRoiSelection(Mesh3D mesh, ArrayList<MguiNumber> rois, int roi){
		
		ArrayList<Boolean> selected = new ArrayList<Boolean>();
		for (int i = 0; i < mesh.n; i++)
			selected.add(rois.get(i).equals(roi));
				
		return new VertexSelection(selected);
		
	}
	
	/*************************************************
	 * Returns a list of indices corresponding to
	 * {@code rois == roi}.
	 * 
	 * @param mesh
	 * @param rois
	 * @param roi
	 * @return
	 */
	public static ArrayList<Integer> getRoiIndices(Mesh3D mesh, ArrayList<MguiNumber> rois, int roi){
		
		ArrayList<Integer> selected = new ArrayList<Integer>();
		for (int i = 0; i < mesh.n; i++)
			if (rois.get(i).equals(roi))
				selected.add(i);
				
		return selected;
		
	}
	
	/*******************************
	 * Breadth-first search of {@code seed}'s neighbours gives sorted list of closest -> farthest (path length)
	 * 
	 * @param mesh
	 * @param n_mesh
	 * @param b_nodes
	 * @param seed
	 * @return
	 */
	static private ArrayList<Integer> getBoundaryNeighbours(Mesh3D mesh, NeighbourhoodMesh n_mesh, ArrayList<Integer> b_nodes, int seed){
		boolean[] processed = new boolean[mesh.n];
		processed[seed] = true;
		
		ArrayList<BFS_element> nbrs = getBoundaryNeighbours(n_mesh, new TreeSet<Integer>(b_nodes),
															new HashMap<Integer, BFS_element>(), seed, 0);
		
		// TEMP DEBUG
//		
//		Mesh3DInt mesh_int = new Mesh3DInt(mesh, "temp_seed_bound_level_mesh_" + seed);
//		ArrayList<MguiNumber> values = new ArrayList<MguiNumber>();
//		for (int i = 0; i < mesh.n; i++){
//			values.add(new MguiInteger(0));
//			}
//		for (int i = 0; i < b_nodes.size(); i++){
//			int idx = nbrs.get(i).value;
//			values.get(idx).setValue(nbrs.get(i).level);
//			}
//		mesh_int.addVertexData("seed_bound_level", values);
//		InterfaceSession.getDisplayPanel().getCurrentShapeSet().addShape(mesh_int);
		
		// END DEBUG
		
		
		ArrayList<Integer> new_nodes = new ArrayList<Integer>(nbrs.size());
		for (int i = 0; i < nbrs.size(); i++)
			new_nodes.add(nbrs.get(i).value);
		return new_nodes;
	}
	
	static private class BFS_element implements Comparable<BFS_element>{
		
		public int value, level;
		
		public BFS_element(int value, int level){
			this.value = value;
			this.level = level;
		}
		
		public int compareTo(BFS_element e2){
			return ((Integer)level).compareTo(e2.level);
		}
		
	}
	
//	static private ArrayList<BFS_element> getBoundaryNeighbours(NeighbourhoodMesh n_mesh, 
//																TreeSet<Integer> b_set, 
//																int seed, 
//																boolean[] processed, 
//																int level){
//		
//		ArrayList<BFS_element> new_nodes = new ArrayList<BFS_element>();
//		new_nodes.add(new BFS_element(seed, level));
//		//processed[seed] = true;
//		
//		int[] nbrs = n_mesh.getNeighbourhood(seed).getNeighbourList();
//		ArrayList<Integer> ok = new ArrayList<Integer>();
//		
//		// Breadth first; add each neighbour
//		for (int i = 0; i < nbrs.length; i++){
//			if (b_set.contains(nbrs[i]) && !processed[nbrs[i]])
//				ok.add(nbrs[i]);
//			processed[nbrs[i]] = true;
//			}
//		
//		for (int i = 0; i < ok.size(); i++){
//			new_nodes.addAll(getBoundaryNeighbours(n_mesh, b_set, ok.get(i), processed, level + 1));
//			}
//		
//		return new_nodes;
//		
//	}
	
	// Assign levels to boundary nodes, indicating their path length proximity
	// to seed
	static private ArrayList<BFS_element> getBoundaryNeighbours(NeighbourhoodMesh n_mesh, 
																TreeSet<Integer> b_set,
																HashMap<Integer,BFS_element> level_map,
																int seed, 
																int level){
		
		BFS_element current_level = level_map.get(seed);
		if (current_level == null)
			level_map.put(seed, new BFS_element(seed, level));		// First occurrence
		else if (current_level.level > level)				
			current_level.level = level;							// Higher-level occurrence; reprocess
		else
			return null;											// Lower-level occurrence; stop here
		
		int[] nbrs = n_mesh.getNeighbourhood(seed).getNeighbourList();
		
		for (int i = 0; i < nbrs.length; i++)
			if (b_set.contains(nbrs[i]))
				getBoundaryNeighbours(n_mesh, b_set, level_map, nbrs[i], level + 1);
		
		ArrayList<BFS_element> list = new ArrayList<BFS_element>(level_map.values());
		Collections.sort(list);
		return list;
		
	}
	
	/*************************************
	 * Returns the lowest roi index that hasn't been assigned to {@code rois}.
	 * 
	 * @param rois
	 * @return
	 */
	static int getNextAvailableRoiIndex(ArrayList<MguiNumber> rois, int min){
		
		MguiNumber max = Collections.max(rois);
		return (int)max.getValue() + 1;
		
	}
	
	/****************************************************************************
	 * Returns the closest vertex in a list of vertices, or from all vertices if the list is empty.
	 * This searches links and accumulates distance along the links, rather than using Euclidean
	 * distance. If the vertex is unconnected to any of the target vertices, returns {@code null}.
	 * The {@code distance} parameter (if not {@code null}) stores the actual link-wise distance.
	 * 
	 * @param mesh
	 * @param idx
	 * @param search_vertices
	 * @param distance
	 * @return
	 */
	public static int getClosestVertex(Mesh3D mesh, int idx, ArrayList<Integer> search_vertices, MguiDouble distance){
		
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		return getClosestVertex(mesh, idx, search_vertices, distance);
		
	}
	
	
	/****************************************************************************
	 * Returns the closest vertex in a list of vertices, or from all vertices if the list is empty.
	 * This searches links and accumulates distance along the links, rather than using Euclidean
	 * distance. If the vertex is unconnected to any of the target vertices, returns {@code null}.
	 * The {@code distance} parameter (if not {@code null}) stores the actual link-wise distance.
	 * 
	 * @param mesh
	 * @param idx
	 * @param search_vertices
	 * @param distance
	 * @return
	 */
	static int getClosestVertex(Mesh3D mesh, int idx, ArrayList<Integer> search_vertices, 
							    MguiDouble distance,
							    NeighbourhoodMesh n_mesh){
		
		double min_dist = 0;
		boolean[] searched = new boolean[mesh.n];
		double[] distances = new double[mesh.n];
		if (search_vertices == null){
			search_vertices = new ArrayList<Integer>(mesh.n);
			for (int i = 0; i < mesh.n; i++)
				search_vertices.add(i);
			}
		
		for (int i = 0; i < search_vertices.size(); i++){
			
			//double this_dist = getVertexDistance(mesh, n_mesh, idx, search_vertices.get(i), distances);
			
			}
		
		return 0;
		
	}
	
	
	
	static ArrayList<Integer> getVertexWeights(ArrayList<MguiNumber> weights,
											   ArrayList<MguiNumber> roi_surf,
											   Mesh3D mesh,
											   NeighbourhoodMesh n_mesh,
											   ArrayList<Integer> b_nodes, 
											   int roi_index,
											   boolean[] processed, 
											   ArrayList<Integer> closest){

		TreeSet<Integer> new_nodes = new TreeSet<Integer>();

		for (int i = 0; i < b_nodes.size(); i++){
			int idx = b_nodes.get(i);
			double this_dist = weights.get(idx).getValue();
			int[] nbrs = n_mesh.getNeighbourhood(idx).getNeighbourList();
			
			for (int j = 0; j < nbrs.length; j++)
				if (roi_surf.get(nbrs[j]).equals(roi_index))
					if (!processed[nbrs[j]]){
						MguiNumber v = weights.get(nbrs[j]);
						double dist = mesh.getVertex(idx).distance(mesh.getVertex(nbrs[j]));
						double val = 0;
						if (v.getValue() > 0){
							if (v.getValue() > this_dist + dist){
								if (closest.get(idx) < 0)
									closest.set(nbrs[j], idx);					// First iteration
								else
									closest.set(nbrs[j], closest.get(idx)); 	// Propagate through next iters
								}
							val = Math.min(v.getValue(), this_dist + dist);
						}else{
							val = this_dist + dist;
							if (closest.get(idx) < 0)
								closest.set(nbrs[j], idx);					// First iteration
							else
								closest.set(nbrs[j], closest.get(idx)); 	// Propagate through next iters
							}
						
						v.setValue(val);
						new_nodes.add(nbrs[j]);
						processed[nbrs[j]] = true;
						}
			}
		
		return new ArrayList<Integer>(new_nodes);
	}

	
	static class Roi implements Comparable<Roi>{
		
		public Integer index, size;
		
		public Roi(int index, int size){
			this.index = index;
			this.size = size;
		}
		
		public int compareTo(Roi roi2){
			return size.compareTo(roi2.size);
		}
		
	}
	
	static HashMap<Integer, Integer> getRoiSizes(ArrayList<MguiNumber> rois, int min, int max){
		
		HashMap<Integer, Integer> sizes = new HashMap<Integer, Integer>();
		
		for (int i = 0; i < rois.size(); i++){
			int roi = (int)rois.get(i).getValue();
			if (roi >= min && roi <= max){
				Integer size = sizes.get(roi);
				if (size == null)
					sizes.put(roi, 1);
				else
					sizes.put(roi, size + 1);
				}
			}
		
		return sizes;
		
	}
	
	static PriorityQueue<Roi> getRoiQueue(ArrayList<MguiNumber> rois, int min, int max){
		
		HashMap<Integer, Integer> sizes = getRoiSizes(rois, min, max);
		PriorityQueue<Roi> Q = new PriorityQueue<Roi>(sizes.size(), new Comparator<Roi>(){
			public int compare(Roi roi1, Roi roi2){
				return roi2.compareTo(roi1);
			}
		});
		ArrayList<Integer> keys = new ArrayList<Integer> (sizes.keySet());
		
		for (int i = 0; i < keys.size(); i++)
			Q.add(new Roi(keys.get(i), sizes.get(keys.get(i))));
		
		return Q;
	}
	
	/**************************************************
	 * 
	 * Returns a list of nodes which form the boundary of all regions defined by <code>value</code>, specified
	 * by the vertex-wise list <code>values</code>.
	 * 
	 * @param mesh
	 * @param values
	 * @param value
	 * @return
	 */
	public static ArrayList<Integer> getRegionBoundaryNodeIndices(Mesh3D mesh, ArrayList<MguiNumber> values, int value){
		
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		boolean[] processed = new boolean[mesh.n];
		ArrayList<Integer> b_nodes = new ArrayList<Integer>();
		
		for (int i = 0; i < mesh.n; i++){
			if (!processed[i] && values.get(i).compareTo(value) == 0)
				addBoundaryNodes(i, values, n_mesh, processed, b_nodes);
			}
		
		return b_nodes;
	}
	
	// Sort boundary nodes starting at the 0th node, such that each subsequent element is a
	// neighbour of the previous
	private static ArrayList<Integer> sortBoundaryNodes(NeighbourhoodMesh n_mesh, ArrayList<Integer> b_nodes){
		 return sortBoundaryNodes(n_mesh, b_nodes, 0);
	}
	
	private static ArrayList<Integer> sortBoundaryNodes(NeighbourhoodMesh n_mesh, ArrayList<Integer> b_nodes, int start){
		
		int idx = b_nodes.get(start);
		
		ArrayList<Integer> new_nodes = new ArrayList<Integer>(b_nodes.size());
		TreeSet<Integer> new_set = new TreeSet<Integer>();
		TreeSet<Integer> b_set = new TreeSet<Integer>(b_nodes);
		
		int[] nbrs;
		new_nodes.add(idx);
		new_set.add(idx);
		
	
			
			
		return new_nodes;
	}
	
	//recursively add boundary nodes until region is completely searched
	//NB: Java is not great at recursion; may be necessary to increase max stack size
	private static void addBoundaryNodes(int i, ArrayList<MguiNumber> values, 
										 NeighbourhoodMesh n_mesh, boolean[] processed,
										 ArrayList<Integer> b_nodes){
		
		if (processed[i]) return;
		processed[i] = true;
		boolean added = false;
		int[] nbrs = n_mesh.getNeighbourhood(i).getNeighbourList();
		
		for (int j = 0; j < nbrs.length; j++)
			if (values.get(nbrs[j]).compareTo(values.get(i)) == 0){
				addBoundaryNodes(nbrs[j], values, n_mesh, processed, b_nodes);
			}else{
				processed[nbrs[j]] = true;
				if (!added){
					b_nodes.add(i);
					added = true;
					}
				}
		
	}
	
	/**************************************************************
	 * Selects all nodes surrounding a seed node in a region enclosed by selected nodes; if seed node is not fully
	 * enclosed, this will select the entire mesh.
	 * 
	 * @param mesh
	 * @param selection
	 * @param seed_node
	 */
	public static void selectFloodFill(final Mesh3D mesh, final VertexSelection selection, final int seed_node,
									   final ProgressUpdater progress){
		
		//keep adding neighbours until all neighbours have been visited,
		//or a selected node is found
		
		final NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		
		if (progress == null){
			floodFillBlocking(n_mesh, mesh, seed_node, selection, null);
		}else{
			Worker.post(new Job(){
				@Override
				public Void run() {
					floodFillBlocking(n_mesh, mesh, seed_node, selection, progress);
					return null;
				}
				
			});
			}
		
	}
	
	//recursively select all neighbours in a closed selection loop (or entire mesh if seed is not in a loop)
	static void floodFillBlocking(NeighbourhoodMesh n_mesh, Mesh3D mesh, int node, VertexSelection selection, ProgressUpdater progress){
		
		// Use stack for this (avoid method recursion)
		Stack<Integer> todo = new Stack<Integer>();
		todo.push(node);
		
		if (progress != null){
			progress.setMinimum(0);
			progress.setMaximum(mesh.getVertexCount());
			}
		
		int itr = 0;
		
		while (!todo.isEmpty()){
			int nbr = todo.pop();
			selection.select(nbr);
			int[] nbrs = n_mesh.getNeighbourhood(nbr).getNeighbourList();
			for (int i = 0; i < nbrs.length; i++){
				if (!selection.isSelected(nbrs[i]))
					todo.push(nbrs[i]);
				}
			if (progress != null){
				progress.update(itr++);
				if (progress.isCancelled()){
					InterfaceSession.log("Select flood fill operation cancelled by user.");
					selection.clear();
					return;
					}
				}
			}
		
//		
//		
//		boolean[] fill_next = new boolean[nbrs.length];
//		
//		for (int i = 0; i < nbrs.length; i++){
//			int n = nbrs[i];
//			if (!selection.isSelected(n)){
//				selection.select(node);
//				fill_next[i] = true;
//				}
//			}
//		
//		for (int i = 0; i < nbrs.length; i++){
//			if (fill_next[i])
//			floodFill(n_mesh, mesh, n, selection);
//			}
		
	}
	
	/**************************************
	 * Returns a submesh comprised of all unmasked (mask != 0) vertices and all
	 * faces whose vertices are retained. 
	 * 
	 * @param mesh
	 * @param mask
	 * @param value
	 * @param retain
	 * @return submesh
	 */
	public static Mesh3D getSubMesh(Mesh3D mesh_old, 
									ArrayList<MguiNumber> mask, 
									int value, 
									boolean retain){
		return getSubMesh(mesh_old, mask, value, retain, false, null, null);
	}
	
	public static Mesh3D getSubMesh(Mesh3D mesh_old, 
									ArrayList<MguiNumber> mask, 
									int value, 
									boolean retain,
									HashMap<String, ArrayList<MguiNumber>> data_old,
									HashMap<String, ArrayList<MguiNumber>> data_new){
		return getSubMesh(mesh_old, mask, value, retain, false, data_old, data_new);
	}
	
	/**************************************
	 * Returns a submesh based upon a mask. If <code>retain</code> is true, retains all vertices where
	 * <code>mask == value</code>; otherwise retains the inverse. Removes all faces containing removed
	 * vertices. If <code>data_old</code> and <code>data_new</code> are non-null, populates
	 * <code>data_new</code> with the retained values from <code>data_old</code>.
	 * 
	 * @param mesh
	 * @param mask
	 * @param value
	 * @param retain
	 * @param any_in_face retains a face if any of its vertices are retained
	 * @param data
	 * @return submesh
	 */
	public static Mesh3D getSubMesh(Mesh3D mesh_old, 
									ArrayList<MguiNumber> mask, 
									int value, 
									boolean retain,
									boolean any_in_face,
									HashMap<String, ArrayList<MguiNumber>> data_old,
									HashMap<String, ArrayList<MguiNumber>> data_new){
		return MeshFunctions.getSubMesh(mesh_old, mask, value, value, retain, any_in_face, data_old, data_new);
	}
	
	/**************************************
	 * Returns a submesh comprised of all unmasked (mask != 0) vertices and all
	 * faces whose vertices are retained. 
	 * 
	 * @param mesh
	 * @param mask
	 * @param value_from
	 * @param value_to
	 * @param retain
	 * @param any_in_face retains a face if any of its vertices are retained
	 * @param data
	 * @return submesh
	 */
	public static Mesh3D getSubMesh(Mesh3D mesh_old, 
									ArrayList<MguiNumber> mask, 
									int value_from,
									int value_to,
									boolean retain,
									boolean any_in_face,
									HashMap<String, ArrayList<MguiNumber>> data_old,
									HashMap<String, ArrayList<MguiNumber>> data_new){
		
		//add all non-zero vertices to new mesh
		//store index map
		Mesh3D mesh_new = new Mesh3D();
		ArrayList<MguiInteger> indexMap = new ArrayList<MguiInteger>(mesh_old.n);
		
		int k = 0;
		for (int i = 0; i < mesh_old.n; i++){
			if (!retainVertex((int)mask.get(i).getValue(), value_from, value_to, retain)){
				indexMap.add(new MguiInteger(-1));
			}else{
				mesh_new.addVertex(mesh_old.getVertex(i));
				indexMap.add(new MguiInteger(k++));
				}
			}
		
		//for each face, add if each of its vertices is non zero
		for (int i = 0; i < mesh_old.f; i++){
			Mesh3D.MeshFace3D face = mesh_old.getFace(i);
			
			boolean accept = false;
			if (any_in_face)
				accept = retainVertex((int)mask.get(face.A).getValue(), value_from, value_to, retain) || 
						 retainVertex((int)mask.get(face.B).getValue(), value_from, value_to, retain) ||
						 retainVertex((int)mask.get(face.C).getValue(), value_from, value_to, retain);
			else
				accept = retainVertex((int)mask.get(face.A).getValue(), value_from, value_to, retain) && 
						 retainVertex((int)mask.get(face.B).getValue(), value_from, value_to, retain) &&
						 retainVertex((int)mask.get(face.C).getValue(), value_from, value_to, retain);
			
			if (accept){
				//add extra vertices if necessary 
				if (any_in_face){
										
					if (indexMap.get(face.A).getInt() < 0){
						mesh_new.addVertex(mesh_old.getVertex(face.A));
						indexMap.get(face.A).setValue(k++);
						}
					if (indexMap.get(face.B).getInt() < 0){
						mesh_new.addVertex(mesh_old.getVertex(face.B));
						indexMap.get(face.B).setValue(k++);
						}
					if (indexMap.get(face.C).getInt() < 0){
						mesh_new.addVertex(mesh_old.getVertex(face.C));
						indexMap.get(face.C).setValue(k++);
						}
					}
				
				mesh_new.addFace(indexMap.get(face.A).getInt(),
								 indexMap.get(face.B).getInt(),
								 indexMap.get(face.C).getInt());
				
				}
			}
		
		//set node data if not null
		//TODO: make this work for any_in_face
		if (data_old != null && data_new != null){
			Iterator<String> itr = data_old.keySet().iterator();
			
			while (itr.hasNext()){
				String c = itr.next();
				ArrayList<MguiNumber> col = data_old.get(c); 
				ArrayList<MguiNumber> new_col = new ArrayList<MguiNumber>(mesh_new.n);
				for (int i = 0; i < col.size(); i++)
					if (retainVertex((int)mask.get(i).getValue(), value_from, value_to, retain))
						new_col.add((MguiNumber)col.get(i).clone());
				data_new.put(c, new_col);
				}
			}
		
		removeStrandedNodes(mesh_new);
		return mesh_new;
	}
	
	/***********************************
	 * Removes all nodes from <code>mesh</code> which do not have a corresponding face.
	 * 
	 * @param mesh
	 */
	public static void removeStrandedNodes(Mesh3D mesh){
		
		boolean[] has_face = new boolean[mesh.n];
		
		for (int i = 0; i < mesh.f; i++){
			Mesh3D.MeshFace3D face = mesh.getFace(i);
			has_face[face.A] = true;
			has_face[face.B] = true;
			has_face[face.C] = true;
			}
		
		int offset = 0;
		
		for (int i = 0; i < has_face.length; i++){
			if (!has_face[i]){
				mesh.removeVertex(i - offset);
				offset++;
				}
			}
		
	}
	
	public static boolean validateSurface(Mesh3D mesh){
		boolean ok;
		ok = removeDuplicateNodes(mesh);
		return ok & removeBadFaces(mesh);
	}
	
	public static boolean removeDuplicateFaces(Mesh3D mesh){
		ArrayList<Boolean> dups = getDuplicateFaces(mesh);
		int j = 0;
		for (int i = 0; i < dups.size(); i++)
			if (dups.get(i).booleanValue())
				mesh.removeFace(i - j++);
		return true;
	}
	
	public static boolean isBadFace(Mesh3D.MeshFace3D face){
		return face.A == face.B || face.A == face.C || face.B == face.C;
	}
	
	public static boolean removeBadFaces(Mesh3D mesh){
		ArrayList<Mesh3D.MeshFace3D> faces = mesh.getFaces();
		if (faces == null) return true;
		mesh.removeAllFaces();
		for (int i = 0; i < faces.size(); i++){
			Mesh3D.MeshFace3D face = faces.get(i);
			if (!isBadFace(face)) 
				mesh.addFace(face);
			}
		return removeDuplicateFaces(mesh);
	}
	
	/*************************
	 * Returns an array of booleans where true indicates a duplicate face
	 * @return
	 */
	public static ArrayList<Boolean> getDuplicateFaces(Mesh3D mesh){
		SortedMesh s_mesh = new SortedMesh(mesh);
		ArrayList<Boolean> dups = new ArrayList<Boolean> (mesh.f);
		for (int i = 0; i < mesh.f; i++)
			dups.add(new Boolean(false));
		FaceComparator c = new FaceComparator();
		//dups.add(new Boolean(false));
		
		for (int i = 0; i < mesh.f - 1; i++){
			int i1 = s_mesh.sorted_faces.get(i);
			int i2 = s_mesh.sorted_faces.get(i + 1);
			Mesh3D.MeshFace3D f1 = SortedMesh.getSortedFace(mesh.getFace(i1));
			Mesh3D.MeshFace3D f2 = SortedMesh.getSortedFace(mesh.getFace(i2));
			dups.set(i1, new Boolean(c.compare(f1, f2) == 0));
		}
		
		return dups;
	}
	
	/***************************
	 * Removes all duplicate vertices from {@code mesh}, and changes {@code mesh} in place.
	 * <p>Duplicates are determined as {@code pt1.distance(pt2) < MeshFunctions.tolerance}.
	 * 
	 * @param mesh
	 * @return {@code true} if successful
	 */
	public static boolean removeDuplicateNodes(Mesh3D mesh){
		
		SortedMesh s_mesh = new SortedMesh(mesh);
		
		int i = 0;
		int l = 0;
		HashMap<Integer,Integer> index_map = new HashMap<Integer,Integer>();
		Mesh3D new_mesh = new Mesh3D();
		boolean[] is_dup = new boolean[mesh.n];
		
		// Identify duplicates
		while (i < s_mesh.sorted_nodes.size()) {
			if (!is_dup[i]) {
				int j = 1;
				ArrayList<Integer> dups = new ArrayList<Integer>();
				while (i+j < s_mesh.sorted_nodes.size() &&
						mesh.getVertex(s_mesh.sorted_nodes.get(i))
							.distance(mesh.getVertex(s_mesh.sorted_nodes.get(i+j))) < MeshFunctions.tolerance) {
					dups.add(s_mesh.sorted_nodes.get(i+j));
					is_dup[i+j] = true;
					j++;
					}
				
				if (dups.size() > 0) {
					for (Integer k : dups) {
						index_map.put(k, l);
						}
					}
				index_map.put(s_mesh.sorted_nodes.get(i), l);
				new_mesh.addVertex(mesh.getVertex(s_mesh.sorted_nodes.get(i)));
				l++;
				}
			i++;
			}
		
		ArrayList<MeshFace3D> faces = new ArrayList<MeshFace3D>();
		
		for (i = 0; i < mesh.f; i++){
			Mesh3D.MeshFace3D face = mesh.getFace(i);
			Mesh3D.MeshFace3D new_face = new Mesh3D.MeshFace3D(index_map.get(face.A),
															   index_map.get(face.B),
															   index_map.get(face.C));
			
			int idx = Collections.binarySearch(faces, new_face);
			if (idx < 0) {
				faces.add(-idx-1,new_face);
				}
			}
		
		new_mesh.addFaces(faces);
		
		mesh.setFromMesh(new_mesh);
			
		return true;
	}
	
	/*************************
	 * Returns an array of booleans where true indicates a duplicate node
	 * @return
	 */
	public static List<Integer> getDuplicateNodes(Mesh3D mesh){
		SortedMesh s_mesh = new SortedMesh(mesh);
		
		int i = 0;
		TreeSet<Integer> dup_nodes = new TreeSet<Integer>();
		
		while (i < s_mesh.sorted_nodes.size()) {
			if (mesh.getVertex(s_mesh.sorted_nodes.get(i))
					.equals(mesh.getVertex(s_mesh.sorted_nodes.get(i+1)))) {
				dup_nodes.add(s_mesh.sorted_nodes.get(i));
				dup_nodes.add(s_mesh.sorted_nodes.get(i+1));
				}
			}
		
		return new ArrayList<Integer>(dup_nodes);
	}
	
	static class NodeComparator implements Comparator<Point3f>{
		public int compare(Point3f n1, Point3f n2) {
			int t = GeometryFunctions.compareFloat(n1.x, n2.x);
			if (t != 0) return t;
			t = GeometryFunctions.compareFloat(n1.y, n2.y);
			if (t != 0) return t;
			return GeometryFunctions.compareFloat(n1.z, n2.z);
		}
	}
	
	static class FaceComparator implements Comparator<Mesh3D.MeshFace3D>{
		public int compare(Mesh3D.MeshFace3D f1, Mesh3D.MeshFace3D f2) {
			if (f1.A > f2.A) return 1;
			if (f1.A < f2.A) return -1;
			if (f1.B > f2.B) return 1;
			if (f1.B < f2.B) return -1;
			if (f1.C > f2.C) return 1;
			if (f1.C < f2.C) return -1;
			return 0;
		}
	}
	
	public static Mesh3D transformWithMatrix(final Mesh3D mesh_in, 
											 final Matrix4d matrix, 
											 final InterfaceProgressBar progress_bar){
		
		if (progress_bar != null)
			progress_bar.register();
		
		//foxtrot rocks...
		Mesh3D mesh =
			((Mesh3D)Worker.post(new Job(){
			
			@Override
			public Mesh3D run(){
				
				Mesh3D mesh_new = new Mesh3D(mesh_in);
				GeometryFunctions.transform(mesh_new, matrix, progress_bar);
				
				return mesh_new;
			}
			}));
		
		if (progress_bar != null)
			progress_bar.deregister();
		
		return mesh;
		
	}
	
	private static boolean retainVertex(int test, int value, boolean retain){
		if (value == test){
			return retain;
			}
		return !retain;
	}
	
	private static boolean retainVertex(int test, int value_from, int value_to, boolean retain){
		if (value_from <= test && test <= value_to){
			return retain;
			}
		return !retain;
	}
	
	public static void subdivideMesh(Mesh3D mesh, int iter){
		MeshSubdivision.subdivideButterflyScheme(mesh, iter, null);
	}
	
	/******************
	 * Subdivides <mesh> iter times, for all triangle in mesh. Operates directly on mesh,
	 * so if user requires a retained original, he/she must make a copy before calling.
	 * @param mesh Mesh object to subdivide.
	 * @param iter Number of times to subdivide this mesh. If <iter> <= 0, operation will
	 * 			   do nothing.
	 */
	
	public static void subdivideMesh(Mesh3DInt mesh, int iter){
		subdivideMesh(mesh, iter, 0, 0);
	}
	
	/******************
	 * Subdivides <mesh> iter times, for triangles which meet the criteria specified by
	 * <edgeThreshold> and <areaThreshold>. Operates directly on mesh,
	 * so if user requires a retained original, he/she must make a copy before calling.
	 * @param mesh Mesh object to subdivide
	 * @param edgeThreshold Triangle will be subdivided if one edge is at least as long as
	 * 						<edgeThreshold>. If edgeThreshold <= 0, edge length is not checked.
	 * @param areaThreshold Triangle will be subdivided if its area is at least <areaThreshold>.
	 * 						If <areaThreshold> <= 0, area is not checked.
	 * @param iter Number of times to subdivide this mesh. If <iter> <= 0, and either
	 * 			   <edgeThreshold> or <areaThreshold> > 0, operation will subdivide until no 
	 * 			   triangles satisfy criteria. Otherwise it will do nothing.
	 */
	
	public static void subdivideMesh(Mesh3DInt mesh, int iter, double edgeThreshold, double areaThreshold){
		MeshSubdivision.subdivideButterflyScheme(mesh, 1.0/16.0);
		
	}
	
	public static ArrayList<MguiNumber> maskMeshWithPlane(Mesh3D mesh, Plane3D plane, double above_val, double below_val){
		return maskMeshWithPlane(mesh, plane, above_val, below_val, Double.NaN);
	}
	
	public static ArrayList<MguiNumber> maskMeshWithPlane(Mesh3D mesh, Plane3D plane, double above_val, double below_val,
													    double contained_val){
		return MeshFunctions.maskMeshWithPlane(mesh, plane, above_val, below_val, contained_val, null);
	}
	
	/*****************
	 * Masks a mesh with a plane.
	 * 
	 */
	public static ArrayList<MguiNumber> maskMeshWithPlane(Mesh3D mesh, Plane3D plane, double above_val, double below_val,
														double contained_val, VertexSelection selection){
		
		ArrayList<MguiNumber> mask = new ArrayList<MguiNumber>();
		
		for (int i = 0; i < mesh.n; i++){
			if (selection == null || selection.isSelected(i)){
				Point3f p = mesh.getVertex(i);
				int compare = GeometryFunctions.compareToPlane(p, plane);
				if (compare < 0)
					mask.add(new MguiInteger(below_val));
				else if (compare > 0)
					mask.add(new MguiInteger(above_val));
				else
					mask.add(new MguiInteger(contained_val));
			}else{
				mask.add(new MguiInteger(0));
				}
			}
		
		return mask;
	}
	
	/*****************
	 * Adds an IndexedTriangleArray to the current mesh. Coincident nodes from either object
	 * will be represented as two seperate nodes, however. To merge these nodes into a single
	 * node, call decimateByLength with a minimal length threshold. 
	 * @param mesh Mesh object to which nodes and faces will be added.
	 * @param tris IndexedTriangleArray object from which nodes and face indices will be extracted.
	 */
	
	public static void addIndexedTriangleArray(Mesh3D mesh, IndexedTriangleArray tris){
		Point3f[] newNodes = new Point3f[tris.getVertexCount()];
		//have to create each point too...
		for (int i = 0; i < newNodes.length; i++)
			newNodes[i] = new Point3f();
		tris.getCoordinates(0, newNodes);
		ArrayList<Point3f> addNodes = new ArrayList<Point3f>(tris.getVertexCount());
		
		for (int i = 0; i < newNodes.length; i++)
			addNodes.add(newNodes[i]);
		int n = mesh.n;
		mesh.addVertices(addNodes);
		//mesh.nodes.addAll(addNodes);
		
		int[] indices = new int[tris.getIndexCount()];
		tris.getCoordinateIndices(0, indices);
		
		for (int i = 0; i < indices.length; i += 3)
			mesh.addFace(n + indices[i], n + indices[i + 1], n + indices[i + 2]);
			
	}
	
	/***************************************
	 * Triangulates a set of nodes that form a ring.
	 * @param mesh
	 * @param nodes
	 * @return
	 */
	public static Mesh3D getTriangulation(Point3f[] nodes){
		Mesh3D t_mesh = new Mesh3D();
		
		GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
		
		gi.reset(GeometryInfo.POLYGON_ARRAY);
		gi.setCoordinates(nodes);
		gi.setStripCounts(new int[]{nodes.length});
		gi.setContourCounts(new int[]{1});
		gi.convertToIndexedTriangles();
		MeshFunctions.addIndexedTriangleArray(t_mesh, 
				(IndexedTriangleArray)gi.getIndexedGeometryArray());
		
		
		return t_mesh;
	}
	
	/**********************
	 * Merges the mesh shapes in {@code list} and returns the resulting single mesh.
	 * 
	 * @param mesh_list
	 * 
	 * @throws MeshFunctionException If the merge failed
	 */
	public static Mesh3D mergeMeshes(ArrayList<Mesh3D> mesh_list) throws MeshFunctionException{
		return mergeMeshes(mesh_list, null);
	}
	
	/**********************
	 * Merges the mesh shapes in {@code list} and returns the resulting single mesh. If
	 * {@code index_map} is not null, also provides a mapping of the original indices to the
	 * merged mesh indices.
	 * 
	 * @param mesh_list
	 * @param index_map
	 * 
	 * @throws MeshFunctionException If the merge failed
	 */
	public static Mesh3D mergeMeshes(ArrayList<Mesh3D> mesh_list, 
									 ArrayList<HashMap<Integer,Integer>> index_map) throws MeshFunctionException{
		
		if (mesh_list.size() < 2)
			throw new MeshFunctionException("MeshFunctions.mergeMeshes: At least two meshes are required.");
		
		
		
		Mesh3D new_mesh = new Mesh3D(mesh_list.get(0));
		
		if (index_map != null){
			index_map.clear();
			HashMap<Integer,Integer> this_map = new HashMap<Integer,Integer>();
			index_map.add(this_map);
			for (int i = 0; i < new_mesh.n; i++)
				this_map.put(i, i);
			}
		
		int n_cum = new_mesh.n;
		
		for (int m = 1; m < mesh_list.size(); m++){
			Mesh3D mesh = mesh_list.get(m);
			HashMap<Integer,Integer> this_map = null;
			if (index_map != null){
				index_map.clear();
				this_map = new HashMap<Integer,Integer>();
				index_map.add(this_map);
				}
			
			for (int i = 0; i < mesh.n; i++){
				new_mesh.addVertex(mesh.getVertex(i));
				int idx = new_mesh.getSize() - 1;
				if (this_map != null)
					this_map.put(i, idx);
				}
			
			for (int i = 0; i < mesh.f; i++){
				MeshFace3D face = mesh.getFace(i);
				face.A += n_cum;
				face.B += n_cum;
				face.C += n_cum;
				new_mesh.addFace(face);
				}
			
			n_cum += mesh.n;
			}
		
		return new_mesh;
	}
	
	/*****************
	 * Decimates this mesh by merging multiple nodes into a single node if they are
	 * separated by less than <threshold>.
	 * @param mesh Mesh3D object to be decimated
	 * @param threshold Threshold separation length at which to merge two nodes
	 * @TODO possibly add some code to situate decimated node at average of all nodes
	 * 		 that were removed due to proximity with it (i.e., all nodes decimate to
	 * 		 an average location). In its current state, the method will
	 * 		 situate the decimated node at the location of the node with the lowest
	 * 		 x value, which is somewhat arbitrary, but shouldn't be an issue except at
	 * 		 large thresholds...
	 */
	
	public static void decimateByDistance(Mesh3D mesh, Mesh3D newmesh, double threshold){
		//  [Plan of attack]
		//	[First, sort list by x and compare nodes sequentially, using the x dist
		//	 as a first pass filter]
		//1. Create new array of IndexedNode objects
		//2. Release original node list
		//3. Set all newindex[i] = index[i], all finalindex to -1
		//4. Sort list by x
		//5. For each node i in indexedNodes:
		//6.	if abs(index[i+1].x - index[i].x) < threshold
		//7.		if dist(index[i], newindex[i+1]) < threshold
		//8.			set newindex[i+1] = newindex[i]
		//	[Now, set final indices for nodes which are not to be removed]
		//9. Set int k = 0
		//10. For each node i in indexedNodes:
		//11. 	if index[i] == newindex[i]
		//12.		finalindex[i] = k
		//13.		k++
		//14.	else
		//			finalindex[i] = finalindex[newindex]
		//  [Now, reindex the faces list, removing invalid faces]
		//15. Sort indexedNodes by index
		//16. For each face i int faces
		//17. 	For each node in face
		//18.		search for index m 
		//19.		set this node = finalindex[m]
		//20. 	if node1, node2, node3 are not distinct
		//21.		remove face from list
		//	[Finally, create new decimated nodes list]
		//22. Set nodes = new array
		//23. For each index i in indexedNodes
		//24. if index[i] = newindex[i]
		//25.	add node[i] to nodes
		
		ArrayList<IndexedNode> indexedNodes = new ArrayList<IndexedNode>(mesh.n);
		IndexedNode thisNode;
		for (int i = 0; i < mesh.n; i++){
			thisNode = new IndexedNode(mesh.getVertex(i), i);
			thisNode.newindex = i;
			thisNode.finalindex = -1;
			indexedNodes.add(thisNode);
			}
		
		//sort list by x
		Collections.sort(indexedNodes, new Comparator<IndexedNode>(){
			public int compare(IndexedNode a, IndexedNode b){
				if (a.node.x < b.node.x) return -1;
				if (a.node.x == b.node.x) return 0;
				return -1;
				}
		});
		
		//don't remove adjacent nodes...
		//boolean blnLastRemoved = false;
		
		//compare subsequent nodes and mark those to be removed
		for (int i = 1; i < indexedNodes.size(); i++)
			if (Math.abs(indexedNodes.get(i).node.x - indexedNodes.get(i - 1).node.x) 
															< threshold)
				if (indexedNodes.get(i).node.distance(indexedNodes.get(i - 1).node) 
															< threshold){
					//this node must go!
					indexedNodes.get(i).newindex = indexedNodes.get(i - 1).newindex;
					}
			
		//set final indices
		int k = -1, n;
		for (int i = 0; i < indexedNodes.size(); i++){
			if (indexedNodes.get(i).index == indexedNodes.get(i).newindex) k++;
			indexedNodes.get(i).finalindex = k;
			}
		
		n = k;
		
		//comparator for index
		Comparator c = new Comparator<IndexedNode>(){
			public int compare(IndexedNode a, IndexedNode b){
				if (a.index < b.index) return -1;
				if (a.index == b.index) return 0;
				return 1;
				}
			};
		
		//sort node list by index
		Collections.sort(indexedNodes, c);
		
		//instantiate new mesh
		Mesh3D.MeshFace3D thisFace;
		
		//reindex and populate faces list
		for (int i = 0; i < mesh.f; i++){
			thisFace = new MeshFace3D(mesh.getFace(i));
			thisFace.A = indexedNodes.get(thisFace.A).finalindex;
			thisFace.B = indexedNodes.get(thisFace.B).finalindex;
			thisFace.C = indexedNodes.get(thisFace.C).finalindex;
			if (thisFace.isValid())
				newmesh.addFace(thisFace);
			}
		
		//newmesh.nodes = new ArrayList<Point3f>(n);
		ArrayList<Point3f> list = new ArrayList<Point3f>(n);
		for (int i = 0; i <= n; i++)
			list.add(new Point3f());
		
		//populate new nodes list
		for (int i = 0; i < indexedNodes.size(); i++)
			if (indexedNodes.get(i).index == indexedNodes.get(i).newindex){
				//newmesh.nodes.add(indexedNodes.get(i).node);
				list.set(indexedNodes.get(i).finalindex, indexedNodes.get(i).node);
				
				if (indexedNodes.get(i).finalindex != list.size() - 1)
					i = i + 0;
			}
		newmesh.addVertices(list);
		
		//debug
		for (int i = 0; i < newmesh.f; i++){
			MeshFace3D face = newmesh.getFace(i); 
			if (face.A >= newmesh.n ||
				face.B >= newmesh.n ||
				face.C >= newmesh.n)
				i = i + 0;
		}
		
	}
	
	/**********
	 * 
	 * Inflate this mesh using the TRP method.
	 * 
	 * TODO: Remove worker here
	 * 
	 * @param mesh3D
	 * @param lambda
	 * @param beta
	 * @param max_itr
	 * @param progress
	 * @return
	 */
	public static Mesh3D inflateMeshTRP(final Mesh3DInt mesh3D, final double lambda, final double beta, 
										final long max_itr, final ProgressUpdater progress){
		
		if (progress == null){
			return MeshInflation.inflateMeshTRP(mesh3D, lambda, beta, max_itr, progress);
		}else{
			
			Mesh3D result = (Mesh3D)Worker.post(new Job(){
				public Mesh3D run(){
					return MeshInflation.inflateMeshTRP(mesh3D, lambda, beta, max_itr, progress);
				}
			});
			
			return result;
			
			}
	}
	
	public static ArrayList<Mesh3D> getMeshParts(Mesh3D mesh){
		return getMeshParts(mesh, null, null, null);
	}
	
	public static ArrayList<Mesh3D> getMeshParts(Mesh3D mesh,
	 		 HashMap<String,ArrayList<MguiNumber>> old_data,
	 		 ArrayList<HashMap<String,ArrayList<MguiNumber>>> parts_data){
		
		return getMeshParts(mesh, old_data, parts_data, null);
		
	}
	
	/*****************************
	 * 
	 * Returns a set of non-contiguous meshes from {@code mesh}. If mesh is a single contiguous 
	 * surface, this method will return only that surface.
	 * 
	 * @param mesh 			Mesh from which to extract parts
	 * @param old_data		Vertex data to be transferred to the new meshes
	 * @param new_data 		Vertex data transferred to the new meshes. Should be an empty object.
	 * 
	 * @return 			List of created mesh parts 
	 * 
	 */
	public static ArrayList<Mesh3D> getMeshParts(Mesh3D mesh,
												 HashMap<String,ArrayList<MguiNumber>> old_data,
												 ArrayList<HashMap<String,ArrayList<MguiNumber>>> parts_data,
												 ProgressUpdater updater){
		
		ArrayList<Mesh3D> parts = new ArrayList<Mesh3D>();
		ArrayList<Boolean> is_added = new ArrayList<Boolean>(Collections.nCopies(mesh.n, false));
		
		mesh = (Mesh3D)mesh.clone();
		removeDuplicateNodes(mesh); // Required to properly instantiate neighbourhood mesh
		
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		
		if (updater != null) {
			updater.setMaximum(mesh.n);
			}
		
		// Start at first node, search neighbours until there are none
		int i = 0;
		
		while (i < mesh.n) {
			
			if (updater != null) {
				updater.update(i);
				}
			
			List<Integer> vertices = new ArrayList<Integer>(getAllNeighbours(n_mesh, i, Integer.MAX_VALUE));
			VertexSelection selection = new VertexSelection(mesh.n);
			selection.select(vertices);
			
			HashMap<String,ArrayList<MguiNumber>> new_data = null;
			if (old_data != null) {
				new_data = new HashMap<String,ArrayList<MguiNumber>>();
				parts_data.add(new_data);
				}
			
			parts.add(getSubMesh(mesh,
								 selection,
								 true,
								 false,
								 old_data,
								 new_data
								 ));
			
			for (Integer j : vertices) {
				is_added.set(j, true);
				}
			
			int j = i;
			while (j <= mesh.n) {
				if (j==mesh.n || !is_added.get(j)) {
					i = j;
					break;
					}
				j++;
				}
			
			}
		
		int k = 0;
		for (Mesh3D part : parts) {
			System.out.println("Mesh " + k + ": ");
			System.out.println("\tNodes: " + part.n);
			System.out.println("\tFaces: " + part.f);
			int[] copy = Arrays.copyOf(part.faces, part.faces.length);
			Arrays.sort(copy);
			System.out.println("\tMin/max index: " + copy[0] + ", " + copy[copy.length-1]);
			}
		
		return parts;
		
	}
	
	// Recursively add neighbours until there are no more, up until max_depth
//	private static Set<Integer> getAllNeighbours(NeighbourhoodMesh n_mesh, int i, int max_depth) {
//		ArrayList<Boolean> is_processed = new ArrayList<Boolean>(Collections.nCopies(n_mesh.getSize(), false));
//		return getAllNeighbours(new TreeSet<Integer>(), is_processed, n_mesh, i, max_depth, 0);
//	}
//	
//	// Recursively add neighbours until there are no more, up until max_depth
//	private static Set<Integer> getAllNeighbours(Set<Integer> nbrs, List<Boolean> is_processed, NeighbourhoodMesh n_mesh, int i, int max_depth, int this_depth) {
//		
//		if (this_depth >= max_depth) return nbrs;
//		
//		nbrs.add(i);
//		is_processed.set(i, true);
//		Neighbourhood hood = n_mesh.getNeighbourhood(i);
//		//nbrs.addAll(hood.neighbours);
//		
//		for (Integer n : hood.neighbours) {
//			if (!is_processed.get(n)) {
//				nbrs = getAllNeighbours(nbrs, is_processed, n_mesh, n, max_depth, this_depth+1);
//				}
//			}
//		
//		return nbrs;
//	}
	
	
	private static Set<Integer> getAllNeighbours(NeighbourhoodMesh n_mesh, int i, int max_depth) {
		
		Set<Integer> nbr_set = new TreeSet<Integer>();
		List<Boolean> is_processed = new ArrayList<Boolean>(Collections.nCopies(n_mesh.getSize(), false));
		
		nbr_set.add(i);
		is_processed.set(i, true);
		Neighbourhood hood = n_mesh.getNeighbourhood(i);
		if (hood.neighbours.isEmpty()) return nbr_set;
		
		nbr_set.addAll(hood.neighbours);
		
		Stack<Integer> new_nbrs = new Stack<Integer>();
		new_nbrs.addAll(hood.neighbours);
		
		int d = 0;
		
		do {
			int j = new_nbrs.pop();
			hood = n_mesh.getNeighbourhood(j);
			
			for (Integer k : hood.neighbours) {
				if (!is_processed.get(k)) {
					nbr_set.add(k);
					new_nbrs.push(k);
					is_processed.set(k, true);
					}
				}
			d++;
			} 
		while (d < max_depth && !new_nbrs.isEmpty());
		
		return nbr_set;
		
	}
		
	
	/**********************************
	 * Returns a submesh defined by the connect ring <code>ring</code>.
	 * Triangulation is performed by connecting n to n + 2, to n - 1, to n + 3, etc.
	 * 
	 * @return optimal submesh
	 */
	public static Mesh3D getSubMesh(Mesh3D mesh, ArrayList<Integer> ring){
		
		if (ring.size() < 3) return null;
		
		//trivial case
		if (ring.size() == 3){
			Mesh3D mesh2 = new Mesh3D();
			mesh2.addVertex(mesh.getVertex(ring.get(0)));
			mesh2.addVertex(mesh.getVertex(ring.get(1)));
			mesh2.addVertex(mesh.getVertex(ring.get(2)));
			mesh2.addFace(0, 1, 2);
			return mesh2;
		}
		
		//start at n = 0
		int i = 0, j = 1, k = 2;
		int n = ring.size();
		Mesh3D new_mesh = new Mesh3D();
		
		//add nodes
		for (int m = 0; m < ring.size(); m++)
			new_mesh.addVertex(mesh.getVertex(ring.get(i)));
		
		//first face
		new_mesh.addFace(i, j, k);
		
		while (i != k){
			j = i;
			i = getNode(n, i - 1);
			if (i != k)
				new_mesh.addFace(k, j, i);
			else
				break;
			j = k;
			k = getNode(n, k + 1);
			if (i != k)
				new_mesh.addFace(k, j, i);
			}
		
		return new_mesh;
		
	}
	
	static int getNode(int n, int i){
		if (i < 0) return n + i;
		if (i > n - 1) return i - n;
		return i;
	}
	
	/**********************************
	 * Returns the optimal submesh defined by the connect ring <code>ring</code>.
	 * Triangulation is optimized by minimizing the sum of angles for each permutation of
	 * possible edge configurations.
	 * @return optimal submesh
	 */
	public static Mesh3D getOptimalSubmesh(Mesh3D mesh, ArrayList<MguiInteger> ring){
		
		if (ring.size() < 3) return null;
		
		//trivial case
		if (ring.size() == 3){
			Mesh3D mesh2 = new Mesh3D();
			mesh2.addVertex(mesh.getVertex(ring.get(0).getInt()));
			mesh2.addVertex(mesh.getVertex(ring.get(1).getInt()));
			mesh2.addVertex(mesh.getVertex(ring.get(2).getInt()));
			mesh2.addFace(0, 1, 2);
			return mesh2;
		}
		
		ArrayList<EdgeConfiguration> configs = new ArrayList<EdgeConfiguration>();
		//get all possible edges
		ArrayList<int[]> edges = new ArrayList<int[]>();
		for (int i = 0; i < ring.size() - 2; i++)
			for (int j = i + 2; j < ring.size(); j++)
				if (j - i > 1 && j - i < ring.size() - 1)
					edges.add(new int[]{i, j});
			
		//get all possible configurations
		//for each edge i, add other edges recursively
		for (int i = 0; i < edges.size(); i++)
			addConfigs(configs, new EdgeConfiguration(mesh, ring), edges.get(i), edges);
		
		//return mesh with lowest angle sum
		double min = Double.MAX_VALUE;
		int r = -1;
		for (int i = 0; i < configs.size(); i++){
			double sum = configs.get(i).getAngleSum();
			if (sum < min){
				min = sum;
				r = i;
				}
			}
		
		Mesh3D m = configs.get(r).getMesh();
		removeBadFaces(m);
		m.finalize();
		return m;
	}
	
	static void addConfigs(ArrayList<EdgeConfiguration> configs, EdgeConfiguration config, 
						  int[] edge, ArrayList<int[]> edges){
		
		if (!config.add(edge)) return;
		
		if (config.isComplete()){
			configs.add(config);
			return;
			}
		
		for (int j = 0; j < edges.size(); j++)
			if (edge != edges.get(j) && !config.contains(edges.get(j)))
				addConfigs(configs, config.copy(), edges.get(j), edges);

	}
	
	static class EdgeConfiguration {
		
		Mesh3D mesh;
		ArrayList<MguiInteger> ring;
		ArrayList<int[]> list = new ArrayList<int[]>();

		public EdgeConfiguration(Mesh3D mesh, ArrayList<MguiInteger> ring){
			this.mesh = mesh;
			this.ring = ring;
		}
		
		//illegal if edges cross
		public boolean isLegal(int[] edge){
			boolean legal = true;
			for (int i = 0; i < list.size(); i++)
				legal &= !((edge[0] < list.get(i)[0] && edge[1] < list.get(i)[1]) ||
						   (edge[0] > list.get(i)[0] && edge[1] > list.get(i)[1]));
			return legal;
		}
		
		public boolean isComplete(){
			return list.size() >= ring.size() - 3;
		}
		
		public EdgeConfiguration copy(){
			EdgeConfiguration c = new EdgeConfiguration(mesh, ring);
			for (int i = 0; i < list.size(); i++)
				c.add(list.get(i));
			return c;
		}
		
		public boolean add(int[] e){
			if (!isLegal(e)) return false;
			list.add(e);
			return true;
		}
		
		public int size(){
			return list.size();
		}
		
		public boolean contains(int[] edge){
			for (int i = 0; i < list.size(); i++)
				if (list.get(i).equals(edge)) return true;
			return false;
		}
		
		public Mesh3D getMesh(){
			Mesh3D r_mesh = new Mesh3D();
			for (int i = 0; i < ring.size(); i++)
				r_mesh.addVertex(mesh.getVertex(ring.get(i).getInt()));
			
			//for each edge, add a face
			for (int i = 0; i < list.size(); i++){
				int[] edge = list.get(i);
				
				int prev = getNode(edge[1] - 1);
				int next = getNode(edge[1] + 1);
				
				if (pathDistance(prev, edge[0]) == 1 || hasEdge(prev, edge[0]))
					r_mesh.addFace(edge[0], prev, edge[1]);
				if (pathDistance(next, edge[0]) == 1 || hasEdge(next, edge[0]))
					r_mesh.addFace(edge[0], next, edge[1]);
				
				prev = getNode(edge[0] - 1);
				next = getNode(edge[0] + 1);
				
				if (pathDistance(prev, edge[1]) == 1 || hasEdge(prev, edge[1]))
					r_mesh.addFace(edge[1], prev, edge[0]);
				if (pathDistance(next, edge[1]) == 1 || hasEdge(next, edge[1]))
					r_mesh.addFace(edge[1], next, edge[0]);
				
				}
			
			removeDuplicateFaces(r_mesh);
			r_mesh.finalize();
			return r_mesh;
		}
		
		public boolean hasEdge(int i, int j){
			for (int k = 0; k < list.size(); k++)
				if ((list.get(k)[0] == i && list.get(k)[1] == j) ||
					(list.get(k)[0] == j && list.get(k)[1] == i))
					return true;
			return false;
		}
		
		int getNode(int i){
			if (i < 0) return ring.size() + i;
			if (i > ring.size() - 1) return i - ring.size();
			return i;
		}
		
		int pathDistance(int i, int j){
			int d1 = Math.abs(i - j);
			int d2 = Math.min(i, j) + ring.size() - Math.max(i, j);
			return Math.min(d1, d2);
		}
		
		public double getAngleSum(){
			double sum = 0;
			
			//for each edge add angle
			for (int i = 0; i < list.size(); i++){
				int[] edge = list.get(i);
				sum += GeometryFunctions.getAngle(mesh.getVertex(ring.get(edge[0]).getInt()), 
												  mesh.getVertex(ring.get(edge[1]).getInt()), 
												  mesh.getVertex(ring.get(getNode(edge[1] - 1)).getInt()), 
												  mesh.getVertex(ring.get(getNode(edge[1] + 1)).getInt()));
				}
			
			return sum;
		}
		
	}
	
	/********************************************
	 * Orients all faces with each other, starting with the first face
	 * @param mesh
	 */
	public static void correctOrientation(Mesh3D mesh){
		correctOrientation(mesh, false);
	}
	
	/********************************************
	 * Orients all faces with each other (starting with face 0)
	 * @param mesh
	 * @param seed
	 * @param reverse
	 */
	public static void correctOrientation(Mesh3D mesh, boolean reverse){
		
		MeshEdgeSet edge_set = new MeshEdgeSet(mesh);
		boolean[] tagged = new boolean[mesh.f];
		long r = 0;
		
		for (int i = 0; i <  edge_set.triangles.size(); i++)
			if (!tagged[i]){
				MeshTriangle tri = edge_set.triangles.get(i);
				//if reversed, flip this triangle
				if (reverse)
					mesh.flipFace(tri.faceIndex);
				tagged[tri.faceIndex] = true;
				for (int j = 0; j < tri.edges.length; j++)
					if (tri.edges[j] != null)
						evalNextFace(edge_set, tagged, tri.edges[j], tri, mesh, r);
				}
		
	}
	
	//recursively reorient all contiguous faces
	static void evalNextFace(MeshEdgeSet edge_set, 
							 boolean[] tagged, 
							 MeshEdge edge, 
							 MeshTriangle tri,
							 Mesh3D mesh,
							 long r){
		
		r++;
		MeshTriangle nextTri = edge_set.getOppositeTri(edge, tri);
		if (nextTri == null || tagged[nextTri.faceIndex]) return;
		
		tagged[nextTri.faceIndex] = true;
		
		//flip face if necessary
		if (needsToFlip(tri, nextTri, edge, mesh))
			mesh.flipFace(nextTri.faceIndex);
		
		//keep going till no more non-tagged tris in this contiguous mesh
		for (int i = 0; i < nextTri.edges.length; i++)
			if (nextTri.edges[i] != null)
				try{
					evalNextFace(edge_set, tagged, nextTri.edges[i], nextTri, mesh, r);
				}catch (VirtualMachineError e){
					InterfaceSession.log("VM error (" + e.getClass().getName() + ") for recursion #: " + r);
					System.exit(0);
				}
		
	}
	
	static boolean needsToFlip(MeshTriangle tri1, MeshTriangle tri2, MeshEdge edge, Mesh3D mesh){
		Mesh3D.MeshFace3D face1 = mesh.getFace(tri1.faceIndex);
		Mesh3D.MeshFace3D face2 = mesh.getFace(tri2.faceIndex);
		
		boolean flipped1 = false;
		for (int i = 0; i < 3 && !flipped1; i++){
			int j = i + 1;
			if (j == 3) j = 0;
			if (face1.getNode(i) == edge.node2)
				if (face1.getNode(j) == edge.node1)
					flipped1 = true;
			}
		
		boolean flipped2 = false;
		for (int i = 0; i < 3 && !flipped2; i++){
			int j = i + 1;
			if (j == 3) j = 0;
			if (face2.getNode(i) == edge.node2)
				if (face2.getNode(j) == edge.node1)
					flipped2 = true;
			}
		
		return flipped1 == flipped2;
	}
	
	public static Vector3f[] flipNormals(Vector3f[] normals){
		Vector3f[] flipped = new Vector3f[normals.length];
		for (int i = 0; i < normals.length; i++)
			flipped[i] = GeometryFunctions.flipVector(normals[i]);
		return flipped;
	}
	
	/********************************
	 * Condenses a mesh by removing all coincident nodes.
	 * 
	 * @param mesh mesh to condense
	 * @return condensed mesh
	 */
	public static Mesh3D condenseMesh(Mesh3D mesh){
		Mesh3D newmesh = new Mesh3D();
		
		ArrayList<IndexedPoint3f> nodes = new ArrayList<IndexedPoint3f>(mesh.n);
		
		for (int i = 0; i < mesh.n; i++)
			nodes.add(new IndexedPoint3f(mesh.getVertex(i), i));
		
		//sort by x
		Collections.sort(nodes, new Comparator(){
			public int compare(Object o1, Object o2){
				if (((IndexedPoint3f)o1).pt.x == ((IndexedPoint3f)o2).pt.x) return 0;
				if (((IndexedPoint3f)o1).pt.x > ((IndexedPoint3f)o2).pt.x) return 1;
				return -1;
				}
			});
		
		Point3f pt = new Point3f();
		int j, k = 0;
		for (int i = 0; i < nodes.size(); i++){
			pt.set(nodes.get(i).pt);
			nodes.get(i).index2 = k;
			newmesh.addVertex(new Point3f(pt));
			j = i + 1;
			//skip any coincident nodes
			while (j < nodes.size() && GeometryFunctions.isCoincident(pt, nodes.get(j).pt)){
				nodes.get(j).index2 = k;
				j++;
				}
			i = j - 1;
			k++;
			}
		
		//sort by original order
		Collections.sort(nodes, new Comparator(){
			public int compare(Object o1, Object o2){
				if (((IndexedPoint3f)o1).index1 == ((IndexedPoint3f)o2).index1) return 0;
				if (((IndexedPoint3f)o1).index1 > ((IndexedPoint3f)o2).index1) return 1;
				return -1;
				}
			});
		
		//add faces, updating indices to fit new node array
		for (int i = 0; i < mesh.f; i++){
			Mesh3D.MeshFace3D face = mesh.getFace(i);
			face.A = nodes.get(face.A).index2;
			face.B = nodes.get(face.B).index2;
			face.C = nodes.get(face.C).index2;
			newmesh.addFace(face);
			}
		
		return newmesh;
	}
	
	protected static class IndexedPoint3f{
		public Point3f pt;
		public int index1;
		public int index2;
		
		public IndexedPoint3f(Point3f p, int index){
			pt = p;
			index1 = index;
		}
	}
	
	/**************************
	 * Computes the curvature of a mesh at each of its nodes, as the magnitude
	 * of the sum of surface normals of the triangles of which it is a node.
	 * Adds this information to <code>mesh3D</code> as a variable named <code>name</code>.
	 * 
	 * @param mesh3D Mesh for which to compute curvature
	 * @param name Name of resulting variable
	 */
	public static boolean computeVertexWiseCurvature(final Mesh3DInt mesh3D, String target_column, final ProgressUpdater progress){
		
		ArrayList<MguiNumber> values = null;
		if (progress == null){
			values = getVertexWiseCurvature(mesh3D.getMesh(), progress);
		}else{
			values = (ArrayList<MguiNumber>)Worker.post(new Job(){
				public ArrayList<MguiNumber> run(){
					return getVertexWiseCurvature(mesh3D.getMesh(), progress);
				}
			});
			}
		
		if (values == null) return false;
		if (mesh3D.hasColumn(target_column)){
			mesh3D.setVertexData(target_column, values);
		}else{
			mesh3D.addVertexData(target_column, values);
			}
		return true;
	}
	
	/*****************************
	 * Computes the mean curvature at each node in the mesh. See:
	 * 
	 * <p>Tosun D, Rettman ME, Prince JL (2004). Mapping techniques for aligning
	 * sulci across multiple brains. <i>Medical Image Analysis</i> 8:295-309.
	 * 
	 * <p>Meyer M, Desbrun M, Schroeder P, Barr AH (2002). Discrete differential-
	 * geometry operators for triangulated 2-manifolds.
	 * http://citeseer.ist.psu.edu/meyer02discrete.html
	 * 
	 * @param mesh
	 * @return
	 */
	public static ArrayList<MguiNumber> getVertexWiseCurvature_bak(Mesh3D mesh, ProgressUpdater progress){
	
		if (progress != null){
			progress.register();
			progress.setMinimum(0);
			progress.setMaximum(mesh.n * 2);
			}
		
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		MeshEdgeSet edge_set = new MeshEdgeSet(mesh);
		
		// Our strategy:
		// 1. Compute A_mixed at each vertex
		// For each vertex i:
		for (int i = 0; i < mesh.n; i++){
			// 	Get its associated faces (1-ring neighbourhood N_1)
			int[] nbrs = n_mesh.getNeighbourhood(i).getNeighbourList();
			//	Compute Voronoi area A_voronoi from each face in N_1:
			double A_voronoi = 0;
			
			//	For each neighbour j:
			for (int j = 0; j < nbrs.length; j++){
				//	Use edge e_ij and its opposite angles alpha_ij, beta_ij
				//	to compute A_voronoi
				int e_idx = edge_set.getEdgeIndex(i, j);
				MeshEdge edge = edge_set.getEdge(e_idx);
				double alpha_ij = mesh.getFaceAngle(edge_set.getFaceIndex(edge.tri1),
													edge_set.getOppositeNode(e_idx, edge.tri2));
				double beta_ij = mesh.getFaceAngle(edge_set.getFaceIndex(edge.tri2),
												   edge_set.getOppositeNode(e_idx, edge.tri1));
				
				Point3f v1 = mesh.getVertex(edge.node1);
				double d_ij = v1.distance(mesh.getVertex(edge.node2));
				A_voronoi += (1.0/Math.tan(alpha_ij) + 1.0/Math.tan(beta_ij)) * d_ij * d_ij;
				}
			
			A_voronoi /= 8;
			
			//	Compute A_mixed for vertex i:
			//		For each face T in N_1:
			//			If non-obtuse:
			//				Add A_voronoi to A_mixed
			// 
			//			Else:
			//				If angle at i is obtuse:
			//					Add area(T)/2
			//				Else:
			//					Add area(T)/4
			}
		// 2. Compute normal K and mean curvature kappa_h at each vertex
		// For each vertex i:
		// 		Get its associated faces (1-ring neighbourhood N_1)
		//		Get normal vector K(i):
		// 			For each neighbour j:
		//				Scale edge vector by alpha_ij and beta_ij and add to K
		//			Scale K by 1/2 * A_mixed
		//		Get mean curvature kappa_h from K:
		//			kappa_h(i) = 1/2 * |K(i)|
		
		
		
		return null;
	}
	
	/*****************************
	 * Computes the mean curvature at each node in the mesh. See:
	 * 
	 * <p>Tosun D, Rettman ME, Prince JL (2004). Mapping techniques for aligning
	 * sulci across multiple brains. <i>Medical Image Analysis</i> 8:295-309.
	 * 
	 * <p>Meyer M, Desbrun M, Schroeder P, Barr AH (2002). Discrete differential-
	 * geometry operators for triangulated 2-manifolds.
	 * http://citeseer.ist.psu.edu/meyer02discrete.html
	 * 
	 * @param mesh
	 * @return
	 */
	public static ArrayList<MguiNumber> getVertexWiseCurvature(Mesh3D mesh, ProgressUpdater progress){
		
		double[] areas = new double[mesh.n];
		//float A_mixed = 0;
		double[][] cotans = new double[mesh.f][3];
		double[] curvature = new double[mesh.n];
		double debug_max = -Double.MAX_VALUE;
		double debug_min = Double.MAX_VALUE;
		
		if (progress != null){
			progress.register();
			progress.setMinimum(0);
			progress.setMaximum(mesh.f);
			}
		
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		MeshEdgeSet edge_set = new MeshEdgeSet(mesh);
		
		//for each triangle, find area, add to its nodes
		for (int i = 0; i < mesh.f; i++){
			//for each triangle, for each node
			Mesh3D.MeshFace3D face = mesh.getFace(i);
			Triangle3D tri = mesh.getFaceTriangle(i);
			float[] angles = tri.getAngles();
			float[] lengths = tri.getEdgeLengths();
			
			//get cotans
			for (int j = 0; j < 3; j++){
				cotans[i][j] = 1.0 / Math.tan(angles[j]);
				if (lengths[j] > debug_max || lengths[j] == Float.NaN){
					debug_max = lengths[j];
					//InterfaceSession.log("lengths: " + debug_max + ", " + debug_min, LoggingType.Debug);
					}
				if (lengths[j] < debug_min){
					debug_min = lengths[j];
					//InterfaceSession.log("lengths: " + debug_max + ", " + debug_min, LoggingType.Debug);
					}
				}
			
			
			//if not obtuse, add voronoi area:
			//1/8 * len(AB)^2 * cot(C) +(AC)^2 * cot(B)
			if (!(angles[0] > Math.PI || 
				  angles[1] > Math.PI || 
				  angles[2] > Math.PI)){
				//node A
				//areas[face.A] += Math.pow(lengths[0], 2) * cotans[i][2]
				areas[face.A] += (Math.pow(lengths[0], 2) * cotans[i][2]                                                     
				               + Math.pow(lengths[2], 2) * cotans[i][1])
				               / 8.0;
				//node B
				areas[face.B] += (Math.pow(lengths[0], 2) * cotans[i][2]
				               + Math.pow(lengths[1], 2) * cotans[i][0])
				               / 8.0;
				//node C
				areas[face.C] += (Math.pow(lengths[2], 2) * cotans[i][1]
				               + Math.pow(lengths[1], 2) * cotans[i][0])
				               / 8.0;
			}else{
			//otherwise
				//if angle at node is obtuse, area(T)/2
				//otherwise, area(T)/4
				double T_area = GeometryFunctions.getArea(tri);
				
				if (angles[0] > Math.PI)
					areas[face.A] += T_area / 2.0;
				else
					areas[face.A] += T_area / 4.0;
				if (angles[1] > Math.PI)
					areas[face.B] += T_area / 2.0;
				else
					areas[face.B] += T_area / 4.0;
				if (angles[2] > Math.PI)
					areas[face.C] += T_area / 2.0;
				else
					areas[face.C] += T_area / 4.0;
				}
			}
		
			// Now, for each neighbour, use computed areas to estimate normal vector
			// K(i) and mean curvature kappa_h(i)
			Vector3f v_i = new Vector3f();
			Vector3f v_j = new Vector3f();
			Vector3f K = new Vector3f();
			for (int i = 0; i < mesh.n; i++){
				int[] nbrs = n_mesh.getNeighbourhood(i).getNeighbourList();
				K.set(0,0,0);
				for (int j = 0; j < nbrs.length; j++){
					v_i.set(mesh.getVertex(i));
					v_j.set(mesh.getVertex(nbrs[j])); 
					v_i.sub(v_j);
					
					// Do some trickery to find the right indices
					int e_idx = edge_set.getEdgeIndex(i, nbrs[j]);
					MeshEdge edge = edge_set.getEdge(e_idx);
					int v_idx1 = edge_set.getOppositeNode(e_idx, edge.tri2);
					int v_idx2 = edge_set.getOppositeNode(e_idx, edge.tri1);
					int f_idx1 = edge_set.getOppositeFaceIndex(e_idx, edge.tri2);
					int f_idx2 = edge_set.getOppositeFaceIndex(e_idx, edge.tri1);
					v_idx1 = mesh.getFace(f_idx1).whichNode(v_idx1);
					v_idx2 = mesh.getFace(f_idx2).whichNode(v_idx2);
					
					v_i.scale((float)(cotans[f_idx1][v_idx1] + cotans[f_idx2][v_idx2]));
					
					K.add(v_i);
					}
				
				// Scale by 1/(4*A_mixed)
				double scale = 1.0 / (areas[i] * 4.0);
				K.scale((float)scale);
				curvature[i] = K.length();
				
				if (progress != null){
					if (progress.isCancelled()){
						InterfaceSession.log("Mean curvature operation cancelled by user.", LoggingType.Warnings);
						progress.deregister();
						return null;
						}
					progress.iterate();
					}
				}
		
		ArrayList<MguiNumber> values = new ArrayList<MguiNumber>(mesh.n);
		
		//compute mean curvature at each node:
		for (int i = 0; i < mesh.n; i++)
			values.add(new MguiDouble(0.5 * curvature[i] / areas[i]));
		
		if (progress != null){
			progress.deregister();
			}
		
		return values;
	}
	
	static float getLength(Point3f p1, Point3f p2){
		Vector3f v = new Vector3f(p2);
		v.sub(p1);
		return v.length();
	}
	
	/*
	static class Neighbourhood{
		public TreeSet<Mesh3D.MeshFace3D> list = new TreeSet<Mesh3D.MeshFace3D>();
		
		public boolean add(Mesh3D.MeshFace3D face){
			return list.add(face);
		}
		
		
		
	}
	*/
	
	public static ArrayList<MguiFloat> getVertexWiseCurvatureBak(Mesh3D mesh){
		//generate normals
		GeometryInfo gi = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);
		mesh.finalize();
        gi.setCoordinates(mesh.nodes);
        gi.setCoordinateIndices(mesh.faces);
        //no double normals
        NormalGenerator ng = new NormalGenerator(Math.PI);
        //ng.normalize = false; //hack by me to get actual length of vectors
        ng.generateNormals(gi);
        Vector3f[] normals = gi.getNormals();
        int[] indices = gi.getNormalIndices();
        
        ArrayList<MguiFloat> magnitudes = new ArrayList<MguiFloat>(mesh.n);
        for (int i = 0; i < mesh.n; i++)
        	magnitudes.add(new MguiFloat(0));
        
        for (int i = 0; i < indices.length; i++)
        	magnitudes.set(indices[i], new MguiFloat(normals[indices[i]].x));
        
        return magnitudes;
	}
	
	public static float getMeanCurvature(Mesh3D mesh){
		//generate normals
		GeometryInfo gi = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);
        gi.setCoordinates(mesh.nodes);
        gi.setCoordinateIndices(mesh.faces);
        NormalGenerator ng = new NormalGenerator();
        ng.generateNormals(gi);
        Vector3f[] normals = gi.getNormals();
        
       float mean = 0;
       
       for (int i = 0; i < mesh.n; i++)
    	   mean += normals[i].length();
       
       return mean / mesh.n;
	}
	
	public static void smoothLR(Mesh3D mesh){
		MeshSmoothing.setEquilateralFacesLR(mesh, 0.1, 2, 1, 0.1, true);
	}
	
	public static void smoothEM(Mesh3DInt mesh){
		MeshSmoothing.setEquilateralFacesEM(mesh, 0.1, 2, 0.9, true, true);
	}
	
	/***********************************************
	 * Smooths {@code values} with an isotropic Gaussian kernel, along the surface of {@code mesh}.
	 * 
	 * @param mesh
	 * @param values
	 * @param sigma
	 * @param sigma_max
	 * @param progress
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<MguiNumber> smoothVertexValuesIsotropicGaussian(final Mesh3D mesh, 
																			final ArrayList<MguiNumber> values,
																			final double sigma,
																			final double sigma_max,
																			final ProgressUpdater progress){
		if (progress == null)
			return smoothVertexValuesIsotropicGaussianBlocking(mesh, 
																values,
																sigma,
																sigma_max,
																null);
		
		return (ArrayList<MguiNumber>)Worker.post(new Job(){
			
			public ArrayList<MguiNumber> run(){
				return smoothVertexValuesIsotropicGaussianBlocking(mesh, 
																	values,
																	sigma,
																	sigma_max,
																	progress);
			}
			
		});
		
	}
	
	/***********************************************
	 * Smooths {@code values} with an isotropic Gaussian kernel, along the surface of {@code mesh}.
	 * 
	 * @param mesh
	 * @param values
	 * @param sigma
	 * @param sigma_max
	 * @param progress
	 * @return
	 */
	static ArrayList<MguiNumber> smoothVertexValuesIsotropicGaussianBlocking(Mesh3D mesh, 
																			ArrayList<MguiNumber> values,
																			double sigma,
																			double sigma_max,
																			ProgressUpdater progress){
		
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		ArrayList<MguiNumber> smoothed = new ArrayList<MguiNumber>(mesh.n);
		if (progress != null){
			progress.setMinimum(0);
			progress.setMaximum(mesh.n);
			progress.update(0);
		}
		
		for (int i = 0; i < mesh.n; i++){
			
			HashMap<Integer,Double> nbrs = getNeighbours(i, n_mesh, mesh, sigma, sigma_max);
			
			// Sample from all the neighbours
			ArrayList<Integer> nbr_idx = new ArrayList<Integer>(nbrs.keySet());
			double new_value = 0;
			double denom = 0;
			for (int j = 0; j < nbr_idx.size(); j++){
				int _j = nbr_idx.get(j);
				double dist = nbrs.get(_j);
				double w = StatFunctions.getGaussian2(dist, 0, sigma);
				new_value += values.get(_j).getValue() * w;
				denom += w;
				}
			
			if (denom > 0)
				new_value /= denom;
			else
				new_value = 0;
			
			MguiNumber num = (MguiNumber)values.get(i).clone();
			num.setValue(new_value);
			smoothed.add(num);
			
			if (progress != null)
				progress.update(i + 1);
			
			}
		
		return smoothed;
	}
	
	/*******************************************
	 * Finds all the neighbourhood of vertices such that all neigbours are within {@code sigma_max} of
	 * {@code i}, along connecting edges. Returns a map of the indices to the distances. 
	 * 
	 * @param i
	 * @param n_mesh
	 * @param mesh
	 * @param sigma
	 * @param sigma_max
	 * @return
	 */
//	public static HashMap<Integer,Double> getNeighbours(int i, NeighbourhoodMesh n_mesh, Mesh3D mesh, double sigma, double sigma_max){
//		return getNeighbours(i, n_mesh, mesh, sigma, sigma_max, new HashMap<Integer,Double>(), new boolean[mesh.n]);
//	}
	
	/*******************************************
	 * Finds all the neighbourhood of vertices such that all neigbours are within {@code sigma_max} of
	 * {@code i}, along connecting edges. Returns a map of the indices to the distances. 
	 * 
	 * @param i
	 * @param n_mesh
	 * @param mesh
	 * @param sigma
	 * @param sigma_max
	 * @return
	 */
	static HashMap<Integer,Double> getNeighbours(int i, NeighbourhoodMesh n_mesh, Mesh3D mesh, double sigma, double sigma_max){
		
		HashMap<Integer,Double> current = new HashMap<Integer,Double>();
		current.put(i, 0.0);
		int[] nbrs = n_mesh.getNeighbourhood(i).getNeighbourList();
		ArrayDeque<Integer> to_process = new ArrayDeque<Integer>();
		
		// First degree distances
		for (int j = 0; j < nbrs.length; j++){
			int _j = nbrs[j];
			to_process.add(_j);
			double dist = mesh.getVertex(i).distance(mesh.getVertex(_j));
			current.put(_j, dist);
			}
		
		// Nth degree distances
		while (!to_process.isEmpty()){
			int j = to_process.poll();
			double dist_to_j = current.get(j);
			nbrs = n_mesh.getNeighbourhood(j).getNeighbourList();
			for (int k = 0; k < nbrs.length; k++){
				int _k = nbrs[k];
				double dist = mesh.getVertex(j).distance(mesh.getVertex(_k)) + dist_to_j;
				// Must be within range, otherwise we're done
				if (dist / sigma < sigma_max){
					if (!current.containsKey(_k)){
						// Hasn't been processed yet, add to queue
						current.put(_k, dist);
						to_process.addLast(_k);
					}else{
						double dist_to_k = current.get(_k);
						if (dist < dist_to_k){
							// This route is shorter, update distance
							current.put(_k, dist);
							}
						}
					}
				}
			}
		
		return current;
	}
	
	static MguiNumber getSmoothedValueAt(int i, double sigma, double sigma_max, NeighbourhoodMesh n_mesh, Mesh3D mesh, ArrayList<MguiNumber> values){
		MguiDouble total = new MguiDouble(0);
		MguiDouble denom = new MguiDouble(0);
		getSmoothedValueAt(i, mesh.getVertex(i), sigma, sigma_max, n_mesh, mesh, values, new boolean[mesh.n], total, denom);
		return total.divide(denom);
	}
	
	static boolean getSmoothedValueAt(int i, Point3f ref_pt, double sigma, double sigma_max,
									 NeighbourhoodMesh n_mesh, Mesh3D mesh, ArrayList<MguiNumber> values, 
									 boolean[] processed, MguiDouble total, MguiDouble denom){
		
		processed[i] = true;
		double this_val = values.get(i).getValue();
		Point3f p = mesh.getVertex(i);
		double disp = (double)p.distance(ref_pt);
		if (disp / sigma > sigma_max)
			return false; 	// Done with this branch
		double wt = StatFunctions.getGaussian2(disp, 0, sigma);
		total.add(wt * this_val);
		denom.add(wt);
		
		int[] nbrs = n_mesh.getNeighbourhood(i).getNeighbourList();
		
		for (int j = 0; j < nbrs.length; j++){
			if (!processed[nbrs[j]])
				getSmoothedValueAt(j, ref_pt, sigma, sigma_max, n_mesh, mesh, values, processed, total, denom);
			}
		
		return true;
		
	}
	
	/****************************************************
	 * Returns a list of edge lengths for the given set of edges
	 * 
	 * @param mesh
	 * @return
	 */
	public static ArrayList<MguiDouble> getEdgeLengths(Mesh3D mesh, ArrayList<MeshEdge> edges){
		
		ArrayList<MguiDouble> lengths = new ArrayList<MguiDouble>(edges.size());
		
		for (int i = 0; i < edges.size(); i++){
			MeshEdge edge = edges.get(i);
			lengths.add(new MguiDouble(mesh.getVertex(edge.node1).distance(mesh.getVertex(edge.node2))));
			}
		
		return lengths;
		
	}
	
	/************************************
	 * Returns the surface area of the given mesh
	 * 
	 * @param mesh
	 * @return
	 */
	public static double getArea(Mesh3D mesh){
		return getVolumeAndArea(mesh, null, null, 0)[0];
	}
	
	/***********************
	 * Given a mesh and a set of thickness values, returns an area and a volume calculation (in that order).
	 * Simply sums triangle areas for area, and multiplies area by the average thickness for volume.
	 * 
	 * @param mesh
	 * @param thickness
	 * @param cutoff value below which the area and volume are not counted.
	 * @return double array with area and volume, in that order
	 */
	public static double[] getVolumeAndArea(Mesh3D mesh, 
										    ArrayList<MguiNumber> thickness, 
										    ArrayList<MguiNumber> filter, 
										    double cutoff){
		//for each face:
		//compute area
		//get average thickness
		//compute volume
		
		double[] results = new double[]{0,0};
		//int count = 0;
		
		for (int i = 0; i < mesh.f; i++){
			Triangle3D tri = mesh.getFaceTriangle(i);
			Mesh3D.MeshFace3D face = mesh.getFace(i);
			
			float num = 3f;
			if (filter != null){
				if (filter.get(face.A).getValue() < cutoff) num--;
				if (filter.get(face.B).getValue() < cutoff) num--;
				if (filter.get(face.C).getValue() < cutoff) num--;
				}
			
			if (num > 0){
				double thisAvr = -1;
				
				double thisArea = GeometryFunctions.getArea(tri);
				if (thickness != null){
					thisAvr = (thickness.get(face.A).getValue() + 
					 	       thickness.get(face.B).getValue() +
						       thickness.get(face.C).getValue()) / 3;
					}
				
				//take fraction of area relative to number of nodes that
				//meet the cutoff
				thisArea *= num / 3f;
				
				results[0] += thisArea;
				if (thickness != null)
					results[1] += thisArea * thisAvr;
				//count++;
				}
			}
		
		//InterfaceSession.log("Surface and area calculation: " + count + " faces passed filter.");
		return results;
	}
	
	/****************************************
	 * Returns a list of points corresponding to the immediate (1st level) neighbourhood of node <code>node</code>.  
	 * 
	 * @param mesh
	 * @param neighbourhoods
	 * @param node
	 * @return
	 */
	public static ArrayList<Point3f> getNeighbourhoodPoints(Mesh3D mesh, NeighbourhoodMesh neighbourhoods, int node){
		TreeSet<Point3f> points = new TreeSet<Point3f>();
		getNeighbourhoodPoints(points, mesh, neighbourhoods, node, 1);
		return new ArrayList<Point3f>(points);
	}
	
	/****************************************
	 * Returns a list of points corresponding to the n-th level neighbourhood of node <code>node</code>, where
	 * n is specified by the parameter <code>levels</code>  
	 * 
	 * @param mesh
	 * @param neighbourhoods
	 * @param node
	 * @return
	 */
	public static ArrayList<Point3f> getNeighbourhoodPoints(Mesh3D mesh, NeighbourhoodMesh neighbourhoods, int node, int levels){
		TreeSet<Point3f> points = new TreeSet<Point3f>();
		getNeighbourhoodPoints(points, mesh, neighbourhoods, node, levels);
		return new ArrayList<Point3f>(points);
	}
	
	protected static void getNeighbourhoodPoints(TreeSet<Point3f> points, Mesh3D mesh, NeighbourhoodMesh neighbourhoods, int node, int levels){

		Iterator<Integer> itr = neighbourhoods.getNeighbourhood(node).neighbours.iterator();
		
		while (itr.hasNext()){
			int n = itr.next();
			//add further points recursively
			//set ensures that we don't run into cycles
			//(add will return false if point is already in list)
			if (points.add(mesh.getVertex(n)) && levels > 1)
				getNeighbourhoodPoints(points, mesh, neighbourhoods, n, levels - 1);
			}
	
	}
	
	/**************************
	 * Removes all nodes specified in <code>nodes</code> from <code>mesh</code>, and retriangulates
	 * the remaining nodes.
	 * 
	 * @param mesh
	 * @param vertices
	 */
	public static void removeNodes(Mesh3D mesh, boolean[] removed){
		removeNodes(mesh, removed, new NeighbourhoodMesh(mesh));
	}
	
	/**************************
	 * Removes all nodes specified in <code>nodes</code> from <code>mesh</code>, and retriangulates
	 * the remaining nodes.
	 * 
	 * @param mesh
	 * @param vertices
	 * @param nmesh Neighbourhood mesh (in case one is already made)
	 */
	public static void removeNodes(Mesh3D mesh, boolean[] removed, NeighbourhoodMesh nmesh){
		//Strategy:
		//1. start new mesh
		Mesh3D new_mesh = new Mesh3D();
		int[] added = new int[mesh.n];
		
		//2. for each node in old mesh,
		int count = 0;
		for (int i = 0; i < mesh.n; i++){
			//   a. if !removed(node), add it to new mesh
			//   b. add its new index to list
			if (!removed[i]){
				new_mesh.addVertex(mesh.getVertex(i));
				added[i] = count;
				count++;
				}
			}
		
		//3. for each face in old mesh,
		for (int i = 0; i < mesh.f; i++){
			//   a. if all of its nodes satisfy !removed(node),
			//	    add it to new mesh
			Mesh3D.MeshFace3D face = mesh.getFace(i);
			if (!removed[face.A] &&
				!removed[face.B] &&
				!removed[face.C])
				new_mesh.addFace(added[face.A], 
								 added[face.B], 
								 added[face.C]);
			}
		
		//..now we have a mesh with lots of holes, so we fill them in
		boolean[] processed = new boolean[mesh.n];
		//3. for each node in old mesh,
		for (int i = 0; i < mesh.n; i++){
			//   a. if removed(node) and !processed(node),
			if (removed[i] && !processed[i]){
				//   b. compile list of neighbours (only those which satisfy !removed(node))
				//      add all neighbours of removed neighbours as well, and mark them as
				//		processed so they won't be triangulated more than once
				ArrayList<MguiInteger> neighbours = getAllNeighbours(i, nmesh, removed, added, processed);
				
				//	 c. order neighbours 
				//orderNeighbours(neighbours, nmesh);
				
				//	 d. triangulate resulting polygon
				if (neighbours.size() > 2)
					triangulate(new_mesh, neighbours);
				}
				
			}
		
		
	}
	
	/********************
	 * Searches for all neighbours of <code>n</code> in <code>nmesh</code>. If a neighbour is 
	 * removed and not yet processed, its neighbours will also be added to this list recursively, 
	 * and its index in the <code>processed</code> array will be set to true (to prevent its 
	 * being reprocessed). This continues until all <code>n</code>'s neighbours have been processed.
	 */ 
	protected static ArrayList<MguiInteger> getAllNeighbours(int n, NeighbourhoodMesh nmesh, 
														   boolean[] removed, int[] added,
														   boolean[] processed){
		
		ArrayList<MguiInteger> neighbours = new ArrayList<MguiInteger>();
		
		Neighbourhood neighbourhood = nmesh.getNeighbourhood(n);
		Iterator<Integer> itr = neighbourhood.neighbours.iterator();
		
		while (itr.hasNext()){
			int i = itr.next();
			if (!removed[i])
				neighbours.add(new MguiInteger(added[i]));
			else{
				neighbours.addAll(getAllNeighbours(i, nmesh, removed, added, processed));
				processed[i] = true;
				}
			}
		
		return neighbours;
	}
	
	protected void orderNeighbours(ArrayList<MguiInteger> list, NeighbourhoodMesh nmesh){
		//we can construct linked lists from neighbours and combine them when
		//appropriate
		
		
		
	}
	
	/************************
	 * Triangulates the given neighbourhood by adding faces using recursive loop splitting
	 * @param mesh
	 * @param n
	 */
	public static void triangulate(Mesh3D mesh, ArrayList<MguiInteger> neighbours){
		//i. if neighbours is of size 3, add a triangle and return
		if (neighbours.size() == 3){
			mesh.addFace(neighbours.get(0).getInt(), 
						 neighbours.get(1).getInt(), 
						 neighbours.get(2).getInt());
			return;
		}
		
		//1. get actual points
		ArrayList<Point3f> points = new ArrayList<Point3f>(neighbours.size());
		for (int i = 0; i < points.size(); i++)
			points.add(mesh.getVertex(neighbours.get(i).getInt()));
		
		//2. get average plane
		Plane3D plane = GeometryFunctions.getOrthogonalRegressionPlane(points);
		
		//3. get best split plane
		
		
		//4. 
		
		//4. partition points
		ArrayList<MguiInteger> P1 = new ArrayList<MguiInteger>();
		//ArrayList<arInteger> P2 = 
		
		
	}
	
	public static ArrayList<MguiNumber> mapVolumeToMeshGaussian(Mesh3D mesh,
															  Volume3DInt volume,
															  String channel,
															  double sigma_normal,
															  double sigma_tangent,
															  double sigma_max_normal,
															  double sigma_max_tangent,
															  String setSigmaT,
															  boolean normalize){
		
		return MeshFunctions.mapVolumeToMeshGaussian(mesh, volume, channel, 
													 sigma_normal, sigma_tangent, 
													 sigma_max_normal, sigma_max_tangent, 
													 setSigmaT, 
													 normalize, 
													 null,
													 false,
													 0,
													 0,
													 null);
		
	}
	
	
	/****************************************************
	 * Maps values from a <code>Grid3D</code> object to a mesh object by applying a Gaussian 
	 * kernal to voxels in the vicinity of each mesh vertex. The Gaussian is defined in the 
	 * direction normal to the vertex, by <code>sigma_normal</code> (one standard deviation) and 
	 * <code>sigma_max_normal</code> (the distance, in standard deviations, at which to stop 
	 * considering voxels). It is defined in the plane tangent to the normal depending on the 
	 * value of <code>setSigmaT</code>:
	 * <p>
	 * <ul>
	 * <li>"Parameter": <code>sigma_tangent</code> is set to the passed parameter
	 * <li>"From mean area": for each vertex, <code>sigma_tangent</code> is set to sqrt(A_tri_mean 
	 * / Pi)
	 * <li>"From mean length": for each vertex, <code>sigma_tangent</code> is set to the mean length of all edges
	 * connecting the vertex
	 *  </ul>
	 *  The values returned will be those obtained by the <code>Grid3D.getDoubleValue</code> method, but can
	 *  alternatively be normalized by setting the <code>normalized</code> flag.
	 * 
	 * 
	 * @param mesh 					Mesh on which to project values 
	 * @param grid 					Input grid
	 * @param sigma_normal 			Sigma defining the Gaussian in the normal direction
	 * @param sigma_tangent 		Sigma defining the Gaussian in the tangent direction
	 * @param sigma_max_normal 		Maximum sigma at which to obtain values in the normal direction
	 * @param sigma_max_tangent 	Maximum sigma at which to obtain values in the tangent direction
	 * @param setSigmaT 			How to determine the tangent sigma; one of "parameter", "area", or... 
	 * @param normalize 			Whether to normalize the resulting values
	 * @param normal_set_max_sigma  From all values within this proximity along the normal, 
	 * 							    set value to the maximum rather than the weighted
	 * 							    average; set to 0 to avoid this behaviour (default)
	 * @param tangent_set_max_sigma From all values within this proximity along the tangent, 
	 * 								set value to the maximum rather than the weighted
	 * 							    average; set to 0 to avoid this behaviour (default)
	 * @return Vertex-wise mapped values, or <code>null</code> if process fails or was cancelled. 
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<MguiNumber> mapVolumeToMeshGaussian(final Mesh3D mesh,
															  final Volume3DInt volume,
															  final String channel,
															  final double sigma_normal,
															  final double sigma_tangent,
															  final double sigma_max_normal,
															  final double sigma_max_tangent,
															  final String setSigmaT,
															  final boolean normalize,
															  final ProgressUpdater progress,
															  final boolean output_matrix,
															  final double normal_set_max_sigma,
															  final double tangent_set_max_sigma,
															  final String matrix_file){
		
		if (progress == null || !(progress instanceof InterfaceProgressBar)) 
			return MeshFunctions.mapVolumeToMeshGaussianBlocking(mesh, 
																 volume, 
																 channel, 
																 sigma_normal, 
																 sigma_tangent, 
																 sigma_max_normal, 
																 sigma_max_tangent, 
																 setSigmaT, 
																 normalize, 
																 progress, 
																 output_matrix,
																 normal_set_max_sigma,
																 tangent_set_max_sigma,
																 matrix_file);
		
		return (ArrayList<MguiNumber>)Worker.post(new Job(){
				@Override
				public ArrayList<MguiNumber> run(){
					return MeshFunctions.mapVolumeToMeshGaussianBlocking(mesh, 
																		 volume, 
																		 channel, 
																		 sigma_normal, 
																		 sigma_tangent, 
																		 sigma_max_normal, 
																		 sigma_max_tangent, 
																		 setSigmaT, 
																		 normalize, 
																		 progress, 
																		 output_matrix,
																		 normal_set_max_sigma,
																		 tangent_set_max_sigma,
																		 matrix_file);
				}
			});
		
	}
	
	static ArrayList<MguiNumber> mapVolumeToMeshGaussianBlocking(final Mesh3D mesh,
															   final Volume3DInt volume,
															   final String channel,
															   final double sigma_normal,
															   final double sigma_tangent,
															   final double sigma_max_normal,
															   final double sigma_max_tangent,
															   final String setSigmaT,
															   final boolean normalize,
															   final ProgressUpdater progress,
															   final boolean output_matrix,
															   final double normal_set_max_sigma,
															   final double tangent_set_max_sigma,
															   final String matrix_file){
		
		if (!volume.hasColumn(channel)){
			InterfaceSession.log("MeshFunctions.mapVolumeToMesgGaussianBlocking: Volume has no column '" + channel +".", 
								 LoggingType.Errors);
			return null;
			}
		
		Grid3D grid = volume.getGrid();
		
		NeighbourhoodMesh n_mesh = null;
		ArrayList<MguiNumber> values = new ArrayList<MguiNumber>();
		for (int i = 0; i < mesh.n; i++)
			values.add(new MguiDouble(0));
		
		//list of normals
		ArrayList<Vector3f> normals = mesh.getNormals();
		
		if (!setSigmaT.toLowerCase().equals("parameter"))
			n_mesh = new NeighbourhoodMesh(mesh);
		
		int null_count = 0;
		
		if (progress != null){
			progress.setMinimum(0);
			progress.setMaximum(mesh.n);
			}
		
		DefaultMatrixFileWriter writer = null;
		
		if (output_matrix){
			//then we need to open a file writer
			writer = new DefaultMatrixFileWriter(new File(matrix_file),
												 MatrixOutOptions.FORMAT_BINARY_SPARSE);
			//lots of precision
			writer.number_format = "#0.000000##";
			writer.precision = 6;
			writer.open();
			}
		
		int prog_int = (int)(0.01 * mesh.n);
		int last_prog = 0;
		
		//iterate through vertices
		for (int m = 0; m < mesh.n; m++){
			
			if (progress != null){
				if (progress.isCancelled()){
					return null;
					}
				if (m > last_prog){
					progress.update(m);
					last_prog += prog_int;
					}
				}
			Point3f node = mesh.getVertex(m);
			double set_max = -Double.MAX_VALUE;
			
			//First set sigma_t
			double sigma_t = sigma_tangent;
			if (setSigmaT.equals("From mean area")){
				//for each neighbour pair, get area
				ArrayList<Integer> ring = n_mesh.getNeighbourhoodRing(m);
				if (ring == null){
					//in this case we have an edge vertex; handle differently..
					InterfaceSession.log("Edge node.. setting sigma_t to parameter..", LoggingType.Debug);
					
				}else{
					double area = 0;
					Point3f n0 = mesh.getVertex(ring.get(0));
					for (int l = 1; l < ring.size(); l++){
						Point3f n1 = mesh.getVertex(ring.get(l));
						area += GeometryFunctions.getArea(node, n0, n1);
						n0 = n1;
						}
					//include last face
					n0 = mesh.getVertex(ring.get(ring.size() - 1));
					Point3f n1 = mesh.getVertex(ring.get(0));
					area += GeometryFunctions.getArea(node, n0, n1);
					sigma_t = Math.sqrt(area / (ring.size() * Math.PI));
					}
				
			}else if (setSigmaT.equals("From mean length")){
				//get mean edge length
				int[] nbrs = n_mesh.getNeighbourhood(m).getNeighbourList();
				
				sigma_t = 0;
				for (int l = 0; l < nbrs.length; l++)
					sigma_t += node.distance(mesh.getVertex(nbrs[l]));
				
				sigma_t /= nbrs.length;
				}
			
			//Next get normal for this node
			Vector3f normal = normals.get(m);
			
			//Next determine search bounds
			normal.normalize();
			normal.scale((float)(sigma_normal * sigma_max_normal));
			Point3f p = new Point3f();
			p.add(node, normal);
			float min_x = p.x;
			float min_y = p.y;
			float min_z = p.z;
			float max_x = p.x;
			float max_y = p.y;
			float max_z = p.z;
			p = new Point3f();
			p.sub(node, normal);
			min_x = Math.min(min_x, p.x);
			min_y = Math.min(min_y, p.y);
			min_z = Math.min(min_z, p.z);
			max_x = Math.max(max_x, p.x);
			max_y = Math.max(max_y, p.y);
			max_z = Math.max(max_z, p.z);
			
			//add/subtract max tangent bounds
			float max_t = (float)(sigma_tangent * sigma_max_tangent);
			min_x -= max_t;
			min_y -= max_t;
			min_z -= max_t;
			max_x += max_t;
			max_y += max_t;
			max_z += max_t;
			
			// Next get subvolume for bounds
			// (will be null if this bounds is outside grid bounds) 
			int[] sub_vol = grid.getSubGrid(new Point3f(min_x, min_y, min_z), 
							 				new Point3f(max_x, max_y, max_z));
			
			// Next determine weights for all voxels in subvolume
			if (sub_vol != null){
				double denom = 0;
				double min_normal = Double.MAX_VALUE;
				double min_tangent = Double.MAX_VALUE;
				Point3f mp = new Point3f();
				Vector3f v_mp = new Vector3f();
				Vector3f v_proj = new Vector3f();
				Point3f ep = new Point3f();
				
				// For each voxel in bounds, add weighted value
				for (int i = sub_vol[0]; i < sub_vol[3]; i++)
					for (int j = sub_vol[1]; j < sub_vol[4]; j++)
						for (int k = sub_vol[2]; k < sub_vol[5]; k++){
							
							// normal weight
							mp.set(grid.getVoxelMidPoint(i, j, k));
							normal.normalize();
							v_mp.set(mp);
							v_mp.sub(node);
							
							// normal distance from node to voxel
							v_proj.set(GeometryFunctions.getProjectedVector(v_mp, normal));
							min_normal = Math.min(min_normal, v_proj.length());
							double delta_n = v_proj.length() / sigma_normal;
							if (delta_n < sigma_max_normal){
								double w_normal = StatFunctions.getGaussian(v_proj.length(), 0, sigma_normal);
								
								// tangent weight
								ep.set(node);
								ep.add(v_proj);
								
								// tangent distance from node to voxel
								v_mp.sub(mp, ep);
								min_tangent = Math.min(min_tangent, v_mp.length());
								double delta_t = v_mp.length() / sigma_t;
								if (delta_t < sigma_max_tangent){
									
									// you've come a long way, baby
									double w_tangent = StatFunctions.getGaussian(v_mp.length(), 0, sigma_t);
									
									// add weighted contribution
									//double d = grid.getScaledValue(channel, i, j, k, 0);
									double d = volume.getDatumAtVoxel(channel, i, j, k).getValue();
									values.get(m).add(d * w_normal * w_tangent);
									denom += w_normal * w_tangent;
									
									// max?
									if (delta_n < normal_set_max_sigma && delta_t < tangent_set_max_sigma)
										set_max = d;
									
									// write to matrix if necessary
									if (output_matrix){
										writer.writeLine(grid.getAbsoluteIndex(i, j, k), 
														 m, 
														 w_normal * w_tangent);
										
										}
									}
								}
							}
				// result is weighted average
				values.get(m).divide(denom);
				
				// set to max value if applicable
				if (values.get(m).getValue() < set_max)
					values.get(m).setValue(set_max);
				if (Double.isNaN(values.get(m).getValue()) || Double.isInfinite(values.get(m).getValue()))
					values.get(m).setValue(0);		// TODO specify this value as a parameter
				}
			}
		
		if (output_matrix){
			InterfaceSession.log("Writing transfer matrix to '" + matrix_file + "...", LoggingType.Debug);
			writer.finalize(grid.getSizeS() * grid.getSizeT() * grid.getSizeR(), mesh.n);
			InterfaceSession.log("Done.", LoggingType.Debug);
			}
		InterfaceSession.log("\nAll done. " + null_count + " vertices not mapped", LoggingType.Debug);
		return values;
	
	}
	
	/**************************************************
	 * Projects vertex-wise data from a given {@link Mesh3DInt} data column to a {@link Grid3D}
	 * channel, normal and transverse (tangent) Gaussian functions. 
	 * 
	 * @param mesh_int			The mesh from which to project
	 * @param grid				The target 3D grid
	 * @param mesh_column		The column from which to obtain the projected data
	 * @param grid_channel		The target grid channel 
	 * @param sigma_normal		The sigma defining a Gaussian normal to the vertex
	 * @param sigma_tangent		The sigma defining a Gaussian tangential to the vertex
	 * @param max_normal		The maximum distance to project data in the normal direction
	 * @param max_tangent		The maximum distance to project data in the tangential direction
	 * @param is_discrete 		Indicates whether data are to be treated as discrete; if so, the
	 *  						mapped value will be the value of the neighbour with the highest
	 *  						Gaussian weight. Otherwise, value is the weighted real-valued mean.
	 * @param progress
	 * @return					A new <code>Grid3D</code> containing the projected values
	 */
	public static Volume3DInt mapMeshToVolumeGaussian(final Mesh3DInt mesh_int,
												  final Volume3DInt volume,
												  final String mesh_column,
												  final String grid_channel,
												  final double sigma_normal,
												  final double sigma_tangent,
												  final double sigma_max_normal,
												  final double sigma_max_tangent,
												  final boolean normalize,
												  final boolean is_discrete,
												  final ProgressUpdater progress){
		
		if (progress == null || !(progress instanceof InterfaceProgressBar)) 
			return MeshFunctions.mapMeshToVolumeGaussianBlocking(mesh_int, 
																 volume,
																 mesh_column,
																 grid_channel, 
																 sigma_normal, 
																 sigma_tangent, 
																 sigma_max_normal, 
																 sigma_max_tangent, 
																 normalize,
																 is_discrete,
																 progress);
		
		return (Volume3DInt)Worker.post(new Job(){
				@Override
				public Volume3DInt run(){
					return MeshFunctions.mapMeshToVolumeGaussianBlocking(mesh_int, 
																		 volume,
																		 mesh_column,
																		 grid_channel, 
																		 sigma_normal, 
																		 sigma_tangent, 
																		 sigma_max_normal, 
																		 sigma_max_tangent, 
																		 normalize,
																		 is_discrete,
																		 progress);
				}
			});
		
	}
	

	/**************************************************
	 * Projects vertex-wise data from a given {@link Mesh3DInt} data column to a {@link Grid3D}
	 * channel, normal and transverse (tangent) Gaussian functions. 
	 * 
	 * @param mesh_int			The mesh from which to project
	 * @param grid				The target 3D grid
	 * @param mesh_column		The column from which to obtain the projected data
	 * @param grid_channel		The target grid channel 
	 * @param sigma_normal		The sigma defining a Gaussian normal to the vertex
	 * @param sigma_tangent		The sigma defining a Gaussian tangential to the vertex
	 * @param max_normal		The maximum distance to project data in the normal direction
	 * @param max_tangent		The maximum distance to project data in the tangential direction
	 * @param is_discrete 		Indicates whether data are to be treated as discrete; if so, the
	 *  						mapped value will be the value of the neighbour with the highest
	 *  						Gaussian weight. Otherwise, value is the weighted real-valued mean.
	 * @param progress
	 * @return					A new <code>Grid3D</code> containing the projected values. The
	 * 							target channel will be of transfer type <code>DOUBLE</code>.
	 */
	static Volume3DInt mapMeshToVolumeGaussianBlocking(final Mesh3DInt mesh_int,
														  final Volume3DInt volume,
														  final String mesh_column,
														  final String grid_channel,
														  final double sigma_normal,
														  final double sigma_tangent,
														  final double sigma_max_normal,
														  final double sigma_max_tangent,
														  final boolean normalize,
														  final boolean is_discrete,
														  final ProgressUpdater progress){
		
		Grid3D grid = volume.getGrid();
		
		Mesh3D mesh = mesh_int.getMesh();
		ArrayList<MguiNumber> values = mesh_int.getVertexData(mesh_column);
		
		//list of normals
		ArrayList<Vector3f> normals = mesh.getNormals();
		
		int null_count = 0;
		
		if (progress != null){
			progress.setMinimum(0);
			progress.setMaximum(mesh.n);
			}
	
		Volume3DInt new_volume = new Volume3DInt(new Grid3D(grid));
		new_volume.addVertexData(grid_channel, DataBuffer.TYPE_DOUBLE);
		new_volume.addVertexData("_denom", DataBuffer.TYPE_DOUBLE);
		new_volume.setCurrentColumn(grid_channel);
		
		int prog_int = (int)(0.01 * mesh.n);
		int last_prog = 0;
		
		int vertex_count = 0;
		
		//iterate through indices
		for (int m = 0; m < mesh.n; m++){
			
			if (progress != null){
				if (progress.isCancelled()){
					return null;
					}
				if (m > last_prog){
					progress.update(m);
					last_prog += prog_int;
					}
				}
			
			Point3f node = mesh.getVertex(m);
			
			//Next get normal for this node
			Vector3f normal = normals.get(m);
			
			//Next determine search bounds
			normal.normalize();
			normal.scale((float)(sigma_normal * sigma_max_normal));
			Point3f p = new Point3f();
			p.add(node, normal);
			float min_x = p.x;
			float min_y = p.y;
			float min_z = p.z;
			float max_x = p.x;
			float max_y = p.y;
			float max_z = p.z;
			p = new Point3f();
			p.sub(node, normal);
			min_x = Math.min(min_x, p.x);
			min_y = Math.min(min_y, p.y);
			min_z = Math.min(min_z, p.z);
			max_x = Math.max(max_x, p.x);
			max_y = Math.max(max_y, p.y);
			max_z = Math.max(max_z, p.z);
			
			//add/subtract max tangent bounds
			float max_t = (float)(sigma_normal * sigma_max_normal);
			min_x -= max_t;
			min_y -= max_t;
			min_z -= max_t;
			max_x += max_t;
			max_y += max_t;
			max_z += max_t;
			
			//Next get subvolume for bounds
			//(will be null if this bounds is outside grid bounds) 
			int[] sub_vol = grid.getSubGrid(new Point3f(min_x, min_y, min_z), 
							 				new Point3f(max_x, max_y, max_z));
			
			//Next determine weights for all voxels in subvolume
			if (sub_vol != null){
				double min_normal = Double.MAX_VALUE;
				double min_tangent = Double.MAX_VALUE;
				Point3f mp = new Point3f();
				Vector3f v_mp = new Vector3f();
				Vector3f v_proj = new Vector3f();
				Point3f ep = new Point3f();
				
				HashMap<Integer,Double> nbr_weights = null;
				if (is_discrete)
					nbr_weights = new HashMap<Integer,Double>();
				
				//For each voxel in bounds, add weighted value
				for (int i = sub_vol[0]; i < sub_vol[3]; i++)
					for (int j = sub_vol[1]; j < sub_vol[4]; j++)
						for (int k = sub_vol[2]; k < sub_vol[5]; k++){
							
							int index = grid.getAbsoluteIndex(i, j, k);
							
							//normal weight
							mp.set(grid.getVoxelMidPoint(i, j, k));
							normal.normalize();
							v_mp.set(mp);
							v_mp.sub(node);
							
							//normal distance from node to voxel
							v_proj.set(GeometryFunctions.getProjectedVector(v_mp, normal));
							min_normal = Math.min(min_normal, v_proj.length());
							if (v_proj.length() / sigma_normal < sigma_max_normal){
								double w_normal = StatFunctions.getGaussian(v_proj.length(), 0, sigma_normal);
								
								//tangent weight
								ep.set(node);
								ep.add(v_proj);
								
								//tangent distance from node to voxel
								v_mp.sub(mp, ep);
								min_tangent = Math.min(min_tangent, v_mp.length());
								if (v_mp.length() / sigma_tangent < sigma_max_tangent){
									
									if (index == 815044){
										index += 0;	//breakpoint
										vertex_count++;
										}
									
									//you've come a long way, baby
									double w_tangent = StatFunctions.getGaussian(v_mp.length(), 0, sigma_tangent);
									
									
									if (is_discrete){
										double old_weight = new_volume.getDatumAtVoxel(grid_channel, i, j, k).getValue();
										double weight = w_normal * w_tangent;
										
										// If weight is higher, set to new value
										if (weight > old_weight){
											double value = values.get(m).getValue();
											new_volume.setDatumAtVoxel(grid_channel, i, j, k, value);
											new_volume.setDatumAtVoxel("_denom", i, j, k, weight);
											}
										
									}else{
										//add weighted contribution
										double value = values.get(m).getValue() * w_normal * w_tangent;
										//values.get(m).add(grid.getValue(channel, i, j, k, 0) * w_normal * w_tangent);
																			
										double g_value = new_volume.getDatumAtVoxel(grid_channel, i, j, k).getValue();
										if (Double.isInfinite(g_value) || Double.isNaN(g_value)) g_value = 0;
										g_value += value;
										new_volume.setDatumAtVoxel(grid_channel, i, j, k, g_value);
										double d_value = new_volume.getDatumAtVoxel("_denom", i, j, k).getValue();
										if (Double.isInfinite(d_value) || Double.isNaN(d_value)) d_value = 0;
										d_value += w_normal * w_tangent;
										new_volume.setDatumAtVoxel("_denom", i, j, k, d_value);
										}
									
									}
								}
							}
				
				}
			}
		
		
			//If continuous,
			//result is weighted average
		if (!is_discrete){
			int n = new_volume.getVertexCount();
			for (int i = 0; i < n; i++){
				double value = new_volume.getDatumAtVertex(grid_channel, i).getValue();
				double denom = new_volume.getDatumAtVertex("_denom", i).getValue();
				if (denom > 0)
					value /= denom;
				if (Double.isNaN(value) || Double.isInfinite(value))
					value = 0;
				
				new_volume.setDatumAtVertex(grid_channel, i, value);
				}
			
			}
		
		new_volume.removeVertexData("_denom");
		
		InterfaceSession.log("\nAll done. " + null_count + " voxels not mapped");
		return new_volume;
		
	}

	/********************************************
	 * Returns the average edge length keyin a mesh.
	 * 
	 * @param mesh
	 * @return
	 */
	public static double getAverageEdgeLength(Mesh3D mesh){
		
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		boolean[] traversed = new boolean[mesh.n];
		double total_length = 0;
		int edge_count = 0;
		
		for (int i = 0; i < mesh.n; i++){
			if (!traversed[i]){
				traversed[i] = true;
				int[] nbrs = n_mesh.getNeighbourhood(i).getNeighbourList();
				for (int j = 0; j < nbrs.length; j++){
					if (!traversed[nbrs[j]]){
						total_length += mesh.getVertex(nbrs[j]).distance(mesh.getVertex(i));
						edge_count++;
						}
					}
				}
			}
		
		return total_length / (double)edge_count;
		
	}
	
	/*********************************
	 * Returns surface normals for all vertices of <code>mesh</code>, determined as the average
	 * vector of all triangular face normals.
	 * 
	 * @param mesh
	 * @return
	 */
	public static ArrayList<Vector3f> getSurfaceNormals(Mesh3D mesh){
		
		ArrayList<Vector3f> normals = new ArrayList<Vector3f>(mesh.n);
		HashMap<Integer, ArrayList<Vector3f>> normal_map = new HashMap<Integer, ArrayList<Vector3f>>();
		
		for (int f = 0; f < mesh.f; f++){
			MeshFace3D face = mesh.getFace(f);
			Vector3f normal = mesh.getNormalAtFace(f);
			
			for (int i = 0; i < 3; i++){
				int node = face.getNode(i);
				ArrayList<Vector3f> n_list = normal_map.get(node);
				if (n_list == null){
					n_list = new ArrayList<Vector3f>();
					normal_map.put(node, n_list);
					}
				n_list.add(normal);
				}	
			}
		
		for (int i = 0; i < mesh.n; i++){
			ArrayList<Vector3f> n_list = normal_map.get(i);
			Vector3f normal = new Vector3f();
			if (n_list != null){
				for (int j = 0; j < n_list.size(); j++){
					normal.add(n_list.get(j));
					}
				normal.normalize();
				}
			normals.add(normal);
			}
		
		return normals;
	}
	
	/******************************************************
	 * Maps the values in <code>channel</code> of <code>grid</code>, an instance of {@link Grid3D}, 
	 * to the vertices of <code>mesh</code>, using a containing-voxel approach. This method essentially maps
	 * the value of the containing voxel to the vertex, ignoring all neighbours. Size is single voxel.
	 * 
	 * @param mesh			Mesh whose vertices are used to determine mapping
	 * @param volume		Volume to map
	 * @param column 		Column to map
	 * @param no_value		Value to use when no mapping is made
	 * @param progress
	 * @return A list of size n, where n = number of vertices in <code>mesh</code>
	 */
	public static ArrayList<MguiNumber> mapVolumeToMeshEV(Mesh3D mesh, Volume3DInt volume, String column, double no_value, ProgressUpdater progress){
		return mapVolumeToMeshEV(mesh, volume, column, no_value, -1, 0, progress);
	}
	
	/******************************************************
	 * Maps the values in <code>channel</code> of <code>grid</code>, an instance of {@link Grid3D}, 
	 * to the vertices of <code>mesh</code>, using a containing-voxel(s) approach. This method essentially maps
	 * the value of the containing voxel to the vertex, ignoring all neighbours.
	 * 
	 * @param mesh			Mesh whose vertices are used to determine mapping
	 * @param volume		Volume to map
	 * @param column 		Column to map
	 * @param no_value		Value to use when no mapping is made
	 * @param size 			Radius of the search sphere, in voxels; <=0 is single voxel (default)
	 * @param stat 			Statistic to use for summarizing values in a search sphere; only used if radius > 0;
	 *  					one of: 1=max, 2=min, 3=abs max, 4=abs min, 0(or other)= mean
	 * @param progress
	 * @return A list of size n, where n = number of vertices in <code>mesh</code>
	 */
	public static ArrayList<MguiNumber> mapVolumeToMeshEV(final Mesh3D mesh, 
															final Volume3DInt volume, 
															final String column, 
															final double no_value, 
															final float radius, 
															final int stat, 
															final ProgressUpdater progress){
		
		if (progress == null || !(progress instanceof InterfaceProgressBar)) 
			return mapVolumeToMeshEVBlocking(mesh,
					volume,
					column,
					no_value,
					radius,
					stat,
					progress);
		
		return (ArrayList<MguiNumber>)Worker.post(new Job(){
			@Override
			public ArrayList<MguiNumber> run(){
				return MeshFunctions.mapVolumeToMeshEVBlocking(mesh,
						volume,
						column,
						no_value,
						radius,
						stat,
						progress);
				}
			});
		
	}
	
	
	/******************************************************
	 * Maps the values in <code>channel</code> of <code>grid</code>, an instance of {@link Grid3D}, 
	 * to the vertices of <code>mesh</code>, using a containing-voxel(s) approach. This method essentially maps
	 * the value of the containing voxel to the vertex, ignoring all neighbours.
	 * 
	 * @param mesh			Mesh whose vertices are used to determine mapping
	 * @param volume		Volume to map
	 * @param column 		Column to map
	 * @param no_value		Value to use when no mapping is made
	 * @param radius 		Radius of the search sphere, in voxels; <=0 is single voxel (default)
	 * @param stat 			Statistic to use for summarizing values in a search sphere; only used if radius > 0;
	 *  					one of: 1=max, 2=min, 3=abs max, 4=abs min, 5=mode, 6=median, 0(or other)= mean
	 * @param progress
	 * @return A list of size n, where n = number of vertices in <code>mesh</code>
	 */
	protected static ArrayList<MguiNumber> mapVolumeToMeshEVBlocking(Mesh3D mesh, Volume3DInt volume, String column, double no_value, float radius, int stat, ProgressUpdater progress){
		
		if (!volume.hasColumn(column)){
			InterfaceSession.log("MeshFunctions.mapVolumeToMeshEV: Volume '" + volume.getName() + "' has no column '" + column + "'.", 
								 LoggingType.Errors);
			return null;
			}
		ArrayList<MguiNumber> values = new ArrayList<MguiNumber>(mesh.n);
		Grid3D grid = volume.getGrid();
		
		if (progress != null){
			progress.setMinimum(0);
			progress.setMaximum(mesh.n);
			}
		
		for (int i = 0; i < mesh.n; i++){
			int[] voxel = grid.getEnclosingVoxel(mesh.getVertex(i));
			if (voxel == null){
				values.add(new MguiDouble(no_value));
			}else{
				if (radius <= 0){
					values.add(new MguiDouble(volume.getDatumAtVoxel(column, voxel[0], voxel[1], voxel[2])));
				}else{
					int[][] voxels = getVoxelSphere(grid, voxel, radius);
					double value = 0;
					ArrayList<MguiNumber> list_vals = null;
					if (stat == 5 || stat == 6){
						list_vals = new ArrayList<MguiNumber>(voxels.length);
						}
					for(int j = 0; j < voxels.length; j++){
						voxel = voxels[j];
						MguiNumber nn = volume.getDatumAtVoxel(column, voxel[0], voxel[1], voxel[2]);
						if (nn == null){
							int a = 0;
						}else{
							double v = volume.getDatumAtVoxel(column, voxel[0], voxel[1], voxel[2]).getValue();
							switch (stat){
								case 1:
									value = Math.max(value, v);
									break;
								case 2:
									value = Math.min(value, v);
									break;
								case 3:
									if (Math.abs(value) < Math.abs(v))
										value = v;
									break;
								case 4:
									if (Math.abs(value) > Math.abs(v))
										value = v;
									break;
								case 5:
								case 6:
									// Mode or median, need to list all values
									if (v != no_value)
										list_vals.add(new MguiDouble(v));
									break;
								default:
									value += v;
									break;
								}
							}
						}
					if (stat < 1 || stat > 4)
						value /= (double)voxels.length;
					if (stat == 5){
						// Mode
						value = StatFunctions.getMode(list_vals);
						}
					if (stat == 6){
						// Median
						value = StatFunctions.getMedian(list_vals);
						}
					values.add(new MguiDouble(value));
					}
				}
			if (progress != null){
				progress.update(i);
				}
			}
		
		return values;
	}
	
	// Determines a sphere of voxels centered on {@code center}, as a search space
	private static int[][] getVoxelSphere(Grid3D grid, int[] center, float radius){
		
		// Radius
		float[] geom = grid.getGeomDims();
		int[] dims = grid.getDims();
		float s_vox = geom[0] / (float)dims[0];
		float t_vox = geom[1] / (float)dims[1];
		float r_vox = geom[2] / (float)dims[2];
		int s_dist = (int)Math.ceil(radius/s_vox);
		int t_dist = (int)Math.ceil(radius/t_vox);
		int r_dist = (int)Math.ceil(radius/r_vox);
		
		Point3f c_pt = grid.getVoxelMidPoint(center[0], center[1], center[2]);
		
		// Get bounds for radius
		int min_s = Math.max(0, center[0] - s_dist);
		int max_s = Math.min(dims[0], center[0] + s_dist);
		int min_t = Math.max(0, center[1] - t_dist);
		int max_t = Math.min(dims[1], center[1] + t_dist);
		int min_r = Math.max(0, center[2] - r_dist);
		int max_r = Math.min(dims[2], center[2] + r_dist);
		
		Point3f v_pt = new Point3f();
		ArrayList<int[]> vox_list = new ArrayList<int[]>();
		for (int i = min_s; i <= max_s; i++)
			for (int j = min_t; j <= max_t; j++)
				for (int k = min_r; k <= max_r; k++){
					v_pt.set(grid.getVoxelMidPoint(i, j, k));
					if (v_pt.distance(c_pt) < radius)
						vox_list.add(new int[]{i,j,k});
					}
		
		if (vox_list.size() == 0) return new int[][]{center};
		
		int[][] voxels = new int[vox_list.size()][3];
		for (int i = 0; i < vox_list.size(); i++)
			voxels[i] = vox_list.get(i);
		
		return voxels;
	}
	
	/**********************
	 * 
	 * Gets a submesh from {@code mesh} consisting of the neighbourhood of vertex {@code i}. 
	 * 
	 * @param mesh
	 * @param i
	 * @return
	 */
	public static Mesh3D getNeighbourhoodSubmesh(Mesh3D mesh, int i){
		
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		Mesh3D mesh_new = new Mesh3D();
		mesh_new.addVertex(mesh.getVertex(i));
		
		Neighbourhood hood = n_mesh.neighbourhoods.get(i);
		int[] nbrs = hood.getNeighbourList();
		
		for (int j = 0; j < nbrs.length; j++)
			mesh_new.addVertex(mesh.getVertex(nbrs[j]));
		
		for (int j = 0; j < nbrs.length; j++)
			for (int k = 0; k < nbrs.length; k++)
				if (n_mesh.getNeighbourhood(nbrs[j]).hasNeighbour(nbrs[k]))
					mesh_new.addFace(0, j + 1, k + 1);
		
		return mesh_new;
		
	}
	
	public static void cleanMesh(Mesh3D mesh){
		//removeDuplicateFaces(mesh);
		removeDuplicateNodes(mesh);
		mesh.finalize();
	}
	
	public static Mesh3D getMeanSphereMesh(Point3f center, float radius, int n_nodes, float stop_delta, long max_itrs){
		
		Mesh3D mesh = getFractalSphereMesh(center, radius, n_nodes);
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		
		float delta_max = Float.MAX_VALUE;
		long itrs = 0; 
		
		while (delta_max > stop_delta && itrs++ < max_itrs){
			
			delta_max = 0;
			//for each neighbourhood, get total area
			//and set i to the mean of its neighbours
			for (int i = 0; i < n_mesh.neighbourhoods.size(); i++){
				
				Point3f node = mesh.getVertex(i);
				int[] nbrs = n_mesh.neighbourhoods.get(i).getNeighbourList();
				
				if (nbrs.length > 0){
					Point3f max_pt = new Point3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
					Point3f min_pt = new Point3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
					
					for (int j = 0; j < nbrs.length; j++){
						//mean_pt.add(mesh.getNode(nbrs[j]));
						Point3f p = mesh.getVertex(nbrs[j]);
						max_pt = GeometryFunctions.getMaxPt(p, max_pt);
						min_pt = GeometryFunctions.getMinPt(p, min_pt);
						}
					
					//mean_pt.scale(1f / (float)nbrs.length);
					//mean_pt.set(getSphereNode(center, radius, mean_pt));
					Vector3f v = new Vector3f(max_pt);
					v.sub(min_pt);
					v.scale(0.5f);
					min_pt.add(v);
					min_pt.set(getSphereNode(center, radius, min_pt));
					delta_max = min_pt.distance(node);
					mesh.setVertex(i, min_pt);
					}
				
				}
			}	
		
		//mesh.finalize();
		return mesh;
	}
	
	public static Mesh3D getFractalSphereMesh(Point3f center, float radius, int min_nodes){
		
		//start with tetrahedron; 5 nodes, 6 faces
		//for each step, split each face and map onto sphere
		
		//make ourselves a tetrahedron
		Mesh3D mesh = new Mesh3D();
		Point3f p = new Point3f(center);
		p = new Point3f(center);
		p.add(new Vector3f(radius, 0, 0)); 
		mesh.addVertex(p);
		p = new Point3f(center);
		p.add(new Vector3f(0, radius, 0)); 
		mesh.addVertex(p);
		p = new Point3f(center);
		p.add(new Vector3f(0, 0, radius)); 
		mesh.addVertex(p);
		p = new Point3f(center);
		p.add(new Vector3f(-radius, 0, 0)); 
		mesh.addVertex(p);
		p = new Point3f(center);
		p.add(new Vector3f(0, -radius, 0)); 
		mesh.addVertex(p);
		p = new Point3f(center);
		p.add(new Vector3f(0, 0, -radius)); 
		mesh.addVertex(p);
		mesh.addFace(4, 0, 2);
		mesh.addFace(0, 1, 2);
		mesh.addFace(1, 3, 2);
		mesh.addFace(3, 4, 2);
		mesh.addFace(5, 4, 3);
		mesh.addFace(5, 0, 4);
		mesh.addFace(5, 3, 1);
		mesh.addFace(5, 1, 0);
		
		//iterate
		while (min_nodes > mesh.n){
			
			//subdivideMesh(mesh, 1);
			
			
			//for all faces, split
			int n = mesh.n;
			
			ArrayList<Mesh3D.MeshFace3D> faces = mesh.getFaces();
			mesh.removeAllFaces();
			
			int[][] added_node = new int[mesh.n][mesh.n];
			for (int i = 0; i < mesh.n; i++)
				Arrays.fill(added_node[i], -1);
			
			for (int i = 0; i < faces.size(); i++)
				splitMeshFace(mesh, faces.get(i), i, added_node);
			
			
			//for all new nodes, make spherical
			for (int i = 0; i < mesh.n; i++)
				mesh.setVertex(i, getSphereNode(center, radius, mesh.getVertex(i)));
				
			}
		
		return mesh;
	}
	
	static void splitMeshFace(Mesh3D mesh, Mesh3D.MeshFace3D face, int i, int[][] added_node){
		
		//get three midpoints
		//make new nodes
		//add four faces
		Point3f p = null;
		int a, b, c;
		
		int low = Math.min(face.A, face.B);
		int high = Math.max(face.A, face.B);
		if (added_node[low][high] >= 0){
			p = mesh.getVertex(added_node[low][high]);
		}else{
			p = GeometryFunctions.getMidPt(mesh.getVertex(low), mesh.getVertex(high));
			mesh.addVertex(p);
			added_node[low][high] = mesh.getSize() - 1;
			}
		a = added_node[low][high];
		
		low = Math.min(face.B, face.C);
		high = Math.max(face.B, face.C);
		if (added_node[low][high] >= 0){
			p = mesh.getVertex(added_node[low][high]);
		}else{
			p = GeometryFunctions.getMidPt(mesh.getVertex(low), mesh.getVertex(high));
			mesh.addVertex(p);
			added_node[low][high] = mesh.getSize() - 1;
			}
		b = added_node[low][high];
		
		low = Math.min(face.C, face.A);
		high = Math.max(face.C, face.A);
		if (added_node[low][high] >= 0){
			p = mesh.getVertex(added_node[low][high]);
		}else{
			p = GeometryFunctions.getMidPt(mesh.getVertex(low), mesh.getVertex(high));
			mesh.addVertex(p);
			added_node[low][high] = mesh.getSize() - 1;
			}
		c = added_node[low][high];
		
		//mesh.addFace(face.A, face.B, face.C);
		mesh.addFace(face.A, a, c);
		mesh.addFace(a, b, c);
		mesh.addFace(a, face.B, b);
		mesh.addFace(c, b, face.C);
		
	}
	
	static Point3f getSphereNode(Point3f center, float radius, Point3f p){
		
		Vector3f v = new Vector3f(p);
		v.sub(center);
		v.normalize();
		v.scale(radius);
		Point3f p2 = new Point3f(center);
		p2.add(v);
		
		return p2;
		
	}
	
	public static Mesh3D getOptimizedSphereMesh(Point3f center, float radius, int n_nodes, 
												double stop_value, long max_iter, float entropy){
		
		Mesh3D mesh = getGlobeSphereMesh(center, radius, n_nodes);
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		EnergySphere e_mesh = new EnergySphere(mesh, n_mesh, center, radius);
		
		long i = 0;
		while (i++ < max_iter && e_mesh.getMaxEnergy() > stop_value)
			e_mesh.update(entropy);
		
		return mesh;
	}
	
	/***************************************************************
	 * Determines whether a vector defined by {@code p} and {@code v} intersects {@code mesh}.
	 * 
	 * @param mesh
	 * @param p
	 * @param v
	 * @return
	 */
	public static boolean intersects(Mesh3D mesh, Point3f p, Vector3f v){
		Box3D bounds = mesh.getBoundBox();
		Point3f p2 = new Point3f(p);
		p2.add(v);
		if (!bounds.contains(p) && ! bounds.contains(p2)) return false;
		for (int i = 0; i < mesh.f; i++){
			Triangle3D tri = mesh.getFaceTriangle(i);
			if (GeometryFunctions.intersects(p, v, tri)) return true;
			}
		return false;
	}
	
	/****************************************************************
	 * Returns a list of face indices indicating which faces intersect between {@code mesh1} and {@code mesh2}.
	 * If there are no such faces, returns an empty list.
	 * 
	 * @param mesh1
	 * @param mesh2
	 * @param search_max Maximum search radius; should be similar to largest face size; larger takes longer
	 * @return
	 */
	public static ArrayList<ArrayList<Integer>> getIntersectingFaces(Mesh3D mesh1, Mesh3D mesh2, float search_max){
		return getIntersectingFaces(mesh1, mesh2, search_max, null);
	}
	
	/****************************************************************
	 * Returns two lists of face indices indicating which faces intersect between {@code mesh1} and {@code mesh2}.
	 * If there are no such faces, returns an empty list.
	 * 
	 * @param mesh1
	 * @param mesh2
	 * @param search_max Maximum search radius; should be similar to largest face size; larger takes longer
	 * @param progress
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<ArrayList<Integer>> getIntersectingFaces(final Mesh3D mesh_1, final Mesh3D mesh_2, 
																	 final float search_max, final ProgressUpdater progress){
		
		return (ArrayList<ArrayList<Integer>>)Worker.post(new Job(){
			@Override
			public ArrayList<ArrayList<Integer>> run(){
				return getIntersectingFacesBlocking(mesh_1, mesh_2, search_max, progress);
			}
		});
		
	}
	
	/****************************************************************
	 * Returns two lists of face indices indicating which faces intersect between {@code mesh1} and {@code mesh2}.
	 * If there are no such faces, returns an empty list.
	 * 
	 * @param mesh1
	 * @param mesh2
	 * @param search_max Maximum search radius; should be similar to largest face size; larger takes longer
	 * @param progress
	 * @return
	 */
	public static ArrayList<ArrayList<Integer>> getIntersectingFacesBlocking(Mesh3D mesh_1, Mesh3D mesh_2, 
																	 		 float search_max, ProgressUpdater progress){
		
		ArrayList<Boolean> intersects_1 = new ArrayList<Boolean>();
		ArrayList<Triangle3D> faces_1 = new ArrayList<Triangle3D>();
		ArrayList<Boolean> intersects_2 = new ArrayList<Boolean>();
		ArrayList<Triangle3D> faces_2 = new ArrayList<Triangle3D>();
		
		//Sorted mesh allows only finite boundaries to be searched
		SortedMesh s_mesh_1 = new SortedMesh(mesh_1);
		ArrayList<Point3f> vertices_1 = new ArrayList<Point3f>();
		FaceMesh f_mesh_1 = new FaceMesh(mesh_1);
		
		for (int i = 0; i < mesh_1.getSize() - 1; i++){
			vertices_1.add(mesh_1.getVertex(s_mesh_1.sorted_nodes.get(i)));
			}
		
		SortedMesh s_mesh_2 = new SortedMesh(mesh_2);
		ArrayList<Point3f> vertices_2 = new ArrayList<Point3f>();
		FaceMesh f_mesh_2 = new FaceMesh(mesh_2);
		
		//ArrayList<Integer> test = f_mesh_2.getFaces(25650);
		//System.out.println(test);
		
		for (int i = 0; i < mesh_2.getSize() - 1; i++){
			//if (s_mesh_2.sorted_nodes.get(i) == 25650)
			//	System.out.println(i);
			vertices_2.add(mesh_2.getVertex(s_mesh_2.sorted_nodes.get(i)));
			}
		
		int f_1 = mesh_1.getFaceCount();
		
		for (int i = 0; i < f_1; i++){
			intersects_1.add(false);
			faces_1.add(mesh_1.getFaceTriangle(i));
			}
		
		int f_2 = mesh_2.getFaceCount();
		
		for (int i = 0; i < f_2; i++){
			intersects_2.add(false);
			faces_2.add(mesh_2.getFaceTriangle(i));
			}
		
		int start = 0, last_j = 0;
		if (progress != null){
			progress.getMinimum();
			progress.setMaximum(start + (vertices_1.size() + vertices_2.size()) / 2);
			}
		
		// Compare sorted vertices
		for (int i = 0; i < vertices_1.size(); i++){
			boolean done = false;
			int _i = s_mesh_1.sorted_nodes.get(i);
			// List of faces associated with vertex i
			ArrayList<Integer> faces_i = f_mesh_1.getFaces(_i);
			for (int j = last_j; faces_i != null && !done && j < vertices_2.size(); j++){
				// Find first vertex that is close enough to consider
				// and iterate through all remaining such vertices 
				if (vertices_2.get(j).x < vertices_1.get(i).x)
					last_j = j;
				if (j == 40870)
					j += 0;	//breakpoint
				while (j < vertices_2.size() &&
					   Math.abs(vertices_2.get(j).x - vertices_1.get(i).x) < search_max){
					if (Math.abs(vertices_2.get(j).y - vertices_1.get(i).y) < search_max &&
						Math.abs(vertices_2.get(j).z - vertices_1.get(i).z) < search_max){
						// This vertex is close enough; get list of associated faces and compare each
						if (j == 40870)
							j += 0;	//breakpoint
						int _j = s_mesh_2.sorted_nodes.get(j);
						ArrayList<Integer> faces_j = f_mesh_2.getFaces(_j);
						for (int k = 0; k < faces_i.size(); k++){
							int f_i = faces_i.get(k);
							for (int l = 0; faces_j != null && l < faces_j.size(); l++){
								int f_j = faces_j.get(l);
								if (!(intersects_1.get(f_i) && intersects_2.get(f_j)) &&
										GeometryFunctions.intersects(faces_1.get(f_i), faces_2.get(f_j))){
									intersects_1.set(f_i, true);
									intersects_2.set(f_j, true);
									}
								}
							}
						}
					j++;
					done = true;
					}
				
				}
				
			if (progress != null){
				progress.update(start + i);
				}
			}
		
		ArrayList<Integer> indices_1 = new ArrayList<Integer>();
		for (int i = 0; i < intersects_1.size(); i++){
			if (intersects_1.get(i)){
				indices_1.add(i);
				//MeshFace3D face = mesh_1.getFace(i);
				//indices_1.add(face.A);
				//indices_1.add(face.B);
				//indices_1.add(face.C);
				}
			}
		ArrayList<Integer> indices_2 = new ArrayList<Integer>();
		for (int i = 0; i < intersects_2.size(); i++){
			if (intersects_2.get(i)){
				indices_2.add(i);
				//MeshFace3D face = mesh_2.getFace(i);
				//indices_2.add(face.A);
				//indices_2.add(face.B);
				//indices_2.add(face.C);
				}
			}
		
		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
		result.add(indices_1);
		result.add(indices_2);
		
		return result;
		
	}
	
	/****************************************************************
	 * Returns a list of face indices indicating which faces intersect within {@code mesh}.
	 * If there are no such faces, returns an empty list.
	 * 
	 * @param mesh
	 * @param search_max Maximum search radius; should be similar to largest face size; larger takes longer
	 * @return
	 */
	public static ArrayList<Integer> getSelfIntersections(Mesh3D mesh, float search_max){
		return getSelfIntersectionsBlocking(mesh, search_max, null);
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<Integer> getSelfIntersections(final Mesh3D mesh, final float search_max, final ProgressUpdater progress){
		return (ArrayList<Integer>)Worker.post(new Job(){
			@Override
			public ArrayList<Integer> run(){
				return getSelfIntersectionsBlocking(mesh, search_max, progress);
			}
		});
	}
	
	/****************************************************************
	 * Returns a list of face indices indicating which faces intersect within {@code mesh}.
	 * If there are no such faces, returns an empty list.
	 * 
	 * @param mesh
	 * @param search_max Maximum search radius; should be similar to largest face size; larger takes longer
	 * @param progress
	 * @return
	 */
	protected static ArrayList<Integer> getSelfIntersectionsBlocking(Mesh3D mesh, float search_max, ProgressUpdater progress){
		
		ArrayList<Boolean> intersects = new ArrayList<Boolean>();
		ArrayList<Triangle3D> faces = new ArrayList<Triangle3D>();
		
		//Sorted mesh allows only finite boundaries to be searched
		SortedMesh s_mesh = new SortedMesh(mesh);
		ArrayList<Point3f> vertices = new ArrayList<Point3f>();
		FaceMesh f_mesh = new FaceMesh(mesh);
				
		for (int i = 0; i < mesh.getSize() - 1; i++){
			vertices.add(mesh.getVertex(s_mesh.sorted_nodes.get(i)));
			}
		
		int f = mesh.getFaceCount();
		
		for (int i = 0; i < f; i++){
			intersects.add(false);
			faces.add(mesh.getFaceTriangle(i));
			}
		
		long n = (long)f * (long)f - (long)f;
		
		int start = 0;
		if (progress != null){
			progress.getMinimum();
			progress.setMaximum(start + vertices.size());
			}
		
		// Compare sorted vertices
		for (int i = 0; i < vertices.size(); i++){
			boolean done = false;
			int _i = s_mesh.sorted_nodes.get(i);
			// List of faces associated with vertex i
			ArrayList<Integer> faces_i = f_mesh.getFaces(_i);
			for (int j = i + 1; faces_i != null && !done && j < vertices.size(); j++){
				// Find first vertex that is close enough to consider
				// and iterate through all remaining such vertices 
				while (j < vertices.size() &&
					   Math.abs(vertices.get(j).x - vertices.get(i).x) < search_max){
					if (Math.abs(vertices.get(j).y - vertices.get(i).y) < search_max &&
						Math.abs(vertices.get(j).z - vertices.get(i).z) < search_max){
						
						// This vertex is close enough; get list of associated faces and compare each
						int _j = s_mesh.sorted_nodes.get(j);
						ArrayList<Integer> faces_j = f_mesh.getFaces(_j);
						for (int k = 0; k < faces_i.size(); k++){
							int f_i = faces_i.get(k);
							for (int l = 0; faces_j != null && l < faces_j.size(); l++){
								int f_j = faces_j.get(l);
								if (f_i != f_j &&
										!(intersects.get(f_i) && intersects.get(f_j)) &&
										!mesh.getFace(f_i).isAdjacent(mesh.getFace(f_j)) &&
										GeometryFunctions.intersects(faces.get(f_i), faces.get(f_j))){
									intersects.set(f_i, true);
									intersects.set(f_j, true);
									}
								}
							}
						}
					j++;
					done = true;
					}
				}
				
			if (progress != null){
				progress.update(start + i);
				}
			}
		
		ArrayList<Integer> indices = new ArrayList<Integer>();
		
		for (int i = 0; i < intersects.size(); i++){
			if (intersects.get(i))
				indices.add(i);
			}
		
		return indices;
	}
	
	/******************************************************
	 * Searches all edges and returns the maximum length
	 * 
	 * @return
	 */
	public static float getMaximumEdgeLength(Mesh3D mesh){
		
		MeshEdgeSet edges = new MeshEdgeSet(mesh);
		ArrayList<Point3f> vertices = mesh.getVertices();
		
		ArrayList<MeshEdge> edge_list = edges.getEdges();
		float max = 0;
		
		for (int i = 0; i < edge_list.size(); i++){
			MeshEdge edge = edge_list.get(i);
			float dist = vertices.get(edge.node1).distance(vertices.get(edge.node2));
			max = Math.max(dist, max);
			}
		
		return max;
	}
	
	/******************************************************
	 * Searches all edges and returns the minimum length
	 * 
	 * @return
	 */
	public static float getMinimumEdgeLength(Mesh3D mesh){
		
		MeshEdgeSet edges = new MeshEdgeSet(mesh);
		ArrayList<Point3f> vertices = mesh.getVertices();
		
		ArrayList<MeshEdge> edge_list = edges.getEdges();
		float min = Float.MAX_VALUE;
		
		for (int i = 0; i < edge_list.size(); i++){
			MeshEdge edge = edge_list.get(i);
			float dist = vertices.get(edge.node1).distance(vertices.get(edge.node2));
			min = Math.min(dist, min);
			}
		
		return min;
	}
	
	/********************************************
	 * Finds the point from the first face in the nodes list which intersects the line
	 * segment defined by <code>p</code> and </code>v</code>. Returns <code>null</code> if no
	 * intersection exists.
	 * 
	 * @status approved
	 * @param mesh
	 * @param p
	 * @param v
	 * @return
	 */
	public static Point3f getIntersectionPoint(Mesh3D mesh, Point3f p, Vector3f v){
		Box3D bounds = mesh.getBoundBox();
		Point3f p2 = new Point3f(p);
		p2.add(v);
		if (!bounds.contains(p) && ! bounds.contains(p2)) return null;
		for (int i = 0; i < mesh.f; i++){
			Triangle3D tri = mesh.getFaceTriangle(i);
			Point3f int_pt = GeometryFunctions.getIntersectionPoint3D(p, v, tri);
			if (int_pt != null) return int_pt;
			}
		return null;
	}
	
	/************************************************
	 * Computes the discrete Laplacian for each vertex in <code>mesh</mesh>.
	 * 
	 * Adapted from matlab script; see
	 * 
	 * <p>Oostendorp, Oosterom & Huiskamp (1989),
	 * Interpolation on a triangulated 3D surface.
	 * Journal of Computational Physics, 80: 331-343
	 * </p>
	 * 
	 * @status Not implemented.
	 * @param mesh
	 * @return vertex-wise Laplacian values
	 */
	public static ArrayList<Double> getLaplacian(Mesh3D mesh){
		
		
		
		
		return null;
	}
	
	/***********************************************
	 * Cuts <code>mesh</code> with <code>plane</code>, creating new faces along the cut seam. Returns
	 * two meshes (if they exist) for above and below, respectively.
	 * 
	 * @param mesh
	 * @param plane
	 * @param as_submeshes
	 * @return
	 */
	
	public static ArrayList<Mesh3D> cutMeshWithPlane3(Mesh3D mesh, Plane3D plane) throws MeshFunctionException{
		
		int ABOVE = 1, BELOW = -1, CONTAINED = 0;
		
		//label nodes as above or below plane
		ArrayList<MguiNumber> mask = maskMeshWithPlane(mesh, plane, ABOVE, BELOW, CONTAINED);
		ArrayList<Mesh3D> meshes = new ArrayList<Mesh3D>();
		
		//subdivide mesh into two parts such that each face has at least one vertex which is above (or below) the plane
		meshes.add(MeshFunctions.getSubMesh(mesh, mask, ABOVE, true, true, null, null));
		meshes.add(MeshFunctions.getSubMesh(mesh, mask, BELOW, true, true, null, null)); 
		
		TreeSet<Integer> remove_nodes = new TreeSet<Integer>();
		TreeSet<Integer> remove_faces = new TreeSet<Integer>();
		
		ArrayList<Point3f> add_nodes = new ArrayList<Point3f>();
		HashMap<Integer, Integer> mid_nodes = new HashMap<Integer, Integer>();
		ArrayList<MeshFace3D> add_faces = new ArrayList<MeshFace3D>();
		
		
		//for each part:
		int above = 1;
		for (int m = 0; m < 2; m++){
			Mesh3D this_mesh = meshes.get(m);
			boolean[] faces_changed = new boolean[this_mesh.f];
			MeshEdgeSet edge_set = new MeshEdgeSet(this_mesh);
			int add_index = this_mesh.n;
			if (this_mesh != null){
				//for each face that intersects the plane:
				for (int i = 0; i < this_mesh.f; i++){
					if (!faces_changed[i]){
						Triangle3D tri = this_mesh.getFaceTriangle(i);
						if (GeometryFunctions.intersects(tri, plane)){
							Mesh3D.MeshFace3D face = this_mesh.getFace(i);
							
							//determine which vertices are opposite the plane
							boolean[] is_opp = new boolean[3];
							int opp_count = 0;
							for (int j = 0; j < 3; j++){
								is_opp[j] = (above == GeometryFunctions.compareToPlane(tri.getVertex(j), plane));
								if (is_opp[j]) opp_count++;
								}
							
							if (opp_count == 1 || opp_count == 2){
								
								//if two vertices are on the opposite side of the plane:
								if (opp_count == 2){
									//a. remove vertices and face
									//Point3f p0 = null, p1 = null, p2 = null;
									int keep; //, opp1 = -1, opp2 = -1;
									int added_node_count = 0;
									int[] opps = new int[2];
									if (is_opp[0]){
										opps[0] = face.A;
										if (is_opp[1]){
											opps[1] = face.B;
											keep = face.C;
											//p0 = this_mesh.getNode(face.C);
										}else{
											opps[1] = face.C;
											keep = face.B;
											//p0 = this_mesh.getNode(face.B);
											}
									}else{
										//p0 = this_mesh.getNode(face.A);
										keep = face.A;
										opps[0] = face.B;
										opps[1] = face.C;
										}
									
									if (keep < 0 || opps[0] < 0 || opps[1] < 0){
										int a = 0;	//debug breakpoint
										}
									
									Point3f V_keep = this_mesh.getVertex(keep);
									Point3f[] V_opp = new Point3f[2];
									V_opp[0] = this_mesh.getVertex(opps[0]);
									V_opp[1] = this_mesh.getVertex(opps[1]);
									
									//b. remove two adjacent faces if they exist
									int[] edges = new int[2];
									
									for (int e = 0; e < 2; e++){
										edges[e] = edge_set.searchEdge(new MeshEdge(0, 0, keep, opps[e]));
										if (edges[e] < 0)
											throw new MeshFunctionException("MeshFunctions.cutMeshWithPlane: Edge [" + keep + ", " + opps[i] + "] not found!");
										
										MeshTriangle T_adj1 = edge_set.getOppositeTri(edges[e], i);
										boolean has_adj = (T_adj1 != null);
										
										if (has_adj){
											int opp_tri_idx = edge_set.getOppositeTriIndex(edges[e], i);
											//shouldn't happen
											if (opp_tri_idx < 0){
												//debug breakpoint
												throw new MeshFunctionException("MeshFunctions.cutMeshWithPlane: opposite tri index not found (?!)");
												}
											int opp_face_idx = edge_set.triangles.get(opp_tri_idx).faceIndex;
											int adj_idx = edge_set.getOppositeNode(edges[e], opp_tri_idx);
											Point3f V_adj = this_mesh.getVertex(adj_idx);
											
											//remove adjacent face
											//remove_faces.add(opp_face_idx);
											faces_changed[opp_face_idx] = true;
											
											//add new node
											Point3f V_add = GeometryFunctions.getIntersectionPoint3D(V_opp[e], V_adj, plane);
											if (V_add == null){
												//debug breakpoint
												throw new MeshFunctionException("MeshFunctions.cutMeshWithPlane: no intersection point found (?!)");
												}
											
											add_nodes.add(V_add);
											added_node_count++;
											
											//add two new faces to replace adjacent face
											//[V_add    V_adj    V_opp ]
											//[V_opp    V_adj    V_keep]
											add_faces.add(new MeshFace3D(add_index + added_node_count, adj_idx, opps[e]));
											add_faces.add(new MeshFace3D(opps[e], adj_idx, keep));
											
											}
										}
									
									//keep track of added nodes
									add_index += added_node_count;
									
									//c. Move V_opp1 and V_opp2
									V_opp[0] = GeometryFunctions.getIntersectionPoint3D(V_keep, V_opp[0], plane);
									if (V_opp[0] != null) this_mesh.setVertex(opps[0], V_opp[0]);
									
									V_opp[1] = GeometryFunctions.getIntersectionPoint3D(V_opp[1], V_keep, plane);
									if (V_opp[1] != null) this_mesh.setVertex(opps[1], V_opp[1]);
									
								//if one vertex is on the opposite side of the plane:
								}else{
									/*
									int keep1 = -1, keep2 = -1;
									int added_node_count = 0;
									
									//note order of nodes is important for normal calculation
									int opp = -1;
									if (is_opp[0]){
										opp = face.A;
										keep1 = face.B;
										keep2 = face.C;
										}
									
									if (is_opp[1]){
										opp = face.B;
										keep1 = face.C;
										keep2 = face.A;
										}
									
									if (is_opp[2]){
										opp = face.C;
										keep1 = face.A;
										keep2 = face.B;
										}
									
									if (opp < 0){
										int a = 0;		//debug breakpoint
										}
									
									Point3f V_opp = this_mesh.getNode(opp);
									Point3f V_keep1 = this_mesh.getNode(keep1);
									Point3f V_keep2 = this_mesh.getNode(keep2);
									
									//a. remove vertex V_opp, face F_i as well, as the adjacent face F_adj (if it exists)
									int e = edge_set.searchEdge(new MeshEdge(0, 0, keep1, keep2));
									if (e < 0)
										throw new MeshFunctionException("MeshFunctions.cutMeshWithPlane: Edge [" + keep1 + ", " + keep2 + "] not found!");
									
									MeshTriangle T_adj = edge_set.getOppositeTri(e, i);
									boolean has_adj = (T_adj != null);
									int V_adj = -1;
									
									if (has_adj){
										int opp_tri_idx = edge_set.getOppositeTriIndex(e, i);
										//shouldn't happen
										if (opp_tri_idx < 0){
											//debug breakpoint
											throw new MeshFunctionException("MeshFunctions.cutMeshWithPlane: opposite tri index not found (?!)");
											}
										int opp_face_idx = edge_set.triangles.get(opp_tri_idx).faceIndex;
										V_adj = edge_set.getOppositeNode(e, opp_tri_idx);
										//remove adjacent face
										remove_faces.add(opp_face_idx);
										faces_changed[opp_face_idx] = true;
										}
									
									//remove opposite vertex and this face
									remove_nodes.add(opp);
									faces_changed[opp] = true;
									remove_faces.add(i);
									
									//b. add a vertex V_mid at middle of V_keep1-V_keep2 edge
									Point3f V_mid = null;	
									
									//check if mid-point has already been added
									Integer mid_idx = mid_nodes.get(e);
									if (mid_idx == null) {
										V_mid = GeometryFunctions.getMidPt(V_keep1, V_keep2);
										add_nodes.add(V_mid);						
										mid_nodes.put(e, add_nodes.size() - 1);		//only add one midpoint per edge 
										mid_idx = add_index;
										added_node_count++;
									}else{
										V_mid = add_nodes.get(mid_idx);
										}
												
									//c. create two vertices V_new1, V_new2 on the plane along the edge line
									//Vector3f v = new Vector3f(V_keep1);
									//v.sub(V_opp);
									Point3f V_new1 = GeometryFunctions.getIntersectionPoint3D(V_opp, V_keep1, plane);
									if (V_new1 == null)
										throw new MeshFunctionException("MeshFunctions.cutMeshWithPlane: Edge [" + opp + ", " + keep1 + "] does not cross plane!");
									add_nodes.add(V_new1);
									added_node_count++;
									//v = new Vector3f(V_keep2);
									//v.sub(V_opp);
									Point3f V_new2 = GeometryFunctions.getIntersectionPoint3D(V_opp, V_keep2, plane);
									if (V_new2 == null)
										throw new MeshFunctionException("MeshFunctions.cutMeshWithPlane: Edge [" + opp + ", " + keep1 + "] does not cross plane!");
									add_nodes.add(V_new2);
									added_node_count++;
									
									//d. create two faces in place of the adjacent face (if it exists), connected to V_adj:
									//	 [V_keep1  V_adj    V_mid  ]
									//	 [V_mid    V_adj    V_keep2]
									if (has_adj){
										add_faces.add(this_mesh.new MeshFace3D(keep1, V_adj, mid_idx));
										add_faces.add(this_mesh.new MeshFace3D(mid_idx, V_adj, keep2));
										}
									
									//e. create three faces connecting added vertices:
									//	 [V_new1   V_keep1  V_mid  ]
									//	 [V_new1   V_mid    V_new2 ]
									//	 [V_new2   V_mid    V_keep2]
									int add1 = added_node_count - 2;
									int add2 = added_node_count - 1;
									add_faces.add(this_mesh.new MeshFace3D(add_index + add1, keep1, mid_idx));
									add_faces.add(this_mesh.new MeshFace3D(add_index + add1, mid_idx, add_index + add2));
									add_faces.add(this_mesh.new MeshFace3D(add_index + add2, mid_idx, keep2));
									
									//keep track of index position
									add_index += added_node_count;
									*/
									}
								}
							}
						}
					}
				}
			
			if (add_nodes.size() > 0){
				//first add new edges and faces
				this_mesh.addVertices(add_nodes);
				this_mesh.addFaces(add_faces);
				
				//now remove edges and faces
				this_mesh.removeVertices(remove_nodes);
				//this_mesh.removeFaces(remove_faces);
				}
			
			above = -1;
			}
		
		return meshes;
	}
	
	/***********************************************
	 * Cuts <code>mesh</code> with <code>plane</code>, creating new faces along the cut seam. Returns
	 * two meshes (if they exist) for above and below, respectively.
	 * 
	 * @param mesh
	 * @param plane
	 * @param as_submeshes
	 * @return
	 */
	
	public static ArrayList<Mesh3D> cutMeshWithPlane(Mesh3D mesh, Plane3D plane, 
													 InterfaceProgressBar progress_bar) throws MeshFunctionException{
		
		int ABOVE = 1, BELOW = -1, CONTAINED = 0;
		
		//label nodes as above or below plane
		ArrayList<MguiNumber> mask = maskMeshWithPlane(mesh, plane, ABOVE, BELOW, CONTAINED);
		ArrayList<Mesh3D> meshes = new ArrayList<Mesh3D>();
		
		//subdivide mesh into two parts such that each face has at least one vertex which is above (or below) the plane
		meshes.add(MeshFunctions.getSubMesh(mesh, mask, ABOVE, true, true, null, null));
		meshes.add(MeshFunctions.getSubMesh(mesh, mask, BELOW, true, true, null, null));
		//Mesh3D debug_mesh = new Mesh3D();
		
		int total_f = 0, p = 0;
		if (meshes.get(0) != null) total_f += meshes.get(0).f;
		if (meshes.get(1) != null) total_f += meshes.get(1).f;
		
		if (progress_bar != null){
			progress_bar.progressBar.setMinimum(0);
			progress_bar.progressBar.setMaximum(total_f);
			progress_bar.setValue(0);
			}
		
		//for each part:
		int position = ABOVE;
		for (int m = 0; m < 2; m++){
			Mesh3D this_mesh = meshes.get(m);
			
			if (this_mesh != null){
				
				TreeSet<Integer> remove_nodes = new TreeSet<Integer>();
				TreeSet<Integer> remove_faces = new TreeSet<Integer>();
				
				ArrayList<Point3f> add_nodes = new ArrayList<Point3f>();
				ArrayList<MeshFace3D> add_faces = new ArrayList<MeshFace3D>();
				
				HashMap<Integer, Integer> mid_nodes = new HashMap<Integer, Integer>();
				MeshEdgeSet edge_set = new MeshEdgeSet(this_mesh);
				int add_index = this_mesh.n;
				
				
				//for each face that intersects the plane:
				for (int i = 0; i < this_mesh.f; i++){
					Triangle3D tri = this_mesh.getFaceTriangle(i);
					if (GeometryFunctions.intersects(tri, plane)){
						Mesh3D.MeshFace3D face = this_mesh.getFace(i);
						
						//determine which vertices are opposite the plane
						boolean[] is_opp = new boolean[3];
						int opp_count = 0;
						
						for (int k = 0; k < 3; k++){
							is_opp[k] = (position != GeometryFunctions.compareToPlane(tri.getVertex(k), plane));
							if (is_opp[k]) opp_count++;
							}
						
						if (opp_count == 1 || opp_count == 2){
							int added_node_count = 0;
							
							//Case 1: if two vertices are on the opposite side of the plane:
							if (opp_count == 2){
								//a. remove vertices and face
								int keep;
								int[] opps = new int[2];
								if (is_opp[0]){
									
									if (is_opp[1]){
										opps[0] = face.A;
										opps[1] = face.B;
										keep = face.C;
									}else{
										opps[0] = face.C;
										opps[1] = face.A;
										keep = face.B;
										}
								}else{
									keep = face.A;
									opps[0] = face.B;
									opps[1] = face.C;
									}
								
								if (keep < 0 || opps[0] < 0 || opps[1] < 0){
									int a = 0;	//debug breakpoint
									}
								
								remove_nodes.add(opps[0]);
								remove_nodes.add(opps[1]);
								//remove_faces.add(i);
								
								//add two new nodes and face
								Point3f V_keep = this_mesh.getVertex(keep);
								Point3f[] V_opp = new Point3f[2];
								Point3f[] V_add = new Point3f[2];
								Integer[] mid_idx = new Integer[2];
								
								//add intersection nodes; use existing nodes if created by preceding iteration
								for (int k = 0; k < 2; k++){
									V_opp[k] = this_mesh.getVertex(opps[k]);
									int e = edge_set.searchEdge(new MeshEdge(0, 0, opps[k], keep));
									mid_idx[k] = mid_nodes.get(e);
									if (mid_idx[k] == null) {
										V_add[k] = GeometryFunctions.getIntersectionPoint3D(V_keep, V_opp[k], plane);
										add_nodes.add(V_add[k]);
										mid_idx[k] = add_index + added_node_count;
										mid_nodes.put(e, mid_idx[k]);		//only add one midpoint per edge 
										added_node_count++;
									}else{
										//mid_idx[k] = add_index + added_node_count;
										}
									}
								
								add_faces.add(new MeshFace3D(mid_idx[1], keep, mid_idx[0]));
								add_index += added_node_count;		//keep track of added nodes
								
							//Case 2: if one vertex is on the opposite side of the plane:
							}else{
								
								int keep1 = -1, keep2 = -1, opp = -1;
								//note order of nodes is important for normal calculation
								if (is_opp[0]){
									opp = face.A;
									keep1 = face.B;
									keep2 = face.C;
									}
								if (is_opp[1]){
									opp = face.B;
									keep1 = face.C;
									keep2 = face.A;
									}
								if (is_opp[2]){
									opp = face.C;
									keep1 = face.A;
									keep2 = face.B;
									}
								if (opp < 0){
									throw new MeshFunctionException("MeshFunctions.cutMeshWithPlane: opp < 0, and shouldn't be!");
									//int a = 0;		//debug breakpoint
									}
								
								//a. Remove T_adj if it exists
								int e = edge_set.searchEdge(new MeshEdge(0, 0, keep1, keep2));
								MeshTriangle T_adj = edge_set.getOppositeTri(e, i);
								Integer adj_idx = null;
								if (T_adj != null && !remove_faces.contains(T_adj.faceIndex)){
									remove_faces.add(T_adj.faceIndex);
									adj_idx = edge_set.getOppositeNode(e, i);
									//debug_mesh.addNode(this_mesh.getNode(adj_idx));
									if (adj_idx == null)
										throw new MeshFunctionException("MeshFunctions.cutMeshWithPlane: adj_idx is null, and shouldn't be!");
									if (adj_idx == keep1){
										adj_idx = edge_set.getOppositeNode(e, i);
										throw new MeshFunctionException("MeshFunctions.cutMeshWithPlane: adj_idx (" + adj_idx + ") = keep1!");
										}
									if (adj_idx == keep2)
										throw new MeshFunctionException("MeshFunctions.cutMeshWithPlane: adj_idx (" + adj_idx + ") = keep2!");
									}
								
								//b. Remove node V_opp (will also remove its face)
								remove_nodes.add(opp);
								
								//c. add nodes V_add1, V_add2, V_add3
								Point3f V_keep1 = this_mesh.getVertex(keep1);
								Point3f V_keep2 = this_mesh.getVertex(keep2);
								Point3f V_opp = this_mesh.getVertex(opp);
								
								Integer add3_idx = mid_nodes.get(e);
								Point3f V_add3 = null;
								if (add3_idx == null){
									V_add3 = GeometryFunctions.getMidPt(V_keep1, V_keep2);
									add3_idx = add_index;
									add_nodes.add(V_add3);
									mid_nodes.put(e, add3_idx);
									added_node_count++;
									}
								
								e = edge_set.searchEdge(new MeshEdge(0, 0, opp, keep1));
								Integer add1_idx = mid_nodes.get(e);
								Point3f V_add1 = null;
								if (add1_idx == null){
									V_add1 = GeometryFunctions.getIntersectionPoint3D(V_keep1, V_opp, plane);
									add1_idx = add_index + added_node_count;
									add_nodes.add(V_add1);
									mid_nodes.put(e, add1_idx);
									added_node_count++;
									}
								
								e = edge_set.searchEdge(new MeshEdge(0, 0, opp, keep2));
								Integer add2_idx = mid_nodes.get(e);
								Point3f V_add2 = null;
								if (add2_idx == null){
									V_add2 = GeometryFunctions.getIntersectionPoint3D(V_keep2, V_opp, plane);
									add2_idx = add_index + added_node_count;
									add_nodes.add(V_add2);
									mid_nodes.put(e, add2_idx);
									added_node_count++;
									}
								
								add_index += added_node_count;		//keep track of added vertices
								
								//d. Create five faces (last two only if T_adj exists):
								//[ V_add1    V_keep1   V_add3  ]
								//[ V_add1    V_add3    V_add2  ]
								//[ V_add2    V_add3    V_keep2 ]
								//[ V_keep1   V_adj     V_add3  ]
								//[ V_add3    V_adj     V_keep2 ]
								add_faces.add(new MeshFace3D(add1_idx, keep1, add3_idx));
								add_faces.add(new MeshFace3D(add1_idx, add3_idx, add2_idx));
								add_faces.add(new MeshFace3D(add2_idx, add3_idx, keep2));
								if (adj_idx != null){
									add_faces.add(new MeshFace3D(keep1, adj_idx, add3_idx));
									add_faces.add(new MeshFace3D(add3_idx, adj_idx, keep2));
									if (keep1 == adj_idx || adj_idx == add3_idx || keep1 == add3_idx)
										throw new MeshFunctionException("MeshFunctions.cutMeshWithPlane: illegal face added [" + 
																		keep1 + ", " + adj_idx + ", " + add3_idx + "]!");
									if (keep2 == adj_idx || adj_idx == add3_idx || keep2 == add3_idx)
										throw new MeshFunctionException("MeshFunctions.cutMeshWithPlane: illegal face added [" + 
																		keep2 + ", " + adj_idx + ", " + add3_idx + "]!");
									}
								}
							}
						}
					progress_bar.update(p++);
					}
				
				//add new nodes and faces
				this_mesh.addVertices(add_nodes);
				this_mesh.addFaces(add_faces);
				
				//now remove nodes and faces
				this_mesh.removeFaces(remove_faces);
				this_mesh.removeVertices(remove_nodes);
					
				}
			
			position = BELOW;
			
			}
		return meshes;
	}
	
	/**********************************
	 * Return a set of line segments which represent the intersection of one mesh with another.
	 * 
	 * @param mesh1
	 * @param mesh2
	 * @param progress_bar
	 * @return
	 * @throws MeshFunctionException
	 */
	public static ArrayList<Polygon3D> getIntersectionSegments(Mesh3D mesh1, Mesh3D mesh2, 
			 												   InterfaceProgressBar progress_bar) throws MeshFunctionException{
		
		ArrayList<Polygon3D> polygons = new ArrayList<Polygon3D>();
		
		int total_f = mesh1.f * mesh2.f;
		
		if (progress_bar != null){
			progress_bar.progressBar.setMinimum(0);
			progress_bar.progressBar.setMaximum(total_f);
			progress_bar.setValue(0);
			}
		
		//must compare each face in mesh1 with each face in mesh2; luckily, most will be quickly rejected
		int k = 0;
		
		for (int i = 0; i < mesh1.f; i++){
			Triangle3D tri1 = mesh1.getFaceTriangle(i);
			for (int j = 0; j < mesh2.f; j++){
				Triangle3D tri2 = mesh2.getFaceTriangle(j);
				Point3f[] pts = GeometryFunctions.getIntersectionSegment(tri1, tri2);
				if (pts != null){
					Polygon3D poly = new Polygon3D();
					poly.addNode(pts[0]);
					poly.addNode(pts[1]);
					polygons.add(poly);
					}
				progress_bar.update(k++);
				}
			}
		
		return polygons;
	}

	public static Mesh3D getGlobeSphereMesh(Point3f center, float radius, int n_nodes){
		
		int n = (int)Math.round(Math.sqrt(n_nodes));
		Mesh3D mesh = new Mesh3D();
	
		//create nodes by varying angles theta and phi (azimuth and dip)
        for (int i = 0 ; i < n ; i++)
            for (int j = 0 ; j < n ; j++) {
                double u = i / (n - 1.0);
                double v = j / (n - 1.0);
                double theta = 2.0 * Math.PI * u;
                double phi = Math.PI * v - Math.PI/2.0;
                Point3f p = new Point3f((float)(Math.cos(theta) * Math.cos(phi)), 
				                		 (float)(Math.sin(theta) * Math.cos(phi)),
				                		 (float)Math.sin(phi));
                p.scale(radius);
                p.add(center);
                mesh.addVertex(p);
            	}
        
        //connect faces (each is two triangles)
        for (int i = 0 ; i < n ; i++)
            for (int j = 0 ; j < n ; j++) {
            	//get i and j as mesh indexes
            	int _i = i * n;
            	int k = i + 1;
            	if (k == n) k = 0;
            	k *= n;
            	int l = j + 1;
            	if (l == n) l = 0;
            	
            	mesh.addFace(_i + j, _i + l, k + l);
            	mesh.addFace(_i + j, k + j, k + l);
            	}
		        
        return mesh;

	}
	
	/**************************
	 * Determine which nodes are boundary nodes, return an array of booleans.
	 * @param mesh
	 * @return
	 */
	public static boolean[] getBoundaryNodes(Mesh3D mesh){
		boolean[] is_boundary = new boolean[mesh.n];
		
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		
		for (int i = 0; i < mesh.n; i++)
			is_boundary[i] = n_mesh.isBoundaryNode(i);
			
		return is_boundary;
		
	}
	
	public static ArrayList<Integer> getBoundaryNodeIndices(Mesh3D mesh){
		
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		ArrayList<Integer> b_nodes = new ArrayList<Integer>();
		
		for (int i = 0; i < mesh.n; i++)
			if (n_mesh.isBoundaryNode(i))
				b_nodes.add(i);
		
		return b_nodes;
	}
	
	//private member class to keep track of node indices for decimation
	static class IndexedNode{
		Point3f node;
		int index, newindex, finalindex;
		
		public IndexedNode(Point3f n, int i){
			node = n;
			index = i;
			}
	}
	
	/**************************************************
	 * Detects nodes corresponding to "jagged" edges; a jagged edge node is defined as one which has only
	 * <code>max_nbrs</code> neighbours with the same value as itself, and at least <code>min_nbrs</code> 
	 * neighbours which are the same value. Values are treated as integers. Such nodes are candidates for a 
	 * value change.
	 * 
	 * @param mesh Mesh to search
	 * @param regions Vertex-wise values
	 * @param min_nbrs The minimum # of neighbours with the same value which is different from a node's value
	 * @param max_brs The maximum # of neighbours with the same value as a node's value
	 * @return A list of indices for jagged edge vertices
	 */
	public static HashMap<Integer, Integer> getJaggedEdgeNodes(Mesh3D mesh, 
														ArrayList<MguiNumber> values, 
														int min_nbrs,
														int max_nbrs,
														boolean islands){
	
		HashMap<Integer, Integer> result = new HashMap<Integer, Integer>();
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		
		for (int i = 0; i < mesh.n; i++){
			int[] nbrs = n_mesh.getNeighbourhood(i).getNeighbourList();
			int value = (int)values.get(i).getValue();
			int same_count = 0;
			HashMap<Integer, Integer> counts = new HashMap<Integer, Integer>();
			
			//count neighbour values
			for (int j = 0; j < nbrs.length; j++){
				int thisVal = (int)values.get(nbrs[j]).getValue();
				if (thisVal == value){
					same_count++;
				}else{
					Integer c = counts.get(thisVal);
					if (c == null)
						counts.put(thisVal, 1);
					else
						counts.put(thisVal, c + 1);
					}
				}
			
			ArrayList<Integer> vals = new ArrayList<Integer>(counts.keySet());
			int max = 0, max_value = 0;
			for (int v = 0; v < vals.size(); v++){
				int count = counts.get(vals.get(v));
				if (count > max){
					max = count;
					max_value = vals.get(v);
					}
				}
			
			if (max > min_nbrs && same_count > 0 && same_count <= max_nbrs)
				result.put(i, max_value);
			else if (islands && same_count == 0)
				result.put(i, max_value);
			}

		return result;
	}
	
	/**********************************************************
	 * Expands <code>mesh</code> along its vertex normals by a distance of <code>expansion</code>. This
	 * is suitable for a convex mesh; for a concave mesh there is no guarantee that the resulting mesh
	 * will be valid (i.e., faces may become crossed).
	 * 
	 * @param mesh
	 * @param expansion
	 * @status approved
	 * @return the expanded mesh
	 */
	public static Mesh3D getMeshExpandedAlongNormals(Mesh3D mesh, float expansion){
		
		Mesh3D new_mesh = new Mesh3D(mesh);
		
		Point3f[] nodes = new Point3f[new_mesh.n];
		new_mesh.getVertices().toArray(nodes);
		int[] indices = new_mesh.faces;
		
		GeometryInfo gi = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);
        gi.setCoordinates(nodes);
        gi.setCoordinateIndices(indices);
        NormalGenerator ng = new NormalGenerator(Math.PI);
        ng.generateNormals(gi);
        Vector3f[] normals = gi.getNormals();
		
        for (int i = 0; i < normals.length; i++){
        	Vector3f v = new Vector3f(normals[i]);
        	v.normalize();
        	v.scale(expansion);
        	Point3f node = new_mesh.getVertex(i);
        	node.add(v);
        	new_mesh.setVertex(i, node);
        	}
        
		return new_mesh;
	}
	
	// Look-up tables for marching cubes
	static final int[] mcEdgeTable= new int[]{
			0x0  , 0x109, 0x203, 0x30a, 0x406, 0x50f, 0x605, 0x70c,
			0x80c, 0x905, 0xa0f, 0xb06, 0xc0a, 0xd03, 0xe09, 0xf00,
			0x190, 0x99 , 0x393, 0x29a, 0x596, 0x49f, 0x795, 0x69c,
			0x99c, 0x895, 0xb9f, 0xa96, 0xd9a, 0xc93, 0xf99, 0xe90,
			0x230, 0x339, 0x33 , 0x13a, 0x636, 0x73f, 0x435, 0x53c,
			0xa3c, 0xb35, 0x83f, 0x936, 0xe3a, 0xf33, 0xc39, 0xd30,
			0x3a0, 0x2a9, 0x1a3, 0xaa , 0x7a6, 0x6af, 0x5a5, 0x4ac,
			0xbac, 0xaa5, 0x9af, 0x8a6, 0xfaa, 0xea3, 0xda9, 0xca0,
			0x460, 0x569, 0x663, 0x76a, 0x66 , 0x16f, 0x265, 0x36c,
			0xc6c, 0xd65, 0xe6f, 0xf66, 0x86a, 0x963, 0xa69, 0xb60,
			0x5f0, 0x4f9, 0x7f3, 0x6fa, 0x1f6, 0xff , 0x3f5, 0x2fc,
			0xdfc, 0xcf5, 0xfff, 0xef6, 0x9fa, 0x8f3, 0xbf9, 0xaf0,
			0x650, 0x759, 0x453, 0x55a, 0x256, 0x35f, 0x55 , 0x15c,
			0xe5c, 0xf55, 0xc5f, 0xd56, 0xa5a, 0xb53, 0x859, 0x950,
			0x7c0, 0x6c9, 0x5c3, 0x4ca, 0x3c6, 0x2cf, 0x1c5, 0xcc ,
			0xfcc, 0xec5, 0xdcf, 0xcc6, 0xbca, 0xac3, 0x9c9, 0x8c0,
			0x8c0, 0x9c9, 0xac3, 0xbca, 0xcc6, 0xdcf, 0xec5, 0xfcc,
			0xcc , 0x1c5, 0x2cf, 0x3c6, 0x4ca, 0x5c3, 0x6c9, 0x7c0,
			0x950, 0x859, 0xb53, 0xa5a, 0xd56, 0xc5f, 0xf55, 0xe5c,
			0x15c, 0x55 , 0x35f, 0x256, 0x55a, 0x453, 0x759, 0x650,
			0xaf0, 0xbf9, 0x8f3, 0x9fa, 0xef6, 0xfff, 0xcf5, 0xdfc,
			0x2fc, 0x3f5, 0xff , 0x1f6, 0x6fa, 0x7f3, 0x4f9, 0x5f0,
			0xb60, 0xa69, 0x963, 0x86a, 0xf66, 0xe6f, 0xd65, 0xc6c,
			0x36c, 0x265, 0x16f, 0x66 , 0x76a, 0x663, 0x569, 0x460,
			0xca0, 0xda9, 0xea3, 0xfaa, 0x8a6, 0x9af, 0xaa5, 0xbac,
			0x4ac, 0x5a5, 0x6af, 0x7a6, 0xaa , 0x1a3, 0x2a9, 0x3a0,
			0xd30, 0xc39, 0xf33, 0xe3a, 0x936, 0x83f, 0xb35, 0xa3c,
			0x53c, 0x435, 0x73f, 0x636, 0x13a, 0x33 , 0x339, 0x230,
			0xe90, 0xf99, 0xc93, 0xd9a, 0xa96, 0xb9f, 0x895, 0x99c,
			0x69c, 0x795, 0x49f, 0x596, 0x29a, 0x393, 0x99 , 0x190,
			0xf00, 0xe09, 0xd03, 0xc0a, 0xb06, 0xa0f, 0x905, 0x80c,
			0x70c, 0x605, 0x50f, 0x406, 0x30a, 0x203, 0x109, 0x0   };
	
	static final int[][] mcTriTable = new int[][]
			{{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{0, 8, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{0, 1, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{1, 8, 3, 9, 8, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{1, 2, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{0, 8, 3, 1, 2, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{9, 2, 10, 0, 2, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{2, 8, 3, 2, 10, 8, 10, 9, 8, -1, -1, -1, -1, -1, -1, -1},
			{3, 11, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{0, 11, 2, 8, 11, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{1, 9, 0, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{1, 11, 2, 1, 9, 11, 9, 8, 11, -1, -1, -1, -1, -1, -1, -1},
			{3, 10, 1, 11, 10, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{0, 10, 1, 0, 8, 10, 8, 11, 10, -1, -1, -1, -1, -1, -1, -1},
			{3, 9, 0, 3, 11, 9, 11, 10, 9, -1, -1, -1, -1, -1, -1, -1},
			{9, 8, 10, 10, 8, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{4, 7, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{4, 3, 0, 7, 3, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{0, 1, 9, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{4, 1, 9, 4, 7, 1, 7, 3, 1, -1, -1, -1, -1, -1, -1, -1},
			{1, 2, 10, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{3, 4, 7, 3, 0, 4, 1, 2, 10, -1, -1, -1, -1, -1, -1, -1},
			{9, 2, 10, 9, 0, 2, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1},
			{2, 10, 9, 2, 9, 7, 2, 7, 3, 7, 9, 4, -1, -1, -1, -1},
			{8, 4, 7, 3, 11, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{11, 4, 7, 11, 2, 4, 2, 0, 4, -1, -1, -1, -1, -1, -1, -1},
			{9, 0, 1, 8, 4, 7, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1},
			{4, 7, 11, 9, 4, 11, 9, 11, 2, 9, 2, 1, -1, -1, -1, -1},
			{3, 10, 1, 3, 11, 10, 7, 8, 4, -1, -1, -1, -1, -1, -1, -1},
			{1, 11, 10, 1, 4, 11, 1, 0, 4, 7, 11, 4, -1, -1, -1, -1},
			{4, 7, 8, 9, 0, 11, 9, 11, 10, 11, 0, 3, -1, -1, -1, -1},
			{4, 7, 11, 4, 11, 9, 9, 11, 10, -1, -1, -1, -1, -1, -1, -1},
			{9, 5, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{9, 5, 4, 0, 8, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{0, 5, 4, 1, 5, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{8, 5, 4, 8, 3, 5, 3, 1, 5, -1, -1, -1, -1, -1, -1, -1},
			{1, 2, 10, 9, 5, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{3, 0, 8, 1, 2, 10, 4, 9, 5, -1, -1, -1, -1, -1, -1, -1},
			{5, 2, 10, 5, 4, 2, 4, 0, 2, -1, -1, -1, -1, -1, -1, -1},
			{2, 10, 5, 3, 2, 5, 3, 5, 4, 3, 4, 8, -1, -1, -1, -1},
			{9, 5, 4, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{0, 11, 2, 0, 8, 11, 4, 9, 5, -1, -1, -1, -1, -1, -1, -1},
			{0, 5, 4, 0, 1, 5, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1},
			{2, 1, 5, 2, 5, 8, 2, 8, 11, 4, 8, 5, -1, -1, -1, -1},
			{10, 3, 11, 10, 1, 3, 9, 5, 4, -1, -1, -1, -1, -1, -1, -1},
			{4, 9, 5, 0, 8, 1, 8, 10, 1, 8, 11, 10, -1, -1, -1, -1},
			{5, 4, 0, 5, 0, 11, 5, 11, 10, 11, 0, 3, -1, -1, -1, -1},
			{5, 4, 8, 5, 8, 10, 10, 8, 11, -1, -1, -1, -1, -1, -1, -1},
			{9, 7, 8, 5, 7, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{9, 3, 0, 9, 5, 3, 5, 7, 3, -1, -1, -1, -1, -1, -1, -1},
			{0, 7, 8, 0, 1, 7, 1, 5, 7, -1, -1, -1, -1, -1, -1, -1},
			{1, 5, 3, 3, 5, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{9, 7, 8, 9, 5, 7, 10, 1, 2, -1, -1, -1, -1, -1, -1, -1},
			{10, 1, 2, 9, 5, 0, 5, 3, 0, 5, 7, 3, -1, -1, -1, -1},
			{8, 0, 2, 8, 2, 5, 8, 5, 7, 10, 5, 2, -1, -1, -1, -1},
			{2, 10, 5, 2, 5, 3, 3, 5, 7, -1, -1, -1, -1, -1, -1, -1},
			{7, 9, 5, 7, 8, 9, 3, 11, 2, -1, -1, -1, -1, -1, -1, -1},
			{9, 5, 7, 9, 7, 2, 9, 2, 0, 2, 7, 11, -1, -1, -1, -1},
			{2, 3, 11, 0, 1, 8, 1, 7, 8, 1, 5, 7, -1, -1, -1, -1},
			{11, 2, 1, 11, 1, 7, 7, 1, 5, -1, -1, -1, -1, -1, -1, -1},
			{9, 5, 8, 8, 5, 7, 10, 1, 3, 10, 3, 11, -1, -1, -1, -1},
			{5, 7, 0, 5, 0, 9, 7, 11, 0, 1, 0, 10, 11, 10, 0, -1},
			{11, 10, 0, 11, 0, 3, 10, 5, 0, 8, 0, 7, 5, 7, 0, -1},
			{11, 10, 5, 7, 11, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{10, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{0, 8, 3, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{9, 0, 1, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{1, 8, 3, 1, 9, 8, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1},
			{1, 6, 5, 2, 6, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{1, 6, 5, 1, 2, 6, 3, 0, 8, -1, -1, -1, -1, -1, -1, -1},
			{9, 6, 5, 9, 0, 6, 0, 2, 6, -1, -1, -1, -1, -1, -1, -1},
			{5, 9, 8, 5, 8, 2, 5, 2, 6, 3, 2, 8, -1, -1, -1, -1},
			{2, 3, 11, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{11, 0, 8, 11, 2, 0, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1},
			{0, 1, 9, 2, 3, 11, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1},
			{5, 10, 6, 1, 9, 2, 9, 11, 2, 9, 8, 11, -1, -1, -1, -1},
			{6, 3, 11, 6, 5, 3, 5, 1, 3, -1, -1, -1, -1, -1, -1, -1},
			{0, 8, 11, 0, 11, 5, 0, 5, 1, 5, 11, 6, -1, -1, -1, -1},
			{3, 11, 6, 0, 3, 6, 0, 6, 5, 0, 5, 9, -1, -1, -1, -1},
			{6, 5, 9, 6, 9, 11, 11, 9, 8, -1, -1, -1, -1, -1, -1, -1},
			{5, 10, 6, 4, 7, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{4, 3, 0, 4, 7, 3, 6, 5, 10, -1, -1, -1, -1, -1, -1, -1},
			{1, 9, 0, 5, 10, 6, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1},
			{10, 6, 5, 1, 9, 7, 1, 7, 3, 7, 9, 4, -1, -1, -1, -1},
			{6, 1, 2, 6, 5, 1, 4, 7, 8, -1, -1, -1, -1, -1, -1, -1},
			{1, 2, 5, 5, 2, 6, 3, 0, 4, 3, 4, 7, -1, -1, -1, -1},
			{8, 4, 7, 9, 0, 5, 0, 6, 5, 0, 2, 6, -1, -1, -1, -1},
			{7, 3, 9, 7, 9, 4, 3, 2, 9, 5, 9, 6, 2, 6, 9, -1},
			{3, 11, 2, 7, 8, 4, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1},
			{5, 10, 6, 4, 7, 2, 4, 2, 0, 2, 7, 11, -1, -1, -1, -1},
			{0, 1, 9, 4, 7, 8, 2, 3, 11, 5, 10, 6, -1, -1, -1, -1},
			{9, 2, 1, 9, 11, 2, 9, 4, 11, 7, 11, 4, 5, 10, 6, -1},
			{8, 4, 7, 3, 11, 5, 3, 5, 1, 5, 11, 6, -1, -1, -1, -1},
			{5, 1, 11, 5, 11, 6, 1, 0, 11, 7, 11, 4, 0, 4, 11, -1},
			{0, 5, 9, 0, 6, 5, 0, 3, 6, 11, 6, 3, 8, 4, 7, -1},
			{6, 5, 9, 6, 9, 11, 4, 7, 9, 7, 11, 9, -1, -1, -1, -1},
			{10, 4, 9, 6, 4, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{4, 10, 6, 4, 9, 10, 0, 8, 3, -1, -1, -1, -1, -1, -1, -1},
			{10, 0, 1, 10, 6, 0, 6, 4, 0, -1, -1, -1, -1, -1, -1, -1},
			{8, 3, 1, 8, 1, 6, 8, 6, 4, 6, 1, 10, -1, -1, -1, -1},
			{1, 4, 9, 1, 2, 4, 2, 6, 4, -1, -1, -1, -1, -1, -1, -1},
			{3, 0, 8, 1, 2, 9, 2, 4, 9, 2, 6, 4, -1, -1, -1, -1},
			{0, 2, 4, 4, 2, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{8, 3, 2, 8, 2, 4, 4, 2, 6, -1, -1, -1, -1, -1, -1, -1},
			{10, 4, 9, 10, 6, 4, 11, 2, 3, -1, -1, -1, -1, -1, -1, -1},
			{0, 8, 2, 2, 8, 11, 4, 9, 10, 4, 10, 6, -1, -1, -1, -1},
			{3, 11, 2, 0, 1, 6, 0, 6, 4, 6, 1, 10, -1, -1, -1, -1},
			{6, 4, 1, 6, 1, 10, 4, 8, 1, 2, 1, 11, 8, 11, 1, -1},
			{9, 6, 4, 9, 3, 6, 9, 1, 3, 11, 6, 3, -1, -1, -1, -1},
			{8, 11, 1, 8, 1, 0, 11, 6, 1, 9, 1, 4, 6, 4, 1, -1},
			{3, 11, 6, 3, 6, 0, 0, 6, 4, -1, -1, -1, -1, -1, -1, -1},
			{6, 4, 8, 11, 6, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{7, 10, 6, 7, 8, 10, 8, 9, 10, -1, -1, -1, -1, -1, -1, -1},
			{0, 7, 3, 0, 10, 7, 0, 9, 10, 6, 7, 10, -1, -1, -1, -1},
			{10, 6, 7, 1, 10, 7, 1, 7, 8, 1, 8, 0, -1, -1, -1, -1},
			{10, 6, 7, 10, 7, 1, 1, 7, 3, -1, -1, -1, -1, -1, -1, -1},
			{1, 2, 6, 1, 6, 8, 1, 8, 9, 8, 6, 7, -1, -1, -1, -1},
			{2, 6, 9, 2, 9, 1, 6, 7, 9, 0, 9, 3, 7, 3, 9, -1},
			{7, 8, 0, 7, 0, 6, 6, 0, 2, -1, -1, -1, -1, -1, -1, -1},
			{7, 3, 2, 6, 7, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{2, 3, 11, 10, 6, 8, 10, 8, 9, 8, 6, 7, -1, -1, -1, -1},
			{2, 0, 7, 2, 7, 11, 0, 9, 7, 6, 7, 10, 9, 10, 7, -1},
			{1, 8, 0, 1, 7, 8, 1, 10, 7, 6, 7, 10, 2, 3, 11, -1},
			{11, 2, 1, 11, 1, 7, 10, 6, 1, 6, 7, 1, -1, -1, -1, -1},
			{8, 9, 6, 8, 6, 7, 9, 1, 6, 11, 6, 3, 1, 3, 6, -1},
			{0, 9, 1, 11, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{7, 8, 0, 7, 0, 6, 3, 11, 0, 11, 6, 0, -1, -1, -1, -1},
			{7, 11, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{7, 6, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{3, 0, 8, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{0, 1, 9, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{8, 1, 9, 8, 3, 1, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1},
			{10, 1, 2, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{1, 2, 10, 3, 0, 8, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1},
			{2, 9, 0, 2, 10, 9, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1},
			{6, 11, 7, 2, 10, 3, 10, 8, 3, 10, 9, 8, -1, -1, -1, -1},
			{7, 2, 3, 6, 2, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{7, 0, 8, 7, 6, 0, 6, 2, 0, -1, -1, -1, -1, -1, -1, -1},
			{2, 7, 6, 2, 3, 7, 0, 1, 9, -1, -1, -1, -1, -1, -1, -1},
			{1, 6, 2, 1, 8, 6, 1, 9, 8, 8, 7, 6, -1, -1, -1, -1},
			{10, 7, 6, 10, 1, 7, 1, 3, 7, -1, -1, -1, -1, -1, -1, -1},
			{10, 7, 6, 1, 7, 10, 1, 8, 7, 1, 0, 8, -1, -1, -1, -1},
			{0, 3, 7, 0, 7, 10, 0, 10, 9, 6, 10, 7, -1, -1, -1, -1},
			{7, 6, 10, 7, 10, 8, 8, 10, 9, -1, -1, -1, -1, -1, -1, -1},
			{6, 8, 4, 11, 8, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{3, 6, 11, 3, 0, 6, 0, 4, 6, -1, -1, -1, -1, -1, -1, -1},
			{8, 6, 11, 8, 4, 6, 9, 0, 1, -1, -1, -1, -1, -1, -1, -1},
			{9, 4, 6, 9, 6, 3, 9, 3, 1, 11, 3, 6, -1, -1, -1, -1},
			{6, 8, 4, 6, 11, 8, 2, 10, 1, -1, -1, -1, -1, -1, -1, -1},
			{1, 2, 10, 3, 0, 11, 0, 6, 11, 0, 4, 6, -1, -1, -1, -1},
			{4, 11, 8, 4, 6, 11, 0, 2, 9, 2, 10, 9, -1, -1, -1, -1},
			{10, 9, 3, 10, 3, 2, 9, 4, 3, 11, 3, 6, 4, 6, 3, -1},
			{8, 2, 3, 8, 4, 2, 4, 6, 2, -1, -1, -1, -1, -1, -1, -1},
			{0, 4, 2, 4, 6, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{1, 9, 0, 2, 3, 4, 2, 4, 6, 4, 3, 8, -1, -1, -1, -1},
			{1, 9, 4, 1, 4, 2, 2, 4, 6, -1, -1, -1, -1, -1, -1, -1},
			{8, 1, 3, 8, 6, 1, 8, 4, 6, 6, 10, 1, -1, -1, -1, -1},
			{10, 1, 0, 10, 0, 6, 6, 0, 4, -1, -1, -1, -1, -1, -1, -1},
			{4, 6, 3, 4, 3, 8, 6, 10, 3, 0, 3, 9, 10, 9, 3, -1},
			{10, 9, 4, 6, 10, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{4, 9, 5, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{0, 8, 3, 4, 9, 5, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1},
			{5, 0, 1, 5, 4, 0, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1},
			{11, 7, 6, 8, 3, 4, 3, 5, 4, 3, 1, 5, -1, -1, -1, -1},
			{9, 5, 4, 10, 1, 2, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1},
			{6, 11, 7, 1, 2, 10, 0, 8, 3, 4, 9, 5, -1, -1, -1, -1},
			{7, 6, 11, 5, 4, 10, 4, 2, 10, 4, 0, 2, -1, -1, -1, -1},
			{3, 4, 8, 3, 5, 4, 3, 2, 5, 10, 5, 2, 11, 7, 6, -1},
			{7, 2, 3, 7, 6, 2, 5, 4, 9, -1, -1, -1, -1, -1, -1, -1},
			{9, 5, 4, 0, 8, 6, 0, 6, 2, 6, 8, 7, -1, -1, -1, -1},
			{3, 6, 2, 3, 7, 6, 1, 5, 0, 5, 4, 0, -1, -1, -1, -1},
			{6, 2, 8, 6, 8, 7, 2, 1, 8, 4, 8, 5, 1, 5, 8, -1},
			{9, 5, 4, 10, 1, 6, 1, 7, 6, 1, 3, 7, -1, -1, -1, -1},
			{1, 6, 10, 1, 7, 6, 1, 0, 7, 8, 7, 0, 9, 5, 4, -1},
			{4, 0, 10, 4, 10, 5, 0, 3, 10, 6, 10, 7, 3, 7, 10, -1},
			{7, 6, 10, 7, 10, 8, 5, 4, 10, 4, 8, 10, -1, -1, -1, -1},
			{6, 9, 5, 6, 11, 9, 11, 8, 9, -1, -1, -1, -1, -1, -1, -1},
			{3, 6, 11, 0, 6, 3, 0, 5, 6, 0, 9, 5, -1, -1, -1, -1},
			{0, 11, 8, 0, 5, 11, 0, 1, 5, 5, 6, 11, -1, -1, -1, -1},
			{6, 11, 3, 6, 3, 5, 5, 3, 1, -1, -1, -1, -1, -1, -1, -1},
			{1, 2, 10, 9, 5, 11, 9, 11, 8, 11, 5, 6, -1, -1, -1, -1},
			{0, 11, 3, 0, 6, 11, 0, 9, 6, 5, 6, 9, 1, 2, 10, -1},
			{11, 8, 5, 11, 5, 6, 8, 0, 5, 10, 5, 2, 0, 2, 5, -1},
			{6, 11, 3, 6, 3, 5, 2, 10, 3, 10, 5, 3, -1, -1, -1, -1},
			{5, 8, 9, 5, 2, 8, 5, 6, 2, 3, 8, 2, -1, -1, -1, -1},
			{9, 5, 6, 9, 6, 0, 0, 6, 2, -1, -1, -1, -1, -1, -1, -1},
			{1, 5, 8, 1, 8, 0, 5, 6, 8, 3, 8, 2, 6, 2, 8, -1},
			{1, 5, 6, 2, 1, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{1, 3, 6, 1, 6, 10, 3, 8, 6, 5, 6, 9, 8, 9, 6, -1},
			{10, 1, 0, 10, 0, 6, 9, 5, 0, 5, 6, 0, -1, -1, -1, -1},
			{0, 3, 8, 5, 6, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{10, 5, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{11, 5, 10, 7, 5, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{11, 5, 10, 11, 7, 5, 8, 3, 0, -1, -1, -1, -1, -1, -1, -1},
			{5, 11, 7, 5, 10, 11, 1, 9, 0, -1, -1, -1, -1, -1, -1, -1},
			{10, 7, 5, 10, 11, 7, 9, 8, 1, 8, 3, 1, -1, -1, -1, -1},
			{11, 1, 2, 11, 7, 1, 7, 5, 1, -1, -1, -1, -1, -1, -1, -1},
			{0, 8, 3, 1, 2, 7, 1, 7, 5, 7, 2, 11, -1, -1, -1, -1},
			{9, 7, 5, 9, 2, 7, 9, 0, 2, 2, 11, 7, -1, -1, -1, -1},
			{7, 5, 2, 7, 2, 11, 5, 9, 2, 3, 2, 8, 9, 8, 2, -1},
			{2, 5, 10, 2, 3, 5, 3, 7, 5, -1, -1, -1, -1, -1, -1, -1},
			{8, 2, 0, 8, 5, 2, 8, 7, 5, 10, 2, 5, -1, -1, -1, -1},
			{9, 0, 1, 5, 10, 3, 5, 3, 7, 3, 10, 2, -1, -1, -1, -1},
			{9, 8, 2, 9, 2, 1, 8, 7, 2, 10, 2, 5, 7, 5, 2, -1},
			{1, 3, 5, 3, 7, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{0, 8, 7, 0, 7, 1, 1, 7, 5, -1, -1, -1, -1, -1, -1, -1},
			{9, 0, 3, 9, 3, 5, 5, 3, 7, -1, -1, -1, -1, -1, -1, -1},
			{9, 8, 7, 5, 9, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{5, 8, 4, 5, 10, 8, 10, 11, 8, -1, -1, -1, -1, -1, -1, -1},
			{5, 0, 4, 5, 11, 0, 5, 10, 11, 11, 3, 0, -1, -1, -1, -1},
			{0, 1, 9, 8, 4, 10, 8, 10, 11, 10, 4, 5, -1, -1, -1, -1},
			{10, 11, 4, 10, 4, 5, 11, 3, 4, 9, 4, 1, 3, 1, 4, -1},
			{2, 5, 1, 2, 8, 5, 2, 11, 8, 4, 5, 8, -1, -1, -1, -1},
			{0, 4, 11, 0, 11, 3, 4, 5, 11, 2, 11, 1, 5, 1, 11, -1},
			{0, 2, 5, 0, 5, 9, 2, 11, 5, 4, 5, 8, 11, 8, 5, -1},
			{9, 4, 5, 2, 11, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{2, 5, 10, 3, 5, 2, 3, 4, 5, 3, 8, 4, -1, -1, -1, -1},
			{5, 10, 2, 5, 2, 4, 4, 2, 0, -1, -1, -1, -1, -1, -1, -1},
			{3, 10, 2, 3, 5, 10, 3, 8, 5, 4, 5, 8, 0, 1, 9, -1},
			{5, 10, 2, 5, 2, 4, 1, 9, 2, 9, 4, 2, -1, -1, -1, -1},
			{8, 4, 5, 8, 5, 3, 3, 5, 1, -1, -1, -1, -1, -1, -1, -1},
			{0, 4, 5, 1, 0, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{8, 4, 5, 8, 5, 3, 9, 0, 5, 0, 3, 5, -1, -1, -1, -1},
			{9, 4, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{4, 11, 7, 4, 9, 11, 9, 10, 11, -1, -1, -1, -1, -1, -1, -1},
			{0, 8, 3, 4, 9, 7, 9, 11, 7, 9, 10, 11, -1, -1, -1, -1},
			{1, 10, 11, 1, 11, 4, 1, 4, 0, 7, 4, 11, -1, -1, -1, -1},
			{3, 1, 4, 3, 4, 8, 1, 10, 4, 7, 4, 11, 10, 11, 4, -1},
			{4, 11, 7, 9, 11, 4, 9, 2, 11, 9, 1, 2, -1, -1, -1, -1},
			{9, 7, 4, 9, 11, 7, 9, 1, 11, 2, 11, 1, 0, 8, 3, -1},
			{11, 7, 4, 11, 4, 2, 2, 4, 0, -1, -1, -1, -1, -1, -1, -1},
			{11, 7, 4, 11, 4, 2, 8, 3, 4, 3, 2, 4, -1, -1, -1, -1},
			{2, 9, 10, 2, 7, 9, 2, 3, 7, 7, 4, 9, -1, -1, -1, -1},
			{9, 10, 7, 9, 7, 4, 10, 2, 7, 8, 7, 0, 2, 0, 7, -1},
			{3, 7, 10, 3, 10, 2, 7, 4, 10, 1, 10, 0, 4, 0, 10, -1},
			{1, 10, 2, 8, 7, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{4, 9, 1, 4, 1, 7, 7, 1, 3, -1, -1, -1, -1, -1, -1, -1},
			{4, 9, 1, 4, 1, 7, 0, 8, 1, 8, 7, 1, -1, -1, -1, -1},
			{4, 0, 3, 7, 4, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{4, 8, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{9, 10, 8, 10, 11, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{3, 0, 9, 3, 9, 11, 11, 9, 10, -1, -1, -1, -1, -1, -1, -1},
			{0, 1, 10, 0, 10, 8, 8, 10, 11, -1, -1, -1, -1, -1, -1, -1},
			{3, 1, 10, 11, 3, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{1, 2, 11, 1, 11, 9, 9, 11, 8, -1, -1, -1, -1, -1, -1, -1},
			{3, 0, 9, 3, 9, 11, 1, 2, 9, 2, 11, 9, -1, -1, -1, -1},
			{0, 2, 11, 8, 0, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{3, 2, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{2, 3, 8, 2, 8, 10, 10, 8, 9, -1, -1, -1, -1, -1, -1, -1},
			{9, 10, 2, 0, 9, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{2, 3, 8, 2, 8, 10, 0, 1, 8, 1, 10, 8, -1, -1, -1, -1},
			{1, 10, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{1, 3, 8, 9, 1, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{0, 9, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{0, 3, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
			{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}};

	
	/********************************************************************
	 * Constructs an isosurface from {@code volume}; i.e., computes a mesh which represents the
	 * location where values in the 3D scalar field in {@code volume} are equal to
	 * {@code iso_level}.
	 * 
	 * @param volume
	 * @param iso_level
	 * @return
	 */
	public static Mesh3D getIsosurfaceFromVolume(Volume3DInt volume, String column, double iso_level){
	
		Mesh3D isomesh = new Mesh3D();
		Grid3D grid = volume.getGrid();
		int n_s = grid.getSizeS();
		int n_t = grid.getSizeT();
		int n_r = grid.getSizeR();
		Point3f[] gridcell = new Point3f[8];
		double[] values = new double[8];
		int ntriang = 0;
		ArrayList<Point3f> triangles = new ArrayList<Point3f>();
		
		// For each voxel,
		for (int i = 0; i < n_s-1; i++){
			for (int j = 0; j < n_t-1; j++){
				for (int k = 0; k < n_r-1; k++){
					// Get seven neighbours (s+1,t+1,r+1) forming a cell
					gridcell[0] = grid.getVoxelMidPoint(i,j+1,k);
					gridcell[1] = grid.getVoxelMidPoint(i+1,j+1,k);
					gridcell[2] = grid.getVoxelMidPoint(i+1,j,k);
					gridcell[3] = grid.getVoxelMidPoint(i,j,k);
					gridcell[4] = grid.getVoxelMidPoint(i,j+1,k+1);
					gridcell[5] = grid.getVoxelMidPoint(i+1,j+1,k+1);
					gridcell[6] = grid.getVoxelMidPoint(i+1,j,k+1);
					gridcell[7] = grid.getVoxelMidPoint(i,j,k+1);
					
					values[0] = volume.getDatumAtVoxel(column,i,j+1,k).getValue();
					values[1] = volume.getDatumAtVoxel(column,i+1,j+1,k).getValue();
					values[2] = volume.getDatumAtVoxel(column,i+1,j,k).getValue();
					values[3] = volume.getDatumAtVoxel(column,i,j,k).getValue();
					values[4] = volume.getDatumAtVoxel(column,i,j+1,k+1).getValue();
					values[5] = volume.getDatumAtVoxel(column,i+1,j+1,k+1).getValue();
					values[6] = volume.getDatumAtVoxel(column,i+1,j,k+1).getValue();
					values[7] = volume.getDatumAtVoxel(column,i,j,k+1).getValue();
					
					// Pass this cell to polygonizeCell()
					triangles.clear();
					ntriang = polygonizeCell(gridcell, values, triangles, iso_level);
					// If polygonizeCell > 0, add triangles to mesh
					for (int t = 0; t < ntriang; t++){
						int idx = isomesh.n;
						isomesh.addVertex(triangles.get(t*3));
						isomesh.addVertex(triangles.get(t*3+1));
						isomesh.addVertex(triangles.get(t*3+2));
						isomesh.addFace(idx,idx+1,idx+2);
						}
					}
				}
			}
		
//		if (!validateSurface(isomesh)){
//			InterfaceSession.log("MeshFunctions.getIsosurfaceFromVolume: Mesh may not be valid.", 
//					LoggingType.Warnings);
//			}
		
		if (!removeDuplicateNodes(isomesh)) {
			InterfaceSession.log("MeshFunctions.getIsosurfaceFromVolume: Could not remove duplicate vertices.", 
					LoggingType.Warnings);
			}
		
		return isomesh;
		
	}
	
	/**********************
	 * Uses the marching cubes lookup tables to find triangular faces intersecting the given
	 * grid cell at the given isolevel
	 * 
	 * @param grid
	 * @param values
	 * @return
	 */
	protected static int polygonizeCell(Point3f[] gridcell, double[] values, ArrayList<Point3f> triangles, double isolevel){
	   
		int cubeindex;
		Point3f[] vertlist = new Point3f[12];
		
		/*
	      Determine the index into the edge table which
	      tells us which vertices are inside of the surface
	   */
	   cubeindex = 0;
	   if (values[0] < isolevel) cubeindex |= 1;
	   if (values[1] < isolevel) cubeindex |= 2;
	   if (values[2] < isolevel) cubeindex |= 4;
	   if (values[3] < isolevel) cubeindex |= 8;
	   if (values[4] < isolevel) cubeindex |= 16;
	   if (values[5] < isolevel) cubeindex |= 32;
	   if (values[6] < isolevel) cubeindex |= 64;
	   if (values[7] < isolevel) cubeindex |= 128;

	   /* Cube is entirely in/out of the surface */
	   if (mcEdgeTable[cubeindex] == 0)
	      return 0;

	   /* Find the vertices where the surface intersects the cube */
	   if ((mcEdgeTable[cubeindex] & 1) == 1){
		  vertlist[0] = VertexInterp(isolevel,gridcell[0],gridcell[1],values[0],values[1]);
	   		}
	   if ((mcEdgeTable[cubeindex] & 2) == 2){
	      vertlist[1] = VertexInterp(isolevel,gridcell[1],gridcell[2],values[1],values[2]);
	   		}
	   if ((mcEdgeTable[cubeindex] & 4) == 4){
	      vertlist[2] = VertexInterp(isolevel,gridcell[2],gridcell[3],values[2],values[3]);
  			}
	   if ((mcEdgeTable[cubeindex] & 8) == 8){
	      vertlist[3] = VertexInterp(isolevel,gridcell[3],gridcell[0],values[3],values[0]);
  			}
	   if ((mcEdgeTable[cubeindex] & 16) == 16){
	      vertlist[4] = VertexInterp(isolevel,gridcell[4],gridcell[5],values[4],values[5]);
  			}
	   if ((mcEdgeTable[cubeindex] & 32) == 32){
	      vertlist[5] = VertexInterp(isolevel,gridcell[5],gridcell[6],values[5],values[6]);
  			}
	   if ((mcEdgeTable[cubeindex] & 64) == 64){
	      vertlist[6] = VertexInterp(isolevel,gridcell[6],gridcell[7],values[6],values[7]);
  			}
	   if ((mcEdgeTable[cubeindex] & 128) == 128){
	      vertlist[7] = VertexInterp(isolevel,gridcell[7],gridcell[4],values[7],values[4]);
  			}
	   if ((mcEdgeTable[cubeindex] & 256) == 256){
	      vertlist[8] = VertexInterp(isolevel,gridcell[0],gridcell[4],values[0],values[4]);
  			}
	   if ((mcEdgeTable[cubeindex] & 512) == 512){
	      vertlist[9] = VertexInterp(isolevel,gridcell[1],gridcell[5],values[1],values[5]);
  			}
	   if ((mcEdgeTable[cubeindex] & 1024) == 1024){
	      vertlist[10] = VertexInterp(isolevel,gridcell[2],gridcell[6],values[2],values[6]);
  			}
	   if ((mcEdgeTable[cubeindex] & 2048) == 2048){
	      vertlist[11] = VertexInterp(isolevel,gridcell[3],gridcell[7],values[3],values[7]);
  			}

	   /* Create the triangle */
	   for (int i = 0; mcTriTable[cubeindex][i] != -1; i += 3) {
	      triangles.add(vertlist[mcTriTable[cubeindex][i  ]]);
	      triangles.add(vertlist[mcTriTable[cubeindex][i+1]]);
	      triangles.add(vertlist[mcTriTable[cubeindex][i+2]]);
	   }

	   return triangles.size() / 3;
	}

	/*
	   Linearly interpolate the position where an isosurface cuts
	   an edge between two vertices, each with their own scalar value
	*/
	static private Point3f VertexInterp(double isolevel, Point3f p1, Point3f p2, double val1, double val2)	{
	   double mu;
	   Point3f p = new Point3f();

	   if (Math.abs(isolevel-val1) < 0.00001)
	      return(p1);
	   if (Math.abs(isolevel-val2) < 0.00001)
	      return(p2);
	   if (Math.abs(val1-val2) < 0.00001)
	      return(p1);
	   mu = (isolevel - val1) / (val2 - val1);
	   p.setX((float)(p1.getX() + mu * (p2.getX() - p1.getX())));
	   p.setY((float)(p1.getY() + mu * (p2.getY() - p1.getY())));
	   p.setZ((float)(p1.getZ() + mu * (p2.getZ() - p1.getZ())));

	   return(p);
	}
	
}