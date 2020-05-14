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

package mgui.interfaces.shapes.volume;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import org.jogamp.java3d.ImageComponent;
import org.jogamp.java3d.ImageComponent3D;
import org.jogamp.java3d.ImageComponent3D.Updater;
import org.jogamp.java3d.Texture;
import org.jogamp.java3d.Texture3D;

import mgui.geometry.Grid3D;
import mgui.geometry.volume.VolumeFunctions;
import mgui.image.util.WindowedColourModel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.shapes.Volume3DInt;


/*****************************
 * Class to create and return Texture3D and TexCoordGeneration objects from a given
 * set of grid data.
 * 
 * @author Andrew Reid
 *
 */

public class Volume3DTexture implements Updater {

	//constants
	public static final int TYPE_INTENSITY_GREY = 0;
	public static final int TYPE_INTENSITY_GREY_ALPHA = 1;
	public static final int TYPE_INTENSITY_CMAP = 2;
	public static final int TYPE_INTENSITY_CMAP_ALPHA = 3;
	public static final int TYPE_RGB = 4;
	public static final int TYPE_RGB_ALPHA = 5;
	
	protected Texture3D texture;
	ImageComponent3D iComp;
	protected int sDim, tDim, rDim;
	protected int colorSpace = ColorSpace.CS_sRGB;
	//public int type = TYPE_RGB;
	protected ColourMap colourMap;
	protected boolean byRef = true;
	protected BufferedImage[] images;
	
	
	public Volume3DTexture(Volume3DInt volume){
		//set texture from grid
		setFromVolume(volume);
	}
	
	public Volume3DTexture(Volume3DInt volume, ColourMap cmap){
		colourMap = cmap;
		setFromVolume(volume);
	}
	
	public Volume3DTexture(int s, int t, int r){
		sDim = s;
		tDim = t;
		rDim = r;
	}
	
	public BufferedImage[] getByRefImages(){
		return images;
	}
	
	/*************************************
	 * Set this volume from the specified grid.
	 *
	 * @param grid
	 */
	public boolean setFromVolume(Volume3DInt volume){
		return setFromVolume(volume, false);
	}
	
	/*************************************
	 * Set this volume from the specified grid.
	 *
	 * @param grid
	 * @param update - Indicates that this is a by-reference update call
	 */
	public boolean setFromVolume(Volume3DInt volume, boolean update){
		
		//volume.deactivateClips();
		
		Grid3D grid = volume.getGrid();
		//byRef = volume.isByRef();
		sDim = grid.getSizeS();
		tDim = grid.getSizeT();
		rDim = grid.getSizeR();
		
		BufferedImage[] _images = VolumeFunctions.getMaskedImages(volume);
		
		if (_images == null){
			InterfaceSession.log("Volume3DTexture: No current data column", LoggingType.Errors);
			//volume.reactivateClips();
			return false;
			}
		iComp = new ImageComponent3D(getFormat(), _images, byRef, false);
		iComp.setCapability(ImageComponent.ALLOW_IMAGE_READ);
		iComp.setCapability(ImageComponent.ALLOW_IMAGE_WRITE);
		iComp.setCapability(ImageComponent.ALLOW_FORMAT_READ);
			
		//set texture from iComp
		texture = null;
		setTexture();
		//volume.reactivateClips();
		return true;
	}
	
	/************************************
	 * Java3D clips bug requires all clips to be deactivated prior to updating nodes
	 * 
	 */
	void dealWithClips(){
		
	}
	
	/*************************************
	 * Set this volume from the specified grid.
	 *
	 * @param grid
	 */
	public boolean updateFromVolume(Volume3DInt volume){
		return updateFromVolume(volume, false);
	}
	
	/*************************************
	 * Updates this volume from the specified grid.
	 *
	 * @param grid
	 * @param update - Indicates that this is a by-reference update call
	 */
	public boolean updateFromVolume(Volume3DInt volume, boolean update){
		
		if (iComp == null) return setFromVolume(volume, update);
		
		BufferedImage[] _images = VolumeFunctions.getMaskedImages(volume);
		
		if (byRef && update){
			// This should be a call from an updateData method
			for (int i = 0; i < images.length; i++)
				images[i].getRaster().setDataElements(0, 0, _images[i].getData());
			return true;
			}
		
		iComp.set(_images);
		return true;
	}
			
			
	
	/***********************************************
	 * Produces a stack of R-planar images from {@code volume}, based upon the current data column and
	 * its associated colour map.
	 * 
	 * @param volume
	 * @return
	 */
//	public static BufferedImage[] getMaskedImages(Volume3DInt volume){
//		 
//		GridVertexDataColumn column = (GridVertexDataColumn)volume.getCurrentDataColumn();
//		if (column == null) return null;
//		
//		Grid3D grid = volume.getGrid();
//		int r_size = grid.getSizeR();
//		int s_size = grid.getSizeS();
//		int t_size = grid.getSizeT();
//		
//		WindowedColourModel colour_model = VolumeFunctions.getColourModel(column.getDataTransferType(), volume.getColourMap(), volume.hasAlpha());
//		
//		boolean[][][] mask = new boolean[s_size][t_size][r_size];
//		for (int i = 0; i < s_size; i++)
//			for (int j = 0; j < t_size; j++)
//				for (int k = 0; k < r_size; k++)
//					mask[i][j][k]=true;
//		
//		// Apply masks if necessary
//		if (volume.getApplyMasks()){
//			mask = volume.getEffectiveMask();
//			}
//		
//		BufferedImage[] images = new BufferedImage[r_size];
//		for (int k = 0; k < r_size; k++){
//			WritableRaster raster = colour_model.createCompatibleWritableRaster(s_size, t_size);
//			BufferedImage image = new BufferedImage(colour_model, raster, false, null);
//			raster = image.getRaster();
//			images[k] = image;
//			for (int i = 0; i < s_size; i++)
//				for (int j = 0; j < t_size; j++){
//					if (mask[i][j][k]){
//						raster.setPixel(i, t_size - j - 1, new double[]{0});
//					}else{
//						raster.setPixel(i, t_size - j - 1, new double[]{column.getDoubleValueAtVoxel(i, j, k)});
//						}
//					}
//			}
//		
//		return images;
//		
//	}
	
	
	protected void setTexture(){
		if (iComp == null) return;
		
		if (texture == null)
			resetTexture();
		
		texture.setImage(0, iComp);
		
	}
	
	protected void resetTexture(){
		int format = Texture.RGBA;
	
		texture = new Texture3D(Texture.BASE_LEVEL,
				                format, 
				                sDim, 
				                tDim, 
				                rDim);
		texture.setEnable(true);
		texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
		texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
		texture.setBoundaryModeS(Texture.CLAMP);
		texture.setBoundaryModeT(Texture.CLAMP);
		texture.setBoundaryModeR(Texture.CLAMP);
	}

	protected int getFormat(){
		return ImageComponent.FORMAT_RGBA;
		
	}
	
	protected void resetImageComponent(){
		iComp = new ImageComponent3D(getFormat(), sDim, tDim, rDim, byRef, true);
	}
	
	protected ColorSpace getColourSpace(){
		return ColorSpace.getInstance(ColorSpace.CS_sRGB);
		
	}
	
	public void loadImage(int r0, int r1, URL image){
		loadImage(r0, r1, image, false, 0f, 0);
	}
	
	
	/*************************************
	 * Loads an image file at the given url into the texture for planes r0 through r1. 
	 * If image does not match the dimensions s and t, it will be scaled to fit. If, 
	 * for example: image.width < sDim, pixels will be inserted to fill the difference. If 
	 * image.width > sDim, pixels will be removed to fit the image to this texture's
	 * dimensions.
	 *  
	 * @param r0 Start r-plane into which to load the image
	 * @param r1 End r-plane into which to load the image
	 * @param image URL of the image file to load
	 * @param hasAlpha Determines whether to add/set an alpha channel to an RGB image.
	 * @param cutoff The value at which to cut alpha to zero, from 0 to 1 where 0 is completely
	 * 				 transparent, and 1 is completely opaque.
	 * @param exp Determines the degree of the equation with which alpha proceeds from the
	 *            cutoff to zero. i.e., determines smoothness of cutoff. A zero or negative 
	 *            value of exp specifies no smoothing.
	 */
	public void loadImage(int r0, int r1, URL image, boolean setAlpha, double cutoff, double exp){
		
		if (r0 > r1 || r0 > rDim || r1 > rDim) return;
		
		//TextureLoader tl = new TextureLoader(url, null);
		BufferedImage urlImage;
		try{
		urlImage = ImageIO.read(image);
		}
		catch (IOException e){
			e.printStackTrace();
			return;
		}
		
		int width = urlImage.getWidth();
        int height = urlImage.getHeight();
        if (width != sDim || height != tDim) return;
        
        if (setAlpha)
        	urlImage = mgui.util.Colours.getRGBtoRGBA(urlImage, cutoff, exp);
		
        //get image array and load into data array
        if (iComp == null)
        	resetImageComponent();
        
        for (int k = r0; k < r1; k++) 
            iComp.set(k, urlImage);
        
        //set texture
        setTexture();
       
	}
	
	// ******************* UPDATER STUFF ************************
	
	boolean[][][] current_mask;
	GridVertexDataColumn current_column;
	UpdateTextureType current_update_type = UpdateTextureType.All;
	boolean current_is_composite = false;
	Volume3DInt composite_volume;
	
	public enum UpdateTextureType{
		Values,
		ColourMap,
		All;
	}
	
	/**************************************************
	 * Sets the current mask for this texture; use {@code null} for no mask.
	 * 
	 * @param mask
	 */
	public void setCurrentMask(boolean[][][] mask){
		this.current_mask = mask;
	}
	
	/**************************************************
	 * Sets the current data column for this texture.
	 * 
	 * @param mask
	 */
	public void setCurrentColumn(GridVertexDataColumn column){
		this.current_column = column;
	}
	
	/**************************************************
	 * Specifies what type of update will be performed by {@linkplain updateData}.
	 * 
	 * @param type
	 */
	public void setCurrentUpdateType(UpdateTextureType type){
		current_update_type = type;
	}
	
	public void setCurrentComposite(Volume3DInt volume){
		composite_volume = volume;
	}
	
	@Override
	public void updateData(ImageComponent3D imageComponent, 
						   int r_index, int x, int y, 
						   int s_size, int t_size) {
		
		if (current_column == null) 
			return;
		BufferedImage image = imageComponent.getImage(r_index);
		WritableRaster raster = image.getRaster();
		
		if (composite_volume != null){
			// Update composite image with new data and colour map
			BufferedImage _image = VolumeFunctions.getMaskedCompositeRSliceImage(composite_volume, r_index, current_mask);
			WritableRaster _raster = _image.getRaster();
			WritableRaster alpha_raster = image.getAlphaRaster();
			WritableRaster _alpha_raster = _image.getAlphaRaster();
			raster.setDataElements(x, y, s_size, t_size, _raster.getDataElements(x, y, s_size, t_size, null));
			alpha_raster.setDataElements(x, y, s_size, t_size, _alpha_raster.getDataElements(x, y, s_size, t_size, null));
			return;
			}
		
		WindowedColourModel current_colour_model = current_column.getColourModel();
		if (current_update_type != UpdateTextureType.Values && current_colour_model != null){
			WindowedColourModel model = (WindowedColourModel)image.getColorModel();
			model.setFromColourModel(current_colour_model);
			}
		
		if (current_update_type != UpdateTextureType.ColourMap){
			for (int i = 0; i < s_size; i++)
				for (int j = 0; j < t_size; j++){
					if (current_mask != null && current_mask[i][j][r_index]){
						raster.setPixel(i, t_size - j - 1, new double[]{0});
					}else{
						raster.setPixel(i, t_size - j - 1, new double[]{current_column.getDoubleValueAtVoxel(i, j, r_index)});
						}
					}
			}
		
	}
	
}