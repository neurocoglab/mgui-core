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

package mgui.interfaces.plots;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.tree.DefaultMutableTreeNode;

import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.graphics.util.MouseRelayListener;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.tools.Tool;
import mgui.interfaces.tools.plots.ToolPlot;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;

import org.xml.sax.Attributes;


/***********************************
 * General graphic interface for all plots.
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */
public class InterfacePlotDisplay extends InterfaceGraphic<ToolPlot> {

	protected InterfacePlot<?> current_plot;
	protected MouseRelayListener mouse_relay_listener;
	
	public InterfacePlotDisplay(){
		init();
		setLayout(new BorderLayout());
	}
	
	public InterfacePlotDisplay(InterfacePlot<?> plot){
		init();
		setLayout(new BorderLayout());
		setSource(plot);
	}
	
	@Override
	protected void init(){
		super.init();
		_init();
		
		mouse_relay_listener = new MouseRelayListener(this);
		
		type = "Plot Display";
		
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		this.setBackground(Color.WHITE);
		
		
		
	}
	
	@Override
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfacePlotDisplay.class.getResource("/mgui/resources/icons/plot_panel_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/plot_panel_20.png");
		return null;
	}
	
	@Override
	public InterfacePopupMenu getPopupMenu(){
		InterfacePopupMenu menu = super.getPopupMenu();
		int start = super.getPopupLength();	//can we get this from the menu itself?
		
		menu.add(new JSeparator(), start);
		menu.add(new JSeparator(), start);
		menu.addMenuItem(new JMenuItem("Plot2D Window", getObjectIcon()));
		menu.add(new JSeparator(), start + 3);
		menu.add(new JSeparator(), start + 3);
		menu.addMenuItem(new JMenuItem("Set plot..."));
		menu.addMenuItem(new JMenuItem("Zoom window"));
		menu.addMenuItem(new JMenuItem("Zoom extents"));
		menu.addMenuItem(new JMenuItem("Zoom out"));
		menu.addMenuItem(new JMenuItem("Select mode"));
		menu.add(new JSeparator(), start + 9);
		menu.addMenuItem(new JMenuItem("Regen window"));
		menu.addMenuItem(new JMenuItem("Set background"));
		
		return menu;
	}
	
	@Override
	protected int getPopupLength(){
		return super.getPopupLength() + 11; 
	}
	
	@Override
	public void handlePopupEvent(ActionEvent e){
		
		if (e.getActionCommand().equals("Set plot...")){
			InterfacePlot<?> plot = NewPlotDialog.showDialog(InterfaceSession.getSessionFrame());
			
			InterfacePlotDialog<?> dialog = plot.getPlotDialog();
			
			if (dialog == null){
				String title = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), 
															"Title for plot", 
															"No Title");
				if (title == null) title = "No Title";
				plot.setTitle(title);
			}else{
				plot = dialog.showDialog(InterfaceSession.getSessionFrame(), 
										 plot.getOptionsInstance());
				}
			
			if (plot == null) return;
			this.setSource(plot);
			return;
			}
		
	}
	
	
	@Override
	public boolean isDisplayable(Object obj){
		if (!(obj instanceof InterfacePlot)) return false;
		return true;
	}
	
	public InterfacePlot<?> getCurrentPlot(){
		return this.current_plot;
	}
	
	@Override
	public boolean setSource(Object obj){
		if (!isDisplayable(obj)) return false;
	
		InterfacePlot<?> plot = (InterfacePlot<?>)obj;
		setPlot(plot);
		
		return true;
	}
	
	protected void setPlot(InterfacePlot<?> plot){
		if (current_plot != null){
			remove(current_plot);
			current_plot.removeMouseRelayListener(mouse_relay_listener);
			current_plot.destroy();
			}
		
		current_plot = plot;
		add(plot, BorderLayout.CENTER);
		current_plot.addMouseRelayListener(mouse_relay_listener);
		
		this.updateTreeNodes();
		updateUI();
	}
	
	public void setTreeNode(InterfaceTreeNode treeNode){
	
		super.setTreeNode(treeNode);
		treeNode.add(attributes.issueTreeNode());
		
		if (current_plot != null){
			treeNode.addChild(current_plot.issueTreeNode());
			}
	}
	
	/*
	public void setTreeNode(){
		treeNode = new InterfaceTreeNode(this);
		treeNode.add(attributes.issueTreeNode());
		
		if (current_plot != null){
			treeNode.addChild(current_plot.issueTreeNode());
			}
	}
	*/
	
	//@Override
	//public InterfaceTreeNode issueTreeNode(){
	//	if (treeNode == null) setTreeNode();
	//	return treeNode;
	//}
	
	@Override
	public String toString(){
		return "Plotting Panel: " + getName();
	}
	
	public DefaultMutableTreeNode getDisplayObjectsNode(InterfaceDisplayPanel p){
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Graphs");
		ArrayList<InterfacePlot<?>> plots = InterfaceSession.getWorkspace().getPlots();
		for (int i = 0; i < plots.size(); i++)
			node.add(new DefaultMutableTreeNode(plots.get(i)));
		return node;
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
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		
		String _tab = XMLFunctions.getTab(tab);
		
		writer.write(_tab + "<InterfaceConsoleDisplay>\n");
		
		//TODO: write stuff
		
		writer.write(_tab + "</InterfaceConsoleDisplay>\n");
		
	}
	
	@Override
	public ToolPlot getCurrentTool() {
		return null;
	}

	@Override
	public boolean setCurrentTool(ToolPlot tool) {
		return false;
	}

	@Override
	public boolean setDefaultTool(ToolPlot tool) {
		return false;
	}
	
	public boolean isToolable(Tool tool){
		return tool instanceof ToolPlot;
	}
	
	
}