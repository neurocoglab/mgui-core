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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.projects.InterfaceProject;
import mgui.pipelines.PipelineLauncher.TaskEvent;
import mgui.pipelines.PipelineLauncher.TaskEvent.EventType;


/********************************************************
 * A launcher for an {@link InterfacePipeline} object. Creates and runs its own thread and then
 * calls {@code InterfacePipeline.launch}, which blocks until the pipeline has finished.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PipelineLauncher extends SwingWorker<Boolean, TaskEvent> implements DynamicPipelineListener{

	protected ArrayList<DynamicPipelineListener> dynamic_pipeline_listeners = new ArrayList<DynamicPipelineListener>();

	public String instance;
	public InterfaceProject project;
	public InterfacePipeline pipeline;
	
	public PipelineLauncher(InterfacePipeline pipeline){
		this.pipeline = pipeline;
		pipeline.addDynamicListener(this);
	}
	
	public PipelineLauncher(InterfacePipeline pipeline, String instance, InterfaceProject project){
		this.pipeline = pipeline;
		this.instance = instance;
		this.project = project;
		pipeline.addDynamicListener(this);
	}
	
	public void addDynamicListener(DynamicPipelineListener listener){
		dynamic_pipeline_listeners.add(listener);
	}
	
	public void removeDynamicListener(DynamicPipelineListener listener){
		dynamic_pipeline_listeners.remove(listener);
	}
	
	@Override
	protected Boolean doInBackground() throws Exception {
		if (pipeline == null)
			throw new PipelineException("PipelineLauncher: No pipeline set!");
		
		if (instance == null || project == null){
			return pipeline.launch(true);
		}else{
			return pipeline.launch(instance, project);
			}
		
	}

	@Override
	protected void done() {
		
		 boolean success = false;
    	 try{
    		 success = get();
    		 if (!success){
    			pipeline.getState().setTaskFailed(true);
	    	 	}
    	 }catch (Exception ex){
    		Throwable cause = ex.getCause();
    		if (cause == null){
    			cause = ex;
    			}
    		InterfaceSession.log("Pipeline '" + pipeline.getName() + "' threw exception (" +
    							cause.getClass().getName() +  "): " +
			    				cause.getMessage(), 
			    				LoggingType.Errors);
    		pipeline.getState().setTaskFailed(true); 
    		pipeline.firePipelineTerminated();
    	 	}
    	 
		pipeline.removeDynamicListener(this);
	}
	
	@Override
	protected void process(final List<TaskEvent> events) {
		
		// This is called on the EDT
		for (int i = 0; i < events.size(); i++){
			TaskEvent event = events.get(i);
			switch (event.type){
				case pipelineLaunched:
					firePipelineLaunched(event);
					break;
				case pipelineTerminated:
					firePipelineTerminated(event);
					break;
				case pipelineTaskTerminated:
					firePipelineTaskTerminated(event);
					break;
				case pipelineTaskUpdated:
					firePipelineTaskUpdated(event);
					break;
				case pipelineTaskLaunched:
					firePipelineTaskLaunched(event);
					break;
				}
			
			}
		
		
		// Relay pipeline events to the Event dispatch thread
		/*
		Runnable test = new Runnable(){
		
			public void run(){
				for (int i = 0; i < events.size(); i++){
					TaskEvent event = events.get(i);
					switch (event.type){
						case pipelineLaunched:
							firePipelineLaunched(event);
							break;
						case pipelineTerminated:
							firePipelineTerminated(event);
							break;
						case pipelineTaskTerminated:
							firePipelineTaskTerminated(event);
							break;
						case pipelineTaskUpdated:
							firePipelineTaskUpdated(event);
							break;
						case pipelineTaskLaunched:
							firePipelineTaskLaunched(event);
							break;
						}
					
					}
			}
		};
		
		try{
			if (SwingUtilities.isEventDispatchThread())
				test.run();
			else
				SwingUtilities.invokeAndWait(test);
		}catch (InvocationTargetException ex){
			InterfaceSession.log("Pipeline '" + pipeline.getName() + "' threw exception (" +
					ex.getClass().getName() +  "): " +
					ex.getMessage(), 
    				LoggingType.Errors);
			pipeline.getState().setTaskFailed(true); 
			pipeline.firePipelineTerminated();
		}catch (InterruptedException ex){
			InterfaceSession.log("Pipeline '" + pipeline.getName() + "' threw exception (" +
					ex.getClass().getName() +  "): " +
					ex.getMessage(), 
    				LoggingType.Errors);
			pipeline.getState().setTaskFailed(true); 
			pipeline.firePipelineTerminated();
			}
			*/
	}
		
		
		protected void firePipelineTaskLaunched(TaskEvent event){
			if (dynamic_pipeline_listeners == null) return;
			ArrayList<DynamicPipelineListener> list = new ArrayList<DynamicPipelineListener>(dynamic_pipeline_listeners);
			
			for (int i = 0; i < list.size(); i++)
				list.get(i).pipelineTaskLaunched(event.event, event.task);
		}
		
		protected void firePipelineTaskTerminated(TaskEvent event){
			if (dynamic_pipeline_listeners == null) return;
			ArrayList<DynamicPipelineListener> list = new ArrayList<DynamicPipelineListener>(dynamic_pipeline_listeners);
			
			for (int i = 0; i < list.size(); i++)
				list.get(i).pipelineTaskTerminated(event.event, event.task);
		}
		
		protected void firePipelineTaskUpdated(TaskEvent event){
			if (dynamic_pipeline_listeners == null) return;
			ArrayList<DynamicPipelineListener> list = new ArrayList<DynamicPipelineListener>(dynamic_pipeline_listeners);
			
			for (int i = 0; i < list.size(); i++)
				list.get(i).pipelineTaskUpdated(event.event, event.task);
		}
		
		protected void firePipelineLaunched(TaskEvent event){
			if (dynamic_pipeline_listeners == null) return;
			ArrayList<DynamicPipelineListener> list = new ArrayList<DynamicPipelineListener>(dynamic_pipeline_listeners);
			
			for (int i = 0; i < list.size(); i++)
				list.get(i).pipelineLaunched(event.event);
		}
		
		protected void firePipelineTerminated(TaskEvent event){
			if (dynamic_pipeline_listeners == null) return;
			ArrayList<DynamicPipelineListener> list = new ArrayList<DynamicPipelineListener>(dynamic_pipeline_listeners);
			
			for (int i = 0; i < list.size(); i++)
				list.get(i).pipelineTerminated(event.event);
		}
		
	static class TaskEvent {
		
		public DynamicPipelineEvent event;
		public PipelineTask task;
		public EventType type;
		
		public static enum EventType{
			pipelineLaunched,
			pipelineTerminated,
			pipelineTaskTerminated,
			pipelineTaskUpdated,
			pipelineTaskLaunched;
		}
		
		public TaskEvent(DynamicPipelineEvent event, PipelineTask task, EventType type){
			this.event = event;
			this.task = task;
			this.type = type;
		}
		
	}
	
	@Override
	public void pipelineLaunched(DynamicPipelineEvent event) {
		publish(new TaskEvent(event, null, EventType.pipelineLaunched));
	}

	@Override
	public void pipelineTerminated(DynamicPipelineEvent event) {
		publish(new TaskEvent(event, null, EventType.pipelineTerminated));
	}

	@Override
	public void pipelineTaskTerminated(DynamicPipelineEvent event, PipelineTask task) {
		publish(new TaskEvent(event, task, EventType.pipelineTaskTerminated));
	}

	@Override
	public void pipelineTaskUpdated(DynamicPipelineEvent event, PipelineTask task) {
		// Here we can publish the update to a task status
		publish(new TaskEvent(event, task, EventType.pipelineTaskUpdated));
		
	}

	@Override
	public void pipelineTaskLaunched(DynamicPipelineEvent event, PipelineTask task) {
		publish(new TaskEvent(event, task, EventType.pipelineTaskLaunched));
	}

	
}