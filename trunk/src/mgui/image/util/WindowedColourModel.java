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

package mgui.image.util;

import java.awt.Color;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.ContinuousColourMap;
import mgui.interfaces.maps.DiscreteColourMap;
import mgui.interfaces.shapes.util.GridSampleModel;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/************************************************
 * Colour model useful for representing medical images. Values are scaled and translated
 * to fit scale and intercept parameters, and windowed to a specified window width
 * (contrast) and window middle (brightness). Colours are indexed to a specified colour 
 * map.
 * 
 * <p>NB: This colour model is <b>mutable</b>, meaning that it can be changed and its changes
 * will be reflected in all objects that reference it. Use the <code>clone()</code> method to
 * make a copy of a particular model state.
 * 
 * <p>NB: an alpha of zero will always map to zero; this is necessary for the current
 * implementation of Volume3DTexture, which requires a power-of-two volume, part of which
 * will be outside the bounds of any non-power-of-two volume, and thus should be always
 * invisible.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class WindowedColourModel extends ColorModel implements Cloneable,
															   XMLObject{

	//Would be better to extend IndexColorModel but its getRed etc. methods are idiotically final and rgb
	//is idiotically private...
	
	

	protected byte[] rgba;
	protected ColourMap colourMap;
	protected int channels;
	public int data_size = 256;
	protected int map_size = 256;
	protected double scale = 1, intercept = 0;
	protected double window_mid = 0.5, window_width = 1.0;		//windowing, in normalized units
	//public boolean setAlpha;
	private boolean has_alpha = true;
	protected double alphaMin = 0, alphaMax = 1.0;
	public boolean is_discrete;
	public boolean is_solid;
	protected int discrete_min = 0;
	protected double alpha = 1.0;
	protected boolean low_is_transparent = true; 				// Values < alphaMin are transparent? Otherwise clamped to min colour
	protected HashMap<Integer,Integer> index_map = null;
	protected int[] solid_colour = new int[4];
	
	/******************************************************
	 * Constructor produces a greyscale model of type {@code DataBuffer.TYPE_DOUBLE}.
	 * 
	 * @param transfer_type
	 */
	public WindowedColourModel(){
		this(ContinuousColourMap.getGreyScale(), 1, 0, 0.5, 1, true, DataBuffer.TYPE_DOUBLE);
	}
	
	/******************************************************
	 * Constructor produces a greyscale model of type {@code transferType}.
	 * 
	 * @param transferType
	 */
	public WindowedColourModel(int transferType){
		this(ContinuousColourMap.getGreyScale(), 1, 0, 0.5, 1, true, transferType);
	}
	
	
	/******************************************************
	 * Constructor specifying min and max values only.
	 * 
	 * @param cm
	 * @param min
	 * @param max
	 * @param has_alpha
	 * @param transfer_type
	 */
	public WindowedColourModel(ColourMap cm, double min, double max, boolean has_alpha, int transfer_type){
		
		this(cm, 1, 0, (max + min) / 2.0, max - min, has_alpha, transfer_type);
		
	}
			
	/*******************************************************
	 * Constructors specifying the full set of model parameters.
	 * 
	 * @param cm
	 * @param scale
	 * @param intercept
	 * @param window_mid
	 * @param window_width
	 * @param has_alpha
	 * @param transferType
	 */
	public WindowedColourModel(ColourMap cm,
							   double scale,
							   double intercept,
							   double window_mid,
							   double window_width,
							   boolean has_alpha,
							   int transferType) {
		
		super(8, 
			  getBits(true, 8), 
			  ColorSpace.getInstance(ColorSpace.CS_sRGB),
			  true, 
			  false, 
			  Transparency.TRANSLUCENT,
			  transferType);
		
	
		this.scale = scale;
		this.intercept = intercept;
		this.window_mid = window_mid;
		this.window_width = window_width;
		
		this.has_alpha = has_alpha;
		this.channels = 4;
		this.data_size = 256;
		if (cm != null)
			setColourMap(cm);
		
	}
	
	public void setIsSolidColour(boolean b){
		is_solid = b;
	}
	
	public void setSolidColour(Color clr){
		solid_colour = new int[]{clr.getRed(), clr.getGreen(), clr.getBlue(), clr.getAlpha()};
	}
	
	public Color getSolidColour(){
		return new Color(solid_colour[0], solid_colour[1], solid_colour[2], solid_colour[3]);
	}
	
	public void setHasAlpha(boolean b){
		has_alpha = b;
	}
	
	public boolean getHasAlpha(){
		return has_alpha;
	}
	
	public double getScale(){
		return scale;
	}
	
	public double getIntercept(){
		return intercept;
	}
	
	public void setScale(double s){
		scale = s;
	}
	
	public void setIntercept(double i){
		intercept = i;
	}
	
	public void setWindowMid(double mid){
		this.window_mid = mid;
	}
	
	public double getWindowMid(){
		return this.window_mid;
	}
	
	public void setWindowWidth(double width){
		this.window_width = width;
	}
	public double getWindowWidth(){
		return this.window_width;
	}
	
	public void setLowIsTransparent(boolean b){
		this.low_is_transparent = b;
	}
	
	public boolean getLowIsTransparent(){
		return this.low_is_transparent;
	}
	
	/****************************************
	 * Set the window from limits.
	 * 
	 * @param limits
	 */
	public void setLimits(double[] limits){
		setLimits(limits[0], limits[1]);
	}
	
	/****************************************
	 * Set the window from limits.
	 * 
	 * @param min
	 * @param max
	 */
	public void setLimits(double min, double max){
		if (max <= min) return;
		min -= this.intercept;
		min *= this.scale;
		max -= this.intercept;
		max *= this.scale;
		
		this.window_width = max - min;
		this.window_mid = min + window_width / 2.0;
	}
	
	/****************************************
	 * Returns the limits of this windowed model
	 * 
	 * @return
	 */
	public double[] getLimits(){
		double[] limits = new double[2];
		limits[0] = this.window_mid - (window_width / 2.0);
		limits[1] = this.window_mid + (window_width / 2.0);
		limits[0] /= scale;
		limits[0] += intercept;
		limits[1] /= scale;
		limits[1] += intercept;
		return limits;
	}
	
	static int[] getBits(boolean hasAlpha, int transferType){
		int a = hasAlpha ? 4 : 3;
		int b = 8;
		int[] bits = new int[a];
		for (int i = 0; i < a; i++)
			bits[i] = b;
		return bits;
	}
	
	//TODO: necessary?
	public int getRedIndex(){
		return 0;
	}
	
	public int getGreenIndex(){
		return 1;
	}
	
	public int getBlueIndex(){
		return 2;
	}
	
	public int getAlphaIndex(){
		return 3;
	}
	
	//******Stuff from IndexColorModel********
	
	@Override
	public WritableRaster createCompatibleWritableRaster(int w, int h) {
		
		SampleModel sm = createCompatibleSampleModel(w, h);
		
		DataBuffer db = null;
		
		switch (transferType){
			case DataBuffer.TYPE_BYTE:
				db = new DataBufferByte(w * h, 1);
				break;
			case DataBuffer.TYPE_SHORT:
				db = new DataBufferShort(w * h, 1);
				break;
			case DataBuffer.TYPE_USHORT:
				db = new DataBufferUShort(w * h, 1);
				break;
			case DataBuffer.TYPE_INT:
				db = new DataBufferInt(w * h, 1);
				break;
			case DataBuffer.TYPE_FLOAT:
				db = new DataBufferFloat(w * h, 1);
				break;
			case DataBuffer.TYPE_DOUBLE:
				db = new DataBufferDouble(w * h, 1);
				break;
			}
		
		return Raster.createWritableRaster(sm, db, null);
		
    }
	
	@Override
	public SampleModel createCompatibleSampleModel(int w, int h) {
		
		return new GridSampleModel(transferType, w, h, 1);
		
	}
	
	@Override
	public boolean isCompatibleRaster(Raster raster) {
		int size = raster.getSampleModel().getSampleSize(0);
		
		int t = raster.getTransferType();
		int b = raster.getNumBands();
		int hm = 1 << size;
		
	    return ((raster.getTransferType() == transferType) &&
			(raster.getNumBands() == 1)); // && ((1 << size) >= map_size));
	    }
	
	 @Override
	public int getRGB(int pixel) {
		 if (hasAlpha())
			return (getAlpha(pixel) << 24)
			    | (getRed(pixel) << 16)
			    | (getGreen(pixel) << 8)
			    | (getBlue(pixel) << 0);
		 return (255 << 24)
		 	| (getRed(pixel) << 16)
		    | (getGreen(pixel) << 8)
		    | (getBlue(pixel) << 0);
	 
	 }
	 
	 @Override
	public int getRGB(Object inData) {
		 if (hasAlpha())
	        return (getAlpha(inData) << 24)
	            | (getRed(inData) << 16)
	            | (getGreen(inData) << 8)
	            | (getBlue(inData) << 0);
		 return (255 << 24)
		 		 | (getRed(inData) << 16)
		         | (getGreen(inData) << 8)
		         | (getBlue(inData) << 0);
	    }
	 
	
	//*******End of stuff from IndexColorModel*********
	
	@Override
	public int getRed(Object inData){
		double pixel = 0;
		int i = 0; 
		
	      // Get the pixel value
	      if (inData instanceof byte[]) { pixel = ((byte[])inData)[i]; }
	      else if (inData instanceof short[]) { pixel = ((short[])inData)[i]; }
	      else if (inData instanceof int[]) { pixel = ((int[])inData)[i]; }
	      else if (inData instanceof float[]) { pixel = ((float[])inData)[i]; }
	      else if (inData instanceof double[]) { pixel = ((double[])inData)[i]; }
	      else { throw new ClassCastException("Unsupported pixel type"); }
		
	      return getRed((int)Math.round(getMappedValue(pixel)));
	}
	
	@Override
	public int getRed(int pixel){
		if (pixel < 0) pixel = 0;
		if (is_solid){
			if (pixel == 0) return 0;
			return solid_colour[0];
			}
		if (pixel * channels + getRedIndex() > rgba.length - 1)
			pixel += 0;
		return rgba[pixel * channels + getRedIndex()] & 0xFF;
	}
	
	
	@Override
	public int getGreen(Object inData){
		double pixel = 0;
		int i = 0; 
		
	      // Get the pixel value
		if (inData instanceof byte[]) { pixel = ((byte[])inData)[i]; }
	      else if (inData instanceof short[]) { pixel = ((short[])inData)[i]; }
	      else if (inData instanceof int[]) { pixel = ((int[])inData)[i]; }
	      else if (inData instanceof float[]) { pixel = ((float[])inData)[i]; }
	      else if (inData instanceof double[]) { pixel = ((double[])inData)[i]; }
	      else { throw new ClassCastException("Unsupported pixel type"); }
		
		return getGreen((int)Math.round(getMappedValue(pixel)));
	}
	
	@Override
	public int getGreen(int pixel){
		if (pixel < 0) pixel = 0;
		if (is_solid){
			if (pixel == 0) return 0;
			return solid_colour[1];
			}
		return rgba[pixel * channels + getGreenIndex()] & 0xFF;
	}
	
	@Override
	public int getBlue(Object inData){
		double pixel = 0;
		int i = 0; 
		
	      // Get the pixel value
		if (inData instanceof byte[]) { pixel = ((byte[])inData)[i]; }
	      else if (inData instanceof short[]) { pixel = ((short[])inData)[i]; }
	      else if (inData instanceof int[]) { pixel = ((int[])inData)[i]; }
	      else if (inData instanceof float[]) { pixel = ((float[])inData)[i]; }
	      else if (inData instanceof double[]) { pixel = ((double[])inData)[i]; }
	      else { throw new ClassCastException("Unsupported pixel type"); }
		
		return getBlue((int)Math.round(getMappedValue(pixel)));
	}
	
	@Override
	public int getBlue(int pixel){
		if (pixel < 0) pixel = 0;
		if (is_solid){
			if (pixel == 0) return 0;
			return solid_colour[2];
			}
		return rgba[pixel * channels + getBlueIndex()] & 0xFF;
	}
	
	
	@Override
	public int getAlpha(Object inData){
		double pixel = Double.NaN;
		
		int i = 0; 
		
		 // Get the pixel value
		if (inData instanceof byte[]) { pixel = ((byte[])inData)[i]; }
	      else if (inData instanceof short[]) { pixel = ((short[])inData)[i]; }
	      else if (inData instanceof int[]) { pixel = ((int[])inData)[i]; }
	      else if (inData instanceof float[]) { pixel = ((float[])inData)[i]; }
	      else if (inData instanceof double[]) { pixel = ((double[])inData)[i]; }
	      else { throw new ClassCastException("Unsupported pixel type"); }
						
		return getAlpha((int)Math.round(getMappedValue(pixel)));
	}
	
	@Override
	public int getAlpha(int pixel){
		
		if (!has_alpha) return 255;
		if (pixel < 0){
			if (has_alpha && low_is_transparent) return 0;
			pixel = 0;
			}
		if (is_solid){
			if (pixel == 0) return 0;
			return solid_colour[3];
			}
		if (!is_discrete && low_is_transparent && pixel <= alphaMin * (data_size - 1)){
			pixel = 0;
		}else if (!is_discrete && pixel > alphaMax * (data_size - 1)){
			pixel = (data_size - 1);
		}else{
			pixel = rgba[pixel * channels + getAlphaIndex()] & 0xFF;
			}
		return (int)(pixel * alpha);	//apply general alpha
	}
	
	/************************************
	 * Maps a value according to the linear window model:
	 * 
	 * <p><code>Y = (value - intercept) * scale * m + b</code>
	 * 
	 * <p>where <code>m = data_size * window_width</code>
	 * <p>and <code>b = data_size * (window_mid - 0.5)</code>
	 * 
	 * @param value
	 * @return
	 */
	public double getMappedValue(double value){
		//Integer.MAX_VALUE indicates voxel should be completely transparent (i.e., masked)
		if (isMaskValue(value)) 
			return -1;
		
		if (is_solid){
			if (value <= 0) return -1;
			return 1;
			}
		
		if (is_discrete){
			if (index_map == null) return -1;
			if (value == 0)
				value +=0;
			Integer _pixel = index_map.get((int)value);
			if (_pixel == null)
				return -1;
			return _pixel;
			}
		
		//scale and translate
		value -= this.intercept;
		value *= this.scale;
		
		//apply linear windowing function y = m * x + b
		
		double m = data_size / window_width;
		double b = data_size * (0.5 - (window_mid / window_width));
		
		value = m * value + b;
		if (value < 0) value = 0;
		if (value > data_size - 1) value = data_size - 1;
		
		return value;
	}
	
	/************************************
	 * Maps a value according to the inverse of the linear window model:
	 * 
	 * <p>{@code Y = (value - b) / (scale * m) + intercept}
	 * 
	 * <p>where <code>m = data_size * window_width</code>
	 * <p>and <code>b = data_size * (window_mid - 0.5)</code>
	 * 
	 * @param value
	 * @return
	 */
	public double getInverseMappedValue(double value){
		
		double m = data_size / window_width;
		double b = data_size * (0.5 - (window_mid / window_width));
		
		value = (value - b) / m;
		value /= scale;
		value += intercept;
		
		return value;
	
	}
	
	private boolean isMaskValue(double value){
		//value is masked if it is the maximum value for this map's data type
		if (is_discrete) return value <= 0 || value >= (data_size + discrete_min) || Double.isNaN(value);
		return (value == getZeroValue(this.transferType) || Double.isNaN(value));
	}
	
	/*****************************************
	 * Returns the "zero" value for the given transfer type. This value is used to set transparency for masked
	 * pixels.
	 * 
	 * @param transferType
	 * @return
	 */
	public static double getZeroValue(int transferType){
		
		switch (transferType){
		
			case DataBuffer.TYPE_INT:
				return Integer.MAX_VALUE;
				
			case DataBuffer.TYPE_SHORT:
				return Short.MAX_VALUE;
			
			case DataBuffer.TYPE_DOUBLE:
				return Double.POSITIVE_INFINITY;
			
			case DataBuffer.TYPE_FLOAT:
				return Float.POSITIVE_INFINITY;
				
			case DataBuffer.TYPE_BYTE:
				return Byte.MIN_VALUE;
			
			default:
				return 0;	
			
			}
		
	}
	
	public boolean setColourMap(ColourMap cm){
		is_discrete = (cm instanceof DiscreteColourMap);
		
		colourMap = cm;
		if (is_discrete){
			data_size = ((DiscreteColourMap)cm).getMax() - ((DiscreteColourMap)cm).getMin() + 1;
			//data_size = ((DiscreteColourMap)cm).getMax();
			discrete_min = ((DiscreteColourMap)cm).getMin();
		}else{
			data_size = 255;
			}
			
		rgba = cm.getDiscreteMap(data_size, channels);
		if (is_discrete)
			index_map = ((DiscreteColourMap)cm).getIndexMap();
		else
			index_map = null;
		return true;
	}
	
	public ColourMap getColourMap(){
		return colourMap;
	}
	
	public void setFromColourModel(WindowedColourModel model){
		this.scale = model.scale;
		this.intercept = model.intercept;
		this.alphaMin = model.alphaMin;
		this.alphaMax = model.alphaMax;
		this.window_mid = model.window_mid;
		this.window_width = model.window_width;
		this.alpha = model.alpha;
		this.has_alpha = model.has_alpha;
		this.low_is_transparent = model.low_is_transparent;
		setColourMap(model.colourMap);
	}
	
	@Override
	public Object clone(){
		WindowedColourModel model = new WindowedColourModel(colourMap,
															scale,
															intercept,
															window_mid,
															window_width,
															has_alpha,
															transferType);
		model.alphaMin = alphaMin;
		model.alphaMax = alphaMax;
		model.alpha = alpha;
		model.low_is_transparent = low_is_transparent;
		return model;
	}

	public double getAlphaMin() {
		return alphaMin;
	}

	public void setAlphaMin(double alphaMin) {
		this.alphaMin = alphaMin;
	}

	public double getAlphaMax() {
		return alphaMax;
	}

	public void setAlphaMax(double alphaMax) {
		this.alphaMax = alphaMax;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}
	
	//********************************* XML STUFF **************************************
	
	
	@Override
	public String getDTD() {
		return "";
	}

	@Override
	public String getXMLSchema() {
		return "";
	}

	@Override
	public String getXML() {
		return getXML(0);
	}

	@Override
	public String getXML(int tab) {
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab+1);
		String xml = _tab + "<" + getLocalName() + "\n";
		xml = xml + _tab2 + "scale = '" + scale + "'\n";
		xml = xml + _tab2 + "intercept = '" + intercept + "'\n";
		xml = xml + _tab2 + "window_mid = '" + window_mid + "'\n";
		xml = xml + _tab2 + "window_width = '" + window_width + "'\n";
		xml = xml + _tab2 + "has_alpha = '" + has_alpha + "'\n";
		xml = xml + _tab2 + "alpha = '" + alpha + "'\n";
		xml = xml + _tab2 + "alpha_min = '" + alphaMin + "'\n";
		xml = xml + _tab2 + "alpha_max = '" + alphaMax + "'\n";
		xml = xml + _tab + "/>";
		
		return xml;
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes,XMLType type) throws SAXException {
		
		if (localName.equals(this.getLocalName())){
			scale = Double.valueOf(attributes.getValue("scale"));
			intercept = Double.valueOf(attributes.getValue("intercept"));
			window_mid = Double.valueOf(attributes.getValue("window_mid"));
			window_width = Double.valueOf(attributes.getValue("window_width"));
			has_alpha = Boolean.valueOf(attributes.getValue("has_alpha"));
			alpha = Double.valueOf(attributes.getValue("alpha"));
			alphaMin = Double.valueOf(attributes.getValue("alpha_min"));
			alphaMax = Double.valueOf(attributes.getValue("alpha_max"));
			return;
		}
		
	}

	@Override
	public void handleXMLElementEnd(String localName) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLString(String s) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLocalName() {
		return "WindowedColourModel";
	}

	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
		writer.write(this.getXML(tab));
	}

	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException {
		writer.write(this.getXML(tab));
	}

	@Override
	public void writeXML(int tab, Writer writer) throws IOException {
		writer.write(this.getXML(tab));
	}

	@Override
	public String getShortXML(int tab) {
		return getXML(tab);
	}
	
	
	
	
	
	
}