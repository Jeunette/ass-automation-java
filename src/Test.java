import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Test {

    public static final String videoPath = "C:\\Users\\luns7\\Downloads\\Video\\[SHANA]event_21-4.mp4";
    public static final String jsonPath = "C:\\Users\\luns7\\Downloads\\Video\\event_21_04.json";

    public static void main(String[] args) throws IOException, InterruptedException {
        currentSystem = System.getProperty("os.name").toLowerCase();
        shell = isWindows() ? CMD.clone() : isMac() ? TERM.clone() : BASH.clone();
        currentSystem = isWindows() ? WINDOWS : isMac() ? MAC_OS : LINUX;
        File mp4 = new File(videoPath);
        if (!mp4.isFile()) {
            System.out.println(videoPath + " Not Found.");
            System.exit(1);
        }
        File json = new File(jsonPath);
        if (!json.isFile()) {
            System.out.println(jsonPath + " Not Found.");
            System.exit(1);
        }
        System.out.println("TASK: " + mp4.getName());
        File dir = new File(mp4.getAbsolutePath() + ".temp/");
        if (!dir.isDirectory()) //noinspection ResultOfMethodCallIgnored
            dir.mkdir();
        File ref = new File(dir.getAbsolutePath() + "/" + mp4.getName() + ".txt");
        File frames = new File(dir.getAbsolutePath() + "/frames");
        File data = new File(dir.getAbsolutePath() + "/" + mp4.getName() + ".data.txt");
        if (!dir.isDirectory() || !ref.isFile()) {
            if (!ref.isFile()) {
                getTimestamp(mp4.getAbsolutePath());
            }
            if (!data.isFile() && (!frames.isDirectory() || Objects.requireNonNull(frames.list()).length == 0)) {
                //noinspection ResultOfMethodCallIgnored
                frames.mkdir();
                video2frame(mp4.getAbsolutePath());
            }
        }
        File ass = new File(mp4.getAbsolutePath() + ".ass");
        try {
            System.out.println("Reading from save file...");
            ImageSystem system = new ImageSystem(data);
            JsonReader reader = new JsonReader(json);
            while (!ref.isFile()) {
                Thread.sleep(2000);
            }
            system.saveResults(new File("test.results.temp.txt"));
            system.saveFormattedResults(new File("test.formatted_results.temp.txt"));
            ASSWriter.printSections(reader.snippets.getEventSections());
            ASSWriter.printSections(ASSWriter.getEventSections(system, ref));
            ASSWriter.printSectionsSimple(reader.snippets.getEventSections());
            ASSWriter.printSectionsSimple(ASSWriter.getEventSections(system, ref));
            //noinspection ResultOfMethodCallIgnored
            ass.delete();
            //noinspection ResultOfMethodCallIgnored
            ass.createNewFile();
            ASSWriter.write(system, reader.snippets, ref, ass);
        } catch (FileNotFoundException | NumberFormatException e) {
            System.out.println("Save file not found.");
            System.out.println("Reading from frames directory...");
            ImageSystem system = new ImageSystem(frames);
            if (data.isFile()) {
                //noinspection ResultOfMethodCallIgnored
                data.renameTo(new File(data.getName() + System.currentTimeMillis() + ".bak" ));
            }
            //noinspection ResultOfMethodCallIgnored
            data.delete();
            //noinspection ResultOfMethodCallIgnored
            data.createNewFile();
            system.save(data);
            JsonReader reader = new JsonReader(json);
            system.saveResults(new File("test.results.temp.txt"));
            system.saveFormattedResults(new File("test.formatted_results.temp.txt"));
            ASSWriter.printSections(reader.snippets.getEventSections());
            ASSWriter.printSections(ASSWriter.getEventSections(system, ref));
            ASSWriter.printSectionsSimple(reader.snippets.getEventSections());
            ASSWriter.printSectionsSimple(ASSWriter.getEventSections(system, ref));
            while (!ref.isFile()) {
                Thread.sleep(2000);
            }
            //noinspection ResultOfMethodCallIgnored
            ass.delete();
            //noinspection ResultOfMethodCallIgnored
            ass.createNewFile();
            ASSWriter.write(system, reader.snippets, ref, ass);
        }
    }

    private static final String LINUX = "Linux";
    private static final String[] BASH = new String[]{""};
    private static final String[] BASH_REMOVE_DIR = new String[]{"rm", "-rf", ""};
    private static final String[] BASH_GET_TIMESTAMP = new String[]{"./get_timestamp-frame-info.sh", null};
    private static final String[] BASH_VIDEO_TO_FRAME = new String[]{"./video2frames.sh", null};

    private static final String MAC_OS = "macOS";
    private static final String[] TERM = new String[]{""};
    private static final String[] TERM_REMOVE_DIR = new String[]{"", null};
    private static final String[] TERM_GET_TIMESTAMP = new String[]{"./get_timestamp-frame-info.sh", null};
    private static final String[] TERM_VIDEO_TO_FRAME = new String[]{"./video2frames.sh", null};

    private static final String WINDOWS = "Windows";
    private static final String[] CMD = new String[]{"cmd", "/C", "start", "cmd.exe", "/C", ""};
    private static final String[] CMD_REMOVE_DIR = new String[]{"rmdir", "/s", "/q", null};
    private static final String[] CMD_GET_TIMESTAMP = new String[]{"get_timestamp-frame-info.bat", null};
    private static final String[] CMD_VIDEO_TO_FRAME = new String[]{"video2frames.bat", null};

    public static String currentSystem;
    public static String[] shell;

    public static boolean isWindows() {
        return (currentSystem.contains("win"));
    }

    public static boolean isMac() {
        return (currentSystem.contains("mac"));
    }

    private static void run(String[] command) {
        ArrayList<String> runtime = new ArrayList<>(Arrays.asList(shell));
        runtime.addAll(runtime.size() - 1, Arrays.asList(command));
        try {
            Runtime.getRuntime().exec(runtime.toArray(new String[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void removeDir(String dir) {
        String[] command = switch (currentSystem) {
            case LINUX -> BASH_REMOVE_DIR.clone();
            case MAC_OS -> TERM_REMOVE_DIR.clone();
            case WINDOWS -> CMD_REMOVE_DIR.clone();
            default -> throw new IllegalStateException("Unexpected value: " + currentSystem);
        };
        command[command.length - 1] = dir;
        run(command);
    }

    private static void getTimestamp(String video) {
        String[] command = switch (currentSystem) {
            case LINUX -> BASH_GET_TIMESTAMP.clone();
            case MAC_OS -> TERM_GET_TIMESTAMP.clone();
            case WINDOWS -> CMD_GET_TIMESTAMP.clone();
            default -> throw new IllegalStateException("Unexpected value: " + currentSystem);
        };
        command[command.length - 1] = video;
        run(command);
    }

    private static void video2frame(String video) {
        String[] command = switch (currentSystem) {
            case LINUX -> BASH_VIDEO_TO_FRAME.clone();
            case MAC_OS -> TERM_VIDEO_TO_FRAME.clone();
            case WINDOWS -> CMD_VIDEO_TO_FRAME.clone();
            default -> throw new IllegalStateException("Unexpected value: " + currentSystem);
        };
        command[command.length - 1] = video;
        run(command);
    }

}
