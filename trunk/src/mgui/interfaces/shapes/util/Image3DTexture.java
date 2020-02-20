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

package mgui.interfaces.shapes.util;

import java.awt.image.BufferedImage;

import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture3D;
import javax.vecmath.TexCoord3f;

public class Image3DTexture {

	public Texture3D texture;
	public int width, height, depth;
	
	public Image3DTexture(){
		
	}
	
	public Image3DTexture(BufferedImage image, int d){
		depth = powerOfTwo(d);
		setTexture(image, false);
	}
	
	public Image3DTexture(BufferedImage image, int d, boolean hasAlpha){
		depth = powerOfTwo(d);
		setTexture(image, hasAlpha);
	}

	/****************************
	 * Sets the texture for this object from an image.
	 * @param image BufferedImage with which to set this texture
	 */
	public void setTexture(BufferedImage image, boolean hasAlpha){
		ImageComponent3D iComp;
		width =  powerOfTwo(image.getWidth());
		height = powerOfTwo(image.getHeight());
		if (hasAlpha){
			texture = new Texture3D(Texture.BASE_LEVEL,
								    Texture.RGBA,
								    width,
								    height,
								    depth);
			//convert to RGBA?
			//BufferedImage rgba = getRGBtoRGBA(image);
			iComp = new ImageComponent3D(ImageComponent.FORMAT_RGBA, width, height, depth);
			
			
		}else{
			texture = new Texture3D(Texture.BASE_LEVEL,
								    Texture.RGB,
								    width,
								    height,
								    depth);
			//convert to RGBA?
			//BufferedImage rgba = getRGBtoRGBA(image);
			iComp = new ImageComponent3D(ImageComponent.FORMAT_RGB, width, height, depth);
			}
		
		for (int k = 0; k < depth; k++)
			iComp.set(k, image);
		texture.setImage(0, iComp);
	}
	
	public Texture3D getTexture(){
		return texture;
	}
	
	public TexCoord3f[] getTexCoords(int i){
		//Rect3D coords should be organized top left to bottom right
		if (i < 0 || i > depth) return null;
		float z = (float)i / (float)depth;
		TexCoord3f[] texPts = {new TexCoord3f(0.0f, 0.0f, z),
							   new TexCoord3f(0.0f, 1.0f, z),
							   new TexCoord3f(1.0f, 1.0f, z),
							   new TexCoord3f(1.0f, 0.0f, z)};
		return texPts;
	}
	
	
	
	private int powerOfTwo(int value) {
		int retval = 16;
		while (retval < value)
		    retval *= 2;
		return retval;
	}
	
}