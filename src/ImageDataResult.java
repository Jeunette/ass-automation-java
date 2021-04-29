public class ImageDataResult {

    public int index;
    public boolean dialogue, start, in, out;

    public ImageDataResult(int index, boolean dialogue) {
        this.index = index;
        this.dialogue = dialogue;
        this.start = false;
        this.in = false;
        this.out = false;
    }

    public String toString() { return "Index: " + index + ", Dialogue: " + dialogue + ", Start: " + start + ", In: " + in + ", out: " + out; }

    public void print() { System.out.println(toString()); }

}
