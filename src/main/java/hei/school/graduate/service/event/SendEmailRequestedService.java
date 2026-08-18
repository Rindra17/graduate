package hei.school.graduate.service.event;

import hei.school.graduate.endpoint.event.model.SendEmailRequested;
import hei.school.graduate.mail.Email;
import hei.school.graduate.mail.Mailer;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SendEmailRequestedService implements Consumer<SendEmailRequested> {
  private final Mailer mailer;

  @SneakyThrows
  @Override
  public void accept(SendEmailRequested sendEmail) {
    var recipientAddress = new InternetAddress(sendEmail.getTo());

    String mailMessage =
        """
          <!DOCTYPE html>
              <html>
              <head>
                <meta charset="UTF-8">
                <title>Grade Report %s - %s</title>
              </head>
              <body style="margin:0; padding:20px; font-family:Arial, Helvetica, sans-serif;">

                <p>Good morning %s,</p>

                <p>Here is youre <strong>Grade report</strong>.</p>

                <p>[<a href="%s">Downoald link </a>]</p>

              </body>
              </html>
        """;

    var formatedEmail =
        String.format(
            mailMessage,
            sendEmail.getReference(),
            sendEmail.getAcademicYear(),
            sendEmail.getFirstName(),
            sendEmail.getReportLink());

    mailer.accept(
        new Email(
            recipientAddress, List.of(), List.of(), "Grade report", formatedEmail, List.of()));
  }
}
