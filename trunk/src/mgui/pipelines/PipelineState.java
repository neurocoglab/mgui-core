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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.io.util.ParallelOutputStream;
import foxtrot.WorkerThread;

/****************************
 * Represents the state of a pipeline.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PipelineState{
	
	public String output_file;
	public String root_dir;
	public String input_file;
	public String temp_input = "temp.0", temp_output = "temp.1";
	
	public int temp_count = 0;
	public boolean fail_on_exception = false;
	
	public boolean taskInterrupted;
	public InterfacePipeline pipeline;
	
	private boolean task_failed = false;
	protected WorkerThread current_worker_thread;
	
	public Throwable exception;
	
	/**************************************
	 * Construct a new pipeline state for the given pipeline.
	 * 
	 * @param pipeline
	 */
	public PipelineState(InterfacePipeline pipeline){
		
		this.pipeline = pipeline;
		
	}
	
	public void reset(){
		input_file = null;
		output_file = null;
		temp_count = 0;
		taskInterrupted = false;
	}
	
	public boolean stopExecution(){
		setTaskInterrupted(true);
		return true;
	}
	
	public void setCurrentWorkerThread(WorkerThread thread){
		this.current_worker_thread = thread;
	}
	
	public String getTempInputPath(){
		String path = root_dir  + File.separator + temp_input;
		//if (path.contains(" ")) path = "\"" + path + "\"";
		return path;
	}
	
	public String getTempOutputPath(){
		String path = root_dir  + File.separator + temp_output;
		//if (path.contains(" ")) path = "\"" + path + "\"";
		return path;
	}
	
	public boolean deleteTempFiles(){
		
		File temp_in = new File(root_dir + File.separator + temp_input);
		if (temp_in.exists() && !temp_in.delete()){
			InterfaceSession.log("Cannot delete temp file '" + temp_in.getAbsolutePath() +"'");
			return false;
			}
		File temp_out = new File(root_dir + File.separator + temp_output);
		if (temp_out.exists() && !temp_out.delete()){
			InterfaceSession.log("Cannot delete temp file '" + temp_out.getAbsolutePath() +"'");
			return false;
			}
		
		return true;
	 }
	
	public boolean isTaskInterrupted(){
		return taskInterrupted;
	}
	
	public boolean isTaskFailed(){
		return task_failed;
	}

	public void setTaskInterrupted(boolean value){
	      taskInterrupted = value;
	}
	
	public void setTaskFailed(boolean value){
	      task_failed = value;
	}
	
	public void exceptionEncountered(Throwable exception) {
		 if (!fail_on_exception) return;
		 InterfaceSession.log("Pipeline: Exception encountered...", LoggingType.Errors);
		 this.exception = exception;
		 setTaskFailed(true);
		 setTaskInterrupted(true);
	 }
	
	
	public class ErrorStreamListener implements Runnable {
		 
		PipedInputStream error_input_stream;
		PipedOutputStream error_output_stream;
		Thread error_reader;
		boolean quit = false;
		
		PipelineState state;
		
		public ErrorStreamListener(PipelineState state){
			this.state = state;
			error_input_stream = new PipedInputStream();
			try{
				error_output_stream = new PipedOutputStream(error_input_stream);
				ParallelOutputStream out_stream = InterfaceEnvironment.getSystemErrorStream();
				out_stream.addStream(error_output_stream);
			}catch (IOException e){
				//e.printStackTrace();
				InterfaceSession.handleException(e);
				}
		}
		
		public void start(){
			error_reader = new Thread(this);
			error_reader.setDaemon(true);	
			error_reader.start();
		}
		
		public synchronized void run(){
			try {
				while (Thread.currentThread() == error_reader){
					try {
						this.wait(100);
					}catch(InterruptedException ie) {}
					
					if (error_input_stream.available() != 0){
						state.exceptionEncountered(null);
						return;
						}
					if (quit) return;
					}
				
			}catch (Exception e){
				state.exceptionEncountered(e);
				return;
				}
		}
		
		public void close(){
			quit = true;
			if (error_output_stream != null){
				try{
					error_output_stream.close();
					InterfaceEnvironment.getSystemErrorStream().removeStream(error_output_stream);
				}catch (IOException e){
					e.printStackTrace();
					}
				}
		}
		 
	 }
	
}