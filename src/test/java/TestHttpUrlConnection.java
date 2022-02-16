import com.easypost.scotch.VCR;
import com.easypost.scotch.clients.httpurlconnection.VCRHttpURLConnection;
import com.easypost.scotch.clients.httpurlconnection.VCRHttpsURLConnection;
import com.easypost.scotch.clients.httpurlconnection.VCRURL;
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.stream.Collectors;

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
        VCR vcr = TestTools.getReplayingVCR(cassettePath);
        VCRURL url = new VCRURL("https://m.facebook.com", vcr);

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
