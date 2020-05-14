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

package mgui.io.domestic.videos;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.graphics.video.Video;
import mgui.interfaces.graphics.video.xml.VideoXMLHandler;
import mgui.io.InterfaceIOOptions;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


public class VideoXMLLoader extends VideoLoader {

	public VideoXMLLoader(){
		
	}
	
	public VideoXMLLoader(File file){
		setFile(file);
	}
	
	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		return loadVideo();
	}
	
	@Override
	public Video loadVideo(VideoInputOptions options,
						   ProgressUpdater progress_bar) {
		
		return loadVideo();
		
	}
	
	public Video loadVideo() {
		
		try{
			XMLReader reader = XMLReaderFactory.createXMLReader();
			VideoXMLHandler handler = new VideoXMLHandler();
			reader.setContentHandler(handler);
			reader.setErrorHandler(handler);
			reader.parse(new InputSource(new FileReader(dataFile)));
			return handler.getVideo();
			
		}catch (Exception e){
			e.printStackTrace();
			return null;
			}
		
	}

}