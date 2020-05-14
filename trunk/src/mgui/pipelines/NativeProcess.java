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

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.command.CommandFunctions;
import mgui.command.CommandFunctions.ProcessState;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.xml.XMLFunctions;

/*******************************************************
 * Represents a native process; i.e., one which runs from the OS command console.
 * 
 * <p>Specified by the <code>command</code>, which is the native executable; and the
 * <code>path</code>, which is a path to the desired version (an empty string results
 * in the system default). 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class NativeProcess extends PipelineProcess {

	protected String command = "";
	protected String path = "";
	protected String set_operator = " "; 	// character which sets a parameter value (some functions
											// annoyingly use "=", for instance
	protected HashMap<Long, Process> current_system_processes = new HashMap<Long, Process>();
	
	//protected HashMap<String, String> environment; // = new HashMap<String, String>();
	
	public NativeProcess(PipelineProcess process){
		setFromProcess(process);
	}
	
	public NativeProcess(String name, String command){
		this(name, command, null, "", " ");
	}
	
	public NativeProcess(String name, String command, String logger){
		this(name, command, logger, "", " ");
	}
	
	public NativeProcess(String name, String command, String logger, String path){
		this(name, command, logger, path, " ");
	}
	
	public NativeProcess(String name, String command, String logger, String path, String set_operator){
		this.name = name;
		this.logger = logger;
		this.command = command;
		this.path = path;
		this.set_operator = set_operator;
		//this.environment = environment;
	}
	
	@Override
	public void setFromProcess(PipelineProcess process){
		if (process instanceof JavaProcess){
			this.command = ((JavaProcess)process).getMainClass();
		}else{
			this.command = ((NativeProcess)process).getCommand();
			this.path = ((NativeProcess)process).getPath();
			this.set_operator = ((NativeProcess)process).getSetOperator();
			//this.setEnvironment(((NativeProcess)process).getEnvironment());
			}
		super.setFromProcess(process);
		
	}
	
	@Override
	public void showHelp(){
		
		if (help_param == null){
			InterfaceSession.log("No help parameter set.", LoggingType.Warnings);
			return;
			}
		
		ProcessState state = tryHelp(help_param);
		if (state == ProcessState.TerminatedNormally) return;
		
		InterfaceSession.log("No help message found.", LoggingType.Warnings);
		
	}
	
	private ProcessState tryHelp(String param){
		try{
			String command = getCommand(new String[]{param});
			// Set the environment from the library if it is set
			HashMap<String,String> environment = null;
			if (getLibrary() != null)
				environment = getLibrary().getEnvironment();
			Process system_process = CommandFunctions.call(getName(), command, environment);
			if (system_process == null){
				InterfaceSession.log("No help message found.", LoggingType.Warnings);
				return ProcessState.TerminatedWithError;
				}
			int exitVal = system_process.waitFor();
			return CommandFunctions.getProcessState(exitVal);
		}catch (Exception e){
			return ProcessState.TerminatedWithError;
			}
	}
	
	
	
	@Override
	public Icon getObjectIcon(){
		URL imgURL = NativeProcess.class.getResource("/mgui/resources/icons/pipelines/native_process_20.png");
		
		if (imgURL == null){
			InterfaceSession.log("NativeProcess: Cannot find icon at /mgui/resources/icons/pipelines/native_process_20.png");
			return null;
		}
		
		return new ImageIcon(imgURL);
		
	}
	
	/*************************************************
	 * Returns the native command for this process.
	 * 
	 * @return
	 */
	public String getCommand() {
		return command;
	}

	/*************************************************
	 * Sets the set operator character for this process. E.g., for a function
	 * expecting -param_name param_value, the character is a space (default).
	 * For -param_name=param_value, the character is "=".
	 * 
	 * @param command
	 */
	public void setSetOperator(String set_operator) {
		this.set_operator = set_operator;
	}
	
	/*************************************************
	 * Returns the set operator character for this process. E.g., for a function
	 * expecting -param_name param_value, the character is a space (default).
	 * For -param_name=param_value, the character is "=".
	 * 
	 * @return
	 */
	public String getSetOperator() {
		return set_operator;
	}

	/*************************************************
	 * Sets the native command for this process.
	 * 
	 * @param command
	 */
	public void setCommand(String command) {
		this.command = command;
	}
	
	/*************************************************
	 * Returns the path to the native binaries for this process.
	 * 
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/*************************************************
	 * Sets the path to the native binaries for this process.
	 * 
	 * @param command
	 */
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String[] getInputArgs(String[] args, String input_file) {
		String param = "-" + input_parameter;
		
		for (int i = 0; i < args.length; i++)
			if (args[i].equals(param)){
				args[i + 1] = input_file;
				return args;
				}
		
		String[] args2 = new String[args.length + 2];
		args2[0] = param;
		args2[1] = input_file;
		System.arraycopy(args, 0, args2, 2, args.length);
		return args2;
	}

	@Override
	public String[] getOutputArgs(String[] args, String output_file) {
		String param = "-" + output_parameter;
		
		for (int i = 0; i < args.length; i++)
			if (args[i].equals(param)){
				args[i + 1] = output_file;
				return args;
				}
		
		String[] args2 = new String[args.length + 2];
		args2[0] = param;
		args2[1] = output_file;
		System.arraycopy(args, 0, args2, 2, args.length);
		return args2;
	}
	
	protected String getCommand(String[] args){
		String command_args = "";
		if (path != null && path.length() > 0){
			command_args = path;
			if (!command_args.endsWith(File.separator))
				command_args = command_args + File.separator;
			}
			
		command_args = command_args + command;
		
		boolean last_was_param = false;
		for (int i = 0; i < args.length; i++){
			boolean this_is_param = args[i].startsWith("-");
			if (last_was_param && !this_is_param)
				command_args = command_args + set_operator + args[i];
			else
				command_args = command_args + " " + args[i];
			last_was_param = this_is_param;
			}
		return command_args;
	}

	
	
	@Override
	public boolean run(String[] args, long uid) throws PipelineException {
		
		String command = getCommand(args);
		
		try{
			HashMap<String,String> environment = null;
			if (getLibrary() != null)
				environment = getLibrary().getEnvironment();
			Process system_process = CommandFunctions.call(getName(), command, environment);
			if (system_process == null){
				InterfaceSession.log("NativeProcess: Process was not executed. See log.", 
						 LoggingType.Errors);
				return false;
				}
			current_system_processes.put(uid, system_process);
			 
			int exitVal = system_process.waitFor();
			CommandFunctions.ProcessState state = CommandFunctions.getProcessState(exitVal);
			
			current_system_processes.remove(uid);
			
			switch (state){
				case Interrupted:
					throw new PipelineException("Process was interrupted.");
					
				case TerminatedWithError:
					throw new PipelineException("Process terminated with error.");
					
				default:
					return true;
				}
			
		}catch (IOException e){
			InterfaceSession.log("NativeProcess: IOException on process '" + command + "'.\n" +
								 "Details: " + e.getMessage(), 
								 LoggingType.Errors);
		}catch (InterruptedException e){
			InterfaceSession.log("NativeProcess: Process '" + command + "' was interrupted.\n" +
								 "Details: " + e.getMessage(), 
								 LoggingType.Errors);
			
		}catch (Exception e){
			InterfaceSession.log("NativeProcess: Exception on process '" + command + "'.\n" +
								 "Details: " + e.getMessage(), 
								 LoggingType.Errors);
			
			}
		
		current_system_processes.remove(uid);
		throw new PipelineException("Process terminated with error.");
		//return false;
	}
	
	@Override
	public boolean interrupt(long uid) throws PipelineException{
		Process system_process = current_system_processes.get(uid);
		if (system_process == null) return false;
		
		// Stops native process dead
		system_process.destroy();
		current_system_processes.remove(uid);
		return true;
	}

	@Override
	public String getFailureMessage() {
		return "Native process " + toString() + " failed.";
	}

	@Override
	public String getSuccessMessage() {
		return "Native process " + toString() + " finished successfully.";
	}

	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progressBar) throws IOException {
		
		super.writeXML(tab, writer, progressBar);
	}

	@Override
	public String getXML(int tab) {
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		String xml = _tab + "<NativeProcess name ='" + name + "' command = '" + command + "'";
		if (input_parameter != null)
			xml = xml + " input_parameter = '" + input_parameter + "'";
		if (output_parameter != null)
			xml = xml + " output_parameter = '" + output_parameter + "'";
		if (help_param != null)
			xml = xml + " help_parameter = '" + help_param + "'";
		if (logger != null)
			xml = xml + " logger = '" + logger + "'";
		if (path != null)
			xml = xml + " path = '" + path + "'";
		if (path != null)
			xml = xml + " set_operator = '" + set_operator + "'";
		xml = xml + " >\n";
		
		for (int i = 0; i < parameters.size(); i++)
			xml = xml + parameters.get(i).getXML(tab + 1);
		
//		if (environment != null){
//			xml = xml + _tab2 + "<Environment>\n";
//			String _tab3 = XMLFunctions.getTab(tab + 2);
//			ArrayList<String> keys = new ArrayList<String>(environment.keySet());
//			Collections.sort(keys);
//			for (int i = 0; i < keys.size(); i++)
//				xml = xml + _tab3 + "<Element name='" + keys.get(i) + 
//									"' value='" + environment.get(keys.get(i)) + "' />\n"; 
//			xml = xml + _tab2 + "</Environment>\n";
//			}
		
		xml = xml + _tab + "</NativeProcess>\n";
		
		return xml;
	}

	@Override
	public Object clone() {
		NativeProcess process = new NativeProcess(name, command, logger, path, set_operator);
		process.input_parameter = input_parameter;
		process.output_parameter = output_parameter;
		process.help_param = help_param;
		for (int i = 0; i < parameters.size(); i++){
			TaskParameter p = parameters.get(i);
			process.addParameter(new TaskParameter(p.name, 
														 p.default_value, 
														 p.optional, 
														 p.has_value,
														 p.use_name));
			}
		return process;
	}
	

}