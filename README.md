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

So step one is to write a JAX-RS application.

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

TODO: more
