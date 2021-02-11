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
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

import org.apache.commons.collections15.Transformer;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.NamedObject;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.trees.TreeObject;
import mgui.resources.icons.IconObject;


/***********************
 * Combo box which implements a variable width popup, which depends on the render mode and the
 * length of its items:
 * 
 * <p>Render Mode | Behaviour
 * <br>AsBox       | Width is the same as the combo box width (default behaviour)
 * <br>FixedWidth  | Width is fixed at a specific value
 * <br>LongestItem | Width is at minimum the width of the combo box, otherwise the length of the longest item,
 * up to a fixed maximum
 * 
 * <p> Adapted from SteppedComboBox: <a href="http://www.devdaily.com/java/swing/tame/combobox/SteppedComboBox.java.shtml">
 * http://www.devdaily.com/java/swing/tame/combobox/SteppedComboBox.java.shtml</a>
 * 
 * @author Nobuo Tamemasa
 * @author Andrew Reid
 * @version 1.0
 * 
 **********/
public class InterfaceComboBox extends JComboBox<Object> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int popupWidth;
	protected RenderMode mode = RenderMode.AsBox;
	SteppedComboBoxUI ui;

	protected Transformer<Object,String> name_transformer = null;
	
  	public static enum RenderMode{
		AsBox,			//same as combo box
		LongestItem,	//set from longest item
		FixedWidth;		//set from fixed width
	}
  
  	/***************************************************
  	 * Constructor for this combo box, with default parameters
  	 * 
  	 */
  	public InterfaceComboBox(){
  		this(RenderMode.AsBox, true, 50, null, null);
  	}
  	
  	/***************************************************
  	 * Constructor for this combo box
  	 * 
  	 * @param mode			RenderMode specifying how items are to be rendered; one of:
  	 * 					    AsBox, LongestItem, FixedWidth
  	 * @param show_icons	Whether to show icons next to the objects (must be instances of 
  	 * 						{@linkplain IconObject}).
  	 * @param width 		The desired width for the item box
  	 */
  	public InterfaceComboBox(RenderMode mode, boolean show_icons, int width){
  		this(mode, show_icons, width, null, true);
  	}
  	
  	/***************************************************
  	 * Constructor for this combo box
  	 * 
  	 * @param mode			RenderMode specifying how items are to be rendered; one of:
  	 * 					    AsBox, LongestItem, FixedWidth
  	 * @param show_icons	Whether to show icons next to the objects (must be instances of 
  	 * 						{@linkplain IconObject}).
  	 * @param width 		The desired width for the item box
  	 * @param icon			A default icon
  	 */
  	public InterfaceComboBox(RenderMode mode, boolean show_icons, int width, Icon icon){
  		this(mode, show_icons, width, icon, true);
  	}
  	
  	/***************************************************
  	 * Constructor for this combo box
  	 * 
  	 * @param mode			RenderMode specifying how items are to be rendered; one of:
  	 * 					    AsBox, LongestItem, FixedWidth
  	 * @param show_icons	Whether to show icons next to the objects (must be instances of 
  	 * 						{@linkplain IconObject}).
  	 * @param width 		The desired width for the item box
  	 * @param model_name	If the objects are {@linkplain InterfaceShape} instances, displays
  	 * 						their name as "model_name.shape_name".
  	 */
  	public InterfaceComboBox(RenderMode mode, boolean show_icons, int width, boolean model_name){
  		this(mode, show_icons, width, null, model_name);
  	}
  	
  	/***************************************************
  	 * Constructor for this combo box
  	 * 
  	 * @param mode			RenderMode specifying how items are to be rendered; one of:
  	 * 					    AsBox, LongestItem, FixedWidth
  	 * @param show_icons	Whether to show icons next to the objects (must be instances of 
  	 * 						{@linkplain IconObject}).
  	 * @param width 		The desired width for the item box
  	 * @param model_name	If the objects are {@linkplain InterfaceShape} instances, displays
  	 * 						their name as "model_name.shape_name".
  	 */
  	public InterfaceComboBox(RenderMode mode, boolean show_icons, int width, Icon icon, boolean model_name){
  		super();
  		if (model_name){
	  		name_transformer = new Transformer<Object,String>(){
		  			public String transform (Object obj){
		  				if (obj == null) return "~";
		  				// Try all the best options first
		  				if (!(obj instanceof InterfaceShape)){
		  					if (obj instanceof TreeObject)
		  						return ((TreeObject)obj).getTreeLabel();
		  					if (obj instanceof NamedObject)
		  						return ((NamedObject)obj).getName();
		  					return obj.toString();
		  					}
		  				InterfaceShape shape = (InterfaceShape)obj;
		  				ShapeModel3D model = shape.getModel();
		  				if (model == null) return shape.getName();
		  				return model.getName() + "." + shape.getName();
		  			}
		  		};
		  	this.setRenderer(new InterfaceComboBoxRenderer(show_icons, name_transformer, null));
	  	}else{
	  		name_transformer = null;
	  		}
  		
  		//this.setBorder(new EmptyBorder(20,1,20,1));
  		this.mode = mode;
	  	this.setUI(new SteppedComboBoxUI());
	  	this.setRenderer(new InterfaceComboBoxRenderer(show_icons, name_transformer, icon));
	  	popupWidth = width;
  	}
  	
  	/***************************************************
  	 * Constructor for this combo box
  	 * 
  	 * @param mode				RenderMode specifying how items are to be rendered; one of:
  	 * 					    	AsBox, LongestItem, FixedWidth
  	 * @param show_icons		Whether to show icons next to the objects (must be instances of 
  	 * 							{@linkplain IconObject}).
  	 * @param width 			The desired width for the item box
  	 * @param name_transformer	The {@linkplain Transformer<Object,String} object used to determine
  	 * 							the name to display for an item
  	 */
  	public InterfaceComboBox(RenderMode mode, boolean show_icons, int width, Transformer<Object,String> name_transformer){
  		this(mode, show_icons, width, name_transformer, null);
  	}
  	
  	/***************************************************
  	 * Constructor for this combo box
  	 * 
  	 * @param mode				RenderMode specifying how items are to be rendered; one of:
  	 * 					    	AsBox, LongestItem, FixedWidth
  	 * @param show_icons		Whether to show icons next to the objects (must be instances of 
  	 * 							{@linkplain IconObject}).
  	 * @param width 			The desired width for the item box
  	 * @param name_transformer	The {@linkplain Transformer<Object,String} object used to determine
  	 * 							the name to display for an item
  	 * @param icon 				A default icon
  	 */
  	public InterfaceComboBox(RenderMode mode, boolean show_icons, int width, Transformer<Object,String> name_transformer, Icon icon){
	  super();
	  this.mode = mode;
	  this.setUI(new SteppedComboBoxUI());
	  this.setRenderer(new InterfaceComboBoxRenderer(show_icons, name_transformer, icon));
	  popupWidth = width;
	  this.name_transformer = name_transformer;
  	}
  
  public InterfaceComboBox(ComboBoxModel aModel) {
    super(aModel);
    setUI(new SteppedComboBoxUI());
    popupWidth = 0;
  }

  public InterfaceComboBox(final Object[] items) {
    super(items);
    setUI(new SteppedComboBoxUI());
    popupWidth = 0;
  }

  public InterfaceComboBox(Vector items) {
    super(items);
    setUI(new SteppedComboBoxUI());
    popupWidth = 0;
  }
  
  /***************************************************
   * Copies this combo box to a new combo box, containing the same items.
   * 
   * @return
   */
  public InterfaceComboBox copy(){
	  InterfaceComboBoxRenderer renderer = (InterfaceComboBoxRenderer)this.getRenderer();
	  InterfaceComboBox copy = new InterfaceComboBox(mode, renderer.show_icon, popupWidth, name_transformer);
	  for (int i = 0; i < this.getItemCount(); i++)
		  copy.addItem(this.getItemAt(i));
	  return copy;
  }

  	public void setInsets(Insets insets){
  		if (this.getUI() instanceof SteppedComboBoxUI){
  			((SteppedComboBoxUI)this.getUI()).setInsets(insets);
  			}
  	}
  
  	public void setPopupWidth(int width) {
  		popupWidth = width;
  	}
  	
  	public void setPopupHeight(int height){
  		this.setSize(popupWidth, height);
  	}

  	public Dimension getPopupSize() {
	    Dimension size = getSize();
	    switch(mode){
	    	case AsBox:
	    		return size;
	    	case FixedWidth:
	    		if (popupWidth < size.width * 1.2 ) popupWidth = (int)((float)size.width * 1.2);
	    		return new Dimension(popupWidth, size.height);
	    	
	    	case LongestItem:
	    		if (popupWidth < size.width * 1.2 ) popupWidth = (int)((float)size.width * 1.2);
	    		int width = size.width;
	    		int longest = -1; 
				for (int i = 0; i < this.getItemCount(); i++){
					FontMetrics metrics = this.getFontMetrics(getFont());
					Object obj = this.getItemAt(i);
					if (obj != null){
						String label = obj.toString();
						if (name_transformer != null){
							label = name_transformer.transform(obj);
							if (label == null) label = obj.toString();
						}else if (obj instanceof InterfaceObject){
							label = ((InterfaceObject)obj).getTreeLabel();
							}
						
						longest = Math.max(longest, metrics.stringWidth(label) + getIconSize());
						}
					}
				if (longest > width) 
					width = longest;
				if (width > popupWidth)
					width = popupWidth;
				return new Dimension(width, size.height);
		    	}
	    
	    return new Dimension(popupWidth, size.height);
  	}
  	
  	protected int getIconSize(){
  		//icon size plus possible scroll bar
  		return getSize().height * 2 + 20;
  	}
  	
  	class ButtonUI extends BasicComboBoxUI {

  	    @Override 
  	    protected JButton createArrowButton() {
  	        return new BasicArrowButton(
  	            BasicArrowButton.SOUTH,
  	            Color.cyan, Color.magenta,
  	            Color.yellow, Color.blue);
  	    }
  	    
  	}
  	
}




/**
@author Nobuo Tamemasa
@version 1.0 12/12/98
*/
class SteppedComboBoxUI extends BasicComboBoxUI {
  
	JPanel background_panel;
	Insets background_padding;
	
	public SteppedComboBoxUI(){
		this(null);
	}
	
	public SteppedComboBoxUI(Insets padding){
		super();
		background_panel = new JPanel();
		LineBorder border = new LineBorder(Color.gray, 1, true);
		Border margin = new EmptyBorder(2,2,2,2);
		background_panel.setBorder(new CompoundBorder(margin, border));
		background_panel.setOpaque(true);
		background_padding = padding;
	}
	
	public void setInsets(Insets insets){
		background_padding = insets;
	}
	
	@Override
	public void paintCurrentValueBackground(Graphics g,Rectangle bounds,boolean hasFocus) {
		int x = bounds.x, y = bounds.y, w = bounds.width, h = bounds.height;
        currentValuePane.paintComponent(g,background_panel,comboBox,x,y,w,h,true);
	}
	
	//Override
	public void paintCurrentValue(Graphics g,Rectangle bounds,boolean hasFocus) {
        ListCellRenderer renderer = comboBox.getRenderer();
        Component c;

        if ( hasFocus && !isPopupVisible(comboBox) ) {
            c = renderer.getListCellRendererComponent( listBox,
                                                       comboBox.getSelectedItem(),
                                                       -1,
                                                       true,
                                                       false );
            //((JComponent)c).setOpaque(false);
        }
        else {
            c = renderer.getListCellRendererComponent( listBox,
                                                       comboBox.getSelectedItem(),
                                                       -1,
                                                       false,
                                                       false );
            c.setBackground(UIManager.getColor("ComboBox.background"));
        }
        c.setFont(comboBox.getFont());
        if ( hasFocus && !isPopupVisible(comboBox) ) {
            c.setForeground(listBox.getSelectionForeground());
            c.setBackground(listBox.getSelectionBackground());
        }
        else {
            if ( comboBox.isEnabled() ) {
                c.setForeground(comboBox.getForeground());
                c.setBackground(comboBox.getBackground());
            }
            else {
                c.setForeground(UIManager.getDefaults().getColor("ComboBox.disabledForeground"));
                c.setBackground(UIManager.getDefaults().getColor("ComboBox.disabledBackground"));
            }
        }

        // Fix for 4238829: should lay out the JPanel.
        boolean shouldValidate = false;
        if (c instanceof JPanel)  {
            shouldValidate = true;
        }

        int x = bounds.x, y = bounds.y, w = bounds.width, h = bounds.height;
        if (padding != null) {
            x = bounds.x + padding.left;
            y = bounds.y + padding.top;
            w = bounds.width - (padding.left + padding.right);
            h = bounds.height - (padding.top + padding.bottom);
        }
        
        if (background_padding != null) {
	        x += background_padding.left;
	        y += background_padding.top;
	        w -= (background_padding.left + background_padding.right);
	        h -= (background_padding.top + background_padding.bottom);
	        }

        currentValuePane.paintComponent(g,c,comboBox,x,y,w,h,shouldValidate);
    }
	
//	@Override
//	protected void installDefaults() {
//		padding = new Insets(20,1,20,1);
//	}
	
	@Override
	protected ComboPopup createPopup() {
    BasicComboPopup popup = new BasicComboPopup( comboBox ) {

      @Override
	public void show() {
    	  InterfaceComboBox i_box = (InterfaceComboBox)comboBox;
    	  Dimension popupSize = i_box.getPopupSize();
	      popupSize.setSize( popupSize.width, getPopupHeightForRowCount( comboBox.getMaximumRowCount() ) );
	      Rectangle popupBounds = computePopupBounds(0, 
	    		  									 comboBox.getBounds().height-1, 
	    		  									 popupSize.width, 
	    		  									 popupSize.height);
	      scroller.setMaximumSize( popupBounds.getSize() );
	      scroller.setPreferredSize( popupBounds.getSize() );
	      scroller.setMinimumSize( popupBounds.getSize() );
	      //list.invalidate();
	      int selectedIndex = comboBox.getSelectedIndex();
	      if ( selectedIndex == -1 ) {
	    	  list.clearSelection();
	      } else {
	    	  list.setSelectedIndex( selectedIndex );
	      	  }
	      
	      list.ensureIndexIsVisible( list.getSelectedIndex() );
	      setLightWeightPopupEnabled( false ); // comboBox.isLightWeightPopupEnabled() );
	
	      show( comboBox, popupBounds.x, popupBounds.y );
      	}
    };
    
    popup.getAccessibleContext().setAccessibleParent(comboBox);
    return popup;
  }
	
	@Override protected JButton createArrowButton() {
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/combo_arrow.png");
		Icon icon = new ImageIcon(imgURL);
		JButton button = new JButton(icon);
		button.setMargin( new Insets( 3, 3, 3, 3 ) );
		return button;
    }
	
}