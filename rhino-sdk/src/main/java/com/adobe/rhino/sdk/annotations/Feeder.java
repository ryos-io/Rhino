package com.adobe.rhino.sdk.annotations;

import com.adobe.rhino.sdk.feeders.Feed;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Feeder {
    Class<? extends Feed> factory();
}
