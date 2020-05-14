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

package mgui.interfaces.shapes;

/*******
 * Extends Polygon2DInt simply to add an array of boolean values indicating
 * the state of its individual nodes. Useful for hightlighting nodes.
 */

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import mgui.geometry.Polygon2D;
import mgui.geometry.Shape2D;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiInteger;


public class BoolPolygon2DInt extends Polygon2DInt {

	public ArrayList<MguiBoolean> booleanNodes;
	
	public BoolPolygon2DInt() {
		super();
		init();
	}

	public BoolPolygon2DInt(Polygon2D thisPoly) {
		super(thisPoly);
		init();
	}
	
	public BoolPolygon2DInt(Polygon2DInt thisPoly){
		super((Polygon2D)thisPoly.thisShape);
		attributes = (AttributeList)thisPoly.attributes.clone();
		init();
	}
	
	private void init(){
		Circle2DInt trueShape = new Circle2DInt();
		trueShape.setAttribute("HasFill", new MguiBoolean(true));
		trueShape.setAttribute("FillColour", Color.RED);
		Circle2DInt falseShape = new Circle2DInt();
		trueShape.setAttribute("HasFill", new MguiBoolean(true));
		trueShape.setAttribute("FillColour", Color.GREEN);
		
		attributes.add(new Attribute("HighlightTrue", new MguiBoolean(true)));
		attributes.add(new Attribute("HighlightFalse", new MguiBoolean(false)));
		attributes.add(new Attribute("TrueShape", trueShape));
		attributes.add(new Attribute("FalseShape", falseShape));
		attributes.add(new Attribute("HighlightSize", new MguiInteger(7)));
		
		resetNodes();
	}
	
	public void resetNodes(){
		booleanNodes = new ArrayList<MguiBoolean>();
		if (this.thisShape != null){
			for (int i = 0; i < ((Polygon2D)thisShape).vertices.size(); i++)
				booleanNodes.add(new MguiBoolean(false));
		}
	}
	
	@Override
	public void setShape(Shape2D thisShape){
		super.setShape(thisShape);
		init();
	}

	public void setNodeBool(int node, boolean bool){
		if (node > -1 && node < booleanNodes.size())
			booleanNodes.get(node).setTrue(bool);
	}
	
	public boolean getNodeBool(int node){
		if (node > -1 && node < booleanNodes.size())
			return booleanNodes.get(node).getTrue();
		return false;
	}
	
	@Override
	public void drawShape2D(Graphics2D g, DrawingEngine d){
		super.drawShape2D(g, d);
		Circle2DInt thisCircle;
		int size = ((MguiInteger)attributes.getValue("HighlightSize")).getInt();
		if (((MguiBoolean)attributes.getValue("HighlightTrue")).getTrue())
			//draw true shape for all true nodes
			for (int i = 0; i < this.getPolygon().vertices.size(); i++)
				if ((booleanNodes.get(i)).getTrue()){
					thisCircle = (Circle2DInt)attributes.getValue("TrueShape");
					thisCircle.getCircle().centerPt.x = this.getPolygon().vertices.get(i).x;
					thisCircle.getCircle().centerPt.y = this.getPolygon().vertices.get(i).y;
					thisCircle.getCircle().radius = d.getMap().getMapDist(size);
					thisCircle.drawShape2D(g, d);
					}
		if (((MguiBoolean)attributes.getValue("HighlightFalse")).getTrue())
			//draw true shape for all true nodes
			for (int i = 0; i < this.getPolygon().vertices.size(); i++)
				if (!(booleanNodes.get(i)).getTrue()){
					thisCircle = (Circle2DInt)attributes.getValue("FalseShape");
					thisCircle.getCircle().centerPt.x = this.getPolygon().vertices.get(i).x;
					thisCircle.getCircle().centerPt.y = this.getPolygon().vertices.get(i).y;
					thisCircle.getCircle().radius = d.getMap().getMapDist(size);
					thisCircle.drawShape2D(g, d);
					}
		
	}
	
	@Override
	public String toString(){
		return "BoolPolygon2DInt [" + String.valueOf(ID) + "]"; 
	}
	
}