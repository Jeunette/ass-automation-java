import java.io.*;
import java.util.Scanner;

public class RunOpenCV {

    public static void main(String[] args) throws IOException, InterruptedException {
        loadLibrary();
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
        System.out.println("TASK: " + video.getName());
        File data = new File(video.getAbsolutePath() + ".data.txt");
        File ass = new File(video.getAbsolutePath() + ".ass");
        try {
            System.out.println("Reading from save file...");
            ImageSystem system = new ImageSystem(data);
            JsonReader reader = new JsonReader(json);
            //noinspection ResultOfMethodCallIgnored
            ass.delete();
            //noinspection ResultOfMethodCallIgnored
            ass.createNewFile();
            ASSWriter.writeOpenCV(system, reader.snippets, ass, video.getAbsolutePath());
        } catch (FileNotFoundException | NumberFormatException e) {
            System.out.println("Save file not found.");
            System.out.println("Reading from frames directory...");
            ImageSystem system = new ImageSystem(video.getParentFile(), video);
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
            //noinspection ResultOfMethodCallIgnored
            ass.delete();
            //noinspection ResultOfMethodCallIgnored
            ass.createNewFile();
            ASSWriter.writeOpenCV(system, reader.snippets, ass, video.getAbsolutePath());
        }
        while (true) {
            System.out.print("Clean save files? (Y/N): ");
            Scanner end = new Scanner(System.in);
            String temp = end.nextLine();
            if (temp.substring(0, 1).equalsIgnoreCase("N")) {
                break;
            } else if (temp.substring(0, 1).equalsIgnoreCase("Y")) {
                //noinspection ResultOfMethodCallIgnored
                data.delete();
                break;
            }
        }
        System.exit(0);
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
