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

package mgui.interfaces.shapes.selection;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;

import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.graphics.GraphicMouseListener;
import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.graphics.InterfaceGraphic2D;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.maps.Map;
import mgui.interfaces.maps.Map2D;
import mgui.interfaces.shapes.Shape2DInt;
import mgui.interfaces.shapes.ShapeModel3D;

/***************************************
 * Interface panel providing user interaction with selection sets.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * @see mgui.interfaces.shapes.selection.ShapeSelectionSet ShapeSelectionSet
 *
 */
@SuppressWarnings("serial")
public class InterfaceSelectionSet extends InterfacePanel implements ItemListener,
																	 ShapeSelectionListener,
																	 GraphicMouseListener{

	//public InterfaceDisplayPanel displayPanel;
	
	JLabel lblHeader = new JLabel("SELECTION SET PANEL");
	JLabel lblModels = new JLabel("Current Model:");
	JComboBox cmbModels = new JComboBox();
	JComboBox cmbSetList = new JComboBox();
	JLabel lblSetList = new JLabel("Current Selection Set:");
	JCheckBox chkSetExclusive = new JCheckBox("Set exclusive");
	JLabel lblSelectedShape = new JLabel("Selected shape:");
	JList lstSelectedShape = new JList();
	DefaultListModel modSelectedShape = new DefaultListModel();
	JScrollPane scrSelectedShape; // = new JScrollPane(); 
	
	
	boolean blnHandleItemChange = true;
	boolean isSelecting = false;
	private ShapeMouseListener thisAdapter;
	private Map2D theMap;
	public int interfaceType = 0;
	public InterfaceGraphic2D thisParent;
	private InterfaceSelectionSet thisObj = this;
	ShapeModel3D currentModel;
	ShapeSelectionSet currentSet;
	
	public InterfaceSelectionSet(){
		//super();
		if (InterfaceSession.isInit())
			init();
	}
	
	/*
	public InterfaceSelectionSet(InterfaceDisplayPanel thisPanel){
		super();
		displayPanel = thisPanel;
		init();
	}
	*/
	
	@Override
	protected void init(){
		thisAdapter = new ShapeMouseListener();
		
		fillModels();
		
		lblHeader.setHorizontalAlignment(SwingConstants.CENTER);
		
		chkSetExclusive.addItemListener(this);
		cmbSetList.addItemListener(this);
		cmbModels.addItemListener(this);
		
		lstSelectedShape.setModel(modSelectedShape);
		scrSelectedShape = new JScrollPane(lstSelectedShape);
		
		//if (displayPanel != null)
			updateSelection();
		
		setLayout(new LineLayout(20, 5, 200));
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0, 1, 1);
		
		c = new LineLayoutConstraints(1, 1, 0, 1, 1);
		add(lblHeader, c);
		c = new LineLayoutConstraints(2, 2, 0, 1, 1);
		add(lblModels, c);
		c = new LineLayoutConstraints(3, 3, 0, 1, 1);
		add(cmbModels, c);
		c = new LineLayoutConstraints(4, 4, 0, 1, 1);
		add(lblSetList, c);
		c = new LineLayoutConstraints(5, 5, 0, 1, 1);
		add(cmbSetList, c);
		c = new LineLayoutConstraints(6, 6, 0, 1, 1);
		add(chkSetExclusive, c);
		c = new LineLayoutConstraints(7, 7, 0, 1, 1);
		add(lblSelectedShape, c);
		c = new LineLayoutConstraints(8, 11, 0, 1, 1);
		add(scrSelectedShape, c);
		
	}
	
	protected void fillModels(){
		blnHandleItemChange = false;
		cmbModels.removeAllItems();
		ArrayList<ShapeModel3D> models = InterfaceSession.getWorkspace().getShapeModels();
		if (models.size() == 0){
			currentModel = null;
			currentSet = null;
			blnHandleItemChange = true;
			return;
			}
			
		for (int i = 0; i < models.size(); i++)
			cmbModels.addItem(models.get(i));
		if (currentModel == null){
			currentModel = (ShapeModel3D)cmbModels.getSelectedItem();
			fillSelectionSets();
			updateControls();
		}else{
			cmbModels.setSelectedItem(currentModel);
			}
		blnHandleItemChange = true;
	}
	
	protected void fillSelectionSets(){
		blnHandleItemChange = false;
		cmbSetList.removeAllItems();
		if (currentModel == null) return;
		
		ArrayList<ShapeSelectionSet> selections = currentModel.selections;
		if (selections == null){
			currentSet = null;
			updateControls();
			blnHandleItemChange = true;
			return;
		}
		
		for (int i = 0; i < selections.size(); i++)
			cmbSetList.addItem(selections.get(i));
		
		if (currentSet == null){
			currentSet = (ShapeSelectionSet)cmbSetList.getSelectedItem();
			updateControls();
		}else{
			cmbSetList.setSelectedItem(currentSet);
			}
		blnHandleItemChange = true;
	}
	
	protected void updateControls(){
		
	}
	
	@Override
	public void updateDisplay(){
		updateUI();
	}
	
	public void itemStateChanged(ItemEvent e){
		
		if (e.getItemSelectable() == chkSetExclusive){
			if (currentModel == null || currentSet == null) return;
			updateSelection();
			InterfaceSession.getDisplayPanel().updateDisplays();
			return;
		}
		
		if (e.getSource().equals(cmbSetList) && blnHandleItemChange){
			if (e.getItem().equals(currentSet)) return;
			blnHandleItemChange = false;
			currentSet = (ShapeSelectionSet)e.getItem();
			updateSelection();
			blnHandleItemChange = true;
		}
		
		if (e.getSource().equals(cmbModels) && blnHandleItemChange){
			if (e.getItem().equals(currentModel)) return;
			blnHandleItemChange = false;
			currentModel = (ShapeModel3D)e.getItem();
			fillSelectionSets();
			if (chkSetExclusive.isSelected())
				updateSelection();
			blnHandleItemChange = true;
			return;
		}
		
	}
	
	public void updateSelection(){
		if (currentSet == null) return;
		currentSet.sort();
		currentModel.setExclusionFilter(currentSet, false);
		currentModel.setExcludeToSelection(chkSetExclusive.isSelected());
		InterfaceSession.getDisplayPanel().updateDisplays();
	}
	
	public void shapeSelectionChanged(ShapeSelectionEvent e){
		if (!blnHandleItemChange) return;
		blnHandleItemChange = false;
		fillSelectionSets();
		updateDisplay();
		updateSelection();
		blnHandleItemChange = true;
	}
	
	@Override
	public String toString(){
		return "Selection Set Panel";
	}
	
	class ShapeMouseListener extends MouseInputAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			
			if (!thisObj.isVisible()) return;
			Shape2DInt pickShape;
			//int j;
			if (!isSelecting) return;
			thisParent = (InterfaceGraphic2D)e.getSource();
				pickShape = thisParent.pickShape(e.getPoint(), 10);
				InterfaceSession.log("Pick selection...");
				ShapeModel3D model = thisParent.getShapeModel();
				modSelectedShape.removeAllElements();
				if (pickShape != null){
					//set list from this shape's selection sets
					//for (int i = 0; i < displayPanel.selectionSets.size(); i++)
					//	if (displayPanel.selectionSets.get(i).hasMember(pickShape)){
					//		j = modSelectedShape.indexOf(displayPanel.selectionSets.get(i));
					//		if (j < 0)
					//			modSelectedShape.addElement(displayPanel.selectionSets.get(i));
					//		}
					//ArrayList<ShapeSelectionSet> sets = displayPanel.getSelectionSets(pickShape, model);
					ArrayList<ShapeSelectionSet> sets = model.getSelectionSets(pickShape);
					
					for (int i = 0; i < sets.size(); i++)
						modSelectedShape.addElement(sets.get(i));
					lstSelectedShape.updateUI();
					
					updateDisplay();
					}
			}
		
	}
	
	public void setParentWindow(InterfaceGraphic thisParent){
	}
	
	public MouseInputAdapter getMouseListener(){
		return thisAdapter;
	}
	
	public MouseWheelListener getMouseWheelListener(){
		return null;
	}
	
	public boolean isShape(){
		return false;
	}
	
	public void setMap(Map m){
	}
	
	public void windowUpdated(InterfaceGraphic g) {
		// TODO Auto-generated method stub
		
	}

	
}