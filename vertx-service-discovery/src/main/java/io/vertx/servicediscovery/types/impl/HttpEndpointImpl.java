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

package io.vertx.servicediscovery.types.impl;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.spi.ServiceType;
import io.vertx.servicediscovery.types.AbstractServiceReference;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.HttpEndpointType;
import io.vertx.servicediscovery.types.HttpLocation;

import java.util.Objects;

import static io.vertx.servicediscovery.types.HttpEndpoint.TYPE;

/**
 * Implementation of {@link ServiceType} for HTTP endpoint (REST api).
 * Consumers receive a HTTP client configured with the host and port of the endpoint.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class HttpEndpointImpl implements HttpEndpointType, HttpEndpoint {

  @Override
  public String name() {
    return TYPE;
  }

  @Override
  public ServiceReference<HttpClient> get(Vertx vertx, ServiceDiscovery discovery, Record record, JsonObject configuration) {
    Objects.requireNonNull(vertx);
    Objects.requireNonNull(record);
    Objects.requireNonNull(discovery);
    return new HttpEndpointReference(vertx, discovery, record, configuration);
  }

  @Override
  public <X> X getObject(ServiceReference ref, Class<X> clazz) {
    return (X) getService(ref);
  }

  @Override
  public HttpClient getService(ServiceReference<HttpClient> ref) {
    return ref.get();
  }

  @Override
  public HttpClient cachedService(ServiceReference<HttpClient> ref) {
    return ref.cached();
  }

  /**
   * {@link ServiceReference} implementation for the HTTP endpoint.
   */
  private class HttpEndpointReference extends AbstractServiceReference<HttpClient> {

    private final HttpLocation location;
    private final JsonObject config;

    HttpEndpointReference(Vertx vertx, ServiceDiscovery discovery, Record record, JsonObject config) {
      super(vertx, discovery, record);
      this.config = config;
      this.location = new HttpLocation(record.getLocation());
    }


    /**
     * Gets a HTTP client to access the service.
     *
     * @return the HTTP client, configured to access the service
     */
    @Override
    public HttpClient retrieve() {
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

      return vertx.createHttpClient(options);
    }

    /**
     * Closes the client.
     */
    @Override
    public void close() {
      service.close();
    }
  }
}
