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

import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.pipelines.trees.TaskTreeNode;
import mgui.interfaces.projects.InterfaceProject;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.util.TimeFunctions;

/*********************************************************
 * Represents a series of <code>Task</code>s that are to be run sequentially, such that the output of a
 * task becomes the input of the subsequent one.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfacePipeline extends PipelineTask implements PipelineTaskListener {
	
	protected PipelineState state;
	protected InterfacePipeline root_pipeline;
	protected LinkedList<PipelineTask> tasks = new LinkedList<PipelineTask>();
	//protected File root_directory;
	protected InterfaceProject assigned_project;
	PipelineTask failed_process;
	protected String instance;
	protected static DataFlavor data_flavor;
	protected boolean terminate_on_failure = false;
	
	ArrayList<StaticPipelineListener> static_listeners = new ArrayList<StaticPipelineListener>();
	ArrayList<DynamicPipelineListener> dynamic_listeners = new ArrayList<DynamicPipelineListener>();
	
	public InterfacePipeline (){
		this("Unnamed", new LinkedList<PipelineTask>());
	}
	
	public InterfacePipeline (String name){
		this(name, new LinkedList<PipelineTask>());
	}
	
	public InterfacePipeline (String name, LinkedList<PipelineTask> processes){
		super(name);
		this.tasks = processes;
		this.state = new PipelineState(this);
	}
	
	public String getCurrentInstance(){
		return instance;
	}
	
	public void setProject(InterfaceProject project){
		this.assigned_project = project;
	}
	
	public File getRootDir(){
		//if (assigned_project != null) return assigned_project.getRootDir();
		return new File(getState().root_dir);
	}
	
	public void addStaticListener(StaticPipelineListener listener){
		if (static_listeners == null) return;
		static_listeners.add(listener);
	}
	
	public void removeStaticListener(StaticPipelineListener listener){
		if (static_listeners == null) return;
		static_listeners.remove(listener);
	}
	
	protected void fireStaticListeners(StaticPipelineEvent event){
		if (static_listeners == null) return;
		ArrayList<StaticPipelineListener> list = new ArrayList<StaticPipelineListener>(static_listeners);
		
		for (int i = 0; i < list.size(); i++)
			list.get(i).pipelineUpdated(event);
	}
	
	public void addDynamicListener(DynamicPipelineListener listener){
		if (dynamic_listeners == null) return;
		dynamic_listeners.add(listener);
	}
	
	public void removeDynamicListener(DynamicPipelineListener listener){
		if (dynamic_listeners == null) return;
		dynamic_listeners.remove(listener);
	}
	
	protected void firePipelineTaskLaunched(PipelineTask task){
		if (dynamic_listeners == null) return;
		ArrayList<DynamicPipelineListener> list = new ArrayList<DynamicPipelineListener>(dynamic_listeners);
		DynamicPipelineEvent event = new DynamicPipelineEvent(this);
		
		for (int i = 0; i < list.size(); i++)
			list.get(i).pipelineTaskLaunched(event, task);
	}
	
	protected void firePipelineTaskTerminated(PipelineTask task){
		if (dynamic_listeners == null) return;
		ArrayList<DynamicPipelineListener> list = new ArrayList<DynamicPipelineListener>(dynamic_listeners);
		DynamicPipelineEvent event = new DynamicPipelineEvent(this);
		
		for (int i = 0; i < list.size(); i++)
			list.get(i).pipelineTaskTerminated(event, task);
	}
	
	protected void firePipelineTaskUpdated(PipelineTask task){
		if (dynamic_listeners == null) return;
		ArrayList<DynamicPipelineListener> list = new ArrayList<DynamicPipelineListener>(dynamic_listeners);
		DynamicPipelineEvent event = new DynamicPipelineEvent(this);
		
		for (int i = 0; i < list.size(); i++)
			list.get(i).pipelineTaskUpdated(event, task);
	}
	
	protected void firePipelineLaunched(){
		if (dynamic_listeners == null) return;
		ArrayList<DynamicPipelineListener> list = new ArrayList<DynamicPipelineListener>(dynamic_listeners);
		DynamicPipelineEvent event = new DynamicPipelineEvent(this);
		
		for (int i = 0; i < list.size(); i++)
			list.get(i).pipelineLaunched(event);
	}
	
	protected void firePipelineTerminated(){
		if (dynamic_listeners == null) return;
		ArrayList<DynamicPipelineListener> list = new ArrayList<DynamicPipelineListener>(dynamic_listeners);
		DynamicPipelineEvent event = new DynamicPipelineEvent(this);
		
		for (int i = 0; i < list.size(); i++)
			list.get(i).pipelineTerminated(event);
	}
	
	@Override
	public void setPipeline(InterfacePipeline pipeline){
		root_pipeline = pipeline;
	}
	
	@Override
	public InterfacePipeline getPipeline(){
		if (root_pipeline == null) return this;
		return root_pipeline;
	}
	
	/**************************************
	 * Returns the state of this pipeline, or the root pipeline if this is not the root.
	 * 
	 * @return
	 */
	public PipelineState getState(){
		InterfacePipeline pipeline = getPipeline();
		if (pipeline == null || this.equals(pipeline)) return state;
		return pipeline.getState();
	}
	
	@Override
	public DataFlavor getDataFlavor(){
		return _getDataFlavor();
	}
	
	static private DataFlavor _getDataFlavor(){
		if (data_flavor == null)
			data_flavor = new DataFlavor(InterfacePipeline.class, "Pipeline");
		return data_flavor;
	}
	
	public boolean setRootDirectory(File d){
		if (d == null || !d.exists() || !d.isDirectory()) return false;
		getState().root_dir = d.getAbsolutePath();
		return true;
	}
	
	public void setSubject(String subject){
		this.instance = subject;
	}
	
	public void append(PipelineTask process){
		tasks.addLast(process);
		setTaskInstance(process);
		fireStaticListeners(new StaticPipelineEvent(this, 
													tasks.size() - 1,
													StaticPipelineEvent.EventType.TaskAppended));
		//updateTreeNodes();
	}
	
	public void fireTaskModified(PipelineTask task){
		fireStaticListeners(new StaticPipelineEvent(task, StaticPipelineEvent.EventType.TaskModified));
	}
	
	public void resetTaskInstances(){
		ArrayList<PipelineProcessInstance> processed = new ArrayList<PipelineProcessInstance>();
		for (int i = 0; i < tasks.size(); i++){
			if (tasks.get(i) instanceof PipelineProcessInstance){
				PipelineProcessInstance process_instance = (PipelineProcessInstance)tasks.get(i);
				process_instance.instance = 1;
				for (int j = 0; j < processed.size(); j++){ 
					if (processed.get(j).getProcess().equals(process_instance.getProcess()))
						process_instance.instance++;
					}
				processed.add(process_instance);
				}
			}
		this.fireStaticListeners(new StaticPipelineEvent(this, StaticPipelineEvent.EventType.PipelineModified));
	}
	
	public void setTaskInstance(PipelineTask task){
		int instance = 1;
		
		if (task instanceof PipelineProcessInstance){
			//get correct instance
			PipelineProcessInstance process = (PipelineProcessInstance)task;
			for (int i = 0; i < tasks.size(); i++){
				if (tasks.get(i) instanceof PipelineProcessInstance){
					PipelineProcessInstance process_instance = (PipelineProcessInstance)tasks.get(i);
					if (task.getName().equals(process.getName()))
						process_instance.instance = instance++;
					}
				}
			}
		
		task.setPipeline(getPipeline());
	}
	
	public void push(PipelineTask process){
		tasks.addFirst(process);
		setTaskInstance(process);
		fireStaticListeners(new StaticPipelineEvent(this,
												0,
												StaticPipelineEvent.EventType.TaskInserted));
	}
	
	public void insert(int index, PipelineTask process){
		tasks.add(index, process);
		setTaskInstance(process);
		fireStaticListeners(new StaticPipelineEvent(this, 
												index,
												StaticPipelineEvent.EventType.TaskInserted));
	}
	
	public void remove(PipelineTask process){
		tasks.remove(process);
		process.setPipeline(null);
		process.destroy();
		fireStaticListeners(new StaticPipelineEvent(process,
												StaticPipelineEvent.EventType.TaskRemoved));
		updateTreeNodes();
		resetTaskInstances();
	}
	
	/*************************
	 * Insert <code>existing_task</code> before <code>new_task</code>.
	 * 
	 * @param existing_task
	 * @param new_task
	 */
	public void insertBefore(PipelineTask existing_task, PipelineTask new_task){
		
		int index = getTaskIndex(existing_task);
		if (index < 0) return;
		insert(index, new_task);
		
	}
	
	public PipelineTask getTaskAtIndex(int index){
		if (index < 0 || index >= tasks.size()) return null;	// Should probably throw exception
		return tasks.get(index);
	}
	
	public int getTaskIndex(PipelineTask task){
		for (int i = 0; i < tasks.size(); i++)
			if (tasks.get(i) == task) return i;
		return -1;
	}
	
	public LinkedList<PipelineTask> getTasks(){
		return tasks;
	}
	
	
	@Override
	public void reset(){
		super.reset();
		failed_process = null;
		
		
		//reset all processes
		for (int i = 0; i < tasks.size(); i++)
			tasks.get(i).reset();
	}
	
	@Override
	public boolean interrupt() throws PipelineException {
		super.interrupt();
		boolean success = true;
		
		for (int i = 0; i < tasks.size(); i++)
			success &= tasks.get(i).interrupt();
		
		return success;
	}
	
	@Override
	public boolean launch(String instance, String root_dir, boolean blocking) throws PipelineException{
		this.assigned_project = null;
		this.instance = instance;
		this.setRootDirectory(new File(root_dir));
		return launch(blocking);
	}
	
	@Override
	public boolean launch(String instance, InterfaceProject project, boolean blocking) throws PipelineException{
		this.assigned_project = project;
		this.instance =  instance;
		return launch(blocking);
	}
	
	@Override
	public boolean launch(String instance, String root) throws PipelineException{
		return launch(instance, root, false);
	}
	
	@Override
	public boolean launch() throws PipelineException{
		return launch(false);
	}
	
	@Override
	public boolean launch(boolean blocking) throws PipelineException{
		
		if (status != Status.NotStarted){
			throw new PipelineException("Pipeline '" + getName() + "' could not launch because it is" +
									  " already started or not reset..");
			}
		
		File root_directory = getRootDir();
		if (root_directory == null){
			throw new PipelineException("Pipeline '" + getName() + "' could not launch because there is" +
									  " no root directory specified..");
			}
		
		String suffix = "'";
		if (instance != null) suffix = "' for instance '" + instance + "'.";
		InterfaceSession.log("Starting pipeline '" + getName() + suffix);	 
		start = System.currentTimeMillis();
		current = System.currentTimeMillis();
		setStatus(Status.Processing);
		
		firePipelineLaunched();
		boolean failed = false, interrupted = false;
		
		for (int i = 0; i < tasks.size() && (!terminate_on_failure || !failed) && !interrupted; i++){
			
			failed = false; interrupted = false;
			
			if (instance == null && assigned_project == null){
				if (!tasks.get(i).launch(blocking)){
					state.reset();	//sets input & output files to null
									//(process will set them if necessary)
					current = System.currentTimeMillis();
					setStatus(Status.Failure);
					failed_process = tasks.get(i);
					state.deleteTempFiles();
					return false;
				}else{
					firePipelineTaskLaunched(tasks.get(i));
					}
			}else{
				if (!tasks.get(i).launch(instance, assigned_project, blocking)){
					state.reset();	//sets input & output files to null
									//(process will set them if necessary)
					current = System.currentTimeMillis();
					setStatus(Status.Failure);
					failed_process = tasks.get(i);
					state.deleteTempFiles();
					return false;
				}else{
					//tasks.get(i).setStatus(Status.Processing);
					firePipelineTaskLaunched(tasks.get(i));
					}
				}
			
			//Wait on task processing
			while (tasks.get(i).getStatus().equals(Status.Processing) && !failed && !interrupted){
			
				// Sleep a bit between checks
				try{
					Thread.sleep(100);
				}catch (InterruptedException ex){
					// hmm...
					tasks.get(i).setStatus(Status.Interrupted);
					}
				
				// Check for interruption
				if (tasks.get(i).getStatus().equals(Status.Interrupted) ||
						getStatus().equals(Status.Interrupted)){
					current = System.currentTimeMillis();
					InterfaceSession.log("Pipeline '" + getName() + "' interrupted at task '" +
							tasks.get(i).getName() + "'.\nTime elapsed: " + (current - start),
							LoggingType.Warnings);
					boolean interrupt_success = true;
					if (!getStatus().equals(Status.Interrupted))
						interrupt_success = interrupt();
					interrupted = true;
					if (!interrupt_success){
						InterfaceSession.log("Not all tasks could be interrupted...", LoggingType.Errors);
						}
					}
				
				}
			
			firePipelineTaskTerminated(tasks.get(i));
			
			if (!tasks.get(i).getStatus().equals(Status.Success)){
				setStatus(Status.Failure);
				failed = true;
				}
			
			}
		
		state.deleteTempFiles();
		current = System.currentTimeMillis();
		if (!failed && ! interrupted){
			setStatus(Status.Success);
			}
		
		this.firePipelineTerminated();
		return getStatus().equals(Status.Success);
	}
	
	@Override
	public void taskStatusChanged(PipelineTaskEvent e) {
		// A task on this pipeline changed; inform listeners
		this.firePipelineTaskUpdated((PipelineTask)e.getSource());
		
	}
	
	@Override
	public String getSuccessMessage(){
		return "Pipeline '" + getName() + "' finished successfully; elapsed time: " + TimeFunctions.getTimeStr(getElapsedTime());
	}
	
	@Override
	public String getFailureMessage(){
		if (this.getStatus().equals(Status.Interrupted)){
			return "Pipeline was interrupted.";
			}
		if (failed_process == null)
			return "Unknown failure.";
		return "Pipeline '" + getName() + "' failed at process '" + failed_process.toString() + "':\n" +
				failed_process.getFailureMessage() + "; total elapsed time " + TimeFunctions.getTimeStr(getElapsedTime());
	}
	
	@Override
	public String toString(){
		return getName();
	}
	
	public Icon getObjectIcon(){
		
		URL imgURL = null;
		
		switch (status){
			case NotStarted:
				imgURL = InterfacePipeline.class.getResource("/mgui/resources/icons/pipelines/pipeline_20.png");
				break;
			case Processing:
				imgURL = InterfacePipeline.class.getResource("/mgui/resources/icons/pipelines/pipeline_started_20.png");
				break;
			case Success:
				imgURL = InterfacePipeline.class.getResource("/mgui/resources/icons/pipelines/pipeline_success_20.png");
				break;
			case Failure:
			case Interrupted:
				imgURL = InterfacePipeline.class.getResource("/mgui/resources/icons/pipelines/pipeline_failed_20.png");
				break;
			}
		
		if (imgURL == null){
			InterfaceSession.log("Can't find icon resources for Pipeline");
			return null;
		}
		return new ImageIcon(imgURL);
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode node){
		
		super.setTreeNode(node);
		TaskTreeNode _node = (TaskTreeNode)node;
		for (int i = 0; i < tasks.size(); i++){
			TaskTreeNode task_node = (TaskTreeNode)tasks.get(i).issueTreeNode();
			_node.addTaskChild(task_node);
			}
		
	}
	
	@Override
	public String getXML(int tab) {
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		String xml = _tab + "<Pipeline name = '" + getName() + "'>\n";
		
		xml = xml + _tab2 + "<Tasks>\n";
		
		for (int i = 0; i < tasks.size(); i++)
			xml = xml + tasks.get(i).getXML(tab + 2);
		
		xml = xml + _tab2 + "</Tasks>\n";
		xml = xml + _tab + "</Pipeline>\n";
		
		return xml;
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		writer.write("\n" + _tab + "<Pipeline name = '" + getName() + "'>\n");
		
		writer.write(_tab2 + "<Tasks>\n");
		
		for (int i = 0; i < tasks.size(); i++)
			writer.write(tasks.get(i).getXML(tab + 2));
		
		writer.write(_tab2 + "</Tasks>\n");
		writer.write(_tab + "</Pipeline>");
		
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException{
		//TODO implement
		throw new CloneNotSupportedException();
	}

	
	
}