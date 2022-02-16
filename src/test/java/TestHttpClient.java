import com.easypost.scotch.VCR;
import com.easypost.scotch.clients.httpclient.VCRBodyHandler;
import com.easypost.scotch.clients.httpclient.VCRHttpRequest;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

public class TestHttpClient {

    private static final String cassettePath = "/Users/nharris/code/scotch_java/cassettes/http_request.json";

    private void testRequest(VCR vcr) throws IOException, InterruptedException, URISyntaxException {
        // standard HttpClient
        HttpClient client = HttpClient.newHttpClient();

        // custom VCRHttpRequest
        VCRHttpRequest request = VCRHttpRequest.newVCRBuilder(new URI("https://google.com"), vcr)
                .POST(VCRHttpRequest.BodyPublishers.ofString(
                        "{ \"name\":\"tammy133\", \"salary\":\"5000\", \"age\":\"20\" }")).build();


        // custom VCRBodyHandler
        VCRBodyHandler bodyHandler = new VCRBodyHandler(vcr);

        // pass custom request and custom body handler into standard client
        // returns a standard HttpResponse<String>
        HttpResponse<String> response = client.send(request, bodyHandler);
        System.out.println(response.body());
    }

    @Test
    public void testHttpRecord() throws URISyntaxException, IOException, InterruptedException {
        VCR vcr = TestTools.getRecordingVCR(cassettePath);
        testRequest(vcr);
    }

    @Test
    public void testHttpReplay() throws URISyntaxException, IOException, InterruptedException {
        VCR vcr = TestTools.getReplayingVCR(cassettePath);
        testRequest(vcr);
    }
}
