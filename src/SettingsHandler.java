import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class SettingsHandler {

    public static final String CAT_REFERENCE_OVERWRITE = "[Reference File Path]";
    public static final String CAT_LIST_REFERENCE_PATH = "[Width Height Reference-File-Path]";
    public static final String CAT_ASS_PATH = "[Sample ASS Path]";

    public static final String CAT_MAX_ACCEPTABLE_DIFFERENCE = "[Max Acceptable Color Difference]";
    public static final String CAT_MIN_BOX_BORDER_DIFFERENCE = "[Box Border Difference]";
    public static final String CAT_MAX_WHITE_STACK = "[Singe Width White Frames]";
    public static final String CAT_POS_GAP = "[Box Border Ref Gap]";
    public static final String CAT_REF_GAP = "[Reference Char Gap]";
    public static final String CAT_CON_GAP = "[Continuous Gap]";
    public static final String CAT_MIN_BOX_COLOR = "[Min Box Color]";
    public static final String CAT_MIN_WHITE_COLOR = "[Min White Color]";

    public static final String CAT_SCREEN_START_OFFSET = "[Screen Start Offset]";
    public static final String CAT_SCREEN_END_OFFSET = "[Screen End Offset]";

    public static final String CAT_DEFAULT_STYLE = "[Default Style]";

    public static final String CAT_LIST_TRANSITION_INFO = "[TransitionType Description Ignore]";
    public static final String CAT_LIST_NAME_INFO = "[Name Style]";

    public static final String REF_CAT_SCREEN_TEXT = "[SCREEN TEXT]";
    public static final String REF_CAT_LOCATION_SCREEN_TEXT = "[LOCATION SCREEN TEXT]";
    public static final String REF_CAT_LOCATION_TEXT = "[LOCATION TEXT]";

    public static final String REF_CAT_FIR_MX = "[TEXT FIR MX]";
    public static final String REF_CAT_REF_MX = "[TEXT REF MX]";
    public static final String REF_CAT_BOX_MX = "[TEXT BOX MX]";
    public static final String REF_CAT_BORDER_MX = "[TEXT BOX BORDER MX]";

    public static final String FILE_SETTINGS = "settings.txt";

    public static String referencePath = null;
    public static boolean debugMode = false;

    public static void setReferencePath(String videoPath) throws IOException {
        String reference = reader(CAT_REFERENCE_OVERWRITE);
        File check = new File(reference);
        if (check.isFile()) {
            referencePath = reference;
            System.out.println("REFERENCE: " + reference);
            return;
        }
        ArrayList<String> list = listReader(CAT_LIST_REFERENCE_PATH);
        VideoCapture capture = new VideoCapture(videoPath);
        int width = (int) capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
        int height = (int) capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
        for (String line : list) {
            String[] temp = line.split(" ");
            if (Integer.parseInt(temp[0]) == width && Integer.parseInt(temp[1]) == height) {
                check = new File(temp[2]);
                if (!check.isFile()) {
                    System.out.println(temp[2] + " Not Found.");
                    System.exit(2);
                }
                referencePath = temp[2];
                System.out.println("REFERENCE: " + temp[2]);
                return;
            }
        }
        System.out.println("Detected resolution: " + width + " x " + height);
        System.out.println("Reference file not detected!");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Path of reference file: ");
        String temp = scanner.next();
        scanner.nextLine();
        check = new File(temp);
        if (!check.isFile()) {
            System.out.println(temp + " Not Found.");
            System.exit(2);
        }
        referencePath = temp;
        listWriter("" + width + " " + height + " " + referencePath, CAT_LIST_REFERENCE_PATH, new File(FILE_SETTINGS));
    }

    public static void listWriter(String str, String cat, File file) throws IOException {
        Scanner scanner = new Scanner(new BufferedReader(new FileReader(file)));
        StringBuilder newFile = new StringBuilder();
        while (scanner.hasNextLine()) {
            String temp = scanner.nextLine();
            if (temp.contains(cat)) {
                do {
                    newFile.append(temp).append("\n");
                    temp = scanner.nextLine();
                } while (!temp.contains(cat));
                newFile.append(str).append("\n");
            }
            newFile.append(temp).append("\n");
        }
        scanner.close();
        FileWriter writer = new FileWriter(file);
        writer.append(newFile);
        writer.close();
    }

    public static String reader(String cat, File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(new BufferedReader(new FileReader(file)));
        while(scanner.hasNextLine()) {
            if (scanner.nextLine().contains(cat)) {
                String temp = scanner.nextLine();
                scanner.close();
                return temp;
            }
        }
        scanner.close();
        throw new FileNotFoundException();
    }

    public static ArrayList<String> listReader(String cat, File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(new BufferedReader(new FileReader(file)));
        ArrayList<String> list = new ArrayList<>();
        while(scanner.hasNextLine()) {
            if (scanner.nextLine().contains(cat)) {
                String temp = scanner.nextLine();
                while (!temp.contains(cat)) {
                    list.add(temp);
                    temp = scanner.nextLine();
                }
                scanner.close();
                return list;
            }
        }
        scanner.close();
        return list;
    }

    public static String reader(String cat) throws FileNotFoundException { return reader(cat, new File(FILE_SETTINGS)); }

    public static ArrayList<String> listReader(String cat) throws FileNotFoundException { return listReader(cat, new File(FILE_SETTINGS)); }

    public static String refReader(String cat) throws FileNotFoundException {
        return reader(cat, new File(referencePath));
    }

    public static ArrayList<String> refListReader(String cat) throws FileNotFoundException {
        return listReader(cat, new File(referencePath));
    }

}

class TransitionHandler {

    ArrayList<TransitionInfo> infos;

    public TransitionHandler() throws FileNotFoundException {
        Scanner scanner;
        this.infos = new ArrayList<>();
        this.infos.add(null);
        ArrayList<TransitionInfo> temp = new ArrayList<>();
        int maxType = 0;
        for (String str : SettingsHandler.listReader(SettingsHandler.CAT_LIST_TRANSITION_INFO)) {
            scanner = new Scanner(str);
            temp.add(new TransitionInfo(scanner.nextInt(), scanner.next(), scanner.nextBoolean()));
            scanner.close();
            if (temp.get(temp.size() - 1).type > maxType) maxType = temp.get(temp.size() - 1).type;
        }
        for (int i = 1; i <= maxType; i++) { this.infos.add(null); }
        for (TransitionInfo info : temp) {
            this.infos.remove(info.type);
            this.infos.add(info.type, info);
        }
    }

    public boolean getIgnore(int type) throws IOException {
        exist(type);
        if (type >= this.infos.size() || this.infos.get(type) == null) return true;
        return this.infos.get(type).ignore;
    }

    public String getDescription(int type) throws IOException {
        exist(type);
        if (type >= this.infos.size() || this.infos.get(type) == null) return "ignored";
        return this.infos.get(type).description;
    }

    public void exist(int type) throws IOException {
        if (!SettingsHandler.debugMode) return;
        if (type >= this.infos.size() || this.infos.get(type) == null) {
            for (int i = this.infos.size(); i <= type; i++) {
                this.infos.add(null);
            }
            System.out.println("Transition type - " + type + " - info NOT FOUND!");
            while (true) {
                System.out.print("Ignore this transition? (Y/N): ");
                Scanner scanner = new Scanner(System.in);
                String temp = scanner.next();
                scanner.nextLine();
                if (temp.substring(0, 1).equalsIgnoreCase("N")) {
                    System.out.print("Enter ONE word description: ");
                    this.infos.remove(type);
                    this.infos.add(type, new TransitionInfo(type, scanner.next(), false));
                    scanner.nextLine();
                    break;
                } else if (temp.substring(0,1).equalsIgnoreCase("Y")) {
                    this.infos.remove(type);
                    this.infos.add(type, new TransitionInfo(type, "ignored", true));
                    break;
                }
            }
            SettingsHandler.listWriter(this.infos.get(type).toString(), SettingsHandler.CAT_LIST_TRANSITION_INFO, new File(SettingsHandler.FILE_SETTINGS));
        }
    }

}

class TransitionInfo {

    public int type;
    public String description;
    public boolean ignore;

    public TransitionInfo (int type, String description, boolean ignore) {
        this.type = type;
        this.description = description;
        this.ignore = ignore;
    }

    public String toString() { return "" + this.type + " " + this.description + " " + this.ignore; }

}

class NameHandler {

    ArrayList<NameInfo> infos;

    public NameHandler()  throws FileNotFoundException {
        Scanner scanner;
        this.infos = new ArrayList<>();
        for (String str : SettingsHandler.listReader(SettingsHandler.CAT_LIST_NAME_INFO)) {
            scanner = new Scanner(str);
            infos.add(new NameInfo(scanner.next(), scanner.next()));
            scanner.close();
        }
    }

    public String getStyle(String name) throws IOException {
        for (NameInfo info : infos) {
            if (info.name.equals(name)) return info.style;
        }
        System.out.println("Actor Name - " + name + " - Info Not Found!");
        String style = SettingsHandler.reader(SettingsHandler.CAT_DEFAULT_STYLE);
        while (true) {
            System.out.print("Use default style - " + style + " - for this actor name? (Y/N): ");
            Scanner scanner = new Scanner(System.in);
            String temp = scanner.next();
            scanner.nextLine();
            if (temp.substring(0,1).equalsIgnoreCase("N")) {
                System.out.print("Enter the custom style (ONE_WORD) for this actor name: ");
                style = scanner.next();
                this.infos.add(new NameInfo(name, style));
                scanner.nextLine();
                break;
            } else if (temp.substring(0,1).equalsIgnoreCase("Y")) {
                this.infos.add(new NameInfo(name, style));
                break;
            }
        }
        SettingsHandler.listWriter(name + " " + style, SettingsHandler.CAT_LIST_NAME_INFO, new File(SettingsHandler.FILE_SETTINGS));
        System.out.println("DONE");
        return style;
    }

}

class NameInfo {

    String name, style;

    public NameInfo(String name, String style) {
        this.name = name;
        this.style = style;
    }

}
