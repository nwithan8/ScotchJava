package com.easypost.easyvcr.clients.httpclient;

import com.easypost.easyvcr.AdvancedSettings;
import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.VCRException;
import com.easypost.easyvcr.interactionconverters.HttpClientInteractionConverter;
import com.easypost.easyvcr.requestelements.HttpInteraction;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.function.Function;

import static com.easypost.easyvcr.internalutilities.Tools.simulateDelay;

public class RecordableBodyHandler implements HttpResponse.BodyHandler<String> {

    // only supports UTF_8 charset

    private final Cassette cassette;
    private final Mode mode;
    private final AdvancedSettings advancedSettings;
    private final HttpClientInteractionConverter converter;

    public RecordableBodyHandler(Cassette cassette, Mode mode, AdvancedSettings advancedSettings,
                                 HttpClientInteractionConverter converter) {
        this.cassette = cassette;
        this.mode = mode;
        this.advancedSettings = advancedSettings;
        this.converter = converter;
    }

    /**
     * Returns a {@link HttpResponse.BodySubscriber BodySubscriber} considering the
     * given response status code and headers. This method is invoked before
     * the actual response body bytes are read and its implementation must
     * return a {@link HttpResponse.BodySubscriber BodySubscriber} to consume the response
     * body bytes.
     *
     * <p> The response body can be discarded using one of {@link
     * HttpResponse.BodyHandlers#discarding() discarding} or {@link
     * HttpResponse.BodyHandlers#replacing(Object) replacing}.
     *
     * @param responseInfo the response info
     * @return a body subscriber
     */
    @Override
    public HttpResponse.BodySubscriber<String> apply(HttpResponse.ResponseInfo responseInfo) {
        switch (mode) {
            case Record:
                // Record response information if recording
                this.converter.noteResponseDetails(responseInfo, advancedSettings.censors);
                return RecordableBodySubscriber.ofRecordable(StandardCharsets.UTF_8, cassette, advancedSettings,
                        converter);
            case Replay:
                // Overwrite response information if replaying
                try {
                    HttpInteraction recording =
                            this.converter.loadExistingInteraction(cassette, advancedSettings.matchRules);
                    assert recording != null;
                    simulateDelay(recording, advancedSettings);
                    return HttpResponse.BodySubscribers.replacing(recording.getResponse().getBody());
                } catch (VCRException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            case Auto:
                // Find existing recording, otherwise make a new one
                try {
                    HttpInteraction recording =
                            this.converter.loadExistingInteraction(cassette, advancedSettings.matchRules);
                    assert recording != null;
                    simulateDelay(recording, advancedSettings);
                    return HttpResponse.BodySubscribers.replacing(recording.getResponse().getBody());
                } catch (VCRException e) {
                    this.converter.noteResponseDetails(responseInfo, advancedSettings.censors);
                    return RecordableBodySubscriber.ofRecordable(StandardCharsets.UTF_8, cassette, advancedSettings,
                            converter);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            case Bypass:
            default:
                return HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
        }
    }

    private static class RecordableBodySubscriber {

        public static HttpResponse.BodySubscriber<String> ofRecordable(Charset charset, Cassette cassette,
                                                                       AdvancedSettings advancedSettings,
                                                                       HttpClientInteractionConverter converter) {
            Objects.requireNonNull(charset);
            // return HttpResponse.BodySubscribers.ofString(charset);
            return new HttpResponse.BodySubscriber<String>() {
                private final Function<byte[], String> finisher = new Function<byte[], String>() {
                    @Override
                    public String apply(byte[] bytes) {
                        return new String(bytes, charset);
                    }
                };
                // private final CompletableFuture<String> result = null;
                private final CompletableFuture<String> result = new MinimalFuture<>();
                private final List<ByteBuffer> received = new ArrayList<>();

                private volatile Flow.Subscription subscription;

                static private byte[] join(List<ByteBuffer> bytes) {
                    long remain = 0;
                    for (ByteBuffer buf : bytes) {
                        remain += buf.remaining();
                        if (remain > Integer.MAX_VALUE) {
                            throw new IllegalArgumentException("too many bytes");
                        }
                    }
                    int size = (int) remain;
                    byte[] res = new byte[size];
                    int from = 0;
                    for (ByteBuffer b : bytes) {
                        int l = b.remaining();
                        b.get(res, from, l);
                        from += l;
                    }
                    return res;
                }

                /**
                 * Returns a {@code CompletionStage} which when completed will return
                 * the response body object. This method can be called at any time
                 * relative to the other {@link Flow.Subscriber} methods and is invoked
                 * using the client's {@link HttpClient#executor() executor}.
                 *
                 * @return a CompletionStage for the response body
                 */
                @Override
                public CompletionStage<String> getBody() {
                    try {
                        converter.noteResponseBody(result.get());
                        converter.saveRecording(cassette, advancedSettings.matchRules, false);
                    } catch (InterruptedException | ExecutionException | VCRException e) {
                        e.printStackTrace();
                    }

                    return result;
                }

                /**
                 * Method invoked prior to invoking any other Subscriber
                 * methods for the given Subscription. If this method throws
                 * an exception, resulting behavior is not guaranteed, but may
                 * cause the Subscription not to be established or to be cancelled.
                 *
                 * <p>Typically, implementations of this method invoke {@code
                 * subscription.request} to enable receiving items.
                 *
                 * @param subscription a new subscription
                 */
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    if (this.subscription != null) {
                        subscription.cancel();
                        return;
                    }
                    this.subscription = subscription;
                    // We can handle whatever you've got
                    subscription.request(Long.MAX_VALUE);
                }

                /**
                 * Method invoked with a Subscription's next item.  If this
                 * method throws an exception, resulting behavior is not
                 * guaranteed, but may cause the Subscription to be cancelled.
                 *
                 * @param items the items
                 */
                @Override
                public void onNext(List<ByteBuffer> items) {
                    // incoming buffers are allocated by http client internally,
                    // and won't be used anywhere except this place.
                    // So it's free simply to store them for further processing.
                    boolean hasRemaining = false;
                    for (ByteBuffer buf : items) {
                        if (buf.hasRemaining()) {
                            hasRemaining = true;
                            break;
                        }
                    }
                    assert hasRemaining;
                    received.addAll(items);
                }

                /**
                 * Method invoked upon an unrecoverable error encountered by a
                 * Publisher or Subscription, after which no other Subscriber
                 * methods are invoked by the Subscription.  If this method
                 * itself throws an exception, resulting behavior is
                 * undefined.
                 *
                 * @param throwable the exception
                 */
                @Override
                public void onError(Throwable throwable) {
                    received.clear();
                    result.completeExceptionally(throwable);
                }

                /**
                 * Method invoked when it is known that no additional
                 * Subscriber method invocations will occur for a Subscription
                 * that is not already terminated by error, after which no
                 * other Subscriber methods are invoked by the Subscription.
                 * If this method throws an exception, resulting behavior is
                 * undefined.
                 */
                @Override
                public void onComplete() {
                    try {
                        result.complete(finisher.apply(join(received)));
                        received.clear();
                    } catch (IllegalArgumentException e) {
                        result.completeExceptionally(e);
                    }
                }
            };
        }
    }
}
