package net.ka0labs.ccnfcpwner;

import android.nfc.tech.IsoDep;
import android.util.Log;

import com.github.devnied.emvnfccard.enums.SwEnum;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.utils.TlvUtil;

import java.io.IOException;

public class NFCProvider implements IProvider {
    private IsoDep mTagCom;
    private static final String TAG = NFCProvider.class.getName();

    @Override
    public byte[] transceive(byte[] pCommand) throws CommunicationException {
        byte[] response = null;
        try {
            response = mTagCom.transceive(pCommand);
        } catch(IOException e) {
            throw new CommunicationException(e.getMessage());
        }
        SwEnum val = SwEnum.getSW(response);
        if (val != null) {
            Log.d(TAG, "resp: " + (TlvUtil.prettyPrintAPDUResponse(response)));
        }
        return response;
    }

    public void setmTagCom(final IsoDep mTagCom) {
        this.mTagCom = mTagCom;
    }
}
