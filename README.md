Joda-Beans
------------

Joda-Beans provides a small framework that adds properties to Java, greatly enhancing JavaBeans.
An API is provided that defines a bean and property model, together with a code generator to make it work in practice.

The key concept is to allow each property on a bean to be accessed as an object.
This enables technologies such as XPath, XML conversion, DB mappings, WebApp validation and Swing bindings.

Joda-Beans is licensed under the business-friendly [Apache 2.0 licence](https://www.joda.org/joda-beans/licenses.html).


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
[a simple user account class](https://github.com/JodaOrg/joda-beans/blob/v2.0/src/test/java/org/joda/beans/gen/UserAccount.java#L32),
[example usage](https://github.com/JodaOrg/joda-beans/blob/v2.0/src/test/java/org/joda/beans/Examples.java#L22),
[example of validation](https://github.com/JodaOrg/joda-beans/blob/v2.0/src/test/java/org/joda/beans/gen/ValidateBean.java#L33).


### Documentation
Various documentation is available:

* The [home page](https://www.joda.org/joda-beans/)
* The helpful [user guide](https://www.joda.org/joda-beans/userguide.html)
* The [Javadoc](https://www.joda.org/joda-beans/apidocs/index.html)
* The change notes for the [releases](https://www.joda.org/joda-beans/changes-report.html)
* The [related projects](related.html) including Maven, Gradle and IntelliJ integration


### Releases
[Release 2.8.3](https://www.joda.org/joda-beans/download.html) is the current release.
This release is considered stable and worthy of the 2.x tag.
There are only [minor incompatibilities](https://www.joda.org/joda-beans/migration.html) with the 1.x codebase.

Joda-Beans requires Java SE 8 or later and depends on [Joda-Convert](https://www.joda.org/joda-convert/).
There are a number of [optional dependencies](https://www.joda.org/joda-beans/dependencies.html) which help with integration.

Available in the [Maven Central repository](https://search.maven.org/search?q=g:org.joda%20AND%20a:joda-beans&core=gav)

For Java SE 6 compatibility, use [release 1.14](https://github.com/JodaOrg/joda-beans/releases/tag/v1.14).

![Tidelift dependency check](https://tidelift.com/badges/github/JodaOrg/joda-beans)


### For enterprise
Available as part of the Tidelift Subscription.

Joda and the maintainers of thousands of other packages are working with Tidelift to deliver one enterprise subscription that covers all of the open source you use.

If you want the flexibility of open source and the confidence of commercial-grade software, this is for you.

[Learn more](https://tidelift.com/subscription/pkg/maven-org-joda-joda-beans?utm_source=maven-org-joda-joda-beans&utm_medium=github)


### Support
Please use [Stack Overflow](https://stackoverflow.com/search?q=joda-beans) for general usage questions.
GitHub [issues](https://github.com/JodaOrg/joda-beans/issues) and [pull requests](https://github.com/JodaOrg/joda-beans/pulls)
should be used when you want to help advance the project.

Any donations to support the project are accepted via [OpenCollective](https://opencollective.com/joda).

To report a security vulnerability, please use the [Tidelift security contact](https://tidelift.com/security).
Tidelift will coordinate the fix and disclosure.


### Release process

* Update version (README.md, index.md, changes.xml)
* Commit and push
* Switch to Java 11
* `mvn clean release:clean release:prepare release:perform`
* `git fetch`
* Website will be built and released by GitHub Actions
