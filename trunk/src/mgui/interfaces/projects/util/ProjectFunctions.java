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

package mgui.interfaces.projects.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.Utility;
import mgui.interfaces.logs.LoggingType;
import mgui.io.util.IoFunctions;
import mgui.util.StringFunctions;

/*****************************************************
 * Utilities for {@linkplain InterfaceProject} objects.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ProjectFunctions extends Utility {

	
	/********************************************************
	 * Consolidates the contents of a set of directories into a single directory. Can optionally
	 * modify top-level subdirectory names to reflect their original directory. Parameters are
	 * a key-value map of {@code Object}s; as follows:
	 * 
	 * <p>Parameters:
	 * 
	 * <ul>
	 * <li>directories 			Type {@code ArrayList<String>}: The directories to consolidate
	 * <li>target_dir 			Type {@code String}: The directory into which to move/copy the contents
	 * <li>retain_original 		No type: If this key exists, the original data will not be deleted; 
	 * 							otherwise it will.
	 * <li>clobber 				No type: If this key exists, any existing files at target locations will
	 * 							be overwritten; otherwise they will be skipped. Not that if the original
	 * 							data is not being retained, this will result in the loss of that data.
	 * <li>prefix 				[Optional] Type String: If specified, this string will be inserted into
	 * 							the top-level subdirectory names; use "{dir}" to insert the name of the
	 * 							containing directory.
	 * <li>suffix 				[Optional] Type String: If specified, this string will be appended after
	 * 							the top-level subdirectory names; use "{dir}" to insert the name of the
	 * 							containing directory.
	 * </ul>
	 * 
	 * @param parameters
	 * @return
	 */
	public static boolean consolidateDirectories(HashMap<String,Object> parameters){
		return consolidateDirectories(parameters, null);
	}
	
	/********************************************************
	 * Consolidates the contents of a set of directories into a single directory. Can optionally
	 * modify top-level subdirectory names to reflect their original directory. Parameters are
	 * a key-value map of {@code Object}s; as follows:
	 * 
	 * <p>Parameters:
	 * 
	 * <ul>
	 * <li>directories 			Type {@code ArrayList<String>}: The directories to consolidate
	 * <li>target_dir 			Type {@code String}: The directory into which to move/copy the contents
	 * <li>retain_original 		No type: If this key exists, the original data will not be deleted; 
	 * 							otherwise it will.
	 * <li>clobber 				No type: If this key exists, any existing files at target locations will
	 * 							be overwritten; otherwise they will be skipped. Not that if the original
	 * 							data is not being retained, this will result in the loss of that data.
	 * <li>prefix 				[Optional] Type String: If specified, this string will be inserted into
	 * 							the top-level subdirectory names; use "{dir}" to insert the name of the
	 * 							containing directory.
	 * <li>suffix 				[Optional] Type String: If specified, this string will be appended after
	 * 							the top-level subdirectory names; use "{dir}" to insert the name of the
	 * 							containing directory.
	 * </ul>
	 * 
	 * @param parameters
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean consolidateDirectories(HashMap<String,Object> parameters, ProgressUpdater progress){
		
		ArrayList<String> directories = (ArrayList<String>)parameters.get("directories");
		if (directories == null){
			InterfaceSession.log("consolidateDirectories: Parameter 'directories' is not optional.",
					 				LoggingType.Errors);
			return false;
			}
		
		String _param = (String)parameters.get("target_dir");
		if (_param == null){
			InterfaceSession.log("consolidateDirectories: Parameter 'target_dir' is not optional.",
					 				LoggingType.Errors);
			return false;
			}
		
		File target_dir = new File(_param);
		if (!target_dir.exists() || !target_dir.isDirectory()){
			InterfaceSession.log("consolidateDirectories: Target location '" + target_dir.getAbsolutePath() + 
								 "' does not exist or is not a directory.",
								 LoggingType.Errors);
			return false;
			}
		
		boolean retain_original = parameters.containsKey("retain_original");
		boolean clobber = parameters.containsKey("clobber");
		
		String prefix = (String)parameters.get("top_prefix");
		if (prefix == null) prefix = "";
		String suffix = (String)parameters.get("top_suffix");
		if (suffix == null) suffix = "";
		
		if (progress != null){
			progress.setMinimum(0);
			int count = 0;
			for (int i = 0; i < directories.size(); i++)
				count += IoFunctions.getFileCount(new File(directories.get(i)));
			progress.setMaximum(count);
			System.out.println("File count: " + count);
			progress.update(0);
			}
		
		boolean success = true;
		int success_count = 0;
		for (int i = 0; i < directories.size(); i++){
			File this_dir = new File(directories.get(i));
			if (!this_dir.exists() || !this_dir.isDirectory()){
				InterfaceSession.log("consolidateDirectories: Source location '" + this_dir.getAbsolutePath() + 
									 "' does not exist or is not a directory.",
									 LoggingType.Errors);
				return false;
				}
			
			String _prefix = StringFunctions.replaceAll(prefix, "{dir}", this_dir.getName());
			String _suffix = StringFunctions.replaceAll(suffix, "{dir}", this_dir.getName());
			
			boolean ok = false;
			try{
				if (retain_original)
					ok = copyDirContents(this_dir, target_dir, true, progress, _prefix, _suffix, clobber);
				else
					ok = moveDirContents(this_dir, target_dir, true, progress, _prefix, _suffix, clobber);
			}catch (IOException ex){
				ok = false;
				InterfaceSession.log("consolidateDirectories: IO Exception: " + ex.getMessage(),
									 LoggingType.Errors);
				}
			
			if (ok){
				InterfaceSession.log("consolidateDirectories: Copied '" + this_dir.getName() + "' -> '" + target_dir.getName() + "'.",
									 LoggingType.Verbose);
				success_count++;
			}else{
				if (progress != null && progress.isCancelled()){
					InterfaceSession.log("consolidateDirectories: Operation cancelled by user.",
							 LoggingType.Errors);
					return false;
					}
				InterfaceSession.log("consolidateDirectories: Failed to copy '" + this_dir.getName() + "' -> '" + target_dir.getName() + "'.",
									 LoggingType.Errors);
				}
			
			success &= ok;
			
			}
		
		if (success)
			InterfaceSession.log("consolidateDirectories: Done; " + success_count + " successes. 0 failures.",
								 LoggingType.Verbose);
		else
			InterfaceSession.log("consolidateDirectories: Done; " + success_count + " successes. " + 
								 (directories.size() - success_count) + " failures.",
								 LoggingType.Errors);
		
		return success;
	}
	
	
	// Modified from IoFunctions to add prefix & suffix...
	private static boolean copyDirContents(File dir, File dest, boolean recurse, ProgressUpdater progress, String prefix, String suffix,
			boolean clobber) throws IOException {
		
		if (!dir.exists() || !dir.isDirectory() || 
			!dest.isDirectory() || !dest.isDirectory())
			return false;
		
		//copy files and subdirectories
		String[] contents = dir.list();
		
		for (int i = 0; i < contents.length; i++){
			File this_file = new File(dir.getAbsolutePath() + File.separator + contents[i]);
			if (this_file.isDirectory()){
				if (recurse){ 
					if (!copyDir(this_file, dest, true, progress, prefix, suffix, clobber))
						return false;
					}
			}else{
				File new_file = new File(dest.getAbsolutePath() + File.separator + contents[i]);
				if (!new_file.exists() || clobber){
					if (!IoFunctions.copyFile(this_file, new_file))
						return false;
					if (progress != null){
						if (progress.isCancelled()){
							InterfaceSession.log("IoFunctions.copyDirContents: Cancelled by user.", LoggingType.Warnings);
							return false;
							}
						progress.iterate();
						}
					}
				}
			}
		
		return true;
		
	}
	
	private static boolean copyDir(File dir, File dest, boolean recurse, ProgressUpdater progress, String prefix, String suffix,
			boolean clobber) throws IOException {
		
		if (!dir.exists() || !dir.isDirectory() || 
			!dest.isDirectory() || !dest.isDirectory())
			return false;
		
		//copy files and subdirectories
		String[] contents = dir.list();
		
		//if necessary, find template file and substitute parts
		boolean replace = false;
		for (int i = 1; i < 10 && !replace; i++)
			replace = (prefix.contains("{" + i + "}") ||
					   suffix.contains("{" + i + "}"));
		if (replace){
			String template = null;
			for (int i = 0; i < contents.length && template == null; i++){
				String file = contents[i];
				File _file = new File(file);
				if (!_file.isDirectory()){
					if (file.contains("_")) template = file;
					}
				}
			if (template != null){
				String[] parts = template.split("_");
				for (int i = 1; i < parts.length; i++){
					prefix = StringFunctions.replaceAll(prefix, "{" + i + "}", parts[i - 1]);
					suffix = StringFunctions.replaceAll(suffix, "{" + i + "}", parts[i - 1]);
					}
				}
			}
		
		//create new dir
		File new_dir = new File(dest.getAbsolutePath() + File.separator + prefix + dir.getName() + suffix);
		new_dir.mkdir();
		
		for (int i = 0; i < contents.length; i++){
			File this_file = new File(dir.getAbsolutePath() + File.separator + contents[i]);
			if (this_file.isDirectory()){
				if (recurse && !IoFunctions.copyDir(this_file, new_dir, true, progress))
					return false;
			}else{
				File new_file = new File(new_dir.getAbsolutePath() + File.separator + contents[i]);
				if (!IoFunctions.copyFile(this_file, new_file))
					return false;
				if (progress != null){
					if (progress.isCancelled()){
						InterfaceSession.log("IoFunctions.copyDirContents: Cancelled by user.", LoggingType.Warnings);
						return false;
						}
					progress.iterate();
					}
				}
			}
		
		return true;
		
	}
	
	private static boolean moveDirContents(File dir, File dest, boolean recurse, ProgressUpdater progress, String prefix, String suffix,
			boolean clobber) throws IOException {
		
		if (!copyDirContents(dir, dest, recurse, progress, prefix, suffix, clobber)) return false;
		
		String[] contents = dir.list();
				
        for (int i=0; i < contents.length; i++) {
        	String name = contents[i];
        	File file = new File(dir, name);
            if (file.isDirectory()){
            	if (recurse && !IoFunctions.deleteDir(file))
            		return false;
            }else{
            	if (!file.delete())
            		return false;
            	}
            }
		
		return dir.delete();
		
	}
	
}