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

package mgui.interfaces.plots;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.resources.icons.IconObject;

/**********************************************
 * General dialog box for generation a new plot object. Provides a list of available
 * plot types and then calls a dialog specific to that type.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class NewPlotDialog extends InterfaceDialogBox implements ListSelectionListener{

	JList type_list;
	JScrollPane scrTypeList;
	TypeModel type_model;
	JLabel lblChoose = new JLabel("Choose plot type:");
	
	protected InterfacePlot<?> new_plot;
	//InterfacePlotDialog<?> plot_dialog;
	
	public NewPlotDialog(){
		super();
		_init();
	}
	
	public NewPlotDialog(JFrame frame){
		super(frame);
		_init();
	}
	
	public static InterfacePlot<?> showDialog(JFrame frame){
		NewPlotDialog dialog = new NewPlotDialog(frame);
		dialog.setVisible(true);
		return dialog.getNewPlot();
	}
	
	private void _init(){
		this.setButtonType(BT_OK_CANCEL);
		super.init();
		
		this.setTitle("Create New Plot");
		
		LineLayout layout = new LineLayout(20, 5, 0);
		setMainLayout(layout);
		setDialogSize(400, 400);
		cmdOK.setText("Next >");
		cmdOK.setEnabled(false);
		
		type_model = new TypeModel();
		//type_list.setModel(type_model);
		type_list = new JList(type_model);
		type_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		type_list.setCellRenderer(new TypeRenderer());
		type_list.addListSelectionListener(this);
		
		scrTypeList = new JScrollPane(type_list);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.9, 1);
		mainPanel.add(lblChoose, c);
		c = new LineLayoutConstraints(2, 11, 0.05, 0.9, 1);
		mainPanel.add(scrTypeList, c);
	}
	
	public InterfacePlot<?> getNewPlot(){
		return this.new_plot;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			
			InterfacePlot<?> plot = (InterfacePlot<?>)type_list.getSelectedValue();
			if (plot == null){
				cmdOK.setEnabled(false);
				return;
				}
			
			this.new_plot = plot;
			this.setVisible(false);
			
			return;
			}
		
		super.actionPerformed(e);
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		
		if (type_list.getSelectedIndex() > -1)
			cmdOK.setEnabled(true);
		else
			cmdOK.setEnabled(false);
		
	}
	
	static class TypeModel extends AbstractListModel{

		ArrayList<String> types;
		ArrayList<Icon> icons;
		
		public TypeModel(){
			update();
		}
		
		@Override
		public Object getElementAt(int index) {
			return InterfaceEnvironment.getInterfacePlotInstance(types.get(index));
		}

		@Override
		public int getSize() {
			return types.size();
		}
		
		public void update(){
			if (types == null) 
				types = new ArrayList<String>();
			else
				types.clear();
			types.addAll(InterfaceEnvironment.getInterfacePlotNames());
			Collections.sort(types);
		}
		
	}
	
	static class TypeRenderer extends DefaultListCellRenderer{
		
		@Override
		public Component getListCellRendererComponent(JList list, 
													  Object value,
													  int index, 
													  boolean isSelected, 
													  boolean cellHasFocus) {
			
			setIcon(null);
			
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			if (value instanceof IconObject){
				setIcon(((IconObject)value).getObjectIcon()); 
				}
			
			if (value instanceof InterfaceObject){
				setText(((InterfacePlot<?>)value).getPlotType());
				}
			
			return this;
			
		}
		
	}
	
}