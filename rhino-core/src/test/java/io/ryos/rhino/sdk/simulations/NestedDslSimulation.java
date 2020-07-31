package io.ryos.rhino.sdk.simulations;

import static io.ryos.rhino.sdk.dsl.DslBuilder.dsl;
import static io.ryos.rhino.sdk.dsl.MaterializableDslItem.some;
import static io.ryos.rhino.sdk.dsl.data.builder.ForEachBuilderImpl.in;
import static io.ryos.rhino.sdk.dsl.utils.SessionUtils.session;

import com.google.common.collect.ImmutableList;
import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserProvider;
import io.ryos.rhino.sdk.dsl.DslBuilder;
import io.ryos.rhino.sdk.dsl.SessionDslItem.Scope;
import io.ryos.rhino.sdk.providers.OAuthUserProvider;
import java.util.List;

@Simulation(name = "Reactive Multi-User Test")
public class NestedDslSimulation {

  @UserProvider
  private OAuthUserProvider userProvider;

  static List<String> getFiles() {
    return ImmutableList.of("file1", "file2");
  }

  @Dsl(name = "test")
  public DslBuilder setUp() {
    return dsl()
        .session("index", ImmutableList.of(1, 2, 3))
        .forEach(in(session("index")).map(n -> (Integer) n * 2).collect("mapped", Scope.USER))
        .forEach(in(session("mapped")).exec(s ->
            some("test").exec(session -> {
              return "OK";
            })));
  }
}
