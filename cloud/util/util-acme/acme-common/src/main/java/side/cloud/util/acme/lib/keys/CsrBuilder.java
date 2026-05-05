package side.cloud.util.acme.lib.keys;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import side.cloud.util.acme.lib.model.AcmeResources.Order;

import java.net.IDN;
import java.util.Base64;
import java.util.Locale;

/**
 * <p>
 * builds a csr for acme servers, following rfc8555 7.4
 * </p>
 * <p>
 * this class generates a {@link Csr} where {@link Csr#asCsrValue()} returns:
 * </p>
 * <blockquote>
 * csr (required, string):  A CSR encoding the parameters for the
 * certificate being requested [RFC2986].  The CSR is sent in the
 * base64url-encoded version of the DER format.  (Note: Because this
 * field uses base64url, and does not include headers, it is
 * different from PEM.)
 * </blockquote>
 * <p>
 *
 * </p>
 *
 * @see <a href=>rfc8555: 7.4 Applying for Certificate Issuance</a>
 * @see <a href=https://datatracker.ietf.org/doc/html/rfc2986>PKCS #10 (Csr syntax v1.7)</a>
 * @see <a href=https://datatracker.ietf.org/doc/html/rfc3490>Internationalizing Domains</a>
 */
@Data
@Accessors(chain = true)
public class CsrBuilder {
    Order order;

    /**
     * @see <a href=https://datatracker.ietf.org/doc/html/rfc3490>Internationalizing Domains</a>
     */
    String fixForIdna(String domain) {
        return IDN.toASCII(domain.trim()).toLowerCase(Locale.ENGLISH);
    }

    @SneakyThrows
    public Csr build(SupportedClientKeyPair keyPair) {
        var name = new X500NameBuilder(X500Name.getDefaultStyle()).build();
        var pubKey = keyPair.getKeyPair().getPublic();

        var generalNameList = order.getIdentifiers().stream()
                .map(acmeIdentifier -> switch (acmeIdentifier.getType()) {
                    case dns -> new GeneralName(GeneralName.dNSName, fixForIdna(acmeIdentifier.getValue()));
                    case ip -> new GeneralName(GeneralName.iPAddress, acmeIdentifier.getValue());
                })
                .toList();
        var san = new GeneralNames(generalNameList.toArray(new GeneralName[0]));

        var extGen = new ExtensionsGenerator();
        extGen.addExtension(Extension.subjectAlternativeName, false, san);
        // ocsp?
        // extGen.addExtension(X509ObjectIdentifiers.id_ad_ocsp, false, new byte[0]);
        var ext = extGen.generate();

        var pkcs10Builder = new JcaPKCS10CertificationRequestBuilder(name, pubKey);
        pkcs10Builder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, ext);

        PKCS10CertificationRequest csr = pkcs10Builder.build(keyPair.contentSigner());
        return new PKCS10Csr(csr);
    }

    public interface Csr {
        String asCsrValue();
    }

    public record PKCS10Csr(PKCS10CertificationRequest csr) implements Csr {
        @SneakyThrows
        public String asCsrValue() {
            var derEncoding = csr.toASN1Structure().getEncoded(ASN1Encoding.DER);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(derEncoding);
        }
    }
}
