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

package mgui.numbers;

import java.awt.image.DataBuffer;
import java.text.DecimalFormat;

import org.jogamp.vecmath.Point2f;
import org.jogamp.vecmath.Point3d;

import mgui.datasources.DataType;
import mgui.datasources.DataTypes;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;

public class NumberFunctions {

	/******************************
	 * Compares two numbers within a given precision (number of zeros). Returns -1 if d1 < d2,
	 * +1 if d1 > d2, zero otherwise.
	 * @param d1
	 * @param d2
	 * @param precision
	 * @return
	 */
	public static int compare(double d1, double d2, double precision){
		double delta = Math.pow(10, -precision);
		if (d1 < d2 - delta) return -1;
		if (d1 > d2 + delta) return 1;
		return 0;
	}
	
	//returns the quotient obtained by d2 / d1, rounded to nearest integer
	public static int getMultiple(double d1, double d2){
		return (int)(d2 / d1);
	}
	
	public static int ceilToOdd(double d){
		
		int i = (int)Math.ceil(d);
		if (isEven(i)) i++;
		
		return i;
	}
	
	/****************************************************
	 * Returns an instance of an {@linkplain MguiNumber} of the specified type and value
	 * 
	 * @param type
	 * @param value
	 * @return
	 */
	public static MguiNumber getInstance(DataType type, double value){
		
		switch (type.val){
			case DataTypes.SHORT:
			case DataTypes.USHORT:
				return new MguiShort(value);
			case DataTypes.FLOAT:
				return new MguiFloat(value);
			case DataTypes.INTEGER:
				return new MguiInteger(value);
			case DataTypes.LONG:
				return new MguiLong(value);
			case DataTypes.DOUBLE:
			default:
				return new MguiDouble(value);
			}
		
	}
	
	/****************************************************
	 * Returns an instance of an {@linkplain MguiNumber} of the specified class and value. Returns {@code null} if instantiation
	 * failed.
	 * 
	 * @param type
	 * @param value
	 * @return
	 */
	public static MguiNumber getInstance(Class<?> clazz, double value){
		
		if (!(MguiNumber.class.isAssignableFrom(clazz))){
			InterfaceSession.log("NumberFunctions.getInstance: class must be an instance of MguiNumber.", LoggingType.Errors);
			return null;
			}
		
		try{
			MguiNumber number = (MguiNumber)clazz.newInstance();
			number.setValue(value);
			return number;
		}catch (Exception ex){
			InterfaceSession.handleException(ex, LoggingType.Errors);
			}
		
		return null;
	}
	
	/****************************************************
	 * Returns an instance of an {@linkplain MguiNumber} of the specified type and value
	 * 
	 * @param type: Name of MguiNumber subclass
	 * @param value
	 * @return
	 */
	public static MguiNumber getInstance(String type, double value){
		
		if (type.equals("MguiInteger"))
			return new MguiInteger(value);
		if (type.equals("MguiLong"))
			return new MguiLong(value);
		if (type.equals("MguiFloat"))
			return new MguiFloat(value);
		return new MguiDouble(value);
			
	}
	
	public static DecimalFormat getDecimalFormat(int decimal_places){
		return getDecimalFormat(decimal_places, false);
	}
	
	public static DecimalFormat getDecimalFormat(int decimal_places, boolean dividers){
		
		if (decimal_places <= 0) return new DecimalFormat("0"); 
		String s = "0.0";
		for (int i = 0; i < decimal_places - 1; i++)
			s = s + "0";
		return new DecimalFormat(s);
	}
	
	public static String getPoint2fStr(Point2f p, String pattern){
		return MguiFloat.getString(p.x, pattern) + ", " + MguiFloat.getString(p.y, pattern);
	}
	
	public static boolean isEven(double value){
		if (value%2 == 0)
			return true;
		else
			return false;
	}

	
	public static String getPoint3dStr(Point3d p, String pattern){
		return MguiDouble.getString(p.x, pattern) + ", " +
			   MguiDouble.getString(p.y, pattern) + ", " +
			   MguiDouble.getString(p.z, pattern);
	}
	
	public static MguiDouble getDifference(MguiNumber n1, MguiNumber n2) {
		MguiDouble d1 = new MguiDouble(n1);
		MguiDouble d2 = new MguiDouble(n2);
		d1.value -= d2.value;
		
		return d1;
	}

	public static MguiDouble getProduct(MguiNumber n1, MguiNumber n2) {
		MguiDouble d1 = new MguiDouble(n1);
		MguiDouble d2 = new MguiDouble(n2);
		d1.value *= d2.value;
		
		return d1;
	}

	public static MguiDouble getQuotient(MguiNumber n1, MguiNumber n2) {
		MguiDouble d1 = new MguiDouble(n1);
		MguiDouble d2 = new MguiDouble(n2);
		d1.value /= d2.value;
		
		return d1;
	}

	public static MguiDouble getSum(MguiNumber n1, MguiNumber n2) {
		MguiDouble d1 = new MguiDouble(n1);
		MguiDouble d2 = new MguiDouble(n2);
		d1.value += d2.value;
		
		return d1;
	}
	
	//SOME MATH STUFF
	
	/**********************
	 * Returns the next highest square number
	 */
	public static double getNextSquare(double n){
		double x = 1, y = 1;
		while (y < n)
			y = x * x++;
		return y;
	}
	
	public static double getSum(double[] n){
		double sum = 0;
		for (int i = 0; i < n.length; i++)
			sum += n[i];
		return sum;
	}
	
	public static String getReasonableString(double d){
		return MguiDouble.getString(d, getReasonableFormat(d));
	}
	
	public static String getReasonableFormat(double d){
	
		String formatStr = "";
		d = Math.abs(d);
		if (d <= 0.001)
			formatStr = "0.000000";
		else if (d <= 0.01)
			formatStr = "0.00000";
		else if (d <= 0.1)
			formatStr = "0.0000";
		else if (d < 1)
			formatStr = "0.000";
		else if (d < 100)
			formatStr = "0.0";
		else
			formatStr = "0";
		return formatStr;
	}
	
//	/******************************************
//	 * Performs a simple 
//	 * 
//	 * @param s
//	 * @return
//	 */
//	public static double getValueForString(String s){
//		if (s.toLowerCase().contains("pi")){
//			int i = s.indexOf("pi");
//			if (i == 0)
//				task.x_angle = Math.PI;
//			else
//				task.x_angle = Double.valueOf(s.substring(0, i)) * Math.PI;
//		}else{
//			task.x_angle = Double.valueOf(s);
//			}
//		
//	}
	
	/*************************************
	 * Determines the numeric value of <code>obj</code> and returns it as a <code>double</code>.
	 * <code>obj</code> must be an instance of <code>arNumber</code>, or a Java primitive
	 * wrapper (one of <code>Integer</code>, <code>Short</code>, <code>Float</code>, or 
	 * <code>Double</code>).
	 * 
	 * <p>If <code>obj</code> does not satisfy these criteria, <code>Double.NaN</code> is returned.
	 * 
	 * @param obj
	 * @return
	 */
	public static double getValueForObject(Object obj){
		if (obj instanceof MguiNumber)
			return ((MguiNumber)obj).getValue();
		
		if (obj instanceof Integer)
			return (Integer)obj;
		if (obj instanceof Float)
			return (Float)obj;
		if (obj instanceof Short)
			return (Short)obj;
		if (obj instanceof Double)
			return (Double)obj;
		
		return Double.NaN;
	}
	
	/**************************************************
	 * Determines whether <code>obj</code> is numeric; i.e., an instance of <code>MguiNumber</code> (except arBoolean), or a Java primitive
	 * wrapper (one of <code>Integer</code>, <code>Short</code>, <code>Float</code>, or <code>Double</code>).
	 * 
	 * @param obj
	 * @return <code>true</code> if numeric, <code>false</code> otherwise.
	 */
	public static boolean isNumeric(Object obj){
		if (obj instanceof MguiBoolean) return false;
		return (obj instanceof MguiNumber ||
				obj instanceof Integer ||
				obj instanceof Float ||
				obj instanceof Short ||
				obj instanceof Double);
	}
	
}