import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

public class Test {

    public static final String videoPath = "C:\\Users\\luns7\\Downloads\\Video\\[SHANA]event_21-8.mp4";
    public static final String jsonPath = "C:\\Users\\luns7\\Downloads\\Video\\event_21_08.json";

    public static void main(String[] args) throws IOException, InterruptedException {
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
            ASSWriter.write(system, reader.snippets, ref, ass);
        }
    }

}
