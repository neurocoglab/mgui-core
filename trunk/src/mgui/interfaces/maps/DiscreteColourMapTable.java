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

package mgui.interfaces.maps;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.AbstractCellEditor;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import mgui.interfaces.gui.ColourButton;
import mgui.numbers.MguiInteger;
import mgui.util.Colour;
import mgui.util.Colour4f;


/**************************************
 * Table to display and update a discrete colour map.
 * 
 * Note this only works for indexes of type arInteger and colours of type Colour4f.
 * 
 * @author Andrew Reid
 *
 */
public class DiscreteColourMapTable extends JTable {

	protected TableModel model;
	
	public DiscreteColourMapTable(){
		
	}
	
	public DiscreteColourMapTable(DiscreteColourMap map){
		setMap(map);
	}
	
	public void setMap(DiscreteColourMap map){
		if (map == null) return;
		if (model == null){
			model = new TableModel(map);
			setModel(model);
			this.setDefaultEditor(Object.class, new Editor());
			this.setDefaultRenderer(Object.class, new Renderer());
		}else{
			model.setMap(map);
			}
	}
	
	class TableModel extends javax.swing.table.AbstractTableModel {

		DiscreteColourMap map;
		ArrayList<Integer> indexes;
		
		public TableModel(DiscreteColourMap m){
			setMap(m);
			}
		
		public void setMap(DiscreteColourMap m){
			map = m;
			indexes = new ArrayList<Integer>(map.colours.keySet());
			Collections.sort(indexes);
			this.fireTableStructureChanged();
		}
		
		@Override
		public boolean isCellEditable(int row, int col){
			return col == 1;
		}
		
		public int getColumnCount() {
			if (map.hasNameMap()) 
				return 3;
			return 2;
		}

		public int getRowCount() {
			return map.colours.size();
		}

		public Object getValueAt(int row, int col) {
			
			switch (col){
				case 0:		//index
					return indexes.get(row);
					
				case 1:		//colour
					return map.colours.get(indexes.get(row));
					
				case 2:		//name if applicable
					return map.nameMap.get(indexes.get(row));
				}
			return null;
		}
		
		@Override
		public void setValueAt(Object o, int row, int col) {
			if (o == null) return;
			Integer index;
			Colour c;
			switch (col){
				case 0:
					if (!(o instanceof MguiInteger)) return;
					//if index already exists, no good
					if (map.colours.containsKey(o)) return;
					//index = (arInteger)getValueAt(row, 0);
					index = indexes.get(row);
					c = map.getColour(index);
					//shouldn't happen
					if (c == null) return;
					//map.colours.remove(index);
					//map.colours.put(index, c);
					map.colours.remove(index);
					map.setColour(index, c);
					break;
				case 1:
					if (!(o instanceof Colour)) return;
					//if index already exists..
					index = (Integer)getValueAt(row, 0);
					//map.colours.remove(index);
					//map.colours.put(index, (Colour)o);
					map.setColour(index, (Colour)o);
					break;
				case 2:
					if (!(o instanceof String) || ((String)o).length() == 0) return;
					//String s = (String)getValueAt(row, 2);
					//index = (arInteger)getValueAt(row, 0);
					index = indexes.get(row);
					map.nameMap.set(index, (String)o);
					//map.setColour((String)o, map.getColour(index.value));
					break;
				}
		}
		
		@Override
		public String getColumnName(int col){
			switch (col){
				case 0:
					return "Value";
				case 1:
					return "Colour";
				case 2:
					return "Name";
				}
			return "";
		}
		
	}
	
	class Renderer implements TableCellRenderer{

		public Component getTableCellRendererComponent(JTable thisTable, 
													   Object thisObj,
													   boolean isSel, 
													   boolean hasFocus, 
													   int row, 
													   int col) {
			
			switch (col){
				case 0:
					return new JLabel(thisObj.toString());
				case 1:
					ColourButton button = new ColourButton();
					if (thisObj != null)
						button.setColour(((Colour)thisObj).getColor());
					else{
						button.setColour(Color.WHITE);
						button.setText("X");
						}
					return button;
				case 2:
					if (thisObj == null) thisObj = "N/V";
					return new JLabel(thisObj.toString());
				}
			
			return null;
			}
		
	}
	
	public class Editor extends AbstractCellEditor implements TableCellEditor,
															  ActionListener{
		
		ColourButton colourButton = new ColourButton();
		JTextField indexBox = new JTextField();
		JTextField nameBox = new JTextField();
		Object currentValue;
		
		public Editor(){
			init();
		}
		
		private void init(){
			colourButton.addActionListener(this);
			colourButton.setActionCommand("Colour");
			indexBox.addActionListener(this);
			indexBox.setActionCommand("Index");
			nameBox.addActionListener(this);
			nameBox.setActionCommand("Name");
		}

		public Component getTableCellEditorComponent(JTable thisTable, Object thisObj,
				boolean isSel, int row, int col) {
			
			currentValue = thisObj;
			
			switch (col){
				case 0:
					indexBox.setText(thisObj.toString());
					return indexBox; 
				case 1:
					colourButton.setColour(((Colour)thisObj).getColor());
					return colourButton;
				case 2:
					nameBox.setText(thisObj.toString());
					return nameBox;
			}
			
			return null;
		}
		
		public Object getCellEditorValue() {
			return currentValue;
			}
		
		public void actionPerformed(ActionEvent e) {
			
			if (e.getActionCommand().equals("Colour")){
				Color c = JColorChooser.showDialog(null, 
												   "Select Colour", 
												   ((Colour)currentValue).getColor());
				if (c != null)
					currentValue = new Colour4f(c);
				fireEditingStopped();
				return;
				}
			
			if (e.getActionCommand().equals("Index")){
				MguiInteger i = new MguiInteger(indexBox.getText());
				if (i != null)
					currentValue = i;
				fireEditingStopped();
				return;
				}
			
			if (e.getActionCommand().equals("Name")){
				currentValue = nameBox.getText();
				fireEditingStopped();
				return;
				}
			
			}

	}
	
}