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

package mgui.interfaces.queries;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.graphics.GraphicMouseListener;
import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.gui.InterfaceComboBoxRenderer;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.maps.Map;
import mgui.interfaces.queries.tables.QueryResultTableTreeEditor;
import mgui.interfaces.queries.tables.QueryResultTableTreeRenderer;
import mgui.interfaces.shapes.queries.InterfaceShapeQuery;
import mgui.interfaces.shapes.queries.InterfaceShapeQuery.QueryMode;
import mgui.interfaces.shapes.selection.ShapeSelectionEvent;
import mgui.interfaces.shapes.selection.ShapeSelectionListener;
import mgui.interfaces.shapes.util.ShapeListener;
import mgui.interfaces.tools.Tool;
import mgui.interfaces.tools.ToolEvent;
import mgui.interfaces.tools.ToolListener;
import mgui.interfaces.tools.graphics.Tool3D;
import mgui.interfaces.tools.shapes.ToolQueryShape2D;
import mgui.interfaces.tools.shapes.ToolQueryShape3D;

/*****************************************
 * Provides an interface for queries; selecting and launching query tools,
 * displaying query results, etc.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceQueryPanel extends InterfacePanel 
								 implements ActionListener, 
											ShapeSelectionListener, 
											GraphicMouseListener,
											ShapeListener,
											ToolListener,
											QueryListener{

	
	CategoryTitle lblQueries = new CategoryTitle("QUERIES");
	JLabel lblQueryQueries = new JLabel("Queries:");
	JList lstQueryQueries;
	JScrollPane scrQueryQueries;
	DefaultListModel queries_list_model = new DefaultListModel();
	JButton cmdQueryNew = new JButton("New");
	JButton cmdQueryRemove = new JButton("Remove");
	JButton cmdQueryStartStop = new JButton("Start");
	
	CategoryTitle lblResults = new CategoryTitle("RESULTS");
	JCheckBox chkResultsClear = new JCheckBox("Clear results");
	JLabel lblResultsWidth = new JLabel("Column width:");
	JTextField txtResultsWidth = new JTextField("20");
	JScrollPane scrResults = new JScrollPane();
	JTree treeResults;
	DefaultMutableTreeNode root_node = new DefaultMutableTreeNode("Results");
	
	InterfaceQuery current_query;
	
	Tool3D previous_tool_3d;
	ToolQueryShape3D current_query_tool_3d;
	ToolQueryShape2D current_query_tool_2d;
	
	public InterfaceQueryPanel(){
		if (InterfaceSession.isInit())
			init();
	}
	
	@Override
	protected void init() {
		
		super._init();
		setLayout(new CategoryLayout(20, 5, 200, 10));
		CategoryLayoutConstraints c;
		
		cmdQueryStartStop.setActionCommand("Query Start/Stop");
		cmdQueryStartStop.addActionListener(this);
		chkResultsClear.setSelected(true);
		//current_model = displayPanel.getCurrentModel();
		
		setQueryList();
		
		c = new CategoryLayoutConstraints();
		add(lblQueries, c);
		lblQueries.setParentObj(this);
		c = new CategoryLayoutConstraints("QUERIES", 1, 1, 0.05, 0.9, 1);
		add(lblQueryQueries, c);
		c = new CategoryLayoutConstraints("QUERIES", 2, 7, 0.05, 0.9, 1);
		add(scrQueryQueries, c);
		c = new CategoryLayoutConstraints("QUERIES", 8, 8, 0.05, 0.44, 1);
		add(cmdQueryNew, c);
		c = new CategoryLayoutConstraints("QUERIES", 8, 8, 0.51, 0.44, 1);
		add(cmdQueryRemove, c);
		c = new CategoryLayoutConstraints("QUERIES", 9, 10, 0.1, 0.8, 1);
		add(cmdQueryStartStop, c);
		
		c = new CategoryLayoutConstraints();
		add(lblResults, c);
		lblResults.setParentObj(this);
		treeResults = new JTree(root_node);
		QueryResultTableTreeRenderer renderer = new QueryResultTableTreeRenderer();
		treeResults.setCellRenderer(renderer);
		QueryResultTableTreeEditor editor = new QueryResultTableTreeEditor();
		editor.setRenderer(renderer);
		treeResults.setCellEditor(editor);
		treeResults.setEditable(true);
		treeResults.setRowHeight(0);
		
		//scrResults = new JScrollPane(tblResults);
		scrResults = new JScrollPane(treeResults);
		c = new CategoryLayoutConstraints("RESULTS", 1, 10, 0.05, 0.9, 1);
		add(scrResults, c);
		
	}
	
	@Override
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfaceQueryPanel.class.getResource("/mgui/resources/icons/query_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/query_20.png");
		return null;
	}

	void setQueryTree(){
		
		root_node.removeAllChildren();
		if (current_query == null) return;
		
		root_node.add(current_query.getResultTreeNode());
		
		DefaultTreeModel model = (DefaultTreeModel)treeResults.getModel();
		try{
			model.nodeStructureChanged(root_node);
		}catch (Exception ex){
			//catches null pointer thrown at BasicTreeUI.java:2014 (probable Swing bug)
			
			}
		
		//expand all
		for (int i = 0; i < treeResults.getRowCount(); i++) {
			treeResults.expandRow(i);
			}
		
	}
	
	void setQueryList(){
		
		ArrayList<InterfaceQuery> queries = InterfaceSession.getWorkspace().getQueries();
		lstQueryQueries = new JList(queries_list_model);
		
		lstQueryQueries.setCellRenderer(new InterfaceComboBoxRenderer());
		
		for (InterfaceQuery q : queries)
			queries_list_model.addElement(q);
		
		scrQueryQueries = new JScrollPane(lstQueryQueries);
	}
	
	
	@Override
	public void showPanel(){
	}
	
	@Override
	public void updateDisplay(){
		
		
		
		updateUI();
	}
	
	
	public void actionPerformed(ActionEvent e) {
		
		String cmd = e.getActionCommand();
		
		if (cmd.equals("Query Start/Stop")){
			
			if (cmdQueryStartStop.getText().equals("Start")){
				previous_tool_3d = InterfaceSession.getDisplayPanel().getCurrentTool3D();
				//get query instance and set query tool
				setQueryTools();
				//setQueryTool2D();
				setQueryTree();
				
				cmdQueryStartStop.setText("Stop");
				
			}else{
				InterfaceSession.getDisplayPanel().getCurrentShapeModel().clearTempShapes();
				InterfaceSession.getDisplayPanel().setCurrentTool(previous_tool_3d);
				cmdQueryStartStop.setText("Start");
				return;
				}
			
			return;
			}
		
	}
	
	
	public void toolStateChanged(Tool tool){
		
		
	}
	
	public void toolDeactivated(ToolEvent e){
		
	}
	
	/*
	protected boolean setQueryTool3D(){
		//InterfaceQuery query = getSelectedQuery();
		setCurrentQuery();
		if (current_query == null || !(current_query instanceof InterfaceShapeQuery))
			return false;
		//query.addListener(this);
		InterfaceSession.getDisplayPanel().getCurrentShapeModel().clearTempShapes();
		if (current_query_tool_3d != null)
			current_query_tool_3d.removeListener(this);
		current_query_tool_3d = new ToolQueryShape3D(getQueryMode(), (InterfaceShapeQuery)current_query);
		current_query_tool_3d.addListener(this);
		
		InterfaceSession.getDisplayPanel().setCurrentTool(current_query_tool_3d);
		return true;
	}
	*/
	
	protected boolean setQueryTools(){
		//InterfaceQuery query = getSelectedQuery();
		setCurrentQuery();
		if (current_query == null || !(current_query instanceof InterfaceShapeQuery))
			return false;
		//query.addListener(this);
		InterfaceSession.getDisplayPanel().getCurrentShapeModel().clearTempShapes();
		if (current_query_tool_3d != null)
			current_query_tool_3d.removeListener(this);
		current_query_tool_3d = new ToolQueryShape3D(getQueryMode(), (InterfaceShapeQuery)current_query);
		current_query_tool_3d.addListener(this);
		
		InterfaceSession.getDisplayPanel().setCurrentTool(current_query_tool_3d);
		
		if (current_query_tool_2d != null)
			current_query_tool_2d.removeListener(this);
		current_query_tool_2d = new ToolQueryShape2D(getQueryMode(), (InterfaceShapeQuery)current_query);
		current_query_tool_2d.addListener(this);
		
		InterfaceSession.getDisplayPanel().setCurrentTool(current_query_tool_2d);
		return true;
	}
	
	public void setCurrentQuery(){
		if (current_query != null)
			current_query.removeListener(this);
		InterfaceQuery query = (InterfaceQuery)lstQueryQueries.getSelectedValue();
		if (query == null) return;
		current_query = query.getNewInstance(query.getName());
		current_query.addListener(this);
	}
	
	@Override
	public void objectQueried(QueryEvent e){
		
		setQueryTree();
		
	}
	
	public QueryMode getQueryMode(){
		return QueryMode.SingleNode;
	}
	
	public void shapeSelectionChanged(ShapeSelectionEvent e) {
		// TODO Auto-generated method stub

	}

	public void itemStateChanged(ItemEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void setMap(Map mt) {
		// TODO Auto-generated method stub

	}

	public boolean isShape() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setParentWindow(InterfaceGraphic thisParent) {
		// TODO Auto-generated method stub

	}

	public MouseInputAdapter getMouseListener() {
		// TODO Auto-generated method stub
		return null;
	}

	public MouseWheelListener getMouseWheelListener() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void windowUpdated(InterfaceGraphic g) {
		// TODO Auto-generated method stub
		
	}

	public void toolStateChanged(ToolEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String toString(){
		return "Queries Panel";
	}
	
}