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

import java.awt.image.BufferedImage;

import javax.media.j3d.Appearance;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import mgui.geometry.Rect3D;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.shapes.util.Image2DTexture;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;


/*********************************
 * Draws a rectangular planar image in R3.
 *  
 * @author Andrew Reid
 * @version 1.0
 *
 */

public class Image3DInt extends Rect3DInt {

	Image2DTexture texture;
	
	public Image3DInt(){
		//super();
		//thisShape = new Rect3D();
		init();
	}
	
	public Image3DInt(Rect3D thisRect, BufferedImage image){
		this(thisRect, image, false);
	}
	
	public Image3DInt(Rect3D thisRect, BufferedImage image, boolean hasAlpha){
		setShape(thisRect);
		init();
		setImage(image, hasAlpha);
	}
	
	public void setImage(BufferedImage image){
		texture = new Image2DTexture(image, hasAlpha());
	}
	
	public void setImage(BufferedImage image, boolean hasAlpha){
		hasAlpha(hasAlpha);
		setImage(image);
	}
	
	@Override
	protected void init(){
		super.init();
		//set up attributes here
		//attributes.add(new Attribute("HasBorder", new MguiBoolean(false)));
		//attributes.add(new Attribute("HasAlpha", new arBoolean(false)));
		updateShape();
	}
	
	@Override
	public void setScene3DObject(){
		//scene3DObject = new BranchGroup();
		super.setScene3DObject();
		if (scene3DObject == null) return;
		if (!this.isVisible() || texture == null) return;
		
		QuadArray quadArray = new QuadArray(4, 
											GeometryArray.COORDINATES | 
											GeometryArray.TEXTURE_COORDINATE_2);
		Point3f[] coords = new Point3f[4];
		shape3d.getVertices().toArray(coords);
		
		//Need to expand rectangle to accommodate power-of-two adjustment (blagh)
		expandRect(coords);
		
		quadArray.setCoordinates(0, coords);
		quadArray.setTextureCoordinates(0, 0, texture.getTexCoords());
		
		Shape3D thisShapeNode = new Shape3D(quadArray);
		Appearance thisAppNode = new Appearance();
		
		//set border here
		thisAppNode.setMaterial(new Material());
		
		//set texture here
		thisAppNode.setTexture(texture.getTexture());
		TextureAttributes texAtt = new TextureAttributes();
		texAtt.setTextureMode(TextureAttributes.REPLACE);
		//texAtt.setCombineAlphaSource(arg0, arg1)
		PolygonAttributes pAtt = new PolygonAttributes();
		pAtt.setCullFace(PolygonAttributes.CULL_NONE);
		pAtt.setBackFaceNormalFlip(true);
		thisAppNode.setPolygonAttributes(pAtt);
		
		TransparencyAttributes tAtt = new TransparencyAttributes();
		tAtt.setTransparencyMode(TransparencyAttributes.BLENDED);
		tAtt.setSrcBlendFunction(TransparencyAttributes.BLEND_SRC_ALPHA);
		
		//set transparency here
		if (((MguiBoolean)attributes.getValue("3D.HasAlpha")).getTrue()){
			float alpha = ((MguiFloat)attributes.getValue("3D.Alpha")).getFloat();
			//alpha is switched for some reason
			tAtt.setTransparency(-alpha + 1);
			}
		
		thisAppNode.setTransparencyAttributes(tAtt);
		thisAppNode.setTextureAttributes(texAtt);
		thisShapeNode.setAppearance(thisAppNode);
		scene3DObject.addChild(thisShapeNode);
		setShapeSceneNode();
	}
	
	//expand rectangle using the power-of-two ratios in the texture
	//very annoying problem to satisfy renderers which require p-o-2
	//images
	private void expandRect(Point3f[] coords){
		
		float h_ratio = (float)((texture.height_ratio - 1.0) / 2.0);
		float w_ratio = (float)((texture.width_ratio - 1.0) / 2.0);
		
		//first node
		Point3f p = coords[0];
		Vector3f v = new Vector3f(p);
		v.sub(coords[3]);
		float factor = v.length();
		v.normalize();
		v.scale(w_ratio * factor);
		p.add(v);
		v = new Vector3f(coords[0]);
		v.sub(coords[1]);
		factor = v.length();
		v.normalize();
		v.scale(h_ratio * factor);
		p.add(v);
		
		//second node
		p = coords[1];
		v = new Vector3f(p);
		v.sub(coords[2]);
		factor = v.length();
		v.normalize();
		v.scale(w_ratio * factor);
		p.add(v);
		v = new Vector3f(coords[1]);
		v.sub(coords[0]);
		factor = v.length();
		v.normalize();
		v.scale(h_ratio * factor);
		p.add(v);
		
		//third node
		p = coords[2];
		v = new Vector3f(p);
		v.sub(coords[1]);
		factor = v.length();
		v.normalize();
		v.scale(w_ratio * factor);
		p.add(v);
		v = new Vector3f(coords[2]);
		v.sub(coords[3]);
		factor = v.length();
		v.normalize();
		v.scale(h_ratio * factor);
		p.add(v);
		
		//fourth node
		p = coords[3];
		v = new Vector3f(p);
		v.sub(coords[0]);
		factor = v.length();
		v.normalize();
		v.scale(w_ratio * factor);
		p.add(v);
		v = new Vector3f(coords[3]);
		v.sub(coords[2]);
		factor = v.length();
		v.normalize();
		v.scale(h_ratio * factor);
		p.add(v);
		
	}
	
	@Override
	public String toString(){
		return "Image3DInt [" + this.ID + "]";
	}
	
}