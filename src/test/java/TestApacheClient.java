import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.clients.apachehttpclient.RecordableCloseableHttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.Test;

import java.io.IOException;

public class TestApacheClient {

    private static final String cassettePath = "/Users/nharris/code/scotch_java/cassettes/apache_http_client.json";

    private void testApacheHttpClientRequest(Mode mode) {
        try {
            RecordableCloseableHttpClient client = new RecordableCloseableHttpClient(new Cassette("test", "name"), mode);
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
    public void testApacheHttpClientRequest() {
        testApacheHttpClientRequest(Mode.Record);
    }
}
