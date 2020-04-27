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

import java.awt.image.DataBuffer;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import mgui.geometry.Grid3D;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.volume.Volume3DTexture;
import mgui.interfaces.shapes.volume.VolumeMetadata;
import mgui.io.InterfaceIO;
import mgui.io.InterfaceIOOptions;

/****************************************************
 * Options specifying a {@linkplain Volume3DInt} object. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class VolumeInputOptions extends ShapeInputOptions {

	public ShapeSet3DInt shapeSet;
	public File[] files;
	public String[] names;
	public String volume_loader = null;
	
	public String name = "No name";
	
	public ColourMap colour_map;
	public boolean set_origin;
	public float origin_x, origin_y, origin_z;
	public boolean set_dims;
	public int dim_x, dim_y, dim_z;
	public boolean set_geom;
	public boolean set_type;
	public float geom_x, geom_y, geom_z;
	public boolean flip_x, flip_y, flip_z;
	public Vector3f axis_x, axis_y, axis_z;
	
	public boolean has_alpha;
	public double scale = 1;
	public double intercept;
	public double window_width = 1;
	public double window_mid = 0.5;
	public double alpha_min = 0;
	public double alpha_max = 1;
	
	public boolean allow_dim_change = true;
	public boolean allow_geom_change = true;
	
	public int transfer_type = DataBuffer.TYPE_DOUBLE;
	public int texture_type = Volume3DTexture.TYPE_INTENSITY_CMAP_ALPHA;
	
	public String input_column = "default";
	public boolean load_as_composite = false;
	
	public InterfaceIOType input_type; // = FORMAT_ANALYZE;
	
	int n = 0;
	
	public VolumeInputOptions(){
		
	}
	
	public VolumeInputOptions(VolumeMetadata metadata){
		Point3f origin = metadata.getOrigin();
		origin_x = origin.x;
		origin_y = origin.y;
		origin_z = origin.z;
		set_origin = true;
		int[] dims = metadata.getDataDims();
		dim_x = dims[0];
		dim_y = dims[1];
		dim_z = dims[2];
		set_dims = true;
		float[] gdims = metadata.getGeomDims();
		geom_x = gdims[0];
		geom_y = gdims[1];
		geom_z = gdims[2];
		set_geom = true;
	}
	
	public void setFiles(ArrayList<File> f, ArrayList<String> n){
		files = new File[f.size()];
		names = new String[f.size()];
		for (int i = 0; i < f.size(); i++){
			files[i] = f.get(i);
			names[i] = n.get(i);
			}
	}
	
	public void setInputType(InterfaceIO loader){
		
		try{
			//ArrayList<InterfaceIOType> types = InterfaceEnvironment.getIOTypesForClass(Class.forName(loader));
			this.input_type = InterfaceEnvironment.getIOTypeForInstance(loader);
//			if (types.size() > 0)
//				this.input_type = types.get(0);
		}catch (Exception e){
			//InterfaceSession.log("VolumeInputOptions: no IO type found for '" + loader + "'.");
			}
		
	}
	
	public JFileChooser getFileChooser(){
		return getFileChooser(null);
	}
	
	public JFileChooser getFileChooser(File f){
		JFileChooser fc = null;
		if (f != null)
			fc = new JFileChooser(f);
		else
			fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		fc.setDialogTitle("Select volume files to input");
		return fc;
	}
	
	public File[] getFiles() {
		return files;
	}

	public VolumeFileLoader getLoader(){
		if (input_type == null) return null;
		return (VolumeFileLoader)input_type.getIOInstance();
	}
	
	public void setFiles(File[] files) {
		this.files = files;
	}
	
	public void setFrom(VolumeInputOptions options){
		//format = options.format;
		files = options.files;
		this.shapeSet = options.shapeSet;
		this.names = options.names;
		this.has_alpha = options.has_alpha;
		this.alpha_max = options.alpha_max;
		this.alpha_min = options.alpha_min;
		this.flip_x = options.flip_x;
		this.flip_y = options.flip_y;
		this.flip_z = options.flip_z;
		this.axis_x = options.axis_x;
		this.axis_y = options.axis_y;
		this.axis_z = options.axis_z;
		this.transfer_type = options.transfer_type;
		this.set_dims = options.set_dims;
		this.dim_x = options.dim_x;
		this.dim_y = options.dim_y;
		this.dim_z = options.dim_z;
		this.set_geom = options.set_geom;
		this.geom_x = options.geom_x;
		this.geom_y = options.geom_y;
		this.geom_z = options.geom_z;
		this.set_origin = options.set_origin;
		this.origin_x = options.origin_x;
		this.origin_y = options.origin_y;
		this.origin_z = options.origin_z;
		this.colour_map = options.colour_map;
		this.input_type = options.input_type;
		this.set_type = options.set_type;
		this.allow_dim_change = options.allow_dim_change;
		this.allow_geom_change = options.allow_geom_change;
		
	}

	@Override
	public void setObject(InterfaceObject obj) throws ClassCastException{
		InterfaceSession.log(this.getClass().getCanonicalName() + ": setObject not implemented.", LoggingType.Warnings);
	}
	
	@Override
	public void setFromComplementaryOptions(InterfaceIOOptions options) {
		if (!(options instanceof VolumeOutputOptions)) return;
		VolumeOutputOptions o_options = (VolumeOutputOptions)options;
		
		this.transfer_type = o_options.datatype;
		this.intercept = o_options.intercept;
		this.flip_x = o_options.flipX;
		this.flip_y = o_options.flipY;
		this.flip_z = o_options.flipZ;
		this.scale = o_options.slope;
		
	}
	
}