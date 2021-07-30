package server;

public class WSEncoder {
    public static byte[] encode(String message) {
        byte[] rawData = message.getBytes();
        byte[] frame = new byte[10];
        int frameCount = getFrameCount(rawData, frame);
        int messageLength = frameCount + rawData.length;
        byte[] messageBytes = new byte[messageLength];
        int rawDataPointer = 0;
        for (int i = 0; i < frameCount; i++) {
            messageBytes[i] = frame[i];
            rawDataPointer++;
        }
        for (byte rawDatum : rawData) {
            messageBytes[rawDataPointer] = rawDatum;
            rawDataPointer++;
        }
        return messageBytes;
    }

    private static int getFrameCount(byte[] rawData, byte[] frame) {
        int frameCount;
        // 129 in binary is 1000 0001.
        // 1000 represents FIN, RSV1, RSV2, RSV3
        // 0001 represents text data.
        // As we are dealing with text only, we are simply setting the first byte to be 129.
        frame[0] = (byte) 129;
        if (rawData.length <= 125) {
            frame[1] = (byte) rawData.length;
            frameCount = 2;
        } else if (rawData.length <= 65535) {
            frame[1] = (byte) 126;
            int len = rawData.length;
            frame[2] = (byte) ((len >> 8) & (byte) 255);
            frame[3] = (byte) (len & (byte) 255);
            frameCount = 4;
        } else {
            frame[1] = (byte) 127;
            int len = rawData.length;
            frame[2] = (byte) ((len >> 56) & (byte) 255);
            frame[3] = (byte) ((len >> 48) & (byte) 255);
            frame[4] = (byte) ((len >> 40) & (byte) 255);
            frame[5] = (byte) ((len >> 32) & (byte) 255);
            frame[6] = (byte) ((len >> 24) & (byte) 255);
            frame[7] = (byte) ((len >> 16) & (byte) 255);
            frame[8] = (byte) ((len >> 8) & (byte) 255);
            frame[9] = (byte) (len & (byte) 255);
            frameCount = 10;
        }
        return frameCount;
    }
}
