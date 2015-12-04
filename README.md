# DSpace Spring Boot UI Prototype
A submission for the [2015 DSpace UI Prototype Challenge](https://wiki.duraspace.org/display/DSPACE/DSpace+UI+Prototype+Challenge)

## Overview / Technologies

This GitHub project is an exact clone of the DSpace 'master' branch (pre-6.0) with one new Maven Project:

`dspace-ui` is the Spring Boot UI Prototype, it is built on the following technologies:

* [Spring Boot](http://projects.spring.io/spring-boot/), version 1.3.0
  * Currently, it is configured to use Spring Boot's embedded Tomcat, so no need to install Tomcat
* [Thymeleaf](http://www.thymeleaf.org/) template engine (see source files in /src/main/resources/templates)
* [Bootstrap](http://getbootstrap.com/)
* DSpace Java API, lastest master branch (pre-6.0) as of Nov 2015 AND the Apache Commons Configuration feature ([PR#1104](https://github.com/DSpace/DSpace/pull/1104)) added 

(NOTE: despite this being a DSpace master clone, all modules EXCEPT `dspace-ui`, `dspace-api` and `dspace-services` are disabled in the build)

## How to use it

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

## Notes / TODO

* Currently, only the following basic DSpace features are implemented:
  * Site-wide Breadcrumbs
  * Basic Homepages for Community, Collection and Item objects
  * Very basic I18N example (on Item Homepage)
  * Very basic Themes (corresponding to CSS files), which can be configured globally or per Community/Collection/Item (see "dspace.theme.*" setting in application.properties)
* Layout provided by `src/main/resources/templates/layout.html`. The header/footer fragments are defined there, and imported into all pages.
  * Currently the layout is just a default Bootstrap theme.
* Pre-minified Bootstrap & jQuery are automatically included via Maven dependencies (using http://webjars.org/)
