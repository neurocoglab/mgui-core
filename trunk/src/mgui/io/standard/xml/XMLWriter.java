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

package mgui.io.standard.xml;

import java.io.BufferedWriter;
import java.io.IOException;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.FileWriter;
import foxtrot.Job;
import foxtrot.Worker;

/********************************************
 * Writes an XML object (i.e., any instance of {@link XMLObject}). Provides generic implementations which
 * may be overridden by subclasses if necessary. The XML type determines how the object (and any member
 * objects) will be written. This gives the option to either write an object fully to XML, or as a reference to
 * a URL at which the data is stored (a much better option for large objects which are better stored as
 * binary or compressed files, and for which a loader exists). The possible XML types are:
 * 
 * <ol>
 * <li>Normal: 		Objects are written by reference if possible, fully otherwise
 * <li>Full: 		Objects are fully written
 * <li>Reference:	Objects are written by reference if possible, short otherwise
 * <li>Short:		Objects are written in short form
 * </ol>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public abstract class XMLWriter extends FileWriter {

	/*****************************************
	 * Writes an XML object given <code>options</code>. 
	 * 
	 * @param options
	 * @param progress_bar
	 * @return
	 * @throws IOException
	 */
	public boolean writeObject(final XMLOutputOptions options, final ProgressUpdater progress_bar) throws IOException {
		
		if (dataFile == null) 
			throw new IOException("XMLWriter: no output file set!");
		
		if (dataFile.exists() && !dataFile.delete())
			throw new IOException("XMLWriter: cannot delete existing file '" + dataFile.getAbsolutePath() + "'");
		
		if (!dataFile.createNewFile())
			throw new IOException("XMLWriter: cannot create output file '" + dataFile.getAbsolutePath() + "'");
		if (progress_bar == null){
			writeXMLObject(options, null);
			return true;
		}else{
			return (Boolean)Worker.post(new Job(){
				@Override
				public Boolean run(){
					try{
						writeXMLObject(options, progress_bar);
						return true;
					}catch (Exception e){
						InterfaceSession.handleException(e);
						return false;
						}
					}
			});
		}
		
	}
	
	/*********************************************************
	 * Write the object specified in {@code options}.
	 * 
	 * @param options
	 * @param progress
	 * @throws IOException
	 */
	protected void writeXMLObject(XMLOutputOptions options, ProgressUpdater progress) throws IOException{
		
		//short and sweet
		XMLObject object = options.object;
		BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(dataFile));
		writer.write(XMLFunctions.getXMLHeader() + "\n\n");
		object.writeXML(0, writer, options, progress);
		writer.close();
		
	}
	
}