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
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import com.resource.skills.api.*;

import static android.app.PendingIntent.getActivity;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://meetception-test.herokuapp.com/v1alpha1/graphql";
    private static String display;

    private TextView skillDisplay;
    private EditText skillEdit;

    private ApolloClient apolloClient;

    private ApolloClient initClient(){
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();

        return ApolloClient.builder()
                .serverUrl(BASE_URL)
                .okHttpClient(okHttpClient)
                .build();
    }

    public void updateDisplay(String out){
        skillDisplay.setText(out);
    }

    private void skiller(String example){
        SkillsQuery skillsQuery = SkillsQuery.builder()
                .example(example)
                .build();

        ApolloCall<SkillsQuery.Data> skillsQueryCall = apolloClient.query(skillsQuery);

        skillsQueryCall.enqueue(new ApolloCall.Callback<SkillsQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<SkillsQuery.Data> response) {
                SkillsQuery.Data data = response.data();
                List<SkillsQuery.Skill> skillList = data.skills();
                String out = "";

                for(int i = 0; i < skillList.size(); i++){
                    out += "Skill Name: " + skillList.get(i).name() + " id: " + skillList.get(i).id();
                }

                if (skillList.size() == 0){
                    out = "No Such Skills";
                }

                final String outf = out;

                Log.d("API Response", data.toString());
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    0);
        }

        setContentView(R.layout.activity_main);
        display = "";

        skillDisplay = (TextView)findViewById(R.id.skillDisplay);
        skillEdit = (EditText)findViewById(R.id.skillEdit);

        apolloClient = initClient();

        skillEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("KeyUp", "Key Up event detected. Calling API.");
                skiller(s.toString() + "%");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}

