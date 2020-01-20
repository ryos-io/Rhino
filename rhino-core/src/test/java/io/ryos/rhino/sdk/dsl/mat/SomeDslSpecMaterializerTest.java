package io.ryos.rhino.sdk.dsl.mat;

import static io.ryos.rhino.sdk.dsl.MaterializableDslItem.some;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import io.ryos.rhino.sdk.data.UserSessionImpl;
import io.ryos.rhino.sdk.dsl.SomeDsl;
import io.ryos.rhino.sdk.runners.EventDispatcher;
import io.ryos.rhino.sdk.users.data.UserImpl;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class SomeDslSpecMaterializerTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  EventDispatcher eventDispatcher;

  @Test
  public void testMaterialize() {

    var user = new UserSessionImpl(
        new UserImpl("user", UUID.randomUUID().toString(), "", ""));

    final SomeDsl spec = (SomeDsl) some("test").as(session -> {
      session.add("test", 1);
      return "OK";
    });

    var materialize = new SomeSpecMaterializer().materialize(spec, user);
    var session = materialize.block();

    assertThat(session, notNullValue());
    assertThat(session.get("test"), notNullValue());
    assertThat(session.get("test").isPresent(), equalTo(true));
    assertThat(session.get("test").get(), equalTo(1));
  }
}
