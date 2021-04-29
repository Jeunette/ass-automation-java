import java.io.IOException;
import java.util.LinkedList;

public class EventSection {

    public static final String SCREEN_STYLE = "screen";
    public static final String SCREEN_NAME = "screen";

    public Event screen;
    public LinkedList<Event> dialogues, transitions;
    public int dialogueCount, transitionCount;

    public EventSection() {
        this.screen = null;
        this.dialogues = new LinkedList<>();
        this.transitions = new LinkedList<>();
        this.dialogueCount = 0;
        this.transitionCount = 0;
    }

    public EventSection(String screenText) {
        this.screen = new Event(SCREEN_STYLE, SCREEN_NAME, screenText, false,true);
        this.dialogues = new LinkedList<>();
        this.transitions = new LinkedList<>();
        this.dialogueCount = 0;
        this.transitionCount = 0;
    }

    public void append(EventSection section) {
        if (this.screen == null && section.screen != null) {
            this.screen = new Event(section.screen.style, section.screen.name, section.screen.text, section.screen.fade, section.screen.comment);
            this.screen.setTime(section.screen.startTime, section.screen.endTime);
        } else if (section.screen != null) {
            this.screen.setTime(this.screen.startTime, section.screen.endTime);
        }
        this.dialogues.addAll(section.dialogues);
        this.transitions.addAll(section.transitions);
        this.dialogueCount += section.dialogueCount;
        this.transitionCount += section.transitionCount;
    }

    public void comment() {
        for (Event dialogue : dialogues) {
            dialogue.comment = true;
        }
    }

    public void addDialogue(ActionContent dialogue) {
        this.dialogues.add(dialogue.toEvent());
        this.dialogueCount++;
    }

    public void addDialogue(ActionContent dialogue, NameHandler handler, int index) throws IOException {
        this.dialogues.add(((Dialogue) dialogue).toEvent(handler, index));
        this.dialogueCount++;
    }

    public void addTransition(ActionContent transition) {
        this.transitions.add(transition.toEvent());
        this.transitionCount++;
    }

    public void addTransition(ActionContent transition, TransitionHandler handler) throws IOException {
        this.transitions.add(((Transition) transition).toEvent(handler));
        this.transitionCount++;
    }

}
