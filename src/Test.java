import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Test {

    public static final String videoPath = "C:\\Users\\luns7\\Downloads\\Video\\RIN_1_x264.mp4";
    public static final String jsonPath = "C:\\Users\\luns7\\Downloads\\Video\\022001_rin01.json";

    public static void main(String[] args) throws IOException, InterruptedException {
        currentSystem = System.getProperty("os.name").toLowerCase();
        shell = isWindows() ? CMD.clone() : isMac() ? TERM.clone() : BASH.clone();
        currentSystem = isWindows() ? WINDOWS : isMac() ? MAC_OS : LINUX;
        File video = new File(videoPath);
        if (!video.isFile()) {
            System.out.println(videoPath + " Not Found.");
            System.exit(1);
        }
        File json = new File(jsonPath);
        if (!json.isFile()) {
            System.out.println(jsonPath + " Not Found.");
            System.exit(1);
        }
        System.out.println("TASK: " + video.getName());
        File dir = new File(video.getAbsolutePath() + ".temp/");
        if (!dir.isDirectory()) //noinspection ResultOfMethodCallIgnored
            dir.mkdir();
        File ref = new File(dir.getAbsolutePath() + "/" + video.getName() + ".txt");
        File frames = new File(dir.getAbsolutePath() + "/frames");
        File data = new File(dir.getAbsolutePath() + "/" + video.getName() + ".data.txt");
        if (!dir.isDirectory() || !ref.isFile() || !frames.isDirectory() || Objects.requireNonNull(frames.list()).length == 0) {
            if (!ref.isFile()) {
                getTimestamp(video.getAbsolutePath());
            }
            if (!data.isFile() && (!frames.isDirectory() || Objects.requireNonNull(frames.list()).length == 0)) {
                //noinspection ResultOfMethodCallIgnored
                frames.mkdir();
                video2frame(video.getAbsolutePath());
            }
        }
        File ass = new File(video.getAbsolutePath() + ".ass");
        try {
            System.out.println("Reading from save file...");
            ImageSystem system = new ImageSystem(data);
            JsonReader reader = new JsonReader(json);
            while (!ref.isFile()) {
                //noinspection BusyWait
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
            ASSWriter.writeFfprobe(system, reader.snippets, ref, ass);
        } catch (FileNotFoundException | NumberFormatException e) {
            System.out.println("Save file not found.");
            System.out.println("Reading from frames directory...");
            ImageSystem system = new ImageSystem(frames);
            if (data.isFile()) {
                //noinspection ResultOfMethodCallIgnored
                data.renameTo(new File(data.getName() + System.currentTimeMillis() + ".bak"));
            }
            //noinspection ResultOfMethodCallIgnored
            data.delete();
            //noinspection ResultOfMethodCallIgnored
            data.createNewFile();
            system.save(data);
            JsonReader reader = new JsonReader(json);
            system.saveResults(new File("test.results.temp.txt"));
            system.saveFormattedResults(new File("test.formatted_results.temp.txt"));
            while (!ref.isFile()) {
                //noinspection BusyWait
                Thread.sleep(2000);
            }
            ASSWriter.printSections(reader.snippets.getEventSections());
            ASSWriter.printSections(ASSWriter.getEventSections(system, ref));
            ASSWriter.printSectionsSimple(reader.snippets.getEventSections());
            ASSWriter.printSectionsSimple(ASSWriter.getEventSections(system, ref));
            //noinspection ResultOfMethodCallIgnored
            ass.delete();
            //noinspection ResultOfMethodCallIgnored
            ass.createNewFile();
            ASSWriter.writeFfprobe(system, reader.snippets, ref, ass);
        }
    }

    private static final String LINUX = "Linux";
    private static final String[] BASH = new String[]{""};
    private static final String[] BASH_GET_TIMESTAMP = new String[]{"./get_timestamp-frame-info.sh", null};
    private static final String[] BASH_VIDEO_TO_FRAME = new String[]{"./video2frames.sh", null};

    private static final String MAC_OS = "macOS";
    private static final String[] TERM = new String[]{""};
    private static final String[] TERM_GET_TIMESTAMP = new String[]{"./get_timestamp-frame-info.sh", null};
    private static final String[] TERM_VIDEO_TO_FRAME = new String[]{"./video2frames.sh", null};

    private static final String WINDOWS = "Windows";
    private static final String[] CMD = new String[]{"cmd", "/C", "start", "cmd.exe", "/C", ""};
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
