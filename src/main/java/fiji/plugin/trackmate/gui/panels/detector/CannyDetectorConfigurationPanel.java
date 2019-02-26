package fiji.plugin.trackmate.gui.panels.detector;

import static fiji.plugin.trackmate.detection.BlockLogDetectorFactory.KEY_NSPLIT;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_DOWNSAMPLE_FACTOR;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_DO_MEDIAN_FILTERING;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_DO_SUBPIXEL_LOCALIZATION;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_RADIUS;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_THRESHOLD;
import static fiji.plugin.trackmate.gui.TrackMateWizard.BIG_FONT;
import static fiji.plugin.trackmate.gui.TrackMateWizard.FONT;
import static fiji.plugin.trackmate.gui.TrackMateWizard.SMALL_FONT;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.detection.CannyDetectorFactory;
import fiji.plugin.trackmate.gui.ConfigurationPanel;
import fiji.plugin.trackmate.gui.panels.components.JNumericTextField;
import fiji.plugin.trackmate.util.JLabelLogger;
import fiji.util.NumberParser;




public class CannyDetectorConfigurationPanel  extends LogDetectorConfigurationPanel{
	private static final long serialVersionUID = 1L;

	private static final String TOOLTIP_REFRESH = null;

	private static final Icon ICON_PREVIEW = null;

	private static final String TOOLTIP_PREVIEW = null;

	private static final Icon ICON_REFRESH = null;
	
	private JLabel jLabelLowThreshold;
	
	private JLabel jLabelHighThreshold;
	
	private JTextField jTextFieldLowThreshold;
	
	private JTextField jTextFieldHighThreshold;
	
	JLabel jLabel1;
	JLabel jLabel2;
	
	public CannyDetectorConfigurationPanel(Settings settings, Model model, String infoText, String detectorName) {
		super(settings, model, infoText, detectorName);

	}

	void refresh()
	{
		if ( null == imp ) {
			return;
		}
		double threshold = imp.getProcessor().getMinThreshold();
		if ( threshold < 0 )
		{
			threshold = 0;
		}
		jTextFieldThreshold.setText( String.format( "%.0f", threshold ) );
		sliderChannel.setValue( imp.getC() );
	}
	
	
	
@Override
protected void initGUI()
	{
	
		
		try
		{
			this.setPreferredSize( new java.awt.Dimension( 300, 461 ) );
			layout = new SpringLayout();
			setLayout( layout );
			{
				 jLabel1 = new JLabel();
				layout.putConstraint( SpringLayout.NORTH, jLabel1, 10, SpringLayout.NORTH, this );
				layout.putConstraint( SpringLayout.WEST, jLabel1, 5, SpringLayout.WEST, this );
				layout.putConstraint( SpringLayout.EAST, jLabel1, -5, SpringLayout.EAST, this );
				this.add( jLabel1 );
				jLabel1.setText( "Configuracion del filtro:" );
				jLabel1.setFont( FONT );
			}
			{
				jLabelSegmenterName = new JLabel();
				layout.putConstraint( SpringLayout.NORTH, jLabelSegmenterName, 33, SpringLayout.NORTH, this );
				layout.putConstraint( SpringLayout.WEST, jLabelSegmenterName, 11, SpringLayout.WEST, this );
				layout.putConstraint( SpringLayout.EAST, jLabelSegmenterName, -11, SpringLayout.EAST, this );
				this.add( jLabelSegmenterName );
				jLabelSegmenterName.setFont( BIG_FONT );
				jLabelSegmenterName.setText( detectorName );
			}
			{
				jLabel2 = new JLabel();
				layout.putConstraint( SpringLayout.NORTH, jLabel2, 247, SpringLayout.NORTH, this );
				layout.putConstraint( SpringLayout.WEST, jLabel2, 16, SpringLayout.WEST, this );
				this.add( jLabel2 );
				jLabel2.setText( "Gaussian Kernel Radius:" );
				jLabel2.setFont( FONT );

			}
			{
				jTextFieldBlobDiameter = new JNumericTextField();
				layout.putConstraint( SpringLayout.EAST, jLabel2, -6, SpringLayout.WEST, jTextFieldBlobDiameter );
				layout.putConstraint( SpringLayout.WEST, jTextFieldBlobDiameter, 168, SpringLayout.WEST, this );
				layout.putConstraint( SpringLayout.NORTH, jTextFieldBlobDiameter, 247, SpringLayout.NORTH, this );
				layout.putConstraint( SpringLayout.SOUTH, jTextFieldBlobDiameter, 263, SpringLayout.NORTH, this );
				jTextFieldBlobDiameter.setHorizontalAlignment( SwingConstants.CENTER );
				jTextFieldBlobDiameter.setColumns( 10 );
				jTextFieldBlobDiameter.setText( "10" );
				this.add( jTextFieldBlobDiameter );
				jTextFieldBlobDiameter.setFont( FONT );
			}
			{
				jLabelBlobDiameterUnit = new JLabel();
				layout.putConstraint( SpringLayout.EAST, jTextFieldBlobDiameter, -11, SpringLayout.WEST, jLabelBlobDiameterUnit );
				layout.putConstraint( SpringLayout.WEST, jLabelBlobDiameterUnit, -62, SpringLayout.EAST, jLabel1 );
				layout.putConstraint( SpringLayout.NORTH, jLabelBlobDiameterUnit, -2, SpringLayout.NORTH, jLabel2 );
				layout.putConstraint( SpringLayout.SOUTH, jLabelBlobDiameterUnit, 262, SpringLayout.NORTH, this );
				layout.putConstraint( SpringLayout.EAST, jLabelBlobDiameterUnit, 0, SpringLayout.EAST, jLabel1 );
				this.add( jLabelBlobDiameterUnit );
				jLabelBlobDiameterUnit.setFont( FONT );
				jLabelBlobDiameterUnit.setText( spaceUnits );
			}
			{
				jCheckBoxMedianFilter = new JCheckBox();
				layout.putConstraint( SpringLayout.NORTH, jCheckBoxMedianFilter, 312, SpringLayout.NORTH, this );
				layout.putConstraint( SpringLayout.WEST, jCheckBoxMedianFilter, 11, SpringLayout.WEST, this );
				layout.putConstraint( SpringLayout.SOUTH, jCheckBoxMedianFilter, 333, SpringLayout.NORTH, this );
				layout.putConstraint( SpringLayout.EAST, jCheckBoxMedianFilter, 241, SpringLayout.WEST, this );
				this.add( jCheckBoxMedianFilter );
				jCheckBoxMedianFilter.setText( "Normalize Contrast" );
				jCheckBoxMedianFilter.setFont( FONT );
			}
			{
				jLabelHelpText = new JLabel();
				layout.putConstraint( SpringLayout.NORTH, jLabelHelpText, 45, SpringLayout.NORTH, this );
				layout.putConstraint( SpringLayout.WEST, jLabelHelpText, 10, SpringLayout.WEST, this );
				layout.putConstraint( SpringLayout.SOUTH, jLabelHelpText, 164, SpringLayout.NORTH, this );
				layout.putConstraint( SpringLayout.EAST, jLabelHelpText, -10, SpringLayout.EAST, this );
				this.add( jLabelHelpText );
				jLabelHelpText.setFont( FONT.deriveFont( Font.ITALIC ) );
				jLabelHelpText.setText( infoText.replace( "<br>", "" ).replace( "<p>", "<p align=\"justify\">" ).replace( "<html>", "<html><p align=\"justify\">" ) );
			}
			{
				jLabelLowThreshold = new JLabel();
				layout.putConstraint( SpringLayout.NORTH, jLabelLowThreshold, -42, SpringLayout.NORTH, jCheckBoxMedianFilter );
				layout.putConstraint( SpringLayout.WEST, jLabelLowThreshold, 16, SpringLayout.WEST, this );
				layout.putConstraint( SpringLayout.SOUTH, jLabelLowThreshold, -29, SpringLayout.NORTH, jCheckBoxMedianFilter );
				layout.putConstraint( SpringLayout.EAST, jLabelLowThreshold, 162, SpringLayout.WEST, this );
				this.add( jLabelLowThreshold );
				jLabelLowThreshold.setText( "Low Threshold:" );
				jLabelLowThreshold.setFont( FONT );
			}
			{
				jTextFieldLowThreshold = new JNumericTextField();
				layout.putConstraint( SpringLayout.NORTH, jTextFieldLowThreshold, 268, SpringLayout.NORTH, this );
				layout.putConstraint( SpringLayout.WEST, jTextFieldLowThreshold, 6, SpringLayout.EAST, jLabelLowThreshold );
				layout.putConstraint( SpringLayout.SOUTH, jTextFieldLowThreshold, 284, SpringLayout.NORTH, this );
				layout.putConstraint( SpringLayout.EAST, jTextFieldLowThreshold, -78, SpringLayout.EAST, this );
				jTextFieldLowThreshold.setHorizontalAlignment( SwingConstants.CENTER );
				jTextFieldLowThreshold.setText( "0" );
				this.add( jTextFieldLowThreshold );
				jTextFieldLowThreshold.setFont( FONT );
			}
			{
				jLabelHighThreshold = new JLabel();
				layout.putConstraint( SpringLayout.NORTH, jLabelHighThreshold, -25, SpringLayout.NORTH, jCheckBoxMedianFilter );
				layout.putConstraint( SpringLayout.WEST, jLabelHighThreshold, 16, SpringLayout.WEST, this );
				layout.putConstraint( SpringLayout.SOUTH, jLabelHighThreshold, -10, SpringLayout.NORTH, jCheckBoxMedianFilter );
				layout.putConstraint( SpringLayout.EAST, jLabelHighThreshold, 162, SpringLayout.WEST, this );
				this.add( jLabelHighThreshold );
				jLabelHighThreshold.setText( "High Threshold:" );
				jLabelHighThreshold.setFont( FONT );
			}
			{
				jTextFieldHighThreshold = new JNumericTextField();
				layout.putConstraint( SpringLayout.NORTH, jTextFieldHighThreshold, 288, SpringLayout.NORTH, this );
				layout.putConstraint( SpringLayout.WEST, jTextFieldHighThreshold, 6, SpringLayout.EAST, jLabelHighThreshold );
				layout.putConstraint( SpringLayout.SOUTH, jTextFieldHighThreshold, 304, SpringLayout.NORTH, this );
				layout.putConstraint( SpringLayout.EAST, jTextFieldHighThreshold, -78, SpringLayout.EAST, this );
				jTextFieldHighThreshold.setHorizontalAlignment( SwingConstants.CENTER );
				jTextFieldHighThreshold.setText( "0" );
				this.add( jTextFieldHighThreshold );
				jTextFieldHighThreshold.setFont( FONT );
			}
			
			{
				jButtonRefresh = new JButton( "Refresh treshold", ICON_REFRESH );
				layout.putConstraint( SpringLayout.NORTH, jButtonRefresh, 370, SpringLayout.NORTH, this );
				layout.putConstraint( SpringLayout.WEST, jButtonRefresh, 11, SpringLayout.WEST, this );
				layout.putConstraint( SpringLayout.SOUTH, jButtonRefresh, 395, SpringLayout.NORTH, this );
				layout.putConstraint( SpringLayout.EAST, jButtonRefresh, 131, SpringLayout.WEST, this );
				// this.add( jButtonRefresh );
				jButtonRefresh.setToolTipText( TOOLTIP_REFRESH );
				jButtonRefresh.setFont( SMALL_FONT );
				jButtonRefresh.addActionListener( new ActionListener()
				{
					@Override
					public void actionPerformed( final ActionEvent e )
					{
						refresh();
					}
				} );
			}

			{
				btnPreview = new JButton( "Preview", ICON_PREVIEW );
				layout.putConstraint( SpringLayout.NORTH, btnPreview, 370, SpringLayout.NORTH, this );
				layout.putConstraint( SpringLayout.WEST, btnPreview, -141, SpringLayout.EAST, this );
				layout.putConstraint( SpringLayout.SOUTH, btnPreview, 395, SpringLayout.NORTH, this );
				layout.putConstraint( SpringLayout.EAST, btnPreview, -10, SpringLayout.EAST, this );
				btnPreview.setToolTipText( TOOLTIP_PREVIEW );
				this.add( btnPreview );
				btnPreview.setFont( SMALL_FONT );
				btnPreview.addActionListener( new ActionListener()
				{
					@Override
					public void actionPerformed( final ActionEvent e )
					{
						preview();
					}
				} );
			}
			
			
			
			{

				// Deal with channels: the slider and channel labels are only
		
			}
			
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public void clean()
	{}

	@Override
	public Map<String, Object> getSettings() {
		final Map< String, Object > lSettings = new HashMap<>( 5 );
		final double kernelRadius=NumberParser.parseDouble( jTextFieldBlobDiameter.getText() ) ;
		final double lowThreshold=NumberParser.parseDouble( jTextFieldLowThreshold.getText() ) ;
		final double highThreshold=NumberParser.parseDouble( jTextFieldHighThreshold.getText() ) ;
		final boolean normalizeContrast=jCheckBoxMedianFilter.isSelected();;
		lSettings.put( CannyDetectorFactory.KEY_KERNEL_RADIUS, kernelRadius );
		lSettings.put( CannyDetectorFactory.KEY_LOW_THRESHOLD, lowThreshold );
		lSettings.put( CannyDetectorFactory.KEY_HIGH_THRESHOLD, highThreshold );
		lSettings.put( CannyDetectorFactory.KEY_NORMALIZE_CONTRAST, normalizeContrast );
		
		return null;
	}
	@Override
	public void setSettings( final Map< String, Object > settings )
	{
	
	}
}