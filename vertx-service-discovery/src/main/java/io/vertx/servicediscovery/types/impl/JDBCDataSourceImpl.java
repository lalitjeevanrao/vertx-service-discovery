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
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.types.AbstractServiceReference;
import io.vertx.servicediscovery.types.JDBCDataSource;
import io.vertx.servicediscovery.types.JDBCDataSourceType;

import java.util.Objects;

/**
 * The implementation of {@link JDBCDataSource}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class JDBCDataSourceImpl implements JDBCDataSourceType {
  @Override
  public String name() {
    return JDBCDataSource.TYPE;
  }

  @Override
  public ServiceReference<JDBCClient> get(Vertx vertx, ServiceDiscovery discovery, Record record, JsonObject configuration) {
    Objects.requireNonNull(vertx);
    Objects.requireNonNull(record);
    Objects.requireNonNull(discovery);
    return new JdbcServiceReference(vertx, discovery, record, configuration);
  }

  @Override
  public <X> X getObject(ServiceReference ref, Class<X> clazz) {
    return (X) getService(ref);
  }

  @Override
  public JDBCClient getService(ServiceReference<JDBCClient> ref) {
    return ref.get();
  }

  @Override
  public JDBCClient cachedService(ServiceReference<JDBCClient> ref) {
    return ref.cached();
  }

  /**
   * A reference on a JDBC data source. When retrieved it provides a {@link JDBCClient}. The _shared_ aspect of the
   * client depends on the {@code shared} flag put in the record's metadata (non shared by default).
   */
  private class JdbcServiceReference extends AbstractServiceReference<JDBCClient> {
    private final JsonObject config;

    JdbcServiceReference(Vertx vertx, ServiceDiscovery discovery, Record record, JsonObject config) {
      super(vertx, discovery, record);
      this.config = config;
    }

    @Override
    public JDBCClient retrieve() {
      JsonObject result = record().getMetadata().copy();
      result.mergeIn(record().getLocation());

      if (config != null) {
        result.mergeIn(config);
      }

      if (result.getBoolean("shared", false)) {
        return JDBCClient.createShared(vertx, result);
      } else {
        return JDBCClient.createNonShared(vertx, result);
      }
    }

    @Override
    protected void close() {
      service.close();
    }
  }
}
