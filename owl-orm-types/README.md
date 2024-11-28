# Value Conversion
This project contains the baseline set of Value Converters that will support the
core of the OWL ORM API.

## Value Converter
This interface describes a service unit that will translate between native Java objects
and their corresponding representations in the RDF graph (RDF4j Values).

## Registry
The ValueConverterRegistry interface describes a centralized place where these types
of entities are kept accessible to the API. This allows the API to seamlessly translate to 
and from RDF for Data Type Properties.