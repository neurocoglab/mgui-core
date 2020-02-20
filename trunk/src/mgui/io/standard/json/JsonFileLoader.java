package mgui.io.standard.json;

import java.io.FileReader;
import java.io.IOException;

import mgui.interfaces.ProgressUpdater;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

/*******************************************************
 * Loads a Json format file.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class JsonFileLoader extends FileLoader {

	JsonObject json_object;
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		
		
		return false;
	}

	
	public JsonObject loadObject(InterfaceIOOptions options, ProgressUpdater progress_bar) throws IOException{
	
		
		JsonReader loader = new JsonReader(new FileReader(dataFile));
		
		
		return null;
		
		
	}
	
	
}
