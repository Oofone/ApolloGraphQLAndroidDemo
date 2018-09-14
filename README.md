# ApolloGraphQLAndroid
An example of the ApolloClient for GraphQL Android. Connects to a custom endpoint hosting a Hasura GraphQL Engine.

A lot of examples/getting started gists exist out there. But here's a simple and hopefully updated version of the implementation.

## Getting Started

### Setting up Dependancies.
</br>
To the **project** build.gradle file add:
```
classpath 'com.apollographql.apollo:apollo-gradle-plugin:<VERS>'
```

Here the current tested release as of writing this README is **1.0.0-alpha2**. </br>
It's usually as follows:

|Type|Verified/Unverified |Release|
|-|-|-|
|Experimental|verified| |
|Post-Release|verified|1.0.0-alpha2|
|Post-Release|verified|1.0.0-alpha|
|Pre-Release|verified|0.5.0|
|Pre-Release|verified|v0.4.4|

To the **app** build.gradle file add:
```
apply plugin: 'com.apollographql.android'
```

Ensure that it's after `apply plugin: 'com.android.application'
`, which should be first.

If you want to use features like *caching* ensure that you add this runtime dependancy to your **app** build.gradle file as well:

```
implementation 'com.apollographql.apollo:apollo-runtime:1.0.0-alpha2'
```

### Setting Up Queries

Apollo will use your GraphQL server's schema and some queries you're setting up to automatically generate code for representing classes for each query.

Create a directory structure similar to the one shown below.

Within your **main** directory create some directory to hold your base graphql queries. Mine is simply called graphql.

```
└── app
    └── src
        └── main
            └── graphql
                └── com
                    └── your
                        └── package_name
                            └── <filename>.graphql

```
In this sample the file structure is as follows:

```
└── app
    └── src
        └── main
            └── graphql
                └── com
                    └── resource
                        └── skills
                            └── api
                                ├── queries.graphql
                                └── schema.json
```

**Note**: Here com.your.package_name is the package that I've designated for the auto-generated code that Apollo will generate for me. Feel free to change that to whatever works for you.

#### The .graphql File

So within the graphql directory at some location your .graphql file needs to exist and it should hold some universal query for GraphQL.

Here we're storing a simple query to implement a sort of *auto-suggest field*. In essence each time the EditText (Text Box) is edited, we'll query it's contents with a simple "like" comparison.

```graphql
query SkillsQuery($example:String!){
  skills(where: {name: {_like: $example}}){
    id
    name
  }
}
```

#### The schema.json File

This file is specific to your individual database. It's the generic response your graphql server will respond to schema requests with.

You can check [this great article](https://www.apollographql.com/docs/graphql-tools/generate-schema.html) for in-depth information about how to generate your Database's schema.json.

If you're using the Hasura GraphQL Engine as well then you can simply click on the settings cog on the top right and select the button that says "Export metadata".

This file can be saved as schema.json.

### Automatic Code-Gen

Now that we have our queries and our schema.json set up, we can simply click on:

 [Build]() -> [Rebuild project]()

 At this point Apollo will automatically generate Classes for your individual queries and you can view **but not edit** them at this path:

 ```
 └── app
     └── build
         └── generated
             └── source
                 └── apollo
 ```

 **Now if you want to import these classes into your projects:**

 Simply include this import statement:

 ```java
 // Generically:
 import com.your.package_name.*; // Replace the * with your Query's name.

 // Specific to this example:
 import com.resource.skills.api.SkillsQuery;
 ```
