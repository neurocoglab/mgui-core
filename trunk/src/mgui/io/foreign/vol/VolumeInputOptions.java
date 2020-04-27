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

package mgui.io.foreign.vol;

import java.io.File;

import org.jogamp.vecmath.Vector3f;

import mgui.datasources.DataType;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.maps.ColourMap;
import mgui.io.domestic.shapes.ShapeInputOptions;


public class VolumeInputOptions extends ShapeInputOptions {

	/**@todo settings for:
	 * colour mapping
	 * 
	 */
	
	//volume file
	//input data type
	//target grid data type
	
	public static final int SET_VOLUME = 0;
	public static final int SET_DYNAMIC = 1;
	
	/****
	 *options:
	 *	calculate alpha from intensity
	 *	max/min alpha cutoffs
	 *	colour map
	 */
	public int format;
	public File file;
	public String inputType;
	public String gridType;
	public boolean setAlpha;
	public float maxAlpha;
	public float minAlpha;
	public boolean flipX, flipY, flipZ;
	public int xDim, yDim, zDim;
	public float xGeom, yGeom, zGeom;
	public double dataMin, dataMax;
	//public int dataSize;
	//public DataType dataType;
	public int dataType;
	public float xOrig, yOrig, zOrig;
	public Vector3f xAxis = new Vector3f(1, 0, 0);
	public Vector3f yAxis = new Vector3f(0, 1, 0);
	public Vector3f zAxis = new Vector3f(0, 0, 1);
	public ColourMap colourMap;
	
	//dynamic
	public boolean isDynamic;
	public File[] volumes;		//for multiple volume files
	public int dynFormat;
	public DataType dynDataType;
	public String dynInputType;
	
	//for copying
	public boolean setDims = true, setGeom = true, setType = true, setOrig = true, setMax, setAxes;
	
	public static final String TYPE_RGB = "RGB";
	public static final String TYPE_RGBA = "RGBa";
	public static final String TYPE_ARGB = "aRGB";
	public static final String TYPE_BGR = "BGR";
	public static final String TYPE_BGRA = "BGRA";
	public static final String TYPE_ABGR = "ABGR";
	public static final String TYPE_INTENSITY = "INTENSITY";
	public static final String TYPE_CMAP = "CMAP";
	
	public static final int FORMAT_VOL = 0;
	public static final int FORMAT_ANALYZE = 1;
	public static final int FORMAT_NIFTI = 2;
	public static final int FORMAT_MINC = 3;

	public boolean hasColourMap(){
		return colourMap != null;
	}
	
	public void setFrom(VolumeInputOptions options){
		format = options.format;
		file = options.file;
		inputType = options.inputType;
		gridType = options.gridType;
		setAlpha = options.setAlpha;
		maxAlpha = options.maxAlpha;
		minAlpha = options.minAlpha;
		flipX = options.flipX;
		flipY = options.flipY;
		flipZ = options.flipZ;
		xAxis = options.xAxis;
		yAxis = options.yAxis;
		zAxis = options.zAxis;
		setDims = false;
		dataType = options.dataType;
		if (options.setDims){
			xDim = options.xDim;
			yDim = options.yDim;
			zDim = options.zDim;
			}
		if (options.setGeom){
			xGeom = options.xGeom;
			yGeom = options.yGeom;
			zGeom = options.zGeom;
			}
		if (options.setOrig){
			xOrig = options.xOrig;
			yOrig = options.yOrig;
			zOrig = options.zOrig;
			}
	
		//dynamic stuff
		volumes = options.volumes;
		isDynamic = options.isDynamic;
		dynDataType = options.dynDataType;
		dynInputType = options.dynInputType;
		dynFormat = options.dynFormat;
		
		colourMap = options.colourMap;
	}
	
}