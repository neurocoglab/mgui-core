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

package mgui.io.domestic.views;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.graphics.View3D;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;

/********************************************
 * Dialog box to specify the output for a View3D object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class View3DOutputDialogBox extends InterfaceIODialogBox {

	JLabel lblSelect = new JLabel("Select views to write:");
	JTable view_list;
	ViewListModel view_list_model;
	JScrollPane scrViews;
	
	public View3DOutputDialogBox(){
		
	}
	
	public View3DOutputDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		super (frame, panel, options);
		_init();
	}
	
	protected void _init(){
		init();
		
		setDialogSize(400, 450);
		setTitle("Output View3D Options");
		LineLayout lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		
		initList();
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.9, 1);
		mainPanel.add(lblSelect, c);
		c = new LineLayoutConstraints(2, 7, 0.05, 0.9, 1);
		mainPanel.add(scrViews, c);
		
	}
	
	private void initList(){
		
		ArrayList<View3D> views = InterfaceSession.getWorkspace().getViews3D();
		view_list_model = new ViewListModel(views);
		
		View3DOutputOptions _options = (View3DOutputOptions)options;
		for (int i = 0; i < _options.views.size(); i++)
			view_list_model.select(views.get(i));
		
		view_list = new JTable(view_list_model);
		scrViews = new JScrollPane(view_list);
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			
			View3DOutputOptions _options = (View3DOutputOptions)options;
			_options.views = view_list_model.getSelectedViews();
			
			this.setVisible(false);
			return;
			}
		
		super.actionPerformed(e);
		
	}
	
	class ViewListModel extends DefaultTableModel{

		ArrayList<Boolean> selected;
		ArrayList<View3D> views;
		
		public ViewListModel(ArrayList<View3D> views){
			this.views = views;
			selected = new ArrayList<Boolean>(views.size());
			for (int i = 0; i < views.size(); i++)
				selected.add(false);
		}
		
		public ArrayList<View3D> getSelectedViews(){
			ArrayList<View3D> list = new ArrayList<View3D>();
			for (int i = 0; i < views.size(); i++)
				if (selected.get(i))
					list.add(views.get(i));
			return list;
		}
		
		public void select(View3D view){
			for (int i = 0; i < views.size(); i++)
				if (views.get(i).equals(view))
					selected.set(i, true);
		}
		
		public void deselect(View3D view){
			for (int i = 0; i < views.size(); i++)
				if (views.get(i).equals(view))
					selected.set(i, false);
		}
		
		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0) return "Write";
			return "View3D";
		}

		@Override
		public int getRowCount() {
			if (views == null) return 0;
			return views.size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (column == 0) return selected.get(row);
			return views.get(row);
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			if (column == 0) return true;
			return false;
		}

		@Override
		public void setValueAt(Object aValue, int row, int column) {
			if (column == 0){
				selected.set(row, (Boolean)aValue);
				return;
				}
		}

		@Override
		public Class<?> getColumnClass(int column) {
			if (column == 0) return Boolean.class;
			return View3D.class;
		}
		
		
	}
	
}