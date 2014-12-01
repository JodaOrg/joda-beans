## User guide

Joda-Beans is a small library that adds properties to the Java programming language.
It consists of these main parts:

* a set of interfaces and classes that define a property
* a source code generator
* serialization and deserialization via JSON, XML and MsgPack binary
* integration with other technologies

The main rationale for the project is to raise the abstraction level of Java development.
Most mainstream languages have some kind of support for properties, unfortunately Java does not.
The benefits of the Joda-Beans approach mostly apply when using the code generator:

* easy to write common utilities acting on arbitrary beans without using reflection
* no need to manually write getters, setters, equals, hashCode, toString
* simple way to create immutable beans
* effective and simple round-trip serialization and deserialization


## Bean and Property model

The Joda-Bean API is based around a model of properties in the Java programming language.

All beans implement the [Bean](apidocs/org/joda/beans/Bean.html) interface.
This defines a minimal API for all beans, providing the ability to obtain the property names and a named property.
The interface also provides access to the *meta-bean* (see below).

The bean provides access to instances of the [Property](apidocs/org/joda/beans/Property.html) interface for each property.
Each property object holds a reference to the bean that the property is part of.
This means that there are simple methods to get and set the value of the property which directly affect the underlying bean.
The property also provides methods to get the property name, the underlying bean and the *meta-property* (see below).

```
  Bean person = new Person();
  Property<String> surnameProperty = bean.property("surname");
  String surname = surnameProperty.get();
```

The [MetaBean](apidocs/org/joda/beans/MetaBean.html) interface defines
the meta-bean. The "meta" name indicates that this is a higher level description of the bean itself.
In fact, the meta-bean is the equivalent to `java.lang.Class` for beans - there is only one meta-bean object
for all instances of the bean.
The meta-bean API provides access to the name of the bean, it's type, a builder and the meta-properties.

The [MetaProperty](apidocs/org/joda/beans/MetaProperty.html) interface
is the main definition of a property.
It is at the "meta" level, describing the property without holding a references to any specific instance of a bean.
As such, it is effectively the equivalent to `java.lang.reflect.Field` for a single property on a bean - there is only
one meta-property object for all instances of the property.
The meta-property API provides access to the name of the property, it's type, any declared annotations, whether it is read-write or read-only
as well as methods to get and set the value which require the bean to be passed in, just like `java.lang.reflect.Field`.

```
  Bean person = new Person();
  MetaProperty<String> surnameMetaProperty = bean.metaBean().metaProperty("surname");
  String surname = surnameMetaProperty.get(person);
```


### Use in an application

Applications can use Joda-Beans properties in many ways, revolving around passing a single property of an object to another method.
For example, it would be possible to write a validating method that checked the validity of a single property, but perhaps
registered errors on the underlying bean.


### Use in a framework

Joda-Beans is designed to work well with frameworks which need dynamic access to the properties of a bean.
It is recommended when working with a framework to primarily use the meta-bean and associated meta-properties.
This is because they tend to be singletons and more efficient than using the property objects.
The meta-property API is also more comprehensive than the property API.

Beans are normal Java objects and can be created from scratch using constructors.
Frameworks may wish to use the [BeanBuilder](apidocs/org/joda/beans/BeanBuilder.html) interface
which provides a way to build the bean.
The builder is obtained from the meta-bean.
Each property is then set before calling `build`.

A selection of utilities is available on [JodaBeanUtils](apidocs/org/joda/beans/JodaBeanUtils.html).
This includes the ability to lookup a meta-bean from a `java.lang.Class`, access to the Joda-Convert string
converter and methods to extract the types of lists and maps using reflection on generics.

The access to the field-level annotations provided by the meta-property is also useful to framework writers.


### Joda-Convert integration

A Joda-Beans meta-property provides the ability to set a property from a string.
The conversion between the string and the type of the property, such as `int` or `URI`, is handled by
[Joda-Convert](http://www.joda.org/joda-convert/). The converter can be customised and is exposed by `JodaBeanUtils`.


### Utilities

The `JodaBeanUtils` contains a number of additional utilities.

The Java bytecode format contains more details about generic types than is often realised.
The `JodaBeanUtils` methods `collectionType()`, `mapKeyType()` and
`mapValueType()` allow the generic parameter type of a property to be extracted.
Thus an application can tell that a property is a list of strings, not just a list.

The `JodaBeanUtils` method `beanIterator()` provides a simple mechanism
to iterate over all the beans within a bean. The iteration is depth-first, and handles collections
and collections within collections.


### Chained access

Joda-Beans provides the ability to chain properties together using
[ChainedBeanQuery](apidocs/org/joda/beans/query/ChainedBeanQuery.html).
This is an immutable class that allows a list of "queries" to be passed in.

Each query is an instance of [BeanQuery](apidocs/org/joda/beans/BeanQuery.html).
All meta-properties implement this interface, thus they can be passed directly to `ChainedBeanQuery`.

This feature allows the value of a nested property to be queried from the parent bean.
For example, these two code sequences are equivalent - obviously the second would only be used within a framework
where the query setup was separate from the actual query.

```
 String city = document.getPerson().getAddress().getCity()
 
 ChainedBeanQuery<String> query = ChainedBeanQuery.of(Document.meta().person(), Person.meta().address(), Address.meta().city());
 String city = query.get(document);
```


## Code generator

It is possible to write a Joda-Bean by hand - they are just normal Java interfaces defining an API for JavaBeans.
There are some base classes that assist as well as some standard map-based implementations, notably
[FlexiBean](apidocs/org/joda/beans/impl/flexi/FlexiBean.html).
It is more common to use the code generator, however the generator is optional.

A code generated Joda-Bean minimises the amount of code that the developer has to write.
The developer simply creates an outline class and adds the properties (fields).
The code generator then produces all the getters and setters, plus additional helper methods.
See this [basic Person class](https://github.com/JodaOrg/joda-beans/blob/v0.8/src/test/java/org/joda/beans/gen/SimplePerson.java#L43)
for an example of the generated code.

The code generator must be invoked before compilation as it generates Java source code.
It cannot operate at runtime, as the getters and setters that are generated must be available for developers
writing the main part of the application.

The code generator operates by creating a portion of the source file marked by "AUTOGENERATED START" and "AUTOGENERATED END" tags
(when first run, if the tags are not present, it will create the block at the end of the file).
The generator only ever changes code within the marked block, apart from imports which it inserts if it determines they are necessary.
Limiting the generation to a fixed part of the file means that developers can customise the rest of the Java file in any
way that is desired, such as adding constructors and functional methods.

The generator has the following requirements:

* The class must be annotated with [BeanDefinition](apidocs/org/joda/beans/BeanDefinition.html)
* The class must implement the [Bean](apidocs/org/joda/beans/Bean.html) interface, or extend a class that does.
It used to be mandatory to subclass [DirectBean](apidocs/org/joda/beans/impl/direct/DirectBean.html) but that is no longer recommended
* The class must be a normal top-level class, nested/inner classes and multiple classes per file are not supported
* Each property field must be annotated with [PropertyDefinition](apidocs/org/joda/beans/PropertyDefinition.html)
* Each property field must be be private
* The Javadoc of each property field should normally start with the word "The".

It is possible to declare a property without it having a matching field.
To do this, annotate the getter with [DerivedProperty](apidocs/org/joda/beans/DerivedProperty.html).
Apart from the absence of a field, a derived property is very similar to a normal read-only property.

The command line takes the following arguments:

```
  Usage java org.joda.beans.gen.BeanCodeGen [file]
  Options
    -R                process all files recursively, default false
    -indent=tab       use a tab for indenting, default 4 spaces
    -indent=[n]       use n spaces for indenting, default 4
    -prefix=[p]       field prefix of p should be removed, no default
    -verbose=[v]      output logging with verbosity from 0 to 3, default 1
    -nowrite          output messages rather than writing, default is to write
```

The prefix is useful for teams that have a coding standard where fields must be prefixed, such as by an underscore or a letter.
Specifying it to the generator allows that prefix to be stripped before generating the property name.

The code generator works by reading the source file and parsing it to memory.
The parser is deliberately simplistic and will reject complex files or unusual code formatting styles.
The generator processes the annotated properties and replaces the auto-generated section of the file in memory.
It then compares the newly generated file with the original and only if they are different is the new file written to the file system.
The generator will make use of property comments and will respect and propagate deprecated annotations.
It is reasonably intelligent - final fields will become read-only properties, most collections and maps are sensibly handled
and booleans generate "is" methods rather than "get" methods. Simple generics are also handled.

The generator must be invoked every time that the source file is changed.
Failure to do so (such as by using an IDE refactoring) could leave the auto-generated block in an invalid state.
Normally the generator will be invoked from the IDE or a tool like Apache Ant.

The code generator only parses and outputs based on a single source Java file.
It does not require the file to compile and never knows that "String" actually means "java.lang.String".
In other words, the type system in the generator is totally dumb and based solely on the short simple class name.

A [Maven plugin](https://github.com/JodaOrg/joda-beans-maven-plugin) is available to generate the beans.


### Code generator customisation

The generator has a limited set of customisations to the core part of the generation.

The `PropertyDefinition` has attributes that allow the getter and setter generation to be controlled.
The main use cases are to prevent generation by specifying "manual".
The developer can then write their own method with whatever special logic is required.

The `PropertyDefinition` also has an attribute to specify validation.
The standard values are "notNull" and "notEmpty", which are implemented in `JodaBeanUtils`.
Developers may specify any text in the attribute however, and it is simply inserted into the output.
Thus a validation attribute of "Validate.noNulls" could be used to connect up to Commons Lang.


## Immutable beans

Code generated beans may also be immutable.

All fields in an immutable bean must be final.
It is recommended that immutable beans are final,
do not extend any bean class and directly implement `ImmutableBean`.

```
 @BeanDefinition
 public final class Foo implements ImmutableBean {
   // code generated immutable bean with public builder
 }
```

It is possible for an immutable bean to be non-final.
Any subclass should logically also be immutable, but must set the
`BeanDefinition` annotation value `hierarchy` to "immutable":

```
 // superclass
 @BeanDefinition
 public abstract class SuperFoo implements ImmutableBean {
   // code generation will enable an immutable subclass
 }
 
 // subclass
 @BeanDefinition(hierarchy = "immutable")
 public final class Foo extends SuperFoo {
   // code generation will connect to superclass
   // note that there is NO 'implements ImmutableBean' on the subclass
 }
```

Immutable beans do not have set methods.
Instead, a public builder class will be generated with methods to setup an instance.
This supports code based manipulation, using the `toBuilder()` method.
The scope of the builder may be controlled in the bean definition:

```
 @BeanDefinition(builderScope = "private")
 public final class Bar implements ImmutableBean {
   // code generated immutable bean with private builder
 }
```

It is also possible for a bean to be partially final.
In this case, the bean will implement `Bean` rather than `ImmutableBean`.
To get the correct behaviour, the `BeanDefinition` annotation value
`builderScope` must be set to "public".
Partially final beans can extend one another, but must all declare the builder scope.

The rules as to what works and what does not are complex.
It is recommended to keep is simple, and only have normal mutable beans or final immutable beans.


### Immutable bean customisation

Code generated immutable beans can be customized as follows.

An immutable bean can be configured to provide additional cross-property validation.
In most cases the per-property validation attribute is sufficient.
When cross-property validation is needed, this technique can be used.
Simply declare a private void method taking no arguments annotated with '@ImutableValidator'.

```
 @ImutableValidator
 private void validate() {
   // validate the instance variables of the bean
 }
```

An immutable bean can be configured to apply default property values.
In most cases this is not necessary, but if the bean has lots of non-null properties it may be
desirable to have some default values.
Simply declare a private static void method taking one 'Builder' argument annotated with '@ImutableDefaults'.

```
 @ImutableDefaults
 private static void applyDefaults(Builder builder) {
   // set default property values directly into the builder
 }
```

An immutable bean can be configured to cache the hash code.
In most cases this is not necessary, but if the bean is used as a hash key, then it may be helpful.
Simply set the boolean 'cacheHashCode' flag of '@BeanDefinition' to true.

```
 @BeanDefinition(cacheHashCode = true)
 public final class Foo implements ImmutableBean {
   // code generated immutable bean with cached hash code
 }
```


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

The serializer makes use the meta-data in the bean to minimize the output size.
In effect, the Joda-Bean acts as a schema to interpret the data.

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


## Integration

Joda-Beans has so far been integrated with Freemarker and MongoDB.
The [OpenGamma](http://developers.opengamma.com/) project has also provided its own integration with the Fudge messaging project.

The integration support classes are included in the jar file but only work when the optional dependency is present.
Note that it is entirely valid to use Joda-Beans without additional jar file dependencies apart from Joda-Convert.
