package com.payneteasy.superfly.wicket;

import org.apache.wicket.*;
import org.apache.wicket.protocol.http.*;
//import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
//import org.apache.wicket.request.IRequestCodingStrategy;
//import org.apache.wicket.request.RequestParameters;
//import org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy;
import org.apache.wicket.util.crypt.ICrypt;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
//import org.apache.wicket.util.string.UrlUtils;
import org.apache.wicket.util.value.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author kuccyp
 */
// TODO: remove it or implement it
public class SessionStoreUrlWebRequestCodingStrategy/* implements IRequestCodingStrategy*/ {
//    /**
//     * log.
//     */
//    private static final Logger log = LoggerFactory.getLogger(SessionStoreUrlWebRequestCodingStrategy.class);
//
//    /**
//     * The default request coding strategy most of the methods are delegated to
//     */
//    private final IRequestCodingStrategy defaultStrategy;
//
//    /**
//     * Construct.
//     *
//     * @param defaultStrategy The default strategy most requests are forwarded to
//     */
//    public SessionStoreUrlWebRequestCodingStrategy(final IRequestCodingStrategy defaultStrategy) {
//        this.defaultStrategy = defaultStrategy;
//    }
//
//    public void putInSession(String aSessionKey,String aValue){
//        WebRequestCycle rc = (WebRequestCycle) RequestCycle.get();
//
//        // get http session, create if necessary
//        HttpSession session = rc.getWebRequest().getHttpServletRequest().getSession(true);
//
//        // retrieve or generate encryption key from session
//        final String keyAttr = rc.getApplication().getApplicationKey() + "." + aSessionKey;
//        session.setAttribute(keyAttr, aValue);
//    }
//
//    /**
//     * Decode the querystring of the URL
//     *
//     * @see org.apache.wicket.request.IRequestCodingStrategy#decode(org.apache.wicket.Request)
//     */
//    public RequestParameters decode(final Request request) {
//
//        String url = request.decodeURL(request.getURL());
//
//
//        String decodedQueryParams = decodeURL(url);
//
//        if (decodedQueryParams != null) {
//            // The difficulty now is that this.defaultStrategy.decode(request)
//            // doesn't know the just decoded url which is why must create
//            // a fake Request for.
//            Request fakeRequest = new DecodedUrlRequest(request, url, decodedQueryParams);
//            return defaultStrategy.decode(fakeRequest);
//        }
//
//        return defaultStrategy.decode(request);
//    }
//
//    /**
//     * Encode the querystring of the URL
//     *
//     * @see org.apache.wicket.request.IRequestCodingStrategy#encode(org.apache.wicket.RequestCycle,
//     *      org.apache.wicket.IRequestTarget)
//     */
//    public CharSequence encode(final RequestCycle requestCycle, final IRequestTarget requestTarget) {
//        CharSequence url = defaultStrategy.encode(requestCycle, requestTarget);
//        url = encodeURL(url);
//        return url;
//    }
//
//    /**
//     * @see org.apache.wicket.request.IRequestTargetMounter#mount(org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy)
//     */
//    public void mount(IRequestTargetUrlCodingStrategy urlCodingStrategy) {
//        defaultStrategy.mount(urlCodingStrategy);
//    }
//
//    /**
//     * @see org.apache.wicket.request.IRequestTargetMounter#unmount(java.lang.String)
//     */
//    public void unmount(String path) {
//        defaultStrategy.unmount(path);
//    }
//
//    /**
//     * @see org.apache.wicket.request.IRequestTargetMounter#addIgnoreMountPath(java.lang.String)
//     */
//    public void addIgnoreMountPath(String path) {
//        defaultStrategy.addIgnoreMountPath(path);
//    }
//
//    /**
//     * @see org.apache.wicket.request.IRequestTargetMounter#urlCodingStrategyForPath(java.lang.String)
//     */
//    public IRequestTargetUrlCodingStrategy urlCodingStrategyForPath(String path) {
//        return defaultStrategy.urlCodingStrategyForPath(path);
//    }
//
//    /**
//     * @see org.apache.wicket.request.IRequestTargetMounter#pathForTarget(org.apache.wicket.IRequestTarget)
//     */
//    public CharSequence pathForTarget(IRequestTarget requestTarget) {
//        return defaultStrategy.pathForTarget(requestTarget);
//    }
//
//    /**
//     * @see org.apache.wicket.request.IRequestTargetMounter#targetForRequest(org.apache.wicket.request.RequestParameters)
//     */
//    public IRequestTarget targetForRequest(RequestParameters requestParameters) {
//        return defaultStrategy.targetForRequest(requestParameters);
//    }
//
//    /**
//     * Returns the given url encoded.
//     *
//     * @param url The URL to encode
//     * @return The encoded url
//     */
//    protected CharSequence encodeURL(final CharSequence url) {
//        // Get the crypt implementation from the application
//        ICrypt urlCrypt = Application.get().getSecuritySettings().getCryptFactory().newCrypt();
//        if (urlCrypt != null) {
//            // The url must have a query string, otherwise keep the url
//            // unchanged
//            final int pos = url.toString().indexOf('?');
//            if (pos > -1) {
//                // The url's path
//                CharSequence urlPrefix = url.subSequence(0, pos);
//
//                // Extract the querystring
//                String queryString = url.subSequence(pos + 1, url.length()).toString();
//
//                // if the querystring starts with a parameter like
//                // "x=", than don't change the querystring as it
//                // has been encoded already
//                if (!queryString.startsWith("x=")) {
//                    // The length of the encrypted string depends on the
//                    // length of the original querystring. Let's try to
//                    // make the querystring shorter first without loosing
//                    // information.
//                    queryString = shortenUrl(queryString).toString();
//
//                    // encrypt the query string
//                    String encryptedQueryString = urlCrypt.encryptUrlSafe(queryString);
//
//                    String encryptedQueryStringKey= WicketURLEncoder.QUERY_INSTANCE.encode(encryptedQueryString);
//
//                    String queryStringKey=getFromSession(encryptedQueryStringKey);
//
//                    if(queryStringKey==null){
//                        queryStringKey= UUID.randomUUID().toString().replace("-","").toUpperCase();
//                        putInSession(encryptedQueryStringKey,queryStringKey);
//                    }
//
//                    putInSession(queryStringKey,encryptedQueryString);
//
//                    encryptedQueryString = WicketURLEncoder.QUERY_INSTANCE.encode(queryStringKey);
//
//                    // build the new complete url
//                    return new AppendingStringBuffer(urlPrefix).append("?x=").append(
//                            encryptedQueryString);
//                }
//            }
//        }
//
//        // we didn't change anything
//        return url;
//    }
//
//    /**
//     * Decode the "x" parameter of the querystring
//     *
//     * @param url The encoded URL
//     * @return The decoded 'x' parameter of the querystring
//     */
//    protected String decodeURL(final String url) {
//        int startIndex = url.indexOf("?x=");
//        if (startIndex == -1) {
//            startIndex = url.indexOf("&x=");
//        }
//
//        if (startIndex != -1) {
//            try {
//                startIndex = startIndex + 3;
//                final int endIndex = url.indexOf("&", startIndex);
//                String secureParam;
//                if (endIndex == -1) {
//                    secureParam = url.substring(startIndex);
//                } else {
//                    secureParam = url.substring(startIndex, endIndex);
//                }
//
//                secureParam = WicketURLDecoder.QUERY_INSTANCE.decode(secureParam);
//
//
//                // get strin from session
//                String queryString = getFromSession(secureParam);
//
//                // Get the crypt implementation from the application
//                final ICrypt urlCrypt = Application.get()
//                        .getSecuritySettings()
//                        .getCryptFactory()
//                        .newCrypt();
//
//                // Decrypt the query string
//                queryString = urlCrypt.decryptUrlSafe(queryString);
//
//
//                // The querystring might have been shortened (length reduced).
//                // In that case, lengthen the query string again.
//                queryString = rebuildUrl(queryString);
//                return queryString;
//            } catch (Exception ex) {
//                return onError(ex, url);
//            }
//        }
//        return null;
//    }
//
//    public String getFromSession(String aSessionKey) {
//        WebRequestCycle rc = (WebRequestCycle)RequestCycle.get();
//
//        // get http session, create if necessary
//        HttpSession session = rc.getWebRequest().getHttpServletRequest().getSession(true);
//
//        // retrieve or generate encryption key from session
//        final String keyAttr = rc.getApplication().getApplicationKey() + "." + aSessionKey;
//
//        String result=(String)session.getAttribute(keyAttr);
//
//        //clear key in session
//        //session.removeAttribute(keyAttr);
//
//        return result;
//    }
//
//    /**
//     * @param ex
//     * @return xxx
//     * @deprecated Use {@link #onError(Exception, String)}
//     */
//    @Deprecated
//    protected String onError(final Exception ex) {
//        throw new PageExpiredException("Invalid URL", ex);
//    }
//
//    /**
//     * @param ex
//     * @param url
//     * @return error text
//     */
//    protected String onError(final Exception ex, String url) {
//        log.info("Invalid URL: " + url + ", exception type: " + ex.getClass().getName() +
//                ", exception message:" + ex.getMessage());
//        return onError(ex);
//    }
//
//    /**
//     * Try to shorten the querystring without loosing information. Note: WebRequestWithCryptedUrl
//     * must implement exactly the opposite logic.
//     *
//     * @param queryString The original query string
//     * @return The shortened querystring
//     */
//    protected CharSequence shortenUrl(CharSequence queryString) {
//        queryString = Strings.replaceAll(queryString,
//                WebRequestCodingStrategy.BEHAVIOR_ID_PARAMETER_NAME + "=", "1*");
//        queryString = Strings.replaceAll(queryString,
//                WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=IRedirectListener", "2*");
//        queryString = Strings.replaceAll(queryString,
//                WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=IFormSubmitListener", "3*");
//        queryString = Strings.replaceAll(queryString,
//                WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=IOnChangeListener", "4*");
//        queryString = Strings.replaceAll(queryString,
//                WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=ILinkListener", "5*");
//        queryString = Strings.replaceAll(queryString,
//                WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=", "6*");
//        queryString = Strings.replaceAll(queryString,
//                WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME + "=", "7*");
//
//        // For debugging only: determine possibilities to further shorten
//        // the query string
//        if (log.isDebugEnabled()) {
//            // Every word with at least 3 letters
//            Pattern words = Pattern.compile("\\w\\w\\w+");
//            Matcher matcher = words.matcher(queryString);
//            while (matcher.find()) {
//                CharSequence word = queryString.subSequence(matcher.start(), matcher.end());
//                log.debug("URL pattern NOT shortened: '" + word + "' - '" + queryString + "'");
//            }
//        }
//
//        return queryString;
//    }
//
//    /**
//     * In case the query string has been shortened prior to encryption, than rebuild (lengthen) the
//     * query string now. Note: This implementation must exactly match the reverse one implemented in
//     * WebResponseWithCryptedUrl.
//     *
//     * @param queryString The URL's query string
//     * @return The lengthened query string
//     */
//    protected String rebuildUrl(CharSequence queryString) {
//        queryString = Strings.replaceAll(queryString, "1*",
//                WebRequestCodingStrategy.BEHAVIOR_ID_PARAMETER_NAME + "=");
//        queryString = Strings.replaceAll(queryString, "2*",
//                WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=IRedirectListener");
//        queryString = Strings.replaceAll(queryString, "3*",
//                WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=IFormSubmitListener");
//        queryString = Strings.replaceAll(queryString, "4*",
//                WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=IOnChangeListener");
//        queryString = Strings.replaceAll(queryString, "5*",
//                WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=ILinkListener");
//        queryString = Strings.replaceAll(queryString, "6*",
//                WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME + "=");
//        queryString = Strings.replaceAll(queryString, "7*",
//                WebRequestCodingStrategy.BOOKMARKABLE_PAGE_PARAMETER_NAME + "=");
//
//        return queryString.toString();
//    }
//
//    /**
//     * @see org.apache.wicket.request.IRequestCodingStrategy#rewriteStaticRelativeUrl(java.lang.String)
//     */
//    public String rewriteStaticRelativeUrl(String string) {
//        return UrlUtils.rewriteToContextRelative(string, RequestCycle.get().getRequest());
//    }
//
//    /**
//     * IRequestCodingStrategy.decode(Request) requires a Request parameter and not a URL. Hence,
//     * based on the original URL and the decoded 'x' parameter a new Request object must be created
//     * to serve the default coding strategy as input for analyzing the URL.
//     */
//    private static class DecodedUrlRequest extends Request {
//        /**
//         * The original request
//         */
//        private final Request request;
//
//        /**
//         * The new URL with the 'x' param decoded
//         */
//        private final String url;
//
//        /**
//         * The new parameter map with the 'x' param removed and the 'new' one included
//         */
//        private final Map parameterMap;
//
//        /**
//         * Construct.
//         *
//         * @param request
//         * @param url
//         * @param encodedParamReplacement
//         */
//        public DecodedUrlRequest(final Request request, final String url,
//                                 final String encodedParamReplacement) {
//            this.request = request;
//
//            // Create a copy of the original parameter map
//            parameterMap = this.request.getParameterMap();
//
//            // Remove the 'x' parameter which contains ALL the encoded params
//            parameterMap.remove("x");
//            // first replace all &amp; with & else the they wont be encoded because there where
//            // encrypted.
//            String decodedParamReplacement = Strings.replaceAll(encodedParamReplacement, "&amp;",
//                    "&").toString();
//
//            decodedParamReplacement = WicketURLDecoder.QUERY_INSTANCE.decode(decodedParamReplacement);
//
//            // Add ALL of the params from the decoded 'x' param
//            ValueMap params = new ValueMap();
//            RequestUtils.decodeParameters(decodedParamReplacement, params);
//            parameterMap.putAll(params);
//
//            // Rebuild the URL with the 'x' param removed
//            int pos1 = url.indexOf("?x=");
//            if (pos1 == -1) {
//                pos1 = url.indexOf("&x=");
//            }
//            if (pos1 == -1) {
//                throw new WicketRuntimeException("Programming error: we should come here");
//            }
//            int pos2 = url.indexOf("&");
//
//            AppendingStringBuffer urlBuf = new AppendingStringBuffer(url.length() +
//                    encodedParamReplacement.length());
//            urlBuf.append(url.subSequence(0, pos1 + 1));
//            urlBuf.append(encodedParamReplacement);
//            if (pos2 != -1) {
//                urlBuf.append(url.substring(pos2));
//            }
//            this.url = urlBuf.toString();
//        }
//
//        /**
//         * Delegate to the original request
//         *
//         * @see org.apache.wicket.Request#getLocale()
//         */
//        @Override
//        public Locale getLocale() {
//            return request.getLocale();
//        }
//
//        /**
//         * @see org.apache.wicket.Request#getParameter(java.lang.String)
//         */
//        @Override
//        public String getParameter(final String key) {
//            if (key == null) {
//                return null;
//            }
//
//            Object val = parameterMap.get(key);
//            if (val == null) {
//                return null;
//            } else if (val instanceof String[]) {
//                String[] arrayVal = (String[]) val;
//                return arrayVal.length > 0 ? arrayVal[0] : null;
//            } else if (val instanceof String) {
//                return (String) val;
//            } else {
//                // never happens, just being defensive
//                return val.toString();
//            }
//        }
//
//        /**
//         * @see org.apache.wicket.Request#getParameterMap()
//         */
//        @Override
//        public Map getParameterMap() {
//            return parameterMap;
//        }
//
//        /**
//         * @see org.apache.wicket.Request#getParameters(java.lang.String)
//         */
//        @Override
//        public String[] getParameters(final String key) {
//            if (key == null) {
//                return null;
//            }
//
//            Object val = parameterMap.get(key);
//            if (val == null) {
//                return null;
//            } else if (val instanceof String[]) {
//                return (String[]) val;
//            } else if (val instanceof String) {
//                return new String[]{(String) val};
//            } else {
//                // never happens, just being defensive
//                return new String[]{val.toString()};
//            }
//        }
//
//        /**
//         * @see org.apache.wicket.Request#getPath()
//         */
//        @Override
//        public String getPath() {
//            // Hasn't changed. We only encoded the querystring
//            return request.getPath();
//        }
//
//        /**
//         * @see org.apache.wicket.Request#getRelativePathPrefixToContextRoot()
//         */
//        @Override
//        public String getRelativePathPrefixToContextRoot() {
//            return request.getRelativePathPrefixToContextRoot();
//        }
//
//        /**
//         * @see org.apache.wicket.Request#getRelativePathPrefixToWicketHandler()
//         */
//        @Override
//        public String getRelativePathPrefixToWicketHandler() {
//            return request.getRelativePathPrefixToWicketHandler();
//        }
//
//        /**
//         * @see org.apache.wicket.Request#getURL()
//         */
//        @Override
//        public String getURL() {
//            return url;
//        }
//
//        /**
//         * @see org.apache.wicket.Request#getQueryString()
//         */
//        @Override
//        public String getQueryString() {
//            return request.getQueryString();
//        }
//    }
}
