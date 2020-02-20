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

package mgui.interfaces.math;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;

import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;

import org.cheffo.jeplite.JEP;


/*********************************************
 * Dialog box to define a mathematical expression, given an instance of VariableObject
 * 
 * @author Andrew Reid
 *
 */

public class MathExpressionDialogBox extends InterfaceOptionsDialogBox {

	JLabel lblVariable = new JLabel("VariableObject:");
	JTextField txtVariable = new JTextField("none");
	JLabel lblVariableList = new JLabel("Variables:");
	JList lstVariables = new JList();
	JScrollPane scrVariables = new JScrollPane();
	JLabel lblExpression = new JLabel("Expression:");
	JTextArea txtExpression = new JTextArea();
	JScrollPane scrExpression = new JScrollPane();
	JButton cmdClear = new JButton("Clear");
	JButton cmdVerify = new JButton("Verify");
	
	public MathExpressionDialogBox(){
		super();
	}
	
	public MathExpressionDialogBox(JFrame f, MathExpressionOptions opts){
		super(f, opts);
		init();
		if (opts != null)
			updateDialog(opts);
	}
	
	@Override
	protected void init(){
		buttonType = BT_OK_CANCEL;
		super.init();
		
		this.setTitle("Define mathematical expression");
		
		
		LineLayout layout = new LineLayout(20, 5, 0);
		setDialogSize(500, 500);
		setMainLayout(layout);
		
		scrVariables.setViewportView(lstVariables);
		scrExpression.setViewportView(txtExpression);
		cmdClear.addActionListener(this);
		cmdClear.setActionCommand("Clear");
		cmdVerify.addActionListener(this);
		cmdVerify.setActionCommand("Verify");
		lstVariables.addMouseListener(new ActionJList(lstVariables));

		lblVariableList.setToolTipText("Double-click item to insert into expression.");
		scrVariables.setToolTipText("Double-click item to insert into expression.");
		lstVariables.setToolTipText("Double-click item to insert into expression.");
		txtVariable.setEditable(false);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.025, 0.3, 1);
		mainPanel.add(lblVariable, c);
		c = new LineLayoutConstraints(1, 1, 0.35, 0.625, 1);
		mainPanel.add(txtVariable, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.5, 1);
		mainPanel.add(lblVariableList, c);
		c = new LineLayoutConstraints(3, 7, 0.025, 0.925, 1);
		mainPanel.add(scrVariables, c);
		c = new LineLayoutConstraints(8, 8, 0.05, 0.5, 1);
		mainPanel.add(lblExpression, c);
		c = new LineLayoutConstraints(9, 13, 0.025, 0.925, 1);
		mainPanel.add(scrExpression, c);
		c = new LineLayoutConstraints(14, 14, 0.5, 0.2325, 1);
		mainPanel.add(cmdClear, c);
		c = new LineLayoutConstraints(14, 14, 0.7425, 0.2325, 1);
		mainPanel.add(cmdVerify, c);
		
	}
	
	@Override
	public boolean updateDialog(InterfaceOptions opts){
		
		MathExpressionOptions options = (MathExpressionOptions)opts;
		
		Vector<String> variables = new Vector<String>(options.variable.getVariables());
		lstVariables.setListData(variables);
		lstVariables.updateUI();
		
		txtVariable.setText(options.variable.getName());
		
		this.options = options;
		
		setJEP();
		
		return true;
	}
	
	void setJEP(){
		MathExpressionOptions options = (MathExpressionOptions)this.options;
		ArrayList<String> variables = options.variable.getVariables();
		
		options.jep = new JEP();
		for (int i = 0; i < variables.size(); i++)
			options.jep.addVariable(variables.get(i), 0);
		
		options.jep.addStandardConstants();
		options.jep.addStandardFunctions();
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Clear")){
			txtExpression.setText("");
			return;
		}
		
		if (e.getActionCommand().equals("Verify")){
			MathExpressionOptions options = (MathExpressionOptions)this.options;
			if (options.jep == null) setJEP();
			options.jep.parseExpression(txtExpression.getText());
			if (options.jep.hasError()){
				//error encountered, show user
				InterfaceSession.log("Errors encountered parsing expression:\n" + options.jep.getErrorInfo());
				JOptionPane.showMessageDialog(null, "Error(s) encountered. See console.");
			}else{
				JOptionPane.showMessageDialog(null, "Looks good!");
				}
			return;
		}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			MathExpressionOptions options = (MathExpressionOptions)this.options;
			if (options.jep == null){
				//shouldn't happen
				JOptionPane.showMessageDialog(null, "No expression set!");
				return;
				}
			
			options.jep.parseExpression(txtExpression.getText());
			if (options.jep.hasError()){
				//error encountered, show user
				InterfaceSession.log("Errors encountered parsing expression:\n" + options.jep.getErrorInfo());
				JOptionPane.showMessageDialog(null, "Error(s) encountered. See console.");
				return;
				}
			
			this.setVisible(false);
			return;
			}
		
		super.actionPerformed(e);
	}
	
	private String getExpression(){
		
		return txtExpression.getText();
		
	}
	
	class ActionJList extends MouseAdapter{
		  protected JList list;
		    
		  public ActionJList(JList l){
		   list = l;
		   }
		    
		  @Override
		public void mouseClicked(MouseEvent e){
		   if(e.getClickCount() == 2){
		     int index = list.locationToIndex(e.getPoint());
		     ListModel dlm = list.getModel();
		     Object item = dlm.getElementAt(index);;
		     list.ensureIndexIsVisible(index);
		     txtExpression.insert(item.toString(), txtExpression.getCaretPosition());
		     }
		   }
	}

}