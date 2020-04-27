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

package mgui.io.util;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileFilter;

/************************************************************
 * File filter allowing wildcard characters..
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class WildcardFileFilter extends FileFilter {

	String description = "";
	boolean accept_directories = true;
	ArrayList<Pattern> filters = new ArrayList<Pattern>();
	
	public WildcardFileFilter(String pattern, String description){
		this(new String[]{pattern}, description);
	}
	
	public WildcardFileFilter(String[] patterns, String description){
		this.description = description;
		
		for (int j = 0; j < patterns.length; j++){
			StringBuffer buffer = new StringBuffer();
		    char [] chars = patterns[j].toCharArray();
		 
			for (int i = 0; i < chars.length; ++i){
		        if (chars[i] == '*')
		            buffer.append(".*");
		        else if (chars[i] == '?')
		            buffer.append(".");
		        else if ("+()^$.{}[]|\\".indexOf(chars[i]) != -1)
		            buffer.append('\\').append(chars[i]);
		        else
		            buffer.append(chars[i]);
		    	}
	 
			filters.add(Pattern.compile(buffer.toString()));
		  }
	}
	
	public ArrayList<String> getPatterns(){
		ArrayList<String> patterns = new ArrayList<String>(filters.size());
		for (int i = 0; i < filters.size(); i++)
			patterns.add(filters.get(i).toString());
		return patterns;
	}
	
	@Override
	public boolean accept(File file) {
		if (accept_directories && file.isDirectory()) return true;
		for (int i = 0; i < filters.size(); i++){
			Pattern pattern = filters.get(i);
			String name = file.getName();
//			String ext = "";
//			int a = file.getName().lastIndexOf(".");
//			if (a > 0)
//				ext = file.getName().substring(a);
			Matcher matcher = pattern.matcher(name);
		    if (matcher.matches()) return true;
			}
		return false;
	}

	@Override
	public String getDescription() {
		return description;
	}

}