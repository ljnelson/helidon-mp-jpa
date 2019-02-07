/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2019 Laird Nelson.
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.spi.CDI;

import io.helidon.microprofile.server.Server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestJPAIntegration {

  private Server server;

  private URL baseUrl;
  
  public TestJPAIntegration() {
    super();
  }

  @Before
  public void startServer() throws MalformedURLException {
    this.server = Server.create().start();
    this.baseUrl = new URL("http://127.0.0.1:" + server.port());
  }

  @After
  public void stopServer() {
    this.server.stop();
    this.baseUrl = null;
  }

  @Test
  public void testGet() throws Exception {
    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(this.baseUrl, "/hello").openStream(), "UTF-8"))) {
      assertEquals("world", reader.readLine());
    }
  }
  
  @Test
  public void testPost() throws Exception {
    final HttpURLConnection c = (HttpURLConnection)new URL(this.baseUrl, "/hello").openConnection();
    assertNotNull(c);
    c.setDoOutput(true);
    c.setRequestProperty("Content-Type", "text/plain");
    c.setRequestMethod("POST");
    final byte[] helloBytes = "hello".getBytes("UTF8");
    assertNotNull(helloBytes);
    c.setRequestProperty("Content-Length", String.valueOf(helloBytes.length));
    try(final OutputStream stream = new BufferedOutputStream(c.getOutputStream())) {
      assertNotNull(stream);
      stream.write(helloBytes, 0, helloBytes.length);
    }
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (final InputStream inputStream = new BufferedInputStream(c.getInputStream())) {
      assertNotNull(inputStream);
      int bytesRead;      
      final byte[] bytes = new byte[4096];
      while ((bytesRead = inputStream.read(bytes, 0, bytes.length)) != -1) {
        baos.write(bytes, 0, bytesRead);
      }
    }
    assertEquals("1", baos.toString("UTF8"));
  }
  
}
