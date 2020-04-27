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

/*
* Copyright (C) 2009 Andrew Reid, University Medical Center Radboud, Nijmegen
* 
* This file is part of modelGUI[core] (mgui-core).
* 
* modelGUI[core] is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* modelGUI[core] is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with modelGUI[core]. If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.ArrayList;

import org.jogamp.java3d.Shape3D;
import org.jogamp.vecmath.Point3d;

import mgui.interfaces.graphics.util.PickInfoShape2D;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.interfaces.tools.graphics.Tool2D;

/******************************************************
 * 2D tool for selecting/deselcting shapes.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ToolSelectShape2D extends Tool2D {

	public static final int SELECT = 0;
	public static final int DESELECT = 1;

	public ShapeSelectionSet selectionSet;
	public Point3d selectedPt;
	public Shape3D selectedShape;
	public int[] selectedNodes;
	
	public int mode = SELECT;
	
	public ToolSelectShape2D(ShapeSelectionSet set){
		this(set, SELECT);
	}
	
	public ToolSelectShape2D(ShapeSelectionSet set, int mode){
		init();
		selectionSet = set;
		setMode(mode);
	}
	
	protected void init(){
		name = "Select Shape 3D";
	}
	
	public void setMode(int m){
		mode = m;
		if (m == SELECT)
			name = "Select Shape 3D";
		else
			name = "Deselect Shape 3D";
	}
	
	public int getMode(){
		return mode;
	}
	
	
	@Override
	public Object clone() {
		return new ToolSelectShape2D(selectionSet, mode);
	}

	@Override
	public void handleToolEvent(ToolInputEvent e) {
		
		switch(e.getEventType()){
			case ToolConstants.TOOL_MOUSE_CLICKED:
		
				ArrayList<PickInfoShape2D> info = targetPanel.getPickShapes(e.getPoint());
				if (info.size() == 0) return;
				selectionSet.addShape(info.get(0).shape);
				
			
		}
		
	}

}