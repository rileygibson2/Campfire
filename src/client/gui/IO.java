package client.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.Set;

import client.Call;
import client.gui.components.Component;
import client.gui.views.CallView;
import client.gui.views.RingView;
import general.Point;

public class IO implements MouseListener, MouseMotionListener, KeyListener {
	
	ClientGUI c;
	Set<Component> keyListeners;
	
	public IO(ClientGUI c) {
		this.c = c;
		this.keyListeners = new HashSet<Component>();
	}
	
	public void registerKeyListener(Component c) {
		if (!keyListeners.contains(c)) keyListeners.add(c);
	}
	
	public void deregisterKeyListener(Component c) {
		if (keyListeners.contains(c)) keyListeners.remove(c);
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		//c.cView.doMove(new Point(e.getX(), e.getY()));
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		c.cView.doClick(new Point(e.getX(), e.getY()));
		c.repaint();
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		for (Component c : keyListeners) c.doKeyPress(e);
		if (e.getExtendedKeyCode()==KeyEvent.VK_A) c.changeView(new RingView());
		if (e.getExtendedKeyCode()==KeyEvent.VK_C) c.changeView(new CallView());
		if (e.getExtendedKeyCode()==KeyEvent.VK_B) {
			c.c.call();
		};
		c.repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mouseDragged(MouseEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}

}
