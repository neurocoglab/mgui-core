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

package mgui.interfaces.maps;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.ColourButton;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.layouts.InterfaceLayoutFrame;
import mgui.interfaces.xml.XMLException;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.domestic.maps.ContinuousColourMapLoader;
import mgui.io.domestic.maps.ContinuousColourMapWriter;
import mgui.io.domestic.maps.DiscreteColourMapLoader;
import mgui.numbers.MguiFloat;
import mgui.util.Colour4f;
import mgui.util.Colours;


/**************************************
 * Panel to create and modify various maps, including colour maps and value maps.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 */

public class InterfaceMapPanel extends InterfacePanel implements ActionListener,
																 KeyListener{

	CategoryTitle lblContinuousColour = new CategoryTitle("CONTINUOUS COLOUR");
	//JComboBox cmbContinuousColourMaps = new JComboBox();
	InterfaceComboBox cmbContinuousColourMaps = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
																	  true, 500);
	ContinuousColourBar barContinuousColour = new ContinuousColourBar();
	JLabel lblContMapName = new JLabel("Name:");
	JTextField txtContMapName = new JTextField("");
	JLabel lblAnchor = new JLabel("Anchor:");
	JTextField txtAnchor = new JTextField("0");
	JButton cmdNextAnchor = new JButton(">");
	JButton cmdPrevAnchor = new JButton("<");
	JLabel lblAnchorValue = new JLabel("Value:");
	JTextField txtAnchorValue = new JTextField("0.0");
	JLabel lblAnchorColour = new JLabel("Colour:");
	ColourButton cmdAnchorColour = new ColourButton();
	JLabel lblAnchorAlpha = new JLabel("Alpha:");
	JTextField txtAnchorAlpha = new JTextField("1.0");
	
	JButton cmdAddContinuous = new JButton("Add");
	JButton cmdCopyContinuous = new JButton("Copy");
	JButton cmdDelContinuous = new JButton("Del");
	JButton cmdLoadContinuous = new JButton("Load");
	JButton cmdSaveContinuous = new JButton("Save");
	JButton cmdInvertContinuous = new JButton("Invert");
	JButton cmdUpdateContinuous = new JButton("Update");
	
	CategoryTitle lblDiscreteColour = new CategoryTitle("DISCRETE COLOUR");
	//JComboBox cmbDiscreteColourMaps = new JComboBox();
	InterfaceComboBox cmbDiscreteColourMaps = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
			  														true, 500);
	JScrollPane paneDiscreteColour;
	DiscreteColourMapTable tblDiscreteColour = new DiscreteColourMapTable();
	JCheckBox chkDiscreteNameMap = new JCheckBox(" Has name map:");
	//JComboBox cmbDiscreteNameMaps = new JComboBox();
	InterfaceComboBox cmbDiscreteNameMaps = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
																  true, 500);
	JButton cmdDiscreteSave = new JButton("Save");
	JButton cmdDiscreteLoad = new JButton("Load");
	JButton cmdDiscreteNew = new JButton("New");
	JButton cmdDiscreteDelete = new JButton("Delete");
	JButton cmdDiscreteShowLayout = new JButton("Layout");
	JButton cmdDiscreteLayoutColour = new JButton();
	
	CategoryTitle lblNameMaps = new CategoryTitle("NAME MAPS");
	//JComboBox cmbNameMaps = new JComboBox();
	InterfaceComboBox cmbNameMaps = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
														  true, 500);
	JScrollPane paneNameMap;
	NameMapTable tblNameMap = new NameMapTable();
	
	static String CMD_ANCHOR_COLOUR = "Change Anchor Colour";
	static String CMB_CONT_MAP = "Change Continuous Map";
	static String CMD_ADD_CONTINUOUS = "Add Continuous Map";
	static String CMD_DEL_CONTINUOUS = "Delete Continuous Map";
	
	boolean updateCombo = true;
	
	public InterfaceMapPanel(){
		if (InterfaceSession.isInit())
			init();
	}
	
	protected void init(){
		
		//set up panel categories and components
		setLayout(new CategoryLayout(InterfaceEnvironment.getLineHeight(), 5, 200, 10));
		
		cmdDiscreteLayoutColour.setBackground(Color.white);
		cmdDiscreteLayoutColour.addActionListener(this);
		cmdDiscreteLayoutColour.setActionCommand("Discrete Layout Colour");
		
		cmdAnchorColour.setActionCommand(CMD_ANCHOR_COLOUR);
		cmdAnchorColour.addActionListener(this);
		cmdAddContinuous.setActionCommand(CMD_ADD_CONTINUOUS);
		cmdAddContinuous.addActionListener(this);
		cmdDelContinuous.setActionCommand(CMD_DEL_CONTINUOUS);
		cmdDelContinuous.addActionListener(this);
		cmdCopyContinuous.setActionCommand("Copy Continuous Map");
		cmdCopyContinuous.addActionListener(this);
		barContinuousColour.addActionListener(this);
		cmbContinuousColourMaps.setActionCommand(CMB_CONT_MAP);
		cmbContinuousColourMaps.addActionListener(this);
		cmdLoadContinuous.setActionCommand("Load Continuous Map");
		cmdLoadContinuous.addActionListener(this);
		cmdSaveContinuous.setActionCommand("Save Continuous Map");
		cmdSaveContinuous.addActionListener(this);
		cmdInvertContinuous.setActionCommand("Invert Continuous Map");
		cmdInvertContinuous.addActionListener(this);
		cmdUpdateContinuous.setActionCommand("Update Continuous Map");
		cmdUpdateContinuous.addActionListener(this);
		cmbNameMaps.setActionCommand("Change Name Map");
		cmbNameMaps.addActionListener(this);
		cmbDiscreteColourMaps.setActionCommand("Discrete Map Changed");
		cmbDiscreteColourMaps.addActionListener(this);
		chkDiscreteNameMap.setActionCommand("Has Discrete Name Map");
		chkDiscreteNameMap.addActionListener(this);
		cmdDiscreteSave.addActionListener(this);
		cmdDiscreteSave.setActionCommand("Discrete Save Map");
		cmdDiscreteLoad.addActionListener(this);
		cmdDiscreteLoad.setActionCommand("Discrete Load Map");
		cmdDiscreteNew.addActionListener(this);
		cmdDiscreteNew.setActionCommand("Discrete New Map");
		cmdDiscreteShowLayout.addActionListener(this);
		cmdDiscreteShowLayout.setActionCommand("Discrete Show Layout");
		cmbDiscreteNameMaps.addActionListener(this);
		cmbDiscreteNameMaps.setActionCommand("Discrete Name Map Changed");
		
		paneDiscreteColour = new JScrollPane(tblDiscreteColour);
		paneNameMap = new JScrollPane(tblNameMap);
		
		txtAnchorAlpha.addKeyListener(this);
		
		updateContinuousColour();
		updateDiscreteColour();
		
		CategoryLayoutConstraints c = new CategoryLayoutConstraints();
		lblContinuousColour.isExpanded = false;
		add(lblContinuousColour, c);
		lblContinuousColour.setParentObj(this);
		
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 1, 1, 0.05, 0.9, 1);
		add(cmbContinuousColourMaps, c);
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 2, 3, 0.05, 0.9, 1);
		add(barContinuousColour, c);
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 4, 4, 0.05, 0.3, 1);
		add(lblContMapName, c);
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 4, 4, 0.31, 0.64, 1);
		add(txtContMapName, c);
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 5, 5, 0.05, 0.3, 1);
		add(lblAnchor, c);
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 5, 5, 0.31, 0.3, 1);
		add(txtAnchor, c);
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 5, 5, 0.62, 0.16, 1);
		add(cmdPrevAnchor, c);
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 5, 5, 0.79, 0.16, 1);
		add(cmdNextAnchor, c);
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 6, 6, 0.62, 0.33, 1);
		add(cmdAddContinuous, c);
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 6, 6, 0.05, 0.3, 1);
		add(lblAnchorValue, c);
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 6, 6, 0.31, 0.3, 1);
		add(txtAnchorValue, c);
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 7, 7, 0.62, 0.33, 1);
		add(cmdDelContinuous, c);
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 7, 7, 0.05, 0.3, 1);
		add(lblAnchorColour, c);
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 7, 7, 0.31, 0.3, 1);
		add(cmdAnchorColour, c);
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 8, 8, 0.62, 0.33, 1);
		add(cmdCopyContinuous, c);
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 8, 8, 0.05, 0.3, 1);
		add(lblAnchorAlpha, c);
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 8, 8, 0.31, 0.3, 1);
		add(txtAnchorAlpha, c);
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 9, 9, 0.05, 0.43, 1);
		add(cmdLoadContinuous, c);
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 9, 9, 0.52, 0.43, 1);
		add(cmdSaveContinuous, c);
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 10, 10, 0.05, 0.43, 1);
		add(cmdInvertContinuous, c);
		c = new CategoryLayoutConstraints("CONTINUOUS COLOUR", 10, 10, 0.52, 0.43, 1);
		add(cmdUpdateContinuous, c);
		
		c = new CategoryLayoutConstraints();
		lblDiscreteColour.isExpanded = false;
		add(lblDiscreteColour, c);
		lblDiscreteColour.setParentObj(this);
		
		c = new CategoryLayoutConstraints("DISCRETE COLOUR", 1, 1, 0.05, 0.9, 1);
		add(cmbDiscreteColourMaps, c);
		c = new CategoryLayoutConstraints("DISCRETE COLOUR", 2, 2, 0.05, 0.9, 1);
		add(chkDiscreteNameMap, c);
		c = new CategoryLayoutConstraints("DISCRETE COLOUR", 3, 3, 0.05, 0.9, 1);
		add(cmbDiscreteNameMaps, c);
		c = new CategoryLayoutConstraints("DISCRETE COLOUR", 4, 12, 0.05, 0.9, 1);
		add(paneDiscreteColour, c);
		c = new CategoryLayoutConstraints("DISCRETE COLOUR", 13, 13, 0.05, 0.44, 1);
		add(cmdDiscreteSave, c);
		c = new CategoryLayoutConstraints("DISCRETE COLOUR", 13, 13, 0.51, 0.44, 1);
		add(cmdDiscreteLoad, c);
		c = new CategoryLayoutConstraints("DISCRETE COLOUR", 14, 14, 0.05, 0.44, 1);
		add(cmdDiscreteNew, c);
		c = new CategoryLayoutConstraints("DISCRETE COLOUR", 14, 14, 0.51, 0.44, 1);
		add(cmdDiscreteDelete, c);
		c = new CategoryLayoutConstraints("DISCRETE COLOUR", 15, 15, 0.05, 0.44, 1);
		add(cmdDiscreteShowLayout, c);
		c = new CategoryLayoutConstraints("DISCRETE COLOUR", 15, 15, 0.51, 0.44, 1);
		add(cmdDiscreteLayoutColour, c);
		
		c = new CategoryLayoutConstraints();
		lblNameMaps.isExpanded = false;
		add(lblNameMaps, c);
		lblNameMaps.setParentObj(this);
		
		c = new CategoryLayoutConstraints("NAME MAPS", 1, 1, 0.05, 0.9, 1);
		add(cmbNameMaps, c);
		c = new CategoryLayoutConstraints("NAME MAPS", 2, 10, 0.05, 0.9, 1);
		add(paneNameMap, c);
		
		updateDisplay();
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfaceMapPanel.class.getResource("/mgui/resources/icons/continuous_cmap_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/continuous_cmap_20.png");
		return null;
	}
	
	public void setContinuousColourMap(ContinuousColourMap map){
		if (map == null) return;
		barContinuousColour.setMap((ContinuousColourMap)map.clone());
		this.txtContMapName.setText(map.name);
		cmbContinuousColourMaps.setSelectedItem(map);
		//updateDisplay();
	}
	
	public void showPanel(){
		updateDisplay();
	}
	
	public void updateDisplay(){
		updateContinuousColour();
		updateDiscreteColour();
		updateNameMaps();
		updateUI();
	}
	
	protected void updateContinuousColour(){
		//if (cmbContinuousColourMaps.getItemCount() == 0) return;
		ContinuousColourMap thisMap = (ContinuousColourMap)cmbContinuousColourMaps.getSelectedItem();
		cmbContinuousColourMaps.removeAllItems();
		
		//ArrayList<ColourMap> maps = displayPanel.colourMaps;
		ArrayList<ColourMap> maps = InterfaceEnvironment.getColourMaps();
		
		for (int i = 0; i < maps.size(); i++)
			if (maps.get(i) instanceof ContinuousColourMap)
				cmbContinuousColourMaps.addItem(maps.get(i));
		
		if (cmbContinuousColourMaps.getItemCount() == 0) return;
		
		if (thisMap != null)
			cmbContinuousColourMaps.setSelectedItem(thisMap);
		
		if (cmbContinuousColourMaps.getSelectedItem() == null)
			cmbContinuousColourMaps.setSelectedIndex(0);
		
		thisMap = (ContinuousColourMap)cmbContinuousColourMaps.getSelectedItem();
		//barContinuousColour.setMap(thisMap);
		setContinuousColourMap(thisMap);
		
	}
	
	protected void updateDiscreteColour(){
		DiscreteColourMap thisMap = (DiscreteColourMap)cmbDiscreteColourMaps.getSelectedItem();
		cmbDiscreteColourMaps.removeAllItems();
		
		updateCombo = false;
		ArrayList<DiscreteColourMap> maps = InterfaceEnvironment.getDiscreteColourMaps();
		for (int i = 0; i < maps.size(); i++)
			cmbDiscreteColourMaps.addItem(maps.get(i));
		
		if (cmbDiscreteColourMaps.getItemCount() == 0){
			
			chkDiscreteNameMap.setEnabled(false);
			updateCombo = true;
			return;
		}
		
		//updateCombo = false;
		chkDiscreteNameMap.setEnabled(true);
		
		if (thisMap != null)
			cmbDiscreteColourMaps.setSelectedItem(thisMap);
		
		if (cmbDiscreteColourMaps.getSelectedItem() == null)
			cmbDiscreteColourMaps.setSelectedIndex(0);
		
		thisMap = (DiscreteColourMap)cmbDiscreteColourMaps.getSelectedItem();
		tblDiscreteColour.setMap(thisMap);
		
		cmbDiscreteNameMaps.removeAllItems();
		ArrayList<NameMap> nameMaps = InterfaceEnvironment.getNameMaps();
		
		for (int i = 0; i < nameMaps.size(); i++)
			cmbDiscreteNameMaps.addItem(nameMaps.get(i));
		
		//name map?
		if (thisMap.nameMap != null){
			cmbDiscreteNameMaps.setEnabled(true);
			cmbDiscreteNameMaps.setSelectedItem(thisMap.nameMap);
			chkDiscreteNameMap.setSelected(true);
		}else{
			chkDiscreteNameMap.setSelected(false);
			cmbDiscreteNameMaps.setEnabled(false);
			}
		
		updateCombo = true;
		updateDiscreteMap();
	}
	
	protected void updateNameMaps(){
		updateCombo = false;
		NameMap thisMap = (NameMap)cmbNameMaps.getSelectedItem();
		cmbNameMaps.removeAllItems();
		
		ArrayList<NameMap> maps = InterfaceEnvironment.getNameMaps();
		
		for (int i = 0; i < maps.size(); i++)
			cmbNameMaps.addItem(maps.get(i));
		
		if (cmbNameMaps.getItemCount() == 0) return;
		
		if (thisMap != null)
			cmbNameMaps.setSelectedItem(thisMap);
		
		if (cmbNameMaps.getSelectedItem() == null)
			cmbNameMaps.setSelectedIndex(0);
		
		thisMap = (NameMap)cmbNameMaps.getSelectedItem();
		tblNameMap.setNameMap(thisMap);
		
		updateCombo = true;
		
	}
	
	void updateDiscreteMap(){
		if (cmbDiscreteColourMaps.getSelectedItem() == null) return;
		
		updateCombo = false;
		
		DiscreteColourMap map = (DiscreteColourMap)cmbDiscreteColourMaps.getSelectedItem();
		chkDiscreteNameMap.setSelected(map.hasNameMap());
		
		//if false, disable combo
		if (!chkDiscreteNameMap.isSelected()){
			cmbDiscreteNameMaps.setEnabled(false);
			tblDiscreteColour.setMap(map);
			updateCombo = true;
			return;
			}
		
		cmbDiscreteNameMaps.setEnabled(true);
		cmbDiscreteNameMaps.setSelectedItem(map.nameMap);
		tblDiscreteColour.setMap(map);
		updateCombo = true;
		return;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals("Colour Map Changed")){
			//update button colour
			cmdAnchorColour.setColour(barContinuousColour.getSelectedAnchor().colour.getColor());
			cmdAnchorColour.updateUI();
			txtAnchor.setText("" + barContinuousColour.selectedAnchor);
			txtAnchor.updateUI();
			txtAnchorValue.setText(barContinuousColour.getSelectedAnchor().value.toString("##0.000"));
			txtAnchorValue.updateUI();
			txtAnchorAlpha.setText(MguiFloat.getString(barContinuousColour.getSelectedAnchor().
													 colour.getAlpha(),
													 "##0.000"));
			txtAnchorAlpha.updateUI();
			return;
			}
		
		if (e.getActionCommand().equals(CMD_ADD_CONTINUOUS)){
			
			ContinuousColourMap map = ContinuousColourMap.getGreyScale();
			map.name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(),
					  "Colour map name:",
					  "New Colour Map",
					  JOptionPane.QUESTION_MESSAGE);
			if (map.name == null) return;
			InterfaceEnvironment.addColourMap(map);
			updateContinuousColour();
			setContinuousColourMap(map);
			return;
			}
		
		if (e.getActionCommand().equals(CMD_ANCHOR_COLOUR)){
			Color thisColour = Colours.getAwtColor(barContinuousColour.getSelectedAnchor().colour.getColor4f());
			thisColour = JColorChooser.showDialog(null, "Select Colour", thisColour);
			if (thisColour == null) return;
			barContinuousColour.getSelectedAnchor().colour.set(thisColour);
			barContinuousColour.getSelectedAnchor().colour.setAlpha(
													Float.valueOf(txtAnchorAlpha.getText()));
			barContinuousColour.update();
			return;
			}
		
		if (e.getActionCommand().equals(CMB_CONT_MAP)){
			if (cmbContinuousColourMaps.getSelectedItem() == null) return;
			setContinuousColourMap((ContinuousColourMap)cmbContinuousColourMaps.getSelectedItem());
			return;
			}
		
		if (e.getActionCommand().equals("Copy Continuous Map")){
			if (cmbContinuousColourMaps.getSelectedItem() == null) return;
			//ContinuousColourMap map = (ContinuousColourMap)cmbContinuousColourMaps.getSelectedItem();
			
			ContinuousColourMap map = barContinuousColour.map;
			
			map = (ContinuousColourMap)map.clone();
			String name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(),
													  "Colour map name:",
													  "New Colour Map",
													  JOptionPane.QUESTION_MESSAGE);
			if (name == null) return;
			map.setName(name);
			
			InterfaceEnvironment.addColourMap(map);
			updateContinuousColour();
			setContinuousColourMap(map);
			return;
			}
		
		if (e.getActionCommand().equals("Discrete Layout Colour")){
			
			Color c = JColorChooser.showDialog(InterfaceSession.getSessionFrame(), 
											   "Choose layout background colour", 
											   cmdDiscreteLayoutColour.getBackground());
			
			if (c == null) return;
			cmdDiscreteLayoutColour.setBackground(c);
			cmdDiscreteLayoutColour.updateUI();
			return;
		}
		
		if (e.getActionCommand().equals("Discrete Save Map")){
			
			DiscreteColourMap map = (DiscreteColourMap)cmbDiscreteColourMaps.getSelectedItem();
			if (map == null) return;
			
			JFileChooser fc = new JFileChooser();
			if (fc.showSaveDialog(InterfaceSession.getSessionFrame()) == JFileChooser.APPROVE_OPTION){
				try{
					XMLFunctions.writeToXML(fc.getSelectedFile(), map);
				}catch (XMLException ex){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "Error saving discrete colour map", 
																"Save Colour Map", 
																JOptionPane.ERROR_MESSAGE);
					return;
					}
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "Saved discrete colour map", 
															"Save Colour Map", 
															JOptionPane.INFORMATION_MESSAGE);
				}
			return;
			}
		
		if (e.getActionCommand().equals("Discrete Load Map")){
			
			JFileChooser fc = new JFileChooser();
			DiscreteColourMap map = null;
			if (fc.showOpenDialog(InterfaceSession.getSessionFrame()) == JFileChooser.APPROVE_OPTION){
				try{
					if (fc.getSelectedFile() == null) return;
					DiscreteColourMapLoader loader = new DiscreteColourMapLoader(fc.getSelectedFile());
					map = loader.loadMap();
					if (map == null){
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "Error loading discrete colour map", 
																	"Load Colour Map", 
																	JOptionPane.ERROR_MESSAGE);
						return;
						}
					InterfaceEnvironment.addColourMap(map);
					updateDisplay();
					
				}catch (Exception ex){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "Error loading discrete colour map", 
																"Load Colour Map", 
																JOptionPane.ERROR_MESSAGE);
					return;
					}
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "Loaded discrete colour map '" + map.getName() + "'", 
															"Load Colour Map", 
															JOptionPane.INFORMATION_MESSAGE);
				}
			return;
			}
		
		if (e.getActionCommand().equals("Discrete New Map")){
			
			String name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(),
													  "Colour map name:",
													  "New Colour Map",
													  JOptionPane.QUESTION_MESSAGE);
			
			String rows = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(),
													  "Number of items:",
													  "New Colour Map",
													  JOptionPane.QUESTION_MESSAGE);
			
			
			if (name != null && rows != null){
				DiscreteColourMap map = new DiscreteColourMap(name);
				int n = Integer.valueOf(rows);
				for (int i = 0; i < n; i++)
					map.setColour(i, new Colour4f(Color.white));
				InterfaceEnvironment.addColourMap(map);
				updateDisplay();
				cmbDiscreteColourMaps.setSelectedItem(map);
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "Created discrete colour map '" + map.getName() + "'", 
															"Create Colour Map", 
															JOptionPane.INFORMATION_MESSAGE);
				}
			
			return;
			
			}
		
		if (e.getActionCommand().equals("Discrete Map Changed")){
			if (!updateCombo) return;
			
			DiscreteColourMap map = (DiscreteColourMap)cmbDiscreteColourMaps.getSelectedItem();
			if (map == null) return;
			
			updateCombo = false;
			if (map.hasNameMap()) 
				chkDiscreteNameMap.setSelected(true);
			else
				chkDiscreteNameMap.setSelected(false);
			
			updateCombo = true;
			
			updateDiscreteMap();
			return;
		}
		
		if (e.getActionCommand().equals("Discrete Show Layout")){
			
			if (cmbDiscreteColourMaps.getSelectedItem() == null) return;
			DiscreteColourMap map = (DiscreteColourMap)cmbDiscreteColourMaps.getSelectedItem();
			
			DiscreteColourMapLayout cm_layout = new DiscreteColourMapLayout(map, cmdDiscreteLayoutColour.getBackground());
			InterfaceLayoutFrame frame = new InterfaceLayoutFrame(cm_layout, 
																  "Discrete Colour Map '" + map.getName() + "'", 
																  new Dimension(300, 400));
			frame.pack();
			frame.setVisible(true);
			return;
		}
		
		if (e.getActionCommand().equals("Has Discrete Name Map")){
			if (!updateCombo) return;
			if (cmbDiscreteColourMaps.getSelectedItem() == null) return;
			updateCombo = false;
			//DiscreteColourMap map = (DiscreteColourMap)cmbDiscreteColourMaps.getSelectedItem();
			
			if (cmbDiscreteNameMaps.getSelectedItem() == null){
				chkDiscreteNameMap.setSelected(false);
				updateCombo = true;
				return;
				}
			
			DiscreteColourMap map = (DiscreteColourMap)cmbDiscreteColourMaps.getSelectedItem();
			if (chkDiscreteNameMap.isSelected())
				map.nameMap = (NameMap)cmbDiscreteNameMaps.getSelectedItem();
			else
				map.nameMap = null;
			
			updateCombo = true;
			updateDiscreteMap();
			return;
			
			}
		
		if (e.getActionCommand().equals("Discrete Name Map Changed")){
			if (!updateCombo) return;
			updateCombo = false;
			
			NameMap nmap = (NameMap)cmbDiscreteNameMaps.getSelectedItem();
			DiscreteColourMap cmap = (DiscreteColourMap)cmbDiscreteColourMaps.getSelectedItem();
			cmap.setNameMap(nmap);
			
			updateCombo = true;
			
			updateDiscreteMap();
			return;
		}
		
		if (e.getActionCommand().equals("Invert Continuous Map")){
			if (cmbContinuousColourMaps.getSelectedItem() == null) return;
			//ContinuousColourMap map = (ContinuousColourMap)cmbContinuousColourMaps.getSelectedItem();
			
			ContinuousColourMap map = barContinuousColour.map;
			
			//invert each anchor value
			for (int i = 0; i < map.anchors.size(); i++){
				double val = map.anchors.get(i).value.getValue();
				//val = MathFunctions.normalize(map.mapMin, map.mapMax, val);
				val = 1.0 - val;
				//val = MathFunctions.unnormalize(map.mapMin, map.mapMax, val);
				map.anchors.get(i).value.setValue(val);
				}
			map.resort();
			barContinuousColour.repaint();
			
			//updateContinuousColour();
			return;
			}
		
		if (e.getActionCommand().equals("Update Continuous Map")){
			
			ContinuousColourMap map = (ContinuousColourMap)cmbContinuousColourMaps.getSelectedItem();
			ContinuousColourMap new_map = barContinuousColour.map;
			
			map.setFromMap(new_map);
			
		}
		
		if (e.getActionCommand().equals("Save Continuous Map")){
			JFileChooser fc = new JFileChooser();
			
			fc.setMultiSelectionEnabled(false);
			fc.setDialogTitle("Save continuous colour map as..");
			if (fc.showSaveDialog(InterfaceSession.getSessionFrame()) == JFileChooser.APPROVE_OPTION){
				File output = fc.getSelectedFile();
				if (!output.exists() || JOptionPane.showConfirmDialog(InterfaceSession.getSessionFrame(), 
																	  "Overwriting existing file?",
																	  "Warning",
																	  JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
					ContinuousColourMapWriter w = new ContinuousColourMapWriter(output);
					try{
						w.writeMap(barContinuousColour.map, "0.0000####");
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
													  "Colour map written to " + output.getAbsolutePath(),
													  "Write continuous colour map",
													  JOptionPane.INFORMATION_MESSAGE);
					}catch (IOException ex){
						//ex.printStackTrace();
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
								  "Error writing to " + output.getAbsolutePath(),
								  "Write continuous colour map",
								  JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		
		if (e.getActionCommand().equals("Change Name Map")){
			if (!updateCombo) return;
			updateCombo = false;
			if (cmbNameMaps.getSelectedItem() != null){
				NameMap nmap = (NameMap)cmbNameMaps.getSelectedItem();
				tblNameMap.setNameMap(nmap);
				}
			
			updateCombo = true;
			
			return;
			}
		
		if (e.getActionCommand().equals("Load Continuous Map")){
			
			JFileChooser fc = new JFileChooser();
			
			fc.setMultiSelectionEnabled(true);
			fc.setDialogTitle("Load continuous colour map");
			if (fc.showOpenDialog(InterfaceSession.getSessionFrame()) == JFileChooser.APPROVE_OPTION){
				
				File[] files = fc.getSelectedFiles();
				
				int count = 0;
				for (int i = 0; i < files.length; i++){
					String name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(),
								  "Name for map " + files[i].getName() + ":",
								  "Load continuous colour map",
								  JOptionPane.QUESTION_MESSAGE);
					try{
						ContinuousColourMapLoader loader = new ContinuousColourMapLoader(files[i]);
						ContinuousColourMap map = loader.loadMap();
						map.name = name;
						//InterfaceSession.getSessionFrame().addColourMap(map);
						InterfaceEnvironment.addColourMap(map);
						count++;
					}catch (IOException ex){
						ex.printStackTrace();
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
								  "Error reading from " + files[i].getAbsolutePath(),
								  "Load continuous colour map",
								  JOptionPane.ERROR_MESSAGE);
						}
					}
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
						  count + " colour maps loaded",
						  "Load continuous colour map",
						  JOptionPane.INFORMATION_MESSAGE);
				updateContinuousColour();
				}
			
			}
		
	}
	
	public String toString(){
		return "Maps Panel";
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER){
			if (e.getSource().equals(txtAnchorAlpha)){
				MguiFloat alpha = new MguiFloat(txtAnchorAlpha.getText());
				if (alpha.getValue() < 0 || alpha.getValue() > 1){
					txtAnchorAlpha.setText(MguiFloat.getString(barContinuousColour.
										   getSelectedAnchor().colour.getAlpha(),
										   "##0.000"));
					return;
					}
				barContinuousColour.getSelectedAnchor().colour.setAlpha(alpha.getFloat());
				barContinuousColour.update();
			}
		}
		
	}

	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}