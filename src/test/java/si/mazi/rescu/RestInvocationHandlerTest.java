/**
 * Copyright (C) 2013 Matija Mazi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package si.mazi.rescu;

import org.testng.annotations.Test;
import si.mazi.rescu.dto.DummyAccountInfo;
import si.mazi.rescu.dto.Order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;


/**
 * @author Matija Mazi
 */
public class RestInvocationHandlerTest {

    @Test
    public void testInvoke() throws Exception {

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        proxy.buy("john", "secret", new BigDecimal("3.14"), new BigDecimal("10.00"));
        assertRequestData(testHandler, "https://example.com/api/2/buy/", "buy/", HttpMethod.POST, Order.class, "user=john&password=secret&amount=3.14&price=10.00", null);

        proxy.buy("john", "secret", new BigDecimal("3.14"), null);
        assertRequestData(testHandler, "https://example.com/api/2/buy/", "buy/", HttpMethod.POST, Order.class, "user=john&password=secret&amount=3.14", null);

        proxy.withdrawBitcoin("john", "secret", new BigDecimal("3.14"), "mybitcoinaddress");
        assertRequestData(testHandler, "https://example.com/api/2/bitcoin_withdrawal/john?amount=3.14&address=mybitcoinaddress", "bitcoin_withdrawal/john", HttpMethod.POST, Object.class, "password=secret", null);

        proxy.getTicker("btc", "usd");
        assertRequestData(testHandler, "https://example.com/api/2/btc_usd/ticker", "btc_usd/ticker", HttpMethod.GET, Object.class, "", null);

        proxy.getInfo(1000L, 2000L);
        assertRequestData(testHandler, "https://example.com/api/2", "", HttpMethod.POST, Object.class, "method=getInfo", null);
    }

    @Test
    public void testHttpBasicAuth() throws Exception {

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        proxy.testBasicAuth(new BasicAuthCredentials("Aladdin", "open sesame"), 23);
        HashMap<String, String> authHeaders = new HashMap<String, String>();
        authHeaders.put("Authorization", "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");
        assertRequestData(testHandler, "https://example.com/api/2/auth?param=23", "auth", HttpMethod.GET, Object.class, "", authHeaders);
    }

    private void assertRequestData(TestRestInvocationHandler testHandler, String url, String methodPath, HttpMethod httpMethod, Class resultClass, String postBody, Map<String, String> headers) {

        assertEquals(url, testHandler.params.getInvocationUrl());
        assertEquals(methodPath, testHandler.params.getMethodPath());
        assertEquals(httpMethod, testHandler.restMethodMetadata.httpMethod);
        assertEquals(resultClass, testHandler.restMethodMetadata.returnType);
        assertEquals(postBody, testHandler.params.getRequestBody());
        if (headers != null) {
            assertEquals(headers, testHandler.params.getHttpHeaders());
        }
    }

    @Test
    public void testJsonBody() throws Exception {

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(ExampleService.class);
        ExampleService proxy = RestProxyFactory.createProxy(ExampleService.class, testHandler);

        proxy.testJsonBody(new DummyAccountInfo("mm", "USD", 3));
        assertEquals("{\"username\":\"mm\",\"currency\":\"USD\",\"amount_int\":3}", testHandler.params.getRequestBody());

    }

    @Test
    public void testRootPathService() throws Exception {

        TestRestInvocationHandler testHandler = new TestRestInvocationHandler(RootPathService.class);
        RootPathService proxy = RestProxyFactory.createProxy(RootPathService.class, testHandler);

        proxy.cancel("424");
        assertRequestData(testHandler, "https://example.com/cancel?id=424", "cancel", HttpMethod.DELETE, Double.class, "", null);
    }

    private static class TestRestInvocationHandler extends RestInvocationHandler {

        private RestMethodMetadata restMethodMetadata;
        private RestInvocationParams params;

        public TestRestInvocationHandler(Class<?> restInterface) {
            super(restInterface, "https://example.com");
        }

        @Override
        protected Object invokeHttp(RestMethodMetadata restMethodMetadata, RestInvocationParams params) {
            this.restMethodMetadata = restMethodMetadata;
            this.params = params;
            return null;
        }
    }
}
