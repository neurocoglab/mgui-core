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
import java.util.ArrayList;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.QuadArray;
import org.jogamp.java3d.Shape3D;
import org.jogamp.vecmath.Point3f;

import mgui.geometry.Box3D;
import mgui.geometry.Shape;
import mgui.util.Colours;


public class Box3DInt extends Shape3DInt {

	public Box3DInt(){
		//super();
		init();
	}
	
	public Box3DInt(Box3D b){
		//super();
		init();
		shape3d = b;
	}
	
	@Override
	protected void init(){
		super.init();
		attributes.setValue("2D.LineColour", Color.BLUE);
		//TODO set up fill, etc.
	}
	
	public Box3D getBox(){
		return (Box3D)getShape();
	}
	
	@Override
	public Shape getGeometryInstance(){
		return new Box3D();
	}
	
	protected QuadArray getFaces(){
		Box3D box = getBox();
		
		//points
		ArrayList<Point3f> points = box.getVertices();
		Point3f[] coords = new Point3f[4];
		
		//faces (6 of them)
		QuadArray quads = new QuadArray(24, GeometryArray.COORDINATES);
		//F1
		coords[0] = points.get(0);
		coords[1] = points.get(3);
		coords[2] = points.get(4);
		coords[3] = points.get(1);
		quads.setCoordinates(0 * 4, coords);
		//F2
		coords[0] = points.get(1);
		coords[1] = points.get(4);
		coords[2] = points.get(7);
		coords[3] = points.get(6);
		quads.setCoordinates(1 * 4, coords);
		//F3
		coords[0] = points.get(6);
		coords[1] = points.get(7);
		coords[2] = points.get(5);
		coords[3] = points.get(2);
		quads.setCoordinates(2 * 4, coords);
		//F4
		coords[0] = points.get(0);
		coords[1] = points.get(3);
		coords[2] = points.get(5);
		coords[3] = points.get(2);
		quads.setCoordinates(3 * 4, coords);
		//F5
		coords[0] = points.get(3);
		coords[1] = points.get(5);
		coords[2] = points.get(7);
		coords[3] = points.get(4);
		quads.setCoordinates(4 * 4, coords);
		//F6
		coords[0] = points.get(1);
		coords[1] = points.get(6);
		coords[2] = points.get(2);
		coords[3] = points.get(0);
		quads.setCoordinates(5 * 4, coords);
		
		return quads;
	}
	
	@Override
	public void setScene3DObject(){
		
		if (!is_auxiliary && (getModel() == null || !getModel().isLive3D()))
			return;
		
		super.setScene3DObject();
		if (scene3DObject == null) return;
		
		//draw box as 6 quads
		//Box3D box = ((Grid3D)thisShape).bounds;
		QuadArray quads = getFaces();
		
		//set up nodes
		Shape3D thisShapeNode = new Shape3D(quads);
		Appearance thisAppNode = new Appearance();
		PolygonAttributes pAtt = new PolygonAttributes();
		//pAtt.setCullFace(PolygonAttributes.CULL_NONE);
		//pAtt.setBackFaceNormalFlip(true);
		pAtt.setPolygonMode(PolygonAttributes.POLYGON_LINE);
		Color edgeColour = (Color)attributes.getValue("3D.LineColour");
		ColoringAttributes cAtt = new ColoringAttributes();
		cAtt.setColor(Colours.getColor3f(edgeColour));
		thisAppNode.setPolygonAttributes(pAtt);
		thisAppNode.setColoringAttributes(cAtt);
		thisShapeNode.setAppearance(thisAppNode);
		
		scene3DObject.addChild(thisShapeNode);
	
	
		setShapeSceneNode();
	}
	
	
}