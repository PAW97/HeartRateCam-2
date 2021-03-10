package com.example.android.signallab;


import android.util.Log;

import java.util.Arrays;

public class SignalProcessing {
    	public double [] freq(double [] redSignal,double[] greenSignal,double[] blueSignal, double time, double samples)
		{

			//Import rgb-signals here
			//double[] redSignal = null; //CHANGE
			//double[] greenSignal = null; //CHANGE
			//double[] blueSignal = null; //CHANGE

			//Constants
			//måste Samplingrate vara en integer?
			double sampleRate = samples/time; //CHANGE
			int SAMPLINGRATE = (int) Math.round(sampleRate);
			//double FRAMERATE = (double) 1 / SAMPLINGRATE;
			//int FRAMES = redSignal.length;
			Log.i("here", "Sampling Rate: " + Integer.toString(SAMPLINGRATE));
			int BPM = 60;
			boolean onlyPositive = true;

			long startTime = System.currentTimeMillis();    //Start timer to check processing time

			//Detrend
			Detrend d1 = new Detrend(redSignal, "linear");
			redSignal = d1.detrendSignal();
			Detrend d2 = new Detrend(greenSignal, "linear");
			greenSignal = d2.detrendSignal();
			Detrend d3 = new Detrend(blueSignal, "linear");
			blueSignal = d3.detrendSignal();

			//Zero padding to 450 samples
			if (redSignal.length < 450) {
				double[] redZero = new double[450];
				double[] greenZero = new double[450];
				double[] blueZero = new double[450];

				for (int i = 0; i < redSignal.length; i++) {
					redZero[i] = redSignal[i];
					greenZero[i] = greenSignal[i];
					blueZero[i] = blueSignal[i];
				}
				for (int i = redSignal.length; i < 450; i++) {
					redZero[i] = 0;
					greenZero[i] = 0;
					blueZero[i] = 0;
				}
				redSignal = redZero;
				greenSignal = greenZero;
				blueSignal = blueZero;
			}

			//FFT
			DiscreteFourier fft1 = new DiscreteFourier(redSignal);
			fft1.dft();
			double[] fftRedSignal = fft1.returnAbsolute(onlyPositive);

			DiscreteFourier fft2 = new DiscreteFourier(greenSignal);
			fft2.dft();
			double[] fftGreenSignal = fft2.returnAbsolute(onlyPositive);

			DiscreteFourier fft3 = new DiscreteFourier(blueSignal);
			fft3.dft();
			double[] fftBlueSignal = fft3.returnAbsolute(onlyPositive);

			//Create array with frequency at idx i corrresponding to fft value at idx i
			double[] freqArray = new double[fftGreenSignal.length];
			double step = 0.5 * SAMPLINGRATE / fftGreenSignal.length;
			for (int i = 0; i < freqArray.length; i++) {
				freqArray[i] = Math.round(i * step * 100) / 100.0 * BPM; //Convert Hz to Heart Rate (Rounded to 2 decimals)
			}

			//Create frequency vector containing only values between 40 - 200 BPM, aswell as a corresponding x-axis vector
			//Also get the maximum peak frequencies
			FinalSignals bpmSignals = new FinalSignals(fftRedSignal, fftGreenSignal, fftBlueSignal, freqArray);

			//Max peaks in 40-200BPM
			double[] peaks = bpmSignals.getPeaks(); //Index 0-Red, 1-Green & 2-Blue
			double [] r = bpmSignals.getRed();
			double [] g = bpmSignals.getGreen();
			double [] b = bpmSignals.getBlue();
			double [] fre = bpmSignals.getFreq();

			//End processing time check
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			//Log.i("here","The processing time was: " + elapsedTime + " ms");
			Log.i("here", Arrays.toString(r));
			Log.i("here", Arrays.toString(g));
			Log.i("here", Arrays.toString(b));
			Log.i("here", Arrays.toString(fre));

			//Log.i("here","Red: " + peaks[0] + " Green: " + peaks[1] + " Blue: " + peaks[2]);    //Print peaks for the 3 channels

			return peaks;

		}
   }
