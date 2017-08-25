package meshgen;

/**
 * This class is responsible for parsing the command-line arguments.
 * 
 * @author Daniel Park (dp435)
 */

public class Parser {

	public String mode;
	public String shape;
	public Integer divisionsU;
	public Integer divisionsV;
	public Double minorRadius;
	public String infile;
	public String outfile;

	/**
	 * Constructor for Parser.
	 * 
	 * @param args
	 *            String[] of command-line arguments..
	 */
	public Parser(String[] args) {
		initializeDefaults();

		for (int idx = 0; idx < args.length; idx += 2) {
			if (args[idx].equals("-g")) {
				mode = "GENERATE";
				shape = args[idx + 1];
			} else if (args[idx].equals("-i")) {
				mode = "CALCULATE";
				infile = args[idx + 1];
			} else if (args[idx].equals("-n"))
				divisionsU = Integer.parseInt(args[idx + 1]);
			else if (args[idx].equals("-m"))
				divisionsV = Integer.parseInt(args[idx + 1]);
			else if (args[idx].equals("-r"))
				minorRadius = Double.parseDouble(args[idx + 1]);
			else if (args[idx].equals("-o"))
				outfile = args[idx + 1];
		}
	}

	/** A helper function to initialize default values. */
	private void initializeDefaults() {
		mode = null;
		shape = null;
		divisionsU = 32;
		divisionsV = 16;
		minorRadius = 0.25;
		infile = null;
		outfile = null;
	}
}
