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

package mgui.interfaces.queries.tables;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTree;
import javax.swing.tree.TreeCellEditor;

/************************************************
 * Editor allows tree node table appearance to be modified  
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class QueryResultTableTreeEditor extends AbstractCellEditor implements TreeCellEditor{

	Component last_component;
	QueryResultTableTreeRenderer renderer;
	
	public QueryResultTableTreeEditor(){
		super();
	}
	
	public void setRenderer(QueryResultTableTreeRenderer renderer){
		this.renderer = renderer;
	}
	
	@Override
	public Component getTreeCellEditorComponent(JTree tree, 
												Object value,
												boolean sel, 
												boolean expanded, 
												boolean leaf, 
												int row) {
		
		if (renderer == null)
			renderer = new QueryResultTableTreeRenderer();
		
		Component this_component = renderer.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, true);
		boolean is_table = (this_component instanceof QueryResultTable);
		
		if (last_component != null && !is_table)
			updateRenderer();
		
		if (is_table) 
			last_component = this_component;
		
		return this_component;
		
	}

	private void updateRenderer(){
		if (last_component == null || renderer == null) return;
		if (!(last_component instanceof QueryResultTable)) return;
		
		QueryResultTable panel = (QueryResultTable)last_component;
		renderer.column_model = panel.table.getColumnModel();
	}
	
	@Override
	public Object getCellEditorValue() {
		return last_component;
	}
	
	
}