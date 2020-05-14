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

package mgui.numbers;

import mgui.interfaces.xml.XMLObject;

/********************************************************
 * Interface for numbers used in ModelGUI. In contrast to native Java number wrapper classes, 
 * instances of {@code MguiNumber} are modifiable; thus must be used with caution due to 
 * synchronicity issues, etc. This interface also specifies methods for basic arithmetic and comparisons.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public interface MguiNumber extends Cloneable, 
								  	Comparable<MguiNumber>,
								  	XMLObject {

	/*******************************
	 * Returns the value of this number as a {@code String}, formatted with {@code pattern}.
	 * 
	 * @param pattern
	 * @return
	 */
	public String toString(String pattern);
	public Object clone();
	public boolean setValue(String val);
	public void setValue(MguiNumber val);
	public void setValue(double val);
	public int compareTo(double d);
	public int compareTo(double d, int precision);
	public int getByteSize();
	
	/***************************
	 * Returns the value of this number
	 * 
	 * @return
	 */
	public double getValue();
	
	/***************************************************
	 * Update the value of this number by adding {@code n}.
	 * 
	 * @param n
	 * @return this number
	 */
	public MguiNumber add(MguiNumber n);
	
	/***************************************************
	 * Update the value of this number by subtracting {@code n}.
	 * 
	 * @param n
	 * @return this number
	 */
	public MguiNumber subtract(MguiNumber n);
	
	/***************************************************
	 * Update the value of this number by multiplying by {@code n}.
	 * 
	 * @param n
	 * @return this number
	 */
	public MguiNumber multiply(MguiNumber n);
	
	/***************************************************
	 * Update the value of this number by dividing by {@code n}.
	 * 
	 * @param n
	 * @return this number
	 */
	public MguiNumber divide(MguiNumber n);
	
	/***************************************************
	 * Update the value of this number by adding {@code n}.
	 * 
	 * @param n
	 * @return this number
	 */
	public MguiNumber add(double n);
	
	/***************************************************
	 * Update the value of this number by subtracting {@code n}.
	 * 
	 * @param n
	 * @return this number
	 */
	public MguiNumber subtract(double n);
	
	/***************************************************
	 * Update the value of this number by multiplying by {@code n}.
	 * 
	 * @param n
	 * @return this number
	 */
	public MguiNumber multiply(double n);
	
	/***************************************************
	 * Update the value of this number by dividing by {@code n}.
	 * 
	 * @param n
	 * @return this number
	 */
	public MguiNumber divide(double n);
	
	
	
}