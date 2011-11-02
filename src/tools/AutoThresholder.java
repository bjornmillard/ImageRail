package tools;

final public class AutoThresholder {

	/**
	 *  Otsu's threshold algorithm
	 *  
	 *	1) Compute histogram and probabilities of each intensity level
	 *	2) Set up initial omega-i(0) and mu-i(0)
     *	3) Step through all possible thresholds t = 1 \ldots maximum intensity
     *  4) Update omega-i and mu-i
     *  5) Compute \sigma^2_b(t)
     *	6) Desired threshold corresponds to the maximum \sigma^2_b(t) 
     **/
	public static int Otsu(int [] data ) {
		// C++ code by Jordan Bevik <Jordan.Bevic@qtiworld.com>
		// adapted to Java for ImageJ plugin by G.Landini
		int k,kStar;  // k = the current threshold; kStar = optimal threshold
		int N1, N;    // N1 = # points with intensity <=k; N = total number of points
		double BCV, BCVmax; // The current Between Class Variance and maximum BCV
		double num, denom;  // temporary bookeeping
		int Sk;  // The total intensity for all histogram points <=k
		int S, L=data.length; // The total intensity of the image

		// Initialize values:
		S = N = 0;
		for (k=0; k<L; k++){
			S += k * data[k];	// Total histogram intensity
			N += data[k];		// Total number of data points
		}

		Sk = 0;
		N1 = data[0]; // The entry for zero intensity
		BCV = 0;
		BCVmax=0;
		kStar = 0;

		// Look at each possible threshold value,
		// calculate the between-class variance, and decide if it's a max
		for (k=1; k<L-1; k++) { // No need to check endpoints k = 0 or k = L-1
			Sk += k * data[k];
			N1 += data[k];

			// The float casting here is to avoid compiler warning about loss of precision and
			// will prevent overflow in the case of large saturated images
			denom = (double)( N1) * (N - N1); // Maximum value of denom is (N^2)/4 =  approx. 3E10

			if (denom != 0 ){
				// Float here is to avoid loss of precision when dividing
				num = ( (double)N1 / N ) * S - Sk; 	// Maximum value of num =  255*N = approx 8E7
				BCV = (num * num) / denom;
			}
			else
				BCV = 0;

			if (BCV >= BCVmax){ // Assign the best threshold found so far
				BCVmax = BCV;
				kStar = k;
			}
		}
		// kStar += 1;	// Use QTI convention that intensity -> 1 if intensity >= k
		// (the algorithm was developed for I-> 1 if I <= k.)
		return kStar;
	}
	
}
