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

package mgui.io.domestic.graphs.xml;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.Utility;
import mgui.interfaces.graphs.AbstractGraphEdge;
import mgui.interfaces.graphs.AbstractGraphNode;
import mgui.interfaces.graphs.InterfaceAbstractGraph;
import mgui.interfaces.logs.LoggingType;

import org.xml.sax.Attributes;

import edu.uci.ics.jung.graph.util.EdgeType;

/*********************************************************
 * Utility class for XML operations involving graphs.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class GraphXMLFunctions extends Utility {

	/**************************************************************
	 * Returns a new instance of {@code InterfaceAbstractGraph}, from the given
	 * SAX attributes.
	 * 
	 * @param attributes
	 * @return The new instance, or {@code null} if things didn't work out
	 */
	public static InterfaceAbstractGraph getGraphInstance(Attributes attributes){
		
		String name = attributes.getValue("name");
		String obj_class = attributes.getValue("class");
		
		try{
			
			Class clazz = Class.forName(obj_class);
			InterfaceAbstractGraph graph = (InterfaceAbstractGraph)clazz.newInstance();
			graph.setName(name);
			return graph;
			
		}catch(ClassNotFoundException e){
			InterfaceSession.log("GraphXMLFunctions.getGraphInstance: Class '" + obj_class +"' not found.", 
								 LoggingType.Errors);
		}catch(Exception e){
			InterfaceSession.log("GraphXMLFunctions.getGraphInstance: Could not instantiate '" + 
								 obj_class + "'.", 
								 LoggingType.Errors);
			}
		
		return null;
	}
	
	/**************************************************************
	 * Returns a new instance of {@code AbstractGraphEdge}, from the given
	 * SAX attributes.
	 * 
	 * @param attributes
	 * @return The new instance, or {@code null} if things didn't work out
	 */
	public static AbstractGraphEdge getGraphEdgeInstance(Attributes attributes){
		
		String obj_class = attributes.getValue("class");
		
		try{
			
			Class clazz = Class.forName(obj_class);
			AbstractGraphEdge edge = (AbstractGraphEdge)clazz.newInstance();
			return edge;
			
		}catch (ClassCastException e){
			InterfaceSession.log("GraphXMLFunctions.getGraphEdgeInstance: Class must be a subclass of AbstractGraphEdge.", 
					 LoggingType.Errors);
		}catch(ClassNotFoundException e){
			InterfaceSession.log("GraphXMLFunctions.getGraphEdgeInstance: Class '" + obj_class +"' not found.", 
								 LoggingType.Errors);
		}catch(Exception e){
			InterfaceSession.log("GraphXMLFunctions.getGraphEdgeInstance: Could not instantiate '" + 
								 obj_class + "'.", 
								 LoggingType.Errors);
			}
		
		return null;
	}
	
	/**************************************************************
	 * Returns a new instance of {@code AbstractGraphNode}, from the given
	 * SAX attributes.
	 * 
	 * @param attributes
	 * @return The new instance, or {@code null} if things didn't work out
	 */
	public static AbstractGraphNode getGraphNodeInstance(Attributes attributes){
		
		String obj_class = attributes.getValue("class");
		
		try{
			
			Class clazz = Class.forName(obj_class);
			AbstractGraphNode node = (AbstractGraphNode)clazz.newInstance();
			return node;
		
		}catch (ClassCastException e){
			InterfaceSession.log("GraphXMLFunctions.getGraphNodeInstance: Class must be a subclass of AbstractGraphNode.", 
					 LoggingType.Errors);
		}catch(ClassNotFoundException e){
			InterfaceSession.log("GraphXMLFunctions.getGraphNodeInstance: Class '" + obj_class +"' not found.", 
								 LoggingType.Errors);
		}catch(Exception e){
			InterfaceSession.log("GraphXMLFunctions.getGraphNodeInstance: Could not instantiate '" + 
								 obj_class + "'.", 
								 LoggingType.Errors);
			}
		
		return null;
	}
	
	/**************************************************
	 * Returns a string representation of {@code type}.
	 * 
	 * @param type
	 * @return
	 */
	public static String getEdgeTypeAsString(EdgeType type){
		
		switch(type){
			case DIRECTED:
				return "directed";
			case UNDIRECTED:
				return "undirected";
		}
		
		return "?";
		
	}
	
	/**************************************************
	 * Returns the type corresponding to the string representation {@code type}.
	 * 
	 * @param type
	 * @return The type, or {@code null} if there is no corresponding type
	 */
	public static EdgeType getEdgeTypeForString(String type){
		
		if (type.equalsIgnoreCase("directed"))
			return EdgeType.DIRECTED;
		if (type.equalsIgnoreCase("undirected"))
			return EdgeType.UNDIRECTED;
		
		return null;
		
	}
	
}