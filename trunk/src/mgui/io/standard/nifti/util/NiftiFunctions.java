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

package mgui.io.standard.nifti.util;

import java.awt.image.DataBuffer;

import mgui.io.standard.nifti.NiftiMetadata;


public class NiftiFunctions {

	
	/***************************
	 * Convert from {@link DataBuffer} types to Nifti types.
	 * 
	 */
	public static int getNiftiDataType(int type){
		switch (type){
			case DataBuffer.TYPE_BYTE:
				return NiftiMetadata.DT_BINARY;
			case DataBuffer.TYPE_INT:
				return NiftiMetadata.NIFTI_TYPE_INT32;
			case DataBuffer.TYPE_FLOAT:
				return NiftiMetadata.NIFTI_TYPE_FLOAT32;
			case DataBuffer.TYPE_DOUBLE:
				return NiftiMetadata.NIFTI_TYPE_FLOAT64;
			case DataBuffer.TYPE_SHORT:
				return NiftiMetadata.NIFTI_TYPE_INT16;
			case DataBuffer.TYPE_USHORT:
				return NiftiMetadata.NIFTI_TYPE_UINT8;
			default:
				return NiftiMetadata.NIFTI_TYPE_INT32;
			}
	}
	
	public static int getDataType(int AnalyzeType){
		switch(AnalyzeType){
			case NiftiMetadata.DT_BINARY:
				return DataBuffer.TYPE_BYTE;
			case NiftiMetadata.NIFTI_TYPE_FLOAT64:
				return DataBuffer.TYPE_DOUBLE;
			case NiftiMetadata.NIFTI_TYPE_FLOAT32:
				return DataBuffer.TYPE_FLOAT;
			case NiftiMetadata.NIFTI_TYPE_INT32:
				return DataBuffer.TYPE_INT;
			case NiftiMetadata.NIFTI_TYPE_UINT8:
			case NiftiMetadata.NIFTI_TYPE_UINT16:
				return DataBuffer.TYPE_USHORT;
			case NiftiMetadata.NIFTI_TYPE_INT16:
				return DataBuffer.TYPE_SHORT;
		
			}
		return -1;
	}
	
}