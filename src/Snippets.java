import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

public class Snippets {

    public LinkedList<Action> snippets;
    public boolean cleaned, events;
    public TransitionHandler tHandler;
    public NameHandler nHandler;

    public int locationCount;

    public Snippets() throws FileNotFoundException {
        this.snippets = new LinkedList<>();
        this.cleaned = false;
        this.events = false;
        this.tHandler = new TransitionHandler();
        this.nHandler = new NameHandler();
        this.locationCount = 0;
    }

    public void add(Action action) {
        this.snippets.add(action);
        if (action.type == ActionContent.TYPE_TRANSITION && ((Transition) action.content).type == ASSWriter.TYPE_LOCATION) locationCount++;
    }

    public void cleanUp() throws IOException {
        if (!this.cleaned) {
            for (int i = 0; i < this.snippets.size();) {
                if (this.snippets.get(i).type == ActionContent.TYPE_TRANSITION) {
                    if (this.tHandler.getIgnore(((Transition) this.snippets.get(i).content).type)) {
                        this.snippets.remove(i);
                        continue;
                    }
                }
                i++;
            }
            this.cleaned = true;
        }
    }

    public LinkedList<EventSection> getEventSections() throws IOException {
        cleanUp();
        LinkedList<EventSection> temp = new LinkedList<>();
        String screenText = SettingsHandler.refReader(SettingsHandler.REF_CAT_SCREEN_TEXT);
        int index = 0;
        temp.add(new EventSection());
        while (index < this.snippets.size() && this.snippets.get(index).type == ActionContent.TYPE_TRANSITION) {
            temp.getLast().addTransition(this.snippets.get(index++).content, tHandler);
        }
        while (index < this.snippets.size()) {
            temp.add(new EventSection(screenText));
            while (index < this.snippets.size()){
                if (this.snippets.get(index).type == ActionContent.TYPE_DIALOGUE) {
                    temp.getLast().addDialogue(this.snippets.get(index).content, nHandler, this.snippets.get(index).index);
                    index++;
                    if (((Dialogue) this.snippets.get(index - 1).content).close || this.snippets.get(index).type == ActionContent.TYPE_TRANSITION) {
                        while (index < this.snippets.size() && this.snippets.get(index).type == ActionContent.TYPE_TRANSITION) {
                            if (temp.getLast().dialogues.size() != 1 && ((Transition) this.snippets.get(index).content).type == ASSWriter.TYPE_JITTER) {
                                Event tempEvent = temp.getLast().dialogues.removeLast();
                                temp.getLast().dialogueCount--;
                                temp.add(new EventSection(screenText));
                                temp.getLast().dialogues.add(tempEvent);
                                temp.getLast().dialogueCount++;
                            }
                            temp.getLast().addTransition(this.snippets.get(index++).content, tHandler);
                        }
                        break;
                    }
                }
            }
        }
        return temp;
    }

    public void print() {
        System.out.println("Snippets: ");
        for(Action action : this.snippets) {
            System.out.println(action.toString());
        }
    }

}
