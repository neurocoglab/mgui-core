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

import java.awt.Color;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.QuadArray;
import org.jogamp.java3d.Shape3D;
import org.jogamp.vecmath.Point3f;

import mgui.geometry.Rect3D;
import mgui.geometry.Shape;
import mgui.interfaces.attributes.Attribute;
import mgui.numbers.MguiBoolean;
import mgui.util.Colours;


public class Rect3DInt extends Shape3DInt {

	public Rect3DInt(){
		//super();
		shape3d = new Rect3D();
		init();
	}
	
	public Rect3DInt(Rect3D thisRect){
		//super();
		shape3d = thisRect;
		init();
	}
	
	@Override
	public Shape getGeometryInstance(){
		return new Rect3D();
	}
	
	@Override
	protected void init(){
		super.init();
		//set up attributes here
		attributes.add(new Attribute("HasFill", new MguiBoolean(false)));
		attributes.add(new Attribute("FillColour", Color.WHITE));
		attributes.add(new Attribute("ShowNodes", new MguiBoolean(true)));
		attributes.add(new Attribute("NodeColour", Color.BLUE));
		updateShape();
	}
	
	public Rect3D getRect(){
		return (Rect3D)shape3d;
	}
	
	@Override
	public void setScene3DObject(){
		//scene3DObject = new BranchGroup();
		super.setScene3DObject();
		if (scene3DObject == null) return;
		if (!this.isVisible()) return;
		
		Point3f[] coords = new Point3f[4];
		shape3d.getVertices().toArray(coords);
		
		Shape3D thisShapeNode;
		Appearance thisAppNode;
		ColoringAttributes cAtt = new ColoringAttributes();;
		PolygonAttributes pAtt = new PolygonAttributes();
		boolean hasFill = ((MguiBoolean)attributes.getValue("HasFill")).getTrue();
		Color thisColour = (Color)attributes.getValue("FillColour");
		
		if (!hasFill){
			thisColour = (Color)attributes.getValue("LineColour");
			pAtt.setPolygonMode(PolygonAttributes.POLYGON_LINE);
			}
		
		QuadArray quadArray = new QuadArray(4, GeometryArray.COORDINATES);
		quadArray.setCoordinates(0, coords);
		thisShapeNode = new Shape3D(quadArray);
		cAtt.setColor(Colours.getColor3f(thisColour));
		pAtt.setCullFace(PolygonAttributes.CULL_NONE);
		pAtt.setBackFaceNormalFlip(true);
	
		thisAppNode = new Appearance();
		thisAppNode.setColoringAttributes(cAtt);
		thisAppNode.setMaterial(new Material());
		thisAppNode.setPolygonAttributes(pAtt);
		thisShapeNode.setAppearance(thisAppNode);
		
		scene3DObject.addChild(thisShapeNode);
		this.setShapeSceneNode();
	}
	
	@Override
	public String toString(){
		return "Rect3DInt [" + this.ID + "]";
	}
	
}