# DSpace Spring Boot UI Prototype
A submission for the [2015 DSpace UI Prototype Challenge](https://wiki.duraspace.org/display/DSPACE/DSpace+UI+Prototype+Challenge)

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
* DSpace Java API, lastest master branch (pre-6.0) as of Nov 2015 AND the Apache Commons Configuration feature ([PR#1104](https://github.com/DSpace/DSpace/pull/1104)) added 

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

## Scope of this Prototype

- [x] User Interface Layout
  * _Header/Footer:_ Has an easily customizable header/footer. See [`src/main/resources/templates/layout.html`](https://github.com/tdonohue/DSpace-Spring-Boot/blob/spring-boot-ui/dspace-ui/src/main/resources/templates/layout.html) which provides Thymeleaf header/footer "fragments" which are included in all other pages.
  * _Breadcrumbs:_ Has basic breadcrumbs which appear on all pages in header. They are also displayed via the `layout.html` and are generated via the [`DSpaceController`](https://github.com/tdonohue/DSpace-Spring-Boot/blob/spring-boot-ui/dspace-ui/src/main/java/org/dspace/ui/controller/DSpaceController.java) (which all other controllers extend)
  * _Menu/Sidebar:_ For now, this is just implemented in the header. It could be moved to another location (via a custom `layout.html`. It does not yet have context-sensitive menus, except for an "Admin" menu which only appears if you are an ADMIN.
  * _URLs:_ Tried to keep to the basic URL structure of DSpace in general, and browser buttons all work.
  * _Responsive:_ Prototype uses the default Bootstrap theme and is responsive.
- [x] Simple Item View
  * Prototype has a simple item view page ([`item.html`](https://github.com/tdonohue/DSpace-Spring-Boot/blob/spring-boot-ui/dspace-ui/src/main/resources/templates/item.html))
  * `ItemController` uses the DSpace Java API to populate the display
  * Currently it just displays a few key metadata fields along with links to download any bitstreams
  * If you are authenticated into the system, an "Edit" button appears in the upper right.
- [x] Community/Collection Views
  * Prototype has simple Community/Collection browse/view pages ([`community.html`](https://github.com/tdonohue/DSpace-Spring-Boot/blob/spring-boot-ui/dspace-ui/src/main/resources/templates/community.html) and [`collection.html`](https://github.com/tdonohue/DSpace-Spring-Boot/blob/spring-boot-ui/dspace-ui/src/main/resources/templates/community.html))
  * Controller uses the DSpace Java API to populate the display
  * Again, they just display basic metadata along with a logo (if provided)
- [x] Browse Navigation
  * Prototype just uses drill-down navigation (no Browse by title/author/subject yet). So, you can step into Top Level Communities and then drill-down to Items within Collections.
- [x] Authentication
  * Prototype uses Spring Security in-memory authentication by default. This means there are just two hardcoded valid users:
    * username: 'user', password: 'dspace' (a USER role account)
    * username: 'admin', password: 'dspace' (an ADMIN role account)
  * While in-memory authentication was chosen based on time constraints, Spring Security provides a variety of plugins for other auth systems (LDAP, Shibboleth, OAuth, Database-based, etc)
- [x] Authorization
  * Also via Spring Security
  * Certain areas of the application are access restricted
     * When logged in as an ADMIN, the "Admin" menu appears in the header, and you have access to an 'admin.html' page
     * When logged in as a USER, an "Edit" button appears on all items and you have access to an Edit Item page
     * Authorization roles are hierarchical. All ADMINs are also USERs.
- [ ] Edit/Create Item
  * While an access-restricted Item Edit form exists, it is not currently functional. It just shows off what a form could look like, but the saving aspect is not yet implemented.

## Customization Capabilities

- [x] CSS-level customizations
  * Basic CSS-driven themes are already implemented in this Prototype. By default, the `layout.html` loads up a `default.css` file. However, you can customize that site-wide or per PATH/URL via an example config in [`application.properties`](https://github.com/tdonohue/DSpace-Spring-Boot/blob/spring-boot-ui/dspace-ui/src/main/resources/application.properties)
     * `dspace.theme=default` (Specify the site-wide CSS to use)
     * `dspace.theme.handle.10673.2=blue-header` (Specifies a DIFFERENT "blue-header.css" for the /handle/10673/2 path, and any objects that include that path in their breadcrumbs)
     * These settings are fully working, and you can play with them to add different themes to different objects. Note that a theme specified for object "10673/2" will be inherited automatically by all child objects.
- [x] Sitewide header/footer customizations
  * The sitewide header/footer are both contained in the [`layout.html`](https://github.com/tdonohue/DSpace-Spring-Boot/blob/spring-boot-ui/dspace-ui/src/main/resources/templates/layout.html). You can easily customize that file, or provide a different `layout.html` to change the overall theme of your site.
- [x] Adjusting Navigation Bar
  * Navigation is also included in `layout.html` and can be adjusted there. It also could be adjusted via CSS changes, via Bootstrap CSS.
  * Admittedly, navigation is currently extremely basic in this prototype. So, ideally, we'd want to ensure the layout.html does fully allow Navigation to appear in header or left/right side, based on local needs.
- [x] Adjusting Breadcrumbs
  * Breadcrumbs are also included in `layout.html` and can be adjusted there.
- [x] Additional metadata in Item View page
  * The Item View page is generated by [`item.html`](https://github.com/tdonohue/DSpace-Spring-Boot/blob/spring-boot-ui/dspace-ui/src/main/resources/templates/item.html). It has some default metadata fields already in there. But, as you can see, others can be easily added by simply calling `itemModel.getMetadataValues(field)`.
  * Obviously, since this page is essentially HTML, the overall layout of the page can also be adjusted easily by anyone familiar with HTML / CSS / Bootstrap.
- [x] New links in Navigation
  * Again, this could be adjusted in `layout.html` by anyone who is comfortable with HTML / CSS / Bootstrap.
