package client.gui.components;

import java.awt.Color;

import general.CLI;
import general.Point;
import general.Rectangle;

public class Slider extends Component {

	private Runnable onUpdate;
	private SimpleBox mainBox;
	private SimpleBox ball;

	private double value;
	private SimpleBox groove;
	private SimpleBox grooveFill;
	
	public Slider(Rectangle r) {
		super(r);

		//Main box
		mainBox = new SimpleBox(new Rectangle(0, 0, 100, 100), new Color(70, 70, 70));
		mainBox.setRounded(new int[]{4, 3});
		mainBox.increasePriority();
		addComponent(mainBox);

		//Groove
		groove = new SimpleBox(new Rectangle(10, 40, 80, 20), new Color(100, 100, 100));
		mainBox.addComponent(groove);
		
		//Coloured Groove
		grooveFill = new SimpleBox(new Rectangle(groove.getX(), groove.getY(), 0, groove.getHeight()), new Color(150, 100, 100));
		mainBox.addComponent(grooveFill);

		//Ball
		ball = new SimpleBox(new Rectangle(groove.getX(), 20, 14, 60), new Color(255, 100, 100));
		ball.setOval(true);
		mainBox.addComponent(ball);
	}

	public void setValue(double v) {
		value = v;
		v /= 100;
		ball.setX(groove.getX()+(v*groove.getWidth())-ball.getWidth()/2);
		grooveFill.setWidth(v*groove.getWidth());
	}
	
	public double getValue() {return value;}
	
	public void setUpdateAction(Runnable r) {onUpdate = r;}
	
	@Override
	public void doClick(Point p) {
		double x = scalePoint(p).x*100;
		
		if (x>=groove.getX()&&x<=groove.getX()+groove.getWidth()) { //Check within bounds of groove
			value = ((x-groove.getX())/groove.getWidth())*100;
			ball.setX(x-ball.getWidth()/2);
			grooveFill.setWidth(x-grooveFill.getX());
		}
		
		if (onUpdate!=null) onUpdate.run();
		super.doClick(p);
	}
	
	@Override
	public void doDrag(Point entry, Point current) {
		double x = scalePoint(current).x*100;
		
		if (x>=groove.getX()&&x<=groove.getX()+groove.getWidth()) { //Check within bounds of groove
			value = ((x-groove.getX())/groove.getWidth())*100;
			ball.setX(x-ball.getWidth()/2);
			grooveFill.setWidth(x-grooveFill.getX());
		}
		
		if (onUpdate!=null) onUpdate.run();
		super.doDrag(entry, current);
	}
}
