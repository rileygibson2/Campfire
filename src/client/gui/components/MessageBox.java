package client.gui.components;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import client.gui.GUI;
import general.Point;
import general.Rectangle;
import threads.AnimationFactory;
import threads.AnimationFactory.Animations;
import threads.ThreadController;

public class MessageBox extends Component {

	public static Color error = new Color(220, 100, 100);
	public static Color ok = new Color(100, 200, 100);
	//public static Color info = new Color(80, 80, 80);
	public static Color info = GUI.focus;
	public static Color update = new Color(100, 100, 200);
	
	private SimpleBox mainBox;
	private Label label;
	private ThreadController move;
	private ThreadController fade;
	

	public MessageBox(String text, Color col, double goalY, int hold) {
		super(new Rectangle(50, -15, 10, 10));

		//Smother
		/*SimpleBox smother = new SimpleBox(new Rectangle(0, 0, 100, 100), new Color(0, 0, 0));
		smother.setOpacity(50);
		smother.setAbsolute(true);
		addComponent(smother);*/

		//Main box
		mainBox = new SimpleBox(new Rectangle(0, 0, 100, 100), col);
		mainBox.setRounded(true);
		mainBox.increasePriority();
		addComponent(mainBox);
		
		label = new Label(new Point(49, 50), text, new Font(GUI.baseFont, Font.BOLD, 12), new Color(255, 255, 255));
		label.setCentered(true);
		mainBox.addComponent(label);
		
		double w = GUI.getInstance().getScreenUtils().getStringWidthAsPerc(label.font, label.text)+5;
		double h = GUI.getInstance().getScreenUtils().getStringHeightAsPerc(label.font, label.text)+5;
		setX((100-w)/2);
		setWidth(w);
		setHeight(h);
		
		fade = AnimationFactory.getAnimation(this, Animations.Fade, 100);
		fade.start();
		move = AnimationFactory.getAnimation(this, Animations.MoveTo, new Point(getX(), goalY));
		move.setFinishAction(() -> {
			move.sleep(hold);
			GUI.getInstance().removeMessage(this);
			fade = AnimationFactory.getAnimation(this, Animations.Fade, 0);
			fade.setFinishAction(() -> removeFromParent());
			fade.start();
		});
		move.start();
		
		setOpacity(0);
	}
	
	public void updateGoal(double goalY) {
		List<Object> extras = new ArrayList<>();
		extras.add(new Point(getX(), goalY));
		
		if (move!=null&&!move.isDoomed()) move.setExtras(extras);
		else {
			move = AnimationFactory.getAnimation(this, Animations.MoveTo, new Point(getX(), goalY));
			move.start();
		}
	}
}
