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

package mgui.interfaces.frames;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JSplitPane;

import mgui.datasources.DataSource;
import mgui.datasources.DataSourceException;
import mgui.interfaces.InterfaceComboPanel;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceFrame;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.InterfaceStatusBarPanel;
import mgui.interfaces.InterfaceTabbedDisplayPanel;
import mgui.interfaces.InterfaceWorkspace;
import mgui.interfaces.graphics.InterfaceGraphicTextBox;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.queries.InterfaceShapeQuery;
import mgui.interfaces.shapes.queries.ShapeSummaryQuery;
import mgui.interfaces.tools.graphics.Tool2D;
import mgui.interfaces.trees.InterfaceTreePanel;

/******************************************
 * Main frame class for an instance of ModelGUI.
 * 
 * Sets up a display panel and adds some InterfaceGraphic objects and InterfacePanel
 * panels.
 * 
 * TODO: implement a more generic frame
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 */
public class SessionFrame extends InterfaceFrame implements WindowListener {

	public Tool2D currentTool;
	public JSplitPane splitPane;
	
	
	public SessionFrame(){
		super();
	}
	
	public boolean init(){
		return init(null);
	}
	
	public boolean init(String init_file){
		
		InterfaceWorkspace workspace = InterfaceSession.getWorkspace();
		InterfaceTabbedDisplayPanel displayPanel = new InterfaceTabbedDisplayPanel(this);
		workspace.setDisplayPanel(displayPanel);
		
		//displayPanel.addTab("Tab2");
		
		Image img = getImage();
		this.setIconImage(img);
		
		addWindowListener(this);
		
		// Display Panel
		
		// TODO: Add these from init file
		workspace.addQuery(new InterfaceShapeQuery("Shapes.Default"));
		workspace.addQuery(new ShapeSummaryQuery("Shapes.Summary"));
		
		// Datasources
		ArrayList<DataSource> sources = InterfaceEnvironment.getDataSources();
		for (int i = 0; i < sources.size(); i++){
			DataSource source = sources.get(i);
			//try to connect
			try{
			if (!source.connect())
				InterfaceSession.log("Cannot connect data source '" + source.getName() + "'", 
 					   				 LoggingType.Errors);

			}catch (DataSourceException ex){
				ex.printStackTrace();
				InterfaceSession.log("Cannot connect data source '" + source.getName() + "'", 
		   				 			 LoggingType.Errors);
				}
			workspace.addDataSource(source);
			}
		
		//initial shape model & find and appropriate window for it
		ShapeModel3D model = new ShapeModel3D("Default Model", new ShapeSet3DInt("Default Shape Set"));
		workspace.addShapeModel(model, true);
		
		//combo panel
		InterfaceComboPanel cPanel = new InterfaceComboPanel();
		cPanel.setPreferredSize(new Dimension(300,500));
		
		//tree panel
		InterfaceTreePanel treePanel = new InterfaceTreePanel();
		treePanel.setName("Tree Panel");
		cPanel.addPanel(treePanel);
		
		//get list of interface panels sorted
		InterfaceEnvironment.setComboPanel(cPanel, displayPanel, true);
			
		//show tree panel first
		cPanel.showPanel(treePanel);

		InterfaceGraphicTextBox text1 = new InterfaceGraphicTextBox("Current Tool:");
		InterfaceGraphicTextBox text2 = new InterfaceGraphicTextBox("Current Window:");
		InterfaceGraphicTextBox text3 = new InterfaceGraphicTextBox("Zoom:");
		InterfaceGraphicTextBox text4 = new InterfaceGraphicTextBox("Mouse Coords:");
		
		InterfaceStatusBarPanel status = new InterfaceStatusBarPanel();
		status.addTextBox(text1);
		status.addTextBox(text2);
		status.addTextBox(text3);
		status.addTextBox(text4);
		displayPanel.setStatusBar(status);
		workspace.setComboPanel(cPanel);
		
		//set up split pane
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cPanel, displayPanel);
		cPanel.setPreferredSize(new Dimension(215,300));
		cPanel.setMinimumSize(new Dimension(200,200));
		displayPanel.setMinimumSize(new Dimension(200,200));
		
		addPanel(splitPane, BorderLayout.CENTER);
		addPanel(status, BorderLayout.SOUTH);
		
		workspace.setObjectTree(treePanel);
		
		//frame stuff
		this.setTitle("ModelGUI v" + InterfaceEnvironment.getVersion());
		return true;
	}
	
	public Image getImage(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/mgui_logo_30.png");
		try{
			if (imgURL != null){
				//return ImageIO.read(imgURL);
				ImageIcon icon = new ImageIcon(imgURL);
				return icon.getImage();
				}
				
			else
				InterfaceSession.log("Cannot find resource: /mgui/resources/icons/mgui_logo_30.png", 
		   				 			 LoggingType.Errors);
		} catch (Exception e){
			e.printStackTrace();
			}
		return null;
	}
	
	public void addPanel(JComponent thisPanel, String constraints){
		this.getContentPane().add(thisPanel, constraints);
	}
	
	public void setCurrentTool(Tool2D thisTool){
		currentTool = thisTool;
	}
	
	@Override
	public void paint(Graphics g){
		super.paint(g);
	} 
	
	
	//window event handling
	
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void windowClosed(WindowEvent e) {
		
		InterfaceWorkspace workspace = InterfaceSession.getWorkspace();
		workspace.endSession();
		//displayPanel.close();
	}

	public void windowClosing(WindowEvent e) {
		InterfaceSession.terminate();
	}

	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	
}