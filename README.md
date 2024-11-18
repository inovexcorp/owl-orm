# OWL ORM
![OWL ORM Logo](owl-orm.png)
This project provides a facade by which to work with native Java POJOs that operate on top of a RDF4j model based
upon an OWL ontology.

## Components

### OWL ORM API

This project defines the interfaces and upper level API by which a user should interact with this API.

### OWL ORM Engine

This project implements the core API by proxying interfaces and wrapping specific types of method calls around
base core logic to interact with the RDF4j model.

### OWL ORM Types
[README.md](README.md)
This project contains the TypeConverter logic that allows us to move between Datatype property Values and native
Java objects/primitives.

#### Extending TypeConverter API

TODO - how to add/register your own TypeConverter implementations.

### OWL ORM Maven Plugin

This API allows you to hook into your maven build to generate our facade POJO classes based directly from OWL 
ontologies.

#### How to Use

TODO - How to set up the Maven plugin!

## OSGi 

This API and implementation supports the OSGi runtime. TODO - how to use!

## Building

TODO - How to build this project...

## Contributing

TODO - How to contribute...
