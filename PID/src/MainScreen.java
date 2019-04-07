import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
				
				try {
			         System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
			         BufferedImage image = ImageIO.read(file);	

			         byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
			         Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
			         mat.put(0, 0, data);

			         Mat mat1 = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC1);
			         Imgproc.cvtColor(mat, mat1, Imgproc.COLOR_RGB2GRAY);

			         byte[] data1 = new byte[mat1.rows() * mat1.cols() * (int)(mat1.elemSize())];
			         mat1.get(0, 0, data1);
			         BufferedImage image1 = new BufferedImage(mat1.cols(),mat1.rows(), BufferedImage.TYPE_BYTE_GRAY);
			         image1.getRaster().setDataElements(0, 0, mat1.cols(), mat1.rows(), data1);

			         File ouptut = new File("grayscale.jpg");
			         ImageIO.write(image1, "jpg", ouptut);
			         label.setIcon(ResizeImage(ouptut.getAbsolutePath()));
			         
			      } catch (Exception e) {
			         System.out.println("Error: " + e.getMessage());
			      }
			}
		});
		
		JTextPane textPane = new JTextPane();
		textPane.setEditable(false);
		
		JButton btnLimiarizar = new JButton("Limiarizar");
		btnLimiarizar.setToolTipText("Limiarizar imagem");
		
		JButton btnLer = new JButton("Ler código");
		btnLer.setToolTipText("Ler código de barras da imagem");
		
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
		
		JMenu mnNewMenu = new JMenu("Arquivo");
		menuBar.add(mnNewMenu);
		
		JMenuItem mntmOpen = new JMenuItem("Abrir imagem");
		mntmOpen.setAction(action);
		mnNewMenu.add(mntmOpen);
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
	                label.setIcon(ResizeImage(path));
	            }
	        } catch (HeadlessException | IOException e1){
	        }
		}
	}
	
	 public ImageIcon ResizeImage(String ImagePath)
	    {
	        ImageIcon MyImage = new ImageIcon(ImagePath);
	        Image img = MyImage.getImage();
	        Image newImg = img.getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_SMOOTH);
	        ImageIcon image = new ImageIcon(newImg);
	        return image;
	    }
}
