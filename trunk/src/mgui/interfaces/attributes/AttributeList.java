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

package mgui.interfaces.attributes;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.resources.icons.IconObject;

import org.xml.sax.Attributes;

/*******************************************************
 * Stores a list of {@link Attribute} objects, and provides methods to access and modify them, as well as
 * set methods to intersect and union with other lists. This class also listens for changes on its
 * attributes and can inform all listeners of these changes. 
 * 
 * <p>It provides XML reading writing implementations.
 * 
 * <p>It constructs its own tree nodes vis the <code>setTreeNode</code> method; these nodes can be used to modify
 * attribute values.
 * 
 * The <code>AttributeList</code> should be used for all {@link InterfaceObject} instances which require attributes.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class AttributeList extends AbstractInterfaceObject 
						   implements Cloneable,
							  		  AttributeListener,
							  		  IconObject,
							  		  XMLObject {

	//protected ArrayList<Attribute> attributes;
	protected HashMap<String, Attribute<?>> attributes;
	protected HashMap<String, ArrayList<Attribute<?>>> category_attributes = new HashMap<String, ArrayList<Attribute<?>>>();
	//private AttributeComparator attrComp; 
	private ArrayList<AttributeListener> attributeListeners = new ArrayList<AttributeListener>();
	public boolean updateable = true;
	
	public AttributeList(){
		//attributes = new ArrayList<Attribute>();
		attributes = new HashMap<String, Attribute<?>>();
		//attrComp = new AttributeComparator();
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/attribute_set_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/attribute_set_20.png");
		return null;
	}
	
	public ArrayList<Attribute<?>> getAsList(){
		ArrayList<Attribute<?>> list = new ArrayList<Attribute<?>>(attributes.values());
		Collections.sort(list);
		return list;
	}
	
	public boolean hasAttribute(String name){
		return attributes.containsKey(name);
	}
	
	public boolean hasAttribute(Attribute<?> attribute){
		return attributes.containsKey(attribute.getName());
	}
	
	@Override
	public String getTreeLabel(){
		return "Attributes";
	}
	
	/*****************************
	 * Adds an attribute to the default list
	 * 
	 * @param thisAttr
	 */
	public void add(Attribute<?> attribute){
		if (attribute.getName() == null){
			InterfaceSession.log("AttributeList.add: Attribute must have a name..", LoggingType.Errors);
			return;
			}
		attributes.put(attribute.getName(), attribute);
		attribute.addAttributeListener(this);
		//Collections.sort(attributes, attrComp);
	}
	
	/*****************************
	 * Adds an attribute to the specified category. If the category does not exist, creates a new one.
	 * 
	 * @param category
	 * @param attribute
	 */
	public void add(String category, Attribute<?> attribute){
		ArrayList<Attribute<?>> attributes = addCategory(category);
		attributes.add(attribute);
		attribute.addAttributeListener(this);
		Collections.sort(attributes); //, attrComp);
	}
	
	public ArrayList<Attribute<?>> addCategory(String category){
		ArrayList<Attribute<?>> attributes = category_attributes.get(category);
		if (attributes == null) {
			attributes = new ArrayList<Attribute<?>>();
			category_attributes.put(category, attributes);
			}
		return attributes;
	}
	
	public void removeCategory(String category){
		category_attributes.remove(category);
	}
	
	public void remove(String name){
		attributes.remove(name);
	}
	
	public void remove(Attribute<?> thisAttr){
		attributes.remove(thisAttr.getName());
	}
	
	public void replace(Attribute<?> thisAttr){
		remove(thisAttr.getName());
		add(thisAttr);
	}
	
	/************************
	 * Get the current value of the attribute named {@code name}.
	 * 
	 * @param name
	 * @return
	 */
	public Object getValue(String name){
		if (!attributes.containsKey(name)) return null;
		return attributes.get(name).getValue();
	}
	
	/**********************
	 * Sets the given attribute. If an attribute by this name already exists, it is overriden.
	 * 
	 * @param thisAttr
	 * @return
	 */
	public boolean setAttribute(Attribute<?> thisAttr){
		return setAttribute(thisAttr, true);
	}
	
	public <V> boolean setAttribute(Attribute<V> attribute, boolean fire){
		Attribute<V> list_attribute = (Attribute<V>)attributes.get(attribute.getName());
		if (list_attribute == null){
			//add Attribute
			add((Attribute<V>)attribute.clone());
			return true;
		}
		
		if (list_attribute.getObjectClass().isInstance(attribute.getValue())){
			list_attribute.setValue(attribute.getValue(), false);
			if (updateable && fire)
				fireAttributeListeners(list_attribute);
			return true;
			}
		return false;
	}
	
	/*************************
	 * Returns the attribute at the specified index. Note: this function will be deprecated.
	 * 
	 * @param index
	 * @return
	 */
	public Attribute<?> getAttribute(int index){
		return getAttribute(getKeys().get(index));
	}
	
	/**************************
	 * Gets the {@code Attribute} object for the specified name; returns {@code null} if
	 * this attribute does not exist.
	 * 
	 * @param name
	 * @return
	 */
	public Attribute<?> getAttribute(String name){
		/*
		int i = Collections.binarySearch(attributes, thisAttr, attrComp);
		if (i < 0) return null;
		*/
		return attributes.get(name);
	}
	
	/**************************************************
	 * Sets the value of the attribute represented by <code>name</code>; ignores the
	 * {@code isEditable} flag.
	 * 
	 * @param thisAttr
	 * @param thisValue
	 * @return
	 */
	public <V> boolean setValueForced(String name, V value){
		try{
			Attribute<V> list_attribute = (Attribute<V>)attributes.get(name);
			boolean editable = list_attribute.isEditable();
			if (!editable) list_attribute.setEditable(true);
			boolean success = setValue(name, value, true);
			if (!editable)
				list_attribute.setEditable(false);
			return success;
		}catch (ClassCastException e){
			InterfaceSession.log("AttributeList: Attribute value is wrong class type.", 
								 LoggingType.Errors);
			return false;
			}
	}
	
	/**************************************************
	 * Sets the value of the attribute represented by <code>name</code>.
	 * 
	 * @param thisAttr
	 * @param thisValue
	 * @return
	 */
	public <V> boolean setValue(String name, V value){
		return setValue(name, value, true);
	}
	
	//TODO throw exception here
	public <V> boolean setValue(String name, V value, boolean fire){
		
		try{
			Attribute<V> list_attribute = (Attribute<V>)attributes.get(name);
		
			if (list_attribute == null) return false;
			
			if (!list_attribute.setValue(value, false))
				return false;
			if (updateable && fire)
				fireAttributeListeners(list_attribute);
			
			return true;
		}catch (ClassCastException e){
			e.printStackTrace();
			InterfaceSession.log("AttributeList: Attribute value is wrong class type.", 
								 LoggingType.Errors);
			return false;
			}
	}

	public <V> boolean setValue(Attribute<V> attribute){
		
		try{
			Attribute<V> list_attribute = (Attribute<V>)attributes.get(attribute.getName());
			if (list_attribute == null) return false;
			
			list_attribute.setValue(attribute.getValue(), false);
			if (updateable)
				fireAttributeListeners(list_attribute);
			
			return true;
		}catch (ClassCastException e){
			InterfaceSession.log("AttributeList: Attribute value is wrong class type.", 
								 LoggingType.Errors);
			return false;
			}
		
	}
	
	public void addAttributeListener(AttributeListener a){
		attributeListeners.add(a);
	}
	
	public void removeAttributeListener(AttributeListener a){
		attributeListeners.remove(a);
	}
	
	public void fireAttributeListeners(Attribute<?> a){
		AttributeEvent e = new AttributeEvent(a);
		for (int i = 0; i < attributeListeners.size(); i++)
			attributeListeners.get(i).attributeUpdated(e);
	}
	
	public void setAttributeListeners(AttributeList a){
		attributeListeners = a.attributeListeners;
	}
	
	@Override
	public String toString(){
		return "Attributes";
	}
	
	public int getSize(){
		return attributes.size();
	}
	
	@Override
	public Object clone(){
		AttributeList retObj = new AttributeList();
		ArrayList<String> keys = getKeys();
		
		for (int i = 0; i < keys.size(); i++)
			retObj.add((Attribute<?>)attributes.get(keys.get(i)).clone());
		return retObj;
	}
	
	public void attributeUpdated(AttributeEvent e){
		fireAttributeListeners((Attribute<?>)e.getSource());
	}
	
	public ArrayList<String> getKeys(){
		ArrayList<String> keys = new ArrayList<String>(attributes.keySet());
		Collections.sort(keys);
		return keys;
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		
		ArrayList<String> keys = getKeys();
		
		for (int i = 0; i < keys.size(); i++)
			treeNode.addChild(attributes.get(keys.get(i)).issueTreeNode());
		
		//add categorized attributes
		ArrayList<String> categories = new ArrayList<String>(category_attributes.keySet());
		for (int i = 0; i < categories.size(); i++){
			ArrayList<Attribute<?>> attributes = category_attributes.get(categories.get(i));
			InterfaceTreeNode node = new InterfaceTreeNode(categories.get(i));
			for (int j = 0; j < attributes.size(); j++)
				node.addChild(attributes.get(i).issueTreeNode());
			treeNode.addChild(node);
			}
		
	}
	
	/*********************
	 * Set any attributes which intersect with the passed list.
	 *
	 */
	public void setIntersection(AttributeList list){
		setIntersection(list, true);
	}
	
	/*********************
	 * Set any attributes which intersect with the passed list.
	 *
	 */
	public void setIntersection(AttributeList list, boolean fire){
		
		ArrayList<String> keys = list.getKeys();
		
		for (int i = 0; i < keys.size(); i++){
			String name = keys.get(i);
			Attribute<?> attr = getAttribute(name);
			if (attr != null)
				attr.setValue(list.getValue(name), fire, true);
			}
	}
	
	/************************************************
	 * Sets this {@code AttributeList} to the union of itself and {@code list}. The values 
	 * of attributes which already exist in this list are updated to reflect the new values.
	 * 
	 * @param list
	 */
	public void setUnion(AttributeList list){
		setUnion(list, true);
	}
	
	/************************************************
	 * Sets this {@code AttributeList} to the union of itself and {@code list}. If {@code overwrite}
	 * is {@code true}, the values of attributes which already exist in this list are updated to 
	 * reflect the new values; otherwise the current values are preserved. Note that if the attribute's
	 * type is different in the new list, overwriting will also change the type.
	 * 
	 * @param list
	 * @param overwrite
	 */
	public void setUnion(AttributeList list, boolean overwrite){
		
		ArrayList<String> keys = list.getKeys();
		
		for (int i = 0; i < keys.size(); i++){
			String name = keys.get(i);
			Attribute<?> attr = getAttribute(name);
			if (attr != null)
				if (overwrite)
					this.replace((Attribute<?>)list.getAttribute(name).clone());
			else
				this.add((Attribute<?>)list.getAttribute(name).clone());
			}
		
	}
	
	public String getDTD() {
		return null;
	}

	public String getLocalName() {
		return "AttributeList";
	}

	public String getXML() {
		return getXML(0);
	}

	public String getXML(int tab) {
		
		String _tab = XMLFunctions.getTab(tab);
		String xml = _tab + "<AttributeList>\n";
		
		ArrayList<String> keys = getKeys();
		
		for (int i = 0; i < keys.size(); i++)
			xml = xml + attributes.get(keys.get(i)).getXML(tab + 1);
		
		xml = xml + _tab + "</AttributeList>\n";
		
		return xml;
	}

	public String getXMLSchema() {
		return null;
	}

	public void handleXMLElementEnd(String localName) {
		
	}

	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		
	}

	public void handleXMLString(String s) {
		
	}
	
	public static XMLObject getXMLInstance(Attributes attributes){
		return new AttributeList();
	}
	
	public void writeXML(int tab, Writer writer) throws IOException{
		writeXML(tab, writer, null);
	}
	
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException{
		this.writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
		
		String _tab = XMLFunctions.getTab(tab);
		ArrayList<String> keys = getKeys();
		
		writer.write(_tab + "<AttributeList>\n");
		for (int i = 0; i < keys.size(); i++)
			attributes.get(keys.get(i)).writeXML(tab + 1, writer, progress_bar);
		writer.write(_tab + "</AttributeList>");
		
	}
	
	public String getShortXML(int tab) {
		return getXML();
	}
	
}