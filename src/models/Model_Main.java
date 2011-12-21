package models;

import features.Feature;
import features.FeatureSorter;
import filters.DotFilterQueue;
import filters.FilterManager;
import gui.Gui_PlateRepository;
import gui.MainGUI;
import gui.MainSplash;
import gui.MainStartupDialog;
import imagerailio.ImageRail_SDCube;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Polygon;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import midasGUI.MidasInputPanel;
import sdcubeio.ExpDesign_Model;
import sdcubeio.H5IO_Exception;
import sdcubeio.SDCube;

public class Model_Main {
	/** The Main Model object */
	private static MainGUI TheMainGUI;
	private static Model_Main TheMainModel;
	private static MainStartupDialog TheStartupDialog;
	public static final String DATE_FORMAT_NOW = "yyyyMMdd_HHmmss";

	static public int MAXPIXELVALUE = 65535;
	static public final int MIDASINPUT = 0;
	static public final int LINEGRAPH = 1;
	static public final int DOTPLOT = 2;
	static public final int HISTOGRAM = 3;
	static final public Font Font_6 = new Font("Helvetica", Font.PLAIN, 6);
	static final public Font Font_8 = new Font("Helvetica", Font.PLAIN, 8);
	static final public Font Font_9 = new Font("Helvetica", Font.PLAIN, 9);
	static final public Font Font_12 = new Font("Helvetica", Font.BOLD, 12);
	static final public Font Font_14 = new Font("Helvetica", Font.BOLD, 14);
	static final public Font Font_16 = new Font("Helvetica", Font.BOLD, 16);
	static final public Font Font_18 = new Font("Helvetica", Font.BOLD, 18);
	static public NumberFormat nf = new DecimalFormat("0.##");
	static public BasicStroke Stroke_1 = new BasicStroke(1);
	static public BasicStroke Stroke_2 = new BasicStroke(2);
	static public BasicStroke Stroke_3 = new BasicStroke(3);
	static public BasicStroke Stroke_4 = new BasicStroke(4);

	private ArrayList<Feature> TheFeatures;
	private Feature TheSelectedFeature;
	private int TheSelectedFeature_Index;
	private String[] ChannelNames;

	private boolean SubtractBackground;
	private float[] ScalingRatios = { 1.0f, 0.75f, 0.5f, 0.25f, 0.1f };
	private double[] MaxValues_ImageDisplay;
	private double[] MinValues_ImageDisplay;
	private File ImageDirectory;
	private File TheProjectParentDirectory;
	private String TheInputProjectPath;
	private String TheOutputProjectPath;
	private int TheColorMap;
	private FilterManager TheFilterManager;
	private ArrayList TheFilters;
	private boolean Processing;
	private boolean StopProcessing;
	// private File TheProjectDirectory;
	private ImageRail_SDCube TheImageRail_H5IO;
	private boolean areDataSetsModified;
	private DotFilterQueue TheFilterQueue;
	private ExpDesign_Model TheExpDesign_Model;
	private Model_PlateRepository ThePlateRepository_Model;
	
	
	public Model_Main() {
		TheMainModel = this;
	}

	/**
	 * Returns the default file directory to make file choosers more convenient
	 * 
	 * @author BLM
	 * */
	public File getTheDirectory() {
		return TheProjectParentDirectory;
	}

	/**
	 * Sets the default file directory to make file choosers more convenient
	 * 
	 * @author BLM
	 * */
	public void setTheDirectory(File dir) {
		TheProjectParentDirectory = dir;
	}

	/**
	 * Returns the directory where images came from
	 * 
	 * @author BLM
	 * */
	public File getImageDirectory() {
		return ImageDirectory;
	}

	/**
	 * Returns the Panel that holds all the plates
	 * 
	 * @author BLM
	 * */
	public Model_PlateRepository getPlateRepository() {
		return ThePlateRepository_Model;
	}

	/**
	 * Sets the plate repository model
	 */
	public void setPlateRepository(Model_PlateRepository rep) {
		this.ThePlateRepository_Model = rep;
	}
	
	/**
	 * Returns the plate holding panel
	 * 
	 * @author BLM
	 */
	public Gui_PlateRepository getPlateRepository_GUI() {
		if (ThePlateRepository_Model == null)
			return null;
		return ThePlateRepository_Model.getGUI();
	}

	/**
	 * Returns the ExpDesignConnector so we can manage the XML data
	 * 
	 * @author Bjorn Millard
	 * @return ExpDesign_IO
	 */
	public ExpDesign_Model getExpDesignConnector() {
		return TheExpDesign_Model;
	}

	public ImageRail_SDCube getH5IO() {
		return TheImageRail_H5IO;
	}

	/**
	 * Constructs a new ImageRail_IO HDF project connector with the currently
	 * loaded project directory
	 * 
	 * @author BLM
	 */
	public void initH5IO(String inputPath, String outputPath) {

		TheImageRail_H5IO = null;
		try {
			TheImageRail_H5IO = new ImageRail_SDCube(inputPath, outputPath);
		} catch (Exception e) {
			System.out.println("Error creating the ImageRail_SDCube: ");
			e.printStackTrace();
		}

	}


	/**
	 * Returns the current input project directory
	 * 
	 * @author BLM
	 */
	public String getInputProjectPath() {

		return TheInputProjectPath;
	}

	/**
	 * Returns the current output project directory
	 * 
	 * @author BLM
	 */
	public String getOutputProjectPath() {

		return TheOutputProjectPath;
	}

	/**
	 * Sets the current project directory
	 * 
	 * @author BLM
	 */
	public void setInputProjectDirectory(File dir) {
		if (TheMainGUI != null)
			TheMainGUI.setTitle("Project: " + dir.getAbsolutePath());
		TheInputProjectPath = dir.getAbsolutePath();

		TheExpDesign_Model = new ExpDesign_Model(
TheInputProjectPath);
	}
	
	

	/**
	 * Loads the plate with the TIFF images in the given directory
	 * 
	 * @author BLM
	 * @throws H5IO_Exception 
	 */
	public void loadImageDirectory(File ImageDir_, Model_Plate plate,
			boolean copyImages) throws H5IO_Exception {

		File dir = ImageDir_;

		// Storing the images in the ImageDirectory
		File Images = new File(getInputProjectPath()
				+ File.separator + "Images");
		if (!Images.exists())
			Images.mkdir();

		// Option of copying or moving the image directory into the project

		if (copyImages)// copy
		{
			if (TheMainGUI != null)
				TheMainGUI.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			// Copying images to new ImageDirectory
			File newDir = new File(Images.getAbsolutePath() + File.separator
					+ "plate_" + (plate.getID() - 1));
			newDir.mkdir();
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = new File(newDir.getAbsolutePath() + File.separator
						+ files[i].getName());
				try {
					tools.ImageTools.copyFile(files[i], file);
				} catch (IOException e) {
					System.out.println("----*ERROR copying TIFF files*----");
					e.printStackTrace();
				}
			}
			dir = newDir;
			if (TheMainGUI != null)
				TheMainGUI.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		} else // Move images
		{

			File newFile = new File(Images.getAbsolutePath() + File.separator
					+ "plate_" + (plate.getID()));
			boolean result = dir.renameTo(newFile);
			if (!result) {
				System.out.println("Error moving images into project.");
				return;
			}
			dir = newFile;


		}

		// Waiting a second
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.out.println("Error Loading Images: ");
			e.printStackTrace();
		}



		for (int r = 0; r < plate.getWells().length; r++)
			for (int c = 0; c < plate.getWells()[0].length; c++) {
				// Getting all files tagged for this well
				File[] allFiles = tools.ImageTools.getFilesForGivenWell(dir,
						plate.getWells()[r][c]);
				// Organizing the images into sets of File[] in a an arraylist
				// where each element of the arrList is a File[] of each
				// wavelength for each field
				ArrayList<File[]> allSets = tools.ImageTools
				.getAllSetsOfCorresponsdingChanneledImageFiles(allFiles);
				int numFields = allSets.size();

				Model_Well well = plate.getWells()[r][c];
				well.setTheFields(new Model_Field[numFields]);
				for (int i = 0; i < numFields; i++) {
					plate.getWells()[r][c].getFields()[i] = new Model_Field(
							((File[]) allSets.get(i)), i, well);

				}
			}

		// Getting Number of unique channel names and adding Features based off
		// of wavelength - TODO features should be added better
		ChannelNames = tools.ImageTools.getNameOfUniqueChannels(dir
				.getParentFile());

		// Init Scaling parameters
		initScalingParameters();
		initFeatures(ChannelNames);

		if (TheMainGUI != null) {
			if (TheMainGUI.getLeftPanelDisplayed() == DOTPLOT) {
				TheMainGUI.setDotPlot(null);
				TheMainGUI.updateDotPlot();
			} else if (TheMainGUI.getLeftPanelDisplayed() == HISTOGRAM) {
				TheMainGUI.setHistogram(null);
				TheMainGUI.updateHistogramPlot();
			}
		}

		// creating a project directory if doesn't exist
		File f = new File(getInputProjectPath());
		if (!f.exists())
			f.mkdir();

		// Trying to load the well mean data from the HDF file if exists
		plate.loadWellMeanAndStdevData();

		if(TheMainGUI!=null)
		{
			TheMainGUI.updateFeatures();
			TheMainGUI.updateAllPlots();
			getPlateRepository_GUI().updatePanel();
			plate.getGUI().updatePanel();
			TheMainGUI.validate();
			TheMainGUI.repaint();
		}

	}
	
	// /**
	// * Sets the path of the HDF5 file that the Model will read
	// *
	// * @author BLM
	// * @param String
	// * inputPathToHDF5
	// * */
	// public void setInputProjectPath(String inputPath) {
	// TheInputProjectPath = inputPath;
	// }

	/**
	 * Sets the path of the HDF5 file that the Model will write to when data is
	 * generated
	 * 
	 * @author BLM
	 * @param String
	 *            outputPathToHDF5
	 * */
	public void setOutputProjectPath(String outPath) {
		TheOutputProjectPath = outPath;
	}

	/**
	 * Loads the plate with the TIFF images in the given directory
	 * 
	 * @author BLM
	 */
	public void loadProject(String inputProjectPath, String outputProjectPath) {

		long sTime = System.currentTimeMillis();
		try {

			setInputProjectDirectory(new File(inputProjectPath));
			setOutputProjectPath(outputProjectPath);
			File ProjectDir = new File(inputProjectPath);
			System.out.println("Loading Project: " + ProjectDir.getName());
			TheProjectParentDirectory = new File(ProjectDir.getParent());

			//Initialized the HDF5 I/O and creates the sample/well hash index
			initH5IO(TheInputProjectPath, TheOutputProjectPath);

			TheImageRail_H5IO.initHDF5ioSampleHash();

			/*
			 * INIT MODEL_PLATES AND GUIs
			 */
			// Looking for what sort of plates were loaded in this prior project
			ArrayList<int[]> plateSizes = TheImageRail_H5IO
					.getPlateSizes(TheImageRail_H5IO.INPUT);

			//We will create X plates where X == maxPlateID+1;
			int max = 0;
			int pSize = 96;
			for (int i = 0; i < plateSizes.size(); i++) {
				int[] one =  plateSizes.get(i);
				int id = one[0];
				if(id>max)
					max = id;
				if(i == 0)
				pSize = one[1];
				else
					if(pSize!=one[1])
						System.out.println("Project contains plates of different sizes*** This is currently not supported by ImageRail");
				}
			max++;
			ArrayList<Model_Plate> arr = new ArrayList<Model_Plate>();
			for (int i = 0; i < max; i++) {
				int numR = (int) Math.ceil(Math.sqrt(pSize / 1.5f));
				int numC = pSize / numR;
				arr.add(new Model_Plate(numR, numC, i, true));
			}

			Model_Plate[] plates = new Model_Plate[arr.size()];
			for (int p = 0; p < plates.length; p++)
 {
				plates[p] = arr.get(p);
				if (!GraphicsEnvironment.isHeadless())
					plates[p].initGUI();
			}
			// Creating the new plate holder with new plates
			ThePlateRepository_Model = new Model_PlateRepository(
					plates);

			int numplates = plates.length;
			for (int i = 0; i < numplates; i++) {

				Model_Plate plate = plates[i];
				
				File dir = new File(TheInputProjectPath
						+ File.separator + "Images" + File.separator + "plate_"
						+ i);

				// Looking for images for this plate in the projPath/Images
				// directory

				// Getting Number of unique channel names and adding Features
				// based off of wavelength - TODO features should be added
				// better

				String[] names = tools.ImageTools.getNameOfUniqueChannels(dir
						.getParentFile());

				if (names != null && names.length > 0)
					ChannelNames = names;

				/*
				 * Organizing the images and Initializing each Model_Field
				 */
				if (dir != null && dir.exists()) {
					for (int r = 0; r < plate.getNumRows(); r++)
						for (int c = 0; c < plate.getNumColumns(); c++) {
							// Getting all files tagged for this well
							File[] allFiles = tools.ImageTools
							.getFilesForGivenWell(dir, plate
									.getWells()[r][c]);
							// Organizing the images into sets of File[] in a an
							// arraylist where each element of the arrList is a
							// File[] of each wavelength for each field
							ArrayList<File[]> allSets = tools.ImageTools
							.getAllSetsOfCorresponsdingChanneledImageFiles(allFiles);
							int numFields = allSets.size();

							Model_Well well = plate.getWells()[r][c];
							well.setTheFields(new Model_Field[numFields]);
							for (int j = 0; j < numFields; j++)
								plate.getWells()[r][c].getFields()[j] = new Model_Field(
										((File[]) allSets.get(j)), j, well);
						}
				}


			}
			
			TheImageRail_H5IO.openHDF5(TheImageRail_H5IO.OUTPUT);
			TheImageRail_H5IO.writePlateCountAndSizes(plates.length,
					plates[0].getNumColumns() * plates[0].getNumRows(), TheImageRail_H5IO.OUTPUT);
			TheImageRail_H5IO.closeHDF5();
			

			initFeatures(ChannelNames);

			// Trying to load the well mean data from the HDF file if
			// exists

			for (int i = 0; i < numplates; i++)
				plates[i].loadWellMeanAndStdevData();

			loadFieldROIs();

			initScalingParameters();

			if(TheMainGUI!=null)
				TheMainGUI.setTitle("Project: "+TheInputProjectPath);

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (true) {
		// Checking that the loaded project contains the same
		// features as that of this version of ImageRail
		Feature[] feat = getFeatures();
		String[] names = new String[feat.length];
		for (int i = 0; i < feat.length; i++)
			names[i] = feat[i].getName().trim();
		StringBuffer[] projectNames = TheImageRail_H5IO
				.validateFeaturesUsedInProject(names);
		if (projectNames != null) {
			shutDown();
			if(TheMainGUI!=null)
				TheMainGUI.setVisible(false);
			
			TheStartupDialog.setVisible(true);
			System.out.println("...Failed validation of project Feature list");

			String st = "[Local]:";
			for (int i = 0; i < names.length; i++)
				st += "\n " + names[i];
			st += "\n\n[Project]:";
			for (int i = 0; i < projectNames.length; i++)
				st += "\n " + projectNames[i];

			JOptionPane
					.showMessageDialog(
							(Component) null,
							"**FEATURE MISMATCH ERROR**\n\nMeasurements in the loaded project do not match \nthose in this version of ImageRail. "
									+ "Please resolve by \nfinding missing Features files or reprocessing \nimages with your version of ImageRail."
									+ "\nContact: <bjornmillard@gmail.com> for further help.\n\n"
									+ st,
							"alert", JOptionPane.OK_OPTION);
			System.exit(0);
		}
		}
	}

	/**
	 * If fields already exist int he data.h5 file, then see if we should load
	 * ROIs
	 * 
	 * @author Bjorn Millard
	 * */
	public void loadFieldROIs() {
		TheImageRail_H5IO.openHDF5(TheImageRail_H5IO.INPUT);
		String h5path = TheInputProjectPath + File.separator
				+ "Data.h5";
		// Iterating through all fields and checking if they have ROIs to load
		Enumeration<String> keys = TheImageRail_H5IO.getHashtable_in().keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			if (key.indexOf("f") >= 0) {
				int plateIndex = Integer.parseInt((key.substring(1, key
						.indexOf("w"))));
				int wellIndex = Integer.parseInt(key.substring(
						key.indexOf("w") + 1, key.indexOf("f")));
				int fieldIndex = Integer.parseInt(key.substring(key
						.indexOf("f") + 1, key.length()));

				Model_PlateRepository rep = getPlateRepository();
				Model_Well well = rep.getWell(plateIndex, wellIndex);



				String fieldPath = TheImageRail_H5IO.getHashtable_in().get(key);
				ArrayList<Polygon> rois = TheImageRail_H5IO.readROIs(h5path,
						fieldPath);
				if (rois != null) {
					int num = rois.size();
					for (int i = 0; i < num; i++) {
						well.getFields()[fieldIndex].setROI(rois.get(i));
					}
				}
			}

		}
		TheImageRail_H5IO.closeHDF5();
	}

	/** */
	public boolean containsFile(File dir, String name) {
		File[] fs = dir.listFiles();
		for (int i = 0; i < fs.length; i++) {
			if (fs[i].getName().indexOf(name) >= 0)
				return true;
		}
		return false;
	}

	/**
	 * Loads the given file directory of images into the given Model_Plate
	 * 
	 * @author BLM
	 */
	public void load(File f, Model_Plate plate) {
		if (f.isDirectory()) {
				System.out.println(" 	---> Loading Image Directory");
				ImageDirectory = f;
			TheProjectParentDirectory = new File(f.getParent());
				
				try {
					loadImageDirectory(f, plate, false);
				} catch (H5IO_Exception e) {
					System.out.println("Error loading project directory: ");
					e.printStackTrace();
				}
		}

	}

	/**
	 * For packages that are extensible (ex: features and segmentors), this
	 * method looks at a given package for all .java files present and compiles
	 * un-compiled files into java Class files. It takes in the splash screen
	 * for status updates if desired
	 * 
	 * @author BLM
	 */
	static public void findAndCompileNewJavaFiles(String packageName,
			MainSplash splash) throws ClassNotFoundException {

		File f = new File("./" + packageName);
		File[] fs = f.listFiles();
		int len = fs.length;

		for (int i = 0; i < len; i++) {
			if (fs[i].getAbsolutePath().indexOf(".java") > 0) {
				String name = fs[i].getName();
				int ind = name.indexOf(".java");
				name = name.substring(0, ind);
				name = packageName + "." + name;
				String message = "Loading: " + name;
				if (splash != null)
					splash.setMessage(message);
				// else
				// System.out.println(message);

				String fileStub = name.replace('.', '/');
				// Build objects pointing to the source code (.java) and object
				// code (.class)

				String javaFilename = fileStub + ".java";
				String classFilename = fileStub + ".class";

				File javaFile = new File(javaFilename);
				File classFile = new File(classFilename);

				// System.out.println( "j "+javaFile.lastModified()+" c "+
				// classFile.lastModified() );
				// First, see if we want to try compiling. We do if (a) there
				// is source code, and either (b0) there is no object code,
				// or (b1) there is object code, but it's older than the source
				if (javaFile.exists()
						&& (!classFile.exists() || javaFile.lastModified() > classFile
								.lastModified())) {
					try {
						// Try to compile it. If this doesn't work, then
						// we must declare failure. (It's not good enough to use
						// and already-existing, but out-of-date, classfile)
						if (!compile(javaFilename, splash)
								|| !classFile.exists()) {
							throw new ClassNotFoundException("Compile failed: "
									+ javaFilename);
						}
					} catch (IOException ie) {
						// Another place where we might come to if we fail
						// to compile
						throw new ClassNotFoundException(ie.toString());
					}
				}
			}
		}

	}

	// Spawn a process to compile the java source code file
	// specified in the 'javaFile' parameter. Return a true if
	// the compilation worked, false otherwise.
	static public boolean compile(String javaFile, MainSplash splash)
	throws IOException {
		// Let the user know what's going on
		String message = "Compiling " + javaFile + "...";
		if (splash != null)
			splash.setMessage(message);
		else
			System.out.println(message);
		// get the classpath and make sure to pass it on to the compiler invocation
		String classPath = System.getProperty("java.class.path");
		String compileCommand = "javac -cp " + classPath + " " + javaFile;
		System.out.println("running command: " + compileCommand);
		// Start up the compiler
		Process p = Runtime.getRuntime().exec(compileCommand);
		// Wait for it to finish running
		try {
			p.waitFor();
		} catch (InterruptedException ie) {
			System.out.println(ie);
		}
		// Check the return code, in case of a compilation error
		int ret = p.exitValue();
		if (ret != 0) {
			java.io.BufferedReader errorStream = new java.io.BufferedReader(new java.io.InputStreamReader(p.getErrorStream()));
			String line;
			do {
				line = errorStream.readLine();
				System.out.println(line);
			} while (line != null);
		}
		
		// Tell whether the compilation worked
		return ret == 0;
	}

	/**
	 * Initializes the filtermanager. Note, this needs to be init after the
	 * MainGUI is initialized
	 * 
	 * @author BLM
	 * */
	public void initFilterManager() {
		TheFilterManager = new FilterManager();
	}


	/**
	 * Sets whether the GUI is running a processor
	 * 
	 * @author BLM
	 */
	public void setProcessing(boolean boo) {
		Processing = boo;
		StopProcessing = false;
	}

	/**
	 * Returns whether the user wants to stop the current image processing
	 * 
	 * @author BLM
	 * */
	public boolean shouldStop() {
		return !StopProcessing;
	}

	public void stopProcessing(boolean boo)
	{
		StopProcessing = boo;
	}
	
	/**
	 * Determines if the GUI is running a processor
	 * 
	 * @author BLM
	 */
	public boolean isProcessing() {
		return Processing;
	}

	/**
	 * Returns all features in an Array format
	 * 
	 * @author BLM
	 */
	public Feature[] getFeatures() {
		if (TheFeatures == null)
			return null;
		int len = TheFeatures.size();
		Feature[] f = new Feature[len];
		for (int i = 0; i < len; i++)
			f[i] = (Feature) TheFeatures.get(i);
		return f;
	}

	/**
	 * Returns the index of the feature with the given name
	 * 
	 * @author BLM
	 */
	public int getFeature_Index(String name) {
		int len = TheFeatures.size();
		for (int i = 0; i < len; i++)
			if (((Feature) TheFeatures.get(i)).getName().equalsIgnoreCase(name))
				return i;
			return -1;
	}

	/**
	 * Returns the number of channels represented in the loaded images
	 * 
	 * @author BLM
	 * */
	public int getNumberOfChannels() {
		if (getTheChannelNames() == null)
			return 0;
		return getTheChannelNames().length;
	}



	/**
	 * Returns the current ColorMap index that is selected
	 * 
	 * @author BLM
	 * */
	public int getTheColorMapIndex() {
		return TheColorMap;
	}

	/**
	 * Returns the max values foudn in the images
	 * 
	 * @author BLM
	 * */
	public double[] getMaxValues_ImageDisplay() {
		return MaxValues_ImageDisplay;
	}

	/**
	 * Returns the max values foudn in the images
	 * 
	 * @author BLM
	 * */
	public double[] getMinValues_ImageDisplay() {
		return MinValues_ImageDisplay;
	}

	/**
	 * Sets the max values foudn in the images
	 * 
	 * @author BLM
	 * */
	public void setMaxValues_ImageDisplay(double[] vals) {
		MaxValues_ImageDisplay = vals;
	}

	/**
	 * Sets the max values foudn in the images
	 * 
	 * @author BLM
	 * */
	public void setMinValues_ImageDisplay(double[] vals) {
		MinValues_ImageDisplay = vals;
	}

	/**
	 * Returns the ArrayList of Features that are currently loaded
	 * 
	 * @author BLM
	 * */
	public ArrayList<Feature> getTheFeatures() {
		return TheFeatures;
	}
	/**
	 * Returns the selected Feature
	 * 
	 * @author BLM
	 * */
	public Feature getTheSelectedFeature() {
		return TheSelectedFeature;
	}

	/**
	 * Sets the selected Feature
	 * 
	 * @author BLM
	 * */
	public void setTheSelectedFeature(Feature feature) {
		TheSelectedFeature = feature;
	}

	/**
	 * Sets the selected Feature index
	 * 
	 * @author BLM
	 * */
	public void setTheSelectedFeature_Index(int idx) {
		TheSelectedFeature_Index = idx;
	}

	/**
	 * Returns the selected Feature index
	 * 
	 * @author BLM
	 * */
	public int getTheSelectedFeature_Index() {
		return TheSelectedFeature_Index;
	}

	/**
	 * Returns the names of the channels that are loaded
	 * 
	 * @author BLM
	 * */
	public String[] getTheChannelNames() {
		return ChannelNames;
	}


	/**
	 * Returns boolean whether to subtract image background or not
	 * 
	 * @author BLM
	 * */
	public boolean getBackgroundSubtract() {
		return SubtractBackground;
	}

	/**
	 * Sets boolean whether to subtract image background or not
	 * 
	 * @author BLM
	 * */
	public void setBackgroundSubtract(boolean boo) {
		SubtractBackground = boo;
}

	/**
	 * Returns the HDF project connector with the currently loaded project
	 * directory
	 * 
	 * @author BLM
	 */
	public ImageRail_SDCube getImageRailio() {
		return TheImageRail_H5IO;
	}

	/**
	 * Adding all the features you desire to measure
	 * */
	public void initFeatures(String[] channelNames) {

		ArrayList<Feature> arr = new ArrayList<Feature>();
		try {
			// Try to load features from src tree, otherwise try deployed
			// location
			File f = new File("./src/features");
			if (!f.exists())
				f = new File("./features");
			File[] fs = f.listFiles();

			int len = fs.length;

			for (int i = 0; i < len; i++) {
				if (fs[i].getAbsolutePath().indexOf(".java") > 0
						&& !fs[i].getName().equalsIgnoreCase("Feature.java")
						&& !fs[i].getName().equalsIgnoreCase(
								"FeatureSorter.java")) {
					String path = fs[i].getName();
					int ind = path.indexOf(".java");
					path = path.substring(0, ind);
					// System.out.println("Loading Feature: "+ path);
					Class c = Class.forName("features." + path);
					arr.add((Feature) c.newInstance());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		int len = arr.size();
		TheFeatures = new ArrayList<Feature>();
		// System.out.println("Found "+len +" Features");
		for (int i = 0; i < len; i++) {
			Feature f = (arr.get(i));
			f.Name = f.getClass().toString();

			if (f.isMultiSpectralFeature() && channelNames != null) {
				for (int w = 0; w < channelNames.length; w++) {
					try {
						Feature fn = f.getClass().newInstance();
						fn.setChannelIndex(w);
						fn.setChannelName(channelNames[w]);
						fn.setName(channelNames[w]);
						TheFeatures.add(fn);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else
				TheFeatures.add(f);
		}

		FeatureSorter sorter = new FeatureSorter();
		Collections.sort(TheFeatures, sorter);
	}

	/**
	 * When creating a new project, this inits the project directory and the
	 * plates
	 */
	public boolean initNewPlates(int numPlates, int numRows, int numCols) {
		File dir = getTheDirectory();
		JFileChooser fc = null;
		if (dir != null)
			fc = new JFileChooser(dir);
		else
			fc = new JFileChooser();

		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {

			File file = fc.getSelectedFile();
			setTheDirectory(new File(file.getParent()));
			File newF = new File(file.getAbsolutePath() + ".sdc");
			//
			newF.mkdir();
			setInputProjectDirectory(newF);
			setOutputProjectPath(TheInputProjectPath);
			System.out.println("Creating Project: " + newF.getName());

			SDCube sdc = new SDCube();
			sdc.setPath(TheInputProjectPath);
			try {
				sdc.write();
			} catch (H5IO_Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//Initialized the HDF5 I/O and creates the sample/well hash index
//			initH5IO(TheInputProjectPath, TheOutputProjectPath);
//
//			TheImageRail_H5IO.initHDF5ioSampleHash();

			
			Model_Plate[] plates = new Model_Plate[numPlates];
			for (int p = 0; p < plates.length; p++) {
				plates[p] = new Model_Plate(numRows, numCols, p, true);
				plates[p].setTitle("Plate #" + p);
				plates[p].initGUI();
			}

			ThePlateRepository_Model = new Model_PlateRepository(
					plates);
			if(TheMainGUI!=null)
				TheMainGUI.setPlateRepositoryGUI(new Gui_PlateRepository(
						ThePlateRepository_Model));

			// Reinit the ExpDesignModel
			TheExpDesign_Model = new ExpDesign_Model(TheInputProjectPath);

			if(TheMainGUI!=null)
			{
				TheMainGUI.setTheInputPanel_Container(new JTabbedPane());
				for (int i = 0; i < numPlates; i++)
					TheMainGUI.getTheInputPanel_Container().addTab("Plate #" + (i + 1),
							new MidasInputPanel(plates[i], TheExpDesign_Model));
			}
			
			
			initH5IO(TheInputProjectPath, TheOutputProjectPath);
			try {
				TheImageRail_H5IO.createProject();
			} catch (H5IO_Exception e) {
				e.printStackTrace();
			}

			// Attempt To init the hashtable
			TheImageRail_H5IO.initHDF5ioSampleHash();
			
				TheImageRail_H5IO.writePlateCountAndSizes(numPlates,
						plates[0].getNumColumns() * plates[0].getNumRows(), TheImageRail_H5IO.OUTPUT);
		
			
			
			

			if (TheMainGUI != null) {
				TheMainGUI.getTheMainPanel().setLeftComponent(
						TheMainGUI.getTheInputPanel_Container());
				TheMainGUI.getTheMainPanel().setRightComponent(
						getPlateRepository_GUI());
				TheMainGUI.getTheMainPanel().setDividerLocation(
						TheMainGUI.getTheMainPanel().getDividerLocation());
				TheMainGUI.getTheMainPanel().validate();
				TheMainGUI.getTheMainPanel().repaint();
				TheMainGUI.repaint();
			}
			return true;
		}

		return false;
	}

	/**
	 * Returns the dot filter queue manager for this project
	 * 
	 * @author BLM
	 */
	public DotFilterQueue getFilterQueue() {
		if (TheFilterQueue == null)
			TheFilterQueue = new DotFilterQueue();
		return TheFilterQueue;
	}

	/**
	 * Prompts the user to select directory where new project will be created
	 * 
	 * @author BLM
	 */
	// public void createNewProject() {
	// File dir = getTheDirectory();
	// JFileChooser fc = null;
	// if (dir != null)
	// fc = new JFileChooser(dir);
	// else
	// fc = new JFileChooser();
	//
	// int returnVal = fc.showSaveDialog(null);
	// if (returnVal == JFileChooser.APPROVE_OPTION) {
	// File file = fc.getSelectedFile();
	// file.renameTo(new File(file.getAbsolutePath() + ".sdc"));
	// TheProjectParentDirectory = new File(file.getParent());
	// file.mkdir();
	// setInputProjectPath(file.getAbsolutePath());
	//
	// Model_Plate[] plates = new Model_Plate[1];
	// plates[0] = new Model_Plate(8, 12, 0, true);
	// plates[0].initGUI();
	// ThePlateRepository_Model = new Model_PlateRepository(plates);
	// int numplates = plates.length;
	//
	// if (TheMainGUI != null)
	// {
	// TheMainGUI.setPlateRepositoryGUI(new Gui_PlateRepository(
	// ThePlateRepository_Model));
	// TheMainGUI.setTheInputPanel_Container(new JTabbedPane());
	// for (int i = 0; i < numplates; i++)
	// TheMainGUI.getTheInputPanel_Container().addTab(
	// "Plate #" + (i + 1),
	// new MidasInputPanel(plates[i], TheExpDesign_Model));
	// }
	// try {
	// initH5IO(TheInputProjectPath,
	// TheOutputProjectPath);
	// if (TheImageRail_H5IO != null)
	// TheImageRail_H5IO.createProject();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// if(TheMainGUI!=null)
	// {
	// TheMainGUI.getTheMainPanel().setLeftComponent(TheMainGUI.getTheInputPanel_Container());
	// TheMainGUI.getTheMainPanel().setRightComponent(getPlateRepository_GUI());
	// TheMainGUI.getTheMainPanel().setDividerLocation(TheMainGUI.getTheMainPanel().getDividerLocation());
	// TheMainGUI.getTheMainPanel().validate();
	// TheMainGUI.getTheMainPanel().repaint();
	// TheMainGUI.repaint();
	// }
	//
	// } else
	// System.out.println("Open command cancelled by user.");
	// }

	/**
	 * Returns the scaling ratios available
	 * 
	 * @author BLM
	 * */
	public float[] getScalingRatios() {
		return ScalingRatios;

	}

	/**
	 * If cells have been deleted, this will be triggered... at which point when
	 * the program is closed, it will ask we we want to make these changes
	 * permenant
	 * 
	 * @author BLM
	 */
	public void setCellsModified(boolean cellsModified) {
		areDataSetsModified = cellsModified;
	}

	/**
	 * Default shutdown method. Checks if cells from the datasets have been
	 * removed; if so, asks if we want to resave the current state of the data
	 * set --> removing those cells from the source HDF5 files
	 * 
	 * @author BLM
	 */
	public void shutDown() {
		if (areDataSetsModified) {
			int result = JOptionPane
					.showConfirmDialog(
							(Component) null,
							"\nCells have been Deleted \n\n Would you like to save these changes \n in your project files?\n",
							"alert", JOptionPane.YES_NO_OPTION);

			if (result == 0) {
				resaveCells();
			}
		}
	}

	/**
	 * Shuts down and exits completely.
	 * 
	 * @author JLM
	 */
	public void shutDownAndExit() {
		shutDown();
		System.exit(0);
	}

	/**
	 * Resaves the current state of the data set by removing those cells from
	 * the source HDF5 files
	 * 
	 * @author BLM
	 */
	private void resaveCells() {
		ImageRail_SDCube io = getH5IO();

		Model_Plate[] plates = getPlateRepository().getPlates();
		int numP = plates.length;
		System.out.println("*** Saving Changes to Wells:");

		Feature[] features = getFeatures();
		StringBuffer[] featureNames = null;
		if (features != null && features.length > 0) {
			featureNames = new StringBuffer[features.length];
			for (int i = 0; i < features.length; i++)
				featureNames[i] = new StringBuffer(features[i].toString());
		}

		for (int i = 0; i < numP; i++) {
			Model_Well[][] wells = plates[i].getWells();
			for (int r = 0; r < wells.length; r++) {
				for (int c = 0; c < wells[0].length; c++) {
					Model_Well well = wells[r][c];
					if (well.areCellsModified()) {
						System.out.println("Well: " + wells[r][c].name);
						try {
							Model_Field[] fields = wells[r][c].getFields();
							for (int j = 0; j < fields.length; j++)
								fields[j].resaveCells(io);

						} catch (Exception e) {
							System.out
									.println("**Error Writing Cells during resave of HDF5 files");
							e.printStackTrace();
						}

						io.openHDF5(io.OUTPUT);
						if (well.Feature_Means != null && io != null) {
							io.writeWellMeans(plates[i].getPlateIndex(),
									well.getWellIndex(), well.Feature_Means);
						}
						if (well.Feature_Stdev != null && io != null)
							io.writeWellStdDevs(plates[i].getPlateIndex(),
									well.getWellIndex(), well.Feature_Stdev);
						io.closeHDF5();
						well.setCellsModified(false);
					}

				}
			}
		}
		setCellsModified(false);
	}

	// /** Gets the current time/date stamp to for unique sample IDs */
	// public String getTimestamp() {
	// Calendar cal = Calendar.getInstance();
	// SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	// return sdf.format(cal.getTime());
	// }

	/** Gets the current time/date stamp to for unique sample IDs */
	public String getTimestamp() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}

	/**
	 * Init Scaling parameters
	 * 
	 * @author BLM
	 * */
	public void initScalingParameters() {
		MaxValues_ImageDisplay = new double[getNumberOfChannels()];
		MinValues_ImageDisplay = new double[getNumberOfChannels()];
		for (int j = 0; j < MaxValues_ImageDisplay.length; j++)
			MaxValues_ImageDisplay[j] = (double) MAXPIXELVALUE;
	}



	/**
	 * Sets the colormap for the GUI display
	 */
	public void setColorMap(int colorMap) {
		TheColorMap = colorMap;
	}

	/**
	 * Gets the colormap for the GUI display
	 */
	public int getColorMap() {
		return TheColorMap;
	}

	
	/**
	 * Returns the singleton instance of The Model
	 * 
	 * @author BLM
	 */
	public static Model_Main getModel() {
		return TheMainModel;
	}

	/**
	 * Returns the GUI associated with this model
	 * 
	 */
	public MainGUI getGUI() {
		return TheMainGUI;
	}

	/**
	 * Sets the GUI associated with this model
	 */
	public void setGUI(MainGUI gui) {
		TheMainGUI = gui;
	}
}
