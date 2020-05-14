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

package mgui.io.domestic.pipelines;

import java.util.ArrayList;

import mgui.pipelines.InterfacePipeline;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*******************************************
 * Handler for XML encoding of modelGUI pipelines.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PipelineXMLHandler extends DefaultHandler {

	//CaminoTask current_task;
	ArrayList<InterfacePipeline> pipelines = new ArrayList<InterfacePipeline>();
	InterfacePipeline current_pipeline, last_pipeline;
	boolean in_tasks;
	TaskXMLHandler task_handler = new TaskXMLHandler();
	PipelineXMLHandler pipeline_handler;
	
	public PipelineXMLHandler(){
		super();
	}
	
	/****************************************
	 * Returns a list of the pipelines loaded from XML.
	 * 
	 * @return
	 */
	public ArrayList<InterfacePipeline> getPipelines(){
		return pipelines;
	}
	
	/****************************************
	 * Returns the last pipeline loaded from XML.
	 * 
	 * @return
	 */
	public InterfacePipeline getLastPipeline(){
		return last_pipeline;
	}
	
	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		
		if (pipeline_handler != null){
			pipeline_handler.startElement(uri, localName, name, attributes);
			return;
			}
		
		if (localName.equals("Pipeline")){
			if (in_tasks){
				pipeline_handler = new PipelineXMLHandler();
				pipeline_handler.startElement(uri, localName, name, attributes);
				return;
				}
			current_pipeline = new InterfacePipeline(attributes.getValue("name"));
			
			return;
			}
		
		if (localName.equals("Tasks")){
			in_tasks = true;
			return;
			}
		
		if (in_tasks)
			task_handler.startElement(uri, localName, name, attributes);
		
	}
	
	
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		
		if (pipeline_handler != null){
			pipeline_handler.endElement(uri, localName, name);
			current_pipeline.append(pipeline_handler.current_pipeline);
			pipeline_handler = null;
			return;
			}
		
		if (localName.equals("Tasks")){
			in_tasks = false;
			return;
			}
		
		if (localName.equals("ProcessInstance") && in_tasks){
			task_handler.endElement(uri, localName, name);
			current_pipeline.append(task_handler.task);
			return;
			}
		
		if (localName.equals("Pipeline")){
			if (current_pipeline == null)
				throw new SAXException("PipelineXMLHandler: Attempt to end current pipeline, but no pipeline started..");
			pipelines.add(current_pipeline);
			last_pipeline = current_pipeline;
			current_pipeline = null;
			return;
			}
		
	}
	
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		
		
		
	}
	
	
}