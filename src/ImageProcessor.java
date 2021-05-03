import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;

public class ImageProcessor extends Thread {

    public Thread t;
    public Thread p;

    public ColorAnalyzer analyzer;
    public LinkedList<ImageData> list;
    public String file;

    public ImageProcessor(ColorAnalyzer analyzer, LinkedList<ImageData> list, String file, ImageProcessor previous) {
        this.analyzer = analyzer;
        this.list = list;
        this.file = file;
        if (previous != null) {
            this.p = previous.t;
        } else {
            this.p = null;
        }
    }

    public void run() {
        BufferedImage buffered;
        try {
            buffered = ImageIO.read(new FileInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException("Image " + file + " Not Found!");
        }
        ImageData temp = analyzer.analyse(buffered, file.substring(file.length() - 12));
        if (p != null) {
            try {
                p.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException("Multithreading Error!");
            }
        }
        list.add(temp);
    }

    public void start() {
        if (this.t == null) {
            this.t = new Thread(this, file.substring(file.length() - 12));
            t.start();
        }
    }

}
