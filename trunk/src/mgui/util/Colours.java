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

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

import mgui.datasources.DataType;
import mgui.datasources.DataTypes;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;


public class Colours {

	public static final int RGB = 0;
	public static final int ARGB = 1;
	public static final int RGBA = 2;
	
	public static byte getR(int colour, int format){
		switch(format){
		
		case RGB:
			return (byte)((colour & 0x00FF0000) >>> 16);
			
		case ARGB:
			return (byte)((colour & 0x00FF0000) >>> 16);
		
		
		}
		return -1;
	}
	
	public static Colour4f getRandom(){
		float b = (float)Math.random();
		float g = (float)Math.random();
		float r = (float)Math.random();
		return new Colour4f(r, g, b, 1);
	}
	
	public static BufferedImage getRGBtoRGBA(BufferedImage rgb, double cutoff, double exp) {
		return getRGBtoRGBA(rgb, true, cutoff, exp);
	}
	
	public static BufferedImage getRGBtoRGBA(BufferedImage rgb, boolean setAlpha, double cutoff, double exp) {
		
		int co = (int)(cutoff * 255);
		int width = rgb.getWidth();
        int height = rgb.getHeight();
        //int depth = 4; // urlImage.getWidth();
        int type = rgb.getType();
        
        byte[] origData = ((DataBufferByte)rgb.getData().getDataBuffer()).getData();

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB); 
        int[] nBits = {8, 8, 8, 8}; 
        ComponentColorModel colorModel = new ComponentColorModel(cs, nBits, true, false, 
        		Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
         
        WritableRaster raster = colorModel.createCompatibleWritableRaster(width, height); 
        BufferedImage bImage = new BufferedImage(colorModel, raster, false, null);
        byte[] byteData = ((DataBufferByte)raster.getDataBuffer()).getData(); 
        //ImageComponent3D pArray = new ImageComponent3D(ImageComponent.FORMAT_RGBA, width, height, depth);

        for (int j = 0; j < height; j++)
            for (int i = 0; i < width; i++){ 
                int indexb = ((j * width) + i) * 4;
                int indexo = ((j * width) + i) * 3;
                byte red = 0, green = 0, blue = 0;
                switch(type){
                case BufferedImage.TYPE_3BYTE_BGR:
                	blue = origData[indexo];
                    green = origData[indexo + 1];
                    red = origData[indexo + 2];
                	break;
                
                
                }
                byteData[indexb + 0] = red;
                byteData[indexb + 1] = green;
                byteData[indexb + 2] = blue;
                
                byte test = 127;
                
                //calculate alpha
                if (setAlpha){
	                int alpha = Math.max(Math.max(byteToInt(red),
					                    		  byteToInt(green)),
					                    		  byteToInt(blue));
	                int t = 0;
	                
	                //smoothed transition?
	                if (alpha < co){
	                	if (exp > 0){
	                	t = (int)(Math.pow((co - alpha), exp));
	                	alpha = Math.max(co - t, 0);
	                	//if (alpha > co - 4)
	                	//	alpha = alpha;
		                }else{
		                	alpha = 0;
		                	}
	                	}
	                //alpha = 255;
	                test = (byte)alpha;
	                }
                byteData[indexb + 3] = test;
            	}
        return bImage;
	}
	
	/****************************************************
	 * Parses a space-delimited list of numbers, and returns a colour of the appropriate size
	 * 
	 * @param s
	 * @return
	 */
	public static Colour parse(String s){
		String[] parts = s.split(" ");
		int n = parts.length;
		if (n > 4) n = 4;
		if (n < 1) n = 1;
		return parse(s, n);
	}
	
	/****************************************************
	 * Parses a space-delimited list of numbers, and returns a colour of size n
	 * 
	 * @param s
	 * @return
	 */
	public static Colour parse(String s, int n){
		String[] parts = s.split(" ");
		if (parts.length < n){
			InterfaceSession.log("Colors.parse: Size " + n + " is too large for string " + s, 
								 LoggingType.Errors);
			return null;
			}
		switch (n){
		case 1:
			return new Colour1f(Float.valueOf(parts[0]));
		case 2:
			return new Colour2f(Float.valueOf(parts[0]), 
								Float.valueOf(parts[1]));
		case 3:
			return new Colour3f(Float.valueOf(parts[0]), 
								Float.valueOf(parts[1]),
								Float.valueOf(parts[2]));
		default:
			return new Colour4f(Float.valueOf(parts[0]), 
								Float.valueOf(parts[1]),
								Float.valueOf(parts[2]),
								Float.valueOf(parts[3]));
		}
		
	}
	
	/************************************
	 * Returns the size, in bytes, of this image
	 * @param image
	 * @return int Size in bytes of this image
	 */
	public static int getDataSize(BufferedImage image){
		switch(image.getType()){
		
		case BufferedImage.TYPE_3BYTE_BGR:
			return 3;
		case BufferedImage.TYPE_4BYTE_ABGR:
			return 4;
		case BufferedImage.TYPE_BYTE_GRAY:
			return 1;
		case BufferedImage.TYPE_INT_ARGB:
			return 4;
		case BufferedImage.TYPE_INT_BGR:
			return 4;
		case BufferedImage.TYPE_4BYTE_ABGR_PRE:
			return 4;
		}
		//unsupported format
		return -1;
	}

	private static int byteToInt(byte b){
		int val = b;
		if (val < 0) val += 256;
		return val;
	}
	
	/***********************************
	 * Interpolate a colour between c1 and c2, and point iVal (0.0 to 1.0)
	 * @param c1 first colour
	 * @param c2 second colour
	 * @param iVal point on continuum 0 to 1 to interpolate
	 * @return
	 */
	public static Colour interpolate(Colour c1, Colour c2, double iVal){
		if (iVal < 0 || iVal > 1){
			InterfaceSession.log("Invalid interpolation value: " + iVal);
			return null;
			}
			
		int dims = Math.min(c1.getDims(), c2.getDims());
		Colour cA = getColourNf(c1, dims);
		Colour cB = getColourNf(c2, dims);
		Colour c = getColourNf(c1, dims);	
		
		for (int i = 0; i < dims; i++){
			double delta = cB.getDim(i) - cA.getDim(i);
			c.setDim(i, cA.getDim(i) + (float)(iVal * delta));
			}
		
		return c;
	}
	
	public static void toBytes(Colour c, byte[] sample){
		int size = Math.min(c.getDims(), sample.length);
		int s;
		for (int i = 0; i < size; i++){
			//assuming value is normalized to 1
			s = Math.round(c.getDim(i) * 255f);		//for debugging
			sample[i] = (byte)(s); 
			}
	}
	
	public static Colour getColourNf(Colour c, int dims){
		Colour cRet;
		switch (dims){
		case 1:
			cRet = new Colour1f();
			cRet.setIntensity(c.getIntensity());
			return cRet;
		case 2:
			cRet = new Colour2f();
			cRet.setIntensity(c.getIntensity());
			cRet.setAlpha(c.getAlpha());
			return cRet;
		case 3:
			cRet = new Colour3f();
			cRet.setRed(c.getRed());
			cRet.setGreen(c.getGreen());
			cRet.setBlue(c.getBlue());
			return cRet;
		case 4:
			cRet = new Colour4f();
			cRet.setRed(c.getRed());
			cRet.setGreen(c.getGreen());
			cRet.setBlue(c.getBlue());
			cRet.setAlpha(c.getAlpha());
			return cRet;
		}
		
		return null;
	}
	
	public static Colour getSimplestColour(Colour c1, Colour c2){
		if (c1.getDims() > c2.getDims()) return c2;
		return c1;
	}
	
	public static double[] getTypeRange(DataType type){
		return getTypeRange(getTransferType(type));
	}
	
	public static double[] getTypeRange(ColorModel model){
		return getTypeRange(model.getTransferType(), model.getColorSpace());
	}
	
	public static double[] getTypeRange(int transferType){
		return getTypeRange(transferType, null);
	}
	
	public static double[] getTypeRange(int transferType, ColorSpace space){
		//double max = 0, min = 0;
		switch (transferType){
			case DataBuffer.TYPE_BYTE:
				return new double[]{0, Byte.MAX_VALUE};
			case DataBuffer.TYPE_SHORT:
				return new double[]{0, Short.MAX_VALUE};
			case DataBuffer.TYPE_INT:
				return new double[]{0, 255}; // Integer.MAX_VALUE};
			case DataBuffer.TYPE_FLOAT:
			case DataBuffer.TYPE_DOUBLE:
				return new double[]{-Float.MAX_VALUE, Float.MAX_VALUE};
			default:
				return new double[]{0, Byte.MAX_VALUE};
			}
	}
	
	public static int getTransferType(DataType type){
		switch (type.val){
			case DataTypes.BYTE:
				return DataBuffer.TYPE_BYTE;
			case DataTypes.SHORT:
				return DataBuffer.TYPE_SHORT;
			case DataTypes.INTEGER:
				return DataBuffer.TYPE_INT;
			case DataTypes.FLOAT:
				return DataBuffer.TYPE_FLOAT;
			case DataTypes.DOUBLE:
				return DataBuffer.TYPE_DOUBLE;
			default:
				return DataBuffer.TYPE_BYTE;
			}
	}
	
}