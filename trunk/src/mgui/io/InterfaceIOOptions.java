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

package mgui.io;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import javax.swing.JFileChooser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.xml.XMLObject;
import mgui.interfaces.xml.XMLObject.XMLType;
import mgui.io.standard.xml.XMLOutputOptions;

/***********************************
 * 
 * Extends <code>InterfaceOptions</code> specifically for I/O methods. This class should be
 * used to specify parameters/variables/objects required for specific I/O operations. It also specifies a number of
 * standard variables (e.g., a file/URL list) and abstract methods. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class InterfaceIOOptions extends InterfaceOptions
										 implements XMLObject{

	

	/**********************************************
	 * Returns the list of files associated with this options instance
	 * 
	 * @return
	 */
	public abstract File[] getFiles();
	
	/**********************************************
	 * Sets the list of files associated with this options instance
	 * 
	 * @return
	 */
	public abstract void setFiles(File[] files);
	
	/**********************************************
	 * Returns a file chooser appropriate for this options instance
	 * 
	 * @return
	 */
	public abstract JFileChooser getFileChooser();
	
	/**********************************************
	 * Returns a file chooser appropriate for this options instance
	 * 
	 * @param file
	 * @return
	 */
	public abstract JFileChooser getFileChooser(File file);
	
	/**********************************************
	 * Sets the object on which to perform an I/O operation
	 * 
	 * @param obj
	 * @throws ClassCastException if {@code obj} is the wrong class for this options instance
	 */
	public abstract void setObject(InterfaceObject obj) throws ClassCastException;
	
	/**********************************************
	 * Sets the values of this object from those of {@code options}, defined for its 
	 * complementary I/O interface.
	 * 
	 * @param options
	 */
	public void setFromComplementaryOptions(InterfaceIOOptions options){
		
	}
	
	@Override
	public String getDTD() {
		return null;
	}

	@Override
	public String getXMLSchema() {
		return null;
	}

	@Override
	public String getXML() {
		return "";
	}

	@Override
	public String getXML(int tab) {
		return "";
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) throws SAXException {
		
	}

	@Override
	public void handleXMLElementEnd(String localName) throws SAXException {
		
	}

	@Override
	public void handleXMLString(String s) throws SAXException {
		
	}

	@Override
	public String getLocalName() {
		return "InterfaceIOOptions";
	}

	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
		writer.write(getXML(tab));
	}

	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException {
		writer.write(getXML(tab));
	}

	@Override
	public void writeXML(int tab, Writer writer) throws IOException {
		writer.write(getXML(tab));
	}

	@Override
	public String getShortXML(int tab) {
		
		return "";
	}
	
}