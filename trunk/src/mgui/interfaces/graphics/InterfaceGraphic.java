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

package mgui.interfaces.graphics;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.tree.DefaultMutableTreeNode;
import org.jogamp.vecmath.Point2f;

import mgui.interfaces.InterfaceMouseListener;
import mgui.interfaces.InterfaceMouseObject;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.InterfaceStatusBarPanel;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.graphs.InterfaceGraphDisplay;
import mgui.interfaces.maps.Map;
import mgui.interfaces.maps.MapEvent;
import mgui.interfaces.maps.MapListener;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.plots.InterfacePlotDisplay;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.tables.InterfaceDataTable;
import mgui.interfaces.tools.Tool;
import mgui.interfaces.tools.ToolEvent;
import mgui.interfaces.tools.ToolInputAdapter;
import mgui.interfaces.tools.ToolInputMouseAdapter;
import mgui.interfaces.tools.ToolListener;
import mgui.interfaces.tools.Toolable;
import mgui.interfaces.xml.XMLObject;
import mgui.io.imaging.ImagingIOFunctions;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.io.util.IoFunctions;
import mgui.resources.icons.IconObject;


/*************************
 * The base class for all graphical interface windows. Has a list of implementing
 * subclasses and has several methods for determining a particular instance type. Methods
 * for setting interface-specific tools are also declared in this class.
 * 
 * <p>TODO: make this class more generic; either by building a list of implementing 
 * classes at runtime, or by having them set by the instantiating frame.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public abstract class InterfaceGraphic<T extends Tool> extends InterfacePanel implements Toolable<T>,
																		 InterfaceMouseObject,
																		 IconObject,
																		 MapListener,
																		 ToolListener,
																		 XMLObject,
																		 Comparable<InterfaceGraphic<T>>{

		//public GraphicType type = GraphicType.Graphic2D;
	protected ArrayList<GraphicMouseListener> status_listeners = new ArrayList<GraphicMouseListener>();
	protected HashMap<GraphicMouseListener, Integer> status_map = new HashMap<GraphicMouseListener, Integer>();
	public ToolInputAdapter toolInputAdapter = new ToolInputMouseAdapter();
	public boolean excludeToSelection;
	protected Map theMap;
	protected boolean init_once = false;
	protected ArrayList<InterfaceGraphicListener> graphic_listeners = new ArrayList<InterfaceGraphicListener>();
	public InterfaceGraphicWindow title_panel;
	protected HashMap<String, Object> post_render_tasks = new HashMap<String, Object>();
	
	protected File last_screen_shot_file = null;
	
	@Override
	public Icon getObjectIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/window_17.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/window_17.png");
		return null;
	}
	
	@Override
	public int compareTo(InterfaceGraphic panel) {
		return getName().compareTo(panel.getName());
	}
	
	//for super calls
	private final Icon getIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/window_17.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/window_17.png");
		return null;
	} 
	
	@Override
	protected void init(){
		if (init_once) return;
		init_once = true;
		type = "Graphic Panel";
		attributes.add(new Attribute<String>("Name", "no-name"));
		attributes.add(new Attribute<Color>("Background", Color.WHITE));
		attributes.addAttributeListener(this);
		
	}
	
	/*********************************************
	 * Return the mapped coordinates, depending on the InterfaceGraphic window, for the given
	 * screen coordinates. 
	 * 
	 * @param p
	 * @return mapped coordinates; or <code>null</code> if not applicable.
	 */
	public Point2f getMouseCoords(Point p){
		return null;
	}
	
	/*********************************************
	 * Returns a list of status messages, to be displayed in the status bar
	 * 
	 * @return
	 */
	public ArrayList<String> getStatusMessages(Point p){
		return new ArrayList<String>();
	}
	
	public void toolStateChanged(ToolEvent e){}
	
	public void toolDeactivated(ToolEvent e){}
	
	//TODO eliminate this to allow a more extensible API...!
	public enum GraphicType{
		Graphic3D,
		Graphic2D,
		DataTable,
		GraphDisplay,
		PlotDisplay;
	}
	
	/*
	public String getWindowTitle(){
		return getTitle();
	}
	*/
	
	public void addGraphicListener(InterfaceGraphicListener l){
		graphic_listeners.add(l);
	}
	
	public void removeGraphicListener(InterfaceGraphicListener l){
		graphic_listeners.remove(l);
	}
	
	protected void fireGraphicListeners(){
		ArrayList<InterfaceGraphicListener> temp = new ArrayList<InterfaceGraphicListener>(graphic_listeners);
		for (int i = 0; i < temp.size(); i++)
			temp.get(i).graphicUpdated(new GraphicEvent(this));
	}
	
	public static String getTypeStr(GraphicType g){
		if (g == GraphicType.Graphic3D)
			return "Graphic3D";
		if (g == GraphicType.Graphic2D)
			return "Graphic2D";
		if (g == GraphicType.DataTable)
			return "Data Table";
		if (g == GraphicType.GraphDisplay)
			return "Graph Display";
		if (g == GraphicType.PlotDisplay)
			return "Plot Display";
		return "";
	}
	
	public static GraphicType getType(String str){
		if (str.compareTo("Graphic3D") == 0)
			return GraphicType.Graphic3D;
		if (str.compareTo("Graphic2D") == 0)
			return GraphicType.Graphic2D;
		if (str.compareTo("Data Table") == 0)
			return GraphicType.DataTable;
		if (str.compareTo("Graph Display") == 0)
			return GraphicType.GraphDisplay;
		if (str.compareTo("Plot Display") == 0)
			return GraphicType.PlotDisplay;
		return null;
	}
	
	//TODO: update to make generic with class object
	public static ArrayList<InterfaceGraphic<?>> getSourceTypes(){
		ArrayList<InterfaceGraphic<?>> retList = new ArrayList<InterfaceGraphic<?>>(5);
		retList.add(new InterfaceGraphic2D());
		retList.add(new InterfaceGraphic3D());
		retList.add(new InterfaceDataTable());
		retList.add(new InterfaceGraphDisplay());
		retList.add(new InterfacePlotDisplay());
		return retList;
	}
	
	@Override
	public void addMouseListener(InterfaceMouseListener thisObj){
		GraphicMouseListener g = (GraphicMouseListener)thisObj;
		addMouseListener(g.getMouseListener());
		addMouseMotionListener(g.getMouseListener());
		addMouseWheelListener(g.getMouseWheelListener());
		status_listeners.add(g);
	}
	
	@Override
	public void removeMouseListener(InterfaceMouseListener thisObj){
		GraphicMouseListener g = (GraphicMouseListener)thisObj;
		removeMouseListener(g.getMouseListener());
		removeMouseMotionListener(g.getMouseListener());
		removeMouseWheelListener(g.getMouseWheelListener());
		status_listeners.remove(g);
	}
	
	
	public void setExcludeToSelection(boolean ex){
		excludeToSelection = ex;
	}
	
	public Map getMap(){
		return theMap;
	}
	
	public void setMap(Map m){
		if (theMap != null)
			theMap.removeMapListener(this);
		theMap = m;
		theMap.addMapListener(this);
	}
	
	public void mapUpdated(MapEvent e){
		
	}
	
	//determine if the given object is displayable by this type of graphic interface
	public boolean isDisplayable(Object obj){
		return false;
	}
	
	@Override
	public void setStatusBar(InterfaceStatusBarPanel status_bar){
		if (statusBar != null){
			//replace its components from listeners list
			statusBar.removeListeners(status_listeners);
			status_map.clear();
			}
		
		Component[] clist = status_bar.getComponents();
		for (int i = 0; i < clist.length; i++)
			if (clist[i] instanceof GraphicMouseListener){
				addStatusListener((GraphicMouseListener)clist[i]);
				status_map.put((GraphicMouseListener)clist[i], i);
				}
	}
	
	/*****************************************************
	 * Notifies this window to update the given status text box with information
	 * as it sees fit. Boxes are mapped to indices within this class; By default, 
	 * if the box's index is 1, the value will be set to the name of the current
	 * window; otherwise it will be set to an empty string. Overriding classes
	 * should call this super method first.
	 * 
	 * @return The index of the box; mostly useful for internal purposes
	 * @param box Text box to update
	 * 
	 */
	public int updateStatusBox(InterfaceGraphicTextBox box, MouseEvent e){
		
		Integer index = this.status_map.get(box);
		if (index == null) return -1;
		
		switch (index){
		
			case 0:
				// Current window
				box.setText("Current window: " + this.getName());
				return index;
		
			}
		
		return index;
	}
	
	public void addStatusListener(GraphicMouseListener thisObj){
		addMouseListener(thisObj);
		thisObj.setParentWindow(this);
		thisObj.setMap(theMap);
	}
	
	public void removeMouseObject(GraphicMouseListener thisObj){
		removeMouseListener(thisObj);
	}
	
	@Override
	public String getName(){
		if (attributes == null) return null; 
		return (String)attributes.getValue("Name");
	}
	
	@Override
	public void setName(String name){
		super.setName(name);
		attributes.setValue("Name", name);
	}
	
	public void setBackgroundColour(Color c){
		attributes.setValue("Background", c);
	}
	
	public Color getBackgroundColour(){
		return (Color)attributes.getValue("Background");
	}
	
	/*****************
	 * Returns a tree node containing a list of objects displayable by a class of
	 * InterfaceGraphic. Must therefore be overriden by that class.
	 * @param p InterfaceDisplayPanel containing displayable data objects
	 * @return DefaultMutableTreeNode with a tree list of displayable objects
	 */
	public DefaultMutableTreeNode getDisplayObjectsNode(){
		return new DefaultMutableTreeNode("Not impl.");
		//return getTreeNode(p.modelSet);
	}
	
	protected DefaultMutableTreeNode getTreeNode(ShapeSet3DInt set){
		DefaultMutableTreeNode thisNode = new DefaultMutableTreeNode(set);
		for (int i = 0; i < set.members.size(); i++){
			if (ShapeSet3DInt.class.isInstance(set.members.get(i)))
				thisNode.add(getTreeNode((ShapeSet3DInt)set.members.get(i)));
			else if (isDisplayable(set.members.get(i)))
				thisNode.add(new DefaultMutableTreeNode(set.members.get(i)));
			}
		return thisNode;
	}
	
	@Override
	public InterfacePopupMenu getPopupMenu(){
		//temp

		if (title_panel != null){
			InterfacePopupMenu menu = title_panel.getPopupMenu();
			popup_length = title_panel.getPopupLength();
			return menu;
			}
		
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		
		menu.addMenuItem(new JMenuItem("Graphic Window", getIcon()));
		menu.add(new JSeparator(), 1);
		menu.add(new JSeparator(), 1);
		
		menu.addMenuItem(new JMenuItem("Maximize"));
		//menu.addMenuItem(new JMenuItem("Close"));
		menu.addMenuItem(new JMenuItem("Snapshot.."));
		
		popup_length = 5;
		
		return menu;
	}
	
	private int popup_length = 5;
	
	protected int getPopupLength(){
		return popup_length;
	}
	
	@Override
	public void handlePopupEvent(ActionEvent e) {
		
		if (!(e.getSource() instanceof JMenuItem)) return;
		JMenuItem item = (JMenuItem)e.getSource();
		
		if (item.getText().equals("Snapshot..")){
			
			JFileChooser jc = null;
			if (last_screen_shot_file != null)
				jc = new JFileChooser(last_screen_shot_file.getParentFile());
			else
				jc = new JFileChooser();
			ArrayList<String> ext = new ArrayList<String>();
			ext.add("png");
			jc.setFileFilter(IoFunctions.getFileChooserFilter(ext, "Portable Network Graphics files (*.png)"));
			jc.showSaveDialog(InterfaceSession.getSessionFrame());
			
			File file = jc.getSelectedFile();
			if (file == null) return;
			
			if (!writeSnapshotToFile(file)){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Error writing to '" + file.getAbsolutePath() + "'..", 
											  "Error taking snapshot",
											  JOptionPane.ERROR_MESSAGE);
			}else{
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Snapshot written to '" + file.getAbsolutePath() + "'..", 
											  "Window Snapshot",
											  JOptionPane.INFORMATION_MESSAGE);
				last_screen_shot_file = file;
				}
			return;
			}
		
//		if (item.getText().equals("Close")){
//			
//			this.destroy();
//			
//			return;
//			}
		
	}
	
	@Override
	public void mouseMoved(MouseEvent e){
		MouseEvent clone = clone_mouse_event(e);
		clone.setSource(this);
		for (int i = 0; i < this.status_listeners.size(); i++)
			this.status_listeners.get(i).getMouseListener().mouseMoved(clone);
	}
	
	@Override
	public void mouseDragged(MouseEvent e){
		MouseEvent clone = clone_mouse_event(e);
		clone.setSource(this);
		for (int i = 0; i < this.status_listeners.size(); i++)
			this.status_listeners.get(i).getMouseListener().mouseDragged(clone);
	}
	
	private MouseEvent clone_mouse_event(MouseEvent e){
		return new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiers(), 
				e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
	}
	
	/**********************************************************
	 * Writes a snapshot of this window to file as a Portable Network Graphics (png) image.
	 * 
	 * @param file
	 * @return
	 */
	public boolean writeSnapshotToFile(final File file){
		BufferedImage image = new  BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = image.createGraphics();
		paint(g);
		g.dispose();
		return ImagingIOFunctions.writeImageToPng(image, file);
	}
	
	
	@Override
	public void writeXML(int tab, Writer writer) throws IOException {
		writeXML(tab, writer, null);
	}
	
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException{
		writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}
	
	@Override
	public T getCurrentTool() {
		return null;
	}

	/***********************************
	 * Sets the current tool. Use this method if the type of the InterfaceGraphic object is unknown.
	 * 
	 * @param tool
	 * @return <code>false</code> if this object is not toolable by <code>tool</code>. 
	 */
	public boolean setTool(Tool tool){
		if (isToolable(tool))
			return setCurrentTool((T)tool);
		else
			return false;
	}
	
	@Override
	public <E extends T> boolean setCurrentTool(E tool) {
		return false;
	}

	@Override
	public <E extends T> boolean setDefaultTool(E tool) {
		return false;
	}
	
	@Override
	public boolean isToolable(Tool tool) {
		return false;
	}

	
}