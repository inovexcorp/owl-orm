package com.realmone.owl.orm.generate;

import com.realmone.owl.orm.OrmException;

public class OrmGenerationException extends OrmException {
    
    public OrmGenerationException(String msg) {
        super(msg);
    }

    public OrmGenerationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
