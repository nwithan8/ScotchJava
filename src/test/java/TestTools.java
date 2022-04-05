import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.CassetteOrder;
import com.easypost.easyvcr.VCR;

public class TestTools {
    public static VCR getVCR(String path) {
        Cassette cassette = new Cassette("/path/to/cassettes", "", new CassetteOrder.Alphabetical());
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
