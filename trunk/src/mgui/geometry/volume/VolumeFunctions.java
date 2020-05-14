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

package mgui.geometry.volume;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jogamp.java3d.ImageComponent3D;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import org.jogamp.vecmath.Matrix4d;
import org.jogamp.vecmath.Matrix4f;
import org.jogamp.vecmath.Point2f;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Tuple3f;
import org.jogamp.vecmath.Vector2f;
import org.jogamp.vecmath.Vector3f;

import mgui.geometry.Box3D;
import mgui.geometry.Grid3D;
import mgui.geometry.Plane3D;
import mgui.geometry.PointSet3D;
import mgui.geometry.Polygon2D;
import mgui.geometry.Polygon3D;
import mgui.geometry.Rect2D;
import mgui.geometry.Rect3D;
import mgui.geometry.util.GeometryFunctions;
import mgui.image.util.WindowedColourModel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.ContinuousColourMap;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.shapes.PointSet3DInt;
import mgui.interfaces.shapes.VertexDataColumn;
import mgui.interfaces.shapes.Volume2DInt;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.interfaces.shapes.volume.GridVertexDataColumn;
import mgui.interfaces.shapes.volume.VolumeMaskOptions_old;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;
import mgui.stats.Histogram;
import mgui.stats.StatFunctions;

import org.apache.commons.math3.analysis.interpolation.TricubicSplineInterpolatingFunction;
import org.apache.commons.math3.exception.OutOfRangeException;

import foxtrot.Job;
import foxtrot.Worker;


/*****************************
 * Utility class providing various functions for use on volume data (Volume3DInt and its descendants).
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class VolumeFunctions {

	//constants
	static final int X = 0, Y = 1, Z = 2;
	static final int ROT90 = 0, ROT180 = 1, ROT270 = 2;
	
	static public enum VolumeAxis{
		S,
		T,
		R;
	}
	
	static public enum VolumeRotationAngle{
		ROT90,
		ROT180,
		ROT270;
	}
	
	/****************************************************
	 * Returns the value of voxel [i,j,k] mapped with its colour model, as a {@code byte}.
	 * Uses the current vertex data column.
	 * 
	 * @param volume
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static byte getMappedValueByte(Volume3DInt volume, int x, int y, int z){
		
		WindowedColourModel model = volume.getColourModel();
		return (byte)model.getMappedValue(volume.getDatumAtVoxel(x, y, z).getValue());
		
	}
	
	public static double getMappedValueDouble(Volume3DInt volume, int x, int y, int z){
		
		WindowedColourModel model = volume.getColourModel();
		return (byte)model.getMappedValue(volume.getDatumAtVoxel(x, y, z).getValue());
		
	}
	
    /***********************************************
	 * Produces a stack of R-planar images from {@code volume}, based upon the current data column and
	 * its associated colour map. If the volume is currently in composite mode, returns the masked
	 * composite images. 
	 * 
	 * @param volume
	 * @return
	 */
    public static BufferedImage[] getMaskedImages(Volume3DInt volume){
    	
    	if (volume.isComposite())
    		return getMaskedCompositeImages(volume);
    	
    	if (volume.getCurrentColumn() == null) return null;
    	return getMaskedImages(volume, volume.getCurrentColumn());
    }
    
    /***********************************************
	 * Produces a stack of R-planar images from {@code volume}, based upon the data in {@code column} and
	 * its associated colour map.
	 * 
	 * @param volume
	 * @return
	 */
    public static BufferedImage[] getMaskedImages(Volume3DInt volume, String column){
    	
    	GridVertexDataColumn v_column = (GridVertexDataColumn)volume.getVertexDataColumn(column);
		if (column == null) return null;
		
		Grid3D grid = volume.getGrid();
		int r_size = grid.getSizeR();
		int s_size = grid.getSizeS();
		int t_size = grid.getSizeT();
		
		WindowedColourModel colour_model = v_column.getColourModel();
		if (volume.hasAlpha() && colour_model.hasAlpha()){
			
			}
		//WindowedColourModel colour_model = getColourModel(v_column.getDataTransferType(), volume.getColourMap(), volume.hasAlpha());
		
		boolean[][][] mask = new boolean[s_size][t_size][r_size];
		for (int i = 0; i < s_size; i++)
			for (int j = 0; j < t_size; j++)
				for (int k = 0; k < r_size; k++)
					mask[i][j][k]=false;
		
		// Apply masks if necessary
		if (volume.getApplyMasks()){
			mask = volume.getEffectiveMask();
			}
		
		BufferedImage[] images = new BufferedImage[r_size];
		for (int k = 0; k < r_size; k++){
			WritableRaster raster = colour_model.createCompatibleWritableRaster(s_size, t_size);
			BufferedImage image = new BufferedImage(colour_model, raster, false, null);
			raster = image.getRaster();
			images[k] = image;
			for (int i = 0; i < s_size; i++)
				for (int j = 0; j < t_size; j++){
					if (mask[i][j][k]){
						raster.setPixel(i, t_size - j - 1, new double[]{0});
					}else{
						raster.setPixel(i, t_size - j - 1, new double[]{v_column.getDoubleValueAtVoxel(i, j, k)});
						}
					}
			}
		
		return images;
    	
    	
    }
    
    /**********************************************************************
     * Returns a set of composite images, comprised of all data columns specified for the
     * composite, with alpha values and ordering as specified in the {@linkplain Volume3DInt}
     * object. Images will be masked by the volume's current set of masks, if any are set.
     * 
     * @param volume
     * @return
     */
    public static BufferedImage[] getMaskedCompositeImages(Volume3DInt volume){
    	
    	if (!volume.isComposite()) return null;
    	
    	Grid3D grid = volume.getGrid();
		int r_size = grid.getSizeR();
		int s_size = grid.getSizeS();
		int t_size = grid.getSizeT();
		
		BufferedImage[] images = new BufferedImage[r_size];
		
		boolean[][][] mask = null; 
//		new boolean[s_size][t_size][r_size];
//		for (int i = 0; i < s_size; i++)
//			for (int j = 0; j < t_size; j++)
//				for (int k = 0; k < r_size; k++)
//					mask[i][j][k]=false;
		
		// Apply masks if necessary
		if (volume.getApplyMasks()){
			mask = volume.getEffectiveMask();
			}
		
		for (int i = 0; i < r_size; i++){
			images[i] = getMaskedCompositeRSliceImage(volume, i, mask);
			}
    	
    	return images;
    }
    
    /*************************************************
     * Returns a composite image from an R-slice plane, volume, and set of colour models
     * and composite alphas. Columns render from bottom to top. Image will be masked by
     * {@code mask}.
     * 
     * @param volume
     * @param models
     * @param alphas
     */
    public static BufferedImage getMaskedCompositeRSliceImage(Volume3DInt volume, int r, boolean[][][] mask){
    	
    	BufferedImage image = getCompositeRSliceImage(volume, r);
    	if (mask == null) return image;
    	
    	int width = image.getWidth();
    	int height = image.getHeight();
    	
    	WritableRaster raster = image.getAlphaRaster();
    	if (raster == null) return image;
    	
    	for (int i = 0; i < width; i++)
    		for (int j = 0; j < height; j++)
    			if (mask[i][j][r])
    				raster.setPixel(i, height - j - 1, new double[]{0});
    	
    	return image;
    }
    
    /*************************************************
     * Returns a composite image from an R-slice plane, volume, and set of colour models
     * and composite alphas. Columns render from bottom to top.
     * 
     * @param volume
     * @param models
     * @param alphas
     */
    public static BufferedImage getCompositeRSliceImage(Volume3DInt volume, int r,
    													 ArrayList<String> ordered_columns,
					    								 ArrayList<WindowedColourModel> colour_models,
					    								 ArrayList<Double> alphas){
    	return getCompositeRSliceImage(volume, r, ordered_columns, colour_models, alphas, null);
    }
    
	 /*************************************************
     * Returns a composite image from an R-slice plane, volume, and set of colour models
     * and composite alphas. Columns render from bottom to top.
     * 
     * @param volume
     * @param models
     * @param alphas
     */
    public static BufferedImage getCompositeRSliceImage(Volume3DInt volume, int r,
    													 ArrayList<String> ordered_columns,
					    								 ArrayList<WindowedColourModel> colour_models,
					    								 ArrayList<Double> alphas,
					    								 ArrayList<Boolean> include){
    	
    	ArrayList<BufferedImage> image_stack = new ArrayList<BufferedImage>();
    	ArrayList<Double> alpha_stack = new ArrayList<Double>();
    	
    	if (ordered_columns.size() == 0) return null;
    	
    	// Get image stack (bottom renders first)
    	for (int i = 0; i < ordered_columns.size(); i++){
    		if (include == null || include.get(i)){
	    		GridVertexDataColumn v_column = (GridVertexDataColumn)volume.getVertexDataColumn(ordered_columns.get(i));
	    		image_stack.add(v_column.getRSliceImage(r, colour_models.get(i)));
	    		alpha_stack.add(alphas.get(i));
	    		}
    		}
    	
    	// Compile composite image
    	BufferedImage composite_image = null;
    	Graphics2D g = null;
    	int s_size = volume.getGrid().getSizeS();
    	int t_size = volume.getGrid().getSizeT();
    	
    	boolean start = true;
    	for (int i = image_stack.size() - 1; i > -1; i--){
    		if (start){
    			ColorModel colour_model = ComponentColorModel.getRGBdefault();
    			WritableRaster raster = colour_model.createCompatibleWritableRaster(s_size, t_size);
    			composite_image = new BufferedImage(colour_model, raster, false, null);
    			g = composite_image.createGraphics();
    			start = false;
    			}
    		 
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha_stack.get(i).floatValue()));
			g.drawImage(image_stack.get(i), 0, 0, null);
    		}
    	
    	if (g != null)
    		g.dispose();
    	return composite_image;
    }
    
    /*************************************************
     * Returns a composite image from an S,T, or R-slice plane, determined by {@code section}, and 
     * {@code volume}. Columns render from bottom to top.
     * 
     * @param volume
     * @param orientation - 0 = S, 1 = T, 2 = R
     * @param section     specifies which section to render 
     */
    public static BufferedImage getCompositeSliceImage(Volume3DInt volume, int orientation, int section){
    	
    	ArrayList<String> ordered_columns = volume.getCompositeOrderedColumns();
    	ArrayList<WindowedColourModel> colour_models = new ArrayList<WindowedColourModel>();
    	ArrayList<Double> alphas = new ArrayList<Double>();
    	ArrayList<Boolean> include = new ArrayList<Boolean>();
    	
    	for (int i = 0; i < ordered_columns.size(); i++){
    		String name = ordered_columns.get(i);
    		GridVertexDataColumn v_column = (GridVertexDataColumn)volume.getVertexDataColumn(name);
    		colour_models.add(v_column.getColourModel());
    		alphas.add(volume.getCompositeAlpha(name));
    		include.add(volume.showInComposite(name));
    		}
    	
    	return getCompositeSliceImage(volume, orientation, section, ordered_columns, colour_models, alphas, include);
    	
    }
    
    /*************************************************
     * Returns a composite image from an image plane, volume, determined by {@code orientation}, and set of colour models
     * and composite alphas. Columns render from bottom to top.
     * 
     * @param volume
     * @param models
     * @param alphas
     * @param orientation - 0 = S, 1 = T, 3 = R
     * @param section     specifies which section to render 
     */
    public static BufferedImage getCompositeSliceImage(Volume3DInt volume, int orientation, int section,
    													 ArrayList<String> ordered_columns,
					    								 ArrayList<WindowedColourModel> colour_models,
					    								 ArrayList<Double> alphas,
					    								 ArrayList<Boolean> include){
    	
    	ArrayList<BufferedImage> image_stack = new ArrayList<BufferedImage>();
    	ArrayList<Double> alpha_stack = new ArrayList<Double>();
    	
    	if (ordered_columns.size() == 0 || section < 0) return null;
    	
    	Grid3D grid = volume.getGrid();
    	//boolean flip_x = false, flip_y = false;
    	
    	switch (orientation){
	    	case 0:
	    		if (section >= grid.getSizeS()) return null;
	    		break;
	    	case 1:
	    		if (section >= grid.getSizeT()) return null;
	    		break;
	    	case 2:
	    		if (section >= grid.getSizeR()) return null;
	    		break;
	    	}
    	
    	int x_size = 0, y_size = 0;
    	
    	// Get image stack (bottom renders first)
    	for (int i = 0; i < ordered_columns.size(); i++){
    		if (include == null || include.get(i)){
	    		GridVertexDataColumn v_column = (GridVertexDataColumn)volume.getVertexDataColumn(ordered_columns.get(i));
	    		switch (orientation){
		    		case 0:
			    		image_stack.add(v_column.getSSliceImage(section, colour_models.get(i)));
			    		x_size = volume.getGrid().getSizeT();
			        	y_size = volume.getGrid().getSizeR();
			    		break;
		    		case 1:
			    		image_stack.add(v_column.getTSliceImage(section, colour_models.get(i)));
			    		x_size = volume.getGrid().getSizeS();
			        	y_size = volume.getGrid().getSizeR();
			    		break;
		    		case 2:
			    		image_stack.add(v_column.getRSliceImage(section, colour_models.get(i)));
			    		x_size = volume.getGrid().getSizeS();
			        	y_size = volume.getGrid().getSizeT();
			    		break;
		    		}
	    		alpha_stack.add(alphas.get(i));
	    		}
    		}
    	
    	// Compile composite image
    	BufferedImage composite_image = null;
    	Graphics2D g = null;
    	
    	
    	boolean start = true;
    	for (int i = image_stack.size() - 1; i > -1; i--){
    		if (start){
    			ColorModel colour_model = ComponentColorModel.getRGBdefault();
    			WritableRaster raster = colour_model.createCompatibleWritableRaster(x_size, y_size);
    			composite_image = new BufferedImage(colour_model, raster, false, null);
    			g = composite_image.createGraphics();
    			start = false;
    			}
    		 
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha_stack.get(i).floatValue()));
			g.drawImage(image_stack.get(i), 0, 0, null);
    		}
    	
    	if (g != null)
    		g.dispose();
    	return composite_image;
    }
    
    /*********************************************************
     * Returns a composite image from an R-slice plane and a volume
     * 
     * @param volume
     * @return
     */
    public static BufferedImage getCompositeRSliceImage(Volume3DInt volume, int r){
    	
    	ArrayList<WindowedColourModel> models = new ArrayList<WindowedColourModel>();
    	ArrayList<Double> alphas = new ArrayList<Double>();
    	ArrayList<Boolean> include = new ArrayList<Boolean>();
    	
    	ArrayList<String> ordered_columns = volume.getCompositeOrderedColumns();
    	
    	for (int i = 0; i < ordered_columns.size(); i++){
    		GridVertexDataColumn v_column = (GridVertexDataColumn)volume.getVertexDataColumn(ordered_columns.get(i));
    		models.add(v_column.getColourModel());
    		alphas.add(volume.getCompositeAlpha(ordered_columns.get(i)));
    		include.add(volume.showInComposite(ordered_columns.get(i)));
    		}
    	
    	return getCompositeRSliceImage(volume, r, ordered_columns, models, alphas, include);
    }
    
    public static WindowedColourModel getColourModel(){
		return getColourModel(DataBuffer.TYPE_BYTE, null, 1, 0, 1, 0.5, true);
	}
	
	public static WindowedColourModel getColourModel(int transferType){
		return getColourModel(transferType, null, 1, 0, 1, 0.5, true);
	}
	
	public static WindowedColourModel getColourModel(int transferType, ColourMap cmap){
		return getColourModel(transferType, cmap, 1, 0, 1, 0.5, true);
	}
	
	public static WindowedColourModel getColourModel(int transferType, ColourMap cmap, boolean setAlpha){
		return getColourModel(transferType, cmap, 1, 0, 1, 0.5, setAlpha);
	}
		
	public static WindowedColourModel getColourModel(int transferType, ColourMap cmap,
											double scale, double intercept, 
											double window_width, double window_mid,
											boolean setAlphaFromIntensity){

		if (cmap == null){
			//make all-grey map
			cmap = ContinuousColourMap.getGreyScale();
		}else{
//			cmap.mapMin = 0;
//			cmap.mapMax = 255;
			}
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		
		WindowedColourModel model = new WindowedColourModel(cmap, 
															scale, intercept,
															window_mid, window_width,
															setAlphaFromIntensity,
															transferType);
		return model;
		
		
	}
	
	/***************************
	 * Returns an {@linkplain Volume2DInt} object representing the intersection of the given 3D {@code volume}
	 * with {@code plane}. Returns {@code null} if the volume does not intersect the plane.
	 * 
	 * @param volume
	 * @param plane
	 * @return Volume2DInt object, in plane coordinates relative to its reference point
	 */
	
	public static Volume2DInt getIntersectionImage(Volume3DInt volume, Plane3D plane, boolean setAlpha){
		return getIntersectionImage(volume, plane, setAlpha, null);
	}
	
	/***************************
	 * Returns an {@linkplain Volume2DInt} object representing the intersection of the given 3D {@code volume}
	 * with {@code plane}. Returns {@code null} if the volume does not intersect the plane.
	 * 
	 * @param volume
	 * @param plane
	 * @return Volume2DInt object, in plane coordinates relative to its reference point
	 */
	
	public static Volume2DInt getIntersectionImage(Volume3DInt volume, Plane3D plane, boolean setAlpha, Matrix4d transform){
		
		if (volume.isComposite())
			return getIntersectionCompositeImage(volume, plane, setAlpha);
		
		Grid3D grid = volume.getGrid();
		
		// 1. Get intersection polygon
		Polygon2D poly = ShapeFunctions.getIntersectionPolygon(grid, plane);
		if (poly == null) return null;
		
		// 2. Get the bounding rectangle of this polygon
		Rect2D bounds = poly.getBounds();
		
		if (transform != null){
			GeometryFunctions.transform(poly, transform);
			GeometryFunctions.transform(bounds, transform);
			}
		
		// 3. Sample from the volume
		// 3.a. Determine the maximal sampling resolution
		
		float sample_res = grid.getGeomS() / (float)grid.getSizeS();
		sample_res = Math.min(sample_res, grid.getGeomT() / (float)grid.getSizeT());
		sample_res = Math.min(sample_res, grid.getGeomR() / (float)grid.getSizeR());
		
		// 3.b. Start at bottom left, scan through volume
		// First compute offset
		Vector3f offset = new Vector3f(grid.getOrigin());
		offset.sub(grid.getBasePt());
		Vector2f offset2 = GeometryFunctions.getProjectedToPlane2D(offset, plane);
		
		float x_offset = Math.abs(offset2.dot(new Vector2f(1,0)));
		float y_offset = Math.abs(offset2.dot(new Vector2f(0,1)));
		offset2.set(x_offset, y_offset);
		
		Point2f start_pt = new Point2f(bounds.getCorner(Rect2D.CNR_BL));
		start_pt.add(offset2);
		Point2f TR = new Point2f(bounds.getCorner(Rect2D.CNR_TR));
		float top = TR.getY();
		float right = TR.getX();
		Vector2f scan_x = new Vector2f(1,0);
		scan_x.scale(sample_res);
		Vector2f scan_y = new Vector2f(0,1);
		scan_y.scale(sample_res);
		
		Point2f scan_pt = new Point2f(start_pt);
		GridVertexDataColumn v_column = (GridVertexDataColumn)volume.getCurrentDataColumn();
		MguiNumber v;
		MguiNumber nv = (MguiNumber)v_column.getValueAtVertex(0).clone();
		nv.setValue(-Double.MAX_VALUE);
		ArrayList<MguiNumber> pixel_vals = new ArrayList<MguiNumber>();
		int x_pixels = 0, y_pixels = 0;
		boolean adding_x = true;
		
		while (scan_pt.y <= top){
			y_pixels++;
			while (scan_pt.x <= right){
				Point3f p = GeometryFunctions.getPointFromPlane(scan_pt, plane);
				v = null;
				if (!volume.isMaskedAtPoint(p))
					v = volume.getDatumAtPoint(p);
				if (v == null)
					v = (MguiNumber)nv.clone();
				pixel_vals.add(v);
				if (adding_x) x_pixels++;
				scan_pt.add(scan_x);
				}
			adding_x = false;
			scan_pt.x = start_pt.x;
			scan_pt.add(scan_y);
			}
			
		// 4. Create image
		WindowedColourModel colourModel = (WindowedColourModel)volume.getColourModel().clone();
		colourModel.setHasAlpha(setAlpha);
		//colourModel.setLowIsTransparent(false);
		WritableRaster raster = colourModel.createCompatibleWritableRaster(x_pixels, y_pixels); 
		BufferedImage bImage = new BufferedImage(colourModel, raster, false, null);
		int k = 0;
		
		for (int j = 0; j < y_pixels; j++){
			for (int i = 0; i < x_pixels; i++){
				raster.setPixel(i, y_pixels - j - 1, new double[]{pixel_vals.get(k++).getValue()});
				}
			}
		
		Volume2DInt volume_2D = new Volume2DInt(bounds, bImage, colourModel.hasAlpha()); 
		volume_2D.setOutline(poly);
		ArrayList<Point2f> nodes2D = bounds.getVertices();
		ArrayList<Point3f> nodes3D = GeometryFunctions.getVerticesFromSection(nodes2D, plane);
		Rect3D rect3D = new Rect3D();
		rect3D.setVertices(nodes3D);
		nodes2D = poly.getVertices();
		nodes3D = GeometryFunctions.getVerticesFromSection(nodes2D, plane);
		Polygon3D poly3D = new Polygon3D(nodes3D);
		volume_2D.setPlaneRect(rect3D);
		volume_2D.setPlanePoly(poly3D);
		
		return volume_2D;
	}
	
	/**************************************
	 * Returns an {@linkplain Volume2DInt} object representing the intersection of the given 3D composite {@code volume}
	 * with {@code plane}. Returns {@code null} if the volume does not intersect the plane.
	 * 
	 * @param volume
	 * @param plane
	 * @return Volume2DInt object, in plane coordinates relative to its reference point
	 */
	public static Volume2DInt getIntersectionCompositeImage(Volume3DInt volume, Plane3D plane, boolean setAlpha){
		
		
		//get bounds rectangle
		Grid3D grid = volume.getGrid();
		
		// 1. Get intersection polygon
		Polygon2D poly = ShapeFunctions.getIntersectionPolygon(grid, plane);
		if (poly == null) return null;
		
		// 2. Get the bounding rectangle of this polygon
		Rect2D bounds = poly.getBounds();
		
		// 3. Sample from the volume
		// 3.a. Determine the maximal sampling resolution
		Vector3f offset = new Vector3f(grid.getOrigin());
		offset.sub(grid.getBasePt());
		Vector2f offset2 = GeometryFunctions.getProjectedToPlane2D(offset, plane);
		
		float x_offset = Math.abs(offset2.dot(new Vector2f(1,0)));
		float y_offset = Math.abs(offset2.dot(new Vector2f(0,1)));
		offset2.set(x_offset, y_offset);
		
		float sample_res = grid.getGeomS() / (float)grid.getSizeS();
		sample_res = Math.min(sample_res, grid.getGeomT() / (float)grid.getSizeT());
		sample_res = Math.min(sample_res, grid.getGeomR() / (float)grid.getSizeR());
		
		// 3.b. Start at bottom right, scan through volume
		Point2f start_pt = new Point2f(bounds.getCorner(Rect2D.CNR_BL));
		start_pt.add(offset2);
		Point2f TR = new Point2f(bounds.getCorner(Rect2D.CNR_TR));
		float top = TR.getY();
		float right = TR.getX();
		Vector2f scan_x = new Vector2f(1,0);
		scan_x.scale(sample_res);
		Vector2f scan_y = new Vector2f(0,1);
		scan_y.scale(sample_res);
		
		ArrayList<WindowedColourModel> models = new ArrayList<WindowedColourModel>();
    	ArrayList<Double> alphas = new ArrayList<Double>();
    	ArrayList<Boolean> include = new ArrayList<Boolean>();
    	
    	ArrayList<String> ordered_columns = volume.getCompositeOrderedColumns();
    	
    	for (int i = 0; i < ordered_columns.size(); i++){
    		GridVertexDataColumn v_column_i = (GridVertexDataColumn)volume.getVertexDataColumn(ordered_columns.get(i));
    		models.add(v_column_i.getColourModel());
    		alphas.add(volume.getCompositeAlpha(ordered_columns.get(i)));
    		include.add(volume.showInComposite(ordered_columns.get(i)));
    		}
	    	
	    ArrayList<String> column_stack = new ArrayList<String>();
	    ArrayList<Double> alpha_stack = new ArrayList<Double>();
	    	
	    if (ordered_columns.size() == 0) return null;
	    	
    	// Get image stack (bottom renders first)
    	for (int i = 0; i < ordered_columns.size(); i++){
    		if (include == null || include.get(i)){
	    		column_stack.add(ordered_columns.get(i));
	    		alpha_stack.add(alphas.get(i));
	    		}
    		}
	    	
		boolean start = true;
		BufferedImage composite_image = null;
		Graphics2D g = null;
		Point2f scan_pt = new Point2f(start_pt);
		MguiNumber v;
		
		for (int k = column_stack.size() - 1; k > -1; k--){
			String column_k = column_stack.get(k);
			ArrayList<MguiNumber> pixel_vals = new ArrayList<MguiNumber>();
			int x_pixels = 0, y_pixels = 0;
			boolean adding_x = true;
			scan_pt.set(start_pt);
			MguiNumber nv = volume.getDatumAtVertex(column_k, 0);
				
    		// Construct image for this column
    		while (scan_pt.y <= top){
    			y_pixels++;
    			while (scan_pt.x <= right){
    				Point3f p = GeometryFunctions.getPointFromPlane(scan_pt, plane);
    				v = null;
    				if (!volume.isMaskedAtPoint(p))
    					v = volume.getDatumAtPoint(column_k, p);
    				if (v == null)
    					v = (MguiNumber)nv.clone();
    				pixel_vals.add(v);
    				if (adding_x) x_pixels++;
    				scan_pt.add(scan_x);
    				}
    			adding_x = false;
    			scan_pt.x = start_pt.x;
    			scan_pt.add(scan_y);
    			}
    		
    		GridVertexDataColumn v_column_k = (GridVertexDataColumn)volume.getVertexDataColumn(column_k);
    		WindowedColourModel colourModel = (WindowedColourModel)v_column_k.getColourModel().clone();
    		colourModel.setHasAlpha(volume.hasAlpha());
    		WritableRaster raster = colourModel.createCompatibleWritableRaster(x_pixels, y_pixels); 
    		BufferedImage image = new BufferedImage(colourModel, raster, false, null);
    		
			// Construct composite image if this is the bottom-most column
    		if (start){
    			ColorModel colour_model = ComponentColorModel.getRGBdefault();
    			WritableRaster c_raster = colour_model.createCompatibleWritableRaster(x_pixels, y_pixels);
    			composite_image = new BufferedImage(colour_model, c_raster, false, null);
    			g = composite_image.createGraphics();
    			start = false;
    			}
    		
    		int l = 0;
    		for (int j = 0; j < y_pixels; j++){
    			for (int i = 0; i < x_pixels; i++){
    				raster.setPixel(i, y_pixels - j - 1, new double[]{pixel_vals.get(l++).getValue()});
    				}
    			}
    		
			// Add image to composite
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha_stack.get(k).floatValue()));
			g.drawImage(image, 0, 0, null);
			}
			
			// Finish up
	    	if (g == null || composite_image == null)
	    		return null;
	    	
	    	g.dispose();
				
			Volume2DInt volume_2D = new Volume2DInt(bounds, composite_image, true); 
			volume_2D.setOutline(poly);
			ArrayList<Point2f> nodes2D = bounds.getVertices();
			ArrayList<Point3f> nodes3D = GeometryFunctions.getVerticesFromSection(nodes2D, plane);
			Rect3D rect3D = new Rect3D();
			rect3D.setVertices(nodes3D);
			nodes2D = poly.getVertices();
			nodes3D = GeometryFunctions.getVerticesFromSection(nodes2D, plane);
			Polygon3D poly3D = new Polygon3D(nodes3D);
			volume_2D.setPlaneRect(rect3D);
			volume_2D.setPlanePoly(poly3D);
			
			return volume_2D;
			
	}
	
	
	
	/***************************************************************
	* Get vectors representing the edges of plane, where it intersects grid,
	* which are closest to the S, T, and R edges, respectively
	* Return array is v_x, v_y, start_pt
	* 
	* @param grid
	* @param plane
	* ***************************************/
	private static Tuple3f[] getAxisEdges(Grid3D grid, Rect3D plane){
		
		ArrayList<Point3f> nodes = plane.getVertices();
		Vector3f[] edges = new Vector3f[2];
		edges[0] = new Vector3f();
		edges[0].sub(nodes.get(1), nodes.get(0));
		edges[1] = new Vector3f();
		edges[1].sub(nodes.get(3), nodes.get(0));
		Point3f[] flip_points = new Point3f[2];
		flip_points[0] = nodes.get(1);
		flip_points[1] = nodes.get(3);
		//edges[2] = new Vector3f();
		//edges[2].cross(edges[0], edges[1]);
		//edges[2].normalize();
		
		Vector3f[] flip_edges = new Vector3f[2];
		for (int i = 0; i < 2; i++){
			flip_edges[i] = new Vector3f(edges[i]);
			flip_edges[i].scale(-1);
			}
		
		Vector3f[] axes = new Vector3f[3];
		axes[0] = grid.getSAxis();
		axes[1] = grid.getTAxis();
		axes[2] = grid.getRAxis();
		
		Tuple3f[] result = new Tuple3f[3];
		
		for (int i = 0; i < 2; i++){
			double min_angle = Double.MAX_VALUE;
			for (int j = 0; j < 3; j++){
				double angle = edges[i].angle(axes[j]);
				if (angle < min_angle){
					result[i] = edges[i];
					result[2] = nodes.get(0);
					min_angle = angle;
					}
				angle = flip_edges[i].angle(axes[j]);
				if (angle < min_angle){
					result[i] = flip_edges[i];
					result[2] = flip_points[i];
					min_angle = angle;
					}
				}
			}
		
		return result;
		
	}
	
	/****************************************************
	 * Returns a {@link Grid3D} object whose data is the inverse of <code>grid</code>.
	 * 
	 * @param grid
	 * @param column
	 * @return
	 */
	public static Volume3DInt getInvertedVolume(Volume3DInt volume){
		String column = volume.getCurrentColumn();
		if (column == null) return null;
		return getInvertedVolume(volume, column, null);
	}
	
	/****************************************************
	 * Returns a {@link Grid3D} object whose data is the inverse of <code>grid</code>.
	 * 
	 * @param grid
	 * @param column
	 * @return
	 */
	public static Volume3DInt getInvertedVolume(Volume3DInt volume, String column){
		return getInvertedVolume(volume, column, null);
	}
	
	/****************************************************
	 * Returns a {@link Grid3D} object whose data is the inverse of <code>grid</code>.
	 * 
	 * @param volume - Volume to invert
	 * @param column - Column to invert
	 * @return
	 */
	public static Volume3DInt getInvertedVolume(Volume3DInt volume, String column, ProgressUpdater progress){
		
		Grid3D grid = volume.getGrid();
		Grid3D new_grid = new Grid3D(grid.getSizeS(), grid.getSizeT(), grid.getSizeR(), grid.getSizeV(), grid.getBoundBox());
		
		ArrayList<MguiNumber> data = volume.getVertexData(column);
		if (data == null) return null;
		
		ArrayList<MguiNumber> i_data = new ArrayList<MguiNumber>(data.size());
		
		for (int i = 0; i < data.size(); i++){
			MguiNumber num = (MguiNumber)data.get(i).clone();
			num.setValue(1);
			MguiNumber denom = (MguiNumber)data.get(i).clone();
			i_data.add(num.divide(denom));
			}
		
		Volume3DInt new_volume = new Volume3DInt(new_grid);
		new_volume.addVertexData(column, i_data);
		new_volume.setCurrentColumn(column);
		
		return new_volume;
	}
	
	/***********************************************************
	 * Geometrically smooths values from {@code source_column} in {@code volume} with an isotropic
	 * Gaussian smoothing kernel with a full-width-at-half-maximum of {@code fwhm}. 
	 * Writes the results to {@code target_column}. If {@code target_column} doesn't exist, 
	 * it will be created with the same data type as {@code source_column}. If it exists, it will 
	 * maintain its current data type and its values will be overwritten. {@code target_column} can 
	 * be the same as {@code source_column}.
	 * 
	 * @param volume
	 * @param source_column
	 * @param target_column
	 * @param fwhm 				Full-width at half maximum of Gaussian kernel
	 * @param max_radius 		Maximum radius at which to obtain values for smoothing. Values
	 * 							of 0 or less specify an infinite radius (not recommended)
	 * @return
	 */
	public static boolean smoothVolumeGaussian(Volume3DInt volume, 
												String source_column, 
												String target_column, 
												double fwhm,
												double max_radius){
		
		return smoothVolumeGaussian(volume, 
									source_column, 
									target_column, 
									fwhm,
									max_radius,
									null,
									0);
							
	}
	
	/***********************************************************
	 * Geometrically smooths values from {@code source_column} in {@code volume} with an isotropic
	 * Gaussian smoothing kernel with a full-width-at-half-maximum of {@code fwhm}. 
	 * Writes the results to {@code target_column}. If {@code target_column} doesn't exist, 
	 * it will be created with the same data type as {@code source_column}. If it exists, it will 
	 * maintain its current data type and its values will be overwritten. {@code target_column} can 
	 * be the same as {@code source_column}.
	 * 
	 * @param volume
	 * @param source_column
	 * @param target_column
	 * @param fwhm
	 * @param max_radius
	 * @return
	 */
	public static boolean smoothVolumeGaussian(final Volume3DInt volume, 
												final String source_column, 
												final String target_column, 
												final double fwhm,
												final double max_radius,
												final ProgressUpdater progress,
												final double default_value){

		if (progress == null){
			return smoothVolumeGaussianBlocking(volume, 
												source_column, 
												target_column, 
												fwhm,
												max_radius,
												null,
												default_value);
			}
		
		return (Boolean)Worker.post(new Job(){
			
			public Boolean run(){
				return smoothVolumeGaussianBlocking(volume, 
													source_column, 
													target_column, 
													fwhm,
													max_radius,
													progress,
													default_value);
			}
		});
		
	}
	
	/***********************************************************
	 * Geometrically smooths values from {@code source_column} in {@code volume} with an isotropic
	 * Gaussian smoothing kernel with a full-width-at-half-maximum of {@code fwhm}. 
	 * Writes the results to {@code target_column}. If {@code target_column} doesn't exist, 
	 * it will be created with the same data type as {@code source_column}. If it exists, it will 
	 * maintain its current data type and its values will be overwritten. {@code target_column} can 
	 * be the same as {@code source_column}.
	 * 
	 * @param volume
	 * @param source_column
	 * @param target_column
	 * @param fwhm
	 * @param max_radius
	 * @return
	 */
	public static boolean smoothVolumeGaussianBlocking(Volume3DInt volume, 
															String source_column, 
															String target_column, 
															double fwhm,
															double max_radius,
															ProgressUpdater progress,
															double default_value){
		
		double sigma = StatFunctions.getFWHMtoSigma(fwhm);
		if (max_radius <= 0) max_radius = Double.MAX_VALUE;
		
		// Deal with target column
		if (!volume.hasColumn(source_column)){
			InterfaceSession.log("smoothVolumeGaussianBlocking: source column '" + source_column + 
									" doesn't exist.", 
									LoggingType.Errors);
			return false;
			}
		
		GridVertexDataColumn s_column = (GridVertexDataColumn)volume.getVertexDataColumn(source_column);
		int data_type = s_column.getDataTransferType();
		
		if (!volume.hasColumn(target_column)){
			volume.addVertexData(target_column, data_type);
			}
		
		GridVertexDataColumn t_column = (GridVertexDataColumn)volume.getVertexDataColumn(target_column);
		
		// For each voxel, search neighbouring voxels
		Grid3D grid = volume.getGrid();
		int s_dist = (int)Math.ceil(max_radius * grid.getSDim() / (double)grid.getSizeS());
		int t_dist = (int)Math.ceil(max_radius * grid.getTDim() / (double)grid.getSizeT());
		int r_dist = (int)Math.ceil(max_radius * grid.getRDim() / (double)grid.getSizeR());
		
		if (progress != null){
			progress.setMinimum(0);
			progress.setMaximum(grid.getSizeS());
			progress.update(0);
			}
		
		double[][][] test = new double[s_dist*2][t_dist*2][r_dist*2];
		
		for (int i = 0; i < grid.getSizeS(); i++){
			int min_s = Math.max(0, i - s_dist);
			int max_s = Math.min(grid.getSizeS()-1, i + s_dist);
			for (int j = 0; j < grid.getSizeT(); j++){
				int min_t = Math.max(0, j - t_dist);
				int max_t = Math.min(grid.getSizeT()-1, j + t_dist);
				for (int k = 0; k < grid.getSizeR(); k++){
					int min_r = Math.max(0, k - r_dist);
					int max_r = Math.min(grid.getSizeR()-1, k + r_dist);
					
					// Get this voxel mid-point
					Point3f vp = grid.getVoxelMidPoint(i, j, k);
					
					// stats
					double sum = 0;
					double denom = 0;
					
					// Is this neighbourhood non-zero?
					for (int ii = min_s; ii < max_s; ii++)
						for (int jj = min_t; jj < max_t; jj++)
							for (int kk = min_r; kk < max_r; kk++){
								test[ii-min_s][jj-min_t][kk-min_r]=s_column.getValueAtVoxel(ii, jj, kk).getValue();
								sum += test[ii-min_s][jj-min_t][kk-min_r];
								}
					
					// Search neighbourhood
					if (sum > 0){
						sum = 0;
						for (int ii = min_s; ii < max_s; ii++)
							for (int jj = min_t; jj < max_t; jj++)
								for (int kk = min_r; kk < max_r; kk++){
									double dist = vp.distance(grid.getVoxelMidPoint(ii, jj, kk));
									if (dist < max_radius){
										double v = test[ii-min_s][jj-min_t][kk-min_r]; // s_column.getValueAtVoxel(ii, jj, kk).getValue();
										double g = StatFunctions.getGaussian(dist, 0, sigma);
										sum +=  v *	g;
										denom += g;
										}
									}
						
						t_column.getValueAtVoxel(i, j, k).setValue(sum / denom);
					}else{
						t_column.getValueAtVoxel(i, j, k).setValue(default_value);
						}
					
					if (progress != null && progress.isCancelled()){
						InterfaceSession.log("VolumeFunctions: Smooth Gaussian operation cancelled by user.", 
								LoggingType.Warnings);
						volume.removeVertexData(target_column);
						return false;
						}
					}
				}
			
			if (progress != null){
				progress.update(i);
				}
			}
		
		t_column.updateDataLimits();
		return true;
	}
	
	/**************************************************
	 * Computes a value for <code>voxel</code> which is the weighted average of a Gaussian sampling of its
	 * neighbouring voxels, where the Gaussian is defined by a normal vector, its corresponding plane, and 
	 * the sigma parameters <code>sigma_normal</code> and <code>sigma_tangent</code>.
	 * 
	 * @param grid grid from which to compute value
	 * @param voxel voxel for which to compute a value
	 * @param normal normal direction of Gaussian distribution
	 * @param sigma_normal sigma of Gaussian in normal direction
	 * @param sigma_tangent sigma of Gaussian in tangent direction
	 * @param cutoff value at which to bound search space, in multiples of sigma
	 * @return
	 */
	public static double getGaussianSmoothedValue(final Volume3DInt volume, final int[] voxel, final int channel,
												  final Vector3f normal, 
												  final float sigma_normal,
												  final float sigma_tangent,
												  final float cutoff){
		
		Grid3D grid = volume.getGrid();
		
		//1. Get search space, defined by cutoff in each grid axis direction
		Point3f mp = grid.getVoxelMidPoint(voxel[0], voxel[1], voxel[2]);
		Point3f p = new Point3f(mp);
		Vector3f v_bounds = grid.getSAxis();
		v_bounds.normalize();
		v_bounds.scale(Math.max(sigma_normal, sigma_tangent) * cutoff);
		p.add(v_bounds);
		Point3f max_pt = new Point3f(p);
		Point3f min_pt = new Point3f(p);
		p.set(mp);
		p.sub(v_bounds);
		max_pt = GeometryFunctions.getMaxPt(max_pt, p);
		min_pt = GeometryFunctions.getMinPt(min_pt, p);
		v_bounds = grid.getTAxis();
		v_bounds.normalize();
		v_bounds.scale(Math.max(sigma_normal, sigma_tangent) * cutoff);
		p.add(v_bounds);
		max_pt = GeometryFunctions.getMaxPt(max_pt, p);
		min_pt = GeometryFunctions.getMinPt(min_pt, p);
		p.set(mp);
		p.sub(v_bounds);
		max_pt = GeometryFunctions.getMaxPt(max_pt, p);
		min_pt = GeometryFunctions.getMinPt(min_pt, p);
		v_bounds = grid.getRAxis();
		v_bounds.normalize();
		v_bounds.scale(Math.max(sigma_normal, sigma_tangent) * cutoff);
		p.add(v_bounds);
		max_pt = GeometryFunctions.getMaxPt(max_pt, p);
		min_pt = GeometryFunctions.getMinPt(min_pt, p);
		p.set(mp);
		p.sub(v_bounds);
		max_pt = GeometryFunctions.getMaxPt(max_pt, p);
		min_pt = GeometryFunctions.getMinPt(min_pt, p);
		
		int[] sub_vol = grid.getSubGrid(min_pt, max_pt);
		//InterfaceSession.log("Gaussian smoothing: sub volume (" + min_pt.toString() + ") to (" + max_pt.toString() + ")");
		
		if (sub_vol == null){
			//InterfaceSession.log("Gaussian smoothing error: could not get sub volume for voxel (" + voxel[0] + "," +
			//		voxel[1] + ", " + voxel[2] + ")..");
			return Double.NaN;
			}
		
		Point3f mp_2 = new Point3f();
		Vector3f v = new Vector3f();
		Vector3f v_normal = new Vector3f();
		Vector3f v_tangent = new Vector3f();
		double w_sum = StatFunctions.getGaussian(0, 0, sigma_normal) * StatFunctions.getGaussian(0, 0, sigma_tangent);
		double new_value = w_sum * volume.getDatumAtVoxel(voxel).getValue();
		
		//2. For each voxel in search space, get value weighted by Gaussian
		for (int i = sub_vol[0]; i < sub_vol[3]; i++)
			for (int j = sub_vol[1]; j < sub_vol[4]; j++)
				for (int k = sub_vol[2]; k < sub_vol[5]; k++){
					
					mp_2.set(grid.getVoxelMidPoint(i, j, k));
					v.sub(mp_2, mp);
					v_normal.set(GeometryFunctions.getProjectedVector(v, normal));
					v_tangent.set(GeometryFunctions.getProjectedToPlane(v, v_normal));
					
					double w = StatFunctions.getGaussian(v_normal.length(), 0, sigma_normal);
					w *= StatFunctions.getGaussian(v_normal.length(), 0, sigma_normal);
					
					w_sum += w;
					new_value += volume.getDatumAtVoxel(i, j, k).getValue() * w;
					}
		
		//3. Divide by sum of weights
		new_value /= w_sum;

		return new_value;
			
	}
	
	public static enum Operation{
		Add,
		Subtract,
		Multiply,
		Divide,
		Average;
	}
	
	/*******************************************************
	 * Performs the specified operation on the two volumes, and returns the result as a new {@link Grid3D}
	 * instance. Uses the current vertex data columns.
	 * 
	 * @param grid1
	 * @param grid2
	 * @param op
	 * @return
	 */
	public static Volume3DInt performOperationBlocking(Volume3DInt volume1, Volume3DInt volume2, Operation op, ProgressUpdater progress){
		
		Grid3D grid1 = volume1.getGrid();
		Grid3D grid2 = volume2.getGrid();
		int x_size = Math.min(grid1.getSizeS(), grid2.getSizeS());
		int y_size = Math.min(grid1.getSizeT(), grid2.getSizeT());
		int z_size = Math.min(grid1.getSizeR(), grid2.getSizeR());
		
		Grid3D new_grid = new Grid3D(x_size, y_size, z_size, grid1.getBoundBox());
		Volume3DInt new_volume = new Volume3DInt(new_grid);
		
		for (int i = 0; i < x_size; i++)
			for (int j = 0; j < y_size; j++)
				for (int k = 0; k < z_size; k++){
					int[] voxel = new int[]{i,j,k}; 
					double d1 = volume1.getDatumAtVoxel(voxel).getValue();
					double d2 = volume2.getDatumAtVoxel(voxel).getValue();
					switch (op){
						case Add:
							new_volume.setDatumAtVoxel(voxel, d1 + d2);
							break;
						case Subtract:
							new_volume.setDatumAtVoxel(voxel, d1 - d2);
							break;
						case Multiply:
							new_volume.setDatumAtVoxel(voxel, d1 * d2);
							break;
						case Divide:
							new_volume.setDatumAtVoxel(voxel, d1 / d2);
							break;
						case Average:
							new_volume.setDatumAtVoxel(voxel, (d1 + d2) / 2.0);
							break;
						}
					}
		
		return new_volume;
		
	}
	
	/*******************************************************
	 * Performs the specified operation on one grid and a constant value, and returns and new volume.
	 * 
	 * @param grid
	 * @param value
	 * @param op
	 * @return
	 */
	public static Volume3DInt performOperationBlocking(Volume3DInt volume, Double value, Operation op, ProgressUpdater progress){
		
		Grid3D grid = volume.getGrid();
		Volume3DInt new_volume = new Volume3DInt(new Grid3D(grid));
		
		int x_size = grid.getSizeS();
		int y_size = grid.getSizeT();
		int z_size = grid.getSizeR();
		
		for (int i = 0; i < x_size; i++)
			for (int j = 0; j < y_size; j++)
				for (int k = 0; k < z_size; k++){
					int[] voxel = new int[]{i,j,k}; 
					double d1 = volume.getDatumAtVoxel(voxel).getValue();
					switch (op){
						case Add:
							new_volume.setDatumAtVoxel(voxel, d1 + value);
							break;
						case Subtract:
							new_volume.setDatumAtVoxel(voxel, d1 - value);
							break;
						case Multiply:
							new_volume.setDatumAtVoxel(voxel, d1 * value);
							break;
						case Divide:
							new_volume.setDatumAtVoxel(voxel, d1 / value);
							break;
						case Average:
							new_volume.setDatumAtVoxel(voxel, (d1 + value) / 2.0);
							break;
						}
					}
		
		return new_volume;
		
	}
	
	/*******************************
	 * Applies a voxel mask to an ImageComponent3D object, using the parameters
	 * in params.
	 * @param grid Grid3D object upon which to apply mask
	 * @param params specifies the parameters describing the mask 
	 */
	public static void applyMask(ImageComponent3D iComp, int index, int x, int y,
								int width, int height, VolumeMaskOptions_old params){
		
		int min = (int)(params.minThreshold * 255);
    	int max = (int)(params.maxThreshold * 255); 
		
		//for each voxel ijk
		BufferedImage bImage = iComp.getImage(index);
		if (bImage == null)
			InterfaceSession.log("bImage is null at " + index);
		
		//for (int k = 0; k < iComp.getDepth(); k++)
			for (int j = y; j < height; j++)
				for (int i = x; i < width; i++){
					//determine whether ijk is in mask shape
					if (isInMask(i, j, index, params)){
						if (params.inputType == VolumeMaskOptions_old.INPUT_CONST){
							//set to constant
							int b = getByteValue(params.inputValue);
							setValue(bImage, i, j, params.outputChannel, b);
							//grid.setValue(i, j, k, params.outputChannel - 1, getByteValue(params.inputValue));
						}else{
							//otherwise set from data channel	
							int b = getValue(bImage, i, j, params.inputChannel);
							
							//invert if required
							if (params.invertData) b = 255 - b;
							
							//thresholds
							if (b < min){
			                	int t = (int)(Math.pow((min - b), params.smoothingExp));
			                	if (params.invertMin)
			                		b = Math.min((255 - min + t), 255);
			                	else
			                		b = Math.max(min - t, 0);
				            	}
							if (b > max){
								int t = (int)(Math.pow((b - max), params.smoothingExp));
								if (params.invertMax)
									b = Math.max(255 - max - t, 0);
								else
									b = Math.min(max + t, 255);
			                	}
							
							//apply factoring
							if (params.inputOp == VolumeMaskOptions_old.FACT_LINEAR)
								b *= params.inputFactor;
							if (params.inputOp == VolumeMaskOptions_old.FACT_EXP)
								b = (int)Math.pow(b, params.inputFactor);
							if (params.inputOp == VolumeMaskOptions_old.FACT_LOG)
								b = (int)Math.log(b); 	//can factor be included here?
							
							//keep within range 0-1
							b = Math.max(0, b);
							b = Math.min(255, b);
							
							//set channel
							setValue(bImage, i, j, params.outputChannel, b);
							}
						}
					}
		
	}
	
	/*******************************
	 * Applies a voxel mask to an ImageComponent3D object, using the parameters
	 * in params.
	 * @param grid Grid3D object upon which to apply mask
	 * @param params specifies the parameters describing the mask 
	 */
	public static boolean[][][] getMask(Grid3D grid, VolumeMaskOptions_old params){
		int x_size = grid.getSizeS();
		int y_size = grid.getSizeT();
		int z_size = grid.getSizeR();
		
    	boolean[][][] mask = new boolean[x_size][y_size][z_size];
		
    	for (int i = 0; i < x_size; i++)
    		for (int j = 0; j < y_size; j++)
    			for (int k = 0; k < z_size; k++)
    				if (isInMask(i, j, k, params))
    					mask[i][j][k] = true;
    	return mask;
	}
	
	//set the value for a single pixel in bImage
	private static void setValue(BufferedImage bImage, 
								 int x, int y, 
								 int channel, int value){
		int[] pixel = null;
		pixel = bImage.getRaster().getPixel(x, y, pixel);
		pixel[channel] = value;
		bImage.getRaster().setPixel(x, y, pixel);
		//pixel = bImage.getRaster().getPixel(x, y, pixel);
		return;
	}
	
//	set the value for a single pixel in bImage
	private static int getValue(BufferedImage bImage, 
								 int x, int y, 
								 int channel){
		int[] pixel = null;
		pixel = bImage.getRaster().getPixel(x, y, pixel);
		return pixel[channel];
	}
	
	/*********************************
	 * Compiles an overlay from the list of images by adding them one-by-one starting from
	 * the last element (since this will be on the bottom).
	 * 
	 * @param images
	 * @return the overlaid image
	 */
	public static BufferedImage getOverlayImage(ArrayList<BufferedImage> images, WindowedColourModel colour_model){
		
		if (images.size() == 1) return images.get(0);
		
		BufferedImage image1 = images.get(images.size() - 1);
		
		for (int i = images.size() - 2; i > 0; i++){
			BufferedImage image2 = images.get(i);
			RenderedOp overlay = JAI.create("overlay", image1, image2);
			image1 = new BufferedImage(colour_model, overlay.copyData(), false, null);
			}
		
		return image1;
	}
	
	/*******************************************************
	 * Returns an integer mask of {@code grid}, where a voxel has value = 1, when 
	 * {@code grid(i,j,k,v) == comp_value} (within tolerance), and 0 otherwise.
	 * 
	 * @param grid
	 * @param value
	 * @return
	 */
	public static Volume3DInt getIntMaskedByValue(Volume3DInt volume, double comp_value){
		return getIntMaskedByValue(volume, comp_value, false);
	}
	
	/*******************************************************
	 * Returns an integer mask of {@code grid}, where a voxel has value = 1, when 
	 * {@code grid(i,j,k,v) == comp_value} (within tolerance), and 0 otherwise.
	 * 
	 * @param grid
	 * @param value
	 * @param round  		If true, rounds the grid value before comparison
	 * @return
	 */
	public static Volume3DInt getIntMaskedByValue(Volume3DInt volume, double comp_value, boolean round){
		
		Grid3D grid = volume.getGrid();
		Volume3DInt mask_volume = new Volume3DInt(new Grid3D(grid));
		mask_volume.addVertexData("mask", DataBuffer.TYPE_INT);
		mask_volume.setCurrentColumn("mask");
		
		int n = grid.getSize();
		for (int index = 0; index < n; index++){
			double val = volume.getDatumAtVertex(index).getValue();
			if (round) val = Math.round(val);
			if (GeometryFunctions.compareDouble(val, comp_value) == 0)
				mask_volume.setDatumAtVertex(index, 1);
			}
		
		return mask_volume;
		
	}
	
	/*********************************
	 * Determines whether the voxel at i, j, k is in the mask specified by params.
	 * @param i
	 * @param j
	 * @param k
	 * @param params
	 * @return true if voxel is in mask shape, false otherwise
	 */
	public static boolean isInMask(int i, int j, int k, VolumeMaskOptions_old params){
		boolean isIn = false;
		//all voxels are obviously in this shape
		if (params.maskShape == VolumeMaskOptions_old.SHAPE_ALL) 
			isIn = true;
		
		//simple calculation for box shape
		if (params.maskShape == VolumeMaskOptions_old.SHAPE_BOX){
			isIn = i >= params.x1 && i <= params.x2 &&
				   j >= params.y1 && j <= params.y2 &&
				   k >= params.z1 && k <= params.z2;
			}
		
		//1-axis
		if (params.maskShape == VolumeMaskOptions_old.SHAPE_AXIS1){
			int c = i;
			//which axis?
			switch (params.a1){
				case 1:
					c = i;
					break;
				case 2:
					c = j;
					break;
				case 3:
					c = k;
				}
			isIn = c >= params.x1 - (params.p1 / 2) && 
				   c <= params.x1 + (params.p1 / 2);
			}
		
		//2-axis
		if (params.maskShape == VolumeMaskOptions_old.SHAPE_AXIS2){
			int c = i, d = j;
			//which axis?
			switch (params.a1){
				case 1:
					c = i;
					break;
				case 2:
					c = j;
					break;
				case 3:
					c = k;
				}
			switch (params.a2){
				case 1:
					d = i;
					break;
				case 2:
					d = j;
					break;
				case 3:
					d = k;
				}
			isIn = (c >= params.x1 - (params.p1 / 2) && 
				    c <= params.x1 + (params.p1 / 2)) ||
				   (d >= params.y1 - (params.p2 / 2) && 
				    d <= params.y1 + (params.p2 / 2));
			}
		
		//3-axis
		if (params.maskShape == VolumeMaskOptions_old.SHAPE_AXIS3){
			isIn = (i >= params.x1 - (params.p1 / 2) && 
				    i <= params.x1 + (params.p1 / 2)) ||
				   (j >= params.y1 - (params.p2 / 2) && 
				    j <= params.y1 + (params.p2 / 2))||
				   (k >= params.z1 - (params.p3 / 2) && 
					k <= params.z1 + (params.p3 / 2));
			}
		
		//invert if specified
		return params.invertShape != isIn;
	}
		
	/*****************************************************************
	 * Rotates a {@link Grid3D} about one of its basis vectors (axes), either 90, 180, or 270
	 * degrees, resizes the relevant dimensions, and returns the new grid.
	 * 
	 * <p>Note: only operates currently on the default channel.
	 * 
	 * <p>TODO: implement this on generic channel
	 * 
	 * @param axis The spatial axis about which to rotate (one of S, T, or R).
	 */
	public static Volume3DInt applyRotation(final VolumeAxis axis, final VolumeRotationAngle angle, 
									   final Volume3DInt volume, final InterfaceProgressBar progress_bar){
		
		
		Volume3DInt new_grid = (Volume3DInt)Worker.post(new Job(){
			
			public Volume3DInt run(){
				return applyRotationBlocking(axis, angle, volume, progress_bar);
			}
		});
		
		return new_grid;
		
	}
	
	/*****************************************************************
	 * Rotates a {@link Grid3D} about one of its basis vectors (axes), either 90, 180, or 270
	 * degrees, resizes the relevant dimensions, and returns the new grid.
	 * 
	 * <p>Note: only operates currently on the default channel.
	 * 
	 * <p>TODO: implement this on generic channel
	 * 
	 * @param axis The spatial axis about which to rotate (one of S, T, or R).
	 * @param angle The angle to rotate (one of 90, 180, 270)
	 * @param grid The grid to rotate
	 */
	public static Volume3DInt applyRotationBlocking(VolumeAxis axis, VolumeRotationAngle angle, Volume3DInt volume){
		return applyRotationBlocking(axis, angle, volume, null);
	}
	
	/*****************************************************************
	 * Rotates a {@link Volume3DInt} about one of its basis vectors (axes), either 90, 180, or 270
	 * degrees, resizes the relevant dimensions, and returns the new grid.
	 * 
	 * <p>Note: only operates currently on the default channel.
	 * 
	 * <p>TODO: implement this on generic channel
	 * 
	 * @param axis The spatial axis about which to rotate (one of S, T, or R).
	 * @param angle The angle to rotate (one of 90, 180, 270)
	 * @param grid The grid to rotate
	 * @param progress_bar The progress bar to track progress; can be <code>null</code>.
	 */
	public static Volume3DInt applyRotationBlocking(VolumeAxis axis, VolumeRotationAngle angle, Volume3DInt volume,
											   InterfaceProgressBar progress_bar){
		
		//get new x, y, z dimensions
		Grid3D grid = volume.getGrid();
		int[] dims = new int[]{grid.getSizeS(), grid.getSizeT(), grid.getSizeR()};
		int[] new_dims = getRotatedDims(dims, axis, angle);
		Box3D new_box = getRotatedBox(grid.getBoundBox(), axis, angle);
		
		Volume3DInt volume_new = new Volume3DInt(new Grid3D(grid));
		volume_new.addVertexData("Default", volume.getDataType());
		volume_new.setCurrentColumn("Default");
		
		int S = grid.getSizeS();
		int T = grid.getSizeT();
		int R = grid.getSizeR();
		
		//update on every S iteration
		if (progress_bar != null){
			progress_bar.setMinimum(0);
			progress_bar.setMaximum(S);
			progress_bar.reset();
			}
		
		for (int i = 0; i < grid.getSizeS(); i++){
			for (int j = 0; j < grid.getSizeT(); j++)
				for (int k = 0; k < grid.getSizeR(); k++){
					double d = volume.getDatumAtVoxel(i, j, k).getValue();
					int _i = -1, _j = -1, _k = -1;
					//get rotated grid coordinates
					switch(axis){
						case S:
							switch (angle){
								case ROT90:
									_i = i;
									_j = k;
									_k = j;
									break;
								case ROT180:
									_i = i;
									_j = T - j - 1;
									_k = R - k - 1;
									break;
								case ROT270:
									_i = i;
									_j = R - k - 1;
									_k = T - j - 1;
									break;
								}
							break;
						
						case T:
							switch (angle){
							case ROT90:
								_i = k;
								_j = j;
								_k = i;
								break;
							case ROT180:
								_i = S - i - 1;
								_j = j;
								_k = R - k - 1;
								break;
							case ROT270:
								_i = R - k - 1;
								_j = j;
								_k = S - i - 1;
								break;
							}
							break;
											
						case R:
							switch (angle){
							case ROT90:
								_i = k;
								_j = i;
								_k = k;
								break;
							case ROT180:
								_i = S - i - 1;
								_j = T - j - 1;
								_k = k;
								break;
							case ROT270:
								_i = R - k - 1;
								_j = S - i - 1;
								_k = k;
								break;
							}
							break;
						}
					//write rotated value
					volume_new.setDatumAtVoxel(_i, _j, _k, d);
					}
				if (progress_bar != null)
					progress_bar.setValue(i);
				}
		
		return volume_new;
	}
	
	protected static int[] getRotatedDims(int[] dims, VolumeAxis axis, VolumeRotationAngle angle){
		
		if (angle.equals(VolumeRotationAngle.ROT180)) return new int[]{dims[0], dims[1], dims[2]};
		
		switch(axis){
		
			case S:
				return new int[]{dims[0], dims[2], dims[1]};
			
			case T:
				return new int[]{dims[2], dims[1], dims[0]};
								
			case R:
				return new int[]{dims[1], dims[0], dims[2]};
				
			}
		
		return null;
	}
	
	protected static Box3D getRotatedBox(Box3D box, VolumeAxis axis, VolumeRotationAngle angle){
		
		if (angle.equals(VolumeRotationAngle.ROT180))
			return new Box3D(box);
		
		Point3f p = box.getBasePt();
		Vector3f s = box.getSAxis();
		Vector3f t = box.getTAxis();
		Vector3f r = box.getRAxis();
		float l;
		
		switch(axis){
		
		case S:
			p.set(p.x, p.z, p.y);
			l = t.length();
			t.normalize();
			t.scale(r.length());
			r.normalize();
			r.scale(l);
			return new Box3D(p, box.getSAxis(), t, r);
		
		case T:
			p.set(p.z, p.y, p.x);
			l = s.length();
			s.normalize();
			s.scale(r.length());
			r.normalize();
			r.scale(l);
			return new Box3D(p, s, box.getTAxis(), r);
							
		case R:
			p.set(p.y, p.x, p.z);
			l = s.length();
			s.normalize();
			s.scale(t.length());
			t.normalize();
			t.scale(l);
			return new Box3D(p, s, t, box.getRAxis());
			
		}
		
		return null;
	}
	
	
	/***************************************************************
	 * Returns new dimensions for a rotation about a basis axis.
	 * 
	 */
	public static int[] rotateDims(int[] dims, int axis, int rotation){
		int x = dims[0], y = dims[1], z = dims[2];
		//180 rotation doesn't change dimensions
		if (rotation == ROT180) return new int[]{x, y, z};
		switch (axis){
			case X:
				//x' = x, y' = z, z' = y
				return new int[]{x, z, y};
			case Y:
				//x' = z, y' = y, z' = x
				return new int[]{z, y, x};
			case Z:
				//x' = y, y' = x, z' = z
				return new int[]{y, x, z};
			default:
				return null;
			}
	}
	
	public static int[] rotate(int[] coords, int[] dims, int axis, int rotation){
		int x = coords[0], y = coords[1], z = coords[2];
		switch (axis){
			case X:
				switch (rotation){
					case ROT90:
						//rotate 90 about X
						//y' = max - z; z' = y
						//InterfaceSession.log("coords " + x + ", " + y + ", " + z + " -> " +
						//							   x + ", " + (dims[2] - z) + ", " + y);
						return new int[]{x, dims[2] - 1 - z, y};
					case ROT180:
						//rotate 180 about X
						//y' = max - y; z' = max - z
						return new int[]{x, dims[1] - 1 - y, dims[2] - 1 - z};
					case ROT270:
						//rotate 270 about X
						//y' = max - z; z' = max - y
						return new int[]{x, dims[2] - 1 - z, dims[1] - 1 - y};
					default:
						return null;
					}
				
			case Y:
				switch (rotation){
					case ROT90:
						//rotate 90 about Y
						//z' = max - x; x' = z
						return new int[]{z, y, dims[0] - 1 - x};
					case ROT180:
						//rotate 180 about Y
						//z' = max - z; x' = max - x
						return new int[]{dims[0] - 1 - x, y, dims[2] - 1 - z};
					case ROT270:
						//rotate 270 about Y
						//z' = max - x; x' = max - z
						return new int[]{dims[2] - 1 - z, y, dims[0] - 1 - x};
					default:
						return null;
					}
				
			case Z:
				switch (rotation){
					case ROT90:
						//rotate 90 about Z
						//y' = max - x; x' = y
						return new int[]{y, dims[1] - 1 - y, z};
					case ROT180:
						//rotate 180 about Z
						//y' = max - y; x' = max - y
						return new int[]{dims[0] - 1 - x, dims[1] - 1 - y, z};
					case ROT270:
						//rotate 270 about Z
						//y' = max - x; x' = max - y
						return new int[]{dims[1] - 1 - y, dims[0] - 1 - x, z};
					default:
						return null;
					}
				
			default:
				return null;
		
		}
		
	}
	
	public static int powerOfTwo(int value) {
		int retval = 2;
		while (retval < value)
		    retval *= 2;
		return retval;
	}
	
	public static byte getByteValue(double d){
		if (d < 0) return 0;
		int i = (int)(255.0 * d);
		i = Math.min(255, i);
		return intToByte(i);
	}
	
	public static byte intToByte(int i){
		if (i >= 0 && i < 128) return (byte)i;
		if (i > 128 && i < 256) return (byte)(i - 256);
		return 0;
	}
	
	public static double getDoubleValue(byte b){
		return b / 255.0;
	}
	
	public static int byteToInt(byte b){
		int val = b;
		if (val < 0) val += 256;
		return val;
	}
	
	/*********************************************************
	 * Returns a histogram for the current column of {@code volume}. 
	 * 
	 * @param volume
	 * @param bins
	 * @return
	 */
	public static Histogram getHistogram(Volume3DInt volume, int bins){
		return getHistogram(volume, 0, bins, volume.getDataMin(), volume.getDataMax());
	}
	
	/********************************************************
	 * Returns a histogram for the current column of {@code volume}. 
	 * 
	 * @param volume
	 * @param t
	 * @param bins
	 * @param min
	 * @param max
	 * @return
	 */
	public static Histogram getHistogram(Volume3DInt volume, int t, int bins, double min, double max){
		return getHistogram(volume, volume.getCurrentColumn(), t, bins, min, max);
	}
	
	/*********************************************************
	 * Returns a histogram for the vertex column {@code column} of {@code volume}. 
	 * 
	 * @param volume
	 * @param column
	 * @param t
	 * @param bins
	 * @param min
	 * @param max
	 * @return
	 */
	public static Histogram getHistogram(Volume3DInt volume, String column, int t, int bins, double min, double max){	
		
		Grid3D grid = volume.getGrid();
		
		if (grid == null || !volume.hasColumn(column))
			return null;
		
		Histogram h = new Histogram();
//		h.dataMin = min;
//		h.dataMax = max;
		h.set(bins, min, max);
		
		for (int i = 0; i < grid.getSizeS(); i++)
			for (int j = 0; j < grid.getSizeT(); j++)
				for (int k = 0; k < grid.getSizeR(); k++)
					h.addValue(volume.getDatumAtVoxel(column, new int[]{i, j, k, t}).getValue());
		
		return h;
	}
	
	/********************************************************************
	 * Maps {@code volume_source} to {@code volume_target}, using the enclosing voxel to assign the
	 * value. If no enclosing voxel exists, assigns a value of 0.
	 * 
	 * @param volume_source 	
	 * @param volume_target
	 * @param column_source
	 * @param column_target
	 * @return
	 */
	public static void mapVolumeToVolumeEV(Volume3DInt volume_source, 
										   Volume3DInt volume_target, 
										   String column_source,
										   String column_target){
		mapVolumeToVolumeEV(volume_source, volume_target, column_source, column_target, 0, null);
	}
	
	/********************************************************************
	 * Maps {@code volume_source} to {@code volume_target}, using the enclosing voxel to assign the
	 * value. If no enclosing voxel exists, assigns a value of 0.
	 * 
	 * @param volume_source 	
	 * @param volume_target
	 * @param column_source
	 * @param column_target
	 * @param progress
	 * @return
	 */
	public static void mapVolumeToVolumeEV(Volume3DInt volume_source, 
										   Volume3DInt volume_target, 
										   String column_source,
										   String column_target,
										   ProgressUpdater progress){
		mapVolumeToVolumeEV(volume_source, volume_target, column_source, column_target, 0, progress);
	}
	
	/********************************************************************
	 * Maps {@code volume_source} to {@code volume_target}, using the enclosing voxel to assign the
	 * value. If no enclosing voxel exists, assigns {@code default_value}. This method is equivalent
	 * to "nearest-neighbour" interpolation.
	 * 
	 * @param volume_source 	
	 * @param volume_target
	 * @param column_source
	 * @param column_target
	 * @param default_value
	 * @return
	 */
	public static boolean mapVolumeToVolumeEV(final Volume3DInt volume_source, 
											  final Volume3DInt volume_target, 
											  final String column_source,
											  final String column_target,
											  final double default_value,
											  final ProgressUpdater progress){
		
		if (progress == null){
			return mapVolumeToVolumeEVBlocking(volume_source, 
										   	  volume_target, 
										   	  column_source,
										   	  column_target,
										   	  default_value,
										   	  null);
			}
		
		return (Boolean)Worker.post(new Job(){
			@Override
			public Boolean run(){
				return mapVolumeToVolumeEVBlocking(volume_source, 
											   	   volume_target, 
											   	   column_source,
											   	   column_target,
											   	   default_value,
											   	   progress);
				}
			});
		
	}
	
	/********************************************************************
	 * Maps {@code volume_source} to {@code volume_target}, using the enclosing voxel to assign the
	 * value. If no enclosing voxel exists, assigns {@code default_value}.
	 * 
	 * @param volume_source 	
	 * @param volume_target
	 * @param column_source
	 * @param column_target
	 * @param default_value
	 * @return
	 */
	protected static boolean mapVolumeToVolumeEVBlocking(Volume3DInt volume_source, 
												   	  Volume3DInt volume_target, 
												   	  String column_source,
												   	  String column_target,
												   	  double default_value,
												   	  ProgressUpdater progress){
		
		if (!volume_target.hasColumn(column_target)){
			InterfaceSession.log("VolumeFunctions.mapVolumeToVolumeEV: No column '" + column_target + "' in target " +
								 "volume '" + volume_target.getFullName() + "'", 
								 LoggingType.Errors);
			return false;
			}
		
		// For each voxel in the target volume
		// find the enclosing voxel in the source volume
		// and assign its value, or the default value if no
		// enclosing voxel exists
		
		int n_target = volume_target.getVertexCount();
		Grid3D grid_target = volume_target.getGrid();
		Grid3D grid_source = volume_source.getGrid();
		GridVertexDataColumn t_column = (GridVertexDataColumn)volume_target.getVertexDataColumn(column_target);
		GridVertexDataColumn s_column = (GridVertexDataColumn)volume_source.getVertexDataColumn(column_source);
		int tr_size = grid_target.getSizeT() * grid_target.getSizeR();
		int s_size = grid_target.getSizeS();
		
		if (progress != null){
			progress.setMinimum(0);
			progress.setMaximum(s_size);
			}
		
		int j = 0,k = 0;
		for (int i = 0; i < n_target; i++){
			j++;
			Point3f cp = grid_target.getVoxelMidPoint(i);
			int[] ev = grid_source.getEnclosingVoxel(cp);
			if (ev == null){
				t_column.getValueAtVertex(i).setValue(default_value);
			}else{
				t_column.getValueAtVertex(i).setValue(s_column.getValueAtVoxel(ev[0], ev[1], ev[2]));
				}
			if (j == tr_size){
				if (progress != null)
					progress.update(k++);
				j = 0;
				}
			}
		
		return true;
		
	}

	
	/********************************************************************
	 * Maps {@code volume_source} to {@code volume_target}, using interpolation from all overlapping 
	 * voxels to assign the value. The {@code method} must be one of:
	 * 
	 * <ul>
	 * <li>nearest neighbour
	 * <li>tri-linear
	 * <li>tri-cubic
	 * </ul>
	 * 
	 * @param volume_source 	
	 * @param volume_target
	 * @param column_source
	 * @param column_target
	 * @param method
	 * @return
	 */
	public static void mapVolumeToVolumeInterp(Volume3DInt volume_source, 
											   Volume3DInt volume_target, 
											   String column_source,
											   String column_target,
											   String method,
											   ArrayList<Object> parameters){
		mapVolumeToVolumeInterp(volume_source, volume_target, column_source, column_target, method, parameters, 0, null);
	}
	
	/********************************************************************
	 * Maps {@code volume_source} to {@code volume_target}, using interpolation from all overlapping 
	 * voxels to assign the value. The {@code method} must be one of:
	 * 
	 * <ul>
	 * <li>nearest neighbour
	 * <li>tri-linear
	 * <li>tri-cubic
	 * </ul>
	 * 
	 * @param volume_source 	
	 * @param volume_target
	 * @param column_source
	 * @param column_target
	 * @param method
	 * @param progress
	 * @return
	 */
	public static void mapVolumeToVolumeInterp(Volume3DInt volume_source, 
										   Volume3DInt volume_target, 
										   String column_source,
										   String column_target,
										   String method,
										   ArrayList<Object> parameters,
										   ProgressUpdater progress){
		mapVolumeToVolumeInterp(volume_source, volume_target, column_source, column_target, method, parameters, 0, progress);
	}
	
	/********************************************************************
	 * Maps {@code volume_source} to {@code volume_target}, using interpolation from all overlapping 
	 * voxels to assign the value. The {@code method} must be one of:
	 * 
	 * <ul>
	 * <li>nearest neighbour
	 * <li>tri-linear
	 * <li>tri-cubic
	 * </ul>
	 * 
	 * @param volume_source 	
	 * @param volume_target
	 * @param column_source
	 * @param column_target
	 * @param method
	 * @param default_value
	 * @return
	 */
	public static boolean mapVolumeToVolumeInterp(final Volume3DInt volume_source, 
												  final Volume3DInt volume_target, 
												  final String column_source,
												  final String column_target,
												  final String method,
												  final ArrayList<Object> parameters,
												  final double default_value,
												  final ProgressUpdater progress){
		
		if (progress == null){
			return mapVolumeToVolumeInterpBlocking(volume_source, 
										   	  volume_target, 
										   	  column_source,
										   	  column_target,
										   	  method,
										   	  parameters,
										   	  default_value,
										   	  null);
			}
		
		return (Boolean)Worker.post(new Job(){
			@Override
			public Boolean run(){
				return mapVolumeToVolumeInterpBlocking(volume_source, 
											   	   volume_target, 
											   	   column_source,
											   	   column_target,
											   	   method,
											   	   parameters,
											   	   default_value,
											   	   progress);
				}
			});
		
	}
	
	/********************************************************************
	 * Maps {@code volume_source} to {@code volume_target}, using interpolation from all overlapping 
	 * voxels to assign the value. The {@code method} must be one of:
	 * 
	 * <ul>
	 * <li>nearest neighbour
	 * <li>tri-linear
	 * <li>tri-cubic
	 * </ul>
	 * 
	 * @param volume_source 	
	 * @param volume_target
	 * @param column_source
	 * @param column_target
	 * @param default_value
	 * @return
	 */
	protected static boolean mapVolumeToVolumeInterpBlocking(Volume3DInt volume_source, 
														   	  Volume3DInt volume_target, 
														   	  String column_source,
														   	  String column_target,
														   	  String method,
														   	  ArrayList<Object> parameters,
														   	  double default_value,
														   	  ProgressUpdater progress){
		
		if (!volume_target.hasColumn(column_target)){
			InterfaceSession.log("VolumeFunctions.mapVolumeToVolumeEV: No column '" + column_target + "' in target " +
								 "volume '" + volume_target.getFullName() + "'", 
								 LoggingType.Errors);
			return false;
			}
		
		// For each voxel in the target volume
		// find the enclosing voxel in the source volume
		// and assign its value, or the default value if no
		// enclosing voxel exists
		
		int n_target = volume_target.getVertexCount();
		Grid3D grid_target = volume_target.getGrid();
		Grid3D grid_source = volume_source.getGrid();
		GridVertexDataColumn t_column = (GridVertexDataColumn)volume_target.getVertexDataColumn(column_target);
		GridVertexDataColumn s_column = (GridVertexDataColumn)volume_source.getVertexDataColumn(column_source);
		int tr_size = grid_target.getSizeT() * grid_target.getSizeR();
		int s_size = grid_target.getSizeS();
		method = method.toLowerCase();
		
		if (progress != null){
			progress.setMinimum(0);
			progress.setMaximum(s_size);
			}
		
		TricubicSplineInterpolatingFunction interp_fcn = null;
		Matrix4f basis_tfm = null;
		
		if (method.equals("tri-cubic")){
			double[] x = getGridXs(grid_source);
			double[] y = getGridYs(grid_source);
			double[] z = getGridZs(grid_source);
			double[][][] v = s_column.getVoxelsAsDouble();
			
			basis_tfm = grid_source.getBasisTransform();
			basis_tfm.invert();
			
			if (progress != null){
				progress.setMessage("Computing interpolating function:");
				//progress.setIndeterminate(true);
				}
			TricubicSplineInterpolaterUpdater interpolator = new TricubicSplineInterpolaterUpdater();
			interp_fcn = interpolator.interpolate(x, y, z, v, progress);
			if (progress != null){
				progress.setMessage("Mapping volume to volume:");
				//progress.setIndeterminate(false);
				}
			}
		
		int j = 0,k = 0;
		for (int i = 0; i < n_target; i++){
			j++;
			Point3f cp = grid_target.getVoxelMidPoint(i);
			
			// This part depends on the method..
			
			if (method.equals("nearest neighbour")){
				// Nearest neighbour: value is same as enclosing voxel
				int[] ev = grid_source.getEnclosingVoxel(cp);
				if (ev == null){
					t_column.getValueAtVertex(i).setValue(default_value);
				}else{
					t_column.getValueAtVertex(i).setValue(s_column.getValueAtVoxel(ev[0], ev[1], ev[2]));
					}
			}else if (method.equals("tri-linear")){
				// Trilinear: get neighbouring and enclosing voxels defining 8 corners,
				// 			  and interpolate between these
				int[] vox = grid_target.getIndexAsVoxel(i);
				if (vox[0]==123 && vox[1]==140 && vox[2]==138){
					int a = 0;
					}
				int[] nbrs = getTrilinearVoxels(grid_source, cp);
				
				Vector3f cp_v = new Vector3f(cp);
				cp_v.sub(grid_source.getOrigin());
				cp_v = GeometryFunctions.transform(cp_v, grid_source.getGridBasisTransform());
				
				if (nbrs == null){
					// Target outside source
					t_column.getValueAtVertex(i).setValue(default_value);
				}else if (nbrs.length == 3){
					// Target/source voxel centers coincident
					t_column.getValueAtVertex(i).setValue(s_column.getDoubleValueAtVoxel(nbrs[0], nbrs[1], nbrs[2]));
				}else{
					// Interpolate
					Point3f c_000 = getVolumeSpaceMidPoint(grid_source, nbrs[0]);
					Point3f c_111 = getVolumeSpaceMidPoint(grid_source, nbrs[1]);
					int[] vox_0 = grid_source.getIndexAsVoxel(nbrs[0]);
					int[] vox_1 = grid_source.getIndexAsVoxel(nbrs[1]);
			
					float d_x = 0, d_y = 0, d_z = 0;
					
					// If voxels match, this is a boundary; don't interpolate in this direction
					if (vox_0[0] != vox_1[0])
						d_x = (cp_v.getX() - c_000.getX()) / (c_111.getX() - c_000.getX());
					if (vox_0[1] != vox_1[1])
						d_y = (cp_v.getY() - c_000.getY()) / (c_111.getY() - c_000.getY());
					if (vox_0[2] != vox_1[2])
						d_z = (cp_v.getZ() - c_000.getZ()) / (c_111.getZ() - c_000.getZ());
					
					float[][] c_yz = new float[2][2];
					try{
					c_yz[0][0] = (float)(s_column.getDoubleValueAtVoxel(vox_0[0], vox_0[1], vox_0[2]) * (1 - d_x) +
								   		 s_column.getDoubleValueAtVoxel(vox_1[0], vox_0[1], vox_0[2]) * d_x);
					c_yz[1][0] = (float)(s_column.getDoubleValueAtVoxel(vox_0[0], vox_1[1], vox_0[2]) * (1 - d_x) +
					   		 			 s_column.getDoubleValueAtVoxel(vox_1[0], vox_1[1], vox_0[2]) * d_x);
					c_yz[0][1] = (float)(s_column.getDoubleValueAtVoxel(vox_0[0], vox_0[1], vox_1[2]) * (1 - d_x) +
					   		 			 s_column.getDoubleValueAtVoxel(vox_1[0], vox_0[1], vox_1[2]) * d_x);
					c_yz[1][1] = (float)(s_column.getDoubleValueAtVoxel(vox_0[0], vox_1[1], vox_1[2]) * (1 - d_x) +
					   		 			 s_column.getDoubleValueAtVoxel(vox_1[0], vox_1[1], vox_1[2]) * d_x);
					}catch (Exception ex){
						int a = 0;
					}
					
					float c_0 = c_yz[0][0] * (1 - d_y) + c_yz[1][0] * d_y;
					float c_1 = c_yz[0][1] * (1 - d_y) + c_yz[1][1] * d_y;
					
					t_column.getValueAtVertex(i).setValue(c_0 * (1 - d_z) + c_1 * d_z);
					
					}
			}else if (method.equals("tri-cubic")){
				// Cubic: determine a tricubic function using Apache Math Commons
				Point3f p = grid_target.getVoxelMidPoint(i);
				if (grid_source.contains(p)){
					basis_tfm.transform(p);
					try{
						t_column.getValueAtVertex(i).setValue(interp_fcn.value(p.getX(), p.getY(), p.getZ()));
					}catch (OutOfRangeException ex){
						// Will occur around edges
						t_column.getValueAtVertex(i).setValue(default_value);
						}
				}else{
					t_column.getValueAtVertex(i).setValue(default_value);
					}
				
			}else{
				InterfaceSession.log("VolumeFunctions.mapVolumeToVolumeInterp: " +
									 "Invalid method '" + method + "'.", 
									 LoggingType.Errors);
				return false;
				}
			
			if (j == tr_size){
				if (progress != null)
					progress.update(k++);
				j = 0;
				}
			}
		
		return true;
		
		
	}
	
	/*******************************
	 * Returns the midpoint of this voxel in {@code grid}'s coordinate space.
	 * 
	 * @param grid
	 * @param voxel
	 * @return
	 */
	public static Point3f getVolumeSpaceMidPoint(Grid3D grid, int voxel){
		return getVolumeSpaceMidPoint(grid, grid.getIndexAsVoxel(voxel));
	}
	
	/*******************************
	 * Returns the midpoint of this voxel in {@code grid}'s coordinate space.
	 * 
	 * @param grid
	 * @param voxel
	 * @return
	 */
	public static Point3f getVolumeSpaceMidPoint(Grid3D grid, int[] voxel){
		float geom = grid.getGeomS();
		float dim = grid.getSizeS();
		float d_vox = geom / dim;
		float x = d_vox * voxel[0];
		geom = grid.getGeomT();
		dim = grid.getSizeT();
		d_vox = geom / dim;
		float y = d_vox * voxel[1];
		geom = grid.getGeomR();
		dim = grid.getSizeR();
		d_vox = geom / dim;
		float z = d_vox * voxel[2];
		return new Point3f(x,y,z);
		
	}
	
	/********************************************
	 * Returns the trilinear neighbourhood of {@code point}, which includes the 8 voxels surrounding
	 * and enclosing it. Returns a list of 8 absolute indices, where a negative value indicate the
	 * neighbouring voxel is outside the bounds.
	 * 
	 * <p>Voxels are arranged as:
	 * 
	 * <br>0 -> [0 0 0]
	 * <br>1 -> [1 1 1]
	 * 
	 * @param grid
	 * @param point
	 * @return
	 */
	protected static int[] getTrilinearVoxels(Grid3D grid, Point3f point){
		
		Point3f P = new Point3f(point);
		int[] ev = grid.getEnclosingVoxel(P);
		if (ev == null) return ev;
		
		// Transform point to volume coordinates
		Point3f C = grid.getVoxelMidPoint(ev);
		
		if (GeometryFunctions.isCoincident(point, C)){
			return ev;
			}
		
		Vector3f v = new Vector3f(P);
		v.sub(C);
		
		// Transform vector into volume space
		v = GeometryFunctions.transform(v, grid.getBasisTransform());
		
		// Assign corners for trilinear interpolation
		// Project v onto R-axis; if dot product is positive,  
		
		Vector3f axis = grid.getSAxis();
		float delta = v.x; //v.dot(axis);
		int cx_0, cx_1;
		if (delta < 0){
			cx_0 = ev[0] - 1;
			cx_1 = ev[0];
		}else{
			cx_0 = ev[0];
			cx_1 = ev[0] + 1;
			}
		
		// Check for boundaries
		if (cx_0 < 0) cx_0 = 0;
		if (cx_1 >= grid.getSizeS()) cx_1 = grid.getSizeS() - 1;
		
		axis = grid.getTAxis();
		delta = v.y; //dot(axis);
		int cy_0, cy_1;
		if (delta < 0){
			cy_0 = ev[1] - 1;
			cy_1 = ev[1];
		}else{
			cy_0 = ev[1];
			cy_1 = ev[1] + 1;
			}
		
		// Check for boundaries
		if (cy_0 < 0) cy_0 = 0;
		if (cy_1 >= grid.getSizeT()) cy_1 = grid.getSizeT() - 1;
		
		axis = grid.getRAxis();
		delta = v.z; //.dot(axis);
		int cz_0, cz_1;
		if (delta < 0){
			cz_0 = ev[2] - 1;
			cz_1 = ev[2];
		}else{
			cz_0 = ev[2];
			cz_1 = ev[2] + 1;
			}
		
		// Check for boundaries
		if (cz_0 < 0) cz_0 = 0;
		if (cz_1 >= grid.getSizeR()) cz_1 = grid.getSizeR() - 1;
		
		return new int[]{grid.getAbsoluteIndex(new int[]{cx_0, cy_0, cz_0}),
						 grid.getAbsoluteIndex(new int[]{cx_1, cy_1, cz_1})};
		
		
//		return new int[]{grid.getAbsoluteIndex(new int[]{cx_0, cy_0, cz_0}),
//						 grid.getAbsoluteIndex(new int[]{cx_1, cy_0, cz_0}),
//						 grid.getAbsoluteIndex(new int[]{cx_1, cy_1, cz_0}),
//						 grid.getAbsoluteIndex(new int[]{cx_1, cy_1, cz_1}),
//						 grid.getAbsoluteIndex(new int[]{cx_0, cy_1, cz_0}),
//						 grid.getAbsoluteIndex(new int[]{cx_0, cy_1, cz_1}),
//						 grid.getAbsoluteIndex(new int[]{cx_0, cy_0, cz_1}),
//						 grid.getAbsoluteIndex(new int[]{cx_1, cy_0, cz_1})};
	}
	
	
	/*****************************************
	 * Returns the indices off all first neighbours of the voxel enclosing {@code point},
	 * plus that voxel.
	 * 
	 * @param grid
	 * @param point
	 * @return
	 */
	public static int[] getNeighbouringVoxels(Grid3D grid, Point3f point){
		return getNeighbouringVoxels(grid, point, 1);
	}
	
	/*****************************************
	 * Returns the indices off all first neighbours of the voxel enclosing {@code point},
	 * plus that voxel.
	 * 
	 * @param grid
	 * @param point
	 * @return
	 */
	public static int[] getNeighbouringVoxels(Grid3D grid, Point3f point, int n){
		
		if (n < 1) return null;
		
		Point3f p = new Point3f(point);
		int[] ev = grid.getEnclosingVoxel(p);
		if (ev == null) return null;
		
		return getNeighbouringVoxels(grid, ev, n);
		
	}
	
	/*****************************************
	 * Returns the indices off all first neighbours of {@code voxel}, plus {@code voxel}.
	 * 
	 * @param grid
	 * @param point
	 * @return
	 */
	public static int[] getNeighbouringVoxels(Grid3D grid, int[] voxel, int n){
		
		int[] min_voxel = new int[]{
				Math.max(voxel[0]-n, 0),
				Math.max(voxel[1]-n, 0),
				Math.max(voxel[2]-n, 0)	
				};
		int[] max_voxel = new int[]{
			Math.min(voxel[0]+n, grid.getSizeS()-1),
			Math.min(voxel[1]+n, grid.getSizeT()-1),
			Math.min(voxel[2]+n, grid.getSizeR()-1)	
			};
		
		ArrayList<Integer> indices = new ArrayList<Integer>(9);
		indices.add(grid.getAbsoluteIndex(voxel));
		
		for (int i = min_voxel[0]; i < max_voxel[0]; i++)
			for (int j = min_voxel[1]; j < max_voxel[1]; j++)
				for (int k = min_voxel[2]; k < max_voxel[2]; k++){
					indices.add(grid.getAbsoluteIndex(i,j,k));
				}
		
		int[] result = new int[indices.size()];
		for (int i = 0; i < indices.size(); i++)
			result[i] = indices.get(i);
		
		return result;
		
	}
	
	/***************************
	 * Finds connected blobs in a thresholded volume.
	 * 
	 * @param volume		Volume to search
	 * @param column		Column to search
	 * @param min_blob		Minimum size (in voxels) at which to include a blob
	 * @param threshold		Value to threshold the volume
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean getBlobsFromVolume(final Volume3DInt volume, final String source_column, 
											 String target_column, final ProgressUpdater progress, 
											 final int min_blob, final double threshold){
	
		ArrayList<MguiNumber> blobs = null;
		
		if (progress == null){
			blobs = getBlobsFromVolumeBlocking(volume, source_column, null, min_blob, threshold);
		}else{
			// Do as a job
			progress.register();
			blobs = (ArrayList<MguiNumber>)Worker.post(new Job(){
				@Override
				public ArrayList<MguiNumber> run(){
					return getBlobsFromVolumeBlocking(volume, source_column, progress, min_blob, threshold);
					}
				});
			progress.deregister();
			}
		if (blobs == null) return false;
		
		if (!volume.hasColumn(target_column)){
			volume.addVertexData(target_column, blobs);
		}else{
			volume.setVertexData(target_column, blobs);
			}
		
		return true;
	}
	
	/***************************
	 * Finds connected blobs in a thresholded volume.
	 * 
	 * @param volume		Volume to search
	 * @param column		Column to search
	 * @param min_blob		Minimum size (in voxels) at which to include a blob
	 * @param threshold		Value to threshold the volume
	 * @return
	 */
	public static ArrayList<MguiNumber> getBlobsFromVolumeBlocking(Volume3DInt volume, String column, ProgressUpdater progress, 
																   int min_blob, double threshold){
		
		GridVertexDataColumn v_column = (GridVertexDataColumn)volume.getVertexDataColumn(column);
		Grid3D grid = volume.getGrid();
		
		if (v_column == null){
			InterfaceSession.log("VolumeFunctions.getBlobsFromVolumeBlocking: Column '" + column + "' does not exist", 
								 LoggingType.Errors);
			return null;
			}
		
		ArrayList<MguiNumber> blobs = new ArrayList<MguiNumber>();
		int n = volume.getVertexCount();
		for (int i = 0; i < n; i++){
			blobs.add(new MguiInteger(-1));
			}
		
		Queue<Integer> queue = new LinkedBlockingQueue<Integer>();
		int current_label = 1;
		ArrayList<Integer> blob_size = new ArrayList<Integer>();
		
		if (progress != null){
			progress.setMinimum(0);
			progress.setMaximum(n);
			}
		
		int itr = 0;
		for (int idx = 0; idx < n; idx++){
			// Only consider if not already processed
			int current_size = 0;
			if (blobs.get(idx).getValue() < 0){
				double value = v_column.getDoubleValueAtVertex(idx);
				if (progress != null){
					if (progress.isCancelled()){
						InterfaceSession.log("VolumeFunction.getBlobsFromVolume: Cancelled by user.", LoggingType.Warnings);
						return null;
						}
					progress.update(itr++);
					}
				if (value <= threshold){
					// Sub-threshold, set blob index to zero
					blobs.get(idx).setValue(0);
				}else{
					// Supra-threshold, set blob index to current_label
					blobs.get(idx).setValue(current_label);
					current_size++;
					queue.add(idx);
					while (!queue.isEmpty()){
						// Get neighbours and test each
						int qidx = queue.poll();
						int[] nbrs = grid.getNeighbours(qidx);
						
						for (int nn = 0; nn < nbrs.length; nn++){
							int nidx = nbrs[nn];
							// Skip if already set
							if (nidx != qidx && ((int)blobs.get(nidx).getValue()) < 0){ 
								value = v_column.getDoubleValueAtVertex(nidx);
								if (value <= threshold){
									// Sub-threshold, set blob index to zero
									blobs.get(nidx).setValue(0);
								}else{
									// Supra-threshold, set blob index to current_label
									blobs.get(nidx).setValue(current_label);
									current_size++;
									queue.add(nidx);
									}
								if (progress != null){
									if (progress.isCancelled()){
										InterfaceSession.log("VolumeFunction.getBlobsFromVolume: Cancelled by user.", LoggingType.Warnings);
										return null;
										}
									progress.update(itr++);
									}
								}
							}
						}
					current_label++;
					blob_size.add(current_size);
					}
				}
			}
		
		// Remove too-small blobs
		HashMap<Integer,Integer> relabeler = new HashMap<Integer,Integer>();
		int offset = 0;
		for (int i = 0; i < blob_size.size(); i++){
			if (i == 3427){
				i+=0;
				}
			if (blob_size.get(i) < min_blob){
				relabeler.put(i+1, 0);
				offset++;
			}else{
				
				relabeler.put(i+1, i+1-offset);
				}
			}
		
		// Update values
		for (int i = 0; i < blobs.size(); i++){
			int value = (int)blobs.get(i).getValue();
			if (value > 0){
				blobs.get(i).setValue(relabeler.get(value));
				}
			}
		
		return blobs;
	}
	
	private static int[] getNeighbouringVoxels_old(Grid3D grid, Point3f point){
		
		Point3f p = new Point3f(point);
		int[] ev = grid.getEnclosingVoxel(p);
		if (ev == null) return null;
		
		Point3f mp = grid.getVoxelMidPoint(ev[0], ev[1], ev[2]);
		Vector3f v = new Vector3f(mp);
		v.sub(p);
		
		ArrayList<Integer> nbrs = new ArrayList<Integer>(3);
		nbrs.add(grid.getAbsoluteIndex(ev));
		// Get neighbour voxels closest to p (determined by sign of scalar projection)
		Vector3f axis = grid.getSAxis();
		double dist = axis.dot(v);
		int s_idx=-1, t_idx=-1, r_idx=-1;
		if (dist < 0){
			if (ev[0] > 0)
				s_idx = ev[0]-1;
		}else{
			if (ev[0] < grid.getSizeS()-1)
				s_idx = ev[0]+1;
			}
		axis = grid.getTAxis();
		dist = axis.dot(v);
		if (dist < 0){
			if (ev[1] > 0)
				t_idx = ev[1]-1;
		}else{
			if (ev[1] < grid.getSizeT()-1)
				t_idx = ev[1]+1;
			}
		axis = grid.getRAxis();
		dist = axis.dot(v);
		if (dist < 0){
			if (ev[2] > 0)
				r_idx = ev[2]-1;
		}else{
			if (ev[2] < grid.getSizeR()-1)
				r_idx = ev[2]+1;
			}
		
		// Add up to 8 neighbours (less if this is an edge voxel)
		if (s_idx > -1){
			nbrs.add(grid.getAbsoluteIndex(s_idx,ev[1],ev[2]));
			if (t_idx > -1){
				nbrs.add(grid.getAbsoluteIndex(s_idx,t_idx,ev[2]));
				if (r_idx > -1) nbrs.add(grid.getAbsoluteIndex(s_idx,t_idx,r_idx));
				}
			if (r_idx > -1) nbrs.add(grid.getAbsoluteIndex(s_idx,ev[1],r_idx));
			}
		if (t_idx > -1){
			nbrs.add(grid.getAbsoluteIndex(ev[0],t_idx,ev[2]));
			if (r_idx > -1) nbrs.add(grid.getAbsoluteIndex(ev[0],t_idx,r_idx));
			}
		if (r_idx > -1) nbrs.add(grid.getAbsoluteIndex(ev[0],ev[1],r_idx));
		
		int[] array = new int[nbrs.size()];
		for (int i = 0; i < nbrs.size(); i++)
			array[i] = nbrs.get(i);
		return array;
		
	}
	
	private static double[] getGridXs(Grid3D grid){
		
		int size = grid.getSizeS();
		double geom = (double)grid.getGeomS();
		double unit = (double)geom / (double)size;
		double offset = unit/2.0;
		
		double[] vals = new double[size];
		for (int i = 0; i < size; i++){
			vals[i] = (double)i * unit + offset;
			}
	
		return vals;
	}
	
	private static double[] getGridYs(Grid3D grid){
		
		int size = grid.getSizeT();
		double geom = (double)grid.getGeomT();
		double unit = (double)geom / (double)size;
		double offset = unit/2.0;
		
		double[] vals = new double[size];
		for (int i = 0; i < size; i++){
			vals[i] = (double)i * unit + offset;
			}
	
		return vals;
	}

	private static double[] getGridZs(Grid3D grid){
		
		int size = grid.getSizeR();
		double geom = (double)grid.getGeomR();
		double unit = (double)geom / (double)size;
		double offset = unit/2.0;
		
		double[] vals = new double[size];
		for (int i = 0; i < size; i++){
			vals[i] = (double)i * unit + offset;
			}
	
		return vals;
	}
	
	/************************************
	 * Returns a global maximum for a given volume or within a given mask, or mulitple maxima from a set of masks, specified by
	 * {@code mask_column}. Will optionally add labels based on {@code label_column}.
	 * 
	 * @param volume
	 * @param source_column
	 * @param mask_column
	 * @param max_point
	 * @param label_column
	 * @return
	 */
	public static PointSet3DInt findMaximaBlocking(Volume3DInt volume, String source_column, String mask_column, String label_column){
		
		HashMap<Integer,Double> maximum = new HashMap<Integer,Double>();
		HashMap<Integer,Integer> indices = new HashMap<Integer,Integer>();
		ArrayList<MguiNumber> values = volume.getVertexData(source_column);
		ArrayList<MguiNumber> mask = null;
		if (mask_column != null){
			mask = volume.getVertexData(mask_column);
			}
		int mask_idx = 0;
		
		for (int i = 0; i < values.size(); i++){
			mask_idx = 1;
			double value = values.get(i).getValue();
			if (mask != null)
				mask_idx = (int)mask.get(i).getValue();
			if (mask_idx > 0){
				Double max = maximum.get(mask_idx);
				if (max == null){
					maximum.put(mask_idx, value);
					indices.put(mask_idx, i);
				}else{
					if (value > max){
						maximum.put(mask_idx, value);
						indices.put(mask_idx, i);
						}
					}
				}
			}
		
		ArrayList<Integer> keys = new ArrayList<Integer>(maximum.keySet());
		Collections.sort(keys);
		
		PointSet3D points = new PointSet3D();
		VertexDataColumn v_column = null;
		NameMap source_nmap = null;
		NameMap target_nmap = new NameMap("Labels",false);
		if (label_column != null){
			v_column = volume.getVertexDataColumn(label_column);
			source_nmap = v_column.getNameMap();
			}
		
		// Add a point for each key
		ArrayList<MguiNumber> mask_data = new ArrayList<MguiNumber>(keys.size());
		ArrayList<MguiNumber> peaks = new ArrayList<MguiNumber>(keys.size());
		for (int i = 0; i < keys.size(); i++){
			int key = keys.get(i);
			int idx = indices.get(key);
			points.addVertex(volume.getVertex(indices.get(key)));
			int a = 0;
			if (v_column != null){
				double lbl = v_column.getValueAtVertex(idx).getValue();
				String label = "" + lbl;
				if (source_nmap != null){
					label = source_nmap.get((int)lbl);
					if (label == null) label = "" + (int)lbl;
					}
				target_nmap.add(key, label);
				}
			mask_data.add(new MguiInteger(key));
			peaks.add(new MguiDouble(maximum.get(key)));
			}
		
		PointSet3DInt point_set = new PointSet3DInt(points);
		
		point_set.addVertexData("mask_index", mask_data);
		point_set.addVertexData(source_column, peaks);
		
		if (label_column != null){
			point_set.addVertexData(label_column, mask_data, target_nmap);
			}
		
		return point_set;
		
	}
	
	/*********************************************
	 * Finds the centroids for all ROIs (identified as voxels with the same value). Optionally maps values of
	 * the source column to the point set.
	 * 
	 * @param volume Volume containing ROIs
	 * @param source_column Column identifying the ROIs; should be of type INTEGER, otherwise values will be
	 * 						rounded
	 * @param value_range The range of values outside which to exclude ROIs
	 * @param min_size Minimal size of an ROI (in voxels) at which to generate a centroid
	 * @param map_values If {@code true}, maps the value of the ROI index to the point set
	 * @return
	 */
	public static PointSet3DInt getRoiCentroidsBlocking(Volume3DInt volume, String source_column, 
														int[] value_range, int min_size, boolean map_values,
														String label_column,
														ProgressUpdater progress){
		
		GridVertexDataColumn v_column = (GridVertexDataColumn)volume.getVertexDataColumn(source_column);
		if (v_column == null){
			InterfaceSession.log("VolumeFunctions.getRoiCentroids: source column doesn't exist (" + source_column + ")", 
								LoggingType.Errors);
			return null;
			}
		
		HashMap<Integer,Integer> frequencies = new HashMap<Integer,Integer>();
		HashMap<Integer,Point3f> centroids = new HashMap<Integer,Point3f>();
		ArrayList<MguiNumber> values = v_column.getData();
		Grid3D grid = volume.getGrid();
		Point3f mp = new Point3f();
		if (min_size < 1) min_size = 1;
		
		if (progress != null){
			progress.setMinimum(0);
			progress.setMaximum(values.size());
			}
		
		for (int i = 0; i < values.size(); i++){
			int value = (int)Math.round(values.get(i).getValue());
			if (value >= value_range[0] && value <= value_range[1]){
				mp.set(grid.getVoxelMidPoint(i));
				if (!frequencies.containsKey(value)){
					frequencies.put(value, 1);
					centroids.put(value, new Point3f(mp));
				}else{
					frequencies.put(value, frequencies.get(value) + 1);
					centroids.get(value).add(mp);
					}
				}
			if (progress != null){
				if (progress.isCancelled()){
					InterfaceSession.log("VolumeFunction.getRoiCentroids: Cancelled by user.", LoggingType.Warnings);
					return null;
					}
				progress.update(i);
				}
			}
		
		ArrayList<Integer> keys = new ArrayList<Integer>(frequencies.keySet());
		ArrayList<MguiNumber> mapped = new ArrayList<MguiNumber>(keys.size());
		Collections.sort(keys);
		PointSet3D pset = new PointSet3D();
		
		for (int i = 0; i < keys.size(); i++){
			int key = keys.get(i);
			int freq = frequencies.get(key);
			if (freq >= min_size){
				Point3f centroid = centroids.get(key);
				centroid.scale(1f/(float)freq);
				pset.addVertex(centroid);
				mapped.add(new MguiInteger(key));
				}
			}
		
		PointSet3DInt points = new PointSet3DInt(pset);
		if (map_values){
			points.addVertexData(source_column, mapped);
			}
		
		if (label_column != null){
			GridVertexDataColumn l_column = (GridVertexDataColumn)volume.getVertexDataColumn(label_column);
			if (l_column == null) return points;
			
			NameMap label_nmap = l_column.getNameMap();
			NameMap target_nmap = new NameMap("Labels",false);
			ArrayList<MguiNumber> label_data = new ArrayList<MguiNumber>(pset.getSize());
			
			for (int i = 0; i < pset.getSize(); i++){
				Point3f point = pset.getVertex(i);
				int[] voxel = volume.getGrid().getEnclosingVoxel(point);
				if (voxel != null){
					double lbl = l_column.getDoubleValueAtVoxel(voxel[0], voxel[1], voxel[2]);
					String label = "" + lbl;
					if (label_nmap != null){
						label = label_nmap.get((int)lbl);
						if (label == null) label = "" + (int)lbl;
						}
					target_nmap.add(i+1, label);
					label_data.add(new MguiInteger(i+1));
					}
				}
			
			points.addVertexData(label_column, label_data, target_nmap);
			}
		
		return points;
		
	}
	
	
}