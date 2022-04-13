import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpsURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableURL;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class TestHttpUrlConnection {

    private static final String cassettePath = "/Users/nharris/code/scotch_java/cassettes/http_url_connection.json";

    @Test
    public void testHttpUrlConnectionRequest() throws IOException {
        Cassette cassette = new Cassette("fake_path", "cassette_name");

        RecordableURL recordableURL = new RecordableURL("https://www.google.com", cassette, Mode.Bypass);

        RecordableHttpsURLConnection connection = recordableURL.openConnectionSecure();

        // connection.connect();
        System.out.println(connection.getHeaderFields());
        System.out.println(connection.getURL());
    }
}
