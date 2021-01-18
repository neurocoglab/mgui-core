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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mgui.interfaces.frames.SessionFrame;
import mgui.interfaces.graphics.GraphicMouseListener;
import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.graphics.InterfaceGraphicWindow;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.tools.Tool;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;

/***********************************************************
 * This class extends {@link InterfaceDisplayPanel} in order to provide a tabbed window
 * pane, similar to a typical web browser. It extends all the functionality necessary
 * to do this.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceTabbedDisplayPanel extends InterfaceDisplayPanel implements PropertyChangeListener,
																				  DisplayPanelListener{

	HashMap<String, InterfaceDisplayPanel> tabbed_panels = new HashMap<String, InterfaceDisplayPanel>();
	JTabbedPane tabbed_pane = new JTabbedPane();
	ArrayList<TabbedDisplayListener> tab_listeners = new ArrayList<TabbedDisplayListener>();
	
	
	
	String default_tab = "Tab1";
	
	public InterfaceTabbedDisplayPanel(SessionFrame frame){
		super(frame);
		init2();
	}
	
	private void init2(){
		
		this.setLayout(new BorderLayout());
		this.add(tabbed_pane, BorderLayout.CENTER);
		this.setName("Tabbed Display Panel");
		
		tabbed_pane.addMouseListener(this);
		
		tabbed_pane.addChangeListener(new ChangeListener(){
			 public void stateChanged(ChangeEvent e) {
				 fireTabChanged();
			 	 }
		});
		
		//add default tab
		addTab(default_tab);
		
	}
	
	@Override
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfaceTabbedDisplayPanel.class.getResource("/mgui/resources/icons/tabbed_display_panel_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/tabbed_display_panel_20.png");
		return null;
	}
	
	protected void fireTabChanged(){
		TabbedDisplayEvent e = new TabbedDisplayEvent(this, TabbedDisplayEvent.EventType.TabChanged);
		InterfaceSession.getWorkspace().tabbedDisplayChanged(e);
	}
	
	public void addTabbedDisplayListener(TabbedDisplayListener l){
		tab_listeners.add(l);
	}
	
	public void removeTabbedDisplayListener(TabbedDisplayListener l){
		tab_listeners.remove(l);
	}
	
	public void addPanel(String tab, InterfaceGraphicWindow graphic_panel, String title){
		
		InterfaceDisplayPanel display_panel = tabbed_panels.get(tab);
		if (display_panel == null)
			display_panel = addTab(tab);
		
		display_panel.addWindow(graphic_panel, title);
		
	}
	
	@Override
	public boolean addWindow(InterfaceGraphicWindow graphic_panel, String title){
		InterfaceDisplayPanel panel = getCurrentPanel();
		if (panel == null) return false;
		return panel.addWindow(graphic_panel, title);
	}
	
	public void displayPanelChanged(DisplayPanelEvent e){
		
		//forward event to this panel's listeners
		DisplayPanelEvent e2 = new DisplayPanelEvent(this, e.type);
		this.fireDisplayPanelChanged(e2);
		
	}
	
	/****************************
	 * Retrieves the last added panel
	 * 
	 * @return
	 */
	@Override
	public InterfaceGraphic getLastAddedPanel(){
		InterfaceDisplayPanel current_panel = getCurrentPanel();
		if (current_panel == null) return null;
		return current_panel.last_added_panel;
	}
	
	/****************************
	 * Retrieves the last removed panel
	 * 
	 * @return
	 */
	@Override
	public InterfaceGraphic getLastRemovedPanel(){
		InterfaceDisplayPanel current_panel = getCurrentPanel();
		if (current_panel == null) return null;
		return current_panel.last_removed_panel;
	}
	
	public InterfaceDisplayPanel addTab(String name){
		if (tabbed_panels.containsKey(name)) return tabbed_panels.get(name);
		
		InterfaceDisplayPanel panel = new InterfaceDisplayPanel(InterfaceSession.getSessionFrame());
		panel.addDisplayPanelListener(this);
		panel.parent_panel = this;
		panel.setName(name);
		
		Iterator<GraphicMouseListener> itr = mouse_listeners.iterator();
		while (itr.hasNext())
			panel.addGraphicMouseListener(itr.next());
		
		if (statusBar != null)
			panel.setStatusBar(this.statusBar);
		
		tabbed_panels.put(name, panel);
		tabbed_pane.add(name, panel);
		panel.addPropertyChangeListener("name", this);
		int index = getTabIndex(name);
		tabbed_pane.setTabComponentAt(index, new DisplayTab(name));
		updateTreeNodes();
		
		return panel;
		
	}
	
	@Override
	public String getTreeLabel(){
		return "Display Panels";
	}
	
	protected int getTabIndex(String name){
		for (int i = 0; i < tabbed_pane.getTabCount(); i++)
			if (tabbed_pane.getTitleAt(i).equals(name))
				return i;
		return -1;
	}
	
	public void removeTab(String name){
		
		InterfaceDisplayPanel panel = tabbed_panels.get(name);
		if (panel == null) return;
		
		panel.removeDisplayPanelListener(this);
		tabbed_pane.remove(panel);
		tabbed_panels.remove(name);
		panel.destroy();
		updateTreeNodes();
		repaint();
		
	}
	
	public InterfaceDisplayPanel getTab(String name){
		return tabbed_panels.get(name);
	}
	
	public void setTitle(InterfaceDisplayPanel panel, String name){
		if (!tabbed_panels.containsValue(panel)) return;
		tabbed_panels.remove(name);
	}
	
	public InterfaceDisplayPanel getCurrentPanel(){
		return (InterfaceDisplayPanel)tabbed_pane.getSelectedComponent();
	}
	
	public boolean containsTab(String name){
		return getTabIndex(name) > -1;
	}
	
	boolean update_name = true;
	
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		
		if (e.getPropertyName().equals("name")){
			if (!update_name) return;
			if (e.getNewValue().equals(e.getOldValue())) return;
			String old_name = (String)e.getOldValue();
			InterfaceDisplayPanel panel = tabbed_panels.get(old_name);
			if (panel == null) return;
			String name = panel.getName();
			
			//don't rename if another tab has same name
			if (containsTab(name)){
				update_name = false;
				panel.setName(old_name);
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Tab already exists!", 
											  "Add New Tab", 
											  JOptionPane.ERROR_MESSAGE);
				update_name = true;
				return;
				}
			
			tabbed_panels.remove(old_name);
			tabbed_panels.put(name, panel);
			int index = getTabIndex(old_name);
			((DisplayTab)tabbed_pane.getTabComponentAt(index)).setTitle(name);
			tabbed_pane.setTitleAt(index, name);
			return;
			}
		
	}
	
	@Override
	public ArrayList<InterfaceGraphicWindow> getWindows(){
		InterfaceDisplayPanel panel = getCurrentPanel();
		if (panel == null) return new ArrayList<InterfaceGraphicWindow>();
		return panel.getWindows();
	}
	
	@Override
	public ArrayList<InterfaceGraphicWindow> getWindowsDepthFirst(){
		InterfaceDisplayPanel panel = getCurrentPanel();
		if (panel == null) return new ArrayList<InterfaceGraphicWindow>();
		return panel.getWindowsDepthFirst();
	}
	
	@Override
	public ArrayList<InterfaceGraphicWindow> getPanels(){
		InterfaceDisplayPanel panel = getCurrentPanel();
		if (panel == null) return null;
		return panel.getWindows();
	}
	
	/****************************************************
	 * Returns all graphics panels from all tabs in this display panel.
	 * 
	 * @return a list of all panels in this set
	 */
	@Override
	public ArrayList<InterfaceGraphicWindow> getAllWindows(){
		
		ArrayList<InterfaceGraphicWindow> graphics_panels = new ArrayList<InterfaceGraphicWindow>();
		
		ArrayList<InterfaceDisplayPanel> panels = new ArrayList<InterfaceDisplayPanel>(tabbed_panels.values());
		for (int i = 0; i < panels.size(); i++)
			graphics_panels.addAll(panels.get(i).getWindows());
		
		return graphics_panels;
	}
	
	@Override
	public void updatePanels(){
		InterfaceDisplayPanel panel = getCurrentPanel();
		if (panel == null) return;
		panel.updatePanels();
	}
	
	@Override
	public void resetPanels(){
		InterfaceDisplayPanel panel = getCurrentPanel();
		if (panel == null) return;
		panel.resetPanels();
	}
	
	@Override
	public void updateDisplays(){
		InterfaceDisplayPanel panel = getCurrentPanel();
		if (panel == null) return;
		panel.updateDisplays();
	}
	
	@Override
	public boolean setCurrentTool(Tool tool){
		if (!super.setCurrentTool(tool)) return false;
		boolean success = true;
		ArrayList<InterfaceDisplayPanel> tabs = new ArrayList<InterfaceDisplayPanel>(tabbed_panels.values());
		for (int i = 0; i < tabs.size(); i++)
			success &= tabs.get(i).setCurrentTool(tool);
		return success;
	}
	
	@Override
	public void setStatusBar(InterfaceStatusBarPanel p){
		super.setStatusBar(p);
		ArrayList<InterfaceDisplayPanel> tabs = new ArrayList<InterfaceDisplayPanel>(tabbed_panels.values());
		for (int i = 0; i < tabs.size(); i++)
			tabs.get(i).setStatusBar(p);
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
	
		for (int i = 0; i < tabbed_pane.getTabCount(); i++)
			treeNode.add(((InterfaceDisplayPanel)tabbed_pane.getComponentAt(i)).issueTreeNode());
		
	}
	
	@Override
	public void addGraphicMouseListener(GraphicMouseListener s){
		this.mouse_listeners.add(s);
		
		ArrayList<String> tabs = new ArrayList<String>(tabbed_panels.keySet());
		for (int i = 0; i < tabs.size(); i++)
			getTab(tabs.get(i)).addGraphicMouseListener(s);
		
	}
	
	@Override
	public void removeGraphicMouseListener(GraphicMouseListener s){
		this.mouse_listeners.remove(s);
		
		ArrayList<String> tabs = new ArrayList<String>(tabbed_panels.keySet());
		for (int i = 0; i < tabs.size(); i++)
			getTab(tabs.get(i)).removeGraphicMouseListener(s);
	}
	
	/********* Popup Menu *********************/
	
	@Override
	public void showPopupMenu(MouseEvent e){
		//do not show menu if it is on a tab
		int index = tabbed_pane.indexAtLocation(e.getX(), e.getY());
		if(index != -1){
			((InterfaceDisplayPanel)tabbed_pane.getComponentAt(index)).showPopupMenu(e);
			return;
			}
				  
		super.showPopupMenu(e);
	}
	
	@Override
	public InterfacePopupMenu getPopupMenu(){
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		menu.addMenuItem(new JMenuItem("Add Tab"));
		
		if (getCurrentPanel() == null) return menu;
		
		JMenu submenu = new JMenu("Add Window");
		ArrayList<String> graphic_types = InterfaceEnvironment.getInterfaceGraphicNames();
		for (int i = 0; i < graphic_types.size(); i++){
			JMenuItem item = new JMenuItem(graphic_types.get(i));
			item.setActionCommand("Change type." + graphic_types.get(i));
			submenu.add(item);
			}
		
		menu.addSubmenu(submenu);
		
		return menu;
	}
	
	@Override
	public void handlePopupEvent(ActionEvent e) {
		
		if (!(e.getSource() instanceof JMenuItem)) return;
		JMenuItem item = (JMenuItem)e.getSource();
		String command = item.getActionCommand();
		
		if (command.startsWith("Add Window")){
			String graphic_type = command.substring(command.indexOf(".") + 1);
			InterfaceGraphic<?> panel = InterfaceEnvironment.getInterfaceGraphicInstance(graphic_type);
			if (panel == null){
				InterfaceSession.log("InterfaceGraphicWindow: Invalid graphic panel type: " + graphic_type + "..",
									 LoggingType.Errors);
				return;
				}
			
			String graphic_name = getValidTitle(graphic_type);
			
			graphic_name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), 
													  "Name for new window:", 
													  graphic_name);
			if (graphic_name == null) return;
			panel.setName(graphic_name);
			
			addWindow(new InterfaceGraphicWindow(panel));
			return;
			}
		
		if (item.getText().equals("Add Tab")){
			String name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), 
													  "Name of new tab:",								  
													  "Add New Tab", 
													  JOptionPane.QUESTION_MESSAGE);
			if (name == null) return;
			if (containsTab(name)){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Tab already exists!", 
											  "Add New Tab", 
											  JOptionPane.ERROR_MESSAGE);
				return;
				}
			addTab(name);
			return;
			}
		
	}
	
	class DisplayTab extends JPanel implements ActionListener {
		
		public String title;
		
		JLabel title_label;
		JButton close_button = new JButton();
		JButton new_button = new JButton();
		
		boolean is_mouse_hover = false;
		
		public DisplayTab(String title){
			this.title = title;
			initTab();
		}
		
		public void setTitle(String title){
			this.title = title;
			title_label.setText(title);
		}
		
		void initTab(){
			title_label = new JLabel(title);
			this.setLayout(new LineLayout(20, 5, 50));
			this.setOpaque(false);
			title_label.setOpaque(false);
			
			this.setPreferredSize(new Dimension(100, 20));
			close_button.setIcon(getGreyCross());
			close_button.setRolloverIcon(getRedCross());
			close_button.setPressedIcon(getRedCross());
			close_button.addActionListener(this);
			close_button.setActionCommand("Close");
			
			LineLayoutConstraints c = new LineLayoutConstraints(0, 0, 0.05, .7, 1);
			add(title_label, c);
			//c = new LineLayoutConstraints(0, 0, 0.55, 0.2, 1);
			//add(new_button, c);
			c = new LineLayoutConstraints(0, 0, 0.75, 0.2, 1);
			add(close_button, c);
			
		}
		
		public void actionPerformed(ActionEvent e){
			
			if (e.getActionCommand().equals("Close")){
				removeTab(title);
				return;
				}
			
		}
		
		public ImageIcon getGreyCross(){
			java.net.URL imgURL = DisplayTab.class.getResource("/mgui/resources/icons/cross_grey_12.png");
			if (imgURL != null)
				return new ImageIcon(imgURL);
			
			InterfaceSession.log("InterfaceTabbedDisplayPanel: Could not load image '/mgui/resources/icons/cross_grey_18.png'.");
			return null;
		}
		
		public ImageIcon getRedCross(){
			java.net.URL imgURL = DisplayTab.class.getResource("/mgui/resources/icons/cross_red_12.png");
			if (imgURL != null)
				return new ImageIcon(imgURL);
			
			InterfaceSession.log("InterfaceTabbedDisplayPanel: Could not load image '/mgui/resources/icons/cross_red_18.png'.");
			return null;
		}
		
	}
	
	
	//***************************** XML Stuff ******************************
	
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progressBar) throws IOException {
		// Writes (Explicit or reference, depending on objects):
		// 1. Tabs + Display Panels
		
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		String _tab3 = XMLFunctions.getTab(tab + 2);
		
		writer.write(_tab + "<InterfaceTabbedDisplayPanel\n" +
							_tab2 + "name = '" + getName() + "'\n" +
							_tab2 + "current_shape_model = '" + this.currentModel.getName() + "'\n" +
							_tab + ">\n");
		
		//1. Tabs + display panels
		writer.write(_tab2 + "<Tabs>\n");
		
		ArrayList<String> tabs = new ArrayList<String>(tabbed_panels.keySet());
		for (int i = 0; i < tabs.size(); i++){
			writer.write(_tab3 + "<Tab name = '" + tabs.get(i) + "'>\n");
			tabbed_panels.get(tabs.get(i)).writeXML(tab + 3, writer, options, progressBar);
			writer.write(_tab3 + "</Tab>\n");
			}
		
		writer.write(_tab2 + "</Tabs>\n");
		
		writer.write(_tab + "</InterfaceTabbedDisplayPanel>\n");
		
	}
	
	
}