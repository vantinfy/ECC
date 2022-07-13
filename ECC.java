package ECC;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;

public class ECC {
    static String algorithm = "EC"; // 椭圆曲线算法 这三个参数保持不变即可
    static String curve = "secp256k1"; // S256曲线 | prime256v1、secp256r1-->P256
    static String signatureAlgorithm = "SHA256withECDSA"; // 签名/验签使用的sha256算法
    static int appId = 10000006;
    static String apiKey = "C9E4UK4gczF3shUBcsyeoRIlnHdVG+cSOXgwckf62Ko="; // ECC私钥D.bytes转base64
    static String serverUrl = "http://192.168.0.31:7788";

    // 随机生成公私钥对
    public static KeyPair GenerateKeys(String algorithm, String curve) {
        // curveName这里取值：secp256k1 即S256
        ECGenParameterSpec spec = new ECGenParameterSpec(curve);
        KeyPairGenerator gen;
        try {
            gen = KeyPairGenerator.getInstance(algorithm);
            // secp256r1 [NIST P-256, X9.62 prime256v1] 不指定curve默认使用P-256算法
            // gen.initialize(256);
            gen.initialize(spec, new SecureRandom()); // secp256k1 (1.3.132.0.10) 即S-256算法
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        // 随机生成密钥对
        KeyPair keys = gen.generateKeyPair();
        // ECPrivateKey priK = (ECPrivateKey) keys.getPrivate();
        // ECPublicKey pubK = (ECPublicKey)keys.getPublic();
        return keys;
    }

    // 私钥D转bytes再转base64
    public static String PriKeyDtoStr(ECPrivateKey key) {
        byte[] privateKeyS = key.getS().toByteArray();
        return Base64.getEncoder().encodeToString(privateKeyS);
    }

    // base64格式D转bytes还原私钥
    public static ECPrivateKey ReducePrivateKey(String keyB64Str, String algorithm, String curve) {
        byte[] privateKeyS = Base64.getDecoder().decode(keyB64Str);
        try {
            AlgorithmParameters parameters = AlgorithmParameters.getInstance(algorithm); // EC
            parameters.init(new ECGenParameterSpec(curve)); // secp256k1
            ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);
            ECPrivateKeySpec privateSpec = new ECPrivateKeySpec(new BigInteger(1, privateKeyS), ecParameters);
            KeyFactory kf = KeyFactory.getInstance(algorithm);
            ECPrivateKey privateKey = (ECPrivateKey) kf.generatePrivate(privateSpec);
            return privateKey;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 私钥签名 message明文先用sha256做摘要
    public static String Sign(String message, ECPrivateKey privateKey) {
        String signRes = "";
        try {
            Signature sig = Signature.getInstance(signatureAlgorithm); // SHA256withECDSA
            sig.initSign(privateKey);
            // string使用UTF-8转bytes
            sig.update(message.getBytes(StandardCharsets.UTF_8));
            signRes = Base64.getEncoder().encodeToString(sig.sign());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signRes;
    }

    public static void main(String[] args) {
        /*
         *  * KeyPair keys = GenerateKeys(algorithm, curve);
         *  * // 获取私钥
         *  * ECPrivateKey privateKey = (ECPrivateKey) keys.getPrivate();
         *  * ECPublicKey publicKey = (ECPublicKey) keys.getPublic();
         *  
         */
        int scene_id = 2;
        String contract_method = "mint";
        String to = "49D078F385DF780A3AFDE32EF437AF54B1732D4C";
        String tokenId = "7777";
        String uri = "https://haha/7777.jpg";

        // 不同的合约方法需要的参数不一样
        MyJson contract_args = new MyJson();
        contract_args.put("_to", to);
        contract_args.put("_tokenId", tokenId);
        contract_args.put("_uri", uri);

        // 最外层请求参数
        MyJson request = new MyJson();
        request.put("contract_args", contract_args);
        request.put("app_id", appId);
        request.put("scene_id", scene_id);
        request.put("contract_method", contract_method);

        // String message = contract_args.toString(); // 通常签名明文是整个请求体本身 这里只对app_id字符串签
        String message = String.format("%d", appId);

        // D恢复私钥
        ECPrivateKey privateKey = ReducePrivateKey(apiKey, algorithm, curve);
        ECPublicKey publicKey = ReduceEcPublicKey("xPcy+8rYxIa9PZUds+F96EIn1flAoTeBQGLvZGPANkI=",
                "3BOYSr6/1XEkIJ8vkfg2m4BZxgyf7A+KQc/J59GKozY=");
        // 使用私钥签名
        String signature = Sign(message, privateKey);
        // 将签名结果附在请求参数中
        request.put("rs", signature);
        System.out.println("签名结果：" + signature);
        System.out.println("验证结果: " + Verify(publicKey, message, signature)); // 验证签名

        // go 签名结果验证 使用ecdsa.SignASN1签名
        // System.out.println("go签名验证结果: " + Verify(publicKey, message,
        // "MEYCIQCO3XenOHMd4gkFBGvvHQjlS96ufbwbOnffDDq5GSQIIgIhAK2Qgnw+5AkhmcKO16fJ6IzjE3VHfexMoQOev9u31OHX"));

        // 远程调用go服务
        String url = serverUrl + "/openNftApi/invoke";
        String requestMethod = "POST";

        String resp = HTTP.sendRequest(url, requestMethod, request.toString());
        System.out.println("服务器响应: " + resp);
    }

    

    // 其他通常用不上的方法
    // X Y转base64string
    public static String[] PubKeyXtoStr(ECPublicKey publicKey) {
        byte[] publicKeyX = publicKey.getW().getAffineX().toByteArray();
        byte[] publicKeyY = publicKey.getW().getAffineY().toByteArray();
        String encodedPublicKeyX = Base64.getEncoder().encodeToString(publicKeyX);
        String encodedPublicKeyY = Base64.getEncoder().encodeToString(publicKeyY);
        String[] XY = { encodedPublicKeyX, encodedPublicKeyY };
        return XY;
    }

    // XY base64还原pubKey
    public static ECPublicKey ReduceEcPublicKey(String encodedPublicKeyX, String encodedPublicKeyY) {
        byte[] publicKeyX = Base64.getDecoder().decode(encodedPublicKeyX);
        byte[] publicKeyY = Base64.getDecoder().decode(encodedPublicKeyY);
        ECPoint pubPoint = new ECPoint(new BigInteger(1, publicKeyX), new BigInteger(1, publicKeyY));
        try {
            AlgorithmParameters parameters = AlgorithmParameters.getInstance(algorithm);
            parameters.init(new ECGenParameterSpec(curve));
            ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);
            ECPublicKeySpec pubSpec = new ECPublicKeySpec(pubPoint, ecParameters);
            KeyFactory kf = KeyFactory.getInstance(algorithm);
            ECPublicKey publicKey = (ECPublicKey) kf.generatePublic(pubSpec);
            return publicKey;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 验证签名 参数: 公钥 明文 签名结果
    public static boolean Verify(ECPublicKey publicKey, String message, String sign) {
        try {
            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initVerify(publicKey);
            // string使用UTF-8转bytes
            signature.update(message.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getDecoder().decode(sign.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 公钥加密 TODO 加解密两个方法还有问题 cipher.getInstance("ECIES")无论有无"BC"参数都会报错
    public static String Encrypt(ECPublicKey publicKey, String message) {
        try {
            Cipher cipher = Cipher.getInstance("ECIES", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] res = cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 私钥解密
    public static String Decrypt(ECPrivateKey privateKey, String m) {
        try {
            Cipher cipher = Cipher.getInstance("ECIES", "BC");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] result = cipher.doFinal(Base64.getDecoder().decode(m));
            return new String(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
