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

package mgui.interfaces.layouts;

import java.awt.Color;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;

/*****************************************************************
 * Implements the top-level container for a 2D layout, containing renderable items within a fixed
 * coordinate system (document coordinates/units). 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class LayoutDocument extends AbstractInterfaceObject {

	protected AttributeList attributes;
	
	protected HashMap<Integer,ArrayList<LayoutItem>> item_map = new HashMap<Integer,ArrayList<LayoutItem>>();
	protected int page_count = 1;
	
	/******************************
	 * Constructs a new document with a default name and format.
	 * 
	 */
	public LayoutDocument(){
		this("No-name", new PageFormat());
	}
	
	/******************************
	 * Constructs a new document with the specified name and format.
	 * 
	 * @param name
	 * @param page_format
	 */
	public LayoutDocument(String name, PageFormat page_format){
		init();
		this.setName(name);
		this.setPageFormat(page_format);
		
	}
	
	/**************************
	 * Initializes this document's attributes.
	 * 
	 */
	protected void init(){
		
		attributes = new AttributeList();
		
		attributes.add(new Attribute<String>("Name","no-name"));
		attributes.add(new Attribute<Color>("Background",Color.white));
		attributes.add(new Attribute<PageFormat>("PageFormat",new PageFormat()));
		
	}
	
	@Override
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	@Override
	public void setName(String name){
		attributes.setValue("Name", name);
	}
	
	/************************************
	 * Sets this document's page format (size and orientation)
	 * 
	 * @param page_format
	 * @see PageFormat
	 */
	public void setPageFormat(PageFormat page_format){
		attributes.setValue("PageFormat", page_format);
	}
	
	/************************************
	 * Returns this document's page format (size and orientation)
	 * 
	 * @return This document's page format
	 * @see PageFormat
	 */
	public PageFormat getPageFormat(){
		return (PageFormat)attributes.getValue("PageFormat");
	}
	
	/************************************
	 * Returns the number of printable pages in this document.
	 * 
	 * @return This document's page count
	 */
	public int getPageCount(){
		return page_count;
	}
	
	/************************************
	 * Sets the number of printable pages in this document. Note that this method will remove any existing pages
	 * which are beyond this values, along with their associated items.
	 * 
	 */
	public void setPageCount(int page_count){
		
		if (page_count == this.page_count) return;
		
		if (page_count > this.page_count){
			// Remove items
			for (int i = this.page_count; i > page_count; i--){
				item_map.remove(i);
				}
		}else{
			// Add empty lists
			for (int i = this.page_count + 1; i <= page_count; i++){
				item_map.put(i, new ArrayList<LayoutItem>());
				}
			}
		
		this.page_count = page_count;
	}
	
	/************************************
	 * Appends a single page to this document.
	 * 
	 */
	public void appendPage(){
		
		page_count++;
		item_map.put(page_count, new ArrayList<LayoutItem>());
		
	}
	
	
}
