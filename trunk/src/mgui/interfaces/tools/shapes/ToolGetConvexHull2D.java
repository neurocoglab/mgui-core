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

import org.jogamp.vecmath.Point2d;
import org.jogamp.vecmath.Point2f;

import mgui.geometry.Polygon2D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.shapes.Polygon2DInt;
import mgui.interfaces.tools.dialogs.DialogTool;
import mgui.interfaces.tools.dialogs.DialogToolDialogBox;


public class ToolGetConvexHull2D extends DialogTool {

	public ToolGetConvexHull2D(){
		init();
	}
	
	@Override
	public Object clone() {
		return null;
	}

	@Override
	protected boolean doTaskBlocking(InterfaceOptions options, InterfaceProgressBar progress_bar) {
		if (options == null){
			message = "Options not set!";
			return false;
			}
		
		ToolGetConvexHull2DOptions _options = (ToolGetConvexHull2DOptions)options;
		if (_options.section_set == null){
			message = "Section set not specified!";
			return false;
			}
		
		if (_options.point_set == null){
			message = "Point set not specified!";
			return false;
			}
		
		ArrayList<Point2f> nodes = _options.point_set.getShape().getVertices();
		ArrayList<Point2d> d_nodes = new ArrayList<Point2d>(nodes.size());
		for (Point2f p : nodes)
			d_nodes.add(new Point2d(p));
		
		Polygon2D hull = GeometryFunctions.getConvexHull(d_nodes);
		if (hull == null){
			message = "Convex hull failed!";
			return false;
			}
		
		Polygon2DInt poly_int = new Polygon2DInt(hull);
		poly_int.setName(_options.name);
		_options.section_set.addShape2D(poly_int, _options.section, true);
		
		message = "Convex hull created.";
		return true;
	}

	@Override
	protected DialogToolDialogBox getDialogBox() {
		return new ToolGetConvexHull2DDialog(InterfaceSession.getSessionFrame(), new ToolGetConvexHull2DOptions());
	}

	@Override
	protected String getToolTitle() {
		return "Get Convex Hull 2D";
	}

}