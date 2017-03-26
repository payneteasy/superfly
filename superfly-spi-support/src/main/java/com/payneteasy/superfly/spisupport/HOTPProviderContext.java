package com.payneteasy.superfly.spisupport;

/**
 * Used to supply parameters and dependencies to a HOTP implementation.
 *
 * @author Roman Puchkovskiy
 */
public interface HOTPProviderContext {
    /**
     * Returns object resolver which may be used to resolve dependencies.
     *
     * @return object resolver
     */
    ObjectResolver getObjectResolver();

    /**
     * Returns a master key used to generate HOTP secret keys per user.
     *
     * @return
     */
    String getMasterKey();

    /**
     * Returns number of digits in the generated HOTP value.
     *
     * @return
     */
    int getCodeDigits();

    /**
     * Returns a lookahead window size for HOTP matching. Minimal size is 1.
     *
     * @return window size
     */
    int getLookahead();

    /**
     * Returns a table size. For counter values above this limit authentication
     * will fail.
     *
     * @return table size
     */
    int getTableSize();

    /**
     * Returns a DAO to work with HOTP.
     *
     * @return DAO
     */
    HOTPDao getHOTPDao();
}
