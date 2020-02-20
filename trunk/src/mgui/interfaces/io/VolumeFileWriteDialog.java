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

package mgui.interfaces.io;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import mgui.geometry.Grid3D;
import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceDialogUpdater;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.io.domestic.shapes.VolumeFileWriter;
import mgui.io.foreign.vol.VolumeInputOptions;
import mgui.io.standard.nifti.NiftiMetadata;
import mgui.io.standard.nifti.NiftiVolumeWriter;

/*******************
 * 
 * 
 * @author areid
 * @deprecated
 */
public class VolumeFileWriteDialog extends InterfaceDialogBox
								   implements ItemListener {

	//file
	JLabel lblFileName = new JLabel("File name:");
	JTextField txtFileName = new JTextField("");
	JButton cmdBrowse = new JButton("Browse..");
	JComboBox cmbFileFormat = new JComboBox();
	JLabel lblFileFormat = new JLabel("File format:");
	JCheckBox chkRemAlpha = new JCheckBox("Remove alpha channel");
	
	//analyze-specific
	JLabel lblAnalyzeDataType = new JLabel("Data type:");
	JComboBox cmbAnalyzeDataType = new JComboBox();
	
	//TODO Descriptors for volume and output
	
	LineLayout lineLayout;
	ArrayList<FileNameExtensionFilter> filters = new ArrayList<FileNameExtensionFilter>();
	File volFile;
	Grid3D volume;
	
	boolean updateFormat;
	
	//constructor
	public VolumeFileWriteDialog(JFrame aFrame, InterfaceDialogUpdater parent){
		super(aFrame, parent);
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		init();
		setLocationRelativeTo(aFrame);
		this.setLocation(300, 300);
	}
	
	//init
	@Override
	protected void init(){
		super.init();
		lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(500,550);
		this.setTitle("Write grid data to volume file");

		//set file filters
		setFilters();
		
		//populate lists
		fillFormatCombo();
		fillAnalyzeDataCombo();
		disableFormatControls();
		
		//buttons
		cmdBrowse.addActionListener(this);
		cmdBrowse.setActionCommand("Browse");
		//cmbFileFormat.addActionListener(this);
		cmbFileFormat.setActionCommand("File Format");
		cmbFileFormat.addItemListener(this);
		
		chkRemAlpha.setSelected(false);
		
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
		//c = new LineLayoutConstraints(3, 3, 0.2, 0.5, 1);
		//mainPanel.add(chkRemAlpha, c);
		
		//format specific stuff
		c = new LineLayoutConstraints(3, 3, 0.01, 0.18, 1);
		mainPanel.add(lblAnalyzeDataType, c);
		c = new LineLayoutConstraints(3, 3, 0.2, 0.5, 1);
		mainPanel.add(cmbAnalyzeDataType, c);
		
	}
	
	private void setFilters(){
		filters.add(new FileNameExtensionFilter("Volume file (*.vol)", "vol"));
		filters.add(new FileNameExtensionFilter("Analyze file (*.hdr)", "hdr"));
		
	}
	
	private int getFileFormat(){
		if (cmbFileFormat.getSelectedItem().equals("Volume file (*.vol)")) 
			return VolumeInputOptions.FORMAT_VOL;
		if (cmbFileFormat.getSelectedItem().equals("Analyze file (*.hdr)")) 
			return VolumeInputOptions.FORMAT_ANALYZE;
		return -1;
	}
	
	//fill format combo box
	private void fillFormatCombo(){
		updateFormat = false;
		cmbFileFormat.removeAllItems();
		for (int i = 0; i < filters.size(); i++)
			cmbFileFormat.addItem(filters.get(i).getDescription());
		if (filters.size() > 0)
			cmbFileFormat.setSelectedIndex(0);
		updateFormat = true;
	}
	
	private void fillAnalyzeDataCombo(){
		cmbAnalyzeDataType.removeAllItems();
		cmbAnalyzeDataType.addItem("DT_BINARY (1)");
		cmbAnalyzeDataType.addItem("DT_UNSIGNED_CHAR (2)");
		cmbAnalyzeDataType.addItem("DT_UNSIGNED_INT (4)");
		cmbAnalyzeDataType.addItem("DT_SIGNED_INT (8)");
		cmbAnalyzeDataType.addItem("DT_FLOAT (16)");
		cmbAnalyzeDataType.addItem("DT_DOUBLE (32)");
		cmbAnalyzeDataType.addItem("DT_RGB (128)");
	}
	
	private FileNameExtensionFilter getFilter(){
		for (int i = 0; i < filters.size(); i++)
			if (cmbFileFormat.getSelectedItem().equals(filters.get(i).getDescription()))
				return filters.get(i);
		return null;
	}
	
	@Override
	public boolean updateDialog(InterfaceOptions p){
		if (!(p instanceof VolumeFileWriteOptions)) return false;
		volume = ((VolumeFileWriteOptions)p).volume;
		return true;
	}
	
	public void itemStateChanged(ItemEvent e) {
		if (!updateFormat) return;
		updateFormat = false;
		disableFormatControls();
		if (e.getSource().equals(cmbFileFormat)){
			switch(getFileFormat()){
				case VolumeInputOptions.FORMAT_ANALYZE:
					lblAnalyzeDataType.setVisible(true);
					cmbAnalyzeDataType.setVisible(true);
					break;
			
				}
			
			}
		mainPanel.updateUI();
		updateFormat = true;
	}

	void disableFormatControls(){
		lblAnalyzeDataType.setVisible(false);
		cmbAnalyzeDataType.setVisible(false);
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Browse")){
			JFileChooser fc;
			if (volFile != null)
				fc = new JFileChooser(volFile);
			else
				fc = new JFileChooser();
			fc.setFileFilter(getFilter());
					
			fc.setDialogTitle("Select Volume File");
			fc.setMultiSelectionEnabled(false);
			if (fc.showDialog(this, "Accept") == JFileChooser.APPROVE_OPTION){
				volFile = fc.getSelectedFile();
				String fileStr = volFile.getAbsolutePath();
				if (fileStr.length() < 4 || 
					fileStr.lastIndexOf(".") != fileStr.length() - 4){
					fileStr += ".hdr";
					volFile = new File(fileStr);
					}
				txtFileName.setText(volFile.getPath());
				}
			return;
			}
		
		//write to specified file
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			//boolean remAlpha = chkRemAlpha.isSelected();
			InterfaceSession.log("Write OK..");
			if (volume == null || volFile == null) return;
			InterfaceSession.log("Volume OK..");
			VolumeFileWriter writer = null;
			//otherwise write this volume to the specified file
			//with the specified format
			switch (getFileFormat()){
				case VolumeInputOptions.FORMAT_ANALYZE:
					InterfaceSession.log("Format = Analyze..");
					//write volume to analyze format files
					String typeStr = (String)cmbAnalyzeDataType.getSelectedItem();
					if (typeStr == null) return;
					typeStr = typeStr.substring(0, typeStr.indexOf(' '));
//					int type = NiftiMetadata.getDataType(typeStr);
//					if (type <= 0){
//						InterfaceSession.log("Invalid type: " + typeStr);
//						return;
//						}
					writer = new NiftiVolumeWriter(volFile);
					break;
				case VolumeInputOptions.FORMAT_VOL:
					//write volume to vol format
					break;
				}
			if (writer != null){
				try{
					writer.writeVolume(new Volume3DInt(volume)); //, remAlpha);
				}catch (IOException ex){
					ex.printStackTrace();
					//TODO: message here
					}
				this.setVisible(false);
				return;
				}
			
			}
		
		//try actionPerformed on super class
		super.actionPerformed(e);
	}
	
}