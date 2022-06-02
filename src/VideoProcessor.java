import org.opencv.core.Mat;


public interface VideoProcessor {
    Mat process(Mat inputImage);

    void setImageThreshold(double imageThreshold);

    void setHistory(int history);

}
