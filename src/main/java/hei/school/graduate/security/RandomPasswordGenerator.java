package hei.school.graduate.security;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class RandomPasswordGenerator {

  private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
  private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String DIGITS = "0123456789";
  private static final String SYMBOLS = "!@#$%&*";
  private static final int LENGTH = 12;

  private final SecureRandom random = new SecureRandom();

  public String generate() {
    var all = LOWERCASE + UPPERCASE + DIGITS + SYMBOLS;
    var password = new StringBuilder(LENGTH);
    password.append(randomChar(LOWERCASE));
    password.append(randomChar(UPPERCASE));
    password.append(randomChar(DIGITS));
    password.append(randomChar(SYMBOLS));
    for (int i = 4; i < LENGTH; i++) {
      password.append(randomChar(all));
    }
    return shuffle(password.toString());
  }

  private char randomChar(String charset) {
    return charset.charAt(random.nextInt(charset.length()));
  }

  private String shuffle(String input) {
    var chars = input.toCharArray();
    for (int i = chars.length - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      char tmp = chars[i];
      chars[i] = chars[j];
      chars[j] = tmp;
    }
    return new String(chars);
  }
}
