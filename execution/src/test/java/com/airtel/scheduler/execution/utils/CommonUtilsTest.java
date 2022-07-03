package com.airtel.scheduler.execution.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class CommonUtilsTest {

    Map<String, Object> requestMap;

    @Before
    public void init() {
        requestMap = new HashMap<>();
        requestMap.put("TestKey", "TestValue");
    }

    @Test
    public void getGson() {
        Assert.assertTrue(CommonUtils.getGson() instanceof Gson);
    }

    @Test
    public void getJson() {
        Assert.assertEquals("{\"TestKey\":\"TestValue\"}", CommonUtils.getJson(requestMap));
    }

    @Test
    public void getJsonFromMapper() throws JsonProcessingException {
        Assert.assertEquals("{\"TestKey\":\"TestValue\"}", CommonUtils.getJsonFromMapper(requestMap));
    }

    @Test
    public void isNullOrEmptyMap() {
        Assert.assertFalse(CommonUtils.isNullOrEmptyMap(requestMap));
    }

    @Test
    public void isEmptyListTest() {
        List<String> stringList = new ArrayList<>();
        Assert.assertTrue(CommonUtils.isEmptyList(stringList));
    }

    @Test
    public void getCurrentDateTimeTest() {
        Assert.assertNotNull(CommonUtils.getCurrentDateTime());
    }
}