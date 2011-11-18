package run;

import imagerailio.ImageRail_SDCube;

public class MergeSDCubes {

	public static void main(String[] args) {
		// Seperate the paths into input and output string arrays
		long time = System.currentTimeMillis();
		int len = args.length;
		String[] in = new String[len - 1];
		for (int i = 0; i < len - 1; i++)
			in[i] = args[i];
		ImageRail_SDCube.mergeSDCubes(in, args[len - 1]);

		System.out.println("Total time to Merge: "
				+ (System.currentTimeMillis() - time));
	}

}
