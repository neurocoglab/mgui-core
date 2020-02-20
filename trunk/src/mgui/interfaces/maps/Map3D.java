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

import java.io.IOException;
import java.io.Writer;

import javax.media.j3d.BadTransformException;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector2d;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import mgui.geometry.Sphere3D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLObject.XMLType;
import mgui.io.standard.xml.XMLOutputOptions;

/*******************
 * Class to provide an interface for mapping a 3D viewing platform. Basic implementation generates 
 * Transform3D nodes for a Java3D scene graph, based upon a center-of-rotation (<centerOfRotation>),
 * zoom level, viewing angle (<viewingVector>), and pan location (<translationVector>)
 * The viewing platform is rotated about the <centerOfRotation>, and this rotation
 * is determined by <viewingVector>. Zoom level is the magnitude of <viewingVector>,
 * described by <zoomRadius>. Any translation of the viewing platform is then applied by
 * means of <translationVector>.
 * 
 * Notes: 
 * 
 * 1. The above is now implemented using Camera3D, which uses a lineOfSight vector (same as
 * <viewingVector>), a centerOfRotation, a targetPoint (specifying camera pannning), and an
 * upVector (specifying the camera's up position)... uses Transform3D's lookAt function to
 * obtain the camera transform. Map3D simply calls the camera's getCameraTransform method to
 * set the target transform.
 * 
 * 2. It is good to know that the default ViewPosition is located at 0, 0, 0 and the
 * default viewingVector is (0, 0, 1); i.e., the initial viewingVector is the pos Z-axis. All
 * transformations generated here are with respect to this default orientation.
 * 
 * 3. The viewing platform is essentially a fixed-size 3D planar quadrangle, one side of which 
 * is the viewport to the 3D model. You can position this viewport however you like. In the
 * present object, the intent is to provide a line-of-sight from some specific 3D point (the 
 * center-of-rotation offset by the translation vector) to the viewer's eye, and to specify
 * a "zoom" about this point, which is essentially a translation along the viewing vector (or,
 * equivalently, a scaling of the platform if you relax the fixed-size requirement). Thus, zoom
 * is expressed as a radius (a distance from the target point).
 * 
 * @author Andrew Reid
 *
 */


public class Map3D extends Map {

	//public fields
	protected Camera3D camera = new Camera3D();
	
	//could also make this a list
	protected TransformGroup targetTransform;
	protected View thisView;
	protected Sphere3D thisBounds = new Sphere3D(new Point3f(0, 0, 0), 50);
	protected Point3f centerPt = new Point3f(0, 0, 0);
	protected double zoomClip = 20;

	public Map3D(){
	}
	
	/************
	 * Generates a Transform3D object specifying this map's parameter states.
	 * @param thisTransform A transform object to set (passing this parameter avoids
	 * the necessity of instantiating a new transform, and thus "memory burn")
	 * @return the Transform3D object to apply to ViewingPlatform
	 */
	
	/**@deprecated **/
	@Deprecated
	public void setViewPlatformTransform(Transform3D thisTransform){
		//thisTransform.set(thisCamera.getCameraTransform());
		updateViewPlatformTransform(thisTransform);
	}
	
	public Camera3D getCamera(){
		return camera;
	}
	
	public void setCamera(Camera3D camera){
		this.camera = camera;
	}
	
	public void updateViewPlatformTransform(Transform3D thisTransform){
		thisTransform.set(camera.getCameraTransform());
	}
	
	public Transform3D getViewPlatformTransform(){
		Transform3D thisT = new Transform3D();
		updateViewPlatformTransform(thisT);
		return thisT;
	}
	
	public void updateTargetTransform(){
		if (targetTransform != null)
			try{
				targetTransform.setTransform(getViewPlatformTransform());
			}catch (BadTransformException e){
				InterfaceSession.log("Map3D: Bad transform encountered..");
				}
		//update light
		camera.updateLightSources();
		camera.fireCameraAngleChanged();
	}
	
	public void setTargetTransform(TransformGroup thisTarget){
		if (targetTransform != null){
			//Destroy it
			targetTransform.removeAllChildren();
			}
		targetTransform = thisTarget;
	}
	
	public void setView(View v){
		thisView = v;
		thisView.setBackClipPolicy(View.VIRTUAL_SCREEN);
	}
	
	public void setBounds(Sphere3D bounds){
		thisBounds = bounds;
	}
	
	public void setCenter(Point3f c){
		centerPt = c;
		camera.centerOfRotation.set(c);
	}
	
	public void updateClipBounds(){
		if (thisView == null || thisBounds == null) return;
		if (camera.centerOfRotation == null) {
			camera.centerOfRotation = new Point3d(centerPt.x, 
													  centerPt.y, 
													  centerPt.z);
			camera.setTranslateXY(new Vector2d(0, 0));
			}
		camera.setMinDistance(Math.min(thisBounds.radius, 1) / 100);
		thisView.setBackClipDistance((camera.distance + thisBounds.radius) * zoomClip);
		updateTargetTransform();
	}
	
	@Override
	public int getType(){
		return Map.MAP_3D;
	}
	
	@Override
	public double getZoom(){
		if (camera != null)
			return camera.distance;
		return -1;
	}
	
	@Override
	public void setZoom(double z){
		camera.setDistance(z);
	}

	public void addLightSource(DirectionalLight light){
		camera.addLightSource(light);
	}
	
	public void addLightSource(DirectionalLight light, Vector2d offset){
		camera.addLightSource(light, offset);
	}
	
	public void removeLightSource(DirectionalLight light){
		camera.removeLightSource(light);
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		
		//TODO: add map stuff
		treeNode.add(camera.issueTreeNode());
		
	}
	
	@Override
	public String toString(){
		return "Map3D";
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
	public void writeXML(int tab, Writer writer, XMLOutputOptions options,
			ProgressUpdater progress_bar) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeXML(int tab, Writer writer) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getShortXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}
	
}