package com.distsystem.test;

import com.distsystem.api.CacheObjectRequest;
import com.distsystem.api.DaoModel;
import com.distsystem.api.DaoTable;
import com.distsystem.api.dtos.DistAgentDaoRow;
import com.distsystem.api.dtos.DistAgentRegisterRow;
import com.distsystem.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModelTest {
    private static final Logger log = LoggerFactory.getLogger(ModelTest.class);

    @Test
    public void modelTest() {
        log.info("START ------ model test");


        var cl = DistAgentDaoRow.class;

        Arrays.stream(cl.getDeclaredFields()).forEach(f -> {
            log.info("-----> FIELD:" + f.getName());
        });


        DaoTable dt = cl.getAnnotation(DaoTable.class);

        log.info("-----> annotations:" + cl.getAnnotations().length);
        log.info("-----> getName:" + cl.getName());
        log.info("-----> getSimpleName:" + cl.getSimpleName());
        log.info("-----> annotation:" + dt);
        var model = DaoModel.authAccount;

        log.info("START ------ getName: " + model.getModelClass().getName());
        log.info("START ------ getSimpleName: " + model.getModelClass().getSimpleName());
        log.info("START ------ getPackageName: " + model.getModelClass().getPackageName());
        log.info("START ------ getMethods().length: " + model.getModelClass().getMethods().length);
        log.info("START ------ getDeclaredMethods().length: " + model.getModelClass().getDeclaredMethods().length);
        Arrays.stream(model.getModelClass().getMethods()).forEach(m -> {
            log.info("METHOD:" + m.getName());
        });
        Arrays.stream(model.getModelClass().getDeclaredMethods()).forEach(m -> {
            log.info("DECLARED METHOD:" + m.getName());
        });
        Arrays.stream(model.getModelClass().getDeclaredFields()).forEach(m -> {
            log.info("FIELD:" + m.getName() + ", type: " + m.getType().getName());
        });

        log.info("getTableName: " + model.getTableName());
        log.info("getKeyName: " + model.getKeyName());
        log.info("getCreateTableQuery: " + model.getCreateTableQuery());
        log.info("getCreateTableIndexQuery: " + model.getCreateTableIndexQuery());
        log.info("getSelectAllQuery: " + model.getSelectAllQuery());
        log.info("getSelectAllDescQuery: " + model.getSelectAllDescQuery());
        log.info("getSelectLimitQuery(10): " + model.getSelectLimitQuery(10));
        log.info("getDeleteOldQuery: " + model.getDeleteOldQuery());
        log.info("getSelectForNameQuery: " + model.getSelectForNameQuery());
        log.info("getInsertQuery: " + model.getInsertQuery());
        log.info("getColumnList: " + model.getColumnList());

        var row = model.convert(Map.of("agentGuid", "123", "daoKey", "key", "daoType", "type"));
        log.info("ROW: " + row.toJson());
        log.info("END-- ---");
    }
}
