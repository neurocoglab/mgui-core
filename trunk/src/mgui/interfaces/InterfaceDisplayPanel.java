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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;

import mgui.interfaces.attributes.AttributeDialogBox;
import mgui.interfaces.frames.SessionFrame;
import mgui.interfaces.graphics.GraphicMouseListener;
import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.graphics.InterfaceGraphic2D;
import mgui.interfaces.graphics.InterfaceGraphic3D;
import mgui.interfaces.graphics.InterfaceGraphicWindow;
import mgui.interfaces.graphics.InterfaceGraphicWindow.GraphicButton;
import mgui.interfaces.graphics.WindowContainer;
import mgui.interfaces.graphics.WindowEvent;
import mgui.interfaces.graphs.InterfaceGraphDisplay;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.Shape2DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeModel3DListener;
import mgui.interfaces.shapes.ShapeModelEvent;
import mgui.interfaces.shapes.ShapeSet2DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeListener;
import mgui.interfaces.tools.Tool;
import mgui.interfaces.tools.Toolable;
import mgui.interfaces.tools.graphics.Tool2D;
import mgui.interfaces.tools.graphics.Tool3D;
import mgui.interfaces.tools.graphics.ToolDZoom2D;
import mgui.interfaces.tools.graphics.ToolMouseOrbit3D;
import mgui.interfaces.tools.graphs.ToolGraph;
import mgui.interfaces.tools.graphs.ToolGraphTransform;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.trees.InterfaceTreePanel;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;

import org.xml.sax.Attributes;


/******************************
 * Main interface panel for displaying data graphically. Contains a hierarchical set of
 * {@linkplain InterfaceGraphic} objects which it displays in a particular order and size, based upon
 * the number of current windows. The framework is a set of embedded {@linkplain InterfaceSplitPane}s,
 * each of which can contain further split panes. InterfaceDisplayPanel also has a reference to the
 * 3D model shape set (ShapeSet3DInt modelSet), a list of data source shapeModels, a list of shape
 * selection sets, and a tree containing nodes for all of these objects. InterfaceGraphic
 * type-specific tools can also be set through this interface, affecting all relevant
 * windows.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class InterfaceDisplayPanel extends InterfacePanel implements ActionListener,
																	 XMLObject,
																	 Toolable<Tool>,
																	 WindowContainer,
																	 SplitPanelListener,
																	 ShapeModel3DListener{
	
	//protected ArrayList<InterfaceGraphicWindow> windows = new ArrayList<InterfaceGraphicWindow>();
	protected InterfaceGraphicWindow window;		// For single window
	//protected InterfaceSplitPanel split_panel;
	
	protected ArrayList<JLabel> titles = new ArrayList<JLabel>();
	protected Tool currentTool;
												//model
	
	//protected AttributeList attributes = new AttributeList();
	public boolean toolLock;
	public ArrayList<SelIDRef> selectionIndex = new ArrayList<SelIDRef>();
	public boolean excludeToSelection;
	protected InterfaceTreePanel objectTree;
	public ShapeSelectionSet currentSelection;
	protected TreeSet<GraphicMouseListener> mouse_listeners = new TreeSet<GraphicMouseListener>();
	//private int expandedPanel = -1;
	private InterfaceGraphicWindow expanded_window = null;
	private InterfaceGraphicWindow unexpanded_window = null;
	private InterfaceSplitPanel expanded_from_split_panel = null;
	private int expanded_from_split_side = 0;
	private double expanded_from_split_ratio = 0.5f;
	private boolean isDestroyed = false;
	
	protected ShapeModel3D currentModel;
	//protected ShapeSet3DInt modelSet;											//model
	protected ArrayList<DisplayPanelListener> displayPanelListeners = new ArrayList<DisplayPanelListener>();
	
	public InterfaceFrame parentFrame;
	protected InterfaceWorkspace workspace;
	
	
	protected transient InterfaceGraphic<?> last_added_panel, last_removed_panel;
	
	protected InterfaceDisplayPanel parent_panel;
	
	protected int last_split_orientation = JSplitPane.VERTICAL_SPLIT;
	
	public InterfaceDisplayPanel(SessionFrame frame){
		super();
		parentFrame = frame;
		workspace = InterfaceSession.getWorkspace();
		setLayout(new GridLayout(1,0));
		setBorder(BorderFactory.createLineBorder(Color.BLUE));
		init();
	}
	
	@Override
	protected void init(){
		//set attributes
		//setCurrentShapeSet(new ShapeSet3DInt());
	}
	
	
	
	@Override
	public String getTreeLabel(){
		//return "Display Panel";
		return getName();
	}
	
	@Override
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfaceDisplayPanel.class.getResource("/mgui/resources/icons/display_panel_17.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/display_panel_17.png");
		return null;
	}
	
	public void addDisplayPanelListener(DisplayPanelListener l){
		for (int i = 0; i < displayPanelListeners.size(); i++)
			if (displayPanelListeners.get(i) == l) return;
		displayPanelListeners.add(l);
	}
	
	public void removeDisplayPanelListener(DisplayPanelListener l){
		displayPanelListeners.remove(l);
	}
	
	public InterfaceFrame getParentFrame(){
		return parentFrame;
	}
	
	@Override
	public void destroy(){
		isDestroyed = true;
		ArrayList<InterfaceGraphicWindow> windows = this.getWindows();
		for (int i = 0; i < windows.size(); i++)
			windows.get(i).destroy();
	}
	
	@Override
	public boolean isDestroyed(){
		return isDestroyed;
	}
	
	public InterfaceWorkspace getWorkspace(){
		return workspace;
	}
	
	public ShapeModel3D getCurrentShapeModel(){
		if (parent_panel != null) return parent_panel.getCurrentShapeModel();
		return currentModel;
	}
	
	public void setCurrentShapeModel(ShapeModel3D model){
		if (currentModel != null){
			currentModel.removeModelListener(this);
			currentModel.setDisplayPanel(null);
			ShapeSet3DInt model_set = currentModel.getModelSet();
			if (model_set != null)
				model_set.removeShapeListener(this);
			}
		currentModel = model;
		if (model == null) return;
		model.setDisplayPanel(this);
		model.addModelListener(this);
	}
	
	@Override
	public void shapeModelChanged(ShapeModelEvent event) {
		
		switch (event.type){
			case ModelDestroyed:
				this.setCurrentShapeModel(null);
				return;
			}
		
		
	}
	
	public ShapeSet3DInt getCurrentShapeSet(){
		if (currentModel == null) return null;
		return currentModel.getModelSet();
	}
	
	public void addShapeListener(ShapeListener s){
		ShapeSet3DInt modelSet = this.getCurrentShapeSet();
		if (modelSet != null)
			modelSet.addShapeListener(s);
	}
	
	public boolean addShapeInt(Shape3DInt thisShape){
		ShapeSet3DInt modelSet = this.getCurrentShapeSet();
		if (modelSet == null) return false;
		modelSet.addShape(thisShape, true);
		return true;
	}
	
	@Override
	public void splitPanelChanged(SplitPanelEvent e){
		
		// Update display to reflect it...
		switch (e.getType()){
		
			case WindowRemoved:
				
				// If this split panel has only one 
				
				break;
		
		
			}
		
		
		this.fireDisplayPanelChanged(new DisplayPanelEvent(this, DisplayPanelEvent.EventType.SplitPanelChanged));
	}
	
	/************************************************************
	 * Returns a list of the windows contained in this display panel. Breadth-first, if this panel contains
	 * split panels.
	 * 
	 * @return
	 */
	public ArrayList<InterfaceGraphicWindow> getWindows(){
		
		ArrayList<InterfaceGraphicWindow> _windows = new ArrayList<InterfaceGraphicWindow>();
		
		if (window != null){
			if (window.getPanel() != null){
				_windows.add(window);
			}else if (window instanceof InterfaceSplitPanel){
				_windows.addAll(((InterfaceSplitPanel)window).getWindowsBreadthFirst());
				}
			}
		
		return _windows;
		
	}
	
	/************************************************************
	 * Returns a list of the windows contained in this display panel. Depth-first, if this panel contains
	 * split panels.
	 * 
	 * @return
	 */
	public ArrayList<InterfaceGraphicWindow> getWindowsDepthFirst(){
		
		ArrayList<InterfaceGraphicWindow> _windows = new ArrayList<InterfaceGraphicWindow>();
		
		if (window != null){
			if (window.getPanel() != null){
				_windows.add(window);
			}else if (window instanceof InterfaceSplitPanel){
				_windows.addAll(((InterfaceSplitPanel)window).getWindowsDepthFirst());
				}
			}
		
		return _windows;
		
	}
	
	public void addGraphicMouseListener(GraphicMouseListener s){
		this.mouse_listeners.add(s);
		ArrayList<InterfaceGraphicWindow> windows = getWindows();
		
		for (int i = 0; i < windows.size(); i++)
			if (windows.get(i).getPanel() instanceof InterfaceMouseObject)
				((InterfaceMouseObject)windows.get(i).getPanel()).addMouseListener(s);
		
	}
	
	public void removeGraphicMouseListener(GraphicMouseListener s){
		this.mouse_listeners.remove(s);
		ArrayList<InterfaceGraphicWindow> windows = getWindows();
		
		for (int i = 0; i < windows.size(); i++)
			if (windows.get(i).getPanel() instanceof InterfaceMouseObject)
				((InterfaceMouseObject)windows.get(i).getPanel()).removeMouseListener(s);
	}

	
	//draw canvas here
	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		paintChildren(g);
	}
	
	/****************************
	 * Retrieves the last added panel
	 * 
	 * @return
	 */
	public InterfaceGraphic<?> getLastAddedPanel(){
		return this.last_added_panel;
	}
	
	/****************************
	 * Retrieves the last removed panel
	 * 
	 * @return
	 */
	public InterfaceGraphic<?> getLastRemovedPanel(){
		return this.last_removed_panel;
	}
	
	
	
	/**********************************************************
	 * Returns a new split panel, determined as the first non-split window encountered, searching through
	 * the nested stack of split panels. This ensures that the highest-level window is always the next to be
	 * split by default.
	 * 
	 * @return
	 */
	protected InterfaceSplitPanel getNextSplitPanel(InterfaceGraphicWindow new_window){
		
		if (window == null) return null;
		
		if (!(window instanceof InterfaceSplitPanel)){
			InterfaceSplitPanel split_panel = new InterfaceSplitPanel(JSplitPane.HORIZONTAL_SPLIT, window, new_window);
			split_panel.addSplitPanelListener(this);
			split_panel.setParentPanel(this);
			window = split_panel;
			return split_panel;
			}
		
		InterfaceGraphicWindow window_to_split = null;
		InterfaceSplitPanel split_panel = null;
		
		if (window instanceof InterfaceSplitPanel){
			InterfaceSplitPanel panel = (InterfaceSplitPanel)window;
			ArrayList<InterfaceGraphicWindow> _windows = panel.getWindowsBreadthFirst();
			if (_windows.size() > 0){
				window_to_split = _windows.get(0);
				// Alternate orientation
				InterfaceSplitPanel parent = panel.getParent(window_to_split);
				int split_orientation = parent.getSplitOrientation() - 1;
				if (split_orientation < 0) split_orientation = 1;
				split_panel = new InterfaceSplitPanel(split_orientation, window_to_split, new_window);
				split_panel.addSplitPanelListener(this);
				parent.replace(window_to_split, split_panel);
				return panel;
				}
			}
		
		// If we get here, our split panel has no windows; replace it entirely with the new window
		window = null;
		return null;
		
	}
		
	public boolean addWindow(InterfaceGraphicWindow new_window){
		return addWindow(new_window, new_window.getPanel().getName());
	}
	
	/**********************************************************
	 * Adds a panel to this display panel, using the default split position, which depends on the previous addition. 
	 * The convention for this is horizontal, then vertical, etc.
	 * 
	 * @param window
	 * @param title
	 */
	public boolean addWindow(InterfaceGraphicWindow new_window, String title){
		
		if (!isValidTitle(title)){
			InterfaceSession.log("InterfaceDisplayPanel.addWindow: Window with title '" + title + "' already exists..", 
								 LoggingType.Errors);
			return false;
			}
		
		// Get next split panel; if null, this is the first window, add as solo window; 
		// otherwise add this split panel
		InterfaceSplitPanel split_panel = getNextSplitPanel(new_window);
		if (split_panel == null){
			window = new_window;
			window.setParentPanel(this);
			}
		
		new_window.setName(title);
		initWindow(new_window);
		
		repaint();
		return true;
	}
	
	public boolean isValidTitle(String title){
		ArrayList<InterfaceGraphicWindow> windows = this.getWindows();
		for (int i = 0; i < windows.size(); i++)
			if (windows.get(i).getPanel().getName().equals(title))
				return false;
		return true;
	}
	
	private void removePanel(InterfaceGraphic<?> old_panel){
		
		if (old_panel instanceof InterfaceMouseObject){
			Iterator<GraphicMouseListener> itr = mouse_listeners.iterator();
			while (itr.hasNext())
				((InterfaceMouseObject)old_panel).removeMouseListener(itr.next());
			}
		
	}
	
	private void initWindow(InterfaceGraphicWindow new_window){
		
		new_window.getTitleButton().removeActionListener(this);
		new_window.getTitleButton().addActionListener(this);
		
		initPanel(new_window.getPanel());
		updatePanels();
		
		//new_window.addWindowListener(this);
		
		DisplayPanelEvent e = new DisplayPanelEvent(this, DisplayPanelEvent.EventType.WindowAdded);
		this.fireDisplayPanelChanged(e);
	}
	
	private void initPanel(InterfaceGraphic<?> new_panel){
		
		if (new_panel instanceof InterfaceMouseObject){
			Iterator<GraphicMouseListener> itr = mouse_listeners.iterator();
			while (itr.hasNext())
				((InterfaceMouseObject)new_panel).addMouseListener(itr.next());
			}
		
		if (statusBar != null)
			new_panel.setStatusBar(statusBar);
		
		if (getCurrentTool2D() == null)
			setCurrentTool(new ToolDZoom2D());
		else if (new_panel.isToolable(getCurrentTool2D()))
			new_panel.setTool(getCurrentTool2D());
		if (getCurrentTool3D() == null)
			setCurrentTool(new ToolMouseOrbit3D());
		else if (new_panel.isToolable(getCurrentTool3D()))
			new_panel.setTool(getCurrentTool3D());
		if (getCurrentToolGraph() == null)
			setCurrentTool(new ToolGraphTransform());
		else if (new_panel.isToolable(getCurrentToolGraph()))
			new_panel.setTool(getCurrentToolGraph());
		
		this.last_added_panel = new_panel;
		
	}
	
	/**********************************************************
	 * Returns a valid title for a new window in this display panel. If no window
	 * by the name {@code desired_name} exists, returns the same name. Otherwise,
	 * Returns that name with "-{n}" appended, where n is an integer indicating the
	 * number of additional names encountered.
	 * 
	 * @param desired_title
	 * @return
	 */
	public String getValidTitle(String desired_title){
		
		ArrayList<InterfaceGraphicWindow> g_windows = getWindows();
		
		String title = desired_title;
		int idx = 1;
		for (int i = 0; i < g_windows.size(); i++){
			if (g_windows.get(i).getName().equals(title)){
				title = desired_title + "-" + idx;
				i = -1;
				idx++;
				}
			}
		
		return title;
	}
	
	/**********************************************************
	 * Adds a new split panel to this display panel, in place of {@code window_to_split}, which must already
	 * be a child of this display panel. 
	 * 
	 * @param window_to_split		Window to be replaced by a split panel
	 * @param window_to_add			New window to share the new split panel
	 * @param orientation			One of {@code JSplitPane.HORIZONTAL_SPLIT} or {@code JSplitPane.VERTICAL_SPLIT}
	 * @param is_left				If {@code true}, {@code window_to_split} goes on the left/top; opposite if {@code false}.
	 * 
	 * @return The new split panel, or {@code null} if process failed
	 */
	public InterfaceSplitPanel splitWindow(InterfaceGraphicWindow window_to_split, InterfaceGraphicWindow window_to_add,
										   int orientation, boolean is_left){
		
		// Validate new window's name
		if (!this.isValidTitle(window_to_add.getPanel().getName())){
			InterfaceSession.log("InterfaceDisplayPanel.addWindow: Window with title '" + 
								 window_to_add.getPanel().getName() + "' already exists..", 
					 LoggingType.Errors);
			return null;
			}
		
		// In the case that there is no window
		if (window == null) return null;
		
		// In the case there is only one current window
		if (window.getPanel() != null){
			if (window != window_to_split) return null;
			
			InterfaceSplitPanel split_panel = null;
			
			if (is_left)
				split_panel = new InterfaceSplitPanel(orientation, window_to_split, window_to_add);
			else
				split_panel = new InterfaceSplitPanel(orientation, window_to_add, window_to_split);
			
			split_panel.addSplitPanelListener(this);
			split_panel.setParentPanel(this);
			
			window = split_panel;
			initWindow(window_to_add);
			
			repaint();
			return split_panel;
			}
		
		// In the case there are already multiple windows
		WindowContainer p = window_to_split.getParentPanel();
		
		if (p == null || !(p instanceof InterfaceSplitPanel)) return null;	// Shouldn't happen
		
		InterfaceSplitPanel parent = (InterfaceSplitPanel)p;
		InterfaceSplitPanel split_panel = null;
		
		if (is_left)
			split_panel = new InterfaceSplitPanel(orientation, window_to_split, window_to_add);
		else
			split_panel = new InterfaceSplitPanel(orientation, window_to_add, window_to_split);
		
		split_panel.addSplitPanelListener(this);
		
		parent.replace(window_to_split, split_panel);
		window_to_split.setParentPanel(split_panel);
		initWindow(window_to_add);
		
		this.fireDisplayPanelChanged(new DisplayPanelEvent(this, DisplayPanelEvent.EventType.WindowAdded));
		
		repaint();
		return split_panel;
	}
	
	
	
	protected void fireDisplayPanelChanged(DisplayPanelEvent e){
		for (int i = 0; i < displayPanelListeners.size(); i++)
			displayPanelListeners.get(i).displayPanelChanged(e);
	}
	
	public void addColourMap(ColourMap map){
		InterfaceEnvironment.addColourMap(map);
	}
	
	public ArrayList<ColourMap> getColourMaps(){
		return InterfaceEnvironment.getColourMaps();
	}
	
	public ArrayList<NameMap> getNameMaps(){
		return InterfaceEnvironment.getNameMaps();
	}
	
	public void updatePanels(){
		
		this.removeAll();
		revalidate();
		if (window == null){
			//revalidate();
			repaint();
			return;
		}
			
		if (expanded_window != null){
			setLayout(new GridLayout(1, 1));
			add(expanded_window);
			validate();
			repaint();
			return;
			}
		
		setLayout(new GridLayout(1, 1));
		window.setMinimumSize(new Dimension(50,50));
		add(window);
		
		validate();
		repaint();
		updateTreeNodes();
	}
	
	
	public Tool2D getCurrentTool2D(){
		ArrayList<InterfaceGraphicWindow> windows = getWindows();
		for (int i = 0; i < windows.size(); i++)
			if (windows.get(i).getPanel() instanceof InterfaceGraphic2D){
				return (Tool2D)windows.get(i).getPanel().getCurrentTool();
				}
		return null;
	}
	
	public Tool3D getCurrentTool3D(){
		ArrayList<InterfaceGraphicWindow> windows = getWindows();
		for (int i = 0; i < windows.size(); i++)
			if (windows.get(i).getPanel() instanceof InterfaceGraphic3D){
				return (Tool3D)windows.get(i).getPanel().getCurrentTool();
				}
		return null;
	}
	
	public ToolGraph getCurrentToolGraph() {
		ArrayList<InterfaceGraphicWindow> windows = getWindows();
		try{
		for (int i = 0; i < windows.size(); i++)
			if (windows.get(i).getPanel() instanceof InterfaceGraphDisplay){
				return (ToolGraph)windows.get(i).getPanel().getCurrentTool().clone();
				}
		}catch(Exception e){
			
			}
		return null;
	}
	
	//TODO clean this up...
	@Override
	public boolean setCurrentTool(Tool tool){
		if (toolLock) return false;
		
		boolean success = true;
		ArrayList<InterfaceGraphicWindow> windows = getWindows();
		
		for (int i = 0; i < windows.size(); i++){
			InterfaceGraphic<?> panel = windows.get(i).getPanel();
			if (panel.isToolable(tool))
				success &= panel.setTool(tool);
			}
		
		return success;
	}
	
	@Override
	public boolean setDefaultTool(Tool tool){
		return false;
	}
	
	@Override
	public Tool getCurrentTool(){
		return null;
	}
	
	@Override
	public boolean isToolable(Tool tool) {
		return false;
	}

	
	/*********************************
	 * Removes all panels from this display panel, and removes all mouse listeners registered on them.
	 * 
	 */
	public void resetPanels(){
		//expandedPanel = -1;
		expanded_window = null;
		
		ArrayList<InterfaceGraphicWindow> windows = getWindows();
		
		//remove listeners
		for (int i = 0; i < windows.size(); i++){
			if (windows.get(i).getPanel() instanceof InterfaceMouseObject){
				Iterator<GraphicMouseListener> itr = mouse_listeners.iterator();
				while (itr.hasNext())
					((InterfaceMouseObject)windows.get(i).getPanel()).removeMouseListener(itr.next());
				}
			windows.get(i).destroy();
			}
		
		
		windows.clear(); // = new ArrayList<InterfaceGraphicWindow>();
		updatePanels();
	}
	
	/********************************
	 * Removes the panel associated with <code>name</code>, if it exists. Also removes all listeners on this
	 * panel.
	 * 
	 * @param name
	 */
	public synchronized boolean removeWindow(String name){
		
		if (window == null) return false;
		
		// If panel is top window, remove and update
		InterfaceGraphic<?> panel = window.getPanel();
		if (panel != null){
			if (!window.getTitle().equals(name)) return false;
			if (panel instanceof InterfaceMouseObject){
				Iterator<GraphicMouseListener> itr = mouse_listeners.iterator();
				while (itr.hasNext())
					((InterfaceMouseObject)panel).removeMouseListener(itr.next());
				}
			window.getTitleButton().removeActionListener(this);
			window.removeWindowListener(this);
			window.destroy();
			expanded_window = null;
			window = null;
			this.last_removed_panel = panel;
			
			updatePanels();
			
			DisplayPanelEvent e = new DisplayPanelEvent(this, DisplayPanelEvent.EventType.WindowRemoved);
			this.fireDisplayPanelChanged(e);
			return true;
			}
		
		// Find panel + container for name
		if (window instanceof InterfaceSplitPanel){
			InterfaceSplitPanel split_panel = (InterfaceSplitPanel)window;
			InterfaceGraphicWindow window_to_remove = split_panel.findWindow(name);
			if (window_to_remove == null) return false;
			
			panel = window_to_remove.getPanel();
			window_to_remove.getTitleButton().removeActionListener(this);
			
			// If panel is expanded panel, unset expanded panel
			if (expanded_window != null && expanded_window.equals(window_to_remove))
				expanded_window = null;
			
			InterfaceSplitPanel parent = split_panel.getParent(window_to_remove);
			
			if (parent.equals(split_panel)){
				// This is the top split panel; replace with remaining window
				InterfaceGraphicWindow window_to_keep = split_panel.panel1;
				if (window_to_keep != null && window_to_keep.equals(window_to_remove)){
					window_to_keep = split_panel.panel2;
					}
				window_to_remove.removeWindowListener(this);
				window_to_remove.destroy();
				
				if (window_to_keep == null){
					expanded_window = null;
					window = null;
					this.last_removed_panel = panel;
					if (panel instanceof InterfaceMouseObject){
						Iterator<GraphicMouseListener> itr = mouse_listeners.iterator();
						while (itr.hasNext())
							((InterfaceMouseObject)panel).removeMouseListener(itr.next());
						}
					updatePanels();
					DisplayPanelEvent e = new DisplayPanelEvent(this, DisplayPanelEvent.EventType.WindowRemoved);
					this.fireDisplayPanelChanged(e);
					return true;
					}
				
				if (expanded_window != window_to_keep)
					expanded_window = null;
				
				window = window_to_keep;
				window.setParentPanel(this);
				initWindow(window_to_keep);
				this.last_removed_panel = panel;
				if (panel instanceof InterfaceMouseObject){
					Iterator<GraphicMouseListener> itr = mouse_listeners.iterator();
					while (itr.hasNext())
						((InterfaceMouseObject)panel).removeMouseListener(itr.next());
					}
				updatePanels();
				DisplayPanelEvent e = new DisplayPanelEvent(this, DisplayPanelEvent.EventType.WindowRemoved);
				this.fireDisplayPanelChanged(e);
				return true;
				
			}else{
				// This is an embedded split panel; replace in parent
				InterfaceGraphicWindow window_to_keep = parent.panel1;
				if (window_to_keep != null && window_to_keep.equals(window_to_remove)){
					window_to_keep = parent.panel2;
					}
				window_to_remove.removeWindowListener(this);
				window_to_remove.destroy();
				
				WindowContainer super_parent = parent.getParentPanel();
				if (window_to_keep != null){
					// Parent replaced by window_to_keep
					if (expanded_window != window_to_keep)
						expanded_window = null;
					if (super_parent instanceof InterfaceDisplayPanel){
						window = window_to_keep;
						window.setParentPanel(this);
					}else{
						((InterfaceSplitPanel)super_parent).replace(parent, window_to_keep);
						}
					
					parent.destroy();
					this.last_removed_panel = panel;
					if (panel instanceof InterfaceMouseObject){
						Iterator<GraphicMouseListener> itr = mouse_listeners.iterator();
						while (itr.hasNext())
							((InterfaceMouseObject)panel).removeMouseListener(itr.next());
						}
					
					updatePanels();
					DisplayPanelEvent e = new DisplayPanelEvent(this, DisplayPanelEvent.EventType.WindowRemoved);
					this.fireDisplayPanelChanged(e);
					return true;
					
					}
					
				}
			}
		
		return false;
	}
	
	public void setCurrentSectionSet(SectionSet3DInt thisSet){
		attributes.setValue("CurrentSectionSet", thisSet);
		((MguiInteger)attributes.getValue("CurrentSection")).setValue(0);
		((MguiDouble)attributes.getValue("CurrentSectionWidth")).setValue(-1);
		setShapeSet2D();
		updateDisplays();
	}
	
	public SectionSet3DInt getCurrentSectionSet(){
		return (SectionSet3DInt)attributes.getValue("CurrentSectionSet");
	}
	
	public void setObjectTree(InterfaceTreePanel treePanel){
		if (objectTree != null)
			removeDisplayListener(objectTree);
		
		objectTree = treePanel;
		treePanel.setRootNode(issueTreeNode());
		addDisplayListener(objectTree);
	}
	
	public ArrayList<InterfaceGraphicWindow> getAllWindows(){
		return getPanels();
	}
	
	/******************
	 * 
	 * @deprecated Use {@code getWindows()}
	 * @return
	 */
	public ArrayList<InterfaceGraphicWindow> getPanels(){
		return getWindows();
		//return new ArrayList<InterfaceGraphicWindow>(windows);
	}
	
	
	public void removeAllPanels(){
		if (window != null)
			window.destroy();
		window = null;
		//windows.clear();
	}
	
	
	@Override
	public void shapeUpdated(ShapeEvent e){
		
		ArrayList<InterfaceGraphicWindow> windows = getWindows();
		AttributeDialogBox current_attribute_dialog = workspace.getCurrentAttributeDialog();
		
		switch (e.eventType){
			//mainly to remove panel references to shapes which cause a memory leak  
			case ShapeRemoved:
				for (int i = 0; i < windows.size(); i++)
					windows.get(i).shapeUpdated(e);
				
				
				current_attribute_dialog.updateDialog();
				break;
				
			case ShapeAdded:
				current_attribute_dialog.updateDialog();
				break;
		}
		
	}
	
	@Override
	public void updateDisplay(){
		this.updateUI();
		updateDisplays();
	}
	
	@Override
	public void updateDisplays(){
		ArrayList<InterfaceGraphicWindow> windows = getWindows();
		for (int i = 0; i < windows.size(); i++)
			windows.get(i).getPanel().updateDisplay();
		fireDisplayListeners();
	}
	
	public void resetSelectionIndex(){
		selectionIndex = new ArrayList<SelIDRef>();
	}
	
	public ShapeSelectionSet getCurrentSelection(){
		return currentSelection;
	}
	
	public void setCurrentSelection(ShapeSelectionSet sel){
		currentSelection = sel;
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		
		if (this.parent_panel != null){
			ArrayList<InterfaceTreeNode> nodes = getPanelNodes();
			for (int i = 0; i < nodes.size(); i++)
				treeNode.addChild(nodes.get(i));
			}
		
	}
	
	public ArrayList<InterfaceTreeNode> getPanelNodes(){
		
		ArrayList<InterfaceTreeNode> nodes = new ArrayList<InterfaceTreeNode>();
		
		ArrayList<InterfaceGraphicWindow> windows = getWindows();
		
		for (int i = 0; i < windows.size(); i++)
			nodes.add(windows.get(i).getPanel().issueTreeNode());
		return nodes;
	}
	
	@Override
	public String toString(){
		return "Interface Display Panel";
	}
	
	public ShapeSet2DInt getShapeSet2D(){
		//setShapeSet2D();
		return (ShapeSet2DInt)attributes.getValue("CurrentShapeSet2D");
	}
	
	public void addShape2D(Shape2DInt thisShape){
		SectionSet3DInt thisSet = (SectionSet3DInt)attributes.getValue("CurrentSectionSet");
		int section = ((MguiInteger)attributes.getValue("CurrentSection")).getInt();
		thisSet.addShape2D(thisShape, section, true);
		setShapeSet2D();
	}
	
	public void setShapeSet2D(){
		double width = ((MguiDouble)attributes.getValue("CurrentSectionWidth")).getValue();
		SectionSet3DInt thisSet = (SectionSet3DInt)attributes.getValue("CurrentSectionSet");
		int thisSection = ((MguiInteger)attributes.getValue("CurrentSection")).getInt();
		
		if (width < 0)
			attributes.setValue("CurrentShapeSet2D", thisSet.getShapeSet(thisSection));
		else
			attributes.setValue("CurrentShapeSet2D", thisSet.getShapeSet(thisSection, width));
	}
	
	public ShapeSelectionSet addSelectionSet(ShapeSelectionSet thisSet, ShapeModel3D model){
		return addSelectionSet(thisSet, model, true);
	}
	
	/**
	 * @deprecated call model directly
	 * @param thisSet
	 * @param model
	 * @param listeners
	 * @return
	 */
	@Deprecated
	public ShapeSelectionSet addSelectionSet(ShapeSelectionSet thisSet, ShapeModel3D model, boolean listeners){
		//TODO throw exception
		if (!getWorkspace().modelExists(model)) return null;
		if (model.hasSelectionSet(thisSet)) return model.getSelectionSet(thisSet.getName());
		
		model.addSelectionSet(thisSet);
		return thisSet;
	}
	
	@Override
	public void setStatusBar(InterfaceStatusBarPanel p){
		statusBar = p;
		//update all panels
		ArrayList<InterfaceGraphicWindow> windows = getWindows();
		for (int i = 0; i < windows.size(); i++)
			windows.get(i).getPanel().setStatusBar(p);
	}
	
	public void registerProgressBar(InterfaceProgressBar bar){
		if (statusBar == null) return;
		statusBar.registerProgressBar(bar);
		updateUI();
	}
	
	public void deregisterProgressBar(){
		if (statusBar == null) return;
		statusBar.deregisterProgressBar();
		updateUI();
	}
	
	public void setCurrentSection(SectionSet3DInt thisSect, int section){
		attributes.setValue("CurrentSectionSet", thisSect);
		attributes.setValue("CurrentSection", new MguiInteger(0));
		setShapeSet2D();
	}
	
	
	
	@Override
	public InterfacePopupMenu getPopupMenu(){
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		menu.addMenuItem(new JMenuItem("Rename"));
		
		return menu;
	}
	
	@Override
	public void handlePopupEvent(ActionEvent e) {
		
		if (!(e.getSource() instanceof JMenuItem)) return;
		JMenuItem item = (JMenuItem)e.getSource();
		
		if (item.getText().equals("Rename")){
			String name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), 
													  "New name for panel:",								  
													  "Rename Display Panel", 
													  JOptionPane.QUESTION_MESSAGE);
			if (name == null) return;
			setName(name);
			updateTreeNodes();
			return;
			}
		
	}
	
	public boolean getToolLock(){
		return toolLock;
	}
	
	public void setToolLock(boolean val){
		toolLock = val;
	}
	
	/********************************************
	 * Toggles the expansion state of the window specified by {@code name}.
	 * 
	 * @param name
	 * @return
	 */
	public int toggleExpandedWindow(String name){
		
		ArrayList<InterfaceGraphicWindow> windows = getWindows();
		if (windows.size() <= 1) 
			return -1;
		
		if (expanded_window != null){
			
			//Restore if necessary
			if (expanded_from_split_panel != null){
				switch (expanded_from_split_side){
				case 0:
					expanded_from_split_panel.split_pane.setLeftComponent(expanded_window);
					break;
				case 1:
					expanded_from_split_panel.split_pane.setRightComponent(expanded_window);
					break;
					}
				expanded_from_split_panel.setSplitRatio(expanded_from_split_ratio);
				}
			
			expanded_from_split_panel = null;
			expanded_window = null;
			//button.setExpanded(false);
			updatePanels();
			this.updateUI();
			return 0;
			}
		
		for (int i = 0; i < windows.size(); i++)
			if (windows.get(i).getPanel().getName().equals(name)){
				//expandedPanel = i;
				expanded_window = windows.get(i);
				if (window instanceof InterfaceSplitPanel){
					expanded_from_split_panel = ((InterfaceSplitPanel)window).getParent(expanded_window);
					expanded_from_split_side = expanded_from_split_panel.getSide(expanded_window);
					expanded_from_split_ratio = 
							((InterfaceSplitPanel)expanded_from_split_panel).getSplitRatio();
				}else{
					expanded_from_split_panel = null;	
					}
				//button.setExpanded(true);
				updatePanels();
				this.updateUI();
				
				}
		
		return 1;
	}
	
	public void actionPerformed(ActionEvent e){
		if (e.getSource() instanceof GraphicButton){
			GraphicButton button = (GraphicButton)e.getSource();
			
			String name = button.getActionCommand();
			
			int is_expanded = toggleExpandedWindow(name);
			
			if (is_expanded == 0)
				button.setExpanded(false);
			if (is_expanded == 1)
				button.setExpanded(true);
			
			return;
			}
	}
	
	/********************************************
	 * Called when this display panel is closed
	 *
	 */
	public void close(){
		//disconnect all data sources
		//for (int i = 0; i < this.dataSources.size(); i++)
		//	dataSources.get(i).disconnect();
		
		
	}
	
	class SelIDRef{
		long ID;
		ShapeSelectionSet set;
		int index;
		public SelIDRef(long id, ShapeSelectionSet selSet, int i){
			ID = id;
			set = selSet;
			index = i;
		}
	}
	
	class SelIDComp implements java.util.Comparator<SelIDRef>{
		public int compare(SelIDRef r1, SelIDRef r2){
			if (r1.ID > r2.ID) return 1;
			if (r1.ID == r2.ID) return 0;
			return -1;
		}
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
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException {
		this.writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		// Writes (Explicit or reference, depending on objects):
		// A1. Name of current model
		// 1. Graphics Panels
		
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		ShapeModel3D model3D = this.getCurrentShapeModel();
		String model_name = "None";
		if (model3D != null)
			model_name = model3D.getName();
		
		writer.write(_tab + "<InterfaceDisplayPanel\n" +
							_tab2 + "name = '" + getName() + "'\n" +
							_tab2 + "current_shape_model = '" + model_name + "'\n" +
							_tab + ">\n");
		
		//1. Graphics panels
		writer.write(_tab2 + "<GraphicsPanels>\n");
		
		if (window != null)
			window.writeXML(tab + 2, writer, progress_bar);
		
		//for (int i = 0; i < windows.size(); i++){
		//	windows.get(i).writeXML(tab + 2, writer, progress_bar);
		//	}
		
		writer.write(_tab2 + "</GraphicsPanels>\n");
		
		writer.write(_tab + "</InterfaceDisplayPanel>\n");
		
	}

	@Override
	public void writeXML(int tab, Writer writer) throws IOException {
		writeXML(tab, writer, null);
	}

	@Override
	public void windowUpdated(WindowEvent e) {
		
		InterfaceGraphicWindow source = (InterfaceGraphicWindow)e.getSource();
		
		switch (e.getType()){
		
			case Destroyed:
				// If the top window is destroyed, we'll have to remove it
					if (!removeWindow(source.getName())){
						InterfaceSession.log("InterfaceDisplayPanel: Could not remove window '" + 
											 source.getName() + ".", 
											 LoggingType.Verbose);
						
						}
				
				return;
		
			}
		
		
	}

	@Override
	public void windowSourceChanged(WindowEvent e) {
		
		InterfaceGraphicWindow window = (InterfaceGraphicWindow)e.getSource();
		this.removePanel(window.getOldPanel());
		this.initWindow(window);
		this.fireDisplayPanelChanged(new DisplayPanelEvent(this, DisplayPanelEvent.EventType.WindowTypeChanged));
		
	}
	
}