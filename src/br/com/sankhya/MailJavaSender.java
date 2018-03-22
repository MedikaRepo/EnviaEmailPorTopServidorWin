package br.com.sankhya;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;

public class MailJavaSender {

    //cria as propriedades necessarias para o envio de email
    public void senderMail(final MailJava mail) throws
         UnsupportedEncodingException, MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.host", mail.getSmtpHostMail());
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465"); 
        
        //classe anonima que realiza a autenticacao
        //do usuario no servidor de email.
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                                   mail.getUserMail(), mail.getPassMail()
                 );
            }
        };

        // Cria a sessao passando as propriedades e a autenticacao
        Session session = Session.getInstance(props, auth);
        // Gera um log no console referente ao processo de envio
        session.setDebug(true);

        //cria a mensagem setando o remetente e seus destinatarios
        Message msg = new MimeMessage(session);
        //aqui seta o remetente
        msg.setFrom(new InternetAddress(
                          mail.getUserMail(), mail.getFromNameMail())
         );

        //setamos o primeiro destinatario
        msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(mail.getToMailsUsers()));
        
        //setamos o segundo destinatario
        msg.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(mail.getToCCMailsUsers()));

        // Adiciona um Assunto a Mensagem
        msg.setSubject(mail.getSubjectMail());

        // Cria o objeto que recebe o texto do corpo do email
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(mail.getBodyMail(), mail.getTypeTextMail());

        // Monta a mensagem SMTP  inserindo o conteudo, texto e anexos
        Multipart mps = new MimeMultipart();
        for (int index = 0; index < mail.getFileMails().size(); index++) {

            // Cria um novo objeto para cada arquivo, e anexa o arquivo
            MimeBodyPart attachFilePart = new MimeBodyPart();
            FileDataSource fds =   new FileDataSource(
                 mail.getFileMails().get(index)
             );
            attachFilePart.setDataHandler(new DataHandler(fds));
            attachFilePart.setFileName(fds.getName());

            //adiciona os anexos da mensagem
            mps.addBodyPart(attachFilePart, index);

        }

        //adiciona o corpo texto da mensagem
       mps.addBodyPart(textPart);

        //adiciona a mensagem o conteudo texto e anexo
        msg.setContent(mps);
        
        // Envia a Mensagem
        Transport.send(msg);
    }
}