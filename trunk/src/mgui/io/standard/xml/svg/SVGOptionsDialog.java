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

package mgui.io.standard.xml.svg;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.io.InterfaceFilePanel;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;


public class SVGOptionsDialog extends JDialog implements ActionListener {

//	custom dialog for setting input options
	//specifically:
	//	Which section set to store shapes in (currentSet by default)
	//	Mapping data from multiple files to sections
	//		1. Table file
	//		2. Start section, with assumption of sequential assignment
	//		3. No mapping, all shapes on one section
	//	Mapping colours to selection sets
	//		1. Table file
	//		2. Numeric naming scheme (with some prefix) based on discrete colour values
	//		3. No selection sets
	
	private enum Command {
		MultiFile ("Multi file map"),
		Apply ("Apply settings"),
		Cancel ("Cancel"),
		ColourMap ("Colour map"),
		CheckBox ("Check Box Changed");
		
		public String type;
		
		Command(String ctype){
			type = ctype;
		}
	}
	
	//controls
	JLabel lblSectionSet = new JLabel("Section set:");
	JComboBox cmbSectionSet = new JComboBox();
	JCheckBox chkMultiFiles = new JCheckBox("Map from multiple files to sections");
	JCheckBox chkTableMultiFiles = new JCheckBox("Map to sections with table file:");
	JTextField txtTableMultiFiles = new JTextField("");
	JButton cmdTableMultiFiles = new JButton("Browse..");
	JCheckBox chkSeqMultiFiles = new JCheckBox("Increment from initial section:");
	JTextField txtSeqMultiFiles = new JTextField("0");
	JCheckBox chkSingleMultiFiles = new JCheckBox("Put all shapes on section:");
	JTextField txtSingleMultiFiles = new JTextField("0");
	JCheckBox chkColourMap = new JCheckBox("Map to selection set by colour");
	JCheckBox chkTableColourMap = new JCheckBox("Map to sets with table file:");
	JTextField txtTableColourMap = new JTextField("");
	JButton cmdTableColourMap = new JButton("Browse..");
	JCheckBox chkSeqColourMap = new JCheckBox("Increment using prefix:");
	JTextField txtSeqColourMap = new JTextField("Set");
	JCheckBox chkSetLineColour = new JCheckBox("Set line colour from file");
	JCheckBox chkSetFillColour = new JCheckBox("Set fill colour from file");
	
	JButton cmdApply = new JButton("Apply");
	JButton cmdCancel = new JButton("Cancel");
	
	//action listeners
	ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
	String actionCommand = "";
	
	//parent
	InterfaceFilePanel filePanel;
	public SVGInputOptions SVGOptions = new SVGInputOptions();
	
	//file stuff
	File multiTableFile;
	File colourMapFile;
	FileNameExtensionFilter tableFileFilter = new FileNameExtensionFilter("Excel file (*.xls)", "xls");
	
	public SVGOptionsDialog(){
		
	}
	
	public SVGOptionsDialog(Frame aFrame, InterfaceFilePanel parent) {
        super(aFrame, true);
        this.setLocationRelativeTo(aFrame);
        this.setBounds(300, 300, 500, 450);
        filePanel = parent;
		init();
	}
	
	//put it together
	private void init(){
		this.setTitle("File Input Options");
		//this.setPreferredSize(new Dimension(200,350));
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0, 1, 1);
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(450,475));
		panel.getInsets().left = 5;
		panel.getInsets().top = 5;
		panel.getInsets().bottom = 5;
		panel.getInsets().right = 5;
		
		panel.setLayout(new LineLayout(20, 5, 0));
		
		chkSetLineColour.setSelected(true);
		chkSetFillColour.setSelected(false);
		
		//populate section sets combo
		setSectionSets();
		
		c = new LineLayoutConstraints(1, 1, 0.1, 0.4, 1);
		panel.add(lblSectionSet, c);
		c = new LineLayoutConstraints(1, 1, 0.45, 0.5, 1);
		panel.add(cmbSectionSet, c);
		c = new LineLayoutConstraints(2, 2, 0.1, 0.5, 1);
		panel.add(chkMultiFiles, c);
		c = new LineLayoutConstraints(3, 3, 0.2, 0.5, 1);
		panel.add(chkTableMultiFiles, c);
		c = new LineLayoutConstraints(4, 4, 0.25, 0.45, 1);
		panel.add(txtTableMultiFiles, c);
		c = new LineLayoutConstraints(4, 4, 0.75, 0.2, 1);
		panel.add(cmdTableMultiFiles, c);
		c = new LineLayoutConstraints(5, 5, 0.2, 0.5, 1);
		panel.add(chkSeqMultiFiles, c);
		c = new LineLayoutConstraints(5, 5, 0.75, 0.2, 1);
		panel.add(txtSeqMultiFiles, c);
		c = new LineLayoutConstraints(7, 7, 0.1, 0.5, 1);
		panel.add(chkSingleMultiFiles, c);
		c = new LineLayoutConstraints(7, 7, 0.65, 0.2, 1);
		panel.add(txtSingleMultiFiles, c);
		
		c = new LineLayoutConstraints(9, 9, 0.1, 0.5, 1);
		panel.add(chkColourMap, c);
		c = new LineLayoutConstraints(10, 10, 0.2, 0.5, 1);
		panel.add(chkTableColourMap, c);
		c = new LineLayoutConstraints(11, 11, 0.25, 0.45, 1);
		panel.add(txtTableColourMap, c);
		c = new LineLayoutConstraints(11, 11, 0.75, 0.2, 1);
		panel.add(cmdTableColourMap, c);
		c = new LineLayoutConstraints(12, 12, 0.2, 0.5, 1);
		panel.add(chkSeqColourMap, c);
		c = new LineLayoutConstraints(12, 12, 0.75, 0.2, 1);
		panel.add(txtSeqColourMap, c);	
		c = new LineLayoutConstraints(14, 14, 0.1, 0.5, 1);
		panel.add(chkSetLineColour, c);
		c = new LineLayoutConstraints(15, 15, 0.1, 0.5, 1);
		panel.add(chkSetFillColour, c);	
		
		c = new LineLayoutConstraints(17, 18, 0.5, 0.2, 1);
		panel.add(cmdApply, c);
		c = new LineLayoutConstraints(17, 18, 0.73, 0.2, 1);
		panel.add(cmdCancel, c);
		
		//set up buttons
		cmdTableMultiFiles.setActionCommand(Command.MultiFile.type);
		cmdTableMultiFiles.addActionListener(this);
		cmdTableColourMap.setActionCommand(Command.ColourMap.type);
		cmdTableColourMap.addActionListener(this);
		cmdApply.setActionCommand(Command.Apply.type);
		cmdApply.addActionListener(this);
		cmdCancel.setActionCommand(Command.Cancel.type);
		cmdCancel.addActionListener(this);
		
		//check boxes
		chkMultiFiles.setActionCommand(Command.CheckBox.type);
		chkTableMultiFiles.setActionCommand(Command.CheckBox.type);
		chkSeqMultiFiles.setActionCommand(Command.CheckBox.type);
		chkSingleMultiFiles.setActionCommand(Command.CheckBox.type);
		chkColourMap.setActionCommand(Command.CheckBox.type);
		chkTableColourMap.setActionCommand(Command.CheckBox.type);
		chkSeqColourMap.setActionCommand(Command.CheckBox.type);
		
		chkMultiFiles.addActionListener(this);
		chkTableMultiFiles.addActionListener(this);
		chkSeqMultiFiles.addActionListener(this);
		chkSingleMultiFiles.addActionListener(this);
		chkColourMap.addActionListener(this);
		chkTableColourMap.addActionListener(this);
		chkSeqColourMap.addActionListener(this);
		
		//link options objects
		//filePanel.SVGOptions = SVGOptions;
		
		updateDialog();
		
		this.setContentPane(panel);
		
	}
	
	private void setSectionSets(){
		cmbSectionSet.removeAllItems();
		ShapeSet3DInt sectionSets = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new SectionSet3DInt());
		for (int i = 0; i < sectionSets.members.size(); i++)
			cmbSectionSet.addItem(sectionSets.members.get(i));
	}
	
	public void updateDialog(){
		//update settings from parent
		//SVGInputOptions SVGOptions = filePanel.SVGOptions;
		
		if (SVGOptions.multiFileOptions == SVGInputOptions.SVGMap.TableMap){
			chkTableMultiFiles.setSelected(true);
			updateCheckBoxes(chkTableMultiFiles);
		}
		if (SVGOptions.multiFileOptions == SVGInputOptions.SVGMap.Increment){
			chkSeqMultiFiles.setSelected(true);
			updateCheckBoxes(chkSeqMultiFiles);
		}
		if (SVGOptions.multiFileOptions == SVGInputOptions.SVGMap.None){
			chkSingleMultiFiles.setSelected(true);
			updateCheckBoxes(chkSingleMultiFiles);
		}
		if (SVGOptions.colourFileOptions == SVGInputOptions.SVGMap.TableMap){
			chkTableColourMap.setSelected(true);
			updateCheckBoxes(chkTableColourMap);
		}
		if (SVGOptions.colourFileOptions == SVGInputOptions.SVGMap.Increment){
			chkSeqColourMap.setSelected(true);
			updateCheckBoxes(chkSeqColourMap);
		}
		if (SVGOptions.colourFileOptions == SVGInputOptions.SVGMap.None){
			chkColourMap.setSelected(false);
			updateCheckBoxes(chkColourMap);
		}
		
		multiTableFile = SVGOptions.multiTableFile;
		if (multiTableFile != null)
			txtTableMultiFiles.setText(multiTableFile.getName());
		else
			txtTableMultiFiles.setText("");
		
		colourMapFile = SVGOptions.colourTableFile;
		if (colourMapFile != null)
			txtTableColourMap.setText(colourMapFile.getName());
		else
			txtTableColourMap.setText("");
		
		chkSetLineColour.setSelected(SVGOptions.setLineColour);
		chkSetFillColour.setSelected(SVGOptions.setFillColour);
		
		txtSingleMultiFiles.setText(String.valueOf(SVGOptions.shapeSection));
		txtSeqColourMap.setText(SVGOptions.prefixStr);
		txtSeqMultiFiles.setText(String.valueOf(SVGOptions.startSection));
		
		updateControls();
	}
	
	public void addActionListener(ActionListener a){
		listeners.add(a);
	}
	
	private void fireActionListeners(){
		ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, actionCommand);
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).actionPerformed(e);
	}
	
	public void actionPerformed(ActionEvent e){
		
		if (Command.Apply.type.equals(e.getActionCommand())){
			actionCommand = InterfaceFilePanel.Command.Apply_Input_Opt.type;
			//set options
			
			if (chkMultiFiles.isSelected()){
				if (chkTableMultiFiles.isSelected()){
					SVGOptions.multiFileOptions = SVGInputOptions.SVGMap.TableMap;
					SVGOptions.multiTableFile = multiTableFile;
				}else{
					SVGOptions.multiFileOptions = SVGInputOptions.SVGMap.Increment;
					SVGOptions.startSection = Integer.valueOf(txtSeqMultiFiles.getText()).intValue();
				}
			}else{
			SVGOptions.multiFileOptions = SVGInputOptions.SVGMap.None;
			SVGOptions.shapeSection = Integer.valueOf(txtSingleMultiFiles.getText()).intValue();
			}
			if (chkColourMap.isSelected()){
				if (chkTableColourMap.isSelected()){
					SVGOptions.colourFileOptions = SVGInputOptions.SVGMap.TableMap;
					SVGOptions.colourTableFile = colourMapFile;
				}else{
					SVGOptions.colourFileOptions = SVGInputOptions.SVGMap.Increment;
					SVGOptions.prefixStr = txtSeqColourMap.getText();
				}
			}else{
				SVGOptions.colourFileOptions = SVGInputOptions.SVGMap.None;
			}
			SVGOptions.setLineColour = chkSetLineColour.isSelected();
			SVGOptions.setFillColour = chkSetFillColour.isSelected();
			SVGOptions.sectionSet = (SectionSet3DInt)cmbSectionSet.getSelectedItem();
			
			fireActionListeners();
		}
		
		if (Command.Cancel.type.equals(e.getActionCommand())){
			actionCommand = InterfaceFilePanel.Command.Cancel_Input_Opt.type;
			fireActionListeners();
		}
		
		if (Command.MultiFile.type.equals(e.getActionCommand())){
			JFileChooser fc;
			if (multiTableFile != null)
				fc = new JFileChooser(multiTableFile);
			else
				fc = new JFileChooser();
			fc.setFileFilter(tableFileFilter);
			fc.setMultiSelectionEnabled(false);
			
			if (fc.showDialog(InterfaceSession.getSessionFrame(), "Accept") == JFileChooser.APPROVE_OPTION){
				multiTableFile = fc.getSelectedFile();
			}
			updateFiles();
		}
		
		if (Command.ColourMap.type.equals(e.getActionCommand())){
			JFileChooser fc;
			if (colourMapFile != null)
				fc = new JFileChooser(colourMapFile);
			else
				fc = new JFileChooser();
			fc.setFileFilter(tableFileFilter);
			fc.setMultiSelectionEnabled(false);
			
			if (fc.showDialog(InterfaceSession.getSessionFrame(), "Accept") == JFileChooser.APPROVE_OPTION){
				colourMapFile = fc.getSelectedFile();
			}
			updateFiles();
		}
		
		if (Command.CheckBox.type.equals(e.getActionCommand())){
			JCheckBox source = (JCheckBox)e.getSource();
			updateCheckBoxes(source);
		}
	}
	
	private void updateCheckBoxes(JCheckBox source){
		if (source.getText().compareTo("Map from multiple files to sections") == 0)
			chkSingleMultiFiles.setSelected(!source.isSelected());
		if (source.getText().compareTo("Put all shapes on section:") == 0)
			chkMultiFiles.setSelected(!source.isSelected());
		if (source.getText().compareTo("Map to sections with table file:") == 0)
			chkSeqMultiFiles.setSelected(!source.isSelected());
		if (source.getText().compareTo("Increment from initial section:") == 0)
			chkTableMultiFiles.setSelected(!source.isSelected());
		if (source.getText().compareTo("Map to sets with table file:") == 0)
			chkSeqColourMap.setSelected(!source.isSelected());
		if (source.getText().compareTo("Increment using prefix:") == 0)
			chkTableColourMap.setSelected(!source.isSelected());
		updateControls();
	}
	
	private void updateFiles(){
		if (multiTableFile != null)
			txtTableMultiFiles.setText(multiTableFile.getName());
		else
			txtTableMultiFiles.setText("");
		
		if (colourMapFile != null)
			txtTableColourMap.setText(colourMapFile.getName());
		else
			txtTableColourMap.setText("");
	}
	
	private void updateControls(){
		//update enabled status based upon check boxes
		if (chkMultiFiles.isSelected()){
			chkTableMultiFiles.setEnabled(true);
			if (chkTableMultiFiles.isSelected()){
				txtTableMultiFiles.setEnabled(true);
				cmdTableMultiFiles.setEnabled(true);
			}else{
				txtTableMultiFiles.setEnabled(false);
				cmdTableMultiFiles.setEnabled(false);
			}
			chkSeqMultiFiles.setEnabled(true);
			if (chkSeqMultiFiles.isSelected())
				txtSeqMultiFiles.setEnabled(true);
			else
				txtSeqMultiFiles.setEnabled(false);
		}
		else {
			chkTableMultiFiles.setEnabled(false);
			txtTableMultiFiles.setEnabled(false);
			cmdTableMultiFiles.setEnabled(false);
			chkSeqMultiFiles.setEnabled(false);
			txtSeqMultiFiles.setEnabled(false);
		}
		if (chkSingleMultiFiles.isSelected())
			txtSingleMultiFiles.setEnabled(true);
		else
			txtSingleMultiFiles.setEnabled(false);
		if (chkColourMap.isSelected()){
			chkTableColourMap.setEnabled(true);
			if (chkTableColourMap.isSelected()){
				txtTableColourMap.setEnabled(true);
				cmdTableColourMap.setEnabled(true);
			}else{
				txtTableColourMap.setEnabled(false);
				cmdTableColourMap.setEnabled(false);
			}
			chkSeqColourMap.setEnabled(true);
			if (chkSeqColourMap.isSelected())
				txtSeqColourMap.setEnabled(true);
			else
				txtSeqColourMap.setEnabled(false);
		}else{
			chkTableColourMap.setEnabled(false);
			txtTableColourMap.setEnabled(false);
			cmdTableColourMap.setEnabled(false);
			chkSeqColourMap.setEnabled(false);
			txtSeqColourMap.setEnabled(false);
		}
		
	}
	
}