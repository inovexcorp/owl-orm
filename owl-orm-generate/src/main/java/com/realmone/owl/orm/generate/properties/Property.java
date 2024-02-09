package com.realmone.owl.orm.generate.properties;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.rdf4j.model.Resource;

@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Property {
    private JCodeModel jCodeModel;
    private Resource resource;
    private String javaName;
    private boolean functional;
    private JClass targetRange;
}
