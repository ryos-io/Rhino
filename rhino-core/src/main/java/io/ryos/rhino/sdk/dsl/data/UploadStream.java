package io.ryos.rhino.sdk.dsl.data;

import io.ryos.rhino.sdk.dsl.HttpDsl;
import io.ryos.rhino.sdk.dsl.impl.HttpDslImpl;
import io.ryos.rhino.sdk.io.ConfigResource;
import java.io.InputStream;
import java.util.Objects;

/**
 * Upload stream is used in {@link HttpDsl} implementations to upload files by POST or PUT
 * requests.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see HttpDslImpl
 * @see ConfigResource
 * @since 1.6.0
 */
public class UploadStream {

  private UploadStream() {
    // intentionally left empty.
  }

  /**
   * Static factory returns the {@link InputStream} from the path to file provided. The client is
   * responsible to close the stream once it is done. The path must be qualified with a scheme, e.g
   * "classpath:///" or "file:///"
   * <p>
   *
   * @param pathToFile Path to file either in classpath or in file system.
   * @return {@link InputStream} instance.
   */
  public static InputStream file(String pathToFile) {
    Objects.requireNonNull(pathToFile, "Path to file parameter must not be null!");
    return new ConfigResource(pathToFile).getInputStream();
  }
}
