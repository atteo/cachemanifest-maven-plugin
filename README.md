Arguments
=====

- manifestPath - where to put yout manifest file
- manifestVersion - manifest file version, will be included in a comment in the file. If left empty version will be taken from the pom. If pom version is a snapshot a timestamp
  will be added to it
- fileResources - add a tree to the manifest (see example below)
- resourcesReferencedFrom - add scripts and css stylesheets referenced from an html file
- resources - add resources manually
- networkResources - network section
- fallback - fallback section

Changes
=======

Version 0.3

- always use '/' in URLs

Version 0.2

- initial release


Example
===========

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.atteo</groupId>
            <artifactId>cachemanifest-maven-plugin</artifactId>
            <version>0.3</version>
            <executions>
                <execution>
                    <phase>process-resources</phase>
                    <goals>
                        <goal>generate-manifest</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <manifestPath>${project.build.directory}/classes/META-INF/resources/webapp/application.cachemanifest</manifestPath>

                <!-- main app files -->
                <fileResources>
                    <fileResource>
                        <directory>${project.build.directory}/classes/META-INF/resources/webapp</directory>
                        <includes>
                            <include>app/**/*.*</include>
                        </includes>
                        <excludes>
                            <exclude>app/scss/**/*.*</exclude>
                        </excludes>
                    </fileResource>
                </fileResources>

                <!-- webjars -->
                <resourcesReferencedFrom>
                    <param>src/main/webapp/index.html</param>
                </resourcesReferencedFrom>

                <!-- resources referenced from outside CSS files -->
                <resources>
                    <resource>app/css/fontello/font/fontello.woff?69525316</resource>
                    <resource>app/css/fontello/font/fontello.ttf?69525316</resource>
                    <resource>app/css/fontello/font/fontello.svg?69525316</resource>
                    <resource>app/css/fonts/font-awesome/fontawesome-webfont.woff?v=4.0.3</resource>
                    <resource>app/css/fonts/font-awesome/fontawesome-webfont.ttf?v=4.0.3</resource>
                    <resource>app/css/fonts/font-awesome/fontawesome-webfont.svg?v=4.0.3</resource>
                </resources>

                <networkResources>
                    <param>*</param>
                </networkResources>
            </configuration>
        </plugin>
    </plugins>
</build>


```

License
===========

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
