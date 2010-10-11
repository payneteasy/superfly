package com.payneteasy.superfly.hotp;

import com.payneteasy.superfly.spisupport.HOTPDao;
import com.payneteasy.superfly.spisupport.HOTPProviderContext;
import com.payneteasy.superfly.spisupport.ObjectResolver;

/**
 * Simple implementation of a {@link HOTPProviderContext}.
 *
 * @author Roman Puchkovskiy
 */
public class HOTPProviderContextImpl implements HOTPProviderContext {
	
	private ObjectResolver objectResolver;
	private String masterKey;
	private int codeDigits;
	private int lookahead;
	private int tableSize;

	public HOTPProviderContextImpl(ObjectResolver objectResolver,
			String masterKey, int codeDigits, int lookahead, int tableSize) {
		super();
		this.objectResolver = objectResolver;
		this.masterKey = masterKey;
		this.codeDigits = codeDigits;
		this.lookahead = lookahead;
		this.tableSize = tableSize;
	}

	public int getCodeDigits() {
		return codeDigits;
	}

	public int getLookahead() {
		return lookahead;
	}

	public String getMasterKey() {
		return masterKey;
	}

	public ObjectResolver getObjectResolver() {
		return objectResolver;
	}

	public int getTableSize() {
		return tableSize;
	}

	public HOTPDao getHOTPDao() {
		return objectResolver.resolve(HOTPDao.class);
	}

}
