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

package mgui.interfaces.graphics.video.xml;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.graphics.video.Video;
import mgui.interfaces.graphics.video.Video3D;
import mgui.interfaces.graphics.video.VideoTask;
import mgui.interfaces.graphics.video.VideoTaskType;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**************************************************
 * A handler to load a video in XML format.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class VideoXMLHandler extends DefaultHandler {

	Video video;
	VideoTask currentTask;
	
	public VideoXMLHandler(){
		super();
	}
	
	public Video getVideo(){
		return video;
	}
	
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		
		if (localName.equals("VideoTask")){
			video.addTask(currentTask);
			currentTask = null;
			return;
			}
		
		if (localName.equals("Video")){
			//displayPanel.addVideo(video);
			return;
			}
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		
		if (currentTask != null){
			currentTask.handleXMLElementStart(localName, attributes, null);
			return;
			}
		
		if (localName.equals("Video")){
			if (attributes.getValue("video_type").equals("3D")){
				video = new Video3D(attributes.getValue("name"));
				video.handleXMLElementStart(localName, attributes, null);
			}else{
				throw new SAXException("Invalid video type: " + attributes.getValue("video_type"));
				}
			return;
			}
		
		if (localName.equals("VideoTask")){
			String c_name = attributes.getValue("class");
			try {
				Class c = Class.forName(c_name);
				VideoTaskType type = InterfaceEnvironment.getVideoTaskType(c);
				if (type == null) 
					throw new SAXException("Could not find type for task " + c_name + ".");
				currentTask = type.getTaskInstance();
				currentTask.handleXMLElementStart(localName, attributes, null);
				return;
			} catch (ClassNotFoundException e) {
				throw new SAXException("Could not find class for task " + c_name + ".");
				}
			}
		
	}

	
	
	
	
	
	
	
	
}