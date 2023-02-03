package client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import client.Campfire;
import client.gui.components.MessageBox;
import client.gui.views.HomeView;
import client.gui.views.View;
import dom.DOM;
import general.Rectangle;


public class GUI extends JPanel {

	private static final long serialVersionUID = 1L;
	private static GUI singleton;
	public static Rectangle screen = new Rectangle(0, 0, 500, 250);
	public static JFrame frame;
	
	private ScreenUtils screenUtils;
	private IO io;
	private View view;
	private List<MessageBox> messages;
	
	DOM dom;
	
	//Styles
	public final static Color bg = new Color(15, 15, 15);
	public final static Color fg = new Color(50, 50, 50);
	public final static Color focus = new Color(70, 70, 70);
	public final static Color focus2 = new Color(90, 90, 90);
	private boolean antiAlias;
	
	public final static String baseFont = "Geneva";
	public final static String logoFont = "neoteric";

	private GUI() {
		io = IO.getInstance();
		dom = new DOM();
		screenUtils = new ScreenUtils(screen);
		messages = new ArrayList<MessageBox>();
		antiAlias = false;
		
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		addMouseListener(io);
		addMouseMotionListener(io);
		addMouseWheelListener(io);
		addKeyListener(io);
		changeView(HomeView.getInstance());
		
		repaint();
	}
	
	public static GUI getInstance() {
		if (singleton==null) singleton = new GUI();
		return singleton;
	}
	
	public void addMessage(String message, Color col) {
		//Find y position message should animate to
		double goalY = messages.size()*12.5+5;
		int hold = 2000;
		if (col.equals(MessageBox.error)) hold = 4000;
		
		MessageBox m = new MessageBox(message, col, goalY, hold);
		messages.add(m);
		HomeView.getInstance().addComponent(m);
	}
	
	public void removeMessage(MessageBox m) {
		messages.remove(m);
		
		//Update position of all other messages
//		for (int i=0; i<messages.size(); i++) {
//			messages.get(i).updateGoal((i*12.5)+5);
//		}
	}
	
	public ScreenUtils getScreenUtils() {return screenUtils;}
	
	public View getView() {return view;}
	
	public void changeView(View v) { 
		if (v==null) return;
		
		if (view!=null) view.destroy();
		view = v;
		view.enter();
		repaint();
	}
	
	public void setAntiAliasing(boolean a) {antiAlias = a;}
	
	public boolean getAntiAliasing() {return antiAlias;}
	
	@Override
	public void paintComponent(Graphics g) {
		if (antiAlias) ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		screenUtils.drawBase((Graphics2D) g);
		view.draw((Graphics2D) g);
		if (dom.visualiserVisible()) dom.update(getView());
	}

	public static GUI initialise(Campfire c) {
		GUI panel = GUI.getInstance();
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//Initialise
				System.setProperty("apple.laf.useScreenMenuBar", "true");
				frame = new JFrame();
				panel.setPreferredSize(new Dimension((int) screen.width, (int) screen.height));
				frame.getContentPane().add(panel);

				//Label and build
				frame.setTitle("Campfire");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				//Finish up
				frame.setVisible(true);
				frame.pack();
			}
		});
		return panel;
	}
}
