import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class Logger {

    public static final String logName = "ass-automation-java.log";

    public static PrintStream out;

    public static void startLogger() throws IOException {
        File log = new File(logName);
        FileOutputStream stream;
        if (log.createNewFile()) {
            stream = new FileOutputStream(log);
            out = new PrintStream(stream);
            out.println(ImageSystem.VALIDATION_STR);
        } else {
            stream = new FileOutputStream(log, true);
            out = new PrintStream(stream);
        }
        out.println("----------------------------------------------------------------");
    }

}
