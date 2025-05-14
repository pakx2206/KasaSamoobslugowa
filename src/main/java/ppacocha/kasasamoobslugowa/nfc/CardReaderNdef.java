package ppacocha.kasasamoobslugowa.nfc;

import javax.smartcardio.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for reading NDEF Text Records from an NFC tag via PC/SC.
 */
public class CardReaderNdef {
    private final CardTerminal terminal;

    public CardReaderNdef() throws Exception {
        TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = factory.terminals().list();
        System.out.println("Detected NFC readers: " + terminals);
        if (terminals.isEmpty()) {
            throw new IllegalStateException("Brak czytników NFC");
        }
        this.terminal = terminals.get(0);
        System.out.println("Using terminal: " + terminal.getName());
    }

    /**
     * Blocks until a tag is presented, reads the first NDEF Text Record, then waits for removal.
     * @return the text payload from the NDEF record
     */
    public String readTextRecord() throws Exception {
        System.out.println("Waiting for tag on terminal: " + terminal.getName());
        terminal.waitForCardPresent(0);
        Card card = terminal.connect("T=1");
        CardChannel channel = card.getBasicChannel();

        System.out.println("Tag detected, reading data...");
        List<Byte> raw = new ArrayList<>();
        for (int block = 4; block < 16; block += 4) {
            ResponseAPDU resp = channel.transmit(
                new CommandAPDU(new byte[]{(byte)0xFF, (byte)0xB0, 0x00, (byte)block, 0x10})
            );
            for (byte b : resp.getData()) raw.add(b);
        }
        byte[] tlv = new byte[raw.size()];
        for (int i = 0; i < tlv.length; i++) tlv[i] = raw.get(i);

        // Find NDEF TLV
        int idx = 0;
        while (idx < tlv.length && tlv[idx] != 0x03) idx++;
        if (idx >= tlv.length) throw new IllegalStateException("Brak TLV 0x03");
        int length = tlv[idx + 1] & 0xFF;
        byte[] ndef = new byte[length];
        System.arraycopy(tlv, idx + 2, ndef, 0, length);

        // Parse NDEF Text Record
        int typeLen = ndef[1] & 0xFF;
        int payloadLen = ndef[2] & 0xFF;
        if (ndef[3] != 0x54) throw new IllegalStateException("To nie jest Text Record");
        int status = ndef[3 + typeLen] & 0xFF;
        int langLen = status & 0x3F;
        int textOffset = 4 + langLen;
        int textLen = payloadLen - 1 - langLen;
        String text = new String(ndef, textOffset, textLen, StandardCharsets.UTF_8);

        System.out.println("Read NDEF payload: " + text);
        card.disconnect(false);
        terminal.waitForCardAbsent(0);
        System.out.println("Tag removed.");
        return text;
    }
}
