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

package mgui.interfaces.variables;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiNumber;

/***********************************************
 * Represents an array of numeric values.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class VectorInt<T extends MguiNumber> extends VariableInt<T> {

	protected ArrayList<T> elements;
	
	public VectorInt(String name, int n){
		elements = new ArrayList<T>(n);
		init();
		setName(name);
	}
	
	public VectorInt(List<T> values){
		this("No-name", values);
	}
	
	public VectorInt(String name, List<T> values){
		setValues(values);
		init();
		setName(name);
	}
	
	/********************************************
	 * Sets the values for this vector.
	 * 
	 * @param values
	 */
	public void setValues(List<T> values){
		elements = new ArrayList<T>(values);
		updated = true;
		if (!batch)
			fireListeners();
	}
	
	@Override
	public ArrayList<T> getAsList(){
		return new ArrayList<T>(elements);
	}
	
	@Override
	public int getSize(){
		return getN();
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
	public VectorInt<T> getPart(int start, int end){
		return getPart("No-name", start, end);
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
	public VectorInt<T> getPart(String name, int start, int end){
		VectorInt<T> new_vector = new VectorInt<T>(elements.subList(start, end));
		new_vector.setName(name);
		return new_vector;
	}
	
	@Override
	public VectorInt<T> getPart(String name, String part){
		if (!part.contains(":")) return null;
		Pattern p = Pattern.compile(":");
		String[] parts = p.split(part);
		
		if (parts[1].equals("*"))
			parts[1] = "" + getN();
		
		return getPart(name, Integer.valueOf(parts[0]), Integer.valueOf(parts[1]));
	}
	
	@Override
	public boolean setValue(List<Integer> indices, T value) throws VariableException{
		if (!isEditable()) return false;
		
		if (indices.size() < 1 || indices.get(0) < 0 || indices.get(0) >= getN())
			throw new VariableException("VectorInt: index is out of bounds.");
		
		getValue(indices).setValue(value);
		
		updated = true;
		if (!batch)
			fireListeners();
		
		return true;
	}
	
	@Override
	public boolean setStringValue(List<Integer> indices, String value) throws VariableException{
		if (!isEditable()) return false;
		
		if (indices.size() < 1 || indices.get(0) < 0 || indices.get(0) >= getN())
			throw new VariableException("VectorInt: index is out of bounds.");
		
		getValue(indices).setValue(value);
		
		updated = true;
		if (!batch)
			fireListeners();
		
		return true;
	}
	
	@Override
	public ArrayList<Integer> getDimensions(){
		ArrayList<Integer> dims = new ArrayList<Integer>(1);
		dims.add(getN());
		return dims;
	}
	
	@Override
	protected T getValue(List<Integer> indices) throws VariableException{
		return getValue(indices.get(0));
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/vector_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/vector_20.png");
		return null;
	}
	
	public T getValue(int i) throws VariableException{
		try{
			return elements.get(i);
		}catch (Exception e){
			throw new VariableException("Vector element out of bounds: " + i);
			}
	}
	
	public int getN(){
		return elements.size();
	}
	
	@Override
	public String toString(){
		return super.toString() + " [" + getN() + "]";
	}
	
}