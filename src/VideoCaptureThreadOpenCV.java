import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.ArrayList;

public class VideoCaptureThreadOpenCV extends Thread {

    public Thread t;

    public ColorAnalyzerOpenCV analyzer;
    public ArrayList<ImageData> list;
    public File video;
    public int start, end;

    public VideoCaptureThreadOpenCV(ColorAnalyzerOpenCV analyzer, ArrayList<ImageData> list, File video, int start, int end) {
        this.analyzer = analyzer;
        this.list = list;
        this.video = video;
        this.start = start;
        this.end = end;
    }

    public void run() {
        VideoCapture capture = new VideoCapture(video.getAbsolutePath(), Videoio.CAP_FFMPEG);
        Mat image = new Mat();
        int index = start;
        if (capture.isOpened()) {
            capture.set(Videoio.CAP_PROP_POS_FRAMES, index - 1);
            while (capture.read(image) && index < end) {
                if (start == 0 && (index % 1000 == 0 || index == end - 1)) System.out.println(video.getName() + " "
                        + String.format("%.2f", ((double) (index - start)) / (double) (end - 1 - start) * 100.0) + "%");
                ImageData temp = analyzer.analyse(matToBufferedImage(image), "" + capture.get(Videoio.CAP_PROP_POS_FRAMES));
                list.add(temp);
                index++;
                System.out.println(index);
            }
        } else {
            throw new RuntimeException("Error! Cannot Open Video!");
        }
        capture.release();
    }

    public static BufferedImage matToBufferedImage(Mat frame) {
        int type = 0;
        if (frame.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (frame.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();
        frame.get(0, 0, data);
        return image;
    }

    public void start() {
        if (this.t == null) {
            this.t = new Thread(this, video.getName() + " " + String.format("%08d", start) + " - " + String.format("%08d", end));
            t.start();
        }
    }

}
