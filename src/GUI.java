import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.prefs.Preferences;

public class GUI {

    public static final String TITLE = "轴姬 3.0";
    private static final String LAST_USED_FOLDER_VIDEO = "";
    private static final String LAST_USED_FOLDER_JSON = "";

    public static File video, json;
    public static JTextArea out;
    public static JFrame frame;

    public static void main(String[] args) throws UnsupportedEncodingException {
        video = null;
        json = null;
        out = new JTextArea(20, 16);
        frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 400);

        JScrollPane outputPane = new JScrollPane(out);
        PrintStream printStream = new PrintStream(new CustomOutputStream(out), true, StandardCharsets.UTF_8);
        System.setOut(printStream);
        System.setOut(printStream);

        Preferences videoPrefs = Preferences.userRoot().node(GUI.class.getName());
        Preferences jsonPrefs = Preferences.userRoot().node(GUI.class.getName());
        FileNameExtensionFilter mp4Filter = new FileNameExtensionFilter("MP4 Files", "mp4");
        FileNameExtensionFilter jsonFilter = new FileNameExtensionFilter("JSON Files", "json");
        JFileChooser videoChooser = new JFileChooser(videoPrefs.get(LAST_USED_FOLDER_VIDEO, new File(".").getAbsolutePath()));
        JFileChooser jsonChooser = new JFileChooser(jsonPrefs.get(LAST_USED_FOLDER_JSON, new File(".").getAbsolutePath()));
        videoChooser.addChoosableFileFilter(mp4Filter);
        videoChooser.setFileFilter(mp4Filter);
        videoChooser.setMultiSelectionEnabled(false);
        jsonChooser.addChoosableFileFilter(jsonFilter);
        jsonChooser.setAcceptAllFileFilterUsed(false);
        jsonChooser.setMultiSelectionEnabled(false);
        JPanel panel = new JPanel();
        JButton videoSelect = new JButton("Select Video File");
        JButton jsonSelect = new JButton("Select JSON File");
        JButton run = new JButton("Start");
        videoSelect.addActionListener(e -> {
            if (videoChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                video = videoChooser.getSelectedFile();
                videoSelect.setText(video.getName());
                videoPrefs.put(LAST_USED_FOLDER_VIDEO, videoChooser.getSelectedFile().getParent());
                if (video != null && json != null) {
                    panel.setLayout(new GridLayout(3, 1));
                    panel.add(run, 1);
                }
            }
        });
        jsonSelect.addActionListener(e -> {
            if (jsonChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                json = jsonChooser.getSelectedFile();
                jsonSelect.setText(json.getName());
                jsonPrefs.put(LAST_USED_FOLDER_JSON, jsonChooser.getSelectedFile().getParent());
                if (video != null && json != null) {
                    panel.setLayout(new GridLayout(3, 1));
                    panel.add(run, 1);
                }
            }
        });
        run.addActionListener(e -> {
            if (video != null && json != null) {
                try {
                    start();
                    video = null;
                    json = null;
                    panel.remove(run);
                    panel.setLayout(new GridLayout(2, 1));
                    videoSelect.setText("Select Video File");
                    jsonSelect.setText("Select JSON File");
                } catch (IOException | InterruptedException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        panel.setLayout(new GridLayout(2, 1));
        panel.add(videoSelect);
        panel.add(jsonSelect);

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
        textArea.append(String.valueOf((char) b));
        textArea.setCaretPosition(textArea.getDocument().getLength());
        textArea.update(textArea.getGraphics());
    }

    public void write(byte[] b) throws NullPointerException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws NullPointerException, IndexOutOfBoundsException {
        if (off < 0 || len < 0 || off + len > b.length)
            throw new ArrayIndexOutOfBoundsException();
        byte[] temp = new byte[len + off];
        System.arraycopy(b, off, temp, 0, len);
        textArea.append(new String(temp, StandardCharsets.UTF_8));
        textArea.setCaretPosition(textArea.getDocument().getLength());
        textArea.update(textArea.getGraphics());
    }

}
