package com.fenquen.rdelay.server.test;

import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TestBundle {
    @Test
    public void testEnum() {
        String a = "{\"season\":\"SPRING\"}";
        Description description = JSON.parseObject(a, Description.class);
        System.out.println(description.season.name);

    }

    static class Description {
        public Season season;
    }

    public enum Season {

        SPRING("spring");

        public final String name;

        Season(String name) {
            this.name = name;
        }
    }

    @Test
    public void testAbstract() throws Exception {
        //  Object object = new ObjectInputStream(new FileInputStream("d:/car.bin")).readObject();
        //  System.out.println(object);


        Car car = new Toyota();
        System.out.println(car.getMyClassName());
    }


    static class Car implements Serializable {

        public String name;

        void Run() {
            System.out.println(this);
        }

        public String getMyClassName() {
            return getClass().getName();
        }
    }

    static class Toyota extends Car {
        public String name = "Toyota";

        @Override
        void Run() {
            System.out.println(name + "run");
        }
    }

    static class Honda extends TestBundle.Car {
        public String name;
        public void print(String a, String b) {
            System.out.println(a + b);
        }

        @Override
        void Run() {
            super.Run();
        }
    }

    @Test
    public void testReflectMethod() throws Exception {
        Honda honda = new Honda();

        Field[] fields = honda.getClass().getFields();

        System.out.println(fields);
    }
}
