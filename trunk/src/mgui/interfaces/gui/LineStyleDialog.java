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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceDialogUpdater;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.maps.NameMap;
import mgui.numbers.MguiFloat;


/***********************************
 * Dialog box to specify a line style (Stroke)
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */

public class LineStyleDialog extends InterfaceDialogBox {

	//controls
	JLabel lblDash = new JLabel("Dash"); 
	JComboBox cmbDash = new JComboBox();
	JTextField txtDashArray = new JTextField();
	JButton cmdRemDash = new JButton("Remove");
	JButton cmdAddDash = new JButton("Add");
	JLabel lblWidth = new JLabel("Width");
	JComboBox cmbWidth = new JComboBox();
	JTextField txtWidth = new JTextField();
	JButton cmdWidthUp = new JButton();
	JButton cmdWidthDown = new JButton();
	JLabel lblJoin = new JLabel("Join"); 
	JComboBox cmbJoin = new JComboBox();
	JLabel lblCap = new JLabel("Cap"); 
	JComboBox cmbCap = new JComboBox();
	JLabel lblSample = new JLabel("Sample");
	LineSample pnlSample = new LineSample(this);
	
	StrokeSample strokeRenderer = new StrokeSample(new BasicStroke(1));
	BasicStroke currentStroke;
	
	LineLayout lineLayout;
	NameMap joins = new NameMap();
	NameMap caps = new NameMap();
	
	public LineStyleDialog(JFrame aFrame, InterfaceDialogUpdater parent){
		super (aFrame, parent);
		setTitle("Line Style Dialog Box");
		init();
	}
	
	public LineStyleDialog(JFrame aFrame, String title, BasicStroke current){
		super (aFrame, null);
		setTitle(title);
		currentStroke = current;
		init();
	}
	
	@Override
	protected void init(){
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		super.init();
		
		//maps
		joins.add(BasicStroke.JOIN_BEVEL, "Bevel");
		joins.add(BasicStroke.JOIN_MITER, "Miter");
		joins.add(BasicStroke.JOIN_ROUND, "Round");
		caps.add(BasicStroke.CAP_BUTT, "Butt");
		caps.add(BasicStroke.CAP_ROUND, "Round");
		caps.add(BasicStroke.CAP_SQUARE, "Square");
		
		cmbDash.setRenderer(new StrokeSample(new BasicStroke(1)));
		cmbWidth.setRenderer(new StrokeSample(new BasicStroke(1), true));
		cmbDash.addActionListener(this);
		cmbDash.setActionCommand("Update Dash");
		cmbWidth.addActionListener(this);
		cmbWidth.setActionCommand("Update Width");
		cmbJoin.addActionListener(this);
		cmbJoin.setActionCommand("Update Join");
		cmbCap.addActionListener(this);
		cmbCap.setActionCommand("Update Cap");
		
		fillDashCombo();
		fillWidthCombo();
		fillJoinsCombo();
		fillCapsCombo();
		
		lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(400,380);
		//this.setTitle(title);
		
		LineLayoutConstraints c;
		
		c = new LineLayoutConstraints(1, 1, 0.05, 0.2, 1);
		mainPanel.add(lblDash, c);
		c = new LineLayoutConstraints(1, 1, 0.25, 0.7, 1);
		mainPanel.add(cmbDash, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.2, 1);
		mainPanel.add(lblWidth, c);
		c = new LineLayoutConstraints(2, 2, 0.25, 0.7, 1);
		mainPanel.add(cmbWidth, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.2, 1);
		mainPanel.add(lblJoin, c);
		c = new LineLayoutConstraints(3, 3, 0.25, 0.7, 1);
		mainPanel.add(cmbJoin, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.2, 1);
		mainPanel.add(lblCap, c);
		c = new LineLayoutConstraints(4, 4, 0.25, 0.7, 1);
		mainPanel.add(cmbCap, c);
		c = new LineLayoutConstraints(5, 5, 0.05, 0.2, 1);
		mainPanel.add(lblSample, c);
		c = new LineLayoutConstraints(6, 9, 0.05, 0.9, 1);
		mainPanel.add(pnlSample, c);
		
	}
	
	void fillDashCombo(){
		//various dash types
		int join = BasicStroke.JOIN_MITER;
		int cap = BasicStroke.CAP_BUTT;
		if (currentStroke != null)
			cmbDash.addItem(new BasicStroke(1.5f, cap, join, 1, currentStroke.getDashArray(), 0));
		cmbDash.addItem(new BasicStroke(1.5f));
		cmbDash.addItem(new BasicStroke(1.5f, cap, join, 1, new float[]{5f,2f},0));
		cmbDash.addItem(new BasicStroke(1.5f, cap, join, 1, new float[]{5f,5f},0));
		cmbDash.addItem(new BasicStroke(1.5f, cap, join, 1, new float[]{10f,2f},0));
		cmbDash.addItem(new BasicStroke(1.5f, cap, join, 1, new float[]{10f,5f},0));
		cmbDash.addItem(new BasicStroke(1.5f, cap, join, 1, new float[]{5f,10f},0));
		cmbDash.addItem(new BasicStroke(1.5f, cap, join, 1, new float[]{10f,10f},0));
	}
	
	void fillWidthCombo(){
		//various dash types
		if (currentStroke != null)
			cmbWidth.addItem(new BasicStroke(currentStroke.getLineWidth()));
		
		cmbWidth.addItem(new BasicStroke(1.0f));
		cmbWidth.addItem(new BasicStroke(1.5f));
		cmbWidth.addItem(new BasicStroke(2.0f));
		cmbWidth.addItem(new BasicStroke(2.5f));
		cmbWidth.addItem(new BasicStroke(3.0f));
		cmbWidth.addItem(new BasicStroke(3.5f));
		cmbWidth.addItem(new BasicStroke(4.0f));
		cmbWidth.addItem(new BasicStroke(4.5f));
		cmbWidth.addItem(new BasicStroke(5.0f));
	}
	
	void fillJoinsCombo(){
		cmbJoin.addItem("Bevel");
		cmbJoin.addItem("Miter");
		cmbJoin.addItem("Round");
		if (currentStroke != null)
			cmbJoin.setSelectedItem(joins.get(currentStroke.getLineJoin()));
	}
	
	void fillCapsCombo(){
		cmbCap.addItem("Butt");
		cmbCap.addItem("Round");
		cmbCap.addItem("Square");
		if (currentStroke != null)
			cmbCap.setSelectedItem(joins.get(currentStroke.getEndCap()));
	}
	
	public static BasicStroke showDialog(JFrame frame, String title, BasicStroke current){
		LineStyleDialog dialog = new LineStyleDialog(frame, title, current);
		dialog.setModal(true);
		dialog.setVisible(true);
		return dialog.currentStroke;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			//construct stroke
			currentStroke = getStroke(); 
			this.setVisible(false);
			return;
			}
		
		if (e.getActionCommand().startsWith("Update")){
			pnlSample.repaint();
		}
		
		super.actionPerformed(e);
	}
	
	public BasicStroke getStroke(){
		BasicStroke stroke = (BasicStroke)cmbDash.getSelectedItem();
		float[] dash = stroke.getDashArray();
		stroke = (BasicStroke)cmbWidth.getSelectedItem();
		float width = stroke.getLineWidth();
		int join = joins.get((String)cmbJoin.getSelectedItem());
		int cap = caps.get((String)cmbCap.getSelectedItem());
		return new BasicStroke(width, cap, join, 1, dash, 0);
	}
	
	class LineSample extends JPanel{
		
		LineStyleDialog dialog;
		
		public LineSample(LineStyleDialog d){
			dialog = d;
			setBackground(Color.WHITE);
			setOpaque(true);
			setBorder(BorderFactory.createLineBorder(Color.BLUE));
		}
		
		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(1));
			super.paintComponent(g);
			
	        Dimension size = getSize();
	        Insets insets = getInsets();
	        double xx = insets.left;
	        double yy = insets.top;
	        double ww = size.getWidth() - insets.left - insets.right;
	        double hh = size.getHeight() - insets.top - insets.bottom;
	        double marginX = ww * 0.1;
	        double marginY = 0.1 * hh;
	   
	        g2.setColor(Color.BLACK);
	        BasicStroke stroke = new BasicStroke(1);
	        if (dialog != null)
	        	stroke = dialog.getStroke();
	        g2.setStroke(stroke);
	        GeneralPath path = new GeneralPath();
	        path.moveTo(xx + marginX, yy + hh - marginY);
	        path.lineTo(xx + marginX, yy + marginY);
	        path.lineTo(xx + ww / 2, yy + hh - marginY);
	        path.lineTo(xx + ww - marginX, yy + hh - marginY);
	        g2.draw(path);
	        
	        g2.setStroke(new BasicStroke(1));
	        
		}
		
		
	}
	
	class StrokeSample extends JComponent implements ListCellRenderer {

	    /** The stroke being displayed. */
	    private Stroke stroke;

	    /** The preferred size of the component. */
	    private Dimension preferredSize;

	    public boolean drawWidth;
	    
	    /**
	     * Creates a StrokeSample for the specified stroke.
	     *
	     * @param stroke  the sample stroke.
	     */
	    public StrokeSample(Stroke stroke) {
	    	super();
	        this.stroke = stroke;
	        this.preferredSize = new Dimension(80, 18);
	    }

	    public StrokeSample(Stroke stroke, boolean width) {
	    	super();
	    	drawWidth = width;
	        this.stroke = stroke;
	        this.preferredSize = new Dimension(80, 18);
	    }

	    
	    /**
	     * Returns the current Stroke object being displayed.
	     *
	     * @return the stroke.
	     */
	    public Stroke getStroke() {
	        return this.stroke;
	    }

	    /**
	     * Sets the Stroke object being displayed.
	     *
	     * @param stroke  the stroke.
	     */
	    public void setStroke(Stroke stroke) {
	        this.stroke = stroke;
	        repaint();
	    }

	    /**
	     * Returns the preferred size of the component.
	     *
	     * @return the preferred size of the component.
	     */
	    @Override
		public Dimension getPreferredSize() {
	        return this.preferredSize;
	    }
	    
	    @Override
		public void setPreferredSize(Dimension d){
	    	super.setPreferredSize(d);
	    	preferredSize = d;
	    }

	    /**
	     * Draws a line using the sample stroke.
	     *
	     * @param g  the graphics device.
	     */
	    @Override
		public void paintComponent(Graphics g) {

	        Graphics2D g2 = (Graphics2D) g;
	        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        Dimension size = getSize();
	        Insets insets = getInsets();
	        double xx = insets.left;
	        double yy = insets.top;
	        double ww = size.getWidth() - insets.left - insets.right;
	        double hh = size.getHeight() - insets.top - insets.bottom;
	        //double ww = size.getWidth();
	        //double hh = size.getHeight();
	        double marginX = ww * 0.1;
	        double marginY = 0.1 * hh;
	        
	        // calculate point one
	        Point2D one =  new Point2D.Double(xx + marginX, yy + hh / 2);
	        //final Point2D one =  new Point2D.Double(margin, hh / 2);
	        // calculate point two
	        Point2D two =  new Point2D.Double(xx + ww - marginX, yy + hh / 2);
	        //final Point2D two =  new Point2D.Double(ww - margin, hh / 2);
	        // draw a circle at point one
	        //final Ellipse2D circle1 = new Ellipse2D.Double(one.getX() - 5, one.getY() - 5, 10, 10);
	        //final Ellipse2D circle2 = new Ellipse2D.Double(two.getX() - 6, two.getY() - 5, 10, 10);
	        //final Rectangle2D rect = new Rectangle2D.Double(xx, yy, xx + ww - 1, yy + hh - 1);
	        //final Rectangle2D rect = new Rectangle2D.Double(0, 0, ww - 1, hh - 1);
	        
	        // draw a circle at point two
	        //g2.draw(rect);
	        //g2.draw(circle1);
	        //g2.fill(circle1);
	        //g2.draw(circle2);
	        //g2.fill(circle2);

	        // draw a line connecting the points
	        Line2D line = new Line2D.Double(one, two);
	        if (this.stroke != null) {
	            g2.setStroke(this.stroke);
	        }else {
	            g2.setStroke(new BasicStroke(0.0f));
	        	}
	        g2.draw(line);
	        if (drawWidth){
	        	g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        			RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	        	g2.setFont(new Font("Courier", Font.PLAIN, 11));
	        	g2.setColor(this.getBackground());
	        	Rectangle2D rect = new Rectangle2D.Double(xx + (ww / 2) - marginX, yy, 
	        											  marginX * 2, hh);
	        	g2.fill(rect);
	        	g2.setColor(Color.BLACK);
	        	
	        	String s = MguiFloat.getString(((BasicStroke)this.stroke).getLineWidth(), "#0.0");
	        	//String s = "?";
	        	//g2.drawString(s, (float)(xx + (ww / 2) - (margin / 2)), (float)(yy + (hh / 2) - margin));
	        	g2.drawString(s + "pt", (int)(xx + ww / 2 - marginX * s.length() / 5), (int)(yy + hh - marginY));
	        	}

	    }

	    /**
	     * Returns a list cell renderer for the stroke, so the sample can be displayed in a list or
	     * combo.
	     *
	     * @param list  the list.
	     * @param value  the value.
	     * @param index  the index.
	     * @param isSelected  selected?
	     * @param cellHasFocus  focussed?
	     *
	     * @return the component for rendering.
	     */
	    public Component getListCellRendererComponent(JList list, Object value, int index,
	                                                  boolean isSelected, boolean cellHasFocus) {
	        if (value instanceof BasicStroke) {
	            //StrokeSample in = (StrokeSample) value;
	        	
	            //setStroke(in.getStroke());
	        	setStroke((BasicStroke)value);
	        }
	        return this;
	    }

	}
	
}