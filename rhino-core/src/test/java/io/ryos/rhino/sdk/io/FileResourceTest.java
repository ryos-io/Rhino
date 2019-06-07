package io.ryos.rhino.sdk.io;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

import java.net.URL;
import java.nio.file.Path;
import org.junit.Test;

public class FileResourceTest {

  @Test
  public void testReadFileResource() throws Exception {

    final URL resource = getClass().getResource("/rhino.properties");
    try (var fr = new FileResource("file://" + Path.of(resource.toURI())).getInputStream()) {
      assertThat(fr, notNullValue());
    }
  }

  @Test()
  public void testReadNotExistingFileResource() throws Exception {
    try (var fr = new FileResource("file:///a/b/c").getInputStream()) {
      assertThat(fr, nullValue());
    }
  }

  @Test
  public void testReadClasspathFileResource() throws Exception {
    try (var fr = new FileResource("classpath:///rhino.properties").getInputStream()) {
      assertThat(fr, notNullValue());
    }
  }
}
