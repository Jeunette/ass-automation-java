import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class ImageSystem {

    private static final int MAX_ACCEPTABLE_DIFFERENCE = 18;
    private static final int MIN_BOX_BORDER_DIFFERENCE = 15;
    private static final int MAX_WHITE_STACK = 15;
    private static final double FIR_GAP = 2.0;
    private static final double REF_GAP = 7.0;
    private static final double CON_GAP = 2.0;

    private static final int[] MIN_BOX_COLOR = {225, 225, 225};
    private static final int[] MIN_WHITE_COLOR = {245, 243, 246};

    private static final String VALIDATION_STR = "###PJSCD-IS###";

    public File directory;
    public LinkedList<ImageData> list;
    public LinkedList<ImageDataResult> results;
    public String[] files;
    public BufferedImage current;

    public int[][] boxRef, borderRef;
    public int whiteStack;

    public ImageSystem(File file) throws IOException, InterruptedException {
        this.list = new LinkedList<>();
        this.results = new LinkedList<>();
        if (file.isDirectory()) {
            this.directory = file;
            initialize();
        } else {
            this.directory = null;
            read(file);
        }
        analyse();
    }

    public LinkedList<ImageDataResult> getResults() { return results; }

    public LinkedList<ImageDataResult> getFormattedResults() {
        LinkedList<ImageDataResult> results = new LinkedList<>(this.results);
        for (int i = 0; i < results.size();) {
            if (!results.get(i).dialogue || (!results.get(i).start && !results.get(i).out)) {
                results.remove(i);
            } else {
                i++;
            }
        }
        return results;
    }

    public void read(File in) throws IOException {
        Scanner scanner = new Scanner(new BufferedReader(new FileReader(in)));
        if (!scanner.nextLine().contains(VALIDATION_STR)) {
            scanner.close();
            throw new FileNotFoundException("Invalid File!");
        }
        scanner.nextLine();
        System.out.println("ImageSystem save file located...");
        System.out.println("Loading ImageData...");
        while (scanner.hasNextLine()) {
            scanner.nextLine();
            list.add(new ImageData(scanner));
        }
        System.out.println("ImageSystem initialized...");
        scanner.close();
    }

    public void save(File out) throws IOException {
        FileWriter temp = new FileWriter(out);
        BufferedWriter writer = new BufferedWriter(temp);
        writer.append(VALIDATION_STR).append("\n");
        writer.append((this.directory != null) ? this.directory.getAbsolutePath() : "" ).append("\n");
        for (int i = 0; i < files.length; i++) {
            writer.append(files[i]).append("\n");
            writer.append(list.get(i).toString());
        }
        writer.close();
        temp.close();
        System.out.println("ImageSystem data saved to " + out.getAbsolutePath());
    }

    public void saveResults(File out) throws IOException {
        FileWriter temp = new FileWriter(out);
        BufferedWriter writer = new BufferedWriter(temp);
        writer.append(VALIDATION_STR).append("\n");
        writer.append((this.directory != null) ? this.directory.getAbsolutePath() : "" ).append("\n");
        for (ImageDataResult result : results) { writer.append(result.toString()).append("\n"); }
        writer.close();
        temp.close();
        System.out.println("ImageSystem full-size result saved to " + out.getAbsolutePath());
    }

    public void saveFormattedResults(File out) throws IOException {
        LinkedList<ImageDataResult> results = getFormattedResults();
        FileWriter temp = new FileWriter(out);
        BufferedWriter writer = new BufferedWriter(temp);
        writer.append(VALIDATION_STR).append("\n");
        writer.append((this.directory != null) ? this.directory.getAbsolutePath() : "" ).append("\n");
        for (ImageDataResult result : results) { writer.append(result.toString()).append("\n"); }
        writer.close();
        temp.close();
        System.out.println("ImageSystem formatted result saved to " + out.getAbsolutePath());
    }

    public boolean isDialogue(int index) {
        return !validateDiff(rgbDifference(MIN_BOX_COLOR, list.get(index).main), -255, 0)
                && validateDiff(rgbDifference(list.get(index).box, boxRef), -MAX_ACCEPTABLE_DIFFERENCE, MAX_ACCEPTABLE_DIFFERENCE, 1)
                && validateDiff(rgbDifference(list.get(index).border, borderRef), -MAX_ACCEPTABLE_DIFFERENCE, MAX_ACCEPTABLE_DIFFERENCE, 1)
                && validateDiff(rgbDifference(list.get(index).border, list.get(index).box), MIN_BOX_BORDER_DIFFERENCE, 255, 1)
                && !validateDiff(rgbDifference(list.get(index).border, list.get(index).box), 0, 1, 1);
    }

    public boolean isIn(int index) { return results.get(index).dialogue && !results.get(index - 1).dialogue; }

    public boolean isOut(int index) { return results.get(index).dialogue && !results.get(index + 1).dialogue; }

    public boolean isStart(int index) {
        if (results.get(index).in || !validateDiff(rgbDifference(list.get(index - 1).ref, list.get(index).ref), REF_GAP)) {
            whiteStack = 0;
            return true;
        } else {
            if (!validateDiff(rgbDifference(MIN_WHITE_COLOR, list.get(index).ref), -255, 0) && (whiteStack == 0 || validateDiff(rgbDifference(list.get(index - 1).ref, list.get(index).ref), -CON_GAP, CON_GAP))) {
                whiteStack++;
                if (whiteStack > MAX_WHITE_STACK && !validateDiff(rgbDifference(list.get(index - 1).fir, list.get(index).fir), -FIR_GAP, FIR_GAP)) {
                    whiteStack = 0;
                    System.out.println("\033[1;93mATTENTION\u001B[0m: Single width text found at \033[1;97mframe " + index + "\u001B[0m. Please verify!");
                    return true;
                }
            } else {
                whiteStack = 0;
            }
            return false;
        }
    }

    private void initialize() throws IOException, InterruptedException {
        System.out.println("Initializing ImageData System...");
        while ( Objects.requireNonNull(this.directory.list()).length == 0) { Thread.sleep(2000); }
        processImages(new ColorAnalyzer());
        System.out.println("ImageSystem initialized.");
    }

    private void processImages(ColorAnalyzer analyzer) throws IOException, InterruptedException {
        System.out.println("Analysing images...");
        this.files = this.directory.list();
        for(int index = 0; index < Objects.requireNonNull(this.files).length; ) {
            if (index % 1000 == 0) System.out.println("Processing " + files[index] + "...");
            current = ImageIO.read(new File(directory.getAbsolutePath() + "\\" + files[index]));
            list.add(analyzer.analyse(current));
            // if (index % 1000 == 0) list.getLast().print();
            this.files = this.directory.list();
            assert this.files != null;
            if (++index >= this.files.length) {
                this.files = this.directory.list();
                assert this.files != null;
                if (index >= this.files.length) {
                    Thread.sleep(2000);
                    this.files = this.directory.list();
                }
            };
        }
        System.out.println("All images processed...");
    }

    private void analyse() {
        System.out.println("Analysing ImageData...");
        System.out.println("Finding data reference...");
        findRef();
        System.out.println("Reference data found...");
        System.out.println("Generating result...");
        this.whiteStack = 0;
        for (int i = 0; i < list.size(); i++) { results.add(new ImageDataResult(i, isDialogue(i))); }
        for (int i = 1; i < list.size(); i++) {
            if (results.get(i).dialogue) {
                results.get(i).in = isIn(i);
                results.get(i).out = isOut(i);
                results.get(i).start = isStart(i);
            }
        }
        System.out.println("Results generated.");
    }

    private void findRef() {
        ArrayList<Node> box = new ArrayList<>();
        ArrayList<Node> border = new ArrayList<>();
        box.add(new Node(this.list.get(0).box, 1));
        border.add(new Node(this.list.get(0).border, 1));
        boolean found;
        for (ImageData data : this.list) {
            found = false;
            for (int i = 0; i < box.size(); i++) {
                if (validateDiff(rgbDifference(data.box, box.get(i).arr))) {
                    box.get(i).count++;
                    if (box.get(i).count >= box.get(0).count) {
                        Collections.swap(box, 0, i);
                    } else {
                        Collections.swap(box, 1, i);
                    }
                    found = true;
                    break;
                }
            }
            if (!found) box.add(new Node(data.box, 1));
            found = false;
            for (int i = 0; i < border.size(); i++) {
                if (validateDiff(rgbDifference(data.border, border.get(i).arr))) {
                    border.get(i).count++;
                    if (border.get(i).count >= border.get(0).count) {
                        Collections.swap(border, 0, i);
                    } else {
                        Collections.swap(border, 1, i);
                    }
                    found = true;
                    break;
                }
            }
            if (!found) border.add(new Node(data.border, 1));
        }
        this.boxRef = box.get(0).arr;
        this.borderRef = border.get(0).arr;

    }

    private int[] rgbDifference(int[] c1, int[] c2) { return ColorAnalyzer.rgbDifference(c1, c2); }

    private int[][] rgbDifference(int[][] cs1, int[][] cs2) { return new int[][] {ColorAnalyzer.rgbDifference(cs1[0], cs2[0]), ColorAnalyzer.rgbDifference(cs1[1], cs2[1]), ColorAnalyzer.rgbDifference(cs1[2], cs2[2]), ColorAnalyzer.rgbDifference(cs1[3], cs2[3])}; }

    private boolean validateDiff(int[] c) { return validateDiff(c, new int[] {0, 0, 0}, new int[] {0, 0, 0}); }

    private boolean validateDiff(int[] c, int min, int max) { return validateDiff(c, new int[] {min, min, min}, new int[] {max, max, max}); }

    private boolean validateDiff(int[] c, int[] min, int[] max) { return c[0] >= min[0] && c[0] <= max [0] && c[1] >= min[1] && c[1] <= max [1] && c[2] >= min[2] && c[2] <= max [2]; }

    private boolean validateDiff(int[] c, double diff) { return ((double) (c[0] + c[1] + c[2])/3) <= diff; }

    private boolean validateDiff(int[] c, double minDiff, double maxDiff) { return ((double) (c[0] + c[1] + c[2])/3) >= minDiff && ((double) (c[0] + c[1] + c[2])/3) <= maxDiff; }

    private boolean validateDiff(int[][] cs) { return validateDiff(cs[0]) || validateDiff(cs[1]) || validateDiff(cs[2]) || validateDiff(cs[3]); }

    private boolean validateDiff(int[][] cs, int min, int max, int count) {
        return (validateDiff(cs[0], min, max) ? 1 : 0) + (validateDiff(cs[1], min, max) ? 1 : 0) + (validateDiff(cs[2], min, max) ? 1 : 0) + (validateDiff(cs[3], min, max) ? 1 :0) >= count;
    }

}

class Node {

    int[][] arr;
    int count;

    public Node(int[][] arr, int count) {
        this.arr = arr;
        this.count = count;
    }


}
