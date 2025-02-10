# OWL ORM API
This project contians the core interfaces, annotations, and exceptions that are
central to the usage of the OWL ORM API.

## Annotations
The following annotations make up the core of the API.  The provide metadata for the proxy
to understand how to interact with the RDF4k model data underlying your proxied instance.

### @Type
Marks a given class/interface as representing a owl:Class.

### @Property
Marks a given method on the interface as being an accessor for an Object or Data Type property
in an ontology. Contains metadata to help inform the proxy on how to interact with the model.

## Types
Another part of the OWL ORM API is how Data Type Properties are serialized to Values within
the RDF4j model statements.  This part of the API provides a set of converters that allow
for seamless translation between RDF and Java objects.

## Thing & ThingFactory
These are the front door/façade on which the API is worked by users.  

The `Thing` interface is the root of all classes that are represented by the API. It provides the baseline methods
that OWL ORM objects will support -- in addition to the properties ascribed to them in the ontology.

The `ThingFactory` implementation provides a way to overlay a proxy on top of the interfaces that
describe your class, and pass you a native POJO with which to work.