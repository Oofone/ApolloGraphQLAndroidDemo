package gql.toys.practice.com.meetceptiongqltest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import okhttp3.OkHttpClient;
import com.resource.skills.api.*;

import static android.app.PendingIntent.getActivity;

/**
 * Here we'll implement all the main functionaries of the application.
 *
 * Goal - To build a simple Auto-Suggest API.
 *
 * We'll take an input from an EditText - (skillEdit)
 * and Query the GQL API to get a list of "skills" that match the ones stored in the Postgres Database.
 *
 */

public class MainActivity extends AppCompatActivity {

    // This is the URL of the ENDPOINT that we're using to host the Hasura GraphQL Engine.
    private static final String BASE_URL = "https://meetception-test.herokuapp.com/v1alpha1/graphql";

    private TextView skillDisplay;
    private EditText skillEdit;

    private ApolloClient apolloClient;

    /**
     * We use this function initClient to return an initialised version of the ApolloClient.
     * It uses an OkHttpClient which we create and assign to the Client object.
     * We also set the endpoint URL here.
     *
     * @return ApolloClient: An ApolloClient object.
     */
    private ApolloClient initClient(){
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();

        return ApolloClient.builder()
                .serverUrl(BASE_URL)
                .okHttpClient(okHttpClient)
                .build();
    }


    /**
     * Here we simply pass in a String to set in a TextView called display.
     * It will display our results.
     *
     * @param out: The string that should be displayed in the TV.
     */
    public void updateDisplay(String out){
        skillDisplay.setText(out);
    }

    /**
     * This funcition accepts the Query String as parameter and performs the query.
     * This function creates a Query object with the Query that was generated from the Queries in the .graphql file.
     * This Class would have been automatically generated by Apollo Codegen.
     *
     * @param example: The query String
     */
    private void getSkill(String example){

        // We'll set the querystring as a parameter to the query.
        SkillsQuery skillsQuery = SkillsQuery.builder()
                .example(example) // this is the parameter.
                .build();

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Requesting Internet Permissions here. NOTE: This step is unnecessary.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    0);
        }

        setContentView(R.layout.activity_main);

        skillDisplay = (TextView)findViewById(R.id.skillDisplay);
        skillEdit = (EditText)findViewById(R.id.skillEdit);

        // Initialize the Apollo Client.
        apolloClient = initClient();

        // Set a Listener for the EditText so that updated query wil be searched.
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
    }
}

