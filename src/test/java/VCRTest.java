import com.easypost.easyvcr.AdvancedSettings;
import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Censors;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.VCR;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpsURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableURL;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;

public class VCRTest {


    @Test
    public void testClient() throws MalformedURLException {
        Cassette cassette = TestUtils.getCassette("test_vcr_client");
        VCR vcr = TestUtils.getSimpleVCR(Mode.Bypass);
        vcr.insert(cassette);

        Assert.assertNotNull(vcr.getHttpUrlConnection("https://google.com"));
    }

    @Test
    public void testClientHandoff() throws Exception {
        Cassette cassette = TestUtils.getCassette("test_vcr_mode_hand_off");
        VCR vcr = TestUtils.getSimpleVCR(Mode.Bypass);
        vcr.insert(cassette);

        // test that we can still control the VCR even after it's been handed off to the service using it
        FakeDataService.HttpsUrlConnection fakeDataService = new FakeDataService.HttpsUrlConnection(vcr);
        // Client should come from VCR, which has a client because it has a cassette.
        Assert.assertNotNull(fakeDataService.getClient("https://google.com"));

        vcr.eject();
        // Should throw an exception because the VCR's cassette has been ejected (can't make a client without a cassette)
        Assert.assertThrows(IllegalArgumentException.class, () -> fakeDataService.getClient("https://google.com"));
    }

    @Test
    public void testClientNoCassette() {
        VCR vcr = TestUtils.getSimpleVCR(Mode.Bypass);
        // Should throw an exception because the VCR has no cassette (can't make a client without a cassette)
        Assert.assertThrows(Exception.class, () -> vcr.getHttpUrlConnection("https://google.com"));
    }

    @Test
    public void testInsertCassette() {
        Cassette cassette = TestUtils.getCassette("test_vcr_insert_cassette");
        var vcr = TestUtils.getSimpleVCR(Mode.Bypass);
        vcr.insert(cassette);
        Assert.assertEquals(cassette.name, vcr.getCassetteName());
    }

    @Test
    public void testEjectCassette() {
        var cassette = TestUtils.getCassette("test_vcr_eject_cassette");
        var vcr = TestUtils.getSimpleVCR(Mode.Bypass);
        vcr.insert(cassette);
        Assert.assertNotNull(vcr.getCassetteName());
        vcr.eject();
        Assert.assertNull(vcr.getCassetteName());
    }

    @Test
    public void testErase() throws Exception {
        var cassette = TestUtils.getCassette("test_vcr_eject_cassette");
        cassette.erase(); // make sure the cassette is empty
        var vcr = TestUtils.getSimpleVCR(Mode.Record);
        vcr.insert(cassette);

        // record a request to a cassette
        FakeDataService.HttpsUrlConnection fakeDataService = new FakeDataService.HttpsUrlConnection(vcr);
        FakeDataService.Post[] posts = fakeDataService.getPosts();
        Assert.assertNotNull(posts);
        Assert.assertTrue(cassette.numInteractions() > 0);

        // erase the cassette
        vcr.erase();
        Assert.assertEquals(0, cassette.numInteractions());
    }

    @Test
    public void testMode() {
        var cassette = TestUtils.getCassette("test_vcr_mode");
        var vcr = TestUtils.getSimpleVCR(Mode.Bypass);
        Assert.assertEquals(Mode.Bypass, vcr.getMode());
        vcr.record();
        Assert.assertEquals(Mode.Record, vcr.getMode());
        vcr.replay();
        Assert.assertEquals(Mode.Replay, vcr.getMode());
        vcr.pause();
        Assert.assertEquals(Mode.Bypass, vcr.getMode());
        vcr.recordIfNeeded();
        Assert.assertEquals(Mode.Auto, vcr.getMode());
    }

    @Test
    public void testRequest() throws Exception {
        var cassette = TestUtils.getCassette("test_vcr_record");
        var vcr = TestUtils.getSimpleVCR(Mode.Bypass);
        vcr.insert(cassette);
        FakeDataService.HttpsUrlConnection fakeDataService = new FakeDataService.HttpsUrlConnection(vcr);

        FakeDataService.Post[] posts = fakeDataService.getPosts();
        Assert.assertNotNull(posts);
        Assert.assertEquals(100, posts.length);
    }

    @Test
    public void testRecord() throws Exception {
        var cassette = TestUtils.getCassette("test_vcr_record");
        var vcr = TestUtils.getSimpleVCR(Mode.Record);
        vcr.insert(cassette);
        FakeDataService.HttpsUrlConnection fakeDataService = new FakeDataService.HttpsUrlConnection(vcr);

        FakeDataService.Post[] posts = fakeDataService.getPosts();
        Assert.assertNotNull(posts);
        Assert.assertEquals(100, posts.length);
        Assert.assertTrue(cassette.numInteractions() > 0);
    }

    @Test
    public void testReplay() throws Exception {
        var cassette = TestUtils.getCassette("test_vcr_replay");
        var vcr = TestUtils.getSimpleVCR(Mode.Record);
        vcr.insert(cassette);
        FakeDataService.HttpsUrlConnection fakeDataService = new FakeDataService.HttpsUrlConnection(vcr);

        // record first
        FakeDataService.Post[] posts = fakeDataService.getPosts();
        Assert.assertTrue(cassette.numInteractions() > 0); // make sure we recorded something

        // now replay
        vcr.replay();
        posts = fakeDataService.getPosts();
        Assert.assertNotNull(posts);

        // double check by erasing the cassette and trying to replay
        vcr.erase();
        // should throw an exception because there's no matching interaction now
        Assert.assertThrows(Exception.class, fakeDataService::getPosts);
    }

    @Test
    public void testCassetteName() {
        String cassetteName = "test_vcr_cassette_name";
        var cassette = TestUtils.getCassette(cassetteName);
        var vcr = TestUtils.getSimpleVCR(Mode.Bypass);
        vcr.insert(cassette);

        // make sure the cassette name is set correctly
        Assert.assertEquals(cassetteName, vcr.getCassetteName());
    }

    @Test
    public void testAdvancedSettings() throws Exception {
        // we can assume that, if one test of advanced settings works for the VCR,
        // that the advanced settings are being properly passed to the cassette
        // refer to ClientTest.cs for individual per-settings tests

        String censorString = "censored-by-test";

        var advancedSettings = new AdvancedSettings();
        advancedSettings.censors = new Censors(censorString).hideHeader("Date");

        var vcr = new VCR(advancedSettings);

        // test that the advanced settings are applied inside the VCR
        Assert.assertEquals(advancedSettings, vcr.getAdvancedSettings());

        // test that the advanced settings are passed to the cassette by checking if censor is applied
        var cassette = TestUtils.getCassette("test_vcr_advanced_settings");
        vcr.insert(cassette);
        vcr.erase(); // erase before recording

        // record first
        vcr.record();
        RecordableURL client = vcr.getHttpUrlConnection(FakeDataService.GET_POSTS_URL);
        FakeDataService.HttpsUrlConnection fakeDataService = new FakeDataService.HttpsUrlConnection(client.openConnectionSecure());
        FakeDataService.Post[] posts = fakeDataService.getPosts();

        // now replay and confirm that the censor is applied
        vcr.replay();
        // changing the VCR settings won't affect a client after it's been grabbed from the VCR
        // so, we need to re-grab the VCR client and re-create the FakeDataService
        client = vcr.getHttpUrlConnection(FakeDataService.GET_POSTS_URL);
        fakeDataService = new FakeDataService.HttpsUrlConnection(client.openConnectionSecure());
        RecordableHttpsURLConnection response = (RecordableHttpsURLConnection) fakeDataService.getPostsRawResponse();

        // check that the censor is applied
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getHeaderField("Date"));
        String censoredHeader = response.getHeaderField("Date");
        Assert.assertNotNull(censoredHeader);
        Assert.assertEquals(censoredHeader, censorString);
    }

    @Test
    public void testCassetteSwap()
    {
        String cassette1Name = "test_vcr_cassette_swap_1";
        String cassette2Name = "test_vcr_cassette_swap_2";

        var vcr = new VCR();

        var cassette = TestUtils.getCassette(cassette1Name);
        vcr.insert(cassette);
        Assert.assertEquals(cassette1Name, vcr.getCassetteName());

        vcr.eject();
        Assert.assertNull(vcr.getCassetteName());

        cassette = TestUtils.getCassette(cassette2Name);
        vcr.insert(cassette);
        Assert.assertEquals(cassette2Name, vcr.getCassetteName());
    }
}
