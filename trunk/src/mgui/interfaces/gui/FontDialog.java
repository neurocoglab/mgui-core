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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mgui.interfaces.InterfaceSession;

public class FontDialog extends JDialog {
	
	public FontChooser font = new FontChooser(this);
	
	public FontDialog(){
		this(null);
	}
	
	public FontDialog(Font current_font){
		this.setLocation(new Point(100,100)); 
 		this.setSize(new Dimension(360, 242)); 
		this.getContentPane().add(font);
		if (current_font != null)
			font.setSelectedFont(current_font);
		this.setModal(true);
	}
	
	public static Font showDialog(){
		return showDialog(null);
	}
	
	
	public static Font showDialog(Font font){
		FontDialog dialog = new FontDialog(font);
		dialog.setVisible(true);
		return dialog.getSelectedFont();
		
	}
	
	public void setSelectedFont(Font selected_font){
		this.font.setSelectedFont(selected_font);
	}
	
	public Font getSelectedFont(){
		return font.SelectedFont;
	}
  
 	class FontChooser extends JPanel{
 	
 	// all variables, aside from the final public Font are simply for the UI of the class
 	 	private JLabel Preview; 
 	 	private JComboBox FontSize; 
 	 	private JList FontList; 
 	 	private JScrollPane jScrollPane1; 
 	 	private JList Style; 
 	 	private JScrollPane jScrollPane2; 
 	 	public  JButton OK_Button; 
 	 	private JButton Cancel_Button; 
 	 	// the SelectedFont attribute can be accessed by other classes
 	 	public  Font SelectedFont;
 	 	private FontDialog Parent;
 	 	
 	 	boolean doUpdate = true;
 		
	 	public FontChooser(FontDialog parent) { 
	 		super(); 
	 		initialiseComponent(); 
	 		String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	 		this.FontList.setListData(fonts);
	 		
	 		updateSizes();
	 		
	 		String[] styles = {"Regular", "Italic", "Bold", "Bold Italic"};
	 		this.Style.setListData(styles);
	 		this.Parent = parent;
	 		
	 	} 
	 	
	 	private void initialiseComponent() { 
	 		this.Preview = new JLabel(); 
	 		this.FontSize = new JComboBox(); 
	 		FontSize.setEditable(true);
	 		this.FontList = new JList(); 
	 		this.jScrollPane1 = new JScrollPane(); 
	 		this.Style = new JList(); 
	 		this.jScrollPane2 = new JScrollPane(); 
	 		this.OK_Button = new JButton(); 
	 		this.Cancel_Button = new JButton(); 
	  
	 		this.Preview.setText("Preview"); 
	
	 		this.FontSize.addActionListener(new ActionListener() { 
	 			public void actionPerformed(ActionEvent e) { 
	 				FontChanged(); 
	 			} 
	 		}); 
	 		this.FontList.addListSelectionListener(new ListSelectionListener() { 
	 			public void valueChanged(ListSelectionEvent e) { 
	 				FontChanged();
	 			} 
	 		}); 
	 		
	 		this.jScrollPane1.setViewportView(this.FontList); 
	 		
	 		this.Style.addListSelectionListener(new ListSelectionListener() { 
	 			public void valueChanged(ListSelectionEvent e) { 
	 				FontChanged();
	 			} 
	 		}); 
	 		
	 		this.jScrollPane2.setViewportView(this.Style); 
	
	 		this.OK_Button.setText("OK"); 
	 		this.OK_Button.addActionListener(new ActionListener() { 
	 			public void actionPerformed(ActionEvent e) { 
	 				OK_Button_actionPerformed(e); 
	 			} 
	 		}); 
	
	 		this.Cancel_Button.setText("Cancel"); 
	 		this.Cancel_Button.addActionListener(new ActionListener() { 
	 			public void actionPerformed(ActionEvent e) { 
	 				Cancel_Button_actionPerformed(e);
	 			} 
	 		}); 
	
	 		this.setLayout(null); 
	 		addComponent(this, this.Preview, 18,166,132,27); 
	 		addComponent(this, this.FontSize, 231,15,100,22); 
	 		addComponent(this, this.jScrollPane1, 15,14,196,131); 
	 		addComponent(this, this.jScrollPane2, 231,47,100,100); 
	 		addComponent(this, this.OK_Button, 158,166,83,28); 
	 		addComponent(this, this.Cancel_Button, 248,165,83,28); 
	
	 		this.setLocation(new Point(100,100)); 
	 		this.setSize(new Dimension(360, 242)); 
	 	} 
	  
	 	private void addComponent(Container container,Component c,int x,int y,int width,int height) { 
	 		c.setBounds(x,y,width,height); 
	 		container.add(c); 
	 	} 
	 	
	 	public void setSelectedFont(Font font){
	 		this.SelectedFont = font;
	 		updateSizes();
	 		
	 		switch (font.getStyle()){
		 		case Font.PLAIN:
		 			this.Style.setSelectedIndex(0);
		 			break;
		 		case Font.ITALIC:
		 			this.Style.setSelectedIndex(1);
		 			break;
		 		case Font.BOLD:
		 			this.Style.setSelectedIndex(2);
		 			break;
		 		case Font.BOLD+Font.ITALIC:
		 			this.Style.setSelectedIndex(3);
		 			break;
		 		default:
		 			this.Style.setSelectedIndex(0);
	 			}
	 		
	 		this.FontList.setSelectedValue(font.getFamily(), true);
	 		this.FontSize.setSelectedItem(font.getSize());
	 		this.Preview.setFont(font);
	 		
	 		updateUI();
	 		
	 	}
	 	
	 	private void updateSizes(){
	 		doUpdate = false;
	 		
	 		FontSize.removeAllItems();
	 		
	 		doUpdate = true;
	 		
	 		int current_size = -1;
	 		if (this.SelectedFont != null)
	 			current_size = SelectedFont.getSize();
	 		for (int i = 1; i <= 15; i += 1)
	 			this.FontSize.addItem(i);
	 		for (int i = 16; i <= 72; i += 2){
	 			this.FontSize.addItem(i);
	 			if (current_size == i + 1)
	 				this.FontSize.addItem(current_size);
	 			}
	 		
	 	}
	
	 	private void FontChanged() { 
	 		if (!doUpdate) return;
	 		try {
		 		//int i = this.FontSize.getSelectedIndex(); //*2 + 8;
		 		//int size = 0;
		 		//if (i < 11)
		 		//	size = i + 1;
		 		//else
		 		//	size = ((i - 6) * 2) + 8;
	 			Integer size = (Integer)this.FontSize.getSelectedItem();
	 			if (size == null) size = 10;
	 			
	 			if (FontList.getSelectedValue() == null) return;
		 		String name = (String)FontList.getSelectedValue().toString();
		 		if (this.Style.getSelectedIndex() == 0) // Regular
		 			this.SelectedFont = new Font(name, Font.PLAIN, size);
		 		else if (this.Style.getSelectedIndex() == 1) // Italic
		 			this.SelectedFont = new Font(name, Font.ITALIC, size);
		 		else if (this.Style.getSelectedIndex() == 2) // Bold
		 			this.SelectedFont = new Font(name, Font.BOLD, size);
		 		else if (this.Style.getSelectedIndex() == 3) // Bold Italic
		 			this.SelectedFont = new Font(name, Font.BOLD+Font.ITALIC, size);
		 		else // default, none selected
		 			this.SelectedFont = new Font(name, Font.PLAIN, size);
		 		this.Preview.setFont(this.SelectedFont);
	 		} catch (Exception ex) {
	 			InterfaceSession.log("Error setting font dialog..." + ex.getMessage());
	 		}
	 	} 
	  
	 	private void OK_Button_actionPerformed(ActionEvent e) { 
	 		this.Parent.setVisible(false);
	 		// the font has been saved in memory
	 	} 
	  
	 	private void Cancel_Button_actionPerformed(ActionEvent e) { 
	 		this.SelectedFont = null;
	 		this.Parent.setVisible(false);
	 	} 
	}


}