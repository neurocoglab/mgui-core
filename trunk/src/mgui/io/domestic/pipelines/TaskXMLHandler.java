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

import mgui.interfaces.InterfaceEnvironment;
import mgui.pipelines.PipelineProcess;
import mgui.pipelines.PipelineProcessInstance;
import mgui.pipelines.TaskParameterInstance;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**************************************************************
 * Handles XML for a {@linkplain PipelineProcessInstance}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class TaskXMLHandler extends DefaultHandler {

	public PipelineProcessInstance task;
	
	public TaskXMLHandler(){
		super();
	}
	
	//TODO: get processes from InterfaceEnvironment
	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		
		if (localName.equals("ProcessInstance")){
			String p_name = attributes.getValue("name");
			PipelineProcess process = InterfaceEnvironment.getPipelineProcess(p_name);
			if (process == null)
				throw new SAXException("TaskXMLHandler: Attempted to add process instance, but process '" + p_name + "' is not registered..");
			task = new PipelineProcessInstance(process, 0);
			String s = attributes.getValue("input");
			if (s.equals("none"))
				task.setHasInput(false);
			else if (!s.equals("previous"))
				task.setInputFile(s);
			s = attributes.getValue("output");
			if (!s.equals("none"))
				task.setOutputFile(s);
			task.setPrependSubjectInput(Boolean.valueOf(attributes.getValue("prepend_subject_input")));
			task.setPrependSubjectOutput(Boolean.valueOf(attributes.getValue("prepend_subject_output")));
			return;
			}
		
		if (localName.equals("TaskParameterInstance")){
			String p_name = attributes.getValue("name");
			TaskParameterInstance p_inst = task.getParameter(p_name);
			
			if (p_inst == null)
				throw new SAXException("TaskXMLHandler: Attempted to set parameter instance, but parameter '" + p_name + 
									   "' is not part of process '" + task.getProcess().getName() + "'..");
			
			p_inst.value = attributes.getValue("value");
			p_inst.apply = Boolean.valueOf(attributes.getValue("apply"));
			return;
			}
		
	}
	
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		
		
		
	}
	
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		
		
		
	}
	
}