# Helidon MicroProfile with JPA

Here's how to do this yourself.

First, let's look at the `pom.xml`.

You'll need to import the Helidon BOM, so in your
`<dependencyManagement>` stanza you'll need:

    <dependency>
        <groupId>io.helidon</groupId>
        <artifactId>helidon-bom</artifactId>
        <version>0.11.0</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>

Then, in your actual `<dependencies>` section, you'll need to bring in
Helidon MicroProfile, obviously:

    <dependency>
      <groupId>io.helidon.microprofile.bundles</groupId>
      <artifactId>helidon-microprofile-1.2</artifactId>
    </dependency>

(That will get its version from the BOM, which is `0.11.0` as you can
see.)

Now you'll also need `provided`-scoped dependencies on the two APIs
you'll be working with directly.  I like to specify versions in my
`<dependencyManagement>` section, like this:

      <dependency>
        <groupId>javax.persistence</groupId>
        <artifactId>javax.persistence-api</artifactId>
        <version>2.2</version>
        <type>jar</type>
      </dependency>

      <dependency>
        <groupId>javax.transaction</groupId>
        <artifactId>javax.transaction-api</artifactId>
        <version>1.2</version>
        <type>jar</type>
      </dependency>
      
...and then in my `<dependencies>` stanza simply "point" to them, like so:

    <dependency>
      <groupId>javax.persistence</groupId>
      <artifactId>javax.persistence-api</artifactId>
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
`<dependencyManagement>`:

    <dependency>
        <groupId>org.microbean</groupId>
        <artifactId>microbean-narayana-jta-cdi</artifactId>
        <version>0.1.6</version>
        <type>jar</type>
    </dependency>

    <dependency>
        <groupId>org.microbean</groupId>
        <artifactId>microbean-narayana-jta-weld-se</artifactId>
        <version>0.1.4</version>
        <type>jar</type>
    </dependency>

...and then you'll want to "point at" it in `<dependencies>`:

    <dependency>
      <groupId>org.microbean</groupId>
      <artifactId>microbean-narayana-jta-weld-se</artifactId>
      <scope>runtime</scope>
      <optional>true</optional>
    </dependency>
    
    <dependency>
      <groupId>org.microbean</groupId>
      <artifactId>microbean-narayana-jta-cdi</artifactId>
      <scope>runtime</scope>
      <optional>true</optional>
    </dependency>
    
Note that these are in `runtime` scope, i.e. you don't compile against
them; they're just drop-in components.  I've listed them as being
`optional` here to emphasize that they're just a particular
implementation of the JTA specification.  (You could write drop-in
components for [Bitronix](https://github.com/bitronix/btm) for example
instead of [Narayana](http://narayana.io/).)

OK, you'll also need to bring in Weld's transactional notifier
support&mdash;after all, we have a transaction manager now, so why not
enable [transactional observer
methods](http://docs.jboss.org/cdi/spec/2.0/cdi-spec.html#transactional_observer_methods)?

So in `<dependencyManagement>` you'll have:

    <dependency>
        <groupId>org.jboss.weld.module</groupId>
        <artifactId>weld-jta</artifactId>
        <version>3.0.3.Final</version>
        <type>jar</type>
    </dependency>

...and in `<dependencies>` you'll have:

    <dependency>
      <groupId>org.jboss.weld.module</groupId>
      <artifactId>weld-jta</artifactId>
      <scope>runtime</scope>
    </dependency>
    
Once again, note this is in `runtime` scope; it's a drop-in.

All right, now let's start building up JPA support.  The first thing
we'll need is a connection pool, so let's use Helidon's.  In
`<dependencyManagement>` you should do this:

    <dependency>
        <groupId>io.helidon.integrations.cdi</groupId>
        <artifactId>helidon-integrations-cdi-datasource-hikaricp</artifactId>
        <version>0.11.1-SNAPSHOT</version> <!-- with https://github.com/oracle/helidon/pull/366 -->
        <type>jar</type>
    </dependency>

At the moment you'll have to build Helidon yourself with the mentioned
pull request; once 1.0 is out you can use that directly.

Then in `<dependencies>` you'll have:

    <dependency>
        <groupId>io.helidon.integrations.cdi</groupId>
        <artifactId>helidon-integrations-cdi-datasource-hikaricp</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>
    
Once again, it's `runtime` and `optional` to indicate that you can
pick any connection pool you wish so long as it has CDI support,
i.e. so long as other components can look up a `DataSource` with
`@Named` on it.

Next, we'll need a JPA implementation.  Here's Eclipselink in
`<dependencyManagement>`:

      <dependency>
        <groupId>org.eclipse.persistence</groupId>
        <artifactId>org.eclipse.persistence.jpa</artifactId>
        <version>2.7.3</version>
        <type>jar</type>
      </dependency>
      
...and in `<dependencies>`:

    <dependency>
      <groupId>org.eclipse.persistence</groupId>
      <artifactId>org.eclipse.persistence.jpa</artifactId>
      <scope>runtime</scope>
      <optional>true</optional>
    </dependency>
    
Note again that I've listed it as `optional` as nothing internally
cares whether you're using Eclipselink or, say, Hibernate.

We'll need a way to tell Eclipselink that it is not going to be
running in a giant snarling Java EE server, but neither, exactly, is
it going to be running in true SE mode.  So put this in your
`<dependencyManagement>` stanza:

    <dependency>
      <groupId>org.microbean</groupId>
      <artifactId>microbean-eclipselink-cdi</artifactId>
      <version>0.0.1</version>
      <type>jar</type>
    </dependency>
      
...and this in your `<dependencies>` stanza:

    <dependency>
      <groupId>org.microbean</groupId>
      <artifactId>microbean-eclipselink-cdi</artifactId>
      <scope>runtime</scope>
      <optional>true</optional>
    </dependency>
    
Another `runtime` `optional` component.

A database for testing would be handy, so put this in your `<dependencyManagement>` stanza:

    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
      <version>1.4.197</version>
      <type>jar</type>
    </dependency>

...and this in your `<dependencies>` stanza:

    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
      <type>jar</type>
      <scope>test</scope>
    </dependency>

Now we need the CDI glue to stitch this together.  Put this in your
`<dependencyManagement>` stanza:

    <dependency>
      <groupId>org.microbean</groupId>
      <artifactId>microbean-jpa-cdi</artifactId>
      <version>0.1.9</version>
      <type>jar</type>
    </dependency>
      
    <dependency>
      <groupId>org.microbean</groupId>
      <artifactId>microbean-jpa-weld-se</artifactId>
      <version>0.3.0</version>
      <type>jar</type>
    </dependency>
      
...and this in your `<dependencies>` stanza:

    <dependency>
      <groupId>org.microbean</groupId>
      <artifactId>microbean-jpa-cdi</artifactId>
      <scope>runtime</scope>
    </dependency>

    <dependency>
      <groupId>org.microbean</groupId>
      <artifactId>microbean-jpa-weld-se</artifactId>
      <scope>runtime</scope>
      <optional>true</optional>
    </dependency>
    
You'll note that I've marked `microbean-jpa-cdi` as not being
`optional`.  That is because it doesn't care what CDI implementation
you're using or what JPA implementation you're using; it's just glue
to make sure the two parts work together.

Then there are some goodies that really are optional.  Woodstox will
make XML parsing much faster, so it's nice to put this in your
`<dependencyManagement>` stanza:

    <dependency>
      <groupId>com.fasterxml.woodstox</groupId>
      <artifactId>woodstox-core</artifactId>
      <version>5.2.0</version>
      <type>jar</type>
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
      <version>2.0.5.Final</version>
      <type>jar</type>
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
          <version>0.11.0</version>
          <type>pom</type>
          <scope>import</scope>
        </dependency>


        <!-- Normal dependencies. -->


        <dependency>
          <groupId>com.fasterxml.woodstox</groupId>
          <artifactId>woodstox-core</artifactId>
          <version>5.2.0</version>
          <type>jar</type>
        </dependency>

        <dependency>
          <groupId>com.h2database</groupId>
          <artifactId>h2</artifactId>
          <version>1.4.197</version>
          <type>jar</type>
        </dependency>

        <dependency>
          <groupId>io.helidon.integrations.cdi</groupId>
          <artifactId>helidon-integrations-cdi-datasource-hikaricp</artifactId>
          <version>0.11.1-SNAPSHOT</version> <!-- with https://github.com/oracle/helidon/pull/366 -->
          <type>jar</type>
        </dependency>

        <dependency>
          <groupId>javax.persistence</groupId>
          <artifactId>javax.persistence-api</artifactId>
          <version>2.2</version>
          <type>jar</type>
        </dependency>

        <dependency>
          <groupId>javax.transaction</groupId>
          <artifactId>javax.transaction-api</artifactId>
          <version>1.2</version>
          <type>jar</type>
        </dependency>

        <dependency>
          <groupId>org.eclipse.persistence</groupId>
          <artifactId>org.eclipse.persistence.jpa</artifactId>
          <version>2.7.3</version>
          <type>jar</type>
        </dependency>

        <dependency>
          <groupId>org.jboss</groupId>
          <artifactId>jandex</artifactId>
          <version>2.0.5.Final</version>
          <type>jar</type>
        </dependency>

        <dependency>
          <groupId>org.jboss.weld.module</groupId>
          <artifactId>weld-jta</artifactId>
          <version>3.0.3.Final</version>
          <type>jar</type>
        </dependency>

        <dependency>
          <groupId>org.microbean</groupId>
          <artifactId>microbean-eclipselink-cdi</artifactId>
          <version>0.0.1</version>
          <type>jar</type>
        </dependency>

        <dependency>
          <groupId>org.microbean</groupId>
          <artifactId>microbean-jpa-cdi</artifactId>
          <version>0.1.9</version>
          <type>jar</type>
        </dependency>

        <dependency>
          <groupId>org.microbean</groupId>
          <artifactId>microbean-jpa-weld-se</artifactId>
          <version>0.3.0</version>
          <type>jar</type>
        </dependency>

        <dependency>
          <groupId>org.microbean</groupId>
          <artifactId>microbean-narayana-jta-cdi</artifactId>
          <version>0.1.6</version>
          <type>jar</type>
        </dependency>

        <dependency>
          <groupId>org.microbean</groupId>
          <artifactId>microbean-narayana-jta-weld-se</artifactId>
          <version>0.1.4</version>
          <type>jar</type>
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
        <optional>true</optional>
      </dependency>

      <dependency>
        <groupId>org.eclipse.persistence</groupId>
        <artifactId>org.eclipse.persistence.jpa</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
      </dependency>

      <dependency>
        <groupId>org.jboss</groupId>
        <artifactId>jandex</artifactId>
        <scope>runtime</scope>
      </dependency>

      <dependency>
        <groupId>org.jboss.weld.module</groupId>
        <artifactId>weld-jta</artifactId>
        <scope>runtime</scope>
      </dependency>

      <dependency>
        <groupId>org.microbean</groupId>
        <artifactId>microbean-eclipselink-cdi</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
      </dependency>

      <dependency>
        <groupId>org.microbean</groupId>
        <artifactId>microbean-jpa-cdi</artifactId>
        <scope>runtime</scope>
      </dependency>

      <dependency>
        <groupId>org.microbean</groupId>
        <artifactId>microbean-jpa-weld-se</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
      </dependency>

      <dependency>
        <groupId>org.microbean</groupId>
        <artifactId>microbean-narayana-jta-weld-se</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
      </dependency>

      <dependency>
        <groupId>org.microbean</groupId>
        <artifactId>microbean-narayana-jta-cdi</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
      </dependency>


      <!-- Provided-scoped dependencies. -->


      <dependency>
        <groupId>javax.persistence</groupId>
        <artifactId>javax.persistence-api</artifactId>
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
        <artifactId>helidon-microprofile-1.2</artifactId>
      </dependency>


    </dependencies>
    
OK that was fun.  Obviously using Maven tricks you can bundle all this
up in various ways (one of which [I blog about
here](https://lairdnelson.wordpress.com/2018/12/21/maven-specifications-and-environments-part-0/)).

On to the actual development.

The support I've added to Helidon MicroProfile looks as much like Java
EE as possible, while using CDI and only CDI as the backplane.

Since we're using CDI, create a
`src/main/resources/META-INF/beans.xml` file and make it look like
this:

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
`[@Transactional](https://javaee.github.io/javaee-spec/javadocs/javax/transaction/Transactional.html)(TxType.REQUIRED)`
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
Feb 07, 2019 5:14:34 PM org.jboss.weld.bootstrap.events.BeforeBeanDiscoveryImpl addAnnotatedType
WARN: WELD-000146: BeforeBeanDiscovery.addAnnotatedType(AnnotatedType<?>) used for class org.glassfish.jersey.ext.cdi1x.internal.CdiComponentProvider$JaxRsParamProducer is deprecated from CDI 1.1!
Feb 07, 2019 5:14:37 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test.server
FINE: Configured server platform: org.microbean.eclipselink.cdi.CDISEPlatform
Feb 07, 2019 5:14:37 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test
INFO: EclipseLink, version: Eclipse Persistence Services - 2.7.3.v20180807-4be1041
Feb 07, 2019 5:14:37 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test.connection
CONFIG: connecting(DatabaseLogin(
	platform=>H2Platform
	user name=> ""
	connector=>JNDIConnector datasource name=>null
))
Feb 07, 2019 5:14:38 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test.connection
CONFIG: Connected: jdbc:h2:mem:test
	User: SA
	Database: H2  Version: 1.4.197 (2018-03-18)
	Driver: H2 JDBC Driver  Version: 1.4.197 (2018-03-18)
Feb 07, 2019 5:14:38 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test.connection
CONFIG: connecting(DatabaseLogin(
	platform=>H2Platform
	user name=> ""
	connector=>JNDIConnector datasource name=>null
))
Feb 07, 2019 5:14:38 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test.connection
CONFIG: Connected: jdbc:h2:mem:test
	User: SA
	Database: H2  Version: 1.4.197 (2018-03-18)
	Driver: H2 JDBC Driver  Version: 1.4.197 (2018-03-18)
Feb 07, 2019 5:14:38 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test.connection
INFO: /file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test login successful
Feb 07, 2019 5:14:38 PM com.arjuna.common.util.propertyservice.AbstractPropertiesFactory getPropertiesFromFile
WARN: ARJUNA048002: Could not find configuration file, URL was: null
Feb 07, 2019 5:14:38 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test.sql
FINE: SELECT ID, FIRSTPART, SECONDPART FROM GREETING WHERE (FIRSTPART = ?)
	bind => [1 parameter bound]
Feb 07, 2019 5:14:38 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test.connection
CONFIG: disconnect
Feb 07, 2019 5:14:38 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test.connection
INFO: /file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test logout successful
Feb 07, 2019 5:14:39 PM org.jboss.weld.bootstrap.events.BeforeBeanDiscoveryImpl addAnnotatedType
WARN: WELD-000146: BeforeBeanDiscovery.addAnnotatedType(AnnotatedType<?>) used for class org.glassfish.jersey.ext.cdi1x.internal.CdiComponentProvider$JaxRsParamProducer is deprecated from CDI 1.1!
Feb 07, 2019 5:14:39 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test.server
FINE: Configured server platform: org.microbean.eclipselink.cdi.CDISEPlatform
Feb 07, 2019 5:14:39 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test
INFO: EclipseLink, version: Eclipse Persistence Services - 2.7.3.v20180807-4be1041
Feb 07, 2019 5:14:39 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test.connection
CONFIG: connecting(DatabaseLogin(
	platform=>H2Platform
	user name=> ""
	connector=>JNDIConnector datasource name=>null
))
Feb 07, 2019 5:14:39 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test.connection
CONFIG: Connected: jdbc:h2:mem:test
	User: SA
	Database: H2  Version: 1.4.197 (2018-03-18)
	Driver: H2 JDBC Driver  Version: 1.4.197 (2018-03-18)
Feb 07, 2019 5:14:39 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test.connection
CONFIG: connecting(DatabaseLogin(
	platform=>H2Platform
	user name=> ""
	connector=>JNDIConnector datasource name=>null
))
Feb 07, 2019 5:14:39 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test.connection
CONFIG: Connected: jdbc:h2:mem:test
	User: SA
	Database: H2  Version: 1.4.197 (2018-03-18)
	Driver: H2 JDBC Driver  Version: 1.4.197 (2018-03-18)
Feb 07, 2019 5:14:39 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test.connection
INFO: /file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test login successful
Feb 07, 2019 5:14:40 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test.sql
FINE: UPDATE SEQUENCE SET SEQ_COUNT = SEQ_COUNT + ? WHERE SEQ_NAME = ?
	bind => [2 parameters bound]
Feb 07, 2019 5:14:40 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test.sql
FINE: SELECT SEQ_COUNT FROM SEQUENCE WHERE SEQ_NAME = ?
	bind => [1 parameter bound]
Feb 07, 2019 5:14:40 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test.sql
FINE: INSERT INTO GREETING (ID, FIRSTPART, SECONDPART) VALUES (?, ?, ?)
	bind => [3 parameters bound]
Feb 07, 2019 5:14:40 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test.connection
CONFIG: disconnect
Feb 07, 2019 5:14:40 PM org.eclipse.persistence.session./file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test.connection
INFO: /file:/Users/LANELSON/Projects/github/ljnelson/helidon-mp-jpa/target/_test logout successful
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 7.538 s - in io.github.ljnelson.helidon.mp.jpa.TestJPAIntegration
```

Yay!
