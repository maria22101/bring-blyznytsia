# Bring Framework 

## The project is an implementation of Dependency Injection Container:
## You declare the class an object of which type you need and Bring Framework delivers the object of this class!

Just use these annotations:
- **@Configuration** - mark configuration file
- **@ComponentScan** - indicate in the configuration file packages where your classes located
- **@Component, @Bean** - indicate your classes objects of which type you want
- **@Autowired** - mark fields for injection 
- **@Qualifier** - select interface implementation class

In details:

**1. Add framework as a library**

     1.1 pull the project
     1.2 generate .jar (mvn clean install)
     1.3 add it as a dependency to your application:
        <dependency>
            <groupId>com.bobocode</groupId>
            <artifactId>bring-blyznytsia</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
     Note: java version - 15

**2. Configure framework**

     2.1 Create Configuration class
         2.1.1 Create a .java class and annotate it with @Configuration. At least one configuration class is required
         2.1.2 Indicate path(s) where the required classes are located: annotate the configuration class with 
               @ComponentScan and add the path(s) as the annotation's value (a single value or an array) 

     2.2 Indicate classes objects of which type you need by using one of the annotations: 
         - @Component: for annotating a class
         - @Bean: for annotating a method in a Configuration class
         See the requirements for each annotation below

     2.3 Requirements for classes annotated with @Component:
         - Custom-classes-types-fields should be marked for injecton using one of the following approaches:
           2.3.1 annotate the fields with @Autowired
           2.3.2 annotate the fields' setters with @Autowired
           2.3.3 annotate the constructor that sets such fields with @Autowired
               Note: 
               - you may combine 2.3.1 and 2.3.2 approaches
               - if you choose 2.3.3 approach - the fields not to be final and it must be a single constructor of the class 
               - if a field is of an interface type:
                  - there should be the interface implementation(s) in the path(s) indicated on step 2.1.2
                  - in case of a single implementation @Autowired annotation of the interface-type-field is sufficient
                    - otherwise add a name as a value in @Component annotation of the required implementation class 
                      and then, in addition to @Autowired, annotate the interface-type-field with @Qualifier
                      adding the implementation name as its value       
         - default constructor to be present except for approach 2.3.3  
         - circular dependency is not supported
         - interfaces not to be annotated
         
     2.4 Requirements for methods annotated with @Bean:
         - public method of Configuration class
         - return type should be the requested class type
         - an interface as a return type is not supported

**3. Get required objects**

     3.1 Get ApplicationContext instance by Bring.bringContext() -
         it is a container holder that will retrieve objects of the requested classes types
       
     3.2 Get the object you need by calling getBean(“Required class”.class) method on the ApplicationContext instance
         Note: repetitive calls for the class will retrieve the same object of this class type 
