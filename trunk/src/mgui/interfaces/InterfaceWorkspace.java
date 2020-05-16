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

package mgui.interfaces;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import mgui.datasources.DataSource;
import mgui.interfaces.attributes.AttributeDialogBox;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.graphics.InterfaceGraphicWindow;
import mgui.interfaces.graphics.View3D;
import mgui.interfaces.graphics.video.Video;
import mgui.interfaces.graphics.video.Video3D;
import mgui.interfaces.graphs.InterfaceAbstractGraph;
import mgui.interfaces.io.DataInputStream;
import mgui.interfaces.io.DataOutputStream;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.math.VariableObject;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.menus.PopupMenuObject;
import mgui.interfaces.models.InterfaceAbstractModel;
import mgui.interfaces.plots.InterfacePlot;
import mgui.interfaces.projects.InterfaceProject;
import mgui.interfaces.projects.InterfaceProjectDialogBox;
import mgui.interfaces.queries.InterfaceQuery;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.Shape2DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeModel3DListener;
import mgui.interfaces.shapes.ShapeModelEvent;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.trees.InterfaceTreePanel;
import mgui.interfaces.trees.TreeObject;
import mgui.interfaces.variables.VariableInt;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.pipelines.InterfacePipeline;
import mgui.pipelines.StaticPipelineEvent;
import mgui.pipelines.StaticPipelineListener;
import mgui.resources.icons.IconObject;

import org.xml.sax.Attributes;

/*********************************************************
 * A Workspace acts as a container for all data models and graphics windows.
 * 
 * <p>TODO: maintain session history 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceWorkspace extends AbstractInterfaceObject implements IconObject,
																		   TabbedDisplayListener,
																		   XMLObject,
																		   StaticPipelineListener,
																		   ShapeModel3DListener{

	protected InterfaceDisplayPanel display_panel;
	
	protected ArrayList<InterfaceAbstractGraph> graphs = new ArrayList<InterfaceAbstractGraph>();
	protected ArrayList<InterfaceAbstractModel> models = new ArrayList<InterfaceAbstractModel>();
	protected ArrayList<DataInputStream<?>> inputStreams = new ArrayList<DataInputStream<?>>();
	protected ArrayList<DataOutputStream<?>> outputStreams = new ArrayList<DataOutputStream<?>>();
	protected ArrayList<InterfacePlot<?>> plots = new ArrayList<InterfacePlot<?>>();
	protected ArrayList<VariableInt<?>> variables = new ArrayList<VariableInt<?>>();
	protected ArrayList<InterfaceQuery> queries = new ArrayList<InterfaceQuery>();
	protected TreeSet<View3D> views3D = new TreeSet<View3D>();
	protected TreeSet<Video> videos = new TreeSet<Video>();
	protected ArrayList<DataSource> dataSources = new ArrayList<DataSource>();	//data sources
	protected ArrayList<ShapeSelectionSet> selectionSets = new ArrayList<ShapeSelectionSet>();
	protected ArrayList<ShapeModel3D> shapeModels = new ArrayList<ShapeModel3D>();
	protected HashMap<String, InterfaceProject> projects = new HashMap<String, InterfaceProject>();
	protected HashMap<String, InterfacePipeline> pipelines = new HashMap<String, InterfacePipeline>();
	
	protected InterfaceTreeNode variablesNode, queriesNode, pipelinesNode;
	protected InterfaceTreePanel objectTree;
	
	protected InterfaceComboPanel comboPanel;
	
	protected AttributeDialogBox current_attribute_dialog = new AttributeDialogBox();
	protected AttributeDialogBox current_shape_attribute_dialog = new AttributeDialogBox(Shape3DInt.class);
	
	public InterfaceWorkspace(){
		init();
	}
	
	void init(){
		
		//temp
		//InterfaceProject project = new InterfaceProject("Test Project", InterfaceEnvironment.getCurrentDir());
		//projects.add(project);
		
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfaceWorkspace.class.getResource("/mgui/resources/icons/workspace_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/workspace_20.png");
		return null;
	}
	
	//************** Attribute Dialogs **********************
	
	public AttributeDialogBox getCurrentAttributeDialog(){
		return current_attribute_dialog;
	}
	
	public AttributeDialogBox getCurrentShapeAttributeDialog(){
		return current_shape_attribute_dialog;
	}
	
	public void showAttributeDialog(AttributeObject object){
		current_attribute_dialog.showDialog(object);
	}
	
	//************** Combo Panel ****************************
	
	public ArrayList<InterfacePanel> getInterfacePanels(){
		if (comboPanel == null) return new ArrayList<InterfacePanel>();
		return comboPanel.getPanels();
	}
	
	public void setComboPanel(InterfaceComboPanel panel){
		comboPanel = panel;
	}
	
	public void populateShapeSetCombo(JComboBox combo_box){
		
		combo_box.removeAllItems();
		ArrayList<ShapeModel3D> models = getShapeModels();
		
		for (int i = 0; i < models.size(); i++){
			ShapeSet3DInt model_set = models.get(i).getModelSet();
			combo_box.addItem(model_set);
			
			List<Shape3DInt> all_sets = model_set.getShapeType(model_set);
			
			for (Shape3DInt set : all_sets) {
				combo_box.addItem(set);
				}
			}
	}
	
	public void populateShapeTypeCombo(JComboBox combo_box, Shape3DInt shape){
		
		combo_box.removeAllItems();
		ArrayList<ShapeModel3D> models = getShapeModels();
		
		for (int i = 0; i < models.size(); i++){
			ShapeSet3DInt model_set = models.get(i).getModelSet();
			if (shape instanceof ShapeSet3DInt)
				combo_box.addItem(model_set);
			List<Shape3DInt> all_sets = model_set.getShapeType(model_set);
			
			for (Shape3DInt set : all_sets) {
				combo_box.addItem(set);
				}
			}
		
	}
	
	public void populateShapeCombo(JComboBox combo_box){
		
		combo_box.removeAllItems();
		
		ArrayList<ShapeModel3D> models = InterfaceSession.getWorkspace().getShapeModels();
		
		for (int i = 0; i < models.size(); i++){
			//combo_box.addItem(models.get(i));
			ShapeSet3DInt model_set = models.get(i).getModelSet();
			
			ArrayList<Shape3DInt> shapes = model_set.get3DShapes(true);
			for (Shape3DInt shape : shapes)
				combo_box.addItem(shape);
			
			}
	}
	
	//**************Display Panel****************************
	public InterfaceDisplayPanel getDisplayPanel(){
		return display_panel;
	}
	
	public void setDisplayPanel(InterfaceDisplayPanel display_panel){
		if (display_panel != null && display_panel instanceof InterfaceTabbedDisplayPanel)
			((InterfaceTabbedDisplayPanel)display_panel).removeTabbedDisplayListener(this);
		this.display_panel = display_panel;
		if (display_panel instanceof InterfaceTabbedDisplayPanel)
			((InterfaceTabbedDisplayPanel)display_panel).addTabbedDisplayListener(this);
	}
	
	
	//**************Data Sources****************************
	public boolean addDataSource(DataSource ds){
		for (int i = 0; i < dataSources.size(); i++){
			if (ds.getName().equals(dataSources.get(i).getName())){
				InterfaceSession.log("InterfaceWorkspace: Cannot add data source '" + ds.getName() + "':" +
									" data source by that name already exists.", 
									LoggingType.Errors);
				return false;
				}
		}
		dataSources.add(ds);
		ds.setWorkspace(this);
		updateDataSourceNode();
		return true;
	}
	
	public void removeDataSource(DataSource ds){
		dataSources.remove(ds);
		ds.setWorkspace(null);
		updateDataSourceNode();
	}
	
	/***********************************
	 * Returns a list of all data sources associated with this workspace.
	 * 
	 * @return
	 */
	public ArrayList<DataSource> getDataSources(){
		return dataSources;
	}
	
	/***********************************
	 * Returns a list of all connected data sources associated with this workspace.
	 * 
	 * @return
	 */
	public ArrayList<DataSource> getConnectedDataSources(){
		ArrayList<DataSource> connected = new ArrayList<DataSource>();
		
		for (int i = 0; i <  dataSources.size(); i++){
			if (dataSources.get(i).isConnected())
				connected.add(dataSources.get(i));
			}
		
		return connected;
	}
	
	public DataSource getDataSource(String ds){
		for (int i = 0; i < dataSources.size(); i++)
			if (dataSources.get(i).getConnection().getName().compareTo(ds) == 0)
				return dataSources.get(i);
		return null;
	}
	
	/**********************************************
	 * Returns a list of all objects in this workspace which are instances of {@link VariableObject}.
	 * 
	 * @return
	 */
	public ArrayList<VariableObject> getVariableObjects(){
		
		ArrayList<VariableObject> list = new ArrayList<VariableObject>();
		
		// Add all shapes
		ArrayList<ShapeModel3D> s_models = this.getShapeModels();
		for (int m = 0; m < s_models.size(); m++){
			ShapeModel3D model = s_models.get(m);
			ArrayList<Shape3DInt> shapes_3d = model.getModelSet().get3DShapes(true);
			list.addAll(shapes_3d);
			
			for (int s = 0; s < shapes_3d.size(); s++){
				Shape3DInt shape = shapes_3d.get(s);
				if (shape instanceof SectionSet3DInt){
					list.addAll(((SectionSet3DInt)shape).get2DShapes());
					}
				}
			}
		
		return list;
		
	}
	
	/***************************************************
	 * Returns a list of all the attribute objects contained in this workspace.
	 * 
	 * NB. currently only returns all instances of <code>InterfaceShape</code>
	 * 
	 * @return
	 */
	public ArrayList<AttributeObject> getAttributeObjects(){
		
		ArrayList<AttributeObject> list = new ArrayList<AttributeObject>();
		
		//*** WINDOWS ***
		if (display_panel != null){
			ArrayList<InterfaceGraphicWindow> windows = display_panel.getAllWindows();
			Collections.sort(windows, new Comparator<InterfaceGraphicWindow>(){
				public int compare(InterfaceGraphicWindow o1, InterfaceGraphicWindow o2){
					return o1.getName().compareTo(o2.getName());
				}
			});
			
			for (int i = 0; i < windows.size(); i++)
				list.add(windows.get(i).getPanel());
			}
		
		//*** SHAPES ***
		ArrayList<ShapeModel3D> models = this.getShapeModels();
		Collections.sort(models, new Comparator<ShapeModel3D>(){
			public int compare(ShapeModel3D m1, ShapeModel3D m2){
				return m1.getName().compareTo(m2.getName());
			}
		});
		
		for (int i = 0; i < models.size(); i++){
			ShapeSet3DInt model_set = models.get(i).getModelSet();
			ArrayList<Shape3DInt> shapes3D = new ArrayList<Shape3DInt>();
			if (model_set != null){
				shapes3D.add(model_set);
				shapes3D.addAll(model_set.get3DShapes(true));
				ArrayList<Shape2DInt> shapes2D = new ArrayList<Shape2DInt>();
				for (int j = 0; j < shapes3D.size(); j++)
					if (shapes3D.get(j) instanceof SectionSet3DInt)
						shapes2D.addAll(((SectionSet3DInt)shapes3D.get(j)).get2DShapes());
				
				list.addAll(shapes3D);
				list.addAll(shapes2D);
				}
			}
		
		return list;
	}
	
	public void addModel(InterfaceAbstractModel model){
		models.add(model);
		updateTreeNodes();
	}
	
	public void removeModel(InterfaceAbstractModel model){
		models.remove(model);
		updateTreeNodes();
	}
	
	//**************Shape Models****************************
	
	public void addShapeModel(ShapeModel3D model, boolean set_current){
		shapeModels.add(model);
		model.addModelListener(this);
		if (set_current && display_panel != null)
			display_panel.setCurrentShapeModel(model);
		updateTreeNodes();
	}
	
	public void removeShapeModel(ShapeModel3D model){
		shapeModels.remove(model);
		model.removeModelListener(this);
		updateTreeNodes();
	}
	
	public boolean modelExists(ShapeModel3D model){
		for (int i = 0; i < shapeModels.size(); i++)
			if (shapeModels.get(i).equals(model)) return true;
		return false;
	}
	
	public ArrayList<ShapeModel3D> getShapeModels(){
		return new ArrayList<ShapeModel3D>(shapeModels);
	}
	
	@Override
	public void shapeModelChanged(ShapeModelEvent event) {
		ShapeModel3D model = (ShapeModel3D)event.getSource();
		
		switch (event.type){
			case ModelDestroyed:
				removeShapeModel(model);
				return;
			}
		
	}
	
	/************************************************
	 * Searches the workspace for a shape with the given full name (i.e., {model}.{sets}.{shape_name}), and returns
	 * it if found. Otherwise returns {@code null}.
	 * 
	 * @param full_name
	 * @return
	 */
	public InterfaceShape getShapeForName(String full_name){
		
		String[] parts = full_name.split("\\.");
		if (parts.length < 2){
			InterfaceSession.log("InterfaceWorkspace.getShapeForName: name must have at least two parts.",
								 LoggingType.Errors);
			return null;
			}
			
		String model_name = parts[0];
		String path = model_name;
		ShapeModel3D model = getShapeModel(model_name);
		if (model == null){
			InterfaceSession.log("InterfaceWorkspace.getShapeForName: No model named '" + model_name + "' in workspace.",
					 			 LoggingType.Errors);
			return null;
			}
		
		ShapeSet3DInt this_set = model.getModelSet();
		if (this_set == null){
			InterfaceSession.log("InterfaceWorkspace.getShapeForName: No model set for '" + model_name + "'.",
					 			 LoggingType.Errors);
			return null;
			}
		
		if (parts.length == 2) // Shape is model set
			return this_set;
		
		for (int i = 2; i < parts.length; i++){
			String part = parts[i];
			if (i < parts.length - 1){
				Shape3DInt shape = this_set.getShape(part);
				path = path + "." + part;
				if (this_set == null || !(shape instanceof ShapeSet3DInt)){
					InterfaceSession.log("InterfaceWorkspace.getShapeForName: No shape set at '" + path + "'.",
							 LoggingType.Errors);
					return null;
					}
				this_set = (ShapeSet3DInt)shape;
			}else{
				// This is the shape
				Shape3DInt shape = this_set.getShape(part);
				path = path + "." + part;
				if (shape == null){
					InterfaceSession.log("InterfaceWorkspace.getShapeForName: No shape at '" + path + "'.",
							 LoggingType.Errors);
					return null;
					}
				return shape;
				}
			
			}
		
		// Shouldn't get here
		return null;
		
	}
	
	/*******************************************
	 * Returns the model with the given name, if it exists.
	 * 
	 * @param name
	 * @return
	 */
	public ShapeModel3D getShapeModel(String name){
		
		for (int i = 0; i < shapeModels.size(); i++)
			if (shapeModels.get(i).getName().equals(name))
				return shapeModels.get(i);
		
		return null;
		
	}
	
	//**************Dynamic Models********************
	public ArrayList<InterfaceAbstractModel> getDynamicModels(){
		return models;
	}
	
	public void addDynamicModel(InterfaceAbstractModel model){
		models.add(model);
	}
	
	public void removeDynamicModel(InterfaceAbstractModel model){
		models.remove(model);
	}
	
	//**************Graphs****************************
	public void addGraph(InterfaceAbstractGraph graph){
		graphs.add(graph);
		updateTreeNodes();
	}
	
	public void removeGraph(InterfaceAbstractGraph graph){
		graphs.remove(graph);
	}
	
	public ArrayList<InterfaceAbstractGraph> getGraphs(){
		return graphs;
	}
	
	
	//**************Variables****************************
	public void addVariable(VariableInt<?> v){
		variables.add(v);
		updateTreeNodes();
	}
	
	public void removeVariable(VariableInt<?> v){
		variables.remove(v);
		updateTreeNodes();
	}
	
	/**********************************************
	 * Returns a list of all variables currently in this workspace.
	 * 
	 * @return
	 */
	public ArrayList<VariableInt<?>> getVariables(){
		return variables;
	}
	
	/**********************************************
	 * Returns the variable in this workspace corresponding to the given name. 
	 * 
	 * @param name
	 * @return
	 */
	public VariableInt<?> getVariableByName(String name){
		for (int i = 0; i < variables.size(); i++)
			if (variables.get(i).getName().equals(name))
				return variables.get(i);
		return null;
	}
	
	
	
	//**************Selection Sets*********************
	
	/*************************************************
	 * Returns a list of all selection sets currently in this workspace.
	 * 
	 */
	public ArrayList<ShapeSelectionSet> getSelectionSets(){
		return selectionSets;
	}
	
	//**************Queries****************************
	public void addQuery(InterfaceQuery q){
		queries.add(q);
		updateTreeNodes();
	}
	
	public void removeQuery(InterfaceQuery q){
		queries.remove(q);
		updateTreeNodes();
	}
	
	public ArrayList<InterfaceQuery> getQueries(){
		return queries;
	}
	
	//**************Data Streams****************************
	public void addInputStream(DataInputStream<?> s){
		inputStreams.add(s);
		//update tree node
	}
	
	public void addOutputStream(DataOutputStream<?> s){
		outputStreams.add(s);
	}
	
	//**************Plots****************************
	public void addPlot(InterfacePlot<?> p){
		plots.add(p);
		updateTreeNodes();
	}
	
	public void removePlot(InterfacePlot<?> v){
		plots.remove(v);
		updateTreeNodes();
	}
	
	public ArrayList<InterfacePlot<?>> getPlots(){
		return plots;
	}
	
	
	//**************Projects*************************
	
	public void addProject(InterfaceProject project){
		projects.put(project.getName(), project);
		updateTreeNodes();
	}
	
	public InterfaceProject getProject(String name){
		return projects.get(name);
	}
	
	//**************Views****************************
	public ArrayList<View3D> getViews3D(){
	
		ArrayList<View3D> list = new ArrayList<View3D>(views3D);
		
		//add section set views
		List<Shape3DInt> sets = display_panel.getCurrentShapeSet().getShapeType(new SectionSet3DInt());
		
		for (Shape3DInt set : sets) {
			list.add(((SectionSet3DInt)set).getView3D(1.0));
			}
		
		return list;
	}
	
	public View3D getView3D(String name){
		
		Iterator<View3D> itr = views3D.iterator();
		
		while (itr.hasNext()){
			View3D v = itr.next();
			if (v.getName().equals(name))
				return v;
			}
		
		return null;
	}
	
	public View3D addView3D(View3D view){
		//updates existing view with new, without replacing it
		//returns the live instance
		Iterator<View3D> itr = views3D.iterator();
		while (itr.hasNext()){
			View3D v = itr.next();
			if (v.getName().equals(view.getName())){
				if (!v.setViewFromCamera(view.camera))
					return null;
				return v;
				}
			}
		views3D.add(view);
		return view;
	}
	
	public void removeView3D(View3D view){
		views3D.remove(view);
	}
	
	
	//**************Videos****************************
	
	public ArrayList<Video3D> getVideos3D(){
		ArrayList<Video3D> videos3d = new ArrayList<Video3D>();
		
		Iterator<Video> itr = videos.iterator();
		
		while (itr.hasNext()){
			Video v = itr.next();
			if (v instanceof Video3D)
				videos3d.add((Video3D)v);
			}
		
		return videos3d;
	}
	
	public ArrayList<Video> getVideos(){
		return new ArrayList<Video>(videos);
	}
	
	public void addVideo(Video video){
		videos.add(video);
	}
	
	public void removeVideo(Video video){
		videos.remove(video);
	}
	
	
	//*******************Tree Nodes******************************
	
	public void setObjectTree(InterfaceTreePanel treePanel){
		//if (objectTree != null)
		//	removeDisplayListener(objectTree);
		
		objectTree = treePanel;
		treePanel.setRootNode(issueTreeNode());
		//addDisplayListener(objectTree);
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		// Add the various collection nodes for interface objects in
		// this workspace.
		
		super.setTreeNode(treeNode);
		
		treeNode.addChild(display_panel.issueTreeNode());
		
		treeNode.addChild(getShapeModelNode());
		
		treeNode.addChild(getDataSourceNode());
		
		treeNode.addChild(getGraphNode());
		
		treeNode.addChild(getDynamicModelNode());
		
		treeNode.addChild(getNameMapNode());
		
		treeNode.addChild(getVariablesNode());
		
		treeNode.addChild(getQueriesNode());
		
		treeNode.addChild(getProjectsNode());
		
		treeNode.addChild(getPipelinesNode());
	}
	
	public InterfaceTreeNode getProjectsNode(){
		//TODO: organize projects by type
		
		PopupMenuObject handler = new PopupMenuObject(){

			@Override
			public InterfacePopupMenu getPopupMenu() {
				return getPopupMenu(null);
			}
			
			@Override
			public InterfacePopupMenu getPopupMenu(List<Object> selected) {
				InterfacePopupMenu menu = new InterfacePopupMenu(this);
				menu.addMenuItem(new JMenuItem("Add new"));
				return menu;
			}

			public void handlePopupEvent(ActionEvent e) {
				//add new project dialog
				InterfaceProject new_project = InterfaceProjectDialogBox.showDialog();
				if (new_project == null) return;
				InterfaceSession.getWorkspace().addProject(new_project);
			}

			public void showPopupMenu(MouseEvent e) {
				InterfacePopupMenu menu = getPopupMenu();
				if (menu == null) return;
				menu.show(e);
			}
			
		};
		
		InterfaceTreeNode node = 
			new InterfaceTreeNode(new CollectionTreeNode("/mgui/resources/icons/project_set_20.png", "Projects"));
		node.setPopupMenu(handler.getPopupMenu());
		ArrayList<String> keys = new ArrayList<String>(projects.keySet());
		Collections.sort(keys);
		
		for (int i = 0; i < keys.size(); i++)
			node.add(projects.get(keys.get(i)).issueTreeNode());
		
		return node;
	}
	
	public ArrayList<InterfaceProject> getProjects(){
		ArrayList<InterfaceProject> projects = new ArrayList<InterfaceProject>(this.projects.values());
		Collections.sort(projects, new Comparator<InterfaceProject>(){

			@Override
			public int compare(InterfaceProject p1, InterfaceProject p2) {
				return p1.getName().compareTo(p2.getName());
			}
			
		});
		
		return projects;
	}
	
	public InterfaceTreeNode getPipelinesNode(){
		
		PopupMenuObject handler = new PopupMenuObject(){

			@Override
			public InterfacePopupMenu getPopupMenu() {
				return getPopupMenu(null);
			}
			
			@Override
			public InterfacePopupMenu getPopupMenu(List<Object> selected) {
				InterfacePopupMenu menu = new InterfacePopupMenu(this);
				menu.addMenuItem(new JMenuItem("Add new"));
				return menu;
			}

			public void handlePopupEvent(ActionEvent e) {
				//add new project dialog
				InterfaceProject new_project = InterfaceProjectDialogBox.showDialog();
				if (new_project == null) return;
				InterfaceSession.getWorkspace().addProject(new_project);
			}

			public void showPopupMenu(MouseEvent e) {
				InterfacePopupMenu menu = getPopupMenu();
				if (menu == null) return;
				menu.show(e);
			}
			
		};
		
		InterfaceTreeNode node = 
			new InterfaceTreeNode(new CollectionTreeNode("/mgui/resources/icons/pipelines/pipeline_set_20.png", "Pipelines"));
		node.setPopupMenu(handler.getPopupMenu());
		ArrayList<String> keys = new ArrayList<String>(pipelines.keySet());
		Collections.sort(keys);
		
		for (int i = 0; i < keys.size(); i++)
			node.add(pipelines.get(keys.get(i)).issueTreeNode());
		
		pipelinesNode = node;
		
		return node;
		
		
	}
	
	/************************************
	 * Returns a list of the pipelines contained in this workspace.
	 * 
	 * @return
	 */
	public ArrayList<InterfacePipeline> getPipelines(){
		ArrayList<InterfacePipeline> pipes = new ArrayList<InterfacePipeline>(pipelines.values());
		Collections.sort(pipes, new Comparator<InterfacePipeline>(){

			@Override
			public int compare(InterfacePipeline p1, InterfacePipeline p2) {
				return p1.getName().compareTo(p2.getName());
			}
			
		});
		
		return pipes;
	}
	
	/*************************************
	 * Returns the pipeline with the given name.
	 * 
	 * @param name
	 * @return
	 */
	public InterfacePipeline getPipeline(String name){
		return pipelines.get(name);
	}
	
	/**************************************
	 * Adds a pipeline to this workspace.
	 * 
	 * @param pipeline
	 * @return <code>false</code> if a pipeline with the same name already exists.
	 */
	public boolean addPipeline(InterfacePipeline pipeline){
		if (pipelines.containsKey(pipeline.getName())) return false;
		pipelines.put(pipeline.getName(), pipeline);
		pipeline.addStaticListener(this);
		if (pipelinesNode != null)
			objectTree.getModel().nodeStructureChanged(pipelinesNode);
		updateTreeNodes();
		return true;
	}
	
	public void removePipeline(InterfacePipeline pipeline){
		if (!pipelines.containsKey(pipeline.getName())) return;
		pipelines.remove(pipeline.getName());
		pipeline.removeStaticListener(this);
		if (pipelinesNode != null)
			objectTree.getModel().nodeStructureChanged(pipelinesNode);
		updateTreeNodes();
	}
	
	public void pipelineUpdated(StaticPipelineEvent event){
		switch (event.getType()){
			case TaskRenamed:
				String old_name = (String)event.getPreviousValue();
				InterfacePipeline pipeline = pipelines.get(old_name);
				if (pipeline != null && pipeline.equals(event.getTask())){
					pipelines.remove(old_name);
					pipelines.put(pipeline.getName(), pipeline);
					}
				break;
			}
	}
	
	public InterfaceTreeNode getDynamicModelNode(){
		InterfaceTreeNode modelNode = 
			new InterfaceTreeNode(new CollectionTreeNode("/mgui/resources/icons/dynamic_model_set_20.png", "Dynamic Models"));
		for (int i = 0; i < models.size(); i++)
			modelNode.add(models.get(i).issueTreeNode());
		return modelNode;
	}
	
	//class for rendering collection nodes
	public static class CollectionTreeNode extends AbstractInterfaceObject 
								implements TreeObject,
										   IconObject{

		String icon_file, label; 
		
		public CollectionTreeNode(String icon_file, String label){
			this.icon_file = icon_file;
			this.label = label;
		}
		
		@Override
		public Icon getObjectIcon() {
			java.net.URL imgURL = CollectionTreeNode.class.getResource(icon_file);
			if (imgURL != null)
				return new ImageIcon(imgURL);
			else
				InterfaceSession.log("Cannot find resource: " + icon_file);
			return null;
		}
		
		@Override
		public String getTreeLabel() {
			return label;
		}
	}
	
	public InterfaceTreeNode getShapeModelNode(){
		InterfaceTreeNode shapeModelNode = 
			new InterfaceTreeNode(new CollectionTreeNode("/mgui/resources/icons/shape_model_set_20.png", "Shape Models")){
			
				public InterfacePopupMenu getPopupMenu() {
					if (popup_menu != null)
						return popup_menu;
					
					if (getUserObject() instanceof PopupMenuObject)
						return ((PopupMenuObject)getUserObject()).getPopupMenu();
					
					InterfacePopupMenu menu = new InterfacePopupMenu(this);
					menu.addMenuItem(new JMenuItem("Add Shape Model"));
					
					return menu;
				}

				public void handlePopupEvent(ActionEvent e) {
					
					if (!(e.getSource() instanceof JMenuItem)) return;
					JMenuItem item = (JMenuItem)e.getSource();
					
					if (item.getActionCommand().equals("Add Shape Model")){
						
						String name = JOptionPane.showInputDialog("Enter name for new shape model:");
						if (name == null) return;
						
						ShapeModel3D model = new ShapeModel3D(name);
						addShapeModel(model, false);
						return;
						}
						
				}
				
				};
		for (int i = 0; i < shapeModels.size(); i++){
			ShapeModel3D thisModel = shapeModels.get(i);
			InterfaceTreeNode shapeNode = thisModel.issueTreeNode();
			shapeModelNode.add(shapeNode);
			}
		
		return shapeModelNode;
	}
	
	public InterfaceTreeNode getNameMapNode(){
		ArrayList<NameMap> nameMaps = InterfaceEnvironment.getNameMaps();
		//InterfaceTreeNode nameNode = new InterfaceTreeNode("Name Maps");
		InterfaceTreeNode nameNode = 
			new InterfaceTreeNode(new CollectionTreeNode("/mgui/resources/icons/name_map_set_20.png", "Name Maps"));
		for (int i = 0; i < nameMaps.size(); i++)
			nameNode.add(nameMaps.get(i).issueTreeNode());
		return nameNode;
	}
	
	public InterfaceTreeNode getDataSourceNode(){
		//if (dataSourceNode == null)
		InterfaceTreeNode dataSourceNode = 
			new InterfaceTreeNode(new CollectionTreeNode("/mgui/resources/icons/data_source_set_20.png", "Data Sources"));
		//dataSourceNode.removeAllChildren();
		for (int i = 0; i < dataSources.size(); i++)
			dataSourceNode.add(dataSources.get(i).issueTreeNode());
		return dataSourceNode;
	}
		
	public InterfaceTreeNode getGraphNode(){
		//if (graphNode == null)
		//InterfaceTreeNode graphNode = new InterfaceTreeNode("Graphs");
		InterfaceTreeNode graphNode = 
			new InterfaceTreeNode(new CollectionTreeNode("/mgui/resources/icons/graph_set_20.png", "Graphs"));
		//graphNode.removeAllChildren();
		for (int i = 0; i < graphs.size(); i++)
			graphNode.add(graphs.get(i).issueTreeNode());
		return graphNode;
	}
	
	public InterfaceTreeNode getVariablesNode(){
		//variablesNode = new InterfaceTreeNode("Variables");
		InterfaceTreeNode variablesNode = 
			new InterfaceTreeNode(new CollectionTreeNode("/mgui/resources/icons/variable_set_20.png", "Variables"));
		
		for (int i = 0; i < variables.size(); i++)
			variablesNode.add(variables.get(i).issueTreeNode());
		return variablesNode;
	}
	
	public InterfaceTreeNode getQueriesNode(){
		//queriesNode = new InterfaceTreeNode("Queries");
		InterfaceTreeNode queriesNode = 
			new InterfaceTreeNode(new CollectionTreeNode("/mgui/resources/icons/query_set_20.png", "Queries"));
		
		for (int i = 0; i < queries.size(); i++)
			queriesNode.add(queries.get(i).issueTreeNode());
		return queriesNode;
	}
	
	
	
	public void updateDataSourceNode(){
		updateTreeNodes();
		
	}
	
	@Override
	public void updateTreeNodes(){
		super.updateTreeNodes();
		updateObjectTree();
	}
	
	public void updateObjectTree(){
		if (objectTree == null) return;
		objectTree.updateDisplay();
	}
	
	//********************Utilities****************************
	
	/******************************
	 * Searches all InterfaceObjects in this panel for one which matches
	 * <code>object_name</code> and <code>object_class</code>; returns the
	 * first instance, or null if not found (or some exception is encountered)
	 * 
	 * @param object_name
	 * @param object_class
	 */
	public InterfaceObject findInterfaceObjectForName(String object_name, String object_class){
		
		try{
			Class clazz = Class.forName(object_class);
			
			if (InterfaceShape.class.isInstance(clazz.newInstance())){
				//search shape model
				Shape3DInt shape = display_panel.getCurrentShapeSet().getShape(object_name, true);
				if (shape != null) return shape;
				//TODO: search 2D shapes
				}
			
			//TODO: search other objects
			
			return null;
		}catch (Exception e){
			e.printStackTrace();
			//all exceptions silently return null;
			return null;
			}
		
	}
	
	
	
	
	//******************** Events *******************************
	public void endSession(){
		//TODO: confirmation and clean-up
		
		display_panel.close();
	}
	
	
	@Override
	public String toString(){
		return "Workspace";
	}
	
	/******************************************
	 * Fires a {@link TabbedDisplayEvent} for each loaded graphics window
	 */
	@Override
	public void tabbedDisplayChanged(TabbedDisplayEvent e) {
		ArrayList<InterfacePanel> panels = getInterfacePanels();
		for (int i = 0; i < panels.size(); i++)
			if (panels.get(i) instanceof TabbedDisplayListener)
				((TabbedDisplayListener)panels.get(i)).tabbedDisplayChanged(e);
		
	}
	
	
	
	
	
	//************************* XML Stuff ****************************
	
	@Override
	public String getDTD() {
		return "";
	}

	@Override
	public String getLocalName() {
		return "InterfaceWorkspace";
	}

	@Override
	public String getShortXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXMLSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleXMLElementEnd(String localName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLString(String s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progressBar) throws IOException {
		
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progressBar) throws IOException {
		// Writes (Explicit or reference, depending on objects):
		
		// 1. Data Sources
		// 2. Variables
		// 3. Projects
		// 4. Shape Models
		// 5. Graphs
		// 6. Queries
		// 7. Views
		// 8. Videos
		// 9. Dynamic Models
		// 10. Display Panel
		
		// NB. it is important to write/load in the correct order,
		//     as some objects may depend upon existing data
		//     sources or projects to load references properly
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		writer.write(_tab + "<InterfaceWorkspace\n" +
							_tab2 + "name = '" + getName() + "'\n" +
							_tab2 + "timestamp = '" + InterfaceEnvironment.getNow("hh:mm:ss z dd.MM.yyyy") + "'\n" +
							_tab + ">\n");
		
		// 1. Data Sources
		writer.write(_tab2 + "<DataSources>\n");
		
		for (int i = 0; i < dataSources.size(); i++)
			dataSources.get(i).writeXML(tab + 2, writer, options, progressBar);
		
		writer.write(_tab2 + "</DataSources>\n");
		
		// 2. Variables
		// 3. Projects
		
		// 4. Shape models
		writer.write(_tab2 + "<ShapeModels>\n");
		
		for (int i = 0; i < shapeModels.size(); i++)
			shapeModels.get(i).writeXML(tab + 2, writer, options, progressBar);
		
		writer.write(_tab2 + "</ShapeModels>\n");
		
		// 5. Graphs
		writer.write(_tab2 + "<Graphs>\n");
		
		for (int i = 0; i < graphs.size(); i++)
			graphs.get(i).writeXML(tab + 2, writer, options, progressBar);
		
		writer.write(_tab2 + "</Graphs>\n");
		// 6. Queries
		// 7. Views
		writer.write(_tab2 + "<Views3D>\n");
		
		ArrayList<View3D> views = new ArrayList<View3D>(views3D);
		for (int i = 0; i < views.size(); i++)
			views.get(i).writeXML(tab + 2, writer, options, progressBar);
		
		writer.write(_tab2 + "</Views3D>\n");
		
		// 8. Videos
		// 9. Dynamic Models
		
		// 10. Display Panel
		writer.write(_tab2 + "<CurrentDisplayPanel>\n");
		if (display_panel != null){
			display_panel.writeXML(tab + 2, writer, options, progressBar);
			}
		writer.write(_tab2 + "</CurrentDisplayPanel>\n");
		
		writer.write(_tab + "</InterfaceWorkspace>\n");
		
	}

	@Override
	public void writeXML(int tab, Writer writer) throws IOException {
		writeXML(tab, writer, null);
	}
	
	
}