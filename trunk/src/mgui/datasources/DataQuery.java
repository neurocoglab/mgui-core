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

package mgui.datasources;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Writer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.menus.PopupMenuObject;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.resources.icons.IconObject;

import org.xml.sax.Attributes;

/*******************************
 * Represents an SQL query.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * 
 */

public class DataQuery extends AbstractInterfaceObject implements PopupMenuObject,
																  IconObject,
																  DataSourceItem{

	AttributeList attributes;
	
	public DataQuery(){
		
	}
	
	public DataQuery(DataSource source, String statement){
		this("no-name", source, statement);
	}
	
	public DataQuery(String name, DataSource source, String statement){
		init();
		setDataSource(source);
		setName(name);
		setSQLStatement(statement);
	}
	
	public boolean hasSortedFields(){
		return false;
	}
	
	public boolean setSortedFields(boolean b){
		return false;
	}
	
	
	protected void init(){
		attributes = new AttributeList();
		
		attributes.add(new Attribute<String>("Name", "no-name"));
		attributes.add(new Attribute<DataSource>("DataSource", DataSource.class));
		attributes.add(new Attribute<String>("SQLStatement", ""));
		
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/data_query_17.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/data_query_17.png");
		return null;
	}
	
	@Override
	public String getTreeLabel(){
		  return getName();
	  }
	
	public void setDataSource(DataSource source){
		//dataSource = source;
		attributes.setValue("DataSource", source);
	}
	
	public DataSource getDataSource(){
		//return dataSource;
		return (DataSource)attributes.getValue("DataSource");
		//return dataSource;
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		 super.setTreeNode(treeNode);
		 
		 treeNode.add(attributes.issueTreeNode());
	}
	
	@Override
	public String getSQLStatement(DataSourceDriver driver){
		return getSQLStatement();
	}
	
	@Override
	public String getSQLStatement(){
		return (String)attributes.getValue("SQLStatement");
	}
	
	public String getSQLStatement(int max_chars){
		String statement = getSQLStatement();
		if (max_chars < 4) max_chars = 4;
		if (statement.length() <= max_chars)
			return statement;
		return statement.substring(0, max_chars - 3) + "...";
	}
	
	public void setSQLStatement(String statement){
		//SQLStatement = statement;
		attributes.setValue("SQLStatement", statement);
		//updateTreeNodes();
	}
	
	@Override
	public void setName(String name){
		attributes.setValue("Name", name);
	}
	
	@Override
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	@Override
	public String toString(){
		return "DataQuery:" + getName();
		 //return "SQL: '" + getSQLStatement(20) + "'";
	}
	
	public InterfacePopupMenu getPopupMenu() {
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		
		menu.addMenuItem(new JMenuItem("DataQuery", getObjectIcon()));
		menu.add(new JSeparator(), 1);
		menu.add(new JSeparator(), 1);
		
		menu.addMenuItem(new JMenuItem("Edit SQL"));
		menu.add(new JSeparator(), 4);
		
		menu.addMenuItem(new JMenuItem("Copy"));
		menu.addMenuItem(new JMenuItem("Delete"));
		
		return menu;
	}

	public void handlePopupEvent(ActionEvent e) {
		
		if (!(e.getSource() instanceof JMenuItem)) return;
		JMenuItem item = (JMenuItem)e.getSource();
		
		if (item.getText().equals("Edit SQL")){
			//TODO: replace with specialized SQL builder dialog
			String sql = JOptionPane.showInputDialog(InterfaceEnvironment.getFrame(), 
													 "SQL text:", 
													 "Add New Query", 
													 JOptionPane.QUESTION_MESSAGE);
			if (sql == null) return;
			
			setSQLStatement(sql);
			return;
		}
		
	}
	
	public void showPopupMenu(MouseEvent e) { }

	@Override
	public String getDTD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXMLSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLElementEnd(String localName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLString(String s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLocalName() {
		return "DataQuery";
	}

	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		writer.write(_tab + "<DataQuery name='" + this.getName() + "'>");
		String string = XMLFunctions.getXMLCodedString(this.getSQLStatement());
		string = XMLFunctions.getXMLTabbedString(string, tab + 1);
		writer.write("\n" + _tab2 + string);
		writer.write("\n" + _tab + "</DataQuery>\n");
		
	}

	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException {
		writeXML(tab, writer, new XMLOutputOptions(), progress_bar);		
	}

	@Override
	public void writeXML(int tab, Writer writer) throws IOException {
		writeXML(tab, writer, new XMLOutputOptions(), null);
	}

	@Override
	public String getShortXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}
	
}