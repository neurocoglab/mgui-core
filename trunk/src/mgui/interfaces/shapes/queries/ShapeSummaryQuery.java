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

import mgui.interfaces.queries.InterfaceQuery;
import mgui.interfaces.queries.QueryResult;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.Shape2DInt;
import mgui.interfaces.shapes.VertexDataColumn;
import mgui.interfaces.shapes.mesh.VertexSelection;
import mgui.numbers.MguiNumber;
import mgui.stats.StatFunctions;

/*********************************************************
 * Extension of {@link InterfaceShapeQuery} to provide summary values for a shape,
 * or a vertex selection within that shape. This includes a vertex count, and means, 
 * variances, and sums for all data columns
 *  
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeSummaryQuery extends InterfaceShapeQuery {

	public ShapeSummaryQuery(String name){
		super(name);
	}

	@Override
	public void setFromShape(InterfaceShape shape){
		
		if (shape instanceof Shape2DInt){
			if (((Shape2DInt)shape).hasParentShape()){
				setFromShape(((Shape2DInt) shape).getParentShape());
				return;
				}
			}
		
		last_shape = shape;
		QueryResult result = new QueryResult(shape);
		
		result.addValue("Name", shape.getName());
		result.addValue("Vertices", shape.getVertexCount());
		
		ArrayList<String> columns = shape.getVertexDataColumnNames();
		
		for (int i = 0; i < columns.size(); i++){
			String column = columns.get(i);
			VertexDataColumn v_column = shape.getVertexDataColumn(column);
			double[] stats = StatFunctions.getBasicNormalStats(v_column.getData());
			result.addValue(column + ".mean", stats[0]);
			result.addValue(column + ".sum", stats[1]);
			result.addValue(column + ".stdev", stats[2]);
			}
		
		
		
		// Selected vertices?
		VertexSelection selection = shape.getVertexSelection();
		int count = selection.getSelectedCount();
		if (count == 0){
			addResult(result);
			return;
			}
		
		result.addValue("Vertices.selected", count);
		
		for (int i = 0; i < columns.size(); i++){
			String column = columns.get(i);
			ArrayList<MguiNumber> selected_values = new ArrayList<MguiNumber>();
			
			for (int j = 0; j < shape.getVertexCount(); j++){
				if (selection.isSelected(j))
					selected_values.add(shape.getDatumAtVertex(column, j));
				}
			
			double[] stats = StatFunctions.getBasicNormalStats(selected_values);
			result.addValue(column + ".selected.mean", stats[0]);
			result.addValue(column + ".selected.sum", stats[1]);
			result.addValue(column + ".selected.stdev", stats[2]);
			}
		
		addResult(result);
		
	}
	
	@Override
	public InterfaceQuery getNewInstance(String name){
		return new ShapeSummaryQuery(name);
	}
	
	@Override
	public String toString(){
		return "Shape Summary Query: " + name; 
	}
	
}