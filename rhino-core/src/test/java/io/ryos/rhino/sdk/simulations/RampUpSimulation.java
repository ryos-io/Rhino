package io.ryos.rhino.sdk.simulations;

import static io.ryos.rhino.sdk.dsl.DslBuilder.dsl;
import static io.ryos.rhino.sdk.dsl.MaterializableDslItem.http;

import io.ryos.rhino.sdk.annotations.Dsl;
import io.ryos.rhino.sdk.annotations.RampUp;
import io.ryos.rhino.sdk.annotations.Simulation;
import io.ryos.rhino.sdk.annotations.UserRepository;
import io.ryos.rhino.sdk.dsl.DslBuilder;
import io.ryos.rhino.sdk.users.repositories.OAuthUserRepositoryFactoryImpl;
import org.junit.jupiter.api.Disabled;

@Disabled
@Simulation(name = "Ramp up simulation", durationInMins = 2)
@UserRepository(factory = OAuthUserRepositoryFactoryImpl.class)
@RampUp(startRps = 10, targetRps = 200)
public class RampUpSimulation {
  @Dsl(name = "Call google")
  public DslBuilder testRampUp() {
    return dsl()
        .run(http("GET google.de")
            .endpoint("https://google.de")
            .get());
  }
}
