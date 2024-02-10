package com.realmone.owl.orm.generate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OntologyMeta {
    private String file;
    private String packageName;
    private String ontologyName;
}
