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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;

public class InterfaceHideablePanel extends InterfacePanel{

	public JLabel thisTitle = new JLabel("Click to hide");
	public InterfacePanel thisPanel;
	public boolean isHidden;
	
	public InterfaceHideablePanel(){
		super();
		init();
	}
	
	public InterfaceHideablePanel(InterfacePanel panel){
		super();
		setPanel(panel);
		init();
	}
	
	@Override
	protected void init(){
		thisTitle.addMouseListener(new MyMouseAdapter());
		thisTitle.setHorizontalAlignment(SwingConstants.CENTER);
		
		showPanelClicked();
	}
	
	public void setPanel(InterfacePanel panel){
		//if (thisPanel != null)
		//	removeDisplayListener(thisPanel);
		thisPanel = panel;
		//addDisplayListener(panel);
	}
	
	class MyMouseAdapter extends MouseInputAdapter{
		
		@Override
		public void mouseClicked(MouseEvent e){
			isHidden = !isHidden;
			showPanelClicked();
		}
		
	}
	
	public void showPanelClicked(){
		if (!isHidden){
			//show
			this.removeAll();
			setLayout(new GridBagLayout());
			GridBagConstraints cShow = new GridBagConstraints();
			cShow.fill = GridBagConstraints.BOTH;
			cShow.gridx = 0;
			cShow.gridy = 0;
			cShow.weighty = 0;
			cShow.weightx = 1;
			thisTitle.setText("Click to Hide");
			add(thisTitle, cShow);
			cShow.gridx = 0;
			cShow.gridy = 1;
			cShow.weighty = 5;
			cShow.weightx = 1;
			add(thisPanel, cShow);
			repaint();
			return;
		}
		this.removeAll();
		thisTitle.setText("Click to Show");
		setLayout(new BorderLayout());
		add(thisTitle, BorderLayout.CENTER);
		repaint();
	}
}