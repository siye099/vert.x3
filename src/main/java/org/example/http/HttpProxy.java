package org.example.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.core.net.JksOptions;
import org.example.Runner;

/*
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class HttpProxy extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
//        Runner.runExample(HttpProxy.class);
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(HttpProxy.class.getName());
    }
    @Override
    public void start() throws Exception {
        HttpClient client = vertx.createHttpClient(new HttpClientOptions().setSsl(false));
        vertx.createHttpServer().requestHandler(req -> {
            System.out.println("Proxying request: " + req.uri());
//            req.response().end("8888");
//            HttpClientRequest c_req = client.request(req.method(), 443, "www.marstranslation.com", req.uri(), c_res -> {
            HttpClientRequest c_req = client.request(req.method(), 80, "www.iteye.com", req.uri(), c_res -> {
                System.out.println("Proxying response: " + c_res.statusCode());
                req.response().setChunked(true);
                req.response().setStatusCode(c_res.statusCode());
                req.response().headers().setAll(c_res.headers());
                c_res.handler(data -> {
                    System.out.println("Proxying response body: " + data.toString("ISO-8859-1"));
                    req.response().write(data);
                });
                c_res.endHandler((v) -> req.response().end());
            });
            c_req.setChunked(true);
            c_req.headers().setAll(req.headers());
            req.handler(data -> {
                System.out.println("Proxying request body " + data.toString("ISO-8859-1"));
                c_req.write(data);
            });
            req.endHandler((v) -> c_req.end());
        }).listen(8080);
    }
}