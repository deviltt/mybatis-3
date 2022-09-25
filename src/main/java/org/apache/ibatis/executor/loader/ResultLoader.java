/*
 *    Copyright 2009-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.executor.loader;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.ResultExtractor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;

/**
 * @author Clinton Begin
 */
public class ResultLoader {

  protected final Configuration configuration;
  protected final Executor executor;
  protected final MappedStatement mappedStatement;
  protected final Object parameterObject;
  protected final Class<?> targetType;
  protected final ObjectFactory objectFactory;
  protected final CacheKey cacheKey;
  protected final BoundSql boundSql;

  /**
   * 负责将延迟加载得到的结果对象转换成 targetType 类型的对象
   */
  protected final ResultExtractor resultExtractor;
  protected final long creatorThreadId;

  protected boolean loaded;
  protected Object resultObject;

  public ResultLoader(Configuration config, Executor executor, MappedStatement mappedStatement, Object parameterObject, Class<?> targetType, CacheKey cacheKey, BoundSql boundSql) {
    this.configuration = config;
    this.executor = executor;
    this.mappedStatement = mappedStatement;
    this.parameterObject = parameterObject;
    this.targetType = targetType;
    this.objectFactory = configuration.getObjectFactory();
    this.cacheKey = cacheKey;
    this.boundSql = boundSql;
    this.resultExtractor = new ResultExtractor(configuration, objectFactory);
    this.creatorThreadId = Thread.currentThread().getId();
  }

  /**
   * 功能：通过 Executor 执行 ResultLoader 中记录的 SQL 语句并返回相应的延迟加载对象，
   * 并将加载得到的嵌套对象设置到外层对象中
   *
   * @return
   * @throws SQLException
   */
  public Object loadResult() throws SQLException {
    // 执行延迟加载，得到结果对象，并以 List 类型返回
    List<Object> list = selectList();
    // 将 list 集合转换成 targetType 指定类型的对象
    resultObject = resultExtractor.extractObjectFromList(list, targetType);
    return resultObject;
  }

  private <E> List<E> selectList() throws SQLException {
    // 记录执行延迟加载的 Executor 对象
    Executor localExecutor = executor;

    // 检测当前线程是否为创建 ResultLoader 对象的线程、检测 localExecutor 是否关闭
    if (Thread.currentThread().getId() != this.creatorThreadId || localExecutor.isClosed()) {
      // 创建新的 Executor 对象来执行延迟加载操作
      localExecutor = newExecutor();
    }
    try {
      // 执行查询操作，得到延迟加载对象
      // 一个可以执行 SQL 语句并将结果集映射成结果对象的黑盒
      return localExecutor.query(mappedStatement, parameterObject, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER, cacheKey, boundSql);
    } finally {
      if (localExecutor != executor) {
        localExecutor.close(false);
      }
    }
  }

  private Executor newExecutor() {
    final Environment environment = configuration.getEnvironment();
    if (environment == null) {
      throw new ExecutorException("ResultLoader could not load lazily.  Environment was not configured.");
    }
    final DataSource ds = environment.getDataSource();
    if (ds == null) {
      throw new ExecutorException("ResultLoader could not load lazily.  DataSource was not configured.");
    }
    final TransactionFactory transactionFactory = environment.getTransactionFactory();
    final Transaction tx = transactionFactory.newTransaction(ds, null, false);
    return configuration.newExecutor(tx, ExecutorType.SIMPLE);
  }

  public boolean wasNull() {
    return resultObject == null;
  }

}
