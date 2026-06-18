package it.gov.pagopa.noticenumber.algorithm;

import it.gov.pagopa.noticenumber.service.algorithm.AuxDigitIUVGeneratorAlgorithm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuxDigitIUVGeneratorAlgorithmTest {

    @Test
    void shouldGenerateValidIuvStructure() {
        // Given
        int auxDigit = 3;
        int segregationCode = 12;

        // When
        String iuv = AuxDigitIUVGeneratorAlgorithm.getInstance()
                .auxDigit(auxDigit)
                .segregationCode(segregationCode)
                .generate();

        // Then
        assertNotNull(iuv);
        assertEquals(17, iuv.length());
        assertTrue(iuv.startsWith("12"));
    }
}