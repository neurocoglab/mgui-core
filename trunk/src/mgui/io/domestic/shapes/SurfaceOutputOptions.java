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

package mgui.io.domestic.shapes;

import java.io.File;

import javax.swing.JFileChooser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject.XMLType;
import mgui.io.InterfaceIOOptions;

/**********************************************************
 * Standard options for writing a surface to file.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class SurfaceOutputOptions extends InterfaceIOOptions {

	InterfaceDisplayPanel displayPanel; 
	public File[] files;
	public Mesh3DInt mesh;
	public String number_format = "#0.00000";

	public SurfaceOutputOptions(){
		
	}
	
	protected String getXMLHeader(int tab) {
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab+1);
		return _tab + "<InterfaceIOOptions\n" +
				_tab2 + "class = '" + this.getClass().getCanonicalName() + "'\n" +
				_tab2 + "number_format = '" + number_format + "'\n";
		
	}
	
	@Override
	public String getXML(int tab) {
		String _tab = XMLFunctions.getTab(tab);
		String xml = getXMLHeader(tab);
		xml = xml + _tab + "/> ";
		return xml;
	}
	
	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) throws SAXException {
		if (localName.equals("InterfaceIOOptions")){
			// TODO: test for proper class...
			this.number_format = attributes.getValue("number_format");
			return;
			}
	}
	
	public void setDisplayPanel(InterfaceDisplayPanel p){
		displayPanel = p;
	}
	
	public InterfaceDisplayPanel getDisplayPanel(){
		return displayPanel;
	}
	
	public File[] getFiles() {
		return files;
	}

	public void setFiles(File[] files) {
		this.files = files;
	}

	public JFileChooser getFileChooser(){
		return getFileChooser(null);
	}
	
	public JFileChooser getFileChooser(File f){
		JFileChooser fc = null;
		if (f != null)
			fc = new JFileChooser(f);
		else
			fc = new JFileChooser();
		
		fc.setMultiSelectionEnabled(true);
		fc.setDialogTitle("Specify output file for mesh");
		return fc;
	}
	
	@Override
	public void setObject(InterfaceObject obj) throws ClassCastException{
		mesh = (Mesh3DInt)obj;
	}
	
}