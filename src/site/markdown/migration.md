## Migration to v2.0

Joda-Beans v2.0 has some incompatible changes.

### BeanQuery

The `BeanQuery` interface has been removed.
It was rarely used and can be better achieved via lambdas and functional interfaces in Java SE 8.
The `ChainedBeanQuery` class is removed, with some behaviour replaced by `JodaBeanUtils.chain()`.


### PropertyMap

The `PropertyMap` interface has been removed.
It was rarely used and can be better achieved via `JodaBeanUtils.flatten()` or `BasicPropertyMap.of()`.


## BeanBuilder generics

The method `BeanBuilder.get(MetaProperty)` has changed its generics.
It you use it or have written a `BeanBuilder` you may be affected.


## BeanBuilder setString() and setAll()

Applications should use Joda-Convert directly for the setString() methods and loop for setAll().


### Registering meta-beans

The `JodaBeanUtils.registerMetaBean()` and `JodaBeanUtils.metaBean()` methods have been deprecated.
Use `MetaBean.register()` and `MetaBean.of()` instead.


## Config file

The config file "jdk6.ini" has been renamed to "jdk.ini".
The old name is recognised on the command line.


## Light and Minimal beans

The code generation strategies have been altered to be simpler.
They are now ready for Java SE 9.


## Reflective beans

The factory method has been changed. The meta-bean now takes all the property names.
This avoids the need to use reflection to find the meta-properties, ready for Java SE 9.
