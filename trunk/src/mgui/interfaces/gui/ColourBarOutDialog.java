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

package mgui.interfaces.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.maps.ContinuousColourBar;
import mgui.interfaces.maps.ContinuousColourMap;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.util.ImageFunctions;

public class ColourBarOutDialog extends JDialog implements ActionListener,
														   ComponentListener,
														   PropertyChangeListener{

	ContinuousColourBar colour_bar;
	JButton cmdWrite = new JButton("Write");
	JButton cmdCancel = new JButton("Cancel");
	JSplitPane split_pane;
	double split_pos = 1.0;
	boolean update_split_pos = true;
	
	public ColourBarOutDialog(JFrame frame, ContinuousColourMap cmap){
		super(frame);
		colour_bar = new DialogColourBar(cmap, this);
		colour_bar.showAnchors = false;
		init();
	}
	
	public static void showDialog(JFrame frame, ContinuousColourMap cmap){
		ColourBarOutDialog dialog = new ColourBarOutDialog(frame, cmap);
		dialog.setVisible(true);
		//dialog.update_split_pos = false;
		dialog.setSplit(1.0);
		//dialog.update_split_pos = true;
	}
	
	protected void init(){
		
		update_split_pos = false;
		this.setLayout(new BorderLayout(0, 0));
		this.setTitle("Output colour bar");
		this.setSize(new Dimension(600,200));
		this.setMinimumSize(new Dimension(1, 70));
		this.getContentPane().setMinimumSize(new Dimension(1, 70));
		
		cmdWrite.setActionCommand("Write");
		cmdWrite.addActionListener(this);
		cmdCancel.setActionCommand("Cancel");
		cmdCancel.addActionListener(this);
		
		JPanel buttons = new JPanel();
		cmdWrite.setMaximumSize(new Dimension(10000, 30));
		cmdWrite.setMinimumSize(new Dimension(1, 30));
		cmdCancel.setMaximumSize(new Dimension(10000, 30));
		cmdCancel.setMinimumSize(new Dimension(1, 30));
		buttons.setMaximumSize(new Dimension(10000, 60));
		buttons.setMinimumSize(new Dimension(1, 60));
		
		buttons.setLayout(new BorderLayout(0, 0));
		buttons.add(cmdWrite, BorderLayout.NORTH);
		buttons.add(cmdCancel, BorderLayout.SOUTH);
		
		JPanel empty_panel = new JPanel();
		empty_panel.setMinimumSize(new Dimension(0,0));
		split_pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, colour_bar, empty_panel);
		//split_pane.addPropertyChangeListener(this);
		split_pane.setResizeWeight(1.0);
		//this.addComponentListener(this);
		colour_bar.setMinimumSize(new Dimension(1,1));
		
		this.add(split_pane, BorderLayout.CENTER);
		this.add(buttons, BorderLayout.SOUTH);
		
		update_split_pos = true;
	}
	
	public void setSplit(double d){
		split_pos = d;
		split_pane.setDividerLocation(d);
	}
	
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Cancel")){
			setVisible(false);
			return;
			}
		
		if (e.getActionCommand().equals("Write")){
			
			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(ImageFunctions.getPngFileFilter());
			
			fc.showSaveDialog(InterfaceSession.getSessionFrame());
			File file = fc.getSelectedFile();
			
			if (file == null) return;
				try{
					colour_bar.writeToImage(file);
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Snapshot written to '" + file.getAbsolutePath() + "'..", 
												  "Save colour bar snapshot",
												  JOptionPane.INFORMATION_MESSAGE);
				}catch (IOException ex){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Error writing to '" + file.getAbsolutePath() + "'..", 
												  "Save colour bar snapshot",
												  JOptionPane.ERROR_MESSAGE);
					}
				}
			
			return;
		
	}
	
	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentResized(ComponentEvent e) {
		update_split_pos = false;
		split_pane.setDividerLocation(split_pos);
		update_split_pos = true;
	}

	@Override
	public void componentShown(ComponentEvent e) {
		
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource().equals(split_pane)){
			if (!update_split_pos) return;
			this.split_pos = (double)split_pane.getDividerLocation() / (double)(split_pane.getWidth() - split_pane.getDividerSize());
			split_pos = Math.min(split_pos, 1.0);
			split_pos = Math.max(split_pos, 0.0);
			}
	}

	//override for the sole purpose of providing a new context menu and functions
	static class DialogColourBar extends ContinuousColourBar{
		
		ColourBarOutDialog dialog;
		
		public DialogColourBar(ContinuousColourMap map, ColourBarOutDialog dialog){
			super(map);
			this.dialog = dialog;
		}
		
		@Override
		public void handlePopupEvent(ActionEvent e) {
			
			if (!(e.getSource() instanceof JMenuItem)) return;
			JMenuItem item = (JMenuItem)e.getSource();
			
			if (item.getText().equals("Set limits..")){
				String scale = JOptionPane.showInputDialog("Map limits:", "" + min + " " + max);
				if (scale == null || !scale.contains(" ")) return;
				min = Double.valueOf(scale.substring(0, scale.indexOf(" ")));
				max = Double.valueOf(scale.substring(scale.indexOf(" ") + 1));
				repaint();
				return;
				}
			
			if (item.getText().equals("Font scale..")){
				String scale = JOptionPane.showInputDialog("New font scale:", "" + fontScale);
				if (scale == null) return;
				this.fontScale = Double.valueOf(scale);
				repaint();
				return;
				}
			
			if (item.getText().equals("Line weight scale..")){
				String scale = JOptionPane.showInputDialog("New line weight scale:", "" + lineWeightScale);
				if (scale == null) return;
				this.lineWeightScale = Double.valueOf(scale);
				repaint();
				return;
				}
			
			if (item.getText().equals("Text space..")){
				String scale = JOptionPane.showInputDialog("New text space proportion:", "" + divSize);
				if (scale == null) return;
				this.divSize = Double.valueOf(scale);
				repaint();
				return;
				}
			
			if (item.getText().equals("Foreground colour..")){
				Color c = JColorChooser.showDialog(InterfaceSession.getSessionFrame(), "Label text colour", this.getForeground());
				if (c == null) return;
				this.setForeground(c);
				repaint();
				return;
				}
			
			if (item.getText().equals("Background colour..")){
				Color c = JColorChooser.showDialog(InterfaceSession.getSessionFrame(), "Background colour", this.getBackground());
				if (c == null) return;
				this.setBackground(c);
				repaint();
				return;
				}
			
			if (item.getText().equals("Padding..")){
				String scale = JOptionPane.showInputDialog("New padding:", "" + padding);
				if (scale == null) return;
				this.padding = Integer.valueOf(scale);
				repaint();
				return;
				}
			
			if (item.getText().equals("Font..")){
				FontDialog dialog = new FontDialog();
				Font font = dialog.showDialog(label_font);
				if (font != null) label_font = new Font(font.getName(), font.getStyle(), 16);
				repaint();
				return;
				}
			
			if (item.getText().equals("Decimals..")){
				String s = JOptionPane.showInputDialog("Decimals (<0 for auto):", "" + decimals);
				if (s == null) return;
				this.decimals = Integer.valueOf(s);
				repaint();
				return;
			}
			
			if (item.getText().equals("Show divisions")){
				this.showDivisions = true;
				repaint();
				return;
				}
			
			if (item.getText().equals("Hide divisions")){
				this.showDivisions = false;
				repaint();
				return;
				}
			
			if (item.getText().equals("Show anchors")){
				this.showAnchors = true;
				repaint();
				return;
				}
			
			if (item.getText().equals("Hide anchors")){
				this.showAnchors = false;
				repaint();
				return;
				}
			
			if (item.getText().equals("Set dimensions..")){
				String dims = "" + dialog.getWidth() + " " + dialog.getHeight();
				dims = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), "Colour bar dimensions:", dims);
				if (dims == null) return;
				String[] new_dims = dims.split(" ");
				if (new_dims.length != 2) return;
				try{
					int w = Integer.valueOf(new_dims[0]);
					int h = Integer.valueOf(new_dims[1]);
					dialog.setSize(w, h);
					repaint();
				}catch (Exception ex){
					
					}
				return;
				}
			
		}
		
		@Override
		public InterfacePopupMenu getPopupMenu() {
			InterfacePopupMenu menu = new InterfacePopupMenu(this);
			menu.addMenuItem(new JMenuItem("Set limits.."));
			menu.addMenuItem(new JMenuItem("Set dimensions.."));
			menu.addMenuItem(new JMenuItem("Background colour.."));
			menu.addMenuItem(new JMenuItem("Foreground colour.."));
			menu.addMenuItem(new JMenuItem("Font.."));
			menu.addMenuItem(new JMenuItem("Font scale.."));
			menu.addMenuItem(new JMenuItem("Line weight scale.."));
			menu.addMenuItem(new JMenuItem("Text space.."));
			menu.addMenuItem(new JMenuItem("Text colour.."));
			menu.addMenuItem(new JMenuItem("Decimals.."));
			menu.addMenuItem(new JMenuItem("Padding.."));
			if (!this.showDivisions){
				menu.addMenuItem(new JMenuItem("Show divisions"));
			}else{
				menu.addMenuItem(new JMenuItem("Hide divisions"));
				}
			if (!this.showAnchors)
				menu.addMenuItem(new JMenuItem("Show anchors"));
			else
				menu.addMenuItem(new JMenuItem("Hide anchors"));
			
			return menu;
		}
		
		//disable mouse actions
		@Override
		public void mouseDragged(MouseEvent e) {
			
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			
		}
		
	}
	
}