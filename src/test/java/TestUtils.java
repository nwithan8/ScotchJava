import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.VCR;
import com.easypost.easyvcr.clients.apachehttpclient.RecordableCloseableHttpClient;
import com.easypost.easyvcr.clients.httpclient.RecordableHttpRequest;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpsURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableURL;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class TestUtils {

    public static final String cassetteFolder = "cassettes";

    public static Cassette getCassette(String cassetteName) {
        return new Cassette(cassetteFolder, cassetteName);
    }

    public static RecordableHttpRequest getSimpleHttpClient(String url, String cassetteName, Mode mode)
            throws URISyntaxException {
        Cassette cassette = getCassette(cassetteName);
        return RecordableHttpRequest.newBuilder(new URI(url), cassette, mode, null).build();
    }

    public static RecordableCloseableHttpClient getSimpleApacheClient(String cassetteName, Mode mode) {
        Cassette cassette = getCassette(cassetteName);
        return new RecordableCloseableHttpClient(cassette, mode, null);
    }

    public static RecordableHttpURLConnection getSimpleHttpURLConnection(String url, String cassetteName, Mode mode)
            throws IOException {
        Cassette cassette = getCassette(cassetteName);
        return new RecordableURL(new URL(url), cassette, mode).openConnection();
    }

    public static RecordableHttpURLConnection getSimpleHttpURLConnection(String cassetteName, Mode mode)
            throws IOException {
        return getSimpleHttpURLConnection(FakeDataService.GET_POSTS_URL, cassetteName, mode);
    }

    public static RecordableHttpsURLConnection getSimpleHttpsURLConnection(String url, String cassetteName, Mode mode)
            throws IOException {
        Cassette cassette = getCassette(cassetteName);
        return new RecordableURL(new URL(url), cassette, mode).openConnectionSecure();
    }

    public static RecordableHttpsURLConnection getSimpleHttpsURLConnection(String cassetteName, Mode mode)
            throws IOException {
        return getSimpleHttpsURLConnection(FakeDataService.GET_POSTS_URL, cassetteName, mode);
    }

    public static VCR getSimpleVCR(Mode mode) {
        VCR vcr = new VCR();

        switch (mode) {
            case Record -> vcr.record();
            case Replay -> vcr.replay();
            case Bypass -> vcr.pause();
            case Auto -> vcr.recordIfNeeded();
            default -> {
            }
        }

        return vcr;
    }
}
