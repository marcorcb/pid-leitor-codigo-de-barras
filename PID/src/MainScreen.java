import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import org.omg.CORBA.Environment;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import sun.awt.image.ToolkitImage;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JMenuBar;
import javax.swing.JTextPane;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;

public class MainScreen {

	private JFrame frame;
	private final Action action = new SwingAction();
	private File file;
	private int threshold = 190;
	BufferedImage resizedImage;
	private int resizeX, resizeY, resizeWidth, resizeHeight;
	BufferedImage image;
	JLabel label;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainScreen window = new MainScreen();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainScreen() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 900, 550);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JButton btnConverter = new JButton("Converter");
		btnConverter.setToolTipText("Converter para tons de cinza");
		btnConverter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				convertToGrayscale();
			}
		});
		
		JTextPane textPane = new JTextPane();
		textPane.setEditable(false);
		
		JButton btnLimiarizar = new JButton("Limiarizar");
		btnLimiarizar.setToolTipText("Limiarizar imagem");
		btnLimiarizar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				limiarizar();
			}
		});
		
		JButton btnLer = new JButton("Ler código");
		btnLer.setToolTipText("Ler código de barras da imagem");
		btnLer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				lerCodigo();
			}
		});
		
		label = new JLabel("");
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
						.addComponent(textPane, Alignment.LEADING)
						.addComponent(label, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 698, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(btnConverter, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnLimiarizar, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnLer, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE))
					.addGap(28))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(label, GroupLayout.PREFERRED_SIZE, 324, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(36)
							.addComponent(btnConverter, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(btnLimiarizar, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
							.addGap(18)
							.addComponent(btnLer, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)))
					.addGap(18)
					.addComponent(textPane, GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
					.addContainerGap())
		);
		frame.getContentPane().setLayout(groupLayout);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu menuArquivo = new JMenu("Arquivo");
		menuBar.add(menuArquivo);
		
		JMenuItem menuAbrir = new JMenuItem("Abrir imagem");
		menuAbrir.setAction(action);
		menuArquivo.add(menuAbrir);
		
		JMenu menuEditar = new JMenu("Editar");
		menuBar.add(menuEditar);
		
		
		JMenuItem menuDetectar = new JMenuItem("Detectar retângulo");
		menuDetectar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				detectarRetangulo();
			}
		});;
		menuEditar.add(menuDetectar);
		
		JMenuItem menuRemover = new JMenuItem("Cortar código de barras");
		menuRemover.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resizeImage();
			}
		});;
		menuEditar.add(menuRemover);
	}
	
	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "Abrir imagem");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
			try {
	            JFileChooser fileChooser = new JFileChooser();
	            FileNameExtensionFilter filter = new FileNameExtensionFilter("*.Images", "jpg","jpeg","png");
	            fileChooser.addChoosableFileFilter(filter);
	            fileChooser.showOpenDialog(frame);
	            
	            file = fileChooser.getSelectedFile();
	            if(!file.exists()){
	                throw new FileNotFoundException();
	            } else {
	            	String path = file.getAbsolutePath();
	            	image = ImageIO.read(file);
	            	File output = new File("image.jpg");
	    	        ImageIO.write(image, "jpg", output);
	    	        file = output;
	                label.setIcon(ResizeImage(path));
	            }
	        } catch (HeadlessException | IOException e1){
	        }
		}
	}
	
	public void removerCor()
    {
		try {
	        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
	        Mat mat = Imgcodecs.imread(file.getAbsolutePath());

	        Mat mat1 = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC1);
	        
	        
	        
	        Imgcodecs.imwrite(file.getAbsolutePath(), mat1);
	        label.setIcon(ResizeImage(file.getAbsolutePath()));
	         
	    } catch (Exception e) {
	    	System.out.println("Error: " + e.getMessage());
	    }
    }
	
	public ImageIcon ResizeImage(String ImagePath)
    {
		Dimension imgDimension = new Dimension(image.getWidth(), image.getHeight());
		Dimension labelDimension = new Dimension(label.getWidth(), label.getHeight());
		Dimension newDimension = getScaledDimension(imgDimension, labelDimension);
        ImageIcon MyImage = new ImageIcon(ImagePath);
        Image img = MyImage.getImage();
        Image newImg = img.getScaledInstance((int)newDimension.getWidth(), (int)newDimension.getHeight(), Image.SCALE_SMOOTH);
        ImageIcon image = new ImageIcon(newImg);
        return image;
    }
	
	public static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {

	    int original_width = imgSize.width;
	    int original_height = imgSize.height;
	    int bound_width = boundary.width;
	    int bound_height = boundary.height;
	    int new_width = original_width;
	    int new_height = original_height;
	    
	    if(original_width > original_height) {
	        //scale width to fit
	        new_width = bound_width;
	        //scale height to maintain aspect ratio
	        new_height = (new_width * original_height) / original_width;
	    } else {
	        //scale height to fit instead
	        new_height = bound_height;
	        //scale width to maintain aspect ratio
	        new_width = (new_height * original_width) / original_height;
	    }

	    return new Dimension(new_width, new_height);
	}
	
	public void convertToGrayscale() {
		try {
	        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
	        Mat mat = Imgcodecs.imread(file.getAbsolutePath());

	        Mat mat1 = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC1);
	        Imgproc.cvtColor(mat, mat1, Imgproc.COLOR_RGB2GRAY);
	        
	        Imgcodecs.imwrite(file.getAbsolutePath(), mat1);
	        label.setIcon(ResizeImage(file.getAbsolutePath()));
	         
	    } catch (Exception e) {
	    	System.out.println("Error: " + e.getMessage());
	    }
	}
	
	public void limiarizar() {
		try {
			System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
			File file = new File("image.jpg");
			BufferedImage image = ImageIO.read(file);
			
			int width = image.getWidth();
	        int height = image.getHeight();
	        for (int i = 0; i < width; i++) {
	            for (int j = 0; j < height; j++) {
	            	int rgb = image.getRGB(i, j);
	            	int r = (int)((rgb&0x00FF0000)>>>16);
	                int g = (int)((rgb&0x0000FF00)>>>8);
	                int b = (int) (rgb&0x000000FF);
	                int media = (r + g + b) / 3;
	                Color white = new Color(255, 255, 255);
	                Color black = new Color(0, 0, 0);
	                if (media < threshold)
	                    image.setRGB(i, j, black.getRGB());
	                else
	                    image.setRGB(i, j, white.getRGB());
	            }
	        }

	        File ouptut = new File("image.jpg");
	        ImageIO.write(image, "jpg", ouptut);
	        label.setIcon(ResizeImage(ouptut.getAbsolutePath()));
	        
			
	      } catch (Exception e) {
	    	  System.out.println("error: " + e.getMessage());
	      }
	}
	
	public void detectarRetangulo() {
		try {
	        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
	        Mat mat = Imgcodecs.imread(file.getAbsolutePath());

	        Mat mat1 = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC1);
	        
	        Imgproc.Canny(mat, mat1, 50, 50);
	        
	        Imgproc.GaussianBlur(mat1, mat1, new  org.opencv.core.Size(5, 5), 5);
	        
	        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	        Imgproc.findContours(mat1, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

	        double maxArea = -1;
	        int maxAreaIdx = -1;
	        MatOfPoint temp_contour = contours.get(0); //the largest is at the index 0 for starting point
	        MatOfPoint2f approxCurve = new MatOfPoint2f();
	        MatOfPoint largest_contour = contours.get(0);

	        List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();

	        for (int idx = 0; idx < contours.size(); idx++) {
	            temp_contour = contours.get(idx);
	            double contourarea = Imgproc.contourArea(temp_contour);
	            //compare this contour to the previous largest contour found
	            if (contourarea > maxArea) {
	                //check if this contour is a square
	                MatOfPoint2f new_mat = new MatOfPoint2f( temp_contour.toArray() );
	                int contourSize = (int)temp_contour.total();
	                MatOfPoint2f approxCurve_temp = new MatOfPoint2f();
	                Imgproc.approxPolyDP(new_mat, approxCurve_temp, contourSize*0.05, true);
	                if (approxCurve_temp.total() == 4) {
	                    maxArea = contourarea;
	                    maxAreaIdx = idx;
	                    approxCurve=approxCurve_temp;
	                    largest_contour = temp_contour;
	                }
	            }
	        }

	        Imgproc.cvtColor(mat1, mat1, Imgproc.COLOR_BayerBG2RGB);
	        
	        double[] temp_double;
	        temp_double = approxCurve.get(0,0);       
	        Point p1 = new Point(temp_double[0], temp_double[1]);
	        temp_double = approxCurve.get(1,0);       
	        Point p2 = new Point(temp_double[0], temp_double[1]);
	        temp_double = approxCurve.get(2,0);       
	        Point p3 = new Point(temp_double[0], temp_double[1]);
	        temp_double = approxCurve.get(3,0);       
	        Point p4 = new Point(temp_double[0], temp_double[1]);

	        resizeX = (int)p1.x;
	        resizeY = (int)p1.y;
	        resizeWidth =(int)p3.x - resizeX;
	        resizeHeight = (int)p3.y - resizeY;
	        
	        Imgproc.line(mat, p1, p2, new Scalar(0, 0, 255), 5);
	        Imgproc.line(mat, p2, p3, new Scalar(0, 0, 255), 5);
	        Imgproc.line(mat, p3, p4, new Scalar(0, 0, 255), 5);
	        Imgproc.line(mat, p4, p1, new Scalar(0, 0, 255), 5);
	        
	        Imgcodecs.imwrite(file.getAbsolutePath(), mat);
	        label.setIcon(ResizeImage(file.getAbsolutePath()));
	        
		} catch (Exception e) {
	    	  System.out.println("error: " + e.getMessage());
	    }
	}
	
	public void resizeImage() {
		try {
			resizedImage = image.getSubimage(resizeX, resizeY, resizeWidth, resizeHeight);
	        ImageIO.write(resizedImage, "jpg", file);
	        label.setIcon(ResizeImage(file.getAbsolutePath()));
		} catch(IOException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	public void lerCodigo() {
		double min, max;
		boolean wasBlack = true;
		String binary = "";
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        Mat mat = Imgcodecs.imread(file.getAbsolutePath());
        int height = (int) mat.size().height;
        int width = (int) mat.size().width;
        if(height > width) {
        	int firstLine = getFirstLine(mat);
        	int lastLine = getLastLine(mat);
        	int lineSize = (lastLine - firstLine)/95;
        	if (lineSize == 0)
        		lineSize = 1;
        	for(int i = firstLine; i < lastLine; i++) {
        		if(i % lineSize == 0) {
        			double[] rgb = mat.get(width/2, i);
            		if(isBlack(rgb))
            			binary = binary + "1";
            		else
            			binary = binary + "0";
        		}
        	}
        } else {
        	int firstLine = getFirstLine(mat);
        	int lastLine = getLastLine(mat);
        	int lineSize = (lastLine - firstLine)/95;
        	if (lineSize == 0)
        		lineSize = 1;
        	
        	for(int i = firstLine; i < lastLine; i++) {
        		if(i % lineSize == 0) {
        			double[] rgb = mat.get(i, height/2);
        			if(isBlack(rgb))
        				binary = binary + "1";
        			else
        				binary = binary + "0";
        		}
        	}
        
        }
        
        System.out.println(binary);
	}
	
	public int getFirstLine(Mat mat) {
		boolean wasBlack = true;
        int height = (int) mat.size().height;
        int width = (int) mat.size().width;
		if(height > width) {
        	for(int i = 0; i < height; i++) {
        		double[] rgb = mat.get(width/2, i);
        		if (isBlack(rgb) && !wasBlack) {
        			return i;
        		} else if(!isBlack(rgb) && wasBlack)
        			wasBlack = false;
        	}
        	return 0;
        } else {
        	for(int i = 0; i < width; i++) {
            	double[] rgb = mat.get(i, height/2);
            	if (isBlack(rgb) && !wasBlack) {
        			return i;
        		} else if(!isBlack(rgb) && wasBlack)
        			wasBlack = false;
            }
        	return 0;
        }
	}
	
	public int getLastLine(Mat mat) {
		boolean wasBlack = true;
        int height = (int) mat.size().height;
        int width = (int) mat.size().width;
		if(height > width) {
        	for(int i = height-1; i >= 0; i--) {
        		double[] rgb = mat.get(width/2, i);
        		if (isBlack(rgb) && !wasBlack) {
        			return i;
        		} else if(!isBlack(rgb) && wasBlack)
        			wasBlack = false;
        	}
        	return 0;
        } else {
        	for(int i = width-1; i >= 0; i--) {
            	double[] rgb = mat.get(i, height/2);
            	if (isBlack(rgb) && !wasBlack) {
        			return i;
        		} else if(!isBlack(rgb) && wasBlack)
        			wasBlack = false;
            }
        	return 0;
        }
	}
	
	public boolean isBlack(double[] rgb) {
		if(rgb != null) {
			double aux = 0.0;
			for(Double value : rgb) {
				aux = aux + value;
			}
			aux = aux /3;
			return !(aux < 177.0 );	
		}
		return false;
	}
}