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

package mgui.interfaces;

/***********************
 * Interface specifying methods to enable progress updates. Allows specifies a method (<ocde>isCancelled</code> 
 * for checking whether user has cancelled a procedure. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public interface ProgressUpdater {
	
	public static enum Mode{
		Determinate,
		Indeterminate;
	}
	
	/*********************************
	 * Increment progress by 1
	 */
	public void iterate();
	
	/*********************************
	 * Uodates the value of this progress indicator
	 * 
	 * @param t
	 */
	public void update(int t);
	public void setMinimum(int min);
	public void setMaximum(int max);
	public int getMinimum();
	public int getMaximum();
	
	/*******************************
	 * Cancels execution of the process monitored by this updater. The process must
	 * respect the cancel notification for this call to have effect.
	 * 
	 */
	public void cancel();
	
	/********************
	 * Indicates whether the monitored process has been cancelled
	 * 
	 * @return
	 */
	public boolean isCancelled();
	public void setMode(Mode mode);
	public void reset();
	
	/********************************
	 * Registers this progress updater with the ModelGUI session
	 * 
	 */
	public void register();
	
	/*******************************
	 * Deregisters this progress updater with the ModelGUI session
	 * 
	 */
	public void deregister();
	public void setMessage(String message);
	public void setIndeterminate(boolean b);
	
	/****************************************************
	 * Allow processes to change the settings of this progress bar? Implementing classes can choose whether or
	 * not to enforce this.
	 * 
	 * @return
	 */
	public boolean allowChanges();
	
}