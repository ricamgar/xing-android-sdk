/*
 * Copyright (c) 2015 XING AG (http://xing.com/)
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xing.android.sdk;

import android.support.annotation.Nullable;

import com.squareup.moshi.JsonAdapter;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;
import com.xing.android.sdk.internal.HttpMethod;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import rx.Observable;

import static com.xing.android.sdk.Utils.assertionError;
import static com.xing.android.sdk.Utils.checkNotNull;
import static com.xing.android.sdk.Utils.closeQuietly;
import static com.xing.android.sdk.Utils.stateError;
import static com.xing.android.sdk.Utils.stateNotNull;

/**
 * TODO docs.
 *
 * @author serj.lotutovici
 */
public final class CallSpec<RT, ET> {
    private final XingApi api;
    private final Builder<RT, ET> builder;
    private final CompositeType responseType;
    private final CompositeType errorType;

    private volatile Call rawCall;
    private boolean executed; // Guarded by this.
    private volatile boolean canceled;

    private CallSpec(Builder<RT, ET> builder) {
        this.builder = builder;
        api = builder.api;
        responseType = builder.responseType;
        errorType = builder.errorType;
    }

    /**
     * Synchronously executes the request and returns it's response.
     *
     * @throws IOException If a problem occurred while talking to the server.
     * @throws RuntimeException If an unexpected error occurred during execution or while parsing response.
     */
    public Response<RT, ET> execute() throws IOException {
        synchronized (this) {
            if (executed) throw stateError("Call already executed");
            executed = true;
        }

        Call rawCall = createRawCall();
        if (canceled) rawCall.cancel();
        this.rawCall = rawCall;

        return parseResponse(rawCall.execute());
    }

    /**
     * Asynchronously send the request and notify {@code callback} of its response or if an error
     * occurred talking to the server, creating the request, or processing the response.
     * <p>
     * This method is <i>null-safe</i>, which means that there will be no failure or error propagated if
     * {@code callback} methods will throw an error.
     * <p>
     * Note that the {@code callback} will be dropped after the call execution.
     */
    public void enqueue(final Callback<RT, ET> callback) {
        synchronized (this) {
            if (executed) throw stateError("Call already executed");
            executed = true;
        }

        Call rawCall;
        try {
            rawCall = createRawCall();
        } catch (Throwable t) {
            callback.onFailure(t);
            return;
        }
        if (canceled) {
            rawCall.cancel();
        }
        this.rawCall = rawCall;

        rawCall.enqueue(new com.squareup.okhttp.Callback() {
            private void callFailure(Throwable e) {
                try {
                    callback.onFailure(e);
                } catch (Throwable t) {
                    // TODO add some logging
                }
            }

            private void callSuccess(Response<RT, ET> response) {
                try {
                    callback.onResponse(response);
                } catch (Throwable t) {
                    // TODO add some logging
                }
            }

            @Override
            public void onFailure(Request request, IOException e) {
                callFailure(e);
            }

            @Override
            public void onResponse(com.squareup.okhttp.Response rawResponse) {
                Response<RT, ET> response;
                try {
                    response = parseResponse(rawResponse);
                } catch (Throwable e) {
                    callFailure(e);
                    return;
                }
                callSuccess(response);
            }
        });
    }

    public Observable<Response<RT, ET>> stream() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Returns true if this call has been either {@linkplain #execute() executed} or {@linkplain #enqueue(Callback)
     * enqueued}. It is an error to execute or enqueue a call more than once.
     */
    public synchronized boolean isExecuted() {
        return executed;
    }

    /** True if {@link #cancel()} was called. */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Cancel this call. An attempt will be made to cancel in-flight calls, and if the call has not yet been executed
     * it never will be.
     */
    public void cancel() {
        canceled = true;
        Call rawCall = this.rawCall;
        if (rawCall != null) rawCall.cancel();
    }

    public CallSpec<RT, ET> queryParam(String name, String value) {
        builder.queryParam(name, value);
        return this;
    }

    public CallSpec<RT, ET> formField(String name, String value) {
        builder.formField(name, value);
        return this;
    }

    public <U> CallSpec<RT, ET> body(Class<U> cls, U body) {
        builder.body(cls, body);
        return this;
    }

    /** Returns a raw {@link Call} pre-building the targeted request. */
    private Call createRawCall() {
        return api.client.newCall(builder.request());
    }

    /** Parsers the OkHttp raw response and returns an response ready to be consumed by the caller. */
    @SuppressWarnings("MagicNumber") // These codes are specific to this method and to the http protocol.
    private Response<RT, ET> parseResponse(com.squareup.okhttp.Response rawResponse) throws IOException {
        ResponseBody rawBody = rawResponse.body();

        // Remove the body's source (the only stateful object) so we can pass the response along.
        rawResponse = rawResponse.newBuilder()
              .body(new NoContentResponseBody(rawBody.contentType(), rawBody.contentLength()))
              .build();

        ExceptionCatchingRequestBody catchingBody = new ExceptionCatchingRequestBody(rawBody);
        int code = rawResponse.code();
        if (code < 200 || code >= 300) {
            try {
                // Buffer the entire body to avoid future I/O.
                ET errorBody = errorType.fromJson(api.converter, catchingBody);
                return Response.error(errorBody, rawResponse);
            } catch (RuntimeException e) {
                // If the underlying source threw an exception, propagate that, rather than indicating it was
                // a runtime exception.
                catchingBody.throwIfCaught();
                throw e;
            } finally {
                closeQuietly(catchingBody);
            }
        }

        // No need to parse the response body since the response should not contain a body.
        if (code == 204 || code == 205) {
            return Response.success(null, rawResponse);
        }

        try {
            RT body = responseType.fromJson(api.converter, catchingBody);
            return Response.success(body, rawResponse);
        } catch (RuntimeException e) {
            // If the underlying source threw an exception, propagate that, rather than indicating it was
            // a runtime exception.
            catchingBody.throwIfCaught();
            throw e;
        } finally {
            closeQuietly(catchingBody);
        }
    }

    /**
     * TODO docs.
     */
    public static final class Builder<RT, ET> {
        // Upper and lower characters, digits, underscores, and hyphens, starting with a character.
        private static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
        private static final Pattern PARAM_NAME_REGEX = Pattern.compile(PARAM);
        private static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");

        static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

        private final XingApi api;
        private final HttpMethod httpMethod;
        private final HttpUrl apiEndpoint;
        private final Request.Builder requestBuilder;
        private final Set<String> resourcePathParams;

        private String resourcePath;
        private HttpUrl.Builder urlBuilder;
        private FormEncodingBuilder formEncodingBuilder;
        private RequestBody body;

        private CompositeType responseType;
        private CompositeType errorType;

        // For now block the possibility to build outside this package.
        Builder(XingApi api, HttpMethod httpMethod, String resourcePath, boolean isFormEncoded) {
            this.api = api;
            this.httpMethod = checkNotNull(httpMethod, "httpMethod == null");
            this.resourcePath = checkNotNull(resourcePath, "resourcePath == null");

            resourcePathParams = parseResourcePathParams(resourcePath);
            apiEndpoint = api.apiEndpoint;
            requestBuilder = new Request.Builder();

            if (isFormEncoded) formEncodingBuilder = new FormEncodingBuilder();
        }

        public Builder<RT, ET> pathParam(String name, String value) {
            stateNotNull(resourcePath, "Path params must be set before query params.");
            validatePathParam(name);
            resourcePath = resourcePath.replace('{' + name + '}', UrlEscapeUtils.escape(value));
            resourcePathParams.remove(name);
            return this;
        }

        public Builder<RT, ET> queryParam(String name, @Nullable Object value) {
            if (resourcePath != null) buildUrlBuilder();
            urlBuilder.addEncodedQueryParameter(name, UrlEscapeUtils.escape(String.valueOf(value)));
            return this;
        }

        public Builder<RT, ET> formField(String name, @Nullable Object value) {
            formEncodingBuilder.add(name, String.valueOf(value));
            return this;
        }

        public Builder<RT, ET> body(RequestBody body) {
            this.body = body;
            return this;
        }

        //TODO Avoid converting response body on main thread?
        public <U> Builder<RT, ET> body(Class<U> cls, U body) {
            Buffer buffer = new Buffer();
            JsonAdapter<U> jsonAdapter = api.converter.adapter(cls);
            try {
                jsonAdapter.toJson(buffer, body);
            } catch (IOException ignored) {
                // Doesn't need to be handled. Buffer should not throw in this case.
            }
            return body(RequestBody.create(MEDIA_TYPE_JSON, buffer.readByteArray()));
        }

        public Builder<RT, ET> header(String name, String value) {
            requestBuilder.header(name, value);
            return this;
        }

        public Builder<RT, ET> responseAs(Class<RT> type, String... roots) {
            responseType = CompositeType.single(checkNotNull(type, "type == null"), roots);
            return this;
        }

        public Builder<RT, ET> responseAsListOf(Type type, String... roots) {
            responseType = CompositeType.list(checkNotNull(type, "type == null"), roots);
            return this;
        }

        public Builder<RT, ET> responseAsFirst(Class<RT> type, String... roots) {
            responseType = CompositeType.first(checkNotNull(type, "type == null"), roots);
            return this;
        }

        //This is needed for XING internal APIs that return the error message in a custom format.
        public Builder<RT, ET> errorAs(Class<ET> type) {
            errorType = CompositeType.single(checkNotNull(type, "type == null"));
            return this;
        }

        public CallSpec<RT, ET> build() {
            if (!resourcePathParams.isEmpty()) {
                throw stateError("Not all path params where set. Found %d unsatisfied parameter(s)",
                      resourcePathParams.size());
            }

            if (urlBuilder == null) buildUrlBuilder();
            if (responseType == null) throw stateError("Response type is not set.");
            if (errorType == null) errorType = CompositeType.single(ErrorBody.class);

            return new CallSpec<>(this);
        }

        Request request() {
            if (urlBuilder == null) throw stateError("#request() can be called only after #build()");
            HttpUrl url = urlBuilder.build();

            RequestBody body = this.body;
            if (body == null) {
                // Try to pull from one of the builders.
                if (formEncodingBuilder != null) {
                    body = formEncodingBuilder.build();
                } else if (httpMethod.hasBody()) {
                    // Body is absent, make an empty body.
                    //noinspection ZeroLengthArrayAllocation
                    body = RequestBody.create(null, new byte[0]);
                }
            }

            //TODO (SerjLtt) set content type.

            return requestBuilder
                  .url(url)
                  .method(httpMethod.method(), body)
                  .build();
        }

        /** Do a one-time combination of the built relative URL and the base URL. */
        private void buildUrlBuilder() {
            urlBuilder = apiEndpoint.resolve(resourcePath).newBuilder();
            resourcePath = null;
        }

        /**
         * Gets the set of unique path parameters used in the given URI. If a parameter is used twice
         * in the URI, it will only show up once in the set.
         */
        static Set<String> parseResourcePathParams(String resourcePath) {
            Matcher matcher = PARAM_URL_REGEX.matcher(resourcePath);
            //noinspection CollectionWithoutInitialCapacity
            Set<String> patterns = new LinkedHashSet<>();
            while (matcher.find()) {
                patterns.add(matcher.group(1));
            }
            return patterns;
        }

        private void validatePathParam(String name) {
            if (!PARAM_NAME_REGEX.matcher(name).matches()) {
                throw assertionError("Path parameter name must match %s. Found: %s", PARAM_URL_REGEX.pattern(), name);
            }
            // Verify URL replacement name is actually present in the URL path.
            if (!resourcePathParams.contains(name)) {
                throw assertionError(
                      "Resource path \"%s\" does not contain \"{%s}\". Or the path parameter has been already set.",
                      resourcePath, name);
            }
        }
    }

    static final class NoContentResponseBody extends ResponseBody {
        private final MediaType contentType;
        private final long contentLength;

        NoContentResponseBody(MediaType contentType, long contentLength) {
            this.contentType = contentType;
            this.contentLength = contentLength;
        }

        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public long contentLength() {
            return contentLength;
        }

        @Override
        public BufferedSource source() {
            throw new IllegalStateException("Cannot read raw response body of a parsed body.");
        }
    }

    static final class ExceptionCatchingRequestBody extends ResponseBody {
        private final ResponseBody delegate;
        private IOException thrownException;

        ExceptionCatchingRequestBody(ResponseBody delegate) {
            this.delegate = delegate;
        }

        @Override
        public MediaType contentType() {
            return delegate.contentType();
        }

        @Override
        public long contentLength() throws IOException {
            try {
                return delegate.contentLength();
            } catch (IOException e) {
                thrownException = e;
                throw e;
            }
        }

        @Override
        public BufferedSource source() throws IOException {
            BufferedSource delegateSource;
            try {
                delegateSource = delegate.source();
            } catch (IOException e) {
                thrownException = e;
                throw e;
            }
            return Okio.buffer(new ForwardingSource(delegateSource) {
                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    try {
                        return super.read(sink, byteCount);
                    } catch (IOException e) {
                        thrownException = e;
                        throw e;
                    }
                }
            });
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        void throwIfCaught() throws IOException {
            if (thrownException != null) throw thrownException;
        }
    }
}
