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
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.command.CommandFunctions;
import mgui.command.CommandInstance;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.xml.XMLFunctions;

/*************************************************
 * Represents a Java process; i.e., a class containing a <code>main</code> method which allows it
 * to be run as a process.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class JavaProcess extends PipelineProcess {

	protected String main_class;
	protected boolean is_interrupted = false;
	
	protected HashMap<Long, CommandInstance> current_system_processes = new HashMap<Long, CommandInstance>();
	
	public JavaProcess(PipelineProcess process){
		setFromProcess(process);
	}
	
	public JavaProcess(String name, String main_class){
		this(name, main_class, "inputfile", "outputfile", null);
	}

	public JavaProcess(String name, String main_class, 
					   String input_parameter, String output_parameter){
		this(name, main_class, input_parameter, output_parameter, null);
	}
	
	public JavaProcess(String name, String main_class, 
					   String input_parameter, String output_parameter,
					   String logger){
		this.name = name;
		this.main_class = main_class;
		this.logger = logger;
		this.input_parameter = input_parameter;
		this.output_parameter = output_parameter;
	}
	
	@Override
	public void setFromProcess(PipelineProcess process){
		if (process instanceof JavaProcess)
			this.main_class = ((JavaProcess)process).getMainClass();
		else
			this.main_class = ((NativeProcess)process).getCommand();
		super.setFromProcess(process);
	}
	
	@Override
	public Icon getObjectIcon(){
		URL imgURL = JavaProcess.class.getResource("/mgui/resources/icons/pipelines/java_process_20.png");
		
		if (imgURL == null){
			InterfaceSession.log("JavaProcess: Cannot find icon at /mgui/resources/icons/pipelines/java_process_20.png");
			return null;
		}
		
		return new ImageIcon(imgURL);
		
	}
	
	@Override
	public void showHelp(){
		
		if (help_param == null || help_param.length() == 0){
			InterfaceSession.log("No help parameter set.", LoggingType.Warnings);
			return;
			}
		
		Class<?>[] argTypes = new Class[1];
		argTypes[0] = String[].class;
		
		try{
			Class<?> target_class = Class.forName(main_class);
			Class<?> command_class = CommandInstance.class;
			if (command_class.isAssignableFrom(target_class)){
				
				CommandInstance instance = (CommandInstance)target_class.newInstance();
				String command = null;
				for (int i = 0; i < parameters.size(); i++){
					TaskParameter param = parameters.get(i);
					if (param.name.equals("command"))
						command = param.default_value;
					}
				if (command == null){
					InterfaceSession.log("No help message found.", LoggingType.Warnings);
					return;
					}
				instance.execute(new String[]{command,help_param});
				}
		}catch (Exception e){
			InterfaceSession.log("No help message found.", LoggingType.Warnings);
			return;
			}
		
	}
	
	@Override
	public String getSuccessMessage(){
		return "Java process " + toString() + " [" + main_class + "] finished successfully.";
	}
	
	@Override
	public String getFailureMessage(){
		return "Java process " + toString() + " [" + main_class + "] failed...";
	}
	
	@Override
	public Object clone(){
		
		JavaProcess process = new JavaProcess(name, main_class, input_parameter, output_parameter, logger);
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
	
	@Override
	public boolean run(String[] args, long uid) throws PipelineException{
		
		Class<?>[] argTypes = new Class[1];
		argTypes[0] = String[].class;
		
		try{
			Class<?> target_class = Class.forName(main_class);
			Class<?> command_class = CommandInstance.class;
			if (command_class.isAssignableFrom(target_class)){
				
				CommandInstance instance = (CommandInstance)target_class.newInstance();
				current_system_processes.put(uid, instance);
				
				CommandFunctions.ProcessState state = instance.execute(args);
				
				current_system_processes.remove(uid);
				
				switch (state){
					case Interrupted:
						throw new PipelineException("Process was interrupted.");
						
					case TerminatedWithError:
						throw new PipelineException("Process terminated with error.");
						
					default:
						return true;
					}
				
				}
		}catch (InvocationTargetException e){
			throw new PipelineException("Java process '" + this.getName() + "." + uid + "' encountered exception: " + e.getCause().getMessage());
		}catch (ClassNotFoundException e){
			throw new PipelineException("JavaProcess main class not found: " + main_class);
		}catch (Exception e){
			throw new PipelineException("JavaProcess encountered exception invoking target: " + e.getMessage());
			}
				
		try{
			Class<?> target_class = Class.forName(main_class);
			Method main_method = target_class.getDeclaredMethod("main", argTypes);
	
			if (main_method == null){
				InterfaceSession.log("Exception running process (no main method).");
				return false;
				}
		
			main_method.invoke(null, new Object[]{args});
		}catch (InvocationTargetException e){
			throw new PipelineException("Java process '" + this.getName() + "." + uid + "' encountered exception: " + e.getCause().getMessage());
		}catch (Exception e){
			throw new PipelineException("JavaProcess encountered exception invoking target: " + e.getMessage());
			}
		
		return true;
	}
	
	@Override
	public boolean interrupt(long uid) throws PipelineException{
		// No good way to interrupt a Java process... unless the class checks for an interrupt
		// request. May be advisable to enforce a particular interface as a wrapper for Java
		// processes, with interrupt functionality.
		// Note: interrupt is now possible for classes which inherit CommandInstance
		CommandInstance instance = current_system_processes.get(uid);
		if (instance == null) return false;
		
		if (instance.interrupt()){
			is_interrupted = true;
			return true;
			}
		
		return false;
	}
	
	public boolean isInterrupted(){
		return is_interrupted;
	}
	
	public String getMainClass() {
		return main_class;
	}

	public void setMainClass(String mainClass) {
		main_class = mainClass;
	}
	
	@Override
	public String getXML(int tab) {
		
		String _tab = XMLFunctions.getTab(tab);
		
		String xml = _tab + "<JavaProcess name ='" + name + "' main_class = '" + main_class + "'"; 
		if (input_parameter != null)
			xml = xml + " input_parameter = '" + input_parameter + "'";
		if (output_parameter != null)
			xml = xml + " output_parameter = '" + output_parameter + "'";
		if (output_parameter != null)
			xml = xml + " help_parameter = '" + help_param + "'";
		if (logger != null)
			xml = xml + " logger = '" + logger + "'";
		xml = xml + " >\n";
		
		for (int i = 0; i < parameters.size(); i++)
			xml = xml + parameters.get(i).getXML(tab + 1);
		
		xml = xml + _tab + "</JavaProcess>\n";
		
		return xml;
	}
	
	@Override
	public String[] getInputArgs(String args[], String input_file){
		
		TaskParameter task_param = getParameter(input_parameter);
		if (task_param == null)	return null;
		
		//parameters are ordered; find the input parameter and change it
		ArrayList<TaskParameter> parameters = this.getParameters();
		int p = 0;
		for (int i = 0; i < parameters.size(); i++){
			if (parameters.get(i).equals(task_param)){
				if (task_param.use_name)
					args[p + 1] = input_file;
				else
					args[p] = input_file;
				return args;
				}
			if (parameters.get(i).use_name)
				p++;
			if (parameters.get(i).has_value)
				p++;
			}
		
		//parameter not found; return null
		return null;
		
	}
	
	@Override
	public String[] getOutputArgs(String args[], String input_file){
		
		TaskParameter task_param = getParameter(output_parameter);
		if (task_param == null)	return null;
		
		//parameters are ordered; find the input parameter and change it
		ArrayList<TaskParameter> parameters = this.getParameters();
		int p = 0;
		for (int i = 0; i < parameters.size(); i++){
			if (parameters.get(i).equals(task_param)){
				if (task_param.use_name)
					args[p + 1] = input_file;
				else
					args[p] = input_file;
				return args;
				}
			if (parameters.get(i).use_name)
				p++;
			if (parameters.get(i).has_value)
				p++;
			}
		
		//parameter not found; return null
		return null;
		
	}
	
}