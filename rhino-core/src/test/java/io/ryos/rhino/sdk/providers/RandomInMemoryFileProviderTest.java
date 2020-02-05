package io.ryos.rhino.sdk.providers;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import io.ryos.rhino.sdk.SimulationConfig;
import io.ryos.rhino.sdk.utils.Environment;
import java.io.IOException;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class RandomInMemoryFileProviderTest {

  @Test
  public void testRandomInMemoryFileProvider() throws IOException {
    SimulationConfig
        .newInstance("classpath:///rhino_RandomInMemoryFileProvider.properties", Environment.DEV);
    final RandomInMemoryFileProvider provider = new RandomInMemoryFileProvider();
    final RandomInMemoryFile file = provider.take();
    final String mimeType = file.getMimeType();
    assertThat(mimeType, equalTo("plain/text"));
    final byte[] bytes = file.asStream().readAllBytes();
    assertTrue(bytes.length > 0);
    assertTrue(bytes.length < 10);
  }
}
