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

package mgui.command;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import mgui.command.CommandFunctions.ProcessState;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.pipelines.JavaProcess;

/****************************************************************
 * Class which handles command-line arguments and calls specific methods
 * as implemented by a sub-class.
 * 
 * <p>Parameters are handled as pairs (parameter name preceded by a dash
 * and followed by a value) or as flags (single flag parameter preceded by
 * a dash).
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class CommandInstance extends CommandInterpreter {

	protected long instance_timer;
	protected HashMap<String, String> parameters;
	protected BufferedWriter log_out;
	protected LoggingType default_logging_type = LoggingType.Concise;
	
	protected static HashMap<Long,JavaProcess> current_processes = new HashMap<Long,JavaProcess>();
	protected boolean is_interrupted = false;
	
	protected boolean exit_on_termination = false;
	
	public CommandInstance(){
		
	}

	/****************************************
	 * Requests that the currently executing process terminate
	 * 
	 * @return
	 */
	public boolean interrupt(){
		is_interrupted = true;
		return true;
	}
	
	@Override
	public ProcessState execute(String[] args) throws Exception{
		
		String command = args[0];
		parameters = getParameters(args);
		
		// Log file
		String param = parameters.get("log_file");
		log_out = null;
		boolean logging = false;
		if (param != null) 
			logging = start_log(param);
		
		startInstanceTimer();
		
		setSuccessStatus(run_command(command));
		
		stopInstanceTimer();
		
		if (logging)
			stop_log();
		
		return instance_state;
	}
	
	protected void setSuccessStatus(boolean status){
		if (is_interrupted){
			instance_state = ProcessState.Interrupted;
			return;
			}
		super.setSuccessStatus(status);
	}
	
	/*******************************************
	 * Converts an array of parameters to a single space-delimited line.
	 * 
	 * @param args
	 * @return
	 */
	protected String getCommandLine(String[] args){
		String command = "";
		for (int i = 0; i < args.length; i++){
			command = command + " " + args[i];
			}
		return command;
	}
	
	protected boolean start_log(String file){
		try{
			log_out = new BufferedWriter(new FileWriter(new File(file)));
			return true;
		}catch (IOException e){
			InterfaceSession.log("command_instance: Exception opening log file '" + file + "'.");
			return false;
			}
	}
	
	protected boolean stop_log(){
		if (log_out == null) return false;
		
		try{
			log_out.close();
		}catch (Exception e){
			InterfaceSession.log("Error closing log file...");
			return false;
			}
		return true;
		
	}
	
	protected void log(String message){
		log(message, default_logging_type);
	}
	
	protected void log(String message, LoggingType type){
		
		if (log_out != null){
			try{
				log_out.write(message + "\n");
			}catch (Exception e){
				InterfaceSession.log("Error writing to log file...", LoggingType.Errors);
				log_out = null;
				}
			return;
			}
		
		InterfaceSession.log(message, type);
		
	}
	
	/*******************************************
	 * Loads a parameter file and populates <code>parameters</code> with it. Returns <code>true</code> if
	 * successful. File must be of the form [parameter_name]\t[parameter_value].
	 * 
	 * @param file
	 * @return
	 */
	protected boolean load_parameter_file(String file){
		
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			
			while (line != null){
				String[] parts = line.split("\t");
				parameters.put(parts[0], parts[1]);
				line = reader.readLine();
				}
			
			reader.close();
			log("Read parameters from '" + file + "'.");
			return true;
			
		}catch (Exception e){
			log(e.getLocalizedMessage());
			return false;
			}
		
	}
	
	/******************************************************
	 * Runs a single command with the current parameters.
	 * 
	 * @param command
	 */
	protected abstract boolean run_command(String command);
	
	/******************************************************
	 * Reads and outputs a help file from the given URL
	 * 
	 * @param resource
	 * @return
	 */
	protected boolean output_help(URL resource){
		
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream()));
		
			String line = reader.readLine();
			while (line != null){
				InterfaceSession.log(line, LoggingType.Concise, false);
				line = reader.readLine();
				}
		
			reader.close();
			return true;
		}catch (IOException ex){
			InterfaceSession.log("Error reading help file: " + resource.toExternalForm(), 
								 LoggingType.Errors);
			return false;
			}
		
	}
	
}