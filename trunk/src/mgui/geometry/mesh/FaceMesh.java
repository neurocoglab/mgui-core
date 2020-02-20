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
import java.util.HashMap;

import mgui.geometry.Mesh3D;
import mgui.geometry.Mesh3D.MeshFace3D;

/****************************************************************************
 * Maintains a vertex-wise list of faces; i.e., allows to user to quickly obtain a list of the faces
 * associated with a particular node. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class FaceMesh {

	//protected HashMap<Integer,ArrayList<MeshFace3D>> faces = new HashMap<Integer,ArrayList<MeshFace3D>>();
	protected HashMap<Integer,ArrayList<Integer>> faces = new HashMap<Integer,ArrayList<Integer>>();
	
	public FaceMesh(Mesh3D mesh){
		
		for (int i = 0; i < mesh.getFaceCount(); i++){
			MeshFace3D face = mesh.getFace(i);

			for (int j = 0; j < 3; j++){
				int index = face.getNode(j);
				if (faces.containsKey(index)){
					faces.get(index).add(i);
				}else{
					ArrayList<Integer> list = new ArrayList<Integer>();
					list.add(i);
					faces.put(index, list);
					}
				}
			}
		
	}
	
	/***********************************************
	 * Returns a list of faces associated with vertex {@code i}.
	 * 
	 * @param i
	 * @return
	 */
	public ArrayList<Integer> getFaces(int i){
		return faces.get(i);
	}
	
	
}