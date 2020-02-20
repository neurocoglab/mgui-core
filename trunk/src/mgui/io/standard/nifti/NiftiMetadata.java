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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteOrder;
import java.util.ArrayList;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import mgui.datasources.DataType;
import mgui.geometry.Box3D;
import mgui.geometry.Grid3D;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.volume.GridVertexDataColumn;
import mgui.interfaces.shapes.volume.VolumeMetadata;
import mgui.io.standard.nifti.util.NiftiFunctions;
import mgui.numbers.MguiNumber;
import mgui.numbers.NumberFunctions;


/********************************
 * Data structure to store header information for Analyze 7.5 format volume files. 
 * 
 * <p><u>Note for setting values</u>
 * 
 * <p>To ensure proper functionality when setting values from a grid, the following order should be observed:
 * 
 * <ol>
 * <li>Data dimensions
 * <li>Geometric dimensions
 * <li>Origin
 * </ol>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class NiftiMetadata extends Nifti1Dataset implements Serializable, VolumeMetadata {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3417560084592704597L;
	
	public ByteOrder byteOrder = ByteOrder.nativeOrder();
	public boolean compress = true;
	protected Vector3f axis_s, axis_t, axis_r;
	
	public NiftiMetadata(){
		super();
	}
	
	public NiftiMetadata(File file){
		super();
		readFromFile(file);
	}
	
	public void readFromFile(File input){
		
		setHeaderFilename(input.getAbsolutePath());
		
		try{
			readHeader();
		
		}catch (Exception e){
			InterfaceSession.handleException(e, LoggingType.Errors);
			}
		
	}
	
	@Override
	public ArrayList<MguiNumber> readVolume(int t, DataType data_type, ProgressUpdater progress) throws IOException{
		
		// TODO: read directly from file and avoid this memory-intensive step
		double[][][] d_vals = readDoubleVol((short)t);
		
		ArrayList<MguiNumber> values = new ArrayList<MguiNumber>(dim[1] * dim[2] * dim[3]);
		if (progress != null){
			progress.setMinimum(0);
			progress.setMaximum(dim[1]);
			progress.update(0);
			}
		
		for (int i = 0; i < dim[1]; i++){
			for (int j = 0; j < dim[2]; j++)
				for (int k = 0; k < dim[3]; k++){
					
					values.add(NumberFunctions.getInstance(data_type, d_vals[i][j][k]));
				}
			if (progress != null)
				progress.update(i);
			}
		
		return null;
	}
	
	public void writeToFile(File output){
		
		setHeaderFilename(output.getAbsolutePath());
		
		try{
			writeHeader();
		}catch (Exception e){
			InterfaceSession.handleException(e);
			//e.printStackTrace();
			}
	}
	
	@Override
	public void writeHeader() throws IOException, FileNotFoundException {
		this.big_endian = byteOrder == ByteOrder.BIG_ENDIAN;
		
		bitpix = getSize(datatype);
		
//		float[] geom = this.getGeomDims();
//		int[] dims = this.getDataDims();
		
//		for (int i=0; i<3; i++)
//			pixdim[i+1] = geom[i] / (float)dims[i];

		qform_code = 0; // Not using this strange transform
		sform_code = 1; // Simple affine transform, thanks
		this.qoffset = new float[]{0,0,0};
		
		super.writeHeader();
	}
	
	public void setFromMetadata(VolumeMetadata metadata){
		
		this.setOrigin(metadata.getOrigin());
		this.setDataType(metadata.getDataType());
		this.setDataDims(metadata.getDataDims());
		this.setGeomDims(metadata.getGeomDims());
		this.setAxes(metadata.getAxes());
		
	}
	
	private short getSize(short datatype){
		switch(datatype){
			case NIFTI_TYPE_UINT8: return 8;
			case NIFTI_TYPE_INT16: return 16; 
			case NIFTI_TYPE_INT32: return 32;
			case NIFTI_TYPE_FLOAT32: return 32;
			case NIFTI_TYPE_FLOAT64: return 64;
			case NIFTI_TYPE_RGB24: return 24;
			case NIFTI_TYPE_INT8: return 8;
			case NIFTI_TYPE_UINT16: return 16;
			case NIFTI_TYPE_UINT32: return 32;
			case NIFTI_TYPE_INT64: return 64;
			case NIFTI_TYPE_UINT64: return 64;
			case NIFTI_TYPE_FLOAT128: return 128;
			}
		return -1;
	}
	
	public int getChannelSize(){
		return bitpix / 8;
	}

	public int getDataType(){
		return NiftiFunctions.getDataType(datatype);
	}
	
	public boolean isNifti(){
		return ds_is_nii;
		//return header.magic[0] == 'n' && header.magic[2] == '1';
	}
	
	@Override
	public void setFromVolume(Volume3DInt volume){
		setFromVolume(volume, volume.getCurrentColumn());
	}
	
	@Override
	public void setFromVolume(Volume3DInt volume, String column){
		
		this.sform_code = 1;
		this.qform_code = 0;
		
		Grid3D grid = volume.getGrid();
		GridVertexDataColumn v_column = (GridVertexDataColumn)volume.getVertexDataColumn(column);
		this.setDataDims(new int[]{grid.getSizeS(), grid.getSizeT(), grid.getSizeR(), grid.getSizeV()});
		this.setGeomDims(new float[]{grid.getGeomS(), grid.getGeomT(), grid.getGeomR()});
		this.setAxes(new Vector3f[]{grid.getSAxis(), grid.getTAxis(), grid.getRAxis()});
		this.setOrigin(grid.getOrigin());
		this.setDataType(getNiftiDataType(v_column.getDataTransferType()));
		float[] geom_dims = this.getGeomDims();
		int[] data_dims = this.getDataDims();
		Vector3f[] axes = this.getAxes();
		
		for (int i = 0; i < 3; i++){
			float factor = geom_dims[i] / (float)data_dims[i];
			Vector3f axis = axes[i];
			srow_x[i] = axis.getX() * factor;
			srow_y[i] = axis.getY() * factor;
			srow_z[i] = axis.getZ() * factor;
			}
		
		//WindowedColourModel c_model = v_column.getColourModel();
		this.scl_slope = 1f; //(float)c_model.getScale();
		this.scl_inter = 0f; // (float)c_model.getIntercept();
		descrip = new StringBuffer("ModelGUI v." + InterfaceEnvironment.getVersion() + " (" + InterfaceEnvironment.getNow("dd/mm/yy") +
								   "): " + volume.getName());
	}
	
	@Override
	public Vector3f[] getAxes() {
			if (axis_s == null){
				// What's up with this...
			if (this.qform_code > 0){
				// Rotation matrix from q_form
				double b = this.quatern[0];
				double c = this.quatern[1];
				double d = this.quatern[2];
				
				double a = 1.0 - (b*b+c*c+d*d);
				if (a < 1E-7){
					a = 1.0 / Math.sqrt(b*b+c*c+d*d);
					b *= a;
					c *= a;
					d *= a;
					a = 0;
				}else{
					a = Math.sqrt(a);
					}
				Matrix3d M = new Matrix3d();
				M.set(new double[]{a*a+b*b-c*c-d*d, 2.0*b*c-2.0*a*d, 2.0*b*d+2.0*a*c,
								  2.0*b*c+2.0*a*d, a*a+c*c-b*b-d*d, 2.0*c*d-2.0*a*b,
								  2.0*b*d-2.0*a*c, 2.0*c*d+2.0*a*b, a*a+d*d-c*c-b*b
								  });
				// No idea...
				if (pixdim[0] < 0){
					M.setM02(-M.getM02());
					M.setM12(-M.getM12());
					M.setM22(-M.getM22());
					}
				// Rotate standard basis
				Vector3d axis_s = new Vector3d(1,0,0);
				Vector3d axis_t = new Vector3d(0,1,0);
				Vector3d axis_r = new Vector3d(0,0,1);
				M.transform(axis_s);
				M.transform(axis_t);
				M.transform(axis_r);
				this.axis_s = new Vector3f(axis_s); 
				this.axis_t = new Vector3f(axis_t); 
				this.axis_r = new Vector3f(axis_r); 
			}else{
				axis_s = new Vector3f(srow_x[0],srow_x[1],srow_x[2]);
				axis_t = new Vector3f(srow_y[0],srow_y[1],srow_y[2]);
				axis_r = new Vector3f(srow_z[0],srow_z[1],srow_z[2]);
				}
			}
		return new Vector3f[]{axis_s, axis_t, axis_r};
	}

	@Override
	public void setAxes(Vector3f[] axes) {
		axis_s = axes[0];
		axis_t = axes[1];
		axis_r = axes[2];
		
//		float xDim = axis_s.length();
//		float yDim = axis_t.length();
//		float zDim = axis_r.length();
		
//		pixdim[1] = (float)((double)xDim / (double)dim[1]);
//		pixdim[2] = (float)((double)yDim / (double)dim[2]);
//		pixdim[3] = (float)((double)zDim / (double)dim[3]);
		
		axis_s.normalize();
		axis_t.normalize();
		axis_r.normalize();
		
	}

	
	@Override
	public int getVoxelDim() {
		return TDIM;
	}

	@Override
	public void setVoxelDim(int t) {
		if (dim != null) dim[4] = (short)t;
		TDIM = (short)t;
	}
	
	@Override
	public int[] getDataDims() {
		int[] dims = new int[4];
		for (int i = 0; i < 4; i++)
			dims[i] = dim[i + 1];
		return dims;
	}

	@Override
	public float[] getGeomDims() {
	
//		if (this.sform_code > 0){
//			Vector3f v = new Vector3f(srow_x[0],srow_x[1],srow_x[2]);
//			float xSpace = Math.abs(v.length());
//			v.set(srow_y[0],srow_y[1],srow_y[2]);
//			float ySpace = Math.abs(v.length());
//			v.set(srow_z[0],srow_z[1],srow_z[2]);
//			float zSpace = Math.abs(v.length());
//			return new float[]{xSpace, ySpace, zSpace};
//			}
//		
		float xDim = dim[1];
		float yDim = dim[2];
		float zDim = dim[3];
		float xSpace = Math.abs(pixdim[1] * xDim);
		float ySpace = Math.abs(pixdim[2] * yDim);
		float zSpace = Math.abs(pixdim[3] * zDim);
		
		return new float[]{xSpace, ySpace, zSpace};
	}

	@Override
	public Point3f getOrigin() {
		
		if (this.qform_code > 0)
			return new Point3f(qoffset[0],qoffset[1],qoffset[2]);
		if (this.sform_code > 0)
			return new Point3f(srow_x[3],srow_y[3],srow_z[3]);
		else
			return new Point3f(0,0,0);
		
	}
	
	@Override
	public Box3D getBounds(){
		
		Box3D box = new Box3D();
		
		//base point
		Point3f origin = this.getOrigin();
		float[] geom_dims = this.getGeomDims();
		int[] data_dims = this.getDataDims();
		
		float s_size = geom_dims[0] / (float)data_dims[0];
		float t_size = geom_dims[1] / (float)data_dims[1];
		float r_size = geom_dims[2] / (float)data_dims[2];
		
		Vector3f[] axes = this.getAxes();
		Vector3f v_s = axes[0];
		v_s.normalize();
		v_s.scale(geom_dims[0]); // + s_size );
		box.setSAxis(new Vector3f(v_s));
		Vector3f v_t = axes[1];
		
		v_t.normalize();
		v_t.scale(geom_dims[1]); // + t_size );
		box.setTAxis(new Vector3f(v_t));
		Vector3f v_r = axes[2];
		
		v_r.normalize();
		v_r.scale(geom_dims[2]); // + r_size);
		box.setRAxis(new Vector3f(v_r));
		
		v_s.normalize();
		v_s.scale(s_size * 0.5f);
		origin.sub(v_s);
		v_t.normalize();
		v_t.scale(t_size * 0.5f);
		origin.sub(v_t);
		v_r.normalize();
		v_r.scale(r_size * 0.5f);
		origin.sub(v_r);
		box.setBasePt(origin);
		
		return new Grid3D(data_dims[0],
						  data_dims[1],
						  data_dims[2],
			 		      box);
		
	}
	
	protected boolean validateBounds(Box3D box){
		
		if (box.getBasePt() == null || 
				box.getSAxis() == null || 
				box.getTAxis() == null ||
				box.getRAxis() == null) return false;
		
		if (box.getSAxis().length() == 0 ||
				box.getTAxis().length() == 0 ||
				box.getRAxis().length() == 0)
			return false;
		
		float[] coords = box.getCoords();
		for (int i = 0; i < coords.length; i++)
			if (Float.isNaN(coords[i]) || Float.isInfinite(coords[i])) return false;
		
		return true;
		
	}

	@Override
	public void setDataDims(int[] dims) {
		int a = dims.length;
		short t = 1;
		if (a == 4) t = (short)dims[3];
			setDims((short)a, (short)dims[0], (short)dims[1], (short)dims[2], t, (short)1, (short)1, (short)1);
	}

	@Override
	public void setDataType(int type) {
		this.setDatatype((short)type);
		this.data_type_string = new StringBuffer(getDataTypeString(type));
	}

	protected String getDataTypeString(int type){
		
		switch (type){
			case Nifti1Dataset.NIFTI_TYPE_UINT8:
				return "NIFTI_TYPE_UINT8";
			case Nifti1Dataset.NIFTI_TYPE_INT16:
				return "NIFTI_TYPE_INT16";
			case Nifti1Dataset.NIFTI_TYPE_UINT16:
				return "NIFTI_TYPE_UINT16";
			case Nifti1Dataset.NIFTI_TYPE_INT32:
				return "NIFTI_TYPE_INT32";
			case Nifti1Dataset.NIFTI_TYPE_FLOAT32:
				return "NIFTI_TYPE_FLOAT32";
			case Nifti1Dataset.NIFTI_TYPE_FLOAT64:
				return "NIFTI_TYPE_FLOAT64";
			default:
				return "?";
		}
	}
	
	@Override
	public void setGeomDims(float[] dims) {
		
		float xDim = dim[1];
		float yDim = dim[2];
		float zDim = dim[3];
		
		pixdim[1] = (float)((double)dims[0] / (double)xDim);
		pixdim[2] = (float)((double)dims[1] / (double)yDim);
		pixdim[3] = (float)((double)dims[2] / (double)zDim);
		
		axis_s = new Vector3f(xDim,0,0);
		axis_t = new Vector3f(0,yDim,0);
		axis_r = new Vector3f(0,0,zDim);

	}

	@Override
	public void setOrigin(Point3f origin) {
		
		float offset_x = 0.5f * pixdim[1];
		float offset_y = 0.5f * pixdim[2];
		float offset_z = 0.5f * pixdim[3];
		
		if (this.qform_code > 0){
			qoffset[0] = -origin.x / pixdim[1];
			qoffset[0] += offset_x;
			qoffset[1] = -origin.y / pixdim[2];
			qoffset[1] += offset_y;
			qoffset[2] = -origin.z / pixdim[3];
			qoffset[2] += offset_z;
			return;
			}
		
		this.srow_x[3] = origin.x; // + offset_x;
		this.srow_y[3] = origin.y; // + offset_y;
		this.srow_z[3] = origin.z; // + offset_z;
		
	}
	
	//TODO data_history
	
	private short getNiftiDataType(int transferType){
		
		switch (transferType){
		
			case DataBuffer.TYPE_DOUBLE:
				return Nifti1Dataset.NIFTI_TYPE_FLOAT64;
			case DataBuffer.TYPE_FLOAT:
				return Nifti1Dataset.NIFTI_TYPE_FLOAT32;
			case DataBuffer.TYPE_INT:
				return Nifti1Dataset.NIFTI_TYPE_INT32;
			case DataBuffer.TYPE_SHORT:
				return Nifti1Dataset.NIFTI_TYPE_INT16;
			case DataBuffer.TYPE_BYTE:
				return Nifti1Dataset.NIFTI_TYPE_INT8;
			default:
				return Nifti1Dataset.NIFTI_TYPE_INT16;
		
			}
		
	}
	
	
}