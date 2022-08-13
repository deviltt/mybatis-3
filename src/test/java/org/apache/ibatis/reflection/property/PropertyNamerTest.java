package org.apache.ibatis.reflection.property;

import org.junit.Assert;
import org.junit.Test;

public class PropertyNamerTest {
  @Test
  public void methodToPropertyTest1(){
    Assert.assertEquals("UUser", PropertyNamer.methodToProperty("getUUser"));
  }

  @Test
  public void methodToPropertyTest2(){
    Assert.assertEquals("user", PropertyNamer.methodToProperty("getUser"));
  }

  @Test
  public void methodToPropertyTest3(){
    Assert.assertEquals("uuser", PropertyNamer.methodToProperty("getUuser"));
  }

  @Test
  public void methodToPropertyTest4(){
    Assert.assertEquals("u", PropertyNamer.methodToProperty("getU"));
  }
}
