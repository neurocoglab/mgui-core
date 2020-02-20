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

package mgui.interfaces.graphics.video;

import java.io.IOException;
import java.io.Writer;

import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;

import org.xml.sax.Attributes;

/****************************************************
 * Base class for a video task, which manipulates an {@link InterfaceGraphic}
 * window or its associated {@link InterfaceObject}s, over a specified time 
 * interval. Video tasks form the elements of a {@link Video} sequence.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class VideoTask implements Cloneable,
										   Comparable<VideoTask>,
										   XMLObject{

	public long start_time, stop_time;
	protected boolean started = false;
	public boolean isOn = true;
	
	/********************************************************
	 * Performs this video task, for the given {@code time}.
	 * 
	 * @param g
	 * @param time
	 * @return
	 */
	public boolean perform(InterfaceGraphic<?> g, long time){
		if (!isActive(time) || !isOn) return false;
		try{
			return do_it(g, time);
		}catch (VideoException e){
			InterfaceSession.handleException(e);
			return false;
			}
	}
	
	/******************************
	 * Perform this task, contingent on {@code time}.
	 * 
	 * @param g
	 * @param time
	 * @return
	 * @throws VideoException
	 */
	protected abstract boolean do_it(InterfaceGraphic<?> g, long time) throws VideoException;
	
	/****************************
	 * Sets the start time for this task
	 * 
	 * @param d
	 */
	public void setStart(long d){
		start_time = d;
	}
	
	/****************************
	 * Sets the stop time for this task
	 * 
	 * @param d
	 */
	public void setStop(long d){
		stop_time = d;
	}
	
	/*****************************
	 * Returns the start time for this task
	 * 
	 * @return
	 */
	public long getStart(){
		return start_time;
	}
	
	/*****************************
	 * Returns the stop time for this task
	 * 
	 * @return
	 */
	public long getStop(){
		return stop_time;
	}
	
	/*****************************
	 * Resets this task
	 * 
	 * @return
	 */
	public void reset(){
		started = false;
	}
	
	public boolean isActive(long time){
		if (start_time == stop_time) return (!started && time >= start_time);
		return time < stop_time && time >= start_time;
	}
	
	public abstract String getName();
	
	public String getDTD(){
		//TODO: retrieve from file
		return "";
	}
	
	public String getSchema(){
		//TODO: retrieve from file
		return "";
	}
	
	public String getXML(int tab){
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		
		String xml = _tab + "<VideoTask\n";
		xml = xml + _tab2 + "class = '" + getClass().getCanonicalName() + "'\n";	//class
		xml = xml + _tab2 + "start = '" + start_time + "'\n";						//start
		xml = xml + _tab2 + "stop = '" + stop_time + "'\n";							//stop
		return xml;
		
	}
	
	public String getShortXML(){
		return getXML();
	}
	
	public String getXML(){
		return getXML(0);
	}
	
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type){
		
		if (localName.equals("VideoTask")){
			start_time = Long.valueOf(attributes.getValue("start"));
			stop_time = Long.valueOf(attributes.getValue("stop"));
			}
		
	}
	
	public void handleXMLElementEnd(String localName){
		
	}
	
	public void handleXMLString(String s){
		
	}
	
	public String getLocalName(){
		return "VideoTask";
	}
	
	public void updateTask(InterfaceDisplayPanel panel){
		
	}
	
	public int compareTo(VideoTask t2){
		
		if (start_time < t2.start_time) return -1;
		if (start_time > t2.start_time) return 1;
		if (stop_time < t2.stop_time) return -1;
		if (stop_time > t2.stop_time) return 1;
		return 0;
		
	}
	
	public abstract void setFromTask(VideoTask task);
	
	@Override
	public abstract Object clone();
	
	public void writeXML(int tab, Writer writer) throws IOException{
		writeXML(tab, writer, null);
	}
	
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException{
		this.writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
			//XML string should be small, so get it directly
		writer.write(getXML(tab));
	}
	
	public String getShortXML(int tab) {
		return getXML();
	}
	
}