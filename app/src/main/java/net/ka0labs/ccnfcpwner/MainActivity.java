package net.ka0labs.ccnfcpwner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.parser.EmvParser;

import net.ka0labs.ccnfcpwner.utils.CardUtils;
import net.ka0labs.ccnfcpwner.utils.SimpleAsyncTask;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends ActionBarActivity {
    private TableLayout layoutData;
    private TextView tvInfo;

    private TextView tvCardNumber;
    private TextView tvHolderName;
    private TextView tvExpiration;
    private TextView tvType;
    private TextView tvApplication;
    private TextView tvTryPin;
    private TextView tvAid;
    private Button btnSaveToFile;

    // Initiate nfc provider
    private final NFCProvider provider = new NFCProvider();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check if NFC is enabled
        if(!CheckNFC()) {
            NFCDialog();
        }

        // Read card if system receives an NFC tag
        if (getIntent().getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED)) {
            onNewIntent(getIntent());
        }

        tvInfo = (TextView)findViewById(R.id.tvInfo);
        layoutData = (TableLayout)findViewById(R.id.LayoutTableData);

        //Textviews to show the credit card data
        tvCardNumber = (TextView)findViewById(R.id.tvCardNumber);
        tvHolderName = (TextView)findViewById(R.id.tvHolderName);
        tvExpiration = (TextView)findViewById(R.id.tvExpiration);
        tvType = (TextView)findViewById(R.id.tvCardType);
        tvApplication = (TextView)findViewById(R.id.tvApplication);
        tvTryPin = (TextView)findViewById(R.id.tvTryPin);
        tvAid = (TextView)findViewById(R.id.tvAid);
        //Save button
        btnSaveToFile = (Button)findViewById(R.id.btnSaveToFile);
        btnSaveToFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveToFile();
            }
        });
    }

    //When we receive a tag
    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        final Tag mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(mTag != null) {
            new SimpleAsyncTask() {
                private IsoDep mTagComm;
                private EmvCard mCard;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    //Do nothing
                }
                @Override
                protected void doInBackground() {
                    mTagComm = IsoDep.get(mTag);
                    try {
                        //Connect to the Credit card
                        mTagComm.connect();
                        provider.setmTagCom(mTagComm);
                        //Parse the credit card data
                        EmvParser parser = new EmvParser(provider, true);
                        mCard = parser.readEmvCard();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        IOUtils.closeQuietly(mTagComm);
                    }
                }

                @Override
                protected void onPostExecute(final Object result) {
                    if(mCard != null) {
                        //Show the Credit card layout
                        layoutData.setVisibility(View.VISIBLE);
                        tvInfo.setVisibility(View.GONE);

                        //Card number
                        tvCardNumber.setText(CardUtils.formatCardNumber(mCard.getCardNumber(), mCard.getType()));

                        //Card holder name
                        if (StringUtils.isNotBlank(mCard.getHolderFirstname()) && StringUtils.isNotBlank(mCard.getHolderLastname())) {
                            tvHolderName.setText(StringUtils.join(mCard.getHolderFirstname(), " ", mCard.getHolderLastname()));
                        } else {
                            tvHolderName.setText("Error");
                        }

                        //Expiration date
                        if (mCard.getExpireDate() != null) {
                            SimpleDateFormat format = new SimpleDateFormat("MM/yy", Locale.getDefault());
                            tvExpiration.setText(format.format(mCard.getExpireDate()));
                        } else {
                            tvExpiration.setText("Error");
                        }

                        //Card type
                        if (mCard.getType() != null) {
                            tvType.setText(mCard.getType().getName());
                        } else {
                            tvType.setText("Error");
                        }

                        //Card application
                        if (StringUtils.isNotEmpty(mCard.getApplicationLabel())) {
                            tvApplication.setText(mCard.getApplicationLabel());
                        } else {
                            tvApplication.setText("Error");
                        }

                        //Pin trys left
                        tvTryPin.setText(mCard.getLeftPinTry() + "/3");

                        //AID
                        if (StringUtils.isNotEmpty(mCard.getAid())) {
                            tvAid.setText(CardUtils.formatAid(mCard.getAid()));
                        } else {
                            tvAid.setText("Error");
                        }

                    } else {
                        //Show info layout
                        layoutData.setVisibility(View.GONE);
                        tvInfo.setVisibility(View.VISIBLE);
                    }
                }
            }.execute();
        }
    }

    void SaveToFile() {
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            //Save data to file sdcard/CCNFCPwner/Cards.txt
            try {
                String data = "Card number: " + tvCardNumber.getText().toString() + "\n" +
                        "Holder name: " + tvHolderName.getText().toString() + "\n" +
                        "Expiration date: " + tvExpiration.getText().toString() + "\n" +
                        "Card type: " + tvType.getText().toString() + "\n" +
                        "Application: " + tvApplication.getText().toString() + "\n" +
                        "Card AID: " + tvAid.getText().toString() + "\n" +
                        "Pin trys: " + tvTryPin.getText().toString() + "\n\n" +
                        "------------------------------------------\n\n";
                File appFolder = new File(Environment.getExternalStorageDirectory()+"/CCNFCPwner");
                if(!appFolder.exists()) appFolder.mkdir();
                File file = new File(appFolder, "cards.txt");
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file, true));
                outputStreamWriter.append(data);
                outputStreamWriter.close();
                Toast.makeText(this, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();

                //Disable save button to prevent duplicates
                btnSaveToFile.setEnabled(false);
                btnSaveToFile.setText(getString(R.string.saved));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    boolean CheckNFC() {
        NfcManager manager = (NfcManager)getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }

    void NFCDialog() {
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.nfcdialog_title))
            .setMessage(getString(R.string.nfcdialog_message))
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                }
            })
            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_about:
                Intent i = new Intent(this, AboutActivity.class);
                startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }
}
