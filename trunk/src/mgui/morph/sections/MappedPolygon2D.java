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

package mgui.morph.sections;

import java.util.ArrayList;

import mgui.geometry.Polygon2D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.numbers.MguiInteger;


/****************************************
 * Implements a node mapping between two Polygon2D shapes. Mapping uses radial representation
 * to assign similarity weights to node pairs, and maps between nodes based upon these weights.
 * This allows mappings between polygons with unequal node counts. The algorithm:
 * <ul>
 * <li>Gets the radial representation of each polygon
 * <li>Assigns weights to node pairs based upon angle similarity
 * <li>First maps nMin nodes, where nMin = min(nA, nB), ensuring that each node gets mapped
 * at least once
 * <li>Secondly maps remaining unmapped nodes based upon their weights and positions relative
 * to already mapped nodes (ensuring that node order is preserved)
 * </ul>
 * 
 * @author Andrew Reid
 * @see mgui.morph.sections.RadialRepresentation
 *
 */

public class MappedPolygon2D implements Cloneable {

	public boolean isReversed, isMapped;
	public Polygon2D A, B;
	public int prevSect, nextSect;
	ArrayList<MorphNode2D> nodesA, nodesB;
	ArrayList<MorphNodeMap2D> nodeMaps;
	ArrayList<MguiInteger> sourceCounts;
	ArrayList<MguiInteger> targetCounts;
	ArrayList<MorphNodeMap2D> extraMaps;
	
	public MappedPolygon2D(){
		
	}
	
	/*************
	 * Constructor passing a source (A) and target (B) polygon. 
	 * @param a Source polygon
	 * @param b Target polygon
	 */
	
	public MappedPolygon2D(Polygon2D a, Polygon2D b, int psect, int nsect){
		setPolygons(a, b);
		prevSect = psect;
		nextSect = nsect;
		//mapPolygons();
	}
	
	/*************
	 * 
	 * @param a
	 * @param b
	 */
	
	public void setPolygons(Polygon2D a, Polygon2D b){
		A = a;
		B = b;
		if (A == null || B == null){
			return;
		}
		
		if (A.vertices.size() < B.vertices.size()){
			isReversed = true;
			A = b;
			B = a;
		}
		
		//set nodes and sourceCounts arrays
		nodesA = new ArrayList<MorphNode2D>(A.vertices.size());
		for (int i = 0; i < A.vertices.size(); i++)
			nodesA.add(new MorphNode2D(A.vertices.get(i)));
		nodesB = new ArrayList<MorphNode2D>(B.vertices.size());
		for (int i = 0; i < B.vertices.size(); i++)
			nodesB.add(new MorphNode2D(B.vertices.get(i)));
		nodeMaps = new ArrayList<MorphNodeMap2D>(A.vertices.size());
		for (int i = 0; i < A.vertices.size(); i++)
			nodeMaps.add(new MorphNodeMap2D(isReversed));
		sourceCounts = new ArrayList<MguiInteger>(B.vertices.size());
		for (int i = 0; i < B.vertices.size(); i++)
			sourceCounts.add(new MguiInteger(0));
		targetCounts = new ArrayList<MguiInteger>(A.vertices.size());
		for (int i = 0; i < A.vertices.size(); i++)
			targetCounts.add(new MguiInteger(0));
		extraMaps = new ArrayList<MorphNodeMap2D>();
		
	}
	
	public Polygon2D getOrderedA(){
		if (isReversed)
			return B;
		return A;
	}
	
	public Polygon2D getOrderedB(){
		if (isReversed)
			return A;
		return B;
	}
	
	public Polygon2D getA(){
		return A;
	}
	
	public Polygon2D getB(){
		return B;
	}
	
	public void mapPolygons(double threshold){
		
		int nA = A.vertices.size();
		int nB = B.vertices.size();
		
		if (nA < 3 || nB < 3) return;
		
		//obtain radial reps
		RadialRep2D repA = new RadialRep2D(A);
		RadialRep2D repB = new RadialRep2D(B);
		
		if (Double.isNaN(repB.radii.get(0).angle))
			nA = nA;
		
		//set weights with radial reps
		double[][] weights = new double[nA][nB];
		for (int i = 0; i < repA.radii.size(); i++)
			for (int j = 0; j < repB.radii.size(); j++)
				weights[i][j] = (1 - (repA.getRadiusAtNode(i).getAngleDiff(repB.getRadiusAtNode(j)))
																						/ (Math.PI));
		
		//for each node N_A(i), map to a node N_B(j) based upon weights
		//first, ensure all B nodes have at least one source
		double thisMax = threshold;
		int aNode, bTarget, aSource;
		
		//array to record source nodes that have been mapped
		boolean[] mapped = new boolean[A.vertices.size()];
		
		//get maps for B nodes based upon weight
		for (int j = 0; j < nB; j++){
			aNode = -1;
			bTarget = -1;
			aSource = -1;
			for (int i = 0; i < nA; i++){
				if (weights[i][j] > thisMax)
					if (mapped[i]){
						if (weights[i][j] > nodeMaps.get(i).weight.weight){
							//already mapped, so alter this map
							aNode = i;
							thisMax = weights[i][j];
							bTarget = nodeMaps.get(i).weight.target;
							aSource = i;
							}
					}else{
						aNode = i;
						thisMax = weights[i][j];
						bTarget = -1;
						}
				}
				if (aNode >= 0){
					nodeMaps.set(aNode, new MorphNodeMap2D(nodesA.get(aNode),
														nodesB.get(j),
														new MapWeight(weights[aNode][j], aNode, j),
														isReversed));
					sourceCounts.get(j).add(1);
					mapped[aNode] = true;
					if (bTarget >= 0){
						sourceCounts.get(bTarget).subtract(1);
						targetCounts.get(aSource).subtract(1);
						}
					else
						targetCounts.get(aNode).add(1);
					}
				}
			
		
		int bNode;
		
		//map unmapped A nodes
		for (int i = 0; i < nA; i++)
			if (!mapped[i]){
				thisMax = Double.MIN_VALUE;
				bNode = -1;
				for (int j = 0; j < nB; j++)
					if (weights[i][j] > thisMax){
						bNode = j;
						thisMax = weights[i][j];
						}
				if (bNode >= 0){
					nodeMaps.set(i, new MorphNodeMap2D(nodesA.get(i),
													nodesB.get(bNode),
													new MapWeight(weights[i][bNode], i, bNode),
													isReversed));
					sourceCounts.get(bNode).add(1);
					targetCounts.get(i).add(1);
					}
				}
		
		//map unmapped B nodes to extraMaps
		for (int j = 0; j < nB; j++)
			if (sourceCounts.get(j).getInt() == 0){
				thisMax = Double.MIN_VALUE;
				aNode = -1;
				for (int i = 0; i < nA; i++)
					if (weights[i][j] > thisMax){
						thisMax = weights[i][j];
						aNode = i;
						}
					if (aNode >= 0){
						if (nodesA == null)
							aNode = aNode;
						if (nodesB == null)
							aNode = aNode;
						extraMaps.add(new MorphNodeMap2D(nodesA.get(aNode),
													nodesB.get(j),
													new MapWeight(weights[aNode][j], aNode, j),
													isReversed));
						sourceCounts.get(j).add(1);
						targetCounts.get(aNode).add(1);
						}
				}
			
		isMapped = true;
		return;
	}
	
	/****************
	 * Sets the nodes in nodesA or nodesB from the previous polygon map in this
	 * series.
	 * @param prevMap
	 */
	
	public void setNodeList(MappedPolygon2D prevMap){
		
		//four cases depending on reversal state of polygons
		if (!this.isReversed){
			//case 1: neither is reversed
			if (!prevMap.isReversed){
				//map prev.B to this.A
				//test whether they match in size
				if (prevMap.B.vertices.size() != this.A.vertices.size()) return;
				for (int i = 0; i < A.vertices.size(); i++)
					this.nodesA.set(i, prevMap.nodesB.get(i));
				
				//case 2: prev map is reversed but this map is not
			}else{
				//map prev.A to this.A
				if (prevMap.A.vertices.size() != this.A.vertices.size()) return;
				for (int i = 0; i < A.vertices.size(); i++)
					this.nodesA.set(i, prevMap.nodesA.get(i));
				}
		}else{
			//case 3: this map is reversed but previous is not
			if (!prevMap.isReversed){
				//map prev.B to this.B
				if (prevMap.B.vertices.size() != this.B.vertices.size()) return;
				for (int i = 0; i < B.vertices.size(); i++)
					this.nodesB.set(i, prevMap.nodesB.get(i));
				
				//case 4: both are reversed
			}else{
				//map prev.A to this.B
				if (prevMap.A.vertices.size() != this.B.vertices.size()) return;
				for (int i = 0; i < this.B.vertices.size(); i++)
					this.nodesB.set(i, prevMap.nodesA.get(i));
				}
			}	
	}
	
	public int removeNodeByCount(double minAngle){
		return removeNodeByCount(minAngle, 0.1);
	}
	
	/*********************
	 * Remove a node from A, selected by the criteria that its target has a maximum
	 * source count (i.e., multiple source (A) nodes mapped to a single target (B)
	 * node), and is attached to a minimal length edge.
	 * @param minAngle the minimum node angle at which a node remove a node is allowed
	 * (may no longer be necessary...)
	 */
	
	public int removeNodeByCount(double minAngle, double minLength){
		//get an edge length array
		float angles[] = GeometryFunctions.getNodeAngles(A);
		float lengths[] = GeometryFunctions.getSegmentLengths(A);
		
		//find first target node with max source count
		//is perhaps possible to refine this search for multiple
		//target nodes with the same source count...
		int thisMax = 0, targetNode = -1;
		for (int i = 0; i < sourceCounts.size(); i++)
			if (sourceCounts.get(i).getInt() > thisMax){
				thisMax = sourceCounts.get(i).getInt();
				targetNode = i;
				}
		
		//now find source node with targetNode as target and maximal angle 
		//... er shortest edge (perhaps this can be made a parameter)
		//float max = Float.MIN_VALUE;
		float sum = 0;
		int minNode = 0;
		for (int i = 0; i < lengths.length; i++){
			if (lengths[i] < lengths[minNode] && angles[i] > minAngle) minNode = i;
			sum += lengths[i];
			}
		float avr = sum / lengths.length; 
		float min = (float)(minLength * avr);
		 
		int j, remNode = -1;
		
		for (int i = 0; i < nodeMaps.size(); i++){
			//if this source node maps to targetNode
			if (nodeMaps.get(i).weight.target == targetNode){
				j = i - 1;
				if (j < 0) j = nodeMaps.size() - 1;
				//find node with maximum angle
				//if (angles[i] > max || angles[j] > max){
				//	max = Math.max(angles[i], angles[j]);
				//	if (angles[i] > angles[j])
				//find node with minimum segment length
				if ((lengths[i] > min || lengths[j] > min) && angles[j] > minAngle){
					min = Math.min(lengths[i], lengths[j]);
					if (lengths[i] < lengths[j])
						remNode = i;
					else
						remNode = j;
					}
				}
			}
		
		remNode = minNode;
		if (remNode >= 0){
			//remove this node and return its index
			removeNode(remNode);
			//decrement source count for this target node as source node
			//is now removed
			sourceCounts.get(targetNode).subtract(1);
			return remNode;
			}
		
		//if no nodes removed, why not?
		return minNode;
	}
	
	public void removeNode(int i){
		nodeMaps.remove(i);
		nodesA.remove(i);
		A.vertices.remove(i);
		//update weights sources
		for (int j = 0; j < nodeMaps.size(); j++)
			if (nodeMaps.get(j).weight.source >= i)
				nodeMaps.get(j).weight.source--;
	}
	
	@Override
	public Object clone(){
		//return a deep copy of this polygon map
		MappedPolygon2D retObj = new MappedPolygon2D((Polygon2D)A.clone(), 
													 (Polygon2D)B.clone(),
													 prevSect,
													 nextSect);
		retObj.isReversed = isReversed;
		
		retObj.nodesA = new ArrayList<MorphNode2D>(nodesA.size());
		for (int i = 0; i < nodesA.size(); i++){
			retObj.nodesA.add(new MorphNode2D(retObj.A.vertices.get(i)));
			retObj.nodesA.get(i).tangentX = nodesA.get(i).tangentX;
			retObj.nodesA.get(i).tangentY = nodesA.get(i).tangentY;
			}
		retObj.nodesB = new ArrayList<MorphNode2D>(nodesB.size());
		for (int i = 0; i < nodesB.size(); i++){
			retObj.nodesB.add(new MorphNode2D(retObj.B.vertices.get(i)));
			retObj.nodesB.get(i).tangentX = nodesB.get(i).tangentX;
			retObj.nodesB.get(i).tangentY = nodesB.get(i).tangentY;
			}
		
		retObj.nodeMaps = new ArrayList<MorphNodeMap2D>(nodeMaps.size());
		for (int i = 0; i < nodeMaps.size(); i++){
			//new map to nodes from weight
			retObj.nodeMaps.add(new MorphNodeMap2D(
					new MorphNode2D(retObj.A.vertices.get(nodeMaps.get(i).weight.source)),
					new MorphNode2D(retObj.B.vertices.get(nodeMaps.get(i).weight.target)),
					(MapWeight)nodeMaps.get(i).weight.clone(),
					isReversed));	
			}
		
		retObj.sourceCounts = new ArrayList<MguiInteger>(sourceCounts.size());
		for (int i = 0; i < sourceCounts.size(); i++)
			retObj.sourceCounts.add((MguiInteger)sourceCounts.get(i).clone());
		return retObj;
	}
	
	public void printWeights(double[][] weights){
		int nI = weights.length;
		int nJ = weights[0].length;
		for (int i = 0; i < nI; i++){
			System.out.print("\n");
			for (int j = 0; j < nJ; j++)
				System.out.print(weights[i][j] + ", ");
				//System.out.print(arDouble.getString(weights[i][j], "#0.00") + ", ");
		}
	}
	
	public void printMap(){
		//print each map node
		System.out.println();
		for (int i = 0; i < nodeMaps.size(); i++)
			nodeMaps.get(i).printMap();
		System.out.println();
		for (int i = 0; i < sourceCounts.size(); i++)
			System.out.print("B(" + i + "): " + sourceCounts.get(i) + "  ");
	}
	
}