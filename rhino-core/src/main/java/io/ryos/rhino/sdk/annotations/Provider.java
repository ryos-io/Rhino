/*
  Copyright 2018 Ryos.io.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package io.ryos.rhino.sdk.annotations;

import io.ryos.rhino.sdk.providers.UUIDProvider;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation is used to mark the {@link Provider} injection point. A provider, is a factory
 * class of type {@link io.ryos.rhino.sdk.providers.Provider} which provides scenario methods
 * with object instances of that particular provider. An example of provider is {@link UUIDProvider}
 * which generates random UUIDs:
 * <p>
 *
 * <code>
 *    @Provider(factory = UUIDProvider.class)
 *    private UUIDProvider uuidProvider;
 * </code>
 *
 * Test developers might choose to write their own {@link io.ryos.rhino.sdk.providers.Provider}
 * instances in addition to such the framework includes.
 * <p>
 *
 * @author Erhan Bagdemir
 * @see io.ryos.rhino.sdk.providers.Provider
 * @since 1.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Provider {

  Class<? extends io.ryos.rhino.sdk.providers.Provider> clazz();
}
