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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.projects.InterfaceProject;

/******************************************
 * Represents a fork in a pipeline; i.e., a branching point, where the output of the previous process becomes the
 * input of two or more independent pipelines. Tasks can be launched in parallel or in series, as determined by the
 * <code>setLaunchParallel</code> method.
 * 
 * <p>The output of the fork is specified by the <code>setOutputPipeline</code> method.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PipelineFork extends PipelineTask {

	protected boolean launch_parallel = false;
	protected LinkedList<InterfacePipeline> pipelines;
	protected InterfacePipeline pipeline, output_pipeline;
	
	protected static DataFlavor data_flavor;
	
	public PipelineFork(String name){
		this("Unnamed", false, new LinkedList<InterfacePipeline>());
	}
	
	public PipelineFork(String name, boolean launch_parallel){
		this (name, false, new LinkedList<InterfacePipeline>());
	}
	
	public PipelineFork(String name, boolean launch_parallel, Collection<InterfacePipeline> tasks){
		this(name, launch_parallel, tasks, null);
	}
	
	public PipelineFork(String name, boolean launch_parallel, Collection<InterfacePipeline> tasks, InterfacePipeline output_pipe){
		setName(name);
		this.launch_parallel = launch_parallel;
		this.pipelines = new LinkedList<InterfacePipeline>(tasks);
		setOutputPipeline(output_pipe);
	}
	
	public void setOutputPipeline(InterfacePipeline output_pipe){
		output_pipeline = output_pipe;
	}
	
	public void push(InterfacePipeline pipe){
		pipelines.addFirst(pipe);
	}
	
	public void insert(int index, InterfacePipeline pipe){
		pipelines.add(index, pipe);
	}
	
	public void remove(InterfacePipeline pipe){
		pipelines.remove(pipe);
	}
	
	public int getPipelineIndex(InterfacePipeline pipe){
		for (int i = 0; i < pipelines.size(); i++)
			if (pipelines.get(i) == pipe) return i;
		return -1;
	}
	
	public LinkedList<InterfacePipeline> getTasks(){
		return pipelines;
	}
	
	public Icon getObjectIcon(){
		URL imgURL = InterfacePipeline.class.getResource("/mgui/resources/icons/pipelines/pipeline_ns_20.png");
		
		if (imgURL == null){
			InterfaceSession.log("PipelineFork: Cannot find icon at /mgui/resources/icons/pipelines/pipeline_ns_20.png");
			return null;
		}
		
		return new ImageIcon(imgURL);
		
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

	@Override
	public String getSuccessMessage(){
		return "Fork succeeded...";
	}
	
	@Override
	public String getFailureMessage(){
		
		return "Fork failed...";
	}
	
	@Override
	public String toString(){
		return getName();
	}
	
	public ImageIcon getIcon(){
		return null;
	}
	
	@Override
	public void setPipeline(InterfacePipeline pipeline){
		this.pipeline = pipeline;
	}
	
	@Override
	public InterfacePipeline getPipeline(){
		return this.pipeline;
	}

	public ArrayList<InterfacePipeline> getPipelines(){
		return new ArrayList<InterfacePipeline>(pipelines);
	}
	
	@Override
	public boolean launch() throws PipelineException{
		return launch(false);
	}
	
	@Override
	public boolean launch(boolean blocking) throws PipelineException{
		
		if (status != Status.NotStarted){
			throw new PipelineException("Fork '" + getName() + "' could not launch because it is" +
									  " already started or not reset..");
			}
		
		String mode = "parallel";
		if (!launch_parallel) mode = "series";
		
		InterfaceSession.log("Starting fork '" + getName() + "' in " + mode + ".");
		
		boolean success = true;
		
		//Launch pipelines in parallel or in series, depending on the flag
		for (int i = 0; i < pipelines.size(); i++){
			try{
				if (this.launch_parallel){
					success &= PipelineFunctions.launchPipelineAsJob(pipeline, blocking);
				}else{
					success &= pipelines.get(i).launch(blocking);
					}
			}catch (PipelineException ex){
				ex.printStackTrace();
				if (PipelineFunctions.fail_on_exception)
					throw ex;
				}
			}
		
		//now set output to the temporary output of the output pipeline, if one is set
		if (output_pipeline != null){
			pipeline.getState().temp_input = output_pipeline.getState().temp_output;
			}
		
		return success;
	}

	@Override
	public boolean launch(String instance, String root) throws PipelineException {
		return launch(instance, root, false);
	}
	
	@Override
	public boolean launch(String instance, String root, boolean blocking) throws PipelineException {
		
		for (int i = 0; i < pipelines.size(); i++){
			pipelines.get(i).instance = instance;
			pipelines.get(i).setRootDirectory(new File(root));
			}
		
		return launch(blocking);
	}
	
	@Override
	public boolean launch(String instance, InterfaceProject project, boolean blocking) throws PipelineException{
		
		if (project == null) return launch(instance, "", blocking);
		
		for (int i = 0; i < pipelines.size(); i++){
			pipelines.get(i).assigned_project = project;
			pipelines.get(i).instance = instance;
			pipelines.get(i).setRootDirectory(project.getRootDir());
			}
		
		return launch(blocking);
		
	}

}