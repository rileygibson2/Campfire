package client.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import client.Client;
import client.gui.components.Button;
import client.gui.components.Component;
import client.gui.views.HomeView;
import client.gui.views.SettingsView;
import client.gui.views.View;
import client.gui.views.View.ViewType;


public class ClientGUI extends JPanel {

	private static final long serialVersionUID = 1L;
	public static int sW = 600;
	public static int sH = 300;
	public static JFrame frame;
	
	Client c;
	public static IO io;
	Set<View> views;
	View cView;

	public ClientGUI(Client c) {
		this.c = c;
		io = new IO(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		addMouseListener(io);
		addMouseMotionListener(io);
		addKeyListener(io);
		
		views = new HashSet<View>();
		views.add(new HomeView());
		views.add(new SettingsView(this));
		changeView(ViewType.Home);
		
		repaint();
	}
	
	public void changeView(ViewType vt) {
		View v = getView(vt);
		if (v==null) return;
		
		if (cView!=null) cView.destroy();
		cView = v;
		cView.enter();
		repaint();
	}
	
	public void changeView(View v) { 
		if (v==null) return;
		
		if (cView!=null) cView.destroy();
		cView = v;
		cView.enter();
		repaint();
	}
	
	public void resetHover(Button except) {
		for (Component c : cView.components) {
			if (c instanceof Button) {
				Button b = (Button) c;
				//.unhover();
			}
		}
	}
	
	public View getView(ViewType vt) {
		for (View v : views) {
			if (v.getViewType()==vt) return v;
		}
		return null;
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
	
	/**
	 * Scales a percentage of the screen width to an actual x point
	 * @param p
	 * @return
	 */
	public static int cW(double p) {
		return (int) (sW*((double) p/100));
	}

	/**
	 * Scales a percentage of the screen height to an actual y point
	 * @param p
	 * @return
	 */
	public static int cH(double p) {
		return (int) (sH*((double) p/100));
	}
	
	/**
	 * Scales an actual x point to a percentage of screen width
	 * @param p
	 * @return
	 */
	public static double cWR(double p) {
		return (p/sW)*100;
	}

	/**
	 * Scales an actual y point to a percentage of screen height
	 * @param p
	 * @return
	 */
	public static double cHR(double p) {
		return (p/sH)*100;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		ScreenUtils.drawBase((Graphics2D) g);
		cView.draw((Graphics2D) g);
	}

	public static ClientGUI initialise(Client c) {
		ClientGUI panel = new ClientGUI(c);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//Initialise
				System.setProperty("apple.laf.useScreenMenuBar", "true");
				frame = new JFrame();
				panel.setPreferredSize(new Dimension(sW, sH));
				frame.getContentPane().add(panel);

				//Label and build
				//frame.setTitle("RileyCom - "+c.name);
				frame.setTitle("RileyCom");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				//Finish up
				frame.setVisible(true);
				frame.pack();
			}
		});
		return panel;
	}
}
