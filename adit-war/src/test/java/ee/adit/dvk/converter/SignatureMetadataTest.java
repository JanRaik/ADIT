package ee.adit.dvk.converter;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import dvk.api.container.v2_1.SignatureMetadata;
import ee.adit.dao.pojo.Document;
import ee.adit.dao.pojo.Signature;

/**
 * @author Hendrik Pärna
 * @since 6.05.14
 */
public class SignatureMetadataTest {

    @Test
    public void testSignatureVerificationDate() throws Exception {
        DocumentToContainerVer2_1ConverterImpl converter = new DocumentToContainerVer2_1ConverterImpl();
        Document document = DocumentTestingUtil.createTestDocument();
        Set<Signature> signatures = new HashSet<Signature>();
        Signature s1 = new Signature();
        s1.setSigningDate(new Date());
        signatures.add(s1);
        document.setSignatures(signatures);

        List<SignatureMetadata> signatureMetadatas = converter.createSignatureMetadata(document);
        Assert.assertNotNull(signatureMetadatas);
        Assert.assertEquals(s1.getSigningDate(), signatureMetadatas.get(0).getSignatureVerificationDate());
    }
}
