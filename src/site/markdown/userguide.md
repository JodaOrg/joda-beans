## User guide

Joda-Beans is a small library that adds properties to the Java programming language.
It consists of these main parts:

* a set of interfaces and classes that define a property
* a source [code generator](userguide-codegen.html)
* [serialization and deserialization](userguide-serialization.html) via JSON, XML and MsgPack binary
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
The interface also provides access to the *meta-bean*.

The [MetaBean](apidocs/org/joda/beans/MetaBean.html) interface defines the meta-bean.
The "meta" name indicates that this is a higher level description of the bean itself.
In fact, the meta-bean is the equivalent to `java.lang.Class` for beans - there is only one meta-bean object
for all instances of the bean.
The meta-bean API provides access to the name of the bean, it's type, a builder and the *meta-properties*.

The [MetaProperty](apidocs/org/joda/beans/MetaProperty.html) interface is the main definition of a property.
It is at the "meta" level, describing the property without holding a references to any specific instance of a bean.
As such, it is effectively the equivalent to `java.lang.reflect.Field` for a single property on a bean - there is only
one meta-property object for all instances of the property.
The meta-property API provides access to the name of the property, it's type, any declared annotations and
whether it is read-write or read-only.
It also include methods to get and set the value, which require the bean to be passed in just like `java.lang.reflect.Field`.

```
  Bean person = new Person();
  MetaProperty<String> surnameMetaProperty = bean.metaBean().metaProperty("surname");
  String surname = surnameMetaProperty.get(person);
```

The bean itself also provides access to instances of the [Property](apidocs/org/joda/beans/Property.html) interface for each property.
Each property object is a simple combination of the bean and the meta-property.
This means that there are simple methods to get and set the value of the property which directly affect the underlying bean.
The property also provides methods to get the property name, the underlying bean and the meta-property.

```
  Bean person = new Person();
  Property<String> surnameProperty = bean.property("surname");
  String surname = surnameProperty.get();
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
which provides a standard way to build the bean.
The builder is obtained from the meta-bean.
Each property is then set before calling `build`.

A selection of utilities is available on [JodaBeanUtils](apidocs/org/joda/beans/JodaBeanUtils.html).
This includes the ability to lookup a meta-bean from a `java.lang.Class`, access to the Joda-Convert string
converter and methods to extract the types of lists and maps using reflection on generics.

The access to the field-level annotations provided by the meta-property is also useful to framework writers.


### Joda-Convert integration

A Joda-Beans meta-property provides the ability to set a property from a string.
The conversion between the string and the type of the property, such as `int` or `URI`, is handled by
[Joda-Convert](/joda-convert/). The converter can be customised, and is exposed by `JodaBeanUtils`.

When iterating over a bean object graph, being able to determine which objects are "leaves", the simple
types that have a direct string representation, is a very useful ability.


### Finding the generic type parameters of a property

The Java bytecode format contains more details about generic types than is often realised.
The `JodaBeanUtils` methods `collectionType()`, `mapKeyType()` and
`mapValueType()` allow the generic parameter type of a property to be extracted.
Thus an application can tell that a property is of type `List<String>`, not just `List`.


### Iterating over a bean object graph

The `JodaBeanUtils` class contains the method `beanIterator()`.
This provides a simple mechanism to iterate over all the beans within a bean.

The iteration is depth-first, and handles collections, and collections within collections.
Note however that there is no protection against cycles in the object graph, thus the method
is only fully safe when using immutable beans.

```
 for (Bean bean : JodaBeanUtils.beanIterator(rootBean)) {
   // perform logic on each bean in the object graph
 }
```


## Code generation

It is entirely possible to write a Joda-Bean by hand - the design utilises simple Java interfaces.
Most users choose to code generate beans however.
For information on code generation, see the [code generation user guide](userguide-codegen.html).


## Serialization

One of the benefits of a bean and property system is that serialization code becomes much simpler.
For information on serialization in XML, JSON and binary, see the [serialization user guide](userguide-serialization.html).


## Integration

Joda-Beans has been integrated with Freemarker, MongoDB and Kryo.
The integration support classes are included in the jar file but only work when the optional dependency is present.
Note that it is entirely valid to use Joda-Beans without additional jar file dependencies apart from Joda-Convert.

The beans are also widely use in the [Strata](https://strata.opengamma.io/) market risk project.
