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

package mgui.pipelines;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.projects.InterfaceProject;
import mgui.io.util.IoFunctions;
import mgui.pipelines.PipelineTask.Status;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import foxtrot.Worker;

/***********************************************************************
 * Provides a set of common functions operating on pipeline objects.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PipelineFunctions {

	public static boolean fail_on_exception = true;
	
	public static boolean launchPipelineProcess(final PipelineProcessInstance process,
												final PipelineState state){
		return launchPipelineProcess(process, "", null, null, false, state);
	}
	
	public static boolean launchPipelineProcess(final PipelineProcessInstance process, 
												boolean blocking,
												final PipelineState state){
		return launchPipelineProcess(process, "", null, null, blocking, state);
	}
	
	public static boolean launchPipelineProcess(final PipelineProcessInstance process, 
												 final String instance, 
												 final InterfaceProject project,
												 final PipelineState state){
		return launchPipelineProcess(process, instance, project, null, false, state);
	}
	
	public static boolean launchPipelineProcess(final PipelineProcessInstance process, 
												final String instance, 
												final InterfaceProject project, 
												final String logger,
												final PipelineState state){
		return launchPipelineProcess(process, instance, project, logger, false, state);
	}
	
	public static boolean launchPipelineProcess(final PipelineProcessInstance process, 
												final String instance, 
												final InterfaceProject project,  
												final String logger,
												final boolean blocking,
												final PipelineState state){
		
		if (blocking){
			boolean success = launchBlockingPipelineProcess(process, instance, project, logger, state);
			processCompleted(process, success, state);
			return success;
			}
		
		//boolean success = false;
		
		SwingWorker<Boolean,Object> worker = new SwingWorker<Boolean,Object>() {
			 @Override 
			public Boolean doInBackground() throws Exception{
		    	 return launchBlockingPipelineProcess(process, instance, project, logger, state);
		     }
		     
		     @Override
		     protected void done(){
		    	 boolean success = false;
		    	 try{
		    		 success = get();
		    		 if (!success){
		    			 state.setTaskFailed(true);
			    	 	 }
		    		 if (process.getStatus().equals(Status.Interrupted)){
		    			 state.setTaskInterrupted(true);
		    		 	 }
		    	 }catch (Exception ex){
		    		InterfaceSession.log("Process '" + process.getName() + "." + instance + "' threw exception: " +
					    				ex.getMessage(), 
					    				LoggingType.Errors);
		    		state.setTaskFailed(true); 
		    	 	}
		    	 
		    	 processCompleted(process, success, state);
		     }
		     
		};
		
		worker.execute();
			
		return true;
	}
	
	/**************************************************
	 * Called when a {@link SwingWorker} invoked by {@code launchPipelineProcess} completes.
	 * 
	 * @param process
	 * @param instance
	 * @param project
	 * @param logger
	 * @param blocking
	 * @param state
	 */
	protected static void processCompleted(PipelineProcessInstance process,
										   boolean success,
										   PipelineState state){
		
		process.processingFinished(success, state);
		
	}
	
//	static class RunProcess implements Runnable {
//		public boolean success;
//		public PipelineProcessInstance process;
//		public String instance; 
//		public InterfaceProject project;  
//		public String logger;
//		public boolean blocking;
//		public PipelineState state;
//		
//		public RunProcess(PipelineProcessInstance process, 
//						  String instance, 
//						  InterfaceProject project,  
//						  String logger,
//						  PipelineState state){
//			this.process = process;
//			this.instance = instance;
//			this.project = project;
//			this.logger = logger;
//			this.blocking = blocking;
//			this.state = state;
//			
//		}
//		
//		public void run(){
//			
//			success = launchBlockingPipelineProcess(process, instance, project, logger, state);
//			
//		}
//		
//		
//	}
	
	/*************************************************
	 * Launches a pipeline process instance and blocks until it terminates.
	 * 
	 * @param process_instance
	 * @param instance
	 * @param project
	 * @param logger
	 * @param state
	 * @return
	 */
	public static boolean launchBlockingPipelineProcess(final PipelineProcessInstance process_instance, 
														final String instance, 
														final InterfaceProject project,  
														final String logger,
														final PipelineState state){
		
		//TODO: add System.err listener to catch any errors in running processes
		PipelineState.ErrorStreamListener error_stream = null;
		
		if (fail_on_exception){
			error_stream = state.new ErrorStreamListener(state);
			error_stream.start();
			}
		
		state.setTaskFailed(false);
		state.setTaskInterrupted(false);
		if (project != null)
			state.root_dir = project.getRootDir().getAbsolutePath();
		
		if (state.root_dir == null){
			InterfaceSession.log("No root directory specified");
			return false;
			}
		
		boolean copy_output = false;
		
		String temp_input = state.getTempInputPath();
		String temp_output = state.getTempOutputPath();
		
		if (process_instance.has_input){
			if (state.input_file == null)
				state.input_file = temp_input;
		}else{
			state.input_file = null;
			}
		//else
		//	copy_input = true;
		
		if (state.output_file == null)
			state.output_file = temp_output;
		else
			copy_output = true;
		
		updateLogger();
		
		try{
			String[] args = null;
			
			if (project != null)
				args = process_instance.getArguments(instance, project, state);
			else
				args = process_instance.getArguments(instance, state);
			
			InterfaceSession.log("Processing " + process_instance.toString() + ": ", 
					 			 LoggingType.Verbose);
			PipelineProcess process = process_instance.getProcess();
			
			process.updateLogger();
			
			boolean failed = !process.run(args, process_instance.uid);
			
			error_stream.close();
			
			state.setTaskFailed(failed);
			
		}catch (PipelineException e){
			InterfaceSession.log("Exception running process.\nDetails: " +
								 e.getMessage(), 
								 LoggingType.Errors); // '" + process.toString() + "'.");
			//e.printStackTrace();
			if (fail_on_exception)
				error_stream.close();
			return false;
			}
		
		if (state.isTaskInterrupted()){
			InterfaceSession.log("User interrupted process.", LoggingType.Verbose); // '" + process.toString() + "'.");
			return false;
			}
		if (state.isTaskFailed()){
			InterfaceSession.log("Task failed: '" + process_instance.toString() + "'.", LoggingType.Errors);
			return false;
			}
		
		InterfaceSession.log("Success.", LoggingType.Verbose);
		
		//copy output file if necessary (so it is available for next process)
		if (copy_output){
			File file = new File (state.output_file);
			File temp = new File (temp_input);
			try{
				if (!temp.exists() && !temp.createNewFile()){
					InterfaceSession.log("Cannot create temp output file '" + temp_input +"'");
					return false;
					}
				if (file.exists() && temp.exists()){
					IoFunctions.copyFile(file, temp);
					}
			}catch (IOException ex){
				InterfaceSession.log("Cannot copy to temp input file '" + temp_input +"'");
				ex.printStackTrace();
				return false;
				}
		}else if (process_instance.has_input){
			//otherwise rename temp output to input
			File temp_in = new File(temp_input);
			if (temp_in.exists() && !temp_in.delete()){
				InterfaceSession.log("Cannot delete temp file '" + temp_in.getAbsolutePath() +"'");
				return false;
				}
			File temp_out = new File(temp_output);
			if (!temp_out.exists()){
				InterfaceSession.log("Cannot find temp file '" + temp_out.getAbsolutePath() +"'");
				return false;
				}
			if (!temp_out.renameTo(temp_in)){
				InterfaceSession.log("Cannot rename temp file '" + temp_out.getAbsolutePath() +"'");
				return false;
				}
			}

		return true;
	}
	
	public static boolean launchPipelineAsJob(final InterfacePipeline pipeline, final boolean blocking) throws PipelineException{
		
		foxtrot.Task current_task = new foxtrot.Task(){
			@Override
			public Object run(){
				try{
					return pipeline.launch(blocking);
				}catch (PipelineException ex){
					return ex;
					}
				}
			};
		
		boolean success = false;
		try{
			Object result = Worker.post(current_task);
			if (result instanceof Boolean)
				success = (Boolean)result;
			else
				throw (PipelineException)result;
		}catch (Exception e){
			e.printStackTrace();
			success = false;
			}
		
		return success;
		
		
	}
	
	private static void updateLogger(){
		Logger logger = Logger.getLogger("camino");
		Handler[] handlers = logger.getHandlers();
		
		for (int i = 0; i < handlers.length; i++)
			if (handlers[i] instanceof ConsoleHandler) return;
		
		logger.addHandler(new ConsoleHandler());
	}
	
	public static String getArgString(String[] args){
		String s = "";
		for (int i = 0; i < args.length; i++)
			if (i > 0)
				s = s + ",'" + args[i] + "'";
			else
				s = "'" + args[i] + "'";
		return s;
	}
	
	public static ArrayList<String> parseArg(String arg){
		return parseArg(arg, "", "");
	}
	
	/******************************************
	 * Parses an argument, replacing special symbols contained within "{" and "}" characters with their value;
	 * including:
	 * 
	 * <ul>
	 * <li>instance		The current pipeline instance
	 * <li>root			The current root directory
	 * <li>instances_dir		The subdirectory for instance data (relative to root; note that root_dir will be 
	 * 						prepended to this string, and instance will be appended)
	 * <li>project_dir		The subdirectory for project data (relative to root; note that root_dir will be 
	 * 						prepended to this string)
	 * </ul>
	 * 
	 * @param arg
	 * @param subject
	 * @param root
	 * @return
	 */
	public static ArrayList<String> parseArg(String arg, String instance, InterfaceProject project){
		return parseArg(arg, instance, 
						project.getRootDir().getAbsolutePath(), 
						project.getInstanceDir(),
						project.getProjectDir());
	}
	
	/******************************************
	 * Parses an argument, replacing special symbols contained within "{" and "}" characters with their value;
	 * including:
	 * 
	 * <ul>
	 * <li>instance		The current pipeline instance
	 * <li>root			The current root directory
	 * <li>instances_dir		The subdirectory for instance data (relative to root; note that root_dir will be 
	 * 						prepended to this string, and instance will be appended)
	 * <li>project_dir		The subdirectory for project data (relative to root; note that root_dir will be 
	 * 						prepended to this string)
	 * </ul>
	 * 
	 * @param arg
	 * @param subject
	 * @param root
	 * @return
	 */
	public static ArrayList<String> parseArg(String arg, String instance, String root_dir){
		return parseArg(arg, instance, root_dir, null, null);
	}
		
		
	/******************************************
	 * Parses an argument, replacing special symbols contained within "{" and "}" characters with their value;
	 * including:
	 * 
	 * <ul>
	 * <li>instance			The current pipeline instance
	 * <li>root_dir			The current root directory
	 * <li>instances_dir		The subdirectory for instance data (relative to root; note that root_dir will be 
	 * 						prepended to this string, and instance will be appended)
	 * <li>project_dir		The subdirectory for project data (relative to root; note that root_dir will be 
	 * 						prepended to this string)
	 * </ul>
	 * 
	 * @param arg
	 * @param subject
	 * @param root
	 * @return
	 */
	public static ArrayList<String> parseArg(String arg, String instance, String root_dir,
											 String instance_dir, String project_dir){
		
		if (instance == null) instance = "";
		ArrayList<String> tokens = new ArrayList<String>();
		
		arg = arg.replace("/", File.separator);
		arg = arg.replace("\\", File.separator);
		char[] chars = arg.toCharArray();
		String thisToken = "";
		boolean in_quotes = false;
		boolean in_brackets = false;
		String replace = null;
		
		if (instance_dir == null)
			instance_dir = root_dir;
		else
			instance_dir = root_dir + File.separator + instance_dir;
		if (project_dir == null)
			project_dir = root_dir;
		else
			project_dir = root_dir + File.separator + project_dir;
				
		//TODO: allow calculations/expressions...?
		for (int i = 0; i < chars.length; i++){
			switch (chars[i]){
				case '{':
					replace = "";
					in_brackets = true;
					break;
				case '}':
					if (in_brackets){
						if (replace.toLowerCase().equals("instance"))
							thisToken = thisToken + instance;
						else if (replace.toLowerCase().equals("root"))
							thisToken = thisToken + root_dir;
						else if (replace.toLowerCase().equals("instances_dir"))
							thisToken = thisToken + instance_dir;
						else if (replace.toLowerCase().equals("project_dir"))
							thisToken = thisToken + project_dir;
						else
							thisToken = thisToken + "{" + replace + "}";
						}
					replace = null;
					in_brackets = false;
					break;
				case '\"':
					if (in_quotes){
						if (thisToken.length() > 0)
							tokens.add(thisToken);
						thisToken = "";
						in_quotes = false;
					}else{
						in_quotes = true;
						thisToken = "";
						}
					break;
				case ' ':
					if (!in_quotes){
						if (thisToken.length() > 0)
							tokens.add(thisToken);
						thisToken = "";
					}else{
						thisToken = thisToken + " ";
						}
					break;
				default:
					if (in_brackets)
						replace = replace + String.valueOf(chars[i]);
					else
						thisToken = thisToken + String.valueOf(chars[i]);
				}
			}
		if (thisToken.length() > 0) tokens.add(thisToken);
		return tokens;
	}
	
	/**************************************************************
	 * Returns a list of all files that match the specified wildcard {@code pattern}. Wildcards are specified
	 * with the "%" character. 
	 * 
	 * @param pattern
	 * @return
	 */
	public static ArrayList<String> getWildcardFiles(File dir, String pattern){
		return getWildcardFiles(dir, pattern, null);
	}
	
	/**************************************************************
	 * Returns a list of all files that match the specified wildcard {@code pattern}. Wildcards are specified
	 * with the "*" character. Additionally, if a sequence of alphanumeric characters is inserted between
	 * two "%" characters, its value will be stored in the {@code value_map}, assuming this is not {@code null}. 
	 * 
	 * @param pattern
	 * @param value_map 		Map of value names with patterns matching them; this will be populated by the function,
	 *                          and associates variable names with lists having an entry for each returned file 
	 * @return
	 */
	public static ArrayList<String> getWildcardFiles(File dir, String pattern, HashMap<String,ArrayList<String>> value_map){
		
		// Replace variable strings
		
		ArrayList<String> vars = new ArrayList<String>();
		String pattern2 = pattern;
		if (dir != null){
			pattern2 = dir.getAbsolutePath() + File.separator + pattern;
			}
		char[] chars = pattern2.toCharArray();
		 
		boolean in_var = false;
		String this_var = null;
		String new_pattern = "";
		ArrayList<String> chunks = new ArrayList<String>();
		int last_idx = 0;
		for (int i = 0; i < chars.length; i++){
			if (chars[i] == '%'){
				if (!in_var){
					// Not already in a variable, start one
					if (i == last_idx && last_idx > 0){
						InterfaceSession.log("PipelineFunctions.getWildcardFiles: Consecutive %'s not allowed.", 
											 LoggingType.Errors);
						return null;
						}
					in_var = true;
					this_var = "";
					if (i > 0){
						chunks.add(pattern2.substring(last_idx, i));
					}
				}else{
					// Already in a variable, finalize it
					in_var = false;
					vars.add(this_var);
					this_var = null;
					new_pattern = new_pattern + "*";
					last_idx = i + 1;
					}
			}else if (in_var){
				this_var = this_var + chars[i];
				}
			}
		
		if (last_idx < pattern2.length() - 1){
			chunks.add(pattern2.substring(last_idx));
			}
		
		for (int i = 0; i < vars.size(); i++){
			pattern2 = pattern2.replace("%" + vars.get(i) + "%", "*");
			}
		
		ArrayList<File> files = IoFunctions.getWildcardPathFiles(null, pattern2);
		
		// Go through the files and extract the variable values for each
		if (value_map != null){
			value_map.clear();
			for (int i = 0; i < vars.size(); i++){
				value_map.put(vars.get(i), new ArrayList<String>());
				}
			for (int i = 0; i < files.size(); i++){
				String this_file = files.get(i).getAbsolutePath();
				for (int j = 0; j < vars.size(); j++){
					String chunk = chunks.get(j);
					int idx1 = chunk.length();
					int idx2 = -1;
					if (j < chunks.size() - 1){
						String subs = this_file.substring(idx1);
						idx2 = idx1 + subs.indexOf(chunks.get(j+1));
					}else{
						idx2 = this_file.length();
						}
					value_map.get(vars.get(j)).add(this_file.substring(idx1, idx2));
					if (j < chunks.size() - 1){
						this_file = this_file.substring(idx2);
						}
					}
				}
			}
		
		ArrayList<String> files_out = new ArrayList<String>();
		for (int i = 0; i < files.size(); i++){
			files_out.add(files.get(i).getAbsolutePath());
			}
		
		return files_out;
	}
	
	/*********************************************************************
	 * Replaces all variables in {@code param} with the values in {@code variables}. Variables in a 
	 * parameter value must be specified by their name, wrapped in "%" 
	 * 
	 * <p>Example "blah_%var1%.dat" will replace the text "%var1%" with the {@code idx}th entry in the
	 * list found in {@code variables.get("var1")}.
	 *
	 * @param param
	 * @param variables
	 * @param i
	 * @return
	 */
	public static String setParameterVariables(String param, HashMap<String,ArrayList<String>> variables, int idx){
		
		ArrayList<String> vars = new ArrayList<String>(variables.keySet());
		
		for (int i = 0; i < vars.size(); i++){
			String var = "%" + vars.get(i) + "%";
			param = param.replaceAll(var, variables.get(vars.get(i)).get(idx));
			}
		
		return param;
	}
	
}