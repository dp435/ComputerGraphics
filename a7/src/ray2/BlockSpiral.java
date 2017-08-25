/**
 * Class wraps code that generates the spiral of image blocks
 * for rendering.
 *
 * @author arbree
 * Copyright 2014 Program of Computer Graphics, Cornell University
 */

package ray2;

public final class BlockSpiral {

	//variables controling ordering of sub-blocks
	private int numSubX;             //number of subblocks in x direction
	private int numSubY;             //number of subblocks in y direction
	protected int totalSubblocks;    //total number of subblocks to be computed in all passes
	protected int curSubX, curSubY;  //current subblock to request
	private int curDir;              //direction to next subblock
	private int movesLeft;           //moves left in this direction
	private int moveLength;          //total moves before next direction change

	//Constants defining directions of motion
	private static final int PLUSY = 0;
	private static final int PLUSX = 1;
	private static final int MINUSY = 2;
	private static final int MINUSX = 3;

	/**
	 * Initialize the sub-block spiral counters (must be incremented once for valid block)
	 */
	protected void initSubblockSpiral(int width, int height) {

		numSubX = ((width - 1) / RayTracer.SUB_WIDTH) + 1;
		numSubY = ((height - 1) / RayTracer.SUB_HEIGHT) + 1;
		totalSubblocks = numSubX * numSubY;
		curSubX = (numSubX / 2) - 1;
		curSubY = (numSubY / 2) - 2;
		curDir = PLUSY;
		movesLeft = 2;
		moveLength = 1;
	}

	/**
	 * Increment the sub-block spiral one block
	 */
	protected void incrementSublockSpiral() {

		do {
			if (movesLeft == 0) { //time to change direction
				curDir++;
				if (curDir > MINUSX)
					curDir = PLUSY;
				if ((curDir == PLUSY) || (curDir == MINUSY))
					moveLength++;
				movesLeft = moveLength;
			}
			if (curDir == PLUSY)
				curSubY++;
			else if (curDir == PLUSX)
				curSubX++;
			else if (curDir == MINUSY)
				curSubY--;
			else if (curDir == MINUSX)
				curSubX--;
			movesLeft--;
		} while (curSubX < 0 || curSubY < 0 || curSubX >= numSubX || curSubY >= numSubY);
	}
}