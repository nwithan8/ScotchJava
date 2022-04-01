import com.easypost.easyvcr.clients.httpurlconnection.VCRHttpURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.VCRHttpsURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.VCRURL;
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class TestHttpUrlConnection {

    private static final String cassettePath = "/Users/nharris/code/scotch_java/cassettes/http_url_connection.json";

    private void testHttpUrlConnectionRequest(OldVCR oldVcr) throws IOException {
        VCRURL url = new VCRURL("https://m.facebook.com", oldVcr);

        VCRHttpURLConnection conn = url.openConnection();

        conn.setRequestMethod("DELETE");

        System.out.println(conn.getHeaderFields());
        System.out.println(conn.getResponseCode());
        System.out.println(conn.getURL());
    }

    @Test
    public void testHttpUrlConnectionRecord() throws IOException {
        OldVCR oldVcr = TestTools.getRecordingVCR(cassettePath);
        testHttpUrlConnectionRequest(oldVcr);
    }

    @Test
    // can run offline
    public void testHttpUrlConnectionReplay() throws IOException {
        OldVCR oldVcr = TestTools.getReplayingVCR(cassettePath);
        testHttpUrlConnectionRequest(oldVcr);
    }

    @Test
    public void testCast() throws IOException {
        OldVCR oldVcr = TestTools.getReplayingVCR(cassettePath);
        VCRURL url = new VCRURL("https://m.facebook.com", oldVcr);

        VCRHttpsURLConnection sConn = url.openConnectionSecure();
        sConn.setRequestMethod("DELETE");
        sConn.setRequestProperty("This", "That");
        sConn.setRequestProperty("this2", "that2");
        HttpsURLConnection sConnCast = (HttpsURLConnection) sConn;

        InputStream stream = sConnCast.getInputStream();
        String text = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        // stream.close();
        System.out.println(text);
    }
}
