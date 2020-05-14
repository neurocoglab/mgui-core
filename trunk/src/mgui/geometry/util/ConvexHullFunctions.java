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
import java.util.HashMap;

import org.jogamp.vecmath.Point3f;

import mgui.geometry.Mesh3D;
import mgui.geometry.mesh.MeshFunctionException;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.Utility;
import au.edu.unsw.cse.DivideAndConquer;
import au.edu.unsw.cse.GiftWrap;
import au.edu.unsw.cse.HullAlgorithm;
import au.edu.unsw.cse.Incremental;
import au.edu.unsw.cse.Object3dList;
import au.edu.unsw.cse.Point3d;
import au.edu.unsw.cse.Point3dObject3d;
import au.edu.unsw.cse.QuickHull;
import au.edu.unsw.cse.Triangle3d;

public class ConvexHullFunctions extends Utility {

	/*********************************
	 * Returns a <code>Mesh3D</code> which is the  
	 * 
	 * @param nodes
	 * @param method
	 * @param progress
	 * @return
	 * @throws MeshFunctionException
	 */
	public static Mesh3D getConvexHull(ArrayList<Point3f> nodes, String method, ProgressUpdater progress) throws MeshFunctionException{
		
		if (nodes.size() < 3)
			throw new MeshFunctionException("ConvexHullFunctions: cannot create hull for less than three nodes!");
		
		HullAlgorithm algorithm = getAlgorithm(method, nodes);
		
		if (algorithm == null)
			throw new MeshFunctionException("ConvexHullFunctions: Invalid method '" + method + "'.");
		
		Object3dList list = algorithm.build(progress);
		if (list == null) return null;
		return getFacesToMesh(list);
		
	}
	
	public static HullAlgorithm getAlgorithm(String method, ArrayList<Point3f> nodes){
		
		if (method.toLowerCase().equals("quickhull"))
			return new QuickHull(getPoints(nodes));
		
		if (method.toLowerCase().equals("giftwrap"))
			return new GiftWrap(getPoints(nodes));
		
		if (method.toLowerCase().equals("divide & conquer"))
			return new DivideAndConquer(getPoints(nodes));
		
		if (method.toLowerCase().equals("incremental"))
			return new Incremental(getPoints(nodes));
		
		return null;
		
	}
	
	static Point3dObject3d[] getPoints(ArrayList<Point3f> nodes){
		
		Point3dObject3d[] points = new Point3dObject3d[nodes.size()];
		
		for (int i = 0; i < nodes.size(); i++){
			Point3f p = nodes.get(i);
			points[i] = new Point3dObject3d(new Point3d(p.x, p.y, p.z));
			}
		
		return points;
	}
	
	static Mesh3D getFacesToMesh(Object3dList tris) throws MeshFunctionException{
		//need to index all distinct points
		
		HashMap<Point3d, Integer> index_map = new HashMap<Point3d, Integer>();
		HashMap<Integer, Point3d> node_map = new HashMap<Integer, Point3d>();
		ArrayList<Mesh3D.MeshFace3D> faces = new ArrayList<Mesh3D.MeshFace3D>();
		
		int current_index = 0;
		Mesh3D hull = new Mesh3D();
		
		for (int i = 0; i < tris.size(); i++){
			if (tris.elementAt(i) instanceof Triangle3d){
				Triangle3d tri = (Triangle3d)tris.elementAt(i);
				Mesh3D.MeshFace3D face = new Mesh3D.MeshFace3D();
				
				Point3d[] pts = tri.tri;
				
				for (int j = 0; j < 3; j++){
					if (index_map.containsKey(pts[j])){
						face.setNode(j, index_map.get(pts[j]));
					}else{
						index_map.put(pts[j], current_index);
						node_map.put(current_index, pts[j]);
						face.setNode(j, current_index++);
						}
					}
				
				faces.add(face);
				}
			}
		
		//now add nodes and faces
		for (int i = 0; i < current_index; i++){
			Point3d p = node_map.get(i);
			if (p == null){
				throw new  MeshFunctionException("QuickHull3D: Null node in hull creation..");
				}
			hull.addVertex(getPoint3fFromPoint3d(p));
			}
		
		hull.addFaces(faces);
		return hull;
		
	}
	
	static Point3f getPoint3fFromPoint3d(Point3d pt){
		return new Point3f((float)pt.x(), (float)pt.y(), (float)pt.z());
	}
	
	
}