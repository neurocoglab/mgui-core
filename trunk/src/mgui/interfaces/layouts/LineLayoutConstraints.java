/*
* Copyright (C) 2011 Andrew Reid and the modelGUI Project <http://mgui.wikidot.com>
* 
* This file is part of modelGUI[core] (mgui-core).
* 
* modelGUI[core] is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* modelGUI[core] is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with modelGUI[core]. If not, see <http://www.gnu.org/licenses/>.
*/

package mgui.interfaces.layouts;

public class LineLayoutConstraints {

	public int lineFrom;
	public int lineTo;
	public double hPos;
	public double hWeight;
	public double vWeight;
	
	public LineLayoutConstraints(){	}
	
	public LineLayoutConstraints(int from, int to, double pos, double hweight, double vweight){
		lineFrom = from;
		lineTo = to;
		hPos = Math.min(pos, 1);
		hPos = Math.max(0, hPos);
		hWeight = Math.min(hweight, 1);
		hWeight = Math.max(0, hWeight);
		vWeight = Math.min(vweight, 2);
		vWeight = Math.max(0, vWeight);
	}
	
}