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

package mgui.interfaces.projects;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.io.FileLoader;
import mgui.io.FileWriter;
import mgui.io.domestic.variables.DefaultMatrixFileLoader;
import mgui.io.domestic.variables.DefaultMatrixFileWriter;

/****************************************************
 * Dialog box to define a {@link ProjectDirectory}, and in particular the list
 * of {@link ProjectDataItem}s it contains.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ProjectDirectoryDialogBox extends InterfaceOptionsDialogBox {

	JLabel lblDataItems = new JLabel("Data items:");
	JTable data_items_table;
	DataItemTableModel table_model;
	JScrollPane lstDataItems;
	JButton cmdAddItem = new JButton("Add item");
	JButton cmdRemoveItems = new JButton("Remove items");
	
	public ProjectDirectoryDialogBox(){
		super();
	}

	public ProjectDirectoryDialogBox(JFrame aFrame, ProjectDirectoryOptions options){
		super(aFrame, options);
		init();
	}
	
	@Override
	protected void init(){
		buttonType = BT_OK_CANCEL;
		super.init();
		
		ProjectDirectoryOptions _options = (ProjectDirectoryOptions)options;
		setTitle("Define Directory: '" + _options.directory.getPath() + "'");
		
		table_model = new DataItemTableModel(_options.directory);
		data_items_table = new JTable(table_model);
		lstDataItems = new JScrollPane(data_items_table);
		
		cmdAddItem.addActionListener(this);
		cmdAddItem.setActionCommand("Add Item");
		cmdRemoveItems.addActionListener(this);
		cmdRemoveItems.setActionCommand("Remove Items");
		
		this.setMainLayout(new LineLayout(20, 5, 0));
		setDialogSize(600, 350);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.9, 1);
		mainPanel.add(lblDataItems, c);
		c = new LineLayoutConstraints(2, 7, 0.05, 0.9, 1);
		mainPanel.add(lstDataItems, c);
		c = new LineLayoutConstraints(8, 8, 0.05, 0.43, 1);
		mainPanel.add(cmdAddItem, c);
		c = new LineLayoutConstraints(8, 8, 0.52, 0.43, 1);
		mainPanel.add(cmdRemoveItems, c);
		
		updateEditors();
	}
	
	public static ProjectDirectory showDialog(JFrame frame, ProjectDirectory dir){
		ProjectDirectoryOptions options = new ProjectDirectoryOptions();
		options.directory = dir;
		ProjectDirectoryDialogBox dialog = new ProjectDirectoryDialogBox(frame, options);
		dialog.setVisible(true);
		return options.directory;
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Add Item")){
			ProjectDataItem item = new ProjectDataItem("new_item", "{instance}_sample.txt");
			//item.setFilenameForm("{instance}_sample.txt");
			item.setFileLoader(new DefaultMatrixFileLoader());
			item.setFileWriter(new DefaultMatrixFileWriter());
			table_model.addItem(item);
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			ProjectDirectoryOptions _options = (ProjectDirectoryOptions)options;
			_options.directory.setDataItems(table_model.data_items);
			
			this.setVisible(false);
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_CANCEL)){
			ProjectDirectoryOptions _options = (ProjectDirectoryOptions)options;
			_options.directory = null;
			
			this.setVisible(false);
			return;
			}
	}
	
	public void updateEditors(){
		
		ArrayList<InterfaceIOType> types = new ArrayList<InterfaceIOType>(InterfaceEnvironment.getIOTypes().values());
		InterfaceComboBox loaders = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
		loaders.addItem("<-None->");
		InterfaceComboBox writers = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
		writers.addItem("<-None->");
		for (int i = 0; i < types.size(); i++){
			if (types.get(i).getType() == InterfaceIOType.TYPE_INPUT)
				loaders.addItem(types.get(i));
			if (types.get(i).getType() == InterfaceIOType.TYPE_OUTPUT)
				writers.addItem(types.get(i));
			}
		
		TableColumn c = data_items_table.getColumnModel().getColumn(2);
		c.setCellEditor(new DefaultCellEditor(loaders));
		c = data_items_table.getColumnModel().getColumn(3);
		c.setCellEditor(new DefaultCellEditor(writers));
		
		table_model.fireTableDataChanged();
	}
	
	class DataItemTableModel extends AbstractTableModel {

		public ArrayList<ProjectDataItem> data_items = new ArrayList<ProjectDataItem>();
		
		public DataItemTableModel(ProjectDirectory dir){
			//clone list
			ArrayList<ProjectDataItem> items = dir.getDataItems();
			for (int i = 0; i < items.size(); i++)
				data_items.add((ProjectDataItem)items.get(i).clone());
		}
		
		public void addItem(ProjectDataItem item){
			data_items.add(item);
			this.fireTableDataChanged();
		}
		
		public void removeItem(ProjectDataItem item){
			data_items.remove(item);
			this.fireTableDataChanged();
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex){
				case 0: 
					return String.class;
				case 1: 
					return String.class;
				case 2:
					return InterfaceIOType.class;
				case 3:
					return InterfaceIOType.class;
				}
			return String.class;
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch (columnIndex){
				case 0: 
					return "Name";
				case 1: 
					return "File Name Form";
				case 2:
					return "Loader";
				case 3:
					return "Writer";
				}
			return "?";
		}

		@Override
		public int getRowCount() {
			// TODO Auto-generated method stub
			return data_items.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex){
				case 0: 
					return data_items.get(rowIndex).getName();
				case 1: 
					return data_items.get(rowIndex).getFilenameForm();
				case 2:
					if (data_items.get(rowIndex).getFileLoader() == null) return "<-None->";
					return InterfaceEnvironment.getIOTypeForInstance(data_items.get(rowIndex).getFileLoader());
				case 3:
					if (data_items.get(rowIndex).getFileWriter() == null) return "<-None->";
					return InterfaceEnvironment.getIOTypeForInstance(data_items.get(rowIndex).getFileWriter());
				}
			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			
			switch (columnIndex){
				case 0: 
					if (value == null) return;
					data_items.get(rowIndex).setName((String)value);
					return;
				case 1: 
					if (value == null) return;
					data_items.get(rowIndex).setFilenameForm((String)value);
					return;
				case 2:
					if (value == null || value.equals("<-None->")){
						data_items.get(rowIndex).setFileLoader(null);
						return;
						}
					data_items.get(rowIndex).setFileLoader((FileLoader)((InterfaceIOType)value).getIOInstance());
					return;
				case 3:
					if (value == null || value.equals("<-None->")){
						data_items.get(rowIndex).setFileWriter(null);
						return;
						}
					data_items.get(rowIndex).setFileWriter((FileWriter)((InterfaceIOType)value).getIOInstance());
					return;
				}
		}
		
	}
	
}