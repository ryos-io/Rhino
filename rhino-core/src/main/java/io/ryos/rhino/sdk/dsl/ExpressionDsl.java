package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import java.util.function.Function;

public interface ExpressionDsl<T> extends MaterializableDslItem, ResultingDsl {

  MaterializableDslItem exec(Function<UserSession, T> function);

  Function<UserSession, T> getExpression();
}
