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

package mgui.pipelines;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.pipelines.libraries.PipelineProcessLibrary;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.util.InterfaceFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.resources.icons.IconObject;

import org.xml.sax.Attributes;

/***************************************************
 * Represents a Java process specification, specified by an executable process (native or Java) and a set of parameters.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class PipelineProcess implements InterfaceObject, 
												 XMLObject,
											  	 Cloneable,
											  	 IconObject,
											  	 Serializable{

	protected String input_parameter = "inputfile";
	protected String output_parameter = "outputfile";
	protected String name;
	protected String logger; 
	protected String help_param = "-help";
	protected ArrayList<TaskParameter> parameters = new ArrayList<TaskParameter>();
	protected PipelineProcessLibrary library;
	
	public void setLibrary(PipelineProcessLibrary library){
		this.library = library;
	}
	
	public PipelineProcessLibrary getLibrary(){
		return this.library;
	}
	
	@Override
	public void clean(){
		InterfaceFunctions.cleanInterfaceObject(this);
	}
	
	public void updateLogger(){
		if (logger == null) return;
		Logger log = Logger.getLogger(logger);
		if (log == null){
			InterfaceSession.log("Logger '" + logger + "' for Process '" + name + "' invalid..");
			return;
			}
		log.setUseParentHandlers(false);
		
		Handler[] handlers = log.getHandlers();
		
		for (int i = 0; i < handlers.length; i++)
			if (handlers[i] instanceof ConsoleHandler) return;
		
		log.addHandler(new ConsoleHandler());
	}
	
	/**********************************
	 * Attempt to display the associated help message
	 * 
	 */
	public abstract void showHelp();
	
	public Icon getObjectIcon(){
		URL imgURL = PipelineProcess.class.getResource("/mgui/resources/icons/pipelines/process_20.png");
		
		if (imgURL == null){
			InterfaceSession.log("PipelineProcess: Cannot find icon at /mgui/resources/icons/pipelines/process_20.png");
			return null;
		}
		
		return new ImageIcon(imgURL);
		
	}
	
	/**************************************
	 * Returns the compound name of this process, of the form [library_name].[compound_name].
	 * 
	 * @return
	 */
	public String getCompoundName(){
		if (library == null)
			return getName();
		return library.getName() + "." + getName();
	}
	
	/**************************************
	 * Gets the parameter which is used to determine the input file for this process.
	 * 
	 * @return
	 */
	public String getInputParameter(){
		return this.input_parameter;
	}
	
	/**************************************
	 * Sets the parameter which is used to determine the input file for this process.
	 * 
	 * @return
	 */
	public void setInputParameter(String parameter){
		this.input_parameter = parameter;
	}
	
	/**************************************
	 * Sets the parameter which is used to determine the output file for this process.
	 * 
	 * @return
	 */
	public String getOutputParameter(){
		return this.output_parameter;
	}
	
	/**************************************
	 * Sets the parameter which is used to determine the output file for this process.
	 * 
	 * @return
	 */
	public void setOutputParameter(String parameter){
		this.output_parameter = parameter;
	}
	
	/**************************************
	 * Gets the parameter which is used to show a help message for this process.
	 * 
	 * @return
	 */
	public String getHelpParameter(){
		return this.help_param;
	}
	
	/**************************************
	 * Sets the parameter which is used to show a help message for this process.
	 * 
	 * @return
	 */
	public void setHelpParameter(String parameter){
		this.help_param = parameter;
	}
	
	/***************************************
	 * Returns the message to be displayed when this process has succeeded.
	 * 
	 * @return
	 */
	public abstract String getSuccessMessage();
	
	/***************************************
	 * Returns the message to be displayed when this process has failed.
	 * 
	 * @return
	 */
	public abstract String getFailureMessage();
	
	/***************************************
	 * Runs an instance of this process
	 * 
	 * @param args Arguments for the process
	 * @param id Unique identifier for the process instance to run
	 * @return
	 * @throws PipelineException
	 */
	public abstract boolean run(String[] args, long uid) throws PipelineException;
	
	/***************************************
	 * Interrupts an instance of this process
	 * 
	 * @param uid The unique identifier of the process instance to interrupt
	 * @return
	 * @throws PipelineException
	 */
	public abstract boolean interrupt(long uid) throws PipelineException;
	
	/***************************************
	 * Alters an argument array to set an input file for this process, and returns the result.
	 * 
	 * @param args
	 * @param input_file
	 * @return
	 */
	public abstract String[] getInputArgs(String args[], String input_file);
	
	/***************************************
	 * Alters an argument array to set an output file for this process, and returns the result.
	 * 
	 * @param args
	 * @param output_file
	 * @return
	 */
	public abstract String[] getOutputArgs(String args[], String output_file);
	
	public static ImageIcon getIcon(){
		URL imgURL = PipelineProcessInstance.class.getResource("/ar/resources/icons/pipelines/process_17.png");
		if (imgURL == null) return null;
		ImageIcon icon = new ImageIcon(imgURL);
		return icon;
	}
	
	public void addParameter(TaskParameter p){
		parameters.add(p);
	}
	
	public void insertParameter(TaskParameter p, int index){
		parameters.add(index, p);
	}
	
	public TaskParameter getParameter(String name){
		for (int i = 0; i < parameters.size(); i++)
			if (parameters.get(i).name.equals(name))
				return parameters.get(i);
		return null;
	}
	
	public boolean removeParameter(String name){
		TaskParameter p = getParameter(name);
		if (p == null) return false;
		return parameters.remove(p);
	}
	
	@Override
	public abstract Object clone();
	
	public PipelineProcessInstance getInstance(int i){
		return new PipelineProcessInstance(this, i);
	}

	@Override
	public String toString(){
		return name;
	}
	
	public void setFromProcess(PipelineProcess process){
		this.name = process.getName();
		this.logger = process.getLogger();
		this.input_parameter = process.getInputParameter();
		this.output_parameter = process.getOutputParameter();
		this.help_param = process.getHelpParameter();
		ArrayList<TaskParameter> params = process.getParameters();
		this.parameters = new ArrayList<TaskParameter>();
		for (int i = 0; i < params.size(); i++){
			this.parameters.add(new TaskParameter(params.get(i)));
			}
		
	}
	
	public String getLogger(){
		return logger;
	}
	
	public String getDTD() {
		return null;
	}

	public String getLocalName() {
		return "Process";
	}

	public String getShortXML(int tab) {
		return null;
	}

	public String getXML() {
		return getXML(0);
	}

	@Override
	public String getXML(int tab) {
		
		
		
		
		return null;
	}

	public String getXMLSchema() {
		
		return null;
	}

	public void handleXMLElementEnd(String localName) {
		
	}

	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		
	}

	public void handleXMLString(String s) {
		
	}
	
	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException {
		this.writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}

	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
		
		writer.write(getXML(tab));
		
	}

	public void writeXML(int tab, Writer writer) throws IOException {
		writeXML(tab, writer, null);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isDestroyed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getTreeLabel() {
		return getName();
	}

	@Override
	public InterfaceTreeNode issueTreeNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTreeNode(InterfaceTreeNode node) {
		// TODO Auto-generated method stub
		
	}
	
	public ArrayList<TaskParameter> getParameters() {
		return parameters;
	}

	public void setParameters(ArrayList<TaskParameter> parameters) {
		this.parameters = parameters;
	}

	public void setLogger(String logger) {
		this.logger = logger;
	}

	
	
}