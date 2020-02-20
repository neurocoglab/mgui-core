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

package mgui.interfaces.shapes;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.ArrayList;

import mgui.geometry.Grid3D;
import mgui.geometry.volume.VolumeFunctions;
import mgui.interfaces.attributes.tree.AttributeTreeNode;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeListener;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.util.ImageFunctions;

/********************************************************
 * A set of {@link Volume3DInt} objects which render as an overlay image. This class extends <code>Volume3DInt</code>
 * and inherits its behaviour; it is specified as a stack of images with given image and geometric dimensions, and
 * samples from its member volumes within its bounding box to compose an overlay image. Anything outside the bounding
 * box is not rendered.
 * 
 * <p>Overlays are determined by: 
 * 
 * <ul>
 * <li>An ordered list of member volumes
 * </ul>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class VolumeSet3DInt extends Volume3DInt implements ShapeListener{

	ArrayList<Volume3DInt> members = new ArrayList<Volume3DInt>();
	ArrayList<Float> alphas = new ArrayList<Float>();
	
	public VolumeSet3DInt(){
		super();
	}
	
	public VolumeSet3DInt(Grid3D grid){
		super(grid);
	}
	
	/***********************************************
	 * Gets a list of this set's members
	 * 
	 * @return
	 */
	public ArrayList<Volume3DInt> getMembers(){
		return new ArrayList<Volume3DInt>(members);
	}
	
	/***********************************************
	 * Gets the i'th member in this set
	 * 
	 * @param i
	 * @return
	 */
	public Volume3DInt getMember(int i){
		return members.get(i);
	}
	
	
	public int getMemberCount(){
		return members.size();
	}
	
	/*************************************
	 * Adds <code>volume</code> to top of this overlay set, with given <code>alpha</code> level, and redraws the overlay.
	 * 
	 * @param volume
	 * @param alpha
	 * @param update
	 */
	public void addVolume(Volume3DInt volume, float alpha, boolean update){
		addVolume(-1, alpha, volume, update);
	}
	
	/*************************************
	 * Adds <code>volume</code> at position <code>index</code> of this overlay set, with given <code>alpha</code> 
	 * level, and redraws the overlay. Prevents <code>volume</code> from creating its own scene nodes using the
	 * <code>creatable_scene_node</code> flag.
	 * 
	 * @param index The position of this volume in the set; negative adds to the top
	 * @param alpha	The transparency of this volume in the overlay
	 * @param volume
	 * @param update
	 */
	public void addVolume(int index, float alpha, Volume3DInt volume, boolean update){
		volume.setCreatableSceneNode(false);
		if (index < 0){
			alphas.add(0, alpha);
			members.add(0, volume);
		}else{
			alphas.add(index, alpha);
			members.add(index, volume);
			}
		
		generateComposites();
		updateSceneNode();
		
		if (update){
			fireShapeModified();
			fireChildren2DModified();
			}
	}
	
	/********************************
	 * Removes the given volume (actually the first volume having the identical name) from this set.
	 * 
	 * @param volume
	 * @param update
	 */
	public void removeVolume(Volume3DInt volume, boolean update){
		removeVolume(volume.getName(), update);
	}
	
	/********************************
	 * Removes the first volume having name <code>name</code> from this set.
	 * 
	 * @param volume
	 * @param update
	 */
	public void removeVolume(String name, boolean update){
		for (int i = 0; i < members.size(); i++){
			if (members.get(i).getName().equals(name)){
				members.get(i).setCreatableSceneNode(true);
				members.remove(i);
				//alphas.remove(i);
				
				generateComposites();
				updateSceneNode();
				
				if (update){
					fireShapeModified();
					fireChildren2DModified();
					}
				}
			}
	}
	
	@Override
	public void setScene3DObject(boolean make_live){
		//set the composites and create the scene node
		generateComposites();
		
		super.setScene3DObject(make_live);
		
	}
	
	/******************************************************
	 * Sets the transparency for the i'th volume in this set.
	 * 
	 * @param i
	 */
	public void setAlpha(int i, float alpha){
		if (i < alphas.size())
			alphas.set(i, alpha);
	}
	
	/******************************************************
	 * Gets the transparency for the i'th volume
	 * 
	 * @param i
	 * @return
	 */
	public float getAlpha(int i){
		return alphas.get(i);
	}
	
	
	
	/*************
	 * This method updates the overlay composite <code>Grid3D</code> by sampling from all members and applying their
	 * transparency levels.
	 * 
	 * @return A stack of composite images
	 */
	public BufferedImage[] generateComposites(){
		
		Grid3D grid = getGrid();
		Volume3DInt sample_volume = new Volume3DInt(new Grid3D(grid));
		
		if (grid == null) return null;
		
		BufferedImage[] volume_images = VolumeFunctions.getMaskedImages(this);
		
		//sample each member for each image plane, and get colour from its colour map
		//start at bottom
		for (int i = members.size() - 1; i >= 0; i--){
			Volume3DInt i_volume = members.get(i);
			
			float alpha = alphas.get(i);
			
			//get samples
			sample_volume.sampleFromVolume(i_volume);
			Grid3D sample_grid = sample_volume.getGrid();
			
			BufferedImage[] sample_images = VolumeFunctions.getMaskedImages(sample_volume);
			
			//apply as overlays
			for (int k = 0; k < sample_grid.getSizeR(); k++){
				BufferedImage sample_img = sample_images[k];
				BufferedImage new_composite_img = volume_images[k];
				BufferedImage prev_composite_img = ImageFunctions.getCopy(new_composite_img); 
				
				Graphics2D g = new_composite_img.createGraphics();
				
				g.drawImage(prev_composite_img, 0, 0, null);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
				g.drawImage(sample_img, 0, 0, null);
				g.dispose();
				}
			}
		
		return volume_images;
	}
	
	/****
	 * Returns an image having a colour model with type {@link BufferedImage.TYPE_INT_ARGB}, with colours set from 
	 * <code>image</code>'s colour model.
	 * 
	 * @param image
	 * @param c_model
	 * @return
	 */
	protected BufferedImage getSampledImage(BufferedImage sample_image){
		
		BufferedImage image = new BufferedImage(sample_image.getWidth(), sample_image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		ColorModel cm = sample_image.getColorModel();
		
		//set each pixel
		for (int i = 0; i < image.getWidth(); i++)
			for (int j = 0; j < image.getHeight(); j++){
				int[] pixel = sample_image.getRaster().getPixel(i, j, (int[])null);
				image.getRaster().setPixel(i, j, new int[]{cm.getAlpha(pixel),
														   cm.getRed(pixel), 
														   cm.getGreen(pixel), 
														   cm.getBlue(pixel)});
				}
		
		return image;
		
	}
	
	@Override
	public void shapeUpdated(ShapeEvent e){
		//respond to member changes
		if (e.getShape().isDestroyed()){
			removeVolume((Volume3DInt)e.getShape(), true);
			return;
			}
		
		generateComposites();
		updateSceneNode();
		fireShapeModified();
		fireChildren2DModified();
			
	}
	
	/****************************************
	 * Constructs a tree node from this shape. Adds an {@link AttributeTreeNode} via the super method, and 
	 * also adds a node to display the vertex-wise data columns associated with this ShapeInt. 
	 * 
	 * @param treeNode node to construct
	 */
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		//Does not call super because we don't want to add a data node here, and super.super is illegal.
		treeNode.removeAllChildren();
		treeNode.setUserObject(this);
		
		for (int i = 0; i < members.size(); i++)
			if (!treeNode.containsObject(members.get(i)))
				treeNode.add(members.get(i).issueTreeNode());
		
	}
	
	
}