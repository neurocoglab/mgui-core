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

package mgui.interfaces.maps;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.numbers.MguiInteger;
import mgui.resources.icons.IconObject;

import org.xml.sax.Attributes;


/*************************
 * Associates a name with an integer. Maintains two <code>HashMap</code>s for time-efficient
 * querying.
 * 
 * @author Andrew Reid
 *
 */
public class NameMap extends AbstractInterfaceObject 
					 implements IconObject,
					 			XMLObject {

	protected HashMap<String, Integer> names = new HashMap<String, Integer>();
	protected HashMap<Integer, String> indexes = new HashMap<Integer, String>();
	
	boolean strict_naming = true;
	//public String name;
	
	public NameMap(){
		int i = 0;
	}
	
	public NameMap(String name){
		setName(name);
	}
	
	/*************************
	 * Instantiate this name map.
	 * 
	 * @param name
	 * @param strict_naming If {@code true}, do not allow identical names; if this is set to {@code false}, the
	 * 						get(String) function will return {@code -Integer.MAX_VALUE}.
	 */
	public NameMap(String name, boolean strict_naming){
		setName(name);
		this.strict_naming = strict_naming;
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/name_map_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/name_map_20.png");
		return null;
	}
	
	public int getSize(){
		return names.size();
	}
	
	/**************************
	 * Returns all names in this name map. If {@code strict_naming=false}, this will be an empty set. 
	 * 
	 * @return
	 */
	public Set<String> getNames(){
		return names.keySet();
	}
	
	@Override
	public String getTreeLabel(){
		return getName();
	}
	
	/*
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	*/
	
	public ArrayList<Integer> getIndices(){
		return new ArrayList<Integer>(indexes.keySet());
	}
	
	public void clear(){
		names.clear();
		indexes.clear();
	}
	
	public boolean contains(String s){
		return names.containsKey(s);
	}
	
	public boolean contains(int i){
		return indexes.containsKey(new MguiInteger(i));
	}
	
	public void addAll(NameMap map){
		Set n_set = map.getNames();
		Iterator<String> n_itr = n_set.iterator();
		
		while (n_itr.hasNext()){
			String n = n_itr.next();
			add(map.get(n), n);
			}
	}
	
	public boolean add(int index, String name){
		
		if (!this.strict_naming){
			indexes.put(index, name);
			names.put(name, index);
			return true;
			}
		
		if (names.containsKey(index) && !names.get(index).equals(name)){
			names.remove(index);
			indexes.remove(name);
			}
		
		int i = get(name);
		if (i >= 0) return false;
		String s = get(index);
		if (s != null) return false;
		
		names.put(name, index);
		indexes.put(index, name);
		return true;
	}
	
	public boolean set(int index, String name){
		remove(name);
		remove(index);
		return add(index, name);
	}
	
	public int get(String name){
//		if (!strict_naming){
//			return -Integer.MAX_VALUE;
//			}
		Integer i = names.get(name);
		if (i == null) return -Integer.MAX_VALUE;
		return i;
	}
	
	public String get(int index){
		return indexes.get(index);
	}
	
	public boolean remove(int index){
		String s = get(index);
		if (s == null) return false;
		names.remove(s);
		indexes.remove(new Integer(index));
		return true;
	}
	
	public boolean remove(String name){
		int index = get(name);
		if (index < 0) return false;
		names.remove(name);
		indexes.remove(new Integer(index));
		return true;
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		
		treeNode.add(new InterfaceTreeNode("Values.."));
		
	}
	
	@Override
	public String toString(){
		return "NameMap: " + getName();
	}
	
	public String getDTD() {
		return "";
	}

	public String getXML() {
		return getXML(0);
	}

	public String getXML(int tab) {
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		String xml = _tab + "<NameMap name = \"" + getName() + "\" strict_naming=\"" + 
							 (this.strict_naming ? "true" : "false") + "\">\n";
		
		//Iterator<Integer> itr = indexes.keySet().iterator();
		ArrayList<Integer> indices = getIndices();
		Collections.sort(indices);
		
		for (int i = 0; i < indices.size(); i++){
			//Integer i = itr.next();
			int index = indices.get(i);
			xml = xml + _tab2 + "<Item ";
			xml = xml + "index = \"" + index + "\" ";
			xml = xml + "value = \"" + get(index) + "\" ";
			xml = xml + "/>\n";
			}
		
		xml = xml + _tab + "</NameMap>\n";
		
		return xml;
	}

	public String getShortXML(int tab){
		return XMLFunctions.getTab(tab) + "<NameMap name = \"" + getName() + "\" />\n";
	}
	
	public String getXMLSchema() {
		
		
		return null;
	}

	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		
		if (localName.equals("Item")){
			Integer index = Integer.valueOf(attributes.getValue("index"));
			String value = attributes.getValue("value");
			add(index, value);
			}
		
	}
	
	public void handleXMLElementEnd(String localName){
		
	}
	
	public void handleXMLString(String s){
		
	}
	
	public String getLocalName(){
		return "NameMap";
	}
	
	public XMLObject getXMLInstance(Attributes attributes){
		return new NameMap(attributes.getValue("name"));
	}
	
	@Override
	public void writeXML(int tab, Writer writer) throws IOException{
		writeXML(tab, writer, null);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException{
		this.writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		//XML string should be small, so get it directly
		writer.write(getXML(tab));
	}
	
}