package com.fenquen.rdelay.model;

public abstract class ModelBase {
    public abstract DbMetaData getDbMetaData();

    public enum DbMetaData {
        TASK("task"), EXECUTION_RESP("execution_resp");

        public String tableName;

        private DbMetaData(String tableName) {
            this.tableName = tableName;
        }
    }
}
