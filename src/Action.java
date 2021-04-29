public class Action {

    public static final String ACTION = "ACTION";
    public static final String TEXT_SPlITER = ": ";
    public static final String INDEX_SPlITER = " - ";
    public static final String DATA_SPlITER = ", ";

    public int type, index;
    public ActionContent content;

    public Action(int type, int index, ActionContent content) {
        this.type = type;
        this.index = index;
        this.content = content;
    }

    public Action(int type) {
        this.type = type;
        this.index = -1;
        this.content = null;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setContent(ActionContent content) {
        this.content = content;
    }

    public String toString() { return ACTION + TEXT_SPlITER + this.type + INDEX_SPlITER + this.index + DATA_SPlITER + content.toString();}

}

