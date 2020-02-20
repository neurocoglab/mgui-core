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

import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import mgui.geometry.Grid3D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.volume.VolumeMetadata;
import mgui.io.domestic.shapes.VolumeFileWriter;
import mgui.io.domestic.shapes.VolumeOutputOptions;
import mgui.numbers.MguiNumber;

/*********************************************
 * Writer for Nifti-format volumes. Built upon {@link NiftiDataset}, which in turn extends the 
 * {@link Nifti1Dataset} class of the niftijlib library.
 * 
 * <p>See: <a href="http://www.nitrc.org/frs/?group_id=26&release_id=168">NITRC page</a>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class NiftiVolumeWriter extends VolumeFileWriter {

	protected NiftiMetadata metadata;
	
	public NiftiVolumeWriter(){
		
	}
	
	public NiftiVolumeWriter(File file){
		setFile(file);
	}
	
	@Override
	public InterfaceIOType getLoaderComplement(){
		return (new NiftiVolumeLoader()).getIOType();
	}
	
	@Override
	public void setFile(File file){
		
		//file must have NiFTI extension (otherwise defaults to .nii)
		String fname = file.getAbsolutePath();
		if (!fname.endsWith(".nii") && !fname.endsWith(".nii.gz") &&
				!fname.endsWith(".hdr") && !fname.endsWith(".hdr.gz")) 
			fname = fname.concat(".nii");
		dataFile = new File(fname);
		
	}
	
	public void setVolumeMetadata(VolumeMetadata metadata){
		
		if (this.metadata == null){
			this.metadata = new NiftiMetadata();
			}
		
		this.metadata.setFromMetadata(metadata);
		
	}
	
	@Override
	public boolean writeVolume(Volume3DInt volume, VolumeOutputOptions options) throws IOException{
		
		if (dataFile == null) return false;
		
		NiftiMetadata dataset = new NiftiMetadata();
		
		dataset.setFromVolume(volume);
		dataset.compress = options.compress;
		short nifti_type = Nifti1Dataset.NIFTI_TYPE_INT32;
		
		switch (options.datatype){

			case DataBuffer.TYPE_BYTE:
				nifti_type = Nifti1Dataset.DT_BINARY;
				break;
			case DataBuffer.TYPE_USHORT:
				nifti_type = Nifti1Dataset.NIFTI_TYPE_UINT8;
				break;
			case DataBuffer.TYPE_SHORT:
				nifti_type = Nifti1Dataset.NIFTI_TYPE_INT16;
				break;
			case DataBuffer.TYPE_INT:
				nifti_type = Nifti1Dataset.NIFTI_TYPE_INT32;
				break;
			case DataBuffer.TYPE_FLOAT:
				nifti_type = Nifti1Dataset.NIFTI_TYPE_FLOAT32;
				break;
			case DataBuffer.TYPE_DOUBLE:
			default:
				nifti_type = Nifti1Dataset.NIFTI_TYPE_FLOAT64;
				break;
			}
		
		String column = options.use_column;
		if (column == null) column = volume.getCurrentColumn();
		if (column == null){
			InterfaceSession.log("NiftiVolumeWriter: No column found for volume '" + volume.getName() + "'.", 
								 LoggingType.Errors);
			return false;
			}
				
		dataset.setDatatype(nifti_type);
		return writeVolume(volume, column, dataset);
		
	}
	
	/******************************************************
	 * Writes {@code volume} to the current file, using the current column and
	 * {@code NiftiMetadata}.
	 * 
	 * @param volume
	 * @param dataset
	 * @return
	 * @throws IOException
	 */
	public boolean writeVolume(Volume3DInt volume, NiftiMetadata dataset) throws IOException{
		String column = volume.getCurrentColumn();
		if (column == null){
			InterfaceSession.log("NiftiVolumeWriter: No current column found for volume '" + volume.getName() + "'.", 
					 LoggingType.Errors);
			return false;
			}
		return writeVolume(volume, column, dataset);
	}
		
	
	/******************************************************
	 * Writes {@code volume} to the current file, using the specified column and
	 * {@code NiftiMetadata}.
	 * 
	 * @param volume
	 * @param column
	 * @param dataset
	 * @return
	 * @throws IOException
	 */
	public boolean writeVolume(Volume3DInt volume, String column, NiftiMetadata dataset) throws IOException{
		
		String fn = dataFile.getAbsolutePath();
		if (dataset.compress && !fn.endsWith(".gz"))
			fn = fn + ".gz";
		if (!dataset.compress && fn.endsWith(".gz"))
			fn = fn.substring(0,fn.lastIndexOf(".gz"));
		
		dataset.setHeaderFilename(fn);
		dataset.setDataFilename(fn);
		
		dataset.db_name = new StringBuffer(volume.getName());
		
		// TODO: implement time dimension
		int v_size = 1; //volume.getSizeV();
		
		//write header
		dataset.writeHeader();
		
		//write each subsequent time point
		for (short v = 0; v < v_size; v++){
			double[][][] data = getDoubleArray(volume, column);
			dataset.writeVol(data, v);
			}
		
		return true;
		
	}
	
	public boolean setFromVolume(Volume3DInt volume){
		return false;
	}
	
	protected double[][][] getDoubleArray(Volume3DInt volume, String column){
		
		Grid3D grid = volume.getGrid();
		int s_size = grid.getSizeS();
		int t_size = grid.getSizeT();
		int r_size = grid.getSizeR();
		
		double[][][] data = new double[r_size][t_size][s_size];
		ArrayList<MguiNumber> v_data = volume.getVertexData(column);
		
		for (int i = 0; i < s_size; i++)
			for (int j = 0; j < t_size; j++)
				for (int k = 0; k < r_size; k++){
					if (i==51 && j==58 && k==48)
						i+=0;
					data[k][j][i] = v_data.get(grid.getAbsoluteIndex(i, j, k)).getValue();
					}
		
		return data;
	}
	
//	/********************************************
//	 * Writes a volume to file using parameters specified in <code>options</code>. 
//	 * 
//	 * @param volume
//	 * @param options
//	 * @return
//	 * @throws IOException
//	 */
//	@Override
//	public boolean writeVolume(VolumeOutputOptions options) throws IOException{
//		
//		if (dataFile == null) return false;
//		
//		NiftiMetadata dataset = new NiftiMetadata();
//		dataset.setHeaderFilename(dataFile.getAbsolutePath());
//		dataset.setDataFilename(dataFile.getAbsolutePath());
//		
//		Volume3DInt volume = options.volume;
//		Grid3D grid = volume.getGrid();
//		
//		dataset.setFromGrid(grid);
//		dataset.setDatatype(options.datatype);		//set data type from options; everything else set from grid
//	
//		int v_size = grid.getSizeV();
//		
//		//write header
//		dataset.writeHeader();
//		
//		//write each subsequent time point
//		for (short v = 0; v < v_size; v++){
//			double[][][] data = grid.getDoubleArray(v, options.flipX, options.flipY, options.flipZ, options.apply_masks);
//			dataset.writeVol(data, v);
//			}
//		
//		return true;
//	}
	
}