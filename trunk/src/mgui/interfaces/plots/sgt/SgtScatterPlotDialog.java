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
import mgui.interfaces.plots.PlotXYDataSource;
import mgui.interfaces.plots.VariablePlotXYDataSource;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.variables.VariableInt;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.util.Colours;

/**********************************************************************
 * Dialog to allow user to specify source data and display options for an XY scatterplot.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class SgtScatterPlotDialog extends InterfacePlotDialog<SgtScatterplot> 
								  implements TreeSelectionListener {

	protected SgtScatterplot plot;
	
	JLabel lblTitle = new JLabel("Plot title:");
	JTextField txtTitle = new JTextField("XY Scatter Plot");
	JButton cmdSetX = new JButton("Set X:");
	JButton cmdSetY = new JButton("Set Y");
	JTextField txtXData = new JTextField();
	JTextField txtYData = new JTextField();
	JButton cmdAddData = new JButton("Add XY Pair \u2193");
	JTable xy_table;
	XYTableModel xy_model = new XYTableModel();
	JScrollPane scrXYData;
	JButton cmdRemove = new JButton("Remove");
	JButton cmdKeep = new JButton("Keep");
	
	String title_x = "", title_y = "";
	
	public SgtScatterPlotDialog(){
		super();
	}
	
	public SgtScatterPlotDialog(JFrame frame, InterfacePlotOptions<SgtScatterplot> options){
		super(frame, options);
		_init();
	}
	
	private void _init(){
		this.setButtonType(BT_OK_CANCEL);
		super.init();
		
		InterfacePlotOptions<SgtScatterplot> _options = (InterfacePlotOptions<SgtScatterplot>)options;
		plot = _options.plot;
		
		cmdSetX.setActionCommand("Set X Data");
		cmdSetX.addActionListener(this);
		cmdSetY.setActionCommand("Set Y Data");
		cmdSetY.addActionListener(this);
		cmdAddData.addActionListener(this);
		cmdAddData.setActionCommand("Add XY Pair");
		cmdRemove.setActionCommand("Remove");
		cmdRemove.addActionListener(this);
		cmdKeep.setActionCommand("Keep");
		cmdKeep.addActionListener(this);
		
		initSourceTree();
		source_tree.getTree().getSelectionModel().addTreeSelectionListener(this);
		
		initXYTable();
		
		this.setTitle("Create XY Scatter Plot");
		
		LineLayout layout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		setMainLayout(layout);
		setDialogSize(650, 750);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.35, 1);
		mainPanel.add(lblTitle, c);
		c = new LineLayoutConstraints(1, 1, 0.4, 0.45, 1);
		mainPanel.add(txtTitle, c);
		c = new LineLayoutConstraints(2, 9, 0.05, 0.9, 1);
		mainPanel.add(scrSourceTree, c);
		c = new LineLayoutConstraints(10, 10, 0.05, 0.35, 1);
		mainPanel.add(cmdSetX, c);
		c = new LineLayoutConstraints(10, 10, 0.4, 0.45, 1);
		mainPanel.add(txtXData, c);
		c = new LineLayoutConstraints(11, 11, 0.05, 0.35, 1);
		mainPanel.add(cmdSetY, c);
		c = new LineLayoutConstraints(11, 11, 0.4, 0.45, 1);
		mainPanel.add(txtYData, c);
		c = new LineLayoutConstraints(12, 12, 0.33, 0.33, 1);
		mainPanel.add(cmdAddData, c);
		c = new LineLayoutConstraints(13, 18, 0.05, 0.9, 1);
		mainPanel.add(scrXYData, c);
		layout.setFlexibleComponent(scrXYData);
		c = new LineLayoutConstraints(19, 19, 0.05, 0.43, 1);
		mainPanel.add(cmdRemove, c);
		c = new LineLayoutConstraints(19, 19, 0.52, 0.43, 1);
		mainPanel.add(cmdKeep, c);
		
		updateDialog();
		
	}
	
	
	private void initXYTable(){
		
		xy_table = new JTable(xy_model);
		xy_table.setDefaultRenderer(Attribute.class, new AttributeCellRenderer());
		xy_table.setDefaultEditor(Attribute.class, new AttributeCellEditor());
		xy_table.getColumnModel().getColumn(0).setPreferredWidth(30);
		xy_table.getColumnModel().getColumn(0).setMaxWidth(30);
		xy_table.getColumnModel().getColumn(0).setMinWidth(30);
		xy_table.getColumnModel().getColumn(4).setPreferredWidth(50);
		xy_table.getColumnModel().getColumn(4).setMaxWidth(50);
		xy_table.getColumnModel().getColumn(5).setPreferredWidth(50);
		xy_table.getColumnModel().getColumn(5).setMaxWidth(50);
		scrXYData = new JScrollPane(xy_table);
		
	}
	
	public SgtScatterplot getPlot(){
		return plot;
	}
	
	@Override
	public boolean updateDialog(){
		if (plot == null) return false;
		
		TreePath[] paths = this.source_tree.getTree().getSelectionPaths();
		
		if (paths == null){
			cmdSetX.setEnabled(false);
			cmdSetY.setEnabled(false);
		}else{
			//at least one selection path should be a leaf
			int enable_count = 0;
			for (int i = 0; i < paths.length; i++){
				if (((InterfaceTreeNode)paths[i].getLastPathComponent()).getUserObject() instanceof VariableInt)
					enable_count++;
				}
			cmdSetX.setEnabled(enable_count == 1);
			cmdSetY.setEnabled(enable_count == 1);
			}
		
		if (txtXData.getText().length() == 0 || txtYData.getText().length() == 0)
			cmdAddData.setEnabled(false);
		else
			cmdAddData.setEnabled(true);
		
		return true;
	}
	
	
	@Override
	public InterfacePlot<?> showDialog(JFrame frame, InterfacePlotOptions<?> options) {
		
		SgtScatterPlotDialog dialog = 
			new SgtScatterPlotDialog(frame, (InterfacePlotOptions<SgtScatterplot>)options);
		
		dialog.setVisible(true);
		
		return dialog.plot;
	}
	
	
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		// Tree selection event, update buttons
		updateDialog();
	}

	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Set X Data") || 
			e.getActionCommand().equals("Set Y Data")){
			
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
			if (e.getActionCommand().contains("X")){
				txtXData.setText(var_text);
				title_x = variable.getName();
			}else{
				txtYData.setText(var_text);
				title_y = variable.getName();
				}
			
			updateDialog();
			return;
			}
		
		if (e.getActionCommand().equals("Add XY Pair")){
			
			if (txtXData.getText().length() == 0 || txtYData.getText().length() == 0) return;
			
			String var_x = txtXData.getText();
			String var_y = txtYData.getText();
			
			String title = title_x + " X " + title_y;
			xy_model.addXYPair(var_x, var_y, title);
			
			return;
			}
		
		if (e.getActionCommand().equals("Remove")){
			xy_model.removeSelected();
			return;
			}
		
		if (e.getActionCommand().equals("Keep")){
			xy_model.keepSelected();
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			//set plot
						
			if (xy_model.select.size() == 0){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "No XY data set!", 
											  "Create XY Scatter Plot", 
											  JOptionPane.ERROR_MESSAGE);
				
				return;
				}
			
			plot = new SgtScatterplot(txtTitle.getText());
			VariablePlotXYDataSource<MguiDouble> plot_data = new VariablePlotXYDataSource<MguiDouble>();
			
			try{
				//plot_data.setXVariable(txtXData.getText());
				for (int i = 0; i < xy_model.select.size(); i++){
					plot_data.addXYPair(xy_model.variables_x.get(i).getValue(),
										xy_model.variables_y.get(i).getValue(),
										xy_model.titles.get(i).getValue());
					SgtPointSet points = new SgtPointSet();
					points.setColour(xy_model.colours.get(i).getValue());
					//Set shape
					//line.setStroke(y_model.strokes.get(i).getValue());
					points.setName(xy_model.titles.get(i).getValue());
					plot.addDataSeries(points);
					}
				
				plot.setDataSource(plot_data);
				plot.setPlotLayout();
				
				this.setVisible(false);
				
			}catch (Exception ex){
				ex.printStackTrace();
				InterfaceSession.log("SgtScatterPlotDialog: Error creating plot: " + ex.getMessage(), 
									 LoggingType.Errors);
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Could not set all data... check syntax", 
											  "Create XY Scatter Plot", 
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
	
	
	protected class XYTableModel extends AbstractTableModel {
		
		public ArrayList<Attribute<MguiBoolean>> select = new ArrayList<Attribute<MguiBoolean>>();
		public ArrayList<Attribute<String>> variables_x = new ArrayList<Attribute<String>>();
		public ArrayList<Attribute<String>> variables_y = new ArrayList<Attribute<String>>();
		public ArrayList<Attribute<Color>> colours = new ArrayList<Attribute<Color>>();
		public ArrayList<Attribute<Stroke>> strokes = new ArrayList<Attribute<Stroke>>();
		public ArrayList<Attribute<String>> titles = new ArrayList<Attribute<String>>();
		
		public XYTableModel(){
			
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
			ArrayList<Attribute<String>> _variables_x = new ArrayList<Attribute<String>>();
			ArrayList<Attribute<String>> _variables_y = new ArrayList<Attribute<String>>();
			ArrayList<Attribute<Color>> _colours = new ArrayList<Attribute<Color>>();
			ArrayList<Attribute<Stroke>> _strokes = new ArrayList<Attribute<Stroke>>();
			ArrayList<Attribute<String>> _titles = new ArrayList<Attribute<String>>();
			
			for (int i = 0; i < select.size(); i++){
				if (isSelected(i) != remove){
					_select.add(select.get(i));
					_variables_x.add(variables_x.get(i));
					_variables_y.add(variables_y.get(i));
					_colours.add(colours.get(i));
					_strokes.add(strokes.get(i));
					_titles.add(titles.get(i));
					}
				}
			
			select = new ArrayList<Attribute<MguiBoolean>>(_select); 
			variables_x = new ArrayList<Attribute<String>>(_variables_x);
			variables_y = new ArrayList<Attribute<String>>(_variables_y);
			colours = new ArrayList<Attribute<Color>>(_colours);
			strokes = new ArrayList<Attribute<Stroke>>(_strokes);
			titles = new ArrayList<Attribute<String>>(_titles);
			this.fireTableDataChanged();
		}
		
		public void addXYPair(String var_x, String var_y, String name){
			String title = getUniqueTitle(name);
			Color colour = getUniqueColour();
			Stroke stroke = new BasicStroke(1f);
			addXYPair(var_x, var_y, title, colour, stroke);
		}

		public void addXYPair(String var_x, String var_y, String title, Color colour, Stroke stroke){
			select.add(new Attribute<MguiBoolean>("Select", new MguiBoolean(true)));
			variables_x.add(new Attribute<String>("Var X", var_x));
			variables_y.add(new Attribute<String>("Var Y", var_y));
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
            return 6;
        }

        public int getRowCount() {
            return variables_x.size();
        }

        @Override
		public String getColumnName(int col) {
            switch(col){
            	case 0:
            		return "";
	            case 1:
	            	return "Var X";
	            case 2:
	            	return "Var Y";
	            case 3:
	            	return "Title";
	            case 4:
	            	return "Colour";
	            case 5:
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
	        		return variables_x.get(row);
	        	case 2:
	        		return variables_y.get(row);
	        	case 3:
	        		return titles.get(row);
	        	case 4:
	        		return colours.get(row);
	        	case 5:
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