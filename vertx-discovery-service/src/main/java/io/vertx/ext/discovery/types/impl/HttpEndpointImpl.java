/*
 * Copyright (c) 2011-2016 The original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *      The Eclipse Public License is available at
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.discovery.types.impl;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.discovery.Record;
import io.vertx.ext.discovery.ServiceReference;
import io.vertx.ext.discovery.spi.ServiceType;
import io.vertx.ext.discovery.types.HttpEndpoint;
import io.vertx.ext.discovery.types.HttpLocation;

/**
 * Implementation of {@link ServiceType} for HTTP endpoint (REST api).
 * Consumers receive a HTTP client configured with the host and port of the endpoint.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class HttpEndpointImpl implements HttpEndpoint {

  @Override
  public String name() {
    return TYPE;
  }

  @Override
  public ServiceReference get(Vertx vertx, Record record, JsonObject configuration) {
    return new HttpEndpointReference(vertx, record, configuration);
  }

  /**
   * {@link ServiceReference} implementation for the HTTP endpoint.
   */
  private class HttpEndpointReference implements ServiceReference {

    private final Vertx vertx;
    private final HttpLocation location;
    private final Record record;
    private final JsonObject config;
    private HttpClient client;

    HttpEndpointReference(Vertx vertx, Record record, JsonObject config) {
      this.vertx = vertx;
      this.config = config;
      this.location = new HttpLocation(record.getLocation());
      this.record = record;
    }

    /**
     * @return the service record.
     */
    @Override
    public Record record() {
      return record;
    }

    /**
     * Gets a HTTP client to access the service.
     *
     * @param <T> {@link HttpClient}
     * @return the HTTP client, configured to access the service
     */
    @Override
    public synchronized <T> T get() {
      if (client != null) {
        return (T) client;
      } else {
        HttpClientOptions options;
        if (config != null) {
          options = new HttpClientOptions(config);
        } else {
          options = new HttpClientOptions();
        }
         options.setDefaultPort(location.getPort()).setDefaultHost(location.getHost());
        if (location.isSsl()) {
          options.setSsl(true);
        }

        client = vertx.createHttpClient(options);
        return (T) client;
      }
    }

    /**
     * Closes the client.
     */
    @Override
    public synchronized void release() {
      client.close();
      client = null;
    }
  }
}
