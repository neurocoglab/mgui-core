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
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import mgui.image.util.WindowedColourModel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceComboBox.RenderMode;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.volume.GridVertexDataColumn;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;

/********************************************************
 * Dialog box for specifying options to write a {@link Volume3DInt} to file, using the default (Nifti)
 * format.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class VolumeOutputDialogBox extends InterfaceIODialogBox {

	JLabel lblVolume = new JLabel("Volume:");
	InterfaceComboBox cmbVolume = new InterfaceComboBox(RenderMode.LongestItem, true, 100);
	JLabel lblDataType = new JLabel("Data type:");
	JComboBox cmbDataType = new JComboBox();
	JLabel lblIntercept = new JLabel("Intercept:");
	JTextField txtIntercept = new JTextField("0.0");
	JLabel lblSlope = new JLabel("Slope:");
	JTextField txtSlope = new JTextField("1.0");
	JLabel lblColumn = new JLabel("Column:");
	JComboBox cmbColumn = new JComboBox();
	JCheckBox chkApplyMasks = new JCheckBox(" Apply masks");
	
	JLabel lblFlipX = new JLabel("Flip X");
	JCheckBox chkFlipX = new JCheckBox();
	JLabel lblFlipY = new JLabel("Flip Y");
	JCheckBox chkFlipY = new JCheckBox();
	JLabel lblFlipZ = new JLabel("Flip Z");
	JCheckBox chkFlipZ = new JCheckBox();
	
	JCheckBox chkCompress = new JCheckBox(" Compress?");
	
	public VolumeOutputDialogBox(){
		
	}
	
	public VolumeOutputDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		super(frame, panel, options);
		_init();
	}
	
	private void _init(){
		super.init();
		
		this.setDialogSize(450, 340);
		this.setTitle("Write Volume to Nifti - Options");
		
		cmbVolume.addActionListener(this);
		cmbVolume.setActionCommand("Volume Changed");
		cmbColumn.addActionListener(this);
		cmbColumn.setActionCommand("Column Changed");
		
		LineLayout lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.2, 1);
		mainPanel.add(lblVolume, c);
		c = new LineLayoutConstraints(1, 1, 0.3, 0.65, 1);
		mainPanel.add(cmbVolume, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.2, 1);
		mainPanel.add(lblDataType, c);
		c = new LineLayoutConstraints(2, 2, 0.3, 0.65, 1);
		mainPanel.add(cmbDataType, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.2, 1);
		mainPanel.add(lblIntercept, c);
		c = new LineLayoutConstraints(3, 3, 0.3, 0.65, 1);
		mainPanel.add(txtIntercept, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.2, 1);
		mainPanel.add(lblSlope, c);
		c = new LineLayoutConstraints(4, 4, 0.3, 0.65, 1);
		mainPanel.add(txtSlope, c);
		c = new LineLayoutConstraints(5, 5, 0.05, 0.2, 1);
		mainPanel.add(lblColumn, c);
		c = new LineLayoutConstraints(5, 5, 0.3, 0.65, 1);
		mainPanel.add(cmbColumn, c);
		c = new LineLayoutConstraints(6, 6, 0.1, 0.3, 1);
		mainPanel.add(chkApplyMasks, c);
		c = new LineLayoutConstraints(6, 6, 0.5, 0.3, 1);
		mainPanel.add(chkCompress, c);
		c = new LineLayoutConstraints(7, 7, 0.1, 0.05, 1);
		mainPanel.add(chkFlipX, c);
		c = new LineLayoutConstraints(7, 7, 0.16, 0.14, 1);
		mainPanel.add(lblFlipX, c);
		c = new LineLayoutConstraints(7, 7, 0.40, 0.05, 1);
		mainPanel.add(chkFlipY, c);
		c = new LineLayoutConstraints(7, 7, 0.46, 0.14, 1);
		mainPanel.add(lblFlipY, c);
		c = new LineLayoutConstraints(7, 7, 0.70, 0.05, 1);
		mainPanel.add(chkFlipZ, c);
		c = new LineLayoutConstraints(7, 7, 0.76, 0.14, 1);
		mainPanel.add(lblFlipZ, c);
				
		updateLists();
		updateDialog();
	}
	
	protected void updateLists(){
		
		//volumes
		cmbVolume.removeAllItems();
		
		ShapeSet3DInt set = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
		List<Shape3DInt> volumes = set.getShapeType(new Volume3DInt());
		
		
		for (Shape3DInt volume : volumes) {
			cmbVolume.addItem(volume);
			}
		
		//data types
		cmbDataType.removeAllItems();
		
		cmbDataType.addItem(getDataTypeStr(DataBuffer.TYPE_BYTE));
		cmbDataType.addItem(getDataTypeStr(DataBuffer.TYPE_USHORT));
		cmbDataType.addItem(getDataTypeStr(DataBuffer.TYPE_SHORT));
		cmbDataType.addItem(getDataTypeStr(DataBuffer.TYPE_INT));
		cmbDataType.addItem(getDataTypeStr(DataBuffer.TYPE_FLOAT));
		cmbDataType.addItem(getDataTypeStr(DataBuffer.TYPE_DOUBLE));
		
	}
	
	@Override
	public boolean updateDialog(){
		
		VolumeOutputOptions _options = (VolumeOutputOptions)options;
		
		if (_options.volumes != null){
			cmbVolume.setSelectedItem(_options.volumes[0]);
		}else{
			Volume3DInt current = (Volume3DInt)cmbVolume.getSelectedItem();
			if (current != null){
				// Force an update for new instance of dialog
				cmbVolume.setSelectedItem(current);
				}
			return true;
			}
		
		cmbDataType.setSelectedItem(getDataTypeStr(_options.datatype));
		txtIntercept.setText("" + _options.intercept);
		txtSlope.setText("" + _options.slope);
		cmbColumn.setSelectedItem(_options.use_column);
		
		chkApplyMasks.setSelected(_options.apply_masks);
		
		chkFlipX.setSelected(_options.flipX);
		chkFlipY.setSelected(_options.flipY);
		chkFlipZ.setSelected(_options.flipZ);
		
		chkCompress.setSelected(_options.compress);
		
		return true;
	}
	
	protected String getDataTypeStr(int type){
		
		switch (type){
			case DataBuffer.TYPE_BYTE:
				return "Byte (8 bit)";
			case DataBuffer.TYPE_SHORT:
				return "Short (16 bit signed)";
			case DataBuffer.TYPE_USHORT:
				return "Short (8 bit unsigned)";
			case DataBuffer.TYPE_INT:
				return "Integer (32 bit signed)";
			case DataBuffer.TYPE_FLOAT:
				return "Float (32 bit)";
			case DataBuffer.TYPE_DOUBLE:
				return "Double (64 bit)";
			default:
				return "?";
		}
		
	}
	
	protected short getDataType(String s){
		
		if (s.equals("Byte (8 bit)"))
			return DataBuffer.TYPE_BYTE;
		if (s.equals("Short (16 bit signed)"))
			return DataBuffer.TYPE_SHORT;
		if (s.equals("Short (8 bit unsigned)"))
			return DataBuffer.TYPE_USHORT;
		if (s.equals("Integer (32 bit signed)"))
			return DataBuffer.TYPE_INT;
		if (s.equals("Float (32 bit)"))
			return DataBuffer.TYPE_FLOAT;
		if (s.equals("Double (64 bit)"))
			return DataBuffer.TYPE_DOUBLE;
		
		return -1;
	}
	
	protected void updateColumns(){
		cmbColumn.removeAllItems();
		Volume3DInt volume = (Volume3DInt)cmbVolume.getSelectedItem();
		
		if (volume == null) return;
		
		ArrayList<String> channels = volume.getNonLinkedDataColumns();
		for (int i = 0; i < channels.size(); i++)
			cmbColumn.addItem(channels.get(i));
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			VolumeOutputOptions _options = (VolumeOutputOptions)options;
			
			Volume3DInt volume = (Volume3DInt)cmbVolume.getSelectedItem();
			if (volume == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "No volume specified!", 
											  "Volume Output Options", 
											  JOptionPane.ERROR_MESSAGE);
				return;
				}
				
			_options.volumes = new Volume3DInt[]{volume};
			_options.use_column = (String)cmbColumn.getSelectedItem();
			_options.intercept = Double.valueOf(txtIntercept.getText());
			_options.slope = Double.valueOf(txtSlope.getText());
			_options.datatype = getDataType((String)cmbDataType.getSelectedItem());
			
			_options.apply_masks = chkApplyMasks.isSelected();
			
			_options.flipX = chkFlipX.isSelected();
			_options.flipY = chkFlipY.isSelected();
			_options.flipZ = chkFlipZ.isSelected();
			
			_options.compress = chkCompress.isSelected();
			
			this.setVisible(false);
			return;
			}
		
		if (e.getActionCommand().equals("Volume Changed")){
			//update intercept, slope, and channel list
			updateColumns();
			
			Volume3DInt volume = (Volume3DInt)cmbVolume.getSelectedItem();
			if (volume == null) return;
			
			cmbColumn.setSelectedItem(volume.getCurrentColumn());
			GridVertexDataColumn column = (GridVertexDataColumn)volume.getCurrentDataColumn();
			if (column == null) return;
			cmbDataType.setSelectedItem(this.getDataTypeStr(column.getDataTransferType()));
			return;
			}
		
		if (e.getActionCommand().equals("Column Changed")){
			Volume3DInt volume = (Volume3DInt)cmbVolume.getSelectedItem();
			if (volume == null) return;
			String column = (String)cmbColumn.getSelectedItem();
			if (column == null) return;
			
			GridVertexDataColumn v_column = (GridVertexDataColumn)volume.getVertexDataColumn(column);
			WindowedColourModel model = v_column.getColourModel();
			txtIntercept.setText("" + model.getIntercept());
			txtSlope.setText("" + model.getScale());
			}	
		
		super.actionPerformed(e);
		
	}
	
	
}