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

package mgui.interfaces.tools.shapes;

import java.util.ArrayList;

import mgui.interfaces.graphics.util.PickInfoShape2D;
import mgui.interfaces.queries.InterfaceQuery;
import mgui.interfaces.queries.ObjectNotQueriableException;
import mgui.interfaces.shapes.queries.InterfaceShapeQuery;
import mgui.interfaces.shapes.queries.QueryShapeVertex;
import mgui.interfaces.shapes.queries.InterfaceShapeQuery.QueryMode;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.interfaces.tools.ToolListener;
import mgui.interfaces.tools.graphics.Tool2D;
import mgui.interfaces.tools.queries.QueryTool;

/*************************************************************
 * Tool allowing point-and-click vertex-wise querying of 2D objects, for a {@linkplain InterfaceGraphic3D} window.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ToolQueryShape2D extends Tool2D implements QueryTool {

	protected transient InterfaceShapeQuery current_query;
	
	QueryMode mode;
	
	public ToolQueryShape2D(InterfaceShapeQuery query){
		this(QueryMode.SingleObject, query);
	}
	
	public ToolQueryShape2D(QueryMode mode, InterfaceShapeQuery query){
		setMode(mode);
		current_query = query;
		init();
	}
	
	@Override
	public boolean isExclusive(){
		return false;
	}
	
	public void setMode(QueryMode mode){
		this.mode = mode;
		
	}
	
	private void init(){
		name = "Query Shape 2D";
	}
	
	public InterfaceQuery getQuery(){
		return current_query;
	}
	
	@Override
	public void handleToolEvent(ToolInputEvent e){
		
		switch(e.getEventType()){
			case ToolConstants.TOOL_MOUSE_CLICKED:
			
				ArrayList<PickInfoShape2D> info = targetPanel.getPickShapes(e.getPoint());
				if (info.size() == 0) return;
				
				//TODO: implement support for parent shapes
				
				switch (mode){
					case SingleObject:
						current_query.clearVertices();
						
						//query and fire listeners
						try{
							current_query.query(info.get(0).shape);
						}catch (ObjectNotQueriableException ex){
							//won't happen
							ex.printStackTrace();
							}
						break;
						
					case SingleNode:
						
						current_query.clearVertices();
						current_query.addQueryVertex(new QueryShapeVertex(info.get(0).shape, info.get(0).closest_vertex));
					
						//query and fire listeners
						try{
							current_query.query(info.get(0).shape);
						}catch (ObjectNotQueriableException ex){
							//won't happen
							ex.printStackTrace();
							}
					}
			}
		
	}
	
	@Override
	public Object clone(){
		ToolQueryShape2D tool = new ToolQueryShape2D(mode, current_query);
		tool.listeners = new ArrayList<ToolListener>(listeners);
		return tool;
	}
	
}