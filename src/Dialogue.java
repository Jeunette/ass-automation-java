import java.io.IOException;

public class Dialogue implements ActionContent{

    public String name, text;
    public boolean close;

    public Dialogue(String name, String text, boolean close) {
        this.name = name;
        this.text = text;
        this.close = close;
    }

    public Dialogue(String name) {
        this.name = name;
        this.text = null;
        this.close = false;
    }

    public Event toEvent() { return new Event(DIALOGUE_STYLE, name, text, close); }

    public Event toEvent(NameHandler handler) throws IOException { return new Event(handler.getStyle(name), name, text, close); }

    public Event toEvent(NameHandler handler, int index) throws IOException { return new Event(handler.getStyle(name), name, index + " | " + text, close); }

    public void setName(String name) { this.name = name; }

    public void setText(String text) { this.text = text; }

    public void setClose(boolean close) { this.close = close; }

    public String toString() { return DIALOGUE + CONTENT_SPlITER + this.name + TEXT_SPlITER + this.text + DATA_SPlITER + this.close; }

}
