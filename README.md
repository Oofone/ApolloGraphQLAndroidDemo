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
|In-Development|verified|1.0.1-SNAPSHOT|
|Post-Release|verified|1.0.0-alpha2|
|Post-Release|verified|1.0.0-alpha|
|Pre-Release|verified|0.5.0|
|Pre-Release|verified|v0.4.4|

To use the latest development snapshot you'll need to add the following maven repository in your **project** build.gradle file:

```
repositories {
    jcenter()
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots/' }
  }
```

To the **app** build.gradle file add:

```
apply plugin: 'com.apollographql.android'
```

Ensure that it's after `apply plugin: 'com.android.application'
`, which would generally be the first line.

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
## Posting Queries and Receiving Data

To start making queries we'll first need to make a OkHttpClient. If you're familiar with OKHttp you'll understand how the builder() function works.

```java
OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();
```

Now we'll use this client to build an ApolloClient object.
This object will be central to all your GraphQL function implementations.

```java
private ApolloClient initClient(){
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();

        return ApolloClient.builder()
                .serverUrl(BASE_URL)
                .okHttpClient(okHttpClient)
                .build();
}
```

Here **BASE_URL** represents your GraphQL server enpoint. Simpy create a **String** constant with your URL and use that.

Now this ApolloClient can be used for any query or mutation you wish to run.

Now to actually **post the query**.

Each query you defined in your **.graphql** file will have a Class generated for it. This class will have a builder() function. If you build the query with that function you will have an implementation of the Query itself.

```java
SkillsQuery skillsQuery = SkillsQuery.builder()
                .example(example) // this is the parameter.
                .build();
```

Now we'll use ApolloClient to queue the query and send it. It'll involve creating a QueryCall object and then defining the callback functions for the response. The two methods of handling the response are **using JavaRx** and **Callbacks**. I'll specify how to use Callbacks here.

```java
ApolloCall<SkillsQuery.Data> skillsQueryCall = apolloClient.query(skillsQuery);

skillsQueryCall.enqueue(new ApolloCall.Callback<SkillsQuery.Data>() {
    @Override
    public void onResponse(@NotNull Response<SkillsQuery.Data> response) {
        SkillsQuery.Data data = response.data();
        List<SkillsQuery.Skill> skillList = data.skills();
        String out = "";

        // We convert the list into an elongated string.
        for(int i = 0; i < skillList.size(); i++){
            out += "Skill Name: " + skillList.get(i).name() + " id: " + skillList.get(i).id();
        }

        if (skillList.size() == 0){
            out = "No Such Skills";
        }

        final String outf = out;

        Log.d("API Response", data.toString());

        // We run the UI Update on the UI Thread.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateDisplay(outf);
            }
        });
    }

    @Override
    public void onFailure(@NotNull ApolloException e) {
        Log.d("Problem", "The Skill API Call Failed");
    }
});
```

When we receive a response we'll create a list of Strings that are completed and print them in the TextField. On failure we'll log the error.

**For the sake of completion** I'll now explain when the query is made:

We'll listen for changes to the EditText field where we want to implement Auto-Suggest. Here *skillEdit* is that EditText.

```java
skillEdit.addTextChangedListener(new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
       Log.d("KeyUp", "Key Up event detected. Calling API.");
       getSkill(s.toString() + "%");
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
 });
```

Everytime the text-box is edited we'll create the query "<str>%" which means 0 or more characters other than ' ' after the input substring.

This is the entire implementation of the demo. 
