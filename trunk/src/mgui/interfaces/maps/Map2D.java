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

package mgui.interfaces.maps;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.Writer;

import javax.vecmath.Point2f;

import mgui.geometry.Rect2D;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/****
 * 
 * Maps from a given coordinate system to the screen
 * @author Andrew Reid
 * @version 1.0
 * @date 08.30.2006
 *
 */

public class Map2D extends Map {
	
	protected double zoomWidth;
	public Dimension bounds;
	public Point2f origin;
	public double min_zoom = 0.001;
	public double max_zoom = 10E10;
	
	public Map2D(){
	}
	
	public Map2D(Dimension theBounds, double theZoom, Point2f theOrigin){
		bounds = theBounds;
		zoomWidth = theZoom;
		origin = theOrigin;
	}
	
	public Dimension getBounds(){
		return bounds;
	}
	
	public Rect2D getMapBounds(){
		Rect2D rect = new Rect2D();
		rect.corner1 = new Point2f(origin);
		rect.corner2 = getMapPoint(new Point(bounds.width, 0));
		return rect;
	}
	
	/****************************************
	 * Returns a transform corresponding to this map.
	 * 
	 * @return
	 */
	public AffineTransform getTransform(){
		// Translation from origin
		double scale = (double)bounds.width / zoomWidth;
		AffineTransform tfm = new AffineTransform();
		Point p = getScreenPoint(new Point2f(0,0));
		tfm.setToTranslation(p.x, p.y);
		tfm.scale(scale, scale);
		return tfm;
	}
	
	public void setScreenBounds(Dimension d){
		bounds = d;
		fireMapListeners();
	}
	
	public void setScreenBounds(int width, int height){
		bounds.width = width;
		bounds.height = height;
		fireMapListeners();
	}
	
	public void setOrigin(float x, float y){
		origin = new Point2f(x, y);
		fireMapListeners();
	}
	
	/****************************************************
	 * Maps {@code p} from model space to screen space.
	 * 
	 * @param thisPt
	 * @return
	 */
	public Point getScreenPoint(Point2f p){
		double zoomFactor = (bounds.width)/zoomWidth;
		float xPt = p.x - origin.x;
		float yPt = p.y - origin.y;
		int newX = (int)Math.round(xPt * zoomFactor);
		int newY = (int)Math.round(yPt * zoomFactor);
		
		return new Point(newX, bounds.height - newY);
	}
	
	public int getScreenDist(double thisDist){
		double zoomFactor = bounds.width / zoomWidth;
		return (int)Math.round(thisDist * zoomFactor);
	}
	
	public float getMapDist(int thisDist){
		double zoomFactor = zoomWidth / bounds.width;
		return (float)(thisDist * zoomFactor);
	}
	
	public float getMapDist(double thisDist){
		double zoomFactor = zoomWidth / bounds.width;
		return (float)(thisDist * zoomFactor);
	}
	
	/*************************************
	 * Get the map coordinate of the screen point
	 * 
	 * @param thisPt
	 * @return
	 */
	public Point2f getMapPoint(Point thisPt){
		double zoomFactor = (bounds.width) / zoomWidth;
		float xPt = (float)((thisPt.x / zoomFactor) + origin.x);
		float yPt = (float)(((bounds.height - thisPt.y) / zoomFactor) + origin.y);
		
		return new Point2f(xPt, yPt);
	}
	
	public void centerOnPoint(Point2f centerPt){
		origin.x = centerPt.x - getMapDist(bounds.width / 2.0);
		origin.y = centerPt.y - getMapDist(bounds.height / 2.0);
		fireMapListeners();
	}
	
	public Point2f getMapCenterPt(){
		return new Point2f(origin.x + getMapDist(bounds.width / 2.0),
						   origin.y + getMapDist(bounds.height / 2.0));
	}
	
	public int getScreenWidth(){
		return bounds.width;
	}
	
	public Rectangle getScreenBounds(Rect2D thisBounds){
		Rectangle retRect = new Rectangle();
		Point newPt = getScreenPoint(new Point2f((Math.min(thisBounds.corner1.x, thisBounds.corner2.x)),
									             (Math.max(thisBounds.corner1.y, thisBounds.corner2.y))));
		retRect.x = newPt.x;
		//retRect.y = bounds.height - newPt.y;
		retRect.y = newPt.y;
		retRect.width = getScreenDist(thisBounds.getWidth());
		retRect.height = getScreenDist(thisBounds.getHeight());
		//retRect.y -= retRect.height;
		return retRect;
	}

	public void zoomToMapWindow(Rect2D window){
		if (window == null) return;
		if (window.getWidth() == 0 || window.getHeight() == 0) return;
		if (window.getWidth() == Double.NaN || window.getHeight() == Double.NaN) return;
		
		if (bounds.width == 0 || bounds.height == 0) return;
		
		Rectangle S = getScreenBounds(window);
		float s_x = (float)S.width/(float)bounds.width;
		float s_y = (float)S.height/(float)bounds.height;
			
		float scale = Math.max(s_x, s_y);
		zoomWidth *= scale;
		
		if (zoomWidth < min_zoom) zoomWidth = min_zoom;
		if (zoomWidth > max_zoom) zoomWidth = max_zoom;
		this.centerOnPoint(window.getCenterPt());
		fireMapListeners();
	}
	
	@Override
	public int getType(){
		return Map.MAP_2D;
	}
	
	@Override
	public double getZoom(){
		return zoomWidth;
	}
	
	@Override
	public void setZoom(double z){
		if (z < min_zoom) z = min_zoom;
		zoomWidth = z;
		fireMapListeners();
	}
	
	/*********************
	 * Sets the zoom; if {@code center = true}, maintains the window center point
	 * 
	 * @param z
	 * @param center
	 */
	public void setZoom(double z, boolean center){
		if (z < min_zoom) z = min_zoom;
		Point2f c_pt = this.getMapCenterPt();
		zoomWidth = z;
		fireMapListeners();
		if (center) this.centerOnPoint(c_pt);
	}
	
	@Override
	public String getDTD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXMLSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type)
			throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLElementEnd(String localName) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLString(String s) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
	
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		writer.write(_tab + "<Map2D\n" +
				_tab2 + "zoom_width='" + zoomWidth + "'\n" +
				_tab2 + "bounds_width='" + bounds.width + "'\n" +
				_tab2 + "bounds_height='" + bounds.height + "'\n" +
				_tab2 + "origin_x='" + origin.x + "'\n" +
				_tab2 + "origin_y='" + origin.y + "'\n" +
				_tab2 + "min_zoom='" + min_zoom + "'\n" +
				_tab + "/>\n");
		
	}

	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar)throws IOException {
		writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}

	@Override
	public void writeXML(int tab, Writer writer) throws IOException {
		writeXML(tab, writer, new XMLOutputOptions(), null);
	}

	@Override
	public String getShortXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}
	
}