package ray2.shader;

import static org.junit.Assert.*;

import org.junit.Test;

import ray2.IntersectionRecord;
import ray2.light.PointLight;
import ray2.Ray;
import ray2.Scene;
import ray2.accel.AccelStruct;
import ray2.accel.NaiveAccelStruct;
import ray2.camera.Camera;
import ray2.camera.PerspectiveCamera;
import ray2.surface.Sphere;
import ray2.surface.Surface;
import egl.math.Colord;
import egl.math.Matrix4d;
import egl.math.Vector3d;

public class ShaderTests {
    
    @Test
    public void testFresnel() {
        Vector3d normal = new Vector3d(1, 1, 1);
        Vector3d outgoing = new Vector3d(1, 1, 1);
        double refractiveIndex = 2.0f;
        CookTorrance shader = new CookTorrance();
        
        double result;
        result = shader.fresnel(normal, outgoing, refractiveIndex);
        
        assertTrue("Fresnel:\n"
                + "normal: " + normal + "\n"
                + "outgoing: " + outgoing + "\n"
                + "refractiveIndex: " + refractiveIndex + "\n"
                + "Expected: 0.1549192\n"
                + "Got: " + result, doublesEqual(0.1549192, result));
        
        outgoing.set(-1, 1, 0);
        result = shader.fresnel(normal, outgoing, refractiveIndex);
        assertTrue("Fresnel:\n"
                + "normal: " + normal + "\n"
                + "outgoing: " + outgoing + "\n"
                + "refractiveIndex: " + refractiveIndex + "\n"
                + "Expected: 1.0\n"
                + "Got: " + result, doublesEqual(1.0, result));
        
        normal.set(1, 2, 0);
        result = shader.fresnel(normal, outgoing, refractiveIndex);
        assertTrue("Fresnel:\n"
                + "normal: " + normal + "\n"
                + "outgoing: " + outgoing + "\n"
                + "refractiveIndex: " + refractiveIndex + "\n"
                + "Expected: 0.111111111\n"
                + "Got: " + result, doublesEqual(0.111111111, result));
        
        refractiveIndex = 5.0;
        result = shader.fresnel(normal, outgoing, refractiveIndex);
        assertTrue("Fresnel:\n"
                + "normal: " + normal + "\n"
                + "outgoing: " + outgoing + "\n"
                + "refractiveIndex: " + refractiveIndex + "\n"
                + "Expected: 0.44444444\n"
                + "Got: " + result, doublesEqual(0.44444444, result));
    }
    
    // Simple element-wise comparison.
    private boolean colorsEqual(Colord v0, Colord v1) {
        double epsilon = 1e-4;
        return (Math.abs(v0.x - v1.x) < epsilon &&
                Math.abs(v0.y - v1.y) < epsilon && 
                Math.abs(v0.z - v1.z) < epsilon);
    }
    
    private boolean doublesEqual(double d0, double d1) {
        double epsilon = 1e-4;
        return Math.abs(d0 - d1) < epsilon;
    }

}
