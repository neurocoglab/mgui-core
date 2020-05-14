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

package mgui.io.standard.xml.svg;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class SVGFileFilter extends FileFilter {

	public SVGFileFilter() {
	}

	@Override
	public boolean accept(File file) {
		if (file.isDirectory()) {
			return true;
		} else {
			return SVGParser.isSVGFile(file);
		}
	}

	@Override
	public String getDescription() {
		return new String("SVG file");
	}
}