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

package mgui.io.domestic.shapes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import mgui.geometry.Box3D;
import mgui.geometry.Grid3D;
import mgui.image.util.WindowedColourModel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.volume.VolumeMetadata;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.variables.MatrixFileLoader;
import foxtrot.Job;
import foxtrot.Worker;

/************************************************************
 * Loader for reading from a volume (3D grid) file into a {@link Volume3DInt} object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class VolumeFileLoader extends FileLoader {
	
	//setting alpha from intensity
    protected boolean setAlpha = true;
    protected float minAlpha = 0.0f;
    protected float maxAlpha = 1.0f;
    
    //flip coordinates
    public boolean flipX, flipY, flipZ;
    public double dataMax = 255;
    public double dataMin = 0;
    
    public VolumeFileLoader(){
    	
    }
    
    public int getVolCount(){return 1;}
	
    public Volume3DInt getVolume3D(){
    	try{
    		return getVolume3D(0);
    	}catch (ShapeIOException e){
    		e.printStackTrace();
    		}
    	return null;
	}
    
    /************************************************************
     * Returns a set of metadata describing the current volume file.
     * 
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    public abstract VolumeMetadata getVolumeMetadata() throws IOException, FileNotFoundException;
    
    @Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException {
    	Volume3DInt volume = getVolume3D((VolumeInputOptions)options, 0, progress_bar);
		if (volume == null) return null;
		volume.setFileLoader(getIOType());
		File file = getFile();
		volume.setUrlReference(file.toURI().toURL());
		return volume;
	}
    
    /*******************************
     * Load the volume at the current file, with the given options.
     * 
     * @param options
     * @param progress_bar
     * @return
     */
    public Volume3DInt loadVolume(VolumeInputOptions options, ProgressUpdater progress_bar){
    	try{
    		return (Volume3DInt)loadObject(progress_bar, options);
    	}catch (IOException e){
    		return null;
    		}
    }
    
    /*******************************
     * Load the volume at the current file, with default options.
     * 
     * @return
     */
    public Volume3DInt loadVolume(){
    	return loadVolume(null);
    }
    
    /*******************************
     * Load the volume at the current file.
     * 
     * @param progress_bar
     * @return
     */
    public Volume3DInt loadVolume(ProgressUpdater progress_bar){
    	try{
    		return (Volume3DInt)loadObject(progress_bar, new VolumeInputOptions());
    	}catch (IOException e){
    		return null;
    		}
    }
    
    /*******************************
     * Load the volume at time point t.
     * 
     * @param t
     * @return
     */
    public Volume3DInt getVolume3D(int t) throws ShapeIOException{
    	return getVolume3D(null, t, null);
    }
    	
    
    /******************************
     * Creates and returns a grid based on the parameters in <code>options</code>.
     * 
     * @param options
     * @return
     */
    public Volume3DInt getVolume3D(VolumeInputOptions options){
    	return getVolume3D(options, 0, null);
    }
    
    /******************************
     * Creates and returns a grid based on the metadata.
     * 
     * @param options
     * @return
     */
    public Volume3DInt getVolume3D(int v, ProgressUpdater progress_bar){
    	return getVolume3D(null, v, progress_bar);
    }
    
    /******************************
     * Creates and returns a grid based on the metadata.
     * 
     * @param options
     * @return
     */
    public Volume3DInt getVolume3D(VolumeInputOptions options, int v, ProgressUpdater progress_bar){
    	
    	if (options != null) return getVolume3DCustom(options, v, progress_bar);
    	
    	try{
    		VolumeMetadata metadata = this.getVolumeMetadata();
    		if (metadata == null) return null;
    		Box3D box = metadata.getBounds();
    		int[] dim = metadata.getDataDims();
    		
    		Grid3D grid = new Grid3D(dim[0], dim[1], dim[2], 1, box);
    		Volume3DInt volume = new Volume3DInt(grid);
    		options = getDefaultOptions();
			setVolume3D(volume, options.input_column, v, options, progress_bar);
			return volume;
    		
    	}catch (Exception ex){
    		InterfaceSession.handleException(ex);
    		//ex.printStackTrace();
    	}
    	
    	return null;
    }
    
    /*******************************
     * Returns a default set of options for loading a volume with this loader.
     * 
     * @return
     */
    public VolumeInputOptions getDefaultOptions(){
    	return new VolumeInputOptions();
    }
    
    /******************************
     * Creates and returns a grid based on the parameters in <code>options</code>. Use this method to
     * override the metadata if necessary.
     * 
     * @param options
     * @return
     */
    protected Volume3DInt getVolume3DCustom(VolumeInputOptions options, int v, ProgressUpdater progress_bar){
    	
    	try{
	    	VolumeMetadata metadata = this.getVolumeMetadata();
	    	
	    	Box3D box = metadata.getBounds();
	    	
//	    	int[] dim = metadata.getDataDims();
//	    	int xDim = dim[0];
//			int yDim = dim[1];
//			int zDim = dim[2];
//			int tDim = 1;
//			if (dim.length > 3)
//				tDim = dim[3];
//			
//			if (options.set_dims){
//				xDim = options.dim_x;
//				yDim = options.dim_y;
//				zDim = options.dim_z;
//			}
//			
//			//TODO throw exception here
//			if (v > tDim) return null;
//			
//			//add 1 to dims to account for 0.5 of start and end voxels
//			float[] geom_dim = metadata.getGeomDims();
//			
//			float xSpace = geom_dim[0];
//			float ySpace = geom_dim[1];
//			float zSpace = geom_dim[2];
//			
//			if (options.set_geom){
//				xSpace = options.geom_x;
//				ySpace = options.geom_y;
//				zSpace = options.geom_z;
//				}
//			
//			Point3f origin = metadata.getOrigin();
//			
//			if (options.set_origin){
//				origin.x = options.origin_x;
//				origin.y = options.origin_y;
//				origin.z = options.origin_z;
//			}
//			
//			Vector3f[] axes = metadata.getAxes();
//			axes[0].normalize();
//			axes[0].scale(xSpace);
//			axes[1].normalize();
//			axes[1].scale(ySpace);
//			axes[2].normalize();
//			axes[2].scale(zSpace);
			
//			Box3D box = new Box3D(origin,
//								  axes[0],
//								  axes[1],
//								  axes[2]);
			
			
			WindowedColourModel model = new WindowedColourModel(options.colour_map,
																options.scale, options.intercept,
																options.window_mid, options.window_width,
																options.has_alpha, options.transfer_type);
			
			model.setAlphaMin(options.alpha_min);
			model.setAlphaMax(options.alpha_max);
			
			int[] dim = metadata.getDataDims();
	    	int xDim = dim[0];
			int yDim = dim[1];
			int zDim = dim[2];
			Grid3D grid = new Grid3D(xDim, yDim, zDim, box);
			Volume3DInt volume = new Volume3DInt(grid);
		
			setVolume3D(volume, v, options, progress_bar);
			
			model.setWindowMid(options.window_mid);
			model.setWindowWidth(options.window_width);
			
			//volume.setColourMap(options.colour_map);
			volume.hasAlpha(options.has_alpha);
			
			return volume;
			
		}catch (Exception e){
			InterfaceSession.handleException(e);
			//e.printStackTrace();
			}
		return null;
    }
    
    public boolean setVolume3D(Volume3DInt volume, VolumeInputOptions options) throws ShapeIOException{
		return setVolume3D(volume, 0, options);
	}
    
    public boolean setVolume3D(Volume3DInt volume, int v, VolumeInputOptions options) throws ShapeIOException{
    	String column = volume.getCurrentColumn();
    	if (column == null) return false;
		return setVolume3D(volume, column, v, options, null);
	}
    
    public boolean setVolume3D(Volume3DInt volume, int v, VolumeInputOptions options, ProgressUpdater progress) throws ShapeIOException{
    	String column = volume.getCurrentColumn();
    	if (column == null)
    		column = options.input_column;
    	if (column == null) return false;
		return setVolume3D(volume, column, v, options, progress);
	}
    
    public boolean setVolume3D(Volume3DInt volume, String column, int v, VolumeInputOptions options, ProgressUpdater progress) throws ShapeIOException{
		if (progress == null || !SwingUtilities.isEventDispatchThread())
			return setVolume3DBlocking(volume, column, v, options, progress);
    	return setVolume3DWorker(volume, column, v, options, progress);
	}
    
    protected boolean setVolume3DWorker(final Volume3DInt volume, final String column, final int v, final VolumeInputOptions options, final ProgressUpdater progress){
		
    	boolean success = ((Boolean)Worker.post(new Job(){
			
			@Override
			public Boolean run(){
				
				try{
					return setVolume3DBlocking(volume, column, v, options, progress);
				}catch (Exception e){
					InterfaceSession.handleException(e);
					return false;
					}
				}
		
		})).booleanValue();
		
		return success;
	}
    
    protected abstract boolean setVolume3DBlocking(Volume3DInt volume, String column, int v, VolumeInputOptions options, ProgressUpdater progress) throws ShapeIOException;
    
	public void setAlpha(boolean set, float min, float max){
    	setAlpha = set;
    	minAlpha = Math.min(Math.max(0.0f, min), 1.0f);
    	maxAlpha = Math.min(Math.max(0.0f, max), 1.0f);
    }
	public void setAlpha(boolean set){
		setAlpha(set, 0, 1);
	}
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar){
		
		if (!(options instanceof VolumeInputOptions)) return false;
		VolumeInputOptions v_options = (VolumeInputOptions)options;
		if (v_options.shapeSet == null || v_options.files == null) return false;
		
		boolean success = true;
		for (int i = 0; i < v_options.files.length; i++){
			setFile(v_options.files[i]);
			flipX = v_options.flip_x;
			flipY = v_options.flip_y;
			flipZ = v_options.flip_z;
			Volume3DInt volume = getVolume3D(v_options);
			if (volume == null) success = false;
			volume.setName(v_options.names[i]);
			v_options.shapeSet.addShape(volume);
			}
		
		return success;
		
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = VolumeFileLoader.class.getResource("/mgui/resources/icons/volume_3d_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/volume_3d_20.png");
		return null;
	}
	
	@Override
	public ArrayList<Class<?>> getSupportedObjects(){
		ArrayList<Class<?>> objs = new ArrayList<Class<?>>();
		objs.add(Volume3DInt.class);
		return objs;
	}
	
}