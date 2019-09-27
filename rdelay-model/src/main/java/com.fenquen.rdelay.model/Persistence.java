package com.fenquen.rdelay.model;

public interface Persistence {
    DbMetaData getDbMetaData();

    /**
     * meaningful only when this model need persistence
     */
    public enum DbMetaData {
        TASK("TASK"), EXECUTION_RESP("EXECUTION_RESP");

        public String tableName;

        private DbMetaData(String tableName) {
            this.tableName = tableName;
        }
    }

}