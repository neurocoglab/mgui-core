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

package mgui.interfaces.attributes;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JColorChooser;
import javax.swing.JScrollPane;

import mgui.interfaces.InterfacePanel;
import mgui.interfaces.attributes.table.AttributeCellEditor;
import mgui.interfaces.attributes.table.AttributeCellRenderer;
import mgui.interfaces.attributes.table.AttributeTableModel;
import mgui.interfaces.attributes.table.InterfaceAttributeTable;
import mgui.interfaces.attributes.tree.AttributeButton;

/*********************************************************
 * Panel to display and modify a given AttributeList.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceAttributePanel extends InterfacePanel implements ActionListener {

	public AttributeTableModel attrModel;
	public InterfaceAttributeTable attrTable;
	public JScrollPane scrollPane;
	private JColorChooser colourDlg = new JColorChooser();
	
	public InterfaceAttributePanel(){
		super();
		attrModel = new AttributeTableModel();
		init();
	}
	
	public InterfaceAttributePanel(AttributeList thisList){
		super();
		attrModel = new AttributeTableModel(thisList);
		init();
	}
	
	public InterfaceAttributePanel(AttributeObject object){
		super();
		attrModel = new AttributeTableModel(object.getAttributes());
		init();
	}
	
	@Override
	protected void init(){
		attrTable = new InterfaceAttributeTable(attrModel);
		attrTable.setAttributePanel(this);
		scrollPane = new JScrollPane(attrTable);
		this.setLayout(new GridLayout(1, 1));
		this.add(scrollPane);
		attrTable.setDefaultRenderer(Object.class, new AttributeCellRenderer());
		attrTable.setDefaultEditor(Object.class, new AttributeCellEditor());
	}
	
	public void actionPerformed(ActionEvent e){
		//Button pressed?
		if (e.getActionCommand().compareTo("Change Colour") == 0){
			Attribute<?> thisAttr = ((AttributeButton)e.getSource()).getAttribute();
			thisAttr.setValue(JColorChooser.showDialog(this, thisAttr.name, (Color)thisAttr.value));
		}
		
	}
	
	public AttributeList getAttributes(){
		AttributeTableModel model = (AttributeTableModel)attrTable.getModel();
		if (model == null) return null;
		return model.attributes;
	}
	
	public Attribute<?> getAttribute(String name) {	
		AttributeTableModel model = (AttributeTableModel)attrTable.getModel();
		if (model == null) return null;
		return model.attributes.getAttribute(name);
	}
	
	@Override
	public void setAttributes(AttributeList a){
		if (a == null){
			attrTable.setAttributes(new AttributeList());
			return;
		}else{
			attrTable.setAttributes(a);
			}
		((AttributeTableModel)attrTable.getModel()).fireTableDataChanged();
		//attrTable.updateUI();
	}
	
	public void setEditable(boolean val){
		((AttributeTableModel)attrTable.getModel()).isEditable = val;
	}
	
}