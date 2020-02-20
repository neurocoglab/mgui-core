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

package mgui.io.domestic.datasources;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import mgui.datasources.DataConnection;
import mgui.datasources.DataQuery;
import mgui.datasources.DataSource;
import mgui.datasources.DataSourceException;
import mgui.datasources.DataTableSet;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.xml.XMLFunctions;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**************************************************************
 * XML (SAX) handler for loading XML-format data source files.
 * 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DataSourceXMLHandler extends DefaultHandler {

	protected DataSource data_source;
	protected DataTableSet table_set;
	protected ArrayList<DataQuery> queries;
	protected DataQuery current_query;
	protected String current_sql;
	
	public DataSourceXMLHandler(){
		
	}

	public DataSource getDataSource(){
		return data_source;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		if (localName.equals("DataSource")){
			if (data_source != null)
				throw new SAXException("DataSourceXMLHandler: DataSource tag encountered, but data source already set.");
			
			String name = attributes.getValue("name");
			if (name == null) name = "No name";
			
			data_source = new DataSource(name);
			return;
			}
		
		if (data_source == null){
			throw new SAXException("DataSourceXMLHandler: DataSource tag must be first in file.");
			}
		
		if (localName.equals("DataConnection")){
			
			DataConnection connection = data_source.getConnection();
			connection.setName(data_source.getName());
			connection.setLogin(attributes.getValue("login"));
			connection.setPassword(attributes.getValue("password"));
			connection.setUrl(attributes.getValue("url"));
			connection.setDriver(attributes.getValue("driver"));
			String filename = attributes.getValue("file");
			if (filename != null && filename.length() > 0)
				connection.setFile(new File(filename));
			
			return;
			}
		
		if (localName.equals("DataTableSet")){
			if (table_set != null)
				throw new SAXException("DataSourceXMLHandler: DataTableSet tag encountered, but table set already initiated.");
			table_set = new DataTableSet(data_source);
			return;
			}
		
		if (table_set != null){
			table_set.handleXMLElementStart(localName, attributes, null);
			return;
			}
		
		if (localName.equals("DataQueries")){
			if (queries != null)
				throw new SAXException("DataSourceXMLHandler: DataQueries tag encountered, but queries already initiated.");
			queries = new ArrayList<DataQuery>();
			return;
			}
		
		if (localName.equals("DataQuery")){
			if (current_query != null)
				throw new SAXException("DataSourceXMLHandler: DataQuery tag encountered, but a query is already initiated.");
			String name = attributes.getValue("name");
			if (name == null || name.length() == 0) name = "No name";
			current_query = new DataQuery(name, data_source, "");
			current_sql = "";
			return;
			}
		
		
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		if (localName.equals("DataSource")){
			if (data_source == null)
				throw new SAXException("DataSourceXMLHandler: DataSource ended without being started.");
			return;
			}
		
		if (localName.equals("DataTableSet")){
			if (table_set == null)
				throw new SAXException("DataSourceXMLHandler: DataTableSet ended without being started.");
			data_source.setTableSet(table_set);
			try{
				// Add remaining tables from metadata
				data_source.setTableSet(false);
			}catch (DataSourceException ex){
				InterfaceSession.log("DataSourceXMLHandler: Warning: Could not load all tables: " + ex.getMessage(), 
									 LoggingType.Debug);
				}
			table_set = null;
			return;
			}
		
		if (localName.equals("DataQueries")){
			if (queries == null)
				throw new SAXException("DataSourceXMLHandler: Queries ended without being started.");
			for (int i = 0; i < queries.size(); i++)
				data_source.addDataQuery(queries.get(i));
			queries = null;
			return;
			}
		
		if (localName.equals("DataQuery")){
			if (queries == null)
				throw new SAXException("DataSourceXMLHandler: Attempt to add query, but queries not started.");
			current_query.setSQLStatement(current_sql.trim());
			queries.add(current_query);
			current_query = null;
			return;
			}
		
		if (table_set != null){
			table_set.handleXMLElementEnd(localName);
			return;
			}
		
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		
		if (current_query != null){
			String append = new String(Arrays.copyOfRange(ch, start, start + length));
			append = XMLFunctions.getXMLDecodedString(append);
			append = XMLFunctions.getXMLUntabbedString(append, 1);
			current_sql = current_sql + append;
			return;
			}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}