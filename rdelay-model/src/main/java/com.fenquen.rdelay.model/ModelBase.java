package com.fenquen.rdelay.model;

public abstract class ModelBase {
    public abstract DbMetaData getDbMetaData();

    public enum DbMetaData {
        TASK("TASK"), EXECUTION_RESP("EXECUTION_RESP");

        public String tableName;

        private DbMetaData(String tableName) {
            this.tableName = tableName;
        }
    }
}
