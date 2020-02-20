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

import javax.swing.JFileChooser;

import mgui.datasources.DataTypes;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.InterfaceIOOptions;
import mgui.io.standard.nifti.Nifti1Dataset;
import mgui.numbers.MguiDouble;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/*****************************************************
 * Specifies options for outputting a <code>Volume3DInt</code> to persistent storage.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class VolumeOutputOptions extends InterfaceIOOptions {

	//public DataType type = DataTypes.getType(DataTypes.INTEGER);
	//public short datatype = Nifti1Dataset.NIFTI_TYPE_INT32;
	public int datatype = DataBuffer.TYPE_INT;
	public File[] files;
	public Volume3DInt[] volumes;
	public double intercept = 0, slope = 1;
	public String use_column = null;		// Set this to use specific column; otherwise will use current column
	public boolean apply_masks = false;
	
	public boolean compress = true; 		// Compress the output?
	
	//other options
	public short sform_code = Nifti1Dataset.NIFTI_XFORM_ALIGNED_ANAT;
	public short qform_code = Nifti1Dataset.NIFTI_XFORM_UNKNOWN;
	
	public boolean flipX, flipY, flipZ;
	
	public VolumeOutputOptions(){
		
	}
	
	@Override
	public JFileChooser getFileChooser(){
		return getFileChooser(null);
	}
	
	@Override
	public JFileChooser getFileChooser(File f){
		JFileChooser fc = null;
		if (f != null)
			fc = new JFileChooser(f);
		else
			fc = new JFileChooser();
		
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(true);
		fc.setDialogTitle("Select output file(s) for volume(s)");
		return fc;
	}

	@Override
	public File[] getFiles() {
		return files;
	}

	@Override
	public void setFiles(File[] files) {
		this.files = files;
	}
	
	@Override
	public void setObject(InterfaceObject obj) throws ClassCastException{
		volumes = new Volume3DInt[]{(Volume3DInt)obj};
	}

	@Override
	public void setFromComplementaryOptions(InterfaceIOOptions options) {
		if (!(options instanceof VolumeInputOptions)) return;
		VolumeInputOptions v_options = (VolumeInputOptions)options;
		
		this.datatype = v_options.transfer_type;
		this.intercept = v_options.intercept;
		this.flipX = v_options.flip_x;
		this.flipY = v_options.flip_y;
		this.flipZ = v_options.flip_z;
		this.slope = v_options.scale;
		
	}

	@Override
	public String getXML(int tab) {
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab+1);
		
		String xml = _tab + "<InterfaceIOOptions\n" +
					 _tab2 + "class='" + this.getClass().getCanonicalName() + "'\n" +
					 _tab2 + "datatype='" + DataTypes.getDataBufferTypeStr(datatype) + "'\n" +
					 _tab2 + "intercept='" + MguiDouble.getString(intercept, 10) + "'\n" +
					 _tab2 + "slope='" + MguiDouble.getString(intercept, 10) + "'\n" +
					 _tab2 + "flip_x='" + Boolean.toString(flipX) + "'\n" +
					 _tab2 + "flip_y='" + Boolean.toString(flipY) + "'\n" +
					 _tab2 + "flip_z='" + Boolean.toString(flipZ) + "'\n" +
					 _tab + " />";
		
		return xml;
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes,
			XMLType type) throws SAXException {
		// TODO Auto-generated method stub
		super.handleXMLElementStart(localName, attributes, type);
	}


}