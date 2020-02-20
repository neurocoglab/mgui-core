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

package mgui.interfaces.variables;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.numbers.MguiDouble;

/*****************************************************************
 * Stores an array of {@code String} objects as a variable. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class StringVectorInt extends VariableInt<String> {

	public ArrayList<String> elements;
	
	public StringVectorInt(String name, int n){
		//this.type = DataType.String;
		elements = new ArrayList<String>(n);
		for (int i = 0; i < n; i++)
			elements.add("");
		init();
		setName(name);
	}
	
	public StringVectorInt(List<String> values){
		this("no-name", values);
	}
	
	public StringVectorInt(String name, List<String> values){
		init();
		setName(name);
		elements = new ArrayList<String>(values);
	}
	
	@Override
	public ArrayList<String> getAsList(){
		return new ArrayList<String>(elements);
	}
	
	@Override
	public int getSize(){
		return getN();
	}
	
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/stringlist_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/stringlist_20.png");
		return null;
	}
	
	public String getValue(int i) throws VariableException{
		try{
			return elements.get(i);
		}catch (Exception e){
			throw new VariableException("Vector element out of bounds: " + i);
			}
	}
	
	@Override
	public boolean setValue(List<Integer> indices, String value) throws VariableException{
		if (!isEditable()) return false;
		
		if (indices.size() < 1 || indices.get(0) < 0 || indices.get(0) >= getN())
			throw new VariableException("VectorInt: index are out of bounds.");
		
		elements.set(indices.get(0), value);
		
		updated = true;
		if (!batch)
			fireListeners();
		
		return true;
	}
	
	@Override
	public boolean setStringValue(List<Integer> indices, String value) throws VariableException{
		return setValue(indices, value);
	}
	
	@Override
	public ArrayList<Integer> getDimensions() {
		ArrayList<Integer> dims = new ArrayList<Integer>(1);
		dims.add(getN());
		return dims;
	}

	@Override
	protected String getValue(List<Integer> indices) throws VariableException {
		return getValue(indices.get(0));
	}
	
	/********************************************
	 * Returns a part of this vector, between and including indices
	 * <code>start</code> and <code>end</code>.
	 * 
	 * @param name
	 * @param start
	 * @param end
	 * @return
	 */
	public StringVectorInt getPart(String name, int start, int end){
		StringVectorInt new_vector = new StringVectorInt(elements.subList(start, end));
		new_vector.setName(name);
		return new_vector;
	}
	
	@Override
	public StringVectorInt getPart(String name, String part){
		if (!part.contains(":")) return null;
		Pattern p = Pattern.compile(":");
		String[] parts = p.split(part);
		
		if (parts[1].equals("*"))
			parts[0] = "" + getN();
		
		return getPart(name, Integer.valueOf(parts[0]), Integer.valueOf(parts[1]));
	}
	
	public int getN(){
		return elements.size();
	}
	
	@Override
	public String toString(){
		return super.toString() + " [" + getN() + "]";
	}
	
	@Override
	public boolean isNumeric(){
		return false;
	}
	
	@Override
	public Class<String> getType(){
		return String.class;
	}
	
}