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

package mgui.interfaces.trees;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;

/****************************************************
 * Provides an implementation for a tree/table combination.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceTreeTable extends JTable {

	 protected TreeTableCellRenderer tree;

	 public InterfaceTreeTable(TreeTableModel treeTableModel) {
		super();

		// Create the tree. It will be used as a renderer and editor. 
		tree = new TreeTableCellRenderer(treeTableModel); 

		// Install a tableModel representing the visible rows in the tree. 
		super.setModel(new TreeTableModelAdapter(treeTableModel, tree));

		// Force the JTable and JTree to share their row selection models. 
		tree.setSelectionModel(new DefaultTreeSelectionModel() { 
		    // Extend the implementation of the constructor, as if: 
		 /* public this() */ {
			setSelectionModel(listSelectionModel); 
		    } 
		}); 
		// Make the tree and table row heights the same. 
		tree.setRowHeight(getRowHeight());

		// Install the tree editor renderer and editor. 
		setDefaultRenderer(TreeTableModel.class, tree); 
		setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());  

		setShowGrid(false);
		setIntercellSpacing(new Dimension(0, 0)); 	        
	    }

	    /* Workaround for BasicTableUI anomaly. Make sure the UI never tries to 
	     * paint the editor. The UI currently uses different techniques to 
	     * paint the renderers and editors and overriding setBounds() below 
	     * is not the right thing to do for an editor. Returning -1 for the 
	     * editing row in this case, ensures the editor is never painted. 
	     */
	    @Override
		public int getEditingRow() {
	        return (getColumnClass(editingColumn) == TreeTableModel.class) ? -1 : editingRow;  
	    }

	    // 
	    // The renderer used to display the tree nodes, a JTree.  
	    //

	    public class TreeTableCellRenderer extends JTree implements TableCellRenderer {

		protected int visibleRow;
	   
		public TreeTableCellRenderer(TreeModel model) { 
		    super(model); 
		}

		@Override
		public void setBounds(int x, int y, int w, int h) {
		    super.setBounds(x, 0, w, InterfaceTreeTable.this.getHeight());
		}

		@Override
		public void paint(Graphics g) {
		    g.translate(0, -visibleRow * getRowHeight());
		    super.paint(g);
		}

		public Component getTableCellRendererComponent(JTable table,
							       Object value,
							       boolean isSelected,
							       boolean hasFocus,
							       int row, int column) {
		    if(isSelected)
			setBackground(table.getSelectionBackground());
		    else
			setBackground(table.getBackground());
	       
		    visibleRow = row;
		    return this;
		}
	    }

	    // 
	    // The editor used to interact with tree nodes, a JTree.  
	    //

	    public class TreeTableCellEditor extends AbstractTreeCellEditor implements TableCellEditor {
	    	
			public Component getTableCellEditorComponent(JTable table, Object value,
								     					 boolean isSelected, int r, int c) {
			    return tree;
			}
			
	    }

	
	
	
	/*
	class TableTreeCellRender extends JTree implements TableCellRenderer{

		protected int visibleRow;
		protected JTable parent;
		
		public TableTreeCellRender(JTable parent){
			super();
			this.parent = parent;
		}
		
	    public void setBounds(int x, int y, int w, int h) { 
	    	super.setBounds(x, 0, w, parent.getHeight());          
	    }
	    
	    public void paint(Graphics g) { 
	    	g.translate(0, -visibleRow * getRowHeight());          
	    	super.paint(g); 
	    }
	    
	    public Component getTableCellRendererComponent(JTable table,          
										               Object value, 
										               boolean isSelected, 
										               boolean hasFocus, 
										               int row, int column) { 
	    	visibleRow = row; 
	    	return this; 
	    } 

		
		
		
		
	}
	*/
	
	
}