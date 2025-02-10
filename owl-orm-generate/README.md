# OWL ORM Generation
This project contains the logic that will generate OWL ORM API interfaces based upon
your provided OWL ontology.

## Source Generation
The `com.realmone.owl.orm.generate.SourceGenerator` class contains the front door for 
executing the generation.  It uses the other classes to build a closure representing
the ontology(ies) you want to generate for and will use codemodel to create an internal
representation of the classes/packages it will generate.  Once complete, it will write
the resulting source to the specified location.