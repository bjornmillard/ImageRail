/** 
 * Author: Bjorn L. Millard
 * (c) Copyright 2010
 * 
 * ImageRail is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; either version 3 of 
 * the License, or (at your option) any later version. SBDataPipe is distributed in the hope that 
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details. You should have received a copy of the GNU General Public License along with this 
 * program. If not, see http://www.gnu.org/licenses/.  */

package tools;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.DataBuffer;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import com.sun.media.jai.widget.DisplayJAI;

public class DisplayCompositionApp extends JFrame  implements ActionListener,MouseListener
    {
     // The original and composite image.
     private PlanarImage originalImage;
     private PlanarImage compositeImage;
     // Where the composite image will be displayed.
     private DisplayJAI display;
    // The number of bands on the image.
    private int nbands;
   // The radio buttons, one for each band and channel combination.
   private JRadioButton[] reds,greens,blues;
  
   /**
  50   * The constructor for this class sets the user interface and creates
  51   * a first combination of bands (RGB=123) to be displayed.
  52   * @param image the (possibly multiband) image to be combined/displayed
  53   */
   public DisplayCompositionApp(PlanarImage image)
       {
       super("Display Composition");
       originalImage = image;
       // How many bands do we have here ?
      nbands = image.getSampleModel().getNumBands();
       // Create the radio buttons and their groups.
       reds = new JRadioButton[nbands];
       greens = new JRadioButton[nbands];
       blues = new JRadioButton[nbands];
       ButtonGroup redGroup = new ButtonGroup();
       ButtonGroup greenGroup = new ButtonGroup();
       ButtonGroup blueGroup = new ButtonGroup();
       // Create the panel for the radio buttons and prepares the GridBagLayout constraints for
       // its components.
       JPanel radioButtonsPanel = new JPanel(new GridBagLayout());
       GridBagConstraints gbc = new GridBagConstraints();
       // Add the first line of components, just some labels.
       gbc.weightx = 1;
       gbc.gridy = 0;
       gbc.gridx = 0;
       JLabel band = new JLabel("Band");
       radioButtonsPanel.add(band,gbc);
       gbc.gridx = 1;
       radioButtonsPanel.add(new JLabel("<html><font color=\"#FF0000\">R</font></html>"),gbc);
       gbc.gridx = 2;
       radioButtonsPanel.add(new JLabel("<html><font color=\"#00FF00\">G</font></html>"),gbc);
       gbc.gridx = 3;
       radioButtonsPanel.add(new JLabel("<html><font color=\"#0000FF\">B</font></html>"),gbc);
       // Add one line of label and 3 radio buttons for each band on the image
       for(int b=0;b<nbands;b++)
         {
        // Label.
         gbc.gridx = 0; gbc.gridy = b+1;
         radioButtonsPanel.add(new JLabel(""+(1+b)),gbc);
        // Red button.
        reds[b] = new JRadioButton();
         reds[b].setForeground(Color.RED);
         reds[b].setOpaque(false);
         reds[b].addActionListener(this);
         redGroup.add(reds[b]);
         gbc.gridx = 1;
         radioButtonsPanel.add(reds[b],gbc);
         // Green button.
         greens[b] = new JRadioButton();
         greens[b].setForeground(Color.GREEN);
        greens[b].setOpaque(false);
        greens[b].addActionListener(this);
        greenGroup.add(greens[b]);
        gbc.gridx = 2;
        radioButtonsPanel.add(greens[b],gbc);
        // Blue button.
        blues[b] = new JRadioButton();
        blues[b].addActionListener(this);
        blues[b].setForeground(Color.BLUE);
        blues[b].setOpaque(false);
        blueGroup.add(blues[b]);
        gbc.gridx = 3;
        radioButtonsPanel.add(blues[b],gbc);
        }
      // Pad the radio buttons panel with an empty label.
      gbc.gridy = nbands+1; gbc.gridx = 0;
      gbc.gridwidth = 4;
      gbc.weighty = 1;
      radioButtonsPanel.add(new JLabel(" "),gbc);
      radioButtonsPanel.setBackground(Color.GRAY);
      // Initially selects the RGB composition.
      reds[0].setSelected(true);
      greens[1].setSelected(true);
      blues[2].setSelected(true);
      // Create the composite image with the R=1,G=2,B=3 combination. Notice
      // that the indices starts with zero.
      recreateCompositeImage(0,1,2);
      // Create the DisplayJAI component.
      display = new DisplayJAI(compositeImage);
      display.addMouseListener(this);
      // Add the DisplayJAI and the radio buttons component to the content pane.
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(new JScrollPane(display),BorderLayout.CENTER);
      // The user interface will get really ugly if there are many bands on the image.
      JScrollPane rbs = new JScrollPane(radioButtonsPanel);
      getContentPane().add(rbs,BorderLayout.EAST);
      // Set the closing operation so the application is finished.
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      pack(); // Set an adequated size for the frame
      setVisible(true); // Show the frame.
      }
   
   /**
 143   * Reconstructs the composite image using the band indices passed as arguments.
 144   * @param nb1 the first band (red channel)
 145   * @param nb2 the second band (green channel)
 146   * @param nb3 the third band (blue channel)
 147   */
    private void recreateCompositeImage(int nb1, int nb2, int nb3)
      {
      compositeImage = JAI.create("bandselect",originalImage,new int[] {nb1,nb2,nb3});
      }
   
   /**
 154   * This method will be called when and action is performed - in this
 155   * application, whenever a button is selected.
 156   */
    public void actionPerformed(ActionEvent e)
      {
      // Which are the selected radio buttons ?
      int sr=0,sg=0,sb=0;
      for(int b=0;b<nbands;b++)
        {
        if (reds[b].isSelected())   sr = b;
        if (greens[b].isSelected()) sg = b;
        if (blues[b].isSelected())  sb = b;
        }
      // Recreate the composite image and set it on the DisplayJAI component.
      // Here's something I didn't expect: even if the original image does not
      // have a ColorModel, the composition appears as a RGB image. I guess it
      // is done by the DisplayJAI component.
      recreateCompositeImage(sr,sg,sb);
      display.set(compositeImage);
     }
   
   /**
 176   * This method allows the saving of the composite image (the one shown by the component).
 177   */
    public void mouseClicked(MouseEvent e)
      {
      String fname = JOptionPane.showInputDialog("Enter the image file name (use .png as extension)");
      // Save the image on a file.
      if (fname != null) JAI.create("filestore",compositeImage,fname,"png");
      }
   
   /**
 186   * This method is here just to keep the MouseListener interface happy.
 187   */
    public void mousePressed(MouseEvent e) { }
   
   /**
 191   * This method is here just to keep the MouseListener interface happy.
 192   */
    public void mouseReleased(MouseEvent e) { }
   
   /**
 196   * This method is here just to keep the MouseListener interface happy.
 197   */
    public void mouseEntered(MouseEvent e) { }
   
   /**
 201   * This method is here just to keep the MouseListener interface happy.
 202   */
    public void mouseExited(MouseEvent e) { }
    
   /**
 206   * The application entry point. It needs a name of an multiband image to run.
 207   * @param args
 208   */
    public static void main(String[] args)
      {
      // We need one argument - the image file name.
      if (args.length != 1)
        {
        System.err.println("Usage: java display.apps.DisplayCompositionsApp image");
        System.exit(0);
        }
      // Read the image. We'll use ImageIO for this.
      PlanarImage image = JAI.create("fileload", args[0]);
      // If the number of bands on the image is not adequate, quits
      if (image.getSampleModel().getNumBands() < 3)
        {
       System.err.println("This app is fun only when there are 3 or more bands on the input image.");
        System.exit(0);
        }
      // This application can also display hyperspectral non-byte images, so a rescaling/format is
      // performed if required.
      if (image.getSampleModel().getDataType() == DataBuffer.TYPE_UNDEFINED)
        {
        System.err.println("Image data type is undefined -- very strange! Sorry, cannot continue.");
        System.exit(0);
        }
      if (image.getSampleModel().getDataType() != DataBuffer.TYPE_BYTE)
        {
        System.out.println("Image data type is not byte, converting...");
        // Get the extrema values of the image for all bands.
        ParameterBlock pbMaxMin = new ParameterBlock();
        pbMaxMin.addSource(image);
        RenderedOp extrema = JAI.create("extrema", pbMaxMin);
        // Must get the extrema of all bands !
        double[] allMins = (double[])extrema.getProperty("minimum");
        double[] allMaxs = (double[])extrema.getProperty("maximum");
        double minGlobalValue = allMins[0];
        double maxGlobalValue = allMaxs[0];
        for(int v=1;v<allMins.length;v++)
          {
          if (allMins[v] < minGlobalValue) minGlobalValue = allMins[v];
          if (allMaxs[v] > maxGlobalValue) maxGlobalValue = allMaxs[v];
          }
        // Rescale the image with the parameters.
        double[] addThis    = new double[1]; addThis[0]    = minGlobalValue;
        double[] multiplyBy = new double[1]; multiplyBy[0] = 255./(maxGlobalValue-minGlobalValue);
        // Now we can rescale the pixels' values:
        ParameterBlock pbSub = new ParameterBlock();
       pbSub.addSource(image);
        pbSub.add(addThis);
        image = (PlanarImage)JAI.create("subtractconst",pbSub,null);
        ParameterBlock pbMult = new ParameterBlock();
        pbMult.addSource(image);
        pbMult.add(multiplyBy);
        image = (PlanarImage)JAI.create("multiplyconst",pbMult,null);
        // Let's convert the data type for displaying.
        ParameterBlock pbConvert = new ParameterBlock();
        pbConvert.addSource(image);
        pbConvert.add(DataBuffer.TYPE_BYTE);
        image = JAI.create("format", pbConvert);
        }
      // Create an instance of DisplayCompositionsApp.
      new DisplayCompositionApp(image);
      } // end main
     
    }
