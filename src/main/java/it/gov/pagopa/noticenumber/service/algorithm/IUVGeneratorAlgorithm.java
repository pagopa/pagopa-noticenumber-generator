package it.gov.pagopa.noticenumber.service.algorithm;

/**
 * IUV code generator with IUV algorithm interface
 */
public interface IUVGeneratorAlgorithm {

    /**
     * Generates the IUV Code
     *
     * @return the IUV Code
     */
    String generate();
}
