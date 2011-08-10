/**  
   ImageRail:
   Software for high-throughput microscopy image analysis

   Copyright (C) 2011 Bjorn Millard <bjornmillard@gmail.com>

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package models;

import gui.MainGUI;

import java.util.ArrayList;

public class Model_ParameterSet {
	private String WellName;
	private String ThresholdChannel_nuc_Name;
	private String ThresholdChannel_cyto_Name;
	private String ThresholdChannel_membrane_Name;
	private int ThresholdChannel_nuc_Index;
	private int ThresholdChannel_cyto_Index;
	private int ThresholdChannel_membrane_Index;
	private float Threshold_Nucleus;
	private float Threshold_Cytoplasm;
	private float Threshold_Background;
	private float Threshold_Membrane;
	private int AnnulusSize;
	private String MeanOrIntegrated;
	private boolean Modified;
	private String ProcessType;
	private String CoordsToSaveToHDF;
	private int NumThreads;
	private ArrayList<Float> GeneralParameters;
	private ArrayList<String> GeneralParameters_names;

	static public String MEAN = "MEAN";
	static public String INTEGRATED = "INTEGRATED";
	static public int NOVALUE = -1;
	static public String SINGLECELL = "SingleCell";
	static public String WELLMEAN = "WellMean";
	static public String UNPROCESSED = "Unprocessed";

	public Model_ParameterSet() {
		Modified = false;
		WellName = null;
		ProcessType = UNPROCESSED;
		ThresholdChannel_nuc_Name = null;
		ThresholdChannel_cyto_Name = null;
		ThresholdChannel_membrane_Name = null;
		Threshold_Nucleus = NOVALUE;
		Threshold_Cytoplasm = NOVALUE;
		Threshold_Background = NOVALUE;
		Threshold_Membrane = NOVALUE;
		AnnulusSize = NOVALUE;
		CoordsToSaveToHDF = "BoundingBox"; // Default
		NumThreads = 1;
		MeanOrIntegrated = null;
		GeneralParameters = new ArrayList<Float>();
		GeneralParameters_names = new ArrayList<String>();
	}

	/**
	 * Returns common parameter set if it exists
	 * 
	 * @author BLM
	 */
	static public Model_ParameterSet doWellsHaveSameParameterSet(Model_Well[] wells) {
		Model_ParameterSet dummy = null;
		boolean initDummy = false;
		int len = wells.length;
		if (len == 1 && wells[0].TheParameterSet != null)
			return Model_ParameterSet.copy(wells[0].TheParameterSet);

		for (int i = 0; i < len; i++) {
			Model_ParameterSet p = wells[i].TheParameterSet;
			if (p != null) {
				if (!initDummy) {
					if (p.ProcessType.equalsIgnoreCase(UNPROCESSED))
						return null;
					dummy = Model_ParameterSet.copy(p);
					initDummy = true;
				} else if (!Model_ParameterSet.isSame(p, dummy))
					return null;
			} else
				return null;
		}

		return dummy;
	}

	/**
	 * Makes a copy of the given parameter set
	 * 
	 * @author BLM
	 */
	static public Model_ParameterSet copy(Model_ParameterSet in) {
		Model_ParameterSet out = new Model_ParameterSet();
		out.Modified = true;
		out.WellName = in.WellName;
		out.ProcessType = in.ProcessType;
		out.ThresholdChannel_nuc_Name = in.ThresholdChannel_nuc_Name;
		out.ThresholdChannel_cyto_Name = in.ThresholdChannel_cyto_Name;
		out.ThresholdChannel_membrane_Name = in.ThresholdChannel_membrane_Name;
		out.Threshold_Nucleus = in.Threshold_Nucleus;
		out.Threshold_Cytoplasm = in.Threshold_Cytoplasm;
		out.Threshold_Membrane = in.Threshold_Membrane;
		out.Threshold_Background = in.Threshold_Background;
		out.AnnulusSize = in.AnnulusSize;
		out.MeanOrIntegrated = in.MeanOrIntegrated;
		out.ProcessType = in.ProcessType;

		return out;
	}

	/**
	 * Compares two parameter sets, but the well name does not have to be the
	 * same
	 * 
	 * @author BLM
	 */
	static public boolean isSame(Model_ParameterSet p1, Model_ParameterSet p2) {
		if (p1.Modified != p2.Modified)
			return false;
		if (!p1.ProcessType.equalsIgnoreCase(p2.ProcessType))
			return false;
		if (p1.ThresholdChannel_nuc_Name != null
				&& p2.ThresholdChannel_nuc_Name != null
				&& !p1.ThresholdChannel_nuc_Name
						.equalsIgnoreCase(p2.ThresholdChannel_nuc_Name))
			return false;
		if (p1.ThresholdChannel_cyto_Name != null
				&& p2.ThresholdChannel_cyto_Name != null
				&& !p1.ThresholdChannel_cyto_Name
						.equalsIgnoreCase(p2.ThresholdChannel_cyto_Name))
			return false;
		if (p1.ThresholdChannel_membrane_Name != null
				&& p2.ThresholdChannel_membrane_Name != null
				&& !p1.ThresholdChannel_membrane_Name
						.equalsIgnoreCase(p2.ThresholdChannel_membrane_Name))
			return false;
		if (p1.Threshold_Nucleus != p2.Threshold_Nucleus)
			return false;
		if (p1.Threshold_Cytoplasm != p2.Threshold_Cytoplasm)
			return false;
		if (p1.Threshold_Background != p2.Threshold_Background)
			return false;
		if (p1.Threshold_Membrane != p2.Threshold_Membrane)
			return false;
		if (p1.AnnulusSize != p2.AnnulusSize)
			return false;
		return true;
	}

	public String toString() {
		String st = "*******\n";
		st += "Name=" + WellName + "\n";
		st += "ProcessType=" + ProcessType + "\n";
		st += "Threshold_nuc_Channel=" + ThresholdChannel_nuc_Name + "\n";
		st += "Threshold_cyto_Channel=" + ThresholdChannel_cyto_Name + "\n";
		st += "ThresholdChannel_nuc_Index=" + ThresholdChannel_nuc_Index + "\n";
		st += "ThresholdChannel_cyto_Index=" + ThresholdChannel_cyto_Index
				+ "\n";
		st += "ThresholdChannel_membrane_Index="
				+ ThresholdChannel_membrane_Index + "\n";
		st += "Threshold_Nucleus=" + Threshold_Nucleus + "\n";
		st += "Threshold_Cytoplasm=" + Threshold_Cytoplasm + "\n";
		st += "Threshold_Bkgd=" + Threshold_Background + "\n";
		st += "Threshold_Membrane=" + Threshold_Membrane + "\n";
		st += "AnnulusSize=" + AnnulusSize + "\n";
		st += "MeanOrIntegrated=" + MeanOrIntegrated + "\n";
		st += "StoreCells="
				+ MainGUI.getGUI().getLoadCellsImmediatelyCheckBox()
						.isSelected() + "\n";
		st += "********\n";
		return st;
	}

	/**
	 * Model_Field Getter/Setters
	 * 
	 * @author BLM
	 * */
	public String getWellName() {
		return WellName;
	}

	public void setWellName(String wellName) {
		WellName = wellName;
	}

	public String getThresholdChannel_nuc_Name() {
		return ThresholdChannel_nuc_Name;
	}

	public void setThresholdChannel_nuc_Name(String thresholdChannelNucName) {
		ThresholdChannel_nuc_Name = thresholdChannelNucName;
	}

	public String getThresholdChannel_cyto_Name() {
		return ThresholdChannel_cyto_Name;
	}

	public void setThresholdChannel_cyto_Name(String thresholdChannelCytoName) {
		ThresholdChannel_cyto_Name = thresholdChannelCytoName;
	}

	public String getThresholdChannel_membrane_Name() {
		return ThresholdChannel_membrane_Name;
	}

	public void setThresholdChannel_membrane_Name(
			String thresholdChannelMembraneName) {
		ThresholdChannel_membrane_Name = thresholdChannelMembraneName;
	}

	public int getThresholdChannel_nuc_Index() {
		return ThresholdChannel_nuc_Index;
	}

	public void setThresholdChannel_nuc_Index(int thresholdChannelNucIndex) {
		ThresholdChannel_nuc_Index = thresholdChannelNucIndex;
	}

	public int getThresholdChannel_cyto_Index() {
		return ThresholdChannel_cyto_Index;
	}

	public void setThresholdChannel_cyto_Index(int thresholdChannelCytoIndex) {
		ThresholdChannel_cyto_Index = thresholdChannelCytoIndex;
	}

	public int getThresholdChannel_membrane_Index() {
		return ThresholdChannel_membrane_Index;
	}

	public void setThresholdChannel_membrane_Index(
			int thresholdChannelMembraneIndex) {
		ThresholdChannel_membrane_Index = thresholdChannelMembraneIndex;
	}

	public float getThreshold_Nucleus() {
		return Threshold_Nucleus;
	}

	public void setThreshold_Nucleus(float thresholdNucleus) {
		Threshold_Nucleus = thresholdNucleus;
	}

	public float getThreshold_Cytoplasm() {
		return Threshold_Cytoplasm;
	}

	public void setThreshold_Cytoplasm(float thresholdCell) {
		Threshold_Cytoplasm = thresholdCell;
	}

	public void setThreshold_Membrane(float thresholdMembrane) {
		Threshold_Membrane = thresholdMembrane;
	}

	public float getThreshold_Membrane() {
		return Threshold_Membrane;
	}

	public float getThreshold_Background() {
		return Threshold_Background;
	}

	public void setThreshold_Background(float thresholdBackground) {
		Threshold_Background = thresholdBackground;
	}

	public int getAnnulusSize() {
		return AnnulusSize;
	}

	public void setAnnulusSize(int annulusSize) {
		AnnulusSize = annulusSize;
	}

	public String getMeanOrIntegrated() {
		return MeanOrIntegrated;
	}

	public void setMeanOrIntegrated(String meanOrIntegrated) {
		MeanOrIntegrated = meanOrIntegrated;
	}

	public boolean isModified() {
		return Modified;
	}

	public void setModified(boolean modified) {
		Modified = modified;
	}

	public String getProcessType() {
		return ProcessType;
	}

	public void setProcessType(String processType) {
		ProcessType = processType;
	}

	public String getCoordsToSaveToHDF() {
		return CoordsToSaveToHDF;
	}

	public void setCoordsToSaveToHDF(String coordsToSaveToHDF) {
		CoordsToSaveToHDF = coordsToSaveToHDF;
	}

	/** */
	public void setNumThreads(int nThreads) {
		NumThreads = nThreads;
	}

	public int getNumThreads() {
		return NumThreads;
	}

	public void clearGeneralParameters() {
		GeneralParameters = new ArrayList<Float>();
		GeneralParameters_names = new ArrayList<String>();
	}

	public void addGeneralParameter(String name, Float val) {
		GeneralParameters.add(val);
		GeneralParameters_names.add(name);
	}

	public float getGeneralParameter(int index) {
		return GeneralParameters.get(index).floatValue();
	}

	public String getGeneralParameter_name(int index) {
		return GeneralParameters_names.get(index);
	}
}
