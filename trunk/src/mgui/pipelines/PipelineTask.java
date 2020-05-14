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

package mgui.pipelines;

import java.awt.EventQueue;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.pipelines.trees.TaskTreeNode;
import mgui.interfaces.pipelines.trees.TaskTreeNodeEvent;
import mgui.interfaces.pipelines.trees.TaskTreeNodeListener;
import mgui.interfaces.projects.InterfaceProject;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.resources.icons.IconObject;
import mgui.util.TimeFunctions;

import org.xml.sax.Attributes;

/**********************************************
 * A task is a launchable component of a pipeline. Tasks can be either pipelines themselves or the
 * process instances that execute data processing.
 *  
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class PipelineTask extends AbstractInterfaceObject implements IconObject,
																			  Serializable,
																			  TaskTreeNodeListener,
																			  XMLObject,
																			  Cloneable,
																			  Transferable,
																			  Comparable<PipelineTask>{

	protected long start, current;
	protected AttributeList attributes = new AttributeList();
	
	
	public enum Status{
		NotStarted,
		Processing,
		Success,
		Failure,
		Interrupted;
	}
	
	Status status = Status.NotStarted;
	
	ArrayList<PipelineTaskListener> listeners = new ArrayList<PipelineTaskListener>();
	
	protected PipelineTask(){
		init();
	}
	
	protected PipelineTask(String name){
		init();
		setName(name);
	}
	
	private void init(){
		attributes.add(new Attribute<String>("Name", "No name"));
			
	}
	
	@Override
	public int compareTo(PipelineTask task){
		return this.getName().compareTo(task.getName());
	}
	
	@Override
	public void setName(String name){
		String old_name = getName();
		attributes.setValue("Name", name);
		StaticPipelineEvent e = new StaticPipelineEvent(this, StaticPipelineEvent.EventType.TaskRenamed, old_name);
		if (this.getPipeline() != null)
			this.getPipeline().fireStaticListeners(e);
		updateTreeNodes();
	}
	
	@Override
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	public abstract InterfacePipeline getPipeline();
	public abstract void setPipeline(InterfacePipeline pipeline);
	
	/***********************
	 * Returns the status of this task; one of:
	 * 
	 * <ul>
	 * <li><code>NotStarted</code>
	 * <li><code>Processing</code>
	 * <li><code>Success</code>
	 * <li><code>Failure</code>
	 * </ul>
	 * 
	 * @return
	 */
	public Status getStatus() {
		return status;
	}
	
	/***********************
	 * Resets this task, and sets its status to <code>NotStarted</code>.
	 * 
	 */
	public void reset(){
		setStatus(Status.NotStarted);
	}
	
	/***********************
	 * Attempts to terminate the execution of this task, if it is started, 
	 * and sets its status to {@code Terminated}.
	 * 
	 */
	public boolean interrupt() throws PipelineException{
		setStatus(Status.Interrupted);
		return true;
	}
	
	/***********************
	 * Returns the time, in milliseconds, elapsed since this task was started.
	 * 
	 * @return
	 */
	public long getElapsedTime(){
		return current - start;
	}

	/************************
	 * Returns the status of this task as a string; one of:
	 * 
	 * <ul>
	 * <li><code>NotStarted</code>
	 * <li><code>Processing</code>
	 * <li><code>Success</code>
	 * <li><code>Failure</code>
	 * </ul>
	 * 
	 * @return
	 */
	public String getStatusStr() {
		String instance = getPipeline().getCurrentInstance();
		if (instance == null)
			instance = "";
		else
			instance = instance + ": ";
		switch (status){
			case NotStarted:
				return "Not started";
			case Processing:
				return instance + "Processing";
			case Success:
				return instance + "Success - " + TimeFunctions.getTimeStr(getElapsedTime());
			case Failure:
				return instance + "Failure - " + TimeFunctions.getTimeStr(getElapsedTime());
		
			}
		return null;
	}
	
	public String getPipelineTreeLabel(){
		return getName();
	}
	
	protected void setStatus(Status s){
		status = s;
		//InterfacePipeline pipeline = getPipeline();
		// Call normally if pipeline is not started; otherwise call via
		// the pipeline to ensure it is called from the Event Dispatch Thread
		//if (pipeline == null || pipeline.getStatus().equals(Status.NotStarted)){
		//	fireStatusChanged();
		//	return;
		//	}
		//else
			//pipeline.taskStatusChanged(new PipelineTaskEvent(this));
		Runnable test = new Runnable(){
			public void run(){
				fireStatusChanged();
			}
		};
		
		//SwingUtilities.invokeAndWait(test);
		EventQueue.invokeLater(test);
	}
	
	/*******************************************
	 * Notifies this task's listeners that its status has changed.
	 * 
	 */
	public void fireStatusChanged(){
		updateListeners();
		PipelineTaskEvent e = new PipelineTaskEvent(this);
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).taskStatusChanged(e);
	}
	
	protected void updateListeners(){
		ArrayList<PipelineTaskListener> list = new ArrayList<PipelineTaskListener>(listeners);
		for (int i = 0; i < list.size(); i++)
			if (list.get(i) instanceof InterfaceTreeNode &&
					((InterfaceTreeNode)list.get(i)).isDestroyed())
				listeners.remove(list.get(i));
	}
	
	public void addListener(PipelineTaskListener l){
		listeners.add(l);
	}
	
	public void removeListener(PipelineTaskListener l){
		listeners.remove(l);
	}
	
	public void treeNodeDetached(TaskTreeNodeEvent e){
		listeners.remove(e.getSource());
	}
	
	/*********************************
	 * Message to display if this task terminated successfully
	 * 
	 * @return
	 */
	public abstract String getSuccessMessage();
	
	/*********************************
	 * Message to display if this task failed 
	 * 
	 * @return
	 */
	public abstract String getFailureMessage();
	
	/*********************************
	 * Launches this task
	 * 
	 * @return <code>true</code>, if this launch was successful
	 * @throws PipelineException If some exception was encountered during execution
	 */
	public abstract boolean launch() throws PipelineException;
	
	/*********************************
	 * Launches this task with the given project instance and root directory
	 * 
	 * @param instance 	The project instance for which to launch this task
	 * @param root 		The project root directory
	 * @return <code>true</code>, if this launch was successful
	 * @throws PipelineException If some exception was encountered during execution
	 */
	public abstract boolean launch(String instance, String root) throws PipelineException;
	
	/*********************************
	 * Launches this task
	 * 
	 * @param blocking Indicates whether this task should block or return immediately
	 * @return <code>true</code>, if this launch was successful
	 * @throws PipelineException If some exception was encountered during execution
	 */
	public abstract boolean launch(boolean blocking) throws PipelineException;
	
	/*********************************
	 * Launches this task with the given project instance and root directory
	 * 
	 * @param instance 	The project instance for which to launch this task
	 * @param root 		The project root directory
	 * @param blocking 	Indicates whether this task should block or return immediately
	 * @return <code>true</code>, if this launch was successful
	 * @throws PipelineException If some exception was encountered during execution
	 */
	public abstract boolean launch(String instance, String root, boolean blocking) throws PipelineException;
	
	/**************************************
	 * Launches this task with the given project instance and project
	 * 
	 * @param instance
	 * @param project
	 * @return
	 * @throws PipelineException
	 */
	public boolean launch(String instance, InterfaceProject project) throws PipelineException{
		return launch(instance, project, true);
	}
	public abstract boolean launch(String instance, InterfaceProject project, boolean blocking) throws PipelineException;
	
	//public abstract ImageIcon getIcon();
	
	@Override
	public String getDTD() {
		return null;
	}

	@Override
	public String getLocalName() {
		return null;
	}

	@Override
	public String getShortXML(int tab) {
		return null;
	}

	@Override
	public String getXML() {
		return getXML(0);
	}

	@Override
	public String getXML(int tab) {
		return null;
	}

	@Override
	public String getXMLSchema() {
		return null;
	}

	@Override
	public void handleXMLElementEnd(String localName) {
		
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		
	}

	@Override
	public void handleXMLString(String s) {
		
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
	
	public abstract DataFlavor getDataFlavor();

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (!isDataFlavorSupported(flavor)) 
			throw new UnsupportedFlavorException(flavor);
		return this;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		if (getDataFlavor() == null) return new DataFlavor[]{};
		return new DataFlavor[]{getDataFlavor()};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if (getDataFlavor() == null) return false;
		return flavor.equals(getDataFlavor());
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	@Override
	public InterfaceTreeNode issueTreeNode(){
		TaskTreeNode treeNode = new TaskTreeNode(this);
		setTreeNode(treeNode);
		tree_nodes.add(treeNode);
		return treeNode;
	}
	
	
	
}