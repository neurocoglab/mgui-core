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

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;

import org.xml.sax.Attributes;

/*********************************************************
 * Implementation of {@link MguiNumber} for values of type <code>double</code>.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class MguiDouble implements MguiNumber {

	protected double value;
	
	public MguiDouble(){}
	
	public MguiDouble(double thisValue){
		value = thisValue;
	}
	
	public MguiDouble(String val){
		setValue(val);
	}
	
	public MguiDouble(MguiNumber thisValue){
		setValue(thisValue.getValue());
	}
	
	public boolean equals(Object obj){
		if (obj instanceof Integer)
			return value == (Integer)obj;
		if (obj instanceof Double)
			return value == (Double)obj;
		if (obj instanceof Float)
			return value == (Float)obj;
		if (obj instanceof Long)
			return value == (Long)obj;
		if (obj instanceof MguiNumber)
			return compareTo((MguiNumber)obj) == 0;
		return obj == (Double)value;
	}
	
	public MguiNumber add(MguiNumber number){
		value += number.getValue();
		return this;
	}
	
	public MguiNumber divide(MguiNumber number){
		value /= number.getValue();
		return this;
	}
	
	public MguiNumber subtract(MguiNumber number){
		value -= number.getValue();
		return this;
	}
	
	public MguiNumber multiply(MguiNumber number){
		value *= number.getValue();
		return this;
	}
	
	public void setValue(MguiNumber val){
		setValue(val.getValue());
	}
	
	public void setValue(double thisValue){
		value = thisValue;
	}
	
	public double getValue(){
		return value;
	}
	
	public static double getValue(String s){
		MguiDouble f = new MguiDouble(s);
		return f.getValue();
	}
	
//	@Override
//	public boolean equals(Object o){
//		if (!(o instanceof MguiNumber)) return false;
//		return compareTo((MguiNumber)o) == 0;
//	}
	
	public int compareTo(double d){
		if (value < d) return -1;
		if (value > d) return 1;
		return 0;
	}
	
	public static ArrayList<MguiNumber> getZeros(int size){
		ArrayList<MguiNumber> result = new ArrayList<MguiNumber>(size);
		for (int i = 0; i < size; i++)
			result.add(new MguiDouble(0));
		return result;
	}
	
	@Override
	public String toString(){
		return toString("#####0.000000");
	}
	
	public String toString(String pattern){
		DecimalFormat mf = new DecimalFormat(pattern);
		//mf.setDecimalFormatSymbols(new DecimalFormatSymbols(NumberFunctions.thisLocale));
		mf.setDecimalFormatSymbols(new DecimalFormatSymbols(InterfaceEnvironment.getLocale()));
		return mf.format(value);
	}
	
	public static String getString(double thisVal, String pattern){
		return (new MguiDouble(thisVal)).toString(pattern);
	}
	
	public static String getString(double thisVal, int precision){
		return getString(thisVal, precision, true);
	}
	
	public static String getString(double thisVal, int precision, boolean no_commas){
		if (precision <= 0) return getString(thisVal, "#####0"); 
		String s = "#####0.0";
		if (no_commas) s = "#0.0";
		for (int i = 0; i < precision - 1; i++)
			s = s + "0";
		return getString(thisVal, s);
	}
	
	public boolean setValue(String val){
		try{
			value = Double.valueOf(val);
		}catch (NumberFormatException e){
			return false;
			}
		return true;
	}
	
	@Override
	public Object clone(){
		return new MguiDouble(value);
	}
	
	public int compareTo(MguiNumber n) throws ClassCastException{
		if (!(n instanceof MguiNumber))
			throw new ClassCastException("Class cast exception: arDouble");
		else {
			if (n instanceof MguiFloat){
				MguiFloat f = (MguiFloat)n;
				if (f.value > value) return -1;
				if (f.value == value) return 0;
				return 1;
				}
			if (n instanceof MguiInteger){
				MguiInteger f = (MguiInteger)n;
				if (f.value > value) return -1;
				if (f.value == value) return 0;
				return 1;
				}
			if (n instanceof MguiLong){
				MguiLong f = (MguiLong)n;
				if (f.value > value) return -1;
				if (f.value == value) return 0;
				return 1;
				}
			if (n instanceof MguiDouble){
				MguiDouble f = (MguiDouble)n;
				if (f.value > value) return -1;
				if (f.value == value) return 0;
				return 1;
				}
			}
		return 0;
	}
	
	public int compareTo(double value, int precision){
		return NumberFunctions.compare(value, this.value, precision);
	}
	
	public int getByteSize(){
		return Double.SIZE / 8;
	}
	
	public MguiNumber add(double n) {
		value += n;
		return this;
	}

	public MguiNumber divide(double n) {
		value /= n;
		return this;
	}

	public MguiNumber multiply(double n) {
		value *= n;
		return this;
	}

	public MguiNumber subtract(double n) {
		value -= n;
		return this;
	}
	
	@Override
	public String getDTD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalName(){
		return "MguiDouble";
	}

	@Override
	public String getXML() {
		return getXML(0);
	}

	@Override
	public String getXML(int tab) {
		return XMLFunctions.getTab(tab) + value;
	}

	@Override
	public String getXMLSchema() {
		
		return null;
	}

	@Override
	public void handleXMLElementEnd(String localName) {
		
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		
	}
	
	@Override
	public void handleXMLString(String s){
		this.setValue(s);
	}

	public static XMLObject getXMLInstance(Attributes attributes){
		return new MguiDouble();
	}
	
	@Override
	public void writeXML(int tab, Writer writer) throws IOException{
		writeXML(tab, writer, null);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException{
		writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		//XML string should be small, so get it directly
		writer.write(getXML(tab));
	}
	
	@Override
	public String getShortXML(int tab){
		return getXML(tab);
	}
	
}