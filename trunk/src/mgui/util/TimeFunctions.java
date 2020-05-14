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

package mgui.util;

public class TimeFunctions {

	public static String getTimeStr(long time){
		
		return getTimeStr(time, ":");
	}
	
	public static String getTimeStr(long time, String div){
		
		String t1 = String.format("%02d", time/60000);
		String t2 = String.format("%02d", (time%60000)/1000);
		String t3 = String.format("%03d", time%1000);
		
		return t1 + div + t2 + div + t3;
		
		
	}
	
	public static long getTimeFromStr(String time){
		
		return Long.valueOf(time.substring(0,2)) * 60000 + 
			   Long.valueOf(time.substring(3,5)) * 1000 +
			   Long.valueOf(time.substring(6,9));
	}
	
}