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
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;

/***********************
 * Interface class acting as a general dialog box that can be extended and designed
 * using the protected init() method and the actionPerformed method implementing the
 * ActionListener interface.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 */

@SuppressWarnings("serial")
public abstract class InterfaceDialogBox extends JDialog implements ActionListener {
	
	public final static int BT_BLANK = 0;
	public final static int BT_OK = 1;
	public final static int BT_OK_CANCEL = 2;
	
	public final static String DLG_CMD_OK = "DLG_CMD_OK";
	public final static String DLG_CMD_CANCEL = "DLG_CMD_CANCEL";
	
	protected JButton cmdOK, cmdCancel;
	protected JPanel buttonPanel;
	protected JPanel mainPanel;
	protected LayoutManager mainLayout = new BorderLayout();
	protected InterfacePanel parentPanel;
	/**@deprecated**/
	@Deprecated
	protected InterfaceDialogUpdater updater;
	
	protected int buttonType = BT_OK_CANCEL;
	protected String type = "None";
	
	protected int minButtonSize = 80;
	protected int minMainSize = 100;
	protected int minWidth = 200;
	
	public InterfaceDialogBox(){
		super();
	}
	
	public InterfaceDialogBox(JFrame aFrame){
		super(aFrame, true);
	}
	
	/**********
	 * @deprecated
	 * @param aFrame
	 * @param updater
	 */
	@Deprecated
	public InterfaceDialogBox(JFrame aFrame, InterfaceDialogUpdater updater){
		super(aFrame, true);
		this.updater = updater;
	}
	
	public void setButtonType(int t){
		buttonType = t;
	}
	
	public int getButtonType(){
		return buttonType;
	}
	
	public String getDialogType(){
		return type;
	}
	
	public void setDialogType(String s){
		type = s;
	}
	
	/**********************************************
	 * Updates the controls on this dialog.
	 * 
	 * @return
	 */
	public boolean updateDialog(){
		return false;
	}
	
	public boolean updateDialog(InterfaceOptions p){
		return false;
	}
	
	public void setMainLayout(LayoutManager lm){
		mainLayout = lm;
		mainPanel.setLayout(mainLayout);
	}
	
	public void setDialogSize(int x, int y){
		if (y < minButtonSize + minMainSize) y = minButtonSize + minMainSize;
		if (x < minWidth) x = minWidth;
		
		this.setPreferredSize(new Dimension(x, y));
		this.setMinimumSize(new Dimension(x, y));
		this.setSize(new Dimension(x, y));
		Dimension buttonSize = new Dimension(x, minButtonSize);
		buttonPanel.setPreferredSize(buttonSize); // Math.max(minButtonSize, (int)(y * 0.2))));
		buttonPanel.setMinimumSize(buttonSize);
		buttonPanel.setMaximumSize(buttonSize);
		buttonPanel.setSize(buttonSize);
		mainPanel.setPreferredSize(new Dimension(x, y - minButtonSize)); // Math.max(minMainSize, (int)(y * 0.8))));
		this.setLocationRelativeTo(getParent());
		
	}
	
	protected void init(){
		//set up stuff
		cmdOK = new JButton("OK");
		cmdOK.setActionCommand(DLG_CMD_OK);
		cmdOK.addActionListener(this);
		cmdCancel = new JButton("Cancel");
		cmdCancel.setActionCommand(DLG_CMD_CANCEL);
		cmdCancel.addActionListener(this);
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new LineLayout(20, 5, 0));
		mainPanel = new JPanel();
		mainPanel.setLayout(mainLayout);
		setDialogSize(400, 200);
		
		//add components if necessary
		LineLayoutConstraints c;
		switch(buttonType){
			case BT_OK:
				//add ok button
				c = new LineLayoutConstraints(1, 2, 0.7, 0.25, 1);
				buttonPanel.add(cmdOK, c);
				break;
				
			case BT_OK_CANCEL:
				//add ok and cancel button
				c = new LineLayoutConstraints(1, 2, 0.4, 0.25, 1);
				buttonPanel.add(cmdCancel, c);
				c = new LineLayoutConstraints(1, 2, 0.7, 0.25, 1);
				buttonPanel.add(cmdOK, c);
				break;
			}
		
		//add all
		this.setLayout(new BorderLayout());
		if (buttonType != BT_BLANK)
			this.add(buttonPanel, BorderLayout.SOUTH);
		this.add(mainPanel, BorderLayout.CENTER);
		this.setLocationRelativeTo(getParent());
		
	}
	
	public void setMainPanel(JPanel p){
		if (mainPanel != null)
			this.remove(mainPanel);
		mainPanel = p;
		this.add(mainPanel, BorderLayout.CENTER);
	}
	
	public InterfaceDialogBox(JFrame frame, boolean modal){
		super(frame, modal);
	}
		
	public void actionPerformed(ActionEvent e) {
	
		//handle cancel event
		if (e.getActionCommand().equals(DLG_CMD_CANCEL)){
			this.setVisible(false);
			}

	}
	

}