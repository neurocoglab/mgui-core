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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.JComboBox;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.logs.LoggingType;

/*******************************************************
 * Extends {@link Attribute} to allow a list of options, one or none of which is the current value.
 * This form of the <code>Attribute</code> class allows attributes to be displayed as a set of possible
 * options, and provides a {@link JComboBox} for rendering. 
 * 
 * <p>The link to the list that is passed to the constructor is maintained; thus, external updates to that list
 * will result in a change to this <code>AttributeSelection</code>'s list.
 * 
 * <p>If a value is set using the <code>setValue</code> method, which is not in the list,
 * the attribute will assume that value, but it will not be added to the list.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class AttributeSelection<V> extends Attribute<V> implements ItemListener {

	protected ArrayList<V> list;
	//protected V selected;
	protected int listWidth;
	protected InterfaceComboBox combo_box;
	protected boolean allow_unlisted = false;
	
	/*************************************
	 * Constructs a new attribute selection with default initial selection.
	 * 
	 * @param name
	 * @param list
	 * @param clazz 	Class for this object; necessary for generics
	 */
	public AttributeSelection(String name, ArrayList<V> list, Class<V> clazz){
		super(name, clazz);
		setList(list);
		
	}
	
	/*************************************
	 * Constructs a new attribute selection with the specified initial selection.
	 * 
	 * @param name
	 * @param list
	 * @param selection
	 */
	public AttributeSelection(String name, ArrayList<V> list, Class<V> clazz, V selection){
		this(name, list, clazz, selection, 0);
		//setList(list);
	}
	
	/*************************************
	 * Constructs a new attribute selection with the specified initial selection and
	 * combo box width.
	 * 
	 * @param name
	 * @param list
	 * @param selection
	 */
	public AttributeSelection(String name, ArrayList<V> list, Class<V> clazz, V selection, int width){
		super(name, clazz);
		setList(list);
		select(selection);
	}
	
	/************************************
	 * Set whether this list allows unlisted values
	 * 
	 * @param b
	 */
	public void allowUnlisted(boolean b){
		this.allow_unlisted = b;
	}
	
	/************************************
	 * Whether this list allows unlisted values
	 * 
	 */
	public boolean allowsUnlisted(){
		return allow_unlisted;
	}
	
	/*******************************
	 * Sets the current selection. If the selection is not in the list, does nothing and
	 * returns false;
	 * 
	 * @param selection
	 * @return <code>true</code> if the selection is in the list, <code>false</code> otherwise.
	 */
	public boolean select(Object selection){
		if (list == null && !allow_unlisted) return false;
		if (selection == null){
			value = null;
			return true;
			}
		if (list == null) this.value = (V)selection;
		if (list.contains(selection) || allow_unlisted){
			value = (V)selection;
			return true;
			}
			
		return false;
	}
	
	@Override
	public boolean setValue(Object value, boolean fire){
		if (!select(value)){
			return false;
			}
		if (object_class == null && value != null)
			this.object_class = (Class<V>)value.getClass();
		if (fire)
			super.fireAttributeListeners();
		return true;
	}
	
	
	public void setList(ArrayList<V> values){
		list = values;
		value = null;
	}
	
	public ArrayList<?> getList(){
		return list;
	}
	
	/*********************
	 * Return a combo box containing this list, and with the specified list width
	 * 
	 * @return
	 */
	public InterfaceComboBox getComboBox(){
		return getComboBox(500);
	}
	
	/*********************
	 * Return a combo box containing this list, and with the specified list width
	 * 
	 * @return
	 */
	public InterfaceComboBox getComboBox(int width){
		if (combo_box != null){
			combo_box.removeItemListener(this);
			}
		ensure_updated();
		combo_box = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, width);
		combo_box.addItem(null);
		for (int i = 0; i < list.size(); i++)
			combo_box.addItem(list.get(i));
		if (value != null){
			if (!list.contains(value) && allow_unlisted)
				combo_box.addItem(value);
			combo_box.setSelectedItem(value);
			}
		combo_box.addItemListener(this);
		return combo_box;
	}
	
	private boolean ensure_updated(){
		if (value == null || allow_unlisted) return true;
		for (int i = 0; i < list.size(); i++)
			if (value.equals(list.get(i)))
				return true;
		value = null;
		return false;
	}
	
	public void itemStateChanged(ItemEvent e) {
		if (combo_box == null) return;
		V obj = (V)combo_box.getSelectedItem();
		if (obj == value) return;
		setValue(obj);
		//this.fireAttributeListeners();
	}
}