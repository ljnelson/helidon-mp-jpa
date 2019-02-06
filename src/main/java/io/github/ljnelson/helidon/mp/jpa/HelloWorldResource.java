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
import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceException; // for javadoc only
import javax.persistence.SynchronizationType;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * A staggeringly stupid JAX-RS root resource class that manipulates
 * greetings in a database.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see #get(String)
 *
 * @see #post(String, String)
 */
@Path("")
@RequestScoped // see https://github.com/oracle/helidon/issues/363
public class HelloWorldResource {  

  /**
   * The {@link EntityManager} used by this class.
   *
   * <p>Note that it behaves as though there is a transaction manager
   * in effect, because there is.</p>
   */
  @PersistenceContext(unitName = "test", synchronization = SynchronizationType.SYNCHRONIZED, type = PersistenceContextType.TRANSACTION)
  private EntityManager entityManager;

  /**
   * Creates a new {@link HelloWorldResource}.
   */
  public HelloWorldResource() {
    super();
  }

  /**
   * When handed a {@link String} like, say, "{@code hello}", responds
   * with the second part of the composite greeting as found via an
   * {@link EntityManager}.
   *
   * <p>I told you this was a dumb application.</p>
   *
   * @param firstPart the first part of the greeting; must not be
   * {@code null}
   *
   * @return the second part of the greeting; never {@code null}
   *
   * @exception NullPointerException if {@code firstPart} was {@code
   * null}
   *
   * @exception PersistenceException if the {@link EntityManager} blew
   * up
   */
  @GET
  @Path("{firstPart}")
  @Produces(MediaType.TEXT_PLAIN)
  public String get(@PathParam("firstPart") final String firstPart) {
    Objects.requireNonNull(firstPart);
    assert this.entityManager != null;
    final TypedQuery<Greeting> query = this.entityManager.createNamedQuery("findByFirstPart", Greeting.class);
    query.setParameter("firstPart", firstPart);
    return query.getSingleResult().toString();
  }

  /**
   * When handed two parts of a greeting, like, say, "{@code hello}"
   * and "{@code world}", stores a new {@link Greeting} entity in the
   * database appropriately.
   *
   * <p>I told you this was a dumb application.</p>
   *
   * @param firstPart the first part of the greeting; must not be
   * {@code null}
   *
   * @param secondPart the second part of the greeting; must not be
   * {@code null}
   *
   * @return the {@link String} representation of the resulting {@link
   * Greeting}'s identifier; never {@code null}
   *
   * @exception NullPointerException if {@code firstPart} or {@code
   * secondPart} was {@code null}
   *
   * @exception PersistenceException if the {@link EntityManager} blew
   * up
   */
  @POST
  @Path("{firstPart}")
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  @Transactional(TxType.REQUIRED)
  public String post(@PathParam("firstPart") final String firstPart,
                     final String secondPart) {
    Objects.requireNonNull(firstPart);
    Objects.requireNonNull(secondPart);
    assert this.entityManager != null;
    Greeting greeting = new Greeting(null, firstPart, secondPart);
    greeting = this.entityManager.merge(greeting);
    return String.valueOf(greeting.getId());
  }

}
