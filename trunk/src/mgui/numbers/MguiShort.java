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

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.interfaces.xml.XMLObject.XMLType;
import mgui.io.standard.xml.XMLOutputOptions;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/*********************************************************
 * Implementation of {@link MguiNumber} for values of type <code>short</code>.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class MguiShort implements MguiNumber {

	protected short value;
	
	public MguiShort(){}
	
	public MguiShort(short thisValue){
		value = thisValue;
	}
	
	public double getValue(){
		return value;
	}
	
	public MguiShort(String thisValue){
		Short i = Double.valueOf(thisValue).shortValue();
		if (i == null){
			value = 0;
			return;
			}
		value = (short)Math.round(i);
	}
	
	public MguiShort(double value){
		this.value = (short)value;
	}
	
	public int compareTo(double value, int precision){
		return NumberFunctions.compare(value, this.value, precision);
	}
	
	public int getInt(){
		return value;
	}
	
	public static int getValue(String s){
		MguiInteger f = new MguiInteger(s);
		return (int)f.getValue();
	}
	
	public void setValue(MguiNumber val){
		setValue(val.getValue());
	}
	
	public void setValue(double thisValue){
		value = (short)thisValue;
	}
	
	public MguiNumber add(MguiNumber number){
		value += number.getValue();
		return this;
	}
	
	public MguiNumber divide(MguiNumber number){
		value = (short)((double)value / number.getValue());
		return this;
	}
	
	public MguiNumber subtract(MguiNumber number){
		value -= number.getValue();
		return this;
	}
	
	public MguiNumber multiply(MguiNumber number){
		value = (short)((double)value * number.getValue());
		return this;
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
		return obj == (Short)value;
	}
	
	@Override
	public String toString(){
		return toString("#####0");
	}
	
	public String toString(String pattern){
		double thisVal = value;
		DecimalFormat mf = new DecimalFormat(pattern);
		mf.setDecimalFormatSymbols(new DecimalFormatSymbols(InterfaceEnvironment.getLocale()));
		return mf.format(thisVal);
	}
	
	public boolean setValue(String val){
		val = removeCommas(val);
		try{
			value = Short.valueOf(val).shortValue();
		}catch (NumberFormatException e){
			return false;
		}
	return true;
	}
	
	public static String getString(int thisVal, String pattern){
		return (new MguiInteger(thisVal)).toString(pattern);
	}
	
	@Override
	public Object clone(){
		return new MguiInteger(value);
	}
	
	public int compareTo(MguiNumber n) throws ClassCastException{
		if (!(n instanceof MguiNumber))
			throw new ClassCastException("Class cast exception: arInteger");
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
	
	public static String removeCommas(String s) {
		   String r = "";
		   for (int i = 0; i < s.length(); i ++) {
		      if (s.charAt(i) != ',') r += s.charAt(i);
		      }
		   return r;
		}
	
	public int compareTo(double d){
		if (value < d) return -1;
		if (value > d) return 1;
		return 0;
	}
	
	public int getByteSize(){
		return Integer.SIZE / 8;
	}
	
//	@Override
//	public boolean equals(Object o){
//		if (!(o instanceof MguiNumber)) return false;
//		return compareTo((MguiNumber)o) == 0;
//	}
	
	@Override
	public int hashCode(){
		return value;
	}
	
	public MguiNumber add(double n) {
		value += n;
		return this;
	}

	/**************************************
	 * Divides this number by {@code n}. Treats the values as doubles for the operation.
	 * 
	 */
	@Override
	public MguiNumber divide(double n) {
		value = (short)((double)value / n);
		return this;
	}

	/**************************************
	 * Multiplies this number by {@code n}. Treats the values as doubles for the operation.
	 * 
	 */
	@Override
	public MguiNumber multiply(double n) {
		value = (short)((double)value * n);
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
		return "MguiShort";
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
		return new MguiInteger();
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