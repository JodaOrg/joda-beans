## User guide - Serialization

This part of the user guide provides information on serialization of beans.
See the [main user guide](userguide.html) for a higher level introduction.


## Serialization

The `org.joda.beans.ser` package provides support for serializing Joda-Beans via JSON, XML or MsgPack binary.
The main class is [JodaBeanSer](apidocs/org/joda/beans/ser/JodaBeanSer.html).
It contains the relevant settings and methods to access the serialization and deserialization.

```
 // write to XML
 JodaBeanSer.COMPACT.xmlWriter().write(bean);
 // read from XML
 MyBean bean = JodaBeanSer.COMPACT.xmlReader().read(xmlStr, MyBean.class);
```

Two standard layouts are provided - COMPACT and PRETTY.
The compact layout has no whitespace, whereas the pretty layout uses indentation and new-lines.
Methods on `JodaBeanSer` allow for further customization.

For binary, replace `xmlWriter()` by `binWriter()` and
`xmlReader()` by `binReader()`.

For JSON, replace `xmlWriter()` by `jsonWriter()` and
`xmlReader()` by `jsonReader()`.

There is also a simple JSON reader/writer that does not expose Java types,
and a simple Map-base reader/writer for interoperation with other libraries.

The serializer makes use the meta-data in the bean to minimize the output size.
In effect, the Joda-Bean acts as a schema to interpret the data.


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


## Links

Return to the [main user guide](userguide.html).
