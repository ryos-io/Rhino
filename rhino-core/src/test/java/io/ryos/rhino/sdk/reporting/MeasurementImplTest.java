package io.ryos.rhino.sdk.reporting;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import io.ryos.rhino.sdk.runners.EventDispatcher;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EventDispatcher.class})
@PowerMockIgnore({ "javax.management.*", "com.sun.org.apache.xerces.*", "javax.xml.*",
    "org.xml.*", "org.w3c.dom.*", "com.sun.org.apache.xalan.*", "javax.activation.*" })
public class MeasurementImplTest {

  private final static String parentName = "container";
  private final static String userId = "userId@org.com";
  private final static String measurementName = "test";
  private final static boolean measurementEnabled = true;

  @Test
  public void testCreateStartMeasurementAndFinish() {
    mockStatic(EventDispatcher.class);
    var dispatcherMock = mock(EventDispatcher.class);
    when(EventDispatcher.getInstance()).thenReturn(dispatcherMock);

    final MeasurementImpl measurement = new MeasurementImpl(parentName,
        userId, measurementName, false, measurementEnabled, dispatcherMock);
    measurement.start();
    measurement.measure("test", "OK");
    measurement.finish();

    final List<LogEvent> events = measurement.getEvents();
    assertThat(events, notNullValue());
    assertThat(events.size(), equalTo(3));
    verify(dispatcherMock, times(1)).dispatchEvents(measurement);
  }
}
