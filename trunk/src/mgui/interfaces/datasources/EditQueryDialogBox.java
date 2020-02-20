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

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import mgui.datasources.DataQuery;
import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;

/***************************************************************
 * Simple dialog to edit a query. TODO: Implement a more robust query builder dialog.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class EditQueryDialogBox extends InterfaceOptionsDialogBox {

	JLabel lblName = new JLabel("Name:");
	JTextField txtName = new JTextField();
	JTextArea txtSQL = new JTextArea();
	
	protected boolean query_changed = false;
	
	public EditQueryDialogBox(JFrame aFrame, DataQueryOptions options){
		super(aFrame, options);
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		
		_init();
		setLocationRelativeTo(aFrame);
		this.setLocation(300, 370);
	}
	
	protected void _init(){
		super.init();
		
		DataQueryOptions _options = (DataQueryOptions)options;
		this.setTitle("Define SQL for Query '" + _options.query.getName() + "'.");
		
		LineLayout lineLayout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(600,350);
		
		txtName.addActionListener(this);
		txtName.setActionCommand("Name Changed");
		
		LineLayoutConstraints c = new LineLayoutConstraints(0, 0, 0.05, 0.2, 1);
		mainPanel.add(lblName, c);
		c = new LineLayoutConstraints(0, 0, 0.25, 0.7, 1);
		mainPanel.add(txtName, c);
		c = new LineLayoutConstraints(1, 6, 0.05, 0.9, 1);
		mainPanel.add(txtSQL, c);
		lineLayout.setFlexibleComponent(txtSQL);
		
		txtSQL.setWrapStyleWord(true);
		txtSQL.setLineWrap(true);
				
		showDialog();
	}
	
	public void showDialog(){
		DataQueryOptions _options = (DataQueryOptions)options;
		
		if (_options == null) return;
		
		DataQuery query = _options.query;
		txtName.setText(_options.query.getName());
		
		this.setTitle("Define SQL for Query '" + _options.query.getName() + "'.");
		
		txtSQL.setText(query.getSQLStatement());
		
		
	}
	
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Name Changed")){
			this.setTitle("Define SQL for Query '" + txtName.getText() + "'.");
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			
			if (txtName.getText().length() == 0 || txtSQL.getText().length() == 0) return;
			
			DataQueryOptions _options = (DataQueryOptions)options;
			_options.query.setName(txtName.getText());
			_options.query.setSQLStatement(txtSQL.getText());
			query_changed = true;
			
			this.setVisible(false);
			return;
			}
		
		super.actionPerformed(e);
		
	}
	
	/**********************************************************
	 * Static method to show this dialog and return the updated query. If no changes were made (i.e., "Cancel"
	 * was clicked) the method returns {@code null}.
	 * 
	 * @param frame
	 * @param query
	 * @return
	 */
	public static DataQuery showDialog(JFrame frame, DataQuery query){
		DataQueryOptions options = new DataQueryOptions(query);
		EditQueryDialogBox dialog = new EditQueryDialogBox(frame, options);
		dialog.setVisible(true);
		if (dialog.query_changed)
			return ((DataQueryOptions)dialog.options).query;
		return null;
	}
	
}