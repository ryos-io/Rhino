package io.ryos.rhino.sdk;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;

/**
 * Application Unit Tests.
 *
 * @author Erhan Bagdemir
 */
public class ApplicationTest {

  @Test
  public void testURIWithCustomURIs() throws URISyntaxException {

    String uris = "classpath:///rhino.properties";
    URI uri = new URI(uris);
    assertThat(uri.toString(), equalTo(uris));
    assertThat(uri.getPath(), equalTo("/rhino.properties"));
    assertThat(uri.getScheme(), equalTo("classpath"));
  }
}
