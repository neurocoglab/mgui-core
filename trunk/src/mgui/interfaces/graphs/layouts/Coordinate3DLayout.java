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

package mgui.interfaces.graphs.layouts;

import java.util.ArrayList;
import java.util.HashMap;

import javax.media.j3d.BoundingSphere;
import javax.vecmath.Point3f;

import mgui.interfaces.graphs.AbstractGraphEdge;
import mgui.interfaces.graphs.AbstractGraphNode;
import mgui.interfaces.graphs.util.StaticLocationTransformer;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.LazyMap;

import edu.uci.ics.jung.algorithms.layout3d.AbstractLayout;
import edu.uci.ics.jung.graph.Graph;

/********************************************************************
 * Layout for graph nodes in 3D, specifying fixed positions in R3.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Coordinate3DLayout<V extends AbstractGraphNode, E extends AbstractGraphEdge> 
											extends AbstractLayout<V, E> {

	protected Point3f min_pt, max_pt;
	HashMap<V,Point3f> map = new HashMap<V,Point3f>();
	protected boolean initialized;
	StaticLocationTransformer<V> location_transformer = new StaticLocationTransformer<V>();
	
	public Coordinate3DLayout(Graph<V, E> graph) {
		super(graph);
		
		setCoordMap(graph);
		initialized = true;
	}
	
	protected void setCoordMap(Graph<V,E> graph){
		ArrayList<V> vertex_list = new ArrayList<V>(graph.getVertices());
		
		min_pt = new Point3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		max_pt = new Point3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
		
		for (V node : vertex_list){
			Point3f p = node.getLocation();
			map.put(node, p);
			
			min_pt.set(Math.min(min_pt.getX(), p.getX()),
					   Math.min(min_pt.getY(), p.getY()),
					   Math.min(min_pt.getZ(), p.getZ()));
			max_pt.set(Math.max(max_pt.getX(), p.getX()),
			   		   Math.max(max_pt.getY(), p.getY()),
			   		   Math.max(max_pt.getZ(), p.getZ()));
			}
	}
	
	@Override
	public void setSize(BoundingSphere bs) {
		setInitializer(location_transformer);
		super.setSize(bs);
	}
	
	@Override
	public void reset() {
		setInitializer(location_transformer);
		setCoordMap(getGraph());
	}

	@Override
	public void initialize() {
		
		if (!initialized) setCoordMap(getGraph());
		
	}
	
	public void setInitializer(Transformer<V,Point3f> initializer) {
    	//this.locations = LazyMap.decorate(new HashMap<V,Point3f>(locations), initializer);
    }
    
	
	public Point3f transform(V v) {
		return location_transformer.transform(v);
	}
	
}