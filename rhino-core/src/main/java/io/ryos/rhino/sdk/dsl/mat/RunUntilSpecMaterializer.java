package io.ryos.rhino.sdk.dsl.mat;

import io.ryos.rhino.sdk.data.UserSession;
import io.ryos.rhino.sdk.dsl.specs.RunUntilSpec;
import io.ryos.rhino.sdk.runners.EventDispatcher;
import org.asynchttpclient.AsyncHttpClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Stream;

public class RunUntilSpecMaterializer implements SpecMaterializer<RunUntilSpec, UserSession> {

    private final EventDispatcher eventDispatcher;
    private final AsyncHttpClient asyncHttpClient;

    public RunUntilSpecMaterializer(EventDispatcher eventDispatcher, AsyncHttpClient asyncHttpClient) {
        this.eventDispatcher = eventDispatcher;
        this.asyncHttpClient = asyncHttpClient;
    }

    @Override
    public Mono<UserSession> materialize(RunUntilSpec spec, UserSession userSession) {
        MaterializerFactory materializerFactory = new MaterializerFactory(asyncHttpClient, eventDispatcher);

        return Flux.fromStream(Stream.iterate(userSession, (session) -> !spec.getPredicate().test(session), s -> s))
                .flatMap(s -> materializerFactory.monoFrom(spec.getSpec(), s))
                .reduce((s1, s2) -> s2);
    }
}
