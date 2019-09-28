package io.ryos.rhino.sdk.providers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class RandomInMemoryFileProviderTest {

  private static final int NUMBER_OF_FILES = 2;
  private static final int MAX_SIZE = 100;

  @Test
  public void testCreateInMemoryFileList() {

    List<String> mimeTypes = new ArrayList<>();
    mimeTypes.add("text/plain");
    mimeTypes.add("image/gif");

    var provider = new RandomInMemoryFileProvider(NUMBER_OF_FILES, MAX_SIZE, mimeTypes);
    RandomInMemoryFile file1 = provider.take();
    RandomInMemoryFile file2 = provider.take();
    RandomInMemoryFile file3 = provider.take();

    assertThat(file1.getMimeType(), equalTo("text/plain"));
    assertThat(file2.getMimeType(), equalTo("image/gif"));
    assertThat(file3.getMimeType(), equalTo("text/plain"));
    assertThat(file1, equalTo(file3));
    assertThat(file2, not(equalTo(file3)));
  }
}
