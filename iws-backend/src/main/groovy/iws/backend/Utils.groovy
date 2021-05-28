package iws.backend

import grails.converters.JSON
import groovy.json.JsonSlurper
import org.mindrot.BCrypt

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.regex.*
import java.net.http.*
import org.imgscalr.Scalr
import org.apache.commons.codec.binary.Base64;
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class Utils {

  static LocalDateTime referenceDate = stringToDate("01/01/2000 05:00")
  // Reference date has to be a Sunday for the week calculations to be accurate
  static LocalDateTime referenceDateWeek = stringToDate("02/01/2000 05:00")

  static String getTimestamp(Date date = new Date()) {
    return date.toTimestamp()
  }

  static Map getErrorAsMap(AppException exception, boolean addDebugMessage = false) {
    def result = [
        error    : exception.message,
        errorId  : exception.id,
        category : exception.category,
        timestamp: getTimestamp()
    ]
    if (addDebugMessage) result['logMessage'] = exception.logMessage
    return result
  }

  static boolean isDebugEnvironment(String environmentName) {
    environmentName == 'Development' || environmentName == 'QA'
  }

  static Map getSubObjectAsMap(def o, ArrayList properties) {
    def result = [:]
    properties.each {
      result[it] = o[it]
    }
    result['timestamp'] = getTimestamp()
    return result
  }

  static Map allDatesToStrings(Map input) {
    Map result = [:]
    input.each { it ->
      String key = it.key
      def value = it.value
      result[key] = (value instanceof LocalDateTime ? dateToString(value) : value)
    }
    return result
  }

  static boolean stringToBoolean(String input) {
    String v = input.toLowerCase()
    return (v == 'true' || v == 'yes' || v == '1')
  }

  static String dateToString(LocalDateTime value) {
    return value.format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm"))
  }

  static LocalDateTime stringToDate(String input) {
    def segments = input.split(' ')
    String value = segments.length < 2 ? "${segments[0]} 05:00" : input
    return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
  }

  static int stringToInt(String input) {
    if (!input)
      throw new InvalidDataType()
    String pattern = '^[0-9]+$'
    boolean checkRegularExpression = isMatchingRegex(pattern, input)
    if (checkRegularExpression)
      return Integer.parseInt(input.toString())
    throw new InvalidDataType()
  }

  static LocalTime stringToTime(String input) {
    if (!input)
      throw new InvalidDataType()
    return LocalTime.parse(input, DateTimeFormatter.ofPattern("hh:mm a"))
  }

  static String timeToString(LocalTime time) {
    return time.format(DateTimeFormatter.ofPattern("hh:mm a"))
  }

  static Map getDateAsDayWeekMonthYear(LocalDateTime dt) {
    return [
        date : dt,
        day  : ChronoUnit.DAYS.between(referenceDate, dt),
        week : ChronoUnit.WEEKS.between(referenceDateWeek, dt),
        month: ChronoUnit.MONTHS.between(referenceDate, dt),
        year : ChronoUnit.YEARS.between(referenceDate, dt)
    ]
  }

  static Map getMapFromString(String inputString) {
    def jsonSlurper = new JsonSlurper()
    return jsonSlurper.parseText(inputString)
  }

  static void ensureRequestBodyExists(String body) {
    if (!body) throw new AppException('invalid input', 'validation')
  }

  static Map restCall(String url, String method = 'GET', Map payload = [:], Map headers = [:]) {
    def connection = new URL(url).openConnection() as HttpURLConnection
    def body = (payload as JSON).toString()
    connection.requestMethod = method
    connection.doOutput = true
    connection.setRequestProperty("Content-Type", "application/json")
    connection.setRequestProperty("Accept", "application/json")
    // TODO:
    //        headers << [
    //                'Content-Type': 'application/json',
    //                'Accept'      : 'application/json'
    //        ]
    headers.keySet().each {
      connection.setRequestProperty(it, headers[it])
    }
    if (method != 'GET')
      connection.outputStream.write(body.getBytes('UTF-8'))
    def responseCode = connection.responseCode
    def responseBody = responseCode < 400 ?
        connection.inputStream.text : connection.errorStream.text
    return [
        status  : responseCode,
        response: (new JsonSlurper()).parseText(responseBody)
    ]
  }

  static boolean isTestEnvironment() {
    return grails.util.Environment.current == grails.util.Environment.TEST
  }

  static String encryptString(String string) {
    BCrypt.hashpw(string, BCrypt.gensalt(12))
  }

  static boolean isValidEncryptedString(String string, String encryptedString) {
    BCrypt.checkpw(string, encryptedString)
  }

  static int getAsInt(String input, int defaultValue = 0) {
    return input ? (input as int) : defaultValue
  }

  static boolean getAsBoolean(String input, boolean defaultValue = false) {
    if (input != null) {
      String i = input.toLowerCase()
      return (i == 'yes' || i == '1' || i == 'true')
    } else return defaultValue
  }

  static String getAsString(String input, String defaultValue = null) {
    return input ? input : defaultValue
  }

  static boolean isPasswordStrong(String password) {
    if (password.size() < 8) return false
    if (!(password =~ /[0-9]/)) return false
    if (!(password =~ /[a-z]/)) return false
    // TODO: Password restriction relaxed.
    //        if (!(password =~ /[A-Z]/)) return false
    //        if (!(password =~ /[%@$#!&*^]/)) return false
    //        if (password =~ /\s/) return false
    return true
  }

  static Map removeEmptyKeys(Map data) {
    Map result = [:]
    data.keySet().each { key ->
      if (data[key] != null) {
        def val = data[key] instanceof Map ?
            removeEmptyKeys(data[key]) : data[key]
        result.put(key, val)
      }
    }
    return result
  }

  static String getSessionId(def header) {
    return header != null ? header.split(' ')[1] : null
  }

  static String getAdminSessionId(def header) {
    return header != null ? header.split(' ')[0] : null
  }

  static UUID newId() {
    return UUID.randomUUID()
  }

  static List<String> getKeys(Map map) {
    return map.keySet() as String[]
  }

  static boolean isMatchingRegex(String regex, String value) {
    return Pattern.matches(regex, value)
  }

  static int strLength(String str) {
    return str.size()
  }

  static LocalDateTime getNow() { LocalDateTime.now(ZoneOffset.UTC) }

  static LocalDateTime getToday() { fixTime(getNow()) }

  static LocalDateTime fixTime(LocalDateTime value) {
    String dt = "${dateToString(value).split(' ')[0]} 05:00"
    return stringToDate(dt)
  }

  static String createOTP(int length) {
    String values = "0123456789"
    Random random = new Random();
    char[] otp = new char[length];
    for (int i = 0; i < length; i++) {
      otp[i] = values.charAt(random.nextInt(values.size()));
    }
    return otp
  }

  static String giveMeKey(int length) {
    def key
    String alphabet = (('A'..'N') + ('P'..'Z') + ('0'..'9')).join()
    key = new Random().with {
      (1..length).collect { alphabet[nextInt(alphabet.length())] }.join()
    }
    return key
  }

  static String toLowerCase(String str) {
    return str.toLowerCase()
  }

  static String toTitleCase(String str) {
    return str.split(' ').collect {
      String word -> word.capitalize()
    }.join(' ')
  }

  static Map httpMethod = [
      get   : 'GET',
      post  : 'POST',
      put   : 'PUT',
      delete: 'DELETE'
  ]

  static Map httpStatus = [
      success        : 200,
      error          : 400,
      unauthorized   : 401,
      unexpectedError: 500
  ]

  static BufferedImage resizeImage(BufferedImage img, int width, int height) {
    return Scalr.resize(img, Scalr.Method.QUALITY, Scalr.Mode.FIT_TO_WIDTH, width, height, Scalr.OP_ANTIALIAS)
  }

  static Map imageFormat = [
      jpg: 'JPG',
      png: 'PNG'
  ]

  static def splitToColumns(String line, String delimiter = ',', String quote = '"') {
    def columns = []
    StringBuilder col = new StringBuilder()
    boolean isInQuotes = false
    line.each { String c ->
      switch (c) {
        case delimiter:
          if (!isInQuotes) {
            columns.add(col.toString())
            col.delete(0, col.length())
          } else
            col.append(c)
          break
        case quote:
          isInQuotes = !isInQuotes
          break
        default:
          col.append(c)
      }
    }
    columns.add(col.toString())
    return columns
  }

  static void resizeImageFile(String inputFile, String outputFile, String format, int width, int height) {
    File input = new File(inputFile)
    BufferedImage file = ImageIO.read(input)
    // Convert to RGB. Files like PNG are in ARGB color space.
    BufferedImage image = new BufferedImage(file.width, file.height, BufferedImage.TYPE_INT_RGB)
    image.createGraphics().drawImage(file, 0, 0, null)
    BufferedImage resized = resizeImage(image, width, height)
    File output = new File(outputFile)
    if (!ImageIO.write(resized, imageFormat[format], output)) {
      throw new AppException('Unexpected error writing image', 'process',
          "Error while resizing image ${inputFile} to ${outputFile}, format ${format}, ${width} x ${height}");
    }
  }

  static String encrypt(String Data, def grailsApplication) throws Exception {
    String encryptKey = grailsApplication.config.getProperty('grails.chat.encrypt-decrypt-key')
    Base64 base64 = new Base64(true);
    try {
      SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes("UTF8"), "Blowfish");
      Cipher cipher = Cipher.getInstance("Blowfish");
      cipher.init(Cipher.ENCRYPT_MODE, key);
      return base64.encodeToString(cipher.doFinal(Data.getBytes("UTF8")));
    } catch (Exception e) {
      throw new AppException('Unexpected error in encrypt a message', 'ChatMessage', "Error ${e.stackTrace}");
    }
  }

  public static String decrypt(String encrypted, def grailsApplication) throws Exception {
    String decryptKey = grailsApplication.config.getProperty('grails.chat.encrypt-decrypt-key')
    Base64 base64 = new Base64(true);
    try {
      byte[] encryptedData = base64.decodeBase64(encrypted);
      SecretKeySpec key = new SecretKeySpec(decryptKey.getBytes("UTF8"), "Blowfish");
      Cipher cipher = Cipher.getInstance("Blowfish");
      cipher.init(Cipher.DECRYPT_MODE, key);
      byte[] decrypted = cipher.doFinal(encryptedData);
      return new String(decrypted);
    } catch (Exception e) {
      throw new AppException('Unexpected error in decrypt a message', 'ChatMessage', "Error ${e.stackTrace}");
    }
  }

}
