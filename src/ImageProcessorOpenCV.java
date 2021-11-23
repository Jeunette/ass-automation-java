import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;

public class ImageProcessorOpenCV {

    public static void processImage(File video, LinkedList<ImageData> list) throws FileNotFoundException, InterruptedException {

        ColorAnalyzerOpenCV analyzer = new ColorAnalyzerOpenCV();

        VideoCapture capture = new VideoCapture(video.getAbsolutePath(), Videoio.CAP_ANY);
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

}