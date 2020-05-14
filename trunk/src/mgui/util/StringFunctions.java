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

package mgui.util;

import java.awt.Color;
import java.awt.Font;

public class StringFunctions {

	public static String trim(String s){
		return s.trim();
	}
	
	/*********************************************
	 * Counts the number of times <code>occ</code> occurs in <code>s</code>.
	 * 
	 * @param s
	 * @param occ
	 * @return
	 */
	public static int countOccurrences(String s, String occ){
		if (!s.contains(occ)) return 0;
		int a = s.indexOf(occ) + occ.length() + 1;
		if (a >= s.length()) return 1;
		return 1 + countOccurrences(s.substring(a), occ);
	}
	
	/*********************************
	 * Resizes {@code string} to length {@code length}, by truncating or padding with spaces.
	 * 
	 * @param string
	 * @param length
	 * @return
	 */
	public static String resizeString(String string, int length){
		return resizeString(string, length, ' ');
	}
	
	/*********************************
	 * Resizes {@code string} to length {@code length}, by truncating or padding.
	 * 
	 * @param string
	 * @param length
	 * @param pad
	 * @return
	 */
	public static String resizeString(String string, int length, char pad){
		if (length <= 0) return "";
		if (string.length() == length) return string;
		if (string.length() > length){
			if (length > 2)
				return string.substring(0,length-2) + "..";
			return string.substring(0,length);
			}
		
		while (string.length() < length)
			string = string + pad;
		
		return string;
	}
	
	/************************************************
	 * Replaces all occurrences of {@code find} in {@code string} and replaces them with
	 * {@code replace}, without using regex.
	 * 
	 * @param string
	 * @param find
	 * @param replace
	 * @return
	 */
	public static String replaceAll(String string, String find, String replace){
		
		String s = string.replace(find, replace);
		String s2 = string;
		while (!s.equals(s2)){
			s2 = s;
			s = s2.replace(find, replace);
			}
		
		return s;
	}
	
	/************************************************
	 * Determines whether {@code s} can be converted to a number
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isNumeric(String s){
		try{
			Double.valueOf(s);
			return true;
		}catch(NumberFormatException ex){
			return false;
			}
	}
	
	/************************************************
	 * Determines whether {@code s} can be converted to an integer
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isInteger(String s){
		try{
			Integer.valueOf(s);
			return true;
		}catch(NumberFormatException ex){
			return false;
			}
	}
	
}