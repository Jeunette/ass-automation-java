import java.io.*;
import java.util.LinkedList;
import java.util.Scanner;

public class JsonReader {

    public static final String BLOCK_ACTION = "\"Snippets\": [";
    public static final String BLOCK_DIALOGUE = "\"TalkData\": [";
    public static final String BLOCK_TRANSITION = "\"SpecialEffectData\": [";

    public static final String IDENTIFIER_ACTION = "\"Action\": ";
    public static final String IDENTIFIER_INDEX = "\"ReferenceIndex\": ";

    public static final String IDENTIFIER_NAME = "\"WindowDisplayName\": \"";
    public static final String IDENTIFIER_TEXT = "\"Body\": \"";
    public static final String IDENTIFIER_CLOSE = "\"WhenFinishCloseWindow\": ";
    public static final String IDENTIFIER_TRUE = "1";

    public static final String IDENTIFIER_TRANSITION = "\"EffectType\": ";

    public static final String IDENTIFIER_END_INT = ",";
    public static final String IDENTIFIER_END_STR = "\",";

    public File eventStory;
    public Scanner scanner;

    public LinkedList<Action> actionList;
    public LinkedList<Dialogue> dialogueList;
    public LinkedList<Transition> transitionList;

    public Snippets snippets;

    public JsonReader(File eventStory) throws IOException {
        this.eventStory = eventStory;
        this.scanner = new Scanner(new BufferedReader(new FileReader(this.eventStory)));
        System.out.println(eventStory.getName() + " located... ");
        this.actionList = new LinkedList<>();
        this.dialogueList = new LinkedList<>();
        this.transitionList = new LinkedList<>();
        this.snippets = new Snippets();
        makeSnippets();
        System.out.println("Snippets loaded. ");
    }

    public void makeSnippets() throws IOException {
        actionReader();
        dialogueReader();
        transitionReader();
        this.scanner.close();
        for(Action action : actionList) {
            switch (action.type) {
                case ActionContent.TYPE_DIALOGUE -> {
                    action.setContent(dialogueList.get(action.index));
                    snippets.add(action);
                }
                case ActionContent.TYPE_TRANSITION -> {
                    action.setContent(transitionList.get(action.index));
                    snippets.add(action);
                }
            }
        }
        snippets.cleanUp();
    }

    public void actionReader() {
        while(scanner.hasNextLine()) {
            if(scanner.nextLine().contains(BLOCK_ACTION)) {
                break;
            }
        }
        while(scanner.hasNextLine()) {
            String temp = scanner.nextLine();
            if(temp.contains(BLOCK_DIALOGUE)) {
                break;
            }
            if(temp.contains(IDENTIFIER_ACTION)) {
                temp = temp.substring(temp.lastIndexOf(IDENTIFIER_ACTION) + IDENTIFIER_ACTION.length(), temp.lastIndexOf(IDENTIFIER_END_INT));
                actionList.add(new Action(Integer.parseInt(temp)));
            }else if(temp.contains(IDENTIFIER_INDEX)) {
                temp = temp.substring(temp.lastIndexOf(IDENTIFIER_INDEX) + IDENTIFIER_INDEX.length(), temp.lastIndexOf(IDENTIFIER_END_INT));
                actionList.getLast().setIndex(Integer.parseInt(temp));
            }
        }
    }

    public void dialogueReader() {
        while(scanner.hasNextLine()) {
            String temp = scanner.nextLine();
            if(temp.contains(BLOCK_TRANSITION)) {
                break;
            }
            if(temp.contains(IDENTIFIER_NAME)) {
                temp = temp.substring(temp.lastIndexOf(IDENTIFIER_NAME) + IDENTIFIER_NAME.length(), temp.lastIndexOf(IDENTIFIER_END_STR));
                dialogueList.add(new Dialogue(temp));
            }else if(temp.contains(IDENTIFIER_TEXT)) {
                temp = temp.substring(temp.lastIndexOf(IDENTIFIER_TEXT) + IDENTIFIER_TEXT.length(), temp.lastIndexOf(IDENTIFIER_END_STR));
                dialogueList.getLast().setText(temp);
            }else if(temp.contains(IDENTIFIER_CLOSE)) {
                temp = temp.substring(temp.lastIndexOf(IDENTIFIER_CLOSE) + IDENTIFIER_CLOSE.length(), temp.lastIndexOf(IDENTIFIER_END_INT));
                dialogueList.getLast().setClose(temp.equals(IDENTIFIER_TRUE));
            }
        }
    }

    public void transitionReader() {
        while(scanner.hasNextLine()) {
            String temp = scanner.nextLine();
            if(temp.contains(IDENTIFIER_TRANSITION)) {
                temp = temp.substring(temp.lastIndexOf(IDENTIFIER_TRANSITION) + IDENTIFIER_TRANSITION.length(), temp.lastIndexOf(IDENTIFIER_END_INT));
                transitionList.add(new Transition(Integer.parseInt(temp)));
            }
        }
        close();
    }

    public void close() { this.scanner.close(); }

}
