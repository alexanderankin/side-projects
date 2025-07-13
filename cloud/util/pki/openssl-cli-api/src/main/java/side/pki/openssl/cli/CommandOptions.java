package side.pki.openssl.cli;

public sealed interface CommandOptions {
    /**
     * <pre>
        Usage: ciphers [options] [cipher]

        General options:
         -help               Display this summary

        Output options:
         -v                  Verbose listing of the SSL/TLS ciphers
         -V                  Even more verbose
         -stdname            Show standard cipher names
         -convert val        Convert standard name into OpenSSL name

        Cipher specification options:
         -s                  Only supported ciphers
         -tls1               Ciphers compatible with TLS1
         -tls1_1             Ciphers compatible with TLS1.1
         -tls1_2             Ciphers compatible with TLS1.2
         -tls1_3             Ciphers compatible with TLS1.3
         -psk                Include ciphersuites requiring PSK
         -srp                (deprecated) Include ciphersuites requiring SRP
         -ciphersuites val   Configure the TLSv1.3 ciphersuites to use

        Provider options:
         -provider-path val  Provider load path (must be before 'provider' argument if required)
         -provider val       Provider to load (can be specified multiple times)
         -propquery val      Property query used when fetching algorithms

        Parameters:
         cipher              Cipher string to decode (optional)
     * </pre>
     */
    enum CiphersOptions implements CommandOptions {
    }
}
