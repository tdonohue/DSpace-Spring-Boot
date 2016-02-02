# DSpace Spring Boot UI Prototype
A submission for the [2015 DSpace UI Prototype Challenge](https://wiki.duraspace.org/display/DSPACE/DSpace+UI+Prototype+Challenge)

***WARNING: Force-updated to latest master (as of Feb 1, 2016). Unfortunately this required a force push in order to rebase on top of the latest [DSpace master code](https://github.com/DSpace/DSpace/).***

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

1. Find a (pre-6) DSpace Installation Directory (Unfortunately, this won't work with DSpace 5 as it requires the Service API refactor and the [Enhanced Configuration Scheme](https://wiki.duraspace.org/display/DSPACE/Enhanced+Configuration+Scheme)).
  * If you don't have one, you can run `mvn clean package; cd dspace/target/dspace-installer; ant fresh_install` to create one.
    * Please note that you may also need to create a `local.cfg` file (if you haven't already) to support the DSpace 6 [Enhanced Configuration Scheme](https://wiki.duraspace.org/display/DSPACE/Enhanced+Configuration+Scheme).
  * You'll also need some content, as this prototype doesn't yet have editing/creating capabilities built in. So, either use another UI or an AIP restore.
2. Move into the DSpace UI folder
  * `cd dspace-ui`
2. Copy the default `application.properties` for this Spring Boot Application to the root directory. 
  * For example: `cp src/main/resources/application.properties .`
  * If you wish to run this application in your IDE, you also can directly edit the `src/main/resources/application.properties` as needed.
3. Modify that `application.properties`, changing the value of `dspace.dir` to point to your DSpace installation directory 
4. Build the dspace-ui module: `mvn clean package`
5. Run it: `java -jar target/dspace-ui-6.0-SNAPSHOT.jar` (This starts the embedded Tomcat on port 8080 by default, or whatever port is specified in your `application.properties` file)
  * You can also run it directly from any IDE. Just select the `org.dspace.ui.Application` task to run.
  * When running from the IDE, it will use `src/main/resources/application.properties` by default.
6. Access it: http://localhost:8080/
7. If you want to customize the theming, you can modify your `[dspace]/config/local.cfg` with the following settings (by default your `local.cfg` will be auto-reloaded every minute):
```
# Custom Theme settings.
# This defines our default site-wide theme
# For example setting to "default" just uses the theme named default
dspace.theme=default

# Different themes can be specified per path
# If this specifies a Handle of a Community or Collection, the theme will
# automatically be inherited to child objects
#
# This sample just loads "blue-header" theme (which makes the header blue like Mirage2)
# for the 10673/2 handle AND any child objects
# Other sample values include "red-header", "blue-header", "default"
dspace.theme.handle.10673.2=blue-header
dspace.theme.handle.10673.3=red-header
```

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
  * Basic themes are already implemented in this Prototype. By default a "default" theme is used which has its own `layout.html` and `styles.css`. However, two other themes are available "blue-header" and "red-header". You can change the theme  site-wide or per PATH/URL by adding one of the following to your `local.cfg` 
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


## Modularization Capabilities

* Support for optional modules/features/add-ons (These are all brainstorms and not yet implemented)
   * All modules should be Spring-Boot-enabled..this means the following:
     * If they create their own page(s), they need to define their own Spring Boot Controllers (`@Controller`) & Views (Thymeleaf HTML pages). Spring Boot will automatically recognize the Controllers and include them.
     * Backend classes/beans should be Spring enabled (again so they are auto-discovered)
   * If a module/addon needed to modify or insert content into an existing page, it likely would need to define one (or more) Thymeleaf HTML fragments (examples `layout.html`) which could be included into whatever pages need them. This might mean a small amount of manual editing of an existing page (to insert that include statement).
     * For example, if new fields were needed to be displayed in the Item View (`item.html`), then it might involve adding a new `<div th:include ..>` into that file manually
   * Or, it might be possible to drive specific content via database tables
     * For example, if most modules just add new menus/links, then the list of available menus/links could be driven via a database table. New modules could just add additional options to that table, and they'd be displayed dynamically in the site menu via the `layout.html`
     * It's also possible to store Thymeleaf templates/fragments in database tables: http://blog.kaczmarzyk.net/2015/01/04/loading-view-templates-from-database-with-thymeleaf/

## Additional Prototype Documentation

- [x] Prototype Design is described at the top of this page
- [x] Prototype Installation is also described above
- [x] Internationalization (i18n)
   * The prototype already includes some basic internationalization examples (using the built in i18n options in Spring Boot + Thymeleaf)
   * i18n is controlled via a [`messages.properties`](https://github.com/tdonohue/DSpace-Spring-Boot/blob/spring-boot-ui/dspace-ui/src/main/resources/i18n/messages.properties) file
   * Currently, it's only used/enabled on the Item View page ([`item.html`](https://github.com/tdonohue/DSpace-Spring-Boot/blob/spring-boot-ui/dspace-ui/src/main/resources/templates/item.html)). For example, this attribute: `th:text="#{item.label.bitstreams}"` says to change the text of the given HTML field to be the value of `item.label.bitstreams` from `messages.properties`
 - [x] Additional Theming Capabilities
   * Basic (CSS/layout) theming is already provided in the prototype, along with a few sample themes
   * It might even be possible to find a way to override default templates (e.g. `item.html`) by [creating additional Thymeleaf template resolvers](http://stackoverflow.com/a/25588429/3750035) (to pull templates from multiple areas)
 - [x] Support for common DSpace Authentication mechanisms
   * If we went with Spring Boot, I'd recommend we move Authentication & Authorization entirely to Spring Security
   * It already provides modules supporting all our major Authentication options (database-based, LDAP, Shibboleth, etc), plus other new ones (OAuth).
   * It also provides a highly configurable Authorization framework, which integrates much better with Spring Boot and Spring in general.
