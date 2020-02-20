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

package mgui.morph.sections;

import java.util.ArrayList;
import java.util.Iterator;

import javax.media.j3d.IndexedTriangleArray;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector2d;

import mgui.geometry.Mesh3D;
import mgui.geometry.Plane3D;
import mgui.geometry.Polygon2D;
import mgui.geometry.Polygon3D;
import mgui.geometry.mesh.MeshFunctions;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.shapes.LPolygon2DInt;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.Polygon2DInt;
import mgui.interfaces.shapes.Polygon3DInt;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.ShapeSet2DInt;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.shapes.util.SectionSetIterator;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;

import com.sun.j3d.utils.geometry.GeometryInfo;


/****************************
 * Engine class implementing algorithms to create polygons which represent the intermediate
 * positions of a morphing between two polygons on adjacent sections. The algorithm is
 * essentially:
 * <ul>
 * <li>Given a section set, filter that set for instances of Polygon2DInt
 * <li>If a selection set is specified, filter for shapes in this set
 * <li>Map between the nodes of adjacent polygons using a radial representation
 * <li>If a spline is requested, calculate the tangents of the map-paths of the set
 * of mapped polygons
 * <li>For each pair of mapped polygons, create a specified number of intermediate polygons
 * whose nodes lie along the path (linear or spline) between mapped nodes.
 * <li>For unequal node counts, remove iter/deltaN nodes per iteration, based upon either
 * segment length, node angle, or some other measure of priority (this still needs tweaking)
 * </ul>
 * 
 * A Mesh3DInt surface can also be generated from a section set of polygons, e.g., the
 * morphed set obtained from the above steps (the mesh routine must be called separately).
 * This algorithm:
 * <ul>
 * <li>Maps polygons using radial representations, as above
 * <li>Triangulates each polygon pair using the Java3D triangulator (NOTE: triangulation
 * might be improved by making use of the mapping information)
 * </ul>
 * 
 * @author Andrew Reid
 * @see ar.morph.sections.RadialRepresention
 *
 */
public class MorphEngine {

	//map nodes between a set of scetions using radial representations
	//create splines from mapped polylines (trajectories)
	//create n new sections, morphing based upon this mapping scheme
	public AttributeList morphAttr = new AttributeList();
	public MorphDrawEngine drawEngine = new MorphDrawEngine();
	
	//constants
	public static final int MORPH_SECTIONS = 0;
	public static final int MORPH_MESH = 1;
	public static final int MORPH_ALL = 2;
	
	public MorphEngine(){
		init();
	}
	
	private void init(){
		//attributes
		morphAttr.add(new Attribute("NoIterations", new MguiInteger(1)));
		morphAttr.add(new Attribute("UseSelectionSet", new MguiBoolean(false)));
		morphAttr.add(new Attribute("SelectionSet", new ShapeSelectionSet()));
		morphAttr.add(new Attribute("ApplySpline", new MguiBoolean(false)));
		morphAttr.add(new Attribute("GenerateSurface", new MguiBoolean(false)));
		morphAttr.add(new Attribute("EndSplineFactor", new MguiDouble(1)));
		morphAttr.add(new Attribute("ShapeName", new String("Unnamed")));
		morphAttr.add(new Attribute("AngleThreshold", new MguiDouble(3 * Math.PI / 4)));
		morphAttr.add(new Attribute("LengthThreshold", new MguiDouble(0.1)));
		morphAttr.add(new Attribute("MapMulti", new MguiBoolean(false)));
	}
	
	//public MorphSections3DInt getMorphSections(SectionSet3DInt sections, double weight_thres){
	public SectionSet3DInt getMorphSections(SectionSet3DInt sections, double weight_thres){
		
		//get polygon objects from set
		SectionSet3DInt polySet = getPolygonSet(sections);
		
		InterfaceSession.log("\nPolygons: " + polySet.getShapeCount());
		
		if (polySet.sections.size() == 0) return new SectionSet3DInt();
		
		Iterator<ShapeSet2DInt> itr = polySet.sections.values().iterator();
		AttributeList attr = itr.next().getAttributes();
		
		polySet.setShape(sections.getShape());
		polySet.setSpacing(sections.getSpacing());
		
		//map all nodes in polygon
		ArrayList<MappedPolygon2DPath> mappedSet = getMappedPolygons(polySet, 
																	 weight_thres, 
																	 ((MguiBoolean)morphAttr.getValue("MapMulti")).getTrue());
		
		
		InterfaceSession.log("\nMapped polygons: " + mappedSet.size());
			
		//if spline X or spline Y selected, set node tangents
		if (((MguiBoolean)morphAttr.getValue("ApplySpline")).getTrue())
			mappedSet = getNodeTangents(mappedSet, sections.getSpacing(), 
								   ((MguiDouble)morphAttr.getValue("EndSplineFactor")).getValue());
		
		int iter = ((MguiInteger)morphAttr.getValue("NoIterations")).getInt();
		
		//generate subsection polygons
		SectionSet3DInt morphSections = getSubsections(mappedSet, iter, attr);
		morphSections.setSpacing(sections.getSpacing() / (iter + 1));
		
		return morphSections;
	}
	
	//attempts to find corner points for a set of polygon shapes,
	//using the criteria of:
	//1. Above threshold relative length of edge attached to node
	//2. At least one angle with threshold acuteness attached to this edge
	//method returns a SectionSet2DInt object with its Polygon2DInt
	//objects replaced by BoolPolygon2DInt objects, with corner points set
	//to <true>
	public SectionSet3DInt getCornerPoints(SectionSet3DInt sections){
		
		//for each section
		//for each Polygon2DInt
		//get BoolPolygon2DInt from GeometryFunctions.getCornerPoints
		
		//return new section set
		
		return null;
	}
	
	//returns a section set with only polygon objects
	public SectionSet3DInt getPolygonSet(SectionSet3DInt sections){
		
		ShapeSet2DInt polySet;
		SectionSet3DInt sectSet = null, newSet = new SectionSet3DInt();
		
		//filter by selection set if specified
		if (((MguiBoolean)morphAttr.getValue("UseSelectionSet")).getTrue() &&
						morphAttr.getValue("SelectionSet") != null)
			sectSet = ((ShapeSelectionSet)morphAttr.getValue("SelectionSet")).
													  getFilteredSectionSet(sections);
		else
			sectSet = sections;
		
		//filter for polygons
		Iterator<Integer> itr = sectSet.sections.keySet().iterator();
		while (itr.hasNext()){
			int s = itr.next().intValue();
			polySet = sectSet.getShapeSet(s).getShapeType(new Polygon2DInt());
			for (int j = 0; j < polySet.getSize(); j++)
				newSet.addShape2D(polySet.getShape(j), s, false);
		}
		
		return sectSet;
		
	}
	
	public ArrayList<MappedPolygon2DPath> getMappedPolygons(SectionSet3DInt polySet, 
			double weight_thres){
		return getMappedPolygons(polySet, weight_thres, false);
	}
	
	/***************************
	 * Maps the set of polygons in <code>polyset</code> between each neighbouring section. 
	 * @param polySet
	 * @param weight_thres
	 * @param mapMulti
	 * @return
	 */
	public ArrayList<MappedPolygon2DPath> getMappedPolygons(SectionSet3DInt polySet, 
															double weight_thres,
															boolean mapMulti){
		
		Polygon2D thisPoly = null, lastPoly = null;
		ArrayList<Polygon2D> thisPolys, lastPolys = null;
		
		MappedPolygon2D mapPoly, lastMap = null;
		ShapeSet2DInt thisSet;
		int thisSect, lastSect = 0;
		ArrayList<MappedPolygon2D> mapList = new ArrayList<MappedPolygon2D>();
		
		ArrayList<MappedPolygon2DPath> paths = new ArrayList<MappedPolygon2DPath>();
		
		if (!polySet.hasSections()) return paths;
		
		SectionSetIterator setItr = (SectionSetIterator)polySet.getIterator();
		thisSect = setItr.getFirstSection();
		
		int firstSect = Integer.MIN_VALUE;
		boolean isSet = false;
		boolean isValid = false;
		
		MappedPolygon2DPath thisPath;
		
		while (setItr.hasNext()){
		
			thisSet = ((ShapeSet2DInt)setItr.next());
			if (thisSet != null && thisSet.members.size() > 0){
				if (firstSect == Integer.MIN_VALUE) firstSect = thisSect;
				
				isValid = true;
				thisPolys = new ArrayList<Polygon2D>();
				//get list of polygons, or only first polygon if mapMulti = false
				for (int i = 0; i < thisSet.members.size() && (i < 1 || mapMulti); i++){
					thisPoly = ((Polygon2DInt)thisSet.members.get(i)).getPolygon();
					isValid &= thisPoly.vertices.size() > 2;
					thisPolys.add(thisPoly);
					}
				isValid &= thisPolys.size() > 0;
				
				//if multiple polygons & lastPolys != null, align thisPolys with lastPolys:
				//align closest polygons (center of gravity) with one another
				boolean[][] map_polys = null;
				if (lastPolys != null)
					map_polys = getMultiPolygonMapping(thisPolys, lastPolys);
					
				if (isValid){
					for (int i = 0; i < thisPolys.size(); i++){
						thisPoly = thisPolys.get(i);
						if (lastPolys != null){
							for (int j = 0; j < lastPolys.size(); j++){
								if (map_polys[i][j]){
									lastPoly = lastPolys.get(j);
									//find path with this poly
									thisPath = null;
									for (int k = 0; k < paths.size(); k++)
										if (paths.get(k).getLastPolygon().equals(lastPoly))
											thisPath = paths.get(k);
		
									if (thisSect > firstSect && thisSect > lastSect && lastPoly != null && thisPath != null){
										mapPoly = new MappedPolygon2D(lastPoly, thisPoly, lastSect, thisSect);
										lastMap = thisPath.getLastMap();
										if (isSet && lastMap != null) mapPoly.setNodeList(lastMap);
										mapPoly.mapPolygons(weight_thres);
										if (mapPoly.isMapped){
											thisPath.newMap(mapPoly);
											mapList.add(mapPoly);
											lastMap = mapPoly;
											isSet = true;
											}
									}else{
										if (thisPath == null)
											InterfaceSession.log("Debug -- No path for poly: " + lastPoly.toString());
										}
									}
								}
						}//else{
							//if lastPolys is null, this is the first section
							//instantiate paths from polygons on first section
							//for (int k = 0; k < thisPolys.size(); k++)
							//	paths.add(new MappedPolygon2DPath(thisPolys.get(k)));
						//	}
							
						}
					//if lastPolys is null, this is the first section
					//instantiate paths from polygons on first section
					if (lastPolys == null)
						for (int k = 0; k < thisPolys.size(); k++)
							paths.add(new MappedPolygon2DPath(thisPolys.get(k)));
					
					//update paths
					ArrayList<MappedPolygon2DPath> temp = new ArrayList<MappedPolygon2DPath>();
					for (int k = 0; k < paths.size(); k++)
						temp.addAll(paths.get(k).getBifurcatedPaths());
					if (temp.size() > 0){
						int p = 0;
						}
					paths.addAll(temp);
					lastPolys = thisPolys;
					lastSect = thisSect;
					}
				}
			thisSect = setItr.getNextSection();
			System.out.print(".");
			}
		System.out.print("!");
		return paths;
		//return mapList;
	}
	
	protected boolean[][] getMultiPolygonMapping(ArrayList<Polygon2D> polys0, ArrayList<Polygon2D> polys1){
		if (polys0 == null || polys1 == null) return null;
		
		int n0 = polys0.size(), n1 = polys1.size();
		if (n0 == 1 && n1 == 1) return new boolean[][]{{true, true},{true,true}};
		
		//get min size
		boolean isReversed = n0 > n1;
		ArrayList<Polygon2D> polys_small = null;
		ArrayList<Polygon2D> polys_big = null;
		
		if (!isReversed){
			polys_small = polys0;
			polys_big = polys1;
		}else{
			polys_small = polys1;
			polys_big = polys0;
			}
			
		n0 = polys_small.size();
		n1 = polys_big.size();
		
		//get centers of gravity
		//get list of distances
		double[][] distances = getPolygonDistances(polys_small, polys_big);
		
		//each polygon will map to its closest counterpart
		boolean[] mapped0 = new boolean[n0];
		boolean[] mapped1 = new boolean[n1];
		boolean[][] map_polys = new boolean[polys0.size()][polys1.size()];
		int[] map0 = new int[n0];
		int[] map1 = new int[n1];
		int jMin, iMin;
		double min;
				
		//map polys0 to polys1
		for (int i = 0; i < n0; i++){
			min = Double.MAX_VALUE;
			jMin = -1;
			for (int j = 0; j < n1; j++){
				//if this distance is < min, set jMin to j
				if (distances[i][j] < min){
					jMin = j;
					min = distances[i][j];
					}
				}
			//map i to jMin
			map0[i] = jMin;
			mapped1[jMin] = true;
			}
		
		//map polys1 to polys0
		for (int j = 0; j < n1; j++){
			min = Double.MAX_VALUE;
			iMin = -1;
			for (int i = 0; i < n0; i++){
				if (distances[i][j] < min){
					iMin = i;
					min = distances[i][j];
					}
				}
			map1[j] = iMin;
			mapped0[iMin] = true;
			}
		
		//for each poly in polys1, attempt to map to a poly in polys0. Map if:
		//1. The closest counterpart to j in polys0 has j as its closest counterpart (mutually closest)
		//2. The closest counterpart in j in polys0 doesn't have j as its closest counterpart, and the
		//   closest counterpart it does have is already mapped
		for (int j = 0; j < n1; j++){
			int i = map1[j];
			//if (map0[i] == j ^ mapped1[map0[i]])
				if (!isReversed)
					map_polys[i][j] = true;
				else
					map_polys[j][i] = true;
			}
		
		return map_polys;
	}
	
	protected double[][] getPolygonDistances(ArrayList<Polygon2D> polys0, ArrayList<Polygon2D> polys1){
		
		//TODO: align overall centers of mass and apply offset to distance calculations?
		
		double[][] distances = new double[polys0.size()][polys1.size()];
		
		Point2f[] centers0 = new Point2f[polys0.size()];
		Point2f[] centers1 = new Point2f[polys1.size()];
		
		for (int i = 0; i < polys0.size(); i++)
			centers0[i] = polys0.get(i).getCenterPt();
		for (int i = 0; i < polys1.size(); i++)
			centers1[i] = polys1.get(i).getCenterPt();
		
		for (int i = 0; i < polys0.size(); i++)
			for (int j = 0; j < polys1.size(); j++)
				distances[i][j] = centers0[i].distance(centers1[j]);
		
		return distances;
	}
	
	
	/*********
	 * Given a list of mapped polygons, assign tangents to nodes based upon the mapping
	 * vectors. Tangents can be used to smooth (spline) these lines and thus smooth a
	 * given morphing transition.
	 * @param maps - a list of MappedPolygon2D objects 
	 * @return maps with tangent values assigned (using the "TangentX" and "TangentY"
	 * Attributes).
	 */
	
	public ArrayList<MappedPolygon2DPath> getNodeTangents(ArrayList<MappedPolygon2DPath> paths, 
			  											  double spacing, double endFactor){
	
		//note: Z-axis is considered as the direction of this section set's normal vector
		//don't ask why I've reversed X and Y here...
		MorphNodeMap2D thisNodeMap;
		double angleX, angleY;
		double deltaX, deltaY;
		Vector2d axis = new Vector2d(1,0);
		
		for (int p = 0; p < paths.size(); p++){
			
			ArrayList<MappedPolygon2D> maps = paths.get(p).maps;
			//for each polygon map, set angle from previous and next nodes (i, i + 1)
			//for each node in polygon i = 1, .., n - 1, calculate tangent from previous
			//and current angle
			for (int i = 0; i < maps.size(); i++){
				for (int j = 0; j < maps.get(i).nodeMaps.size(); j++){
					thisNodeMap = maps.get(i).nodeMaps.get(j);
		
					if (thisNodeMap == null)
						i = i;
					
					if (thisNodeMap.getNext() == null)
						i = i;
					
					deltaY = thisNodeMap.getNext().thisPt.y - thisNodeMap.getPrev().thisPt.y;
					deltaX = thisNodeMap.getNext().thisPt.x - thisNodeMap.getPrev().thisPt.x;
					
					//Y angle is angle of vector spacing, deltaY
					angleY = axis.angle(new Vector2d(spacing, deltaY));
					angleX = axis.angle(new Vector2d(deltaX, deltaY));
					
					//assign to next node
					thisNodeMap.getNext().tangentY = angleY;
					thisNodeMap.getNext().tangentX = angleX;
					
					//if this is the first polygon, assign tangent as angle * endFactor
					if (i == 0){
						thisNodeMap.getPrev().setTangentY(angleY * endFactor);
						thisNodeMap.getPrev().setTangentX(angleX * endFactor);
						}
					
					//otherwise, assign tangent from source and present angle
					else {
						thisNodeMap.getPrev().setTangentY(GeometryFunctions.getAngleTangent(
															thisNodeMap.getPrev().tangentY, angleY));
						thisNodeMap.getPrev().setTangentX(GeometryFunctions.getAngleTangent(
															thisNodeMap.getPrev().tangentX, angleX));
						}
					
					//if last polygon, simply calculate tangents
					if (i == maps.size() - 1){
						thisNodeMap.getNext().setTangentY(thisNodeMap.getNext().tangentY * endFactor);
						thisNodeMap.getNext().setTangentX(thisNodeMap.getNext().tangentX * endFactor);
						}
					}
				}
			}
		//return maps;
		return paths;
	}
	
	
	/******
	 * Given a set of mapped polygons, generate <iters> new subsections for each section s, which
	 * morph from polygon i	to polygon j.
	 * @param mappedPolys set of polygons whose nodes are mapped between sections
	 * @param iters number of subsections to generate
	 * @param
	 * @return a MorphSections3DInt object containing generated subsections
	 */
	public SectionSet3DInt getSubsections(ArrayList<MappedPolygon2DPath> paths, 
										  int iters,
										  AttributeList attr){
		
		MappedPolygon2D thisPoly;
		MappedPolygon2D itrPoly;
		int nA, nB, deltaN;
		double nodeChange;
		double residual;
		int remNodes;
		MorphNode2D thisNode;
		int thisItr, thisSect;
		int sectVal;
		
		//for splining
		double t;
		Point2f N1y, N2y, N1x, N2x;
		Point2f C1y, C2y, C1x, C2x;
		Point2f splinePtY, splinePtX;
		
		SectionSet3DInt morphSections = new SectionSet3DInt();
		morphSections.setName((String)morphAttr.getValue("ShapeName"));
		
		ArrayList<Point2f> splineXNodes = null, splineYNodes = null;
		
		double minAngle = ((MguiDouble)morphAttr.getValue("AngleThreshold")).getValue();
		double minLength = ((MguiDouble)morphAttr.getValue("LengthThreshold")).getValue();
		
		//for each path
		for (int p = 0; p < paths.size(); p++){
			
			ArrayList<MappedPolygon2D> mappedPolys = paths.get(p).maps;
			
			for (int i = 0; i < mappedPolys.size(); i++){
				thisPoly = mappedPolys.get(i);
				
				//set spline nodes array, if selected
				if (((MguiBoolean)morphAttr.getValue("ApplySpline")).getTrue()){
						//get X, Y points and control points
						//as array {N1i, C1i, N2i, C2i, ... N1n, C1n, N2n, C2n}
						splineXNodes = getSplineXNodes(thisPoly.nodeMaps);
						splineYNodes = getSplineYNodes(thisPoly.nodeMaps);
					}
				
				thisSect = thisPoly.prevSect;
				
				nA = thisPoly.A.vertices.size();
				nB = thisPoly.B.vertices.size();
				itrPoly = (MappedPolygon2D)thisPoly.clone();
				
				thisItr = (iters + 1) * (thisPoly.nextSect - thisPoly.prevSect);
				deltaN = nB - nA;
				
				nodeChange = (double)Math.abs(deltaN) / ((double)thisItr);
				residual = 0;
				
				//add first and last polys
				int sect = thisPoly.prevSect * (iters + 1);
				//int sect1 = thisPoly.nextSect * (iters + 1);
				//if (itrPoly.isReversed)
				//	sect = thisPoly.nextSect * (iters + 1);
					//sect1 = thisPoly.prevSect * (iters + 1);
				
				
				LPolygon2DInt newPoly = new LPolygon2DInt(itrPoly.getOrderedA());
				morphSections.addShape2D(newPoly, sect, true);
				if (((MguiBoolean)morphAttr.getValue("UseSelectionSet")).getTrue())
					((ShapeSelectionSet)morphAttr.getValue("SelectionSet")).addShape(newPoly, false);
				
				
				//add last polygon only if this is the last section in map
				if (i == mappedPolys.size() - 1){
					sect = thisPoly.nextSect * (iters + 1);
					//if (itrPoly.isReversed)
					//	sect = thisPoly.prevSect * (iters + 1);
					itrPoly = (MappedPolygon2D)itrPoly.clone();
					newPoly = new LPolygon2DInt(itrPoly.getOrderedB());
					morphSections.addShape2D(newPoly, sect, true);
					if (((MguiBoolean)morphAttr.getValue("UseSelectionSet")).getTrue())
						((ShapeSelectionSet)morphAttr.getValue("SelectionSet")).addShape(newPoly, false);
					}
					
				
				itrPoly = (MappedPolygon2D)itrPoly.clone();
				
				//for each iteration
				for (int j = 1; j < thisItr; j++){
					
					if (j > 0){
						//calculate how many nodes (if any) to remove this iteration
						remNodes = (int)Math.floor(nodeChange + residual);
						residual = nodeChange + residual - remNodes;
						
						nA = itrPoly.A.vertices.size();
						
						//remove by priority of count
						//TODO: allow choice of priority measures
						if (nA > nB){
							for (int l = 0; l < remNodes; l++){
								int k = itrPoly.removeNodeByCount(minAngle, minLength);
								//if splining, remove spline nodes
								if (k >= 0 && (((MguiBoolean)morphAttr.getValue("ApplySpline")).getTrue())){
									for (int m = 0; m < 4; m++){
										splineXNodes.remove(k * 4);
										splineYNodes.remove(k * 4);
										}
									}
								}
							}
				
						nA = itrPoly.nodeMaps.size();
						
						//for each node in polygon i
						for (int k = 0; k < nA; k++){
							
							//now we can apply the morphing
							//if splining, use (TangentX(i+1) - TangentX(i)) / iters
							//otherwise use (AngleX(i+1) - AngleX(i)) / iters
							thisNode = itrPoly.nodeMaps.get(k).N_A;
							
							//apply cubic spline mapping if selected
							if (((MguiBoolean)morphAttr.getValue("ApplySpline")).getTrue()){
								//apply spline using spline nodes array
								int m = (itrPoly.nodeMaps.get(k).weight.source) * 4;
								//int m = k * 4;
								
								//these assignments make the code nicer =)
								N1x = splineXNodes.get(m);
								C1x = splineXNodes.get(m + 1);
								N2x = splineXNodes.get(m + 2);
								C2x = splineXNodes.get(m + 3);
								N1y = splineYNodes.get(m);
								C1y = splineYNodes.get(m + 1);
								N2y = splineYNodes.get(m + 2);
								C2y = splineYNodes.get(m + 3);
								
								t = (double)j / (double)thisItr;
								
								splinePtX = GeometryFunctions.getCubicSplinePt(N1x, C1x, N2x, C2x, t);
								splinePtY = GeometryFunctions.getCubicSplinePt(N1y, C1y, N2y, C2y, t);
								
								thisNode.thisPt.x = splinePtX.y;
								thisNode.thisPt.y = splinePtY.y;
								
								}
							//otherwise apply linear mapping
							else {
								//change in x, y coordinates between mapped nodes
								double deltaX = itrPoly.nodeMaps.get(k).N_B.thisPt.x - 
												itrPoly.nodeMaps.get(k).N_A.thisPt.x;
								double deltaY = itrPoly.nodeMaps.get(k).N_B.thisPt.y - 
												itrPoly.nodeMaps.get(k).N_A.thisPt.y;
								
								//change in coordinates for this iteration
								thisNode.thisPt.x = (float)(thisNode.thisPt.x + (1 / ((double)thisItr - j) * deltaX));
								thisNode.thisPt.y = (float)(thisNode.thisPt.y + (1 / ((double)thisItr - j) * deltaY));
								
								}
							
							}//each kth node
						}
					
					//add polygon to jth subsection
					//LPolygon2DInt 
					newPoly = new LPolygon2DInt(itrPoly.A);
					
					//if mapping is reversed, add to sections in reversed order
					if (itrPoly.isReversed)
						sectVal = thisItr - j;
					else
						sectVal = j;
					morphSections.addShape2D(newPoly, thisSect * (iters + 1) + sectVal, true);
					if (((MguiBoolean)morphAttr.getValue("UseSelectionSet")).getTrue()){
						/**@TODO move this to the interface panel **/
						((ShapeSelectionSet)morphAttr.getValue("SelectionSet")).addShape(newPoly, false); //,
						}
						
					itrPoly = (MappedPolygon2D)itrPoly.clone();
					}
				}
			}
		
		//morphSections.resetModel();
		return morphSections;
	}
	
	private ArrayList<Point2f> getSplineYNodes(ArrayList<MorphNodeMap2D> maps){
		//for each mapped node, we want to define a start and end node with
		//control points based upon their tangents, in the plane YZ, where
		//Z is the plane normal direction of the section set; note here that
		//the Z-axis acts as an X-axis
		
		Point2f N1, C1, N2, C2;
		ArrayList<Point2f> retList = new ArrayList<Point2f>();
		
		for (int i = 0; i < maps.size(); i++){
			N1 = new Point2f(0, maps.get(i).N_A.thisPt.y);
			N2 = new Point2f(1, maps.get(i).N_B.thisPt.y);
			C1 = new Point2f((float)Math.sin(maps.get(i).N_A.tangentY), 
							 (float)Math.cos(maps.get(i).N_A.tangentY) + 
							 maps.get(i).N_A.thisPt.y);
			C2 = new Point2f(1.0f - (float)Math.cos(maps.get(i).N_B.tangentY),
							 (float)(maps.get(i).N_B.thisPt.y -
							 Math.sin(maps.get(i).N_B.tangentY)));
			retList.add(N1);
			retList.add(C1);
			retList.add(N2);
			retList.add(C2);
			}
		return retList;
	}
	
	private ArrayList<Point2f> getSplineXNodes(ArrayList<MorphNodeMap2D> maps){
		//for each mapped node, we want to define a start and end node with
		//control points based upon their tangents, in the plane XZ, where
		//Z is the plane normal direction of the section set; note that here
		//the Z axis acts as the X axis and the X axis acts as the Y axis.
		
		Point2f N1, C1, N2, C2;
		ArrayList<Point2f> retList = new ArrayList<Point2f>();
		
		for (int i = 0; i < maps.size(); i++){
			N1 = new Point2f(0.0f, maps.get(i).N_A.thisPt.x);
			N2 = new Point2f(1.0f, maps.get(i).N_B.thisPt.x);
			C1 = new Point2f((float)Math.sin(maps.get(i).N_A.tangentX), 
							 (float)Math.cos(maps.get(i).N_A.tangentX) + 
							 maps.get(i).N_A.thisPt.x);
			C2 = new Point2f(1.0f - (float)Math.cos(maps.get(i).N_B.tangentX),
							 (float)(maps.get(i).N_B.thisPt.x -
							 Math.sin(maps.get(i).N_B.tangentX)));
			retList.add(N1);
			retList.add(C1);
			retList.add(N2);
			retList.add(C2);
			}
		return retList;
	}
	
	/*********************
	 * Given a set of sections, map nodes using radial rep and then triangulate between
	 * sections based upon this mapping, and each polygon's direction (CW or CCW)
	 * @param morphSections section set containing polygons (and only polygons) with which to
	 * generate the mesh.
	 * @return Mesh3DInt object representing the surface connecting these section polygons
	 */
	
	public Mesh3DInt getMorphMesh(SectionSet3DInt morphSections, double weight_thres){
			
		//get mapped polygons
		ArrayList<MappedPolygon2DPath> paths = getMappedPolygons(morphSections, 
																 weight_thres,
																 ((MguiBoolean)morphAttr.getValue("MapMulti")).getTrue());
		
		Mesh3D mesh = new Mesh3D();
		
		//surface for each path
		for (int p = 0; p < paths.size(); p++){
			ArrayList<MappedPolygon2D> mappedPolys = paths.get(p).maps; 
				
			if (mappedPolys.size() == 0){
				InterfaceSession.log("Morph Mesh -- No mapped polygons!");
				return null;
			}
			
			Polygon3D polyA = null, polyB = null;
			int dirB;
			int nA, nB;
			float distA, distB;
				
			//For each polygon, create a new polygon with join at map 0
			//Then triangulate using triangulator and GeometryInfo
			//Then take resulting IndexedTriangleArray and create mesh
			for (int i = 0; i < mappedPolys.size(); i++){
				if (!mappedPolys.get(i).isReversed){
					distA = (float)morphSections.getSectionDist(mappedPolys.get(i).prevSect);
					distB = (float)morphSections.getSectionDist(mappedPolys.get(i).nextSect);
				}else{
					distA = (float)morphSections.getSectionDist(mappedPolys.get(i).nextSect);
					distB = (float)morphSections.getSectionDist(mappedPolys.get(i).prevSect);
					}
				
				polyA = ((Polygon3DInt)ShapeFunctions.
						getShape3DIntFromSection((Plane3D)morphSections.shape3d,
												 distA,
												 new Polygon2DInt(mappedPolys.get(i).A))).getPolygon();
				
				polyB = ((Polygon3DInt)ShapeFunctions.
						getShape3DIntFromSection((Plane3D)morphSections.shape3d,
												 distB,
												 new Polygon2DInt(mappedPolys.get(i).B))).getPolygon();
				
				GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
				MappedPolygon2D thisMap = mappedPolys.get(i);
				nA = thisMap.A.vertices.size();
				nB = thisMap.B.vertices.size();
				Point3f[] nodes = null;
				dirB = -1;
				
				if (GeometryFunctions.isClockwise(thisMap.A) != 
					GeometryFunctions.isClockwise(thisMap.B)) dirB = 1;
				int b, b2;
				
				//between each map set
				int NA = 0, k = -1;
				boolean isFound = false, isDone = false, newK = false;
				int thisN = 0, aDiff = 0, bDiff = 0;
				
				while (!isDone){
					isFound = false;
					if (NA == nA) isDone = true;
					while (!isFound && !isDone){
						if (thisMap.targetCounts.get(NA).getInt() == 1)
							isFound = true;
						if (!isFound) NA++;
						isDone = (NA == nA);
						}
					
					if (!isDone){
						k = NA + 1;
						if (k == nA){
							if (newK) isDone = true;
							k = 0;
							newK = true;
						}
						isFound = false;
						while (!isFound && !isDone){
							if (thisMap.targetCounts.get(k).getInt() == 1)
								isFound = true;
							if (!isFound) k++;
							if (k == nA) {
								if (newK) isDone = true;
								newK = true;
								k = 0;
								}
							}
						}
					
					if (!isDone){
						//add surface component
						aDiff = k - NA;
						if (aDiff < 0) aDiff = nA - NA + k;
						b = thisMap.nodeMaps.get(NA).getTarget();
						b2 = thisMap.nodeMaps.get(k).getTarget();
						if (dirB > 0){
							bDiff = b - b2;
							if (bDiff < 0) bDiff = nB - b2 + b;
						}else{
							bDiff = b2 - b;
							if (bDiff < 0) bDiff = nB - b + b2;
							}
						thisN = aDiff + bDiff + 2;
						nodes = new Point3f[thisN];
						int k2;
						
						//add A nodes
						for (int q = 0; q <= aDiff; q++){
							k2 = q + NA;
							if (k2 >= nA) k2 -= nA;
							if (k2 >= polyA.n && !thisMap.isReversed)
								NA = NA;
							if (k2 >= polyB.n && thisMap.isReversed)
								NA = NA;
							if (!thisMap.isReversed)
								nodes[q] = polyA.getVertex(k2);
							else
								nodes[thisN - q - 1] = polyA.getVertex(k2);
							}
						
						//add B nodes
						for (int q = 0; q <= bDiff; q++){
							k2 = b2 + dirB * q;
							if (k2 >= nB) k2 -= nB;
							if (k2 < 0) k2 += nB;
							if (!thisMap.isReversed)
								nodes[q + aDiff + 1] = polyB.getVertex(k2);
							else
								nodes[thisN - (q + aDiff + 1) - 1] = polyB.getVertex(k2);
							}
						}
					
					//triangulate this surface component
					if (nodes != null){
						if (nodes.length > 2){
							gi.reset(GeometryInfo.POLYGON_ARRAY);
							gi.setCoordinates(nodes);
							gi.setStripCounts(new int[]{thisN});
							gi.setContourCounts(new int[]{1});
							gi.convertToIndexedTriangles();
							MeshFunctions.addIndexedTriangleArray(mesh, 
									(IndexedTriangleArray)gi.getIndexedGeometryArray());
							}
						}else{
							//debug
							NA = NA;
							}
					NA++;
					}
			}
			
			InterfaceSession.log("MorphMesh -- Mesh created.");
			
			//merge coincident nodes
			//Mesh3D newmesh = new Mesh3D();
			//newmesh = MeshFunctions.condenseMesh(mesh);
			//Mesh3DInt retMesh = new Mesh3DInt(newmesh);
			
			//return retMesh;
			//return new Mesh3DInt(MeshFunctions.condenseMesh(mesh));
		}
		
		return new Mesh3DInt(MeshFunctions.condenseMesh(mesh));
	}
	
}