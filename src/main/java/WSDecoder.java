public class WSDecoder {
    public static byte[] decode(int len, byte[] b) {
        // The second byte represents the length of the raw data
        // or if the length is larger than 125, either the next 2 or next 8 bytes
        // gives us the actual length of the raw data.
        // Why are we not using just b[1]? Why are we doing & with 127?
        // The first bit of this byte will be 1, indicating that the raw data is encoded.
        // And only the next 7 bits gives the length data, that's why we are getting rid
        // of the first bit by taking an "&" with 0111 111 or 127.
        byte lengthCode = b[1];
        byte op = (byte) 127; // 0111 1111
        byte rLength = (byte) (lengthCode & op);

        // The first byte give the type of the data
        // The bytes 2 - 10 gives the length of the raw data.
        // We are assuming that the length of the raw data is below 125.
        // and so we are expecting the mask decoding key to present in the index 2 - 6;
        int rMaskIndex = 2;

        // Just like encoding, if the raw data length is 126
        // then it means the next two bytes gives us the total length
        // of the raw data. So, the mask decoding key will be present
        // from the 4th index.
        if (rLength == (byte) 126) rMaskIndex = 4;

        // Just like above, if the raw data length is 127
        // then it means the next eight bytes gives us the total length
        // of the raw data. So, the mask decoding key will be present
        // from the 10th index.
        if (rLength == (byte) 127) rMaskIndex = 10;

        // Mask decoding takes 4 bytes space.
        byte[] masks = new byte[4];
        int j = 0;
        int i = 0;
        for (i = rMaskIndex; i < (rMaskIndex + 4); i++) {
            masks[j] = b[i];
            j++;
        }

        // Reading raw data.
        int rawDataStart = rMaskIndex + 4;
        int messLen = len - rawDataStart;
        byte[] message = new byte[messLen];
        for (i = rawDataStart, j = 0; i < len; i++, j++) {
            // The four mask bytes are used in decoding the data.
            // The algorithm for decoding the data is
            // encodedByte XOR masks[encodedByteIndex MOD 4]
            message[j] = (byte) (b[i] ^ masks[j % 4]);
        }
        return message;
    }
}
