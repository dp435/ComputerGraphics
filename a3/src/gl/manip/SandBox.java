package gl.manip;

import egl.math.Matrix4;
import egl.math.Vector3;
import egl.math.Vector4;

public class SandBox {

	public static void main(String[] args) {
		Vector3 e = new Vector3(1, 1, 0);
		Vector3 g = new Vector3(0, -1, 1);
		Vector3 tIntermediate = new Vector3(1, 1, 0);
		Vector3 p = new Vector3(5, -20, 15);

		Vector3 w = g.clone().normalize();
		Vector3 u = tIntermediate.clone().cross(w).normalize();
		Vector3 v = w.clone().cross(u);

		Vector4 w4 = new Vector4(w.x, w.y, w.z, 0);
		Vector4 u4 = new Vector4(u.x, u.y, u.z, 0);
		Vector4 v4 = new Vector4(v.x, v.y, v.z, 0);
		Vector4 p4 = new Vector4(0f, 0f, 0f, 1f);

		Matrix4 mCam = new Matrix4(w4, u4, v4, p4);
		System.out.println("Before mCam: " + mCam);
		Matrix4 viewMat = new Matrix4(1, 0, 0, -e.x, 0, 1, 0, -e.y, 0, 0, 1, -e.z, 0, 0, 0, 1);
		mCam = mCam.mulBefore(viewMat);

		System.out.println("mCam: " + mCam);

		Vector4 camP = mCam.clone().mul(new Vector4(p.x, p.y, p.z, 1));
		System.out.println("p in CamSpace: " + camP);

		float n = -3f;
		float f = -50f;
		float r = 10f;
		float l = -10f;
		float b = -10f;
		float t = 10f;

		Matrix4 persMat = new Matrix4(2 * n / (r - l), 0f, (l + r) / (l - r), 0f, 0f, 2 * n / (t - b),
				(b + t) / (b - t), 0f, 0f, 0f, (f + n) / (n - f), (2 * f * n / (f - n)), 0f, 0f, 1f, 0f);

		System.out.println("persMat untouched:" + persMat);
		Vector4 persMatApplied = persMat.clone().mul(camP);
		persMatApplied.div(persMatApplied.w);
		System.out.println("persMatApplied: " + persMatApplied);

		Matrix4 orthMat = new Matrix4(2f / (r - l), 0f, 0f, -(r + l) / (r - l), 0f, 2f / (t - b), 0f,
				-(t + b) / (t - b), 0f, 0f, 2 / (n - f), -(n + f) / (n - f), 0f, 0f, 0f, 1f);

		float nx = 100f;
		float ny = 50f;

		Matrix4 vpMat = new Matrix4(nx / 2f, 0f, 0f, (nx - 1) / 2, 0f, ny / 2, 0f, (ny - 1) / 2, 0f, 0f, 1f, 0f, 0f, 0f,
				0f, 1f);
		System.out.println(vpMat);
		System.out.println("Rest");
		Vector4 finalM = vpMat.clone().mul(persMatApplied);
		System.out.println(finalM.clone().div(finalM.w));

		Vector3 basis1 = new Vector3(1.5f, 2f, -4f);
		Vector3 basis2 = new Vector3(-1, -1, -1).normalize().clone().cross(basis1);
		Vector3 normal = basis1.clone().cross(basis2);
		System.out.println("normal is:" + normal);
		Vector3 mouseOrigin = new Vector3(0f, 0f, 0f);
		Vector3 mouseDir = basis1.clone();
		Vector3 manipOrigin = new Vector3(2, 2, -6);
		Vector3 manipDir = new Vector3(-1f, -1f, -1f).normalize();
		float distNumer = (mouseOrigin.clone().sub(manipOrigin)).dot(normal);
		float distDenom = manipDir.clone().dot(normal);
		float dist = distNumer / distDenom;
		System.out.println("dist is:" + dist);
		System.out.println("point is at:" + mouseDir.clone().mul(dist));
		Vector3 loc = mouseDir.clone().mul(dist);
		System.out.println("t is:" + (loc.clone().sub(manipOrigin)).dot(manipDir.clone().normalize()));
	}
}
