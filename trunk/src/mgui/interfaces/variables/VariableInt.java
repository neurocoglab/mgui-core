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

package mgui.interfaces.variables;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiNumber;
import mgui.resources.icons.IconObject;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**********************************************
 * An interface to a variable object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class VariableInt<T> extends AbstractInterfaceObject
								  			implements AttributeObject, 
								  			 		   IconObject,
								  			 		   XMLObject{

	protected ArrayList<VariableListener> listeners = new ArrayList<VariableListener>();
	protected AttributeList attributes = new AttributeList();
	protected boolean batch = false, updated = false;
	
	protected void init(){
		attributes.add(new Attribute<String>("Name", "unnamed"));
		attributes.add(new Attribute<MguiBoolean>("IsEditable", new MguiBoolean(true)));
	}
	
	public abstract ArrayList<Integer> getDimensions();
	
	@Override
	public void setName(String name){
		attributes.setValue("Name", name);
		
	}
	
	public boolean isEditable(){
		return ((MguiBoolean)attributes.getValue("IsEditable")).getTrue();
	}
	
	public void setEditable(boolean b){
		attributes.setValue("IsEditable", new MguiBoolean(b));
	}
	
	@Override
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	public void setBatch(boolean b){
		this.batch = b;
		if (b == false && updated){
			fireListeners();
			}
	}
	
	protected void fireListeners(){
		if (!updated) return;
		VariableEvent event = new VariableEvent(this);
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).variableValuesUpdated(event);
		updated = false;
	}
	
	/****************************
	 * Returns the value of this variable at the given indices; throws an exception if they are out of bounds,
	 * or of a higher dimensionality than the variable supports. If the indices are of a lower dimensionality than 
	 * that of the variable, the returned object is handled by the subclass method. As a general policy, this 
	 * object should represent the remaining dimensions in some way (i.e., using array notation).
	 * 
	 * @param indices
	 * @return object at the given indices
	 * @throws VariableException if <code>indices.size()</code> is greater than this variable's dimensionality, or
	 * indices are out of bounds.
	 */
	public T getValueAt(List<Integer> indices) throws VariableException{
		ArrayList<Integer> dims = getDimensions();
		ArrayList<Integer> new_indices = null;
		
		// If necessary, resize indices to the dimensionality of this variable
		if (indices.size() > dims.size()){
			new_indices = new ArrayList<Integer>();
			for (int i = 0; i < dims.size(); i++)
				new_indices.add(indices.get(i));
		}else{
			new_indices = new ArrayList<Integer>(indices);
			}
		return getValue(new_indices);
	}
	
	/****************************
	 * Returns the value at the given indices; throws an exception if they are out of bounds. If the indices
	 * are of a lower dimension than the variable dimension, the returned object is handled by the subclass
	 * method. As a general policy, this object should represent the remaining dimensions in some way. 
	 * 
	 * @param indices
	 * @return object at the given indices
	 * @throws VariableException
	 */
	protected abstract T getValue(List<Integer> indices) throws VariableException;
	
	/****************************
	 * Sets the value at the given indices; throws an exception if they are out of bounds, this variable is
	 * not editable, or <code>value</code> is an inappropriate value. 
	 * 
	 * @param indices
	 * @return object at the given indices
	 * @throws VariableException
	 */
	public abstract boolean setValue(List<Integer> indices, T value) throws VariableException;
	
	/****************************
	 * Sets the value at the given indices as a <code>String</code>; throws an exception if they are out 
	 * of bounds, this variable is not editable, or <code>value</code> is an inappropriate value. 
	 * 
	 * @param indices
	 * @return object at the given indices
	 * @throws VariableException
	 */
	public abstract boolean setStringValue(List<Integer> indices, String value) throws VariableException;
	
	public void addListener(VariableListener listener){
		this.listeners.add(listener);
	}
	
	public void removeListener(VariableListener listener){
		this.listeners.remove(listener);
	}
	
	/*****************************************
	 * Determines whether this variable contains numeric values.
	 * 
	 * @return
	 */
	public boolean isNumeric(){
		Class<? extends T> type = getType();
		if (MguiNumber.class.isAssignableFrom(type) && !MguiBoolean.class.isAssignableFrom(type)) return true;
		if (Number.class.isAssignableFrom(type)) return true;
		return false;
	}
	
	private T getValueInstance() throws VariableException{
		ArrayList<Integer> dims = this.getDimensions();
		Collections.fill(dims, 0);
		return getValue(dims);
	}
	
	/**********************************************
	 * Returns a <code>Class</code> object which is the parameterized type of this variable.
	 * 
	 * @return
	 */
	public Class<? extends T> getType(){
		try{
			T value = getValueInstance();
			if (value != null) return (Class<T>)value.getClass();
		}catch (Exception e){
			
			}
		
		Type type = getClass(); //.getGenericSuperclass();
		TypeVariable<?>[] hmm = ((Class<?>)type).getTypeParameters();
		if (hmm != null && hmm.length > 0)
			return (Class<? extends T>)hmm[0].getClass();
		
		if (type == null || !(type instanceof ParameterizedType))
			//if (value != null) return (Class<V>)value.getClass();
			return (Class<? extends T>)Object.class;
        ParameterizedType paramType = (ParameterizedType) type;
        return (Class<? extends T>) paramType.getActualTypeArguments()[0];
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/variable_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/variable_20.png");
		return null;
	}
	
	/*****************************************************
	 * Parses <code>part</code> to return a part of this variable. 
	 * Syntax for n dimensions is: "<code>x_start,y_start,..,n_start:x_start,y_start,..,n_start</code>".
	 * E.g., "<code>1,2:20,2</code>". Use asterisk, "*" to indicate last element in a dimension.
	 * E.g,, "<code>1,2:*,*</code>".
	 * 
	 * @param name
	 * @param part
	 * @return
	 */
	public VariableInt<T> getPart(String part){
		return getPart("No-name", part);
	}
	
	/*****************************************************
	 * Parses <code>part</code> to return a part of this variable. 
	 * Syntax for n dimensions is: "<code>x_start,y_start,..,n_start:x_start,y_start,..,n_start</code>".
	 * E.g., "<code>1,2:20,2</code>". Use asterisk, "*" to indicate last element in a dimension.
	 * E.g,, "<code>1,2:*,*</code>".
	 * 
	 * @param name
	 * @param part
	 * @return
	 */
	public abstract VariableInt<T> getPart(String name, String part);
	
	/******************************************
	 * Returns this variable as a single list of values.
	 * 
	 * @return
	 */
	public abstract ArrayList<T> getAsList();
	
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
	public Object getAttributeValue(String name) {
		Attribute<?> attribute = getAttribute(name);
		if (attribute == null) return null;
		return attribute.getValue();
	}

	@Override
	public AttributeList getAttributes() {
		return attributes;
	}
	
	/******************************************
	 * Returns the total size (number of elements) of this variable.
	 * 
	 * @return
	 */
	public abstract int getSize();

	@Override
	public void setAttribute(String attrName, Object newValue) {
		attributes.setValue(attrName, newValue);
	}

	@Override
	public void setAttributes(AttributeList thisList) {
		attributes = thisList;
	}
	
	@Override
	public String toString(){
		return getName() + " (" + getType().getSimpleName() + ")";
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
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type)
			throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLElementEnd(String localName) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLString(String s) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLocalName() {
		return this.getClass().getCanonicalName();
	}

	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options,
			ProgressUpdater progress_bar) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeXML(int tab, Writer writer) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getShortXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}
	
}