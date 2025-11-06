package springbook.chatbotserver;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.jupiter.api.Test;


public class JasyptEncryptorTest {

  @Test
  void encryptTest() {
    StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
    encryptor.setPassword("qweasdzxc12!");
    String plainText = "http://localhost:5005/model/parse"; // 여기에 평문 API 키 입력
    String encrypted = encryptor.encrypt(plainText);
    System.out.println("🔐 Encrypted: ENC(" + encrypted + ")");
  }

  @Test
  void decryptTest() {
    StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
    encryptor.setPassword("qweasdzxc12!");
    String encryptedText = "+N7WLIO0LxO464K7sJ7zwB5Jo9xBATvU"; // 여기에 암호화된 텍스트 입력
    String decrypted = encryptor.decrypt(encryptedText);
    System.out.println("🔓 Decrypted: " + decrypted);
  }
}
