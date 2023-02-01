package client.gui.components;

import client.gui.GUI;
import general.GetterSubmitter;
import general.Point;
import general.Rectangle;
import threads.AnimationFactory;
import threads.AnimationFactory.Animations;
import threads.ThreadController;

public class CheckBox extends Component {

	private GetterSubmitter<Boolean, Boolean> actions;
	private SimpleBox innerBox;
	private Image tick;
	private ThreadController transform;
	private boolean checked;

	public CheckBox(Rectangle r) {
		super(r);
		checked = false;
		
		//Outer box
		SimpleBox sB = new SimpleBox(new Rectangle(0, 0, 100, 100), GUI.focus);
		//sB.setFilled(false);
		sB.setRounded(true);
		addComponent(sB);

		//Inner box
		innerBox = new SimpleBox(new Rectangle(50, 50, 0, 0), GUI.focus2);
		innerBox.setRounded(true);
		addComponent(innerBox);
		
		//Tick
		tick = new Image(new Rectangle(15, 10, 80, 80), "ok.png");
		tick.setVisible(false);
		addComponent(tick);
	}

	public void setActions(GetterSubmitter<Boolean, Boolean> a) {
		actions = a;
		checked = a.get();
		if (checked) {
			tick.setVisible(true);
			innerBox.setRec(new Rectangle(0, 0, 100, 100));
		}
		else {
			tick.setVisible(false);
			innerBox.setRec(new Rectangle(50, 50, 0, 0));
		}
	}

	@Override
	public void doClick(Point p) {
		if (actions!=null) {
			actions.submit(!checked);
			checked = actions.get();
		}
		else checked = !checked;
		
		if (transform!=null) transform.end();
		if (checked) {
			tick.setVisible(true);
			transform = AnimationFactory.getAnimation(innerBox, Animations.Transform, new Rectangle(0, 0, 100, 100));
		}
		else {
			tick.setVisible(false);
			transform = AnimationFactory.getAnimation(innerBox, Animations.Transform, new Rectangle(50, 50, 0, 0));
		}
		transform.setWait(5);
		transform.start();
		
		super.doClick(p);
	}
}
