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

package mgui.interfaces.shapes.queries;

import java.util.ArrayList;
import java.util.TreeSet;

import mgui.interfaces.queries.InterfaceQuery;
import mgui.interfaces.queries.InterfaceQueryObject;
import mgui.interfaces.queries.ObjectNotQueriableException;
import mgui.interfaces.queries.QueryResult;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.Shape2DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.numbers.MguiNumber;

/****************************************************
 * Base class for all shape queries.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0?
 *
 */
public class InterfaceShapeQuery extends InterfaceQuery {
	
	QueryMode mode;

	public static enum QueryMode {
		SingleObject,
		SingleNode,
		MultiObject,
		MultiNode;
	}
	
	protected TreeSet<QueryShapeVertex> vertices = new TreeSet<QueryShapeVertex>();
	
	public InterfaceShape last_shape;
	
	public InterfaceShapeQuery(String name){
		this.name = name;
	}
	
	public Object getLastQueriedObject(){
		return last_shape;
	}
	
	
	public void setFromShape(InterfaceShape shape){
		if (shape instanceof Shape3DInt){
			setFromShape3D((Shape3DInt)shape);
			return;
			}
		
		if (shape instanceof Shape2DInt){
			setFromShape2D((Shape2DInt)shape);
			return;
			}
	}
	
	protected void setFromShape2D(Shape2DInt shape2d){
		
		if (shape2d.hasParentShape()){
			setFromShape3D(shape2d.getParentShape());
			return;
			}
		
		last_shape = shape2d;
		
		QueryResult result = new QueryResult(shape2d);
		result.addValue("Name", shape2d.getName());
		result.addValue("Center", shape2d.getCenterPoint());
		result.addValue("Vertices", shape2d.getVertices().size());
		result.addValue("Parent Set", shape2d.getParentSet());
		
		ArrayList<String> columns = shape2d.getVertexDataColumnNames();
		
		ArrayList<QueryShapeVertex> vertices = this.getVertices();
		for (int i = 0; i < vertices.size(); i++){
			QueryShapeVertex vertex = vertices.get(i);
			//result = new QueryResult(vertex);
			result.addValue("Index", vertex.index);
			result.addValue("Location", shape2d.getVertex(vertex.index));
			
			if (columns != null)
				for (String col : columns){
					Object obj = shape2d.getVertexData(col).get(vertex.index);
					if (shape2d.getNameMap(col) != null){
						obj = obj.toString() + " - " + shape2d.getNameMap(col).get((int)shape2d.getVertexData(col).get(vertex.index).getValue());
						}
					result.addValue(col, obj);
					}
			
			}
		addResult(result);
		
	}
	
	protected void setFromShape3D(Shape3DInt shape3d){
		
		//TODO: 2D parents?
		
		//clearResults();
		last_shape = shape3d;
		
		QueryResult result = new QueryResult(shape3d);
		result.addValue("Name", shape3d.getName());
		result.addValue("Center", shape3d.getCenterOfGravity());
		result.addValue("Vertices", shape3d.getVertexCount());
		result.addValue("Parent Set", shape3d.getParentSet());
		//addResult(result);
		
		//ArrayList<Point3f> points = shape3d.getNodes();
		ArrayList<String> columns = shape3d.getVertexDataColumnNames();
		
		ArrayList<QueryShapeVertex> vertices = this.getVertices();
		for (int i = 0; i < vertices.size(); i++){
			QueryShapeVertex vertex = vertices.get(i);
			//result = new QueryResult(vertex);
			String str = "" + vertex.index;
			if (shape3d instanceof Volume3DInt){
				int[] voxel = ((Volume3DInt)shape3d).getGrid().getIndexAsVoxel(vertex.index);
				if (voxel != null){
					str = voxel[0] + ", " + voxel[1] + ", " + voxel[2];
					}
				}
			result.addValue("Index", str);
			result.addValue("Location", shape3d.getVertex(vertex.index));
			
			if (columns != null)
				for (String col : columns){
					MguiNumber value = shape3d.getDatumAtVertex(col, vertex.index);
					if (shape3d.getNameMap(col) != null){
						double d = value.getValue();
						int index = -Integer.MAX_VALUE;
						//need to map double to integer properly
						if (!Double.isInfinite(d) && !Double.isNaN(d))
							index = (int)d; 
						String s = shape3d.getNameMap(col).get(index);
						if (s == null) 
							s = index + " - N/V";
						else
							s = index + " - " + s;
						result.addValue(col, s);
					}else{
						result.addValue(col, value);
						}
					}
			}
		addResult(result);
	}
	
	@Override
	public boolean canQuery(InterfaceQueryObject obj){
		return obj instanceof InterfaceShape;
	}

	@Override
	public void query(InterfaceQueryObject object) throws ObjectNotQueriableException {
		if (!canQuery(object)) 
			throw new ObjectNotQueriableException("Object of type '" + 
												  object.getClass().getName() + 
												  "' cannot be queried by InterfaceShapeQuery.");
		
		setFromShape((InterfaceShape)object);
		fireListeners();
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode node) {
		node.removeAllChildren();
	}
	
	@Override
	public InterfaceQuery getNewInstance(String name){
		return new InterfaceShapeQuery(name);
	}
	
	public void addQueryVertex(QueryShapeVertex vertex){
		vertices.add(vertex);
	}
	
	public void removeQueryVertex(QueryShapeVertex vertex){
		vertices.remove(vertex);
	}
	
	public ArrayList<QueryShapeVertex> getVertices(){
		return new ArrayList<QueryShapeVertex>(vertices);
	}
	
	public void clearVertices(){
		vertices.clear();
	}
	
	@Override
	public String toString(){
		return "Shape Query: " + name; 
	}
	
}