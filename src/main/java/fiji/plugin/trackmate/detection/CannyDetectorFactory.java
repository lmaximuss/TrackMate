package fiji.plugin.trackmate.detection;


import static fiji.plugin.trackmate.detection.DetectorKeys.DEFAULT_DO_MEDIAN_FILTERING;
import static fiji.plugin.trackmate.detection.DetectorKeys.DEFAULT_DO_SUBPIXEL_LOCALIZATION;
import static fiji.plugin.trackmate.detection.DetectorKeys.DEFAULT_RADIUS;
import static fiji.plugin.trackmate.detection.DetectorKeys.DEFAULT_TARGET_CHANNEL;
import static fiji.plugin.trackmate.detection.DetectorKeys.DEFAULT_THRESHOLD;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_DOWNSAMPLE_FACTOR;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_DO_MEDIAN_FILTERING;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_DO_SUBPIXEL_LOCALIZATION;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_RADIUS;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_THRESHOLD;
import static fiji.plugin.trackmate.io.IOUtils.readDoubleAttribute;
import static fiji.plugin.trackmate.io.IOUtils.readIntegerAttribute;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom2.Element;
import org.scijava.plugin.Plugin;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.gui.ConfigurationPanel;
import fiji.plugin.trackmate.gui.panels.detector.CannyDetectorConfigurationPanel;
import fiji.plugin.trackmate.util.TMUtils;
import net.imagej.ImgPlus;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;





@Plugin( type = SpotDetectorFactory.class )
public class CannyDetectorFactory< T extends RealType< T > & NativeType< T >> extends LogDetectorFactory< T > 
{
	/*
	 * CONSTANTS
	 */

	/** A string key identifying this factory. */
	public static final String THIS_DETECTOR_KEY = "CANNY_EDGE_DETECTOR";

	/** The pretty name of the target detector. */
	public static final String THIS_NAME = "Canny Edge detector";

	/** An html information text. */
	public static final String THIS_INFO_TEXT = "<html>" + "In 1986, Canny proposed a method for edge detection, <br>" + "which was based on three criteria <br>" + " A detection criterion expresses the fact of avoiding <br>" + "the elimination of important edges and not supplying false edges. <br>" +"<p> " + " The location criterion establishes that the distance between the real <br>" + " and the localized position of the edge must be minimized. <br>" + "<p> " + " The criterion of a response that integrates multiple responses corresponding to a single edge. <br> " + "</html>";

	/*
	 * FIELDS
	 */

	/** The image to operate on. Multiple frames, single channel. */
	protected ImgPlus< T > img;

	protected Map< String, Object > settings;

	protected String errorMessage;
	
	public static final String KEY_KERNEL_RADIUS = "RADIUS";
	public static final String KEY_LOW_THRESHOLD = "LOW_THRESHOLD";
	public static final String KEY_HIGH_THRESHOLD = "HIGH_THRESHOLD";
	public static final String KEY_NORMALIZE_CONTRAST = "NORMALIZE_CONTRAST";
	

	/*
	 * METHODS
	 */

	
	@Override
	public boolean setTarget( final ImgPlus< T > img, final Map< String, Object > settings )
	{
		this.img = img;
		this.settings = settings;
		return checkSettings( settings );
	}
	
	
	protected RandomAccessible< T > prepareFrameImg( final int frame )
	{
		final double[] calibration = TMUtils.getSpatialCalibration( img );
		RandomAccessible< T > imFrame;
		final int cDim = TMUtils.findCAxisIndex( img );
		if ( cDim < 0 )
		{
			imFrame = img;
		}
		else
		{
			// In ImgLib2, dimensions are 0-based.
			final int channel = ( Integer ) settings.get( KEY_TARGET_CHANNEL ) - 1;
			imFrame = Views.hyperSlice( img, cDim, channel );
		}

		int timeDim = TMUtils.findTAxisIndex( img );
		if ( timeDim >= 0 )
		{
			if ( cDim >= 0 && timeDim > cDim )
			{
				timeDim--;
			}
			imFrame = Views.hyperSlice( imFrame, timeDim, frame );
		}

		// In case we have a 1D image.
		if ( img.dimension( 0 ) < 2 )
		{ // Single column image, will be rotated internally.
			calibration[ 0 ] = calibration[ 1 ]; // It gets NaN otherwise
			calibration[ 1 ] = 1;
			imFrame = Views.hyperSlice( imFrame, 0, 0 );
		}
		if ( img.dimension( 1 ) < 2 )
		{ // Single line image
			imFrame = Views.hyperSlice( imFrame, 1, 0 );
		}

		return imFrame;
	}
	
	
	
	@Override
	public SpotDetector< T > getDetector( final Interval interval, final int frame )
	{
		final double radius = ( Double ) settings.get( KEY_RADIUS );
		final double threshold = ( Double ) settings.get( KEY_THRESHOLD );
		final boolean doMedian = ( Boolean ) settings.get( KEY_DO_MEDIAN_FILTERING );
		final boolean doSubpixel = ( Boolean ) settings.get( KEY_DO_SUBPIXEL_LOCALIZATION );
		final double[] calibration = TMUtils.getSpatialCalibration( img );
		final RandomAccessible< T > imFrame = prepareFrameImg( frame );
		
		final Canny_Edge_Detector<T> detector=new Canny_Edge_Detector<>( imFrame, interval, calibration, radius,
				threshold, doSubpixel, doMedian);
		return detector;
	}
	
	@Override
	public String getInfoText()
	{
		return THIS_INFO_TEXT;
	}

	@Override
	public String getName()
	{
		return THIS_NAME;
	}

	@Override
	public String getKey()
	{
		return THIS_DETECTOR_KEY;
	}

	@Override
	public ConfigurationPanel getDetectorConfigurationPanel( final Settings lSettings, final Model model )
	{
		return new CannyDetectorConfigurationPanel( lSettings, model, THIS_INFO_TEXT, THIS_NAME );
	}


	@Override
	public boolean unmarshall( final Element element, final Map< String, Object > lSettings )
	{
		lSettings.clear();
		final StringBuilder errorHolder = new StringBuilder();
		boolean ok = true;
		ok = ok & readDoubleAttribute( element, lSettings, KEY_RADIUS, errorHolder );
		ok = ok & readDoubleAttribute( element, lSettings, KEY_THRESHOLD, errorHolder );
		ok = ok & readIntegerAttribute( element, lSettings, KEY_DOWNSAMPLE_FACTOR, errorHolder );
		ok = ok & readIntegerAttribute( element, lSettings, KEY_TARGET_CHANNEL, errorHolder );
		if ( !ok )
		{
			errorMessage = errorHolder.toString();
			return false;
		}
		return checkSettings( lSettings );
	}


	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}
	
	@Override
	public Map< String, Object > getDefaultSettings()
	{
		final Map< String, Object > lSettings = new HashMap<>();
		lSettings.put( KEY_TARGET_CHANNEL, DEFAULT_TARGET_CHANNEL );
		lSettings.put( KEY_RADIUS, DEFAULT_RADIUS );
		lSettings.put( KEY_THRESHOLD, DEFAULT_THRESHOLD );
		lSettings.put( KEY_DO_MEDIAN_FILTERING, DEFAULT_DO_MEDIAN_FILTERING );
		lSettings.put( KEY_DO_SUBPIXEL_LOCALIZATION, DEFAULT_DO_SUBPIXEL_LOCALIZATION );
		return lSettings;
	}

	@Override
	public ImageIcon getIcon()
	{
		return null;
	}
}
 
