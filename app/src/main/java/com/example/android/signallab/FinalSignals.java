package com.example.android.signallab;
import android.util.Log;

import java.util.ArrayList;

public class FinalSignals {
	private double[] bpmArray;
	private double[] redSignal; 
	private double[] greenSignal; 
	private double[] blueSignal; 
	private double[] peaks;
	
	public FinalSignals(double[] red, double[] green, double[] blue, double[] freq) {
		
		ArrayList<Integer> freqIdxs = new ArrayList<Integer>();   

		  for (int i = 0; i < freq.length - 1; i++) {
			  	double freqVal = freq[i]; 
		  		if ((freqVal > 50) && (freqVal < 200.1)) { //Only retrieve frequencies corresponding to 40-200BPM
		  				freqIdxs.add(i);
		  		}
		  }
	      double[] peaks = new double[3]; //Index 0-Red, 1-Green & 2-Blue
		  double[] bpmArray = new double[freqIdxs.size()];
		  double[] redSignal = new double[freqIdxs.size()]; 
		  double[] greenSignal = new double[freqIdxs.size()]; 
		  double[] blueSignal = new double[freqIdxs.size()]; 
		  
		  int i = 0;
	      double maxRed = -1.0;
	      double maxGreen = -1.0;
	      double maxBlue = -1.0;
		  for (int j : freqIdxs) {
			  //Add frequency values to final vector
			  bpmArray[i] = freq[j];
			  redSignal[i] = red[j]; 
			  greenSignal[i] = green[j]; 
			  blueSignal[i] = blue[j];
			  //Check new max peak
			  if (red[j] > maxRed) {
				  maxRed = red[j];
				  peaks[0] = Math.round(freq[j] * 10) / 10.0;
				  Log.i("maxR", Integer.toString(j));
			  }
			  if (green[j] > maxGreen) {
				  maxGreen = green[j];
				  peaks[1] = Math.round(freq[j] * 10) / 10.0;
				  Log.i("maxG", Integer.toString(j));
			  }
			  if (blue[j] > maxBlue) {
				  maxBlue = blue[j];
				  peaks[2] = Math.round(freq[j] * 10) / 10.0;
				  Log.i("maxB", Integer.toString(j));
			  }
			  i++;
		  }
		  this.bpmArray = bpmArray;
		  this.redSignal = redSignal;
		  this.greenSignal = greenSignal;
		  this.blueSignal = blueSignal;
		  this.peaks = peaks;
	}
	
	public double[] getFreq() {
		return this.bpmArray;
	}
	
	public double[] getRed() {
		return this.redSignal;
	}
	
	public double[] getGreen() {
		return this.greenSignal;
	}
	
	public double[] getBlue() {
		return this.blueSignal;
	}

	public double[] getPeaks() {
		return this.peaks;
	}
	
	
}
