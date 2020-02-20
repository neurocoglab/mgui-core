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

package mgui.interfaces.gui;

import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.io.InterfaceFile;

/**************************************************
 * A button which contains a file and file filter, and displays a file browser dialog when pressed
 * 
 * @author Andrew Reid
 *
 */
public class FileButton extends JButton {

	protected InterfaceFile current_file;
	protected String external_action_command = "";
	//protected ArrayList<ActionListener> external_listeners = new ArrayList<ActionListener>();
	
	public FileButton(){
		super();
		init();
	}
	
	public FileButton(InterfaceFile file){
		super();
		this.current_file = file;
		init();
	}
	
	public FileButton(File file, FileFilter filter){
		super();
		current_file = new InterfaceFile(file, filter);
	}
	
	protected void init(){
		
		this.setText("Browse..");
		
	}
	
	public void setInterfaceFile(InterfaceFile file){
		this.current_file = file;
	}
	
	public InterfaceFile getInterfaceFile(){
		return this.current_file;
	}
	
	public File getCurrentFile(){
		return current_file.getFile();
	}
	
	public void setCurrentFile(File file){
		current_file.setFile(file);
	}
	
	public void setFileFilter(FileFilter filter){
		current_file.setFileFilter(filter);
	}
	
	public void showChooser(){
		
		JFileChooser chooser = null;
		if (current_file == null)
			chooser = new JFileChooser();
		else
			chooser = new JFileChooser(current_file.getFile());
		
		
		chooser.setMultiSelectionEnabled(false);
		if (current_file.getFileFilter() != null){
			chooser.setFileFilter(current_file.getFileFilter());
			if (current_file.getFileFilter().getDescription().equals("Directories"))
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			}
		chooser.showOpenDialog(InterfaceSession.getSessionFrame());
		if (chooser.getSelectedFile() != null)
			current_file.setFile(chooser.getSelectedFile());
		
	}
	
	/*
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals("Pressed")){
			showChooser();
			return;
			}
		
		for (int i = 0; i < external_listeners.size(); i++)
			external_listeners.get(i).actionPerformed(e);
		
	}
	*/

	
	
	
}