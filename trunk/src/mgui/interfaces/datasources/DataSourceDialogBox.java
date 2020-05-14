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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import mgui.datasources.DataConnection;
import mgui.datasources.DataSource;
import mgui.datasources.DataSourceDriver;
import mgui.datasources.security.SecureDataSourceFunctions;
import mgui.datasources.util.DataSourceFunctions;
import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;

/*************************************************************
 * Dialog box for defining or editing a {@link DataSource} object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DataSourceDialogBox extends InterfaceOptionsDialogBox implements ActionListener {

	static final String CHK_LOCAL = "Local";
	static final String CHK_NEW = "New";
	static final String CHK_URL = "URL";
	
	//controls
	DataSourceDialogPanel dbPanel;
	
	JLabel lblDriver = new JLabel("Driver:");
	JComboBox cmbDriver = new JComboBox();
	JCheckBox chkLocal = new JCheckBox(" Local");
	JCheckBox chkNew = new JCheckBox(" New");
	DataSourceDialogPanel newDataSourcePanel = new DataSourceDialogPanel();
	JLabel lblNewDataSource = new JLabel("Source Name:");
	JTextField txtNewDataSource = new JTextField();
	//JLabel lblConnName = new JLabel("Name:");
	//JTextField txtConnName = new JTextField();
	JLabel lblLogin = new JLabel("Login:");
	JTextField txtLogin = new JTextField();
	JLabel lblPassword = new JLabel("Password:");
	JPasswordField txtPassword = new JPasswordField();
	
	String checked = CHK_LOCAL;
	boolean blnUpdate = true;
	boolean is_encrypted = false;
	LineLayout lineLayout;
	
	DataSource temp_source;
	
	//TODO replace with InterfaceDialogUpdater
	public DataSourceDialogBox(JFrame aFrame, DataSourceOptions options){
		super(aFrame, options);
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		
		_init();
		setLocationRelativeTo(aFrame);
		//this.setLocation(300, 100);
	}
	
	protected void _init(){
		super.init();
		
		lineLayout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(600,600);
		this.setTitle("Define Data Source");
		
		chkNew.setActionCommand(CHK_NEW);
		chkNew.addActionListener(this);
		chkLocal.setActionCommand(CHK_LOCAL);
		chkLocal.addActionListener(this);
		cmbDriver.addActionListener(this);
		cmbDriver.setActionCommand("Driver Changed");
		
		txtPassword.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent keyEvent) {
				is_encrypted = false;
			}
			
		    public void keyReleased(KeyEvent keyEvent) {
		    
		    }
		    
		    public void keyTyped(KeyEvent keyEvent) {
		    	//is_encrypted = false;
		    }
		});
		
		txtPassword.setActionCommand("Password Changed");
		LineLayout layout = new LineLayout(25, 5, 0);
		
		newDataSourcePanel.setLayout(layout);
		
		LineLayoutConstraints c = new LineLayoutConstraints(0, 0, 0, 0.24, 1);
		newDataSourcePanel.add(lblNewDataSource, c);
		c = new LineLayoutConstraints(0, 0, 0.24, 0.76, 1);
		newDataSourcePanel.add(txtNewDataSource, c);
		chkLocal.setSelected(true);
		
		
		if (options != null){
			DataSourceOptions _options = (DataSourceOptions)options;
			temp_source = new DataSource();
			temp_source.setFromDataSource(_options.source);
			}
		
		updateCombos();
		updateDialog();
		
		c = new LineLayoutConstraints(1, 1, 0.02, 0.18, 1);
		mainPanel.add(chkLocal, c);
		c = new LineLayoutConstraints(1, 1, 0.20, 0.18, 1);
		mainPanel.add(lblDriver, c);
		c = new LineLayoutConstraints(1, 1, 0.38, 0.57, 1);
		mainPanel.add(cmbDriver, c);
		c = new LineLayoutConstraints(2, 2, 0.02, 0.18, 1);
		mainPanel.add(chkNew, c);
//		c = new LineLayoutConstraints(2, 2, 0.20, 0.18, 1);
//		mainPanel.add(lblConnName, c);
//		c = new LineLayoutConstraints(2, 2, 0.38, 0.57, 1);
//		mainPanel.add(txtConnName, c);
		c = new LineLayoutConstraints(2, 2, 0.20, 0.18, 1);
		mainPanel.add(lblLogin, c);
		c = new LineLayoutConstraints(2, 2, 0.38, 0.57, 1);
		mainPanel.add(txtLogin, c);
		c = new LineLayoutConstraints(3, 3, 0.20, 0.18, 1);
		mainPanel.add(lblPassword, c);
		c = new LineLayoutConstraints(3, 3, 0.38, 0.57, 1);
		mainPanel.add(txtPassword, c);
		
		lineLayout.setFlexibleComponent(newDataSourcePanel);
	}
	
	@Override
	public boolean updateDialog(){
		
		if (dbPanel != null)
			mainPanel.remove(dbPanel);
		
		//fill text boxes with current data source
		//DataSourceOptions _options = (DataSourceOptions)options;
		DataSource source = temp_source;
		
		is_encrypted = false;
		
		if (source != null){
			blnUpdate = false;
			//txtConnName.setText(source.getConnection().getName());
			cmbDriver.setSelectedItem(getSourceName(source.getConnection().getDriver()));
			txtLogin.setText(source.getConnection().getLogin());
			//set password?
			String password = source.getConnection().getPassword();
			if (password.length() > 0){
				txtPassword.setText(password);
				is_encrypted = true;
				}
			blnUpdate = true;
			}
		
		//if (checked.equals(CHK_NEW))
		dbPanel = newDataSourcePanel;
		
		if (checked.equals(CHK_LOCAL)){
			if (cmbDriver.getSelectedItem() != null){
				String driver_name = (String)cmbDriver.getSelectedItem();
				DataSourceDriver driver = InterfaceEnvironment.getDataSourceDriver(driver_name);
				//dbPanel = InterfaceEnvironment.getDataSourceDialogPanel(driver);
				DataSourceDialogPanel panel = driver.getDataSourceDialogPanel();
				if (panel != null)
					dbPanel = panel;
				
				if (source != null)
					dbPanel.setDataSourceFromUrl(source.getConnection().getUrl());
				}
			}
		
		if (dbPanel == null) return false;
		
		mainPanel.add(dbPanel, new LineLayoutConstraints(4, 4 + dbPanel.height, 0.20, 0.75, 1));
		if (dbPanel.height > 8)
			this.setDialogSize(600, 720);
		else if (dbPanel.height > 5)
			this.setDialogSize(600, 560);
		else
			this.setDialogSize(600, 330);
		
		lineLayout.setFlexibleComponent(dbPanel);
		mainPanel.updateUI();
		return true;
	}
	
	String getSourceName(String class_name){
		DataSourceDriver driver = InterfaceEnvironment.getDataSourceDriverByClass(class_name);
		if (driver != null) return driver.getName();
		return class_name;
	}
	
	void updateCheckBoxes(){
		if (!blnUpdate) return;
		blnUpdate = false;
		chkNew.setSelected(false);
		chkLocal.setSelected(false);
		if (checked.equals(CHK_NEW))
			chkNew.setSelected(true);
		if (checked.equals(CHK_LOCAL))
			chkLocal.setSelected(true);
		blnUpdate = true;
		updateDialog();
	}
	
	void updateCombos(){
		blnUpdate = false;
		cmbDriver.removeAllItems();
		ArrayList<String> drivers = InterfaceEnvironment.getDataSourceDriverNames();
		if (drivers.size() == 0) return;
		for (int i = 0; i < drivers.size(); i++)
			cmbDriver.addItem(drivers.get(i));
		
		if (temp_source != null)
			cmbDriver.setSelectedItem(temp_source.getConnection().getDriver());
		else
			cmbDriver.setSelectedIndex(0);
		blnUpdate = true;
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		//handle OK or Cancel events
		super.actionPerformed(e);
		
		if (e.getActionCommand().equals(CHK_NEW)){
			checked = CHK_NEW;
			updateCheckBoxes();
			return;
		}
		
		if (e.getActionCommand().equals(CHK_LOCAL)){
			checked = CHK_LOCAL;
			updateCheckBoxes();
			return;
		}
		
		if (e.getActionCommand().equals("Driver Changed")){
			if (!blnUpdate || temp_source == null) return;
			String name = (String)cmbDriver.getSelectedItem();
			DataSourceDriver driver = InterfaceEnvironment.getDataSourceDriver(name);
			if (driver != null) name = driver.getName();
			temp_source.getConnection().setDriver(name);
			updateDialog();
			return;
		}
		
		if (e.getActionCommand().equals("Password Changed")){
			if (!blnUpdate) return;
			is_encrypted = false;
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			//add new datasource to interface
			
			DataSourceOptions _options = (DataSourceOptions)options;
			
			DataConnection conn = new DataConnection();
			conn.setName(getDataSourceName());
			conn.setLogin(txtLogin.getText());
			String encrypted_password = null;
			if (txtPassword.getPassword().length > 0){
				if (!is_encrypted){
					try{
						encrypted_password = 
							SecureDataSourceFunctions.getEncryptedPassword(txtPassword.getPassword());
					}catch (Exception ex){
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
													  "Error encrypting password!", 
													  "Set Data Source", 
													  JOptionPane.ERROR_MESSAGE);
						InterfaceSession.handleException(ex);
						return;
						}
					conn.setPassword(encrypted_password);
				}else{
					conn.setPassword(new String(txtPassword.getPassword()));
					}
			}else{
				if (_options.source != null)
					conn.setPassword(""); //_options.source.getConnection().getPassword());
				}
			
			String d = (String)cmbDriver.getSelectedItem();
			if (d != null){
				DataSourceDriver driver = InterfaceEnvironment.getDataSourceDriver(d);
				conn.setDriver(driver.getClassName());
				conn.setUrl(getDataSourceUrl()); //, getDataSource()));
				}
			
			DataSource ds = new DataSource(conn);
			
			if (chkNew.isSelected()){
				// Try to create new database...
				DataSource new_source = DataSourceFunctions.createNewDataSource(conn.getDriver(), 
																				conn.getName(), 
																				conn.getUrl(), 
																				conn.getLogin(), 
																				conn.getPassword());
				
				if (new_source == null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Could not create new data source.", 
												  "Create Data Source", 
												  JOptionPane.ERROR_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Created new data source '" + conn.getName() + "'.", 
												  "Create Data Source", 
												  JOptionPane.INFORMATION_MESSAGE);
					new_source.disconnect();
					_options.source = new_source;
					}
				
			}else{
			
					_options.source = ds;
				}
			
			this.setVisible(false);
		}
		
	}
	
	public static DataSource showPanel(DataSourceOptions options){
		DataSourceDialogBox dialog = new DataSourceDialogBox(InterfaceSession.getSessionFrame(), options);
		dialog.setVisible(true);
		return options.source;
	}
	
	DataSourceDriver getDriver(){
		return InterfaceEnvironment.getDataSourceDriver((String)cmbDriver.getSelectedItem());
	}
	
	protected String getDataSourceName(){
		if (checked.equals(CHK_NEW)){
			return txtNewDataSource.getText();
			}
		if (checked.equals(CHK_LOCAL)){
			return dbPanel.getUrl();
			}
		return null;
	}
	
	protected String getDataSourceUrl(){
		String name = getDataSourceName();
		if (name == null) return null;
		return getDriver().getUrlFromName(name);
		
	}
	
	
}