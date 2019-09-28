package io.ryos.rhino.sdk.providers;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Random;

public class RandomInMemoryFile implements Closeable {
  private String mimeType;
  private InputStream stream;

  public RandomInMemoryFile(int size, String mimeType) {
    this.mimeType = mimeType;

    var content = new byte[size];
    new Random().nextBytes(content);
    this.stream = new ByteArrayInputStream(content);
  }

  public InputStream asStream() {
    return stream;
  }

  public String getMimeType() {
    return mimeType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RandomInMemoryFile that = (RandomInMemoryFile) o;
    return mimeType.equals(that.mimeType) &&
        stream.equals(that.stream);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mimeType, stream);
  }

  @Override
  public void close() throws IOException {
    asStream().close();
  }
}
