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

package mgui.interfaces.datasources.tinysql;

import mgui.datasources.DataSourceDriver;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.datasources.DataSourceDialogPanel;

/**********************************************************************
 * Dialog panel for a TinySQL data source. Since TinySQL has only one possible URL (jdbc:tinySQL), this panel
 * sets it and doesn't allow changes.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class TinySQLDialogPanel extends DataSourceDialogPanel {

	public DataSourceDriver driver;
	
	
	public TinySQLDialogPanel(){
		driver = InterfaceEnvironment.getDataSourceDriver("TinySQL");
		_init();
	}
	
	public TinySQLDialogPanel(DataSourceDriver d){
		driver = d;
		_init();
	}
	
	private void _init(){
		super.init();
		txtURL.setText("jdbc:dbfFile:");
		txtURL.setEditable(false);
	}
	
	@Override
	public boolean setDataSource(String source){
		return false;
	}
	
	@Override
	public boolean setDataSourceFromUrl(String url){
		return false;
	}
	
	
}