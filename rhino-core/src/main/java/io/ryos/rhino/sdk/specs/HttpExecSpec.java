package io.ryos.rhino.sdk.specs;

import io.ryos.rhino.sdk.data.UserSession;

import java.util.function.Function;

public class HttpExecSpec implements ExecSpec<HttpExecContext, HttpExecutable> {

    @Override
    public ExecSpec exec(Function<HttpExecContext, HttpExecutable> executable) {
        return null;
    }
}
