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

package mgui.interfaces.tools.graphs;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import mgui.interfaces.InterfacePanel;
import mgui.interfaces.graphs.AbstractGraphEdge;
import mgui.interfaces.graphs.AbstractGraphNode;
import mgui.interfaces.graphs.InterfaceGraphDisplay;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.tools.ToolInputEvent;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;

/********************************************
 * Acts as a bridge between the mgui Tool interface and Jung's view control mouse plugins.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ToolGraphTransform extends ToolGraph {

	protected DefaultModalGraphMouse<AbstractGraphNode, AbstractGraphEdge> graph_mouse;
	
	public ToolGraphTransform(){
		init();
	}
	
	private void init(){
		graph_mouse = new DefaultModalGraphMouse<AbstractGraphNode, AbstractGraphEdge>();
		name = "Graph Mouse";
	}
	
	public void handleToolEvent(ToolInputEvent e){
		int a = 0;
		
	}
	
	@Override
	public void setTargetPanel(InterfacePanel panel){
		if (panel == null){ 
			return;
		}
		
		if (panel == target_panel){
			//
		}else if (target_panel != null && target_panel.getViewer() != null){
			target_panel.getViewer().setGraphMouse(null);
			}
		this.target_panel = (InterfaceGraphDisplay)panel;
		if (target_panel.getViewer() == null){
			return;
		}
		
		VisualizationViewer<AbstractGraphNode, AbstractGraphEdge> viewer = target_panel.getViewer();
		viewer.setGraphMouse(graph_mouse);
		
	}
	
	
	
	@Override
	public Object clone() {
		ToolGraphTransform tool = new ToolGraphTransform();
		//tool.setTargetPanel(target_panel);
		return tool;
	}

	@Override
	public void activate() {

	}

	@Override
	public void deactivate() {

	}

	@Override
	public boolean isExclusive() {
		return false;
	}

	

	@Override
	public InterfacePopupMenu getPopupMenu() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handlePopupEvent(ActionEvent e) {
		String command = e.getActionCommand();
		
		if (command.endsWith("Pan/zoom")){
			this.graph_mouse.setMode(Mode.TRANSFORMING);
			}
		
		if (command.endsWith("Picking")){
			this.graph_mouse.setMode(Mode.PICKING);
			}

		if (command.endsWith("Annotating")){
			this.graph_mouse.setMode(Mode.ANNOTATING);
			}
		
	}

	@Override
	public void showPopupMenu(MouseEvent e) {
		// TODO Auto-generated method stub

	}

}