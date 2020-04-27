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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import org.jogamp.vecmath.Matrix4d;
import org.jogamp.vecmath.SingularMatrixException;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceFrame;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.io.domestic.variables.MatrixTransformLoader;


/****************************************
 * Dialog to edit and/or load a 4 x 4 matrix.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class MatrixEditorDialog extends InterfaceDialogBox {

	public Matrix4d matrix;
	
	InterfacePanel panel;
	MatrixModel model;
	JTable table;
	
	JButton cmdLoad = new JButton("Load..");
	JButton cmdInvert = new JButton("Invert");
	JLabel lblMessage = new JLabel();
	
	public MatrixEditorDialog(Matrix4d matrix){
		super();
		this.matrix = new Matrix4d(matrix);
		_init();
	}
	
	public MatrixEditorDialog(InterfacePanel panel, Matrix4d matrix){
		super(InterfaceSession.getSessionFrame());
		this.panel = panel;
		this.matrix = new Matrix4d(matrix);
		_init();
	}
	
	public MatrixEditorDialog(InterfaceFrame frame, Matrix4d matrix){
		super(frame);
		this.matrix = new Matrix4d(matrix);
		_init();
	}
	
	void _init(){
		this.setButtonType(BT_OK_CANCEL);
		
		super.init();
		
		this.setDialogSize(400,260);
		this.setTitle("Edit 4x4 Matrix");
		
		cmdLoad.addActionListener(this);
		cmdLoad.setActionCommand("Load Matrix");
		cmdInvert.addActionListener(this);
		cmdInvert.setActionCommand("Invert Matrix");
		
		LineLayout lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		
		model = new MatrixModel(matrix);
		table = new JTable(model);
		table.setBorder(new LineBorder(Color.BLACK, 1));
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 3, 0.05, 0.9, 1);
		mainPanel.add(table, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.44, 1);
		mainPanel.add(cmdInvert, c);
		c = new LineLayoutConstraints(4, 4, 0.51, 0.44, 1);
		mainPanel.add(cmdLoad, c);
		c = new LineLayoutConstraints(5, 5, 0.05, 0.9, 1);
		mainPanel.add(lblMessage, c);
		
	}
	
	public static Matrix4d showDialog(InterfaceFrame owner, Matrix4d matrix){
		MatrixEditorDialog dialog = new MatrixEditorDialog(owner, matrix);
		
		dialog.setVisible(true);
		
		return dialog.matrix;
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			matrix.set(model.matrix);
			if (panel != null)
				panel.updateFromDialog(this);
			setVisible(false);
			return;
			}
		
		if (e.getActionCommand().equals("Invert Matrix")){
			Matrix4d m = model.matrix;
			try{
				m.invert();
				lblMessage.setForeground(Color.green);
				lblMessage.setText("Matrix inverted.");
				lblMessage.updateUI();
			}catch (SingularMatrixException ex){
				//ex.printStackTrace();
				lblMessage.setForeground(Color.red);
				lblMessage.setText("Matrix could not be inverted.");
				lblMessage.updateUI();
				return;
				}
			model.setMatrix(m);
			return;
			}
		
		if (e.getActionCommand().equals("Load Matrix")){
			
			JFileChooser jc = new JFileChooser();
			jc.setFileFilter(new FileFilter(){
			    @Override
				public boolean accept(File f){
			    	if (f.isDirectory()) return true;
			    	String ext = f.getAbsolutePath();
			    	if (!ext.contains(".")) return true;
			    	ext = ext.substring(ext.indexOf("."));
			    	return (ext.equals(".txt") || ext.equals(".xfm"));
			    }

			    @Override
				public String getDescription(){
			    	return "4x4 matrix files (*., *.txt, *.xfm)";
			    }
			});
			
			if (jc.showOpenDialog(this.getParent()) != JFileChooser.APPROVE_OPTION)
				return;
			
			//TODO: allow generic loaders
			MatrixTransformLoader loader = new MatrixTransformLoader(jc.getSelectedFile());
			Matrix4d m = loader.loadMatrix();
			if (m != null){
				model.setMatrix(m);
				lblMessage.setForeground(Color.green);
				lblMessage.setText("Matrix loaded successfully.");
			}else{
				lblMessage.setForeground(Color.red);
				lblMessage.setText("Error loading matrix.");
				}
			lblMessage.updateUI();
			
			return;
			}
		
		super.actionPerformed(e);
	}
	
	class MatrixModel extends AbstractTableModel{

		public Matrix4d matrix;
		
		public MatrixModel(Matrix4d matrix){
			this.matrix = new Matrix4d(matrix);
		}
		
		public void setMatrix(Matrix4d m){
			matrix = m;
			this.fireTableDataChanged();
		}
		
		public int getColumnCount() {
			
			return 4;
		}

		public int getRowCount() {
			return 4;
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return true;
		}
		
		public Object getValueAt(int row, int col) {
			if (row < 0 || row > 3 || col < 0 || col > 3)
				return null;
			return String.valueOf(matrix.getElement(row, col));
		}
		
		@Override
		public void setValueAt(Object aValue, int row, int col) {
			if (row < 0 || row > 3 || col < 0 || col > 3)
				return;
			matrix.setElement(row, col, Double.valueOf((String)aValue));
		}
		
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}
		
	}
	
	
	
}