package com.realmone.owl.orm.generate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class OntologyMeta {
    private String file;
    private String packageName;
    private String ontologyName;
}
