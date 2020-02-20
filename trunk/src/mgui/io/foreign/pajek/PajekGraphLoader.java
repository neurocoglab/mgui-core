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

package mgui.io.foreign.pajek;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.vecmath.Point3f;

import mgui.geometry.Graph3D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.graphs.AbstractGraphEdge;
import mgui.interfaces.graphs.AbstractGraphNode;
import mgui.interfaces.graphs.DefaultGraph;
import mgui.interfaces.graphs.DefaultGraphEdge;
import mgui.interfaces.graphs.DefaultGraphNode;
import mgui.interfaces.graphs.InterfaceAbstractGraph;
import mgui.interfaces.graphs.WeightedGraphEdge;
import mgui.interfaces.graphs.util.GraphFunctions;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.graphs.Graph3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.graphs.GraphFileLoader;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.functors.OrPredicate;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.io.PajekNetReader;

/*************************************************************
 * Loads a graph from the Pajek graph format (see 
 * <a href="http://pajek.imfm.si/doku.php">http://pajek.imfm.si/doku.php</a>).
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PajekGraphLoader extends GraphFileLoader {

	public PajekGraphLoader(){
		
	}
	
	public InterfaceAbstractGraph loadGraph(){
		return loadGraph(true, false);
	}
	
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar) {

		if (!(options instanceof PajekGraphInputOptions)){
			InterfaceSession.log("PajekGraphLoader: options must be instance of PajekGraphInputOptions..");
			return false;
			}
		
		PajekGraphInputOptions _options = (PajekGraphInputOptions)options;
		boolean success = true;
		
		if (_options.files == null){
			InterfaceSession.log("PajekGraphLoader: No files selected!",
					 LoggingType.Errors);
			return false;
		}
		
		for (int i = 0; i < _options.files.length; i++){
			setFile(_options.files[i]);
			InterfaceAbstractGraph graph = loadGraph(true, true);
			if (graph == null){
				InterfaceSession.log("PajekGraphLoader: Error reading file '" + dataFile.getAbsolutePath() + "'..",
									 LoggingType.Errors);
				success = false;
			}else{
				graph.setName(_options.graph_names.get(i));
				InterfaceSession.getWorkspace().addGraph(graph);
				if (_options.create_shape.get(i)){
					ShapeSet3DInt set = _options.shape_sets.get(i);
					Graph3DInt graph_int = new Graph3DInt(new Graph3D(graph), _options.shape_names.get(i));
					graph_int.setFileLoader(getIOType());
					InterfaceIOType complement = this.getWriterComplement();
					if (complement != null)
						graph_int.setFileWriter(complement);
					try{
						graph_int.setUrlReference(dataFile.toURI().toURL());
					}catch (Exception ex){
						InterfaceSession.log("PajekGraphLoader: Could not set URL for file '" + 
											 dataFile.getAbsolutePath() + "'.", LoggingType.Errors);
						}
					set.addShape(graph_int);
					}
				
				}
			
			}
		
		return success;
	}
	
	@Override
	public InterfaceAbstractGraph loadGraph(boolean unique_labels, boolean has_locations) {
		if (dataFile == null && dataURL == null) return null;
		
		//new Pajek3DReader
		Pajek3DReader reader = new Pajek3DReader(has_locations);
		
		try{
			DefaultGraph graph = new DefaultGraph();
			reader.load(dataFile.getAbsolutePath(), graph);
			//DefaultGraph graph = new DefaultGraph(g);
			
			return graph;
			
		}catch (IOException e){
			e.printStackTrace();
			}
		
		return null;
	}

	public Graph3DInt loadGraph3DInt(){
		return loadGraph3DInt(true);
	}
	
	@Override
	public Graph3DInt loadGraph3DInt(boolean unique_labels){
		if (dataFile == null && dataURL == null) return null;
		
		//new Pajek3DReader
		Pajek3DReader reader = new Pajek3DReader();
		
		try{
			DefaultGraph g = new DefaultGraph();
			reader.load(dataFile.getAbsolutePath(), g);
			Graph3DInt g3d = new Graph3DInt(new Graph3D(g, reader.node_pts)); 
			return g3d;
			
		}catch (IOException e){
			e.printStackTrace();
			}
		
		return null;
	}
	
	//A lot of copy paste because of private crap in the super class.. why?
	static class Pajek3DReader extends PajekNetReader<InterfaceAbstractGraph, 
											   AbstractGraphNode, 
											   AbstractGraphEdge>{
		
		public HashMap<AbstractGraphNode,Point3f> node_pts = new HashMap<AbstractGraphNode,Point3f>();
		private boolean get_locations = true;
		
		private static final Predicate<String> v_pred = new StartsWithPredicate("*vertices");
	    private static final Predicate<String> a_pred = new StartsWithPredicate("*arcs");
	    private static final Predicate<String> e_pred = new StartsWithPredicate("*edges");
	    private static final Predicate<String> t_pred = new StartsWithPredicate("*");
	    private static final Predicate<String> c_pred = OrPredicate.getInstance(a_pred, e_pred);
		
	    /**
	     * A Predicate which evaluates to <code>true</code> if the
	     * argument starts with the constructor-specified String.
	     * 
	     * @author Joshua O'Madadhain
	     */
	    protected static class StartsWithPredicate implements Predicate<String> {
	        private String tag;
	        
	        protected StartsWithPredicate(String s) {
	            this.tag = s;
	        }
	        
	        public boolean evaluate(String str) {
	            return (str != null && str.toLowerCase().startsWith(tag));
	        }
	    }
	    
		/**********************************
		 * Default constructor, using default node and edge types:
		 * <br>Nodes: {@link DefaultGraphNode}
		 * <br>Edges: {@link DefaultGraphEdge}
		 * 
		 */
		public Pajek3DReader(){
			this(true);
		}
		
		/**********************************
		 * Default constructor, using default node and edge types:
		 * <br>Nodes: {@link DefaultGraphNode}
		 * <br>Edges: {@link DefaultGraphEdge}
		 * 
		 * @param get_locations Flag specifies whether to load locations from Pajek file
		 */
		public Pajek3DReader(boolean get_locations){
			super(GraphFunctions.getNodeFactory(), 
				  GraphFunctions.getEdgeFactory());
			this.get_locations = get_locations;
		}
		
		/**
	     * Returns the graph created by parsing the specified file, by populating the
	     * specified graph.
	     * @throws IOException
	     */
	    public void load(String filename, DefaultGraph g) throws IOException
	    {
	        if (g == null)
	            throw new IllegalArgumentException("Graph provided must be non-null");
	        load(new FileReader(filename), g);
	    }
		
		 public void load(Reader reader, DefaultGraph g) throws IOException
		    {
		        BufferedReader br = new BufferedReader(reader);
		                
		        // ignore everything until we see '*Vertices'
		        String curLine = skip(br, v_pred);
		        
		        if (curLine == null) // no vertices in the graph; return empty graph
		            return;
		        
		        // create appropriate number of vertices
		        StringTokenizer st = new StringTokenizer(curLine);
		        st.nextToken(); // skip past "*vertices";
		        int num_vertices = Integer.parseInt(st.nextToken());
		        List<AbstractGraphNode> id = null;
		        if (vertex_factory != null)
		        {
		            for (int i = 1; i <= num_vertices; i++)
		                g.addVertex(vertex_factory.create());
		            id = new ArrayList<AbstractGraphNode>(g.getVertices());
		        }

		        // read vertices until we see any Pajek format tag ('*...')
		        curLine = null;
		        while (br.ready())
		        {
		            curLine = br.readLine();
		            if (curLine == null || t_pred.evaluate(curLine))
		                break;
		            if (curLine == "") // skip blank lines
		                continue;
		            
		            try
		            {
		                readVertex(curLine, id, num_vertices);
		            }
		            catch (IllegalArgumentException iae)
		            {
		                br.close();
		                reader.close();
		                throw iae;
		            }
		        }   

		        // skip over the intermediate stuff (if any) 
		        // and read the next arcs/edges section that we find
		        curLine = readArcsOrEdges(curLine, br, g, id, edge_factory);

		        // ditto
		        readArcsOrEdges(curLine, br, g, id, edge_factory);
		        
		        br.close();
		        reader.close();
		        
		    }

		 
		 String readArcsOrEdges(String curLine, BufferedReader br, DefaultGraph g, List<AbstractGraphNode> id, 
				 				Factory<AbstractGraphEdge> edge_factory)
	        throws IOException
	    {
	        String nextLine = curLine;
	        
	        // in case we're not there yet (i.e., format tag isn't arcs or edges)
	        if (! c_pred.evaluate(curLine))
	            nextLine = skip(br, c_pred);

	        boolean reading_arcs = false;
	        boolean reading_edges = false;
	        EdgeType directedness = null;
	        if (a_pred.evaluate(nextLine))
	        {
	            if (g instanceof UndirectedGraph) {
	                throw new IllegalArgumentException("Supplied undirected-only graph cannot be populated with directed edges");
	            } else {
	                reading_arcs = true;
	                directedness = EdgeType.DIRECTED;
	            }
	        }
	        if (e_pred.evaluate(nextLine))
	        {
	            if (g instanceof DirectedGraph)
	                throw new IllegalArgumentException("Supplied directed-only graph cannot be populated with undirected edges");
	            else
	                reading_edges = true;
	            directedness = EdgeType.UNDIRECTED;
	        }
	        
	        if (!(reading_arcs || reading_edges))
	            return nextLine;
	        
	        boolean is_list = l_pred.evaluate(nextLine);

	        while (br.ready())
	        {
	            nextLine = br.readLine();
	            if (nextLine == null || t_pred.evaluate(nextLine))
	                break;
	            if (curLine == "") // skip blank lines
	                continue;
	            
	            StringTokenizer st = new StringTokenizer(nextLine.trim());
	            
	            int vid1 = Integer.parseInt(st.nextToken()) - 1;
	            AbstractGraphNode v1;
	            if (id != null)
	              v1 = id.get(vid1);
	            else
	              v1 = (AbstractGraphNode)new DefaultGraphNode("N" + vid1);

	            
	            if (is_list) // one source, multiple destinations
	            {
	                do
	                {
	                    createAddEdge(st, v1, directedness, g, id, edge_factory);
	                } while (st.hasMoreTokens());
	            }
	            else // one source, one destination, at most one weight
	            {
	                AbstractGraphEdge e = createAddEdge(st, v1, directedness, g, id, edge_factory);
	                // get the edge weight if we care
	                if (st.hasMoreTokens()){
	                	if (e instanceof WeightedGraphEdge)
	                		((WeightedGraphEdge) e).setWeight(new Float(st.nextToken()));
	                	}
	            }
	        }
	        return nextLine;
	    }
		
		
		/********************************
	     * Parses <code>curLine</code> as a reference to a vertex, and optionally assigns 
	     * label and (3D) location information.
	     * 
	     * @throws IOException
	     */
	    protected void readVertex(String curLine, List<AbstractGraphNode> id, int num_vertices) throws IOException
	    {
	        AbstractGraphNode node;
	        String[] parts = null;
	        int coord_idx = -1;     // index of first coordinate in parts; -1 indicates no coordinates found
	        String index;
	        String label = null;
	        // if there are quote marks on this line, split on them; label is surrounded by them
	        if (curLine.indexOf('"') != -1)
	        {
	            String[] initial_split = curLine.trim().split("\"");
	            // if there are any quote marks, there should be exactly 2
	            if (initial_split.length < 2 || initial_split.length > 3)
	                throw new IllegalArgumentException("Unbalanced (or too many) quote marks in " + curLine);
	            index = initial_split[0].trim();
	            label = initial_split[1].trim();
	            if (initial_split.length == 3)
	                parts = initial_split[2].trim().split("\\s+", -1);
	            coord_idx = 0;
	        }
	        else // no quote marks, but are there coordinates?
	        {
	            parts = curLine.trim().split("\\s+", -1);
	            index = parts[0];
	            switch (parts.length)
	            {
	                case 1:         // just the ID; nothing to do, continue
	                    break;  
	                case 2:         // just the ID and a label
	                    label = parts[1];
	                    break;
	                case 3:         // ID, no label, coordinates
	                    coord_idx = 1;
	                    break;
	                case 4:         // ID, label, (x,y) coordinates
	                    coord_idx = 2;
	                    break;
	            }
	        }
	        int v_id = Integer.parseInt(index) - 1; // go from 1-based to 0-based index
	        if (v_id >= num_vertices || v_id < 0)
	            throw new IllegalArgumentException("Vertex number " + v_id +
	                    "is not in the range [1," + num_vertices + "]");
	        node = id.get(v_id);
	        //TODO: make this work
	        // only attach the label if there's one to attach
	        if (label != null && label.length() > 0)
	            node.setLabel(label);
	        
	        // parse the rest of the line
	        if (get_locations) {
	            if (coord_idx == -1 || parts == null || parts.length < coord_idx+3){
	            	get_locations = false;
	            	InterfaceSession.log("PajekGraphLoader: No coordinates for graph.", 
	            						 LoggingType.Errors);
	            }else{
	            	
		                //throw new IllegalArgumentException("Coordinates requested, but " +
		                 //       curLine + " does not include coordinates");
		            double x = Double.parseDouble(parts[coord_idx]);
		            double y = Double.parseDouble(parts[coord_idx+1]);
		            double z = Double.parseDouble(parts[coord_idx+2]);
		            
		            node.setLocation(new Point3f((float)x, (float)y, (float)z));
		            
		            node_pts.put(node, new Point3f((float)x, (float)y, (float)z));
		            vertex_locations.set(node, new Point2D.Double(x,y));
		            }
	        	}
	    }
	    
	}
	
}