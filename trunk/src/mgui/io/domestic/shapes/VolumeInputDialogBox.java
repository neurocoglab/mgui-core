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

package mgui.io.domestic.shapes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.DataBuffer;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import mgui.datasources.DataTypes;
import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.volume.VolumeMetadata;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;
import mgui.io.util.WildcardFileFilter;
import mgui.numbers.MguiFloat;

/*****************************************************
 * Dialog box for specifying parameters for loading a Volume3D object
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class VolumeInputDialogBox extends InterfaceIODialogBox 
								   implements ActionListener {
	
	static String preferred_filter = "";
	
	//controls
	//file
	JLabel lblFileName = new JLabel("File name:");
	JTextField txtFileName = new JTextField("");
	JButton cmdBrowse = new JButton("Browse..");
	JComboBox cmbFileFormat = new JComboBox();
	JLabel lblFileFormat = new JLabel("File format:");
	
	//header info
	JLabel lblHeaderInfo = new JLabel("HEADER INFO");
	JLabel lblSetGrid = new JLabel("Set grid");
	JLabel lblImgOrigin = new JLabel("Image origin");
	JTextField txtImgOrigin = new JTextField("");
	JCheckBox chkImgOrigin = new JCheckBox();
	JLabel lblDataDim = new JLabel("Data dimensions");
	JTextField txtDataDim = new JTextField("");
	JCheckBox chkDataDim = new JCheckBox();
	JLabel lblGeomDim = new JLabel("Geometric dimensions");
	JTextField txtGeomDim = new JTextField("");
	JCheckBox chkGeomDim = new JCheckBox();
	JLabel lblDataType = new JLabel("Data type");
	JTextField txtDataType = new JTextField("");
	JCheckBox chkDataType = new JCheckBox();
	JCheckBox chkAxes = new JCheckBox();
	
	//data format
	
	//options
	JLabel lblOptions = new JLabel("OPTIONS");
	JCheckBox chkSetAlpha = new JCheckBox();
	JLabel lblSetAlpha = new JLabel("Has alpha");
	JLabel lblColourMap = new JLabel("Colour map");
	InterfaceComboBox cmbColourMap = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
			  											   true, 500);
	//JComboBox cmbColourMap = new JComboBox();
	JButton cmdColourMap = new JButton("Edit..");
	JLabel lblFlipX = new JLabel("Flip X");
	JCheckBox chkFlipX = new JCheckBox();
	JLabel lblFlipY = new JLabel("Flip Y");
	JCheckBox chkFlipY = new JCheckBox();
	JLabel lblFlipZ = new JLabel("Flip Z");
	JCheckBox chkFlipZ = new JCheckBox();
	
	LineLayout lineLayout;
	ArrayList<WildcardFileFilter> filters = new ArrayList<WildcardFileFilter>();
	
	File volume_file;
	boolean hasValidHeader = false;
	boolean formatIsAlpha = true;
	float xOrigin;
	float yOrigin;
	float zOrigin;
	int xDim;
	int yDim;
	int zDim;
	float xSpace;
	float ySpace;
	float zSpace;
	int bytesPerVoxel;
	double dataMax, dataMin;
	//DataType dataType;
	//int dataType;
	int transferType;
	Vector3f xAxis = new Vector3f(1, 0, 0);
	Vector3f yAxis = new Vector3f(0, 1, 0);
	Vector3f zAxis = new Vector3f(0, 0, 1);
	
	boolean show_checks = true;
	Volume3DInt volume;
	
	ArrayList<InterfaceIOType> types;
		
	public VolumeInputDialogBox(){
		
	}
	
	public VolumeInputDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions opts){
		super(frame, panel, opts);
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		this.show_checks = show_checks;
		_init();
		
		this.setLocation(300, 300);
	}
	
	public VolumeInputDialogBox(InterfaceIOPanel panel, InterfaceIOOptions opts){
		this(panel, opts, true);
	}
	
	//constructor
	public VolumeInputDialogBox(InterfaceIOPanel panel, InterfaceIOOptions opts, boolean show_checks){
		super(InterfaceSession.getSessionFrame(), panel, opts);
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		this.show_checks = show_checks;
		_init();
		
		this.setLocation(300, 300);
	}
	
	public VolumeInputOptions getOptions(){
		return (VolumeInputOptions)this.options;
	}
	
	//init
	private void _init(){
		super.init();
		lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(500,430);
		this.setTitle("Set grid from volume file");
		
		chkDataDim.setSelected(true);
		chkGeomDim.setSelected(true);
		chkImgOrigin.setSelected(true);
		chkDataType.setSelected(false);
		chkSetAlpha.setSelected(true);
		
		//set IO types (available formats)
		setIOTypes();
		
		//populate lists
		fillFormatCombo();
		fillColourMapCombo();
		updateControls();
		updateDialog(options);
		
		//init controls
		txtDataDim.setEditable(false);
		txtImgOrigin.setEditable(false);
		txtGeomDim.setEditable(false);
		txtDataType.setEditable(false);
		txtFileName.setEditable(false);
		//txtDataMax.setEditable(false);
		
		//TODO: make this option visible
		chkAxes.setSelected(true);
		
		//buttons
		cmdBrowse.addActionListener(this);
		cmdBrowse.setActionCommand("Browse");
		chkSetAlpha.addActionListener(this);
		chkSetAlpha.setActionCommand("Set Alpha");
		cmbFileFormat.addActionListener(this);
		cmbFileFormat.setActionCommand("File Format");
		
		//set up controls
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.01, 0.18, 1);
		
		mainPanel.add(lblFileFormat, c);
		c = new LineLayoutConstraints(1, 1, 0.2, 0.5, 1);
		mainPanel.add(cmbFileFormat, c);
		c = new LineLayoutConstraints(1, 1, 0.71, 0.28, 1);
		mainPanel.add(cmdBrowse, c);
		c = new LineLayoutConstraints(2, 2, 0.01, 0.18, 1);
		mainPanel.add(lblFileName, c);
		c = new LineLayoutConstraints(2, 2, 0.2, 0.5, 1);
		mainPanel.add(txtFileName, c);
		c = new LineLayoutConstraints(2, 2, 0.71, 0.28, 1);
		mainPanel.add(cmdBrowse, c);
		
		//header info
		c = new LineLayoutConstraints(3, 3, 0.05, 0.5, 1);
		mainPanel.add(lblHeaderInfo, c);
		c = new LineLayoutConstraints(3, 3, 0.88, 0.1, 1);
		mainPanel.add(lblSetGrid, c);
		c = new LineLayoutConstraints(4, 4, 0.1, 0.3, 1);
		mainPanel.add(lblImgOrigin, c);
		c = new LineLayoutConstraints(4, 4, 0.4, 0.45, 1);
		mainPanel.add(txtImgOrigin, c);
		if (show_checks){
			c = new LineLayoutConstraints(4, 4, 0.9, 0.1, 1);
			mainPanel.add(chkImgOrigin, c);
			}
		c = new LineLayoutConstraints(5, 5, 0.1, 0.3, 1);
		mainPanel.add(lblDataDim, c);
		c = new LineLayoutConstraints(5, 5, 0.4, 0.45, 1);
		mainPanel.add(txtDataDim, c);
		if (show_checks){
			c = new LineLayoutConstraints(5, 5, 0.9, 0.1, 1);
			mainPanel.add(chkDataDim, c);
			}
		c = new LineLayoutConstraints(6, 6, 0.1, 0.3, 1);
		mainPanel.add(lblGeomDim, c);
		c = new LineLayoutConstraints(6, 6, 0.4, 0.45, 1);
		mainPanel.add(txtGeomDim, c);
		if (show_checks){
			c = new LineLayoutConstraints(6, 6, 0.9, 0.1, 1);
			mainPanel.add(chkGeomDim, c);
			}
		c = new LineLayoutConstraints(7, 7, 0.1, 0.3, 1);
		mainPanel.add(lblDataType, c);
		c = new LineLayoutConstraints(7, 7, 0.4, 0.45, 1);
		mainPanel.add(txtDataType, c);
		txtDataType.setEditable(false);
		if (show_checks){
			c = new LineLayoutConstraints(7, 7, 0.9, 0.1, 1);
			mainPanel.add(chkDataType, c);
			}
		
		//options
		c = new LineLayoutConstraints(8, 8, 0.05, 0.5, 1);
		mainPanel.add(lblOptions, c);
		c = new LineLayoutConstraints(9, 9, 0.1, 0.05, 1);
		mainPanel.add(chkSetAlpha, c);
		c = new LineLayoutConstraints(9, 9, 0.16, 0.7, 1);
		mainPanel.add(lblSetAlpha, c);
		/*
		c = new LineLayoutConstraints(10, 10, 0.2, 0.4, 1);
		mainPanel.add(lblMinAlpha, c);
		c = new LineLayoutConstraints(10, 10, 0.6, 0.25, 1);
		mainPanel.add(txtMinAlpha, c);
		c = new LineLayoutConstraints(11, 11, 0.2, 0.4, 1);
		mainPanel.add(lblMaxAlpha, c);
		c = new LineLayoutConstraints(11, 11, 0.6, 0.25, 1);
		mainPanel.add(txtMaxAlpha, c);
		*/
		c = new LineLayoutConstraints(10, 10, 0.1, 0.3, 1);
		mainPanel.add(lblColourMap, c);
		c = new LineLayoutConstraints(10, 10, 0.3, 0.4, 1);
		mainPanel.add(cmbColourMap, c);
		//c = new LineLayoutConstraints(12, 12, 0.75, 0.2, 1);
		//mainPanel.add(cmdColourMap, c);
		c = new LineLayoutConstraints(11, 11, 0.1, 0.05, 1);
		mainPanel.add(chkFlipX, c);
		c = new LineLayoutConstraints(11, 11, 0.16, 0.14, 1);
		mainPanel.add(lblFlipX, c);
		c = new LineLayoutConstraints(11, 11, 0.40, 0.05, 1);
		mainPanel.add(chkFlipY, c);
		c = new LineLayoutConstraints(11, 11, 0.46, 0.14, 1);
		mainPanel.add(lblFlipY, c);
		c = new LineLayoutConstraints(11, 11, 0.70, 0.05, 1);
		mainPanel.add(chkFlipZ, c);
		c = new LineLayoutConstraints(11, 11, 0.76, 0.14, 1);
		mainPanel.add(lblFlipZ, c);
		
	}
	
	public static Volume3DInt showDialog(InterfaceIOPanel panel, VolumeInputOptions opts, boolean show_checks){
		VolumeInputDialogBox dialog = new VolumeInputDialogBox(panel, opts, show_checks);
		dialog.setVisible(true);
		
		VolumeInputOptions _options = (VolumeInputOptions)dialog.options;
		if (_options == null || _options.files == null) return null;
		
		_options.transfer_type = DataBuffer.TYPE_DOUBLE;
		
		return loadVolume(_options, panel);
	}
	
	protected InterfaceIOType getCurrentIOType(){
		
		String filterStr = (String)cmbFileFormat.getSelectedItem();
		WildcardFileFilter filter = null;
		
		for (int i = 0; i < filters.size(); i++)
			if (filters.get(i).getDescription().equals(filterStr))
				filter = filters.get(i);
		
		for (int i = 0; i < types.size(); i++)
			if (types.get(i).getFilter().equals(filter)) return types.get(i);
		
		return null;
	}
	
	public static Volume3DInt loadVolume(VolumeInputOptions options, InterfaceIOPanel panel) {
	
		VolumeFileLoader loader = options.getLoader();
		
		loader.setFile(options.getFiles()[0]);
		loader.setAlpha(options.has_alpha,
						(float)options.alpha_min,
						(float)options.alpha_max);
		loader.flipX = options.flip_x;
		loader.flipY = options.flip_y;
		loader.flipZ = options.flip_z;
			
		InterfaceProgressBar progress_bar = new InterfaceProgressBar();
		Volume3DInt volume = loader.getVolume3D(options, 0, progress_bar);
		return volume;
	}
	
	private void setIOTypes(){
		
		types = InterfaceEnvironment.getIOTypesForClass(VolumeFileLoader.class);
		for (int i = 0; i < types.size(); i++){
			filters.add(types.get(i).getFilter());
			}
	
	}
	
	@Override
	public boolean updateDialog(InterfaceOptions opt){
		
		updateColourMaps();
		VolumeInputOptions options = (VolumeInputOptions)opt;
		
		if (options == null || options.files == null || options.files[0] == null || options.input_type == null){
			txtFileName.setText("");
			updateControls();
			return true;
			}
		
		cmbFileFormat.setSelectedItem(options.input_type.getFilter().getDescription());
		
		txtFileName.setText(options.files[0].getAbsolutePath());
		volume_file = options.files[0];
		
		if (volume_file != null){
			updateFileHeader();
			}
		
		chkSetAlpha.setSelected(options.has_alpha);
		chkFlipX.setSelected(options.flip_x);
		chkFlipY.setSelected(options.flip_y);
		chkFlipZ.setSelected(options.flip_z);
		transferType = options.transfer_type;
		cmbColourMap.setSelectedItem(options.colour_map);
		
		return true;
	}
	
	protected void setFormat(String loader){
		
		try{
			ArrayList<InterfaceIOType> types = InterfaceEnvironment.getIOTypesForClass(Class.forName(loader));
			if (types.size() == 0) return;
			cmbFileFormat.setSelectedItem(types.get(0).getFilter().getDescription());
		}catch (Exception e){
			InterfaceSession.log("Could not find IO type for class '" + loader + "'.");
			}
		
	}
	
	public void updateColourMaps(){
		ArrayList<ColourMap> maps = InterfaceEnvironment.getColourMaps();
		
		ColourMap current = (ColourMap)cmbColourMap.getSelectedItem();
		
		cmbColourMap.removeAllItems();
		
		for (int i = 0; i < maps.size(); i++)
			cmbColourMap.addItem(maps.get(i));
		
		if (current != null) cmbColourMap.setSelectedItem(current);
		
	}
	
	//update parent with these settings
	private void updateParent(){
		
		VolumeInputOptions options = (VolumeInputOptions)this.options;
		
		if (volume_file == null){
			options.setFiles(null);
			return;
			}
		
		options.setFiles(new File[]{volume_file});
		
		options.has_alpha = chkSetAlpha.isSelected();
		options.flip_x = chkFlipX.isSelected();
		options.flip_y = chkFlipY.isSelected();
		options.flip_z = chkFlipZ.isSelected();
		
		options.set_dims = chkDataDim.isSelected() && options.allow_dim_change;
		options.set_geom = chkGeomDim.isSelected() && options.allow_geom_change;
		options.set_origin = chkImgOrigin.isSelected() && options.allow_geom_change;
		
		options.transfer_type = transferType;
		
		if (options.allow_dim_change){
			options.dim_x = xDim;
			options.dim_y = yDim;
			options.dim_z = zDim;
			}
		
		if (options.allow_geom_change){
			options.geom_x = xSpace;
			options.geom_y = ySpace;
			options.geom_z = zSpace;
			options.origin_x = xOrigin;
			options.origin_y = yOrigin;
			options.origin_z = zOrigin;
			options.axis_x = xAxis;
			options.axis_y = yAxis;
			options.axis_z = zAxis;
			}
		
		options.set_type = chkDataType.isSelected();
		
		options.input_type = getCurrentIOType();
		preferred_filter = (String)cmbFileFormat.getSelectedItem();
		
		options.colour_map = null;
		if (cmbColourMap.isEnabled())
			options.colour_map = (ColourMap)cmbColourMap.getSelectedItem();
		
		io_panel.updateFromDialog(this);
		
	}
	
	//fill format combo box
	private void fillFormatCombo(){
		cmbFileFormat.removeAllItems();
		for (int i = 0; i < filters.size(); i++) {
			cmbFileFormat.addItem(filters.get(i).getDescription());
			}
		
		if (filters.size() > 0) {
			if (preferred_filter.length() > 0) {
				cmbFileFormat.setSelectedItem(preferred_filter);
			} else {
				cmbFileFormat.setSelectedIndex(0);
				}
			}
	}
	
	private void fillColourMapCombo(){
		ArrayList<ColourMap> maps = InterfaceEnvironment.getColourMaps();
		for (int i = 0; i < maps.size(); i++)
			cmbColourMap.addItem(maps.get(i));
	}
	
	//update controls based upon file status
	private void updateControls(){
		
		VolumeInputOptions _options = (VolumeInputOptions)this.options;
		boolean allow_dim_change = true;
		boolean allow_geom_change = true;
		
		if (_options != null){
			allow_dim_change = _options.allow_dim_change;
			allow_geom_change = _options.allow_geom_change;
			}
		
		//header info
		lblHeaderInfo.setEnabled(hasValidHeader && (allow_dim_change || allow_geom_change));
		lblSetGrid.setEnabled(hasValidHeader && (allow_dim_change || allow_geom_change));
		lblDataDim.setEnabled(hasValidHeader && allow_dim_change);
		txtDataDim.setEnabled(hasValidHeader && allow_dim_change);
		chkDataDim.setEnabled(hasValidHeader && allow_dim_change);
		lblGeomDim.setEnabled(hasValidHeader && allow_geom_change);
		txtGeomDim.setEnabled(hasValidHeader && allow_geom_change);
		chkGeomDim.setEnabled(hasValidHeader && allow_geom_change);
		lblImgOrigin.setEnabled(hasValidHeader && allow_geom_change);
		txtImgOrigin.setEnabled(hasValidHeader && allow_geom_change);
		chkImgOrigin.setEnabled(hasValidHeader && allow_geom_change);
		lblDataType.setEnabled(hasValidHeader);
		txtDataType.setEnabled(hasValidHeader);
		chkDataType.setEnabled(hasValidHeader);
		
		//options
		lblOptions.setEnabled(hasValidHeader);
		chkSetAlpha.setEnabled(hasValidHeader && formatIsAlpha);
		lblSetAlpha.setEnabled(hasValidHeader && formatIsAlpha);
		lblColourMap.setEnabled(hasValidHeader);
		cmbColourMap.setEnabled(hasValidHeader && true); // ((String)cmbGridFormat.getSelectedItem()).contains("map"));
		cmdColourMap.setEnabled(hasValidHeader);
		lblFlipX.setEnabled(hasValidHeader);
		chkFlipX.setEnabled(hasValidHeader);
		lblFlipY.setEnabled(hasValidHeader);
		chkFlipY.setEnabled(hasValidHeader);
		lblFlipZ.setEnabled(hasValidHeader);
		chkFlipZ.setEnabled(hasValidHeader);
	
	}
	
	private void updateFormat(){
		formatIsAlpha = true;
	}
	
	//fill header fields with data from this file
	private void updateFileHeader(){
		hasValidHeader = false;
		clearHeaderInfo();
		
		//get loader
		InterfaceIOType io_type = this.getCurrentIOType();
		if (io_type == null) return;
		
		VolumeFileLoader loader = (VolumeFileLoader)io_type.getIOInstance();
		if (loader == null) return;
		
		VolumeInputOptions options = (VolumeInputOptions)io_type.getOptionsInstance();
		options.setFrom((VolumeInputOptions)this.options);
		options.allow_dim_change = ((VolumeInputOptions)this.options).allow_dim_change;
		options.allow_geom_change = ((VolumeInputOptions)this.options).allow_geom_change;
		this.options = options;
		
		if (volume_file == null || !volume_file.exists()){
			return;
			}
		
		loader.setFile(volume_file);
		VolumeMetadata metadata = null;
		
		try{
			metadata = loader.getVolumeMetadata();
		}catch (Exception e){
			//e.printStackTrace();
			InterfaceSession.handleException(e);
			return;
			}
		
		if (metadata == null){
			InterfaceSession.log("Error: Could not load volume metadata.", LoggingType.Errors);
			return;
			}
		
		int[] dims = metadata.getDataDims();
		xDim = dims[0];
		yDim = dims[1];
		zDim = dims[2];
		
		float[] geom = metadata.getGeomDims();
		xSpace = geom[0];
		ySpace = geom[1];
		zSpace = geom[2];
		
		Point3f origin = metadata.getOrigin();
		xOrigin = origin.x;
		yOrigin = origin.y;
		zOrigin = origin.z;
		
		Vector3f[] axes = metadata.getAxes();
		xAxis = axes[0];
		yAxis = axes[1];
		zAxis = axes[2];
		
		transferType = metadata.getDataType();
		hasValidHeader = true;
		
		txtImgOrigin.setText(xOrigin + ", " + yOrigin + ", " + zOrigin);
		txtDataDim.setText(xDim + ", " + yDim + ", " + zDim);
		txtGeomDim.setText(MguiFloat.getString(xSpace, "#0.00") + ", " +
						   MguiFloat.getString(ySpace, "#0.00") + ", " +
						   MguiFloat.getString(zSpace, "#0.00"));
		txtDataType.setText(DataTypes.getDataBufferTypeStr(transferType));
		updateControls();
			
	}
	
	private void clearHeaderInfo(){
		txtImgOrigin.setText("");
		txtDataDim.setText("");
		txtGeomDim.setText("");
		txtDataType.setText("");
		updateControls();
	}
	
	private WildcardFileFilter getFilter(){
		for (int i = 0; i < filters.size(); i++)
			if (cmbFileFormat.getSelectedItem().equals(filters.get(i).getDescription()))
				return filters.get(i);
		return null;
	}
	
	//handling
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Browse")){
			JFileChooser fc;
			if (volume_file != null) {
				fc = new JFileChooser(volume_file);
			} else {
				fc = new JFileChooser();
				}
			fc.setFileFilter(getFilter());
					
			fc.setDialogTitle("Select Volume File");
			fc.setMultiSelectionEnabled(false);
			if (fc.showDialog(InterfaceSession.getSessionFrame(), "Accept") == JFileChooser.APPROVE_OPTION){
				volume_file = fc.getSelectedFile();
				txtFileName.setText(volume_file.getPath());
				updateFileHeader();
				}
			return;
			}
		
		if (e.getActionCommand().equals("Set Alpha")){
			updateControls();
			return;
			}
		
		if (e.getActionCommand().equals("Format Changed")){
			updateFormat();
			updateControls();
			return;
		}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			//set parent options
			updateParent();
			this.setVisible(false);
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_CANCEL)){
			
			this.setVisible(false);
			}
		
	}

}