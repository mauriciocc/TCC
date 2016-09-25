package com.simple.app.async.report;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.Uri;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.pattern.PatternsCS;
import akka.stream.Materializer;
import scala.concurrent.ExecutionContextExecutor;

import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.model.HttpRequest.GET;

public class HttpActor extends UntypedActor {

    public static final Unmarshaller<HttpEntity, String> STRING_UNMARSHALLER = Unmarshaller.entityToString();
    public static final Unmarshaller<HttpEntity, byte[]> BYTE_UNMARSHALLER = Unmarshaller.entityToByteArray();

    private final ExecutionContextExecutor dispatcher;
    private final Materializer materializer;

    private final Http http;
    private final String todoUrl;
    private final String raytracerUrl;


    public HttpActor(Materializer materializer, String todoUrl, String raytracerUrl) {
        this.todoUrl = todoUrl;
        this.raytracerUrl = raytracerUrl;
        this.http = Http.get(context().system());
        this.materializer = materializer;
        dispatcher = getContext().dispatcher();
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        switch ((Message) message) {
            case FIND_TODOS: {
                CompletionStage<String> response = http.singleRequest(GET(todoUrl+"/api/todos"), materializer)
                        .thenCompose(resp -> STRING_UNMARSHALLER.unmarshall(resp.entity(), dispatcher, materializer));
                PatternsCS.pipe(response, dispatcher).to(sender());
                break;
            }
            case RENDER_IMAGE: {
                CompletionStage<byte[]> response = http.singleRequest(GET(raytracerUrl+"/api/render"), materializer)
                        .thenCompose(resp -> BYTE_UNMARSHALLER.unmarshall(resp.entity(), dispatcher, materializer));
                PatternsCS.pipe(response, dispatcher).to(sender());
            }
        }
    }

    public static Props props(Materializer materializer, String todoUrl, String raytracerUrl) {
        return Props.create(HttpActor.class, () -> new HttpActor(materializer, todoUrl, raytracerUrl));
    }

    public enum Message {
        FIND_TODOS,
        RENDER_IMAGE
    }

}