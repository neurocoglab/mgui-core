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

package mgui.interfaces.graphs;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject.XMLType;
import mgui.io.domestic.graphs.xml.GraphXMLFunctions;
import mgui.numbers.MguiDouble;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import edu.uci.ics.jung.graph.util.EdgeType;

/*******************************************
 * Default implementation of a Graph edge. Allows an edge weight to be specified.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DefaultGraphEdge extends AbstractGraphEdge implements WeightedGraphEdge {

	protected double weight;
	
	public DefaultGraphEdge(){
		super(new DefaultGraphNode(), new DefaultGraphNode());
	}
	
	public DefaultGraphEdge(AbstractGraphNode from, AbstractGraphNode to){
		super(from, to);
	}
	
	public DefaultGraphEdge(AbstractGraphNode from, AbstractGraphNode to, EdgeType type){
		super(from, to);
		this.type = type;
	}
	
	public DefaultGraphEdge(AbstractGraphNode from, AbstractGraphNode to, double weight){
		super(from, to);
		this.weight = weight;
	}
	
	public double getWeight(){
		return weight;
	}
	
	public Object clone(){
		return new DefaultGraphEdge(from, to, weight);
	}
	
	public void setWeight(double weight){
		this.weight = weight;
	}
	
	public String getLabel(){
		return "" + MguiDouble.getString(weight, 3);
	}
	
	public Factory<AbstractGraphEdge> getFactory(){
		return new Factory<AbstractGraphEdge>(){
			
			public AbstractGraphEdge create(){
				return new DefaultGraphEdge();
			}
		};
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type)
			throws SAXException {
		
		super.handleXMLElementStart(localName, attributes, type);
		this.setWeight(Double.valueOf(attributes.getValue("weight")));
		
	}
	
	@Override
	public String getXML(int tab, int v_from, int v_to) {
		
		String _tab = XMLFunctions.getTab(tab);
		
		String xml = _tab + "<" + getLocalName() + 
							 " class='" + getClass().getCanonicalName() + "'" +
							 " type='" + getType() + "'" +
							 " from='" + v_from + "'" +
							 " to='" + v_to + "'" +
							 " weight='" + weight + "'/>";
					 
		return xml;
		
	}
	
	public static Transformer<AbstractGraphEdge, Double> getWeightTransformer(){
		return new Transformer<AbstractGraphEdge, Double>(){
			
			public Double transform(AbstractGraphEdge edge){
				try{
					return ((WeightedGraphEdge)edge).getWeight();
				}catch (ClassCastException ex){
					InterfaceSession.handleException(new GraphException("DefaultGraphEdge.getWeightTransformer: Edge must be an " +
											 		 "instance of WeightedGraphEdge."));
					// Treat as unweighted
					return 1.0;
					}
			}
			
		};
	}
	
}