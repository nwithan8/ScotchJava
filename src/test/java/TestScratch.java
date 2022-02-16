import com.easypost.scotch.HttpClients;
import com.easypost.scotch.ScotchMode;
import com.easypost.scotch.clients.apachehttpclient.VCRCloseableHttpClient;
import com.easypost.scotch.clients.httpclient.VCR;
import com.easypost.scotch.clients.httpclient.VCRBodyHandler;
import com.easypost.scotch.clients.httpclient.VCRHttpRequest;
import com.easypost.scotch.clients.httpclient.VCRHttpResponse;
import com.easypost.scotch.clients.httpurlconnection.VCRHttpUrlConnection;
import com.easypost.scotch.clients.httpurlconnection.VCRURL;
import com.easypost.scotch.clients.scotchhttpclient.VCRScotchHttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TestScratch {
    private VCRScotchHttpClient getScotchHTTPClient(ScotchMode mode) {
        String path = "/Users/nharris/code/scotch_java/cassettes/custom_client.json";
        return HttpClients.NewVCRScotchHttpClient(path, mode);
    }

    private VCRURL getHttpUrlConnectionClient(ScotchMode mode) throws MalformedURLException {
        String path = "/Users/nharris/code/scotch_java/cassettes/http_url_connection.json";
        return new VCRURL("https://m.facebook.com", path, mode);
    }

    private VCRCloseableHttpClient getApacheClient(ScotchMode mode) {
        String path = "/Users/nharris/code/scotch_java/cassettes/apache_http_client.json";
        return new VCRCloseableHttpClient(path, mode);
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
    // can run offline
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
    // can run offline
    public void testHttpUrlConnectionReplay() throws IOException {
        VCRURL url = getHttpUrlConnectionClient(ScotchMode.Replaying);

        testHttpUrlConnectionRequest(url);
    }

    private void testApacheHttpClientRequest(VCRCloseableHttpClient client) {
        try {
            HttpPost post = new HttpPost("https://google.com");
            StringEntity requestEntity = new StringEntity("fun", ContentType.APPLICATION_JSON);
            post.setEntity(requestEntity);
            CloseableHttpResponse response = client.execute(post);
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testApacheHttpClientRecord() {
        VCRCloseableHttpClient client = getApacheClient(ScotchMode.Recording);

        testApacheHttpClientRequest(client);
    }

    @Test
    public void testApacheHttpClientReplay() {
        VCRCloseableHttpClient client = getApacheClient(ScotchMode.Replaying);

        testApacheHttpClientRequest(client);
    }

    @Test
    public void testHttpRequest() throws URISyntaxException, IOException, InterruptedException, ExecutionException {
        String path = "/Users/nharris/code/scotch_java/cassettes/http_request.json";

        VCR vcr = new VCR(path, ScotchMode.Replaying);

        // standard HttpClient
        HttpClient client = HttpClient.newHttpClient();

        // custom VCRHttpRequest
        VCRHttpRequest request = VCRHttpRequest.newVCRBuilder(new URI("https://google.com"), vcr)
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{ \"name\":\"tammy133\", \"salary\":\"5000\", \"age\":\"20\" }")).build();

        // custom VCRBodyHandler
        VCRBodyHandler bodyHandler = new VCRBodyHandler(vcr);

        // pass custom request and custom body handler into standard client
        // returns a standard HttpResponse<String>
        HttpResponse<String> response = client.send(request, bodyHandler);
        System.out.println(response.body());
    }
}
