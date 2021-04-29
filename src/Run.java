import java.io.*;
import java.util.Objects;
import java.util.Scanner;

public class Run {

    public static void main (String[] args) throws IOException, InterruptedException {
        String videoPath, jsonPath;
        if (args.length != 2 || args[0].length() == 0 || args[1].length() ==0) {
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
        File dir = new File(mp4.getAbsolutePath() + ".temp\\");
        if (!dir.isDirectory()) //noinspection ResultOfMethodCallIgnored
            dir.mkdir();
        File ref = new File(dir.getAbsolutePath() + "\\" + mp4.getName() + ".txt");
        File frames = new File(dir.getAbsolutePath() + "\\frames");
        File data = new File(dir.getAbsolutePath() + "\\" + mp4.getName() + ".data.txt");
        if (!dir.isDirectory() || !ref.isFile()) {
            if (!ref.isFile()) {
                try {
                    String command = "get_timestamp-frame-info.bat \"" + mp4.getAbsolutePath() + "\"";
                    Runtime.getRuntime().exec("cmd /C start cmd.exe /C " + command);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!data.isFile() && (!frames.isDirectory() || Objects.requireNonNull(frames.list()).length == 0)) {
                //noinspection ResultOfMethodCallIgnored
                frames.mkdir();
                try {
                    String command = "video2frames.bat \"" + mp4.getAbsolutePath() + "\"";
                    Runtime.getRuntime().exec("cmd /C start cmd.exe /C " + command);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        File ass = new File(mp4.getAbsolutePath() + ".ass");
        //noinspection ResultOfMethodCallIgnored
        ass.delete();
        //noinspection ResultOfMethodCallIgnored
        ass.createNewFile();
        try {
            System.out.println("Reading from save file...");
            ImageSystem system = new ImageSystem(data);
            if (frames.isDirectory()) {
                try {
                    String command = "rmdir /s /q " + frames.getAbsolutePath();
                    Runtime.getRuntime().exec("cmd /C start cmd.exe /C " + command);
                } catch (IOException ex) { ex.printStackTrace(); }
            }
            JsonReader reader = new JsonReader(json);
            while (!ref.isFile()) {
                Thread.sleep(2000);
            }
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
            try {
                String command = "rmdir /s /q " + frames.getAbsolutePath();
                Runtime.getRuntime().exec("cmd /C start cmd.exe /C " + command);
            } catch (IOException ex) { ex.printStackTrace(); }
            JsonReader reader = new JsonReader(json);
            while (!ref.isFile()) {
                Thread.sleep(2000);
            }
            ASSWriter.write(system, reader.snippets, ref, ass);
        }
        while (true) {
            System.out.print("Clean save files? (Y/N): ");
            Scanner end = new Scanner(System.in);
            String temp = end.nextLine();
            if (temp.substring(0,1).equalsIgnoreCase("N")) {
                break;
            } else if (temp.substring(0,1).equalsIgnoreCase("Y")) {
                try {
                    String command = "rmdir /s /q " + mp4.getAbsolutePath() + ".temp" ;
                    Runtime.getRuntime().exec("cmd /C start cmd.exe /C " + command);
                } catch (IOException e) { e.printStackTrace(); }
                break;
            }
        }
    }

}
