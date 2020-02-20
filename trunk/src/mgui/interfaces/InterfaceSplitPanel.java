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

package mgui.interfaces;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Stack;

import javax.swing.JSplitPane;

import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.graphics.InterfaceGraphicWindow;
import mgui.interfaces.graphics.WindowContainer;
import mgui.interfaces.graphics.WindowEvent;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;

/******************************************************************
 * Displays two {@link InterfaceGraphicWindow}s, split either horizontally or vertically. The split ratio is
 * adjustable.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceSplitPanel extends InterfaceGraphicWindow implements ComponentListener,
																		   WindowContainer{

	
	protected int split_orientation = JSplitPane.HORIZONTAL_SPLIT;
	protected InterfaceGraphicWindow panel1, panel2;
	JSplitPane split_pane;
	protected ArrayList<SplitPanelListener> listeners = new ArrayList<SplitPanelListener>();
	
	public InterfaceSplitPanel(int direction, InterfaceGraphicWindow panel1, InterfaceGraphicWindow panel2){
		
		split_pane = new JSplitPane(direction, panel1, panel2);
		//split_pane.addComponentListener(this);
		setLayout(new GridLayout(1, 1));
		add(split_pane);
		
		this.split_orientation = direction;
		this.panel1 = panel1;
		this.panel2 = panel2;
		this.panel1.setParentPanel(this);
		this.panel2.setParentPanel(this);
		
		panel1.setMinimumSize(new Dimension(200,200));
		panel2.setMinimumSize(new Dimension(200,200));
		
		split_pane.setResizeWeight(0.5);
		split_pane.setDividerLocation(0.5);
		
	}
	
	public void setSplitRatio(double loc){
		split_pane.setDividerLocation(loc);
	}
	
	public double getSplitRatio(){
		double s = 0;
		if (split_orientation == JSplitPane.VERTICAL_SPLIT)
			s =	split_pane.getHeight() - split_pane.getDividerSize();
		else
			s =	split_pane.getWidth() - split_pane.getDividerSize();
		
		return (double)split_pane.getDividerLocation() / s;
	}
	
	@Override
	public void destroy(){
		super.destroy();
		if (panel1 != null)
			panel1.destroy();
		if (panel2 != null)
			panel2.destroy();
	}
	
	public void addSplitPanelListener(SplitPanelListener listener){
		this.listeners.add(listener);
	}
	
	public void removeSplitPanelListener(SplitPanelListener listener){
		this.listeners.remove(listener);
	}
	
	protected void fireSplitPanelChanged(SplitPanelEvent event){
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).splitPanelChanged(event);
	}
	
	public int getSplitOrientation(){
		return split_orientation;
	}
	
	@Override
	public void windowUpdated(WindowEvent e) {
		
		InterfaceGraphicWindow window = (InterfaceGraphicWindow)e.getSource();
		
		
		switch (e.getType()){
			
			case Destroyed:
				
				// Relay to higher listeners (i.e., cascade to display panel)
				this.fireWindowListeners(e);
				
				return;
		
			}
		
	}

	@Override
	public void windowSourceChanged(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	
	/****************************************************
	 * Returns a breadth-first list of windows contained by this split panel; meaning that the first windows
	 * are on the top-most level and later windows are increasingly deep in the nesting tree.
	 * 
	 * @param max_depth The maximum depth in the nesting tree to search
	 * @return
	 */
	public ArrayList<InterfaceGraphicWindow> getWindowsBreadthFirst(){
		
		ArrayList<InterfaceGraphicWindow> windows = new ArrayList<InterfaceGraphicWindow>();
		
		Stack<InterfaceGraphicWindow> stack = getSplitPanelStack();
		while (!stack.empty())
			windows.add(stack.pop());
		
		return windows;
		
	}
	
	/****************************************************
	 * Returns a depth-first list of all windows contained by this split panel or its nested panels (left or top first).
	 * 
	 * @return
	 */
	public ArrayList<InterfaceGraphicWindow> getWindowsDepthFirst(){
		
		ArrayList<InterfaceGraphicWindow> windows = new ArrayList<InterfaceGraphicWindow>();
		
		if (panel1 instanceof InterfaceSplitPanel){
			windows.addAll(((InterfaceSplitPanel)panel1).getWindowsDepthFirst());
		}else if (panel1 != null){
			windows.add(panel1);
			}
		
		if (panel2 instanceof InterfaceSplitPanel){
			windows.addAll(((InterfaceSplitPanel)panel2).getWindowsDepthFirst());
		}else if (panel2 != null){
			windows.add(panel2);
			}
		
		return windows;
		
	}
	
	/********************************************
	 * Returns the parent panel of {@code window}.InterfaceSplitPanel
	 * 
	 * @param window
	 * @return The parent, or {@code null} if window is not found.
	 */
	public InterfaceSplitPanel getParent(InterfaceGraphicWindow window){
		
		if (panel1.equals(window) || panel2.equals(window)) return this;
		
		if (panel1 instanceof InterfaceSplitPanel){
			InterfaceSplitPanel parent = ((InterfaceSplitPanel)panel1).getParent(window);
			if (parent != null) return parent;
			}
		
		if (panel2 instanceof InterfaceSplitPanel){
			InterfaceSplitPanel parent = ((InterfaceSplitPanel)panel2).getParent(window);
			if (parent != null) return parent;
			}
		
		return null;
	}
	
	/*********************************************
	 * Replace {@code old} with {@code nieuw}.
	 * 
	 * 
	 * @param old
	 * @param nieuw
	 * @return {@code true} if window was replaced; {@code false} if there is no such window
	 */
	public boolean replace(InterfaceGraphicWindow old, InterfaceGraphicWindow nieuw){
		
		if (old.equals(panel1)){
			panel1.setParentPanel(null);
			panel1 = nieuw;
			panel1.setParentPanel(this);
			switch (this.split_orientation){
				case JSplitPane.HORIZONTAL_SPLIT: 
					this.split_pane.setLeftComponent(nieuw);
					break;
				case JSplitPane.VERTICAL_SPLIT:
					this.split_pane.setTopComponent(nieuw);
				}
			nieuw.setMinimumSize(new Dimension(200,200));
			fireSplitPanelChanged(new SplitPanelEvent(this, SplitPanelEvent.EventType.WindowReplaced));
			return true;
			}
		
		if (old.equals(panel2)){
			panel2.setParentPanel(null);
			panel2 = nieuw;
			panel2.setParentPanel(this);
			switch (this.split_orientation){
				case JSplitPane.HORIZONTAL_SPLIT:
					this.split_pane.setRightComponent(nieuw);
					break;
				case JSplitPane.VERTICAL_SPLIT:
					this.split_pane.setBottomComponent(nieuw);
				}
			nieuw.setMinimumSize(new Dimension(200,200));
			fireSplitPanelChanged(new SplitPanelEvent(this, SplitPanelEvent.EventType.WindowReplaced));
			return true;
			}
		
		return false;
	}
	
	/*********************************************
	 * Returns the side of the split pane {@code window} is on.
	 * 
	 * @return 0 for left/top; 1 for right/bottom; -1 if not in this panel
	 */
	public int getSide(InterfaceGraphicWindow window){
		
		if (window.equals(panel1)) return 0;
		if (window.equals(panel2)) return 1;
		return -1;
		
	}
	
	/**********************************************
	 * Returns the window on the specified side of the panel; where 0 = left, 1 = right.
	 * 
	 * @param side
	 * @return
	 */
	public InterfaceGraphicWindow getWindow(int side){
		switch (side){
			case 0:
				return (InterfaceGraphicWindow)this.split_pane.getLeftComponent();
			case 1:
				return (InterfaceGraphicWindow)this.split_pane.getRightComponent();
			}
		return null;
	}
	
	/*********************************************
	 * Searches this split pane for a window with the title {@code name}.
	 * 
	 * @param name
	 * @return The window, or {@code null} if not found.
	 */
	public InterfaceGraphicWindow findWindow(String name){
		
		ArrayList<InterfaceGraphicWindow> windows = getWindowsBreadthFirst();
		for (int i = 0; i < windows.size(); i++)
			if (windows.get(i).getTitle().equals(name))
				return windows.get(i);
		
		return null;
	}
	
	/**********************************************
	 * Flips the orientation of this split panel.
	 * 
	 */
	public void flip(){
		
		double ratio = this.getSplitRatio();
		switch (split_orientation){
		
			case JSplitPane.HORIZONTAL_SPLIT:
				split_orientation = JSplitPane.VERTICAL_SPLIT;
				remove(split_pane);
				split_pane = new JSplitPane(split_orientation, panel1, panel2);
				
				add(split_pane);
				split_pane.setDividerLocation(ratio);
				this.updateUI();
				break;
				
			case JSplitPane.VERTICAL_SPLIT:
				split_orientation = JSplitPane.HORIZONTAL_SPLIT;
				remove(split_pane);
				split_pane = new JSplitPane(split_orientation, panel1, panel2);
				
				add(split_pane);
				split_pane.setDividerLocation(ratio);
				this.updateUI();
				break;
			}
		
	}
	
	/********************************************
	 * Swaps the two components of this split panel
	 * 
	 */
	public void swap(){
		
		double ratio = this.getSplitRatio();
		InterfaceGraphicWindow swap = panel2;
		panel2 = panel1;
		panel1 = swap;
		remove(split_pane);
		split_pane = new JSplitPane(split_orientation, panel1, panel2);
		add(split_pane);
		split_pane.setDividerLocation(ratio);
		this.updateUI();
		
	}
	
	/*********************************************
	 * Returns a stack of split panels, with the top-most being the highest in the nested tree
	 * 
	 * @return
	 */
	public Stack<InterfaceGraphicWindow> getSplitPanelStack(){
		PriorityQueue<DepthPanel> queue = getSplitPanelQueue(0);
		Stack<InterfaceGraphicWindow> stack = new Stack<InterfaceGraphicWindow>();
		while (queue.size() > 0)
			stack.push(queue.poll().panel);
		return stack;
	}
	
	/*********************************************
	 * Returns a reversed queue of split panels, with the bottom-most being the highest in the nested tree
	 * 
	 * @return
	 */
	protected PriorityQueue<DepthPanel> getSplitPanelQueue(int depth){
		
		PriorityQueue<DepthPanel> queue = new PriorityQueue<DepthPanel>(50, new Comparator<DepthPanel>(){
			public int compare(DepthPanel panel1, DepthPanel panel2){
				if (panel1.depth < panel2.depth) return 1;
				if (panel1.depth > panel2.depth) return -1;
				if (panel1.side < panel2.side) return 1;
				if (panel1.side > panel2.side) return -1;
				return 0;
				}
		});
		
		if (panel1 instanceof InterfaceSplitPanel){
			queue.addAll(((InterfaceSplitPanel) panel1).getSplitPanelQueue(depth + 1));
		}else{
			queue.add(new DepthPanel(panel1, depth, 0));
			}
		
		if (panel2 instanceof InterfaceSplitPanel){
			queue.addAll(((InterfaceSplitPanel) panel2).getSplitPanelQueue(depth + 1));
		}else{
			queue.add(new DepthPanel(panel2, depth, 1));
			}
		
		return queue;
		
	}
	
	private class DepthPanel{
		public InterfaceGraphicWindow panel;
		public int depth;
		public int side;
		public DepthPanel(InterfaceGraphicWindow panel, int depth, int side){
			this.panel = panel;
			this.depth = depth;
			this.side = side;
		}
	}
	
	protected ArrayList<InterfaceGraphicWindow> getWindows(int depth, int max_depth){
		
		ArrayList<InterfaceGraphicWindow> windows = new ArrayList<InterfaceGraphicWindow>();
		if (depth == max_depth) return windows;
		
		if (panel1 instanceof InterfaceGraphicWindow)
			windows.add((InterfaceGraphicWindow)panel1);
		if (panel2 instanceof InterfaceGraphicWindow)
			windows.add((InterfaceGraphicWindow)panel2);
	
		if (panel1 instanceof InterfaceSplitPanel)
			windows.addAll(((InterfaceSplitPanel)panel1).getWindows(depth + 1, max_depth));
		
		if (panel2 instanceof InterfaceSplitPanel)
			windows.addAll(((InterfaceSplitPanel)panel2).getWindows(depth + 1, max_depth));
		
		return windows;
		
	}
	
	@Override
	protected void init() {
		
		this.setLayout(new BorderLayout());
		this.add(split_pane, BorderLayout.CENTER);
		
	}
	
	public void setPanel(InterfaceGraphic p){
		
	}
	
	public void updateTitle(){
		
	}
	
	@Override
	public void setName(String thisName){
		
	}
	
	public InterfaceGraphic<?> getPanel(){
		return null;
	}
	
	@Override
	public void updateDisplay(){
		
	}
	
	@Override
	public String toString(){
		return "InterfaceSplitPanel";
	}
	
	@Override
	public String getLocalName() {
		return this.getClass().getSimpleName();
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		writer.write(_tab + "<InterfaceSplitPanel>\n");
		
		//Write left
		InterfaceGraphicWindow window = this.getWindow(0);
		writer.write(_tab2 + "<LeftWindow>\n");
		if (window != null)
			window.writeXML(tab + 2, writer, options, progress_bar);
		writer.write(_tab2 + "</LeftWindow>\n");
		
		//Write right
		window = this.getWindow(1);
		writer.write(_tab2 + "<RightWindow>\n");
		if (window != null)
			window.writeXML(tab + 2, writer, options, progress_bar);
		writer.write(_tab2 + "</RightWindow>\n");
		
		writer.write(_tab + "</InterfaceSplitPanel>\n"); 
		
	}
	
	
}