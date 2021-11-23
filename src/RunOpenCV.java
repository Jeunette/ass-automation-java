import javax.swing.*;
import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Scanner;

public class RunOpenCV {

    public static PrintStream stream;

    public static void main(String[] args) throws IOException, InterruptedException {
        loadLibrary();
        String videoPath, jsonPath;
        if (args.length != 2 || args[0].length() == 0 || args[1].length() == 0) {
            System.out.println("[System] Arguments invalid / not detected.");
            Scanner scanner = new Scanner(System.in);
            System.out.println("[System] Path of video: ");
            videoPath = scanner.nextLine();
            System.out.println("[System] Path of .json file: ");
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
            return;
        }
        File json = new File(jsonPath);
        if (!json.isFile()) {
            System.out.println(jsonPath + " Not Found.");
            return;
        }
        Logger.startLogger();
        System.out.println("TASK-VIDEO = " + video.getName());
        Logger.out.println("TASK-VIDEO = " + video.getName());
        if (!SettingsHandler.setReferencePath(videoPath)) return;
        File temp = new File("temp");
        if (!temp.isDirectory()) //noinspection ResultOfMethodCallIgnored
            temp.mkdir();
        File data = new File(temp.getAbsolutePath() + "/" + video.getName() + ".data.temp");
        File ass = new File(video.getAbsolutePath() + ".ass");
        try {
            if (data.isFile()) {
                while (true) {
                    int result = JOptionPane.showConfirmDialog(GUI.frame, "Load save data?", GUI.TITLE,
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (result == JOptionPane.YES_OPTION) {
                        break;
                    } else if (result == JOptionPane.NO_OPTION) {
                        //noinspection ResultOfMethodCallIgnored
                        data.delete();
                        throw new FileNotFoundException("USER");
                    }
                }
            } else {
                throw new FileNotFoundException("FIRST");
            }
            ImageSystem system = new ImageSystem(data);
            JsonReader reader = new JsonReader(json);
            if (ass.isFile()) {
                //noinspection ResultOfMethodCallIgnored
                ass.renameTo(new File(temp.getAbsolutePath() + "/" + ass.getName() + "." + System.currentTimeMillis() + ".BAK"));
                System.out.println("[System] Previous ass file moved to " + temp.getAbsolutePath());
            }
            //noinspection ResultOfMethodCallIgnored
            ass.createNewFile();
            ASSWriter.writeOpenCV(system, reader.snippets, ass, video.getAbsolutePath());
        } catch (FileNotFoundException | NumberFormatException e) {
            if (!e.getMessage().equals("USER") && !e.getMessage().equals("FIRST"))
                System.out.println("[System] Loading failed.");
            try {
                System.out.println("[System] Reading from video " + video.getName());
                ImageSystem system = new ImageSystem(video.getParentFile(), video);
                //noinspection ResultOfMethodCallIgnored
                data.delete();
                //noinspection ResultOfMethodCallIgnored
                data.createNewFile();
                system.save(data);
                JsonReader reader = new JsonReader(json);
                if (ass.isFile()) {
                    //noinspection ResultOfMethodCallIgnored
                    ass.renameTo(new File(temp.getAbsolutePath() + "/" + ass.getName() + "." + System.currentTimeMillis() + ".BAK"));
                    System.out.println("[System] Previous ass file moved to " + temp.getAbsolutePath());
                }
                //noinspection ResultOfMethodCallIgnored
                ass.createNewFile();
                ASSWriter.writeOpenCV(system, reader.snippets, ass, video.getAbsolutePath());
            } catch (Exception ex) {
                //noinspection ResultOfMethodCallIgnored
                data.delete();
                //noinspection ResultOfMethodCallIgnored
                ass.delete();
                System.out.println(ex.getMessage());
                System.out.println("[FATAL_ERROR] System cannot proceed. Check your settings & reference files.");
                Logger.out.println("[FATAL_ERROR] System cannot proceed. Check your settings & reference files.");
                return;
            }
        } catch (Exception e) {
            //noinspection ResultOfMethodCallIgnored
            data.delete();
            //noinspection ResultOfMethodCallIgnored
            ass.delete();
            System.out.println("[FATAL_ERROR] System cannot proceed. Check your settings & reference files.");
            Logger.out.println("[FATAL_ERROR] System cannot proceed. Check your settings & reference files.");
            return;
        }
        FileFilter tempFilter = file -> file.isFile() && file.getName().toLowerCase().endsWith(".temp");
        System.out.println("[System] Instructions appended to " + Logger.logName);
        while (getDirectorySize(temp, tempFilter) >= 200000000) {
            File[] files = temp.listFiles(tempFilter);
            assert files != null;
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
            //noinspection ResultOfMethodCallIgnored
            files[0].delete();
        }
    }

    public static long getDirectorySize(File dir, FileFilter filer) {
        long size = 0;
        for (File file : Objects.requireNonNull(dir.listFiles(filer))) {
            if (file.isFile())
                size += file.length();
        }
        return size;
    }

    public static void loadLibrary() {
        try {
            InputStream in = null;
            File fileOut = null;
            String osName = System.getProperty("os.name");
            if (osName.startsWith("Windows")) {
                in = new FileInputStream("lib/opencv_java452.dll");
                fileOut = File.createTempFile("lib", ".dll");
            } else if (osName.equals("Mac OS X")) {
                in = new FileInputStream("lib/libopencv_java452.dylib");
                fileOut = File.createTempFile("lib", ".dylib");
            }
            if (fileOut != null) {
                OutputStream out = new FileOutputStream(fileOut);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
                in.close();
                out.close();
                System.load(fileOut.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load opencv native library", e);
        }
    }

}
