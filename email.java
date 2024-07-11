package Journalism.journal.service;

import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.search.ReceivedDateTerm;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EmailReader {
    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // Schedule the fetchEmails method to run every hour
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("fetching emails");
                fetchEmails();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.MINUTES);
    }
    public static void fetchEmails(){
         // Use your app-specific password if 2FA is enabled
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imaps.host", "imap.gmail.com");
        properties.put("mail.imaps.port", "993");
        properties.put("mail.imaps.ssl.enable", "true");
        String username = "";
        String password = "";
        try {
            // Create a session
            Session session = Session.getInstance(properties);
            // Connect to the Gmail server
            Store store = session.getStore("imaps");
            store.connect(username, password);

            // Open the inbox folder
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR_OF_DAY,-1);
//          last hours to get last hour messages
            Date lastHour = cal.getTime();
            System.out.println(lastHour);
            cal.add(Calendar.DAY_OF_MONTH,-1);
            Date lastDay = cal.getTime();
            System.out.println(lastDay);
            ReceivedDateTerm term = new ReceivedDateTerm(ReceivedDateTerm.GT,lastDay);
            System.out.println(term);

            Message[] allRecentMessages = inbox.search(term);
            System.out.println(Arrays.toString(allRecentMessages));
//            Message[] messages = inbox.getMessages();
            Message[] messages = Arrays.stream(allRecentMessages)
                    .filter(m -> {
                        try {
                            return m.getReceivedDate().after(lastHour);
                        } catch (MessagingException e) {
                            e.printStackTrace();
                            return false;
                        }
                    })
                    .toArray(Message[]::new);


            System.out.println(Arrays.toString(messages));
            
            // Process each message
            for (Message message : messages) {
                Address[] fromAddresses = message.getFrom();
                String fromEmail = ((InternetAddress) fromAddresses[0]).getAddress();
//                System.out.println(fromEmail);
                if (fromEmail.equals("hemantty0208@gmail.com")) {
                    System.out.println("Subject: " + message.getSubject());
                    String body = getTextFromMessage(message);
                    System.out.println("Body: " + body);
                }
            }
            inbox.close(false);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private static String getTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            System.out.println("Plain Text");
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            System.out.println("multipart");
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            return getTextFromMimeMultipart(mimeMultipart);
        }
        return "";
    }
    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent());
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result.append(org.jsoup.Jsoup.parse(html).text());
            }
        }
        return result.toString();
    }

    private static String extractField(String body, String fieldName) {
        Pattern pattern = Pattern.compile(fieldName + ":\\s*(.*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }
}
