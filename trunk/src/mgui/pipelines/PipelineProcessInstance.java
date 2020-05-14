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
import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.projects.InterfaceProject;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.util.IoFunctions;
import mgui.util.TimeFunctions;

/*********************************************
 * Represents a specific instance of a Java process.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PipelineProcessInstance extends PipelineTask {

	protected InterfacePipeline pipeline;
	protected PipelineProcess process;
	protected long uid = InterfaceSession.getUID();
	protected int instance;
	protected boolean has_input = true;
	protected String output_file, input_file;
	protected boolean prepend_instance_input = false, prepend_instance_output = false;
	protected String logger;
	protected HashMap<String, TaskParameterInstance> parameters = new HashMap<String, TaskParameterInstance>();
	protected static DataFlavor data_flavor;
	
	public PipelineProcessInstance(){
		initLogger();
	}
	
	public PipelineProcessInstance(PipelineProcess process, int instance){
		
		this.process = process;
		this.instance = instance;
		
		initParameters();
		initLogger();
	}
	
	public PipelineProcessInstance(PipelineProcessInstance process_instance){
		this.setFromProcessInstance(process_instance);
	}
	
	public void setFromProcessInstance(PipelineProcessInstance process_instance){
		this.process = process_instance.process;
		this.instance = process_instance.instance;
		this.output_file = process_instance.output_file;
		this.input_file = process_instance.input_file;
		this.has_input = process_instance.has_input;
		this.prepend_instance_input = process_instance.prepend_instance_input;
		this.prepend_instance_output = process_instance.prepend_instance_output;
		
		initParameters();
		ArrayList<TaskParameterInstance> params = new ArrayList<TaskParameterInstance>(process_instance.parameters.values());
		for (int i = 0; i < params.size(); i++)
			this.parameters.get(params.get(i).name).setFromParamaterInstance(params.get(i));
		
		this.updateTreeNodes();
	}

	@Override
	public void setPipeline(InterfacePipeline pipeline){
		this.pipeline = pipeline;
	}
	
	@Override
	public InterfacePipeline getPipeline(){
		return this.pipeline;
	}
	
	@Override
	public DataFlavor getDataFlavor(){
		return _getDataFlavor();
	}
	
	private static DataFlavor _getDataFlavor(){
		if (data_flavor == null)
			data_flavor = new DataFlavor(PipelineProcessInstance.class, "ProcessInstance");
		return data_flavor;
	}
	
	void initLogger(){
		if (logger == null || logger.length() == 0) return;
		Logger log = Logger.getLogger(logger);
		log.addHandler(new ConsoleHandler());
	}
	
	protected void initParameters(){
		if (process == null) return;
		
		for (int i = 0; i < process.parameters.size(); i++){
			TaskParameterInstance p = process.parameters.get(i).getInstance();
			parameters.put(p.name, p);
			}
		
	}
	
	/************************************
	 * Returns the parameter instance for the specified name. 
	 * 
	 * @param name
	 * @return
	 */
	public TaskParameterInstance getParameter(String name){
		return parameters.get(name);
	}
	
	/************************************
	 * Returns the process of which this is an instance.
	 * 
	 * @return
	 */
	public PipelineProcess getProcess(){
		return process;
	}
	
	@Override
	public String toString(){
		return process.name + "." + instance;
	}
	
	@Override
	public String getName(){
		return process.name;
	}
	
	@Override
	public boolean launch() throws PipelineException{
		return launch(false);
	}
	
	@Override
	public boolean launch(boolean blocking) throws PipelineException{
		//launch me
		
		if (this.pipeline == null){
			throw new PipelineException("Process '" + toString() + "' could not launch because it is" +
				" not part of a pipeline..");
		}
		
		if (status != Status.NotStarted){
			throw new PipelineException("Process '" + toString() + "' could not launch because it is" +
			  							" already started or not reset..");
			}
		
		//set input/output files from subject name and prepend subject to file names
		HashMap<String,ArrayList<String>> variables = null;
		ArrayList<String> files_in = null;
		if (input_file != null){
			String subdir_in = "";
			String file_in = input_file;
			if (input_file.lastIndexOf(File.separator) > 0){
				subdir_in = input_file.substring(0, input_file.lastIndexOf(File.separator) + 1);
				file_in = input_file.substring(input_file.lastIndexOf(File.separator) + 1);
				}
			
			// Find variables, if any; compile list and replace with "*"
			// Filter for wildcards, compile list of variables and values
			if (input_file.contains("*") || input_file.contains("%")){
				variables = new HashMap<String,ArrayList<String>>();
				files_in = PipelineFunctions.getWildcardFiles(null, input_file, variables);
			}else{
				files_in = new ArrayList<String>();
				files_in.add(input_file);
				}
			
			}
		
		ArrayList<String> files_out = null;
		if (output_file != null){
			files_out = new ArrayList<String>();
//			String file_out = output_file;
//			if (output_file.lastIndexOf(File.separator) > 0){
//				//subdir_out = output_file.substring(0, output_file.lastIndexOf(File.separator) + 1);
//				file_out = output_file.substring(output_file.lastIndexOf(File.separator) + 1);
//				}
			
			// Replace variable values if necessary
			for (int i = 0; i < files_in.size(); i++){
				if (output_file.contains("%") && variables != null){
					files_out.add(PipelineFunctions.setParameterVariables(output_file, variables, i));
				}else{
					files_out.add(output_file);
					}
				}
			
			}
		
		start = System.currentTimeMillis();
		setStatus(Status.Processing);
		PipelineState state = pipeline.getState();
		
		// Loop through all files matching wildcards; if no wildcards are present, this will always be a
		// single iteration.
		for (int i = 0; i < files_in.size(); i++){
			state.output_file = files_out.get(i);
			state.input_file = files_in.get(i);
			
			if (!PipelineFunctions.launchPipelineProcess(this, blocking, state)){
				current = System.currentTimeMillis();
				setStatus(Status.Failure);
				return false;
				}
			}
		
		return true;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException{
		return new PipelineProcessInstance(this);
	}
	
	@Override
	public boolean launch(String instance, String root) throws PipelineException{
		return launch(instance, root, false);
	}
	
	@Override
	public boolean launch(String instance, String root, boolean blocking) throws PipelineException{
		getPipeline().getState().root_dir = root;
		return launch(instance, (InterfaceProject)null, blocking);
	}
	
	@Override
	public boolean launch(String instance, InterfaceProject project, boolean blocking) throws PipelineException{
		//launch me
		
		if (status != Status.NotStarted){
			throw new PipelineException("Process '" + toString() + "' could not launch because it is" +
			  							" already started or not reset..");
			}
		
		start = System.currentTimeMillis();
		setStatus(Status.Processing);
		
		try{
			PipelineState state = pipeline.getState();
			
			File root_dir = new File(state.root_dir);
			if (project != null){
				root_dir = project.getRootDir();
				}
			
			//set input/output files from subject name and prepend subject to file names
			HashMap<String,ArrayList<String>> variables = null;
			ArrayList<String> files_in = null;
			if (input_file != null){
				String subdir_in = "";
				String file_in = input_file;
				if (input_file.lastIndexOf(File.separator) > 0){
					subdir_in = input_file.substring(0, input_file.lastIndexOf(File.separator) + 1);
					file_in = input_file.substring(input_file.lastIndexOf(File.separator) + 1);
					}
				String instance_in = "";
				if (prepend_instance_input && instance != null) 
					instance_in = instance;
				file_in = subdir_in + instance_in + file_in;
				if (project != null && instance != null)
					file_in = PipelineFunctions.parseArg(file_in, instance, project).get(0);
				else
					file_in = PipelineFunctions.parseArg(file_in, state.root_dir, project).get(0);
				
				// Find variables, if any; compile list and replace with "*"
				// Filter for wildcards, compile list of variables and values
				if (file_in.contains("*") || file_in.contains("%")){
					variables = new HashMap<String,ArrayList<String>>();
					files_in = PipelineFunctions.getWildcardFiles(null, file_in, variables);
				}else{
					files_in = new ArrayList<String>();
					files_in.add(file_in);
					}
				
				}
			
			String subdir_out = "";
			ArrayList<String> files_out = null;
			if (output_file != null){
				files_out = new ArrayList<String>();
				String file_out = output_file;
				if (output_file.lastIndexOf(File.separator) > 0){
					subdir_out = output_file.substring(0, output_file.lastIndexOf(File.separator) + 1);
					file_out = output_file.substring(output_file.lastIndexOf(File.separator) + 1);
					}
				String instance_out = "";
				if (prepend_instance_output && instance != null) 
					instance_out = instance;
				file_out = subdir_out + instance_out + file_out;
				
				// Replace instance-specific tags
				if (project != null && instance != null)
					file_out = PipelineFunctions.parseArg(file_out, instance, project).get(0);
				else
					file_out = PipelineFunctions.parseArg(file_out, state.root_dir, project).get(0);
				
				// Replace variable values if necessary
				for (int i = 0; i < files_in.size(); i++){
					if (file_out.contains("%") && variables != null){
						files_out.add(PipelineFunctions.setParameterVariables(file_out, variables, i));
					}else{
						files_out.add(file_out);
						}
					
					}
				
				}
			
			// Loop through all files matching wildcards; if no wildcards are present, this will always be a
			// single iteration.
			for (int i = 0; i < files_in.size(); i++){
				state.output_file = files_out.get(i);
				state.input_file = files_in.get(i);
				
				if (!PipelineFunctions.launchPipelineProcess(this, instance, project, logger, blocking, state)){
					current = System.currentTimeMillis();
					setStatus(Status.Failure);
					return false;
					}
				}
		
		}catch (Exception e){
			//e.printStackTrace();
			InterfaceSession.log("PipelineProcessInstance: Exception executing process.\n" +
								 "Details: " + e.getMessage(), 
								 LoggingType.Errors);
			setStatus(Status.Failure);
			return false;
			}
		
		//setStatus(Status.Success);
		return true;
	}
	
	/****************************************
	 * Called when a process worker thread finishes execution or terminates unexpectedly.
	 * 
	 * @param success Whether process returned successfully
	 * @param state The state of the pipeline which launched this process instance
	 * @return
	 */
	public boolean processingFinished(boolean success, PipelineState state){
		
		if (status.equals(Status.Interrupted)){
			state.setTaskInterrupted(true);
			}
		
		success &= !state.isTaskFailed();
		success &= !state.isTaskInterrupted();
		
		if (success){
			current = System.currentTimeMillis();
			setStatus(Status.Success);
			return true;
		}else{
			if (state.isTaskInterrupted()){
				setStatus(Status.Interrupted);
			}else{
				setStatus(Status.Failure);
				}
			}
		
		return success;
	}
	
	@Override
	public boolean interrupt() throws PipelineException{
		if (!getStatus().equals(Status.Processing))
			return false;
		
		if (process == null)
			throw new PipelineException("Process instance has no assigned process.");
		
		process.interrupt(uid);
		super.interrupt();
		
		return true;
	}
	
	/******************************************
	 * Returns the parameters of this process instance as an array of <code>String</code> values. 
	 * 
	 * @return
	 */
	public String[] getArguments(PipelineState state){
		return getArguments("", state);
	}
	
	/******************************************
	 * Returns the parameters of this process instance as an array of <code>String</code> values,
	 * for the specific instance and root directory.
	 * 
	 * @param instance
	 * @param shapes_dir
	 * @return
	 */
	public String[] getArguments(String instance, PipelineState state){
		ArrayList<String> v = new ArrayList<String>();
		//ArrayList<TaskParameterInstance> params = new ArrayList<TaskParameterInstance>(parameters.values());
		ArrayList<TaskParameter> params = process.getParameters();
		for (int i = 0; i < params.size(); i++){
			TaskParameterInstance p_instance = this.getParameter(params.get(i).name); 
			if (has_input && params.get(i).name.equals(process.input_parameter)){
				if (!state.input_file.startsWith("\""))
					p_instance.value = "\"" + state.input_file + "\"";
				else
					p_instance.value = state.input_file;
				}
			if (params.get(i).name.equals(process.output_parameter)){
				if (!state.output_file.startsWith("\""))
					p_instance.value = "\"" + state.output_file + "\"";
				else
					p_instance.value = state.output_file;
				}
			if (p_instance.apply)
				v.addAll(p_instance.asArgs(instance, state.root_dir));
			}
		
		String[] s = new String[v.size()];
		v.toArray(s);
		return s;
	}
	
	/******************************************
	 * Returns the parameters of this process instance as an array of <code>String</code> values,
	 * for the specific instance and project.
	 * 
	 * @param instance
	 * @param project
	 * @return
	 */
	public String[] getArguments(String instance, InterfaceProject project, PipelineState state){
		
		ArrayList<String> v = new ArrayList<String>();
		//ArrayList<TaskParameterInstance> params = new ArrayList<TaskParameterInstance>(parameters.values());
		ArrayList<TaskParameter> params = process.getParameters();
		for (int i = 0; i < params.size(); i++){
			TaskParameterInstance p_instance = this.getParameter(params.get(i).name); 
			if (has_input && params.get(i).name.equals(process.input_parameter)){
				if (!state.input_file.startsWith("\""))
					p_instance.value = "\"" + state.input_file + "\"";
				else
					p_instance.value = state.input_file;
				}
			if (params.get(i).name.equals(process.output_parameter)){
				if (!state.output_file.startsWith("\""))
					p_instance.value = "\"" + state.output_file + "\"";
				else
					p_instance.value = state.output_file;
				}
			if (p_instance.apply)
				v.addAll(p_instance.asArgs(instance, project));
			}
		
		String[] s = new String[v.size()];
		v.toArray(s);
		return s;
	}
	
	@Override
	public String getSuccessMessage(){
		return process.getSuccessMessage() + "; elapsed time " + TimeFunctions.getTimeStr(getElapsedTime());
	}
	
	@Override
	public String getFailureMessage(){
		return process.getFailureMessage() + "; elapsed time " + TimeFunctions.getTimeStr(getElapsedTime());
	}
	
	@Override
	public Icon getObjectIcon(){
		
		URL imgURL = null;
		boolean observe = false;
		
		switch (status){
			case NotStarted:
				if (this.process instanceof JavaProcess)
					imgURL = PipelineProcessInstance.class.getResource("/mgui/resources/icons/pipelines/java_process_instance_ns_20.png");
				else
					imgURL = PipelineProcessInstance.class.getResource("/mgui/resources/icons/pipelines/native_process_instance_ns_20.png");
				break;
			case Processing:
				if (this.process instanceof JavaProcess)
					imgURL = PipelineProcessInstance.class.getResource("/mgui/resources/icons/pipelines/java_process_instance_started_20.gif");
				else
					imgURL = PipelineProcessInstance.class.getResource("/mgui/resources/icons/pipelines/native_process_instance_started_20.gif");
				observe = true;
				break;
			case Success:
				if (this.process instanceof JavaProcess)
					imgURL = PipelineProcessInstance.class.getResource("/mgui/resources/icons/pipelines/java_process_instance_success_20.png");
				else
					imgURL = PipelineProcessInstance.class.getResource("/mgui/resources/icons/pipelines/native_process_instance_success_20.png");
				break;
			case Failure:
			case Interrupted:
				if (this.process instanceof JavaProcess)
					imgURL = PipelineProcessInstance.class.getResource("/mgui/resources/icons/pipelines/java_process_instance_failed_20.png");
				else
					imgURL = PipelineProcessInstance.class.getResource("/mgui/resources/icons/pipelines/native_process_instance_failed_20.png");
				break;
			}
		
		if (imgURL == null) return null;
		ImageIcon icon = new ImageIcon(imgURL);
		return icon;
	}
	
	@Override
	public String getTreeLabel(){
		return process.getCompoundName() + "." + getInstance();
	}
	
	@Override
	public String getPipelineTreeLabel(){
		return process.getCompoundName() + "." + getInstance() + " [" + getStatusStr() + "]";
	}
	
	/***********************************************
	 * Sets the value of the specified parameter, and whether it should be applied.
	 * 
	 * @param name
	 * @param value
	 * @param apply
	 * @return
	 */
	public boolean setParameter(String name, String value, boolean apply){
		TaskParameterInstance parameter = parameters.get(name);
		if (parameter == null) return false;
		
		parameter.value = value;
		parameter.apply = apply;
		
		return true;
	}
	
	/***********************************************
	 * Sets the value of the specified parameter.
	 * 
	 * @param name
	 * @param value
	 * @param apply
	 * @return
	 */
	public boolean setParameter(String name, String value){
		TaskParameterInstance parameter = parameters.get(name);
		if (parameter == null) return false;
		
		parameter.value = value;
		
		return true;
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode node){
	
		super.setTreeNode(node);
		
		node.add(new DefaultMutableTreeNode("Input: " + getInputString()));
		node.add(new DefaultMutableTreeNode("Output: " + getOutputString()));
		
		//ArrayList<TaskParameterInstance> params = new ArrayList<TaskParameterInstance>(parameters.values());
		ArrayList<TaskParameter> params = this.getProcess().getParameters();
		
		for (int i = 0; i < params.size(); i++){
			TaskParameterInstance instance = this.getParameter(params.get(i).name);
			if (instance != null && instance.apply){
				node.add(new InterfaceTreeNode(instance));
				}
			}
		
	} 
	
	/*****************************************
	 * Returns the path of the output file associated with this process.
	 * 
	 * @return
	 */
	public String getOutputFile(){
		return output_file;
	}
	
	/*****************************************
	 * Sets the path of the output file associated with this process.
	 * 
	 * @return
	 */
	public void setOutputFile(String file){
		output_file = file;
	}
	
	/*****************************************
	 * Returns the path of the input file associated with this process.
	 * 
	 * @return
	 */
	public String getInputFile(){
		return input_file;
	}
	
	/*****************************************
	 * Sets the path of the output file associated with this process.
	 * 
	 * @return
	 */
	public void setInputFile(String file){
		input_file = file;
	}
	
	/*****************************************
	 * Returns the path of the output file associated with this process. If no output file is set,
	 * returns "none".
	 * 
	 * @return
	 */
	public String getOutputString(){
		if (output_file == null) return "none";
		return output_file;
	}
	
	/*****************************************
	 * Returns the path of the input file associated with this process. 
	 * If this process has no input, returns "none". If it is set to receive input from the
	 * previous process, returns "previous".
	 * 
	 * @return
	 */
	public String getInputString(){
		if (!has_input) return "none";
		if (input_file == null) return "previous";
		return input_file;
	}
	
	@Override
	public String getLocalName() {
		return "ProcessInstance";
	}

	@Override
	public String getShortXML(int tab) {
		String _tab = XMLFunctions.getTab(tab);
		
		return _tab + "<ProcessInstance name = '" + getName() + "'/>\n";
	}

	@Override
	public String getXML(int tab) {
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		String xml = _tab + "<ProcessInstance\n";
		xml = xml + _tab2 + " name = '" + process.getCompoundName() + "'\n";
		xml = xml + _tab2 + " output = '" + getOutputString() + "'\n";
		xml = xml + _tab2 + " input = '" + getInputString() + "'\n";
		xml = xml + _tab2 + " prepend_subject_input = '" + prepend_instance_input + "'\n";
		xml = xml + _tab2 + " prepend_subject_output = '" + prepend_instance_output + "'\n";
		
		xml = xml + _tab2 + ">\n";
		
		ArrayList<TaskParameterInstance> params = new ArrayList<TaskParameterInstance>(parameters.values());
		
		for (int i = 0; i < params.size(); i++)
			xml = xml + params.get(i).getXML(tab + 2);
		
		xml = xml + _tab + "</ProcessInstance>\n";
		
		return xml;
	}
	
	public int getInstance() {
		return instance;
	}

	public void setInstance(int instance) {
		this.instance = instance;
	}

	public boolean hasInput() {
		return has_input;
	}

	public void setHasInput(boolean hasInput) {
		has_input = hasInput;
	}

	public String getLogger() {
		return logger;
	}

	public void setLogger(String logger) {
		this.logger = logger;
	}

	public HashMap<String, TaskParameterInstance> getParameters() {
		return parameters;
	}

	/*********************************************
	 * Sets the parameters for this process.
	 * 
	 * @param process_instance
	 */
	public void setParameters(HashMap<String, TaskParameterInstance> parameters) {
		this.parameters = parameters;
	}

	/*********************************************
	 * Sets the process defining this instance.
	 * 
	 * @param process
	 */
	public void setProcess(PipelineProcess process) {
		this.process = process;
	}
	
	public boolean isPrependSubjectInput() {
		return prepend_instance_input;
	}

	public void setPrependSubjectInput(boolean prependSubjectInput) {
		prepend_instance_input = prependSubjectInput;
	}

	public boolean isPrependSubjectOutput() {
		return prepend_instance_output;
	}

	public void setPrependSubjectOutput(boolean prependSubjectOutput) {
		prepend_instance_output = prependSubjectOutput;
	}
	
}