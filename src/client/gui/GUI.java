package client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cli.CLI;
import client.Intercom;
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

	private GUI() {
		io = IO.getInstance();
		dom = new DOM();
		screenUtils = new ScreenUtils(screen);
		messages = new ArrayList<MessageBox>();
		
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
	
	public void createCallDialog(String name) {
		 // Create a dialog with "Accept" and "Decline" buttons
       JDialog dialog = new JDialog(frame, "Incoming Call", true);
       dialog.setLayout(new FlowLayout());
       JLabel label = new JLabel("Incoming call from "+name);
       dialog.add(label);
       JButton acceptButton = new JButton("Accept");
       JButton declineButton = new JButton("Decline");
       dialog.add(acceptButton);
       dialog.add(declineButton);
       dialog.pack();

       // Add an action listener to the "Accept" button
       acceptButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               System.out.println("Accept pressed!!!!");
               dialog.dispose();
           }
       });
       // Add an action listener to the "Decline" button
       declineButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               dialog.dispose();
           }
       });
       dialog.setVisible(true);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		screenUtils.drawBase((Graphics2D) g);
		view.draw((Graphics2D) g);
		if (dom.visualiserVisible()) dom.update(getView());
	}

	public static GUI initialise(Intercom c) {
		GUI panel = GUI.getInstance();
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//Initialise
				System.setProperty("apple.laf.useScreenMenuBar", "true");
				frame = new JFrame();
				panel.setPreferredSize(new Dimension((int) screen.width, (int) screen.height));
				frame.getContentPane().add(panel);

				//Label and build
				//frame.setTitle("RileyCom - "+c.name);
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
