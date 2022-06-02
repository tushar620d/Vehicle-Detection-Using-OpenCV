import java.io.IOException;

import org.opencv.core.Core;

import jxl.write.WriteException;
import tw.edu.sju.ee.commons.nativeutils.NativeUtils;

public class VehicleDetection {
	static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME); 
        } catch (UnsatisfiedLinkError e) {
            try {
                NativeUtils.loadLibraryFromJar("opencv_java310");
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    public static void main(String[] args) throws IOException, WriteException, InterruptedException {
        GraphicalUserInterface graphicalUserInterface = new GraphicalUserInterface();
		graphicalUserInterface.init();
    }
}
