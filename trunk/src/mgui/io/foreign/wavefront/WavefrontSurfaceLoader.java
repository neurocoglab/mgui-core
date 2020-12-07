package mgui.io.foreign.wavefront;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjReader;
import mgui.geometry.Mesh3D;
import mgui.geometry.Mesh3D.MeshFace3D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceFileLoader;

/******************************************
 * 
 * Loader for a Wavefront OBJ format surface mesh object. Based on the Obj library:
 * <p><a href="https://github.com/javagl/Obj">https://github.com/javagl/Obj</a></p>
 * 
 * <p>The format is specified here:</p>
 * 
 * <p><a href="https://en.wikipedia.org/wiki/Wavefront_.obj_file">https://en.wikipedia.org/wiki/Wavefront_.obj_file</a>
 * 
 * @author Andrew Reid
 * @since 1.0.30
 *
 */
public class WavefrontSurfaceLoader extends SurfaceFileLoader {

	@Override
	public Mesh3DInt loadSurface(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException {
		
		
		if (dataFile == null){
			InterfaceSession.log("WavefrontMeshLoader: No input file specified..");
			return null;
			}
			
		if (!dataFile.exists()){
			InterfaceSession.log("WavefrontMeshLoader: Cannot find file '" + dataFile.getAbsolutePath() + "'");
			return null;
			}
		
		progress_bar.setIndeterminate(true);
		
		InputStream objInputStream = new FileInputStream(dataFile.getAbsolutePath());
        Obj obj = ObjReader.read(objInputStream);
        
        Mesh3D mesh = new Mesh3D();
        
        
        // Assign vertices
        for (int i = 0; i < obj.getNumVertices(); i++) {
        	mesh.addVertex(getPoint3f(obj.getVertex(i)));
        	}
        
        int bad_face_count = 0;
        
        // Assign faces
        for (int i = 0; i < obj.getNumFaces(); i++) {
        	ObjFace face = obj.getFace(i);
        	if (face.getNumVertices() != 3) {
        		bad_face_count++;
        	} else {
	        	mesh.addFace(getFace(obj.getFace(i)));
	        	}
        	}
        
        if (bad_face_count > 0) {
        	InterfaceSession.log("WaveFrontMesh " + dataFile.getName() + ": " + bad_face_count + " non-triangular faces encountered.", 
        						 LoggingType.Warnings);
        	
        	}
        
        mesh.finalize();
		
		InterfaceSession.log("Wavefront OBJ file '" + dataFile.getAbsolutePath() + "' loaded.", LoggingType.Verbose);
		InterfaceSession.log("Faces: " + mesh.f, LoggingType.Debug);
		InterfaceSession.log("Nodes: " + mesh.n, LoggingType.Debug);
		
		Mesh3DInt mesh_int = new Mesh3DInt(mesh);
		mesh_int.setFileLoader(getIOType());
		mesh_int.setUrlReference(dataFile.toURI().toURL());
		
		return mesh_int;
		
	}
	
	@Override
	public String getTitle(){
		return "Load Wavefront mesh";
	}

	protected MeshFace3D getFace(ObjFace face) {
		return new MeshFace3D(face.getVertexIndex(0), face.getVertexIndex(1), face.getVertexIndex(2));
	}
	
	protected Point3f getPoint3f(FloatTuple tuple) {
		return new Point3f(tuple.getX(), tuple.getY(), tuple.getZ());
	}
	
	protected Vector3f getVector3f(FloatTuple tuple) {
		return new Vector3f(tuple.getX(), tuple.getY(), tuple.getZ());
	}
	
}
