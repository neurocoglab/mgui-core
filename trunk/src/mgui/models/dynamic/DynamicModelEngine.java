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

package mgui.models.dynamic;

import java.util.ArrayList;
import java.util.Collections;

import mgui.interfaces.InterfaceSession;
import mgui.util.IDFactory;


/***************************************
 * The main engine class for coordinating a dynamic model. DynamicModelEngine has the
 * following main functions:
 * 
 * <ul>
 * <li>Sending time-step signals to all dynamic components in a dynamic model
 * <li>Update a model environment at every time-step
 * <li>Handle discrete causal events fired by dynamic components
 * </ul>
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */

public class DynamicModelEngine implements DynamicModelListener {
	
	//List of dynamic components
	ArrayList<DynamicModelComponent> components = new ArrayList<DynamicModelComponent>();
	DynamicModel model;
	public double clock;
	public int iter;
	
	//Time step
	public double timeStep;
	
	//ID Factory
	protected IDFactory idFactory = new IDFactory();
	
	int verbose = 0;		//no console output
	//int verbose = 1;		//output events to console
	
	public DynamicModelEngine(double ts){
		setTimeStep(ts);
	}
	
	public DynamicModelEngine(double ts, DynamicModel model){
		setTimeStep(ts);
		setModel(model);
	}
	
	public void setTimeStep(double ts){
		timeStep = ts;
	}
	
	public DynamicModel getModel(){
		return model;
	}
	
	/**************************
	 * Executes the model for <iters> iterations.
	 *
	 */
	public void executeModel(int iters) throws DynamicModelException{
		
		if (model == null) throw new DynamicModelException("No model to execute!");
		
		for (int i = 0; i < iters; i++){
			
			//Note that events will execute based only upon whether the elapsed
			//time exceeds their latency time. Events with shorter latencies will
			//therefore not necessarily execute prior to those with longer
			//latencies. To ensure the proper sequence is followed requires a
			//sufficiently short time step.
			//TODO implement a latency sort on all event stacks?
			
			//1. execute/elapse all events on all components
			for (int c = 0; c < components.size(); c++)
				components.get(c).executeEvents(timeStep);
			
			//2. send time signal to environment
			model.getEnvironment().timeElapsed(timeStep);
				
			//3. send time signal to all components
			for (int c = 0; c < components.size(); c++)
				components.get(c).timeElapsed(timeStep);
				
			clock += timeStep;
			}
		
		iter += iters;
		
	}
	
	/*********************************
	 * Set the model for this engine to execute.
	 * @param model
	 */
	public void setModel(DynamicModel model){
		if (this.model != null)
			this.model.removeListener(this);
		this.model = model;
		components = model.getComponents();
		//set ids for components (this also ensures they are sorted)
		for (int i = 0; i < components.size(); i++)
			components.get(i).setID(idFactory.getID(), false);
		model.fireListeners();
		model.addListener(this);
		reset();
	}
	
	public void reset(){
		iter = 0;
		clock = 0;
		model.reset();
	}
	
	public int getIterations(){
		return iter;
	}
	
	public double getClock(){
		return clock;
	}
	
	/*************************
	 * Add <code>c</code> to this engine. Note that adding a component to an engine assigns it a new
	 * ID; thus, it is a very bad idea to attach the same model to more than one engine.
	 * If component is already in list, does nothing. Note this method requires two binary
	 * search operations; if adding an entire model, use <code>setModel()</code>.
	 * <p>TODO enforce this restriction
	 * <p>TODO consider making this protected, so that components can only be added via the
	 * setModel and componentAdded methods...
	 * @param c DynamicModelComponent to add
	 */
	public void addComponent(DynamicModelComponent c){
		//if already in list, do nothing
		int i = seek(c);
		if (i >= 0 && components.get(i).equals(c)) return;
		c.setID(idFactory.getID());
		i = seek(c);
		//TODO throw exception, but shouldn't get here
		while (i >= 0){
			InterfaceSession.log("ID exists: " + c.getID());
			c.setID(idFactory.getID());
			i = seek(c);
			}
		i = -i - 1;
		components.add(i, c);
	}
	
	public void removeComponent(DynamicModelComponent c){
		components.remove(c);
	}
	
	public void componentAdded(DynamicModelComponent c){
		addComponent(c);
	}
	
	public void componentRemoved(DynamicModelComponent c){
		addComponent(c);
	}
	
	/******************************
	 * Returns the index of component c. Same rules as for Collections.binarySearch
	 * @param c
	 * @return
	 */
	public int seek(DynamicModelComponent c){
		return Collections.binarySearch(components, c);
	}

}