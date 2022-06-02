
import static org.opencv.imgproc.Imgproc.resize;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import com.opencsv.CSVWriter;

import jxl.Cell;
import jxl.Workbook;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;


public class GraphicalUserInterface {
	
	Cursor cursor = new Cursor(Cursor.HAND_CURSOR);
	private JLabel imageView;
    private JFrame frame;
    private JLabel BGSview;

    private JButton playPauseButton;
    JButton loadButton;
    private JButton saveButton;
    private JButton resetButton;
    private JButton countingLineButton;
    private JButton speedLineButton;


    private volatile boolean isPaused = true;
    private boolean crossingLine = false;
    private boolean crossingSpeedLine = false;
    private int areaThreshold = 1700;
    private double imageThreshold = 20;
    private int history = 1500;
    private int vehicleSizeThreshold = 17000;

    private VideoCapture capture;
    private Mat currentImage = new Mat();
    private VideoProcessor videoProcessor = new MixtureOfGaussianBackground(imageThreshold, history);
    private ImageProcessor imageProcessor = new ImageProcessor();
    private Mat foregroundImage;

    private Point lineCount1;           
    private volatile Point lineCount2;          
    private Point lineSpeed1;           
    private volatile Point lineSpeed2;          
    private int counter = 0;
    private int lastTSM = 0;
    private HashMap<Integer, Integer> speed = new HashMap<Integer, Integer>();

    private double distanceCS = 6.0;
    private double videoFPS;
    private int maxFPS;
    private int whichFrame;
    private JSpinner distanceBLfield;

    private File fileToSaveXLS;
    private WritableWorkbook workbook;
    private WritableSheet sheet;
    private jxl.write.Label label;
    private Number number;


    private CSVWriter CSVwriter;
    private ArrayList<String[]> ListCSV = new ArrayList<>();
    private FileWriter filetoSaveCSV;

    private JRadioButton xlsButton;
    private JRadioButton csvButton;
    private static final String xlsWriteResults = "XLS";
    private static final String csvWriteResults = "CSV";
    private String writeFlag = xlsWriteResults;
    private boolean isExcelToWrite = true;
    private boolean isWritten = false;

    private volatile String videoPath;
    private volatile String savePath;

    private JFormattedTextField carsAmountField;
    private JFormattedTextField carsSpeedField;
    private JFormattedTextField vansAmountField;
    private JFormattedTextField vansSpeedField;
    private JFormattedTextField lorriesAmountField;
    private JFormattedTextField lorriesSpeedField;

    private int cars = 0;
    private int vans = 0;
    private int lorries = 0;

    private double sumSpeedCar = 0;
    private double sumSpeedVan = 0;
    private double sumSpeedLorry = 0;

    private int divisorCar = 1;
    private int divisorVan = 1;
    private int divisorLorry = 1;

    private JRadioButton onButton;
    private JRadioButton offButton;
    private static final String onSaveVideo = "On";
    private static final String offSaveVideo = "Off";
    private String saveFlag = offSaveVideo;
    private boolean isToSave = false;
    private VideoWriter videoWriter;

    private boolean mouseListenertIsActive;
    private boolean mouseListenertIsActive2;
    private boolean startDraw;
    private Mat copiedImage;

    private volatile boolean loopBreaker = false;

    private JPanel BGSPane;
    private JButton BGScloseButton;
    private JButton BGSButton;
    private JSpinner imgThresholdField;
    private volatile boolean isBGSview = false;
    private Mat ImageBGS = new Mat();

    private JSpinner videoHistoryField;

    private JFormattedTextField currentTimeField;
    private double timeInSec;
    private int minutes = 1;
    private int second = 0;

    private JButton realTimeButton;
    private volatile boolean isProcessInRealTime = false;
    private long startTime;
    private long oneFrameDuration;

    private Mat foregroundClone;

    
    public void init() throws IOException, WriteException, InterruptedException {
        setSystemLookAndFeel();
        initGUI();

        while (true) {
            if (videoPath != null && savePath != null) {
                countingLineButton.setEnabled(true);
                speedLineButton.setEnabled(true);
                distanceBLfield.setEnabled(true);

                resetButton.setEnabled(true);
                break;
            }
        }

        while (true) {
            if (lineSpeed2 != null && lineCount2 != null) {

                playPauseButton.setEnabled(true);
                if (saveFlag.equals(onSaveVideo)) {
                    videoWriter = new VideoWriter(savePath + "\\Video.avi", VideoWriter.fourcc('P', 'I', 'M', '1'), videoFPS, new Size(imageView.getWidth(),imageView.getHeight()));
                }
                onButton.setEnabled(false);
                offButton.setEnabled(false);


                String xlsSavePath = savePath + "\\Results.xls";
                fileToSaveXLS = new File(xlsSavePath);
                try {
                    writeToExel(fileToSaveXLS);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (WriteException e) {
                    e.printStackTrace();
                }

                if (!isExcelToWrite) {
                    String csvSavePath = savePath + "\\Results.csv";
                    try {
                        filetoSaveCSV = new FileWriter(csvSavePath);
                        writeToCSV(filetoSaveCSV);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                xlsButton.setEnabled(false);
                csvButton.setEnabled(false);

                break;
            }
        }


        Thread mainLoop = new Thread(new Loop());
        mainLoop.start();
    }
    
    public void initGUI()  {
		frame = createJFrame("Real Time Vehicle Detection");
		frame.setResizable(false);
		frame.setLayout(null);
		frame.setVisible(true);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
        playPauseButton.setEnabled(false);
        countingLineButton.setEnabled(false);
        speedLineButton.setEnabled(false);
        distanceBLfield.setEnabled(false);
        resetButton.setEnabled(false);
	}
	
	
    private JFrame createJFrame(String windowName) {
        frame = new JFrame(windowName);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(new Color(12, 12, 12));
		frame.setSize(800, 600);
		
        setHeader(frame);
		setupVideo(frame);
		infoCars(frame);
		infoVans(frame);
		infoLorries(frame);
		tabelHeader(frame);
		setupbgsView(frame); 
		setupBGSvisibility(frame);
		playPause(frame);
		reset(frame);
		currentTime(frame);
		selectSpeedLine(frame);
		selectCountingLine(frame);
		setupDistanceBetweenLines(frame);
		functionPane(frame);
		loadFile(frame);
        saveFile(frame);
        setupSaveVideo(frame);
        setupWriteType(frame);
		ioPane(frame);
		setupImageThreshold(frame);
		setupVideoHistory(frame);
		setupAreaThreshold(frame);
		setupVehicleSizeThreshold(frame);
        setupRealTime(frame);
		ratioPane(frame);
		

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        return frame;
    }
    
	private int getPercentageNumber(int percentage,int total) {
		return (percentage*total)/100;
	}
	private void setHeader(JFrame frame){
		
		JPanel pane =  new JPanel();
		
		pane.setBounds(0, 0, frame.getWidth(), getPercentageNumber(9,frame.getHeight()));
		pane.setBackground(new Color(24, 24, 24));
		
		BufferedImage myPicture = null;
		try {
			myPicture = ImageIO.read(new File("resources/main.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JLabel picLabel = new JLabel();
		picLabel.setBounds(5, 5, 40, pane.getHeight()-10);
		Image dimg = myPicture.getScaledInstance(picLabel.getWidth(), picLabel.getHeight(),
		        Image.SCALE_SMOOTH);
		ImageIcon ii = new ImageIcon(dimg);
		picLabel.setIcon(ii);
		
		JLabel nameLabel = new JLabel();
		nameLabel.setBounds(10+picLabel.getWidth(), 5, pane.getWidth()-100, pane.getHeight()-10);
		nameLabel.setText("Real Time Vehicle Detection");
		nameLabel.setFont(new Font("Tahoma",Font.BOLD,20));
		nameLabel.setForeground(new Color(228,230,235));
		
		JButton cbutton = new JButton("Presented by");
		cbutton.setForeground(new Color(228,230,235));
		cbutton.setFont(new Font(cbutton.getFont().getFamily(),Font.BOLD,15));
		cbutton.setBounds(frame.getWidth()-115,10,105,pane.getHeight()-20);
		cbutton.setContentAreaFilled(false);
		cbutton.setFocusable(false);
		cbutton.setBorder(null);
		cbutton.setCursor(cursor);
		cbutton.addActionListener(event -> {
			JFrame creditsFrame = new CreditsFrame("Presented by:");
			creditsFrame.setVisible(true);
			cbutton.setEnabled(false);
			creditsFrame.addWindowListener(new WindowAdapter() {
	            @Override
	            public void windowClosing(WindowEvent e) {
	                super.windowClosing(e);
	                cbutton.setEnabled(true);
	            }
	        });
		});
		
		
		frame.add(cbutton);
		frame.add(nameLabel);
		frame.add(picLabel);
		frame.add(pane);
	}
	
	private void setupVideo(JFrame frame) {
        imageView = new JLabel("hello");
        imageView.setBounds(10, getPercentageNumber(9,frame.getHeight())+10, getPercentageNumber(65,frame.getWidth()), getPercentageNumber(53,frame.getHeight()));
        imageView.setOpaque(true);
        imageView.setBackground(new Color(24, 24, 24));
        imageView.setForeground(Color.green);
        frame.add(imageView);
        
        Mat localImage = new Mat(new Size(imageView.getWidth(),imageView.getHeight()), CvType.CV_8UC3, new Scalar(24, 24, 24));
        resize(localImage, localImage, new Size(imageView.getWidth(), imageView.getHeight()));
        updateView(localImage);
    }
	
	private void selectCountingLine(JFrame frame) {
		countingLineButton = new JButton("Draw count line");
		countingLineButton.setBounds(playPauseButton.getWidth()+115, getPercentageNumber(63,frame.getHeight())+18,135,20);
		countingLineButton.setToolTipText("vehicle count line");
		countingLineButton.setContentAreaFilled(false);
		countingLineButton.setBorder(null);
		countingLineButton.setFocusable(false);
		countingLineButton.setForeground(new Color(255,255,255));
		countingLineButton.setFont(new Font(countingLineButton.getFont().getFamily(),Font.PLAIN,11));
		countingLineButton.setCursor(cursor);
		
		countingLineButton.addActionListener(event -> {
            countingLineButton.setEnabled(false);
            speedLineButton.setEnabled(false);
            mouseListenertIsActive = true;
            startDraw = false;
            imageView.addMouseListener(ml);
            imageView.addMouseMotionListener(ml2);

        });
		
		BufferedImage myPicture = null;
		try {
			myPicture = ImageIO.read(new File("resources/redcountingline.gif"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Image dimg = myPicture.getScaledInstance(11, 11,
		        Image.SCALE_SMOOTH);
		ImageIcon ii = new ImageIcon(dimg);
		countingLineButton.setIcon(ii);
		countingLineButton.setHorizontalAlignment(SwingConstants.LEFT);
		frame.add(countingLineButton);
	}
	private void selectSpeedLine(JFrame frame) {
		speedLineButton = new JButton("Draw speed line");
		speedLineButton.setBounds(playPauseButton.getWidth()+255, getPercentageNumber(63,frame.getHeight())+18,140,20);
		speedLineButton.setToolTipText("vehicle speed line");
		speedLineButton.setContentAreaFilled(false);
		speedLineButton.setBorder(null);
		speedLineButton.setFocusable(false);
		speedLineButton.setForeground(new Color(255,255,255));
		speedLineButton.setFont(new Font(speedLineButton.getFont().getFamily(),Font.PLAIN,11));
		speedLineButton.setCursor(cursor);

        speedLineButton.addActionListener(event -> {
            countingLineButton.setEnabled(false);
            speedLineButton.setEnabled(false);
            mouseListenertIsActive2 = true;
            startDraw = false;
            imageView.addMouseListener(ml);
            imageView.addMouseMotionListener(ml2);

        });
        BufferedImage myPicture = null;
		try {
			myPicture = ImageIO.read(new File("resources/greenspeedline.gif"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Image dimg = myPicture.getScaledInstance(11, 11,
		        Image.SCALE_SMOOTH);
		ImageIcon ii = new ImageIcon(dimg);
		speedLineButton.setIcon(ii);
		speedLineButton.setHorizontalAlignment(SwingConstants.LEFT);
        frame.add(speedLineButton);
    }
	private void tabelHeader(JFrame frame) {
		JPanel pane = new JPanel();
		pane.setBounds(getPercentageNumber(65,frame.getWidth())+15, getPercentageNumber(9,frame.getHeight())+10, getPercentageNumber(31,frame.getWidth()), getPercentageNumber(26,frame.getHeight()));
		pane.setBackground(new Color(24, 24, 24));
		
		JLabel vehiclesLabel = new JLabel("Vehicles");
		vehiclesLabel.setForeground(new Color(228,230,235));
		vehiclesLabel.setFont(new Font(vehiclesLabel.getFont().getFamily(),Font.BOLD,12));
		vehiclesLabel.setBounds(getPercentageNumber(65,frame.getWidth())+20, getPercentageNumber(9,frame.getHeight())+15,55,20);
		
		JLabel countsLabel = new JLabel("Quantity");
		countsLabel.setForeground(new Color(228,230,235));
		countsLabel.setFont(new Font(vehiclesLabel.getFont().getFamily(),Font.BOLD,12));
		countsLabel.setBounds(getPercentageNumber(65,frame.getWidth())+80, getPercentageNumber(9,frame.getHeight())+15,57,20);
		
		JLabel speedLabel = new JLabel("Avg speed [km/h]");
		speedLabel.setForeground(new Color(228,230,235));
		speedLabel.setFont(new Font(speedLabel.getFont().getFamily(),Font.BOLD,12));
		speedLabel.setBounds(getPercentageNumber(65,frame.getWidth())+147, getPercentageNumber(9,frame.getHeight())+15,125,20);
		
		frame.add(speedLabel);
		frame.add(countsLabel);
		frame.add(vehiclesLabel);
		frame.add(pane);
	}
	private void infoCars(JFrame frame) {

        JLabel carsLabel = new JLabel("Cars",SwingConstants.CENTER);
        carsLabel.setForeground(new Color(228,230,235));
        carsLabel.setFont(new Font(carsLabel.getFont().getFamily(),Font.PLAIN,12));
        carsLabel.setBounds(getPercentageNumber(65,frame.getWidth())+20, getPercentageNumber(9,frame.getHeight())+45,50,30);
        
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        carsAmountField = new JFormattedTextField(numberFormat);
        carsAmountField.setValue(new Integer(0));
        carsAmountField.setBackground(new Color(148,0,211));
        carsAmountField.setForeground(new Color(255,255,255));
        carsAmountField.setFont(new Font(carsAmountField.getFont().getFamily(),Font.BOLD,13));
        carsAmountField.setEditable(false);
        carsAmountField.setHorizontalAlignment(SwingConstants.CENTER);
        carsAmountField.setBounds(getPercentageNumber(65,frame.getWidth())+82, getPercentageNumber(9,frame.getHeight())+45,50,30);

        carsSpeedField = new JFormattedTextField(numberFormat);
        carsSpeedField.setValue(new Integer(0));
        carsSpeedField.setBackground(new Color(148,0,211));
        carsSpeedField.setForeground(new Color(255,255,255));
        carsSpeedField.setFont(new Font(carsSpeedField.getFont().getFamily(),Font.BOLD,13));
        carsSpeedField.setEditable(false);
        carsSpeedField.setHorizontalAlignment(SwingConstants.CENTER);
        carsSpeedField.setBounds(getPercentageNumber(65,frame.getWidth())+175, getPercentageNumber(9,frame.getHeight())+45,50,30);

        frame.add(carsAmountField);
        frame.add(carsSpeedField);
        frame.add(carsLabel);
    }
	private void infoVans(JFrame frame) {

        JLabel vansLabel = new JLabel("<html><div style='text-align: center;'>Vans/<br/>Bus</div></html>", SwingConstants.CENTER);
        vansLabel.setForeground(new Color(228,230,235));
        vansLabel.setFont(new Font(vansLabel.getFont().getFamily(),Font.PLAIN,12));
        vansLabel.setBounds(getPercentageNumber(65,frame.getWidth())+20, getPercentageNumber(9,frame.getHeight())+85,50,30);

        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        vansAmountField = new JFormattedTextField(numberFormat);
        vansAmountField.setValue(new Integer(0));
        vansAmountField.setBackground(new Color(148,0,211));
        vansAmountField.setForeground(new Color(255,255,255));
        vansAmountField.setFont(new Font(vansAmountField.getFont().getFamily(),Font.BOLD,13));
        vansAmountField.setEditable(false);
        vansAmountField.setHorizontalAlignment(SwingConstants.CENTER);
        vansAmountField.setBounds(getPercentageNumber(65,frame.getWidth())+82, getPercentageNumber(9,frame.getHeight())+85,50,30);

        vansSpeedField = new JFormattedTextField(numberFormat);
        vansSpeedField.setValue(new Integer(0));
        vansSpeedField.setBackground(new Color(148,0,211));
        vansSpeedField.setForeground(new Color(255,255,255));
        vansSpeedField.setFont(new Font(vansSpeedField.getFont().getFamily(),Font.BOLD,13));
        vansSpeedField.setEditable(false);
        vansSpeedField.setHorizontalAlignment(SwingConstants.CENTER);
        vansSpeedField.setBounds(getPercentageNumber(65,frame.getWidth())+175, getPercentageNumber(9,frame.getHeight())+85,50,30);
        
        frame.add(vansLabel);
        frame.add(vansAmountField);
        frame.add(vansSpeedField);
    }
	private void infoLorries(JFrame frame) {

        JLabel lorriesLabel = new JLabel("Lorries", JLabel.CENTER);
        lorriesLabel.setForeground(new Color(228,230,235));
        lorriesLabel.setFont(new Font(lorriesLabel.getFont().getFamily(),Font.PLAIN,12));
        lorriesLabel.setBounds(getPercentageNumber(65,frame.getWidth())+20, getPercentageNumber(9,frame.getHeight())+125,50,30);

        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        lorriesAmountField = new JFormattedTextField(numberFormat);
        lorriesAmountField.setValue(new Integer(0));
        lorriesAmountField.setBackground(new Color(148,0,211));
        lorriesAmountField.setForeground(new Color(255,255,255));
        lorriesAmountField.setFont(new Font(lorriesAmountField.getFont().getFamily(),Font.BOLD,13));
        lorriesAmountField.setEditable(false);
        lorriesAmountField.setHorizontalAlignment(SwingConstants.CENTER);
        lorriesAmountField.setBounds(getPercentageNumber(65,frame.getWidth())+82, getPercentageNumber(9,frame.getHeight())+125,50,30);

        lorriesSpeedField = new JFormattedTextField(numberFormat);
        lorriesSpeedField.setValue(new Integer(0));
        lorriesSpeedField.setBackground(new Color(148,0,211));
        lorriesSpeedField.setForeground(new Color(255,255,255));
        lorriesSpeedField.setFont(new Font(lorriesSpeedField.getFont().getFamily(),Font.BOLD,13));
        lorriesSpeedField.setEditable(false);
        lorriesSpeedField.setHorizontalAlignment(SwingConstants.CENTER);
        lorriesSpeedField.setBounds(getPercentageNumber(65,frame.getWidth())+175, getPercentageNumber(9,frame.getHeight())+125,50,30);

        frame.add(lorriesLabel);
        frame.add(lorriesAmountField);
        frame.add(lorriesSpeedField);
    }
	
	private void setupbgsView(JFrame frame) {
		BGSPane = new JPanel();
		BGSPane.setBounds(getPercentageNumber(65,frame.getWidth())+15, getPercentageNumber(36,frame.getHeight())+10, getPercentageNumber(31,frame.getWidth()), getPercentageNumber(26,frame.getHeight()));
		BGSPane.setBackground(new Color(24, 24, 24));
		
		BGScloseButton = new RoundedButton("");
		BGScloseButton.setCursor(cursor);
		BGScloseButton.setBorder(null);
        BGScloseButton.setBounds(getPercentageNumber(65,frame.getWidth())+(BGSPane.getWidth()/2)+5, getPercentageNumber(62,frame.getHeight())+20, 25, 25);
        BufferedImage myPicture = null;
		try {
			myPicture = ImageIO.read(new File("resources/close.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Image dimg = myPicture.getScaledInstance(BGScloseButton.getWidth(), BGScloseButton.getHeight(),
		        Image.SCALE_SMOOTH);
		ImageIcon ii = new ImageIcon(dimg);
		BGScloseButton.setVisible(false);
		BGScloseButton.setIcon(ii);
        
        frame.add(BGScloseButton);
		frame.add(BGSPane);
		
		
	}
	
	private void setupBGSvisibility(JFrame frame) {
        BGSButton = new RoundedButton("BGS view");
        BGSButton.setBackground(new Color(24, 24, 24));
        BGSButton.setForeground(new Color(255,255,255));
        BGSButton.setFont(new Font(BGSButton.getFont().getFamily(),Font.BOLD,13));
        BGSButton.setCursor(cursor);
        BGSButton.setBounds(getPercentageNumber(65,frame.getWidth())+15, getPercentageNumber(62,frame.getHeight())+10, getPercentageNumber(31,frame.getWidth()), getPercentageNumber(7,frame.getHeight()));
        
        BGSButton.addActionListener(event -> {
            initBGSview();
            BGSButton.setEnabled(false);
            isBGSview = true;
        });

        BGSButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        frame.add(BGSButton);

    }
	
	private void initBGSview() {
		BGSButton.setVisible(false);
        BGSview = new JLabel();
        BGSview.setBounds(BGSPane.getX()+10,BGSPane.getY(),BGSPane.getWidth()-20,BGSPane.getHeight()-10);
        BGSPane.add(BGSview);
        BGScloseButton.setVisible(true);
        BGScloseButton.addActionListener(event -> {
        	BGScloseButton.setVisible(false);
        	BGSButton.setEnabled(true);
        	BGSButton.setVisible(true);
        	BGSview.setVisible(false);
            isBGSview = false;
        });
        Mat localImage = new Mat(new Size(BGSview.getWidth(), BGSview.getHeight()), CvType.CV_8UC3, new Scalar(170, 170, 170));
        BGSview.setIcon(new ImageIcon(imageProcessor.toBufferedImage(localImage)));
    }
	
	private void functionPane(JFrame frame) {
		JPanel pane = new JPanel();
		pane.setBounds(10, getPercentageNumber(63,frame.getHeight())+10, getPercentageNumber(65,frame.getWidth()), getPercentageNumber(6,frame.getHeight()));
		pane.setOpaque(true);
		pane.setBackground(new Color(24, 24, 24));
		pane.setForeground(Color.green);
        frame.add(pane);
	}
	
	private void playPause(JFrame frame) {

        playPauseButton = new RoundedButton("");
        playPauseButton.setEnabled(true);
        playPauseButton.setBounds(15, getPercentageNumber(63,frame.getHeight())+16, 25, 25);
        playPauseButton.setCursor(cursor);
        playPauseButton.setBorder(null);
        BufferedImage myPicture = null;
		try {
			myPicture = ImageIO.read(new File("resources/play.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Image dimg = myPicture.getScaledInstance(playPauseButton.getWidth(), playPauseButton.getHeight(),
		        Image.SCALE_SMOOTH);
		ImageIcon ii = new ImageIcon(dimg);
		playPauseButton.setIcon(ii);
		
		BufferedImage myPicture1 = null;
		try {
			myPicture1 = ImageIO.read(new File("resources/pause.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Image dimg1 = myPicture1.getScaledInstance(playPauseButton.getWidth(), playPauseButton.getHeight(),
		        Image.SCALE_SMOOTH);
		ImageIcon ii1 = new ImageIcon(dimg1);
        
		playPauseButton.addActionListener(event -> {
            if (!isPaused) {
                isPaused = true;
                playPauseButton.setIcon(ii);
                playPauseButton.setToolTipText("PLAY");

                loadButton.setEnabled(true);
                saveButton.setEnabled(true);

                onButton.setEnabled(false);
                offButton.setEnabled(false);

                countingLineButton.setEnabled(true);
                distanceBLfield.setEnabled(true);
                speedLineButton.setEnabled(true);

                xlsButton.setEnabled(false);
                csvButton.setEnabled(false);

            } else {
                isPaused = false;
                playPauseButton.setIcon(ii1);
                playPauseButton.setToolTipText("PAUSE");

                maxWaitingFPS();

                loadButton.setEnabled(false);
                saveButton.setEnabled(false);

                onButton.setEnabled(false);
                offButton.setEnabled(false);

                countingLineButton.setEnabled(false);
                distanceBLfield.setEnabled(false);
                speedLineButton.setEnabled(false);

                xlsButton.setEnabled(false);
                csvButton.setEnabled(false);
            }
        });
        playPauseButton.setAlignmentX(Component.LEFT_ALIGNMENT);


        frame.add(playPauseButton);
    }
	private void reset(JFrame frame) {
        resetButton = new JButton("Reset");
        resetButton.setBounds(15, getPercentageNumber(70,frame.getHeight())+115,80,25);
        resetButton.setToolTipText("Reset");
        resetButton.setForeground(new Color(255,255,255));
        resetButton.setContentAreaFilled(false);
        resetButton.setFocusable(false);
        resetButton.setBorder(null);
        resetButton.setFont(new Font(resetButton.getFont().getFamily(),Font.BOLD,13));
        resetButton.setCursor(cursor);
        resetButton.addActionListener(event -> {

            int n = JOptionPane.showConfirmDialog(
                    frame, "Are you sure you want to reset the video?",
                    "Reset", JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.YES_OPTION) {
                loopBreaker = true;

                capture = new VideoCapture(videoPath);
                capture.read(currentImage);
                videoFPS = capture.get(Videoio.CAP_PROP_FPS);
                resize(currentImage, currentImage, new Size(imageView.getWidth(), imageView.getHeight()));
                updateView(currentImage);

                currentTimeField.setValue("0 sec");

                isPaused = true;
                BufferedImage myPicture = null;
        		try {
        			myPicture = ImageIO.read(new File("resources/play.png"));
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
        		Image dimg = myPicture.getScaledInstance(playPauseButton.getWidth(), playPauseButton.getHeight(),
        		        Image.SCALE_SMOOTH);
        		ImageIcon ii = new ImageIcon(dimg);
        		playPauseButton.setIcon(ii);
                playPauseButton.setToolTipText("Play");;
                playPauseButton.setEnabled(false);
                videoProcessor = new MixtureOfGaussianBackground(imageThreshold, history);

                resetButton.setEnabled(false);

                onButton.setEnabled(true);
                offButton.setEnabled(true);

                xlsButton.setEnabled(true);
                csvButton.setEnabled(true);

                countingLineButton.setEnabled(true);
                speedLineButton.setEnabled(true);
                distanceBLfield.setEnabled(true);
                lineCount1 = null;
                lineCount2 = null;
                lineSpeed1 = null;
                lineSpeed2 = null;

                minutes = 1;
                second = 0;
                whichFrame = 0;
                timeInSec = 0;

                carsAmountField.setValue(new Integer(0));
                carsSpeedField.setValue(new Integer(0));
                vansAmountField.setValue(new Integer(0));
                vansSpeedField.setValue(new Integer(0));
                lorriesAmountField.setValue(new Integer(0));
                lorriesSpeedField.setValue(new Integer(0));

                cars = 0;
                vans = 0;
                lorries = 0;

                sumSpeedCar = 0;
                sumSpeedVan = 0;
                sumSpeedLorry = 0;

                divisorCar = 1;
                divisorVan = 1;
                divisorLorry = 1;

                counter = 0;
                lastTSM = 0;

                if (isToSave)
                    videoWriter.release();

                if (!isWritten) {
                    try {
                        workbook.write();
                        workbook.close();
                    } catch (IOException | WriteException e) {
                        e.printStackTrace();
                    }
                    if (!isExcelToWrite) {
                        try {
                            CSVwriter.writeAll(ListCSV);
                            CSVwriter.close();
                            new File(savePath + "\\Results.xls").delete();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    isWritten = true;
                }

                Thread reseting = new Thread(new Reseting());
                reseting.start();
                loopBreaker = false;
            }

        });
        BufferedImage myPicture = null;
		try {
			myPicture = ImageIO.read(new File("resources/reset.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Image dimg = myPicture.getScaledInstance(playPauseButton.getWidth(), playPauseButton.getHeight(),
		        Image.SCALE_SMOOTH);
		ImageIcon ii = new ImageIcon(dimg);
		resetButton.setIcon(ii);
		resetButton.setHorizontalAlignment(SwingConstants.LEFT);
        
        frame.add(resetButton);
    }
	private void currentTime(JFrame frame) {

        currentTimeField = new JFormattedTextField();
        currentTimeField.setBounds(playPauseButton.getWidth()+20, getPercentageNumber(63,frame.getHeight())+16,90,25);
        currentTimeField.setValue("0 sec");
        currentTimeField.setBackground(new Color(148,0,211));
        currentTimeField.setForeground(new Color(255,255,255));
        currentTimeField.setFont(new Font(currentTimeField.getFont().getFamily(),Font.BOLD,13));
        
        currentTimeField.setHorizontalAlignment(JFormattedTextField.LEFT);
        currentTimeField.setEditable(false);

        frame.add(currentTimeField);
    }
	private void setupDistanceBetweenLines(JFrame frame) {
        JLabel distanceBLLabel = new JLabel("Distance between lines [m]:");
        distanceBLLabel.setBounds(getPercentageNumber(47,frame.getWidth())+23, getPercentageNumber(70,frame.getHeight())+20, 190, 20);
        distanceBLLabel.setForeground(new Color(228,230,235));
        distanceBLLabel.setFont(new Font(distanceBLLabel.getFont().getFamily(),Font.BOLD,13));

        distanceBLfield = new JSpinner(new SpinnerNumberModel(distanceCS, 0, 10, 0.5));
        distanceBLfield.setUI(new CustomSpinnerUI());
        distanceBLfield.setBounds(distanceBLLabel.getWidth()+getPercentageNumber(47,frame.getWidth())+23, getPercentageNumber(70,frame.getHeight())+15,55,30);
        distanceBLfield.setFont(new Font(distanceBLfield.getFont().getFamily(),Font.BOLD,13));
        distanceBLfield.setBorder(null);
        setColors(distanceBLfield);
        setButtonColors(distanceBLfield);
        
        distanceBLfield.addChangeListener(e ->
                distanceCS = (double) distanceBLfield.getValue());

        frame.add(distanceBLLabel);
        frame.add(distanceBLfield);
    }
	
	private void ioPane(JFrame frame) {
		JPanel pane = new JPanel();
		pane.setBounds(10, getPercentageNumber(70,frame.getHeight())+10, getPercentageNumber(47,frame.getWidth())+3, getPercentageNumber(22,frame.getHeight()));
		pane.setOpaque(true);
		pane.setBackground(new Color(24, 24, 24));
		pane.setForeground(Color.green);
        frame.add(pane);
	}
	
	private void loadFile(JFrame frame) {

		JLabel videoOrCam = new JLabel("Video/Cam:");
		videoOrCam.setBounds(15, getPercentageNumber(70,frame.getHeight())+15, 80, 20);
		videoOrCam.setForeground(new Color(228,230,235));
		videoOrCam.setFont(new Font(videoOrCam.getFont().getFamily(),Font.BOLD,13));
        
        JTextField field = new JTextField();
        field.setBounds(videoOrCam.getWidth()+15, getPercentageNumber(70,frame.getHeight())+15, 250, 20);
        field.setText(" ");
        field.setBackground(new Color(148,0,211));
        field.setForeground(new Color(228,230,235));
        field.setFont(new Font(field.getFont().getFamily(),Font.BOLD,11));
        field.setEditable(true);

        loadButton = new JButton("");
        loadButton.setBounds(videoOrCam.getWidth()+field.getWidth()+20, getPercentageNumber(70,frame.getHeight())+12, 25, 25);
        loadButton.setForeground(new Color(255,255,255));
        loadButton.setContentAreaFilled(false);
        loadButton.setFocusable(false);
        loadButton.setBorder(null);
        loadButton.setFont(new Font(loadButton.getFont().getFamily(),Font.BOLD,13));
        loadButton.setCursor(cursor);
        BufferedImage myPicture = null;
		try {
			myPicture = ImageIO.read(new File("resources/browse.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Image dimg = myPicture.getScaledInstance(loadButton.getWidth(), loadButton.getHeight(),
		        Image.SCALE_SMOOTH);
		ImageIcon ii = new ImageIcon(dimg);
		loadButton.setIcon(ii);
		
        JFileChooser fc = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Video Files", "avi", "mp4", "mpg", "mov");
        fc.setFileFilter(filter);
        fc.setCurrentDirectory(new File(System.getProperty("user.home"), "Videos"));
        fc.setAcceptAllFileFilterUsed(false);

        loadButton.addActionListener(event -> {
            int returnVal = fc.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();

                videoPath = file.getPath();
                field.setText(videoPath);
                capture = new VideoCapture(videoPath);
                capture.read(currentImage);
                videoFPS = capture.get(Videoio.CAP_PROP_FPS);
                resize(currentImage, currentImage, new Size(imageView.getWidth(),imageView.getHeight()));
                updateView(currentImage);

            }
        });
        loadButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        
        frame.add(videoOrCam);
	    frame.add(loadButton);
        frame.add(field);
    }

    private void saveFile(JFrame frame) {
    	
    	JLabel saveResult = new JLabel("Result Save:");
    	saveResult.setBounds(15, getPercentageNumber(70,frame.getHeight())+65, 90, 20);
    	saveResult.setForeground(new Color(228,230,235));
    	saveResult.setFont(new Font(saveResult.getFont().getFamily(),Font.BOLD,13));

        JTextField field = new JTextField();
        field.setBounds(saveResult.getWidth()+15, getPercentageNumber(70,frame.getHeight())+65, 250, 20);
        field.setText(" ");
        field.setBackground(new Color(148,0,211));
        field.setForeground(new Color(228,230,235));
        field.setFont(new Font(field.getFont().getFamily(),Font.BOLD,11));
        field.setEditable(true);

        saveButton = new JButton("");
        saveButton.setBounds(saveResult.getWidth()+field.getWidth()+20, getPercentageNumber(70,frame.getHeight())+62, 25, 25);
        saveButton.setForeground(new Color(255,255,255));
        saveButton.setContentAreaFilled(false);
        saveButton.setFocusable(false);
        saveButton.setBorder(null);
        saveButton.setFont(new Font(saveButton.getFont().getFamily(),Font.BOLD,13));
        saveButton.setCursor(cursor);
        BufferedImage myPicture = null;
		try {
			myPicture = ImageIO.read(new File("resources/browse.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Image dimg = myPicture.getScaledInstance(saveButton.getWidth(), saveButton.getHeight(),
		        Image.SCALE_SMOOTH);
		ImageIcon ii = new ImageIcon(dimg);
		saveButton.setIcon(ii);

        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setCurrentDirectory(new File(System.getProperty("user.home"), "Documents"));
        fc.setAcceptAllFileFilterUsed(false);

        saveButton.addActionListener(event -> {
            int returnVal = fc.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {

                File file = fc.getSelectedFile();

                savePath = file.getPath();
                field.setText(savePath);

            }
        });
        saveButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        frame.add(saveResult);
        frame.add(saveButton);
        frame.add(field);
    }

    private void setupSaveVideo(JFrame frame) {

        onButton = new JRadioButton(onSaveVideo);
        onButton.setMnemonic(KeyEvent.VK_O);
        onButton.setActionCommand(onSaveVideo);
        onButton.setSelected(false);
        onButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        onButton.setFont(new Font(onButton.getFont().getFamily(),Font.BOLD,11));
        onButton.setForeground(new Color(228,230,235));
        onButton.setBackground(new Color(24,24,24));

        offButton = new JRadioButton(offSaveVideo);
        offButton.setMnemonic(KeyEvent.VK_F);
        offButton.setActionCommand(offSaveVideo);
        offButton.setSelected(true);
        offButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        offButton.setFont(new Font(onButton.getFont().getFamily(),Font.BOLD,11));
        offButton.setForeground(new Color(228,230,235));
        offButton.setBackground(new Color(24,24,24));

        ButtonGroup group = new ButtonGroup();
        group.add(onButton);
        group.add(offButton);

        ActionListener operationChangeListener = event -> {
            saveFlag = event.getActionCommand();
            isToSave = (saveFlag.equals(onSaveVideo));
        };

        onButton.addActionListener(operationChangeListener);
        offButton.addActionListener(operationChangeListener);


        JLabel fillLabel = new JLabel("Save a video:");
        fillLabel.setBounds(15, getPercentageNumber(70,frame.getHeight())+40, 90, 20);
        fillLabel.setForeground(new Color(228,230,235));
		fillLabel.setFont(new Font(fillLabel.getFont().getFamily(),Font.BOLD,13));
		
		GridLayout gridRowLayout = new GridLayout(1, 0);
        JPanel saveOperationPanel = new JPanel(gridRowLayout);
        saveOperationPanel.setBounds(fillLabel.getWidth()+15, getPercentageNumber(70,frame.getHeight())+40, 90, 20);
        saveOperationPanel.setBackground(new Color(24,24,24,0));
        saveOperationPanel.add(onButton);
        saveOperationPanel.add(offButton);

        
        frame.add(fillLabel);
        frame.add(saveOperationPanel);
    }

    private void setupWriteType(JFrame frame) {

        xlsButton = new JRadioButton(xlsWriteResults);
        xlsButton.setMnemonic(KeyEvent.VK_O);
        xlsButton.setActionCommand(xlsWriteResults);
        xlsButton.setSelected(true);
        xlsButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        xlsButton.setFont(new Font(xlsButton.getFont().getFamily(),Font.BOLD,11));
        xlsButton.setForeground(new Color(228,230,235));
        xlsButton.setBackground(new Color(24,24,24));

        csvButton = new JRadioButton(csvWriteResults);
        csvButton.setMnemonic(KeyEvent.VK_F);
        csvButton.setActionCommand(csvWriteResults);
        csvButton.setSelected(false);
        csvButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        csvButton.setFont(new Font(csvButton.getFont().getFamily(),Font.BOLD,11));
        csvButton.setForeground(new Color(228,230,235));
        csvButton.setBackground(new Color(24,24,24));

        ButtonGroup group = new ButtonGroup();
        group.add(xlsButton);
        group.add(csvButton);

        ActionListener operationChangeListener = event -> {
            writeFlag = event.getActionCommand();
            isExcelToWrite = (writeFlag.equals(xlsWriteResults)) ? true : false;
        };

        xlsButton.addActionListener(operationChangeListener);
        csvButton.addActionListener(operationChangeListener);

        JLabel writeLabel = new JLabel("Result save as:");
        writeLabel.setBounds(15, getPercentageNumber(70,frame.getHeight())+90, 100, 20);
        writeLabel.setForeground(new Color(228,230,235));
        writeLabel.setFont(new Font(writeLabel.getFont().getFamily(),Font.BOLD,13));

        GridLayout gridRowLayout = new GridLayout(1, 0);
        JPanel writeOperationPanel = new JPanel(gridRowLayout);
        writeOperationPanel.setBounds(writeLabel.getWidth()+15, getPercentageNumber(70,frame.getHeight())+90, 100, 20);
        writeOperationPanel.setBackground(new Color(24,24,24,0));
        writeOperationPanel.add(xlsButton);
        writeOperationPanel.add(csvButton);

       
        frame.add(writeLabel);
        frame.add(writeOperationPanel);
    }

    private void ratioPane(JFrame frame) {
		JPanel pane = new JPanel();
		pane.setBounds(getPercentageNumber(47,frame.getWidth())+18, getPercentageNumber(70,frame.getHeight())+10, getPercentageNumber(49,frame.getWidth()), getPercentageNumber(22,frame.getHeight()));
		pane.setOpaque(true);
		pane.setBackground(new Color(24, 24, 24));
		pane.setForeground(Color.green);
        frame.add(pane);
	}
    
    private void setupImageThreshold(JFrame frame) {
        JLabel imgThresholdLabel = new JLabel("Video threshold:");
        imgThresholdLabel.setBounds(getPercentageNumber(47,frame.getWidth())+23, getPercentageNumber(70,frame.getHeight())+60, 120, 20);
        imgThresholdLabel.setForeground(new Color(228,230,235));
        imgThresholdLabel.setFont(new Font(imgThresholdLabel.getFont().getFamily(),Font.BOLD,13));
        
        imgThresholdField = new JSpinner(new SpinnerNumberModel(imageThreshold, 0, 10000, 5));
        imgThresholdField.setUI(new CustomSpinnerUI());
        imgThresholdField.setBounds(imgThresholdLabel.getWidth()+getPercentageNumber(47,frame.getWidth())+23, getPercentageNumber(70,frame.getHeight())+55,55,30);
        imgThresholdField.setFont(new Font(imgThresholdField.getFont().getFamily(),Font.BOLD,13));
        imgThresholdField.setBorder(null);
        setColors(imgThresholdField);
        setButtonColors(imgThresholdField);

        imgThresholdField.addChangeListener(e -> {
            imageThreshold = (double) imgThresholdField.getValue();
            videoProcessor.setImageThreshold(imageThreshold);
        });

        frame.add(imgThresholdLabel);
        frame.add(imgThresholdField);
    }
    
    private void setupVideoHistory(JFrame frame) {
        JLabel videoHistoryLabel = new JLabel("History:");
        videoHistoryLabel.setBounds(getPercentageNumber(47,frame.getWidth())+23, getPercentageNumber(70,frame.getHeight())+100, 65, 20);
        videoHistoryLabel.setForeground(new Color(228,230,235));
        videoHistoryLabel.setFont(new Font(videoHistoryLabel.getFont().getFamily(),Font.BOLD,13));

        videoHistoryField = new JSpinner(new SpinnerNumberModel(history, 0, 100000, 50));
        videoHistoryField.setUI(new CustomSpinnerUI());
        videoHistoryField.setBounds(videoHistoryLabel.getWidth()+getPercentageNumber(47,frame.getWidth())+23, getPercentageNumber(70,frame.getHeight())+95,75,30);
        videoHistoryField.setFont(new Font(videoHistoryField.getFont().getFamily(),Font.BOLD,13));
        videoHistoryField.setBorder(null);
        setColors(videoHistoryField);
        setButtonColors(videoHistoryField);

        videoHistoryField.addChangeListener(e -> {
            history = (int) videoHistoryField.getValue();
            videoProcessor.setHistory(history);
        });

        frame.add(videoHistoryLabel);
        frame.add(videoHistoryField);
    }

    private void setupAreaThreshold(JFrame frame) {
        JLabel areaThresholdLabel = new JLabel("Area threshold:");
        areaThresholdLabel.setBounds(getPercentageNumber(47,frame.getWidth())+205, getPercentageNumber(70,frame.getHeight())+60, 110, 20);
        areaThresholdLabel.setForeground(new Color(228,230,235));
        areaThresholdLabel.setFont(new Font(areaThresholdLabel.getFont().getFamily(),Font.BOLD,13));

        final JSpinner areaThresholdField = new JSpinner(new SpinnerNumberModel(areaThreshold, 0, 100000, 50));
        areaThresholdField.setUI(new CustomSpinnerUI());
        areaThresholdField.setBounds(areaThresholdLabel.getWidth()+getPercentageNumber(47,frame.getWidth())+205, getPercentageNumber(70,frame.getHeight())+55,75,30);
        areaThresholdField.setFont(new Font(areaThresholdField.getFont().getFamily(),Font.BOLD,13));
        areaThresholdField.setBorder(null);
        setColors(areaThresholdField);
        setButtonColors(areaThresholdField);

        areaThresholdField.addChangeListener(e ->
                areaThreshold = (int) areaThresholdField.getValue());

        
        frame.add(areaThresholdLabel);
        frame.add(areaThresholdField);
    }

    private void setupVehicleSizeThreshold(JFrame frame) {
        JLabel vehicleSizeThresholdLabel = new JLabel("Vehicle size threshold:");
        vehicleSizeThresholdLabel.setBounds(getPercentageNumber(47,frame.getWidth())+170, getPercentageNumber(70,frame.getHeight())+100, 147, 20);
        vehicleSizeThresholdLabel.setForeground(new Color(228,230,235));
        vehicleSizeThresholdLabel.setFont(new Font(vehicleSizeThresholdLabel.getFont().getFamily(),Font.BOLD,13));

        final JSpinner vehicleSizeThresholdField = new JSpinner(new SpinnerNumberModel(vehicleSizeThreshold, 0, 100000, 100));
        vehicleSizeThresholdField.setUI(new CustomSpinnerUI());
        vehicleSizeThresholdField.setBounds(vehicleSizeThresholdLabel.getWidth()+getPercentageNumber(47,frame.getWidth())+177, getPercentageNumber(70,frame.getHeight())+95,75,30);
        vehicleSizeThresholdField.setFont(new Font(vehicleSizeThresholdField.getFont().getFamily(),Font.BOLD,13));
        vehicleSizeThresholdField.setBorder(null);
        setColors(vehicleSizeThresholdField);
        setButtonColors(vehicleSizeThresholdField);

        vehicleSizeThresholdField.addChangeListener(e ->
                vehicleSizeThreshold = (int) vehicleSizeThresholdField.getValue());

       
        frame.add(vehicleSizeThresholdLabel);
        frame.add(vehicleSizeThresholdField);
    }
    
    private void setupRealTime(JFrame frame) {

        realTimeButton = new JButton("<html><div style='text-align: center;'>Process in real time<br/>OFF</div></html>");
        realTimeButton.setForeground(new Color(255,255,255));
        realTimeButton.setContentAreaFilled(false);
        realTimeButton.setFocusable(false);
        realTimeButton.setBorder(null);
        realTimeButton.setBackground(new Color(24, 24, 24));
        realTimeButton.setForeground(new Color(255,255,255));
        realTimeButton.setFont(new Font(realTimeButton.getFont().getFamily(),Font.BOLD,13));
        realTimeButton.setCursor(cursor);
        realTimeButton.setBounds(getPercentageNumber(47,frame.getWidth())+260, getPercentageNumber(70,frame.getHeight())+20, 160, 25);

        realTimeButton.addActionListener(event -> {
            if (isProcessInRealTime) {
                isProcessInRealTime = false;
                realTimeButton.setText("<html><div style='text-align: center;'>Process in real time<br/>OFF</div></html>");
            } else {
                isProcessInRealTime = true;
                realTimeButton.setText("<html><div style='text-align: center;'>Process in real time<br/>ON</div></html>");
            }

        });
        realTimeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        frame.add(realTimeButton);
    }
    
	private void setColors(JSpinner spinner){
	        JComponent editor = spinner.getEditor();
	        int n = editor.getComponentCount();
	        for (int i=0; i<n; i++)
	        {
	            Component c = editor.getComponent(i);
	            if (c instanceof JTextField)
	            {
	            	((JTextField) c).setDisabledTextColor(new Color(228,230,235));
	            	((JTextField) c).setHorizontalAlignment(JTextField.CENTER);
	            	c.setForeground(new Color(228,230,235));
	            	((JTextField) c).setBorder(null);
	                c.setBackground(new Color(148,0,211));
	            }
	        }
	}
	
	private void setButtonColors(JSpinner spinner){
		    int n = spinner.getComponentCount();
		    for (int i=0; i<n; i++)
		    {
		        Component c = spinner.getComponent(i);
		        if (c instanceof JButton)
		        {
		        	((JButton) c).setContentAreaFilled(false);
		        	c.setCursor(cursor);
		        	c.setFocusable(false);
		        	((JButton) c).setBorder(null);
		            c.setForeground(new Color(228,230,235)); 
		            c.setBackground(new Color(24,24,24));
		        }
		    }
	}
	
	private class Reseting implements Runnable {

        @Override
        public void run() {

            while (true) {
                if (lineSpeed2 != null && lineCount2 != null) {
                    playPauseButton.setEnabled(true);
                    resetButton.setEnabled(true);

                    onButton.setEnabled(false);
                    offButton.setEnabled(false);

                    xlsButton.setEnabled(false);
                    csvButton.setEnabled(false);

                    if (saveFlag.equals(onSaveVideo)) {
                        videoWriter = new VideoWriter(savePath + "\\Video.avi", VideoWriter.fourcc('P', 'I', 'M', '1'), videoFPS, new Size(imageView.getWidth(),imageView.getHeight()));
                    }

                    Thread mainLoop = new Thread(new Loop());
                    mainLoop.start();

                    String xlsSavePath = savePath + "\\Results.xls";
                    fileToSaveXLS = new File(xlsSavePath);
                    try {
                        writeToExel(fileToSaveXLS);
                    } catch (IOException | WriteException e) {
                        e.printStackTrace();
                    }

                    if (!isExcelToWrite) {
                        String csvSavePath = savePath + "\\Results.csv";
                        try {
                            filetoSaveCSV = new FileWriter(csvSavePath);
                            writeToCSV(filetoSaveCSV);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    isWritten = false;

                    break;
                }
            }
        }
    }
	
	private MouseListener ml = new MouseListener() {
        public void mouseClicked(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
            if (mouseListenertIsActive) {
                call(e.getButton(), new Point(e.getX(), e.getY()));
            } else if (mouseListenertIsActive2) {
                call2(e.getButton(), new Point(e.getX(), e.getY()));
            }
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    };

	private MouseMotionListener ml2 = new MouseMotionListener() {
        public void mouseDragged(MouseEvent e) {

        }

        public void mouseMoved(MouseEvent e) {
            if (mouseListenertIsActive) {
                call(e.getButton(), new Point(e.getX(), e.getY()));
                
            } else if (mouseListenertIsActive2) {
                call2(e.getButton(), new Point(e.getX(), e.getY()));
            }
        }
    };
	
    public void call(int event, Point point) {
        if (event == 1) {
            if (!startDraw) {
                lineCount1 = point;
                startDraw = true;
            } else {
                lineCount2 = point;
                startDraw = false;
                mouseListenertIsActive = false;
                countingLineButton.setEnabled(true);
                speedLineButton.setEnabled(true);
                imageView.removeMouseListener(ml);
                imageView.removeMouseMotionListener(ml2);
            }

        } else if (event == 0 && startDraw) {
            copiedImage = currentImage.clone();
            Imgproc.line(copiedImage, lineCount1, point, new Scalar(0, 0, 255), 1);
            if (lineSpeed1 != null && lineSpeed2 != null)
                Imgproc.line(copiedImage, lineSpeed1, lineSpeed2, new Scalar(0, 255, 0), 1);
            updateView(copiedImage);
        }
    }

    private void call2(int event, Point point) {
        if (event == 1) {
            if (!startDraw) {
                lineSpeed1 = point;
                startDraw = true;
            } else {
                lineSpeed2 = point;
                startDraw = false;
                mouseListenertIsActive2 = false;
                countingLineButton.setEnabled(true);
                speedLineButton.setEnabled(true);
                imageView.removeMouseListener(ml);
                imageView.removeMouseMotionListener(ml2);
            }

        } else if (event == 0 && startDraw) {
            copiedImage = currentImage.clone();
            Imgproc.line(copiedImage, lineSpeed1, point, new Scalar(0, 255, 0), 1);
            if (lineCount1 != null && lineCount2 != null)
                Imgproc.line(copiedImage, lineCount1, lineCount2, new Scalar(0, 0, 255), 1);
            updateView(copiedImage);
        }
    }

	private void updateView(Mat image) {
        imageView.setIcon(new ImageIcon(imageProcessor.toBufferedImage(image)));
    }
	
	public void maxWaitingFPS() {
        double time = (distanceCS / 3);
        double max = videoFPS * time;
        maxFPS = (int) max;

        oneFrameDuration = 1000 / (long) videoFPS;
    }

	public void writeToCSV(FileWriter fileWriter) throws IOException {
        CSVwriter = new CSVWriter(fileWriter, '\t');
        ListCSV.add("No.#Vehicle type#Speed [km/h]#Video time [sec]".split("#"));
    }

    public void writeToExel(File file) throws IOException, WriteException {

        workbook = Workbook.createWorkbook(file);
        sheet = workbook.createSheet("Counting", 0);
        addLabel(sheet, 0, 0, "No.");
        addLabel(sheet, 1, 0, "Vehicle type");
        addLabel(sheet, 2, 0, "Speed [km/h]");
        addLabel(sheet, 3, 0, "Video time [sec]");
    }
    private void addLabel(WritableSheet sheet, int column, int row, String text)
            throws WriteException {
        label = new jxl.write.Label(column, row, text);
        sheet.addCell(label);
    }
    
    private void addNumberInteger(WritableSheet sheet, int column, int row, Integer integer)
            throws WriteException {

        number = new Number(column, row, integer);
        sheet.addCell(number);
    }

    private void addNumberDouble(WritableSheet sheet, int column, int row, Double d)
            throws WriteException {

        number = new Number(column, row, d);
        sheet.addCell(number);
    }

    public double computeSpeed(int speedPFS) {
        double duration = speedPFS / videoFPS;
        double v = (distanceCS / duration) * 3.6;
        return v;
    }

    private double videoRealTime() {
        whichFrame++;
        timeInSec = whichFrame / videoFPS;
        setTimeInMinutes();
        return timeInSec;
    }

    private void setTimeInMinutes() {
        if (timeInSec < 60) {
            currentTimeField.setValue((int) timeInSec + " sec");
        } else if (second < 60) {
            second = (int) timeInSec - (60 * minutes);
            currentTimeField.setValue(minutes + " min " + second + " sec");
        } else {
            second = 0;
            minutes++;
        }
    }
    
    private void saveVideo() {
        if (isToSave)
            videoWriter.write(currentImage);
    }
    
    public synchronized void count(CountVehicles countVehicles) throws WriteException {
        if (countVehicles.isVehicleToAdd()) {
            counter++;
            lastTSM++;
            speed.put(lastTSM, 0);
            String vehicleType = countVehicles.classifier();
            switch (vehicleType) {
                case "Car":
                    cars++;
                    carsAmountField.setValue(cars);
                    break;
                case "Van":
                    vans++;
                    vansAmountField.setValue(vans);
                    break;
                case "Lorry":
                    lorries++;
                    lorriesAmountField.setValue(lorries);
                    break;
            }

            addNumberInteger(sheet, 0, counter, counter);
            addLabel(sheet, 1, counter, vehicleType);

        }
        crossingLine = countVehicles.isCrossingLine();
    }


    public synchronized void speedMeasure(CountVehicles countVehicles) throws WriteException {
        if (!speed.isEmpty()) {
            int firstTSM = speed.entrySet().iterator().next().getKey();
            if (countVehicles.isToSpeedMeasure()) {
                for (int i = firstTSM; i <= lastTSM; i++) {
                    if (speed.containsKey(i)) {
                        speed.put(i, (speed.get(i) + 1));
                    }
                }

                double currentSpeed = computeSpeed(speed.get(firstTSM));
                Cell cell = sheet.getWritableCell(1, firstTSM);

                String carType = cell.getContents();
                switch (carType) {
                    case "Car":
                        sumSpeedCar = sumSpeedCar + currentSpeed;
                        double avgspeed1 = sumSpeedCar / divisorCar;
                        divisorCar++;
                        carsSpeedField.setValue(avgspeed1);
                        break;
                    case "Van":
                        sumSpeedVan = sumSpeedVan + currentSpeed;
                        double avgspeed2 = sumSpeedVan / divisorVan;
                        divisorVan++;
                        vansSpeedField.setValue(avgspeed2);
                        break;
                    case "Lorry":
                        sumSpeedLorry = sumSpeedLorry + currentSpeed;
                        double avgspeed3 = sumSpeedLorry / divisorLorry;
                        divisorLorry++;
                        lorriesSpeedField.setValue(avgspeed3);
                        break;
                }


                addNumberDouble(sheet, 2, firstTSM, currentSpeed);
                addNumberDouble(sheet, 3, firstTSM, timeInSec);

                if (!isExcelToWrite) {
                    ListCSV.add((firstTSM + "#" + carType + "#" + currentSpeed + "#" + timeInSec).split("#"));
                }

                speed.remove(firstTSM);

            } else {
                for (int i = firstTSM; i <= lastTSM; i++) {
                    if (speed.containsKey(i)) {
                        int currentFPS = speed.get(i);
                        speed.put(i, (currentFPS + 1));
                        if (currentFPS > maxFPS) {
                            speed.remove(i);

                            Cell cell = sheet.getWritableCell(1, i);
                            String carType = cell.getContents();
                            switch (carType) {
                                case "Car":
                                	System.out.println("car Remove");
                                    //cars--;
                                    carsAmountField.setValue(cars);
                                    break;
                                case "Van":
                                    //vans--;
                                    vansAmountField.setValue(vans);
                                    break;
                                case "Lorry":
                                    //lorries--;
                                    lorriesAmountField.setValue(lorries);
                                    break;
                            }

                        }
                    }
                }
            }
        }
        crossingSpeedLine = countVehicles.isCrossingSpeedLine();
    }

    public class Loop implements Runnable {

        @Override
        public void run() {

            maxWaitingFPS();
            videoProcessor = new MixtureOfGaussianBackground(imageThreshold, history);
            if (capture.isOpened()) {
                while (true) {
                    if (!isPaused) {
                        capture.read(currentImage);
                        if (!currentImage.empty()) {
                            resize(currentImage, currentImage, new Size(imageView.getWidth(), imageView.getHeight()));
                            foregroundImage = currentImage.clone();
                            foregroundImage = videoProcessor.process(foregroundImage);

                            foregroundClone = foregroundImage.clone();
                            Imgproc.bilateralFilter(foregroundClone, foregroundImage, 2, 1600, 400);

                            if (isBGSview) {
                                resize(foregroundImage, ImageBGS, new Size(BGSview.getWidth(),BGSview.getHeight()));
                                BGSview.setIcon(new ImageIcon(imageProcessor.toBufferedImage(ImageBGS)));
                            }

                            CountVehicles countVehicles = new CountVehicles(areaThreshold, vehicleSizeThreshold, lineCount1, lineCount2, lineSpeed1, lineSpeed2, crossingLine, crossingSpeedLine);
                            countVehicles.findAndDrawContours(currentImage, foregroundImage);

                            try {
                                count(countVehicles);
                                speedMeasure(countVehicles);
                            } catch (WriteException e) {
                                e.printStackTrace();
                            }


                            videoRealTime();

                            saveVideo();

                            if (isProcessInRealTime) {
                                long time = System.currentTimeMillis() - startTime;
                                if (time < oneFrameDuration) {
                                    try {
                                        Thread.sleep(oneFrameDuration - time);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            updateView(currentImage);
                            startTime = System.currentTimeMillis();

                            if (loopBreaker)
                                break;

                        } else {
                            if (isToSave)
                                videoWriter.release();

                            if (!isWritten) {
                                try {
                                    workbook.write();
                                    workbook.close();
                                } catch (IOException | WriteException e) {
                                    e.printStackTrace();
                                }

                                if (!isExcelToWrite) {
                                    try {
                                        CSVwriter.writeAll(ListCSV);
                                        CSVwriter.close();
                                        new File(savePath + "\\Results.xls").delete();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                isWritten = true;
                            }

                            playPauseButton.setEnabled(false);

                            saveButton.setEnabled(true);
                            loadButton.setEnabled(true);
                            
                            BufferedImage myPicture = null;
                    		try {
                    			myPicture = ImageIO.read(new File("resources/play.png"));
                    		} catch (IOException e) {
                    			// TODO Auto-generated catch block
                    			e.printStackTrace();
                    		}
                    		Image dimg = myPicture.getScaledInstance(playPauseButton.getWidth(), playPauseButton.getHeight(),
                    		        Image.SCALE_SMOOTH);
                    		ImageIcon ii = new ImageIcon(dimg);
                    		playPauseButton.setIcon(ii);
                            playPauseButton.setToolTipText("Play");
                            minutes = 1;
                            second = 0;
                            whichFrame = 0;
                            break;
                        }
                    }
                }
            }
        }
    }
    
	private void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }
}
