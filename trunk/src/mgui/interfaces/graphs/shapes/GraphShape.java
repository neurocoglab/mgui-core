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

package mgui.interfaces.graphs.shapes;

import java.awt.Graphics;
import java.awt.Graphics2D;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.shapes.Shape2DInt;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;

/***********************************************
 * Abstract class for objects which implement {@link Paintable}; these can be used
 * to render shapes on a graph display window. Shape coordinates are either relative
 * to the screen, or to the graph layout; this is specified by the 'IsScreen' attribute.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class GraphShape extends AbstractInterfaceObject implements Paintable {

	AttributeList attributes;
	Shape2DInt shape;
	DrawingEngine drawing_engine = new DrawingEngine();
	
	@Override
	public void paint(Graphics g) {
		shape.drawShape2D((Graphics2D)g, drawing_engine);
	}

	@Override
	public boolean useTransform() {
		
		return true;
	}

	
	public DrawingEngine getDrawingEngine(){
		return drawing_engine;
	}
	
	

}