package io.ryos.rhino.sdk.annotations;

import io.ryos.rhino.sdk.monitoring.GrafanaDashboard;
import io.ryos.rhino.sdk.monitoring.RhinoGrafanaDashboard;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>
 *
 * @author Erhan Bagdemir
 * @since 1.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Grafana {

  String name() default "classpath:///grafana/dashboard_v2.json";

  Class<? extends GrafanaDashboard> dashboard() default RhinoGrafanaDashboard.class;
}
