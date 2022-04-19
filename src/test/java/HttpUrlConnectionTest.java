import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpsURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableURL;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class HttpUrlConnectionTest {

    @Test
    public void testHttpUrlConnectionRequest() throws IOException {
        Cassette cassette = new Cassette("cassettes", "test");

        // record a request first
        RecordableHttpsURLConnection initialConnection = new RecordableURL("https://www.google.com", cassette, Mode.Record).openConnectionSecure();
        int responseCode = initialConnection.getResponseCode();  // get will trigger a cache-write

        // now make a real request and a replay request
        // one is a real (in-memory cached) request, the other is a replay (loaded from file) request
        RecordableHttpsURLConnection realConnection = new RecordableURL("https://www.google.com", cassette, Mode.Bypass).openConnectionSecure();
        RecordableHttpsURLConnection recordedConnection = new RecordableURL("https://www.google.com", cassette, Mode.Replay).openConnectionSecure();


        Instant start = Instant.now();
        int realResponse = realConnection.getResponseCode();
        Instant end = Instant.now();
        System.out.println("realConnection took " + Duration.between(start, end).toMillis() + "ms");

        Instant start2 = Instant.now();
        int replayedResponse = recordedConnection.getResponseCode();
        Instant end2 = Instant.now();
        System.out.println("recordedConnection took " + Duration.between(start2, end2).toMillis() + "ms");

        Assert.assertEquals(realResponse, replayedResponse);
    }
}
