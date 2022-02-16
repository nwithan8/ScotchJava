import com.easypost.scotch.VCR;
import com.easypost.scotch.clients.httpurlconnection.VCRHttpURLConnection;
import com.easypost.scotch.clients.httpurlconnection.VCRHttpsURLConnection;
import com.easypost.scotch.clients.httpurlconnection.VCRURL;
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;

public class TestHttpUrlConnection {

    private static final String cassettePath = "/Users/nharris/code/scotch_java/cassettes/http_url_connection.json";

    private void testHttpUrlConnectionRequest(VCR vcr) throws IOException {
        VCRURL url = new VCRURL("https://m.facebook.com", vcr);

        VCRHttpURLConnection conn = url.openConnection();

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

    @Test
    public void testCast() throws IOException {
        VCR vcr = TestTools.getRecordingVCR(cassettePath);
        VCRURL url = new VCRURL("https://m.facebook.com", vcr);

        VCRHttpURLConnection conn = url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("This", "That");
        conn.setRequestProperty("this2", "that2");
        HttpURLConnection connCast = (HttpURLConnection) conn;

        System.out.println(connCast.getContent());
        System.out.println(connCast.getInputStream());

        VCRHttpsURLConnection sConn = url.openConnectionSecure();
        sConn.setRequestMethod("DELETE");
        sConn.setRequestProperty("This", "That");
        sConn.setRequestProperty("this2", "that2");
        HttpsURLConnection sConnCast = (HttpsURLConnection) sConn;

        System.out.println(sConnCast.getURL());
        System.out.println(sConnCast.getContent());
        System.out.println(sConnCast.getInputStream());

    }
}
