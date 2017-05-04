Joda-Beans
------------

Joda-Beans provides a small framework that adds properties to Java, greatly enhancing JavaBeans.
An API is provided that defines a bean and property model, together with a code generator to make it work in practice.

The key concept is to allow each property on a bean to be accessed as an object.
This enables technologies such as XPath, XML conversion, DB mappings, WebApp validation and Swing bindings.

Joda-Beans is licensed under the business-friendly [Apache 2.0 licence](http://www.joda.org/joda-beans/license.html).


### Why Joda Beans?

Joda-Beans has been created to plug a gap in the Java language - properties.
The concept of properties is familiar to those coding in almost every other modern language.
Java stands alone in its pursuit of the terrible JavaBean approach, and personally I believe that
properties should have been added to Java before generics and closures.

JavaBeans are typically created by manual coding or one-off IDE generation, such as by Eclipse.
The same approach is taken to the creation of equals and hashCode methods.
However, none of these approaches provides for a simple and fast mechanism to query a bean for the properties it exposes.

Joda-Beans provides a solution. As a developer, you just write the fields much as you would today.
Then you add annotations to the bean and properties.
Finally, you run a code generator, which creates the get/set methods plus framework methods that allow the properties
to be effectively queried.
If you use Eclipse and the Joda-Beans Maven plugin, the bean will be regenerated automatically on save.

A key point is that the code generator may be run again and again on the Java file, and is non-destructive.
See these sample classes used for testing -
[a simple user account class](https://github.com/JodaOrg/joda-beans/blob/v1.4/src/test/java/org/joda/beans/gen/UserAccount.java#L34),
[example usage](https://github.com/JodaOrg/joda-beans/blob/v1.0/src/test/java/org/joda/beans/Examples.java#L26),
[example of validation](https://github.com/JodaOrg/joda-beans/blob/v1.0/src/test/java/org/joda/beans/gen/ValidateBean.java#L38).


### Documentation
Various documentation is available:

* The [home page](http://www.joda.org/joda-beans/)
* The helpful [user guide](http://www.joda.org/joda-beans/userguide.html)
* The [Javadoc](http://www.joda.org/joda-beans/apidocs/index.html)
* The change notes for the [releases](http://www.joda.org/joda-beans/changes-report.html)


### Releases
[Release 1.13](http://www.joda.org/joda-beans/download.html) is the current release.
This release is considered stable and worthy of the 1.x tag.

Joda-Beans requires Java SE 6 or later and depends on [Joda-Convert](http://www.joda.org/joda-convert/).
There are a number of [optional dependencies](http://www.joda.org/joda-beans/dependencies.html) which help with integration.

Available in the [Maven Central repository](http://search.maven.org/#artifactdetails|org.joda|joda-beans|1.13|jar)


### Support
Please use GitHub issues and Pull Requests for support.


### Release process

* Update version (pom.xml, README.md, index.md, changes.xml)
* Commit and push
* `mvn clean deploy -Doss.repo -Dgpg.passphrase=""`
* Release project in [Nexus](https://oss.sonatype.org)
* Website will be built and released by Travis
