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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;

import mgui.geometry.Radius2D;
import mgui.geometry.Text2D;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.maps.Map2D;
import mgui.interfaces.shapes.Text2DInt;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;


public class MorphDrawEngine extends DrawingEngine {

	public MorphDrawEngine(){
		super();
		init();
	}
	
	public MorphDrawEngine(Map2D thisMap){
		super(thisMap);
		init();
	}
	
	private void init(){
		drawing_attributes.add(new Attribute("RadiiTrueLineColour", Color.BLACK));
		drawing_attributes.add(new Attribute("RadiiExtLineColour", Color.GRAY));
		drawing_attributes.add(new Attribute("RadiiTrueLineStyle", new BasicStroke()));
		drawing_attributes.add(new Attribute("RadiiExtLineStyle", new BasicStroke()));
		drawing_attributes.add(new Attribute("DrawStyle", new MguiInteger(RadialRep2DInt.DRAW_CIRCLE_EXT)));
		
	}
	
	public void DrawRadialRep2D(Graphics2D g, RadialRep2D thisRadialRep){
		
		//circle fill
		//radii
		//center point
		//circle outline
		//nodes
		switch (((MguiInteger)drawing_attributes.getValue("DrawStyle")).getInt()){
		
		case RadialRep2DInt.DRAW_CIRCLE_EXT:
			DrawRadialRep2DCircleExt(g, thisRadialRep);
		
		}
		
	}
	
	public void DrawRadialRep2DCircleExt(Graphics2D g, RadialRep2D thisRadialRep){
		
		//circle fill (use circle-defined radius)
		if (((MguiBoolean)drawing_attributes.getValue("HasFill")).getTrue())
			drawCircle2D(g, thisRadialRep.circle);
		
		//radii (scale so max radius length = circle.radius
		DrawRadii(g, thisRadialRep, true, true);
		//center point
		
		//circle outline
		drawing_attributes.setValue("HasFill", new MguiBoolean(false));
		drawCircle2D(g, thisRadialRep.circle);
		
		//nodes
		if (((MguiBoolean)drawing_attributes.getValue("ShowNodes")).getTrue()){
			g.setPaint((Color)drawing_attributes.getValue("NodeColour"));
			DrawRadialNodes(g, thisRadialRep, true, ((MguiInteger)drawing_attributes.getValue("NodeSize")).getInt());
		}
		
		//node labels
		if (((MguiBoolean)drawing_attributes.getValue("LabelNodes")).getTrue()){
			Text2DInt thisText = (Text2DInt)drawing_attributes.getValue("LabelObj");
			int textSize = getScreenDist(((Text2D)thisText.thisShape).getBounds().getHeight());
			g.setFont(new Font((String)(thisText.getAttributes().getValue("FontName")),
					           ((MguiInteger)thisText.getAttributes().getValue("FontStyle")).getInt(),
					           textSize));
			g.setPaint((Color)thisText.getAttributes().getValue("LineColour"));
			DrawRadialNodeLabels(g, thisRadialRep, true, thisText.getText());
		}
		
	}

	public void DrawRadii(Graphics2D g, RadialRep2D thisRadialRep, boolean scaleRadii, boolean extRadii){
		//for each radius
		//if scaleRadii: draw radius with new length thisLen / maxLen * scaleRadii
		
		float thisLen;
		Radius2D newRadius;
		double scaleFactor = thisRadialRep.circle.radius / thisRadialRep.maxRadius;
		for (int i = 0; i < thisRadialRep.radii.size(); i++){
			thisLen = thisRadialRep.radii.get(i).length;
			if (scaleRadii) thisLen = (float)(thisLen * scaleFactor);
			g.setPaint((Color)drawing_attributes.getValue("RadiiTrueLineColour"));
			g.setStroke((Stroke)drawing_attributes.getValue("RadiiTrueLineStyle"));
			newRadius = new Radius2D(thisRadialRep.radii.get(i).angle, thisLen);
			drawRadius2D(g, newRadius, thisRadialRep.circle.centerPt);
			if (extRadii){
				g.setPaint((Color)drawing_attributes.getValue("RadiiExtLineColour"));
				g.setStroke((Stroke)drawing_attributes.getValue("RadiiExtLineStyle"));
				if (scaleRadii) 
					thisLen = thisRadialRep.circle.radius - thisLen;
				else
					thisLen =  thisRadialRep.maxRadius - thisLen;
				drawRadius2D(g, new Radius2D(thisRadialRep.radii.get(i).angle, thisLen),
							 newRadius.getEndpoint(thisRadialRep.circle.centerPt));
			}
		}
	}
	
	public void DrawRadialNodes(Graphics2D g, RadialRep2D thisRadialRep, boolean scaleRadii, int nodeSize){
		//for each radius
		//if scaleRadii: draw radius with new length thisLen / maxLen * scaleRadii
		
		float thisLen;
		Radius2D newRadius;
		double scaleFactor = thisRadialRep.circle.radius / thisRadialRep.maxRadius;
		for (int i = 0; i < thisRadialRep.radii.size(); i++){
			thisLen = thisRadialRep.radii.get(i).length;
			if (scaleRadii) thisLen = (float)(thisLen * scaleFactor);
			newRadius = new Radius2D(thisRadialRep.radii.get(i).angle, thisLen);
			drawRadius2DNode(g, newRadius, thisRadialRep.circle.centerPt, nodeSize);
		}
	}
	
	public void DrawRadialNodeLabels(Graphics2D g, RadialRep2D thisRadialRep, boolean scaleRadii, String preStr){
		ArrayList<String> labelStr = (ArrayList<String>)drawing_attributes.getValue("LabelStrings");
		boolean useStrings = (labelStr.size() > 0);
		int offsetX = getScreenDist(((MguiDouble)drawing_attributes.getValue("LabelOffsetX")).getValue());
		int offsetY = getScreenDist(((MguiDouble)drawing_attributes.getValue("LabelOffsetY")).getValue());
		
		float thisLen;
		Radius2D newRadius;
		double scaleFactor = thisRadialRep.circle.radius / thisRadialRep.maxRadius;
		Point thisPt;
		
		for (int i = 0; i < thisRadialRep.radii.size(); i++){
			thisLen = thisRadialRep.radii.get(i).length;
			if (scaleRadii) thisLen = (float)(thisLen * scaleFactor);
			newRadius = new Radius2D(thisRadialRep.radii.get(i).angle, thisLen);
			
			thisPt = getScreenPoint(newRadius.getEndpoint(thisRadialRep.circle.centerPt));
			thisPt.x += offsetX;
			thisPt.y -= offsetY;
			if (useStrings)
				g.drawString(preStr + labelStr.get(i), thisPt.x, thisPt.y);
			else
				g.drawString(preStr + String.valueOf(i), thisPt.x, thisPt.y);
		}
		
	}
	
}