import com.easypost.scotch.VCR;
import com.easypost.scotch.cassettes.Cassette;

public class TestTools {
    public static VCR getVCR(String path) {
        Cassette cassette = new Cassette(path);
        VCR vcr = new VCR();
        vcr.insert(cassette);
        return vcr;
    }

    public static VCR getRecordingVCR(String path) {
        VCR vcr = getVCR(path);
        vcr.record();
        return vcr;
    }

    public static VCR getReplayingVCR(String path) {
        VCR vcr = getVCR(path);
        vcr.replay();
        return vcr;
    }
}
