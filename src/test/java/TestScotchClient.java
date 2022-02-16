import com.easypost.scotch.VCR;
import com.easypost.scotch.clients.scotchhttpclient.ScotchVCRHttpClient;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestScotchClient {

    private static final String cassettePath = "/Users/nharris/code/scotch_java/cassettes/custom_client.json";

    private void testScotchHTTPClientRequest(VCR vcr) throws URISyntaxException, IOException, InterruptedException {
        ScotchVCRHttpClient client = ScotchVCRHttpClient.NewScotchVCRHttpClient(vcr);
        List<Map.Entry<String, String>> headers = new ArrayList<>();
        headers.add(ScotchVCRHttpClient.makeHeaderEntry("fund", "raising"));
        HttpResponse<String> response = client.post(new URI("https://www.google.com"), headers, "fake_body");

        System.out.println(response.body());
    }

    @Test
    public void testScotchHTTPClientRecord() throws URISyntaxException, IOException, InterruptedException {
        VCR vcr = TestTools.getRecordingVCR(cassettePath);
        testScotchHTTPClientRequest(vcr);
    }

    @Test
    // can run offline
    public void testScotchHTTPClientReplay() throws URISyntaxException, IOException, InterruptedException {
        VCR vcr = TestTools.getReplayingVCR(cassettePath);
        testScotchHTTPClientRequest(vcr);
    }
}
