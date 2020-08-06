package io.ryos.rhino.sdk.dsl;

import io.ryos.rhino.sdk.data.UserSession;
import java.util.function.Consumer;

public interface ExpressionDsl extends MaterializableDslItem, ResultingDsl {

  Consumer<UserSession> getExpression();
}
