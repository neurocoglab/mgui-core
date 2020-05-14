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

import java.awt.Font;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class QueryResultPanel extends JScrollPane {

	JTextArea text_area;
	public int max_property_chars = 20;
	
	public QueryResultPanel(){
		super();
		init();
	}
	
	public void init(){
		initPanel();
	}
	
	public void setQuery(InterfaceQuery query){
		if (text_area == null) initPanel();
		
		text_area.append("Property");
		for (int i = 0; i < max_property_chars - ("Property").length() + 2; i++) text_area.append(" ");
		text_area.append("Value\n");
		for (int i = 0; i < ("Property").length(); i++) text_area.append("-");
		for (int i = 0; i < max_property_chars - ("Property").length() + 2; i++) text_area.append(" ");
		for (int i = 0; i < ("Value").length(); i++) text_area.append("-");
		
	}
	
	public void clear(){
		text_area.setText(null);
	}
	
	public void append(String s){
		text_area.append(s);
	}
	
	protected void initPanel(){
		text_area = new JTextArea();
		text_area.setFont(new Font("Courier New", Font.PLAIN, 12));
		this.setViewportView(text_area);
	}
	
}