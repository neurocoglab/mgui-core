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

package mgui.interfaces.shapes.volume;

import java.awt.image.DataBuffer;
import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import mgui.datasources.DataType;
import mgui.geometry.Box3D;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.numbers.MguiNumber;

/****************************************
 * An interface for extracting metadata from a volume header.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public interface VolumeMetadata {

	/******
	 * The data dimensions: (x, y, z) or (x, y, z, t).
	 * 
	 * @return The data dimensions.
	 */
	public int[] getDataDims();
	
	/******
	 * The geometric dimensions of the volume (x, y, z) or (S, T, R) in texture coordinates. Note this is the distance from the center point
	 * of the origin voxel to the center point of the last voxel in the direction of the axis. The geometric extent of the bounding box will
	 * be a full voxel length larger, in order to place the voxel centers at the correct coordinate.
	 * 
	 * @return The geometric dimensions of the volume.
	 */
	public float[] getGeomDims();
	
	/*************
	 * Returns the S, T, and R axes of this volume, respectively.
	 * 
	 * @return
	 */
	public Vector3f[] getAxes();
	
	/***********
	 * Returns the intravoxel dimension (e.g., time or vector/tensor information)
	 * 
	 * @return
	 */
	public int getVoxelDim();
	
	/******
	 * The geometric origin of the volume as a <code>Point3f</code>. Note that this is the center point of the 
	 * origin voxel. The origin of the bounding box will be a half voxel displaced from this point, to place its
	 * center point correctly. Metadata handlers must adjust according to the policy of their data formats, to return the correct coordinates.
	 * 
	 * @return The geometric origin of the volume
	 */
	public Point3f getOrigin();
	
	/******
	 * The data type; must correspond to {@link DataBuffer} types.
	 * 
	 * @return
	 */
	public int getDataType();
	
	/******
	 * The geometric bounds of this volume; note that the bounding box will be scaled up by one voxel length in each axis
	 * direction, and its origin will be shifted a half voxel, such that voxel center points are positioned correctly. 
	 * 
	 * @return
	 */
	public Box3D getBounds();

	
	/******
	 * Sets the data dimensions
	 * 
	 * @param dims
	 */
	public void setDataDims(int[] dims);
	
	/******
	 * Sets the geometric dimensions
	 * 
	 * @param dims
	 */
	public void setGeomDims(float[] dims);
	
	/***********
	 * Sets the S, T, and R axes of this volume.
	 * 
	 * @param axes Three vectors, in the order S, T, and R.
	 */
	public void setAxes(Vector3f[] axes);
	
	/************
	 * Sets the intravoxel dimension (e.g., time or vector/tensor information)
	 * 
	 * @param t
	 */
	public void setVoxelDim(int t);
	
	/******
	 * Sets the origin
	 * 
	 * @param origin
	 */
	public void setOrigin(Point3f origin);
	
	/******
	 * Sets the data type; must correspond to {@link DataBuffer} types. Note that this is the origin of the
	 * bounding box; thus the corner of the first voxel, rather than its center point. Metadata handlers
	 * must adjust according to the policy of their data formats.
	 * 
	 * @param type
	 */
	public void setDataType(int type);
	
	/********
	 * Sets this metadata from <code>volume</code>.
	 * 
	 * @param grid
	 */
	public void setFromVolume(Volume3DInt volume);
	
	/********
	 * Sets this metadata from <code>grid</code>, for the specified column.
	 * 
	 * @param grid
	 * @param channel
	 */
	public void setFromVolume(Volume3DInt volume, String column);
	
	/********
	 * Reads the 3D volume at voxel dimension {@code t}. 
	 * 
	 * @param t
	 * @return
	 */
	public ArrayList<MguiNumber> readVolume(int t, DataType data_type, ProgressUpdater progress) throws IOException;
	
	/**************
	 * Sets this metadata from another metadata object.
	 * 
	 * @param metadata
	 */
	public void setFromMetadata(VolumeMetadata metadata);
	
	
}