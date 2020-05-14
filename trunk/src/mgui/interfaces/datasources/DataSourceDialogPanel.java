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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionListener;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;

public class DataSourceDialogPanel extends JPanel {

	//String data_source;
	protected JLabel lblURL = new JLabel("URL:");
	protected JTextField txtURL = new JTextField();
	public int height = 2;
	
	public DataSourceDialogPanel(){
		init();
	}
	
	protected void init(){
		LineLayout lineLayout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		setLayout(lineLayout);
		LineLayoutConstraints c = new LineLayoutConstraints(0, 0, 0, 0.24, 1);
		add(lblURL, c);
		c = new LineLayoutConstraints(0, 0, 0.24, 0.76, 1);
		add(txtURL, c);
	}
	
	public String getDataSource(){
		return txtURL.getText();
	}
	
	public boolean setDataSource(String source){
		txtURL.setText(source);
		return true;
	}
	
	public boolean setDataSourceFromUrl(String url){
		return setDataSource(url);
	}
	
	public String getUrl(){
		return getDataSource();
	}
	
	public void addListSelectionListener(ListSelectionListener l){
		
	}
	
	public void removeListSelectionListener(ListSelectionListener l){
		
	}
	
}