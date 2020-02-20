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

package mgui.interfaces.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.InterfaceSplitPanel;
import mgui.interfaces.InterfaceTabbedDisplayPanel;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.graphics.WindowEvent.EventType;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.resources.icons.IconObject;

import org.xml.sax.Attributes;

/***********************************************************
 * Provides a title for an <code>InterfaceGraphic</code> window, which is a button.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 */
public class InterfaceGraphicWindow extends InterfacePanel implements XMLObject,
																	  IconObject{

	protected GraphicButton title_button = new GraphicButton();
	protected InterfaceGraphic<?> panel; // = new InterfaceGraphic();
	protected WindowContainer parent_panel;
	
	protected InterfaceGraphic<?> old_panel;
	protected ArrayList<WindowListener> window_listeners = new ArrayList<WindowListener>();
	
	public InterfaceGraphicWindow(){
		super();
	}
	
	public InterfaceGraphicWindow(InterfaceGraphic<?> p){
		super();
		setPanel(p);
	}
	
	@Override
	protected void init(){
		_init();
		if (panel == null) return;
		title_button.setHorizontalAlignment(SwingConstants.CENTER);
		title_button.setBackground(new Color(240,240,240));
		title_button.setActionCommand(panel.getName());
		title_button.setPreferredSize(new Dimension(1, 20));
		title_button.setIcon(panel.getObjectIcon());
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		this.removeAll();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 0;
		c.weightx = 1;
		add(title_button, c);
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 5;
		c.weightx = 1;
		add(panel, c);
	}
	
	public void addWindowListener(WindowListener listener){
		for (int i = 0; i < window_listeners.size(); i++)
			if (window_listeners.get(i).equals(listener)) return;
		this.window_listeners.add(listener);
	}
	
	public void removeWindowListener(WindowListener listener){
		this.window_listeners.remove(listener);
	}

	@Override
	public void destroy(){
		if (isDestroyed) return;
		super.destroy();
		if (this.panel != null)
			panel.destroy();
	}
	
	public InterfaceGraphic<?> getOldPanel(){
		return old_panel;
	}
	
	public void setParentPanel(WindowContainer panel){
		if (panel == null){
			this.removeWindowListener(panel);
			parent_panel = null;
			return;
			}
		
		if (parent_panel == panel) return;
		
		this.removeWindowListener(parent_panel);
		this.parent_panel = panel;
		this.addWindowListener(parent_panel);
		
	}
	
	public WindowContainer getParentPanel(){
		return this.parent_panel;
	}
	
	private int popup_length;
	
	@Override
	public InterfacePopupMenu getPopupMenu(){
				
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		popup_length = 8;
		
		menu.addMenuItem(new JMenuItem("Graphic Window", getIcon()));
		menu.add(new JSeparator(), 1);
		menu.add(new JSeparator(), 1);
		
		ArrayList<String> graphic_types = InterfaceEnvironment.getInterfaceGraphicNames();
		
		JMenu submenu = new JMenu("Change type");
		for (int i = 0; i < graphic_types.size(); i++){
			JMenuItem item = new JMenuItem(graphic_types.get(i));
			item.setActionCommand("Change type." + graphic_types.get(i));
			submenu.add(item);
			}
		menu.addSubmenu(submenu);
		
		submenu = new JMenu("Split horizontal");
		for (int i = 0; i < graphic_types.size(); i++){
			JMenuItem item = new JMenuItem(graphic_types.get(i));
			item.setActionCommand("Split horizontal." + graphic_types.get(i));
			submenu.add(item);
			}
		menu.addSubmenu(submenu);
		
		submenu = new JMenu("Split vertical");
		for (int i = 0; i < graphic_types.size(); i++){
			JMenuItem item = new JMenuItem(graphic_types.get(i));
			item.setActionCommand("Split vertical." + graphic_types.get(i));
			submenu.add(item);
			}
		menu.addSubmenu(submenu);
		
		if (parent_panel != null && parent_panel instanceof InterfaceSplitPanel){
			menu.addMenuItem(new JMenuItem("Flip current split"));
			menu.addMenuItem(new JMenuItem("Swap current split"));
			popup_length += 2;
			}
		
		if (!title_button.is_expanded)
			menu.addMenuItem(new JMenuItem("Maximize"));
		else
			menu.addMenuItem(new JMenuItem("Unmaximize"));
		//menu.addMenuItem(new JMenuItem("Close"));
		menu.addMenuItem(new JMenuItem("Snapshot.."));
		
		return menu;
		
	}
	
	@Override
	public void handlePopupEvent(ActionEvent e){
		
		if (!(e.getSource() instanceof JMenuItem)) return;
		JMenuItem item = (JMenuItem)e.getSource();
		String command = item.getActionCommand();
	
		if (command.equals("Maximize") || command.equals("Unmaximize")){
			InterfaceDisplayPanel d_panel = getDisplayPanel();
			if (d_panel == null) return;
			int exp = d_panel.toggleExpandedWindow(this.getName());
			if (exp == 0)
				title_button.setExpanded(false);
			if (exp == 1)
				title_button.setExpanded(true);
			return;
			}
		
		if (command.startsWith("Change type")){
			String graphic_type = command.substring(command.indexOf(".") + 1);
			InterfaceGraphic<?> panel = InterfaceEnvironment.getInterfaceGraphicInstance(graphic_type);
			if (panel == null){
				InterfaceSession.log("InterfaceGraphicWindow: Invalid graphic panel type: " + graphic_type + "..",
									 LoggingType.Errors);
				return;
				}
			InterfaceDisplayPanel display_panel = this.getDisplayPanel();
			String graphic_name = display_panel.getValidTitle(graphic_type);
			
			panel.setName(graphic_name);
			
			this.setPanel(panel);
			return;
			}
		
		//TODO: implement stuff
		if (command.startsWith("Split")){
			
			int orientation = JSplitPane.HORIZONTAL_SPLIT;
			if (command.contains(" vertical."))
				orientation = JSplitPane.VERTICAL_SPLIT;
			
			String graphic_type = command.substring(command.indexOf(".") + 1);
			InterfaceGraphic<?> panel = InterfaceEnvironment.getInterfaceGraphicInstance(graphic_type);
			if (panel == null){
				InterfaceSession.log("InterfaceGraphicWindow: Invalid graphic panel type: " + graphic_type + "..",
									 LoggingType.Errors);
				return;
				}
			
			// Has no parent, so split and add to display panel
			InterfaceDisplayPanel display_panel = InterfaceSession.getDisplayPanel();
			if (display_panel instanceof InterfaceTabbedDisplayPanel)
				display_panel = ((InterfaceTabbedDisplayPanel)display_panel).getCurrentPanel();
			
			//ArrayList<InterfaceGraphicWindow> g_windows = display_panel.getWindows();
			String graphic_name = display_panel.getValidTitle(graphic_type);
			
			String name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), 
													  "Name for new window:", 
													  graphic_name);
			if (name == null) return;
			panel.setName(name);
			
			InterfaceGraphicWindow new_window = new InterfaceGraphicWindow(panel);
			
			display_panel.splitWindow(this, new_window, orientation, true);
			return;
			}
		
		if (command.equals("Flip current split")){
			if (parent_panel == null || !(parent_panel instanceof InterfaceSplitPanel)) return;
			((InterfaceSplitPanel)parent_panel).flip();
			return;
			}
		
		if (command.equals("Swap current split")){
			if (parent_panel == null || !(parent_panel instanceof InterfaceSplitPanel)) return;
			((InterfaceSplitPanel)parent_panel).swap();
			return;
			}
		
		panel.handlePopupEvent(e);
	}
	
	protected InterfaceDisplayPanel getDisplayPanel(){
		if (parent_panel == null) return null;
		
		if (parent_panel instanceof InterfaceSplitPanel)
			return ((InterfaceSplitPanel)parent_panel).getDisplayPanel();
		
		return (InterfaceDisplayPanel)parent_panel;
		
	}
	
	protected int getPopupLength(){
		return popup_length;
	}
	
	
	@Override
	public Icon getObjectIcon(){
		if (panel == null) return null;
		return panel.getObjectIcon();
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
	
	public void setPanel(InterfaceGraphic<?> p){
		if (panel != null){
			remove(panel);
			panel.destroy();
			panel.title_panel = null;
			old_panel = panel;
			}
		panel = p;
		panel.title_panel = this;
		setName(panel.getName());
		init();
		firePanelChanged();
	}
	
	protected void firePanelChanged(){
		WindowEvent event = new WindowEvent(this);
		ArrayList<WindowListener> copy = new ArrayList<WindowListener>(window_listeners);
		
		for (int i = 0; i < copy.size(); i++)
			copy.get(i).windowSourceChanged(event);
		
	}
	
	protected void fireWindowListeners(WindowEvent event){
		ArrayList<WindowListener> copy = new ArrayList<WindowListener>(window_listeners);
		
		for (int i = 0; i < copy.size(); i++)
			copy.get(i).windowUpdated(event);
	}
	
	public void updateTitle(){
		title_button.setText(panel.getTitle());
	}
	
	@Override
	public void setName(String thisName){
		super.setName(thisName);
		panel.setName(thisName);
		title_button.setText(panel.getTitle());
		title_button.setActionCommand(panel.getName());
	}
	
	public InterfaceGraphic<?> getPanel(){
		return panel;
	}
	
	public GraphicButton getTitleButton(){
		return title_button;
	}
	
	public String getTitle(){
		return title_button.getText();
	}
	
	public void setPanelName(String title){
		setName(title);
	}
	
	@Override
	public void updateDisplay(){
		panel.updateDisplay();
		
	}
	
	@Override
	public String toString(){
		return panel.toString();
	}
	
	
	
	//************************ XML Stuff *****************************************
	
	@Override
	public String getDTD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
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
		this.writeXML(tab, writer, new XMLOutputOptions(), progressBar);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progressBar) throws IOException {
		
		String _tab = XMLFunctions.getTab(tab);
		writer.write(_tab + "<InterfaceGraphicWindow>\n"); // name='" + getName() + "'>\n");
		if (this.panel != null)
			panel.writeXML(tab + 1, writer, options, progressBar);
		writer.write(_tab + "</InterfaceGraphicWindow>\n");
		
	}

	@Override
	public void writeXML(int tab, Writer writer) throws IOException {
		writeXML(tab, writer, null);
	}

	
	
	
	/*********************************************
	 * Button to display window title; overrides UI delegate.
	 * 
	 * @author Andrew Reid
	 *
	 */
	public class GraphicButton extends JButton implements MouseListener{
		
		public Color hover_background = new Color(255, 204, 204);
		public Color regular_background = new Color(0.9f, 0.9f, 0.9f);
		boolean is_expanded = false;
		
		public GraphicButton(){
			super();
			this.addMouseListener(this);
			setBackground(regular_background);
			setToolTipText("Click to maximize this window");
		}
		
		public void setExpanded(boolean b){
			is_expanded = b;
			if (b)
				setToolTipText("Click to unmaximize this window");
			else
				setToolTipText("Click to maximize this window");
		}
		
		@Override
		protected void paintBorder(Graphics g) {
			Graphics2D g2d = (Graphics2D)g;
			
			Rectangle2D.Double drawRect = new Rectangle2D.Double(0, 0, getWidth() - 1, getHeight() + 1);
			g2d.setPaint(Color.black);
			g2d.setStroke(new BasicStroke(1f));
			g2d.draw(drawRect);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D)g;
			
			Rectangle2D.Double drawRect = new Rectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 1);
			//g2d.setPaint(regular_background);
			g2d.setPaint(getBackground());
			g2d.fill(drawRect);
			
			//draw arrow and label
			Font font = new Font("Arial Narrow", Font.PLAIN, 15);
			g2d.setColor(Color.black);
			
			TextLayout layout = new TextLayout(this.getText(), font, g2d.getFontRenderContext());
			Rectangle bounds = layout.getPixelBounds(g2d.getFontRenderContext(), 0, 0);
			
			//center text
			int start_x = (int)((this.getWidth() / 2f) - bounds.getCenterX());
			int start_y = (int)((this.getHeight() / 2f) - bounds.getCenterY());
			
			layout.draw(g2d, start_x, start_y);
			
			//draw icon
			ImageIcon icon = (ImageIcon)this.getIcon();
			if (icon == null) return;
			
			int height = Math.min(getHeight() - 4, icon.getIconHeight());
			
			Image icon_img = icon.getImage();
			g2d.drawImage(icon_img, 2, 2, icon.getIconWidth(), height, null);
			
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
			this.setBackground(hover_background);
			repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			this.setBackground(regular_background);
			repaint();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}


		
		public void clearListeners(){
			Object[] list = this.listenerList.getListenerList();
			for (int i = 0; i < list.length; i++)
				if (ActionListener.class.isInstance(list[i]))
					this.removeActionListener((ActionListener)list[i]);
		}
	}
	
	
}