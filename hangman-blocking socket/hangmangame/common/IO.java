package common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class IO {

    /**
     * Calculate the length of the message and prepend it before sending it
     *
     * @param out outputstream to where to send the message
     * @param msg the message to send
     */

    public static void send(OutputStream out, String msg) {
        try {
            byte[] data = msg.getBytes("UTF-8");//convert String to byte array
            int length = data.length;
            byte[] dataWithLength = new byte[data.length + Integer.BYTES];

            byte[] lengthAsBytes = ByteBuffer.allocate(Integer.BYTES)// allocate a new buffer
                    .putInt(length)
                    .array();

            System.arraycopy(lengthAsBytes, 0, dataWithLength, 0, Integer.BYTES);
            System.arraycopy(data, 0, dataWithLength, Integer.BYTES, length);

            out.write(dataWithLength);
            out.flush();

        } catch (IOException e) {
            System.err.println("Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Tries to read the number of bytes by using the first 4 bytes as an integer for message length
     *
     * @param in inputstream to read from
     * @return the message sent
     * @throws IOException if unable to read from stream
     */
    public static String receive(InputStream in) throws IOException {
        byte[] inputData = in.readNBytes(Integer.BYTES);
        ByteBuffer wrapped = ByteBuffer.wrap(inputData); // wrap inputData array in a buffer
        int length = wrapped.getInt();
        byte[] data = in.readNBytes(length);
        String msg = new String(data);
        return msg;
    }

}
