package com.draxvel.firebasechatnew;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class MainActivity extends AppCompatActivity {

    private static int SIGN_IN_REQUEST_CODE = 1;
    private FirebaseListAdapter<Message> adapter;
    private RelativeLayout relativeLayout;
    private RelativeLayout relativeLayoutItem;

    private Button button;

    FirebaseApp firebaseApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseApp = FirebaseApp.initializeApp(this);

        relativeLayout = (RelativeLayout) findViewById(R.id.relative_layout);
        relativeLayoutItem = findViewById(R.id.relative_layout_item);


        button = (Button) findViewById(R.id.buttonSend);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText) findViewById(R.id.editText);

                //відправляємо екземпляр повідомлення в базу
                FirebaseDatabase.getInstance().getReference().push()
                        .setValue(new Message(input.getText().toString(),
                                FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                input.setText("");
            }
        });


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {

            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .build(), SIGN_IN_REQUEST_CODE);

        }else             {
            displayChat();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();

    }

    private void displayChat() {

        ListView listView = (ListView) findViewById(R.id.listView);
        Query query = FirebaseDatabase.getInstance().getReference();

        FirebaseListOptions<Message> options =
                new FirebaseListOptions.Builder<Message>()
                        .setQuery(query, Message.class)
                        .setLayout(R.layout.item)
                        .build();

        adapter = new FirebaseListAdapter<Message>(options) {

            @Override
            protected void populateView(View v, Message model, int position) {

                TextView textMessage, author, timeMessage;
                textMessage = (TextView) v.findViewById(R.id.msg_tv);
                author = (TextView) v.findViewById(R.id.user_tv);
                timeMessage = (TextView) v.findViewById(R.id.time_tv);

                textMessage.setText(model.getTextMessage());
                author.setText(model.getAuthor());
                timeMessage.setText(DateFormat.format("dd-mm-yyyyy {HH:MM::ss}", model.getTimeMessage()));
            }
        };

        listView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_REQUEST_CODE){
            if (requestCode==RESULT_OK){
                Snackbar.make(relativeLayout, "Вхід виконано", Snackbar.LENGTH_SHORT).show();
                displayChat();
            }
            else{
                Snackbar.make(relativeLayout, "Вхід не виконано", Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.exit_menu){
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Snackbar.make(relativeLayout, "Вихід виконано", Snackbar.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        }
        return  true;
    }
}
