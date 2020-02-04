package io.ryos.rhino.sdk.dsl.mat;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import io.ryos.rhino.sdk.data.UserSessionImpl;
import io.ryos.rhino.sdk.dsl.impl.WaitDslImpl;
import io.ryos.rhino.sdk.users.data.UserImpl;
import java.time.Duration;
import org.junit.Test;

public class WaitDslDslMaterializerTest {
  private static final int WAIT_TIME = 100;
  private static final double OVERHEAD_RATIO = 1.2;

  @Test
  public void testMaterialize() throws InterruptedException {
    var waitSpec = new WaitDslImpl(Duration.ofMillis(WAIT_TIME));
    var waitSpecMaterializer = new WaitDslMaterializer(waitSpec);
    var user = new UserImpl("username", "pw", "id", "scope");
    var userSession = new UserSessionImpl(user);
    var mono = waitSpecMaterializer.materialize(userSession);

    // The first call is needed to get the pipeline initialized.
    // It takes a little longer.
    mono.block();

    for (int i = 0; i < 10; i++) {
      long start = System.currentTimeMillis();
      mono.block();
      long end = System.currentTimeMillis();
      long elapsed = end - start;
      assertThat(elapsed >= WAIT_TIME, equalTo(true));
      assertThat(elapsed <  WAIT_TIME * OVERHEAD_RATIO, equalTo(true));
    }
  }
}
