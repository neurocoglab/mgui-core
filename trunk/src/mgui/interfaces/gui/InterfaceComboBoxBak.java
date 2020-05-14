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

package mgui.interfaces.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicListUI;
import javax.swing.plaf.basic.BasicPopupMenuUI;
import javax.swing.plaf.basic.ComboPopup;

import mgui.interfaces.InterfaceObject;
import mgui.resources.icons.IconObject;

/***********************************************************
 * Extends {@link JComboBox} to provide fixed- or variable-width dropdown lists, as well as the 
 * ability to display icons.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceComboBoxBak extends JComboBox implements PopupMenuListener {

	protected RenderMode mode = RenderMode.AsBox;
	protected int width = 40, height = 50;
	protected boolean show_icons = true;
	
	public static enum RenderMode{
		AsBox,
		LongestItem,
		FixedWidth;
	}
	
	public InterfaceComboBoxBak(){
		this(RenderMode.AsBox, 40, 50, true);
	}
	
	public InterfaceComboBoxBak(RenderMode mode){
		this(mode, 40, 50, true);
	}
	
	public InterfaceComboBoxBak(RenderMode mode, int width, int height, boolean show_icons){
		super();
		this.mode = mode;
		this.width = width;
		this.height = height;
		this.show_icons = show_icons;
		this.setRenderer(new InterfaceComboBoxRenderer(show_icons));
		this.setUI(new CustomComboUI());
	
		
		addPopupMenuListener(this);
	}
	
	public void setFixedWidth(int width){
		this.width = width;
		this.repaint();
	}
	
	public int getFixedWidth(){
		return this.width;
	}
	
	public RenderMode getRenderMode(){
		return this.mode;
	}
	
	public void setRenderMode(RenderMode mode){
		this.mode = mode;
	}
	
	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		int i = 0;
	}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		// Set the size of combo box menu according to the mode
		JComboBox box = (JComboBox) e.getSource();
		Object comp = box.getUI().getAccessibleChild(box, 0);
		if (!(comp instanceof JPopupMenu)) return;
		JPopupMenu menu = (JPopupMenu)comp;
		
		int final_width = getFinalWidth();
		
		JComponent scrollPane = (JComponent) menu.getComponent(0);
		final_width = Math.max(final_width, this.getWidth());
        Dimension size = scrollPane.getPreferredSize();
        size.width = Math.max(final_width, size.width);
        scrollPane.setPreferredSize(size);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);
	}
	
	protected int getFinalWidth(){
		int final_width = width;
		
		switch (mode){
		
			case AsBox:

				break;
			
			case FixedWidth:
				//set width from fixed width
				//menu.setSize(width, height);
				
				//return;
				break;
				
			case LongestItem:
				//set width to the longest string in this list
				int longest = -1; 
				for (int i = 0; i < this.getItemCount(); i++){
					FontMetrics metrics = this.getFontMetrics(getFont());
					Object obj = this.getItemAt(i);
					String label = obj.toString();
					if (obj instanceof InterfaceObject)
						label = ((InterfaceObject)obj).getTreeLabel();
					longest = Math.max(longest, metrics.stringWidth(label));
					}
				if (longest > 0) 
					final_width = longest;
				
			}
		
		return final_width;
	}
	
	static class InterfaceComboBoxRenderer extends BasicComboBoxRenderer{
		
		protected boolean show_icon = true;
		
		
		public InterfaceComboBoxRenderer(){
			this(true);
		}
		
		public InterfaceComboBoxRenderer(boolean show_icon){
			super();
			this.show_icon = show_icon;
			setBackground(Color.white);
			setForeground(Color.black);
		}
		
		@Override
		public Component getListCellRendererComponent(JList list, 
													  Object value,
													  int index, 
													  boolean isSelected, 
													  boolean cellHasFocus) {
			
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			if (value instanceof IconObject){
				setIcon(((IconObject)value).getObjectIcon()); 
				}
			
			if (value instanceof InterfaceObject){
				setText(((InterfaceObject)value).getTreeLabel());
				}
			
			setBorder(BorderFactory.createLineBorder(getForeground()));
			
			InterfaceComboBoxRenderer wtf = this;
			return wtf;
			
		}
		
	}
	
	static class CustomPopupMenuUI extends BasicPopupMenuUI{
		
		public CustomPopupMenuUI(){
			
		}
		
	}
	
	static class CustomListUI extends BasicListUI{
		
		public InterfaceComboBox combo_box;
		//protected BasicListUI current_ui;
		
		public CustomListUI(InterfaceComboBox combo_box){
			this.combo_box = combo_box;
			
			
			//this.current_ui = (BasicListUI)UIManager.getUI(new JList());
		}

		@Override
		protected void paintCell(Graphics g,
						         int row,
						         Rectangle rowBounds,
						         ListCellRenderer cellRenderer,
						         ListModel dataModel,
						         ListSelectionModel selModel,
						         int leadIndex)  {
			
		        Object value = dataModel.getElementAt(row);
		        boolean cellHasFocus = list.hasFocus() && (row == leadIndex);
		        boolean isSelected = selModel.isSelectedIndex(row);

		        Component rendererComponent =
		            cellRenderer.getListCellRendererComponent(list, value, row, isSelected, cellHasFocus);

		        int cx = rowBounds.x;
		        int cy = rowBounds.y;
		        int cw = rowBounds.width;
		        int ch = rowBounds.height;

		        rendererPane.paintComponent(g, rendererComponent, list, cx, cy, cw, ch, true);
		       
		}
		
		@Override
		public Dimension getPreferredSize(JComponent c) {
			Dimension dim = null;
			//if (current_ui != null)
			//	dim = current_ui.getPreferredSize(c);
			//else
				dim = super.getPreferredSize(c);
			
			//if (combo_box != null) 
				//dim.width = Math.max(combo_box.getFinalWidth(), dim.width);
			
			return dim;
		}
				
	}
	
	//a lot of pointless work to get a simple fixed-width combo box
	static class CustomComboUI extends BasicComboBoxUI{
		
		//BasicComboBoxUI current_ui;
		
		public CustomComboUI(){
			//current_ui = (BasicComboBoxUI)UIManager.getUI(new JComboBox());
		}
		
		public static ComponentUI createUI(JComponent c) {
			return new CustomComboUI();
		}
		
		@Override
		protected ComboPopup createPopup() {
			return new InterfaceComboBoxPopup( (InterfaceComboBox)comboBox );
	    }

		@Override
	    public void installUI( JComponent c ) {
	        super.installUI(c);
	        
	        comboBox = (JComboBox)c;
	        popup = createPopup();
	    }
		
		@Override
		protected ListCellRenderer createRenderer() {
			return new InterfaceComboBoxRenderer(true);
		}
		
		/**
	     * Creates an button which will be used as the control to show or hide
	     * the popup portion of the combo box.
	     *
	     * @return a button which represents the popup control
	     */
	    @Override
		protected JButton createArrowButton() {
	        JButton button = new BasicArrowButton(SwingConstants.SOUTH,
					    Color.white,
					    Color.lightGray,
					    Color.darkGray,
					    Color.black);
	        button.setName("ComboBox.arrowButton");
	        return button;
	    }

		@Override
		public Dimension getPreferredSize(JComponent c) {
			Dimension dim = getPreferredSize(c);
			if (this.comboBox instanceof InterfaceComboBox){
				//dim.width = Math.max(dim.width, ((InterfaceComboBox)comboBox).getFinalWidth());
				}
			
			return dim;
		}

		@Override
		public void paint(Graphics g, JComponent c) {
	        super.paint(g, c);
	        
		}
		
	}
	
	static class InterfaceComboBoxPopup extends BasicComboPopup{
		
		public InterfaceComboBoxPopup(InterfaceComboBox combo) {
			super(combo);
			this.setUI(new BasicPopupMenuUI());
			list.setUI(new CustomListUI(combo));
			
		}
		
	}
	

}