import com.easypost.scotch.HttpClients;
import com.easypost.scotch.ScotchMode;
import com.easypost.scotch.clients.httpurlconnection.VCRHttpUrlConnection;
import com.easypost.scotch.clients.httpurlconnection.VCRURL;
import com.easypost.scotch.clients.scotchhttpclient.VCRScotchHttpClient;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestScratch {
    private VCRScotchHttpClient getScotchHTTPClient(ScotchMode mode) {
        String path = "/Users/nharris/code/scotch_java/cassettes/custom_client.json";
        return HttpClients.NewVCRScotchHttpClient(path, mode);
    }

    private VCRURL getHttpUrlConnectionClient(ScotchMode mode) throws MalformedURLException {
        String path = "/Users/nharris/code/scotch_java/cassettes/http_url_connection.json";
        return new VCRURL("https://m.facebook.com", path, mode);
    }

    private void testScotchHTTPClientRequest(VCRScotchHttpClient client)
            throws URISyntaxException, IOException, InterruptedException {
        List<Map.Entry<String, String>> headers = new ArrayList<>();
        headers.add(VCRScotchHttpClient.makeHeaderEntry("fund", "raising"));
        HttpResponse<String> response = client.post(new URI("https://www.google.com"), headers, "fake_body");

        System.out.println(response.body());
    }

    @Test
    public void testScotchHTTPClientRecord() throws URISyntaxException, IOException, InterruptedException {
        VCRScotchHttpClient vcr = getScotchHTTPClient(ScotchMode.Recording);

        testScotchHTTPClientRequest(vcr);
    }

    @Test
    public void testScotchHTTPClientReplay() throws URISyntaxException, IOException, InterruptedException {
        VCRScotchHttpClient vcr = getScotchHTTPClient(ScotchMode.Replaying);

        testScotchHTTPClientRequest(vcr);
    }

    private void testHttpUrlConnectionRequest(VCRURL vcrurl) throws IOException {
        VCRHttpUrlConnection conn = vcrurl.openConnection();

        conn.setRequestMethod("DELETE");

        System.out.println(conn.getHeaderFields());
    }

    @Test
    public void testHttpUrlConnectionRecord() throws IOException {
        VCRURL url = getHttpUrlConnectionClient(ScotchMode.Recording);

        testHttpUrlConnectionRequest(url);
    }

    @Test
    public void testHttpUrlConnectionReplay() throws IOException {
        VCRURL url = getHttpUrlConnectionClient(ScotchMode.Replaying);

        testHttpUrlConnectionRequest(url);
    }
}
