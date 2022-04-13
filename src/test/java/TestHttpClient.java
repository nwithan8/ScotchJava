import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.clients.httpclient.RecordableHttpRequest;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

public class TestHttpClient {

    private static final String cassettePath = "/Users/nharris/code/scotch_java/cassettes/";

    @Test
    public void testRequest() throws IOException, InterruptedException, URISyntaxException {
        // standard HttpClient
        HttpClient client = HttpClient.newHttpClient();

        Cassette cassette = new Cassette(cassettePath, "test_cassette");
        Mode mode = Mode.Replay;

        // custom RecordableHttpRequest
        RecordableHttpRequest request = RecordableHttpRequest.newBuilder(new URI("https://google.com"), cassette, mode, null)
                .POST(RecordableHttpRequest.BodyPublishers.ofString(
                        "{ \"name\":\"tammy133\", \"salary\":\"5000\", \"age\":\"20\" }")).build();

        // pass custom request and custom body handler into standard client
        // returns a standard HttpResponse<String>
        HttpResponse<String> response = client.send(request, request.getBodyHandler());
        System.out.println(response.body());
    }
}
