/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.reactivex.netty.examples.http.helloworld;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.functions.Func2;

import java.nio.charset.Charset;
import java.util.Map;

import static io.reactivex.netty.examples.http.helloworld.HelloWorldServer.DEFAULT_PORT;

/**
 * @author Nitesh Kant
 */
public class HelloWorldClient {

    private final int port;

    public HelloWorldClient(int port) {
        this.port = port;
    }

    public HttpResponseStatus sendHelloRequest() {
        HttpResponseStatus statusCode = RxNetty.createHttpGet("http://localhost:" + port + "/hello")
                .mergeMap(new Func1<HttpClientResponse<ByteBuf>, Observable<ByteBuf>>() {
                    @Override
                    public Observable<ByteBuf> call(HttpClientResponse<ByteBuf> response) {
                        return response.getContent();
                    }
                }, new Func2<HttpClientResponse<ByteBuf>, ByteBuf, HttpResponseStatus>() {
                    @Override
                    public HttpResponseStatus call(HttpClientResponse<ByteBuf> response, ByteBuf content) {
                        printResponseHeader(response);
                        System.out.println(content.toString(Charset.defaultCharset()));
                        return response.getStatus();
                    }
                })
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        System.out.println("=======================");
                    }
                }).toBlocking().last();

        return statusCode;
    }

    public void printResponseHeader(HttpClientResponse<ByteBuf> response) {
        System.out.println("New response received.");
        System.out.println("========================");
        System.out.println(response.getHttpVersion().text() + ' ' + response.getStatus().code()
                + ' ' + response.getStatus().reasonPhrase());
        for (Map.Entry<String, String> header : response.getHeaders().entries()) {
            System.out.println(header.getKey() + ": " + header.getValue());
        }
    }

    public static void main(String[] args) {
        new HelloWorldClient(DEFAULT_PORT).sendHelloRequest();
    }
}
