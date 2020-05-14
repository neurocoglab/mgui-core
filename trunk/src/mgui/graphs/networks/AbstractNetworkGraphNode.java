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

package mgui.graphs.networks;

import java.util.ArrayList;

import mgui.interfaces.graphs.AbstractGraphNode;
import mgui.models.networks.components.InterfaceNetworkComponentListener;
import mgui.models.networks.components.NetworkComponentEvent;

/***********************************************
 * Represents a node in a network graph.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class AbstractNetworkGraphNode extends AbstractGraphNode 
											   implements InterfaceNetworkComponentListener,
											   			  NetworkGraphElement{

	protected ArrayList<NetworkGraphListener> listeners = new ArrayList<NetworkGraphListener>();
	

	/*
	public static VertexStringer getStringer(){
		return new Stringer();
	}
	*/
	
	public void addListener(NetworkGraphListener listener){
		listeners.add(listener);
	}
	
	public void removeListener(NetworkGraphListener listener){
		listeners.remove(listener);
	}
	
	public void componentUpdated(NetworkComponentEvent e){
		fireListeners();
	}
	
	protected void fireListeners(){
		NetworkGraphEvent e = new NetworkGraphEvent(this);
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).graphUpdated(e);
	}
	
	public abstract double getWeight(AbstractNetworkGraphNode target);
	
	//Stringer for edges of type NeuralNetConnection
	//TODO: make more flexible in terms of what can be displayed as a label
	/*
	static class Stringer implements VertexStringer {
		
		public String getLabel(ArchetypeVertex node){
			if (!(node instanceof AbstractNetworkGraphNode)) return null;
			AbstractNetworkGraphNode n = (AbstractNetworkGraphNode)node;
			return new String(n.getLabel());
		}
	}
	*/
	
}