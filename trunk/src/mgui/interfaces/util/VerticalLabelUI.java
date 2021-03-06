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

package mgui.interfaces.util;

import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

/************************************************
 * Extension of {@linkplain BasicLabelUI} to allow vertical rendering of text
 * 
 * See <a href="http://stackoverflow.com/questions/92781/how-do-i-present-text-vertically-in-a-jlabel-java-1-6/92962#92962">
 * http://stackoverflow.com/questions/92781/how-do-i-present-text-vertically-in-a-jlabel-java-1-6/92962#92962</a>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class VerticalLabelUI extends BasicLabelUI {
  static {
    labelUI = new VerticalLabelUI(false);
  }

  protected boolean clockwise;

  public VerticalLabelUI( boolean clockwise ){
    super();
    this.clockwise = clockwise;
  }


  public Dimension getPreferredSize(JComponent c){
    Dimension dim = super.getPreferredSize(c);
    return new Dimension( dim.height, dim.width );
  }

  private static Rectangle paintIconR = new Rectangle();
  private static Rectangle paintTextR = new Rectangle();
  private static Rectangle paintViewR = new Rectangle();
  private static Insets paintViewInsets = new Insets(0, 0, 0, 0);

  public void paint(Graphics g, JComponent c){
    JLabel label = (JLabel)c;
    String text = label.getText();
    Icon icon = (label.isEnabled()) ? label.getIcon() : label.getDisabledIcon();

    if ((icon == null) && (text == null)) {
      return;
    }

    FontMetrics fm = g.getFontMetrics();
    paintViewInsets = c.getInsets(paintViewInsets);

    paintViewR.x = paintViewInsets.left;
    paintViewR.y = paintViewInsets.top;

    // Use inverted height & width
    paintViewR.height = c.getWidth() - (paintViewInsets.left + paintViewInsets.right);
    paintViewR.width = c.getHeight() - (paintViewInsets.top + paintViewInsets.bottom);

    paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
    paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;

    String clippedText =
            layoutCL(label, fm, text, icon, paintViewR, paintIconR, paintTextR);

    Graphics2D g2 = (Graphics2D) g;
    AffineTransform tr = g2.getTransform();
    if( clockwise )
    {
      g2.rotate( Math.PI / 2 );
      g2.translate( 0, - c.getWidth() );
    }
    else
    {
      g2.rotate( - Math.PI / 2 );
      g2.translate( - c.getHeight(), 0 );
    }

    if (icon != null) {
      icon.paintIcon(c, g, paintIconR.x, paintIconR.y);
    }

    if (text != null) {
      int textX = paintTextR.x;
      int textY = paintTextR.y + fm.getAscent();

      if (label.isEnabled()) {
        paintEnabledText(label, g, clippedText, textX, textY);
      }
      else {
        paintDisabledText(label, g, clippedText, textX, textY);
      }
    }

    g2.setTransform( tr );
  }
}
