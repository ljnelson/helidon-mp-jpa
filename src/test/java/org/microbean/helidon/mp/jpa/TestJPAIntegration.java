/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2019 microBean.
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
package org.microbean.helidon.mp.jpa;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URL;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.spi.CDI;

import io.helidon.microprofile.server.Server;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestJPAIntegration {

  public TestJPAIntegration() {
    super();
  }

  @Test
  public void testIt() throws Exception {
    final Server server = Server.create().start();
    assertNotNull(server);

    final int port = server.port();
    
    final URL url = new URL("http://127.0.0.1:" + port + "/hello");
    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
      assertEquals("world", reader.readLine());
    }

    // This stops the server.
    ((SeContainer)CDI.current()).close();
  }
  
}
