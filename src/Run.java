import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class Run {

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

    public static void main(String[] args) throws IOException, InterruptedException {
        currentSystem = System.getProperty("os.name").toLowerCase();
        shell = isWindows() ? CMD.clone() : isMac() ? TERM.clone() : BASH.clone();
        currentSystem = isWindows() ? WINDOWS : isMac() ? MAC_OS : LINUX;
        String videoPath, jsonPath;
        if (args.length != 2 || args[0].length() == 0 || args[1].length() == 0) {
            System.out.println("Arguments invalid / not detected. ");
            System.out.println("Entering manual input mode... ");
            Scanner scanner = new Scanner(System.in);
            System.out.println("Path of video: ");
            videoPath = scanner.nextLine();
            System.out.println("Path of .json file: ");
            jsonPath = scanner.nextLine();
        } else {
            videoPath = args[0];
            jsonPath = args[1];
        }
        if (videoPath.charAt(videoPath.length() - 1) == ' ') videoPath = videoPath.substring(0, videoPath.length() - 1);
        if (jsonPath.charAt(jsonPath.length() - 1) == ' ') jsonPath = jsonPath.substring(0, jsonPath.length() - 1);
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
        SettingsHandler.setReferencePath(videoPath);
        System.out.println("TASK: " + video.getName());
        File dir = new File(video.getAbsolutePath() + ".temp/");
        if (!dir.isDirectory()) //noinspection ResultOfMethodCallIgnored
            dir.mkdir();
        File ref = new File(dir.getAbsolutePath() + "/" + video.getName() + ".txt");
        File frames = new File(dir.getAbsolutePath() + "/frames");
        File data = new File(dir.getAbsolutePath() + "/" + video.getName() + ".data.txt");
        if (!dir.isDirectory() || !ref.isFile()) {
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
            if (frames.isDirectory()) {
                removeDir(frames.getAbsolutePath());
            }
            //noinspection ResultOfMethodCallIgnored
            ass.delete();
            //noinspection ResultOfMethodCallIgnored
            ass.createNewFile();
            JsonReader reader = new JsonReader(json);
            while (!ref.isFile()) {
                //noinspection BusyWait
                Thread.sleep(2000);
            }
            ASSWriter.writeFfprobe(system, reader.snippets, ref, ass);
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
            removeDir(frames.getAbsolutePath());
            JsonReader reader = new JsonReader(json);
            while (!ref.isFile()) {
                //noinspection BusyWait
                Thread.sleep(2000);
            }
            //noinspection ResultOfMethodCallIgnored
            ass.delete();
            //noinspection ResultOfMethodCallIgnored
            ass.createNewFile();
            ASSWriter.writeFfprobe(system, reader.snippets, ref, ass);
        }
        while (true) {
            System.out.print("Clean save files? (Y/N): ");
            Scanner end = new Scanner(System.in);
            String temp = end.nextLine();
            if (temp.substring(0, 1).equalsIgnoreCase("N")) {
                break;
            } else if (temp.substring(0, 1).equalsIgnoreCase("Y")) {
                removeDir(video.getAbsolutePath() + ".temp");
                break;
            }
        }
        System.exit(0);
    }

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
