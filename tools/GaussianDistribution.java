/**
 * GaussianDistribution.java
 *
 * @author Created by Omnicore CodeGuide
 */

package tools;


import java.io.PrintWriter;

public class GaussianDistribution
{
	// Parameters describing the distribution ==> [Amplitude, Mean, Width];
	public float[] Parameters;
	
	
	
	public GaussianDistribution()
	{
		Parameters = new float[3];
		
		
		
		
	}
	
	
	public void getInitialConditionsForParameters(int[] data)
	{
		Parameters[0] = getMaxValue(data);
		Parameters[1] = getMeanValue(data)/(float)data.length;
		Parameters[2] = 0.1f;
		
	}
	
	public void copy(GaussianDistribution g)
	{
		for (int i = 0; i < 3; i++)
			Parameters[i] = g.Parameters[i];
	}
	
	
	
	/**
	 *  Static methods below
	 * */
	
	static public GaussianDistribution[] fitGaussians(int[] data, int numModes)
	{
		System.out.println("*****************");
		
		//Fitting a single Gaussian to the input data
		//
		GaussianDistribution[] gaussians = null;
		
		
		if ( numModes==2)
		{
			gaussians = new GaussianDistribution[2];
			gaussians[0] = new GaussianDistribution();
			gaussians[0].getInitialConditionsForParameters(data);
			gaussians[0].Parameters[1] -= 0.1;
			
			gaussians[1] = new GaussianDistribution();
			gaussians[1].getInitialConditionsForParameters(data);
			gaussians[1].Parameters[1] += 0.3;
		}
		else if (numModes == 1)
		{
			gaussians = new GaussianDistribution[1];
			gaussians[0] = new GaussianDistribution();
			gaussians[0].getInitialConditionsForParameters(data);
		}
		
		
		//Setting up a loop to iteratively change Gaussian parameters to look more and more like the data
		float[][] gradP = new float[gaussians.length][3];
		boolean boo = false;
		double error = 0;
		int counter = 0;
		double threshold = 0.001;
		
		GaussianDistribution[] gaussians_last = new GaussianDistribution[gaussians.length];
		for (int i = 0; i < gaussians_last.length; i++)
			gaussians_last[i] = new GaussianDistribution();
		
		while(counter<1000)
		{
			error = computeGaussianFitError(gaussians, data);
			if (error<threshold || areGaussiansSame(gaussians, gaussians_last))
			{
				boo = true;
				break;
			}
			else
				for (int i = 0; i < gaussians.length; i++)
					gaussians_last[i].copy(gaussians[i]);
			
			adjustParameters(data, gaussians, gradP);
			counter++;
			
			if (counter==(1000-1))
				System.out.println("FAILURE TO CONVERGE");
		}
		
		
//		printResults(data, all);
		
		//Report convergence
		if (boo)
			System.out.println("CONVERGED--> Error: "+ error+"   Iters: "+counter);
		
		
//		if (pw!=null)
//		{
//			pw.print((1f-error)+",");
//			pw.flush();
//		}
		
		
		//Reporting Error
		return gaussians;
	}
	
	
	static public boolean areGaussiansSame(GaussianDistribution[] g, GaussianDistribution[] g_old)
	{
		int len = g.length;
		for (int i = 0; i < len; i++)
			for (int p = 0; p < 3; p++)
				if (g[i].Parameters[p]!=g_old[i].Parameters[p])
					return false;
		return true;
	}
	
	static public void printParameters(GaussianDistribution g)
	{
		System.out.println("Pars: "+ g.Parameters[0]+ "  ,   "+ g.Parameters[1] +"  ,   "+   g.Parameters[2]);
	}
	
	static public void printResults(int[] origData, double[][] fitData)
	{
		int num = origData.length;
		//Printing results
		for (int i = 0; i < 10; i++)
			System.out.println();
		for (int x = 0; x < num; x++)
			System.out.println(origData[x]);
		
		
		for (int p = 0; p < fitData.length; p++)
		{
			for (int i = 0; i < 10; i++)
				System.out.println();
			for (int x = 0; x < num; x++)
				System.out.println(fitData[p][x]);
		}
	}
	
	static public void printResults(double[] origData, double[][] fitData)
	{
		int num = origData.length;
		//Printing results
		for (int i = 0; i < 10; i++)
			System.out.println();
		for (int x = 0; x < num; x++)
			System.out.println(origData[x]);
		
		
		for (int p = 0; p < fitData.length; p++)
		{
			for (int i = 0; i < 10; i++)
				System.out.println();
			for (int x = 0; x < num; x++)
				System.out.println(fitData[p][x]);
		}
	}
	
	
	static public void printResults(double[] origData, GaussianDistribution dist)
	{
		int num = origData.length;
		double[] fitData = new double[num];
		getGaussian(dist.Parameters, fitData);
		
		
		//Printing results
		for (int i = 0; i < 10; i++)
			System.out.println();
		for (int x = 0; x < num; x++)
			System.out.println(origData[x]);
		
		for (int i = 0; i < 10; i++)
			System.out.println();
		for (int x = 0; x < num; x++)
			System.out.println(fitData[x]);
		
	}
	static public void printResults(double[] origData, double[] fitData)
	{
		
		
		int num = origData.length;
		//Printing results
		for (int i = 0; i < 10; i++)
			System.out.println();
		for (int x = 0; x < num; x++)
			System.out.println(origData[x]);
		
		for (int i = 0; i < 10; i++)
			System.out.println();
		for (int x = 0; x < num; x++)
			System.out.println(fitData[x]);
		
	}
	
	static public void getGaussian(float[] pars, double[] v)
	{
		int len = v.length;
		for (int x = 0; x < len; x++)
		{
			float xf = (float)x/(float)len;
			v[x] = pars[0]*Math.exp(-((xf-pars[1])*(xf-pars[1]))/(2f*pars[2]*pars[2]));
		}
	}
	
	static public void getGaussian_wNoise(float[] pars, double[] v, float nFactor)
	{
		int len = v.length;
		for (int x = 0; x < len; x++)
		{
			float xf = (float)x/(float)len;
			v[x] = nFactor*Math.random()+pars[0]*Math.exp(-((xf-pars[1])*(xf-pars[1]))/(2f*pars[2]*pars[2]));
		}
	}
	
	static public float getMeanValue(int[] v)
	{
		int len = v.length;
		float mean = 0;
		float integrated = 0;
		for (int i = 0; i < len; i++)
		{
			mean+=((float)i*(float)v[i]);
			integrated+=v[i];
		}
		return (mean/(float)integrated);
	}
	static public float getMaxValue(int[] v)
	{
		int len = v.length;
		float max = Float.NEGATIVE_INFINITY;
		for (int i = 0; i < len; i++)
			if(v[i]>max)
				max=(float)v[i];
		return max;
	}
	
	static public float getMeanValue(double[] v)
	{
		int len = v.length;
		float mean = 0;
		float integrated = 0;
		for (int i = 0; i < len; i++)
		{
			mean+=((float)i*v[i]);
			integrated+=v[i];
		}
		return (mean/(float)integrated);
	}
	static public float getMaxValue(double[] v)
	{
		int len = v.length;
		float max = Float.NEGATIVE_INFINITY;
		for (int i = 0; i < len; i++)
			if(v[i]>max)
				max=(float)v[i];
		return max;
	}
	static public void adjustParameters(int[] data, GaussianDistribution[] gauss, float[][] gradP)
	{
		int numDist = gauss.length;
		getGradient(data, gauss, gradP);
		
		for (int i = 0; i < numDist; i++)
			for (int p = 0; p < 3; p++)
				gauss[i].Parameters[p] += (float)(gradP[i][p]*(0.01f*gauss[i].Parameters[p]));
	}
	static public void adjustParameters(double[] data, GaussianDistribution[] gauss, float[][] gradP)
	{
		int numDist = gauss.length;
		getGradient(data, gauss, gradP);
		
		for (int i = 0; i < numDist; i++)
			for (int p = 0; p < 3; p++)
				gauss[i].Parameters[p] += (float)(gradP[i][p]*(0.01f*gauss[i].Parameters[p]));
	}
	
	static public void getGradient(int[] data, GaussianDistribution[] gauss, float[][] gradP)
	{
		int numDist = gauss.length;
		int num = 3;
		GaussianDistribution[] gauss_test = new GaussianDistribution[numDist];
		for (int i = 0; i < numDist; i++)
		{
			gauss_test[i] = new GaussianDistribution();
			for (int p = 0; p < num; p++)
				gauss_test[i].Parameters[p] = gauss[i].Parameters[p];
		}
		
		double error_orig = computeGaussianFitError(gauss_test, data);;
		double error = 0;
		for (int i = 0; i < numDist; i++)
			for (int p = 0; p < num; p++)
			{
				double val = (0.01f*gauss_test[i].Parameters[p]);
				gauss_test[i].Parameters[p] += val;
				error = computeGaussianFitError(gauss_test, data);
				if (error<error_orig)
					gradP[i][p] = 1;
				else
				{
					gauss_test[i].Parameters[p] -= (2*val);
					error = computeGaussianFitError(gauss_test, data);
					if (error<error_orig)
						gradP[i][p] = -1;
					else
						gradP[i][p] = 0;
				}
				//reset par while testing others
				gauss_test[i].Parameters[p] = gauss[i].Parameters[p];
			}
		
		
//		System.out.println(	gradP[0]);
		
//		System.out.println("g[0] = "+gradP[0] +" g[1] = "+gradP[1] +"  g[2] = "+gradP[2] );
	}
	
	static public void getGradient(double[] data, GaussianDistribution[] gauss, float[][] gradP)
	{
		int numDist = gauss.length;
		int num = 3;
		GaussianDistribution[] gauss_test = new GaussianDistribution[numDist];
		for (int i = 0; i < numDist; i++)
		{
			gauss_test[i] = new GaussianDistribution();
			for (int p = 0; p < num; p++)
				gauss_test[i].Parameters[p] = gauss[i].Parameters[p];
		}
		
		double error_orig = computeGaussianFitError(gauss_test, data);;
		double error = 0;
		for (int i = 0; i < numDist; i++)
			for (int p = 0; p < num; p++)
			{
				double val = (0.01f*gauss_test[i].Parameters[p]);
				gauss_test[i].Parameters[p] += val;
				error = computeGaussianFitError(gauss_test, data);
				if (error<error_orig)
					gradP[i][p] = 1;
				else
				{
					gauss_test[i].Parameters[p] -= (2*val);
					error = computeGaussianFitError(gauss_test, data);
					if (error<error_orig)
						gradP[i][p] = -1;
					else
						gradP[i][p] = 0;
				}
				//reset par while testing others
				gauss_test[i].Parameters[p] = gauss[i].Parameters[p];
			}
		
		
//		System.out.println(	gradP[0]);
		
//		System.out.println("g[0] = "+gradP[0] +" g[1] = "+gradP[1] +"  g[2] = "+gradP[2] );
	}
	static public double computeGaussianFitError(GaussianDistribution[] gauss, int[] dist)
	{
		int nBins = dist.length;
		int numD = gauss.length;
		double[][] all = new double[numD][nBins];
		
		for (int i = 0; i < numD; i++)
		{
			all[i] = new double[nBins];
			getGaussian(gauss[i].Parameters, all[i]);
			
		}
		
		//Calculating the error of this parameter set:
		return getPercentageError_L1Dist(all, dist);
		
	}
	static public double computeGaussianFitError(GaussianDistribution[] gauss, double[] dist)
	{
		int nBins = dist.length;
		int numD = gauss.length;
		double[][] all = new double[numD][nBins];
		
		for (int i = 0; i < numD; i++)
		{
			all[i] = new double[nBins];
			getGaussian(gauss[i].Parameters, all[i]);
			
		}
		
		
		//Calculating the error of this parameter set:
		return getPercentageError_L1Dist(all, dist);
		
	}
	
	static public double getPercentageError_L1Dist(double[][] all, int[] v2)
	{
		int numDist = all.length;
		int lenData = v2.length;
		int integratedRealData = 0;
		
		double err = 0;
		for (int i = 0; i < lenData; i++)
		{
			int sumThis = 0;
			for (int p = 0; p < numDist; p++)
				sumThis+=all[p][i];
			
			integratedRealData+=v2[i];
			err+= Math.abs(sumThis-v2[i]);
		}
		
		return (float)err/(float)integratedRealData;
		
	}
	static public double getPercentageError_L1Dist(double[][] all, double[] v2)
	{
		int numDist = all.length;
		int lenData = v2.length;
		
		double err = 0;
		for (int i = 0; i < lenData; i++)
		{
			int sumThis = 0;
			for (int p = 0; p < numDist; p++)
				sumThis+=all[p][i];
			
			err+= Math.abs(sumThis-v2[i]);
		}
		
		return err;
		
	}
	
	static public double getPercentageError_L1Dist(double[] v1, double[] v2)
	{
		int len = v1.length;
		int len2 = v2.length;
		if (len!=len2)
			return -1;
		
		double err = 0;
		for (int i = 0; i < len; i++)
			err+= (v1[i]-v2[i])*(v1[i]-v2[i]);
		return err;
	}
	
	
}

