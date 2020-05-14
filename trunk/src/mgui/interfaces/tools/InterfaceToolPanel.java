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

package mgui.interfaces.tools;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.menus.PopupMenuObject;
import mgui.interfaces.tools.graphics.ToolDZoom2D;
import mgui.interfaces.tools.graphics.ToolMouseOrbit3D;
import mgui.interfaces.tools.shapes.ToolCreateMeshFromPolylines;
import mgui.interfaces.tools.shapes.ToolCreatePolygon2D;
import mgui.interfaces.tools.shapes.ToolCutMeshWithPlane;
import mgui.interfaces.tools.shapes.ToolGetConvexHull2D;
import mgui.interfaces.tools.shapes.ToolGetMeshIntersectionPolylines;
import mgui.interfaces.tools.shapes.ToolRuler2D;
import mgui.interfaces.tools.shapes.ToolSelectMeshBoundaryNodes;
import mgui.interfaces.tools.shapes.ToolSelectNodesWithPolygon;
import mgui.interfaces.tools.shapes.ToolSelectRegionBoundaryNodes;

/********************************************************************
 * Panel displaying buttons for various tools, categorized.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceToolPanel extends InterfacePanel implements ActionListener {

	CategoryTitle lblView2D = new CategoryTitle("VIEW 2D");
	ToolItem toolDynZoom2D; 
	
	CategoryTitle lblView3D = new CategoryTitle("VIEW 3D");
	ToolItem toolOrbit3D; 
	
	CategoryTitle lblQuery2D = new CategoryTitle("QUERY 2D");
	ToolItem toolRuler2D; 
	
	CategoryTitle lblQuery3D = new CategoryTitle("QUERY 3D");
	
	CategoryTitle lblDraw2D = new CategoryTitle("DRAW 2D");
	ToolItem toolClosedPolygon2D; 
	ToolItem toolOpenPolygon2D; 
	ToolItem toolGetConvexHull2D;
	
	CategoryTitle lblDraw3D = new CategoryTitle("DRAW 3D");
	ToolItem toolCreateMeshFromPolylines;
	ToolItem toolGetMeshIntersectPolylines;
	
	CategoryTitle lblEdit2D = new CategoryTitle("EDIT 2D");
	
	CategoryTitle lblEdit3D = new CategoryTitle("EDIT 3D");
	ToolItem toolCutMeshWithPlane; 
	ToolItem toolSelectNodesWithPolygon;
	ToolItem toolSelectMeshBoundaryNodes;
	ToolItem toolSelectMeshRegionBoundaryNodes;
	
	public InterfaceToolPanel(){
		if (InterfaceSession.isInit())
			init();
	}
	
	@Override
	protected void init() {
		
		_init();
		
		//instantiate tools
		toolClosedPolygon2D = new ToolItem(new ToolCreatePolygon2D(true), "Create closed polygon");
		toolOpenPolygon2D = new ToolItem(new ToolCreatePolygon2D(false), "Create open polygon");
		toolOrbit3D = new ToolItem(new ToolMouseOrbit3D(), "Dynamic zoom/pan");
		toolDynZoom2D = new ToolItem(new ToolDZoom2D(), "Dynamic zoom/pan");
		toolCreateMeshFromPolylines = new ToolItem(new ToolCreateMeshFromPolylines(), "Create mesh from polylines");
		toolCutMeshWithPlane = new ToolItem(new ToolCutMeshWithPlane(), "Cut mesh with plane");
		toolGetMeshIntersectPolylines = new ToolItem(new ToolGetMeshIntersectionPolylines(), "Get mesh intersection polylines");
		toolSelectNodesWithPolygon = new ToolItem(new ToolSelectNodesWithPolygon(), "Select nodes with polygon");
		toolGetConvexHull2D = new ToolItem(new ToolGetConvexHull2D(), "Get convex hull of pointset");
		toolSelectMeshBoundaryNodes = new ToolItem(new ToolSelectMeshBoundaryNodes(), "Select mesh boundary nodes");
		toolSelectMeshRegionBoundaryNodes = new ToolItem(new ToolSelectRegionBoundaryNodes(), "Select mesh region boundary nodes");
		toolRuler2D = new ToolItem(new ToolRuler2D(), "Polyline ruler");
		
		//layout setup
		setLayout(new CategoryLayout(35, 5, 200, 10, 20));
		
		//Window Category
		CategoryLayoutConstraints c = new CategoryLayoutConstraints();
		add(lblView2D, c);
		lblView2D.setParentObj(this);
		c = new CategoryLayoutConstraints("VIEW 2D", 1, 1, 0.05, 0.9, 1);
		add(toolDynZoom2D, c);
		
		c = new CategoryLayoutConstraints();
		add(lblView3D, c);
		lblView3D.setParentObj(this);
		c = new CategoryLayoutConstraints("VIEW 3D", 1, 1, 0.05, 0.9, 1);
		add(toolOrbit3D, c);
		
		c = new CategoryLayoutConstraints();
		add(lblQuery2D, c);
		lblQuery2D.setParentObj(this);
		c = new CategoryLayoutConstraints("QUERY 2D", 1, 1, 0.05, 0.9, 1);
		add(toolRuler2D, c);
		
		c = new CategoryLayoutConstraints();
		add(lblDraw2D, c);
		lblDraw2D.setParentObj(this);
		c = new CategoryLayoutConstraints("DRAW 2D", 1, 1, 0.05, 0.9, 1);
		add(toolClosedPolygon2D, c);
		c = new CategoryLayoutConstraints("DRAW 2D", 2, 2, 0.05, 0.9, 1);
		add(toolOpenPolygon2D, c);
		c = new CategoryLayoutConstraints("DRAW 2D", 3, 3, 0.05, 0.9, 1);
		add(toolGetConvexHull2D, c);
		
		c = new CategoryLayoutConstraints();
		add(lblDraw3D, c);
		lblDraw3D.setParentObj(this);
		c = new CategoryLayoutConstraints("DRAW 3D", 1, 1, 0.05, 0.9, 1);
		add(toolCreateMeshFromPolylines, c);
		c = new CategoryLayoutConstraints("DRAW 3D", 2, 2, 0.05, 0.9, 1);
		add(toolGetMeshIntersectPolylines, c);
		
		c = new CategoryLayoutConstraints();
		add(lblEdit2D, c);
		lblEdit2D.setParentObj(this);
		
		
		c = new CategoryLayoutConstraints();
		add(lblEdit3D, c);
		lblEdit3D.setParentObj(this);
		c = new CategoryLayoutConstraints("EDIT 3D", 1, 1, 0.05, 0.9, 1);
		add(toolCutMeshWithPlane, c);
		c = new CategoryLayoutConstraints("EDIT 3D", 2, 2, 0.05, 0.9, 1);
		add(toolSelectNodesWithPolygon, c);
		c = new CategoryLayoutConstraints("EDIT 3D", 3, 3, 0.05, 0.9, 1);
		add(toolSelectMeshBoundaryNodes, c);
		c = new CategoryLayoutConstraints("EDIT 3D", 4, 4, 0.05, 0.9, 1);
		add(toolSelectMeshRegionBoundaryNodes, c);
		
	}

	@Override
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfaceToolPanel.class.getResource("/mgui/resources/icons/tools/tool_3d_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/tools/tool_3d_20.png");
		return null;
	}
	
	
	public void actionPerformed(ActionEvent e) {
		
		
		
	}
	
	@Override
	public String toString(){
		return "Tools Panel";
	}

	
	static class ToolItem extends JPanel implements PopupMenuObject,
													ActionListener{
		
		Tool tool;
		JLabel label = new JLabel();
		JButton tool_button = new JButton();
		//InterfaceDisplayPanel display_panel;
		
		public ToolItem(Tool tool, String label_str){
			//display_panel = panel;
			this.tool = tool;
			label.setText(label_str);
			tool_button.setIcon(tool.getObjectIcon());
			
			SpringLayout layout = new SpringLayout();
			this.setLayout(layout);
			
			tool_button.setPreferredSize(new Dimension(35,35));
			add(tool_button);
			add(this.label);
			
			layout.putConstraint(SpringLayout.WEST, label,
				                 10,
				                 SpringLayout.EAST, tool_button);
			layout.putConstraint(SpringLayout.VERTICAL_CENTER, label,
			                     0,
			                     SpringLayout.VERTICAL_CENTER, tool_button);

			tool_button.setActionCommand("Activate Tool");
			tool_button.addActionListener(this);
		}
		
		@Override
		public InterfacePopupMenu getPopupMenu() {
			return getPopupMenu(null);
		}
		
		@Override
		public InterfacePopupMenu getPopupMenu(List<Object> selected) {
			return tool.getPopupMenu();
		}

		public void handlePopupEvent(ActionEvent e) {
			tool.handlePopupEvent(e);
		}

		public void showPopupMenu(MouseEvent e) {
			tool.showPopupMenu(e);
		}
		
		public void actionPerformed(ActionEvent e) {
			
			if (e.getActionCommand().equals("Activate Tool")){
				InterfaceSession.getDisplayPanel().setCurrentTool(tool);
				//tool.activate();
				return;
				}
			
			
		}
		
	}
	
}