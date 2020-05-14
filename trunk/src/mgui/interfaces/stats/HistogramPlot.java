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

package mgui.interfaces.stats;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mgui.image.util.WindowedColourModel;
import mgui.numbers.MguiDouble;
import mgui.numbers.NumberFunctions;
import mgui.stats.Histogram;
import mgui.util.JMultiLineToolTip;

/***************************************************************
 * Histogram visualization panel
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class HistogramPlot extends JPanel implements MouseMotionListener,
													 MouseListener{

	public Histogram histogram;
	public WindowedColourModel colour_model;
	float plot_x_start = 0.1f, plot_x_end = 0.05f;
	float plot_y_start = 0.05f, plot_y_end = 0.05f;
	public float y_scale = 1f;
	protected boolean is_adjustable = true;
	
	private Rectangle2D handle_low, handle_high;
	private Ellipse2D handle_mid;
	private boolean is_mouseover_handle_low = false, is_mouseover_handle_high = false, is_mouseover_handle_mid = false;
	private float handle_size = 6;
	private boolean is_busy;
	
	protected ArrayList<ChangeListener> change_listeners = new ArrayList<ChangeListener>();
	
	public HistogramPlot(){
		init();
	}
	
	public HistogramPlot(Histogram h){
		this(h, true);
	}
	
	public HistogramPlot(Histogram h, boolean is_adjustable){
		histogram = h;
		this.is_adjustable = is_adjustable;
		init();
	}
	
	protected void init(){
		this.setOpaque(true);
		this.setBackground(Color.white);
		addMouseMotionListener(this);
		addMouseListener(this);
		
	}
	
	/**************
	 * Add a {@linkplain ChangeListener} to this plot panel
	 * 
	 * @param l
	 */
	public void addChangeListener(ChangeListener l){
		change_listeners.add(l);
	}
	
	/***************
	 * Remove a {@linkplain ChangeListener} from this plot panel
	 *  
	 * @param l
	 */
	public void removeChangeListener(ChangeListener l){
		change_listeners.remove(l);
	}
	
	@Override
	public JToolTip createToolTip(){
		return new JMultiLineToolTip();
	}

	
	public void setHistogram(Histogram h){
		histogram = h;
		if (h != null) h.setLimits();
		repaint();
	}
	
	public void setYScale(float scale){
		y_scale = scale;
		repaint();
	}
	
	public boolean isBusy(){
		return is_busy;
	}
	
	public void setBusy(boolean b){
		is_busy = b;
		repaint();
	}
	
	@Override
	public void paintComponent(Graphics g){
		
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(Color.BLUE);
    	g2.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
    	
    	if (is_busy){
    		TextLayout layout = new TextLayout("Computing..", 
    										   new Font("Courier New", Font.PLAIN, 10), 
    										   g2.getFontRenderContext());
    		
    		Rectangle2D bounds = layout.getBounds();
    		int start_x = (int)((this.getWidth() - bounds.getWidth()) / 2.0);
    		int start_y = (int)((this.getHeight() - bounds.getHeight()) / 2.0);
    		
    		g2.setColor(Color.BLUE);
    		layout.draw(g2, start_x, start_y);
    		
    		return;
    		}
    	
		if (histogram == null) return;
		
		float x_start = this.getWidth() * plot_x_start;
		float x_end = this.getWidth() - this.getWidth() * plot_x_end;
		float width = x_end - x_start;
		
		float y_start = this.getHeight() - (int)(this.getHeight() * plot_y_start);
		float y_end = this.getHeight() * plot_y_end;
		float height = y_start - y_end;
		
		float bin_width = width / histogram.bins.size();
		float y_unit = (height / (float)histogram.maxY) / y_scale; 
		
		g2.setStroke(new BasicStroke());
		GeneralPath p = new GeneralPath();
		GeneralPath window_line = new GeneralPath();
		
		p.moveTo(x_start, y_start);
		
		for (int i = 0; i < histogram.bins.size(); i++){
			boolean y_maxed = false;
			Histogram.Bin bin = histogram.bins.get(i);
			float x = x_start + (i * bin_width);
			float _y = (float)(bin.y * y_unit);
			if (_y > height){
				_y = height;
				y_maxed = true;
				}
			float y = y_start - _y;
			p.lineTo(x, y);
			//only outline top if it is not maxed
			if (y_maxed)
				p.moveTo(x + bin_width, y);
			else
				p.lineTo(x + bin_width, y);
			if (colour_model != null){
				double[] x_pixel = new double[]{bin.x};
				Color c = new Color(colour_model.getRed(x_pixel), 
									colour_model.getGreen(x_pixel),
									colour_model.getBlue(x_pixel),
									colour_model.getAlpha(x_pixel));
				
				g2.setColor(c);
				Rectangle2D rect = new Rectangle2D.Float(x, y, bin_width, _y + 1);
				g2.fill(rect);
				
				//window line
				double windowed = colour_model.getMappedValue(bin.x);
				if (!colour_model.is_discrete){
					if (i == 0){
						window_line.moveTo(x + (bin_width / 2f), (float)(y_start - (windowed / colour_model.data_size * height)));
					}else{
						window_line.lineTo(x + (bin_width / 2f), y_start - (windowed / colour_model.data_size * height));
						}
					}
			}else{
				g2.setColor(Color.BLUE);
				Rectangle2D rect = new Rectangle2D.Float(x, y, bin_width, _y + 1);
				g2.fill(rect);
				}
			}
		
		//histogram outline
		g2.setColor(Color.BLACK);
		g2.draw(p);
		
		//window line
		if (colour_model != null && !colour_model.is_discrete && !colour_model.is_solid){
			g2.setColor(Color.RED);
			g2.draw(window_line);
			
			// Handles for adjustment
			if (is_adjustable){
				
				// Compute window points
				double[] limits = this.colour_model.getLimits();
				double mid = (limits[0] + limits[1]) / 2.0;
				double hist_width = histogram.maxX - histogram.minX;
				double scale = width / hist_width;
				
				if (limits[0] < histogram.minX){
					limits[0] = histogram.minX;
				}else if (limits[0] > histogram.maxX){
					limits[0] = histogram.maxX;
					}
				
				
				double windowed = colour_model.getMappedValue(limits[0]);
				Point2D window_pt1 = new Point2D.Float((float)(x_start + (limits[0] - histogram.minX) * scale) - handle_size/2f,
						   							   (float)(y_start - (windowed / colour_model.data_size * height) - handle_size/2f));
				
				
				handle_low = new Rectangle2D.Float((float)window_pt1.getX(),
												   (float)window_pt1.getY(),
												   handle_size,
												   handle_size);
				if (is_mouseover_handle_low)
					g2.fill(handle_low);
				else
					g2.draw(handle_low);
				
				if (limits[1] > histogram.maxX){
					limits[1] = histogram.maxX;
				}else if (limits[1] < histogram.minX){
					limits[1] = histogram.minX;
					}
				
				windowed = colour_model.getMappedValue(limits[1]);
				Point2D window_pt2 = new Point2D.Float((float)(x_start + (limits[1] - histogram.minX) * scale) - handle_size/2f,
												   	   (float)(y_start - (windowed / colour_model.data_size * height)) - handle_size/2f);
				handle_high = new Rectangle2D.Float((float)window_pt2.getX(),
						   							(float)window_pt2.getY(),
												   handle_size,
												   handle_size);
				if (is_mouseover_handle_high)
					g2.fill(handle_high);
				else
					g2.draw(handle_high);
				
				if (mid > histogram.maxX){
					mid = histogram.maxX;
				}else if (mid < histogram.minX){
					mid = histogram.minX;
					}
				
				float handle_size2 = handle_size * 1.5f;
				windowed = colour_model.getMappedValue(mid);
				Point2D window_pt3 = new Point2D.Float((float)(x_start + (mid - histogram.minX) * scale) - handle_size2 / 2f,
					   	   							   (float)(y_start - (windowed / colour_model.data_size * height)) - handle_size2 / 2f);
				
				handle_mid = new Ellipse2D.Float((float)window_pt3.getX(),
												 (float)window_pt3.getY(),
												  handle_size2,
												  handle_size2);
				if (is_mouseover_handle_mid)
					g2.fill(handle_mid);
				else
					g2.draw(handle_mid);
				
			}else{
				handle_low = null;
				handle_high = null;
				handle_mid = null;
				}
			
			}
		
		//values
		
		
		//colour bar
		
		
	}
	
	protected void fireChangeListeners(ChangeEvent e){
		for (int i = 0; i < change_listeners.size(); i++)
			change_listeners.get(i).stateChanged(e);
	}
	
	private boolean adjusting_low = false, adjusting_high = false, adjusting_mid = false;
	
	@Override
	public void mouseDragged(MouseEvent e) {
		
		if (adjusting_low || adjusting_high || adjusting_mid){
			Point p = e.getPoint();
			boolean ctrl_down = e.isControlDown();
			float x = getXAtPoint(p);
			
			double[] old_limits = this.colour_model.getLimits();
			
			if (adjusting_low){
				if (ctrl_down){
					double diff = x-old_limits[0];
					this.colour_model.setLimits(x, old_limits[1]-diff);
				}else{
					this.colour_model.setLimits(x, old_limits[1]);
					}
			}else if (adjusting_high){
				if (ctrl_down){
					double diff = x-old_limits[1];
					this.colour_model.setLimits(old_limits[0]-diff, x);
				}else{
					this.colour_model.setLimits(old_limits[0], x);
					}
			}else{
				double mid = (old_limits[1] + old_limits[0]) / 2f;
				double delta_x = x - mid;
				this.colour_model.setLimits(old_limits[0] + delta_x, old_limits[1] + delta_x);
				}
			
			fireChangeListeners(new ChangeEvent(this));
			this.revalidate();
			this.repaint();
			return;
			}
		
		if (is_adjustable){
			Point p = e.getPoint();
			
			if (is_mouseover_handle_low){
				adjusting_low = true;
				return;
				}
			
			if (is_mouseover_handle_high){
				adjusting_high = true;
				return;
				}
			
			if (is_mouseover_handle_mid){
				adjusting_mid = true;
				return;
				}
			
			}
		
		
	}
	
	private float getXAtPoint(Point p){
		float x_start = this.getWidth() * plot_x_start;
		float x_end = this.getWidth() - this.getWidth() * plot_x_end;
		float width = x_end - x_start;
		
		float delta_x = p.x - x_start;
		
		double hist_width = histogram.maxX - histogram.minX;
		double scale = hist_width / width;
		
		return (float)(histogram.minX + delta_x * scale);
		
	}
	

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseReleased(MouseEvent e){
		
		adjusting_low = false;
		adjusting_high = false;
		adjusting_mid = false;
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		
		if (histogram == null) return;
		
		if (is_adjustable && handle_low != null){
			
			Point2D pt = e.getPoint();
			if (handle_low.contains(pt)){
				this.setToolTipText("Drag to adjust lower limit\nCtrl-Drag to adjust width");
				this.setCursor(new Cursor(Cursor.HAND_CURSOR));
				is_mouseover_handle_low = true;
				this.repaint(handle_low.getBounds());
				return;
				}
			
			else if (handle_high.contains(pt)){
				this.setToolTipText("Drag to adjust higher limit\nCtrl-Drag to adjust width");
				this.setCursor(new Cursor(Cursor.HAND_CURSOR));
				is_mouseover_handle_high = true;
				this.repaint(handle_high.getBounds());
				return;
				}
			
			else if (handle_mid.contains(pt)){
				this.setToolTipText("Drag to adjust window mid-point");
				this.setCursor(new Cursor(Cursor.HAND_CURSOR));
				is_mouseover_handle_mid = true;
				this.repaint(handle_mid.getBounds());
				return;
				}
			
			else {
				this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
				}
			}
		
		is_mouseover_handle_low = false;
		is_mouseover_handle_high = false;
		is_mouseover_handle_mid = false;
		
		if (handle_low != null)
			repaint(handle_low.getBounds());
		if (handle_high != null)
			repaint(handle_high.getBounds());
		
		int i = getBinAtPoint(e.getPoint());
		
		Histogram.Bin bin = histogram.bins.get(i);
		String x_fmt = NumberFunctions.getReasonableString(bin.x);
		
		this.setToolTipText("Bin " + (i + 1) + ": " + x_fmt + "\nFreq: " + (int)bin.y + "\nNorm: " +
											  	MguiDouble.getString(histogram.getNormalized(bin.x), "##0.0000"));
		
		
	}

	protected int getBinAtPoint(Point p){
		
		float x_start = this.getWidth() * plot_x_start;
		float x_end = this.getWidth() - this.getWidth() * plot_x_end;
		float width = x_end - x_start;
		
		float bin_width = width / histogram.bins.size();
		
		int i = (int)((p.x - x_start) / bin_width);
		if (i < 0) i = 0;
		if (i >= histogram.bins.size()) i = histogram.bins.size()- 1;
		
		return i;
	}
	
}