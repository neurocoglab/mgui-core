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

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import mgui.geometry.Mesh3D;


public class EnergySphere {

	public ArrayList<EnergyNode> nodes;
	public Mesh3D mesh;
	NeighbourhoodMesh n_mesh;
	int max_node = -1;
	public Point3f center;
	public float radius;
	
	public EnergySphere(Mesh3D mesh, NeighbourhoodMesh n_mesh, Point3f center, float radius){
		
		this.mesh = mesh;
		this.n_mesh = n_mesh;
		this.center = center;
		this.radius = radius;
		
		//energy determined as inverse of sum of distances to neighbours
		updateEnergy();
		
	}
	
	void updateEnergy(){
		
		nodes = new ArrayList<EnergyNode>(mesh.n);
		double min_dist = Double.MAX_VALUE;
		
		for (int i = 0; i < n_mesh.neighbourhoods.size(); i++){
			Neighbourhood n = n_mesh.getNeighbourhood(i);
			double dist = 0;
			int[] nbrs = n.getNeighbourList();
			Point3f p = mesh.getVertex(i); 
			
			for (int j = 0; j < nbrs.length; j++)
				dist += p.distance(mesh.getVertex(nbrs[j]));
			
			if (dist < min_dist){
				max_node = i;
				min_dist = dist;
				}
			
			nodes.add(new EnergyNode(i, 1.0 / dist));
			}
		
	}
	
	public void update(float entropy){
		
		EnergyNode e_node = nodes.get(max_node);
		Neighbourhood n = n_mesh.getNeighbourhood(e_node.index);
		int[] nbrs = n.getNeighbourList();
		Point3f p = mesh.getVertex(e_node.index); 
		Vector3f F = new Vector3f();
		float e_max = 0;
		Vector3f[] forces = new Vector3f[nbrs.length];
		
		//get forces and max energy
		for (int i = 0; i < nbrs.length; i++){
			Point3f n_pt = mesh.getVertex(nbrs[i]);
			Vector3f f = new Vector3f(n_pt);
			f.sub(p);
			float e_i = 1f / f.length();
			if (e_i > e_max)
				e_max = e_i;
			f.normalize();
			f.scale(e_i * entropy);
			forces[i] = f;
			}
		
		//normalize forces, apply repulsion
		for (int i = 0; i < nbrs.length; i++){
			forces[i].scale(1f / e_max);
			Point3f n_pt = mesh.getVertex(nbrs[i]);
			n_pt.add(forces[i]);
			mesh.setVertex(nbrs[i], getSphereNode(n_pt));
			forces[i].scale(-1);
			p.add(forces[i]);
			}
		
		mesh.setVertex(e_node.index, getSphereNode(p));
		
		updateEnergy();
		
	}
	
	Point3f getSphereNode(Point3f p){
		
		Vector3f v = new Vector3f(p);
		v.sub(center);
		v.normalize();
		v.scale(radius);
		Point3f p2 = new Point3f(center);
		p2.add(v);
		
		return p2;
		
	}
	
	public double getMaxEnergy(){
		return nodes.get(max_node).energy;
	}
	
	public class EnergyNode implements Comparable<EnergyNode>{
		public int index;
		public double energy;
		
		public EnergyNode(int index, double energy){
			this.index = index;
			this.energy = energy;
		}
		
		public int compareTo(EnergyNode n){
			if (n.energy < energy) return -1;
			if (n.energy > energy) return 1;
			return 0;
		}
	}
	
	
}