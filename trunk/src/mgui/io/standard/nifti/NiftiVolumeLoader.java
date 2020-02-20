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

package mgui.io.standard.nifti;

import java.awt.Transparency;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import mgui.datasources.DataType;
import mgui.datasources.DataTypes;
import mgui.geometry.Box3D;
import mgui.geometry.Grid3D;
import mgui.image.util.WindowedColourModel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.volume.GridVertexDataColumn;
import mgui.interfaces.shapes.volume.VolumeMetadata;
import mgui.io.domestic.shapes.ShapeIOException;
import mgui.io.domestic.shapes.VolumeFileLoader;
import mgui.io.domestic.shapes.VolumeInputOptions;
import mgui.numbers.MguiNumber;
import mgui.numbers.NumberFunctions;


/***************************
 * File loader to read Analyze 7.5 format volume files. Has methods to read from
 * both header (.hdr) and volume (.img) files.
 * 
 * See http://www.mayo.edu/PDF/ANALYZE75.pdf for details on this format.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class NiftiVolumeLoader extends VolumeFileLoader {

	public File header;
	public File volume;
	public File transform;
	
	public boolean is_nifti;
	
	public NiftiVolumeLoader(){
		
	}
	
	public NiftiVolumeLoader(File input){
		try{
			init(input, false);
		}catch (IOException e){
			e.printStackTrace();
			}
	}
	
	public NiftiVolumeLoader(File input, boolean trans) {
		try{
			init(input, trans);
		}catch (IOException e){
			InterfaceSession.log("NiftiVolumeLoader: Error setting file " + input.getAbsolutePath() + "'", 
					 LoggingType.Errors);
			}
	}
	
	@Override
	public void setFile(File input){
		try{
			init(input, false);
		}catch (IOException e){
			InterfaceSession.log("NiftiVolumeLoader: Error setting file " + input.getAbsolutePath() + "'", 
								 LoggingType.Errors);
			}
	}
	
	@Override
	public File getFile(){
		return this.volume;
	}
	
	protected void init(File input, boolean trans) throws java.io.IOException{
		String fileName = input.getAbsolutePath();
		if (fileName.endsWith(".nii") || fileName.endsWith(".nii.gz")){
			header = new File(input.getAbsolutePath()); 
			volume = new File(input.getAbsolutePath()); 
			is_nifti = true;
			return;
			}
		String volName = "", transName = "";
		if (fileName.length() > 4){
			volName = fileName.substring(0, fileName.length() - 4) + ".img";
			volume = new File(volName);
			if (!volume.exists())
				volName = fileName.substring(0, fileName.length() - 4) + ".img.gz";
			transName = fileName.substring(0, fileName.length() - 4) + ".mat";
			}
		//header = new File(fileName);
		header = input;
		volume = new File(volName);
		
		if (!header.exists())
		     throw new java.io.IOException("Error reading header file: '" + fileName + "'");
		if (!volume.exists())
		     throw new java.io.IOException("Error reading volume file: '" + volName + "'");
		if (trans){
			transform = new File(transName);
			if (!transform.exists())
				throw new java.io.IOException("Error reading transform file: '" + transName + "'");
			}
	}
	
	public Box3D getBoundingBox(){
		
		NiftiMetadata header = this.readHeader();
		return header.getBounds();
		
	}
	
	public NiftiMetadata readHeader(){
		if (header == null || !header.exists()){
			InterfaceSession.log("NiftiVolumeLoader.readHeader: No header file set.", LoggingType.Errors);
			return null;
			}
		NiftiMetadata h = new NiftiMetadata();
		h.readFromFile(header);
		return h;
	}
	
	@Override
	public VolumeMetadata getVolumeMetadata() throws IOException, FileNotFoundException{
		return readHeader();
	}
	
	//TODO update this
	public void writeHeader(NiftiMetadata h){
		
		try{
			h.writeHeader();
		}catch (Exception e){
			InterfaceSession.handleException(e, LoggingType.Errors);
			//e.printStackTrace();
			}
		
	}
	
	public boolean hasVol(int v){
		return v <= getVolCount();
	}
	
	@Override
	public int getVolCount(){
		NiftiMetadata h = readHeader();
		int[] dim = h.getDataDims();
		if (dim.length > 3)
			return dim[3];
		return 1;
		//return h.image.dim[4];
	}
	
	ColorModel getComponentColourModel(ColorModel model, boolean alpha){
		
		int a = model.getNumComponents();
		if (alpha && !model.hasAlpha())
			a++;
		int[] bits = new int[a];
		for (int i = 0; i < a; i++)
			bits[i] = 8;
		
		return new ComponentColorModel(model.getColorSpace(), 
									   bits, alpha, false, 
									   Transparency.TRANSLUCENT, 
									   model.getTransferType());
	}
	
	@Override
	protected boolean setVolume3DBlocking(Volume3DInt volume3d, String column, int v, VolumeInputOptions options, ProgressUpdater progress) throws ShapeIOException{
		
		try{
			Nifti1Dataset dataset = new Nifti1Dataset(volume.getAbsolutePath());
			dataset.readHeader();
			
//			NiftiMetadata metadata = new NiftiMetadata(volume);
			double[][][] data = dataset.readDoubleVol((short)0);
			
			flipX = options.flip_x;
			flipY = options.flip_y;
			flipZ = options.flip_z;
			
			Grid3D grid3d = volume3d.getGrid();
			
			double min = Double.MAX_VALUE;
			double max = -Double.MAX_VALUE;
			int x = Math.min(grid3d.getSizeS(), data[0][0].length);
			int y = Math.min(grid3d.getSizeT(), data[0].length);
			int z = Math.min(grid3d.getSizeR(), data.length);
			
			if (progress != null){
				progress.setMinimum(0);
				progress.setMaximum(z);
				progress.update(0);
				}
			
			DataType data_type = null;
			switch(options.transfer_type){
				case DataBuffer.TYPE_USHORT:
					data_type = DataTypes.getType(DataTypes.USHORT);
					break;
				case DataBuffer.TYPE_SHORT:
					data_type = DataTypes.getType(DataTypes.SHORT);
					break;
				case DataBuffer.TYPE_INT:
					data_type = DataTypes.getType(DataTypes.INTEGER);
					break;
				case DataBuffer.TYPE_FLOAT:
					data_type = DataTypes.getType(DataTypes.FLOAT);
					break;
				case DataBuffer.TYPE_DOUBLE:
				default:
					data_type = DataTypes.getType(DataTypes.DOUBLE);
					break;
				}
			
			ArrayList<MguiNumber> values = new ArrayList<MguiNumber>();
			
			for (int k = 0; k < z; k++){
				int k0 = k;
				if (flipZ) k0 = z - k - 1;
				
				if (progress != null)
					progress.update(k);
			
			    for (int j = 0; j < y; j++)
			    	for (int i = 0; i < x; i++){
			    		int i0 = i;
			    		if (flipX) i0 = x - i - 1;
			    		int j0 = j;
			    		if (flipY) j0 = y - j - 1;
			    		
			    		double d = data[k0][j0][i0];
			    		if (d > 0)
			    			d += 0;
			    		if (Double.isNaN(data[k0][j0][i0])){
			    			d = 0;	//debug breakpoint
			    			}
			    		
			    		if (Double.isInfinite(data[k0][j0][i0])){
			    			d = 0;	//debug breakpoint
			    			}
			    		
			    		if (NumberFunctions.compare(dataset.scl_slope, 0, 8) != 0){
				    		d = d * dataset.scl_slope + dataset.scl_inter;
				    		}
			    		
			    		values.add(NumberFunctions.getInstance(data_type, d));
			    		
					  	min = Math.min(min, d);
					  	max = Math.max(max, d);
					  	}
			    if (progress != null)
					progress.update(k);
				}
			
			  volume3d.addVertexData(column, values, options.colour_map);
			  volume3d.hasAlpha(options.has_alpha);
			  volume3d.setCurrentColumn(column, false);
			
			  GridVertexDataColumn v_column = (GridVertexDataColumn)volume3d.getVertexDataColumn(column);
			  if (v_column == null){
				  return false;
			  	  }
			  WindowedColourModel cm = v_column.getColourModel();
			  
			  //intercept and scale are handled by Nifti1Dataset
			  cm.setIntercept(min);
			  if (max > min)
				  cm.setScale(1.0 / (max - min));
			  else
				  cm.setScale(1.0);
			  cm.setWindowMid(0.5);
			  cm.setWindowWidth(1.0);
			  v_column.setDataMin(min, false);
			  v_column.setDataMax(max, false);
			  v_column.setColourLimits(min, max, false);
			  
			  InterfaceSession.log("Scale: " + cm.getScale() + " Intercept: " + cm.getIntercept(), LoggingType.Debug);
			  InterfaceSession.log("Min: " + min + " Max: " + max, LoggingType.Debug);
			  
			return true;
			
		}catch (IOException e){
			InterfaceSession.handleException(e);
			return false;
			}
		
	}

	@Override
	public InterfaceIOType getWriterComplement(){
		return (new NiftiVolumeWriter()).getIOType();
	}
		
	
	/*************************
	 * Newest version uses LONI reader
	 * See http://www.loni.ucla.edu/Software/
	 *
	public boolean setGrid3D_bak(Grid3D grid, int v, InterfaceProgressBar progress) throws ShapeIOException{
		
		if (volume == null) 
			throw new ShapeIOException("Input analyze file not set.");
		
		if (progress != null){
			progress.setMessage("Loading '" + volume.getName() + "': ");
			progress.progressBar.setMinimum(0);
			progress.progressBar.setMaximum(grid.x_size * grid.y_size * grid.z_size);
			progress.progressBar.setStringPainted(true);
			
			if (!setGrid3DWorker(grid, v, progress))
				throw new ShapeIOException("Loading '" + volume.getName() + " failed..");
			
			return true;
			}
			
		//try{
			//AnalyzeImageReader reader = new AnalyzeImageReader(new AnalyzeImageReaderSpi());
			//reader.setInput(volume);
			//Grid3D grid3d = (Grid3D)grid;
			
			double min = Double.MAX_VALUE;
			double max = -Double.MAX_VALUE;
								
			//grid3d.resetArray();
			int x_size = grid.getSizeX();
			int y_size = grid.getSizeY();
			int z_size = grid.getSizeZ();
			
			for (int k = 0; k < z_size; k++){
				int k0 = k;
				if (flipZ) k0 = z_size - k - 1;
				//copy images to grid
				BufferedImage image = null; //reader.read(k);
				int val = (k * x_size * y_size);
				
			    int x = Math.min(x_size, image.getWidth());
			    int y = Math.min(y_size, image.getHeight());
			 
			    for (int i = 0; i < x; i++)
			    	for (int j = 0; j < y; j++){
			    		int i0 = i;
			    		if (flipX) i0 = x - i - 1;
			    		int j0 = j;
			    		if (flipY) j0 = y - j - 1;
					  
			    		double[] p = new double[image.getColorModel().getNumComponents()];
			    		p = image.getRaster().getPixel(i, j, new double[p.length]);
					
					  	double pixel = p[0];
					  	if (Double.isNaN(pixel)) pixel = 0;
					
					  	min = Math.min(min, pixel);
					  	max = Math.max(max, pixel);
					
					  	grid3d.getImage(k0).getRaster().setPixel(i0, j0, new double[]{pixel});
					
					  	}
				}
				//progress.update(grid.xSize * grid.ySize * grid.ySize);
				
				if (grid3d.getColourModel() instanceof WindowedColourModel){
					  WindowedColourModel cm = (WindowedColourModel)grid3d.getColourModel();
					  cm.scale = (cm.data_size - 1) / (double)(max - min);
					  cm.intercept = min;
					  grid3d.setDataMin(min);
					  grid3d.setDataMax(max);
					  InterfaceSession.log("Scale: " + cm.scale + " Intercept: " + cm.intercept);
					  InterfaceSession.log("Min: " + min + " Max: " + max);
					  }
				
			//}catch (IOException e){
			//	return false;
			//	}
			
			return true;
		
	}
	*/
	
	
}