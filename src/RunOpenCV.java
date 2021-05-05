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
            System.out.println("\033[1;97m[System]\033[0m Arguments invalid / not detected.");
            Scanner scanner = new Scanner(System.in);
            System.out.println("\033[1;97m[System]\033[0m Path of video: ");
            videoPath = scanner.nextLine();
            System.out.println("\033[1;97m[System]\033[0m Path of .json file: ");
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
        Logger.startLogger();
        System.out.println("\033[1;94mTASK-VIDEO\033[0m = \033[1;92m" + video.getName() + "\033[0m");
        Logger.out.println("TASK-VIDEO = " + video.getName());
        SettingsHandler.setReferencePath(videoPath);
        File temp = new File("temp");
        if (!temp.isDirectory()) //noinspection ResultOfMethodCallIgnored
            temp.mkdir();
        File data = new File(temp.getAbsolutePath() + "/" + video.getName() + ".data.temp");
        File ass = new File(video.getAbsolutePath() + ".ass");
        try {
            if (data.isFile()) {
                while (true) {
                    System.out.print("\033[1;97m[System]\033[0m Load save data? (Y/N): ");
                    Scanner scanner = new Scanner(System.in);
                    String user = scanner.nextLine();
                    if (user.substring(0, 1).equalsIgnoreCase("N")) {
                        //noinspection ResultOfMethodCallIgnored
                        data.delete();
                        throw new FileNotFoundException("USER");
                    } else if (user.substring(0, 1).equalsIgnoreCase("Y")) {
                        break;
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
                System.out.println("\033[1;97m[System]\033[0m Previous ass file moved to \033[1;92m" + temp.getAbsolutePath() + "\033[0m");
            }
            //noinspection ResultOfMethodCallIgnored
            ass.createNewFile();
            ASSWriter.writeOpenCV(system, reader.snippets, ass, video.getAbsolutePath());
        } catch (FileNotFoundException | NumberFormatException e) {
            if (!e.getMessage().equals("USER") && !e.getMessage().equals("FIRST"))
                System.out.println("\033[1;97m[System]\033[0m Loading failed.");
            try {
                System.out.println("\033[1;97m[System]\033[0m Reading from video \033[1;92m" + video.getName() + "\033[0m");
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
                    System.out.println("\033[1;97m[System]\033[0m Previous ass file moved to \033[1;92m" + temp.getAbsolutePath() + "\033[0m");
                }
                //noinspection ResultOfMethodCallIgnored
                ass.createNewFile();
                ASSWriter.writeOpenCV(system, reader.snippets, ass, video.getAbsolutePath());
            } catch (Exception ex) {
                //noinspection ResultOfMethodCallIgnored
                data.delete();
                //noinspection ResultOfMethodCallIgnored
                ass.delete();
                System.out.println("\033[1;30m\033[0;101mFATAL_ERROR\033[0m: System cannot procecced. Check your settings & reference files.");
                Logger.out.println("[FATAL_ERROR] System cannot proceed. Check your settings & reference files.");
                System.exit(3);
            }
        } catch (Exception e) {
            //noinspection ResultOfMethodCallIgnored
            data.delete();
            //noinspection ResultOfMethodCallIgnored
            ass.delete();
            System.out.println("\033[1;30m\033[0;101mFATAL_ERROR\033[0m: System cannot procecced. Check your settings & reference files.");
            Logger.out.println("[FATAL_ERROR] System cannot proceed. Check your settings & reference files.");
            System.exit(3);
        }
        FileFilter tempFilter = file -> file.isFile() && file.getName().toLowerCase().endsWith(".temp");
        System.out.println("\033[1;97m[System]\033[0m Instructions appended to \033[1;92m" + Logger.logName + "\033[0m.");
        while (getDirectorySize(temp, tempFilter) >= 200000000) {
            File[] files = temp.listFiles(tempFilter);
            assert files != null;
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
            //noinspection ResultOfMethodCallIgnored
            files[0].delete();
        }
        System.exit(0);
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
