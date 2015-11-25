# Experiments with Spring Boot UI

## Technologies

* [Spring Boot](http://projects.spring.io/spring-boot/), version 1.3.0
* [Thymeleaf](http://www.thymeleaf.org/) template engine (see source files in /src/main/resources/templates)
* [Bootstrap](http://getbootstrap.com/)
* DSpace Java API (pre-6 version with Service API refactor AND the Apache Commons Configuration feature)

## How to use it

1. Find a pre-installed (pre-6) DSpace Installation Directory (Unfortunately, this won't work with DSpace 5 as it requires the Service API refactor).
2. Copy the default `application.properties` for this Spring Boot Application to the root directory. 
  * For example: `cp src/main/resources/application.properties .`
3. Modify that `application.properties`, changing the value of `dspace.dir` to point to your DSpace installation directory 
4. Build it: `mvn clean package`
5. Run it: `java -jar target/dspace-ui-6.0-SNAPSHOT.jar` (this starts the embedded Tomcat on port 8080)
  * You can also run it from any IDE. Just select the `org.dspace.ui.Application` task to run.
6. Access it: http://localhost:8080/

## Notes / TODO

* Currently, only the following basic DSpace features are implemented:
  * Site-wide Breadcrumbs
  * Basic Homepages for Community, Collection and Item objects
  * Very basic I18N example (on Item Homepage)
* Layout provided by `src/main/resources/templates/layout.html`. The header/footer fragments are defined there, and inported into all pages.
  * Currently the layout is just a default Bootstrap theme.
* Pre-minified Bootstrap & jQuery are automatically included via Maven dependencies (using http://webjars.org/)