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

package mgui.interfaces.xml;

import java.io.IOException;
import java.io.Writer;

import mgui.interfaces.ProgressUpdater;
import mgui.io.standard.xml.XMLOutputOptions;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/*************************************************
 * Interface specifies methods for obtaining or writing the data contained in an implementing class in XML
 * format. 
 * 
 * <p>See <a href='http://www.saxproject.org'>http://www.saxproject.org</a> for further information.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public interface XMLObject {

	/****************************
	 * Defines the type of XML write to perform:
	 * 
	 * <ol>
	 * <li>Normal: 		Objects are written by reference if possible, fully otherwise
	 * <li>Full: 		Objects are fully written
	 * <li>Reference:	Objects are written by reference if possible, short otherwise
	 * <li>Short:		Objects are written in short form
	 * </ol>
	 * 
	 * @author Andrew Reid
	 *
	 */
	public static enum XMLType{
		Normal,
		Full,
		Reference,
		Short;
	}
	
	/*****************************************
	 * Defines the data I/O encoding, for internal XML representations. This
	 * value is ignored if an external writer/reader is defined.
	 * 
	 * @author Andrew Reid
	 *
	 */
	public static enum XMLEncoding{
		XML,
		Ascii,
		Base64Binary,
		Base64BinaryZipped,
		Base64BinaryGZipped;
	}
	
	/********************************
	 * Returns the Data Type Declaration (DTD) for this object's XML representation
	 * 
	 * <p>See <a href="http://en.wikipedia.org/wiki/Document_Type_Definition">
	 * http://en.wikipedia.org/wiki/Document_Type_Definition</a> for a description.
	 * 
	 * @return
	 */
	public String getDTD();
	
	/********************************
	 * Returns the XML schema for this object's XML representation
	 * 
	 * <p>See <a href="http://en.wikipedia.org/wiki/XML_schema">http://en.wikipedia.org/wiki/XML_schema</a>.
	 * 
	 * @return
	 */
	public String getXMLSchema();
	
	/********************************
	 * Returns this object's XML representation as a single string. NOTE: this is not feasible for larger
	 * objects and containers, thus may not be implemented for these objects. Use the {@link writeXML}
	 * functions to write larger objects to file.
	 * 
	 * @return
	 */
	public String getXML();
	
	/********************************
	 * Returns this object's XML representation as a single string. NOTE: this is not feasible for larger
	 * objects and containers, thus may not be implemented for these objects. Use the {@link writeXML}
	 * functions to write larger objects to file.
	 * 
	 * @param tab The number of tabs to place before the opening XML wrapper
	 * @return
	 */
	public String getXML(int tab);
	
	/********************************
	 * Handles the start of an XML element.
	 * 
	 * @param localName 	Local name of the element
	 * @param attributes	Set of element attributes
	 * @param type 			The {@code XMLType} of this element
	 */
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) throws SAXException;
	
	/********************************
	 * Handles the end of an XML element.
	 * 
	 * @param localName 	Local name of the element
	 */
	public void handleXMLElementEnd(String localName) throws SAXException;
	
	/********************************
	 * Handles a string within an XML element.
	 * 
	 * @param s				String to handle
	 */
	public void handleXMLString(String s) throws SAXException;
	
	/********************************
	 * Returns the local name associated with this XML object.
	 * 
	 * @return
	 */
	public String getLocalName();
	
	/********************************
	 * Writes the XML representation of this object to file. The basic contract for this method is that
	 * it should not write a newline character at its start or end. The default format of 
	 * {@code XMLFormat.Ascii} will be used.
	 * 
	 * @param tab				The number of tabs to place before the XML text
	 * @param writer			The writer
	 * @param options 			XMLOutputOptions defining the write parameters
	 * @param progress_bar		Optional progress updater (may be <code>null</code>)
	 * @throws IOException
	 */
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException;
		
	/********************************
	 * Writes the XML representation of this object to file, as <code>XMLType.Normal</code>. 
	 * The default format of {@code XMLFormat.Ascii} will be used.
	 * The basic contract for this method is that it should not write a newline character at its 
	 * start or end.
	 * 
	 * @param tab				The number of tabs to place before the XML text
	 * @param writer			The writer
	 * @param progress_bar		Optional progress updater (may be <code>null</code>)
	 * @throws IOException
	 */
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException;
	
	/********************************
	 * Writes the XML representation of this object to file. 
	 * The basic contract for this method is that
	 * it should not write a newline character at its start or end. The default format of 
	 * {@code XMLFormat.Ascii} will be used.
	 * 
	 * @param tab				The number of tabs to place before the XML text
	 * @param writer			The writer
	 * @throws IOException
	 */
	public void writeXML(int tab, Writer writer) throws IOException;
	
	/********************************
	 * Returns a short XML representation of this object.
	 * 
	 * @param tab
	 * @return
	 */
	public String getShortXML(int tab);
	
}