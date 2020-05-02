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

package mgui.io.standard.xml;

import java.io.File;

import javax.swing.JFileChooser;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.xml.XMLObject;
import mgui.interfaces.xml.XMLObject.XMLEncoding;
import mgui.interfaces.xml.XMLObject.XMLType;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;

/**************************************************
 * Specifies options for writing an XML object (i.e., an instance of {@link XMLObject}). 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class XMLOutputOptions extends InterfaceIOOptions {

	public File file;
	public XMLType type = XMLType.Full; 
	public XMLObject object;
	public XMLEncoding encoding = XMLEncoding.Base64BinaryZipped;
	public int sig_digits = 6;
	public int max_line_size = 100;
	public String delimiter = " ";
	public FileWriter writer;
	public InterfaceIOOptions io_options;
	public String filename;
	
	public File[] getFiles() {
		return new File[]{file};
	}

	public void setFiles(File[] files) {
		file = files[0];
	}

	public JFileChooser getFileChooser(){
		return getFileChooser(null);
	}
	
	@Override
	public void setObject(InterfaceObject obj) throws ClassCastException{
		object = (XMLObject)obj;
	}
	
	@Override
	public JFileChooser getFileChooser(File file) {
		return null;
	}

	
}