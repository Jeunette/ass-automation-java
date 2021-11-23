import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.prefs.Preferences;

public class GUI {

    public static final String TITLE = "轴姬 ver3.0";
    private static final String LAST_USED_FOLDER_VIDEO = "";
    private static final String LAST_USED_FOLDER_JSON = "";

    public static File video, json;
    public static JTextArea out;
    public static JFrame frame;

    public static void main(String[] args) {
        video = null;
        json = null;
        out = new JTextArea(20, 16);
        frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 400);

        JScrollPane outputPane = new JScrollPane(out);
        PrintStream printStream = new PrintStream(new CustomOutputStream(out));
        System.setOut(printStream);
        System.setOut(printStream);

        Preferences videoPrefs = Preferences.userRoot().node(GUI.class.getName());
        Preferences jsonPrefs = Preferences.userRoot().node(GUI.class.getName());
        JFileChooser videoChooser = new JFileChooser(videoPrefs.get(LAST_USED_FOLDER_VIDEO, new File(".").getAbsolutePath()));
        JFileChooser jsonChooser = new JFileChooser(jsonPrefs.get(LAST_USED_FOLDER_JSON, new File(".").getAbsolutePath()));
        JPanel panel = new JPanel();
        JButton videoSelect = new JButton("MP4 Video");
        JButton jsonSelect = new JButton("JSON Script");
        JButton run = new JButton("Start");
        videoSelect.addActionListener(e -> {
            if (videoChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                video = videoChooser.getSelectedFile();
                videoSelect.setText(video.getName());
                videoPrefs.put(LAST_USED_FOLDER_VIDEO, videoChooser.getSelectedFile().getParent());
            }
        });
        jsonSelect.addActionListener(e -> {
            if (jsonChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                json = jsonChooser.getSelectedFile();
                jsonSelect.setText(json.getName());
                jsonPrefs.put(LAST_USED_FOLDER_JSON, jsonChooser.getSelectedFile().getParent());
            }
        });
        run.addActionListener(e -> {
            if (video != null && json != null) {
                try {
                    start();
                } catch (IOException | InterruptedException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        panel.setLayout(new GridLayout(3, 1));
        panel.add(videoSelect);
        panel.add(jsonSelect);
        panel.add(run);

        frame.getContentPane().add(BorderLayout.CENTER, outputPane);
        frame.getContentPane().add(BorderLayout.EAST, panel);
        frame.setVisible(true);
    }

    public static void start() throws IOException, InterruptedException {
        out.setText("");
        RunOpenCV.main(new String[]{video.getAbsolutePath(), json.getAbsolutePath()});
    }

}

class CustomOutputStream extends OutputStream {

    public JTextArea textArea;

    public CustomOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    public void write(int b) {
        // redirects data to the text area
        textArea.append(String.valueOf((char) b));
        // scrolls the text area to the end of data
        textArea.setCaretPosition(textArea.getDocument().getLength());
        // keeps the textArea up to date
        textArea.update(textArea.getGraphics());
    }
}
