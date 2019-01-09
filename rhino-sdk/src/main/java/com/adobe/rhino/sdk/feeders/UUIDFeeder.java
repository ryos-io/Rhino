package com.adobe.rhino.sdk.feeders;

import java.util.UUID;

public class UUIDFeeder implements Feed<String> {

  @Override
  public String take() {
    return UUID.randomUUID().toString();
  }
}
