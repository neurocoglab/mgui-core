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

package mgui.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.Utility;
import mgui.interfaces.logs.LoggingType;
import mgui.numbers.MguiBoolean;

/********************************************************
 * Utility class providing functions to interact with the operating system.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class CommandFunctions extends Utility{
	
	static boolean is_init = false;
	
	public static enum ProcessState{
		NotStarted,
		Running,
		TerminatedNormally,
		TerminatedWithError,
		Interrupted,
		Waiting;
	}
	
	static String getOSPrefix(){
		switch(InterfaceEnvironment.getOsType()){
			case WindowsXP:
				return "cmd.exe /C ";
			case Linux:
				return ""; ///bin/bash -c ";
			}
		
		return "";
	}
	
	/*************************************************
	 * Makes a call to the command console and returns immediately
	 * 
	 * TODO: allow an ESC interrupt
	 * 
	 * @param command
	 * @throws IOException
	 */
	//public static synchronized ProcessState call(String command) throws IOException{
	public static Process call(String command) throws Exception{
		return call("no-name", command);
	}
	
	/*************************************************
	 * Makes a call to the command console and returns immediately.
	 * 
	 * TODO: allow an ESC interrupt
	 * 
	 * @param command
	 * @param name
	 */
	//public static synchronized ProcessState call(String name, String command, long timeout) throws IOException{
	public static Process call(String name, String command) throws Exception{
		return call(name, command, null);
	}
	
	/*************************************************
	 * Makes a call to the command console and returns immediately.
	 * 
	 * TODO: allow an ESC interrupt
	 * 
	 * @param command
	 * @param name
	 * @param environment
	 */
	public static Process call(String name, String command, HashMap<String, String> environment) throws Exception{
		
		try{
			String[] asarray = command.split(" ");
			ArrayList<String> args = new ArrayList<String>(asarray.length);
			for (int i = 0; i < asarray.length; i++){
				if (i == 0)
					args.add(getOSPrefix() + asarray[i]);
				else
					args.add(asarray[i]);
				}
				
			File script = File.createTempFile("temp", "");
			FileWriter writer = new FileWriter(script);
			writer.write(getOSPrefix() + " " + command);
			writer.close();
			Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
	        //add owners permission
	        perms.add(PosixFilePermission.OWNER_READ);
	        perms.add(PosixFilePermission.OWNER_WRITE);
	        perms.add(PosixFilePermission.OWNER_EXECUTE);
			Files.setPosixFilePermissions(script.toPath(), perms);
				
			ProcessBuilder pb = new ProcessBuilder(new String[]{script.getAbsolutePath()});
			Map<String, String> env = pb.environment();
			if (environment != null)
				env.putAll(environment);
			
			Process process = pb.start();
						
			StreamGobbler errorGobbler = new 
	        StreamGobbler(process.getErrorStream(), "ERROR");            
	     
			// any output?
	    	StreamGobbler outputGobbler = new 
	        StreamGobbler(process.getInputStream(), "OUTPUT");
	         
	    	// kick them off
	    	errorGobbler.start();
	    	outputGobbler.start();
	                             
	    	return process;
	    	
		}catch (Exception e){
			Throwable cause = e.getCause();
			if (cause == null)
				cause = e;
			InterfaceSession.log("CommandFunctions: Exception while executing call to process '" +
								 command + "'.\nDetails [" + cause.getClass().getCanonicalName() + "]: " + cause.getMessage(), 
								 LoggingType.Errors);
			InterfaceSession.handleException(e);
			throw new Exception(cause); 	
			}
		
	}
	
	/************************************************
	 * Calls {@code command} and blocks until it is complete
	 * 
	 * @param command
	 * @return
	 * @throws IOException
	 */
	public static ProcessState callAndWait(String command) throws Exception{
		
		return callAndWait("no-name", command, -1);
		
	}
	
	/************************************************
	 * Calls {@code command} and blocks until it is complete
	 * 
	 * @param name
	 * @param command
	 * @param timeout
	 * @return
	 * @throws IOException
	 */
	public static ProcessState callAndWait(String name, String command) throws Exception{
		
		return callAndWait(name,command,-1);
		
	}
	
	/***************************************************************
	 * Calls {@code command} and blocks until it is complete
	 * 
	 * @param name
	 * @param command
	 * @param timeout
	 * @return
	 * @throws IOException
	 */
	public static ProcessState callAndWait(String name, String command, long timeout) throws Exception{
		return callAndWait(name, command, timeout, null);
		
	}
	
	/***************************************************************
	 * Calls {@code command} and blocks until it is complete
	 * 
	 * @param name
	 * @param command
	 * @param timeout
	 * @param environment
	 * @return
	 * @throws IOException
	 */
	public static ProcessState callAndWait(String name, String command, long timeout, HashMap<String, String> environment) throws Exception{
		return callAndWait(name, command, timeout, environment, false);
	}
	
	
	/***************************************************************
	 * Calls {@code command} and blocks until it is complete
	 * 
	 * @param name
	 * @param command
	 * @param timeout
	 * @param environment
	 * @return
	 * @throws IOException
	 */
	public static ProcessState callAndWait(String name, String command, long timeout, HashMap<String, String> environment, boolean fail_on_err) throws Exception{
		
		try{
			String[] asarray = command.split(" ");
			ArrayList<String> args = new ArrayList<String>(asarray.length);
			for (int i = 0; i < asarray.length; i++){
				if (i == 0)
					args.add(getOSPrefix() + asarray[i]);
				else
					args.add(asarray[i]);
				}
			
			File script = File.createTempFile("temp", "");
			FileWriter writer = new FileWriter(script);
			writer.write(getOSPrefix() + " " + command);
			writer.close();
			Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
	        //add owners permission
	        perms.add(PosixFilePermission.OWNER_READ);
	        perms.add(PosixFilePermission.OWNER_WRITE);
	        perms.add(PosixFilePermission.OWNER_EXECUTE);
			Files.setPosixFilePermissions(script.toPath(), perms);
				
			ProcessBuilder pb = new ProcessBuilder(new String[]{script.getAbsolutePath()});
			Map<String, String> env = pb.environment();
			if (environment != null)
				env.putAll(environment);
			
			final Process process = pb.start();
			
			if (process == null){
				script.delete();
				return ProcessState.TerminatedWithError;
			}
						
			StreamGobbler errorGobbler = new 
	        StreamGobbler(process.getErrorStream(), "ERROR");            
	     
			// any output?
	    	StreamGobbler outputGobbler = new 
	        StreamGobbler(process.getInputStream(), "OUTPUT");
	         
	    	// kick them off
	    	errorGobbler.start();
	    	outputGobbler.start();
	    	
	    	final MguiBoolean was_killed = new MguiBoolean(false);
	    	
	    	// This will kill the process if the error stream encounters input
	    	if (fail_on_err){
	    		errorGobbler.addStreamListener(new StreamListener(){
					@Override
					public void streamUpdated(EventObject event) {
						was_killed.setTrue(true);
						process.destroy();
					}
	    		});
	    		}
	        
	    	// Sleep for 50 ms to give error stream time to process
	    	Thread.sleep(50);
			int exitVal = process.waitFor();
			
			script.delete();
			
			if (was_killed.getTrue())
				return ProcessState.TerminatedWithError;
			return getProcessState(exitVal);
		}catch (InterruptedException ex){
			InterfaceSession.log("CommandFunctions: Process '" + name + "' interrupted for command: '" + 
					 command + "'.\nDetails: " + ex.getMessage(), 
					 LoggingType.Debug);
			return ProcessState.TerminatedWithError;
		}catch (Exception e){
			Throwable cause = e.getCause();
			if (cause == null)
				cause = e;
			InterfaceSession.log("CommandFunctions: Exception while executing call to process '" +
								 command + "'.\nDetails [" + cause.getClass().getCanonicalName() + "]: " + cause.getLocalizedMessage(), 
								 LoggingType.Errors);
			InterfaceSession.handleException(e);
			//throw new Exception(cause); 	
			return ProcessState.TerminatedWithError;
			}
		
	}
	
	/**************************************
	 * Returns the {@code ProcessState} corresponding to the system exit value.
	 * 
	 * @param exitVal
	 * @return
	 */
	public static ProcessState getProcessState(int exitVal){
		
		switch (exitVal){
			case -1:
				return ProcessState.TerminatedWithError;
			default:
				return ProcessState.TerminatedNormally;
		}
		
	}
	
	
	static class StreamGobbler extends Thread{
	    InputStream is;
	    String type;
	    
	    ArrayList<StreamListener> listeners = new ArrayList<StreamListener>();
	    
	    StreamGobbler(InputStream is, String type)
	    {
	        this.is = is;
	        this.type = type;
	    }
	    
	    public void addStreamListener(StreamListener listener){
	    	listeners.add(listener);
	    }
	    
	    public void removeStreamListener(StreamListener listener){
	    	listeners.remove(listener);
	    }
	    
	    @Override
		public void run()
	    {
	        try
	        {
	            InputStreamReader isr = new InputStreamReader(is);
	            BufferedReader br = new BufferedReader(isr);
	            String line=br.readLine();
	            boolean has_input = false;
	            while ( line != null ){
	            	has_input = true;
	            	if (type.equals("ERROR")){
	            		InterfaceSession.log(line, LoggingType.Errors);
	            	}else{
	            		InterfaceSession.log(line, LoggingType.Concise);
	            		}
	            	if (!br.ready()) break;
	            	line = br.readLine();
	            	}
	            if (has_input){
	            	for (int i = 0; i < listeners.size(); i++)
	      	        	listeners.get(i).streamUpdated(new EventObject(this));
	            	}
	            } catch (IOException ioe)
	              {
	            	InterfaceSession.log("CommandFunctions: I/O stream error encountered: " + ioe.getMessage(), LoggingType.Errors);
	            	//InterfaceSession.handleException(ioe);
	              }
	      
	    }
	}


}