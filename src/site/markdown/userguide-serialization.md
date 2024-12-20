## User guide - Serialization

This part of the user guide provides information on serialization of beans.
See the [main user guide](userguide.html) for a higher level introduction.


## Serialization

The `org.joda.beans.ser` package provides support for serializing Joda-Beans via JSON, XML or MsgPack binary.
The main class is [JodaBeanSer](apidocs/org.joda.beans/org/joda/beans/ser/JodaBeanSer.html).
It contains the relevant settings and methods to access the serialization and deserialization.

The serializer makes use the meta-data in the bean to minimize the output size.
In effect, the Joda-Bean acts as a schema to interpret the data.

Two standard layouts are provided - COMPACT and PRETTY.
The compact layout has no whitespace, whereas the pretty layout uses indentation and new-lines.
Methods on `JodaBeanSer` allow for further customization.

### XML

```java
 // write to XML
 String xmlStr = JodaBeanSer.COMPACT.xmlWriter().write(bean);
 // read from XML
 MyBean bean = JodaBeanSer.COMPACT.xmlReader().read(xmlStr, MyBean.class);
```

### JSON

```java
 // write to JSON
 String jsonStr = JodaBeanSer.COMPACT.jsonWriter().write(bean);
 // read from JSON
 MyBean bean = JodaBeanSer.COMPACT.jsonReader().read(jsonStr, MyBean.class);
```

There is also a simple JSON reader/writer that does not expose Java types.

### Binary

For binary there are three different formats available:

* `binReader(STANDARD)` is the default and suitable for most uses.
* `binReader(REFERENCING)` deduplicates all beans it sees, which can be slow.
* `binReader(PACKED)` is the effective replacement for REFERENCING where callers choose which beans to deduplicate.

The classes to deduplicate can be controlled in the PACKED format using `JodaBeanSer.withBeanValueClasses()`.

```java
 // write to Binary
 var bytes = JodaBeanSer.COMPACT.binWriter(JodaBeanBinFormat.PACKED).write(bean);
 // read from Binary
 MyBean bean = JodaBeanSer.COMPACT.binReader().read(bytes, MyBean.class);
```

Note that COMPACT vs PRETTY makes no difference in binary mode.


## Supported types

The serialization mechanism supports implementations of `Bean`, `Collection`, `Map`, `Optional` and arrays.
Some formats also support `Iterable`.
If Guava is present, implementations of `Multimap`, `Multiset`, `Table`, `BiMap` and Guava's `Optional` are supported.
If Joda-Collect is present, implementations of `Grid` are supported.

The serialization format is generally stable over different versions of the API.
The following incompatibilities apply:

* The simple JSON format in v3.x alters two-dimensional primitive arrays to be written using
the natural format `[[1, 2], [2, 3]]` instead of the previous format `["1,2", "2,3"]`.
* The standard binary format in v3.x adjusts two-dimensional primitive arrays in a similar way to JSON.
* The binary formats in v3.x permit null keys in maps.


## Handling change

When deserializing, the Joda-Bean meta-data is used to parse the input JSON/XML/binary.
However, this causes a problem if the bean is refactored between serialization and deserialization.
To handle this, three mechanisms are provided.

Firstly, the `RenameHandler` class from Joda-Convert is used.
This allows type renames and enum constant renames to be handled.
The class is a singleton and must be configured at application startup.

Secondly, Joda-Beans provides the `SerDeserializer` mechanism.
A `SerDeserializer` class can be written for each refactored bean.
It supports multiple kinds of change, including property rename, property type change and
complex semantic changes. It is registered with `SerDeserializers` and called
directly as part of the parsing process.

Finally, the `PropertyDefinition` annotation has an "alias" attribute.
Setting that allows the old name of a property to be retained when a property is renamed.


## Leniency

The default deserialization mechanism is relatively strict.
If the input contains an unknown property then an error will be thrown.
Similarly, if a Java type is not handled by `SerTypeMapper` then an exception is thrown.

There is an alternative mode that is more lenient:

    JodaBeanSer s = JodaBeanSer.COMPACT.withDeserializers(SerDeserializers.LENIENT);

This mode will ignore unknown properties received for a bean.
If a Java type cannot be found, lenient mode will attempt to fallback to an alternative type, such as `String`.
Together these help solve problems when integrating multiple projects/services using Joda-Beans.


## Smart reader

When reading data, it is possible to determine the type of the serialized data dynamically.
Use `JodaBeanSer.COMPACT.smartReader()` to read an input stream that contains a JSON, XML or binary serialized bean.


## Links

Return to the [main user guide](userguide.html).
