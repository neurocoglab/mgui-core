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

package mgui.interfaces.queries.tables;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.queries.QueryResult;
import mgui.interfaces.trees.InterfaceTreeCellRenderer;

/************************************************************
 * Renders a query as a table; all other objects as labels
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class QueryResultTableTreeRenderer extends InterfaceTreeCellRenderer implements CellEditorListener {

	TableColumnModel column_model;
	
	public QueryResultTableTreeRenderer(){
		super();
	}
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, 
												  Object value,
												  boolean sel,
												  boolean expanded,
												  boolean leaf, int row,
												  boolean hasFocus) {
		
		if (!(value instanceof DefaultMutableTreeNode))
			return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
		value = node.getUserObject();
		
		if (!(value instanceof QueryResult)) 
			return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
		QueryResult result = (QueryResult)value;
		QueryResultTable table = new QueryResultTable(result);
		if (column_model != null) table.table.setColumnModel(column_model);
		
//		table.setMinimumSize(new Dimension(200,(table.table.getRowCount()+1)*InterfaceEnvironment.getLineHeight()));
//		table.setPreferredSize(new Dimension(200,(table.table.getRowCount()+1)*InterfaceEnvironment.getLineHeight()));
		return table;

	}
	
	@Override
	public void editingCanceled(ChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void editingStopped(ChangeEvent e) {
		
		QueryResultTableTreeEditor editor = (QueryResultTableTreeEditor)e.getSource();
		
		Component c = (Component)editor.getCellEditorValue();
		if (!(c instanceof QueryResultTable)) return;
		
		QueryResultTable panel = (QueryResultTable)c;
		column_model = panel.table.getColumnModel();
		
	}
	
	
}