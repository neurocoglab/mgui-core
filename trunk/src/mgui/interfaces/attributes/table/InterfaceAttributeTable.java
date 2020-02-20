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

package mgui.interfaces.attributes.table;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.InterfaceAttributePanel;



/******************************
 * An extension of JTable to display an AttributeList in tabular format. Uses the model
 * class AttributeTableModel. 
 * 
 * @author AndrewR
 * @see AttributeTableModel
 */

public class InterfaceAttributeTable extends JTable {

	public InterfaceAttributePanel parentPanel;
	AttributeCellRenderer renderer = new AttributeCellRenderer();
	AttributeCellEditor editor = new AttributeCellEditor();
	AttributeTableModel model;
	
	public InterfaceAttributeTable(){
		super();
	}
	
	public InterfaceAttributeTable(AttributeList list){
		super();
		setAttributes(list);
	}
	
	public InterfaceAttributeTable(AttributeTableModel thisModel){
		super(thisModel);
	}
	
	public InterfaceAttributePanel getAttributePanel(){
		return parentPanel;
	}
	
	public void setAttributePanel(InterfaceAttributePanel newParent){
		parentPanel = newParent;
	}
	
	 @Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		 return renderer;
	 }
	 
	 @Override
	public TableCellEditor getCellEditor(int row, int column) {
		 return editor;
	 }
	 
	 @Override
	public TableCellEditor getCellEditor() {
		 return editor;
	 }
	 
	 @Override
	public TableCellEditor getDefaultEditor(Class<?> columnClass) {
		 return editor;
	 }
	 
	 @Override
	public TableCellRenderer getDefaultRenderer(Class<?> columnClass) {
		 return renderer;
	 }
	
	public void setAttributes(AttributeList list){
		if (model == null){
			model = new AttributeTableModel(list);
			this.setModel(model);
		}else{
			model.setAttributes(list);
			}
		
	}
	
	public Attribute getAttribute(int row){
		if (this.getModel() == null || !(this.getModel() instanceof AttributeTableModel)) return null;
		return ((AttributeTableModel)this.getModel()).getAttribute(row);
	}
	
}