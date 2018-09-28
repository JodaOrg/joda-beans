## <i></i> About

**Joda-Beans** provides a small framework that adds properties to Java, greatly enhancing JavaBeans.

The key concept is to allow each property on a bean to be accessed as an object in its own right.
This provides the hook for other technologies to build on, such as serialization, mapping,
expression languages and validation.

Joda-Beans is licensed under the business-friendly [Apache 2.0 licence](licensecover.html).


## <i></i> Features

A selection of key features:

* Code generate mutable and immutable beans
* No more manually written getters, setters, equals, hashCode, toString or immutable builders
* High quality generated code, perfect for use in an API or for creating Javadoc
* Full-featured property abstraction, treating a bean as a map of properties
* Fast, no use of reflection
* Built in JSON, XML and binary serialization


## <i></i> Documentation

Various documentation is available:

* The helpful [user guide](userguide.html)
* The extended [code generation guide](userguide-codegen.html)
* The [Javadoc](apidocs/index.html)
* The [change notes](changes-report.html) for each release
* The [GitHub](https://github.com/JodaOrg/joda-beans) source repository
* The [related projects](related.html) including Maven, Gradle and IntelliJ integration


---

## <i></i> Why Joda Beans?

Joda-Beans has been created to plug a gap in the Java language - properties.
The concept of properties is familiar to those coding in almost every other modern language.
Java stands alone in its pursuit of the JavaBeans, a specification built on naming conventions and reflection.

JavaBeans are typically created by manual coding or one-off IDE generation, such as by Eclipse.
The same approach is taken to the creation of equals and hashCode methods.
However, none of these approaches provides for a simple and fast mechanism to query a bean for the properties it exposes.

Joda-Beans provides a solution. As a developer, you just write the fields much as you would today.
Then you add annotations to the bean and properties.
Finally, you run a code generator, which creates the get/set methods plus framework methods that allow the properties
to be effectively queried.
A key point is that the code generator may be run again and again on the Java file, and is non-destructive.

```
  @BeanDefinition
  public final class Foo implements Bean {
    /** The forename. */
    @PropertyDefinition
    private String forename;

    /** The surname. */
    @PropertyDefinition(validate = "notNull")
    private String surname;

    /** The address of the person. */
    @PropertyDefinition
    private Address address;
    
    // Joda-Beans will code generate all getters, setters, equals, hashCode, toString and property accessors
  }
```

See these sample classes used for testing -
[a simple user account class](https://github.com/JodaOrg/joda-beans/blob/v2.0/src/test/java/org/joda/beans/gen/UserAccount.java#L32),
[example usage](https://github.com/JodaOrg/joda-beans/blob/v2.0/src/test/java/org/joda/beans/Examples.java#L22),
[example of validation](https://github.com/JodaOrg/joda-beans/blob/v2.0/src/test/java/org/joda/beans/gen/ValidateBean.java#L33).

As well as mutable beans following the Java Bean specification. Joda-Beans supports the creation of immutable beans.
One of the biggest issues with using immutable objects in Java is their creation.
Joda-Beans simplifies this by code generating builder classes for each immutable bean:

```
  // using code generated immutable bean builders, example showing an interest rate swap leg
  FixedRateSwapLeg payLeg = FixedRateSwapLeg.builder()
    .accrualPeriods(PeriodicScheduleDefn.builder()
      .startDate(LocalDate.of(2014, 9, 12))
      .endDate(LocalDate.of(2021, 9, 12))
      .frequency(Frequency.P6M)
      .businessDayAdjustment(BusinessDayConventions.MODIFIED_FOLLOWING)
      .startDateBusinessDayAdjustment(BusinessDayAdjustment.NONE)
      .build())
    .calculation(FixedRateCalculation.builder()
      .notional(CurrencyAmount.of(Currency.USD, 100_000_000))
      .dayCount(DayCounts.THIRTY_U_360)
      .rate(0.015)
      .build())
    .build();
```

Once beans and properties are defined, it is possible to use them in powerful ways.
A fast and efficient serialization mechanism is provided to read and write JSON, XML and binary
using [Joda-Convert](/joda-convert/) for object to string conversion.
Further integration is included for MongoDB and Freemarker.

---

## <i></i> Releases

[Release 2.3](download.html) is the current release.
This release is considered stable and worthy of the 2.x tag.
There are only [minor incompatibilities](migration.html) with the 1.x codebase.

Joda-Beans requires Java SE 8 or later and depends on [Joda-Convert](/joda-convert/).
There are a number of [optional dependencies](dependencies.html) which help with integration.

Available in [Maven Central](https://search.maven.org/search?q=g:org.joda%20AND%20a:joda-beans&core=gav).

```xml
<dependency>
  <groupId>org.joda</groupId>
  <artifactId>joda-beans</artifactId>
  <version>2.3</version>
</dependency>
```

The main jar file is based on Java 8 but contains a `module-info.class` file for Java 9 and later.
If you have problems with this, there is a "classic" variant you can use instead:

```xml
<dependency>
  <groupId>org.joda</groupId>
  <artifactId>joda-beans</artifactId>
  <version>2.3</version>
  <classifier>classic</classifier>
</dependency>
```

For Java SE 6 compatibility, use [release 1.14](https://github.com/JodaOrg/joda-beans/releases/tag/v1.14).

See the [related projects](related.html) page for Maven, Gradle and IntelliJ integration.


---

### Support

Support on bugs, library usage or enhancement requests is available on a best efforts basis.

To suggest enhancements or contribute, please [fork the source code](https://github.com/JodaOrg/joda-beans)
on GitHub and send a Pull Request.

Alternatively, use GitHub [issues](https://github.com/JodaOrg/joda-beans/issues).
