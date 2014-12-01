package net.ka0labs.ccnfcpwner;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.parser.EmvParser;

import net.ka0labs.ccnfcpwner.utils.CardUtils;
import net.ka0labs.ccnfcpwner.utils.SimpleAsyncTask;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends ActionBarActivity {
    private TextView tvCardNumber;
    private TextView tvHolderName;
    private TextView tvExpiration;
    private TextView tvType;
    private TextView tvApplication;
    private TextView tvTryPin;
    private TextView tvAid;
    private TableLayout layoutData;
    private TextView tvInfo;

    // Initiate nfc provider
    private final NFCProvider provider = new NFCProvider();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                            tvAid.setText(mCard.getAid());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
