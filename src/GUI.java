import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class GUI {

    public static final String TITLE = "轴姬 ver3.0";

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
        out.setText("TEST\n");

        JFileChooser videoChooser = new JFileChooser();
        JFileChooser jsonChooser = new JFileChooser();
        videoChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "\\Downloads"));
        jsonChooser.setCurrentDirectory(new File(System.getProperty("user.home") + "\\Downloads"));
        JPanel panel = new JPanel();
        JButton videoSelect = new JButton("MP4 Video");
        JButton jsonSelect = new JButton("JSON Script");
        JButton run = new JButton("Start");
        videoSelect.addActionListener(e -> {
            if (videoChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                video = videoChooser.getSelectedFile();
                videoSelect.setText(video.getName());
            }
        });
        jsonSelect.addActionListener(e -> {
            if (jsonChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                json = jsonChooser.getSelectedFile();
                jsonSelect.setText(json.getName());
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

        panel.add(videoSelect);
        panel.add(jsonSelect);
        panel.add(run);

        frame.getContentPane().add(BorderLayout.SOUTH, outputPane);
        frame.getContentPane().add(BorderLayout.CENTER, panel);
        frame.setVisible(true);
    }

    public static void start() throws IOException, InterruptedException {
        out.setText("START\n");
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
