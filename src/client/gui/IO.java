package client.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import cli.CLI;
import client.gui.components.Component;
import general.Point;
import network.managers.BroadcastManager;
import network.managers.NetworkManager;
import threads.ThreadController;

public class IO implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
	
	static IO singleton;
	
	private Set<Component> keyListeners;
	private Point dragPoint;
	
	private static ThreadController paint;
	private static Instant paintRequested; //Time at which screen was last interacted with or a paint was requested
	private final static int paintTimeout = 2;
	
	private IO() {
		this.keyListeners = new HashSet<Component>();
		dragPoint = null;
		startPaintThread();
	}
	
	public static IO getInstance() {
		if (singleton==null) singleton = new IO();
		return singleton;
	}
	
	public void registerKeyListener(Component c) {
		if (!keyListeners.contains(c)) keyListeners.add(c);
	}
	
	public void deregisterKeyListener(Component c) {
		if (keyListeners.contains(c)) keyListeners.remove(c);
	}
	
	public void finishEvent() {
		requestPaint();
	}
	
	public void startPaintThread() {
		paint = new ThreadController() {
			@Override
			public void run() {
				while (isRunning()) {
					if (!paintExpired()) GUI.getInstance().repaint();
					iterate();
				}
			}
		};
		paint.setPaintOnIterate(false);
		paint.setWait(50);
		paint.start();
	}
	
	public void requestPaint() {paintRequested = Instant.now();}
	
	public boolean paintExpired() {
		if (paintRequested==null) return true;
        if ((Instant.now().getEpochSecond()-paintRequested.getEpochSecond())>=paintTimeout) {
        	paintRequested = null;
        	return true;
        }
        return false;
    }
	
	@Override
	public void mouseMoved(MouseEvent e) {
		GUI.getInstance().getView().doMove(new Point(e.getX(), e.getY()));
		finishEvent();
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if (dragPoint==null) dragPoint = new Point(e.getX(), e.getY());
		GUI.getInstance().getView().doDrag(dragPoint, new Point(e.getX(), e.getY()));
		finishEvent();
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		dragPoint = null;
		finishEvent();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		GUI.getInstance().getView().doClick(new Point(e.getX(), e.getY()));
		finishEvent();
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		GUI.getInstance().getView().doScroll(new Point(e.getX(), e.getY()), e.getWheelRotation());
		finishEvent();
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		for (Component c : keyListeners) c.doKeyPress(e);
		if (e.getExtendedKeyCode()==KeyEvent.VK_C) {
			if (CLI.viewerActive()) CLI.showViewer(false);
			else CLI.showViewer(true);
		}
		if (e.getExtendedKeyCode()==KeyEvent.VK_D) {
			boolean show = true;
			if (GUI.getInstance().dom.visualiserVisible()) show = false;
			GUI.getInstance().dom.showVisualiser(show);
		}
		if (e.getExtendedKeyCode()==KeyEvent.VK_V) {
			CLI.setVerbose(!CLI.isVerbose());
			CLI.getViewer().repaint();
		}
		if (e.getExtendedKeyCode()==KeyEvent.VK_L) NetworkManager.getInstance().printLocalAddresses();
		if (e.getExtendedKeyCode()==KeyEvent.VK_P) BroadcastManager.printPotentialClients();
		finishEvent();
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}

}
