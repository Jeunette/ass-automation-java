import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TestOpenCV {

    public static final String videoPath = "C:\\Users\\luns7\\Downloads\\Video\\event_32-2.mp4";
    public static final String jsonPath = "C:\\Users\\luns7\\Downloads\\Video\\event_32_02.json";

    public static void main(String[] args) throws IOException, InterruptedException {
        RunOpenCV.loadLibrary();
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
        System.out.println("TASK-VIDEO = " + video.getName());
        Logger.out.println("TASK-VIDEO = " + video.getName() + " **TESTING**");
        SettingsHandler.setReferencePath(videoPath);
        SettingsHandler.debugMode = false;
        File data = new File(video.getAbsolutePath() + ".data.txt");
        File ass = new File(video.getAbsolutePath() + ".ass");
        try {
            System.out.println("Reading from save " + data.getName() + "...");
            ImageSystem system = new ImageSystem(data);
            JsonReader reader = new JsonReader(json);
            system.saveResults(new File("test.results.temp.txt"));
            system.saveFormattedResults(new File("test.formatted_results.temp.txt"));
            ASSWriter.printSections(reader.snippets.getEventSections());
            ASSWriter.printSections(ASSWriter.getEventSectionsOpenCV(system, video.getAbsolutePath()));
            ASSWriter.printSectionsSimple(reader.snippets.getEventSections());
            ASSWriter.printSectionsSimple(ASSWriter.getEventSectionsOpenCV(system, video.getAbsolutePath()));
            //noinspection ResultOfMethodCallIgnored
            ass.delete();
            //noinspection ResultOfMethodCallIgnored
            ass.createNewFile();
            ASSWriter.writeOpenCV(system, reader.snippets, ass, video.getAbsolutePath());
        } catch (FileNotFoundException | NumberFormatException e) {
            System.out.println("Save " + data.getName() + " not found.");
            System.out.println("Reading from video " + video.getName() + "...");
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
            system.saveResults(new File("test.results.temp.txt"));
            system.saveFormattedResults(new File("test.formatted_results.temp.txt"));
            ASSWriter.printSections(reader.snippets.getEventSections());
            ASSWriter.printSections(ASSWriter.getEventSectionsOpenCV(system, video.getAbsolutePath()));
            ASSWriter.printSectionsSimple(reader.snippets.getEventSections());
            ASSWriter.printSectionsSimple(ASSWriter.getEventSectionsOpenCV(system, video.getAbsolutePath()));
            //noinspection ResultOfMethodCallIgnored
            ass.delete();
            //noinspection ResultOfMethodCallIgnored
            ass.createNewFile();
            ASSWriter.writeOpenCV(system, reader.snippets, ass, video.getAbsolutePath());
        }
    }

}
