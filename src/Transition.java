import java.io.IOException;

public class Transition implements ActionContent{

    public int type;

    public Transition(int type) { this.type = type; }

    public Event toEvent() { return new Event(TRANSITION_STYLE, TRANSITION_NAME, null, false); }

    public Event toEvent(TransitionHandler handler) throws IOException { return new Event(TRANSITION_STYLE, TRANSITION_NAME, handler.getDescription(type), false); }

    public String toString() { return TRANSITION + CONTENT_SPlITER + this.type; }

}
