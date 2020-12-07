package mgui.io.foreign.wavefront;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjWriter;
import de.javagl.obj.Objs;
import mgui.geometry.Mesh3D;
import mgui.geometry.Mesh3D.MeshFace3D;
import mgui.geometry.mesh.MeshFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceFileWriter;

/******************************************
 * 
 * Writer for a Wavefront OBJ format surface mesh object. Based on the Obj library:
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
public class WavefrontSurfaceWriter extends SurfaceFileWriter {

	@Override
	public boolean writeSurface(Mesh3DInt mesh, InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		if (dataFile == null){
			InterfaceSession.log("WavefrontSurfaceWriter: No output file specified.", LoggingType.Errors);
			return false;
			}
		
		progress_bar.setIndeterminate(true);
		
		Mesh3D mesh3d = mesh.getMesh();
		
		Obj obj = Objs.create();
		
		for (Point3f v : mesh3d.getVertices()) {
			obj.addVertex(v.getX(), v.getY(), v.getZ());
			}
		
		ArrayList<Vector3f> normals = MeshFunctions.getSurfaceNormals(mesh3d);
		for (Vector3f n : normals) {
			obj.addNormal(n.getX(), n.getY(), n.getZ());
			}
		
		for (MeshFace3D face : mesh3d.getFaces()) {
			int[] f = {face.A, face.B, face.C};
			obj.addFace(f, null, f);
			}
 
        try {
        	
	        OutputStream objOutputStream = new FileOutputStream(dataFile.getAbsolutePath());
	        ObjWriter.write(obj, objOutputStream);
	        
        } catch (IOException ex) {
        	
        	InterfaceSession.log("WavefrontSurfaceWriter: Error writing surface. Reason: " + ex.getMessage(), 
					 				LoggingType.Errors);
        	return false;
        	}
		
        InterfaceSession.log("Surface written to Wavefront OBJ file '" + dataFile.getAbsolutePath() + "'.", LoggingType.Verbose);
		InterfaceSession.log("Faces: " + obj.getNumFaces(), LoggingType.Debug);
		InterfaceSession.log("Nodes: " + obj.getNumVertices(), LoggingType.Debug);
        
		return true;
	}

}
