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

package mgui.interfaces.tools.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.tools.Tool;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.interfaces.tools.ToolListener;
import foxtrot.Job;
import foxtrot.Worker;

/**************************************************
 * Abstract class to be inherited by all tools which operate using a modal dialog box.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class DialogTool implements Tool {
	
	protected ArrayList<ToolListener> listeners = new ArrayList<ToolListener>();
	protected Icon icon;
	protected String message;
	public boolean show_progress = true;
	
	protected void init(){
		setIcon();
	}
	
	@Override
	public Tool getPreviousTool(){
		return null;
	}
	
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/tools/tool_3d_30.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/tools/tool_3d_30.png");
	}
	
	public void activate(){
		activate (true);
	}
	
	public void deactivate(){
		
	}
	
	@Override
	public void setTargetPanel(InterfacePanel panel) {
		
	}
	
	public boolean isExclusive(){
		return false;
	}
	
	protected void reportSuccess(boolean success){
		if (success)
			JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), message, getToolTitle(), JOptionPane.INFORMATION_MESSAGE);
		else
			JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), message, getToolTitle(), JOptionPane.ERROR_MESSAGE);
	}
	
	/************************************************
	 * Activate the tool by showing its dialog box.
	 * 
	 * @param report_success
	 */
	public void activate(boolean report_success){
		DialogToolDialogBox dialog = getDialogBox();
		dialog.setLocationRelativeTo(InterfaceSession.getSessionFrame());
		dialog.setVisible(true);
		if (dialog.getOptions() == null) return;			//indicates a cancel by user
		//TODO: set progress bar here and run as SwingWorker task
		boolean success = doTask(dialog.getOptions());
		if (!report_success) return;
		reportSuccess(success);
	}
	
	/*********************************************
	 * Call this to run the task as a worker task.
	 * 
	 * @param options
	 * @return
	 */
	protected boolean doTask(final InterfaceOptions options){
		
		if (!show_progress) return doTaskBlocking(options, null);
		final InterfaceProgressBar progress_bar = new InterfaceProgressBar(getName() + ": ");
		progress_bar.register();
		
		boolean success = (Boolean)Worker.post(new Job(){
			@Override
			public Boolean run(){
				return doTaskBlocking(options, progress_bar);
			}
		});
		
		progress_bar.deregister();
		return success;
	}
	
	protected abstract String getToolTitle();
	protected abstract DialogToolDialogBox getDialogBox();
	
	/****************************************************
	 * Runs this tool's task and blocks until it is complete. To avoid GUI freezing, call {@linkplain doTask}
	 * instead.
	 * 
	 * @param options
	 * @param progress_bar
	 * @return
	 */
	protected abstract boolean doTaskBlocking(InterfaceOptions options, InterfaceProgressBar progress_bar);
	
	public void addListener(ToolListener tl){
		listeners.add(tl);
	}
	
	public void removeListener(ToolListener tl){
		listeners.remove(tl);
	}

	public String getName() {
		return "Unspecified tool";
	}

	public boolean isImmediate() {
		return true;
	}

	public void handleToolEvent(ToolInputEvent e) {
		
	}

	public Icon getObjectIcon() {
		return icon;
	}

	public InterfacePopupMenu getPopupMenu() {
		
		return null;
	}

	public void handlePopupEvent(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	public void showPopupMenu(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public abstract Object clone() throws CloneNotSupportedException;
	
}