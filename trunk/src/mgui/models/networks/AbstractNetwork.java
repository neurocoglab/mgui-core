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

package mgui.models.networks;

import java.util.ArrayList;

import mgui.interfaces.graphs.InterfaceAbstractGraph;
import mgui.models.dynamic.DynamicModelComponent;
import mgui.util.IDFactory;


public abstract class AbstractNetwork extends AbstractNetworkModel {

	public ArrayList<NetworkListener> listeners = new ArrayList<NetworkListener>();
	protected IDFactory idFactory = new IDFactory();
	
	protected void init(){
		
	}
	
	public void addListener(NetworkListener l){
		listeners.add(l);
	}
	
	public void removeListener(NetworkListener l){
		listeners.remove(l);
	}
	
	public void fireListeners(){
		fireListeners(0);
	}
	
	protected void fireListeners(int code){
		//need to make a copy of this list since it can change...
		ArrayList<NetworkListener> copy = new ArrayList<NetworkListener>();
		copy.addAll(listeners);
		for (int i = 0; i < copy.size(); i++)
			copy.get(i).networkUpdated(new NetworkEvent(this, code));
	}
	
	public void reset(){
		if (environment != null) environment.reset();
		ArrayList<DynamicModelComponent> components = getComponents();
		for (int i = 0; i < components.size(); i++)
			components.get(i).reset();
	}
	
	@Override
	public abstract InterfaceAbstractGraph getGraph();
	
	//public InterfaceTreeNode getTreeNode() {
	//	if (treeNode == null) setTreeNode();
	//	return treeNode;
	//}

	public abstract ArrayList<DynamicModelComponent> getComponents();

}