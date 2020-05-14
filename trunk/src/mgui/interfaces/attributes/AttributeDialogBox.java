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

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.table.InterfaceAttributeTable;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;

/********************************************************
 * Non-modal, always-on-top dialog box which allows a user to select an attribute object and modify its attributes.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class AttributeDialogBox extends InterfaceDialogBox {

	AttributeObject current_object;
	
	InterfaceComboBox cmbObject = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem,
														true, 500);
	
	InterfaceAttributeTable attributes_table = new InterfaceAttributeTable();
	JScrollPane lstAttributes = new JScrollPane(attributes_table);
	
	boolean updateCombo = true;
	private boolean is_init = false; 
	protected Class<?> list_class = AttributeObject.class;
	
	public AttributeDialogBox(){
		
	}
	
	public AttributeDialogBox(Class<?> list_class){
		this.list_class = list_class;
	}
	
	public AttributeDialogBox(JFrame frame, AttributeObject object){
		this(frame, object, AttributeObject.class);
	}
	
	public AttributeDialogBox(JFrame frame, AttributeObject object, Class<?> list_class){
		super(frame);
		current_object = object;
		this.list_class = list_class;
		_init();
	}
	
	public Image getImage(){
		java.net.URL imgURL = AttributeDialogBox.class.getResource("/mgui/resources/icons/attribute_dialog_20.png");
		try{
			if (imgURL != null){
				ImageIcon icon = new ImageIcon(imgURL);
				return icon.getImage();
				}
				
			else
				InterfaceSession.log("Cannot find resource: /mgui/resources/icons/attribute_dialog_20.png");
		} catch (Exception e){
			e.printStackTrace();
			}
		return null;
	}
	
	void _init(){
		this.setButtonType(BT_BLANK);
		super.init();
		
		this.setAlwaysOnTop(true);
		this.setIconImage(this.getImage());
		
		
		setDialogSize(300, 450);
		setTitle("Attribute Editor");
		
		cmbObject.setActionCommand("Object Changed");
		cmbObject.addActionListener(this);
		
		LineLayout lineLayout = new LineLayout(20, 5, 0);
		setMainLayout(lineLayout);
		
		LineLayoutConstraints c = new LineLayoutConstraints(0, 0, 0.0, 1.0, 1);
		mainPanel.add(cmbObject, c);
		c = new LineLayoutConstraints(1, -1, 0.0, 1.0, 1);
		mainPanel.add(lstAttributes, c);
		
		is_init = true;
		
		updateCombo();
		updateAttributeTable();
		
	}
	
	public void showDialog(AttributeObject object){
		if (!is_init){
			current_object = object;
			_init();
		}else{
			setAttributeObject(object);
			}
		if (!this.isVisible())
			this.setVisible(true);
	}
	
	public void setAttributeObject(AttributeObject object){
		current_object = object;
		cmbObject.setSelectedItem(object);
		setTitle("Attribute Editor: '" + current_object.getName() + "'");
		updateCombo();
		updateAttributeTable();
	}
	
	@Override
	public boolean updateDialog(){
		updateCombo();
		return true;
	}
	
	void updateCombo(){
		if (!updateCombo || !is_init) return;
		updateCombo = false;
		
		ArrayList<AttributeObject> list = InterfaceSession.getWorkspace().getAttributeObjects();
		cmbObject.removeAllItems();
		
		boolean found = false;
		
		for (int i = 0; i < list.size(); i++){
			if (list_class.isInstance(list.get(i))){
				cmbObject.addItem(list.get(i));
				if (current_object != null && list.get(i).equals(current_object))
					found = true;
				}
			}
		
		if (found)
			cmbObject.setSelectedItem(current_object);
		else
			current_object = (AttributeObject)cmbObject.getSelectedItem();
		
		updateCombo = true;
	}
	
	void updateAttributeTable(){
		if (current_object == null){
			lstAttributes.setVisible(false);
			return;
			}
		
		attributes_table.setAttributes(current_object.getAttributes());
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Object Changed")){
			if (!updateCombo) return;
			updateCombo = false;
			setAttributeObject((AttributeObject)cmbObject.getSelectedItem());
			updateCombo = true;
			return;
			}
		
		super.actionPerformed(e);
	}
	
}