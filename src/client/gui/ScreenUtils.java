package client.gui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import cli.CLI;
import client.Campfire;
import client.gui.components.Component;
import client.gui.components.GradientButton;
import client.gui.components.Image;
import client.gui.components.Label;
import client.gui.components.SimpleBox;
import client.gui.components.TextBox;
import client.gui.views.CallView;
import general.Pair;
import general.Point;
import general.Rectangle;
import general.Utils;
import threads.ThreadController;

public class ScreenUtils {

	private Rectangle screen;

	public ScreenUtils(Rectangle screen) {
		this.screen = screen;
		loadFonts();
	}
	
	public void loadFonts() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, Utils.getInputStream("assets/fonts/neoteric.ttf")));
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, Utils.getInputStream("assets/fonts/neoteric-bold.ttf")));
        } catch (Exception e) {CLI.error("Error loading fonts - "+e.getMessage());}
	}
	
	public void drawBase(Graphics2D g) {
		fillRect(g, GUI.bg, new Rectangle(0, 0, 100, 100));
	}

	public void drawLabel(Graphics2D g, Label l) {
		Rectangle r = l.getRealRec();
		Color col = new Color(l.col.getRed(), l.col.getGreen(), l.col.getBlue(), percToCol(l.getOpacity()));
		g.setFont(l.font);
		
		if (l.isCentered()) drawCenteredString(g, l.font, l.text, col, new Rectangle(r.x, r.y, 1, 1));
		else drawStringFromPoint(g, l.font, l.text, col, new Point(r.x, r.y));
	}

	public void drawTextBox(Graphics2D g, TextBox t) {
		Rectangle r = t.getRealRec();

		fillRoundRect(g, GUI.focus, r); //Main box
		if (t.isSelected()) drawRoundRect(g, GUI.focus2, r); //Highlight
	}

	public void drawImage(Graphics2D g, Image i) {
		Rectangle r = i.getRealRec();
		
		BufferedImage img = null;
		try {img = ImageIO.read(Utils.getURL("assets/"+i.src));}
		catch (IOException | IllegalArgumentException e) {
			if (!Campfire.isShuttingdown()) CLI.error("ImageIO failed for assets/"+i.src);
			return;
		}
		if (i.getOpacity()<100) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (i.getOpacity()/100)));
		
		g.drawImage(img, cW(r.x), cH(r.y), cW(r.width), cH(r.height), null);
		
		//Reset alpha
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
	}
	
	public void drawSimpleBox(Graphics2D g, SimpleBox b) {
		Rectangle r = b.getRealRec();
		Color col = new Color(b.col.getRed(), b.col.getGreen(), b.col.getBlue(), percToCol(b.getOpacity()));
		if (b.col.getAlpha()==0) return; //Duck tape fix as this method does net respect the boxe's alpha channel
		
		if (b.isOval()) {
			if (b.isFilled()) fillOval(g, col, r);
			else drawOval(g, col, r);
		}
		else if (b.isRounded()) {
			if (b.getRoundedCorners()!=null) {
				if (b.isFilled()) fillRoundRect(g, col, r, b.getRoundedCorners());
				//TODO
			}
			else {
				if (b.isFilled()) fillRoundRect(g, col, r);
				else drawRoundRect(g, col, r);
			}
		}
		else {
			if (b.isFilled()) fillRect(g, col, r); 
			else drawRect(g, col, r); 
		}
	}

	public void drawDataWave(Graphics2D g, CallView v, SimpleBox b) {
		Rectangle r = b.getRealRec();

		//Gradient
		Color start = new Color(40, 40, 40, 255);
		Color end = new Color(GUI.bg.getRed(), GUI.bg.getGreen(), GUI.bg.getBlue(), 0);
		setGradientRadial(g, start, end, new float[]{0f, 1f}, new Rectangle(r.x-r.width*0.2, r.y, r.width*1.4, r.height));
		fillRect(g, new Rectangle(r.x, r.y, r.width+5, r.height));


		double xInc = r.width/v.dataLen;
		double yInc = (r.height/2)/v.dataBounds.y;
		double x = 0, y = 0, yI = 0, xBez, yPrev, yPrevI;
		int i = 0;

		//Open paths on center line
		GeneralPath gP = new GeneralPath();
		GeneralPath gPI = new GeneralPath(); //Inverse Path
		gP.moveTo(cW(r.x), cH(r.y+r.height/2)); //Upper line
		gPI.moveTo(cW(r.x), cH(r.y+r.height/2)); //Inverted line

		for (Double d : v.getData()) {
			//Next Point
			yPrev = y;
			yPrevI = yI;
			x = r.x+(i*xInc);
			y = (r.y+r.height/2)-(d*yInc);
			yI = (r.y+r.height/2)+(d*yInc);

			if (i==0) {
				gP.moveTo(cW(x), cH(y)); //Upper line
				gPI.moveTo(cW(x), cH(yI)); //Inverted line
			}
			else {
				//Bezier control points allow for a double parabolic curve between each poing
				xBez = x-(xInc/2) ;
				gP.curveTo(cW(xBez), cH(yPrev), cW(xBez), cH(y), cW(x), cH(y)); //Upper line
				gPI.curveTo(cW(xBez), cH(yPrevI), cW(xBez), cH(yI), cW(x), cH(yI)); //Inverted line
			}
			i++;
		}

		//Move paths back to center line
		yPrev = y;
		yPrevI = yI;
		x = r.x+(i*xInc);
		y = (r.y+r.height/2);
		xBez = x-(xInc/2) ;
		gP.curveTo(cW(xBez), cH(yPrev), cW(xBez), cH(y), cW(x), cH(y)); //Upper line
		gPI.curveTo(cW(xBez), cH(yPrevI), cW(xBez), cH(y), cW(x), cH(y)); //Inverted line

		//Add tail
		gP.lineTo(cW(x+5), cH(y));
		gPI.lineTo(cW(x+5), cH(y));

		//Wave fill
		start = new Color(255, 0, 0, 100);
		end = new Color(0, 220, 50, 200);
		setGradientLinear(g, start, end, new Rectangle(r.x+r.width, r.y, r.x+r.width, r.y+r.height/2));
		g.fill(gP);
		setGradientLinear(g, end, start, new Rectangle(r.x+r.width, r.y+r.height/2, r.x+r.width, r.y+r.height));
		g.fill(gPI);

		//Wave outline
		g.setColor(new Color(100, 100, 100));
		Stroke s = g.getStroke();
		g.setStroke(new BasicStroke(0.5f));
		g.draw(gP);
		g.draw(gPI);
		g.setStroke(s);
	}

	public void drawPulse(Graphics2D g, ThreadController tC) {
		if (!tC.hasElements()) return;
		Element e = (Element) tC.getTarget();
		Rectangle r;

		for (Object o : tC.getElements()) {
			@SuppressWarnings("unchecked")
			Pair<Point, Color> pa = (Pair<Point, Color>) o;
			Point p = pa.a;
			Color c = pa.b;
			r = e.getRealRec();

			//Adjust rectangle to represent new bubble
			r.x = (r.x+r.width/2)-(p.y*r.width)/2;
			r.y = (r.y+r.height/2)-(p.y*r.height)/2;
			r.width = p.y*r.width;
			r.height = p.y*r.height;
			fillOval(g, new Color(c.getRed(), c.getGreen(), c.getBlue(), percToCol(p.x)), r);
		}
	}

	public void drawXBoxButton(Graphics2D g, GradientButton b) {
		Rectangle r = b.getRealRec();
		double rad = r.width;
		Color start = new Color(b.start.getRed(), b.start.getGreen(), b.start.getBlue(), percToCol(b.getOpacity()));
		Color end = new Color(b.end.getRed(), b.end.getGreen(), b.end.getBlue(), percToCol(b.getOpacity()));
		
		//Button
		for (double i=0; i<rad; i+= 0.1) {
			Rectangle r1 = new Rectangle(r.x+i/2, r.y+i, rad-i, (rad-i)*2);
			fillOval(g, getGrad(start, end, i, rad), r1);
		}
	}

	public void drawShadow(Graphics2D g, Component c) {
		Rectangle r = c.getRealRec(c.getShadowRec());
		Color start = new Color(70, 70, 70, 255);
		Color end = new Color(GUI.bg.getRed(), GUI.bg.getGreen(), GUI.bg.getBlue(), 0);

		double size = 1;
		Rectangle r1 = new Rectangle(r.x-(r.width*(size/2)), r.y-(r.height*(size/2)), r.width*(size+1), r.height*(size+1));
		setGradientRadial(g, start, end, new float[]{0f, 1f}, r1);
		fillRect(g, r1);

	}

	public void drawRect(Graphics2D g, Color c, Rectangle r) {
		g.setColor(c);
		g.drawRect(cW(r.x), cH(r.y), cW(r.width), cH(r.height));
	}
	
	public void fillRect(Graphics2D g, Color c, Rectangle r) {
		g.setColor(c);
		g.fillRect(cW(r.x), cH(r.y), cW(r.width), cH(r.height));
	}

	public void fillRect(Graphics2D g, Rectangle r) {
		g.fillRect(cW(r.x), cH(r.y), cW(r.width), cH(r.height));
	}
	
	public void drawRoundRect(Graphics2D g, Color c, Rectangle r) {
		g.setColor(c);
		g.drawRoundRect(cW(r.x), cH(r.y), cW(r.width), cH(r.height), 10, 10);
	}

	public void fillRoundRect(Graphics2D g, Color c, Rectangle r) {
		g.setColor(c);
		g.fillRoundRect(cW(r.x), cH(r.y), cW(r.width), cH(r.height), 10, 10);
	}
	
	public void fillRoundRect(Graphics2D g, Color c, Rectangle r, int[] corners) {
		g.setColor(c);
		g.fillRoundRect(cW(r.x), cH(r.y), cW(r.width), cH(r.height), 10, 10);
		
		if (corners.length==0) return;
		List<Integer> cor = Arrays.stream(corners).boxed().collect(Collectors.toList());
		
		/*
		 * If a corner is not present then fill out the rounded edge. Corners
		 * go in anti-clockwise order with 1 being top left and 4 being top right.
		 */
		if (!cor.contains(1)) g.fillRect(cW(r.x), cH(r.y), cW(r.width*0.2), cH(r.height*0.2));
		if (!cor.contains(2)) g.fillRect(cW(r.x), cH(r.y+r.height*0.8), cW(r.width*0.2), cH(r.height*0.2));
		if (!cor.contains(3)) g.fillRect(cW(r.x+r.width*0.8), cH(r.y+r.height*0.8), cW(r.width*0.2), cH(r.height*0.2));
		if (!cor.contains(4)) g.fillRect(cW(r.x+r.width*0.8), cH(r.y), cW(r.width*0.2), cH(r.height*0.2));
	}

	public void drawOval(Graphics2D g, Color c, Rectangle r) {
		g.setColor(c);
		g.drawOval(cW(r.x), cH(r.y), cW(r.width), cH(r.height));
	}

	public void fillOval(Graphics2D g, Color c, Rectangle r) {
		g.setColor(c);
		g.fillOval(cW(r.x), cH(r.y), cW(r.width), cH(r.height));
	}
	
	public void drawLine(Graphics2D g, Color c, Point p1, Point p2) {
		g.setColor(c);
		g.drawLine(cW(p1.x), cH(p1.y), cW(p2.x), cH(p2.y));
	}
	
	public void drawCenteredString(Graphics2D g, Font f, String s, Color c, Rectangle r) {
		FontMetrics metrics = g.getFontMetrics(f);
		int x = (int) (cW(r.x)+(cW(r.width)-metrics.stringWidth(s))/2);
		int y = (int) (cH(r.y)+((cH(r.height)-metrics.getHeight())/2)+metrics.getAscent());
		g.setFont(f);
		g.setColor(c);
		g.drawString(s, x, y);
	}
	
	public void drawStringFromPoint(Graphics2D g, Font f, String s, Color c, Point p) {
		FontMetrics metrics = g.getFontMetrics(f);
		int y = (int) (cH(p.y)+(-metrics.getHeight()/2))+metrics.getAscent();
		g.setFont(f);
		g.setColor(c);
		g.drawString(s, cW(p.x), y);
	}
	
	public double getStringWidthAsPerc(Graphics2D g, Font f, String s) {
		FontMetrics metrics = g.getFontMetrics(f);
		return cWR(metrics.stringWidth(s));
	}
	
	public double getStringHeightAsPerc(Graphics2D g, Font f) {
		FontMetrics metrics = g.getFontMetrics(f);
		return cHR(metrics.getHeight());
	}

	public void setGradientLinear(Graphics2D g, Color start, Color end, Rectangle gR) {
		GradientPaint gr = new GradientPaint(cW(gR.x), cH(gR.y), start, cW(gR.width), cH(gR.height), end);
		g.setPaint(gr);
	}

	public void setGradientRadial(Graphics2D g, Color start, Color end, float[] fracts, Rectangle gR) {
		Rectangle2D r = new Rectangle2D.Double(cW(gR.x), cH(gR.y), cW(gR.width), cH(gR.height));
		Color[] cols = {start, end};
		RadialGradientPaint gr = new RadialGradientPaint(r, fracts, cols, CycleMethod.NO_CYCLE);
		g.setPaint(gr);
	}

	public Color getGrad(Color start, Color end, double i, double total) {
		int r, g, b, a;
		r = (int) (start.getRed()+(((end.getRed()-start.getRed())/total)*i));
		g = (int) (start.getGreen()+(((end.getGreen()-start.getGreen())/total)*i));
		b = (int) (start.getBlue()+(((end.getBlue()-start.getBlue())/total)*i));
		a = (int) (start.getAlpha()+(((end.getAlpha()-start.getAlpha())/total)*i));

		if (r>255) r = 255; if (r<0) r = 0;
		if (g>255) g = 255; if (g<0) g = 0;
		if (b>255) b = 255; if (b<0) b = 0;
		if (a>255) a = 255; if (a<0) a = 0;
		return new Color(r, g, b, a);
	}

	public int percToCol(double p) {
		int c = (int) ((p/100)*255);
		if (c>255) c = 255;
		if (c<0) c = 0;
		return c;
	}

	/**
	 * Scales a percentage of the screen width to an actual x point
	 * @param p
	 * @return
	 */
	public int cW(double p) {
		return (int) Math.round(screen.width*((double) p/100));
	}

	/**
	 * Scales a percentage of the screen height to an actual y point
	 * @param p
	 * @return
	 */
	public int cH(double p) {
		return (int) Math.round(screen.height*((double) p/100));
	}

	/**
	 * Scales an actual x point to a percentage of screen width
	 * @param p
	 * @return
	 */
	public double cWR(double p) {
		return (p/screen.width)*100;
	}

	/**
	 * Scales an actual y point to a percentage of screen height
	 * @param p
	 * @return
	 */
	public double cHR(double p) {
		return (p/screen.height)*100;
	}
}
