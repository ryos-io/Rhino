package io.ryos.rhino.sdk.specs;

import io.ryos.rhino.sdk.data.UserSession;

import java.util.function.Function;

public interface ExecSpec<E extends ExecContext, T extends Executable> {


   ExecSpec exec(Function<E, T> executable);
}
