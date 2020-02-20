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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComboBox;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceComboBoxRenderer;
import mgui.interfaces.logs.LoggingType;

/*****************************************************
 * Represents an {@link Attribute} with possible values mapped with <code>String</code> keys.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 * @param <V>
 */
public class AttributeSelectionMap<V> extends Attribute<V> implements ItemListener {
	
	protected HashMap<String, V> map;
	protected String selected;
	protected InterfaceComboBoxRenderer renderer = new InterfaceComboBoxRenderer(true);
	protected ComboMode combo_mode = ComboMode.AsKeys;
	protected int combo_width = 500;
	
	public enum ComboMode{
		AsKeys,
		AsValues;
	}

	public AttributeSelectionMap(String name, HashMap<String, V> map, Class<V> clazz){
		super(name, clazz);
		this.map = map;
		if (map.size() == 0) return;
		ArrayList<String> list = new ArrayList<String>(map.keySet());
		select(list.get(0));
	}
	
	public AttributeSelectionMap(String name, HashMap<String, V> map, Class<V> clazz, String selected) {
		super(name, clazz);
		this.map = map;
		select(selected);
	}
		
	public void setComboWidth(int width){
		this.combo_width = width;
	}
	
	@Override
	public V getValue() {
		if (selected == null)
			return super.getValue();
		return map.get(selected);
	}

	public void setComboRenderer(InterfaceComboBoxRenderer renderer){
		this.renderer = renderer;
	}
	
	public ComboMode getComboMode(){
		return combo_mode;
	}
	
	public void setComboMode(ComboMode mode){
		combo_mode = mode;
	}
	
	/*****************************************
	 * Sets the current value, either as a key (if <code>value</code> is a String), or as the
	 * mapped value (instance of V).
	 * 
	 * <p>TODO: fails if V is instance of String; resolve
	 * 
	 */
	@Override
	public boolean setValue(Object value, boolean fire) {
		
		if (value instanceof String){
			if (!select((String)value)){
				InterfaceSession.log("AttributeSelectionMap: Key not in list: " + value.toString(), 
									 LoggingType.Errors);
				return false;
				}
			V new_value = getValue();
			if (object_class == null && value != null)
				object_class = (Class<V>)new_value.getClass();
			if (fire)
				super.fireAttributeListeners();
			return true;
			}
		
		if (value == null){
			this.selected = null;
			if (fire)
				super.fireAttributeListeners();
			return true;
			}
		
		try{
			V val = (V)value;
			if (!map.containsValue(val)){
				selected = null;
				return false;
				}
			
			ArrayList<String> keys = new ArrayList<String>(map.keySet());
			for (int i = 0; i < keys.size(); i++)
				if (map.get(keys.get(i)).equals(val))
					selected = keys.get(i);
			
			V new_value = getValue();
			if (new_value == null){
				InterfaceSession.log("AttributeSelection: Value assigned to key '" + selected + "' is null.", 
						 			  LoggingType.Errors);
				return false;
				}
			if (object_class == null && value != null)
				object_class = (Class<V>)new_value.getClass();
			
		}catch (ClassCastException e){
			InterfaceSession.log("AttributeSelection: Invalid value class type.", 
								 LoggingType.Errors);
			return false;
			}
		
		if (fire)
			super.fireAttributeListeners();
		return true;
	}
	
	protected boolean select(String key){
		if (key == null){
			selected = null;
			return true;
			}
		if (!map.containsKey(key)) return false;
		selected = key;
		return true;
		
	}

	/*********************
	 * Return a combo box containing this map, as either key values or mapped values (depending on
	 * the state of the ComboMode.
	 * 
	 * @return
	 */
	public JComboBox getComboBox(){
		InterfaceComboBox combo_box = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, combo_width);
		combo_box.setRenderer(renderer);
		
		populateComboBox(combo_box);
		
		combo_box.addItemListener(this);
		return combo_box;
	}
	
	public boolean populateComboBox(JComboBox combo_box){
		
		if (!ensure_updated())
			return false;
		
		ArrayList<?> list = null;
		Object select = selected;
		switch (combo_mode){
			case AsValues:
				list = new ArrayList<V>(map.values());
				select = map.get(selected);
				break;
			case AsKeys:
				list = new ArrayList<String>(map.keySet());
			}
		
		for (int i = 0; i < list.size(); i++)
			combo_box.addItem(list.get(i));
		
		combo_box.setSelectedItem(select);
		
		return true;
	}
	
	private boolean ensure_updated(){
		if (selected == null) return false;
		if (!map.containsKey(selected)){
			selected = null;
			return false;
			}
		return true;
	}
	
	public void itemStateChanged(ItemEvent e) {
		
		JComboBox box = (JComboBox)e.getSource();
		Object obj = box.getSelectedItem();
		switch (combo_mode){
			case AsKeys:
				if (obj == selected) return;
				break;
			case AsValues:
				if (selected != null &&
						obj == map.get(selected))
					return;
				break;
			}
		
		setValue(obj);
	}
	
}