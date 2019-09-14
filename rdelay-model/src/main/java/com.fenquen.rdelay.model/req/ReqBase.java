package com.fenquen.rdelay.model.req;

import com.fenquen.rdelay.model.annotation.Nullable;

import java.lang.reflect.Field;

public abstract class ReqBase {
    /**
     * determine the uri on rdelay-server this req matches
     *
     * @return
     */
    public abstract String getRequestUri();

    public abstract void verifyFields() throws Exception;
}
