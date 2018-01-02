package be.heh.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import be.heh.database.AutomatonAccessDB;
import be.heh.models.Automaton;
import be.heh.models.CurrentAutomaton;
import be.heh.models.Session;

public class AutomatonPillsActivity extends Activity {

    private Session session;
    private CurrentAutomaton currentAutomaton;
    String automatonName;
    Automaton automaton;

    FloatingActionButton fab_automatonPills_connect;
    FloatingActionButton fab_automatonPills_logout;

    TextView tv_automatonPills_connected;
    TextView tv_automatonPills_status;
    TextView tv_automatonPills_plc;
    TextView tv_automatonPills_bottles;
    TextView tv_automatonPills_pills;

    private NetworkInfo network;
    private ConnectivityManager connexStatus;
    private ReadTaskS7 readS7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automaton_pills);

        session = new Session(getApplicationContext());
        currentAutomaton = new CurrentAutomaton(getApplicationContext());

        tv_automatonPills_connected = findViewById(R.id.tv_automatonPills_connected);
        tv_automatonPills_status = findViewById(R.id.tv_automatonPills_status);
        tv_automatonPills_plc = findViewById(R.id.tv_automatonPills_plc);
        tv_automatonPills_bottles = findViewById(R.id.tv_automatonPills_bottles);
        tv_automatonPills_pills = findViewById(R.id.tv_automatonPills_pills);

        fab_automatonPills_connect = findViewById(R.id.fab_automatonPills_connect);
        fab_automatonPills_logout = findViewById(R.id.fab_automatonPills_logout);

        // If not logged in, redirection to LoginActivity
        if (session.checkLogin() || currentAutomaton.checkCurrent()) {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        HashMap<String, String> user = session.getUserDetails();
        automatonName = currentAutomaton.getAutomatonName().get(CurrentAutomaton.KEY_NAME);

        AutomatonAccessDB automatonDB = new AutomatonAccessDB(this);
        automatonDB.openForWrite();
        automaton = automatonDB.getAutomaton(automatonName);
        automatonDB.Close();

        tv_automatonPills_connected.setText(Html.fromHtml(getString(R.string.connected_as) + " '<b>" + user.get(Session.KEY_EMAIl) + "</b>'."));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (session.checkLogin() || currentAutomaton.checkCurrent()) {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void onAutomatonClickManager(View v) {

        switch (v.getId()) {
            case R.id.fab_automatonPills_connect:

                connexStatus = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connexStatus != null) {
                    network = connexStatus.getActiveNetworkInfo();
                }

                if (network != null && network.isConnectedOrConnecting()) {
                    if (fab_automatonPills_connect.getContentDescription().equals(getString((R.string.login)))) {

                        fab_automatonPills_connect.setContentDescription(getString((R.string.logout_title)));
                        fab_automatonPills_connect.setImageResource(R.drawable.ic_signout);
                        tv_automatonPills_status.setText(String.format(getString(R.string.connected_automaton), network.getTypeName()));

                        readS7 = new ReadTaskS7(v, tv_automatonPills_plc, tv_automatonPills_bottles, tv_automatonPills_pills);
                        readS7.Start(automatonName, automaton.getIp(), automaton.getRack(), automaton.getSlot());

                        /*ln_main_ecrireS7.setVisibility(View.VISIBLE);

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        writeS7 = new WriteTaskS7();
                        //writeS7.Start("10.1.0.110", "0", "1");*/

                    } else {
                        readS7.Stop();

                        fab_automatonPills_connect.setContentDescription(getString((R.string.login)));
                        fab_automatonPills_connect.setImageResource(R.drawable.ic_signin);
                        tv_automatonPills_status.setText(getString((R.string.pills)));

                        /*try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        writeS7.Stop();
                        ln_main_ecrireS7.setVisibility(View.INVISIBLE);*/

                    }
                } else {
                    Toast.makeText(this, getString((R.string.impossible_connection)), Toast.LENGTH_SHORT).show();
                }


                break;
            case R.id.fab_automatonPills_logout:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle(R.string.logout_title)
                        .setMessage(R.string.logout_message)
                        .setPositiveButton(R.string.yes_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                session.logoutUser();
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                            }
                        })
                        .setNegativeButton(R.string.no_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create().show();

                break;
        }
    }
}

