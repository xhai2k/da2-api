package utils;

import com.ning.http.util.Base64;
import play.Logger;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 暗号化/複合化処理を実行します
 *
 * @author plusone
 */
public class EncoderUtils {

    /**
     * Base64エンコード処理を実施します
     *
     * @param bytes Base64エンコードを施す対象データ
     * @return Base64エンコード処理後の文字列
     */
    public static String base64Encode(byte[] bytes) {
        return Base64.encode(bytes).toString();
    }

    /**
     * Base64デコード処理を実施します
     *
     * @param encoded Base64エンコード後の文字列.
     * @return デコード後の文字列.
     * @throws UnsupportedEncodingException 不正な文字エンコードを指定した場合にthrowされる.
     */
    public static String base64Decode(String encoded) throws UnsupportedEncodingException {
        byte[] buff = Base64.decode(encoded);
        return new String(buff, "utf-8");
    }

    /**
     * 認証キーの作成
     *
     * @param target       認証元の文字列
     * @param signatureKey 認証キーを作成する署名キー
     * @param algorithm    アルゴリズム <br>
     *                     AES, ARCFOUR, Blowfish, DES, DESede, HmacMD5, HmacSHA1, HmacSHA256, HmacSHA384,
     *                     HmacSHA512 が利用可
     * @return 生成した認証キー
     * @throws NoSuchAlgorithmException 存在しないアルゴリズムの場合throw
     * @throws InvalidKeyException      "Message Authentication Code" (MAC) algorithmに適さないKeyを指定するとthrow
     */
    public static String generateAuthenticationKey(String target, String signatureKey, String algorithm)
            throws NoSuchAlgorithmException, InvalidKeyException {
        // 秘密鍵の作成
        SecretKey secretKey = new SecretKeySpec(signatureKey.getBytes(), algorithm);

        // 認証キーの作成
        Mac mac = Mac.getInstance(algorithm);
        mac.init(secretKey);
        mac.update(target.getBytes());

        // 暗号化
        byte[] encryptedData = mac.doFinal();

        // base64エンコード
        return base64Encode(encryptedData);
    }

    /**
     * 認証キーの作成
     *
     * @param target 認証元の文字列 HmacSHA256を利用
     * @return 生成した認証キー
     * @throws NoSuchAlgorithmException 存在しないアルゴリズムの場合throw
     * @throws InvalidKeyException      "Message Authentication Code" (MAC) algorithmに適さないKeyを指定するとthrow
     */
    public static String generateAuthenticationKey(String target) {
        String resultVal = target;
        try {
            String systemSecretKey = "1964101001";
            resultVal = generateAuthenticationKey(target, systemSecretKey, "HmacSHA256");
        } catch (Exception e) {
            Logger.error(e, e.getMessage());
        }
        return resultVal;
    }

}
