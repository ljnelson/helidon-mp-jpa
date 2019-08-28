# Helidon MicroProfile with JPA

Here's how to do this yourself.  This is using Maven 3.5.0 or later.
I presume that you're not a Maven or Java novice.

## `pom.xml` Setup

First, let's look at the `pom.xml`.

You'll need to import the Helidon BOM, so in your
`<dependencyManagement>` stanza you'll need:

    <dependency>
      <groupId>io.helidon</groupId>
      <artifactId>helidon-bom</artifactId>
      <version>1.2.1</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>

Then, in your actual `<dependencies>` section, you'll need to bring in
Helidon MicroProfile, obviously:

    <dependency>
      <groupId>io.helidon.microprofile.bundles</groupId>
      <artifactId>helidon-microprofile-2.2</artifactId>
    </dependency>

(That will get its version from the BOM, which is `1.2.1` as you can
see.)

Now you'll also need `provided`-scoped dependencies on the two APIs
you'll be working with directly.  I like to specify versions in my
`<dependencyManagement>` section, like this:

    <dependency>
      <groupId>jakarta.persistence</groupId>
      <artifactId>jakarta.persistence-api</artifactId>
      <version>2.2.2</version>
    </dependency>

    <dependency>
      <groupId>javax.transaction</groupId>
      <artifactId>javax.transaction-api</artifactId>
      <version>1.2</version>
    </dependency>
      
...and then in my `<dependencies>` stanza simply "point" to them, like so:

    <dependency>
      <groupId>jakarta.persistence</groupId>
      <artifactId>jakarta.persistence-api</artifactId>
      <scope>provided</scope>
    </dependency>

    <dependency>
      <groupId>javax.transaction</groupId>
      <artifactId>javax.transaction-api</artifactId>
      <scope>provided</scope>
    </dependency>
    
So that gets you the APIs you'll program against.  Now you need some
implementations that will actually be there at runtime.

For the JTA stuff, you'll want to have this in
`<dependencies>`:

    <dependency>
      <groupId>io.helidon.integrations.cdi</groupId>
      <artifactId>helidon-integrations-cdi-jta-weld</artifactId>
      <scope>runtime</scope>
    </dependency>
    
Note that these are in `runtime` scope, i.e. you don't compile against
them; they're just drop-in components.

All right, now let's start building up JPA support.  The first thing
we'll need is a connection pool, so let's use Helidon's.  In
`<dependencies>` you should do this:

    <dependency>
      <groupId>io.helidon.integrations.cdi</groupId>
      <artifactId>helidon-integrations-cdi-datasource-hikaricp</artifactId>
      <scope>runtime</scope>
    </dependency>
    
Next, we need a component that will bring in a JPA provider and will
adapt that provider to how it will be used.  Put this in your
`<dependencies>` stanza:

    <dependency>
      <groupId>io.helidon.integrations.cdi</groupId>
      <artifactId>helidon-eclipselink-cdi</artifactId>
      <scope>runtime</scope>
    </dependency>
    
Another `runtime` component.

A database for testing would be handy, so put this in your `<dependencyManagement>` stanza:

    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
      <version>1.4.199</version>
    </dependency>

...and this in your `<dependencies>` stanza:

    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
      <scope>test</scope>
    </dependency>

Now, finally, we need the CDI glue to stitch this together.  Put this
in your `<dependencies>` stanza:

    <dependency>
      <groupId>io.helidon.integrations.cdi</groupId>
      <artifactId>helidon-integrations-cdi-jpa</artifactId>
      <scope>runtime</scope>
    </dependency>

Then there are some goodies that really are optional.  Woodstox will
make XML parsing much faster, so it's nice to put this in your
`<dependencyManagement>` stanza:

    <dependency>
      <groupId>com.fasterxml.woodstox</groupId>
      <artifactId>woodstox-core</artifactId>
      <version>5.2.0</version>
    </dependency>
      
...and this in your `<dependencies>` stanza:

    <dependency>
      <groupId>com.fasterxml.woodstox</groupId>
      <artifactId>woodstox-core</artifactId>
      <scope>runtime</scope>
      <optional>true</optional>
    </dependency>
    
Jandex will make annotation scanning super fast so do this in your
`<dependencyManagement>` stanza:

    <dependency>
      <groupId>org.jboss</groupId>
      <artifactId>jandex</artifactId>
      <version>2.1.1.Final</version>
    </dependency>
      
...and this in your `<dependencies>` stanza:

    <dependency>
      <groupId>org.jboss</groupId>
      <artifactId>jandex</artifactId>
      <scope>runtime</scope>
    </dependency>
    
When you put all of this together, you end up with something like
this (obviously sanity-check the versions if you like):

    <dependencyManagement>
      <dependencies>


        <!-- Imports. -->


        <dependency>
          <groupId>io.helidon</groupId>
          <artifactId>helidon-bom</artifactId>
          <version>1.2.1</version>
          <type>pom</type>
          <scope>import</scope>
        </dependency>


        <!-- Normal dependencies. -->


        <dependency>
          <groupId>com.fasterxml.woodstox</groupId>
          <artifactId>woodstox-core</artifactId>
          <version>5.2.0</version>
        </dependency>

        <dependency>
          <groupId>com.h2database</groupId>
          <artifactId>h2</artifactId>
          <version>1.4.199</version>
        </dependency>

        <dependency>
          <groupId>jakarta.persistence</groupId>
          <artifactId>jakarta.persistence-api</artifactId>
          <version>2.2.2</version>
        </dependency>

        <dependency>
          <groupId>javax.transaction</groupId>
          <artifactId>javax.transaction-api</artifactId>
          <version>1.2</version>
        </dependency>

      </dependencies>
    </dependencyManagement>


    <dependencies>


      <!-- Test-scoped dependencies. -->


      <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
      </dependency>

      <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <scope>test</scope>
      </dependency>


      <!-- Runtime-scoped dependencies. -->


      <dependency>
        <groupId>com.fasterxml.woodstox</groupId>
        <artifactId>woodstox-core</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
      </dependency>

      <dependency>
        <groupId>io.helidon.integrations.cdi</groupId>
        <artifactId>helidon-integrations-cdi-datasource-hikaricp</artifactId>
        <scope>runtime</scope>
      </dependency>

      <dependency>
        <groupId>io.helidon.integrations.cdi</groupId>
        <artifactId>helidon-integrations-cdi-eclipselink</artifactId>
        <scope>runtime</scope>
      </dependency>

      <dependency>
        <groupId>io.helidon.integrations.cdi</groupId>
        <artifactId>helidon-integrations-cdi-jpa</artifactId>
        <scope>runtime</scope>
      </dependency>

      <dependency>
        <groupId>io.helidon.integrations.cdi</groupId>
        <artifactId>helidon-integrations-cdi-jta-weld</artifactId>
        <scope>runtime</scope>
      </dependency>


      <!-- Provided-scoped dependencies. -->


      <dependency>
        <groupId>jakarta.persistence</groupId>
        <artifactId>jakarta.persistence-api</artifactId>
        <scope>provided</scope>
      </dependency>

      <dependency>
        <groupId>javax.transaction</groupId>
        <artifactId>javax.transaction-api</artifactId>
        <scope>provided</scope>
      </dependency>


      <!-- Compile-scoped dependencies. -->


      <dependency>
        <groupId>io.helidon.microprofile.bundles</groupId>
        <artifactId>helidon-microprofile-2.2</artifactId>
      </dependency>


    </dependencies>
    
OK that was fun.  Obviously using Maven tricks you can bundle all this
up in various ways (one of which [I blog about
here](https://lairdnelson.wordpress.com/2018/12/21/maven-specifications-and-environments-part-0/)).

## Development Setup

On to the actual development.

The support I've added to Helidon MicroProfile looks as much like Java
EE as possible, while using CDI and only CDI as the backplane.

Since we're using CDI, create a
`src/main/resources/META-INF/beans.xml` file and make it look exactly
like this:

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://xmlns.jcp.org/xml/ns/javaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
                           http://xmlns.jcp.org/xml/ns/javaee/beans_2_0.xsd"
       version="2.0"
       bean-discovery-mode="annotated">
</beans>
```

Also right now before you forget create
`src/test/resources/META-INF/beans.xml` and make it look identical:

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://xmlns.jcp.org/xml/ns/javaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
                           http://xmlns.jcp.org/xml/ns/javaee/beans_2_0.xsd"
       version="2.0"
       bean-discovery-mode="annotated">
</beans>
```

OK, we're set up for CDI success.

### Writing the JAX-RS Application

So step one in actual development is to write a JAX-RS application.

To do this portably, you'll need a root resource class and an
`Application` to state authoritatively that it owns it.

The root resource class might be named `HelloWorldResource` and might
look like this to start (note that example code is subject to this
project's license unless it explicitly says otherwise):

```
package io.github.ljnelson.helidon.mp.jpa;

import java.util.Objects;
import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceException; // for javadoc only
import javax.persistence.SynchronizationType;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("")
@RequestScoped
public class HelloWorldResource {

  @PersistenceContext(unitName = "test")
  private EntityManager entityManager;

  public HelloWorldResource() {
    super();
  }

  @GET
  @Path("{firstPart}")
  @Produces(MediaType.TEXT_PLAIN)
  public String get(@PathParam("firstPart") final String firstPart) {
    return "world";
  }

}
```

Here we're not using it yet, but you can see that we've asked for an
`EntityManager` to be handed to us (note the `@PersistenceContext`
annotation).

This `EntityManager` will behave exactly as an `EntityManager` would
in a fat application server, i.e. _not_ like an EntityManager you
create by hand.  Specifically, if there is a transaction in effect, it
will automatically join it and do all the other Java EE-like things
you're probably used to if you've programmed Java EE JPA stuff in the
past.

The `Application` might look like this:

```
package io.github.ljnelson.helidon.mp.jpa;

import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import javax.ws.rs.core.Application;

@ApplicationScoped
public class GreetingApplication extends Application {

  public GreetingApplication() {
    super();
  }

  @Override
  public Set<Class<?>> getClasses() {
    return Collections.singleton(HelloWorldResource.class);
  }
  
}
```

Let's beef up the `get()` method in our root resource class.  Let's
change it to use the `EntityManager`:

```
  @GET
  @Path("{firstPart}")
  @Produces(MediaType.TEXT_PLAIN)
  public String get(@PathParam("firstPart") final String firstPart) {
    Objects.requireNonNull(firstPart);
    assert this.entityManager != null;
    final TypedQuery<Greeting> query = this.entityManager.createNamedQuery("findByFirstPart", Greeting.class);
    query.setParameter("firstPart", firstPart);
    return query.getSingleResult().toString();
  }
```

Note in particular there's no starting of transactions or other
boilerplate; that's all handled for you.  In this case, no transaction
is in effect.

Obviously for that bit of code to work we'll need an entity called
`Greeting`, which might look like this:

```
package io.github.ljnelson.helidon.mp.jpa;

import java.util.Objects;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Access(AccessType.FIELD)
@Entity(name = "Greeting")
@NamedQuery(name = "findByFirstPart",
            query = "SELECT g FROM Greeting g WHERE g.firstPart = :firstPart")
@Table(name = "GREETING",
       uniqueConstraints = {
         @UniqueConstraint(columnNames = {
             "FIRSTPART", "SECONDPART"
           },
           name = "UNQ_GREETING"
         )
       })
public class Greeting {

  @Id
  @Column(name = "ID", insertable = true, nullable = false, updatable = false)
  @GeneratedValue
  private Long id;

  @Basic(optional = false)
  @Column(name = "FIRSTPART", insertable = true, nullable = false, updatable = true)
  private String firstPart;

  @Basic(optional = false)
  @Column(name = "SECONDPART", insertable = true, nullable = false, updatable = true)
  private String secondPart;

  /**
   * Creates a new {@link Greeting}; required by the JPA specification
   * and for no other purpose.
   *
   * @deprecated Please use the {@link #Greeting(Long, String,
   * String)} constructor instead.
   *
   * @see #Greeting(Long, String, String)
   */
  @Deprecated
  protected Greeting() {
    super();
  }

  /**
   * Creates a new {@link Greeting}.
   *
   * @param id the identifier; may be {@code null}
   *
   * @param firstPart the first part of the greeting; must not be
   * {@code null}
   *
   * @param secondPart the second part of the greeting; must not be
   * {@code null}
   *
   * @exception NullPointerException if {@code firstPart} or {@code
   * secondPart} is {@code null}
   */
  public Greeting(final Long id, final String firstPart, final String secondPart) {
    super();
    this.id = id;
    this.firstPart = Objects.requireNonNull(firstPart);
    this.secondPart = Objects.requireNonNull(secondPart);
  }

  /**
   * Returns the identifier of this {@link Greeting}.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the identifier of this {@link Greeting} or {@code null}
   */
  public Long getId() {
    return this.id;
  }

  /**
   * Returns a {@link String} representation of the second part of
   * this {@link Greeting}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return a non-{@code null} {@link String} representation of the
   * second part of this {@link Greeting}
   */
  @Override
  public String toString() {
    return this.secondPart;
  }
  
}
```

OK, so that's all the code you need for a fetch-type operation.  Now
let's set up the test database and the connection pool.

### Setting Up the Database and Connection Pool

The connection pool creates itself from configuration properties, and
the kinds of configuration properties in play when you're talking
about Helidon MicroProfile are MicroProfile Config configuration
properties, so to get started let's add them to the default place
where such properties live.  MicroProfile Config is very flexible so
there are a zillion other places you could put them, but this is easy.
Create `src/test/resources/META-INF/microprofile-config.properties`
and make it look like this:

```
javax.sql.DataSource.test.dataSourceClassName=org.h2.jdbcx.JdbcDataSource
javax.sql.DataSource.test.dataSource.url=jdbc:h2:mem:test;INIT=CREATE TABLE GREETING (ID BIGINT NOT NULL, FIRSTPART VARCHAR NOT NULL, SECONDPART VARCHAR NOT NULL, PRIMARY KEY (ID))\\;ALTER TABLE GREETING ADD CONSTRAINT UNQ_GREETING UNIQUE (FIRSTPART, SECONDPART)\\;CREATE TABLE SEQUENCE (SEQ_NAME VARCHAR(50) NOT NULL, SEQ_COUNT NUMERIC(38), PRIMARY KEY (SEQ_NAME))\\;INSERT INTO SEQUENCE(SEQ_NAME, SEQ_COUNT) VALUES ('SEQ_GEN', 0)\\;INSERT INTO GREETING (ID, FIRSTPART, SECONDPART) VALUES (-1, 'hello', 'world')
javax.sql.DataSource.test.username=sa
javax.sql.DataSource.test.password=
```

Anything starting with `javax.sql.DataSource.` is a piece of
information destined for the guts of the Helidon MicroProfile
connection pool, which is [documented over
here](https://github.com/oracle/helidon/blob/master/integrations/cdi/datasource-hikaricp/README.adoc).
Here you can see that we're setting up the `DataSource` to be backed
by an in-memory H2 database named `test`.  It's using the [convenient
H2 feature of being able to run some initialization SQL on
startup](http://www.h2database.com/html/features.html#execute_sql_on_connection).
The user is the default `sa` user, and the password is the empty
string.

### Setting Up JPA

Next, we'll need to tell JPA what we're up to.  Create
`src/test/resources/META-INF/persistence.xml` and make it look like
this:

```
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence
                                 http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd"
             version="2.2">
  <persistence-unit
      name="test"
      transaction-type="JTA">
    <provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>
    <class>io.github.ljnelson.helidon.mp.jpa.Greeting</class>
    <properties>
      <property name="eclipselink.ddl-generation" value="create-tables"/>
      <property name="eclipselink.ddl-generation.output-mode" value="sql-script"/>
      <property name="eclipselink.create-ddl-jdbc-file-name" value="createDDL_ddlGeneration.jdbc"/>
      <property name="eclipselink.deploy-on-startup" value="true"/>
      <property name="eclipselink.jdbc.native-sql" value="true"/>
      <property name="eclipselink.logging.logger" value="JavaLogger"/>
      <property name="eclipselink.target-database" value="org.eclipse.persistence.platform.database.H2Platform"/>
      <property name="eclipselink.target-server" value="org.microbean.eclipselink.cdi.CDISEPlatform"/>
    </properties>
  </persistence-unit>
</persistence>
```

Here we've defined a persistence unit named `test`.  You'll note
that's what our `DataSource` was named as well.  That's on purpose.

(Oh, hey, did you see that little `transaction-type="JTA"` nugget in
there?  That's interesting, isn't it?)

This persistence unit uses Eclipselink as the provider.

There are [various Eclipselink-specific
properties](https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm)
here, most of which just make things nicer.  One that is definitely
not optional in this case is the `eclipselink.target-server` property,
which **must bet set to a value of
[`org.microbean.eclipselink.cdi.CDISEPlatform`](https://github.com/microbean/microbean-eclipselink-cdi/blob/bd8c8c765a3e436d6d8d9c7f79d1d1405752cf64/src/main/java/org/microbean/eclipselink/cdi/CDISEPlatform.java#L36-L223)**.
This allows Eclipselink to think that it's running in a Java EE server
on occasion but helpfully tells it to stop trying to use JNDI.
(There's an analogous [Hibernate
construct](https://github.com/microbean/microbean-hibernate-cdi/tree/master/src/main/java/org/microbean/hibernate/cdi)
too.)

So now we have:
* A JAX-RS application
* A JAX-RS implementation (Helidon MicroProfile)
* A connection pool (Helidon's HikariCP integration)
* A JPA implementation (EclipseLink)
* A transaction manager (Narayana)
* Various runtime components that stitch it all together
* Configuration that sets up the database and links it to the
  persistence unit

### Testing

We can add a JUnit test that tries to drive all this.  It might look like this:

```
package io.github.ljnelson.helidon.mp.jpa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.spi.CDI;

import io.helidon.microprofile.server.Server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestJPAIntegration {

  private Server server;

  private URL baseUrl;
  
  public TestJPAIntegration() {
    super();
  }

  @Before
  public void startServer() throws MalformedURLException {
    this.server = Server.create().start();
    this.baseUrl = new URL("http://127.0.0.1:" + server.port());
  }

  @After
  public void stopServer() {
    this.server.stop();
    this.baseUrl = null;
  }

  @Test
  public void testGet() throws Exception {
    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(this.baseUrl, "/hello").openStream(), "UTF-8"))) {
      assertEquals("world", reader.readLine());
    }
  }
  
}
```

If you run `mvn test` at this point (I haven't tried it, but you'll
note an uncanny resemblance between this README and this actual
project, which works!), you should see the test pass.

Now let's go back to the root resource class and add a stupid `POST` method:

```
  @POST
  @Path("{firstPart}")
  @Consumes(MediaType.TEXT_PLAIN)
  @Produces(MediaType.TEXT_PLAIN)
  @Transactional(TxType.REQUIRED)
  public String post(@PathParam("firstPart") final String firstPart,
                     final String secondPart) {
    Objects.requireNonNull(firstPart);
    Objects.requireNonNull(secondPart);
    assert this.entityManager != null;
    Greeting greeting = new Greeting(null, firstPart, secondPart);
    greeting = this.entityManager.merge(greeting);
    return String.valueOf(greeting.getId());
  }
```

Note the
[`@Transactional`](https://javaee.github.io/javaee-spec/javadocs/javax/transaction/Transactional.html)`(TxType.REQUIRED)`
annotation.

Normally putting this kind of logic directly on a JAX-RS class would
be quite rightly frowned upon but we're in Happy Example Land™ so
we get to do things like this.

This annotation replaces what EJBs used to do for us.  Now we can mark
methods as requiring EJB transaction semantics, only without all the
other EJB stuff no one actually cared about.  Here, we're saying:
"Please start a JTA transaction encompassing this method".

Remember our friend the `EntityManager`?  That `EntityManager` will
automatically take part in that transaction, just as if you were in a
big, fat Java EE application server.  Only you're not.

We can update our test to test this behavior too:

```
  @Test
  public void testPost() throws Exception {
    final HttpURLConnection c = (HttpURLConnection)new URL(this.baseUrl, "/hello").openConnection();
    assertNotNull(c);
    c.setDoOutput(true);
    c.setRequestProperty("Content-Type", "text/plain");
    c.setRequestMethod("POST");
    final byte[] helloBytes = "hello".getBytes("UTF8");
    assertNotNull(helloBytes);
    c.setRequestProperty("Content-Length", String.valueOf(helloBytes.length));
    try(final OutputStream stream = new BufferedOutputStream(c.getOutputStream())) {
      assertNotNull(stream);
      stream.write(helloBytes, 0, helloBytes.length);
    }
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (final InputStream inputStream = new BufferedInputStream(c.getInputStream())) {
      assertNotNull(inputStream);
      int bytesRead;      
      final byte[] bytes = new byte[4096];
      while ((bytesRead = inputStream.read(bytes, 0, bytes.length)) != -1) {
        baos.write(bytes, 0, bytesRead);
      }
    }
    assertEquals("1", baos.toString("UTF8"));
  }
```

That is some convoluted JDK gymnastics that sends a `POST` request to
our REST endpoint.  If you run `mvn test` at this point, everything
should still pass, and if you could see what was going on you'd see
SQL going into the database.

#### Logging

To see what's going on, it might help to set up Java logging
appropriately.  Let's create `src/test/logging.properties` and make it
look like this:

```
.level=WARNING
org.eclipse.persistence.level=FINE
handlers=java.util.logging.ConsoleHandler
java.util.logging.ConsoleHandler.formatter=java.util.logging.SimpleFormatter
java.util.logging.ConsoleHandler.level=FINEST
```

Then let's tell Maven's Surefire, the testing plugin, to make sure
it's passed when it forks off a JVM to run our test.  In your
`<plugins>` stanza you'll want something like this:

    <plugin>
      <artifactId>maven-surefire-plugin</artifactId>
      <configuration>
        <systemPropertyVariables>
          <java.util.logging.config.file>${basedir}/src/test/logging.properties</java.util.logging.config.file>
        </systemPropertyVariables>
      </configuration>
    </plugin>

Now when you run `mvn test` you should see output kind of like this:
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running io.github.ljnelson.helidon.mp.jpa.TestJPAIntegration
Aug 28, 2019 12:26:20 PM com.arjuna.ats.jta.common.JTAEnvironmentBean setXaRecoveryNodes
DEBUG: Setting up node identifiers '[1]' for which recovery will be performed
Aug 28, 2019 12:26:22 PM io.helidon.microprofile.security.SecurityMpService configure
INFO: Security extension for microprofile is enabled, yet security configuration is missing from config (requires providers configuration at key security.providers). Security will not have any valid provider.
Aug 28, 2019 12:26:22 PM io.smallrye.openapi.api.OpenApiDocument initialize
INFO: OpenAPI document initialized: io.smallrye.openapi.api.models.OpenAPIImpl@65f2f9b0
Aug 28, 2019 12:26:23 PM io.helidon.webserver.NettyWebServer <init>
INFO: Version: 1.2.1
Aug 28, 2019 12:26:23 PM io.helidon.webserver.NettyWebServer lambda$start$8
INFO: Channel '@default' started: [id: 0x3c0e6a63, L:/0:0:0:0:0:0:0:0:7001]
Aug 28, 2019 12:26:23 PM io.helidon.microprofile.server.ServerImpl lambda$start$10
INFO: Server started on http://localhost:7001 (and all other host addresses) in 103 milliseconds.
Aug 28, 2019 12:26:24 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test.server
FINE: Configured server platform: io.helidon.integrations.cdi.eclipselink.CDISEPlatform
Aug 28, 2019 12:26:25 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test
INFO: EclipseLink, version: Eclipse Persistence Services - 2.7.4.v20190115-ad5b7c6b2a
Aug 28, 2019 12:26:25 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test
INFO: Server: CDISEPlatform
Aug 28, 2019 12:26:25 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test.connection
CONFIG: connecting(DatabaseLogin(
	platform=>H2Platform
	user name=> ""
	connector=>JNDIConnector datasource name=>null
))
Aug 28, 2019 12:26:25 PM com.zaxxer.hikari.HikariDataSource <init>
INFO: HikariPool-1 - Starting...
Aug 28, 2019 12:26:25 PM com.zaxxer.hikari.HikariDataSource <init>
INFO: HikariPool-1 - Start completed.
Aug 28, 2019 12:26:25 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test.connection
CONFIG: Connected: jdbc:h2:mem:test
	User: SA
	Database: H2  Version: 1.4.199 (2019-03-13)
	Driver: H2 JDBC Driver  Version: 1.4.199 (2019-03-13)
Aug 28, 2019 12:26:25 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test.connection
CONFIG: connecting(DatabaseLogin(
	platform=>H2Platform
	user name=> ""
	connector=>JNDIConnector datasource name=>null
))
Aug 28, 2019 12:26:25 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test.connection
CONFIG: Connected: jdbc:h2:mem:test
	User: SA
	Database: H2  Version: 1.4.199 (2019-03-13)
	Driver: H2 JDBC Driver  Version: 1.4.199 (2019-03-13)
Aug 28, 2019 12:26:26 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test.connection
INFO: /file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test login successful
Aug 28, 2019 12:26:26 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test.sql
FINE: SELECT ID, FIRSTPART, SECONDPART FROM GREETING WHERE (FIRSTPART = ?)
	bind => [hello]
Aug 28, 2019 12:26:26 PM io.helidon.webserver.NettyWebServer lambda$start$6
INFO: Channel '@default' closed: [id: 0x3c0e6a63, L:/0:0:0:0:0:0:0:0:7001]
Aug 28, 2019 12:26:26 PM io.helidon.microprofile.server.ServerImpl lambda$stopWebServer$12
INFO: Server stopped in 23 milliseconds.
Aug 28, 2019 12:26:26 PM com.zaxxer.hikari.HikariDataSource close
INFO: HikariPool-1 - Shutdown initiated...
Aug 28, 2019 12:26:26 PM com.zaxxer.hikari.HikariDataSource close
INFO: HikariPool-1 - Shutdown completed.
Aug 28, 2019 12:26:26 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test.connection
CONFIG: disconnect
Aug 28, 2019 12:26:26 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test.connection
INFO: /file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test logout successful
Aug 28, 2019 12:26:27 PM io.helidon.microprofile.security.SecurityMpService configure
INFO: Security extension for microprofile is enabled, yet security configuration is missing from config (requires providers configuration at key security.providers). Security will not have any valid provider.
Aug 28, 2019 12:26:27 PM io.smallrye.openapi.api.OpenApiDocument initialize
INFO: OpenAPI document initialized: io.smallrye.openapi.api.models.OpenAPIImpl@594131f2
Aug 28, 2019 12:26:27 PM io.helidon.webserver.NettyWebServer <init>
INFO: Version: 1.2.1
Aug 28, 2019 12:26:27 PM io.helidon.webserver.NettyWebServer lambda$start$8
INFO: Channel '@default' started: [id: 0x569cf601, L:/0:0:0:0:0:0:0:0:7001]
Aug 28, 2019 12:26:27 PM io.helidon.microprofile.server.ServerImpl lambda$start$10
INFO: Server started on http://localhost:7001 (and all other host addresses) in 3 milliseconds.
Aug 28, 2019 12:26:27 PM com.arjuna.ats.arjuna.recovery.TransactionStatusManager addService
DEBUG: com.arjuna.ats.arjuna.recovery.ActionStatusService starting
Aug 28, 2019 12:26:27 PM com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem <init>
DEBUG: TransactionStatusManagerItem host: {0} port: {1}
Aug 28, 2019 12:26:27 PM com.arjuna.ats.arjuna.recovery.TransactionStatusManager start
INFO: ARJUNA012170: TransactionStatusManager started on port 53066 and host 127.0.0.1 with service com.arjuna.ats.arjuna.recovery.ActionStatusService
Aug 28, 2019 12:26:27 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test.server
FINE: Configured server platform: io.helidon.integrations.cdi.eclipselink.CDISEPlatform
Aug 28, 2019 12:26:27 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test
INFO: EclipseLink, version: Eclipse Persistence Services - 2.7.4.v20190115-ad5b7c6b2a
Aug 28, 2019 12:26:27 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test
INFO: Server: CDISEPlatform
Aug 28, 2019 12:26:27 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test.connection
CONFIG: connecting(DatabaseLogin(
	platform=>H2Platform
	user name=> ""
	connector=>JNDIConnector datasource name=>null
))
Aug 28, 2019 12:26:27 PM com.zaxxer.hikari.HikariDataSource <init>
INFO: HikariPool-2 - Starting...
Aug 28, 2019 12:26:27 PM com.zaxxer.hikari.HikariDataSource <init>
INFO: HikariPool-2 - Start completed.
Aug 28, 2019 12:26:27 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test.connection
CONFIG: Connected: jdbc:h2:mem:test
	User: SA
	Database: H2  Version: 1.4.199 (2019-03-13)
	Driver: H2 JDBC Driver  Version: 1.4.199 (2019-03-13)
Aug 28, 2019 12:26:27 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test.connection
CONFIG: connecting(DatabaseLogin(
	platform=>H2Platform
	user name=> ""
	connector=>JNDIConnector datasource name=>null
))
Aug 28, 2019 12:26:27 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test.connection
CONFIG: Connected: jdbc:h2:mem:test
	User: SA
	Database: H2  Version: 1.4.199 (2019-03-13)
	Driver: H2 JDBC Driver  Version: 1.4.199 (2019-03-13)
Aug 28, 2019 12:26:27 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test.connection
INFO: /file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test login successful
Aug 28, 2019 12:26:27 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test.sql
FINE: UPDATE SEQUENCE SET SEQ_COUNT = SEQ_COUNT + ? WHERE SEQ_NAME = ?
	bind => [50, SEQ_GEN]
Aug 28, 2019 12:26:27 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test.sql
FINE: SELECT SEQ_COUNT FROM SEQUENCE WHERE SEQ_NAME = ?
	bind => [SEQ_GEN]
Aug 28, 2019 12:26:27 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test.sql
FINE: INSERT INTO GREETING (ID, FIRSTPART, SECONDPART) VALUES (?, ?, ?)
	bind => [1, hello, hello]
Aug 28, 2019 12:26:27 PM io.helidon.webserver.NettyWebServer lambda$start$6
INFO: Channel '@default' closed: [id: 0x569cf601, L:/0:0:0:0:0:0:0:0:7001]
Aug 28, 2019 12:26:27 PM io.helidon.microprofile.server.ServerImpl lambda$stopWebServer$12
INFO: Server stopped in 2 milliseconds.
Aug 28, 2019 12:26:27 PM com.zaxxer.hikari.HikariDataSource close
INFO: HikariPool-2 - Shutdown initiated...
Aug 28, 2019 12:26:27 PM com.zaxxer.hikari.HikariDataSource close
INFO: HikariPool-2 - Shutdown completed.
Aug 28, 2019 12:26:27 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test.connection
CONFIG: disconnect
Aug 28, 2019 12:26:27 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test.connection
INFO: /file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/classes/_test logout successful
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 8.189 s - in io.github.ljnelson.helidon.mp.jpa.TestJPAIntegration
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  17.236 s
[INFO] Finished at: 2019-08-28T12:26:28-07:00
[INFO] ------------------------------------------------------------------------
```

Yay!

## Related Projects

* [Narayana](https://github.com/jbosstm/narayana): A JTA transaction
  manager.  Battle-tested.  I [helped with the CDI
  internals](https://github.com/jbosstm/narayana/pull/1346).
* [Eclipselink](https://github.com/eclipse-ee4j/eclipselink/tree/master/jpa):
  A JPA implementation.
* [Weld](https://github.com/weld): the CDI reference implementation.
* [HikariCP](https://github.com/brettwooldridge/HikariCP): The fastest
  connection pool implementation.
* [H2](http://www.h2database.com/html/main.html): A nice little Java
  database.
* [Woodstox](https://github.com/FasterXML/woodstox): Stax XML parser
  that flies.
* [Jandex](https://github.com/wildfly/jandex): Don't scan annotations
  by loading classes; look at efficient indices instead.
