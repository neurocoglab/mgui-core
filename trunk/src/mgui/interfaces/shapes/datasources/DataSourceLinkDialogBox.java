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

package mgui.interfaces.shapes.datasources;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import mgui.datasources.DataField;
import mgui.datasources.DataSource;
import mgui.datasources.DataSourceException;
import mgui.datasources.DataTable;
import mgui.datasources.LinkedDataStream;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.VertexDataColumn;

/**************************************************
 * Dialog box for specifying a vertex data column link to a data source.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DataSourceLinkDialogBox extends InterfaceOptionsDialogBox {

	JLabel lblName = new JLabel("Name:");
	JTextField txtName = new JTextField();
	JLabel lblDataSource = new JLabel("Data Source:");
	JComboBox cmbDataSource = new JComboBox();
	JLabel lblDataTable = new JLabel("Data Table:");
	JComboBox cmbDataTable = new JComboBox();
	JLabel lblLinkField = new JLabel("Link Field:");
	JComboBox cmbLinkField = new JComboBox();
	JCheckBox chkNameMap = new JCheckBox(" Link name map");
	
	VertexDataColumn data_column;
	String data_link_name;
	
	boolean doUpdate = true;
	
	public DataSourceLinkDialogBox(JFrame frame, InterfaceOptions options){
		super (frame, options);
		_init();
	}
	
	private void _init(){
		this.setButtonType(BT_OK_CANCEL);
		super.init();
		
		DataSourceLinkOptions _options = (DataSourceLinkOptions)options;
		data_column = _options.column;
		if (_options.name != null){
			this.setTitle("Edit Data Link: " + data_column.getName() + ".{" + _options.name + "}");
			txtName.setText(_options.name);
			data_link_name = _options.name;
			
		}else{
			this.setTitle("Add Data Link: " + data_column.getName());
			}
		
		cmbDataSource.addActionListener(this);
		cmbDataSource.setActionCommand("Data Source Changed");
		chkNameMap.addActionListener(this);
		chkNameMap.setActionCommand("Name Map Changed");
		
		setMainLayout(new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 100));
		this.setDialogSize(400, 400);
		
		updateDataSources();
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.35, 1);
		mainPanel.add(lblName, c);
		c = new LineLayoutConstraints(1, 1, 0.4, 0.55, 1);
		mainPanel.add(txtName, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.35, 1);
		mainPanel.add(lblDataSource, c);
		c = new LineLayoutConstraints(2, 2, 0.4, 0.55, 1);
		mainPanel.add(cmbDataSource, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.35, 1);
		mainPanel.add(lblDataTable, c);
		c = new LineLayoutConstraints(3, 3, 0.4, 0.55, 1);
		mainPanel.add(cmbDataTable, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.35, 1);
		mainPanel.add(lblLinkField, c);
		c = new LineLayoutConstraints(4, 4, 0.4, 0.55, 1);
		mainPanel.add(cmbLinkField, c);
		c = new LineLayoutConstraints(5, 5, 0.4, 0.55, 1);
		mainPanel.add(chkNameMap, c);
		
		chkNameMap.setEnabled(data_column.hasNameMap());
	}
	
	protected void updateDataSources(){
		if (!doUpdate) return;
		doUpdate = false;
		
		cmbDataSource.removeAllItems();
		
		ArrayList<DataSource> sources = InterfaceSession.getWorkspace().getConnectedDataSources();
		for (int i = 0; i < sources.size(); i++){
			cmbDataSource.addItem(sources.get(i));
			}
		
		if (data_column != null && data_link_name != null){
			cmbDataSource.setSelectedItem(data_column.getLinkedData(data_link_name).getDataSource());
			}
		
		doUpdate = true;
		
		updateDataTables();
	}
	
	protected void updateDataTables(){
		if (!doUpdate) return;
		doUpdate = false;
		
		cmbDataTable.removeAllItems();
		DataSource currentDataSource = (DataSource)cmbDataSource.getSelectedItem();
		
		if (currentDataSource == null){
			doUpdate = true;
			updateDataFields();
			return;
			}
		
		try{
			ArrayList<DataTable> tables = currentDataSource.getTableSet().getTables();
			for (int i = 0; i < tables.size(); i++){
				cmbDataTable.addItem(tables.get(i).getName());
				}
			
			if (data_link_name != null){
				LinkedDataStream stream = data_column.getLinkedData(data_link_name);
				if (stream != null && stream.getDataSource().equals(currentDataSource))
					cmbDataTable.setSelectedItem(stream.getLinkTable());
				}
			
		}catch (DataSourceException ex){
			//ex.printStackTrace();
			JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
										"Error accessing '" + currentDataSource.getName(), 
										"DataSource Error", 
										JOptionPane.ERROR_MESSAGE);
			}
		
		doUpdate = true;
		updateDataFields();
		
	}
	
	protected void updateDataFields(){
		
		if (!doUpdate) return;
		doUpdate = false;
		
		cmbLinkField.removeAllItems();
		DataSource currentDataSource = (DataSource)cmbDataSource.getSelectedItem();
		String currentTable = (String)cmbDataTable.getSelectedItem();
		
		if (currentTable == null){
			doUpdate = true;
			return;
			}
		
		try{
			DataTable table = currentDataSource.getTableSet().getTable(currentTable);
			ArrayList<DataField> fields = table.getFieldList();
			boolean has_name_map = data_column.getNameMap() != null &&
					chkNameMap.isSelected();
			
			for (int i = 0; i < fields.size(); i++){
				if ((!has_name_map && fields.get(i).getDataType() == java.sql.Types.INTEGER) ||
					 (has_name_map && fields.get(i).getDataType() == java.sql.Types.VARCHAR)){
					cmbLinkField.addItem(fields.get(i).getName());
					}
				}
			
			if (data_link_name != null){
				cmbLinkField.setSelectedItem(data_column.getLinkedData(data_link_name).getLinkField());
				if (cmbLinkField.getSelectedItem() == null)
					cmbLinkField.setSelectedIndex(0);
				}
			
		}catch (DataSourceException ex){
			//ex.printStackTrace();
			JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
										"Error accessing '" + currentDataSource.getName(), 
										"DataSource Error", 
										JOptionPane.ERROR_MESSAGE);
			
			}
		
		doUpdate = true;
		
	}
	
	public static void showDialog(DataSourceLinkOptions options){
		DataSourceLinkDialogBox dialog = new DataSourceLinkDialogBox(InterfaceSession.getSessionFrame(), options);
		dialog.setVisible(true);
		return;
	}
	
	protected LinkedDataStream<?> getDataStream(){
		
		DataSource source = (DataSource)cmbDataSource.getSelectedItem();
		if (source == null) return null;
		String table = (String)cmbDataTable.getSelectedItem();
		if (table == null) return null;
		String field = (String)cmbLinkField.getSelectedItem();
		if (field == null) return null;
		String name = txtName.getText();
		boolean has_name_map = chkNameMap.isEnabled() && chkNameMap.isSelected();
		
		try{
			LinkedDataStream<?> stream = LinkedDataStream.getInstance(source, table, field, true, name, has_name_map);
					//new LinkedDataStream(source, table, field, true, name);
			return stream;
		
		}catch (DataSourceException e){
			//e.printStackTrace();
			InterfaceSession.log("DataSourceLinkDialog: Could not create linked field.\nDetails: " + e.getMessage(), 
								 LoggingType.Errors);
			return null;
			}
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Name Map Changed")){
			this.updateDataFields();
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			String name = txtName.getText();
			if (name == null || name.length() == 0){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											"No name specified!", 
											"Add Data Link", 
											JOptionPane.ERROR_MESSAGE);
				return;
				}
				LinkedDataStream<?> stream = getDataStream();
				if (stream == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												"Could not create link.. check your values.", 
												"Add Data Link", 
												JOptionPane.ERROR_MESSAGE);
					return;
					}
			
			if (data_link_name != null && data_link_name != name)
				data_column.removeDataLink(name);
			data_column.addDataLink(name, stream, chkNameMap.isSelected());
			JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
					"Added/updated link '" + name + "'.", 
					"Add/Edit Data Link", 
					JOptionPane.INFORMATION_MESSAGE);
			
			this.setVisible(false);
			return;
			}
		
		if (e.getActionCommand().equals("Data Source Changed")){
			updateDataTables();
			return;
			}
		
		if (e.getActionCommand().equals("Data Table Changed")){
			updateDataFields();
			return;
			}
		
		super.actionPerformed(e);
	}
	
}