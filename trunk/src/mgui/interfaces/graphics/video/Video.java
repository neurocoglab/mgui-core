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

package mgui.interfaces.graphics.video;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;

import org.xml.sax.Attributes;

import foxtrot.Job;
import foxtrot.Worker;

/*********************************************************
 * Represents a video schedule, using a list of <code>VideoTask</code> objects which are activated at
 * specific time points. A <code>VideoTask</code> typically effects some visible change to an 
 * <code>InterfaceGraphic</code> window, which occurs at run-time, and can optionally be written as
 * a movie file.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * @see mgui.interfaces.graphics.video.VideoTask
 *
 */
public abstract class Video extends AbstractInterfaceObject implements XMLObject {

	AttributeList attributes = new AttributeList();
	ArrayList<VideoListener> listeners = new ArrayList<VideoListener>();
	public ArrayList<VideoTask> tasks = new ArrayList<VideoTask>();
	public long refresh = 10;		//refresh rate (<= 0 indicates 1 msec)
	public long clock;
	public long duration = 60000;
	boolean stopped;
	public InterfaceGraphic<?> window;
	
	protected void init(){
		attributes.add(new Attribute<String>("Name", "no name"));
	}
	
	/******************************
	 * Returns all tasks in this video.
	 * 
	 * @return
	 */
	public ArrayList<VideoTask> getTasks(){
		return tasks;
	}
	
	/******************************
	 * Gets the current state of the clock.
	 * 
	 * @return
	 */
	public long getClock(){
		return clock;
		
	}
	
	/******************************
	 * Determines whether this video is currently running or not.
	 * 
	 * @return
	 */
	public boolean isStopped(){
		return stopped;
	}
	
	/******************************
	 * Initiates the running of this video. Alerts all video listeners of this. 
	 * 
	 */
	public void start(){
		stopped = false;
		fireClockStarted(new VideoEvent(this));
	}
	
	/******************************
	 * Resets the clock and all tasks in this video. Alerts all video listeners of this.
	 * 
	 */
	public void reset(){
		clock = 0;
		for (int i = 0; i < tasks.size(); i++)
			tasks.get(i).reset();
		fireClockChanged(new VideoEvent(this));
	}
	
	/*******************************
	 * Restarts this video by resetting the clock and resuming playback. Alerts all
	 * video listeners of this.
	 * 
	 * @throws VideoException
	 */
	public void restart() throws VideoException{
		reset();
		resume();
	}
	
	/******************************
	 * Stops the video and alerts all video listeners of this.
	 */
	public void stop(){
		stopped = true;
		fireClockStopped(new VideoEvent(this));
	}
	
	/******************************
	 * Sets the clock to a specific value and alerts all listeners of this.
	 * 
	 * @param c
	 */
	public void setClock(long c){
		clock = c;
		fireClockChanged(new VideoEvent(this));
	}
	
	/******************************
	 * Sets the refresh rate of this video.
	 * 
	 * @param rate
	 */
	public void setRefreshRate(long rate){
		if (rate <= 0) refresh = 1;
		else refresh = rate;
	}
	
	@Override
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	@Override
	public void setName(String name){
		attributes.setValue("Name", name);
	}
	
	public void setTasks(ArrayList<VideoTask> tasks){
		this.tasks = tasks;
		sortTasks();
	}
	
	public void sortTasks(){
		Collections.sort(tasks);
	}
	
	public void addTask(VideoTask task){
		tasks.add(task);
		sortTasks();
	}
	
	public void removeTask(VideoTask task){
		tasks.remove(task);
	}
	
	
	public void addListener(VideoListener l){
		listeners.add(l);
	}
	
	public void removeListener(VideoListener l){
		listeners.remove(l);
	}
	
	public void fireClockChanged(VideoEvent e){
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).ClockChanged(e);
	}
	
	public void fireClockStopped(VideoEvent e){
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).ClockStopped(e);
	}
	
	public void fireClockStarted(VideoEvent e){
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).ClockStarted(e);
	}
	
	public void fireClockLagged(VideoEvent e, long lag){
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).ClockLagged(e, lag);
	}
	
	/***********************************************
	 * Resumes playback of this video. Playback will be handled in a separate Foxtrot thread, so
	 * this method returns after initiating it. Video loops at a rate determined by its refresh
	 * rate, unless this is smaller than the time lag required for processing task procedures.
	 *
	 * Playback can be stopped by calling the <code>stop</code> method.
	 * 
	 * @throws VideoException if no source window is set
	 */
	public void resume() throws VideoException{
		
		if (window == null) throw new VideoException("Video '" + getName() + "': no window set!");
		
		stopped = false;
		if (clock > duration){
			stopped = true;
			return;
			}
		
		final VideoEvent ev = new VideoEvent(this);
		
		Worker.post(new Job(){
			@Override
			public Boolean run(){
				
				InterfaceSession.log("Video '" + getName() + "' resumed at " + clock + " (duration = " + duration + ")");
				//loop through tasks until stop
				
				long clock_start = clock;
				long start = System.currentTimeMillis();
				long start_loop, lag;
				
				while (!stopped) {
					try{
						start_loop = System.currentTimeMillis();
						for (int i = 0; i < tasks.size(); i++)
							//lag += registerTask(tasks.get(i));
							tasks.get(i).perform(window, clock);
						
						lag = System.currentTimeMillis() - start_loop;
						if (lag > refresh) fireClockLagged(ev, lag - refresh);
						lag = Math.max(lag, refresh - lag);
						Thread.sleep(lag);
						
						clock = clock_start + System.currentTimeMillis() - start;
						fireClockChanged(ev);
						
						if (clock > duration) stop();
							//stopped = true;
					
					}catch (InterruptedException e){
						//stopped = true;
						stop();
						break;
						}
					}
				
				InterfaceSession.log("Video '" + getName() + "' exited at " + clock + " (duration = " + duration + ")");
				return true;
			}
			
		});
	}
	
	public abstract String getType();
	
	public String getDTD(){
		//TODO: retrieve from file
		return "";
	}
	
	public String getXMLSchema(){
		//TODO: retrieve from file
		return "";
	}
	
	public String getXML(int tab){
		//get schema or dtd
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		String xml = _tab + "<Video\n";
		xml = xml + _tab2 + "video_type = '" + getType() + "'\n";	//type
		xml = xml + _tab2 + "name = '" + getName() + "'\n";			//name
		xml = xml + _tab2 + "refresh = '" + refresh + "'\n";		//refresh
		xml = xml + _tab2 + "duration = '" + duration + "'\n";		//duration
		xml = xml + _tab2 + ">\n";
		
		//Tasks
		if (tasks.size() > 0){
			xml = xml + _tab2 + "<Tasks>\n";
			for (int i = 0; i < tasks.size(); i++)
				xml = xml + tasks.get(i).getXML(tab + 2);
			xml = xml + _tab2 + "</Tasks>\n";
			}
		
		xml = xml + _tab + "</Video>\n";
		return xml;
	}
	
	public void updateTasks(InterfaceDisplayPanel panel){
		for (int i = 0; i < tasks.size(); i++)
			tasks.get(i).updateTask(panel);
	}
	
	public String getXML(){
		return getXML(0);
	}
	
	public String getShortXML(int tab){
		return XMLFunctions.getTab(tab) + "<Video name = '" + getName() + "' />\n";
	}
	
	public void handleXMLString(String s){
		
	}
	
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type){
		
		if (localName.equals(getLocalName())){
			refresh = Long.valueOf(attributes.getValue("refresh"));
			duration = Long.valueOf(attributes.getValue("duration"));
			}
		
	}
	
	public void handleXMLElementEnd(String localName){
		
	}
	
	@Override
	public void writeXML(int tab, Writer writer) throws IOException{
		writeXML(tab, writer, null);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException{
		this.writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		//XML string should be small, so get it directly
		writer.write(getXML(tab));
	}
	
	public String getLocalName(){
		return "Video";
	}
	
}