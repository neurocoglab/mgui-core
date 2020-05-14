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

public class Instruction extends SVGObject {
	
	public Instruction(SVGParser.Mode mode, int max_number_of_numbers) {
		this.mode = mode;
		this.max_number_of_numbers = max_number_of_numbers;
		numbers = new int[max_number_of_numbers];
		number_of_numbers = 0;
	}

	public boolean addNumber(int number) {
		if (number_of_numbers >= max_number_of_numbers) {
			return false;
		} else {
			numbers[number_of_numbers] = number;
			number_of_numbers++;
			return true;
		}
	}

	public SVGParser.Mode getMode() {
		return mode;
	}

	public int[] numbers;
	public int number_of_numbers;
	public int max_number_of_numbers;
	private SVGParser.Mode mode;
	
}