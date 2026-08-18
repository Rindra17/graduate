package hei.school.graduate.model;

import hei.school.graduate.endpoint.rest.controller.dto.StudentGradesResponse;
import hei.school.graduate.repository.model.JUser;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class ReportTemplate {
  private String email;

  public ReportTemplate(
      JUser student, String reference, String academicYear, StudentGradesResponse grades) {
    var rowTemplate =
        """
<tr>
<td style="padding: 12px 20px; font-size: 14px; color: #111827; border-bottom: 1px solid #f3f4f6;">%s</td>
<td style="padding: 12px 20px; font-size: 14px; color: #111827; border-bottom: 1px solid #f3f4f6;">%s</td>
<td style="padding: 12px 20px; font-size: 14px; color: #4b5563; text-align: center; border-bottom: 1px solid #f3f4f6;">%s</td>
<td style="padding: 12px 20px; font-size: 14px; color: #111827; font-weight: bold; text-align: center; border-bottom: 1px solid #f3f4f6;">%s/20</td>
</tr>
""";

    var rowsHtml =
        grades.courses().stream()
            .map(
                g ->
                    String.format(
                        rowTemplate, g.courseCode(), g.courseTitle(), g.credits(), g.average()))
            .collect(Collectors.joining());

    var baseEmail =
        """
    <!DOCTYPE html>
         <html>
         <head>
         <meta charset="UTF-8"/>
         <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
         <title>Academic Transcript</title>
         </head>
         <body style="margin: 0; padding: 0; background-color: #f4f7f6; font-family: Arial, Helvetica, sans-serif; color: #333333; -webkit-font-smoothing: antialiased;">
         <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background-color: #f4f7f6; padding: 40px 20px;">
             <tr>
             <td align="center">
                 <table role="presentation" width="600" cellspacing="0" cellpadding="0" border="0" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                 <tr>
                     <td style="background-color: rgb(223, 164, 8); padding: 30px; text-align: center; color: #ffffff;">
                     <h1 style="margin: 0; font-size: 24px; font-weight: bold; letter-spacing: 1px;">HEI Madagascar</h1>
                     <p style="margin: 5px 0 0; font-size: 14px; opacity: 0.9;">Academic Transcript</p>
                     </td>
                 </tr>
                 <tr>
                     <td style="padding: 30px 40px;">
                     <table width="100%%" cellspacing="0" cellpadding="0" border="0">
                         <tr>
                         <td valign="top">
                             <p style="margin: 0; font-size: 18px; font-weight: bold; color: #111827;">First name: %s</p>
                             <p style="margin: 0; font-size: 18px; color: #111827;">Last name: %s</p>
                             <p style="margin: 4px 0 0; font-size: 13px; color: #6b7280;">Reference: %s</p>
                         </td>
                         <td valign="top" align="right">
                             <p style="margin: 0; font-size: 13px; color: #6b7280;">Academic Year</p>
                             <p style="margin: 2px 0 0; font-size: 16px; font-weight: bold; color: #111827;">%s</p>
                         </td>
                         </tr>
                     </table>
                     </td>
                 </tr>
                 <tr>
                     <td style="padding: 0 40px 30px;">
                     <table width="100%%" cellspacing="0" cellpadding="0" border="0" style="border: 1px solid #e5e7eb; border-radius: 8px; overflow: hidden;">
                         <tr>
                         <td style="padding: 10px 20px; font-size: 12px; color: #9ca3af; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid #e5e7eb; background-color: #f9fafb;">Course</td>
                        <td style="padding: 10px 20px; font-size: 12px; color: #9ca3af; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid #e5e7eb; background-color: #f9fafb;">Title</td>
                        <td style="padding: 10px 20px; font-size: 12px; color: #9ca3af; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid #e5e7eb; background-color: #f9fafb; text-align: center;">Credit</td>
                         <td style="padding: 10px 20px; font-size: 12px; color: #9ca3af; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid #e5e7eb; background-color: #f9fafb; text-align: center;">Grade</td>
                         </tr>
                         %s
                     </table>
                     </td>
                 </tr>
                 <tr>
                     <td style="padding: 20px 40px">
                         <p style="margin: 2px 0 0; font-size: 20px; color: #6b7280;">Result:</p>
                         <ul style="margin: 8px 0 0; padding-left: 40px; font-size: 16px; color: #6b7280;">
                             <li style="margin: 2px 0;">Average: %s/20</li>
                             <li style="margin: 2px 0;">Credit: %s/60</li>
                         </ul>
                     </td>
                 </tr>
                 <tr>
                     <td style="background-color: #f9fafb; padding: 25px 40px; text-align: center; border-top: 1px solid #e5e7eb;">
                     <p style="margin: 0; font-size: 12px; color: #9ca3af;">
                         © 2026 HEI Madagascar. All rights reserved.
                     </p>
                     </td>
                 </tr>
                 </table>
             </td>
             </tr>
         </table>
         </body>
         </html>
""";

    this.email =
        String.format(
            baseEmail,
            student.getFirstName(),
            student.getLastName(),
            reference,
            academicYear,
            rowsHtml,
            grades.yearAverage(),
            grades.creditEarned());
  }
}
