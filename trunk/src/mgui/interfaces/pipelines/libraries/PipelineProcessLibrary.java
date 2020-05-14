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

package mgui.interfaces.pipelines.libraries;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.pipelines.PipelineProcess;
import mgui.resources.icons.IconObject;

import org.xml.sax.Attributes;

/*******************************************************
 * Represents a collection of {@link PipelineProcess}es, typically referring to the same underlying
 * software library.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PipelineProcessLibrary extends AbstractInterfaceObject 
									implements IconObject,
											   XMLObject{

	protected AttributeList attributes;
	
	protected HashMap<String, PipelineProcess> processes = new HashMap<String, PipelineProcess>();
	protected HashMap<String, String> environment = new HashMap<String, String>();
	
	public PipelineProcessLibrary(String name){
		init();
		setName(name);
	}
	
	private void init(){
		
		attributes = new AttributeList();
		attributes.add(new Attribute("Name", "Unnamed"));
		
	}
	
	@Override
	public void setName(String name){
		attributes.setValue("Name", name);
	}
	
	@Override
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	/*****************************************************
	 * Sets the system environment for this process
	 * 
	 * @param environment
	 */
	public void setEnvironment(HashMap<String, String> environment){
		this.environment = environment;
	}
	
	/*****************************************************
	 * Gets the system environment for this process; returns {@code null} if no environment
	 * is set.
	 * 
	 * @return
	 */
	public HashMap<String, String> getEnvironment(){
		return environment;
	}
	
	public PipelineProcess getProcess(String name){
		return processes.get(name);
	}
	
	public void addProcess(PipelineProcess process){
		processes.put(process.getName(), process);
		process.setLibrary(this);
	}
	
	public void removeProcess(String name){
		PipelineProcess process = processes.get(name);
		if (process == null) return;
		process.setLibrary(null);
		processes.remove(name);
		
	}
	
	public ArrayList<PipelineProcess> getProcesses(){
		return new ArrayList<PipelineProcess>(processes.values());
	}
	
	@Override
	public String toString(){
		return "Pipeline Process Library: " + getName();
	}
	
	@Override
	public String getTreeLabel(){
		return getName();
	}
	
	@Override
	public Icon getObjectIcon(){
		
		URL imgURL = PipelineProcessLibrary.class.getResource("/mgui/resources/icons/pipelines/pipeline_process_library_20.png");
				
		if (imgURL == null){
			InterfaceSession.log("PipelineProcessLibrary: Can't find icon at /mgui/resources/icons/pipelines/pipeline_process_library_20.png");
			return null;
			}
		return new ImageIcon(imgURL);
	}
	
	@Override
	public String getDTD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalName() {
		return "PipelineProcessLibrary";
	}

	@Override
	public String getShortXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXML() {
		
		
		
		
		return null;
	}

	@Override
	public String getXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXMLSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleXMLElementEnd(String localName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLString(String s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException {
		writeXML(tab, writer, new XMLOutputOptions(), null);
	}

	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		writer.write(_tab + "<" + getLocalName() + " name = '" + getName() + "'>\n");
		writer.write(_tab2 + "<Processes>\n");
		
		ArrayList<PipelineProcess> processes = getProcesses();
		for (int i = 0; i < processes.size(); i++){
			processes.get(i).writeXML(tab + 2, writer, options, progress_bar);
			}
		
		writer.write(_tab2 + "</Processes>\n");
		
		if (environment != null){
			String xml = _tab2 + "<Environment>\n";
			String _tab3 = XMLFunctions.getTab(tab + 2);
			ArrayList<String> keys = new ArrayList<String>(environment.keySet());
			Collections.sort(keys);
			for (int i = 0; i < keys.size(); i++)
				xml = xml + _tab3 + "<Element name='" + keys.get(i) + 
									"' value='" + environment.get(keys.get(i)) + "' />\n"; 
			xml = xml + _tab2 + "</Environment>\n";
			writer.write(xml);
			}
		
		writer.write(_tab + "</" + getLocalName() + ">");
	}

	@Override
	public void writeXML(int tab, Writer writer) throws IOException {
		writeXML(tab, writer, null);
	}

	

	
	
}