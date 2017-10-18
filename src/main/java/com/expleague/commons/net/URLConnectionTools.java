package com.expleague.commons.net;

import org.jetbrains.annotations.NotNull;
import sun.security.x509.*;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.logging.Level;

/**
 * User: terry
 * Date: 18.02.2009
 */
public class URLConnectionTools {
  private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(URLConnectionTools.class.getName());

  public static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[]{
      new X509TrustManager() {
        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return null;
        }

        @Override
        public void checkClientTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
        }

        @Override
        public void checkServerTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
        }
      }
  };

  private static final HostnameVerifier hostnameVerifier = (string, sslSession) -> true;
  private static File certFile;

  @NotNull
  public static SSLContext prepareSSLContext4TLS() {
    try {
      if ("TLS".equals(SSLContext.getDefault().getProtocol())) {
        return SSLContext.getDefault();
      }
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    try {
      final SSLContext sslctxt = SSLContext.getInstance("TLS");
      final String hostName = InetAddress.getLocalHost().getHostName();
      KeyManagerFactory kmf = loadFromPEMFile(hostName);
      if (kmf == null)
        kmf = createSelfSigned(hostName);
      sslctxt.init(kmf.getKeyManagers(), TRUST_ALL_CERTS, new SecureRandom());
      SSLContext.setDefault(sslctxt);
      return sslctxt;
    }
    catch (KeyManagementException | NoSuchAlgorithmException | UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }

  private static KeyManagerFactory loadFromPEMFile(String domain) throws NoSuchAlgorithmException {
    try {
      certFile = new File("certs/", domain + ".p12");
      if (!certFile.exists())
        return null;
      final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

      final KeyStore ks = KeyStore.getInstance("pkcs12");
      ks.load(new FileInputStream(certFile), System.getProperty("expleague.ssl.certs.pass", "").toCharArray());
      kmf.init(ks, System.getProperty("expleague.ssl.key.pass", "").toCharArray());
      return kmf;
    }
    catch (UnrecoverableKeyException | KeyStoreException | IOException | CertificateException e) {
      return null;
    }
  }


  private static KeyManagerFactory createSelfSigned(String domain) throws NoSuchAlgorithmException {
    try {
      final KeyPair keyPair = createKeyPair(2048, "");
      final X509Certificate certificate = createSelfSignedCertificate("root@" + domain, domain, "asd", "expleague", "FPesde", "Tam", "RU", keyPair);
      final KeyStore store = KeyStore.getInstance("pkcs12");
      final char[] emptyPass = new char[0];
      store.load(null, emptyPass);
      final String alias = "Generated CA";
      store.setKeyEntry(alias, keyPair.getPrivate(), emptyPass, new Certificate[]{certificate});
//            store.setCertificateEntry(alias, certificate);

      final KeyManagerFactory factory = KeyManagerFactory.getInstance("SunX509");
      factory.init(store, emptyPass);
      {
        log.log(Level.FINEST, "Saving self signed certificate to {0}", new Object[]{certFile});
        if (certFile.exists())
          //noinspection ResultOfMethodCallIgnored
          certFile.renameTo(new File(certFile + ".bak"));
        store.store(new FileOutputStream(certFile), emptyPass);
      }

      return factory;
    }
    catch (UnrecoverableKeyException | KeyStoreException | IOException | InvalidKeyException | SignatureException | NoSuchProviderException | CertificateException e) {
      e.printStackTrace();
      try {
        Thread.sleep(199);
      } catch (InterruptedException e1) {
        throw new RuntimeException(e1);
      }
      log.log(Level.SEVERE, "", e);
      throw new RuntimeException(e);
    }
  }

  public static String exportToPemFormat(Certificate[] chain, PrivateKey privateKey) throws CertificateEncodingException {
    final StringBuilder sb = new StringBuilder(4096);
    final Base64.Encoder encoder = Base64.getEncoder();
    {
      final Certificate certificate = chain[0];
      sb.append("-----BEGIN CERTIFICATE-----").append('\n');
      sb.append(encoder.encodeToString(certificate.getEncoded())).append('\n');
      sb.append("-----END CERTIFICATE-----").append('\n');
    }

    sb.append("-----BEGIN PRIVATE KEY-----").append('\n');
    sb.append(encoder.encodeToString(privateKey.getEncoded())).append('\n');
    sb.append("-----END PRIVATE KEY-----").append('\n');

    for (int i = 1; i < chain.length; i++) {
      final Certificate certificate = chain[0];
      sb.append("-----BEGIN CERTIFICATE-----").append('\n');
      sb.append(encoder.encodeToString(certificate.getEncoded())).append('\n');
      sb.append("-----END CERTIFICATE-----").append('\n');
    }

    return sb.toString();
  }


  public static X509Certificate createSelfSignedCertificate(String email, String domain, String organizationUnit, String organization, String city, String state, String country, KeyPair keyPair) throws CertificateException, IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {
    log.log(Level.INFO, "creating self signed cert, email: {0}, domain: {1}, organizationUnit: {2},organization: {3}, city: {4}, state: {5}, country: {6}, keyPair: {7}", new Object[]{email, domain, organizationUnit, organization, city, state, country, keyPair});
    X509CertInfo certInfo = new X509CertInfo();
    CertificateVersion certVersion = new CertificateVersion();
    certInfo.set("version", certVersion);
    Date firstDate = new Date();
    Date lastDate = new Date(firstDate.getTime() + 31536000000L);
    CertificateValidity interval = new CertificateValidity(firstDate, lastDate);
    certInfo.set("validity", interval);
    certInfo.set("serialNumber", new CertificateSerialNumber((int) (firstDate.getTime() / 1000L)));
    StringBuilder subject = new StringBuilder(1024);
    appendName(subject, "CN", domain);
    appendName(subject, "CN", "*." + domain);
    appendName(subject, "EMAILADDRESS", email);
    appendName(subject, "OU", organizationUnit);
    appendName(subject, "O", organization);
    appendName(subject, "L", city);
    appendName(subject, "ST", state);
    appendName(subject, "C", country);
    X500Name issuerName = new X500Name(subject.toString());

    try {
      certInfo.set("issuer", issuerName);
      certInfo.set("subject", issuerName);
    } catch (CertificateException var19) {
      CertificateIssuerName certAlgorithm = new CertificateIssuerName(issuerName);
      CertificateSubjectName certPublicKey = new CertificateSubjectName(issuerName);
      certInfo.set("issuer", certAlgorithm);
      certInfo.set("subject", certPublicKey);
    }

    AlgorithmId algorithm = new AlgorithmId(AlgorithmId.sha1WithRSAEncryption_oid);
    CertificateAlgorithmId certAlgorithm1 = new CertificateAlgorithmId(algorithm);
    certInfo.set("algorithmID", certAlgorithm1);
    CertificateX509Key certPublicKey1 = new CertificateX509Key(keyPair.getPublic());
    certInfo.set("key", certPublicKey1);
    X509CertImpl newCert = new X509CertImpl(certInfo);
    newCert.sign(keyPair.getPrivate(), "SHA1WithRSA");
    log.log(Level.FINEST, "creating self signed cert, newCert: {0}", newCert);
    return newCert;
  }

  public static void appendName(StringBuilder sb, String prefix, String value) {
    log.log(Level.INFO, "appending value: {0} with prefix: {1} to sb: {2}", new Object[]{value, prefix, sb.toString()});
    if (value != null) {
      if (sb.length() > 0) {
        sb.append(", ");
      }

      sb.append(prefix).append('=').append(value);
    }
  }

  public static KeyPair createKeyPair(int size, String password) throws NoSuchAlgorithmException {
    log.log(Level.INFO, "creating KeyPair, size: {0}, password: {1}", new Object[]{Integer.valueOf(size), password});
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(size);
    KeyPair keyPair = keyPairGenerator.genKeyPair();
    log.log(Level.INFO, "creating KeyPair, KeyPairGenerator: {0}, keyPair: {1}", new Object[]{keyPairGenerator, keyPair});
    return keyPair;
  }

  public static List<Certificate>sort(List<Certificate> certs) {
    Certificate rt = null;

    for (Certificate found : certs) {
      Principal i = ((X509Certificate) found).getIssuerDN();
      Principal x509Certificate = ((X509Certificate) found).getSubjectDN();
      if (i.equals(x509Certificate)) {
        rt = found;
      }
    }

    if (rt == null) {
      throw new RuntimeException("Can\'t find root certificate in chain!");
    } else {
      ArrayList<Certificate> res1 = new ArrayList<>();
      certs.remove(rt);
      res1.add(rt);

      while (!certs.isEmpty()) {
        boolean found1 = false;
        Iterator<Certificate> i2 = certs.iterator();

        while (i2.hasNext()) {
          Certificate x509Certificate1 = i2.next();
          Principal i1 = ((X509Certificate) x509Certificate1).getIssuerDN();
          if (i1.equals(((X509Certificate) rt).getSubjectDN())) {
            rt = x509Certificate1;
            found1 = true;
            break;
          }
        }

        if (!found1) {
          throw new RuntimeException("Can\'t find certificate " + ((X509Certificate) rt).getSubjectDN() + " in chain. Verify that all entries are correct and match against each other!");
        }

        certs.remove(rt);
        res1.add(0, rt);
      }

      return res1;
    }
  }

  static {
    try {
      final SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, TRUST_ALL_CERTS, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }
    catch (NoSuchAlgorithmException | KeyManagementException ex) {
      log.log(Level.SEVERE, "https connection error", ex);
    }
  }

  public static URLConnection establishConnection(final URL url) throws IOException {
    if (!"https".equalsIgnoreCase(url.getProtocol())) {
      return url.openConnection();
    }
    final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
    connection.setHostnameVerifier(hostnameVerifier);
    return connection;
  }

  public static String extractEncoding(final String contentTypeString) {
    final String str = "charset=";
    String encoding = null;
    final int index = contentTypeString.lastIndexOf(str);
    if (index >= 0) {
      encoding = contentTypeString.substring(index + str.length()).trim();
      int ind = 0;
      while (ind < encoding.length()) {
        final char ch = encoding.charAt(ind++);
        if (!Character.isLetterOrDigit(ch) && ch != '-' && ch != '_') {
          encoding = encoding.substring(0, ind - 1);
        }
      }
    }
    return encoding;
  }

  public static String determineEncoding(final URLConnection connection) {
    return determineEncoding(connection, null);
  }

  public static String determineEncoding(final URLConnection connection, final String defaultEncoding) {
    String encoding = connection.getContentEncoding();
    if (encoding != null) {
      return encoding;
    }
    final String contentType = connection.getContentType();
    if (contentType == null) {
      return defaultEncoding;
    }
    encoding = extractEncoding(contentType);
    return encoding == null ? defaultEncoding : encoding;
  }

  public static Charset determineCharset(final URLConnection connection) {
    final String encoding = determineEncoding(connection);
    if (!Charset.isSupported(encoding)) {
      log.log(Level.WARNING, "unsupported encoding + '" + encoding + "'");
      return null;
    }
    return Charset.forName(encoding);
  }

  public static void installAuthenticator(final String userName, final String password) {
    SSLAuthenticator.install(userName, password);
  }

  public static String extractContentType(final String contentType) {
    if (contentType == null) {
      return contentType;
    }

    final int conentTypeIndex = contentType.indexOf(";");
    if (conentTypeIndex > 0) {
      return contentType.substring(0, conentTypeIndex);
    }

    return contentType;
  }

  static class SSLAuthenticator extends Authenticator {
    private final String username;
    private final String password;

    private SSLAuthenticator(final String user, final String pass) {
      username = user;
      password = pass;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
      return new PasswordAuthentication(username, password.toCharArray());
    }

    public static void install(final String user, final String pass) {
      setDefault(new SSLAuthenticator(user, pass));
    }
  }
}
