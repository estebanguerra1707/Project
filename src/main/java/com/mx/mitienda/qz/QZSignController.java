package com.mx.mitienda.qz;


import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@RestController
@RequestMapping("/qz")
public class QZSignController {

    @PostMapping("/sign")
    public ResponseEntity<String> sign(@RequestBody String data) throws Exception {
        PrivateKey privateKey = loadPrivateKey();

        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));

        byte[] signed = signature.sign();
        String base64Signature = Base64.getEncoder().encodeToString(signed);

        return ResponseEntity.ok(base64Signature);
    }

    private PrivateKey loadPrivateKey() throws Exception {
        ClassPathResource resource = new ClassPathResource("qz/qz-private.pem");
        try (InputStream is = resource.getInputStream()) {
            String key = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            key = key
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] encoded = Base64.getDecoder().decode(key);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");

            return kf.generatePrivate(keySpec);
        }
    }
}
