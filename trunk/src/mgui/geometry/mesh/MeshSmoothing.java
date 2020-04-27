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
import java.util.Collections;
import java.util.Comparator;

import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import mgui.geometry.Mesh3D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.numbers.MguiInteger;


/************************
 * Contains static methods for all mesh smoothing algorithms.
 * @author Andrew Reid
 * @version 1.0
 */

public class MeshSmoothing {

	/************************
	 * Uses an energy model to obtain a representation of <mesh> with triangular faces
	 * having maximal equilaterality. Nodes are treated as charged particles; thus the
	 * most uniform distribution within the mesh boundary is the one with the lowest
	 * energy. See ...(website?). This method uses a local relaxation algorithm.
	 * @param mesh Mesh3D to be operated on
	 * @param timeStep determines the magnitude the force vector in this physical model
	 * @param nodeStep number of neighbours to traverse 
	 */
	
	public static void setEquilateralFacesLR(Mesh3D mesh, 
											 double timeStep, 
											 double decay, 
											 int nodeStep,
											 double limit,
											 boolean holdbounds){
		//method to determine neighbours (exponential decay, nearest k neighbours,
		//linear decay?)
		boolean[] isBoundary = null;
		if (holdbounds)
			isBoundary = MeshFunctions.getBoundaryNodes(mesh);
		
		//parameter to determine acceptable error to stop
		
		//for each node in mesh
		//  find its neighbours
		//  determine charge repulsion (neg. entropy) based upon distance
		ArrayList<Particle> particles = getParticles(mesh, nodeStep, isBoundary);
		for (int i = 0; i < particles.size(); i++)
			setCharge(mesh, particles.get(i), timeStep, decay, nodeStep);
	
		EntropyComp entropy = new EntropyComp();
		
		//sort nodes ascending by entropy
		Collections.sort(particles, entropy);
		
		//TODO: ideally we want to stop when the st dev of entropy (or total entropy) has
		//converged to some limit of stability
		
		//while min entropy > limit
		//  move top node along charge vector projected to surface
		//  insert node sorted
		Particle thisParticle;
		Vector3f v;
		Point3f pt;
		//TODO use st dev here?
		double range = particles.get(0).charge.length() - 
					   particles.get(particles.size() - 1).charge.length();
		
		double lastrange = Double.MAX_VALUE;
		
		limit *= range;
		int itr = 0;
		
		//while (Math.abs(range - lastrange) > limit){
		while (range > limit){
			for (int i = 0; i < particles.size(); i++){
				thisParticle = particles.get(i);
				
				//stop moving nodes if charge is less than limit
				if (thisParticle.charge.length() < limit) break;
			
				//get projected vector
				v = getProjectedVector(mesh, thisParticle, holdbounds);
				
				//move particle
				pt = mesh.getVertex(thisParticle.position);
				pt.add(v);
				mesh.setVertex(thisParticle.position, pt);
				
				//recalculate charge repulsion
				//setCharge(mesh, thisParticle, timeStep, decay, nodeStep);
				}
			
			for (int i = 0; i < particles.size(); i++)
				setCharge(mesh, particles.get(i), timeStep, decay, nodeStep);
				
			//re-sort list
			Collections.sort(particles, entropy);
			
			//recalculate range
			lastrange = range;
			range = particles.get(0).charge.length() - 
					particles.get(particles.size() - 1).charge.length();
			itr++;
			InterfaceSession.log("Range (" + itr + "):" + range);
			}
		
	}
	
	/***************************
	 * EM = Edge method...
	 * @param mesh
	 * @param timeStep
	 * @param decay
	 * @param nodeStep
	 * @param limit
	 */
	public static void setEquilateralFacesEM(Mesh3DInt meshInt, 
											 double max, 
											 double decay,
											 double limit,
											 boolean holdbounds,
											 boolean constrain){
		//method to determine neighbours (exponential decay, nearest k neighbours,
		//linear decay?)
		//if hold bounds, determine boundary nodes
		Mesh3D mesh = meshInt.getMesh();
		
		boolean[] constraints = null;
		if (holdbounds)
			constraints = MeshFunctions.getBoundaryNodes(mesh);
		
		if (constrain)
			for (int i = 0; i < meshInt.getConstraints().length; i++)
				constraints[i] = constraints[i] || meshInt.getConstraints()[i];
		
		//parameter to determine acceptable error to stop
		
		//for each node in mesh
		//  find its neighbours
		//  determine charge repulsion (neg. entropy) based upon distance
		ArrayList<Particle> particles = getParticles(mesh, 1, constraints);
		for (int i = 0; i < particles.size(); i++)
			setEdgeVariance(mesh, particles.get(i));
		
		VarianceComp variance = new VarianceComp();
		
		Collections.sort(particles, variance);
		
		Particle thisParticle;
		Vector3f v;
		Point3f pt;
		//TODO use st dev here?
		double range = particles.get(0).charge.length() - 
					   particles.get(particles.size() - 1).charge.length();
		
		double lastrange = Double.MAX_VALUE;
		
		limit *= range;
		int itr = 0;
		
		//one iteration
		//while (range > limit && Math.abs(lastrange - range) > limit / 100){
		
			//for each particle, move along the longest edge a length of 1 st dev
			for (int i = 0; i < particles.size(); i++)
				moveParticleByVariance(mesh, particles.get(i), holdbounds);
			
			//recalculate edge variance
			for (int i = 0; i < particles.size(); i++)
				setEdgeVariance(mesh, particles.get(i));
			
			//re-sort by variance
			Collections.sort(particles, variance);
			lastrange = range;
			
			//re-calculate range
			range = particles.get(0).charge.length() - 
			   		particles.get(particles.size() - 1).charge.length();
			
			itr++;
			InterfaceSession.log("Range (" + itr + "): " + range);
			//}
		
		
		}
	
	protected static void moveParticleByVariance(Mesh3D mesh, Particle particle){
		moveParticleByVariance(mesh, particle, true);
	}
	
	protected static void moveParticleByVariance(Mesh3D mesh, Particle particle, boolean holdbounds){
		if (holdbounds && particle.boundary) return;
		
		//moves p towards the mean of it and its neighbours by 0.5 st dev
		Point3f p = mesh.getVertex(particle.position);
		Vector3f v = new Vector3f(particle.charge);
		v.scale(0.1f);
		p.add(v);
		mesh.setVertex(particle.position, p);
		
		/*
		float variance = particle.charge.length();
		Vector3f edge = new Vector3f(), maxedge = new Vector3f();
		Point3f p = mesh.getNode(particle.position);
		float max = Float.MIN_VALUE;
		for (int i = 0; i < particle.getNeighbourCount(0); i++){
			edge.sub(mesh.getNode(particle.getNeighbour(i, 0)), p);
			if (edge.length() > max){
				max = edge.length();
				maxedge.set(edge);
				}
			}
		//move along this edge by 1 st dev
		maxedge.normalize();
		maxedge.scale(variance / 2);
		p.add(maxedge);
		mesh.setNode(particle.position, p); */
	}
	
	protected static void setEdgeVariance(Mesh3D mesh, Particle particle){
		Point3f p, n = new Point3f();
		Vector3f v = new Vector3f();
		p = mesh.getVertex(particle.position);
		int N = particle.getNeighbourCount(0);
		float edges[] = new float[N];
		float edgeSum = 0;
		Point3f meanNode = new Point3f(p);
		
		//determine standard deviation of edge lengths for this node's immediate
		//neighbours and set the charge vector to one of this length
		
		for (int j = 0; j < N; j++){
			n = mesh.getVertex(particle.getNeighbour(j, 0));
			meanNode.add(n);
			v.sub(p, n);
			edges[j] = v.length();
			edgeSum += edges[j];
			}
		
		float mean = edgeSum / N;
		meanNode.scale(1.0f / (N + 1));
		
		edgeSum = 0;
		for (int j = 0; j < N; j++)
			edgeSum += Math.pow(edges[j] - mean, 2);

		particle.charge = new Vector3f();
		particle.charge.sub(meanNode, p);
		//particle.charge.normalize();
		//particle.charge.scale((float)Math.sqrt(edgeSum / (float)N));
		particle.variance = (float)Math.sqrt(edgeSum / N);
		
		//particle.charge = new Vector3f((float)Math.sqrt(edgeSum / (float)N), 0, 0);
		}
	
	/************************
	 * Get the projection of this particle's charge vector onto the mesh surface.
	 * Currently, projects the charge vector onto the edge vector having the 
	 * smallest angle from it.
	 * 
	 * @return
	 * @param mesh
	 * @param p
	 */
	protected static Vector3f getProjectedVector(Mesh3D mesh, Particle p){
		return getProjectedVector(mesh, p, true);
	}
	
	/************************
	 * Get the projection of this particle's charge vector onto the mesh surface.
	 * Currently, projects the charge vector onto the edge vector having the 
	 * smallest angle from it.
	 * 
	 * @return
	 * @param mesh
	 * @param p
	 * @param holdbounds - indicates whether boundary nodes should be moved or not
	 */
	protected static Vector3f getProjectedVector(Mesh3D mesh, Particle p, boolean holdbounds){
		//given that p cannot project beyond the edges of its 1st order
		//neighbours, it must project onto one of these faces
		if (p.boundary && holdbounds) return new Vector3f(0, 0, 0);
		
		//degenerate case is where p falls on an edge, in which case it
		//can only move along this edge
		
		//first try: project vector onto nearest edge
		Vector3f[] edges = new Vector3f[p.getNeighbourCount(0)];
		float[] angles = new float[p.getNeighbourCount(0)];
		Point3f neighbour, node = mesh.getVertex(p.position);
		float min = Float.MAX_VALUE, angle;
		float max = Float.MIN_VALUE;
		int n = 0;
		Vector3f edge = new Vector3f(), thisEdge = new Vector3f();
		
		for (int i = 0; i < p.getNeighbourCount(0); i++){
			neighbour = mesh.getVertex(p.getNeighbour(i, 0));
			thisEdge.sub(neighbour, node);
			angle = p.charge.angle(thisEdge); 
			//if (thisEdge.length() > max){
			if (angle < min){
				min = angle;
				//max = thisEdge.length();
				n = i;
				edge.set(thisEdge);
				}
			}
		
		//maximum displacement magnitude is half the edge's length
		float maxdist = edge.length() * 0.5f;
		edge.normalize();
		float dot = p.charge.dot(edge), scale;
		dot = Math.max(0.0f, dot);
		//float dot = p.charge.dot(edge), scale;
		//if (dot > 0)
		scale = Math.min(dot, maxdist);
		//else
		//	scale = Math.max(dot, -maxdist);
		edge.scale(scale);
		
		return edge;
	}
	
	/************************
	 * Set the charge repulsion for this particle based upon its neighbour's edge
	 * distances. Decay is exponential to the factor <decay>. The magnitude of a unit
	 * force vector is determined by <force>.
	 * @param mesh
	 * @param particles
	 * @param force value by which to multiply charge vector
	 * @param decay exponent determining exponential decay with distance
	 * @param m number of neighbours to search
	 */
	
	protected static void setCharge(Mesh3D mesh, 
									Particle particle, 
									double force, 
									double decay,
									int m){
		Point3f p, n;
		Vector3f v = new Vector3f();
		float dist;
		p = mesh.getVertex(particle.position);
		for (int k = 0; k < m; k++){
			for (int j = 0; j < particle.getNeighbourCount(k); j++){
				n = mesh.getVertex(particle.getNeighbour(j, k));
				//vector from neighbour to p
				//TODO determine distance along edges...
				v.sub(p, n);
				dist = v.length();
				dist = (float)(1.0 / Math.pow(dist, decay));
				dist *= force;
				//maximum vector magnitude is half the original distance
				//dist = Math.min(dist, (float)(v.length() * force));
				v.normalize();
				v.scale(dist);
				//apply this vector to particle
				particle.applyForce(v);
				}
			}
	}
	
	//return a list of particles, with k neighbours per particle
	protected static ArrayList<Particle> getParticles(Mesh3D mesh, int k){
		return getParticles(mesh, k, null);
	}
	
	protected static ArrayList<Particle> getParticles(Mesh3D mesh, int k, boolean[] boundaries){
		
		ArrayList<Particle> particles = new ArrayList<Particle>(mesh.n);
		
		//add all nodes
		for (int i = 0; i < mesh.n; i++)
			if (boundaries != null)
				particles.add(new Particle(i, k, boundaries[i]));
			else
				particles.add(new Particle(i, k));
		
		//for each face, add immediate neighbours to each node
		for (int i = 0; i < mesh.f; i++){
			Mesh3D.MeshFace3D face = mesh.getFace(i);
			particles.get(face.A).addNeighbour(face.B, 0);
			particles.get(face.A).addNeighbour(face.C, 0);
			particles.get(face.B).addNeighbour(face.A, 0);
			particles.get(face.B).addNeighbour(face.C, 0);
			particles.get(face.C).addNeighbour(face.A, 0);
			particles.get(face.C).addNeighbour(face.B, 0);
			}
		
		//add 1 to k neighbours to each particle
		for (int i = 1; i < k; i++){
			for (int j = 0; j < particles.size(); j++){
				//for each neighbour at distance i - 1, add its neighbours at this
				//particle's i-distance
				for (int m = 0; m < particles.get(j).getNeighbourCount(i - 1); m++){
					int p = particles.get(j).getNeighbour(m, i - 1);
					//add p's neighbours to j at distance i
					for (int l = 0; l < particles.get(p).getNeighbourCount(0); l++){
						particles.get(j).addNeighbour(particles.get(p).getNeighbour(l, 0), i);
						}
					}
				}
			}
		
		//remove particles with no neighbours (why do these exist?)
		for (int i = 0; i < particles.size(); i++)
			if (particles.get(i).getNeighbourCount(0) == 0){
				particles.remove(i);
				i--;
				}
		
		return particles;
	}
	
	//represents a charged particle with position and a charge force vector
	static class Particle {
		
		public int position;
		public Vector3f charge = new Vector3f(0, 0, 0);
		//4 is a good starting estimate?
		public ArrayList<ArrayList<MguiInteger>> neighbours;
		int n = 0;
		boolean boundary = false;
		public float variance = 0; 
		
		public Particle(int p, int k){
			position = p;
			neighbours = new ArrayList<ArrayList<MguiInteger>>(k);
			for (int i = 0; i < k; i++)
				neighbours.add(new ArrayList<MguiInteger>(5));
			//discharge();
		}
		
		public Particle(int p, int k, boolean b){
			position = p;
			neighbours = new ArrayList<ArrayList<MguiInteger>>(k);
			for (int i = 0; i < k; i++)
				neighbours.add(new ArrayList<MguiInteger>(5));
			boundary = b;
		}
		
		public void discharge(){
			charge = new Vector3f(0, 0, 0);
		}
		
		public void applyForce(Vector3f force){
			charge.add(force);
		}
		
		/****************
		 * Add neighbour at distance k
		 * @param i index of neighbour
		 * @param k distance (in edges) of neighbour from particle
		 */
		public void addNeighbour(int i, int k){
			//if neighbour already exists, do nothing
			MguiInteger j = new MguiInteger(i);
			int a = Collections.binarySearch(neighbours.get(k), j);
			if (a >= 0) return;
			a = -1 - a;
			neighbours.get(k).add(a, j);
		}
		
		public int getNeighbour(int i, int k){
			return neighbours.get(k).get(i).getInt();
		}
		
		public int getNeighbourCount(int k){
			return neighbours.get(k).size();
		}
		
	}
	
	//comparator class comparing entropies of two particles
	//greater charge forces indicates lower entropy
	static class EntropyComp implements Comparator{
		public int compare(Object o1, Object o2){
			Particle p1 = (Particle)o1;
			Particle p2 = (Particle)o2;
			if (p1.charge.length() < p2.charge.length()) return 1;
			if (p1.charge.length() == p2.charge.length()) return 0;
			return -1;
		}
	}
	
	static class VarianceComp implements Comparator{
		public int compare(Object o1, Object o2){
			Particle p1 = (Particle)o1;
			Particle p2 = (Particle)o2;
			if (p1.variance < p2.variance) return 1;
			if (p1.variance == p2.variance) return 0;
			return -1;
		}
	}
	
}