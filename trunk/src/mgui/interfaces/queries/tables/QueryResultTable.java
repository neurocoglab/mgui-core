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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import mgui.interfaces.queries.QueryResult;

public class QueryResultTable extends JPanel {

	int line_height = 20, line_width = 300;
	QueryResultTableModel model;
	JTable table = new JTable();
	boolean variable_width = true;
	
	public QueryResultTable(QueryResult result){
		this(result, 300, 20);
	}
	
	public QueryResultTable(QueryResult result, int line_width, int line_height){
		this(result, line_width, line_height, true);
	}
	
	/****************************************
	 * Constructor
	 * 
	 * @param result
	 * @param line_width
	 * @param line_height		
	 * @param variable_width	Whether to adjust width to fit elements
	 */
	public QueryResultTable(QueryResult result, int line_width, int line_height, boolean variable_width){
		super();
		this.line_width = line_width;
		this.line_height = line_height;
	
		setLayout(new BorderLayout());
		add(table.getTableHeader(), BorderLayout.PAGE_START);
		add(table, BorderLayout.CENTER);
		int header_ht = table.getTableHeader().getHeight();
		Dimension pref = new Dimension(line_width, line_height * (result.getProperties().size()) + header_ht);
		table.setPreferredSize(pref);
		this.setPreferredSize(pref);
		
		setResult(result);
	}
	
	public void setResult(QueryResult result){
		if (model == null){
			model = new QueryResultTableModel(result);
		}else{
			model.setResult(result);
			}
		table.setModel(model);
		
		if (variable_width){
			int buffer = 10;		//necessary for some reason...
			ArrayList<String> properties = result.getProperties();
			TableColumn p_column = table.getColumn("Item");
			int p_width = p_column.getWidth();
			TableColumn v_column = table.getColumn("Value");
			int v_width = v_column.getWidth();
			FontMetrics fm = table.getFontMetrics(table.getFont());
			
			for (int i = 0; i < properties.size(); i++){
				int s_width = fm.stringWidth(properties.get(i));
				if (s_width > p_width) p_width = s_width;
				Object obj = result.getValue(properties.get(i));
				if (obj == null) obj = "Null";
				s_width = fm.stringWidth(obj.toString());
				if (s_width > v_width) v_width = s_width;
				}
			
			p_width += buffer;
			v_width += buffer;
			
			p_column.setWidth(p_width);
			p_column.setMinWidth(p_width );
			
			v_column.setWidth(v_width);
			v_column.setMinWidth(v_width);
			
			Dimension size = table.getPreferredSize();
			size.width = Math.max(size.width, p_width + v_width);
			table.setPreferredSize(size);
			table.setMinimumSize(size);
			this.setPreferredSize(size);
			this.setMinimumSize(size);
			}
		
		
		
	}
	
}