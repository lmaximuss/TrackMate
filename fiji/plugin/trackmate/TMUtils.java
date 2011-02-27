package fiji.plugin.trackmate;

import fiji.plugin.trackmate.Feature.Dimension;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.StackConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.TreeMap;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImagePlusAdapter;
import mpicbg.imglib.type.numeric.RealType;

/**
 * List of static utilities for the {@link TrackMate_} plugin
 */
public class TMUtils {
	
	/**
	 * Return a 3D stack or a 2D slice as an {@link Image} corresponding to the frame number <code>iFrame</code>
	 * in the given 4D or 3D {@link ImagePlus}.
	 * @param imp  the 4D or 3D source ImagePlus
	 * @param iFrame  the frame number to extract, 0-based
	 * @return  a 3D or 2D {@link Image} with the single time-point required 
	 */
	public static <T extends RealType<T>> Image<T> getSingleFrameAsImage(ImagePlus imp, int iFrame, Settings settings) {
		ImageStack stack = imp.getImageStack();
		ImageStack frame = new ImageStack(settings.xend-settings.xstart, settings.yend-settings.ystart, stack.getColorModel());
		int numSlices = imp.getNSlices();
		
		// ...create the slice by combining the ImageProcessors, one for each Z in the stack.
		ImageProcessor ip, croppedIp;
		Roi cropRoi = new Roi(settings.xstart-1, settings.ystart-1, settings.xend-settings.xstart, settings.yend-settings.ystart);
		for (int j = settings.zstart; j <= settings.zend; j++) {
			ip = stack.getProcessor(j + (iFrame * numSlices));
			ip .setRoi(cropRoi);
			croppedIp = ip.crop();
			frame.addSlice(Integer.toString(j + (iFrame * numSlices)), croppedIp);
		}
		
		ImagePlus ipSingleFrame = new ImagePlus(imp.getShortTitle()+"-Frame_" + Integer.toString(iFrame + 1), frame);
		return ImagePlusAdapter.wrap(ipSingleFrame);
	}


	/**
	 * Convenience static method that executes the thresholding part.
	 * <p>
	 * Given a list of spots, only spots with the feature satisfying the threshold given
	 * in argument are returned. 
	 */
	public static TreeMap<Integer, List<Spot>> thresholdSpots(final TreeMap<Integer, List<Spot>> spots, final FeatureThreshold featureThreshold) {
		TreeMap<Integer, List<Spot>> selectedSpots = new TreeMap<Integer, List<Spot>>();
		Collection<Spot> spotThisFrame, spotToRemove;
		List<Spot> spotToKeep;
		Float val, tval;	

		for (int timepoint : spots.keySet()) {

			spotThisFrame = spots.get(timepoint);
			spotToKeep = new ArrayList<Spot>(spotThisFrame);
			spotToRemove = new ArrayList<Spot>(spotThisFrame.size());

			tval = featureThreshold.value;
			if (null != tval) {

				if (featureThreshold.isAbove) {
					for (Spot spot : spotToKeep) {
						val = spot.getFeature(featureThreshold.feature);
						if (null == val)
							continue;
						if ( val < tval)
							spotToRemove.add(spot);
					}

				} else {
					for (Spot spot : spotToKeep) {
						val = spot.getFeature(featureThreshold.feature);
						if (null == val)
							continue;
						if ( val > tval)
							spotToRemove.add(spot);
					}
				}
				spotToKeep.removeAll(spotToRemove); // no need to treat them multiple times
				
			}
			
			selectedSpots.put(timepoint, spotToKeep);
		}
		return selectedSpots;
	}
	
	/**
	 * Convenience static method that executes the thresholding part.
	 * <p>
	 * Given a list of spots, only spots with the feature satisfying <b>all</b> of the thresholds given
	 * in argument are returned. 
	 */
	public static TreeMap<Integer, List<Spot>> thresholdSpots(final TreeMap<Integer, List<Spot>> spots, final List<FeatureThreshold> featureThresholds) {
		TreeMap<Integer, List<Spot>> selectedSpots = new TreeMap<Integer, List<Spot>>();
		Collection<Spot> spotThisFrame, spotToRemove;
		List<Spot> spotToKeep;
		Float val, tval;	
		
		for (int timepoint : spots.keySet()) {
			
			spotThisFrame = spots.get(timepoint);
			spotToKeep = new ArrayList<Spot>(spotThisFrame);
			spotToRemove = new ArrayList<Spot>(spotThisFrame.size());

			for (FeatureThreshold threshold : featureThresholds) {

				tval = threshold.value;
				if (null == tval)
					continue;
				spotToRemove.clear();

				if (threshold.isAbove) {
					for (Spot spot : spotToKeep) {
						val = spot.getFeature(threshold.feature);
						if (null == val)
							continue;
						if ( val < tval)
							spotToRemove.add(spot);
					}

				} else {
					for (Spot spot : spotToKeep) {
						val = spot.getFeature(threshold.feature);
						if (null == val)
							continue;
						if ( val > tval)
							spotToRemove.add(spot);
					}
				}
				spotToKeep.removeAll(spotToRemove); // no need to treat them multiple times
			}
			selectedSpots.put(timepoint, spotToKeep);
		}
		return selectedSpots;
	}
	
	
	
	/*
	 * PRIVATE METHODS
	 */

	
	/**
     * Returns an estimate of the <code>p</code>th percentile of the values
     * in the <code>values</code> array. Taken from commons-math.
	 */
	public static final double getPercentile(final double[] values, final double p) {

		final int size = values.length;
		if ((p > 1) || (p <= 0)) {
            throw new IllegalArgumentException("invalid quantile value: " + p);
        }
        if (size == 0) {
            return Double.NaN;
        }
        if (size == 1) {
            return values[0]; // always return single value for n = 1
        }
        double n = (double) size;
        double pos = p * (n + 1);
        double fpos = Math.floor(pos);
        int intPos = (int) fpos;
        double dif = pos - fpos;
        double[] sorted = new double[size];
        System.arraycopy(values, 0, sorted, 0, size);
        Arrays.sort(sorted);

        if (pos < 1) {
            return sorted[0];
        }
        if (pos >= n) {
            return sorted[size - 1];
        }
        double lower = sorted[intPos - 1];
        double upper = sorted[intPos];
        return lower + dif * (upper - lower);
	}
	
	
	/** 
	 * Returns <code>[range, min, max]</code> of the given double array.
	 * @return A double[] of length 3, where index 0 is the range, index 1 is the min, and index 2 is the max.
	 */
	public static final double[] getRange(final double[] data) {
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		double value;
		for (int i = 0; i < data.length; i++) {
			value = data[i];
			if (value < min) min = value;
			if (value > max) max = value;
		}		
		return new double[] {(max-min), min, max};
	}

	/**
	 * Return the feature values of this Spot collection as a new double array.
	 */
	public static final double[] getFeature(final Collection<SpotImp> spots, final Feature feature) {
		final double[] values = new double[spots.size()];
		int index = 0;
		for(SpotImp spot : spots) {
			values[index] = spot.getFeature(feature);
			index++;
		}
		return values;
	}
	
	/**
	 * Return a map of {@link Feature} values for the spot collection given.
	 * Each feature maps a double array, with 1 element per {@link Spot}, all pooled
	 * together.
	 */
	public static EnumMap<Feature, double[]> getFeatureValues(Collection<? extends Collection<Spot>> spots) {
		 EnumMap<Feature, double[]> featureValues = new  EnumMap<Feature, double[]>(Feature.class);
		if (null == spots || spots.isEmpty())
			return featureValues;
		int index;
		Float val;
		boolean noDataFlag = true;
		// Get the total quantity of spot we have
		int spotNumber = 0;
		for(Collection<? extends Spot> collection : spots)
			spotNumber += collection.size();
		
		for(Feature feature : Feature.values()) {
			// Make a double array to comply to JFreeChart histograms
			double[] values = new double[spotNumber];
			index = 0;
			for(Collection<? extends Spot> collection : spots) {
				for (Spot spot : collection) {
					val = spot.getFeature(feature);
					if (null == val)
						continue;
					values[index] = val; 
					index++;
					noDataFlag = false;
				}
				if (noDataFlag)
					featureValues.put(feature, null);
				else 
					featureValues.put(feature, values);
			}
		}
		return featureValues;
	}


	
	/**
	 * Return the optimal bin number for a histogram of the data given in array, using the 
	 * Freedman and Diaconis rule (bin_space = 2*IQR/n^(1/3)).
	 * It is ensured that the bin number returned is not smaller and no bigger than the bounds given
	 * in argument.
	 */
	public static final int getNBins(final double[] values, int minBinNumber, int maxBinNumber) {
		final int size = values.length;
		final double q1 = getPercentile(values, 0.25);
		final double q3 = getPercentile(values, 0.75);
		final double iqr = q3 - q1;
		final double binWidth = 2 * iqr * Math.pow(size, -0.33);
		final double[] range = getRange(values);
		int nBin = (int) ( range[0] / binWidth + 1 );
		if (nBin > maxBinNumber)
			nBin = maxBinNumber;
		else if (nBin < minBinNumber)
			nBin = minBinNumber;
		return  nBin;
	}
	
	/**
	 * Return the optimal bin number for a histogram of the data given in array, using the 
	 * Freedman and Diaconis rule (bin_space = 2*IQR/n^(1/3)).
	 * It is ensured that the bin number returned is not smaller than 8 and no bigger than 256.
	 */
	public static final int getNBins(final double[] values){
		return getNBins(values, 8, 256);
	}
	

	/**
	 * Create a histogram from the data given.
	 */
	public static final int[] histogram(final double data[], final int nBins) {
		final double[] range = getRange(data);
		final double binWidth = range[0]/nBins;
		final int[] hist = new int[nBins];
		int index;

		if (nBins > 0)
			for (int i = 0; i < data.length; i++) {
				index = Math.min((int) Math.floor((data[i] - range[1]) / binWidth), nBins - 1);
				hist[index]++;
			}
		return hist;
	}
	
	/**
	 * Create a histogram from the data given, with a default number of bins given by {@link #getNBins(double[])}.
	 * @param data
	 * @return
	 */
	public static final int[] histogram(final double data[]) {
		return histogram(data, getNBins(data));
	}
	
	/**
	 * Return a threshold for the given data, using an Otsu histogram thresholding method.
	 */
	public static final double otsuThreshold(double[] data) {
		return otsuThreshold(data, getNBins(data));
	}
	
	/**
	 * Return a threshold for the given data, using an Otsu histogram thresholding method with a given bin number.
	 */
	public static final double otsuThreshold(double[] data, int nBins) {
		final int[] hist = histogram(data, nBins);
		final int thresholdIndex = otsuThresholdIndex(hist, data.length);
		double[] range = getRange(data);
		double binWidth = range[0] / nBins;
		return 	range[1] + binWidth * thresholdIndex;
	}
	
	/**
	 * Given a histogram array <code>hist</code>, built with an initial amount of <code>nPoints</code>
	 * data item, this method return the bin index that thresholds the histogram in 2 classes. 
	 * The threshold is performed using the Otsu Threshold Method, 
	 * {@link http://www.labbookpages.co.uk/software/imgProc/otsuThreshold.html}.
	 * @param hist  the histogram array
	 * @param nPoints  the number of data items this histogram was built on
	 * @return the bin index of the histogram that thresholds it
	 */
	public static final int otsuThresholdIndex(final int[] hist, final int nPoints)	{
		int total = nPoints;

		double sum = 0;
		for (int t = 0 ; t < hist.length ; t++) 
			sum += t * hist[t];

		double sumB = 0;
		int wB = 0;
		int wF = 0;

		double varMax = 0;
		int threshold = 0;

		for (int t = 0 ; t < hist.length ; t++) {
			wB += hist[t];               // Weight Background
			if (wB == 0) continue;

			wF = total - wB;                 // Weight Foreground
			if (wF == 0) break;

			sumB += (float) (t * hist[t]);

			double mB = sumB / wB;            // Mean Background
			double mF = (sum - sumB) / wF;    // Mean Foreground

			// Calculate Between Class Variance
			double varBetween = (float)wB * (float)wF * (mB - mF) * (mB - mF);

			// Check if new maximum found
			if (varBetween > varMax) {
				varMax = varBetween;
				threshold = t;
			}
		}
		return threshold;
	}
	
	/**
	 * Computes the square Euclidean distance between two Featurable.
	 * @param i Spot i.
	 * @param j Spot j.
	 * @return The Euclidean distance between Featurable i and j, based on their
	 * position features X, Y, Z.
	 */
	public static final double euclideanDistanceSquared(Spot i, Spot j) {
		final Float xi, xj, yi, yj, zi, zj;
		double eucD = 0;
		xi = i.getFeature(Feature.POSITION_X);
		xj = j.getFeature(Feature.POSITION_X);
		yi = i.getFeature(Feature.POSITION_Y);
		yj = j.getFeature(Feature.POSITION_Y);
		zi = i.getFeature(Feature.POSITION_Z);
		zj = j.getFeature(Feature.POSITION_Z);

		if (xj != null && xi != null)
			eucD += (xj-xi)*(xj-xi);
		if (yj != null && yi != null)
			eucD += (yj-yi)*(yj-yi);
		if (zj != null && zi != null)
			eucD += (zj-zi)*(zj-zi);
		return eucD;
	}

	
	/**
	 * Ensure an 8-bit gray image is sent to the 3D viewer.
	 */
	public static final ImagePlus[] makeImageForViewer(final Settings settings) {
		final ImagePlus origImp = settings.imp;
		origImp.killRoi();
		final ImagePlus imp;
		
		if (origImp.getType() == ImagePlus.GRAY8)
			imp = origImp;
		else {
			imp = new Duplicator().run(origImp);
			new StackConverter(imp).convertToGray8();
		}
		
		int nChannels = imp.getNChannels();
		int nSlices = settings.nslices;
		int nFrames = settings.nframes;
		ImagePlus[] ret = new ImagePlus[nFrames];
		int w = imp.getWidth(), h = imp.getHeight();

		ImageStack oldStack = imp.getStack();
		String oldTitle = imp.getTitle();
		
		for(int i = 0; i < nFrames; i++) {
			
			ImageStack newStack = new ImageStack(w, h);
			for(int j = 0; j < nSlices; j++) {
				int index = imp.getStackIndex(1, j+1, i+settings.tstart+1);
				Object pixels;
				if (nChannels > 1) {
					imp.setPositionWithoutUpdate(1, j+1, i+1);
					pixels = new ColorProcessor(imp.getImage()).getPixels();
				}
				else
					pixels = oldStack.getPixels(index);
				newStack.addSlice(oldStack.getSliceLabel(index), pixels);
			}
			ret[i] = new ImagePlus(oldTitle	+ " (frame " + i + ")", newStack);
			ret[i].setCalibration(imp.getCalibration().copy());
			
		}
		return ret;
	}
	
	/**
	 * Return a String unit for the given dimension. When suitable, the unit is taken from the settings
	 * field, which contains the spatial and time units. Otherwise, default units are used.
	 */
	public static final String getUnitsFor(final Dimension dimension, final Settings settings) {
		String units = "no unit";
		switch (dimension) {
		case ANGLE:
			units = "Radians";
			break;
		case INTENSITY:
			units = "Counts";
			break;
		case INTENSITY_SQUARED:
			units = "Counts^2";
			break;
		case NONE:
			units = "";
			break;
		case POSITION:
		case SIZE:
			units = settings.spaceUnits;
			break;
		case QUALITY:
			units = "Quality";
			break;
		case TIME:
			units = settings.timeUnits;
			break;
		}
		return units;
	}
	

}
