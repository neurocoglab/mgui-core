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

package mgui.models.environments;

import java.util.ArrayList;
import java.util.List;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.io.InterfaceDataSource;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.trees.TreeObject;
import mgui.models.dynamic.DynamicModelEnvironment;
import mgui.models.dynamic.DynamicModelEnvironmentListener;
import mgui.models.dynamic.DynamicModelEnvironmentSensor;
import mgui.models.dynamic.DynamicModelException;
import mgui.models.dynamic.DynamicModelOutputEvent;
import mgui.models.dynamic.DynamicModelUpdater;
import mgui.models.updaters.LinearUpdater;
import mgui.models.updaters.SimpleEnvironmentUpdater;
import mgui.numbers.MguiNumber;


/*************************************
 * Represents a simple environment, with a set of named observable scalar state variables. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class SimpleEnvironment<T extends MguiNumber> extends AbstractInterfaceObject 
												   implements DynamicModelEnvironment<T>,
															  InterfaceObject,
															  TreeObject{

	protected double[] inputState = new double[0];			//state of environmental input
	protected double[] observableState = new double[0];		//state of environmental observables
	protected double clock;
	public SimpleEnvironmentUpdater updater;
	public ArrayList<DynamicModelEnvironmentSensor> sensors = new ArrayList<DynamicModelEnvironmentSensor>();
	public boolean updated;
	ArrayList<DynamicModelEnvironmentListener> listeners = new ArrayList<DynamicModelEnvironmentListener>();
	public int mode;
	protected SimpleEnvironmentObservableDataSource<T> observable_data_source;
	protected SimpleEnvironmentInputDataSource<T> input_data_source;
	protected ArrayList<InterfaceTreeNode> treeNodes = new ArrayList<InterfaceTreeNode>();
	protected NameMap observable_names = new NameMap();
	protected NameMap input_names = new NameMap();
	protected boolean isDestroyed = false;
	
	public SimpleEnvironment(){
		this(new LinearUpdater());
	}
	
	/******************************
	 * Creates an instance of SimpleEnvironment with the specified updater.
	 * @param updater
	 */
	public SimpleEnvironment(SimpleEnvironmentUpdater updater){
		try{
			setUpdater(updater);
		}catch (DynamicModelException e){
			e.printStackTrace();
			}
	}
		
	public double getClock(){
		return clock;
	}
	
	public void addListener(DynamicModelEnvironmentListener l){
		listeners.add(l);
	}
	
	public void removeListener(DynamicModelEnvironmentListener l){
		listeners.remove(l);
	}
	
	protected void fireListeners(){
		try{
			for (int i = 0; i < listeners.size(); i++)
				listeners.get(i).environmentUpdated(new SimpleEnvironmentEvent(this));
		}catch (DynamicModelException e){
			e.printStackTrace();
			}
	}
	
	/**@TODO***
	 * Provide multiple updaters.
	 */
	public void setUpdater(DynamicModelUpdater updater) throws DynamicModelException{
		if (!(updater instanceof SimpleEnvironmentUpdater)) throw new DynamicModelException("Updater for SimpleEnvironment must" +
				" be instance of SimpleEnvironmentUpdater.");
		this.updater = (SimpleEnvironmentUpdater)updater;
	}
	
	@Override
	public List<InterfaceDataSource<T>> getDataSources(){
		ArrayList<InterfaceDataSource<T>> sources = new ArrayList<InterfaceDataSource<T>>();
		sources.add(input_data_source);
		sources.add(observable_data_source);
		return sources;
	}
	
	public void setInputState(double[] v){
		inputState = new double[v.length];
		System.arraycopy(v, 0, inputState, 0, v.length);
	}
	
	public double[] getInputState(){
		return inputState;
	}
	
	public double getInputState(int i){
		return inputState[i];
	}
	
	@Override
	public void setInputName(int index, String name){
		input_names.set(index, name);
	}
	
	@Override
	public void setInputNames(List<String> names){
		input_names.clear();
		for (int i = 0; i < names.size(); i++)
			input_names.add(i, names.get(i));
	}
	
	@Override
	public List<String> getInputNames(){
		return new ArrayList<String>(input_names.getNames());
	}
	
	@Override
	public void setObservableName(int index, String name){
		observable_names.set(index, name);
	}
	
	@Override
	public void setObservableNames(NameMap names){
		observable_names.clear();
		observable_names.addAll(names);
	}
	
	@Override
	public ArrayList<String> getObservableNames(){
		return new ArrayList<String>(observable_names.getNames());
	}
	
	@Override
	public void removeObservableName(String name){
		observable_names.remove(name);
	}
	
	public void removeObservableName(int index){
		observable_names.remove(index);
	}
	
	public void setObservableState(double[] values){
		observableState = new double[values.length];
		System.arraycopy(values, 0, observableState, 0, values.length);
		updated = true;
	}
	
	public void setObservableState(int index, double value){
		observableState[index] = value;
	}
	
	public double getObservableState(int i){
		return observableState[i];
	}
	
	public double[] getObservableState(){
		return observableState;
	}

	@Override
	public int getObservableSize() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/********************************
	 * Returns the difference between this environment's current state and the given
	 * values. Calculated as delta[i] = env[i] - comp[i].
	 * 
	 * @param comp
	 * @return
	 */
	public double[] getDeltas(double[] comp){
		int n = Math.min(comp.length, observableState.length);
		double[] deltas = new double[n];
		for (int i = 0; i < n; i++)
			deltas[i] = observableState[i] - comp[i];
		return deltas;
	}
	
	/********************************
	 * Returns the squared difference between this environment's current state and the
	 * given values.
	 * 
	 * @param comp
	 * @return
	 */
	public double[] getSquaredDeltas(double[] comp){
		double[] deltas = getDeltas(comp);
		for (int i = 0; i < deltas.length; i++)
			deltas[i] *= deltas[i];
		return deltas;
	}
	
	public void timeElapsed(double time) {
		//if (!updated || updater == null) return;
		updater.update(this, time);
		fireSensors();
		clock += time;
		updated = false;
	}
	
	public void setMode(int m){
		mode = m;
	}
	
	public int getMode(){
		return mode;
	}
	
	public void reset(){
		clock = 0;
		if (updater != null) updater.reset();
		for (int i = 0; i < sensors.size(); i++)
			sensors.get(i).reset();
		resetInputState();
		resetObservableState();
	}
	
	protected void resetInputState(){
		for (int i = 0 ; i < inputState.length; i++)
			inputState[i] = 0;
	}
	
	protected void resetObservableState(){
		for (int i = 0 ; i < observableState.length; i++)
			observableState[i] = 0;
	}
	
	protected void fireSensors(){
		SimpleEnvironmentEvent e = new SimpleEnvironmentEvent(this);
		for (int i = 0; i < sensors.size(); i++)
			sensors.get(i).stimulate(e);
		//update data sources
		if (observable_data_source != null)
			observable_data_source.stimulate(e);
		if (input_data_source != null)
			input_data_source.stimulate(e);
	}
	
	public void handleOutputEvent(DynamicModelOutputEvent e){
		setObservableState(e.getIndex(), e.getOutput());
	}
	
	@Override
	public void setObservableSize(int s){
		observableState = new double[s];
	}
	
	@Override
	public void setInputSize(int s){
		inputState = new double[s];
		resetInputNames();
	}

	@Override
	public void addSensor(DynamicModelEnvironmentSensor s){
		sensors.add(s);
		setInputSize(sensors.size());
		resetInputDataSource();
	}
	
	@Override
	public void removeSensor(DynamicModelEnvironmentSensor s){
		sensors.remove(s);
		setInputSize(sensors.size());
		resetInputDataSource();
	}
	
	@Override
	public AbstractEnvironmentDataSource<T> getObservableDataSource(){
		if (observable_data_source == null)
			resetObservableDataSource();
		return observable_data_source;
	}
	
	public void resetObservableDataSource(){
		if (observable_data_source == null)
			observable_data_source = new SimpleEnvironmentObservableDataSource<T>();
		observable_data_source.setChannelNames(getObservableNames());
	}
	
	@Override
	public AbstractEnvironmentDataSource<T> getInputDataSource(){
		if (input_data_source == null)
			resetInputDataSource();
		return input_data_source;
	}
	
	public void resetInputDataSource(){
		if (input_data_source == null)
			input_data_source = new SimpleEnvironmentInputDataSource<T>();
		input_data_source.setChannelNames(getInputNames());
	}
	
	protected void resetInputNames(){
		input_names.clear();
		for (int i = 0; i < inputState.length; i++)
			input_names.add(i, "Input[" + i + "]");
	}
	
	@Override
	public Object clone(){
		//clones state but not sensors
		SimpleEnvironment<T> env = new SimpleEnvironment<T>(updater);
		env.setObservableState(observableState);
		env.setInputState(inputState);
		
		return env;
	}
	
	@Override
	public InterfaceTreeNode issueTreeNode() {
		InterfaceTreeNode treeNode = new InterfaceTreeNode(this);
		
		//treeNode.removeAllChildren();
		
		InterfaceTreeNode sensorNode = new InterfaceTreeNode("Sensors");
		for (int i = 0; i < sensors.size(); i++)
			//sensorNode.add(new InterfaceTreeNode(sensors.get(i)));
			sensorNode.add(sensors.get(i).issueTreeNode());
		treeNode.add(sensorNode);
		
		InterfaceTreeNode sourceNode = new InterfaceTreeNode("Data Sources");
		if (observable_data_source != null) sourceNode.add(observable_data_source.issueTreeNode());
		if (input_data_source != null) sourceNode.add(input_data_source.issueTreeNode());
		treeNode.add(sourceNode);
		
		InterfaceTreeNode updateNode = new InterfaceTreeNode("Updater");
		if (updater != null) updateNode.add(updater.issueTreeNode());
		treeNode.add(updateNode);
		
		treeNodes.add(treeNode);
		
		return treeNode;
	}

	/*
	public void updateTreeNode(InterfaceTreeNode node){
		Enumeration children = node.children();
		
		while (children.hasMoreElements()){
			InterfaceTreeNode thisNode = (InterfaceTreeNode)children.nextElement();
			if (thisNode.getUserObject().equals("Sensors")){
				Enumeration sensorNodes = thisNode.children();
				while (sensorNodes.hasMoreElements()){
					InterfaceTreeNode sNode = (InterfaceTreeNode)sensorNodes.nextElement();
					DynamicModelEnvironmentSensor s = (DynamicModelEnvironmentSensor)sNode.getUserObject();
					if (s.isDestroyed() || !this.hasSensor(s))
						thisNode.remove(sNode);
					else
						s.updateTreeNode(sNode);
					}
				}
			
			if (thisNode.getUserObject().equals("Data Source")){
				InterfaceTreeNode sourceNode = (InterfaceTreeNode)thisNode.getFirstChild();
				if (sourceNode != null){
					SimpleEnvironmentDataSource source = (SimpleEnvironmentDataSource)sourceNode.getUserObject();
					if (source.isDestroyed() || !source.equals(data_source)){
						thisNode.remove(sourceNode);
						thisNode.add(data_source.getTreeNode());
					}else{
						data_source.updateTreeNode(sourceNode);
						}
					}
				}
			
			if (thisNode.getUserObject().equals("Data Source")){
				DefaultMutableTreeNode uNode = (DefaultMutableTreeNode)thisNode.getFirstChild();
				if (uNode != null && uNode.getUserObject() != updater)
						uNode.setUserObject(updater);
				}
			
			}
		
	}
	*/
	
	public boolean hasSensor(DynamicModelEnvironmentSensor s){
		for (int i = 0; i < sensors.size(); i++)
			if (sensors.get(i).equals(s)) return true;
		return false;
	}
	
	public InterfaceTreeNode getTreeNodeCopy() {
		
		return null;
	}

	public void setTreeNode() {
		/*
		if (treeNode == null)
			treeNode = new InterfaceTreeNode(this);
		
		treeNode.removeAllChildren();
		
		InterfaceTreeNode sensorNode = new InterfaceTreeNode("Sensors");
		for (int i = 0; i < sensors.size(); i++)
			sensorNode.add(new DefaultMutableTreeNode(sensors.get(i)));
		treeNode.add(sensorNode);
		
		InterfaceTreeNode sourceNode = new InterfaceTreeNode("Data Source");
		if (data_source != null) sourceNode.add(new DefaultMutableTreeNode(data_source));
		treeNode.add(sourceNode);
		
		InterfaceTreeNode updateNode = new InterfaceTreeNode("Updater");
		if (updater != null) updateNode.add(new DefaultMutableTreeNode(updater));
		treeNode.add(updateNode);
		*/
	}
	
	@Override
	public String toString(){
		return "Simple Environment";
	}
	
	@Override
	public void destroy(){
		isDestroyed = true;
	}
	
	@Override
	public boolean isDestroyed(){
		return isDestroyed;
	}
	
}