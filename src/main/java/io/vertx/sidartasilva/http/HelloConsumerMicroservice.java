package io.vertx.sidartasilva.http;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.web.*;
import io.vertx.rxjava.ext.web.client.*;
import io.vertx.rxjava.ext.web.codec.BodyCodec;
import rx.Single;

public class HelloConsumerMicroservice extends AbstractVerticle {

    private WebClient client;

    @Override
    public void start() {
        client = WebClient.create(vertx);

        Router router = Router.router(vertx);

        router.get("/").handler(this::invokeMyFirstMicroservice);

        vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(8081);
    }

    private void invokeMyFirstMicroservice(RoutingContext rc) {
        HttpRequest<JsonObject> request1 = client
            .get(System.getenv("HOST"), "/Adam")
            .as(BodyCodec.jsonObject());
        HttpRequest<JsonObject> request2 = client
            .get(System.getenv("HOST"), "/Eve")
            .as(BodyCodec.jsonObject());
            
        Single<JsonObject> s1 = request1.rxSend()
            .map(HttpResponse::body);
        Single<JsonObject> s2 = request2.rxSend()
            .map(HttpResponse::body);
        
        Single.zip(s1, s2, (adam, eve) -> {
            // We have the results of both requests in Adam and Eve
            return new JsonObject()
                .put("Adam", adam.getString("message"))
                .put("Eve", eve.getString("message"));
        })
        .subscribe(
            result -> rc.response().end(result.encodePrettily()),
            error -> {
                error.printStackTrace();
                rc.response().setStatusCode(500).end(error.getMessage());
            }

        );
    }

}
