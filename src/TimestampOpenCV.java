import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class TimestampOpenCV {

    public double fps;

    public TimestampOpenCV(String videoPath) {
        VideoCapture capture = new VideoCapture(videoPath, Videoio.CAP_FFMPEG);
        this.fps = capture.get(Videoio.CAP_PROP_FPS);
    }

    public double get(int frame) {
        return (double) frame / fps;
    }


}
