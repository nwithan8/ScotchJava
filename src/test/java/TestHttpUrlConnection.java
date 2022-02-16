import com.easypost.scotch.VCR;
import com.easypost.scotch.clients.httpurlconnection.VCRHttpUrlConnection;
import com.easypost.scotch.clients.httpurlconnection.VCRURL;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;

public class TestHttpUrlConnection {

    private static final String cassettePath = "/Users/nharris/code/scotch_java/cassettes/http_url_connection.json";

    private void testHttpUrlConnectionRequest(VCR vcr) throws IOException {
        VCRURL url = new VCRURL("https://m.facebook.com", vcr);

        VCRHttpUrlConnection conn = url.openConnection();

        conn.setRequestMethod("DELETE");

        System.out.println(conn.getHeaderFields());
        System.out.println(conn.getResponseCode());
        System.out.println(conn.getURL());
    }

    @Test
    public void testHttpUrlConnectionRecord() throws IOException {
        VCR vcr = TestTools.getRecordingVCR(cassettePath);
        testHttpUrlConnectionRequest(vcr);
    }

    @Test
    // can run offline
    public void testHttpUrlConnectionReplay() throws IOException {
        VCR vcr = TestTools.getReplayingVCR(cassettePath);
        testHttpUrlConnectionRequest(vcr);
    }
}
