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

package mgui.interfaces.trees;

/*************************************************
 * Interface for all objects which issue {@link InterfaceTreeNode}s. A <code>TreeObject</code> should behave as
 * a tree node server, issuing new tree nodes to requesting objects, and notifying these objects of changes which
 * may require alterations to the nodes or trees which contain them.  
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public interface TreeObject {

	/*********************************************
	 * Issues a new {@link InterfaceTreeNode} for this object. The object is responsible for maintaining,
	 * updating, and destroying the tree nodes it issues, or notifying containers -- i.e., parent trees -- of 
	 * changes which require the tree nodes to be modified or destroyed. 
	 * 
	 * @return
	 */
	public InterfaceTreeNode issueTreeNode();
	
	/*********************************************
	 * Sets the children for this node's {@link InterfaceTreeNode}.
	 * 
	 * @param node
	 */
	public void setTreeNode(InterfaceTreeNode node);
	
	/*********************************************
	 * Returns the label text to appear in a tree node.
	 * 
	 * @return
	 */
	public String getTreeLabel();
	
}