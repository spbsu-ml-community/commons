package com.spbsu.commons.net;

import com.spbsu.commons.util.logging.Logger;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * User: terry
 * Date: 18.02.2009
 */
public class URLConnectionTools {
    private static final Logger LOG = Logger.create(URLConnectionTools.class);

    private static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
                }

                public void checkServerTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
                }
            }
    };

    private static final HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        public boolean verify(final String string, final SSLSession sslSession) {
            return true;
        }
    };

    static {
        try {
            final SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, TRUST_ALL_CERTS, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException ex) {
            LOG.error("https connection error", ex);
        } catch (KeyManagementException ex) {
            LOG.error("https connection error", ex);
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
            LOG.warn("unsupported encoding + '" + encoding + "'");
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

        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password.toCharArray());
        }

        public static void install(final String user, final String pass) {
            setDefault(new SSLAuthenticator(user, pass));
        }
    }
}
