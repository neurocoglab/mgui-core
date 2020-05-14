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

package mgui.interfaces;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.MouseInputAdapter;

import mgui.interfaces.attributes.InterfaceAttributePanel;
import mgui.interfaces.graphics.GraphicMouseListener;
import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.maps.Map;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeSet;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.queries.InterfaceShapeQuery.QueryMode;
import mgui.interfaces.shapes.selection.ShapeSelectionEvent;
import mgui.interfaces.shapes.selection.ShapeSelectionListener;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeListener;
import mgui.interfaces.tools.Tool;
import mgui.interfaces.tools.ToolEvent;
import mgui.interfaces.tools.ToolListener;
import mgui.interfaces.tools.graphics.Tool2D;
import mgui.interfaces.tools.graphics.Tool3D;
import mgui.interfaces.tools.shapes.ToolQueryShape3D;
import mgui.interfaces.tools.shapes.ToolSelectShape3D;

/********************************************************************
 * Provides an interface for creating and managing shape selection sets.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceSelectionPanel extends InterfacePanel 
									 implements ActionListener, 
									 			ShapeSelectionListener, 
									 			ItemListener, 
									 			GraphicMouseListener,
									 			ShapeListener,
									 			ToolListener{
	
	final static String NEW_SET = "<-New Set->";
	//final static String WORKING_SET = "<-Working Set->";
	
	CategoryTitle lblSelection = new CategoryTitle("SELECTION");
	JLabel lblCurrentModel = new JLabel("Current model");
	JComboBox cmbCurrentModel = new JComboBox();
	JLabel lblCurrentSelSet = new JLabel("Current selection set");
	JComboBox cmbCurrentSelSet = new JComboBox();
	JButton cmdCurrentSetAdd = new JButton("Add");
	JButton cmdCurrentSetRem = new JButton("Remove");
	JLabel lblCurrentSelSetName = new JLabel("Name:");
	JTextField txtCurrentSelSetName = new JTextField("-");
	JLabel lblSelShapes = new JLabel("Selected shapes:");
	JList lstSelShapes = new JList();
	DefaultListModel modSelShapes = new DefaultListModel();
	JScrollPane scrSelShapes;
	
	CategoryTitle lblMouse = new CategoryTitle("MOUSE");
	JCheckBox chkMouse2D = new JCheckBox(" 2D");
	JCheckBox chkMouse3D = new JCheckBox(" 3D");
	JCheckBox chkMouseAdd = new JCheckBox(" Add");
	JCheckBox chkMouseRem = new JCheckBox(" Remove");
	JCheckBox chkMouseClick = new JCheckBox(" Click"); 
	JCheckBox chkMouseBox = new JCheckBox(" Box");
	JButton cmdMouseStartStop = new JButton("Start");
	
	CategoryTitle lblActions = new CategoryTitle("ACTIONS");
	JComboBox cmbActionSource = new JComboBox();
	JCheckBox chkActionRecursive = new JCheckBox(" Recursive");
	JList lstActionSets = new JList();
	DefaultListModel modActionSets = new DefaultListModel();
	JScrollPane scrActionSets;
	JLabel lblActionOps = new JLabel(" Operations");
	JButton cmdOpIntersect = new JButton("Intersect");
	JButton cmdOpUnion = new JButton("Union");
	JButton cmdOpSubtract = new JButton("Subtract");
	JButton cmdOpCopy = new JButton("Copy");
	JLabel lblActionQuery = new JLabel(" Queries");
	JButton cmdQueryContains = new JButton("Contains");
	JButton cmdQueryDisjoint = new JButton("Disjoint");
	
	CategoryTitle lblAttributes = new CategoryTitle("ATTRIBUTES");
	JCheckBox chkAttrOverride = new JCheckBox(" Override");
	InterfaceAttributePanel lstAttributes = new InterfaceAttributePanel();
	JButton cmdAttrSetAll = new JButton("Set selection");
	
	CategoryTitle lblDisplay = new CategoryTitle("DISPLAY");
	JCheckBox chkDisplayExcl = new JCheckBox(" Show selected only");
	JCheckBox chkDisplayCenter = new JCheckBox(" Center on selection");
	JCheckBox chkDisplayZoom = new JCheckBox(" Zoom to selection");
	
	//objects
	ShapeModel3D current_model;
	ShapeSelectionSet current_set;
	
	//tools
	Tool2D previousTool2D;
	Tool3D previousTool3D;
	ToolSelectShape3D current_select_tool;
	ToolQueryShape3D current_query_tool;
	
	public InterfaceSelectionPanel(){
		if (InterfaceSession.isInit())
			init();
	}
	
	/*
	public InterfaceSelectionPanel(InterfaceDisplayPanel thisPanel){
		setDisplayPanel(thisPanel);
	}
	*/
	
	protected void init(){
		super._init();
		setLayout(new CategoryLayout(20, 5, 200, 10));
		CategoryLayoutConstraints c;
		
		//set alignment
		lblSelection.setHorizontalAlignment(JLabel.CENTER);
		lblMouse.setHorizontalAlignment(JLabel.CENTER);
		lblActions.setHorizontalAlignment(JLabel.CENTER);
		lblAttributes.setHorizontalAlignment(JLabel.CENTER);
		lblDisplay.setHorizontalAlignment(JLabel.CENTER);
		
		//set buttons
		cmdCurrentSetAdd.setActionCommand("Current Add");
		cmdCurrentSetRem.setActionCommand("Current Remove");
		//chkMouse2D.setActionCommand("Mouse Add");
		//chkMouse3D.setActionCommand("Mouse Add");
		chkMouseAdd.setActionCommand("Mouse Add");
		chkMouseRem.setActionCommand("Mouse Remove");
		chkMouseClick.setActionCommand("Mouse Click");
		chkMouseBox.setActionCommand("Mouse Box");
		cmdMouseStartStop.setActionCommand("Mouse Start/Stop");
		cmbActionSource.setActionCommand("Action Source");
		cmdOpIntersect.setActionCommand("Op Intersect");
		cmdOpUnion.setActionCommand("Op Union");
		cmdOpSubtract.setActionCommand("Op Subtract");
		cmdOpCopy.setActionCommand("Op Copy");
		cmdQueryContains.setActionCommand("Query Contains");
		cmdQueryDisjoint.setActionCommand("Query Disjoint");
		cmdAttrSetAll.setActionCommand("Attr Set");
		chkDisplayExcl.setActionCommand("Display Exclude");
		chkDisplayCenter.setActionCommand("Display Center");
		chkDisplayZoom.setActionCommand("Display Zoom");
		
		cmdCurrentSetAdd.addActionListener(this);
		cmdCurrentSetRem.addActionListener(this);
		chkMouseAdd.addActionListener(this);
		chkMouseRem.addActionListener(this);
		chkMouseClick.addActionListener(this);
		chkMouseBox.addActionListener(this);
		cmdMouseStartStop.addActionListener(this);
		cmbActionSource.addActionListener(this);
		cmdOpIntersect.addActionListener(this);
		cmdOpUnion.addActionListener(this);
		cmdOpSubtract.addActionListener(this);
		cmdOpCopy.addActionListener(this);
		cmdQueryContains.addActionListener(this);
		cmdQueryDisjoint.addActionListener(this);
		cmdAttrSetAll.addActionListener(this);
		chkDisplayExcl.addActionListener(this);
		chkDisplayCenter.addActionListener(this);
		chkDisplayZoom.addActionListener(this);
		
		//set defaults
		chkMouseAdd.setSelected(true);
		chkMouseClick.setSelected(true);
		chkActionRecursive.setSelected(false);
		
		//set lists
		lstSelShapes.setModel(modSelShapes);
		scrSelShapes = new JScrollPane(lstSelShapes);
		lstActionSets.setModel(modActionSets);
		scrActionSets = new JScrollPane(lstActionSets);
		
		//set combos
		cmbActionSource.addItem("Selection Sets");
		cmbActionSource.addItem("Shape Sets");
		cmbActionSource.setSelectedItem("Selection Sets");
		
		cmbCurrentSelSet.setActionCommand("Current Set Changed");
		cmbCurrentSelSet.addActionListener(this);
		txtCurrentSelSetName.setActionCommand("Current Name Changed");
		txtCurrentSelSetName.addActionListener(this);
		
		fillModelCombo();
		
		c = new CategoryLayoutConstraints();
		add(lblSelection, c);
		lblSelection.setParentObj(this);
		c = new CategoryLayoutConstraints("SELECTION", 1, 1, 0.05, 0.9, 1);
		add(lblCurrentModel, c);
		c = new CategoryLayoutConstraints("SELECTION", 2, 2, 0.05, 0.9, 1);
		add(cmbCurrentModel, c);
		c = new CategoryLayoutConstraints("SELECTION", 3, 3, 0.05, 0.9, 1);
		add(lblCurrentSelSet, c);
		c = new CategoryLayoutConstraints("SELECTION", 4, 4, 0.05, 0.9, 1);
		add(cmbCurrentSelSet, c);
		c = new CategoryLayoutConstraints("SELECTION", 5, 5, 0.05, 0.4, 1);
		add(lblCurrentSelSetName, c);
		c = new CategoryLayoutConstraints("SELECTION", 5, 5, 0.55, 0.4, 1);
		add(txtCurrentSelSetName, c);
		c = new CategoryLayoutConstraints("SELECTION", 6, 6, 0.05, 0.4, 1);
		add(cmdCurrentSetAdd, c);
		c = new CategoryLayoutConstraints("SELECTION", 6, 6, 0.55, 0.4, 1);
		add(cmdCurrentSetRem, c);
		c = new CategoryLayoutConstraints("SELECTION", 7, 7, 0.05, 0.9, 1);
		add(lblSelShapes, c);
		c = new CategoryLayoutConstraints("SELECTION", 8, 11, 0.05, 0.9, 1);
		add(scrSelShapes, c);
		
		c = new CategoryLayoutConstraints();
		add(lblMouse, c);
		lblMouse.setParentObj(this);
		c = new CategoryLayoutConstraints("MOUSE", 1, 1, 0.05, 0.4, 1);
		add(chkMouse2D, c);
		c = new CategoryLayoutConstraints("MOUSE", 1, 1, 0.55, 0.4, 1);
		add(chkMouse3D, c);
		c = new CategoryLayoutConstraints("MOUSE", 2, 2, 0.05, 0.4, 1);
		add(chkMouseAdd, c);
		c = new CategoryLayoutConstraints("MOUSE", 2, 2, 0.55, 0.4, 1);
		add(chkMouseRem, c);
		c = new CategoryLayoutConstraints("MOUSE", 3, 3, 0.05, 0.4, 1);
		add(chkMouseClick, c);
		c = new CategoryLayoutConstraints("MOUSE", 3, 3, 0.55, 0.4, 1);
		add(chkMouseBox, c);
		c = new CategoryLayoutConstraints("MOUSE", 4, 5, 0.2, 0.6, 1);
		add(cmdMouseStartStop, c);
		
		c = new CategoryLayoutConstraints();
		add(lblActions, c);
		lblActions.setParentObj(this);
		c = new CategoryLayoutConstraints("ACTIONS", 1, 1, 0.05, 0.9, 1);
		add(cmbActionSource, c);
		c = new CategoryLayoutConstraints("ACTIONS", 2, 2, 0.05, 0.9, 1);
		add(chkActionRecursive, c);
		c = new CategoryLayoutConstraints("ACTIONS", 3, 6, 0.05, 0.9, 1);
		add(scrActionSets, c);
		c = new CategoryLayoutConstraints("ACTIONS", 7, 7, 0.05, 0.9, 1);
		add(lblActionOps, c);
		c = new CategoryLayoutConstraints("ACTIONS", 8, 8, 0.05, 0.4, 1);
		add(cmdOpIntersect, c);
		c = new CategoryLayoutConstraints("ACTIONS", 8, 8, 0.55, 0.4, 1);
		add(cmdOpUnion, c);
		c = new CategoryLayoutConstraints("ACTIONS", 9, 9, 0.05, 0.4, 1);
		add(cmdOpSubtract, c);
		c = new CategoryLayoutConstraints("ACTIONS", 9, 9, 0.55, 0.4, 1);
		add(cmdOpCopy, c);
		c = new CategoryLayoutConstraints("ACTIONS", 10, 10, 0.05, 0.9, 1);
		add(lblActionQuery, c);
		c = new CategoryLayoutConstraints("ACTIONS", 11, 11, 0.05, 0.4, 1);
		add(cmdQueryContains, c);
		c = new CategoryLayoutConstraints("ACTIONS", 11, 11, 0.55, 0.4, 1);
		add(cmdQueryDisjoint, c);
		
		c = new CategoryLayoutConstraints();
		add(lblAttributes, c);
		lblAttributes.setParentObj(this);
		c = new CategoryLayoutConstraints("ATTRIBUTES", 1, 1, 0.05, 0.9, 1);
		add(chkAttrOverride, c);
		c = new CategoryLayoutConstraints("ATTRIBUTES", 2, 5, 0.05, 0.9, 1);
		add(lstAttributes, c);
		c = new CategoryLayoutConstraints("ATTRIBUTES", 6, 6, 0.05, 0.9, 1);
		add(cmdAttrSetAll, c);
		
		c = new CategoryLayoutConstraints();
		add(lblDisplay, c);
		lblDisplay.setParentObj(this);
		c = new CategoryLayoutConstraints("DISPLAY", 1, 1, 0.05, 0.9, 1);
		add(chkDisplayExcl, c);
		c = new CategoryLayoutConstraints("DISPLAY", 2, 2, 0.05, 0.9, 1);
		add(chkDisplayCenter, c);
		c = new CategoryLayoutConstraints("DISPLAY", 3, 3, 0.05, 0.9, 1);
		add(chkDisplayZoom, c);
		
		
	}
	
	protected void fillModelCombo(){
		cmbCurrentModel.removeAllItems();
		//if (displayPanel == null) return;
		ArrayList<ShapeModel3D> models = InterfaceSession.getWorkspace().getShapeModels();
		
		for (int i = 0; i < models.size(); i++)
			cmbCurrentModel.addItem(models.get(i));
		
		setCurrentModel(InterfaceSession.getDisplayPanel().getCurrentShapeModel());
	}
	
	protected void setCurrentModel(ShapeModel3D model){
		if (model != null && model == current_model) return;
		
		cmbCurrentModel.setSelectedItem(model);
		
		if (cmbCurrentModel.getSelectedItem() == null){
			if (current_model != null) 
				cmbCurrentModel.setSelectedItem(current_model);
			else if (cmbCurrentModel.getItemCount() > 0)
				cmbCurrentModel.setSelectedIndex(0);
			current_model = (ShapeModel3D)cmbCurrentModel.getSelectedItem();
			fillSelectionSetCombo();
			return;
			}
		
		current_model = model;
		cmbCurrentModel.setSelectedItem(current_model);
		
		fillSelectionSetCombo();
	}
	
	protected void fillSelectionSetCombo(){
		cmbCurrentSelSet.removeAllItems();
		cmbCurrentSelSet.addItem(NEW_SET);
		//cmbCurrentSelSet.addItem(WORKING_SET);
		ArrayList<ShapeSelectionSet> selections = current_model.getSelectionSets();
		if (current_model != null)
			for (int i = 0; i < selections.size(); i++)
				cmbCurrentSelSet.addItem(selections.get(i));
		
		updateDisplay();
	}
	
	protected void setCurrentSet(ShapeSelectionSet set){
		if (set == current_set) return;
		
		if (current_set != null)
			current_set.removeSelectionListener(this);
		
		cmbCurrentSelSet.setSelectedItem(set);
		
		if (cmbCurrentSelSet.getSelectedItem() == null){
			if (current_set != null)
				cmbCurrentSelSet.setSelectedItem(current_set);
			else if (cmbCurrentSelSet.getItemCount() > 0)
				cmbCurrentSelSet.setSelectedIndex(0);
			else
				cmbCurrentSelSet.setSelectedItem(NEW_SET);
			current_set = null;
			if (cmbCurrentSelSet.getSelectedItem() instanceof ShapeSelectionSet)
				current_set = (ShapeSelectionSet)cmbCurrentSelSet.getSelectedItem();
			return;
			}
		
		current_set = set;
		txtCurrentSelSetName.setText(current_set.getName());
		InterfaceSession.getDisplayPanel().setCurrentSelection(current_set);
		current_set.addSelectionListener(this);
		
		updateDisplay();
	}
	
	public void showPanel(){
		fillModelCombo();
		updateButtons();
	}
	
	public void updateDisplay(){
		//fill selection list
		fillSelectionList();
		
		//fill selection attributes
		
		updateButtons();
		updateUI();
	}
	
	protected void updateButtons(){
		boolean has_selection = cmbCurrentSelSet.getSelectedItem() != null;
		boolean is_new = cmbCurrentSelSet.getSelectedItem().equals(NEW_SET);
		boolean has_valid_name = txtCurrentSelSetName.getText().length() > 0 && 
								!txtCurrentSelSetName.getText().equals("-");
		//cmdCurrentSetAdd.setEnabled(has_selection && has_valid_name && (is_new || isWorkingSet()));
		if (has_selection && has_valid_name && (is_new || isWorkingSet())){
			cmdCurrentSetAdd.setEnabled(true);
			cmdCurrentSetAdd.setText("Add");
			cmdCurrentSetAdd.setActionCommand("Current Add");
		}else if (current_set != null && has_valid_name && !isWorkingSet()){
			cmdCurrentSetAdd.setEnabled(true);
			cmdCurrentSetAdd.setText("Update");
			cmdCurrentSetAdd.setActionCommand("Current Update");
		}else{
			cmdCurrentSetAdd.setEnabled(false);
			cmdCurrentSetAdd.setText("Add");
			cmdCurrentSetAdd.setActionCommand("Current Add");
			}
		
		cmdCurrentSetRem.setEnabled(current_set != null);
		if (isWorkingSet()){
			cmdCurrentSetRem.setText("Clear");
			cmdCurrentSetRem.setActionCommand("Clear Working Set");
		}else{
			cmdCurrentSetRem.setText("Remove");
			cmdCurrentSetRem.setActionCommand("Current Remove");
			}
	}
	
	protected void fillSelectionList(){
		modSelShapes.removeAllElements();
		if (current_set == null) return;
		
		Iterator<InterfaceShape> itr = current_set.getIterator();
		while (itr.hasNext())
			modSelShapes.addElement(itr.next());
	}
	
	protected void fillSelectionAttributes(){
		if (current_set == null) return;
		lstAttributes.setAttributes(current_set.getAttributes());
	}
	
	protected void fillActionList(){
		modActionSets.removeAllElements();
		if (current_model == null) return;
		
		if (cmbActionSource.getSelectedItem().equals("Selection Sets")){
			//add all but current
			for (int i = 0; i < current_model.selections.size(); i++)
				if (!current_model.selections.get(i).equals(current_set))
					modActionSets.addElement(current_model.selections.get(i));
		}else if (current_model.getModelSet() != null){
			ShapeSet3DInt set = current_model.getModelSet().getShapeType(new ShapeSet3DInt());
			modActionSets.addElement(current_model.getModelSet());
			if (set != null)
				for (int i = 0; i < set.members.size(); i++)
					modActionSets.addElement(set.members.get(i));
			}
		updateDisplay();
	}
	
	protected void addSelectionSet(ShapeSelectionSet set){
		if (current_model == null) return;
		current_model.addSelectionSet(set);
	}
	
	public void shapeUpdated(ShapeEvent e) {
		
	}

	protected boolean isWorkingSet(){
		if (cmbCurrentSelSet.getSelectedItem() == null) return false;
		return cmbCurrentSelSet.getSelectedItem() instanceof ShapeSelectionSet &&
			   ((ShapeSelectionSet)cmbCurrentSelSet.getSelectedItem()).getName().endsWith(".working");
	}
	
	public void actionPerformed(ActionEvent e) {
		
		String cmd = e.getActionCommand();
		
		if (cmd.startsWith("Current Set")){
			
			if (cmbCurrentSelSet.getSelectedItem() == null) return;
			
			if (cmd.endsWith("Changed")){
				if (cmbCurrentSelSet.getSelectedItem().equals(NEW_SET)){
					current_set = null;
					txtCurrentSelSetName.setText("-");
					showPanel();
					return;
					}
				if (isWorkingSet()){
					current_set = current_model.getWorkingSelection();
					txtCurrentSelSetName.setText("-");
					showPanel();
					return;
					}
				
				current_set = (ShapeSelectionSet)cmbCurrentSelSet.getSelectedItem();
				txtCurrentSelSetName.setText(current_set.getName());
				showPanel();
				return;
				}
			
			return;
			}
		
		if (cmd.equals("Current Name Changed")){
			updateButtons();
			return;
			}
		
		if (cmd.startsWith("Current")){
			if (cmd.endsWith("Add")){
				if (cmbCurrentSelSet.getSelectedItem().equals(NEW_SET)){
					
					String name = txtCurrentSelSetName.getText();
					if (name.length() == 0 || name.equals("-")) return;
					
					ShapeSelectionSet new_set = new ShapeSelectionSet(name);
					current_model.addSelectionSet(new_set);
					fillSelectionSetCombo();
					setCurrentSet(new_set);
					
					return;
					}
				
				if (isWorkingSet()){
					
					String name = txtCurrentSelSetName.getText();
					if (current_set == null || name.length() == 0 || name.equals("-")) return;
					ShapeSelectionSet new_set = new ShapeSelectionSet(current_set);
					new_set.setName(txtCurrentSelSetName.getText());
					current_model.addSelectionSet(new_set);
					fillSelectionSetCombo();
					
					setCurrentSet(new_set);
					return;
					}
				
				return;
				}
			
			
			}
		
		if (cmd.startsWith("Mouse")){
			if (cmd.endsWith("Add")){
				chkMouseRem.setSelected(!chkMouseAdd.isSelected());
				if (cmdMouseStartStop.getText().equals("Stop"))
					setSelectionTool3D();
					
				return;
				}
			if (cmd.endsWith("Remove")){
				chkMouseAdd.setSelected(!chkMouseRem.isSelected());
				if (cmdMouseStartStop.getText().equals("Stop"))
					setSelectionTool3D();
				
				return;
				}
			if (cmd.endsWith("Click")){
				chkMouseBox.setSelected(!chkMouseClick.isSelected());
				return;
				}
			if (cmd.endsWith("Box")){
				chkMouseClick.setSelected(!chkMouseBox.isSelected());
				return;
				}
			if (cmd.endsWith("Start/Stop")){
				//TODO: activate select tool
				if (chkMouse3D.isSelected())
					if (cmdMouseStartStop.getText().equals("Start")){
						if (current_set == null){
							return;
							//InterfaceSession.log("Using working sel");
							//setCurrentSet(current_model.getWorkingSelection());
							//addSelectionSet(current_set);
							}
						previousTool3D = InterfaceSession.getDisplayPanel().getCurrentTool3D();
						setSelectionTool3D();
						InterfaceSession.log("Starting selection..");
						cmdMouseStartStop.setText("Stop");
						return;
					}else{
						InterfaceSession.log("Stopping selection..");
						current_set.getModel().clearTempShapes();
						InterfaceSession.getDisplayPanel().setCurrentTool(previousTool3D);
						cmdMouseStartStop.setText("Start");
						return;
						}
				}
			}
		
		if (cmd.startsWith("Action")){
			if (cmd.endsWith("Source")){
				fillActionList();
				return;
				}
			
			
			}
		
		if (cmd.startsWith("Op")){
			Object[] selected = lstActionSets.getSelectedValues();
			boolean recursive = chkActionRecursive.isSelected();
			
			if (selected.length == 0) return;
			if (current_set == null){
				current_set = new ShapeSelectionSet();
				addSelectionSet(current_set);
				}
			
			if (cmd.endsWith("Union")){
				//set current set to union with selected sets
				for (int i = 0; i < selected.length; i++)
					if (selected[i] instanceof ShapeSet)
				current_set.addUnion((ShapeSet)selected[i], recursive);
				
				}
			
			updateDisplay();
			return;
			}
		
		
	}
	
	public void toolStateChanged(Tool tool){
		
		
	}

	protected void setSelectionTool3D(){
		if (current_set.getModel() != null)
			current_set.getModel().clearTempShapes();
		current_select_tool = new ToolSelectShape3D(current_set);
		if (chkMouseRem.isSelected())
			current_select_tool.setMode(ToolSelectShape3D.DESELECT);
		InterfaceSession.getDisplayPanel().setCurrentTool(current_select_tool);
	}
	
	protected void setQueryTool3D(){
		current_set.getModel().clearTempShapes();
		if (current_query_tool != null)
			current_query_tool.removeListener(this);
		current_query_tool.addListener(this);
		current_query_tool = new ToolQueryShape3D(getQueryMode(), null);
		
		InterfaceSession.getDisplayPanel().setCurrentTool(current_query_tool);
	}
	
	public QueryMode getQueryMode(){
		return QueryMode.SingleObject;
	}
	
	public void shapeSelectionChanged(ShapeSelectionEvent e) {
		fillSelectionList();
		updateDisplay();
	}

	public void itemStateChanged(ItemEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void setMap(Map mt) {
		// TODO Auto-generated method stub

	}

	public boolean isShape() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setParentWindow(InterfaceGraphic thisParent) {
		// TODO Auto-generated method stub

	}

	public MouseInputAdapter getMouseListener() {
		// TODO Auto-generated method stub
		return null;
	}

	public MouseWheelListener getMouseWheelListener() {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString(){
		return "Selection Set Panel";
	}
	
	public void windowUpdated(InterfaceGraphic g) {
		// TODO Auto-generated method stub
		
	}

	public void toolStateChanged(ToolEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public void toolDeactivated(ToolEvent e){
		
	}

	
	
}