package hei.school.graduate.service;

import static java.io.File.createTempFile;

import hei.school.graduate.endpoint.rest.controller.dto.GraduateStudentResponse;
import hei.school.graduate.file.bucket.BucketComponent;
import java.io.File;
import java.io.FileOutputStream;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class GraduateExportService {

  private final CohortService cohortService;
  private final BucketComponent bucketComponent;

  @SneakyThrows
  public String generateGraduatesDownloadUrl(String cohortId) {
    List<GraduateStudentResponse> graduates =
        cohortService.getCohortGraduates(UUID.fromString(cohortId));

    File file = createTempFile("graduates-" + cohortId, ".xlsx");

    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
      XSSFSheet sheet = workbook.createSheet("Graduates " + cohortId);
      XSSFRow header = sheet.createRow(0);
      header.createCell(0).setCellValue("Rank");
      header.createCell(1).setCellValue("Reference");
      header.createCell(2).setCellValue("Last name");
      header.createCell(3).setCellValue("First name");
      header.createCell(4).setCellValue("General average");

      int rank = 1;
      int rowIndex = 1;
      log.info("Found {} graduates for cohort {}", graduates.size(), cohortId);
      for (GraduateStudentResponse graduate : graduates) {
        XSSFRow row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(rank++);
        row.createCell(1).setCellValue(graduate.getReference());
        row.createCell(2).setCellValue(graduate.getLastName());
        row.createCell(3).setCellValue(graduate.getFirstName());
        row.createCell(4).setCellValue(graduate.getAverage());
      }

      for (int i = 0; i < 5; i++) {
        sheet.autoSizeColumn(i);
      }

      try (FileOutputStream fileOut = new FileOutputStream(file)) {
        workbook.write(fileOut);
      }
    }

    String bucketKey = "cohorts/" + cohortId + "/graduates.xlsx";
    bucketComponent.upload(file, bucketKey);
    return bucketComponent.presign(bucketKey, Duration.ofMinutes(10)).toString();
  }
}
