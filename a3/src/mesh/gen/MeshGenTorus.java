package mesh.gen;

import common.BasicType;
import mesh.MeshData;
import egl.NativeMem;
import egl.math.Matrix4;
import egl.math.Vector3;

/**
 * Generates A Torus Mesh
 * @author Cristian (Original)
 * @author Tongcheng (Revised 8/26/2015)
 */
public class MeshGenTorus extends MeshGenerator {
	@Override
	public void generate(MeshData outData, MeshGenOptions opt) {
		// Extra Credit, But Not Difficult
		
		// TODO#A1 SOLUTION START

		// Calculate Vertex And Index Count
		int vertsPerRing = opt.divisionsLatitude + 1;
		outData.vertexCount = (opt.divisionsLongitude + 1) * vertsPerRing;
		int tris = opt.divisionsLongitude * opt.divisionsLatitude * 2;
		outData.indexCount = tris * 3;
		
		// Create Storage Spaces
		outData.positions = NativeMem.createFloatBuffer(outData.vertexCount * 3);
		outData.uvs = NativeMem.createFloatBuffer(outData.vertexCount * 2);
		outData.normals = NativeMem.createFloatBuffer(outData.vertexCount * 3);
		outData.indices = NativeMem.createIntBuffer(outData.indexCount);
		
		for (int i=0;i<=opt.divisionsLongitude;i++){
			double phi=2*Math.PI*(i/(float)opt.divisionsLongitude);
			//ringCenter is center of each vertical ring, all ringCenters form a circle of radius 1 on x-z plane
			float[] ringCenter=new float[]{(float)Math.sin(phi),.0f,(float)-Math.cos(phi)};
			for (int j=0;j<=opt.divisionsLatitude;j++){
				double theta=2*Math.PI*(j/(float)opt.divisionsLatitude);
				//ring vector is the unit vector from center of ring in the direction to the vertex
				float[] ringVector=new float[]{
						(float)-(Math.cos(theta)*Math.sin(phi)),
						(float)(Math.sin(theta)),
						(float)(Math.cos(theta)*Math.cos(phi))
				};
				//position
				outData.positions.put(ringCenter[0]+opt.innerRadius*ringVector[0]);
				outData.positions.put(ringCenter[1]+opt.innerRadius*ringVector[1]);
				outData.positions.put(ringCenter[2]+opt.innerRadius*ringVector[2]);
				//normal
				outData.normals.put(ringVector[0]);outData.normals.put(ringVector[1]);outData.normals.put(ringVector[2]);
				//UV
				outData.uvs.put(1-i/(float)opt.divisionsLongitude);
				outData.uvs.put(1-j/(float)opt.divisionsLatitude);
			}
		}
		
		// Create The Indices
		for(int i = 0;i < opt.divisionsLongitude;i++) {
			int si = i * vertsPerRing;
			for(int pi = 0;pi < opt.divisionsLatitude;pi++) {
				outData.indices.put(si);
				outData.indices.put(si + vertsPerRing);
				outData.indices.put(si + 1);
				outData.indices.put(si + 1);
				outData.indices.put(si + vertsPerRing);
				outData.indices.put(si + vertsPerRing + 1);
				si++;
			}
		}
		
		// #SOLUTION END
	}
	
	@Override
	public BasicType getType() {
		return BasicType.TriangleMesh; // Ray-casting Slightly More Difficult On A Torus 
	}
}
