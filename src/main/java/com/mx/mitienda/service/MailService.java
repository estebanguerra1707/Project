package com.mx.mitienda.service;

import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Emailv31;
import lombok.RequiredArgsConstructor;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.mailjet.client.MailjetClient;

import java.util.Base64;
import java.util.List;


@Service
@RequiredArgsConstructor
public class MailService {
    @Value("${mailjet.apikey.public}")
    private String publicKey;

    @Value("${mailjet.apikey.private}")
    private String privateKey;
    private final JavaMailSender mailSender;

    @Value("${alertas.stock.email.origen}")
    private String origen;


    public void sendPDFEmail(List<String> emailList, String toName, String subject, String htmlBody, byte[] pdfBytes, String filename) {
        try {
            MailjetClient client = new MailjetClient(publicKey,privateKey);

            String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);

            JSONArray messages = new JSONArray();

            for (String email : emailList) {
                JSONObject message = new JSONObject()
                        .put(Emailv31.Message.FROM, new JSONObject()
                                .put("Email", origen)
                                .put("Name", "Mi Tienda"))
                        .put(Emailv31.Message.TO, new JSONArray()
                                .put(new JSONObject()
                                        .put("Email", email)
                                        .put("Name", toName)))
                        .put(Emailv31.Message.SUBJECT, subject)
                        .put(Emailv31.Message.HTMLPART, htmlBody)
                        .put(Emailv31.Message.ATTACHMENTS, new JSONArray()
                                .put(new JSONObject()
                                        .put("ContentType", "application/pdf")
                                        .put("Filename", filename)
                                        .put("Base64Content", base64Pdf)
                                ));

                messages.put(message);
            }

            MailjetRequest request = new MailjetRequest(Emailv31.resource)
                    .property(Emailv31.MESSAGES, messages);

            MailjetResponse response = client.post(request);
            System.out.println("Status: " + response.getStatus());
            System.out.println("Data: " + response.getData());

        } catch (Exception e) {
            throw new RuntimeException("Error enviando correo con Mailjet", e);
        }
    }

    public void sendSimpleEmail(String from, String to, String subject, String body) {
        try {
            MailjetClient client = new MailjetClient(publicKey, privateKey);

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject()
                    .put(Emailv31.Message.FROM, new JSONObject()
                            .put("Email", from)
                            .put("Name", "Mi Tienda"))
                    .put(Emailv31.Message.TO, new JSONArray()
                            .put(new JSONObject()
                                    .put("Email", to)))
                    .put(Emailv31.Message.SUBJECT, subject)
                    .put(Emailv31.Message.HTMLPART, body);

            messages.put(message);


            MailjetRequest request = new MailjetRequest(Emailv31.resource)
                    .property(Emailv31.MESSAGES, messages);

            MailjetResponse response = client.post(request);
            System.out.println("Status: " + response.getStatus());
            System.out.println("Data: " + response.getData());

        } catch (Exception e) {
            throw new RuntimeException("Error enviando correo con Mailjet", e);
        }

    }

    public void sendEmail(String email, String restableceTuContraseña, String body) {
        try {
            MailjetClient client = new MailjetClient(publicKey, privateKey);

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject()
                    .put(Emailv31.Message.FROM, new JSONObject()
                            .put("Email", origen)
                            .put("Name", "Mi Tienda"))
                    .put(Emailv31.Message.TO, new JSONArray()
                            .put(new JSONObject()
                                    .put("Email", email)))
                    .put(Emailv31.Message.SUBJECT, restableceTuContraseña)
                    .put(Emailv31.Message.HTMLPART, body);

            messages.put(message);


            MailjetRequest request = new MailjetRequest(Emailv31.resource)
                    .property(Emailv31.MESSAGES, messages);

            MailjetResponse response = client.post(request);
            System.out.println("Status: " + response.getStatus());
            System.out.println("Data: " + response.getData());

        } catch (Exception e) {
            throw new RuntimeException("Error enviando correo con Mailjet", e);
        }
    }
}
