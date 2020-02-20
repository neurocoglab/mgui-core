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

package mgui.io.domestic.pipelines;

import java.util.ArrayList;
import java.util.HashMap;

import mgui.pipelines.JavaProcess;
import mgui.pipelines.NativeProcess;
import mgui.pipelines.PipelineProcess;
import mgui.pipelines.TaskParameter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*******************************************************
 * XML handler for reading pipeline process libraries.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * @see mgui.interfaces.pipelines.PipelineProcessLibrary
 *
 */
public class PipelineProcessLibraryXMLHandler extends DefaultHandler {

	public String library_name;
	public PipelineProcess current_process;
	public ArrayList<PipelineProcess> processes = new ArrayList<PipelineProcess>();
	public HashMap<String,String> environment;
	
	public PipelineProcessLibraryXMLHandler(){
		super();
	}
	
	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		
		if (localName.equals("PipelineProcessLibrary")){
			if (attributes.getValue("name") != null)
				library_name = attributes.getValue("name");
			}
		
		
		if (localName.equals("JavaProcess")){
			if (current_process != null)
				throw new SAXException("PipelineProcessLibraryXMLHandler: Process element started without ending previous..");
			
			current_process = new JavaProcess(attributes.getValue("name"), attributes.getValue("main_class"));
			
			if (attributes.getValue("logger") != null)
				current_process.setLogger(attributes.getValue("logger"));
			
			if (attributes.getValue("input_parameter") != null)
				current_process.setInputParameter(attributes.getValue("input_parameter"));
			
			if (attributes.getValue("output_parameter") != null)
				current_process.setOutputParameter(attributes.getValue("output_parameter"));
			
			if (attributes.getValue("help_parameter") != null)
				current_process.setHelpParameter(attributes.getValue("help_parameter"));
			
			return;
			}
		
		if (localName.equals("NativeProcess")){
			if (current_process != null)
				throw new SAXException("PipelineProcessLibraryXMLHandler: Process element started without ending previous..");
			
			current_process = new NativeProcess(attributes.getValue("name"), attributes.getValue("command"));
			
			if (attributes.getValue("input_parameter") != null)
				current_process.setInputParameter(attributes.getValue("input_parameter"));
			
			if (attributes.getValue("output_parameter") != null)
				current_process.setOutputParameter(attributes.getValue("output_parameter"));
			
			if (attributes.getValue("help_parameter") != null)
				current_process.setHelpParameter(attributes.getValue("help_parameter"));
			
			if (attributes.getValue("logger") != null)
				current_process.setLogger(attributes.getValue("logger"));
			
			if (attributes.getValue("path") != null)
				((NativeProcess)current_process).setPath(attributes.getValue("path"));
			
			if (attributes.getValue("set_operator") != null)
				((NativeProcess)current_process).setSetOperator(attributes.getValue("set_operator"));
			
			return;
			}
		
		if (current_process == null){
			if (localName.equals("Environment")){
				if (environment != null)
					throw new SAXException("PipelineProcessLibraryXMLHandler: Environment started while one already exists..");
				
				environment = new HashMap<String,String>();
				
				return;
				}
			
			if (localName.equals("Element")){
				if (environment == null)
					throw new SAXException("PipelineProcessLibraryXMLHandler: Environment element started while no environment started..");
				
				environment.put(attributes.getValue("name"), attributes.getValue("value"));
				
				return;
				}
			}
		
		if (localName.equals("TaskParameter")){
			if (current_process == null)
				throw new SAXException("PipelineProcessLibraryXMLHandler: TaskParameter added with no current Process..");
			
			TaskParameter p = new TaskParameter(attributes.getValue("name"));
			p.optional = Boolean.valueOf(attributes.getValue("optional"));
			p.default_value = attributes.getValue("default_value");
			p.has_value = Boolean.valueOf(attributes.getValue("has_value"));
			p.use_name = Boolean.valueOf(attributes.getValue("use_name"));
			current_process.addParameter(p);
			return;
			}
		
	}
	
	
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		
		if (localName.equals("JavaProcess")){
			if (current_process == null)
				throw new SAXException("PipelineProcessLibraryXMLHandler: Process element ended without being started..");
			
			if (!(current_process instanceof JavaProcess))
				throw new SAXException("PipelineProcessLibraryXMLHandler: JavaProcess element ended without being " +
									   "started as a JavaProcess..");
			
			processes.add(current_process);
			current_process = null;
			return;
			}
		
		if (localName.equals("NativeProcess")){
			if (current_process == null)
				throw new SAXException("PipelineProcessLibraryXMLHandler: Process element ended without being started..");
			
			if (!(current_process instanceof NativeProcess))
				throw new SAXException("PipelineProcessLibraryXMLHandler: NativeProcess element ended without being " +
									   "started as a NativeProcess..");
			
			processes.add(current_process);
			current_process = null;
			return;
			}
		
		if (current_process == null && localName.equals("Environment")){
			
			if (environment == null)
				throw new SAXException("PipelineProcessLibraryXMLHandler: Environment ended without being started..");
			
//			((NativeProcess)current_process).setEnvironment(environment);
//			environment = null;
			
			return;
			}
		
		if (localName.equals("TaskParameter")){
			return;
			}
		
	}
	
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		
		
		
	}
	
	
}