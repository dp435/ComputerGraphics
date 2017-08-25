package splines;
import java.util.ArrayList;

import egl.math.Matrix4;
import egl.math.Vector2;

public class BSpline extends SplineCurve{

	public BSpline(ArrayList<Vector2> controlPoints, boolean isClosed,
			float epsilon) throws IllegalArgumentException {
		super(controlPoints, isClosed, epsilon);
	}

	@Override
	public CubicBezier toBezier(Vector2 p0, Vector2 p1, Vector2 p2, Vector2 p3,
			float eps) {
		
		// P1 = M1Inverse * M2 *P2
		Matrix4 bSplineCoeff = new Matrix4(-1f/6f, 3f/6f, -3f/6f, 1f/6f,
											3f/6f, -6f/6f, 3f/6f, 0f/6f,
											-3f/6f, 0f/6f, 3f/6f, 0f/6f,
											1f/6f, 4f/6f, 1f/6f, 0f/6f);
		
		Matrix4 bezCoeff = new Matrix4 (-1,  3, -3, 1,
										 3, -6,  3, 0,
										-3,  3,  0, 0, 
										 1,  0,  0, 0);
		
		//Just putting this in a Matrix4 because we don't have arbitrary sized matrices
		Matrix4 catRomPoints = new Matrix4(p0.x, p0.y, 0, 0,
										   p1.x, p1.y, 0, 0,
										   p2.x, p2.y, 0, 0,
										   p3.x, p3.y, 0, 0);
		
	
		Matrix4 bezPoints = bezCoeff.invert().mulBefore(bSplineCoeff.mulBefore(catRomPoints));
		
		Vector2 bezPoint0 = new Vector2(bezPoints.m[0], bezPoints.m[4]);
		Vector2 bezPoint1 = new Vector2(bezPoints.m[1], bezPoints.m[5]);
		Vector2 bezPoint2 = new Vector2(bezPoints.m[2], bezPoints.m[6]);
		Vector2 bezPoint3 = new Vector2(bezPoints.m[3], bezPoints.m[7]);
		
		return new CubicBezier(bezPoint0, bezPoint1, bezPoint2, bezPoint3, eps);
	}
	

		
	
}
