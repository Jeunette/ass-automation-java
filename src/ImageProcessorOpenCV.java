import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;

public class ImageProcessorOpenCV {

    public static void processImage(File video, LinkedList<ImageData> list) throws FileNotFoundException, InterruptedException {

        System.out.println("Analysing images...");

        ColorAnalyzerOpenCV analyzer = new ColorAnalyzerOpenCV();

        VideoCapture capture = new VideoCapture(video.getAbsolutePath(), Videoio.CAP_FFMPEG);
        double temp = capture.get(Videoio.CAP_PROP_FRAME_COUNT);

        VideoCaptureThreadOpenCV[] threads = new VideoCaptureThreadOpenCV[2];
        ArrayList<ArrayList<ImageData>> data = new ArrayList<>();

        for (int i = 0; i < threads.length; i++) {
            data.add(new ArrayList<>());
            threads[i] = new VideoCaptureThreadOpenCV(analyzer, data.get(i), video, (int) (temp / threads.length * i), (int) (temp / threads.length * (i + 1)));
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].t.join();
            list.addAll(data.get(i));
        }

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

}