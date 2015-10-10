package io.trivium.test.cases;

import io.trivium.anystore.ObjectRef;
import io.trivium.glue.om.Element;
import io.trivium.glue.om.Json;
import io.trivium.test.Assert;
import io.trivium.test.TestCase;

public class _f52cdee20625452c8f5bd9360d546945 implements TestCase{
    @Override
    public String getTestName() {
        return "tranform swagger json";
    }

    @Override
    public void run() throws Exception {
        String str ="{\"apiVersion\":\"1.0.0\",\"swaggerVersion\":\"1.2\",\"apis\":[{\"path\":\"/user\",\"description\":\"Operations about user\"},{\"path\":\"/pet\",\"description\":\"Operations about pets\"}],\"authorizations\":{\"oauth2\":{\"type\":\"oauth2\",\"scopes\":[\"PUBLIC\"],\"grantTypes\":{\"implicit\":{\"loginEndpoint\":{\"url\":\"http://petstore.swagger.wordnik.com/api/oauth/dialog\"},\"tokenName\":\"access_code\"},\"authorization_code\":{\"tokenRequestEndpoint\":{\"url\":\"http://petstore.swagger.wordnik.com/api/oauth/requestToken\",\"clientIdName\":\"client_id\",\"clientSecretName\":\"client_secret\"},\"tokenEndpoint\":{\"url\":\"http://petstore.swagger.wordnik.com/api/oauth/token\",\"tokenName\":\"access_code\"}}}},\"apiKey\":{\"type\":\"apiKey\",\"keyName\":\"api_key\",\"passAs\":\"header\"},\"basicAuth\":{\"type\":\"basicAuth\"}},\"info\":{\"title\":\"Swagger Sample App\",\"description\":\"This is a sample server Petstore server.  You can find out more about Swagger \\n    at <a href=\\\"http://swagger.wordnik.com\\\">http://swagger.wordnik.com</a> or on irc.freenode.net, #swagger.  For this sample,\\n    you can use the api key \\\"special-key\\\" to test the authorization filters\",\"termsOfServiceUrl\":\"http://helloreverb.com/terms/\",\"contact\":\"apiteam@wordnik.com\",\"license\":\"Apache 2.0\",\"licenseUrl\":\"http://www.apache.org/licenses/LICENSE-2.0.html\"}}";
        Element root = Json.jsonToElement(str);
        String s2 = Json.elementToJson(root);
        Assert.equalsString(str,s2);
    }

    @Override
    public ObjectRef getTypeId() {
        return ObjectRef.getInstance("f52cdee2-0625-452c-8f5b-d9360d546945");
    }
}

