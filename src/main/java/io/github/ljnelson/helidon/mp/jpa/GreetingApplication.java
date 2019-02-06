/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2019 Laird Nelson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package io.github.ljnelson.helidon.mp.jpa;

import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import javax.ws.rs.core.Application;

/**
 * The world's stupidest application.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
@ApplicationScoped
public class GreetingApplication extends Application {

  /**
   * Creates a new {@link GreetingApplication}.
   */
  public GreetingApplication() {
    super();
  }

  /**
   * Returns a non-{@code null} {@link Set} of {@link Class}es that
   * comprise this JAX-RS application.
   *
   * @return a non-{@code null}, {@linkplain
   * Collections#unmodifiableSet(Set) unmodifiable <code>Set</code>}
   *
   * @see HelloWorldResource
   */
  @Override
  public Set<Class<?>> getClasses() {
    return Collections.singleton(HelloWorldResource.class);
  }
  
}
