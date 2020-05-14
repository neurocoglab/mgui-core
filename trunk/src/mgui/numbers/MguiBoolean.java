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

import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.interfaces.xml.XMLObject.XMLType;
import mgui.io.standard.xml.XMLOutputOptions;

import org.xml.sax.Attributes;

/*********************************************************
 * Implementation of {@link MguiNumber} for values of type <code>boolean</code>. As a number,
 * this treats a value as <code>0 == false</code> and any other value is considered <code>true</code>.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class MguiBoolean implements MguiNumber {

	protected boolean value;
	
	public MguiBoolean(){}
	
	public void setValue(MguiNumber val){
		setValue(val.getValue());
	}
	
	public MguiBoolean(String thisValue){
		value = new Boolean(thisValue);
	}
	
	public MguiBoolean(boolean thisValue){
		value = thisValue;
	}
	
	public void setValue(double thisValue){
		if (thisValue == 0)
			value = true;
		else
			value = false;
	}
	
	public int compareTo(double value, int precision){
		return 0;
	}
	
	public void setValue(boolean thisValue){
		value = thisValue;
	}
	
	@Override
	public String toString(){
		return toString("");
	}
	
	public boolean getTrue(){
		return value;
	}
	
	public void setTrue(boolean b){
		value = b;
	}
	
	public double getValue(){
		if (value) return -1;
		return 0;
	}
	
	public String toString(String pattern){
		if (value)
			return "True";
		return "False";
	}
	
	public boolean setValue(String val){
			value = Boolean.valueOf(val).booleanValue();
		
		return true;
	}
	
	@Override
	public Object clone(){
		return new MguiBoolean(value);
	}
	
	public int compareTo(MguiNumber n) throws ClassCastException{
		return 0;
	}
	
	public boolean equals(Object obj){
		int val = 0;
		if (value) val = 1;
		if (obj instanceof Integer)
			return val == (Integer)obj;
		if (obj instanceof Double)
			return val == (Double)obj;
		if (obj instanceof Float)
			return val == (Float)obj;
		if (obj instanceof Long)
			return val == (Long)obj;
		if (obj instanceof MguiNumber)
			return compareTo((MguiNumber)obj) == 0;
		return obj == (Boolean)value;
	}
	
//	@Override
//	public boolean equals(Object o){
//		if (!(o instanceof MguiBoolean)) return false;
//		return compareTo((MguiBoolean)o) == 0;
//	}
	
	//these do nothing on booleans
	public MguiNumber add(MguiNumber number){return this;}
	public MguiNumber divide(MguiNumber number){return this;}
	public MguiNumber subtract(MguiNumber number){return this;}
	public MguiNumber multiply(MguiNumber number){return this;}
	public MguiNumber add(double n) {return this;}
	public MguiNumber divide(double n) {return this;}
	public MguiNumber multiply(double n) {return this;}
	public MguiNumber subtract(double n) {return this;}
	
	//boolean is 1 bit
	public int getByteSize(){
		return 0;
	}
	
	public int compareTo(double d){
		return 1;
	}
	
	public static String toString(boolean b){
		if (b)
			return "True";
		return "False";
	}
	
	@Override
	public String getDTD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalName(){
		return "MguiBoolean";
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
		return new MguiBoolean();
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