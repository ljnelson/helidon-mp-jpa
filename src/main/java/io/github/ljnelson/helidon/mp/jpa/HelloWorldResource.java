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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("hello")
@ApplicationScoped // see https://github.com/oracle/helidon/issues/363
public class HelloWorldResource {  

  @PersistenceContext(unitName = "test")
  private EntityManager entityManager;
  
  public HelloWorldResource() {
    super();
  }

  @PostConstruct
  private void postConstruct() {
    this.entityManager.merge(new Greeting(1L, "world"));
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Transactional(TxType.REQUIRED)
  public String getDefaultMessage() {
    assert entityManager != null;
    return entityManager.find(Greeting.class, Long.valueOf(1L)).toString();
  }

}
