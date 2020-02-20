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

package mgui.interfaces.shapes;

import java.awt.Color;

import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

import mgui.geometry.Rect3D;
import mgui.geometry.Shape;
import mgui.interfaces.attributes.Attribute;
import mgui.morph.sections.RadialRep2D;
import mgui.numbers.MguiBoolean;


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
		cAtt.setColor(new Color3f(thisColour));
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