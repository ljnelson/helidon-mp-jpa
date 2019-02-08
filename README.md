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
    
TODO: more
