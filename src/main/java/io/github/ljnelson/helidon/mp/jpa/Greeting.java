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
import javax.enterprise.context.Dependent;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * A breathtakingly stupid representation of a two-part greeting as
 * might be stored in a database.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
@Access(AccessType.FIELD)
@Entity(name = "Greeting")
@NamedQuery(name = "findByFirstPart",
            query = "SELECT g FROM Greeting g WHERE g.firstPart = :firstPart")
@Table(name = "GREETING",
       uniqueConstraints = {
         @UniqueConstraint(columnNames = {
             "FIRSTPART", "SECONDPART"
           },
           name = "UNQ_GREETING"
         )
       })
public class Greeting {

  @Id
  @Column(name = "ID", insertable = true, nullable = false, updatable = false)
  @GeneratedValue
  private Long id;

  @Basic(optional = false)
  @Column(name = "FIRSTPART", insertable = true, nullable = false, updatable = true)
  private String firstPart;

  @Basic(optional = false)
  @Column(name = "SECONDPART", insertable = true, nullable = false, updatable = true)
  private String secondPart;

  /**
   * Creates a new {@link Greeting}; required by the JPA specification
   * and for no other purpose.
   *
   * @deprecated Please use the {@link #Greeting(Long, String,
   * String)} constructor instead.
   *
   * @see #Greeting(Long, String, String)
   */
  @Deprecated
  protected Greeting() {
    super();
  }

  /**
   * Creates a new {@link Greeting}.
   *
   * @param id the identifier; may be {@code null}
   *
   * @param firstPart the first part of the greeting; must not be
   * {@code null}
   *
   * @param secondPart the second part of the greeting; must not be
   * {@code null}
   *
   * @exception NullPointerException if {@code firstPart} or {@code
   * secondPart} is {@code null}
   */
  public Greeting(final Long id, final String firstPart, final String secondPart) {
    super();
    this.id = id;
    this.firstPart = Objects.requireNonNull(firstPart);
    this.secondPart = Objects.requireNonNull(secondPart);
  }

  /**
   * Returns the identifier of this {@link Greeting}.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the identifier of this {@link Greeting} or {@code null}
   */
  public Long getId() {
    return this.id;
  }

  /**
   * Returns a {@link String} representation of the second part of
   * this {@link Greeting}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return a non-{@code null} {@link String} representation of the
   * second part of this {@link Greeting}
   */
  @Override
  public String toString() {
    return this.secondPart;
  }
  
}
