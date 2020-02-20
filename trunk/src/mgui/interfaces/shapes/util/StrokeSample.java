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

package mgui.interfaces.shapes.util;
/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2005, by Object Refinery Limited and Contributors.
 * 
 * Project Info:  http://www.jfree.org/jcommon/index.html
 *
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, 
 * USA.  
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 * 
 * -----------------
 * StrokeSample.java
 * -----------------
 * (C) Copyright 2000-2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: StrokeSample.java,v 1.3 2005/10/18 13:18:34 mungady Exp $
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.*;
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 21-Mar-2003 : Fixed null pointer exception, bug 705126 (DG);
 *
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * A panel that displays a stroke sample.
 *
 * @author David Gilbert
 */
public class StrokeSample extends JButton implements ListCellRenderer {

    /** The stroke being displayed. */
    private Stroke stroke;

    /** The preferred size of the component. */
    private Dimension preferredSize;

    /**
     * Creates a StrokeSample for the specified stroke.
     *
     * @param stroke  the sample stroke.
     */
    public StrokeSample(final Stroke stroke) {
        this.stroke = stroke;
        //this.preferredSize = new Dimension(80, 18);
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
    public void setStroke(final Stroke stroke) {
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
    	preferredSize = d;
    }

    /**
     * Draws a line using the sample stroke.
     *
     * @param g  the graphics device.
     */
    @Override
	public void paintComponent(final Graphics g) {

        final Graphics2D g2 = (Graphics2D) g;
        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        final Dimension size = getSize();
        final Insets insets = getInsets();
        final double xx = insets.left;
        final double yy = insets.top;
        //final double ww = size.getWidth() - insets.left - insets.right;
        //final double hh = size.getHeight() - insets.top - insets.bottom;
        double ww = size.getWidth();
        double hh = size.getHeight();
        double margin = ww * 0.1;
        
        // calculate point one
        //final Point2D one =  new Point2D.Double(xx + margin, yy + hh / 2);
        final Point2D one =  new Point2D.Double(margin, hh / 2);
        // calculate point two
        //final Point2D two =  new Point2D.Double(xx + ww - margin, yy + hh / 2);
        final Point2D two =  new Point2D.Double(ww - margin, hh / 2);
        // draw a circle at point one
        //final Ellipse2D circle1 = new Ellipse2D.Double(one.getX() - 5, one.getY() - 5, 10, 10);
        //final Ellipse2D circle2 = new Ellipse2D.Double(two.getX() - 6, two.getY() - 5, 10, 10);
        //final Rectangle2D rect = new Rectangle2D.Double(xx, yy, xx + ww - 1, yy + hh - 1);
        final Rectangle2D rect = new Rectangle2D.Double(0, 0, ww - 1, hh - 1);
        
        // draw a circle at point two
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.BLACK);
        g2.draw(rect);
        //g2.draw(circle1);
        //g2.fill(circle1);
        //g2.draw(circle2);
        //g2.fill(circle2);

        // draw a line connecting the points
        final Line2D line = new Line2D.Double(one, two);
        if (this.stroke != null) {
            g2.setStroke(this.stroke);
        }
        else {
            g2.setStroke(new BasicStroke(0.0f));
        }
        g2.draw(line);
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.BLACK);

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
    public Component getListCellRendererComponent(final JList list, final Object value, final int index,
                                                  final boolean isSelected, final boolean cellHasFocus) {
        if (value instanceof StrokeSample) {
            final StrokeSample in = (StrokeSample) value;
            setStroke(in.getStroke());
        }
        return this;
    }

}