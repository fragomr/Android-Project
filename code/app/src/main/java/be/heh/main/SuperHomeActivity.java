package be.heh.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.view.View;
import android.support.design.widget.FloatingActionButton;
import android.widget.TextView;

import java.util.HashMap;

import be.heh.databases.UserAccessDB;
import be.heh.session.Session;

public class SuperHomeActivity extends Activity {

    private Session session;
    TextView tv_superHome_email;
    TextView tv_superHome_users;
    FloatingActionButton fab_superHome_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_home);

        session = new Session(getApplicationContext());

        tv_superHome_email = findViewById(R.id.tv_superHome_email);
        tv_superHome_users = findViewById(R.id.tv_superHome_users);

        fab_superHome_logout = findViewById(R.id.fab_superHome_logout);

        // If not logged in, redirection to LoginActivity
        if (session.checkLogin()) {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        HashMap<String, String> user = session.getUserDetails();

        UserAccessDB userDB = new UserAccessDB(this);
        userDB.openForWrite();
        int nbUsers = userDB.getNumberOfUsers();
        String nbRUsers = userDB.getRUsers();
        String nbRWUsers = userDB.getRWUsers();
        userDB.Close();

        tv_superHome_email.setText(Html.fromHtml("Connecté en tant que '<b>" + user.get(Session.KEY_EMAIl) + "</b>'."));
        tv_superHome_users.setText(Html.fromHtml("Il y a <b>"+ nbUsers +
                "</b> utilisateurs dont :<br><br>" +
                "<b>"+ nbRUsers + "</b> avec un accès en lecture seule;<br>" +
                "<b>" + nbRWUsers + "</b> avec un accès en lecture et écriture."));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (session.checkLogin()) {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        UserAccessDB userDB = new UserAccessDB(this);
        userDB.openForWrite();
        int nbUsers = userDB.getNumberOfUsers();
        String nbRUsers = userDB.getRUsers();
        String nbRWUsers = userDB.getRWUsers();
        userDB.Close();

        tv_superHome_users.setText(Html.fromHtml("Il y a <b>"+ nbUsers +
                "</b> utilisateurs dont :<br><br>" +
                "<b>"+ nbRUsers + "</b> avec un accès en lecture seule;<br>" +
                "<b>" + nbRWUsers + "</b> avec un accès en lecture et écriture."));

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void onSuperHomeClickManager(View v) {

        switch (v.getId()) {
            case R.id.btn_superHome_users:
                Intent intentListUsers = new Intent(this, ListUsersActivity.class);
                startActivity(intentListUsers);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            case R.id.fab_superHome_logout:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Déconnexion")
                        .setMessage("Voulez-vous vraiment vous déconnecter ?")
                        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                session.logoutUser();
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            }
                        })
                        .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create().show();

                break;
        }
    }

}
