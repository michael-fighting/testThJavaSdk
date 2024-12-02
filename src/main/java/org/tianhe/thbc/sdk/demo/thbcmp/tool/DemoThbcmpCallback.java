package org.tianhe.thbc.sdk.demo.thbcmp.tool;

import static org.tianhe.thbc.sdk.utils.ByteUtils.byteArrayToInt;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tianhe.thbc.sdk.model.MsgType;
import org.tianhe.thbc.sdk.thbcmp.ThbcmpCallback;
import org.tianhe.thbc.sdk.thbcmp.topic.ThbcmpMsgIn;

public class DemoThbcmpCallback extends ThbcmpCallback {
    private static Logger logger = LoggerFactory.getLogger(DemoThbcmpCallback.class);

    @Override
    public byte[] receiveThbcmpMsg(ThbcmpMsgIn msg) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (msg.getContent().length > 8) {
            byte[] content = msg.getContent();
            byte[] byteflag = subbytes(content, 0, 4);
            int flag = byteArrayToInt(byteflag);
            if (flag == -128) {
                // Received a file.
                byte[] bytelength = subbytes(content, 4, 4);
                int length = byteArrayToInt(bytelength);
                byte[] bytefilename = subbytes(content, 8, length);
                String filename = new String(bytefilename);
                System.out.println(
                        "Step 2:Receive file,  time: "
                                + df.format(LocalDateTime.now())
                                + " filename length:"
                                + length
                                + " filename binary:"
                                + Arrays.toString(bytefilename)
                                + " filename:"
                                + filename);

                int contentlength = content.length - 8 - filename.length();
                byte[] fileContent = subbytes(content, 8 + filename.length(), contentlength);
                getFileFromBytes(fileContent, filename);
                System.out.println("|---save file:" + filename + " success");
                byte[] responseData = "Yes, I received!".getBytes();
                if (msg.getType() == (short) MsgType.THBCMP_REQUEST.getType()) {
                    System.out.println(
                            "|---response, time: "
                                    + df.format(LocalDateTime.now())
                                    + " content: "
                                    + new String(responseData));
                }
                return responseData;
            }
        }

        byte[] responseData = "Yes, I received!".getBytes();
        // Print receive thbcmp message
        System.out.println(
                "Step 2:Receive msg, time: "
                        + df.format(LocalDateTime.now())
                        + "topic:"
                        + msg.getTopic()
                        + " content:"
                        + new String(msg.getContent()));
        if (msg.getType() == (short) MsgType.THBCMP_REQUEST.getType()) {
            System.out.println(
                    "|---response, time: "
                            + df.format(LocalDateTime.now())
                            + " content: "
                            + new String(responseData));
        }
        // Response to the message sender
        return responseData;
    }

    public static byte[] subbytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);
        return bs;
    }

    public static void getFileFromBytes(byte[] b, String outputFile) {
        File ret = null;
        BufferedOutputStream stream = null;
        FileOutputStream fstream = null;
        try {
            ret = new File(outputFile);
            fstream = new FileOutputStream(ret);
            stream = new BufferedOutputStream(fstream);
            stream.write(b);
        } catch (Exception e) {
            logger.error(" write exception, message: {}", e.getMessage());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    logger.error(" close exception, message: {}", e.getMessage());
                }
            }

            if (fstream != null) {
                try {
                    fstream.close();
                } catch (IOException e) {
                    logger.error(" close exception, message: {}", e.getMessage());
                }
            }
        }
    }
}
