# Welcome to the Spring Boot UI


## Overview / Technologies

This GitHub project is an exact clone of the DSpace 'master' branch (pre-6.0) with one new Maven Project:

`dspace-ui` is the Spring Boot UI Prototype, it is built on the following technologies:

* [Spring Boot](http://projects.spring.io/spring-boot/), version 1.3.0
  * Currently, it is configured to use Spring Boot's embedded Tomcat, so no need to install Tomcat
* [Thymeleaf](http://www.thymeleaf.org/) template engine (see source files in `/src/main/resources/templates`)
  * Thymeleaf was chosen because it's a powerful template engine that just uses HTML files (with some custom syntax)
* [Bootstrap](http://getbootstrap.com/)
  * This prototype just uses the default/example Bootstrap theme. It obviously could be customized further to look like Mirage2 or other Bootstrap based themes.
  * Pre-minified Bootstrap & jQuery are automatically included via Maven dependencies (using http://webjars.org/)
* [Spring Security](http://projects.spring.io/spring-security/) for basic authentication/authorization
  * Spring Security was chosen as it's a best practice for Spring Boot. It also has modules to support most every type of authentication imaginable (DB based, LDAP, Shibooleth, OAuth, etc).
  * Plus it provides very useful Authorization tools/roles that are easy to use within Spring Boot
* DSpace Java API, lastest master branch (pre-6.0) as of Feb 2016.

(NOTE: despite this being a DSpace master clone, all modules EXCEPT `dspace-ui`, `dspace-api` and `dspace-services` are disabled in the build)

## How to run it

1. Find a (pre-6) DSpace Installation Directory (Unfortunately, this won't work with DSpace 5 as it requires the Service API refactor).
  * If you don't have one, you can run `mvn clean package; cd dspace/target/dspace-installer; ant fresh_install` to create one.
2. Move into the DSpace UI folder
  * `cd dspace-ui`
2. Copy the default `application.properties` for this Spring Boot Application to the root directory. 
  * For example: `cp src/main/resources/application.properties .`
3. Modify that `application.properties`, changing the value of `dspace.dir` to point to your DSpace installation directory 
4. Build the dspace-ui module: `mvn clean package`
5. Run it: `java -jar target/dspace-ui-6.0-SNAPSHOT.jar` (This starts the embedded Tomcat on port 8080 by default)
  * You can also run it directly from any IDE. Just select the `org.dspace.ui.Application` task to run.
  * When running from the IDE, it will use `src/main/resources/application.properties` by default.
6. Access it: http://localhost:8080/


## UPDATES

As of Feb 2016, this Prototype was also enhanced to show off some basic "REST-like" capabilities of Item view pages.

Simply visit an Item View page and click the "JSON View" or "XML View" buttons. They give a view of the same underlying data, in JSON or XML format, respectively.
For the code behind this, see the ItemController class.

This feature shows off the ability to perform ["content negotiation" using Spring Boot/MVC](https://spring.io/blog/2013/05/11/content-negotiation-using-spring-mvc).
The goal is to demonstrate how we could refactor the existing REST API so that it actually becomes a part of the new UI, and both would share the same underlying business logic.
