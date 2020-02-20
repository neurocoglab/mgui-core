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

package mgui.interfaces.datasources;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import mgui.datasources.DataSourceDriver;
import mgui.datasources.security.SecureDataSourceFunctions;
import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.table.AttributeCellEditor;
import mgui.interfaces.attributes.table.AttributeCellRenderer;
import mgui.interfaces.attributes.table.AttributeTableModel;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.security.StringEncrypter;
import mgui.io.FileLoader;
import mgui.io.domestic.datasources.DataSourceDriverLoader;
import mgui.io.domestic.datasources.DataSourceDriverWriter;

/*************************************************************
 * Dialog box which allows user to set up the data source drivers, i.e., with login, url, and password
 * information.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DataSourceDriverDialogBox extends InterfaceOptionsDialogBox {

	JLabel lblDriver = new JLabel("Driver: ");
	JComboBox cmbDriver = new JComboBox();
	JLabel lblClass = new JLabel("JDBC Driver Class*:");
	JTextField txtClass = new JTextField("");
	JLabel lblName = new JLabel("Name*:");
	JTextField txtName = new JTextField("");
	JLabel lblLogin = new JLabel("Login:");
	JTextField txtLogin = new JTextField("");
	JLabel lblPassword = new JLabel("Password:");
	JPasswordField txtPassword = new JPasswordField("");
	JLabel lblUrl = new JLabel("URL*:");
	JTextField txtUrl = new JTextField("");
	JButton cmdRevert = new JButton("Revert");
	JButton cmdUpdate = new JButton("Update");
	JButton cmdAddNew = new JButton("Add New");
	JButton cmdRemove = new JButton("Remove");
	JButton cmdRefresh = new JButton("Refresh from File");
	JButton cmdSave = new JButton("Save to File");
	JButton cmdDelete = new JButton("Delete File");
	
	JLabel lblAttributes = new JLabel("Additional properties:");
	JScrollPane scrAttributes;
	JTable attr_table;
	AttributeTableModel attr_table_model;
	
	boolean is_encrypted = false;
	DataSourceDriver new_driver = null;
	Class<? extends DataSourceDialogPanel> dialog_class = null;
	
	public DataSourceDriverDialogBox(){
		super();
	}
	
	public DataSourceDriverDialogBox(JFrame frame, DataSourceDriverOptions options){
		super(frame, options);
		_init();
	}
	
	private void _init(){
		buttonType = InterfaceDialogBox.BT_OK;
		super.init();
		
		init_attribute_table();
		
		LineLayout lineLayout = new LineLayout(25, 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(600,600);
		this.setTitle("Define JDBC Drivers");
		
		cmbDriver.addActionListener(this);
		cmbDriver.setActionCommand("Driver Changed");
		cmdSave.addActionListener(this);
		cmdSave.setActionCommand("Save to File");
		cmdRefresh.addActionListener(this);
		cmdRefresh.setActionCommand("Refresh from Files");
		cmdDelete.addActionListener(this);
		cmdDelete.setActionCommand("Delete File");
		cmdRevert.addActionListener(this);
		cmdRevert.setActionCommand("Revert");
		cmdUpdate.addActionListener(this);
		cmdUpdate.setActionCommand("Update");
		cmdAddNew.addActionListener(this);
		cmdAddNew.setActionCommand("Add New");
		cmdRemove.addActionListener(this);
		cmdRemove.setActionCommand("Revert");
		
		txtPassword.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent keyEvent) {
				is_encrypted = false;
			}
			
		    public void keyReleased(KeyEvent keyEvent) { }
		    
		    public void keyTyped(KeyEvent keyEvent) { }
		});
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.3, 1);
		mainPanel.add(lblDriver, c);
		c = new LineLayoutConstraints(1, 1, 0.35, 0.6, 1);
		mainPanel.add(cmbDriver, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.3, 1);
		mainPanel.add(lblName, c);
		c = new LineLayoutConstraints(2, 2, 0.35, 0.6, 1);
		mainPanel.add(txtName, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.3, 1);
		mainPanel.add(lblClass, c);
		c = new LineLayoutConstraints(3, 3, 0.35, 0.6, 1);
		mainPanel.add(txtClass, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.3, 1);
		mainPanel.add(lblLogin, c);
		c = new LineLayoutConstraints(4, 4, 0.35, 0.6, 1);
		mainPanel.add(txtLogin, c);
		c = new LineLayoutConstraints(5, 5, 0.05, 0.3, 1);
		mainPanel.add(lblPassword, c);
		c = new LineLayoutConstraints(5, 5, 0.35, 0.6, 1);
		mainPanel.add(txtPassword, c);
		c = new LineLayoutConstraints(6, 6, 0.05, 0.3, 1);
		mainPanel.add(lblUrl, c);
		c = new LineLayoutConstraints(6, 6, 0.35, 0.6, 1);
		mainPanel.add(txtUrl, c);
		c = new LineLayoutConstraints(7, 7, 0.05, 0.9, 1);
		mainPanel.add(lblAttributes, c);
		c = new LineLayoutConstraints(8, 11, 0.05, 0.9, 1);
		mainPanel.add(scrAttributes, c);
		
		c = new LineLayoutConstraints(12, 12, 0.15, 0.3, 1);
		mainPanel.add(cmdUpdate, c);
		c = new LineLayoutConstraints(13, 13, 0.15, 0.3, 1);
		mainPanel.add(cmdRevert, c);
		c = new LineLayoutConstraints(14, 14, 0.15, 0.3, 1);
		mainPanel.add(cmdAddNew, c);
		c = new LineLayoutConstraints(12, 12, 0.55, 0.3, 1);
		mainPanel.add(cmdSave, c);
		c = new LineLayoutConstraints(13, 13, 0.55, 0.3, 1);
		mainPanel.add(cmdRefresh, c);
		c = new LineLayoutConstraints(14, 14, 0.55, 0.3, 1);
		mainPanel.add(cmdDelete, c);
	
		
		
		updateDialog();
		
	}
	
	@Override
	public boolean updateDialog(){
		
		if (options == null) return false;
		
		DataSourceDriverOptions options = (DataSourceDriverOptions)this.options;
		HashMap<String, DataSourceDriver> drivers = options.getDrivers();
		ArrayList<String> keys = new ArrayList<String>(drivers.keySet());
		Collections.sort(keys);
		
		cmbDriver.removeAllItems();
		
		for (int i = 0; i < keys.size(); i++)
			cmbDriver.addItem(keys.get(i));
		
		if (cmbDriver.getItemCount() > 0)
			cmbDriver.setSelectedIndex(0);
		
		return true;
	}
	
	protected void updateControls(){
		
		if (new_driver != null){
			cmdUpdate.setText("Apply New");
			cmdUpdate.setActionCommand("Apply New");
			cmdSave.setText("Cancel New");
			cmdSave.setActionCommand("Cancel New");
			cmdRevert.setVisible(false);
			cmdAddNew.setVisible(false);
			cmdRemove.setVisible(false);
			cmdRefresh.setVisible(false);
			cmdDelete.setVisible(false);
			cmbDriver.setEnabled(false);
			return;
			}
		
		cmdRevert.setVisible(true);
		cmdAddNew.setVisible(true);
		cmdRemove.setVisible(true);
		cmdRefresh.setVisible(true);
		cmdDelete.setVisible(true);
		cmbDriver.setEnabled(true);
		cmdUpdate.setText("Update");
		cmdUpdate.setActionCommand("Update");
		cmdSave.setText("Save to File");
		cmdSave.setActionCommand("Save to File");
		
		this.repaint();
	}
	
	protected boolean controlsValid(){
		
		if (cmbDriver.getSelectedItem() == null) return false;
		if (txtName.getText().length() == 0) return false;
		if (txtUrl.getText().length() == 0) return false;
		if (txtClass.getText().length() == 0) return false;
		return true;
		
	}
	
	
	protected void init_attribute_table(){
		
		attr_table_model = new AttributeTableModel(new AttributeList());
		attr_table = new JTable(attr_table_model);
		scrAttributes = new JScrollPane(attr_table);
		
		attr_table.setDefaultRenderer(Object.class, new AttributeCellRenderer());
		attr_table.setDefaultEditor(Object.class, new AttributeCellEditor());
	}
	
	protected void updateAttributeTable(){
		
		//TODO: allow add/delete of attributes
		
		String name = (String)cmbDriver.getSelectedItem();
		if (name == null){
			attr_table_model.setAttributes(new AttributeList());
			return;
			}
		
		DataSourceDriver driver = InterfaceEnvironment.getDataSourceDriver(name);
		AttributeList list = driver.getAttributes();
		attr_table_model.setAttributes(list);
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Driver Changed")){
			
			if (new_driver != null) return;
			
			String name = (String)cmbDriver.getSelectedItem();
			if (name == null){
				// Clear fields
				
				txtName.setText("");
				txtClass.setText("");
				txtLogin.setText("");
				txtPassword.setText("");
				txtUrl.setText("");
				
				return;
				}
			
			DataSourceDriver driver = InterfaceEnvironment.getDataSourceDriver(name);
			if (driver == null){
				// Shouldn't happen
				InterfaceSession.log("DataSourceDriverDialogBox: Driver '" + name + "' not found.", LoggingType.Errors);
				return;
				}
			
			txtName.setText(driver.getName());
			txtClass.setText(driver.getClassName());
			txtLogin.setText(driver.getLogin());
			txtPassword.setText(driver.getPassword());
			is_encrypted = true;
			txtUrl.setText(driver.getUrl());
			
			updateAttributeTable();
			
			return;
			}
		
		if (e.getActionCommand().equals("Revert")){
		
			String current = (String)cmbDriver.getSelectedItem();
			updateDialog();
			cmbDriver.setSelectedItem(current);
			
			return;
			}
		
		if (e.getActionCommand().equals("Add New")){
			
			String new_name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), 
														  "Enter name for new driver: ");
			if (new_name == null) return;
			
			String current = (String)cmbDriver.getSelectedItem();
			new_driver = null;
			dialog_class = null;
			
			if (current != null){
				int q = JOptionPane.showConfirmDialog(InterfaceSession.getSessionFrame(), 
														"Base on '" + current + "'?", 
														"Add new JDBC driver", 
														JOptionPane.YES_NO_CANCEL_OPTION,
														JOptionPane.QUESTION_MESSAGE);
				if (q == JOptionPane.CANCEL_OPTION) return;
				if (q == JOptionPane.YES_OPTION){
					DataSourceDriver driver = InterfaceEnvironment.getDataSourceDriver(current);
					new_driver = (DataSourceDriver)driver.clone();
					}
				}
			
			if (new_driver == null){
				String driver_class = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), 
																  "Mgui driver class:", 
																  "mgui.datasources.DataSourceDriver");
				if (driver_class == null) return;
				
				boolean error = false;
				try{
					Class<?> clazz = Class.forName(driver_class);
					new_driver = (DataSourceDriver)clazz.newInstance();
				}catch (ClassNotFoundException ex){
					InterfaceSession.log("DataSourceDriverDialogBox: Class '" + driver_class + "' not found.", 
										 LoggingType.Errors);
					error = true;
				}catch(IllegalAccessException ex){
					InterfaceSession.log("DataSourceDriverDialogBox: Class '" + driver_class + "' could not be instantiated.", 
							 LoggingType.Errors);
					error = true;
				}catch(InstantiationException ex){
					InterfaceSession.log("DataSourceDriverDialogBox: Class '" + driver_class + "' could not be instantiated.", 
							 LoggingType.Errors);
					error = true;
					}
				
				}
			
			new_driver.setName(new_name);
			if (new_driver.getClassName() == null)
				new_driver.setClassName("");
			if (new_driver.getUrl() == null)
				new_driver.setUrl("");
			if (new_driver.getLogin() == null)
				new_driver.setLogin("");
			if (new_driver.getPassword() == null){
				new_driver.setPassword("");
				is_encrypted = false;
			}else{
				is_encrypted = true;
				}
			
			updateControls();
			
			txtName.setText(new_driver.getName());
			txtClass.setText(new_driver.getClassName());
			txtLogin.setText(new_driver.getLogin());
			txtPassword.setText(new_driver.getPassword());
			txtUrl.setText(new_driver.getUrl());
			
			return;
			}
		
		if (e.getActionCommand().equals("Apply New")){
			
			if (new_driver == null){
				updateControls();
				return;
				}
			
			if (!controlsValid()){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Not all required values are set.", 
											  "Apply New Data Source Driver", 
											  JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			new_driver.setName(txtName.getText());
			new_driver.setClassName(txtClass.getText());
			new_driver.setUrl(txtUrl.getText());
			new_driver.setLogin(txtLogin.getText());
			String password = null;
			
			if (this.is_encrypted){
				password = String.valueOf(txtPassword.getPassword());
			}else{
				try{
					password = SecureDataSourceFunctions.getEncryptedPassword(txtPassword.getPassword());
				}catch (StringEncrypter.EncryptionException ex){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Could not encrypt password for driver.", 
												  "Update Data Source Driver", 
												  JOptionPane.ERROR_MESSAGE);
					InterfaceSession.handleException(ex, LoggingType.Verbose);
					return;
					}
				}
			
			new_driver.setPassword(password);
			
			if (!InterfaceEnvironment.addDataSourceDriver(new_driver)){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Name '" + new_driver.getName() + "' already exists.", 
											  "Apply New Data Source Driver",
											  JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			String current = new_driver.getName();
			JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
										  "Driver '" + new_driver.getName() + "' added.", 
										  "Apply New Data Source Driver",
										  JOptionPane.INFORMATION_MESSAGE);
			
			new_driver = null;
			dialog_class = null;
			updateControls();
			updateDialog();
			cmbDriver.setSelectedItem(current);
			
			return;
			}
		
		if (e.getActionCommand().equals("Cancel New")){
			
			new_driver = null;
			dialog_class = null;
			updateControls();
			updateDialog();
			
			return;
			}
		
		if (e.getActionCommand().equals("Update")){
			String current = (String)cmbDriver.getSelectedItem();
			if (current == null) return;
			if (!controlsValid()){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Not all required values are set.", 
											  "Update Data Source Driver", 
											  JOptionPane.ERROR_MESSAGE);
				return;
				}
				
			DataSourceDriver driver = InterfaceEnvironment.getDataSourceDriver(current);
			driver.setName(txtName.getText());
			driver.setLogin(txtLogin.getText());
			driver.setUrl(txtUrl.getText());
			driver.setClassName(txtClass.getText());
			String password = null;
			if (this.is_encrypted){
				password = String.valueOf(txtPassword.getPassword());
			}else{
				try{
					password = SecureDataSourceFunctions.getEncryptedPassword(txtPassword.getPassword());
				}catch (StringEncrypter.EncryptionException ex){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Could not encrypt password for driver.", 
												  "Update Data Source Driver", 
												  JOptionPane.ERROR_MESSAGE);
					InterfaceSession.handleException(ex, LoggingType.Verbose);
					return;
					}
				}
			driver.setPassword(password);
			driver.setAttributes(attr_table_model.attributes);
			updateDialog();
			cmbDriver.setSelectedItem(current);
			return;
			}
		
		if (e.getActionCommand().startsWith("Refresh")){
			
			// Load data from files
			ArrayList<String> names = InterfaceEnvironment.getDataSourceDriverNames();
			boolean success = true;
			int count = 0;
			
			for (int i = 0; i < names.size(); i++){
				DataSourceDriver driver = InterfaceEnvironment.getDataSourceDriver(names.get(i));
				URL url_ref = driver.getUrlReference();
				if (url_ref != null){ 
					File file = new File(url_ref.getFile());
					if (!file.exists()){
						InterfaceSession.log("DataSourceDriverDialogBox: URL for driver '" + driver.getName() + "' is not a " +
											 "valid file reference: " + file.getAbsolutePath(),
											 LoggingType.Errors);
						
					}else{
						InterfaceEnvironment.removeDataSourceDriver(driver.getName());
						DataSourceDriverLoader loader = (DataSourceDriverLoader)driver.getFileLoader();
						loader.setFile(file);
						DataSourceDriver new_driver = loader.loadDriver();
						if (new_driver == null || !InterfaceEnvironment.addDataSourceDriver(new_driver)){
							InterfaceSession.log("DataSourceDriverDialogBox: Could not refresh driver from file: " + 
												 file.getAbsolutePath(), 
												 LoggingType.Errors);
							success = false;
						}else{
							count++;
							}
						}
					}
				}
			
			if (!success){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Could not reload all drivers from file. " +
											  "Please see log for details.", 
											  "Refresh data source drivers", 
											  JOptionPane.ERROR_MESSAGE);
			}else if (count > 0){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Reloaded data source drivers from " + count + " file(s).", 
											  "Refresh data source drivers", 
											  JOptionPane.INFORMATION_MESSAGE);
				}
			
			return;
			}
		
		if (e.getActionCommand().equals("Save to File")){
			
			// Show file dialog, with url ref as default (if it exists)
			String current = (String)cmbDriver.getSelectedItem();
			if (current == null) return;
			
			if (!controlsValid()){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Not all required values are set.", 
											  "Write Data Source Driver", 
											  JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			DataSourceDriver driver = InterfaceEnvironment.getDataSourceDriver(current);
			URL url_dir = InterfaceEnvironment.class.getResource("/data_sources/");
			File db_dir = new File(url_dir.getFile());
			URL url_ref = driver.getUrlReference();
			File start_file = null;
			if (url_ref != null){
				start_file = new File(url_ref.getFile());
				if (!start_file.exists())
					start_file = null;
				}
			
			JFileChooser fc = new JFileChooser(db_dir);
			if (start_file != null)
				fc.setSelectedFile(start_file);
			
			if (fc.showSaveDialog(InterfaceSession.getSessionFrame()) != JFileChooser.APPROVE_OPTION)
				return;
			
			File save_file = fc.getSelectedFile();
			String name = txtName.getText();
			if (start_file != null && !save_file.getName().equals(start_file.getName())){
				String new_name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), 
															  "Enter new name for new data source:", 
																driver.getName() + "1");
				if (new_name == null) return;
				name = new_name;
				}
			
			DataSourceDriverWriter writer = (DataSourceDriverWriter)driver.getFileWriter();
			writer.setFile(fc.getSelectedFile());
			
			String password = null;
			if (this.is_encrypted){
				password = String.valueOf(txtPassword.getPassword());
			}else{
				try{
					password = SecureDataSourceFunctions.getEncryptedPassword(txtPassword.getPassword());
				}catch (StringEncrypter.EncryptionException ex){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Could not encrypt password for driver.", 
												  "Write Data Source Driver", 
												  JOptionPane.ERROR_MESSAGE);
					InterfaceSession.handleException(ex, LoggingType.Verbose);
					return;
					}
				}
			
			DataSourceDriver new_driver = (DataSourceDriver)driver.clone();
			new_driver.setName(name);
			new_driver.setClassName(txtClass.getText());
			new_driver.setUrl(txtUrl.getText());
			new_driver.setLogin(txtLogin.getText());
			new_driver.setPassword(password);
			
			try{
				new_driver.setUrlReference(fc.getSelectedFile().toURI().toURL());
			}catch (MalformedURLException ex){
				InterfaceSession.log("DataSourceDriverDialog: Malformed URL '" + fc.getSelectedFile().getAbsolutePath() + "'.", 
									 LoggingType.Warnings);
				}
			
			if (!writer.write(new_driver)){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Error writing driver '" + driver.getName() + "' to file...", 
											  "Write Data Source Driver", 
											  JOptionPane.ERROR_MESSAGE);
			}else{
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Wrote driver '" + driver.getName() + "' to file.", 
											  "Write Data Source Driver", 
											  JOptionPane.INFORMATION_MESSAGE);
				}
			
			if (driver.getName().equals(new_driver.getName()))
				InterfaceEnvironment.removeDataSourceDriver(driver.getName());
			InterfaceEnvironment.addDataSourceDriver(new_driver);
			this.updateDialog();
			cmbDriver.setSelectedItem(new_driver.getName());
			
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			this.setVisible(false);
			}
		
	}
	
	/**********************************
	 * Shows a new dialog and blocks until it has closed.
	 * 
	 * @param options
	 */
	public static void showDialog(DataSourceDriverOptions options){
		
		DataSourceDriverDialogBox dialog = new DataSourceDriverDialogBox(InterfaceSession.getSessionFrame(),
																		 options);
		
		dialog.setVisible(true);
		
	}
	
}