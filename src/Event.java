public class Event {

    public static final String EVENT_FORMAT = "%s: 0,%s,%s,%s,%s,0,0,0,,%s";

    public static final String EVENT_DIALOGUE = "Dialogue";
    public static final String EVENT_COMMENT = "Comment";

    public String toString() { return String.format(EVENT_FORMAT, comment ? EVENT_COMMENT : EVENT_DIALOGUE, start, end, style,
            (name != null) ? name : "", (text != null) ? text : ASSWriter.CAUTION + "\\N" + hashCode()); }

    String start, end, style, name, text;
    double startTime, endTime;
    boolean fade, comment;

    public Event(String style, String name, String text, boolean fade) {
        this.style = style;
        this.name = name;
        this.text = text;
        this.startTime = 0.0;
        this.endTime = 0.0;
        this.start = ASSWriter.getTimestamp(startTime);
        this.end = ASSWriter.getTimestamp(endTime);
        this.fade = fade;
        this.comment = false;
    }

    public Event(String style, String name, String text, boolean fade, boolean comment) {
        this.style = style;
        this.name = name;
        this.text = text;
        this.startTime = 0.0;
        this.endTime = 0.0;
        this.start = ASSWriter.getTimestamp(startTime);
        this.end = ASSWriter.getTimestamp(endTime);
        this.fade = fade;
        this.comment = comment;
    }

    public void setTime(double startTime, double endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.start = ASSWriter.getTimestamp(startTime);
        this.end = ASSWriter.getTimestamp(endTime);
    }

}
