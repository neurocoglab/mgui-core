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

package mgui.interfaces.datasources;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import mgui.datasources.DataSourceDriver;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.table.AttributeCellEditor;
import mgui.interfaces.attributes.table.AttributeCellRenderer;
import mgui.interfaces.attributes.table.AttributeTableModel;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;

/*********************************************************************
 * Panel which queries and lists the available databases in a given environment.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DataSourceListDialogPanel extends DataSourceDialogPanel {

	//String data_source;
	JLabel lblSources = new JLabel("Sources:");
	JScrollPane lstSources = new JScrollPane();
	DefaultTableModel model = new DefaultTableModel();
	JTable sourceList = new JTable(model);
	public DataSourceDriver driver;
	JScrollPane scrAttributes;
	JTable attr_table;
	AttributeTableModel attr_table_model;
	JLabel lblAttributes = new JLabel("Additional properties:");
	
	private boolean is_init = false;
	
	public DataSourceListDialogPanel(){
		
	}
	
	public DataSourceListDialogPanel(String d){
		driver = InterfaceEnvironment.getDataSourceDriver(d);
		_init();
	}
	
	public DataSourceListDialogPanel(DataSourceDriver d){
		driver = d;
		_init();
	}
	
	public void setDataSourceDriver(String name){
		driver = InterfaceEnvironment.getDataSourceDriver(name);
		_init();
		this.repaint();
	}
	
	@Override
	public String getDataSource(){
		int row = sourceList.getSelectedRow();
		if (row < 0) return null;
		String name = (String)model.getValueAt(row, 0);
		return name;
	}
	
	@Override
	public boolean setDataSource(String source){
		int row = findDataSourceRow(source);
		if (row < 0) return false;
		
		sourceList.getSelectionModel().setSelectionInterval(row, row); // .setValueAt(source, row, 0);
		//sourceList.setSelectedValue(source, true);
		return true;
	}
	
	int findDataSourceRow(String name){
		
		for (int i = 0; i < sourceList.getRowCount(); i++){
			String s = (String)sourceList.getValueAt(i, 0);
			if (s.equals(name)) return i;
			}
		
		return -1;
		
	}
	
	@Override
	public boolean setDataSourceFromUrl(String url){
		//extract datasource name from url
		return setDataSource(driver.getDataSourceFromUrl(url));
		
		
		//return setDataSource(part);
	}
	
	@Override
	public void addListSelectionListener(ListSelectionListener l){
		sourceList.getSelectionModel().addListSelectionListener(l);
	}
	
	@Override
	public void removeListSelectionListener(ListSelectionListener l){
		sourceList.getSelectionModel().removeListSelectionListener(l);
	}
	
	private void _init(){
		if (is_init) return;
		is_init = true;
		lstSources.setViewportView(sourceList);
		
		updateList();
		
		LineLayout layout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 100);
		this.setLayout(layout);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0, 1, 1);
		add(lblSources, c);
		c = new LineLayoutConstraints(2, 7, 0, 1, 1);
		add(lstSources, c);
		
		layout.setFlexibleComponent(lstSources);
		
		height = 8;
		
		//init_attribute_table();
		
		if (scrAttributes != null){
			c = new LineLayoutConstraints(8, 8, 0, 1, 1);
			add(lblAttributes, c);
			c = new LineLayoutConstraints(9, 14, 0, 1, 1);
			add(scrAttributes, c);
			height = 14;
			}
		
		updateUI();
	}
	
	private void init_attribute_table(){
		
		AttributeList list = driver.getAttributes();
		if (list == null) return;
		
		attr_table_model = new AttributeTableModel(list);
		attr_table = new JTable(attr_table_model);
		scrAttributes = new JScrollPane(attr_table);
		
		attr_table.setDefaultRenderer(Object.class, new AttributeCellRenderer());
		attr_table.setDefaultEditor(Object.class, new AttributeCellEditor());
	}
	
	protected void updateList(){
		//model.elements = DataSourceDrivers.getDatabaseNames(driver);
		if (driver == null) return;
		HashMap<String, HashMap<String,String>> properties = new HashMap<String,HashMap<String,String>>();
		ArrayList<String> dbs = driver.getDatabases(properties);
		
		if (dbs == null){
			model.setDataVector(new Object[0][0], new Object[0]);
			model.fireTableStructureChanged();
			
			sourceList.repaint();
			return;
			}
		
		ArrayList<String> columns = new ArrayList<String>();
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		columns.add("Name");
		
		for (int i = 0; i < dbs.size(); i++){
			String db_name = dbs.get(i);
			ArrayList<String> row = new ArrayList<String>();
			row.add(db_name);
			if (properties.size() > 0){
				if (i == 0){
					ArrayList<String> props = new ArrayList<String>(properties.get(db_name).keySet());
					for (int j = 0; j < props.size(); j++)
						columns.add(props.get(j));
					}
				HashMap<String,String> map = properties.get(dbs.get(i));
				for (int j = 1; j < columns.size(); j++){
					row.add(map.get(columns.get(j)));
					}
				}
			data.add(row);
			}
		
		Object[] _columns = new Object[columns.size()];
		Object[][] _data = new Object[dbs.size()][columns.size()];
		
		for (int i = 0; i < columns.size(); i++)
			_columns[i] = columns.get(i);
		
		for (int i = 0; i < dbs.size(); i++)
			for (int j = 0; j < columns.size(); j++)
				_data[i][j] = data.get(i).get(j);
		
		model.setDataVector(_data, _columns);
		model.fireTableStructureChanged();
		
		sourceList.repaint();
	}
	
	class DataListModel implements ListModel{
		
		public ArrayList<String> elements = new ArrayList<String>();
		
		public void addListDataListener(ListDataListener arg0) {
			// TODO Auto-generated method stub
			
		}

		public int getSize() {
			if (elements == null) return 0;
			return elements.size();
		}

		public void setElements(ArrayList<String> list){
			elements = list;
		}
		
		public void removeListDataListener(ListDataListener arg0) {
			// TODO Auto-generated method stub
			
		}
		
		public Object getElementAt(int i){
			return elements.get(i);
		}
		
	}
}