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

package mgui.datasources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.resources.icons.IconObject;

import org.xml.sax.Attributes;

/*********************************
 * Specifies a basic JDBC database connection, including:
 * 
 * <ul>
 * <li>JDBC driver
 * <li>URL
 * <li>Connection name
 * <li>File location
 * <li>Login
 * <li>Password //TODO implement security on this
 * </ul>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0.0
 *
 *****/
public class DataConnection extends AbstractInterfaceObject 
							implements AttributeObject,
									   IconObject,
									   Cloneable,
									   XMLObject{

	public AttributeList attributes;
	
	public DataConnection(){
		init();
	}
	
	public DataConnection(String name){
		init();
		setName(name);
	}
	
	private void init(){
		attributes = new AttributeList();
		attributes.add(new Attribute<String>("driver" , ""));
		attributes.add(new Attribute<String>("url" , ""));
		attributes.add(new Attribute<String>("name" , "no-name"));
		attributes.add(new Attribute<File>("file" , new File("")));
		attributes.add(new Attribute<String>("login" , ""));
		Attribute<String> a = new Attribute<String>("password" , "");
		a.setSecret(true);
		attributes.add(a);
		
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/data_connection_17.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/data_connection_17.png");
		return null;
	}
	
	public boolean isValid(){
		if (((String)attributes.getValue("driver")).length() == 0 ||
			((String)attributes.getValue("url")).length() == 0 ||
			((String)attributes.getValue("name")).length() == 0)
				return false;
		return true;
	}
	
	//getters
	public String getDriver(){
		return (String)attributes.getValue("driver");
	}
	
	public String getUrl(){
		return (String)attributes.getValue("url");
	}
	
	@Override
	public String getName(){
		return (String)attributes.getValue("name");
	}
	
	public String getLogin(){
		return (String)attributes.getValue("login");
	}
	
	/*********************************
	 * Returns the password for this connection. Password should be decrypted
	 * using <code>SecurityFunctions.getKeyDecryptedString()</code>.
	 * @param s
	 */
	public String getPassword(){
		return (String)attributes.getValue("password");
	}
	
	public File getFile(){
		return (File)attributes.getValue("file");
	}
	
	//setters
	public void setDriver(String s){
		attributes.setValue("driver", s);
	}
	
	public void setUrl(String s){
		attributes.setValue("url", s);
	}
	
	@Override
	public void setName(String s){
		attributes.setValue("name", s);
	}
	
	public void setLogin(String s){
		attributes.setValue("login", s);
	}
	
	/*********************************
	 * Sets the password for this connection. Password should already be encrypted
	 * using <code>SecureDataSourceFunctions.getEncryptedPassword()</code>.
	 * @param s
	 */
	public void setPassword(String s){
		attributes.getAttribute("password").setValue(s);
		
	}
	
	public void setFile(File f){
		attributes.getAttribute("file").setValue(f);
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		treeNode.add(attributes.issueTreeNode());
	}
	
	@Override
	public Attribute<?> getAttribute(String attrName) {	
		return attributes.getAttribute(attrName);
	}

	@Override
	public AttributeList getAttributes() {
		return attributes;
	}

	@Override
	public void setAttribute(String attrName, Object newValue) {
		attributes.setValue(attrName, newValue);
	}

	@Override
	public void setAttributes(AttributeList thisList) {
		attributes = thisList;
	}
	
	@Override
	public Object getAttributeValue(String name) {
		Attribute<?> attribute = getAttribute(name);
		if (attribute == null) return null;
		return attribute.getValue();
	}

	public boolean setFromFile(File file) throws IOException{
		if (!file.exists()) throw new IOException("File not found: '" + file.getAbsolutePath() + "'");
		
		//try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			
			while (line != null){
			int c = line.indexOf("=");
				if (c > 0 && c < line.length()){
					String item = line.substring(0, c);
					String value = line.substring(c + 1);
					if (item.equals("name")) setName(value);
					if (item.equals("file")) setFile(new File(value));
					if (item.equals("password")) 
						setPassword(value);
					if (item.equals("login")) setLogin(value);
					if (item.equals("url")) setUrl(value);
					if (item.equals("driver")) setDriver(value);
					}
				line = reader.readLine();
				}
			reader.close();
			
		return true;
	}
	
	@Override
	public String getTreeLabel(){
		return "Connection";
	}
	
	@Override
	public String toString(){
		return "Data Connection";
	}
	
	@Override
	public Object clone(){
		DataConnection c = new DataConnection();
		c.attributes = (AttributeList)attributes.clone();
		return c;
	}

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
		return "DataConnection";
	}

	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		writer.write(_tab + "<DataConnection" + 
					 "\n" + _tab2 + "name='" + getName() + "'" +
					 "\n" + _tab2 + "driver='" + getDriver() + "'" +
					 "\n" + _tab2 + "login='" + getLogin() + "'" +
					 "\n" + _tab2 + "password='" + getPassword() + "'" +
					 "\n" + _tab2 + "url='" + getUrl() + "'" +
					 "\n" + _tab2 + "file='" + getFile() + "'" +
					 "\n" + _tab + "/>\n");
		
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
		
		return null;
	}
	
}