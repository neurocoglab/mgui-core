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

package mgui.interfaces.attributes;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.tree.AttributeTreeNode;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLException;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;
import mgui.numbers.NumberFunctions;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/********************************
 * Class serves as a general-purpose attribute. Attributes are stored as Objects, with
 * a String identifier. Once an Attribute is set, all subsequent set operations perform
 * a check to ensure the new value is an instance of the original class (or its subclass).
 * Attributes store an AttributeNode object, which is null until it is requested.
 * 
 * @author Andrew Reid
 * @see AttributeTreeNode
 */

public class Attribute<V> extends AbstractInterfaceObject implements Cloneable,
																  XMLObject,
																  Comparable<Attribute<V>>{


	protected String name;
	protected V value;
	protected Class<V> object_class; // = Object.class;
	protected boolean isNumeric;
	protected boolean isEditable = true;
	protected boolean isSecret = false;
	private ArrayList<AttributeListener> attributeListeners = new ArrayList<AttributeListener>();
	
	// Shouldn't be called except as default by subclasses
	protected Attribute() {
		
	}

	public Attribute(String name, Class<V> clazz) {
		this.name = name;
		object_class = clazz;
	}
	
	public Attribute (String thisName, V value){
		name = thisName;
		setValue(value);
		isNumeric = (value instanceof mgui.numbers.MguiNumber ||
					 value instanceof Number);
	}
	
	public Attribute (String thisName, V value, boolean isNum){
		name = thisName;
		setValue(value);
		isNumeric = isNum;
	}
	
	public Attribute (String thisName, V value, boolean isNum, boolean isEditable){
		name = thisName;
		setValue(value);
		isNumeric = isNum;
		this.isEditable = isEditable;
	}
	
	
	@Override
	public int compareTo(Attribute<V> attribute) {
		return name.compareTo(attribute.getName());
	}
	
	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public void setName(String name){
		this.name = name;
	}
	
	@Override
	public String getTreeLabel(){
		return name;
	}
	
	public void setEditable(boolean b){
		isEditable = b;
	}
	
	public boolean isEditable(){
		return isEditable;
	}
	
	public boolean isNumeric(){
		return NumberFunctions.isNumeric(value); // isNumeric;
	}
	
	public void setSecret(boolean b){
		isSecret = b;
		//if (b) isEditable = false;	//secret attributes cannot be editable
	}
	
	public boolean isSecret(){
		return isSecret;
	}
	
	public void setFromAttribute(Attribute<V> a){
		setValue(a.getValue());
	}
	
	public void setFromAttribute(Attribute<V> a, boolean fire){
		setValue(a.getValue(), fire);
	}
	
	/***************************************
	 * Sets the value of this attribute, and notifies its listeners of the change. The value will 
	 * only be changed if "isEditable" is {@code true}.
	 * 
	 * @param value
	 * @return
	 */
	public boolean setValue(Object value) {
		return setValue(value, true);
	}
	
	/***************************************
	 * Sets the value of this attribute.
	 * 
	 * @param value
	 * @param fire 				Whether to notify listeners
	 * @return
	 */
	public boolean setValue(Object value, boolean fire) {
		try{
			this.value = (V)value;
			if (object_class == null && value != null)
				object_class = (Class<V>)value.getClass();
			
			if (fire)
				fireAttributeListeners();
			
			return true;
		
		}catch (ClassCastException e){
			InterfaceSession.log("Attribute [" + this.getName() + "]: Invalid value class type.", LoggingType.Errors);
			return false;
			}
	}
	
	/***************************************
	 * Sets the value of this attribute.
	 * 
	 * @param value
	 * @param fire 				Whether to notify listeners
	 * @param ignore_editable   If {@code true}, ignores this attribute's "isEditable" state;
	 * 						    otherwise the value will only be changed if "isEditable" is {@code true}
	 * @return
	 */
	public boolean setValue(Object value, boolean fire, boolean ignore_editable) {
		
		if (!isEditable() && !ignore_editable){
			return false;
			}

		return setValue(value, fire);
	}
	
	/*************************************************
	 * Returns the value of this {@code Attribute}.
	 * 
	 * @return
	 */
	public V getValue(){
		return value;
	}
	
	@Override
	public Object clone(){
		if (value instanceof MguiNumber)
			return new Attribute<V>(name, value, true);
		Object obj = value;
		try{
			if (value instanceof Cloneable) 
				obj = ((Cloneable)value).getClass().getMethod("clone", null).invoke(obj, null);
		} catch (Exception e){
			InterfaceSession.log("Error cloning Attribute, class: " + obj.getClass().getName());
			//e.printStackTrace();
			return new Attribute<V>(name, value, isNumeric);
			}
		return new Attribute<V>(name, value, isNumeric);
	}
	
	@Override
	public String toString(){
		Object value = getValue();
		if (value == null) return "Null";
		if (isSecret) return getName() + " [*****]";
		return getName() + " [" + value.toString() + "]";
	}
	
	@Override
	public InterfaceTreeNode issueTreeNode(){
		AttributeTreeNode treeNode = new AttributeTreeNode(this);
		setTreeNode(treeNode);
		tree_nodes.add(treeNode);
		return treeNode;
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		ArrayList<InterfaceTreeNode> nodes = new ArrayList<InterfaceTreeNode>(tree_nodes);
		
		//if any children are in treeNode list, remove them (otherwise we have a memory leak)
		Enumeration children = treeNode.children();
		while (children.hasMoreElements()){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)children.nextElement();
			if (nodes.contains(node)) tree_nodes.remove(node);
			}
	}
	
	public String getValueStr(){
		Object value = getValue();
		if (value == null) return "Null";
		if (isSecret)
			return "*****";
		return value.toString();
	}
	
	public String getValueStr(String pattern){
		Object value = getValue();
		if (isSecret)
			return "*****";
		if (value instanceof MguiDouble)
			return ((MguiDouble)value).toString(pattern);
		if (value instanceof MguiFloat)
			return ((MguiFloat)value).toString(pattern);
		if (value instanceof MguiDouble)
			return ((MguiInteger)value).toString(pattern);
		if (value == null)
			return "Null";
		return value.toString();
	}
	
	public void addAttributeListener(AttributeListener a){
		attributeListeners.add(a);
	}
	
	public void removeAttributeListener(AttributeListener a){
		attributeListeners.remove(a);
	}
	
	public void fireAttributeListeners(){
		AttributeEvent e = new AttributeEvent(this);
		for (int i = 0; i < attributeListeners.size(); i++)
			attributeListeners.get(i).attributeUpdated(e);
	}
	
	public String getDTD() {
		
		return null;
	}

	public String getLocalName() {
		
		return "Attribute";
	}

	public String getXML() {
		
		return getXML(0);
	}

	public String getXML(int tab) {
		
		String _tab = XMLFunctions.getTab(tab);
		
		String xml = _tab + "<Attribute name = '" + getName() + "' ";
		//xml = xml + "object_class = '" + objClass.getCanonicalName() + "'>\n";
		
		xml = xml + XMLFunctions.getXMLForObject(value, tab + 1);
		
		xml = xml + _tab + "</Attribute>\n";
		
		return xml;
	}

	public String getXMLSchema() {
		return null;
	}

	public String getShortXML(int tab) {
		return getXML();
	}
	
	public void handleXMLElementEnd(String localName) throws SAXException{
		if (value != null && value instanceof XMLObject)
			((XMLObject) value).handleXMLElementEnd(localName);
			
	}
	
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) throws SAXException{
		
		if (value == null){
			String class_name = attributes.getValue("object_class");
			if (class_name.equals("null"))
				class_name = "java.lang.Object";
			setValue((V)XMLFunctions.getObjectForXML(localName, attributes, class_name));
			return;
			}
		
		if (value instanceof XMLObject)
			((XMLObject) value).handleXMLElementStart(localName, attributes, null);
		
	}
	
	/**********************************************
	 * Returns the object class for this parameterized {@link Attribute}.
	 * 
	 * @return
	 */
	public Class<V> getObjectClass(){
		
		return this.object_class;
		
	}
	
	/***************************************************
	 * Attempts to handle an XML string intended to set the value of this
	 * {@code Attribute}. 
	 * 
	 */
	public void handleXMLString(String string) throws SAXException{
		if (value instanceof XMLObject){
			((XMLObject) value).handleXMLString(string);
			return;
			}
		
		try{
			Class<?> c = getObjectClass();
			if (string.equalsIgnoreCase("null"))
				setValue(null);
			else
				setValue((V)XMLFunctions.getObjectForXMLString(c, string));
		}catch (XMLException e){
			InterfaceSession.log("Attribute: XML Warning: " + e.getLocalizedMessage(),
								 LoggingType.Errors);
			}
		return;
		
	}
	
	public static XMLObject getXMLInstance(Attributes attributes){
		try{
			Class<?> clazz = Class.forName(attributes.getValue("object_class"));
			Attribute<?> attribute = getGenericInstance(attributes.getValue("name"),
														clazz);
			return attribute;
		}catch (Exception e){
			InterfaceSession.handleException(e);
			//e.printStackTrace();
			return null;
		}
	}
	
	protected static <T> Attribute<T> getGenericInstance(String name, Class<T> clazz){
		return new Attribute<T>(name, clazz);
	}
	
	public void writeXML(int tab, Writer writer) throws IOException{
		writeXML(tab, writer, null);
	}
	
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException{
		this.writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
		this.writeXML(tab, writer, XMLType.Normal, XMLEncoding.Ascii, progress_bar);
	}
	
	public void writeXML(int tab, Writer writer, XMLType type, XMLEncoding format, ProgressUpdater progress_bar) throws IOException{
		String _tab = XMLFunctions.getTab(tab);
		
		String xml = _tab + "<Attribute name = '" + getName() + "' ";
		V value = getValue();
		Class<V> c = this.getObjectClass();
		if (c != null){
			String class_name = c.getCanonicalName();
			xml = xml + "object_class = '" + class_name + "'> ";
		}else{
			xml = xml + "object_class = 'null'> ";
			}
				
		xml = xml + XMLFunctions.getXMLForAttributeObject(value, 0);
		xml = xml + " </Attribute>\n";
		
		writer.write(xml);
	}
	
	
}