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

import java.util.ArrayList;
import java.util.Vector;

import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import mgui.geometry.Mesh3D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.numbers.MguiNumber;


public class MeshInflation {

	
	/****************************
	 * Inflates a given mesh, by relaxing its vertices towards the mean of its
	 * neighbours, as described by:
	 * 
	 * <p>Tosun D, Rettman ME, Prince JL (2004). Mapping techniques for aligning
	 * sulci across multiple brains. <i>Medical Image Analysis</i> 8:295-309.
	 * 
	 * @param mesh Mesh to inflate
	 * @param lambda Relaxation (smoothing) parameter, ranging from 0 to 1. 0
	 * results in no inflation; 1 results (ultimately) in a sphere.
	 * @param beta Stopping criterion for minimization of mean curvature.
	 * @param max_itr Maximum number of iterations
	 */
	public static Mesh3D inflateMeshTRP(Mesh3DInt mesh3D, double lambda, double beta, long max_itr, ProgressUpdater progress){
		
		Mesh3D mesh = (Mesh3D)mesh3D.getMesh().clone();
		
		//we need:
		//a list relating each vertex to the triangles it is part of
		//a list of the centers of each triangle
		//a list of the areas of each triangle
		//a list of the curvatures of each vertex
		
		ArrayList<ArrayList<Integer>> vertex_faces = new ArrayList<ArrayList<Integer>>();
		Point3f[] centers = new Point3f[mesh.f];
		double[] areas = new double[mesh.f];
		double[] vertex_areas = new double[mesh.n];
		
		//init stuff
		if (progress != null){
			progress.setIndeterminate(true);
			progress.register();
			}
		
		for (int i = 0; i < mesh.n; i++){
			vertex_faces.add(new ArrayList<Integer>());
			}
		
		for (int i = 0; i < mesh.f; i++){
			//add triangle indices to vertices
			Mesh3D.MeshFace3D face = mesh.getFace(i);
			for (int j = 0; j < 3; j++){
				vertex_faces.get(face.getNode(j)).add(i);
				}
			}
		
		boolean notConverged = true;
		ArrayList<Point3f> vertices = mesh.getVertices();
		Vector3f V_t = new Vector3f(), V_t1 = new Vector3f(), V_tbar = new Vector3f();
		Vector3f T = new Vector3f();
		
		int itr = 0;
		
		// Step through inflation
		while (notConverged){
			
			for (int i = 0; i < mesh.f; i++){
				//add center to list
				centers[i] = GeometryFunctions.getCenterOfGravity(mesh.getFaceNodes(i));
				//add area to list
				areas[i] = GeometryFunctions.getArea(mesh.getFaceTriangle(i));
				}
			
			// Compute mean curvature
			ArrayList<MguiNumber> curvatures = MeshFunctions.getVertexWiseCurvature(mesh, progress);
			float mean_curv = 0;
			
			for (int i = 0; i < mesh.n; i++){
				mean_curv += curvatures.get(i).getValue();
				vertex_areas[i] = 0;
				}
			mean_curv /= mesh.n;
			
			// Vertex-wise areas
			for (int i = 0; i < mesh.f; i++){
				Mesh3D.MeshFace3D face = mesh.getFace(i);
				for (int j = 0; j < 3; j++)
					vertex_areas[face.getNode(j)] += areas[i];
				}
			
			// Reposition according to V(t+1) = (1-lambda)*V(t) + (lambda)*V_bar_t
			for (int i = 0; i < mesh.n; i++){
				
				V_t1.set(mesh.getVertex(i));
				ArrayList<Integer> faces = vertex_faces.get(i);
				double B = 0;
				V_tbar.set(0,0,0);
				for (int j = 0; j < faces.size(); j++){
					int f_idx = faces.get(j);
					B += areas[f_idx];
					T.set(centers[f_idx]);
					T.scale((float)areas[f_idx]);
					V_tbar.add(T);
					}
				V_tbar.scale((float)(lambda/B));
				
				//V_t1.set(vertices.get(i));
				V_t1.scale((float)(1.0-lambda));
				V_t1.add(V_tbar);
				
				mesh.setVertex(i, new Point3f(V_t1));
				
				if (progress != null){
					progress.iterate();
					if (progress.isCancelled()){
						InterfaceSession.log("Inflate Mesh operation cancelled by user.", 
											 LoggingType.Warnings);
						progress.deregister();
						return null;
						}
					}
				
				}
			
			// Test for convergence
			// Term is sqrt(1/4*pi * sum(curvature_i^2 * area_i))
			// Or just curvature
			
			InterfaceSession.log("Inflation iteration " + itr + "; H=" + mean_curv, LoggingType.Debug);
			
			if (mean_curv < beta){
				notConverged = false;
				InterfaceSession.log("Converged.", LoggingType.Debug);
			}else if (itr == max_itr){
				notConverged = false;
				InterfaceSession.log("Max iterations reached without converging.", LoggingType.Debug);
				}
			
			itr++;
			
			}
		
		if (progress != null){
			progress.deregister();
			}
		
		return mesh;
		
	}
	
	
}