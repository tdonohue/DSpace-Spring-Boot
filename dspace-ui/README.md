# Experiments with Spring Boot UI

## Technologies

* [Spring Boot](http://projects.spring.io/spring-boot/), version 1.2.4
* [Thymeleaf](http://www.thymeleaf.org/) template engine (see source files in /src/main/resources/templates)
* [Bootstrap](http://getbootstrap.com/)

## How to use it

1. Find a pre-installed DSpace Installation Directory (only tested with DSpace 5.x/master)
2. Copy the default `application.properties` for this Spring Boot Application to the root directory. 
  * For example: `cp src/main/resources/application.properties .`
3. Modify that `application.properties`, changing the value of `dspace.dir` to point to your DSpace installation directory 
4. Build it: `mvn clean package`
5. Run it: `java -jar target/dspace-ui-6.0-SNAPSHOT.jar` (this starts the embedded Tomcat on port 8081)
  * You can also run it from any IDE. Just select the `org.dspace.ui.Application` task to run.
6. Access it: http://localhost:8081/test/

## Notes / TODO

* Currently, there's just one page (at "/test"). But it has examples of reading data from all the following:
  * Query params (e.g. "/test?name=Tim")
  * Spring Boot Settings from `application.properties`
  * DSpace configs from `dspace.cfg` (in DSpace installation directory)
  * DSpace top-level communities
  * See `org.dspace.ui.controller.TestController` & `src/main/resources/templates/test.html` for Test Controller & View respectively
* Layout provided by `src/main/resources/templates/layout.html`. The header/footer fragments are defined there, and inported into `test.html`.
* Pre-minified Bootstrap & jQuery are automatically included via Maven dependencies (using http://webjars.org/)