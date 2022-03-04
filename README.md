# Integration tests for the mobility dataspace

Run ```mvn test``` to start the tests.

## Adding a new DSC version

The easiest way to add a new DSC Version is to use one of the existing tests classes , like DSC_7_0_0_Test class, and
make a copy of it and replace the image name with the image name of a new DSC version.

In addition it might make sense to copy the config.json of the new version to
```src/main/test/resources``` and update the path to this new config.json.

### Optionally: Updating the DSC client libs

The tests are testing against the backend- aka. admin API of the DSC. To invoke the API, Micronaut HTTP client classes
are generated out of the OpenAPI specification of the DSC.

You can find the used OpenAPI DSC spec in the ```src/main/resources/openapi``` folder.

The HTTP client classes are generated out of the specification in the Maven build. The following plugin configuration
creates the DSC 5.1 client classes used in the tests:

```
<plugin>
     <groupId>org.openapitools</groupId>
     <artifactId>openapi-generator-maven-plugin</artifactId>
     <version>5.1.1</version>
     <executions>
         <execution>
             <id>dsc_5_1</id>
             <goals>
                <goal>generate</goal>
             </goals>
             <configuration>
                  <skipValidateSpec>false</skipValidateSpec>
                  <strictSpec>false</strictSpec>
                  <generatorName>micronaut</generatorName>
                  <output>${project.build.directory}</output>
                  <inputSpec>${project.basedir}/src/main/resources/openapi/dsc-openapi-5_1.yaml</inputSpec>
                  <modelPackage>de.fhg.ivi.ids.dsc_5_1.model</modelPackage>
                  <apiPackage>de.fhg.ivi.ids.dsc_5_1.api</apiPackage>
                  <invokerPackage>de.fhg.ivi.ids.dsc_5_1.invoker</invokerPackage>
                  <generateAliasAsModel>true</generateAliasAsModel>
                  <configOptions>
                       <useReferencedSchemaAsDefault>true</useReferencedSchemaAsDefault>
                       <clientId>dsc</clientId>
                       <useOptional>true</useOptional>
                       <jacksonDatabindNullable>false</jacksonDatabindNullable>
                  </configOptions>
             </configuration>
         </execution>
     </executions>
     <dependencies>
         <dependency>
             <groupId>io.kokuwa.micronaut</groupId>
             <artifactId>micronaut-openapi-codegen</artifactId>
             <version>${version.micronaut-openapi-codegen}</version>
         </dependency>
     </dependencies>
</plugin>
```

To add additional HTTP client classes for a new version of the DSC do the following
  * copy the openapi.yml file from the new DSC version to ```src/main/resources/openapi```
    (you can find the openapi.yml in the root directory of the DSC source repository) 
  * in the ```pom.xml``` file, add an additional configuration section for the new version 
    (the variables in curly braces {...} have to be replaced with your version):

```
<configuration>
      <skipValidateSpec>false</skipValidateSpec>
      <strictSpec>false</strictSpec>
      <generatorName>micronaut</generatorName>
      <output>${project.build.directory}</output>
      <inputSpec>${project.basedir}/src/main/resources/openapi/{new_dsc_openapi_version}.yaml</inputSpec>
      <modelPackage>de.fhg.ivi.ids.dsc_{new_dsc_version}.model</modelPackage>
      <apiPackage>de.fhg.ivi.ids.dsc_{new_dsc_version}.api</apiPackage>
      <invokerPackage>de.fhg.ivi.ids.dsc_{new_dsc_version}.invoker</invokerPackage>
      <generateAliasAsModel>true</generateAliasAsModel>
      <configOptions>
           <useReferencedSchemaAsDefault>true</useReferencedSchemaAsDefault>
           <clientId>dsc</clientId>
           <useOptional>true</useOptional>
           <jacksonDatabindNullable>false</jacksonDatabindNullable>
      </configOptions>
</configuration>
```

The build might fail due to things in the openapi.yml file that the generator cannot interpret.
In this case you have to update the openapi.yml a little bit to get the genrator work.