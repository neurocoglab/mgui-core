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

import java.awt.image.BufferedImage;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.QuadArray;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.TextureAttributes;
import org.jogamp.java3d.TransparencyAttributes;
import org.jogamp.vecmath.Point3f;

import mgui.geometry.Rect3D;
import mgui.geometry.Sphere3D;
import mgui.interfaces.shapes.util.Image3DTexture;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;


public class TestTextureInt extends Rect3DInt {

	Image3DTexture texture;
	
	public TestTextureInt(){
		//super();
		init();
	}
	
	public TestTextureInt(Rect3D p, BufferedImage image, int depth){
		//super(p);
		this(p, image, depth, false);
		//setShape(p);
		//setImage(image, depth);
		//init();
	}
	
	public TestTextureInt(Rect3D p, BufferedImage image, int depth, boolean hasAlpha){
		//super(p);
		setShape(p);
		init();
		hasAlpha(hasAlpha);
		setImage(image, depth);
	}
	
	/*
	public void setAlpha(boolean a){
		((arBoolean)attributes.getValue("HasAlpha")).value = a;
	}
	
	public boolean getAlpha(){
		return ((arBoolean)attributes.getValue("HasAlpha")).value;
	}
	*/
	
	@Override
	protected void init(){
		super.init();
		//attributes.add(new Attribute("HasAlpha", new arBoolean(false)));
		updateShape();
	}
	
	public void setImage(BufferedImage image, int depth){
		texture = new Image3DTexture(image, depth, hasAlpha());
	}
	
	@Override
	public void setScene3DObject(){
		scene3DObject = new BranchGroup();
		if (!this.isVisible() || texture == null) return;
		
		QuadArray quadArray = new QuadArray(4, 
											GeometryArray.COORDINATES | 
											GeometryArray.TEXTURE_COORDINATE_3);
		Point3f[] coords = new Point3f[4];
		shape3d.getVertices().toArray(coords);
		
		//set depth as radius
		Sphere3D sphere = this.getBoundSphere();
		float offset = sphere.radius / texture.depth;
		
		Shape3D thisShapeNode; // = new Shape3D(quadArray);
		Appearance thisAppNode = new Appearance();
		
		thisAppNode.setMaterial(new Material());
		
		//set texture here
		thisAppNode.setTexture(texture.getTexture());
		
		//texAtt.setCombineAlphaSource(arg0, arg1)
		PolygonAttributes pAtt = new PolygonAttributes();
		pAtt.setCullFace(PolygonAttributes.CULL_NONE);
		//pAtt.setBackFaceNormalFlip(true);
		thisAppNode.setPolygonAttributes(pAtt);
		
		TransparencyAttributes tAtt = new TransparencyAttributes();
		tAtt.setTransparencyMode(TransparencyAttributes.BLENDED);
		tAtt.setSrcBlendFunction(TransparencyAttributes.BLEND_SRC_ALPHA);
		
		//set transparency here
		if (((MguiBoolean)attributes.getValue("HasTransparency")).getTrue()){
			float alpha = ((MguiFloat)attributes.getValue("Alpha")).getFloat();
			//alpha is switched for some reason
			tAtt.setTransparency(-alpha + 1);
			}
		
		thisAppNode.setTransparencyAttributes(tAtt);
		TextureAttributes texAtt = new TextureAttributes();
		texAtt.setTextureMode(TextureAttributes.REPLACE);
		thisAppNode.setTextureAttributes(texAtt);
		
		for (int i = 0; i < texture.depth; i++){
			quadArray = new QuadArray(4, 
									  GeometryArray.COORDINATES | 
									  GeometryArray.TEXTURE_COORDINATE_3);
			quadArray.setCoordinates(0, coords);
			quadArray.setTextureCoordinates(0, 0, texture.getTexCoords(i));
			thisShapeNode = new Shape3D(quadArray);
			thisShapeNode.setAppearance(thisAppNode);
			scene3DObject.addChild(thisShapeNode);
			//offset z coordinate (todo make this offset in normal of rect plane,
			//for now we assume z-axis)
			for (int j = 0; j < coords.length; j++)
				coords[j].z += offset;
			}
		
	}
	
	//todo override this to factor in depth, or else use a Box3D object
	//public void updateShape() {
		
		
	//}
	
	@Override
	public String toString(){
		return "Test Volume3DInt [" + this.ID + "]";
	}
	
}