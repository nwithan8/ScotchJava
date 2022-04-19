import com.easypost.easyvcr.VCR;
import com.easypost.easyvcr.clients.apachehttpclient.RecordableCloseableHttpClient;
import com.easypost.easyvcr.clients.httpclient.RecordableHttpRequest;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpsURLConnection;
import com.easypost.easyvcr.internalutilities.json.Serialization;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.net.URI;
import java.net.http.HttpResponse;

import static com.easypost.easyvcr.internalutilities.Tools.readFromInputStream;

public class FakeDataService {

    private final static String GET_POSTS_URL = "https://jsonplaceholder.typicode.com/posts";
    private final static String GET_POST_URL = "https://jsonplaceholder.typicode.com/posts";

    public static class Post {
        public int userId;
        public int id;
        public String title;
        public String body;

        public Post(int userId, int id, String title, String body) {
            this.userId = userId;
            this.id = id;
            this.title = title;
            this.body = body;
        }
    }

    public static class ApacheHttpClient extends FakeDataServiceBase implements FakeDataServiceBaseInterface {
        protected RecordableCloseableHttpClient client;

        public ApacheHttpClient(RecordableCloseableHttpClient client) {
            this.client = client;
        }

        public ApacheHttpClient(VCR vcr) {
            this.vcr = vcr;
        }

        public RecordableCloseableHttpClient getClient() throws Exception {
            if (client != null) {
                return client;
            } else if (vcr != null) {
                return vcr.getApacheHttpClient();
            }
            throw new Exception("No VCR or client has been set.");
        }

        @Override
        public Post[] getPosts() throws Exception {
            RecordableCloseableHttpClient client = getClient();
            HttpGet post = new HttpGet(GET_POSTS_URL);
            CloseableHttpResponse response = client.execute(post);
            String json = readFromInputStream(response.getEntity().getContent());

            return Serialization.convertJsonToObject(json, Post[].class);
        }

        @Override
        public Post getPost(int id) throws Exception {
            RecordableCloseableHttpClient client = getClient();
            HttpGet post = new HttpGet(GET_POSTS_URL + "/" + id);
            CloseableHttpResponse response = client.execute(post);
            String json = readFromInputStream(response.getEntity().getContent());

            return Serialization.convertJsonToObject(json, Post.class);
        }
    }

    public static class HttpClient extends FakeDataServiceBase implements FakeDataServiceBaseInterface {
        protected RecordableHttpRequest.Builder builder;

        public HttpClient(RecordableHttpRequest.Builder builder) {
            this.builder = builder;
        }

        public HttpClient(VCR vcr) {
            this.vcr = vcr;
        }

        public RecordableHttpRequest.Builder getBuilder(String url) throws Exception {
            if (builder != null) {
                return builder;
            } else if (vcr != null) {
                return vcr.getHttpClientBuilder(new URI(url));
            }
            throw new Exception("No VCR or client has been set.");
        }

        @Override
        public Post[] getPosts() throws Exception {
            RecordableHttpRequest.Builder builder = getBuilder(GET_POSTS_URL);
            RecordableHttpRequest request = builder.GET().build();

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String json = response.body();

            return Serialization.convertJsonToObject(json, Post[].class);
        }

        @Override
        public Post getPost(int id) throws Exception {
            RecordableHttpRequest.Builder builder = getBuilder(GET_POST_URL + "/" + id);
            RecordableHttpRequest request = builder.GET().build();

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String json = response.body();

            return Serialization.convertJsonToObject(json, Post.class);
        }
    }

    public static class HttpUrlConnection extends FakeDataServiceBase implements FakeDataServiceBaseInterface {
        protected RecordableHttpURLConnection client;

        public HttpUrlConnection(RecordableHttpURLConnection client) {
            this.client = client;
        }

        public HttpUrlConnection(VCR vcr) {
            this.vcr = vcr;
        }

        public RecordableHttpURLConnection getClient(String url) throws Exception {
            if (client != null) {
                return client;
            } else if (vcr != null) {
                return vcr.getHttpUrlConnection(url).openConnection();
            }
            throw new Exception("No VCR or client has been set.");
        }

        @Override
        public Post[] getPosts() throws Exception {
            RecordableHttpURLConnection client = getClient(GET_POSTS_URL);
            client.connect();
            String json = readFromInputStream(client.getInputStream());

            return Serialization.convertJsonToObject(json, Post[].class);
        }

        @Override
        public Post getPost(int id) throws Exception {
            RecordableHttpURLConnection client = getClient(GET_POST_URL + "/" + id);
            client.connect();
            String json = readFromInputStream(client.getInputStream());

            return Serialization.convertJsonToObject(json, Post.class);
        }
    }

    public static class HttpsUrlConnection extends FakeDataServiceBase implements FakeDataServiceBaseInterface {
        protected RecordableHttpsURLConnection client;

        public HttpsUrlConnection(RecordableHttpsURLConnection client) {
            this.client = client;
        }

        public HttpsUrlConnection(VCR vcr) {
            this.vcr = vcr;
        }

        public RecordableHttpsURLConnection getClient(String url) throws Exception {
            if (client != null) {
                return client;
            } else if (vcr != null) {
                return vcr.getHttpUrlConnection(url).openConnectionSecure();
            }
            throw new Exception("No VCR or client has been set.");
        }

        @Override
        public Post[] getPosts() throws Exception {
            RecordableHttpsURLConnection client = getClient(GET_POSTS_URL);
            client.connect();
            String json = readFromInputStream(client.getInputStream());

            return Serialization.convertJsonToObject(json, Post[].class);
        }

        @Override
        public Post getPost(int id) throws Exception {
            RecordableHttpsURLConnection client = getClient(GET_POST_URL + "/" + id);
            client.connect();
            String json = readFromInputStream(client.getInputStream());

            return Serialization.convertJsonToObject(json, Post.class);
        }
    }

    private static class FakeDataServiceBase {
        protected VCR vcr;

        public FakeDataServiceBase() {
        }
    }

    private interface FakeDataServiceBaseInterface {

        public Post[] getPosts() throws Exception;

        public Post getPost(int id) throws Exception;
    }
}
