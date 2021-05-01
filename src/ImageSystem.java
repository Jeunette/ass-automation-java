import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class ImageSystem {

    private static final int MAX_ACCEPTABLE_DIFFERENCE_DEFAULT = 18;
    private static final int MIN_BOX_BORDER_DIFFERENCE_DEFAULT = 15;
    private static final int MAX_WHITE_STACK_DEFAULT = 15;
    private static final double REF_GAP_DEFAULT = 7.0;
    private static final double CON_GAP_DEFAULT = 2.0;

    private static final int[] MIN_BOX_COLOR_DEFAULT = {225, 225, 225};
    private static final int[] MIN_WHITE_COLOR_DEFAULT = {245, 243, 246};

    private int MAX_ACCEPTABLE_DIFFERENCE;
    private int MIN_BOX_BORDER_DIFFERENCE;
    private int MAX_WHITE_STACK;
    private double REF_GAP;
    private double CON_GAP;

    private int[] MIN_BOX_COLOR;
    private int[] MIN_WHITE_COLOR;

    private void readSettings() {
        MAX_ACCEPTABLE_DIFFERENCE = MAX_ACCEPTABLE_DIFFERENCE_DEFAULT;
        MIN_BOX_BORDER_DIFFERENCE = MIN_BOX_BORDER_DIFFERENCE_DEFAULT;
        MAX_WHITE_STACK = MAX_WHITE_STACK_DEFAULT;
        REF_GAP = REF_GAP_DEFAULT;
        CON_GAP = CON_GAP_DEFAULT;
        MIN_BOX_COLOR = MIN_BOX_COLOR_DEFAULT;
        MIN_WHITE_COLOR = MIN_WHITE_COLOR_DEFAULT;
        try {
            MAX_ACCEPTABLE_DIFFERENCE = Integer.parseInt(SettingsHandler.reader(SettingsHandler.CAT_MAX_ACCEPTABLE_DIFFERENCE));
        } catch (Exception ignored) {
        }
        try {
            MIN_BOX_BORDER_DIFFERENCE = Integer.parseInt(SettingsHandler.reader(SettingsHandler.CAT_MIN_BOX_BORDER_DIFFERENCE));
        } catch (Exception ignored) {
        }
        try {
            MAX_WHITE_STACK = Integer.parseInt(SettingsHandler.reader(SettingsHandler.CAT_MAX_WHITE_STACK));
        } catch (Exception ignored) {
        }
        try {
            REF_GAP = Double.parseDouble(SettingsHandler.reader(SettingsHandler.CAT_REF_GAP));
        } catch (Exception ignored) {
        }
        try {
            CON_GAP = Double.parseDouble(SettingsHandler.reader(SettingsHandler.CAT_CON_GAP));
        } catch (Exception ignored) {
        }
        try {
            String temp = SettingsHandler.reader(SettingsHandler.CAT_MIN_BOX_COLOR);
            MIN_BOX_COLOR = new int[]{Integer.parseInt(temp.substring(0, 3)), Integer.parseInt(temp.substring(4, 7)), Integer.parseInt(temp.substring(8, 11))};
        } catch (Exception ignored) {
        }
        try {
            String temp = SettingsHandler.reader(SettingsHandler.CAT_MIN_WHITE_COLOR);
            MIN_WHITE_COLOR = new int[]{Integer.parseInt(temp.substring(0, 3)), Integer.parseInt(temp.substring(4, 7)), Integer.parseInt(temp.substring(8, 11))};
        } catch (Exception ignored) {
        }
    }

    private static final String VALIDATION_STR = "###PJSCD-IS###";

    public File directory;
    public LinkedList<ImageData> list;
    public ArrayList<ImageDataResult> results;
    public String[] files;

    public int[][] boxRef, borderRef;
    public int whiteStack;

    public ImageSystem(File file) throws IOException, InterruptedException {
        this.list = new LinkedList<>();
        this.results = new ArrayList<>();
        if (file.isDirectory()) {
            this.directory = file;
            initialize();
        } else {
            this.directory = null;
            read(file);
        }
        analyse();
    }

    public List<ImageDataResult> getResults() {
        return results;
    }

    public LinkedList<ImageDataResult> getFormattedResults() {
        LinkedList<ImageDataResult> results = new LinkedList<>(this.results);
        results.removeIf(result -> !result.dialogue || (!result.start && !result.out));
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
        writer.append((this.directory != null) ? this.directory.getAbsolutePath() : "").append("\n");
        for (ImageDataResult result : results) {
            writer.append(result.toString()).append("\n");
        }
        writer.close();
        temp.close();
        System.out.println("ImageSystem formatted result saved to " + out.getAbsolutePath());
    }

    public boolean isDialogue(ImageData data) {
        return !validateDiff(rgbDifference(MIN_BOX_COLOR, data.main), -255, 0)
                && validateDiff(rgbDifference(data.box, boxRef), -MAX_ACCEPTABLE_DIFFERENCE, MAX_ACCEPTABLE_DIFFERENCE, 1)
                && validateDiff(rgbDifference(data.border, borderRef), -MAX_ACCEPTABLE_DIFFERENCE, MAX_ACCEPTABLE_DIFFERENCE, 1)
                && validateDiff(rgbDifference(data.border, data.box), MIN_BOX_BORDER_DIFFERENCE, 255, 1)
                && !validateDiff(rgbDifference(data.border, data.box), 0, 0, 1);
    }

    public boolean isIn(ImageDataResult previous, ImageDataResult current) {
        return current.dialogue && !previous.dialogue;
    }

    public boolean isOut(ImageDataResult current, ImageDataResult next) {
        return current.dialogue && !next.dialogue;
    }

    public boolean isStart(ImageDataResult current, ImageData previousData, ImageData currentData) {
        if (current.in || !validateDiff(rgbDifference(previousData.ref, currentData.ref), REF_GAP)) {
            whiteStack = 0;
            return true;
        } else {
            if (!validateDiff(rgbDifference(MIN_WHITE_COLOR, currentData.ref), -255, 0) && (whiteStack == 0 || validateDiff(rgbDifference(previousData.ref, currentData.ref), -CON_GAP, CON_GAP))) {
                whiteStack++;
                if (whiteStack > MAX_WHITE_STACK && !validateDiff(rgbDifference(previousData.fir, currentData.fir), -CON_GAP, CON_GAP)) {
                    whiteStack = 0;
                    System.out.println("\033[1;93mATTENTION\u001B[0m: Single width text found at \033[1;97mframe " + current.index + "\u001B[0m. Please verify!");
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
        while (Objects.requireNonNull(this.directory.list()).length == 0) {
            //noinspection BusyWait
            Thread.sleep(2000);
        }
        this.files = this.directory.list();
        processImages(new ColorAnalyzer(ImageIO.read(new FileInputStream(directory.getAbsolutePath() + "\\" + files[0]))));
        System.out.println("ImageSystem initialized.");
    }

    private void processImages(ColorAnalyzer analyzer) throws IOException, InterruptedException {
        System.out.println("Analysing images...");
        for(int index = 0; index < Objects.requireNonNull(this.files).length; ) {
            if (index % 1000 == 0) System.out.println("Processing " + files[index] + "...");
            DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new FileInputStream(directory.getAbsolutePath() + "/" + files[index])));
            byte[] temp = new byte[datainputstream.available()];
            datainputstream.readFully(temp);
            datainputstream.close();
            BufferedImage buffered = ImageIO.read(new ByteArrayInputStream(temp));
            list.add(analyzer.analyse(buffered));
            // if (index % 1000 == 0) list.getLast().print();
            if (++index >= this.files.length) {
                this.files = this.directory.list();
                assert this.files != null;
                if (index >= this.files.length) {
                    //noinspection BusyWait
                    Thread.sleep(2000);
                    this.files = this.directory.list();
                }
            }
        }
        System.out.println("All images processed...");
    }

    private void analyse() {
        System.out.println("Analysing ImageData...");
        System.out.println("Finding data reference...");
        findRef();
        System.out.println("Reference data found...");
        System.out.println("Generating result...");
        readSettings();
        this.whiteStack = 0;
        ListIterator<ImageData> dataIterator = list.listIterator();
        Queue<ImageDataResult> tempResults = new LinkedList<>();
        for (int i = 0; dataIterator.hasNext(); i++) {
            tempResults.add(new ImageDataResult(i, isDialogue(dataIterator.next())));
        }
        ImageDataResult previous;
        ImageDataResult current = tempResults.poll();
        ImageDataResult next = tempResults.poll();
        dataIterator = list.listIterator();
        results.add(current);
        while (tempResults.size() > 1) {
            previous = current;
            current = next;
            next = tempResults.poll();
            if (current.dialogue) {
                current.in = isIn(previous, current);
                current.out = isOut(current, next);
                current.start = isStart(current, dataIterator.next(), dataIterator.next());
                dataIterator.previous();
            } else {
                dataIterator.next();
            }
            results.add(current);
        }
        results.add(tempResults.poll());
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
