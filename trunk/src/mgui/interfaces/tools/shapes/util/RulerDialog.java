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

package mgui.interfaces.tools.shapes.util;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.tools.Tool;
import mgui.interfaces.tools.shapes.ToolRuler2D;
import mgui.resources.icons.IconObject;

/*********************************************************************
 * Dialog to be displayed when executing a Ruler tool ({@linkplain ToolRuler2D} or {@linkplain ToolRuler3D}).
 * Displays a table which contins the distance of each segment of the path, along with
 * the cumulative distance at each vertex.  
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class RulerDialog extends InterfaceDialogBox implements WindowListener,
															   IconObject{


	Icon icon = null;

	RulerTableModel table_model;
	JTable table;
	JScrollPane scrDistances;
	ArrayList<Tool> registered_tools = new ArrayList<Tool>();
	Tool current_tool;
	
	public RulerDialog(JFrame frame){
		super(frame);
		_init();
		
	}
	
	@Override
	public Icon getObjectIcon(){
		if (icon == null) setIcon();
		return icon;
	}
	
	//override to set a specific icon
	protected void setIcon(){
		java.net.URL imgURL = InterfaceShape.class.getResource("/mgui/resources/icons/tools/ruler_2d_30.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/tools/ruler_2d_30.png");
	}
	
	void _init(){
		this.setButtonType(BT_BLANK);
		super.init();
		
		this.setModal(false);
		this.setAlwaysOnTop(true);
		//this.setIconImage(this.getImage());
		
		table_model = new RulerTableModel();
		table = new JTable(table_model);
		table.getColumnModel().getColumn(0).setMinWidth(30);
		table.getColumnModel().getColumn(0).setMaxWidth(30);
		table.setCellSelectionEnabled(true);
		table.setAutoCreateColumnsFromModel( false );
		scrDistances = new JScrollPane(table);
		
		setTitle("2D Ruler Tool");
		setDialogSize(250, 300);
	
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(scrDistances, BorderLayout.CENTER);
		
		addWindowListener(this);
		
//		ImageIcon iicon = (ImageIcon)getObjectIcon();
//		this.setIconImage(iicon.getImage());
	}
	
	/****************************************
	 * Registers that {@code tool} is currently using this dialog. No other tools should therefore use it.
	 * 
	 * @param tool
	 * @return
	 */
	public boolean setCurrentTool(Tool tool){
		if (current_tool != null && current_tool != tool) return false;
		current_tool = tool;
		return true;
	}
	
	/****************************************
	 * Returns the tool that is currently using this dialog
	 * 
	 * @return
	 */
	public Tool getCurrentTool(){
		return current_tool;
	}
	
	/****************************************
	 * Clears the current tool, allowing other tools to use it
	 * 
	 */
	public void clearCurrentTool(){
		current_tool = null;
	}
	
	/****************************************
	 * Registers a ruler tool with this dialog, which registers that the tool requires the use of this dialog. 
	 * If it is not already visible, it will set itself visible.
	 * 
	 * @param tool
	 */
	public void registerTool(Tool tool){
		registered_tools.add(tool);
		if (!this.isVisible()){
			this.setVisible(true);
			}
	}
	
	/****************************************
	 * Deregisters a ruler tool with this dialog. If no other tools are registered, the dialog will
	 * set itself invisible.
	 * 
	 * @param tool
	 */
	public void deregisterTool(Tool tool){
		registered_tools.remove(tool);
		if (tool == current_tool) current_tool = null;
		if (registered_tools.size() == 0 && this.isVisible()){
			this.setVisible(false);
			}
	}
	
	public void addSegment(double dist){
		table_model.addSegment(dist);
	}
	
	public void reset(){
		table_model.reset();
	}
	
	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// The window has been closed so deactivate all registered tools
		for (int i = 0; i < registered_tools.size(); i++)
			registered_tools.get(i).deactivate();
		
		registered_tools.clear();
	}

	@Override
	public void windowClosed(WindowEvent e) {
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		
	}
	
	class RulerTableModel extends AbstractTableModel{

		@Override
		public String getColumnName(int column) {
			switch (column){
				case 0: 
					return "";
				case 1:
					return "d(seg)";
				default:
					return "d(cum)";
			}
			
		}

		public ArrayList<Double> d_segments = new ArrayList<Double>();
		public ArrayList<Double> d_cumulative = new ArrayList<Double>();
		
		public RulerTableModel(){
			
		}
		
		/**************************
		 * Add a segment distance (row) to this model
		 * 
		 * @param dist
		 */
		public void addSegment(double dist){
			double cum = dist;
			for (int i = 0; i < d_segments.size(); i++)
				cum += d_segments.get(i);
			addRow(dist, cum);
		}
		
		public void addRow(double d_seg, double d_cum){
			d_segments.add(d_seg);
			d_cumulative.add(d_cum);
			this.fireTableStructureChanged();
		}
		
		public void reset(){
			d_segments.clear();
			d_cumulative.clear();
			this.fireTableStructureChanged();
		}
		
		@Override
		public int getRowCount() {
			return d_segments.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public Object getValueAt(int row, int col) {
			switch (col){
				case 0:
					return row+1;
				case 1:
					return String.valueOf(d_segments.get(row));
				case 2:
					return String.valueOf(d_cumulative.get(row));
				}
			return null;
		}
		
		
		
	}
	
	
	
}