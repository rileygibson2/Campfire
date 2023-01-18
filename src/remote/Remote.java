package remote;

import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;

public class Remote {
    public static void main(String[] args) {
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        for(Controller c : controllers){
    		System.out.println("Name: " + c.getName() + " type: " + c.getType());
    	}
        
        for (int i = 0; i < controllers.length; i++) {
            if (controllers[i].getType() == Controller.Type.GAMEPAD) {
                Controller controller = controllers[i];
                while (true) {
                    controller.poll();
                    EventQueue queue = controller.getEventQueue();
                    Event event = new Event();
                    while (queue.getNextEvent(event)) {
                        String buttonName = event.getComponent().getName();
                        float value = event.getValue();
                        if (value == 1.0f) {
                            System.out.println("Button pressed: " + buttonName);
                        }
                    }
                }
            }
        }
    }
}