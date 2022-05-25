package io.fansir.uploader.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static void runCommand(String command){
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command);
            info("执行命令：");
            info(command);
            //等待命令执行完成
            process.waitFor(2, TimeUnit.MINUTES);
        }catch (Exception e){
            info("执行出现异常");
            e.printStackTrace();
            stopTask(e.getMessage());
        }finally {
            info("执行完毕");
            if (process != null) {
                process.destroy();
            }
        }
    }

    public static void stopTask(String message) {
        throw new RuntimeException(message);
    }

    public static void info(String message) {
        System.out.println(message);
    }


    /**
     * 说明：关闭流
     *
     * @param close
     */
    public static void closeIO(Closeable... close) {
        if (null != close && close.length > 0) {
            for (Closeable closeable : close) {
                try {
                    if (closeable != null) {
                        closeable.close();
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    public static void deleteFile(File file) {
        if (file == null) {
            return;
        }
        if (!file.exists()) {
            return;
        }
        if (!file.isFile()) {
            for (File f : file.listFiles()) {
                deleteFile(f);
            }
        }
        file.delete();
    }

    /**
     * 说明：判断给定字符串是否空白串 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     */
    public static boolean isEmpty(CharSequence input) {
        if (input == null || "".equals(input))
            return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    public static boolean writeText(File file,String content){
        boolean result = false;
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            writer.write(content);
            writer.flush();
            result = true;
        } catch (Exception e) {
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }


    public static boolean isNotEmpty(CharSequence input){
        return !isEmpty(input);
    }

    public static StringBuilder readText(File file){
        StringBuilder fileContent = new StringBuilder();
        if (file == null || !file.isFile() || !file.exists()) {
            return fileContent;
        }

        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(file), "UTF-8");
            reader = new BufferedReader(is);
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileContent;
    }

    public static String getFileMd5(File file) {
        String md5 = null;
        if (file != null && file.exists() && file.isFile()) {
            FileInputStream in = null;
            FileChannel ch = null;
            try {
                in = new FileInputStream(file);
                ch = in.getChannel();
                MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                messageDigest.update(byteBuffer);
                md5 = byteArrayToHex(messageDigest.digest());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closeIO(ch, in);
            }
        }
        return md5;
    }

    private static String byteArrayToHex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
            if (n < b.length - 1) {
                hs = hs + "";
            }
        }
        return hs;
    }

    public static File findApkFile(File dir) {
        if (dir == null || !dir.exists()) {
            return null;
        }
        if (dir.isFile()) {
            String name = dir.getName();
            if (name.endsWith(".apk")) {
                return dir;
            } else {
                return null;
            }
        } else {
            for (File f : dir.listFiles()) {
                File apk = findApkFile(f);
                if (apk != null) {
                    return apk;
                }
            }
        }
        return null;
    }

    public static float parseFloat(String percent){
        float value = 0;
        try {
            value = Float.parseFloat(percent);
        }catch (Exception e){
            e.printStackTrace();
        }
        return value;
    }
}
