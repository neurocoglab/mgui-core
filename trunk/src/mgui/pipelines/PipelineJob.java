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

public class PipelineJob implements Runnable {

	//public boolean is_native;
	public PipelineProcessInstance process;
	public boolean success;
	public String name = "";
	
	public void run() {
	
		try{
			launch();
		}catch (PipelineException e){
			e.printStackTrace();
			success = false;
			}
		
		/*
		try{
			if (is_native)
				success = launchNative();
			else
				success = launchJava();
		}catch (PipelineException e){
			e.printStackTrace();
			success = false;
			}
		*/
		
	}
	
	protected boolean launch() throws PipelineException {
		if (process == null) throw new PipelineException("No process set for job '" + name + "'.");
		return process.launch();
		
	}
	
	/*
	protected boolean launchNative() throws PipelineException {
		//TODO: implement me
		
		return false;
	}
	
	protected boolean launchJava() throws PipelineException {
		
		try{
			Class[] argTypes = new Class[1];
			argTypes[0] = String[].class;
			String[] args = process.getArguments();
			process.process.updateLogger();
			
			Method main_method = Class.forName(process.process.main_class).
											   getDeclaredMethod("main", argTypes);
			
			if (main_method == null)
				throw new PipelineException("Exception running process '" + process.getName() + "' (no main method).");
				
			main_method.invoke(null, new Object[]{args});
			
			return true;
			
		}catch (Exception e){
			e.printStackTrace();
			return false;
			}
		
	}
	*/

}