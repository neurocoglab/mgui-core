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

package mgui.interfaces;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import mgui.datasources.DataSource;
import mgui.datasources.DataSourceDriver;
import mgui.datasources.security.SecureDataSourceFunctions;
import mgui.geometry.util.NodeShape;
import mgui.geometry.util.SpatialUnit;
import mgui.interfaces.datasources.DataSourceDialogPanel;
import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.graphics.InterfaceGraphicWindow;
import mgui.interfaces.graphics.video.VideoTask;
import mgui.interfaces.graphics.video.VideoTaskType;
import mgui.interfaces.graphs.util.GraphFunctions;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.logs.LoggingTarget;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.ContinuousColourMap;
import mgui.interfaces.maps.DiscreteColourMap;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.pipelines.libraries.PipelineProcessLibrary;
import mgui.interfaces.plots.InterfacePlot;
import mgui.io.InterfaceIO;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.datasources.DataSourceLoader;
import mgui.io.domestic.maps.ColourMapLoader;
import mgui.io.domestic.maps.ContinuousColourMapLoader;
import mgui.io.domestic.maps.DiscreteColourMapLoader;
import mgui.io.domestic.maps.NameMapLoader;
import mgui.io.domestic.pipelines.PipelineProcessLibraryLoader;
import mgui.io.util.ParallelOutputStream;
import mgui.io.util.WildcardFileFilter;
import mgui.pipelines.PipelineProcess;
import mgui.util.QuotedStringTokenizer;
import mgui.util.ScaledUnit;
import mgui.util.Unit;

/*********************************
 * Stores environmental variables and provides static methods to retrieve them. The <code>init()</code>
 * method should be called by <code>InterfaceFrame</code> in order to calibrate the session environment
 * from a specific init file. By default this file is /mgui/resources/init/mgui.model.init.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 * TODO: implement logging to file
 *
 */
public class InterfaceEnvironment implements Environment {
	
	static Locale locale = Locale.CANADA;
	static HashMap<String, Class<? extends InterfacePanel>> interfacePanelClasses = 
										new HashMap<String, Class<? extends InterfacePanel>>();
	static HashMap<String, Class<? extends InterfaceGraphic<?>>> interfaceGraphicClasses = 
										new HashMap<String, Class<? extends InterfaceGraphic<?>>>();
	static HashMap<String, DataSourceDriver> dataSourceDrivers = 
										new HashMap<String, DataSourceDriver>();
//	static HashMap<String, Class<?>> interfaceDataSourceDialogPanelClasses = 
//										new HashMap<String, Class<?>>();
	static HashMap<String, Class<? extends InterfacePlot<?>>> interfacePlotClasses = 
										new HashMap<String, Class<? extends InterfacePlot<?>>>();
	static InterfaceDataSourceDialogLoader interfaceDataSourceDialogLoader = 
										new InterfaceDataSourceDialogLoader();
	static InterfaceGraphicLoader interfaceGraphicLoader = new InterfaceGraphicLoader();
	static InterfacePanelLoader interfacePanelLoader = new InterfacePanelLoader();
	static InterfacePlotLoader interfacePlotLoader = new InterfacePlotLoader();
	static DialogPanelLoader dialogPanelLoader = new DialogPanelLoader();
	static InterfaceDialogLoader interfaceDialogLoader = new InterfaceDialogLoader();
	static InterfaceOptionsLoader interfaceOptionsLoader = new InterfaceOptionsLoader();
	static InterfaceIOLoader interfaceIOLoader = new InterfaceIOLoader();
	static VideoTaskLoader videoTaskLoader = new VideoTaskLoader();
	static String version = "unknown";
	static ArrayList<DataSource> dataSources = new ArrayList<DataSource>();
	static ArrayList<InterfaceGraphicWindow> graphicsWindows = new ArrayList<InterfaceGraphicWindow>();
	static ArrayList<ColourMap> colourMaps = new ArrayList<ColourMap>();		//put in display panel?
	static ArrayList<NameMap> nameMaps = new ArrayList<NameMap>();				//put in "		 "	  ?
	static HashMap<String, NodeShape> vertexShapes = new HashMap<String, NodeShape>();
	static HashMap<String, VideoTaskType> videoTaskTypes = new HashMap<String, VideoTaskType>();
	static HashMap<String, InterfaceIOType> ioTypes = new HashMap<String, InterfaceIOType>();
	public static File init_file; // = new File("ar.model.init");
	static File application_data_dir;
	static HashMap<String, Unit> units = new HashMap<String, Unit>();
	//stable lists of units
	static ArrayList<Unit> unit_list = new ArrayList<Unit>();
	static ArrayList<SpatialUnit> spatial_unit_list = new ArrayList<SpatialUnit>();
	static String default_spatial_unit;
	//protected static ArrayList<PipelineProcess> processes = new ArrayList<PipelineProcess>(); //pipeline processes
	static HashMap<String, PipelineProcessLibrary> process_libraries = new HashMap<String, PipelineProcessLibrary>();
	static String look_and_feel = "java";
	
	static ParallelOutputStream output_stream;
	static ParallelOutputStream error_stream;
	
	protected static File current_dir;
	
	static String xml_header = "<?xml version=\"1.0\"?>";
	static InterfaceFrame top_frame;
	
	static String locked_task = null;
	static int line_height = 24;
	static int max_display_vertices = 10000;  	// Maximal # of vertices to display for a shape
	
	public static enum Snapshot3DMode{
		OffscreenBuffer,
		ScreenCapture;
	}
	static Snapshot3DMode snapshot_3d_mode = Snapshot3DMode.OffscreenBuffer;
	
	static boolean is_init = false;
	
	public static enum OsType{
		Windows,
		Linux,
		Mac;
	}
	
	static OsType OS = initOS(); // OsType.WindowsXP; 
	
	protected static LoggingTarget logging_target = LoggingTarget.ToConsole;
	protected static LoggingType logging_type = LoggingType.Verbose;
	protected static boolean logging_timestamp = true;
	protected static File log_file;
	
	static OsType initOS(){
		String os = System.getProperty("os.name").toLowerCase();
		
		if (os.contains("win"))
			OS = OsType.Windows;
		if (os.contains("nux"))
			OS = OsType.Linux;
		if (os.contains("mac"))
			OS = OsType.Mac;
		
		return OS;
	}
	
	protected InterfaceEnvironment() {
		throw new AssertionError("Cannot instantiate class InterfaceEnvironment!");
	}	//can't instantiate an environment
	
	/**************************
	 * Initiate environment from a specified init file.
	 * 
	 * @param frame
	 * @param initFile
	 * @return
	 */
	public static boolean init(InterfaceFrame frame, String initFile){
		top_frame = frame;
		current_dir = new File((new File(".")).getAbsolutePath()).getParentFile();
		setInitFile(initFile);
		return init();
	}
	
	/**************************************
	 * Indicates whether this environment has been initialized (i.e., the {@code init} function
	 * has been called).
	 * 
	 * @return
	 */
	public static boolean isInit(){
		return is_init;
	}
	
	public static OsType getOsType(){
		if (OS == null)
			return initOS();
		return OS;
	}
	
	public static LoggingTarget getLoggingTarget(){
		return logging_target;
	}
	
	public static LoggingType getLoggingType(){
		return logging_type;
	}
	
	public static void setLoggingType(LoggingType type){
		logging_type = type;
	}
	
	public static File getLogFile(){
		return log_file;
	}
	
	public static boolean getLoggingTimestamp(){
		return logging_timestamp;
	}
	
	/***************************************
	 * Specifies the line width for panels using {@link LineLayout}s or
	 * {@link CategoryLayout}s.
	 * 
	 * @return
	 */
	public static int getLineHeight(){
		return line_height;
	}
	
	/***************************************
	 * Specifies the maximal number of vertices to display on a shape for the current system.
	 * Shapes with more vertices than this should not attempt to render them.
	 * 
	 */
	public static int getMaxDisplayVertices(){
		return max_display_vertices;
	}
	
	/************************************************
	 * Initialize the environment
	 * 
	 */
	public static void initOSType(){
		String os_string = System.getProperty("os.name").toLowerCase();
		if (os_string.contains("windows") && os_string.contains("xp"))
			OS = OsType.Windows;
		if (os_string.contains("linux") || os_string.contains("unix"))
			OS = OsType.Linux;
	}
	
	public static InterfaceFrame getFrame(){
		return top_frame;
	}
	
	public static void setFrame(InterfaceFrame frame){
		top_frame = frame;
	}
	
	public static InterfaceWorkspace getWorkspaceInstance(){
		//TODO: set up workspace here
		
		return new InterfaceWorkspace();
	}
	
	/*******************************
	 * Returns the current mode for take 3D screenshots. One of:
	 * 
	 * <ul>
	 * <li>OffscreenBuffer  [default]: Write to an offscreen buffer
	 * <li>ScreenCapture 	         : Use a screen capture to get the graphics; only use if buffer is not working with your system
	 * </ul>
	 * 
	 * @return
	 */
	public static Snapshot3DMode getSnapshot3DMode(){
		return snapshot_3d_mode;
	}
	
	/*****************************
	 * Returns the current directory
	 * 
	 * @return
	 */
	public static File getCurrentDir(){
		return current_dir;
	}
	
	/******************************
	 * Returns the application data directory; if this is not set, returns the current dir
	 * 
	 * @param line
	 * @return
	 */
	public static File getApplicationDataDir(){
		if (application_data_dir == null) return getCurrentDir();
		return application_data_dir;
	}
	
	/******************************
	 * Sets the application data directory; if this is not set, returns the current dir
	 * 
	 * @param line
	 * @return
	 */
	public static void setApplicationDataDir(String dir){
		application_data_dir = new File(dir);
		System.out.println("Application dir: " + application_data_dir.getAbsolutePath());
	}
	
	static boolean handleLine(String line){
		
		String sep = File.separator;
		int c_index = line.indexOf(" ");
		if (!line.startsWith("#")){ // && line.indexOf(" ") > 0){
			String command = line;
			if (c_index > 0)
				command = line.substring(0, line.indexOf(" "));
			if (command.equals("setVersion")){
				version = line.substring(c_index + 1);
				}
			if (command.equals("setSnapshot3DMode")){
				String mode = line.substring(c_index + 1);
				if (mode.toLowerCase().equals("screencapture"))
					snapshot_3d_mode = Snapshot3DMode.ScreenCapture;
				}
			if (command.equals("setApplicationDataDir")){
				int p = c_index;
				String s = line.substring(p + 1);
				if (s.length() > 0){
					if (s.contains("{user}")){
						s = s.replace("{user}", System.getProperty("user.name"));
						}
					setApplicationDataDir(s);
					}
				
				}
			if (command.startsWith("setLogFile")){
				int p = c_index;
				String s = line.substring(p + 1);
				s = s.concat("_" + getNow("yyyyMMdd") + ".log");
				//java.net.URL log_url = InterfaceEnvironment.class.getResource("/logs");
				
				try{
					//File dir = new File(log_url.toURI());
					File dir = getApplicationDataDir();
					log_file = new File(dir.getAbsolutePath() + File.separator + "logs" + File.separator + s);
				}catch (Exception e){
					//shouldn't get here..
					InterfaceSession.log("InterfaceEnvironment: Cannot create log file '" + 
										 s + "'.",
										 LoggingType.Errors);
					e.printStackTrace();
					return false;
					}
				
				try{
					if (!log_file.exists() && !log_file.createNewFile()){
						InterfaceSession.log("InterfaceEnvironment: Cannot create log file '" + 
											log_file.getAbsolutePath());
						log_file = null;
						p = -1;
					}
				}catch (IOException ex){
					InterfaceSession.log("InterfaceEnvironment: Cannot create log file '" + 
							log_file.getAbsolutePath());
					return false;
					}
				if (p == 10)
					logging_target = LoggingTarget.ToFile;
				else if (p > 0 && command.contains("AndConsole"))
					logging_target = LoggingTarget.ToFileAndConsole;
				else
					logging_target = LoggingTarget.ToConsole;
				}
			if (command.startsWith("setLogConsole")){
				log_file = null;
				logging_target = LoggingTarget.ToConsole;
				InterfaceSession.log("Logging to console", LoggingType.Verbose);
				}
			if (command.equals("setLogNone")){
				log_file = null;
				logging_target = LoggingTarget.None;
				}
			if (command.startsWith("setLoggingType")){
				String s = line.substring(c_index + 1);
				String[] parts = s.split(" ");
				s = parts[0].toLowerCase();
				if (s.equals("errors"))
					logging_type = LoggingType.Errors;
				if (s.equals("concise"))
					logging_type = LoggingType.Concise;
				if (s.equals("verbose"))
					logging_type = LoggingType.Verbose;
				if (s.equals("debug"))
					logging_type = LoggingType.Debug;
				if (parts.length > 1 && parts[1].equals("timestamp"))
					logging_timestamp = true;
				else
					logging_timestamp = false;
				}
			if (command.equals("setDataSourceKey")){
				String key_urlstr = line.substring(c_index + 1);
				setDataSourceKey(key_urlstr);
				}
			if (command.equals("setLocale")){
				String language = line.substring(c_index + 1);
				String country = language.substring(language.indexOf(" ") + 1);
				language = language.substring(0, language.indexOf(" "));
				setLocale(language, country);
				}
			if (command.equals("addUnit")){
				StringTokenizer tokens = new StringTokenizer(line);
				tokens.nextToken();
				if (!(tokens.countTokens() > 2)){
					InterfaceSession.log("InterfaceEnvironment: Unit ill-specified!", 
										 LoggingType.Errors);
				}else{
					String type = tokens.nextToken();
					String name = tokens.nextToken();
					String params = "";
					int a = line.indexOf(type) + type.length() + name.length() + 2;
					if (line.length() > a)
						params = line.substring(a);
					addUnit(name, type, params);
					}
				}
			if (command.equals("addKnownDataSourceDrivers")){
				addKnownDataSourceDrivers();
				}
			if (command.equals("addKnownDataSourceDriver")){
				if (!addKnownDataSourceDriver(line.substring(c_index + 1)))
					InterfaceSession.log("InterfaceEnvironment: error loading known data source driver '"
										 + line.substring(line.indexOf(" ") + 1) + "'",
										 LoggingType.Errors);
				}
			if (command.equals("addDataSourceDriver")){
				if (!addDataSourceDriver(line.substring(c_index + 1)))
					InterfaceSession.log("InterfaceEnvironment: error loading data source driver '"
										 + line.substring(line.indexOf(" ") + 1) + "'",
										 LoggingType.Errors);
				}
			if (command.equals("loadDataSource")){
				String file = line.substring(c_index + 1);
				if (!loadDataSource(file))
					InterfaceSession.log("InterfaceEnvironment: error loading data source '"
										 + file + "'", 
										 LoggingType.Errors);
				}
			if (command.equals("loadDataSources")){
				String dir = line.substring(c_index + 1);
				if (!loadDataSources(dir))
					InterfaceSession.log("InterfaceEnvironment: error loading data sources from '" + dir + "'", 
										 LoggingType.Errors);
				}
			if (command.equals("setXMLHeader")){
				String h = line.substring(c_index + 1);
				if (h.length() > 0) xml_header = h;
				}
			if (command.equals("loadInterfaceGraphicClass")){
				String sub = line.substring(c_index + 1);
				int a = -1;
				String name = "";
				if (sub.startsWith("\""))
					a = sub.lastIndexOf("\"");
				if (a > 0)
					name = sub.substring(1, a);
				else
					name = sub.substring(0, sub.indexOf(" "));
				if (name.length() > 0){
					String cName = sub.substring(sub.lastIndexOf(" ") + 1);
					addInterfaceGraphicClass(name, cName);
					}
				}
			if (command.equals("loadInterfacePanelClass")){
				String sub = line.substring(c_index + 1);
				int a = -1;
				String name = "";
				if (sub.startsWith("\""))
					a = sub.lastIndexOf("\"");
				if (a > 0)
					name = sub.substring(1, a);
				else
					name = sub.substring(0, sub.indexOf(" "));
				if (name.length() > 0){
					String cName = sub.substring(sub.lastIndexOf(" ") + 1);
					addInterfacePanelClass(name, cName);
					}
					
				}
			if (command.equals("loadInterfacePlotClass")){
				String sub = line.substring(c_index + 1);
				int a = -1;
				String name = "";
				if (sub.startsWith("\""))
					a = sub.lastIndexOf("\"");
				if (a > 0)
					name = sub.substring(1, a);
				else
					name = sub.substring(0, sub.indexOf(" "));
				if (name.length() > 0){
					String cName = sub.substring(sub.lastIndexOf(" ") + 1);
					addInterfacePlotClass(name, cName);
					}
				}
			if (command.equals("loadPipelineProcessLibrary")){
				String sub = line.substring(c_index + 1);
				
				if (!loadPipelineProcessLibrary(sub))
					InterfaceSession.log("InterfaceEnvironment: error loading pipeline process library from '"
							+ line.substring(c_index + 1) + "'",
						LoggingType.Errors);
				}
			
			if (command.equals("loadPipelineProcessLibraries")){
				String sub = line.substring(c_index + 1);
				
				if (!loadPipelineProcessLibraries(sub))
					InterfaceSession.log("InterfaceEnvironment: error loading pipeline process libraries from '"
							+ line.substring(c_index + 1) + "'",
						LoggingType.Errors);
				}
			
			if (command.equals("loadIOType")){
				if (!loadIOType(line.substring(c_index + 1)))
					InterfaceSession.log("InterfaceEnvironment: error loading IO type '"
							+ line.substring(c_index + 1) + "'",
						LoggingType.Errors);
				}
			
			if (command.equals("loadVideoTaskType")){
				if (!loadVideoTaskType(line.substring(c_index + 1)))
					InterfaceSession.log("InterfaceEnvironment: error loading IO type '"
							+ line.substring(c_index + 1) + "'",
						LoggingType.Errors);
				}
			
			if (command.equals("addGraphicsWindow")){
				String sub = line.substring(c_index + 1);
				String type = sub.substring(0, sub.indexOf(" "));
				sub = sub.substring(sub.indexOf(" ") + 1);
				if (sub.startsWith("\"")) sub = sub.substring(1, sub.length() - 1);
				if (!addGraphicsWindow(type, sub))
					InterfaceSession.log("InterfaceEnvironment: Could not instantiate " +
									   "Graphics Window '" + type + "'");
				}
			
			if (command.equals("loadColourMaps")){
				File dir = null;
				java.net.URL url = null;
				try{
					String sub = line.substring(c_index + 1);
					if (!sub.contains("/")) sub = "/" + sub;
					//url = InterfaceEnvironment.class.getResource(sub);
					//dir = new File(url.toURI());
					dir = new File(getApplicationDataDir().getAbsolutePath() + File.separator + sub);
				}catch(Exception e){
					InterfaceSession.log("InterfaceEnvironment: error loading cmaps from '" + 
								url.toString() + "'",
							LoggingType.Errors);
					e.printStackTrace();
					}
		
				if (dir == null || !dir.exists() || !dir.isDirectory()){
					if (dir != null)
						InterfaceSession.log("InterfaceEnvironment: invalid cmap directory '" + 
								dir.getAbsolutePath() + "'",
							LoggingType.Errors);
				}else{
					String[] files = dir.list();
					for (int i = 0; i < files.length; i++)
						addColourMap(new File(dir + sep + files[i]));
					}
				
				}
			if (command.equals("loadNameMaps")){
				File dir = null;
				java.net.URL url = null;
				try{
					String sub = line.substring(c_index + 1);
					if (!sub.contains("/")) sub = "/" + sub;
					//url = InterfaceEnvironment.class.getResource(sub);
					//dir = new File(url.toURI());
					dir = new File(getApplicationDataDir().getAbsolutePath() + File.separator + sub);
					if (dir == null || !dir.exists() || !dir.isDirectory()){
						if (dir != null)
							InterfaceSession.log("InterfaceEnvironment: invalid nmap directory '" + 
									dir.getAbsolutePath() + "'",
								LoggingType.Errors);
					}else{
						String[] files = dir.list(new FilenameFilter(){

							@Override
							public boolean accept(File dir, String name) {
								return name.endsWith(".nmap");
							}
							
						});
						for (int i = 0; i < files.length; i++)
							addNameMap(new File(dir + sep + files[i]));
						}
				}catch(Exception e){
					InterfaceSession.log("InterfaceEnvironment: error loading name maps'",
							LoggingType.Errors);
					InterfaceSession.handleException(e);
					}
				}
			if (command.equals("loadGraphNodeShapes")){
				File dir = null;
				java.net.URL url = null;
				try{
					String sub = line.substring(c_index + 1);
					if (!sub.contains("/")) sub = "/" + sub;
					//url = InterfaceEnvironment.class.getResource(sub);
					//dir = new File(url.toURI());
					dir = new File(getApplicationDataDir().getAbsolutePath() + File.separator + sub);
				}catch(Exception e){
					InterfaceSession.log("InterfaceEnvironment: error loading graph node shapes from '" + 
								url.toString() + "'",
							LoggingType.Errors);
					e.printStackTrace();
					}
		
				if (dir == null || !dir.exists() || !dir.isDirectory()){
					if (dir != null)
						InterfaceSession.log("InterfaceEnvironment: invalid cmap directory '" + 
								dir.getAbsolutePath() + "'",
							LoggingType.Errors);
				}else{
					String[] files = dir.list();
					for (int i = 0; i < files.length; i++)
						addVertexShapes(new File(dir + sep + files[i]));
					}
				}
			if (command.equals("setLookAndFeel")){
				String s = line.substring(c_index + 1);
				if (s.length() > 0) look_and_feel = s;
				}
			}
		return true;
	}
	
	/**************************
	 * Initiate environment from the set init file, or the default init file if none is set. 
	 * Terminates execution if init file is not found.
	 * 
	 * @return <code>true</code> if no errors were encountered, <code>false</code> otherwise.
	 */
	public static boolean init(){
		
		colourMaps.add(ContinuousColourMap.DEFAULT_2);
		colourMaps.add(ContinuousColourMap.DEFAULT_3);
		colourMaps.add(ContinuousColourMap.GREY_SCALE);
		
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		//add meter unit
		//addUnit("meter", "spatial", "m 1.0");
		if (init_file == null){
			setInitFile("mgui.default.init");
			//java.net.URL init_url = InterfaceEnvironment.class.getResource("/init/mgui.default.init");
//			File dir = new File(System.getProperty("user.dir"));
//			init_file = new File(dir.getAbsolutePath() + File.separator + "mgui.default.init");
//			if (init_file != null && init_file.exists()){
////				try{
////					//File dir = new File(init_url.toURI());
////					init_file = new File(init_url.toURI());
////				}catch (Exception e){
////					//shouldn't get here..
////					InterfaceSession.log("Fatal error: Syntax error with default init file: '" + init_url + "'.");
////					e.printStackTrace();
////					System.exit(0);
////					}
//			}else{
//				InterfaceSession.log("Fatal error: Cannot find default init file...");
//				System.exit(0);
//				}
			}
		
		initOSType();
		
		try{
			String sep = File.separator;
			
			//set parallel output streams for System.out and System.err
			output_stream = new ParallelOutputStream();
			output_stream.addStream(System.out);
			System.setOut(new PrintStream(output_stream, true));
			error_stream = new ParallelOutputStream();
			error_stream.addStream(System.err);
			System.setErr(new PrintStream(error_stream, true));
			
			//initialize environment from init file
			//BufferedReader reader = new BufferedReader(new FileReader(init_file)); 
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(init_file), "UTF-8"));
			String line = reader.readLine();
			String path = "";
			if (current_dir == null)
				current_dir = new File(System.getProperty("user.dir"));
			if (current_dir != null)
				path = current_dir.getAbsolutePath() + sep;
			
			while (line != null){
				if (!handleLine(line)){
					reader.close();
					return false;
					}
				line = reader.readLine();
				}
			
			reader.close();
			
		}catch (IOException e){
			InterfaceSession.handleException(e, LoggingType.Debug);
			InterfaceSession.log("Error initializing ModelGUI environment from '" + 
								init_file.getAbsolutePath() + "'",
							LoggingType.Errors);
			return false;
			}
		
		is_init = true;
		return true;
	}
	
	public static void setLookAndFeel(String s){
		look_and_feel = s;
	}
	
	public static String getLookAndFeel(){
		return look_and_feel;
	}
	
	private static void setDataSourceKey(String key_urlstr){
		URL key_url = null;
		try{
			if (!key_urlstr.contains(":")){
				// This is a file
				if (key_urlstr.startsWith("/")){
					// Absolute address
					key_url = new URL("file:/" + key_urlstr);
				}else{
					// Relative address
					key_url = InterfaceEnvironment.class.getResource("/" + key_urlstr);
					}
			
		}else{
			key_url = new URL(key_urlstr);
			}
		
		if (!SecureDataSourceFunctions.setPrivateKey(key_url)){
			InterfaceSession.log("Could not set data source key from URL: " + key_urlstr, LoggingType.Errors);
			}
		
		}catch (MalformedURLException ex){
			InterfaceSession.log("Malformed URL for data source key: " + key_urlstr, LoggingType.Errors);
			}
	}
	
	/*******************************
	 * 
	 * Returns a named list of interface objects, probably belonging to a particular
	 * container and having the same type (although this is not necessarily true). 
	 * 
	 * <p>TODO: implement.
	 * 
	 * @param name
	 * @return
	 */
	public ArrayList<InterfaceObject> getInterfaceObjectList(String name){
		return null;
	}
	
	/********************************
	 * Attempts to lock the environment with the specified task. Once the environment locks,
	 * <code>isTaskLocked()</code> returns <code>true</code> and new tasks should not be run.
	 * 
	 * @param task name of the task to be run; should indicate class, function, and time started
	 * @return
	 */
	public boolean setLockTask(String task){
		if (isTaskLocked()) return false;
		locked_task = task;
		return true;
	}
	
	/********************************
	 * Unlocks the environment from its present task.
	 * 
	 */
	public void unsetTaskLock(){
		locked_task = null;
	}
	
	/*********************************
	 * Indicates whether the environment is locked with a task. If so, new tasks should not be run.
	 * 
	 * @return true if environment is locked; false otherwise.
	 */
	public boolean isTaskLocked(){
		return locked_task != null;
	}
	
	/*******************************
	 * Gets the system output stream.
	 * 
	 */
	public static ParallelOutputStream getSystemOutputStream(){
		return output_stream;
	}
	
	/*******************************
	 * Gets the system input stream.
	 * 
	 */
	public static ParallelOutputStream getSystemErrorStream(){
		return error_stream;
	}
	
	/*******************************
	 * Sets the locale of this environment. 
	 * 
	 * @param newLocale
	 */
	public static void setLocale(Locale newLocale){
		locale = newLocale;
	}
	
	/*******************************
	 * Sets the locale of this environment from language and country. 
	 * 
	 * @param newLocale
	 */
	static void setLocale(String language, String country){
		locale = new Locale(language, country);
	}
	
	/*******************************
	 * Gets the locale.
	 * 
	 */
	public static Locale getLocale(){
		return locale;
	}
	
	public static void setInitFile(String file){
		if (!file.startsWith("/"))
			file = "/init/" + file;
		java.net.URL init_url = InterfaceEnvironment.class.getResource(file);
		
		
		if (init_url != null){
			try{
				if (init_url.toString().startsWith("jar:")){
					// Remove jar from the protocol...
					String url_str = init_url.toString();
					url_str = url_str.substring(4,url_str.length());
					init_url = new URL(url_str);
					}
				init_file = new File(init_url.toURI());
			}catch (Exception e){
				//shouldn't get here..
				InterfaceSession.log("Fatal error: Syntax error with default init file: '" + init_url + "'.");
				e.printStackTrace();
				System.exit(0);
				}
		}else{
			// Try current directory
			File dir = new File(System.getProperty("user.dir"));
			init_file = new File(dir.getAbsolutePath() + File.separator + file);
			if (!init_file.exists()){
				InterfaceSession.log("Fatal error: Could not find init file: '" + init_file.getAbsolutePath() + "'.");
				System.exit(0);
				}
			}
	}
	
	public static File getInitFile(){
		return init_file;
	}
	
	/*****************************
	 * Add a unit of a specific type to this environment. Type must be a recognized unit instance
	 * (currently only <code>ScaledUnit</code> and <code>SpatialUnit</code> are recognized).
	 * 
	 * @param unit
	 */
	static boolean addUnit(String name, String type, String params){
		Unit unit = null;
		try{
		if (type.toLowerCase().equals("scaled")){
			StringTokenizer tokens = new StringTokenizer(params);
			String short_name = parseUnicode(tokens.nextToken());
			double factor = Double.valueOf(tokens.nextToken());  
			unit = new ScaledUnit(name, short_name, factor);
		}else if (type.toLowerCase().equals("spatial")){
			StringTokenizer tokens = new StringTokenizer(params);
			String short_name = parseUnicode(tokens.nextToken());
			double factor = Double.valueOf(tokens.nextToken());  
			unit = new SpatialUnit(name, short_name, factor);
			if (default_spatial_unit == null)
				setDefaultSpatialUnit(name);
			}
		}catch (Exception e){
			InterfaceSession.log("Error adding unit '" + name + "'..");
			}
		if (unit == null) return false;
		
		units.put(unit.getName(), unit);
		return true;
	}
	
	public static void setDefaultSpatialUnit(String name){
		default_spatial_unit = name;
	}
	
	public static SpatialUnit getDefaultSpatialUnit(){
		if (default_spatial_unit == null) return null;
		return getSpatialUnit(default_spatial_unit);
	}
	
	static String parseUnicode(String string){
		return string;
	}
	
	public static ArrayList<Unit> getAllUnits(){
		ArrayList<Unit> list = new ArrayList<Unit>(units.values());
		Collections.sort(list);
		unit_list.clear();
		unit_list.addAll(list);
		return unit_list; 
	}
	
	public static ArrayList<SpatialUnit> getSpatialUnits(){
		ArrayList<Unit> list = new ArrayList<Unit>(units.values());
		ArrayList<SpatialUnit> spatial_list = new ArrayList<SpatialUnit>();
		
		for (int i = 0; i < list.size(); i++)
			if (list.get(i) instanceof SpatialUnit)
				spatial_list.add((SpatialUnit)list.get(i));
		
		Collections.sort(spatial_list);
		spatial_unit_list.clear();
		spatial_unit_list.addAll(spatial_list);
		return spatial_unit_list; 
	}
	
	public static Unit getUnit(String name){
		return units.get(name);
	}
	
	/*******************************************
	 * Returns the spatial unit with the specified name, or {@code null} if not spatial unit exists
	 * by that name.
	 * 
	 * @param name
	 * @return
	 */
	public static SpatialUnit getSpatialUnit(String name){
		Unit unit = units.get(name);
		if (unit == null || !(unit instanceof SpatialUnit)) return null;
		return (SpatialUnit)unit;
	}
	
	/*******************************
	 * Add new graphics window to this environment. 
	 * 
	 * @param cm
	 * @return <code>true</code> if successful.
	 */
	static boolean addGraphicsWindow(String type, String name){
		InterfaceGraphic p = getInterfaceGraphicInstance(type);
		if (p == null) return false;
		InterfaceGraphicWindow g = new InterfaceGraphicWindow(p);
		g.setName(name);
		graphicsWindows.add(g);
		return true;
	}
	
	/*******************************
	 * Load new continuous colour map into the environment from the specified file. 
	 * 
	 * @param cm
	 * @return <code>true</code> if successful.
	 */
	static boolean addContinuousColourMap(String file, String name){
		File f = new File(file);
		if (!f.exists()) return false;
		try{
			ContinuousColourMapLoader loader = new ContinuousColourMapLoader(f);
			ContinuousColourMap map = loader.loadMap();
			map.name = name;
			colourMaps.add(map);
			Collections.sort(colourMaps, new Comparator<ColourMap>(){
				public int compare(ColourMap m1, ColourMap m2){
					return m1.getName().compareTo(m2.getName());
				}
			});
			return true;
		}catch (IOException e){
			e.printStackTrace();
			return false;
			}
		
	}
	
	/*******************************
	 * Load new discrete colour map into the environment from the specified file. 
	 * 
	 * @param cm
	 * @return <code>true</code> if successful.
	 */
	static boolean addDiscreteColourMap(String file, String name){
		File f = new File(file);
		if (!f.exists()) return false;
		try{
			DiscreteColourMapLoader loader = new DiscreteColourMapLoader(f);
			DiscreteColourMap map = loader.loadMap();
			map.name = name;
			colourMaps.add(map);
			Collections.sort(colourMaps, new Comparator<ColourMap>(){
				public int compare(ColourMap m1, ColourMap m2){
					return m1.getName().compareTo(m2.getName());
				}
			});
			return true;
		}catch (IOException e){
			e.printStackTrace();
			return false;
			}
	}
	
	/*******************************
	 * Load new colour map into the environment from the specified file. 
	 * 
	 * @param cm
	 * @return <code>true</code> if successful.
	 */
	public static boolean addColourMap(File file){
		
		try{
			ColourMapLoader loader = new ColourMapLoader(file);
			ColourMap map = loader.loadMap();
			if (map != null)
				return addColourMap(map);
		}catch (IOException e){
			e.printStackTrace();
			}
		
		return false;
	}
	
	/*******************************
	 * Add new colour map to the environment. 
	 * 
	 * @param cm
	 * @return
	 */
	public static boolean addColourMap(ColourMap cm){
		colourMaps.add(cm);
		Collections.sort(colourMaps, new Comparator<ColourMap>(){
			public int compare(ColourMap m1, ColourMap m2){
				return m1.getName().compareTo(m2.getName());
			}
		});
		if (cm instanceof DiscreteColourMap){
			DiscreteColourMap dm = (DiscreteColourMap)cm;
			if (dm.hasNameMap())
				addNameMap(dm.nameMap);
			}
			
		return true;
	}
	
	/***************************
	 * Returns all loaded graphics windows.
	 * 
	 * @return all loaded graphics windows
	 * @deprecated
	 */
	public static ArrayList<InterfaceGraphicWindow> getGraphicsWindows(){
		return graphicsWindows;
	}
	
	/*******************************
	 * Gets a list of currently loaded colour maps. List is a direct link, since components will
	 * want to access an updated version. 
	 * 
	 * <p>TODO: This presents concurrency issues; address
	 * 
	 * @return The list of colour maps
	 */
	public static ArrayList<ColourMap> getColourMaps(){
		return colourMaps;
	}
	
	/*******************************
	 * Gets a the colour map with the name {@code name}. List is a direct link, since components will
	 * want to access an updated version.
	 * 
	 * <p>TODO: This presents concurrency issues; address
	 * 
	 * @param name Name of the colour map to return
	 * @return the corresponding colour map, or {@code null} if none were found
	 */
	public static ColourMap getColourMap(String name){
		for (int i = 0; i < colourMaps.size(); i++)
			if (colourMaps.get(i).getName().equals(name))
				return colourMaps.get(i);
		return null;
	}
	
	/***************************
	 * Returns all loaded continuous colour maps.
	 * 
	 * @return all loaded continuous colour maps
	 */
	public static ArrayList<ContinuousColourMap> getContinuousColourMaps(){
		ArrayList<ContinuousColourMap> maps = new ArrayList<ContinuousColourMap>();
		for (int i = 0; i < colourMaps.size(); i++)
			if (colourMaps.get(i) instanceof ContinuousColourMap)
				maps.add((ContinuousColourMap)colourMaps.get(i));
		return maps;
	}
	
	/***************************
	 * Returns all loaded discrete colour maps.
	 * 
	 * @return all loaded discrete colour maps
	 */
	public static ArrayList<DiscreteColourMap> getDiscreteColourMaps(){
		ArrayList<DiscreteColourMap> maps = new ArrayList<DiscreteColourMap>();
		for (int i = 0; i < colourMaps.size(); i++)
			if (colourMaps.get(i) instanceof DiscreteColourMap)
				maps.add((DiscreteColourMap)colourMaps.get(i));
		return maps;
	}
	
	/***************************
	 * Adds a new name map to the environment.
	 * 
	 * @param map
	 */
	public static void addNameMap(NameMap map){
		for (int i = 0; i < nameMaps.size(); i++)
			if (nameMaps.get(i).getName().equals(map.getName())){
				nameMaps.remove(i);
				break;
				}
		nameMaps.add(map);
	}
	
	/***************************
	 * Loads and adds a new name map to the environment.
	 * 
	 * @param map
	 */
	public static boolean addNameMap(File file){
		try{
			NameMapLoader loader = new NameMapLoader(file);
			NameMap map = loader.loadNameMap();
			if (map == null) return false;
			addNameMap(map);
			return true;
		}catch(Exception ex){
			InterfaceSession.handleException(ex);
			return false;
			}
	}
	
	/***************************
	 * Removes a name map from the environment.
	 * 
	 */
	public static void removeNameMap(NameMap map){
		nameMaps.remove(map);
	}
	
	/****************************
	 * Gets a list of loaded name maps.
	 * 
	 * @return
	 */
	public static ArrayList<NameMap> getNameMaps(){
		return nameMaps;
	}
	
	/****************************
	 * Gets the NameMap with the specified name
	 * 
	 * @return
	 */
	public static NameMap getNameMap(String name){
		for (int i = 0; i < nameMaps.size(); i++)
			if (nameMaps.get(i).getName().equals(name))
				return nameMaps.get(i);
		return null;
	}
	
	public static boolean addInterfacePanelClass(String name, String full){
		try{
			Class<? extends InterfacePanel> c = 
				(Class<? extends InterfacePanel>)interfacePanelLoader.loadClass(full);
			if (c == null) return false;
			interfacePanelClasses.put(name, c);
			return true;
		}catch (ClassCastException e){
			InterfaceSession.log("InterfaceEnvironment: " + full + 
								 " is not an instance of InterfacePanel", 
								 LoggingType.Errors);
			return false;
			}
	}
	
	public static boolean addInterfacePlotClass(String name, String full){
		try{
			Class<? extends InterfacePlot<?>> c = 
				(Class<? extends InterfacePlot<?>>)interfacePlotLoader.loadClass(full);
			if (c == null) return false;
			interfacePlotClasses.put(name, c);
			return true;
		}catch (ClassCastException e){
			InterfaceSession.log("InterfaceEnvironment: " + full + 
								 " is not an instance of InterfacePlot", 
								 LoggingType.Errors);
			return false;
			}
	}
	
	public static boolean addInterfaceGraphicClass(String name, String full){
		try{
			Class<? extends InterfaceGraphic<?>> c = 
				(Class<? extends InterfaceGraphic<?>>)interfaceGraphicLoader.loadClass(full);
			if (c == null) return false;
			interfaceGraphicClasses.put(name, c);
			return true;
		}catch (ClassCastException e){
			InterfaceSession.log("InterfaceEnvironment: " + full + 
								 " is not an instance of InterfaceGraphic", 
								 LoggingType.Errors);
			return false;
			}
	}
	
	/*******************************************************
	 * Returns the name corresponding to the given interface graphic class.
	 * 
	 * @param _class
	 * @return The name, or {@code null} if no such class is installed
	 */
	public static String getNameForInterfaceGraphicClass(Class<? extends InterfaceGraphic> _class){
		
		ArrayList<String> keys = new ArrayList<String>(interfaceGraphicClasses.keySet());
		for (int i = 0; i < keys.size(); i++)
			if (interfaceGraphicClasses.get(keys.get(i)).equals(_class))
				return keys.get(i);
		
		return null;
		
	}
	
	/*************************************************
	 * Returns a list of the currently loaded data source drivers.
	 * 
	 * @return
	 */
	public static ArrayList<String> getDataSourceDriverNames(){
		ArrayList<String> names = new ArrayList<String>(); 
		Iterator<String> itr = dataSourceDrivers.keySet().iterator();
		while (itr.hasNext())
			names.add(itr.next());
		return names;
	}
	
	public static DataSourceDriver getDataSourceDriver(String name){
		return dataSourceDrivers.get(name);
	}
	
	public static DataSourceDriver getDataSourceDriverByClass(String className){
		Iterator<DataSourceDriver> itr = dataSourceDrivers.values().iterator();
		
		while (itr.hasNext()){
			DataSourceDriver driver = itr.next();
			if (driver.getClassName().equals(className)) return driver;
			}	
		
		return null;
	}
	
	/************************************************
	 * Returns a dialog panel for the indicated driver, if one exists. Otherwise
	 * returns <code>null</code>.
	 * 
	 * @param name Name of the driver for which to find a dialog panel
	 */
//	public static DataSourceDialogPanel getDataSourceDialogPanel(String name){
//		Class<?> dialog_class = interfaceDataSourceDialogPanelClasses.get(name);
//		if (dialog_class == null)
//			dialog_class = DataSourceDialogPanel.class;
//		try{
//			DataSourceDialogPanel panel = (DataSourceDialogPanel)dialog_class.newInstance();
//			if (panel instanceof DataSourceListDialogPanel){
//				DataSourceListDialogPanel list_panel = (DataSourceListDialogPanel)panel;
//				list_panel.setDataSourceDriver(name);
//				}
//			return panel;
//			//return (DataSourceDialogPanel)dialog_class.newInstance();
//		}catch (Exception e){
//			//e.printStackTrace();
//			InterfaceSession.log("InterfaceEnvironment: Error creating the dialog panel for '" + name + "'.", LoggingType.Errors);
//			return null;
//			}
//	}
	
	public static HashMap<String, VideoTaskType> getVideoTaskTypes(){
		return videoTaskTypes;
	}
	
	public static VideoTaskType getVideoTaskType(Class c){
		Iterator<VideoTaskType> itr = videoTaskTypes.values().iterator();
		
		while (itr.hasNext()){
			VideoTaskType type = itr.next();
			if (type.task_class.equals(c)) return type;
			}
		
		return null;
	}
	
	public static void addVideoTaskType(String name, VideoTaskType type){
		videoTaskTypes.put(name, type);
	}
	
	/************************
	 * Fills a combo panel with new instances of all loaded InterfacePanel classes
	 * 
	 * @param panel
	 * @return
	 */
	public static boolean setComboPanel(InterfaceComboPanel panel, 
										InterfaceDisplayPanel dp, 
										boolean sort){
		ArrayList<String> names = getInterfacePanelNames();
		
		if (sort) Collections.sort(names);
		boolean success = true;
		for (int i = 0; i < names.size(); i++){
			InterfacePanel p = getInterfacePanelInstance(names.get(i));
			if (p != null){
				//p.setDisplayPanel(dp);
				panel.addPanel(p);
				if (p instanceof DisplayPanelListener)
					dp.addDisplayPanelListener((DisplayPanelListener)p);
			}else{
				success = false;
				}
			}
		return success;
	}
	
	/*****************************
	 * Looks for format:
	 * <p><code>#addDataSourceDriver name_without_spaces driver_class [user] [login]</code></p>
	 *
	 * @param params
	 * @return
	 */
	public static boolean addDataSourceDriver(String params){
		StringTokenizer tokens = new StringTokenizer(params);
		String token = tokens.nextToken();
		String name = "";
		if (token == null) return false;
		name = token;
		token = tokens.nextToken();
		if (token == null) return false;
		String driver = token;
		token = tokens.nextToken();
		if (token == null) return false;
		String url = token;
		
		if (!tokens.hasMoreTokens()){
			dataSourceDrivers.put(name, new DataSourceDriver(name, driver, url));
			return true;
			}
		token = tokens.nextToken();
		String user = token;
		token = tokens.nextToken();
		String password = "";
		if (token != null)
			password = token;
		dataSourceDrivers.put(name, new DataSourceDriver(name, driver, url, user, password));
		return true;
	}
	
	/*********************************
	 * Add the JDBC driver {@code driver} to the environment. Fails if a driver of the same name already exists.
	 * 
	 * @param driver
	 * @return
	 */
	public static boolean addDataSourceDriver(DataSourceDriver driver){
		return addDataSourceDriver(driver, null);
	}
	
	/*********************************
	 * Add the JDBC driver {@code driver} to the environment. Fails if a driver of the same name already exists.
	 * 
	 * @param driver
	 * @return
	 */
	public static boolean addDataSourceDriver(DataSourceDriver driver, Class<? extends DataSourceDialogPanel> panel_class){
		if (dataSourceDrivers.containsKey(driver.getName()))
			return false;
		dataSourceDrivers.put(driver.getName(), driver);
//		if (panel_class == null) return true;
//		interfaceDataSourceDialogPanelClasses.put(driver.getName(), panel_class);
		return true;
	}
	
	/*****************************
	 * Adds all drivers in the data_sources directory.
	 *
	 * @param params
	 * @return
	 */
	static boolean addKnownDataSourceDrivers(){
		//java.net.URL log_url = InterfaceEnvironment.class.getResource("/data_sources");
		File db_dir = new File(getApplicationDataDir().getAbsolutePath() + File.separator + "data_sources");
		
		if (db_dir == null || !db_dir.exists()){
			InterfaceSession.log("InterfaceEnvironment: Error loading data sources. Directory '" + 
								 db_dir.getAbsolutePath() + "' does not exist.",
								 LoggingType.Errors);
			return false;
			}
		
		String[] drivers = db_dir.list(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".driver");
			}
		});
		
		boolean success = true;
		for (int i = 0; i < drivers.length; i++){
			success &= addKnownDataSourceDriver(drivers[i]);
			}
		
		return success;
	}
	
	/*****************************
	 * Looks for format:
	 * <p><code>#addKnownDataSourceDriver DataSourceDriver_subclass_name url
	 * 		name [user] [login]</code></p>
	 *
	 * @param params
	 * @return
	 */
	static boolean addKnownDataSourceDriver(String param_file){
		
		String params = null;
		try{
			File db_dir = new File(getApplicationDataDir().getAbsolutePath() + File.separator + "data_sources");
			
			
			File file =  new File(db_dir.getAbsolutePath() + File.separator + param_file);
			if (!file.exists()){
				InterfaceSession.log("InterfaceEnvironment: error loading data source" + 
						" init file '" + file.getAbsolutePath() + "': file not found.",
						LoggingType.Errors);
				return false;
				}
			BufferedReader reader = new BufferedReader(new FileReader(file));
			params = reader.readLine();
			
			QuotedStringTokenizer tokens = new QuotedStringTokenizer(params, " ");
			String token = tokens.nextToken();
			String className = "";
			if (token == null) return false;
		
			className = token;
			DataSourceDriverLoader loader = new DataSourceDriverLoader();
			Class c = loader.loadClass(className);
			DataSourceDriver driver = (DataSourceDriver)c.newInstance();
			token = tokens.nextToken();
			if (token == null) return false;
			driver.setUrl(token);
			token = tokens.nextToken();
			if (token == null) return false;
			driver.setName(token);
			driver.setUrlReference(file.toURI().toURL());
			
			driver.init();
//			if (dialog_panel_class != null){
//				Class<?> panel_class = interfaceDataSourceDialogLoader.loadClass(dialog_panel_class);
//				interfaceDataSourceDialogPanelClasses.put(driver.getName(), panel_class);
//				}
			if (!tokens.hasMoreTokens()){
				dataSourceDrivers.put(driver.getName(), driver);
				return true;
				}
			token = tokens.nextToken();
			driver.setLogin(token);
			if (tokens.hasMoreTokens()) driver.setPassword(tokens.nextToken());
			dataSourceDrivers.put(driver.getName(), driver);
			return true;
			
		}catch (Exception e){
			InterfaceSession.log("InterfaceEnvironment: Could not load JDBC driver from line '" + params + "'.\n" +
							 	 "Details: " + e.getMessage(), 
							 	 LoggingType.Errors);
			return false;
		}
	}
	
	/*******************************************
	 * Removes the specified data source driver from the environment.
	 * 
	 * @param name
	 * @return {@code true} if data source driver was removed
	 */
	public static boolean removeDataSourceDriver(String name){
		if (!dataSourceDrivers.containsKey(name)) return false;
		dataSourceDrivers.remove(name);
		return true;
	}
	
	/*****************************
	 * Looks for format:
	 * <p><code>#loadIOType name_without_spaces dialog_class options_class "Name for filter" ext1 [ext2] [ext3]</code></p>
	 * <br>Use "null" to indicate that no dialog or options class or file ext filter is specified
	 * <br>Filter name must be wrapped with double quotes; no quotes anywhere else 
	 * @param params
	 * @return
	 */
	static boolean loadIOType(String params){
		StringTokenizer tokens = new StringTokenizer(params);
		String token = tokens.nextToken();
		String name = "";
		if (token == null) return false;
		InterfaceIOType type = new InterfaceIOType(); 
		type.setName( token);
		name = token;
		token = tokens.nextToken();
		if (token == null) return false;
		type.setType(token);
		token = tokens.nextToken();
		if (token == null) return false;
		if (token.equals("null")) return false;
		type.setIO(interfaceIOLoader.loadClass(token));
		token = tokens.nextToken();
		if (token == null) return false;
		if (!token.equals("null"))
			type.setDialog(interfaceDialogLoader.loadClass(token));
		token = tokens.nextToken();
		if (token == null) return false;
		if (!token.equals("null"))
			type.setOptions(interfaceOptionsLoader.loadClass(token));
		
		boolean setFilter = true;
		int a = params.indexOf("\"");
		setFilter &= (a > 0);
		int b = params.lastIndexOf("\"");
		setFilter &= (b > a);
		
		type.setLabel(name);
		
		if (setFilter){
			String filter = params.substring(a + 1, b);
			type.setLabel(filter);
			if (params.length() > b + 2){
				tokens = new StringTokenizer(params.substring(b + 2));
				String[] ext = new String[tokens.countTokens()];
				
				a = 0;
				while (tokens.hasMoreTokens()) {
					String exti = tokens.nextToken();
					ext[a++] = "*." + exti;
					type.addExtension(exti);
					}
				
				type.setFilter(new WildcardFileFilter(ext, filter));
						
			}else{
				type.setFilter(new WildcardFileFilter("*.*", filter));
				}
			}
		
		ioTypes.put(name, type);
		return true;
		
	}
	
	
	static boolean loadVideoTaskType(String params){
		
		StringTokenizer tokens = new StringTokenizer(params);
		String token = tokens.nextToken();
		String name = "";
		if (token == null) return false;
		
		VideoTaskType type = new VideoTaskType();
		
		type.name = token;
		name = token;
		token = tokens.nextToken();
		if (token == null) return false;
		if (token.equals("null")) return false;
		type.task_class = videoTaskLoader.loadClass(token);
		token = tokens.nextToken();
		if (token == null) return false;
		if (!token.equals("null"))
			type.dialog_class = dialogPanelLoader.loadClass(token);
		
		videoTaskTypes.put(name, type);
		
		return true;
	}
	
	/********************
	 * Returns a list of the data sources loaded at init
	 * 
	 * @return
	 */
	public static ArrayList<DataSource> getDataSources(){
		return dataSources;
	}
	
	/********************
	 * Returns a list of the IO types loaded at init
	 * 
	 * @return
	 */
	public static HashMap<String, InterfaceIOType> getIOTypes(){
		return ioTypes;
	}
	
	/********************
	 * Returns a the IO types associated with <code>name</code>
	 * 
	 * @param name
	 * 
	 * @return the type, if one exists; <code>null</code> otherwise.
	 */
	public static InterfaceIOType getIOType(String name){
		return ioTypes.get(name);
	}
	
	
	/********************
	 * Return the I/O type associated with the specific instance of {@link InterfaceIO}. 
	 * 
	 * @param instance
	 * @return
	 */
	public static InterfaceIOType getIOTypeForInstance(InterfaceIO instance){
		if (instance == null) return null;
		ArrayList<InterfaceIOType> types = new ArrayList<InterfaceIOType>(ioTypes.values());
		for (int i = 0; i < types.size(); i++)
			if (types.get(i).getIO().equals(instance.getClass())) return types.get(i);
		return null;
	}
	
	/********************
	 * Return all I/O types which are assignable from <code>clazz</code>. 
	 * 
	 * @param clazz
	 * @return
	 */
	public static ArrayList<InterfaceIOType> getIOTypesForClass(Class<?> clazz){
		ArrayList<InterfaceIOType> types = new ArrayList<InterfaceIOType>(ioTypes.values());
		ArrayList<InterfaceIOType> list = new ArrayList<InterfaceIOType>();
		for (int i = 0; i < types.size(); i++)
			if (clazz.isAssignableFrom(types.get(i).getIO())) 
				list.add(types.get(i));
		return list;
	}
	
	public static String getXMLHeader(){
		return xml_header;
	}
	
	/***********************************
	 * Load all data sources in a given directory
	 * 
	 * @param directory
	 * @return
	 */
	private static boolean loadDataSources(String directory){
		
//		if (!directory.contains("/")) directory = "/" + directory;
//		java.net.URL url = InterfaceEnvironment.class.getResource(directory);
//		if (url == null) return false;
//		
//		File ds_dir = null;
//		try{
//			ds_dir = new File(url.toURI());
//		}catch (Exception e){
//			
//			return false;
//			}
		File ds_dir = new File(getApplicationDataDir().getAbsolutePath() + File.separator + directory);
		
		if (ds_dir == null || !ds_dir.exists()) return false;
		
		String[] files = ds_dir.list();
		boolean success = true;
		
		for (int i = 0; i < files.length; i++){
			if (files[i].endsWith(".src"))
				success &= loadDataSource(ds_dir.getAbsolutePath() + File.separator + files[i]);
			}
		
		return success;
		
	}
	
	/***********************************
	 * Load a data source from file.
	 * 
	 * @param file
	 * @return
	 */
	private static boolean loadDataSource(String file_name){
		
			//DataConnection dc = new DataConnection();
			
//			java.net.URL url = InterfaceEnvironment.class.getResource(file);
//			if (url == null){
//				InterfaceSession.log("InterfaceEnvironment: error loading data source" + 
//						" init file '" + file + "'",
//					LoggingType.Errors);
//				return false;
//				}
		try{
			File file =  new File(file_name);
			DataSourceLoader loader = new DataSourceLoader(file);
			DataSource source = (DataSource)loader.loadObject();
			
			if (source != null)
				dataSources.add(source);
			
		}catch(Exception e){
			InterfaceSession.log("InterfaceEnvironment: error loading data source from file '" + file_name + "'",
				LoggingType.Errors);
			e.printStackTrace();
			return false;
			}
		
		return true;
		
	}
	
	/*******************************
	 * Adds a graph node shapes to the environment from file.
	 * 
	 * @param shape3d
	 * @return
	 */
	private static boolean addVertexShapes(File file){
		
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			
			boolean success = true;
			
			while (line != null){
				if (!line.startsWith("#")){
					NodeShape shape = GraphFunctions.createNodeShape(line);
					if (shape != null){
						String name = line.substring(0, line.indexOf(" "));
						success &= addVertexShape(name, shape);
						}
					}
				line = reader.readLine();
				}
			
			reader.close();
			return success;
		}catch (IOException e){
			InterfaceSession.log("InterfaceEnvironment: Could not load graph node shapes from '" +
								 file.getAbsolutePath() + "'.", 
								 LoggingType.Errors);
			}
		
		return false;
	}
	
	/*******************************
	 * Adds a graph node shape to the environment.
	 * 
	 * @param shape
	 * @return
	 */
	private static boolean addVertexShape(String name, NodeShape shape){
		
		vertexShapes.put(name, shape);
		return true;
		
	}
	
	/****************************************
	 * Returns a map of vertex shapes.
	 * 
	 * @return
	 */
	public static HashMap<String,NodeShape> getVertexShapes(){
		return vertexShapes;
	}
	
	/*************************
	 * Load all libraries in a given directory.
	 * 
	 * @param directory
	 * @return
	 */
	private static boolean loadPipelineProcessLibraries(String directory){
		
		//if (!directory.contains(File.separator)) directory = "/" + directory;
		//java.net.URL url = InterfaceEnvironment.class.getResource(directory);
		File lib_dir = new File(getApplicationDataDir().getAbsolutePath() + File.separator + directory);
		
		if (lib_dir == null || !lib_dir.exists()) return false;
		
		String[] files = lib_dir.list();
		boolean success = true;
		
		for (int i = 0; i < files.length; i++){
			if (files[i].endsWith(".proclib"))
				success &= loadPipelineProcessLibrary(lib_dir.getAbsolutePath() + File.separator + files[i]);
			}
		
		return success;
		
	}
	
	/***************************
	 * Load a pipeline process library from an XML file.
	 * 
	 * @param file_name
	 * @return
	 */
	private static boolean loadPipelineProcessLibrary(String file_name){
		
//		java.net.URL url = InterfaceEnvironment.class.getResource(file_name);
//		if (url == null) return false;
//		File file = null;
//		
//		try{
//			file = new File(url.toURI());
//		}catch(Exception e){
//			InterfaceSession.log("InterfaceEnvironment: error pipeline processes from '" + url.toString() + "'");			
//			//e.printStackTrace();
//			return false;
//			}
		File file = new File(file_name);
		
		if (!file.exists()){
			InterfaceSession.log("InterfaceEnvironment: pipeline processes file '" + file_name + "' does not exist..");
			return false;
			}
		
		PipelineProcessLibraryLoader loader = new PipelineProcessLibraryLoader(file);
		PipelineProcessLibrary library = loader.loadLibrary();
		if (library == null){
			InterfaceSession.log("InterfaceEnvironment: Error loading pipeline processes library '" + file_name + "'");
			return false;
			}
		registerPipelineProcessLibrary(library);
		return true;
		
//		try{
//			XMLReader reader = XMLReaderFactory.createXMLReader();
//			PipelineProcessLibraryXMLHandler handler = new PipelineProcessLibraryXMLHandler();
//			reader.setContentHandler(handler);
//			reader.setErrorHandler(handler);
//			reader.parse(new InputSource(new FileReader(file)));
//			
//			PipelineProcessLibrary library = new PipelineProcessLibrary(handler.library_name);
//			
//			for (int i = 0; i < handler.processes.size(); i++)
//				library.addProcess(handler.processes.get(i));
//				//registerPipelineProcess(handler.processes.get(i));
//			
//			registerPipelineProcessLibrary(library);
//			
//			return true;
//		}catch (Exception e){
//			//e.printStackTrace();
//			InterfaceSession.handleException(e);
//			return false;
//			}
		
	}
	
	/********************************
	 * Gets a list of all registered pipeline process libraries.
	 * 
	 * @return list of all registered pipeline processes
	 */
	public static ArrayList<PipelineProcessLibrary> getPipelineProcessLibraries(){
		return new ArrayList<PipelineProcessLibrary>(process_libraries.values());
	}
	
	/********************************
	 * Returns the pipeline process library with the given name. Returns <code>null</code> if no such
	 * library exists in this environment.
	 * 
	 * @param name
	 * @return
	 */
	public static PipelineProcessLibrary getPipelineProcessLibrary(String name){
		return process_libraries.get(name);
	}
	
	/********************************
	 * Adds pipeline process library to this environment.
	 * 
	 * @param name
	 * @return
	 */
	public static boolean addPipelineProcessLibrary(PipelineProcessLibrary library){
		if (process_libraries.get(library.getName()) != null) return false;
		process_libraries.put(library.getName(), library);
		return true;
	}
	
	/********************************
	 * Returns a list of processes contained in the given library.
	 * 
	 * @param library_name
	 * @return
	 */
	public static ArrayList<PipelineProcess> getProcesses(String library_name){
		PipelineProcessLibrary library = process_libraries.get(library_name);
		if (library == null) return null;
		return library.getProcesses();
	}
	
	/********************************
	 * Gets a specific pipeline process with the compound name <code>compound_name</code>, of the form
	 * [library_name].[process_name].
	 * 
	 * @param compound_name
	 * @return pipeline process with the name <code>process_name</code>
	 */
	public static PipelineProcess getPipelineProcess(String compound_name){
		
		String[] names = compound_name.split("\\.");
		if (names.length != 2) return null;
		return getPipelineProcess(names[0], names[1]);
		
	}
	
	/********************************
	 * Gets a specific pipeline process with the name <code>process_name</code> from the library
	 * <code>library_name</code>.
	 * 
	 * @param library_name
	 * @param process_name
	 * @return pipeline process with the name <code>process_name</code>
	 */
	public static PipelineProcess getPipelineProcess(String library_name, String process_name){
		PipelineProcessLibrary library = process_libraries.get(library_name);
		if (library == null) return null;
		return library.getProcess(process_name);
	}
	
	/*********************************
	 * Registers a new pipeline process library.
	 * 
	 * @param process process to register
	 */
	public static void registerPipelineProcessLibrary(PipelineProcessLibrary library){
		process_libraries.put(library.getName(), library);
	}
	
	/********************************
	 * Deregisters the given library. 
	 * 
	 * @param library_name
	 * @return <code>true</code> if removed; <code>false</code> if library is not registered with this
	 * environment.
	 */
	public static boolean deregisterPipelineProcessLibrary(String library_name){
		PipelineProcessLibrary library = getPipelineProcessLibrary(library_name);
		if (library == null) return false;
		process_libraries.remove(library);
		return true;
	}
	
	/*****************************
	 * Creates an instance of the specified interface panel.
	 * 
	 * @param name
	 * @return new instance of specified panel, or <code>null</code> if not found.
	 */
	public static InterfacePanel getInterfacePanelInstance(String name){
		Class<? extends InterfacePanel> c = interfacePanelClasses.get(name);
		if (c == null) 
			return null;
		try{
			InterfacePanel p = (InterfacePanel)c.newInstance();
			p.setType(name);
			p.setName(name);
			return p;
		}catch (Exception e){
			e.printStackTrace();
			}
		return null;
	}
	
	/*****************************
	 * Creates an instance of the specified graphic panel.
	 * 
	 * @param name
	 * @return new instance of specified panel, or <code>null</code> if not found.
	 */
	public static InterfaceGraphic<?> getInterfaceGraphicInstance(String name){
		Class<? extends InterfaceGraphic<?>> c = interfaceGraphicClasses.get(name);
		if (c == null) 
			return null;
		try{
			InterfaceGraphic<?> p = (InterfaceGraphic<?>)c.newInstance();
			p.setType(name);
			p.setName(name);
			return p;
		}catch (Exception e){
			e.printStackTrace();
			}
		return null;
	}
	
	/*****************************
	 * Creates an instance of the specified plot panel.
	 * 
	 * @param name
	 * @return new instance of specified plot, or <code>null</code> if not found.
	 */
	public static InterfacePlot<?> getInterfacePlotInstance(String name){
		Class<? extends InterfacePlot<?>> c = interfacePlotClasses.get(name);
		if (c == null) 
			return null;
		try{
			InterfacePlot<?> p = (InterfacePlot<?>)c.newInstance();
			p.setType(name);
			p.setName(name);
			return p;
		}catch (Exception e){
			e.printStackTrace();
			}
		return null;
	}
	
	/******************************************
	 * Returns a list of all {@link InterfacePanel} classes currently loaded in this
	 * environment.
	 * 
	 * @return
	 */
	public static ArrayList<String> getInterfacePanelNames(){
		ArrayList<String> names = new ArrayList<String>(interfacePanelClasses.keySet());
		Collections.sort(names);
		return names;
	}
	
	/******************************************
	 * Returns a list of all {@link InterfaceGraphic} classes currently loaded in this
	 * environment.
	 * 
	 * @return
	 */
	public static ArrayList<String> getInterfaceGraphicNames(){
		ArrayList<String> names = new ArrayList<String>(interfaceGraphicClasses.keySet());
		Collections.sort(names);
		return names;
	}
	
	/******************************************
	 * Returns a list of all {@link InterfacePlot} classes currently loaded in this
	 * environment.
	 * 
	 * @return
	 */
	public static ArrayList<String> getInterfacePlotNames(){
		ArrayList<String> names = new ArrayList<String>(interfacePlotClasses.keySet());
		Collections.sort(names);
		return names;
	}
	
	static class InterfacePanelLoader extends ClassLoader{
		public InterfacePanelLoader(){
			
		}
		public synchronized Class<?> loadClass(String className){
			try{
				Class<?> c = loadClass(className, false);
				if (c == null) 
					return null;
				if (InterfacePanel.class.isInstance(c.newInstance())){
					c = loadClass(className, true);
					return c;
					}
			}catch (Exception e){
				e.printStackTrace();
				}
			return null;
		}
	}
	
	static class InterfacePlotLoader extends ClassLoader{
		public InterfacePlotLoader(){
			
		}
		public synchronized Class<?> loadClass(String className){
			try{
				Class<?> c = loadClass(className, false);
				if (c == null) 
					return null;
				if (InterfacePlot.class.isInstance(c.newInstance())){
					c = loadClass(className, true);
					return c;
					}
			}catch (Exception e){
				e.printStackTrace();
				}
			return null;
		}
	}
	
	static class InterfaceGraphicLoader extends ClassLoader{
		public InterfaceGraphicLoader(){
			
		}
		public synchronized Class loadClass(String className){
			try{
				Class c = loadClass(className, false);
				if (c == null) 
					return null;
				if (InterfaceGraphic.class.isInstance(c.newInstance())){
					c = loadClass(className, true);
					return c;
					}
			}catch (Exception e){
				e.printStackTrace();
				}
			return null;
		}
	}
	
	static class InterfaceDialogLoader extends ClassLoader{
		public InterfaceDialogLoader(){
			
		}
		public synchronized Class loadClass(String className){
			try{
				Class c = loadClass(className, false);
				if (c == null) 
					return null;
				if (InterfaceDialogBox.class.isInstance(c.newInstance())){
					c = loadClass(className, true);
					return c;
					}
			}catch (Exception e){
				e.printStackTrace();
				}
			return null;
		}
	}
	
	static class InterfaceDataSourceDialogLoader extends ClassLoader{
		public InterfaceDataSourceDialogLoader(){
			
		}
		public synchronized Class loadClass(String className){
			try{
				Class c = loadClass(className, false);
				if (c == null) 
					return null;
				if (DataSourceDialogPanel.class.isInstance(c.newInstance())){
					c = loadClass(className, true);
					return c;
					}
			}catch (Exception e){
				e.printStackTrace();
				}
			return null;
		}
	}
	
	static class InterfaceIODialogLoader extends ClassLoader{
		public InterfaceIODialogLoader(){
			
		}
		public synchronized Class loadClass(String className){
			try{
				Class c = loadClass(className, false);
				if (c == null) 
					return null;
				if (InterfaceIODialogBox.class.isInstance(c.newInstance())){
					c = loadClass(className, true);
					return c;
					}
			}catch (Exception e){
				e.printStackTrace();
				}
			return null;
		}
	}
	
	static class DataSourceDriverLoader extends ClassLoader{
		public DataSourceDriverLoader(){
			
		}
		public synchronized Class loadClass(String className){
			try{
				Class c = loadClass(className, false);
				if (c == null) 
					return null;
				if (DataSourceDriver.class.isInstance(c.newInstance())){
					c = loadClass(className, true);
					return c;
					}
			}catch (Exception e){
				e.printStackTrace();
				}
			return null;
		}
	}
	
	static class InterfaceOptionsLoader extends ClassLoader{
		public InterfaceOptionsLoader(){
			
		}
		public synchronized Class loadClass(String className){
			try{
				Class c = loadClass(className, false);
				if (c == null) 
					return null;
				if (InterfaceIOOptions.class.isInstance(c.newInstance())){
					c = loadClass(className, true);
					return c;
					}
			}catch (Exception e){
				e.printStackTrace();
				}
			return null;
		}
	}
	
	static class InterfaceIOLoader extends ClassLoader{
		public InterfaceIOLoader(){
			
		}
		public synchronized Class loadClass(String className){
			try{
				Class c = loadClass(className, false);
				if (c == null) 
					return null;
				if (InterfaceIO.class.isInstance(c.newInstance())){
					c = loadClass(className, true);
					return c;
					}
			}catch (Exception e){
				e.printStackTrace();
				}
			return null;
		}
	}
	
	static class VideoTaskLoader extends ClassLoader{
		public VideoTaskLoader(){
			
		}
		public synchronized Class loadClass(String className){
			try{
				Class c = loadClass(className, false);
				if (c == null) 
					return null;
				if (VideoTask.class.isInstance(c.newInstance())){
					c = loadClass(className, true);
					return c;
					}
			}catch (Exception e){
				e.printStackTrace();
				}
			return null;
		}
	}
	
	static class DialogPanelLoader extends ClassLoader{
		public DialogPanelLoader(){
			
		}
		public synchronized Class loadClass(String className){
			try{
				Class c = loadClass(className, false);
				if (c == null) 
					return null;
				if (JPanel.class.isInstance(c.newInstance())){
					c = loadClass(className, true);
					return c;
					}
			}catch (Exception e){
				e.printStackTrace();
				}
			return null;
		}
	}
	
	public static String getVersion(){
		return version;
	}
	
	public static void setVersion(String v){
		version = v;
	}
	
	public static String getNow(String dateFormat) {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
	    return sdf.format(cal.getTime());

	}
	
	
}