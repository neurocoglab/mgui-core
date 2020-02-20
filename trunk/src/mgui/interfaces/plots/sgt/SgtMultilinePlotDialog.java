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

package mgui.interfaces.plots.sgt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.table.AttributeCellEditor;
import mgui.interfaces.attributes.table.AttributeCellRenderer;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.plots.InterfacePlot;
import mgui.interfaces.plots.InterfacePlotDialog;
import mgui.interfaces.plots.InterfacePlotOptions;
import mgui.interfaces.plots.VariablePlotTimeSeriesDataSource;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.variables.VariableInt;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.util.Colours;

/************************************************************
 * Dialog box for specifying the source data, attributes, labels, and axes of a
 * SGT multiline plot object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class SgtMultilinePlotDialog extends InterfacePlotDialog<SgtMultilinePlot>
									implements TreeSelectionListener{

	protected SgtMultilinePlot plot;
	
	JLabel lblTitle = new JLabel("Plot title:");
	JTextField txtTitle = new JTextField("Multiline Plot");
	JButton cmdSetX = new JButton("Set X");
	JButton cmdAddY = new JButton("Add Y");
	JLabel lblXData = new JLabel("X Data:");
	JTextField txtXData = new JTextField();
	JLabel lblYData = new JLabel("Y Data:");
	JTable y_table;
	YTableModel y_model = new YTableModel();
	JScrollPane scrYData;
	JButton cmdRemoveY = new JButton("Remove");
	JButton cmdKeepY = new JButton("Keep");
	
	public SgtMultilinePlotDialog(){
		super();
	}
	
	public SgtMultilinePlotDialog(JFrame frame, InterfacePlotOptions<SgtMultilinePlot> options){
		super(frame, options);
		_init();
	}
	
	private void _init(){
		this.setButtonType(BT_OK_CANCEL);
		super.init();
		
		InterfacePlotOptions<SgtMultilinePlot> _options = (InterfacePlotOptions<SgtMultilinePlot>)options;
		plot = _options.plot;
		
		cmdSetX.setActionCommand("Set X Data");
		cmdSetX.addActionListener(this);
		cmdAddY.setActionCommand("Add Y Data");
		cmdAddY.addActionListener(this);
		cmdRemoveY.setActionCommand("Remove Y");
		cmdRemoveY.addActionListener(this);
		cmdKeepY.setActionCommand("Keep Y");
		cmdKeepY.addActionListener(this);
		
		initSourceTree();
		source_tree.getTree().getSelectionModel().addTreeSelectionListener(this);
		
		initYTable();
		
		
		this.setTitle("Create Multiline Plot");
		
		LineLayout layout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		setMainLayout(layout);
		setDialogSize(650, 750);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.35, 1);
		mainPanel.add(lblTitle, c);
		c = new LineLayoutConstraints(1, 1, 0.4, 0.45, 1);
		mainPanel.add(txtTitle, c);
		c = new LineLayoutConstraints(2, 9, 0.05, 0.9, 1);
		mainPanel.add(scrSourceTree, c);
		c = new LineLayoutConstraints(10, 10, 0.05, 0.43, 1);
		mainPanel.add(cmdSetX, c);
		c = new LineLayoutConstraints(10, 10, 0.52, 0.43, 1);
		mainPanel.add(cmdAddY, c);
		c = new LineLayoutConstraints(11, 11, 0.05, 0.35, 1);
		mainPanel.add(lblXData, c);
		c = new LineLayoutConstraints(11, 11, 0.4, 0.55, 1);
		mainPanel.add(txtXData, c);
		c = new LineLayoutConstraints(12, 12, 0.05, 0.9, 1);
		mainPanel.add(lblYData, c);
		c = new LineLayoutConstraints(13, 18, 0.05, 0.9, 1);
		mainPanel.add(scrYData, c);
		layout.setFlexibleComponent(scrYData);
		c = new LineLayoutConstraints(19, 19, 0.05, 0.43, 1);
		mainPanel.add(cmdRemoveY, c);
		c = new LineLayoutConstraints(19, 19, 0.52, 0.43, 1);
		mainPanel.add(cmdKeepY, c);
		
		updateDialog();
		
	}
	
	private void initYTable(){
		
		y_table = new JTable(y_model);
		y_table.setDefaultRenderer(Attribute.class, new AttributeCellRenderer());
		y_table.setDefaultEditor(Attribute.class, new AttributeCellEditor());
		y_table.getColumnModel().getColumn(0).setPreferredWidth(30);
		y_table.getColumnModel().getColumn(0).setMaxWidth(30);
		y_table.getColumnModel().getColumn(0).setMinWidth(30);
		y_table.getColumnModel().getColumn(3).setPreferredWidth(50);
		y_table.getColumnModel().getColumn(3).setMaxWidth(50);
		y_table.getColumnModel().getColumn(4).setPreferredWidth(50);
		y_table.getColumnModel().getColumn(4).setMaxWidth(50);
		scrYData = new JScrollPane(y_table);
		
	}
	
	public SgtMultilinePlot getPlot(){
		return plot;
	}
	
	@Override
	public boolean updateDialog(){
		if (plot == null) return false;
		
		TreePath[] paths = this.source_tree.getTree().getSelectionPaths();
		
		if (paths == null){
			cmdSetX.setEnabled(false);
			cmdAddY.setEnabled(false);
		}else{
			//at least one selection path should be a leaf
			int enable_count = 0;
			for (int i = 0; i < paths.length; i++){
				if (((InterfaceTreeNode)paths[i].getLastPathComponent()).getUserObject() instanceof VariableInt)
					enable_count++;
				}
			cmdSetX.setEnabled(enable_count == 1);
			cmdAddY.setEnabled(enable_count > 0);
			}
		
		return true;
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Set X Data")){
			
			TreePath[] paths = this.source_tree.getTree().getSelectionPaths();
			if (paths == null || paths.length != 1){
				updateDialog();
				return;
				}
			
			VariableInt<?> variable = (VariableInt<?>)((InterfaceTreeNode)paths[0].getLastPathComponent()).getUserObject();
			String left = "0", right = "*";
			int dims = variable.getDimensions().size();
			for (int i = 1; i < dims; i++){
				left = left + ",0";
				right = right + ",*";
				}
			String var_text = "{variable='" + variable.getName() + "' part='" 
											+ left + ":" + right + "'}";
			txtXData.setText(var_text);
			
			return;
			}
		
		if (e.getActionCommand().equals("Add Y Data")){
			
			TreePath[] paths = this.source_tree.getTree().getSelectionPaths();
			if (paths == null){
				updateDialog();
				return;
				}
			
			for (int i = 0; i < paths.length; i++){
				VariableInt<?> variable = (VariableInt<?>)((InterfaceTreeNode)paths[i].getLastPathComponent()).getUserObject();
				String left = "0", right = "*";
				int dims = variable.getDimensions().size();
				for (int j = 1; j < dims; j++){
					left = left + ",0";
					right = right + ",*";
					}
				String var_text = "{variable='" + variable.getName() + "' part='" 
												+ left + ":" + right + "'}";
				y_model.addVariable(var_text, variable.getName());
				}
			
			return;
			}
		
		if (e.getActionCommand().equals("Remove Y")){
			y_model.removeSelected();
			return;
			}
		
		if (e.getActionCommand().equals("Keep Y")){
			y_model.keepSelected();
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			//set plot
			if (txtXData.getText().length() == 0){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "No X data set!", 
											  "Create Multiline Plot", 
											  JOptionPane.ERROR_MESSAGE);
				
				return;
				}
			
			if (y_model.select.size() == 0){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "No Y data set!", 
											  "Create Multiline Plot", 
											  JOptionPane.ERROR_MESSAGE);
				
				return;
				}
			
			plot = new SgtMultilinePlot(txtTitle.getText());
			VariablePlotTimeSeriesDataSource<MguiDouble> plot_data = 
											new VariablePlotTimeSeriesDataSource<MguiDouble>();
			
			try{
				plot_data.setXVariable(txtXData.getText());
				for (int i = 0; i < y_model.select.size(); i++){
					plot_data.addYVariable(y_model.variables.get(i).getValue());
					SgtLine line = new SgtLine();
					line.setColour(y_model.colours.get(i).getValue());
					line.setStroke(y_model.strokes.get(i).getValue());
					line.setName(y_model.titles.get(i).getValue());
					plot.addDataSeries(line);
					}
				
				plot.setDataSource(plot_data);
				this.setVisible(false);
				
			}catch (Exception ex){
				//ex.printStackTrace();
				InterfaceSession.log("SgtMultilinePlotDialog: Error creating plot: " + ex.getMessage(), 
									 LoggingType.Errors);
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Could not set all data... check syntax", 
											  "Create Multiline Plot", 
											  JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_CANCEL)){
			plot = null;
			this.setVisible(false);
			}
	}
	
	@Override
	public InterfacePlot<?> showDialog(JFrame frame, InterfacePlotOptions<?> options) {
		
		SgtMultilinePlotDialog dialog = 
			new SgtMultilinePlotDialog(frame, (InterfacePlotOptions<SgtMultilinePlot>)options);
		
		dialog.setVisible(true);
		
		return dialog.plot;
	}


	@Override
	public void valueChanged(TreeSelectionEvent e) {
		// Tree selection event, update buttons
		updateDialog();
	}
	
	
	protected class YTableModel extends AbstractTableModel {
		
		public ArrayList<Attribute<MguiBoolean>> select = new ArrayList<Attribute<MguiBoolean>>();
		public ArrayList<Attribute<String>> variables = new ArrayList<Attribute<String>>();
		public ArrayList<Attribute<Color>> colours = new ArrayList<Attribute<Color>>();
		public ArrayList<Attribute<Stroke>> strokes = new ArrayList<Attribute<Stroke>>();
		public ArrayList<Attribute<String>> titles = new ArrayList<Attribute<String>>();
		
		public YTableModel(){
			
		}
		
		public int getSize(){
			return select.size();
		}
		
		public boolean isSelected(int i){
			return select.get(i).getValue().getTrue();
		}
		
		public void removeSelected(){
			updateSelected(true);
		}
		
		public void keepSelected(){
			updateSelected(false);
		}
		
		private void updateSelected(boolean remove){
			ArrayList<Attribute<MguiBoolean>> _select = new ArrayList<Attribute<MguiBoolean>>();
			ArrayList<Attribute<String>> _variables = new ArrayList<Attribute<String>>();
			ArrayList<Attribute<Color>> _colours = new ArrayList<Attribute<Color>>();
			ArrayList<Attribute<Stroke>> _strokes = new ArrayList<Attribute<Stroke>>();
			ArrayList<Attribute<String>> _titles = new ArrayList<Attribute<String>>();
			
			for (int i = 0; i < select.size(); i++){
				if (isSelected(i) != remove){
					_select.add(select.get(i));
					_variables.add(variables.get(i));
					_colours.add(colours.get(i));
					_strokes.add(strokes.get(i));
					_titles.add(titles.get(i));
					}
				}
			
			select = new ArrayList<Attribute<MguiBoolean>>(_select); 
			variables = new ArrayList<Attribute<String>>(_variables);
			colours = new ArrayList<Attribute<Color>>(_colours);
			strokes = new ArrayList<Attribute<Stroke>>(_strokes);
			titles = new ArrayList<Attribute<String>>(_titles);
			this.fireTableDataChanged();
		}
		
		public void addVariable(String var, String name){
			String title = getUniqueTitle(name);
			Color colour = getUniqueColour();
			Stroke stroke = new BasicStroke(1f);
			addVariable(var, title, colour, stroke);
		}

		public void addVariable(String var, String title, Color colour, Stroke stroke){
			select.add(new Attribute<MguiBoolean>("Select", new MguiBoolean(true)));
			variables.add(new Attribute<String>("Variable", var));
			titles.add(new Attribute<String>("Title", title));
			colours.add(new Attribute<Color>("Colour", colour));
			strokes.add(new Attribute<Stroke>("Stroke", stroke));
			this.fireTableDataChanged();
		}
		
		private String getUniqueTitle(String title){
			for (int i = 0; i < titles.size(); i++)
				if (titles.get(i).getValue().equals(title))
					return getUniqueTitle(title, 1);
			return title;
		}
		
		private String getUniqueTitle(String title, int n){
			String title2 = title + "." + n;
			for (int i = 0; i < titles.size(); i++)
				if (titles.get(i).getValue().equals(title2))
					return getUniqueTitle(title, n + 1);
			return title2;
		}
		
		private Color getUniqueColour(){
			boolean found = false;
			Color c = null;
			do {
				c = Colours.getRandom().getColor();
				for (int i = 0; i < colours.size(); i++)
					if (colours.get(i).getValue().equals(c)){
						found = true;
						break;
						}
			} while(found);
			return c;
		}
		
        public int getColumnCount() {
            return 5;
        }

        public int getRowCount() {
            return variables.size();
        }

        @Override
		public String getColumnName(int col) {
            switch(col){
            	case 0:
            		return "";
	            case 1:
	            	return "Variable";
	            case 2:
	            	return "Title";
	            case 3:
	            	return "Colour";
	            case 4:
	            	return "Line";
            
            	}
            return "?";
        }

        public Object getValueAt(int row, int col) {
        	return getAttribute(row, col);
        }

        @Override
		public Class<?> getColumnClass(int col) {
        	return Attribute.class;
        }

        @Override
		public boolean isCellEditable(int row, int col) {
        	return true;
        }
        
        private Attribute<?> getAttribute(int row, int col){
        	switch (col){
	        	case 0:
	        		return select.get(row);
	        	case 1:
	        		return variables.get(row);
	        	case 2:
	        		return titles.get(row);
	        	case 3:
	        		return colours.get(row);
	        	case 4:
	        		return strokes.get(row);
	        	}
        	return null;
        }

        @Override
		public void setValueAt(Object value, int row, int col) {
           
        	Attribute<?> attribute = getAttribute(row, col);
    		attribute.setValue(((Attribute<?>)value).getValue(), false);
    		
            fireTableCellUpdated(row, col);
        }

	}
	
}