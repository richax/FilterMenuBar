package com.example.x.compoundselector;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Node node = new Gson().fromJson(getResources().getString(R.string.single_node), Node.class);
        Node node1 = new Gson().fromJson(getResources().getString(R.string.two_node), Node.class);

        FilterMenuBar filterMenuBar = (FilterMenuBar) findViewById(R.id.fiterBar);
        filterMenuBar.setMenuItems(Arrays.asList(node, node1));
        filterMenuBar.appendMenuItem(node);
        filterMenuBar.setOnFilterItemSelectedListener(new FilterMenuBar.OnFilterItemSelectedListener() {
            @Override
            public void onFilterItemSelected(List<List<Node>> selectedGroups, int invokedGroupIndex) {

                StringBuilder result = new StringBuilder("Invoked index : " + invokedGroupIndex + "\n");
                for (List<Node> nodeList : selectedGroups) {
                    result.append("Group\n");
                    for (Node node : nodeList) {
                        result.append("\t").append(node.toString()).append("\n");
                    }
                }
                System.out.println(result.toString());
                Toast.makeText(MainActivity.this, result.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
