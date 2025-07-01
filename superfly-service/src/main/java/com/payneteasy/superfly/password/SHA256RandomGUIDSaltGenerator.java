package com.payneteasy.superfly.password;

import com.payneteasy.superfly.common.utils.CryptoHelper;
import com.payneteasy.superfly.spisupport.SaltGenerator;
import com.payneteasy.superfly.utils.RandomGUID;
import org.springframework.stereotype.Service;

/**
 * Salt generator which uses SHA-256 from secure RandomGUID values.
 *
 * @author Roman Puchkovskiy
 */
@Service
public class SHA256RandomGUIDSaltGenerator implements SaltGenerator {

    public String generate() {
        RandomGUID guid = new RandomGUID(true);
        return CryptoHelper.SHA256(guid.toString());
    }

}
