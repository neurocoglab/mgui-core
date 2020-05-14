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

package mgui.interfaces.plots;

import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.InterfaceWorkspace.CollectionTreeNode;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.trees.InterfaceTreePanel;
import mgui.interfaces.util.InterfaceFunctions;
import mgui.interfaces.variables.VariableInt;

/******************************************************
 * Abstract class to be extended by all dialogs which define {@link InterfacePlot}
 * objects.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class InterfacePlotDialog<T extends InterfacePlot<?>> extends InterfaceOptionsDialogBox {

	protected InterfaceTreePanel source_tree;
	protected JScrollPane scrSourceTree;
	
	public InterfacePlotDialog(){
		super();
	}
	
	public InterfacePlotDialog(JFrame frame, InterfacePlotOptions<T> options){
		super(frame, options);
	}
	
	public abstract InterfacePlot<?> showDialog(JFrame frame, InterfacePlotOptions<?> options);
	
	protected void initSourceTree(){
		//InterfaceTreeNode vNode = InterfaceSession.getWorkspace().getVariablesNode();
		InterfaceTreeNode vNode = 
			new InterfaceTreeNode(new CollectionTreeNode("/mgui/resources/icons/variable_set_20.png", "Variables"));
		ArrayList<VariableInt<?>> variables = InterfaceSession.getWorkspace().getVariables();
		
		for (int i = 0; i < variables.size(); i++)
			vNode.addChild(new InterfaceTreeNode(variables.get(i)));
		
		//TODO: add other data sources
		
		InterfaceTreeNode root = new InterfaceTreeNode("Plot Data");
		root.addChild(vNode);
		source_tree = new InterfaceTreePanel();
		source_tree.setRootNode(root);
		scrSourceTree = new JScrollPane(source_tree);
		
	}
	
	class DummyObject implements InterfaceObject{

		@Override
		public void destroy() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void clean(){
		}

		@Override
		public boolean isDestroyed() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setName(String name) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getTreeLabel() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public InterfaceTreeNode issueTreeNode() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setTreeNode(InterfaceTreeNode node) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}