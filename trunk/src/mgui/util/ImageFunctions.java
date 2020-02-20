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

package mgui.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.Hashtable;

import javax.swing.filechooser.FileFilter;

import mgui.interfaces.Utility;

/******************************************************
 * Utility class for imaging-related functions.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class ImageFunctions extends Utility{

	public static final int CENTERED = 0;
	public static final int TOP_LEFT = 1;
	
	/***********************************
	 * Set all values in image to value
	 * @param image
	 * @param value
	 */
	public static void fillImage(BufferedImage image, double value){
		WritableRaster raster = image.getRaster();
		double[] dummy = null;
		
		for (int i = 0; i < image.getWidth(); i++)
			for (int j = 0; j < image.getHeight(); j++){
				double[] pixel = raster.getPixel(i, j, dummy);
				for (int m = 0; m < pixel.length; m++)
					pixel[m] = value;
				raster.setPixel(i, j, pixel);
				}
	}
	
	/***********************************
	 * Returns a power-of-two dimensioned version of <code>image</code>, with its boundaries
	 * filled with <code>filler</code>.
	 * 
	 * @param image
	 * @param filler
	 * @param position (one of CENTERED, TOP_LEFT, etc.)
	 * @return
	 */
	public static BufferedImage getPowerOfTwoImage(BufferedImage image, double filler, int pos){
		
		String[] pnames = image.getPropertyNames();
		Hashtable<String, Object> cproperties = new Hashtable<String, Object>();
		if(pnames != null) {
			for(int i = 0; i < pnames.length; i++) {
				cproperties.put(pnames[i], image.getProperty(pnames[i]));
				}
			}
		WritableRaster wr = image.getRaster();
		
		int width = getPowerOfTwo(image.getWidth());
		int height = getPowerOfTwo(image.getHeight());
		WritableRaster cwr = wr.createCompatibleWritableRaster(width, height);
		
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++){
				double[] pixel = cwr.getPixel(i, j, (double[])null);
				for (int m = 0; m < pixel.length; m++)
					pixel[m] = filler;
				cwr.setPixel(i, j, pixel);
				}
		
		switch (pos){
		
			case TOP_LEFT:
				cwr.setRect(wr);
				break;
				
			case CENTERED:
				int dif_w = (int)((width - image.getWidth()) / 2.0);
				int dif_h = (int)((height - image.getHeight()) / 2.0);
				
				cwr.setRect(dif_w, dif_h, wr);
				break;
		
		
			}
		
		BufferedImage cimage = new BufferedImage(image.getColorModel(), // should be immutable
				 cwr,
				 image.isAlphaPremultiplied(),
				 cproperties);

		return cimage;
		
	}
	
	protected static int getPowerOfTwo(int value) {
		int retval = 2;
		while (retval < value)
		    retval *= 2;
		return retval;
	}
	
	public static FileFilter getPngFileFilter(){
		return new FileFilter(){

			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getAbsolutePath().endsWith(".png");
			}

			@Override
			public String getDescription() {
				// TODO Auto-generated method stub
				return "Portable Network Graphics file (*.png)";
			}
			
		};
		
	}
	
	/*************************************************
	 * Returns a clone of <code>image</code>; i.e., a new instance with the same state.
	 * 
	 * @param image
	 * @return
	 */
	public static BufferedImage getCopy(BufferedImage image){
			
		String[] pnames = image.getPropertyNames();
		Hashtable<String, Object> cproperties = new Hashtable<String, Object>();
		if(pnames != null) {
			for(int i = 0; i < pnames.length; i++) {
				cproperties.put(pnames[i], image.getProperty(pnames[i]));
				}
			}
		WritableRaster wr = image.getRaster();
		WritableRaster cwr = wr.createCompatibleWritableRaster();
		cwr.setRect(wr);
		BufferedImage cimage = new BufferedImage(image.getColorModel(), // should be immutable
												 cwr,
												 image.isAlphaPremultiplied(),
												 cproperties);
		
		return cimage;
	}
	
	
	/*************************************************************
	 * Resamples {@code image_to_scale} and returns the result
	 * 
	 * @param image_to_scale
	 * @param new_width
	 * @param new_height
	 * @return
	 */
	public static BufferedImage getResampledImage(BufferedImage image_to_scale, double scale_x, double scale_y){
		int w = image_to_scale.getWidth();
		int h = image_to_scale.getHeight();
		
		BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		at.scale(scale_x, scale_y);
		AffineTransformOp scaleOp = 
		   new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		after = scaleOp.filter(image_to_scale, after);
		return after;
	}
	
	
	/*************************************************************
	 * Resamples {@code image_to_scale} and returns the result
	 * 
	 * @param image_to_scale
	 * @param new_width
	 * @param new_height
	 * @return
	 */
	public static BufferedImage getResampledImage(BufferedImage image_to_scale, int new_width, int new_height){
		int w = image_to_scale.getWidth();
		int h = image_to_scale.getHeight();
		
		BufferedImage resized = new BufferedImage(new_width, new_height, image_to_scale.getType());
	    Graphics2D g = resized.createGraphics();
	    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g.drawImage(image_to_scale, 0, 0, new_width, new_height, 0, 0, w, h, null);
	    g.dispose();
		return resized;
		
		//return getResampledImage(image_to_scale, (double)new_width / w, (double)new_height / h);
		
	}
	
}