import com.easypost.scotch.VCR;
import com.easypost.scotch.clients.httpurlconnection.VCRHttpUrlConnection;
import com.easypost.scotch.clients.httpurlconnection.VCRHttpsUrlConnection;
import com.easypost.scotch.clients.httpurlconnection.VCRURL;
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

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

    @Test
    public void testCast() throws IOException {
        VCR vcr = TestTools.getRecordingVCR(cassettePath);
        VCRURL url = new VCRURL("https://m.facebook.com", vcr);

        VCRHttpUrlConnection conn = url.openConnection();
        conn.setRequestMethod("DELETE");
        HttpURLConnection connCast = (HttpURLConnection) conn;

        System.out.println(conn.getContent());
        System.out.println(connCast.getContent());

        VCRHttpsUrlConnection sConn = url.openConnectionSecure();
        sConn.setRequestMethod("DELETE");
        HttpsURLConnection sConnCast = (HttpsURLConnection) sConn;

        System.out.println(sConn.getURL());
        System.out.println(sConnCast.getURL());

    }
}
