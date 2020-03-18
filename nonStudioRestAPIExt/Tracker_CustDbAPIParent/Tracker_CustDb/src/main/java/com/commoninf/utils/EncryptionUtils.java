package com.commoninf.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import com.commoninf.logger.CiiLogger;

//import com.commoninf.common.exceptions.CWFApplicationException;

import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

// The encryption/decryption routines coded here are adapted from examples presented here:
//    http://javapapers.com/java/java-symmetric-aes-encryption-decryption-using-jce/

// and here:
//    https://gist.github.com/bricef/2436364

public class EncryptionUtils {
    private static final CiiLogger logger = new CiiLogger (EncryptionUtils.class.getName());  
    
    public static final String CVW_PARAMS = "cvwParams";

    /**
     * Encrypt a secret using a base64-encoded key
     * 
     * @param plainText - A String containing a secret to be encrypted
     * @param encryptionKeyAsBase64EncodedString - A String containing a base64-encoded version of the encryption key
     * @return the encrypted secret as a base64-encoded String
     */
    public static String encrypt(String plainText, String encryptionKeyAsBase64EncodedString) {        
        byte[] plainTextAsByteArray = plainText.getBytes();
        byte[] encryptionKeyAsBase64EncodedByteArray = encryptionKeyAsBase64EncodedString.getBytes();
        byte[] encryptionKeyAsRawByteArray = Base64.decodeBase64(encryptionKeyAsBase64EncodedByteArray);
        return new String(Base64.encodeBase64(encrypt(plainTextAsByteArray, encryptionKeyAsRawByteArray)));
    }   
    /**
     * Encrypt a secret using a "raw" key
     * 
     * @param plainTextAsRawByteArray - A byte array whose elements comprise the content to be encrypted
     * @param encryptionKeyAsRawByteArray - A byte array whose elements comprise the "raw" (i.e., non-base64-encoded)
     *           encryption key 
     * @return the encrypted value as a byte array
     */
    private static byte[] encrypt(byte[] plainTextAsByteArray, byte[] encryptionKeyAsRawByteArray) {        
        SecretKeySpec key = new SecretKeySpec(encryptionKeyAsRawByteArray, "AES");
        try {          
            Cipher cipher = Cipher.getInstance("AES", "SunJCE");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(plainTextAsByteArray);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | 
                 InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            String msg = "Error encrypting";
            logger.error(msg+": "+e.getMessage());
            throw new RuntimeException(e.getMessage());            
        } 
    } 
    
    /**
     * Decrypt a base64-encoded secret using a base64-encoded key
     * 
     * @param encryptedTextAsBase64EncodedString - A String containing a base64-encoded version of the encrypted text
     * @param encryptionKeyAsBase64EncodedString - A String containing a base64-encoded version of the encryption key
     * @return the decrypted value as a String
     */
    public static String decrypt(String encryptedTextAsBase64EncodedString, String encryptionKeyAsBase64EncodedString) { 
        byte[] encryptedTextAsBase64EncodedByteArray = encryptedTextAsBase64EncodedString.getBytes();
        byte[] encryptedTextAsRawByteArray = Base64.decodeBase64(encryptedTextAsBase64EncodedByteArray);
        byte[] encryptionKeyAsBase64EncodedByteArray = encryptionKeyAsBase64EncodedString.getBytes();
        byte[] encryptionKeyAsRawByteArray = Base64.decodeBase64(encryptionKeyAsBase64EncodedByteArray);
        return new String(decrypt(encryptedTextAsRawByteArray, encryptionKeyAsRawByteArray));
    }
    
    /**
     * Decrypt a "raw" secret using a "raw" key
     * 
     * @param encryptedTextAsRawByteArray - A byte array whose elements comprise the "raw" (i.e., non-base64-encoded)
     *           encrypted content 
     * @param encryptionKeyAsRawByteArray - A byte array whose elements comprise the "raw" (i.e., non-base64-encoded)
     *           encryption key 
     * @return the decrypted value as a byte array
     */
    private static byte[] decrypt(byte[] encryptedTextAsRawByteArray, byte[] encryptionKeyAsRawByteArray) { 
        SecretKeySpec key = new SecretKeySpec(encryptionKeyAsRawByteArray, "AES");          
        try {
            Cipher cipher = Cipher.getInstance("AES", "SunJCE");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(encryptedTextAsRawByteArray);                     
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException |
                 InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            String msg = "Error decrypting";
            logger.error(msg+": "+e.getMessage());
            throw new RuntimeException(e.getMessage());    
        }  
    }   
    
    public static String generateKeyAsBase64EncodedString(int keyLength) {
        byte[] keyByteArray = generateKey(keyLength);
        byte[] keyByteArrayBase64Encoded = Base64.encodeBase64(keyByteArray);
        return new String(keyByteArrayBase64Encoded);       
    }
    
    private static byte[] generateKey(int keyLength) {
        try {
            KeyGenerator gen = KeyGenerator.getInstance("AES");
            gen.init(keyLength);
            SecretKey secretKey = gen.generateKey();
            return secretKey.getEncoded();   
        } catch (NoSuchAlgorithmException e) {
            String msg = "Error generating key";
            logger.error(msg+": "+e.getMessage());
            throw new RuntimeException(e.getMessage());    
        }
    }
    
    public static void verifySupportFor256BitKey() {
        try {
            int maxKeyLength = Cipher.getMaxAllowedKeyLength("AES");
            if (maxKeyLength < 256) {
                logger.error("Failed to verify java security configuration");
                throw new RuntimeException("Java environment does not support unlimited strength encryption.");      
            }
        } catch (NoSuchAlgorithmException e) {
            String msg = "Failed to verify java security configuration";
            logger.error(msg+": "+e.getMessage());
            throw new RuntimeException("Error checking Java security configuration.");      
        }
    }
    
    
    /* Start of JSON Web Token (JWT) support
       Based on the example at:
       https://stormpath.com/blog/jwt-java-create-verify
    */
    
    
    private static final int DEFAULT_TIME_TO_LIVE = 1000 * 60 * 20;  // in ms
    private static final int DEFAULT_CLOCK_SKEW = 60;  // in seconds
    private static int KEY_LENGTH = 32;
    
    private static String jwtKey(String secret) {
        String key = secret;
        while (key.length() < KEY_LENGTH) {
            key += key;
        }
        try {
            key = new String (Base64.encodeBase64(key.substring(0, KEY_LENGTH).getBytes()), "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            logger.info ("The UTF-8 encoding is unssupported");
            e.printStackTrace();
        }
        return key;
    }

    // If we use more reserved headers, the set that is special cased will need to be updated.
    private static String[] reservedClaims = {"sub", "iss", "iat"};
    private static List<String> reservedClaimsList = Arrays.asList(reservedClaims);
    private static Map<String, Object> encryptClaims(Map<String, Object> claims, String secret) {
        Set<String> keys = claims.keySet();
        String encryptionKey = null;
        for (String k : keys) {
            if (reservedClaimsList.contains(k)) {
                continue;
            }
            Object val = claims.get(k);
            if (val != null && val instanceof String) {
                if (encryptionKey == null) {
                    encryptionKey = jwtKey(secret);
                }
                val = encrypt((String)val, encryptionKey);
                claims.put(k, val);
            }
        }
        return claims;
    }
    
    private static Claims decryptClaims(Claims claims, String secret) {
        Set<String> keys = claims.keySet();
        String decryptionKey = null;
        for (String k : keys) {
            if (reservedClaimsList.contains(k)) {
                continue;
            }
            Object val = claims.get(k);
            if (val != null && val instanceof String) {
                if (decryptionKey == null) {
                    decryptionKey = jwtKey(secret);
                }
                val = decrypt((String)val, decryptionKey);
                claims.put(k, val);
            }
        }
        return claims;
    }
    

    public static String createJwt(String cvwParams, String issuer, String subject, long ttlMillis, String secret, boolean encrypt) {
        Map<String, Object> claims = new HashMap<>();
        // iss and sub are 2 of the standard "registered" claims.
        claims.put("iss", issuer);
        claims.put("sub", subject);
        claims.put(CVW_PARAMS, cvwParams);
        return createJwt(claims, ttlMillis, secret, encrypt);
    }
    
    public static String createJwt(Map<String, Object> claims, String secret, boolean encrypt) {
        return createJwt(claims, DEFAULT_TIME_TO_LIVE, secret, encrypt);
    }
    
    public static String createJwt(Map<String, Object> claims, long ttlMillis, String secret, boolean encrypt) {
        //The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        // We will sign our JWT with our secret
        Key signingKey = new SecretKeySpec(secret.getBytes(), signatureAlgorithm.getJcaName());
     
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        // Set the JWT Claims
        JwtBuilder builder = Jwts.builder();
        if (claims != null) {
            if (encrypt) {
                claims = encryptClaims(claims, secret);
            }
            builder.setClaims(claims);
        }
        // Set issued at after setting claims or else it gets wiped out
        builder.setIssuedAt(now)
            .signWith(signatureAlgorithm, signingKey);
     
        // If it has been specified, add the expiration
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
     
        // Builds the JWT and serializes it to a compact, URL-safe string
        String jwt = builder.compact();
        return jwt;
    }
    
    public static Claims jwtClaims(String jwt, String secret, boolean decrypt) {
        return jwtClaims(jwt, secret, DEFAULT_CLOCK_SKEW, decrypt);
    }
    
    // For unit testing to allow skew to be set to a small value
    public static Claims jwtClaims(String jwt, String secret, int skew, boolean decrypt) {
        Claims claims = Jwts.parser()
                .setAllowedClockSkewSeconds(skew)
                .setSigningKey(secret.getBytes())
                .parseClaimsJws(jwt).getBody();
        if (decrypt) {
            claims = decryptClaims(claims, secret);
        }
        return claims;
    }
    
    /*public static JwsHeader jwtHeader(String jwt, String secret) {
        JwsHeader header = Jwts.parser() 
                .setAllowedClockSkewSeconds(DEFAULT_CLOCK_SKEW)
                .setSigningKey(secret.getBytes())
                .parseClaimsJws(jwt).getHeader();
        return header;
    }*/


}