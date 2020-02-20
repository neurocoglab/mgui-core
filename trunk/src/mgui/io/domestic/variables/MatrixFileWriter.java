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

package mgui.io.domestic.variables;

import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOException;
import Jama.Matrix;

/*******************************************
 * Base abstract class for writing a matrix to file.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public abstract class MatrixFileWriter extends FileWriter {

	public String number_format = "#0.0000####";
	
	public boolean writeMatrix(Matrix m) throws IOException, InterfaceIOException{
		return writeMatrix(m, null);
	}
	
	public abstract boolean writeMatrix(Matrix m, ProgressUpdater progress_bar) throws IOException, InterfaceIOException;
	public void setFormat(String format){
		this.number_format = format;
	}
	
	@Override
	public String getSuccessMessage(){
		return "Matrix written successfully.";
	}
	
	@Override
	public String getFailureMessage(){
		return "Failed to fully write matrix.";
	}
	
	@Override
	public String getTitle(){
		return "Write matrix to file";
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = MatrixFileWriter.class.getResource("/mgui/resources/icons/matrix_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/matrix_20.png");
		return null;
	}
	
}