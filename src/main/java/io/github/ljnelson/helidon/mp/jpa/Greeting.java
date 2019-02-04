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

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
public class Greeting {

  @Id
  private Long id;

  private String greeting;

  protected Greeting() {
    super();
  }
  
  public Greeting(final long id, final String greeting) {
    super();
    this.id = Long.valueOf(id);
    this.greeting = Objects.requireNonNull(greeting);
  }

  @Override
  public int hashCode() {
    return this.greeting == null ? 0 : this.greeting.hashCode();
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof Greeting) {
      final Greeting her = (Greeting)other;
      if (this.greeting == null) {
        if (her.greeting != null) {
          return false;
        }
      } else if (!this.greeting.equals(her.greeting)) {
        return false;
      }
      return true;
    } else {
      return false;
    }
  }
  
  @Override
  public String toString() {
    return this.greeting;
  }
  
}
