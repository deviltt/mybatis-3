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
package org.apache.ibatis.scripting.xmltags;

/**
 * 对应的动态 SQL 节点是 <if> 节点
 *
 * @author Clinton Begin
 */
public class IfSqlNode implements SqlNode {
  // 该对象用于解析 <if> 节点的 test 表达式的值
  private final ExpressionEvaluator evaluator;
  // 记录 <if> 节点中 test 表达式
  private final String test;
  // 记录 <if> 节点的子节点，实际也就是 <if> 里面的 sql
  private final SqlNode contents;

  public IfSqlNode(SqlNode contents, String test) {
    this.test = test;
    this.contents = contents;
    this.evaluator = new ExpressionEvaluator();
  }

  @Override
  public boolean apply(DynamicContext context) {
    // 内部使用 OGNL 解析 test 表达式，看结果是否为 true
    if (evaluator.evaluateBoolean(test, context.getBindings())) {
      // 如果 test 表达式为 true，则执行子节点的 apply() 方法
      contents.apply(context);
      return true;
    }
    return false;
  }

}
