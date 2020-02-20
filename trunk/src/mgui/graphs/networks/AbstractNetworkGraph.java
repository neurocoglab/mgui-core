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

package mgui.graphs.networks;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.graphs.AbstractGraphEdge;
import mgui.interfaces.graphs.AbstractGraphNode;
import mgui.interfaces.graphs.DefaultGraph;
import mgui.interfaces.logs.LoggingType;
import mgui.models.networks.AbstractNetwork;
import mgui.models.networks.NetworkException;

/***************************************************
 * A graph which explicitly represents a connected network.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class AbstractNetworkGraph extends DefaultGraph {

	public abstract boolean setFromNetwork(AbstractNetwork net) throws NetworkException;
	
	@Override
	public boolean addGraphNode(AbstractGraphNode node) {
		if (!(node instanceof AbstractNetworkGraphNode)){
			InterfaceSession.log("AbstractNetworkGraph: Nodes must be instances of " +
								 "AbstractNetworkGraphNode.", LoggingType.Errors);
			return false;
			}
		((AbstractNetworkGraphNode)node).addListener(this);
		return super.addGraphNode(node);
	}
	
	@Override
	public boolean removeGraphNode(AbstractGraphNode node){
		if (!(node instanceof AbstractNetworkGraphNode)){
			InterfaceSession.log("AbstractNetworkGraph: Nodes must be instances of " +
								 "AbstractNetworkGraphNode.", LoggingType.Errors);
			return false;
			}
		((AbstractNetworkGraphNode)node).removeListener(this);
		return super.removeVertex(node);
	}
	
	@Override
	public boolean addGraphEdge(AbstractGraphEdge edge){
		if (!(edge instanceof AbstractNetworkGraphConnection)){
			InterfaceSession.log("AbstractNetworkGraph: Edges must be instances of " +
								 "AbstractNetworkGraphConnection.", LoggingType.Errors);
			return false;
			}
		((AbstractNetworkGraphConnection)edge).addListener(this);
		return super.addGraphEdge(edge);
	}
	
	@Override
	public boolean removeGraphEdge(AbstractGraphEdge edge){
		if (!(edge instanceof AbstractNetworkGraphConnection)){
			InterfaceSession.log("AbstractNetworkGraph: Edges must be instances of " +
								 "AbstractNetworkGraphConnection.", LoggingType.Errors);
			return false;
			}
		((AbstractNetworkGraphConnection)edge).removeListener(this);
		return super.removeEdge(edge);
	}
	
}