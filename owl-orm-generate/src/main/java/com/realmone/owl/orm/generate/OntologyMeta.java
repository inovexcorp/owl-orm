package com.realmone.owl.orm.generate;

import lombok.*;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OntologyMeta {
    private String file;
    private String packageName;
    private String ontologyName;
}
