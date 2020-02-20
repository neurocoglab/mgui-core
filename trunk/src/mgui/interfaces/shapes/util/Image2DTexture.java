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
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.vecmath.TexCoord2f;

import mgui.util.ImageFunctions;

/*************************************
 * Creates a Texture2D object from a given image. 
 * 
 * @author Andrew Reid
 *
 */

public class Image2DTexture {

	Texture2D texture;
	
	public float width_ratio = 1f, height_ratio = 1f;
	
	public Image2DTexture(){
		
	}
	
	public Image2DTexture(BufferedImage image){
		setTexture(image, false);
	}
	
	public Image2DTexture(BufferedImage image, boolean hasAlpha){
		setTexture(image, hasAlpha);
	}

	/****************************
	 * Sets the texture for this object from an image.
	 * @param image BufferedImage with which to set this texture
	 */
	public void setTexture(BufferedImage image, boolean hasAlpha){
		ImageComponent2D iComp;
		BufferedImage po2_image = ImageFunctions.getPowerOfTwoImage(image, Double.POSITIVE_INFINITY, ImageFunctions.CENTERED);
		width_ratio =  (float)((double)po2_image.getWidth() / (double)image.getWidth()); 
		height_ratio = (float)((double)po2_image.getHeight() / (double)image.getHeight());
		//BufferedImage po2_image = image;
		
		if (hasAlpha){
			texture = new Texture2D(Texture.BASE_LEVEL,
								    Texture.RGBA,
								    po2_image.getWidth(),
								    po2_image.getHeight());
								    //powerOfTwo(image.getWidth()),
								    //powerOfTwo(image.getHeight()));
			//convert to RGBA?
			//BufferedImage rgba = getRGBtoRGBA(image);
			iComp = new ImageComponent2D(ImageComponent.FORMAT_RGBA, po2_image);
		}else{
			texture = new Texture2D(Texture.BASE_LEVEL,
								    Texture.RGB,
								    po2_image.getWidth(),
								    po2_image.getHeight());
			//convert to RGBA?
			//BufferedImage rgba = getRGBtoRGBA(image);
			iComp = new ImageComponent2D(ImageComponent.FORMAT_RGB, po2_image);
			}
		
		texture.setImage(0, iComp);
		texture.setBoundaryModeS(Texture.CLAMP);
		texture.setBoundaryModeT(Texture.CLAMP);
		texture.setBoundaryColor(0f, 0f, 0f, 1f);
	}
	
	public Texture2D getTexture(){
		return texture;
	}
	
	public TexCoord2f[] getTexCoords(){
		//Rect3D coords should be organized top left to bottom right
	
		
		TexCoord2f[] texPts = {new TexCoord2f(1.0f, 0.0f),
							   new TexCoord2f(0.0f, 0.0f),
							   new TexCoord2f(0.0f, 1.0f),
							   new TexCoord2f(1.0f, 1.0f)};
		
		
		/*
		TexCoord2f[] texPts = {new TexCoord2f(width_ratio, 0.0f),
							   new TexCoord2f(0.0f, 0.0f),
							   new TexCoord2f(0.0f, height_ratio),
							   new TexCoord2f(width_ratio, height_ratio)};
		*/
		
		return texPts;
	}
	
	private int powerOfTwo(int value) {
		int retval = 16;
		while (retval < value)
		    retval *= 2;
		return retval;
	}
	
	
	
	
}