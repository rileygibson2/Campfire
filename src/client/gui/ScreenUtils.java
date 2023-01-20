package client.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import client.gui.components.Button;
import client.gui.components.Component;
import client.gui.components.DropDown;
import client.gui.components.Image;
import client.gui.components.Label;
import client.gui.components.SimpleBox;
import client.gui.components.TextBox;
import client.gui.components.XboxButton;
import client.gui.views.CallView;
import general.Pair;
import general.Point;
import general.Rectangle;
import general.Utils;
import threads.ThreadController;

public class ScreenUtils {

	static Rectangle screen = ClientGUI.screen;
	static Color bg = new Color(20, 20, 20);

	public static void drawBase(Graphics2D g) {
		fillRect(g, bg, new Rectangle(0, 0, 100, 100));
	}

	public static void drawLabel(Graphics2D g, Label l) {
		Rectangle r = l.getRealRec();

		g.setColor(l.col);
		g.setFont(l.font);
		g.drawString(l.text, cW(r.x), cH(r.y));
	}

	public static void drawDropDown(Graphics2D g, DropDown<?> s) {
		Rectangle r = s.getRealRec();
		fillRoundRect(g, new Color(80, 80, 80), r);

		if (!s.selected) return; //Closed selector

		//Open selector
		Rectangle r2 = s.r.clone();
		r2.y += r2.height;
		r2.height = s.getOptions().size()*r2.height;
		r2 = s.getRealRec(r2);
		fillRoundRect(g, new Color(100, 100, 100), r2); //Bottom part
		r2.height = 2;
		r2.y -= 1;
		fillRect(g, new Color(100, 100, 100), r2); //Mask curve between top and bottom

		//Top bar
		fillRect(g, new Color(50, 50, 50), new Rectangle(r.x, r.y+r.height-1, r.width, 0.5));

		//Bottom bars
		for (int i=1; i<s.getOptions().size(); i++) {
			fillRect(g, new Color(60, 60, 60), new Rectangle(r.x, r.y+r.height*(i+1), r.width, 0.5));
		}
	}

	public static void drawButton(Graphics2D g, Button b) {
		Rectangle r = b.getRealRec();

		fillRoundRect(g, b.col, r);
	}

	public static void drawTextBox(Graphics2D g, TextBox t) {
		Rectangle r = t.getRealRec();

		fillRoundRect(g, new Color(100, 100, 100), r); //Main box
		if (t.selected) drawRoundRect(g, new Color(120, 120, 120), r); //Highlight
	}

	public static void drawImage(Graphics2D g, Image i) {
		Rectangle r = i.getRealRec();
		BufferedImage img = null;
		try {img = ImageIO.read(Utils.getInputStream("assets/"+i.src));}
		catch (IOException e) {e.printStackTrace();}
		
		g.drawImage(img, cW(r.x), cH(r.y), cW(r.width), cH(r.height), null);
	}

	public static void drawDataWave(Graphics2D g, CallView v, SimpleBox b) {
		Rectangle r = b.getRealRec();

		//Gradient
		Color start = new Color(60, 60, 60, 255);
		Color end = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 0);
		//start = Color.RED;
		//end = Color.BLUE;
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

	public static void drawPulse(Graphics2D g, ThreadController tC) {
		if (!tC.hasElements()) return;
		Element e = (Element) tC.getTarget();
		Rectangle r;

		for (Object o : tC.getElements()) {
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

	public static void drawXBoxButton(Graphics2D g, XboxButton b) {
		Rectangle r = b.getRealRec();
		double rad = r.width;

		//Button
		for (double i=0; i<rad; i+= 0.1) {
			Rectangle r1 = new Rectangle(r.x+i/2, r.y+i, rad-i, (rad-i)*2);
			fillOval(g, getGrad(b.start, b.end, i, rad), r1);
		}

		//Label
		g.setColor(b.start);
		g.setFont(new Font("Verdana", Font.BOLD, 30));
		g.drawString(b.label, cW(r.x+rad*0.25), cH(r.y+rad*1.55));
	}

	public static void drawShadow(Graphics2D g, Component c) {
		Rectangle r = c.getRealRec();
		Color start = new Color(80, 80, 80, 255);
		Color end = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 0);

		double size = 1;
		Rectangle r1 = new Rectangle(r.x-(r.width*(size/2)), r.y-(r.height*(size/2)), r.width*(size+1), r.height*(size+1));
		setGradientRadial(g, start, end, new float[]{0f, 1f}, r1);
		fillRect(g, r1);

	}

	public static void fillRect(Graphics2D g, Color c, Rectangle r) {
		g.setColor(c);
		g.fillRect(cW(r.x), cH(r.y), cW(r.width), cH(r.height));
	}

	public static void fillRect(Graphics2D g, Rectangle r) {
		g.fillRect(cW(r.x), cH(r.y), cW(r.width), cH(r.height));
	}

	public static void fillRoundRect(Graphics2D g, Color c, Rectangle r) {
		g.setColor(c);
		g.fillRoundRect(cW(r.x), cH(r.y), cW(r.width), cH(r.height), 10, 10);
	}

	public static void drawRoundRect(Graphics2D g, Color c, Rectangle r) {
		g.setColor(c);
		g.drawRoundRect(cW(r.x), cH(r.y), cW(r.width), cH(r.height), 10, 10);
	}

	public static void drawOval(Graphics2D g, Color c, Rectangle r) {
		g.setColor(c);
		g.drawOval(cW(r.x), cH(r.y), cW(r.width), cH(r.height));
	}

	public static void fillOval(Graphics2D g, Color c, Rectangle r) {
		g.setColor(c);
		g.fillOval(cW(r.x), cH(r.y), cW(r.width), cH(r.height));
	}

	public static void setGradientLinear(Graphics2D g, Color start, Color end, Rectangle gR) {
		GradientPaint gr = new GradientPaint(cW(gR.x), cH(gR.y), start, cW(gR.width), cH(gR.height), end);
		g.setPaint(gr);
	}

	public static void setGradientRadial(Graphics2D g, Color start, Color end, float[] fracts, Rectangle gR) {
		Rectangle2D r = new Rectangle2D.Double(cW(gR.x), cH(gR.y), cW(gR.width), cH(gR.height));
		Color[] cols = {start, end};
		RadialGradientPaint gr = new RadialGradientPaint(r, fracts, cols, CycleMethod.NO_CYCLE);
		g.setPaint(gr);
	}

	public static Color getGrad(Color start, Color end, double i, double total) {
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

	public static int percToCol(double p) {
		return (int) ((p/100)*255);
	}

	/**
	 * Scales a percentage of the screen width to an actual x point
	 * @param p
	 * @return
	 */
	public static int cW(double p) {
		return (int) Math.round(screen.width*((double) p/100));
	}

	/**
	 * Scales a percentage of the screen height to an actual y point
	 * @param p
	 * @return
	 */
	public static int cH(double p) {
		return (int) Math.round(screen.height*((double) p/100));
	}

	/**
	 * Scales an actual x point to a percentage of screen width
	 * @param p
	 * @return
	 */
	public static double cWR(double p) {
		return (p/screen.width)*100;
	}

	/**
	 * Scales an actual y point to a percentage of screen height
	 * @param p
	 * @return
	 */
	public static double cHR(double p) {
		return (p/screen.height)*100;
	}
}
