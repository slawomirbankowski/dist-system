package com.distsystem.test.custom.utils;

import com.distsystem.api.DaoModel;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

public class GenerateMethodsTest {
    private static final Logger log = LoggerFactory.getLogger(GenerateMethodsTest.class);

    @Test
    public void modelTest() {
        log.info("START ------ model test");
        //DaoModel.allModels.forEach(m -> {
        //    System.out.println(m.generateInsertMethod());
        //    System.out.println(m.generateToMapMethod());
       // });
        Arrays.stream(DaoModel.class.getDeclaredFields())
                .filter(f -> f.getType().getSimpleName().equals("DaoModel") && Modifier.isStatic(f.getModifiers()))
                .forEach(f -> {
            try {
                System.out.println("Field, name=" + f.getName() + ", m=" + f.getModifiers() + ", t=" + f.getType().getName() + ", TABLE=" + ((DaoModel)f.get(null)).getTableName());
                Optional.of(((DaoModel)f.get(null)).getTableName());
            } catch (Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
                Optional.empty();
            }
        });
        log.info("END-- ---");
    }
}
