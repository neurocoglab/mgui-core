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

import java.util.HashMap;

import mgui.command.CommandFunctions.ProcessState;

/**********************************************************
 * Serves as the base class for command-line interpreters which interact with mgui.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class CommandInterpreter {

	protected static long clock_start;
	protected long elapsed;
	protected long instance_clock_start;
	protected boolean success_status = false;
	
	protected ProcessState instance_state = ProcessState.NotStarted;
	
	/*******************************************************
	 * Executes a process, similar to <code>main</code>, but runs as an instance, and returns the elapsed
	 * time in milliseconds. 
	 * 
	 * @param args
	 * @return
	 */
	public ProcessState execute(String[] args) throws Exception{
		return ProcessState.NotStarted;
	}
	
	public boolean getSuccessStatus(){
		return success_status;
	}
	
	protected void setSuccessStatus(boolean status){
		this.success_status = status;
		if (status)
			instance_state = ProcessState.TerminatedNormally;	
		else
			instance_state = ProcessState.TerminatedWithError;
	}
	
	/*******************************************************
	 * Extracts parameters from a list of arguments, such that a parameter is specified by a dash. A parameter
	 * followed by an argument without a dash is assigned that value. This method returns the parameters as a 
	 * hash map, with the parameter names as keys pointing to their values.
	 * 
	 * <p>For instance:
	 * <p>the argument series "-z" "-b" "value" would be placed in the hash map as [["z"->""],["b"->"value"]
	 * 
	 * @param args
	 * @return
	 */
	public static HashMap<String, String> getParameters(String[] args){
		
		HashMap<String, String> params = new HashMap<String, String>();
		
		for (int i = 0; i < args.length; i++){
			if (args[i].startsWith("-") && !isNumeric(args[i])){
				String name = args[i].substring(1);
				if (i + 1 == args.length){
					params.put(name, "");
					return params;
					}
				if (isNumeric(args[i + 1]) || !args[i + 1].startsWith("-")){
					params.put(name, args[i + 1]);
					i++;
				}else{
					params.put(name, "");
					}
				}
			}
		
		return params;	
		
	}
	
	protected static boolean isNumeric(String n){
		try{
			Double.parseDouble(n);
			return true;
		}catch (Exception e){
			return false;
			}
	}
	
	protected static void startTimer(){
		clock_start = System.currentTimeMillis();
	}
	
	protected static long getTimer(){
		return System.currentTimeMillis() - clock_start;
	}
	
	protected void startInstanceTimer(){
		instance_clock_start = System.currentTimeMillis();
		elapsed = 0;
	}
	
	protected long getInstanceTimer(){
		if (elapsed > 0) return elapsed;
		return System.currentTimeMillis() - instance_clock_start;
	}
	
	protected void stopInstanceTimer(){
		elapsed = getInstanceTimer();
	}
	
}