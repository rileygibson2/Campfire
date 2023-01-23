package client.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;
import java.util.Set;

import client.Client;
import client.gui.components.Component;
import general.Point;

public class IO implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
	
	static IO singleton;
	
	private Set<Component> keyListeners;
	private Point dragPoint;
	
	private IO() {
		this.keyListeners = new HashSet<Component>();
		dragPoint = null;
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
		GUI.getInstance().repaint();
		if (GUI.getInstance().dom.visualiserVisible()) GUI.getInstance().dom.update(GUI.getInstance().getView());
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
		//if (e.getExtendedKeyCode()==KeyEvent.VK_R) Client.getInstance().startRing(true);
		/*if (e.getExtendedKeyCode()==KeyEvent.VK_H) {
			Client.getInstance().endCall();
			Client.getInstance().endRing();
		}*/
		
		if (e.getExtendedKeyCode()==KeyEvent.VK_D) {
			boolean show = true;
			if (GUI.getInstance().dom.visualiserVisible()) show = false;
			GUI.getInstance().dom.showVisualiser(show);
		}
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
