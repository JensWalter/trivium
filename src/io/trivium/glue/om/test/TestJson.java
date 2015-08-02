package io.trivium.glue.om.test;

import io.trivium.NVList;
import io.trivium.NVPair;
import io.trivium.glue.om.Element;
import io.trivium.glue.om.Json;

public class TestJson {

	public static void main(String[] args) {
        jsonWithArrayObject();
		simpleJson();
		jsonWithArray();
        jsonWithArray2();
		jsonWithArrayInArray();
		json2();
//		nvpair2json();
	}

	public static void simpleJson(){
		String str= "{\"a\":{\"b\":\"blah\"},\"z\":\"h\",\"y\":{\"c\":\"blah2\"}}";
		System.out.println("TC1: " + str);
		Element root = Json.jsonToElement(str);
		System.out.println("TC1: " + root.toString());
		String s2 = Json.elementToJson(root);
		System.out.println("TC1: " + s2);
	}
	
	public static void jsonWithArray(){
		String str= "{\"a\":[\"b\",\"blah\",\"c\",\"blah2\",true,123]}";
		Element root = Json.jsonToElement(str);
		System.out.println("TC2: "+str);
		System.out.println("TC2: "+root.toString());
		System.out.println("TC2: "+Json.elementToJson(root));
	}
    public static void jsonWithArrayObject(){
        String str= "{\"a\":[{\"b\":\"blah\"}],\"z\":{\"x\":\"y\"}}";
        Element root = Json.jsonToElement(str);
        System.out.println("TC6: "+str);
        System.out.println("TC6: "+root.toString());
        System.out.println("TC6: "+Json.elementToJson(root));
    }
    public static void jsonWithArray2(){
        String str= "[\"a\",\"b\",\"blah\",true,123]";
        Element root = Json.jsonToElement(str);
        System.out.println("TC5: "+str);
        System.out.println("TC5: "+root.toString());
        System.out.println("TC5: "+Json.elementToJson(root));
    }
	public static void jsonWithArrayInArray(){
		String str= "{\"a\":[[\"b\"]]}";
		Element root = Json.jsonToElement(str);
		System.out.println("TC3: "+str);
		System.out.println("TC3: "+root.toString());
		System.out.println("TC3: "+Json.elementToJson(root));
	}
	public static void json2(){
		String str ="{\"apiVersion\":\"1.0.0\",\"swaggerVersion\":\"1.2\",\"apis\":[{\"path\":\"/user\",\"description\":\"Operations about user\"},{\"path\":\"/pet\",\"description\":\"Operations about pets\"}],\"authorizations\":{\"oauth2\":{\"type\":\"oauth2\",\"scopes\":[\"PUBLIC\"],\"grantTypes\":{\"implicit\":{\"loginEndpoint\":{\"url\":\"http://petstore.swagger.wordnik.com/api/oauth/dialog\"},\"tokenName\":\"access_code\"},\"authorization_code\":{\"tokenRequestEndpoint\":{\"url\":\"http://petstore.swagger.wordnik.com/api/oauth/requestToken\",\"clientIdName\":\"client_id\",\"clientSecretName\":\"client_secret\"},\"tokenEndpoint\":{\"url\":\"http://petstore.swagger.wordnik.com/api/oauth/token\",\"tokenName\":\"access_code\"}}}},\"apiKey\":{\"type\":\"apiKey\",\"keyName\":\"api_key\",\"passAs\":\"header\"},\"basicAuth\":{\"type\":\"basicAuth\"}},\"info\":{\"title\":\"Swagger Sample App\",\"description\":\"This is a sample server Petstore server.  You can find out more about Swagger \\n    at <a href=\\\"http://swagger.wordnik.com\\\">http://swagger.wordnik.com</a> or on irc.freenode.net, #swagger.  For this sample,\\n    you can use the api key \\\"special-key\\\" to test the authorization filters\",\"termsOfServiceUrl\":\"http://helloreverb.com/terms/\",\"contact\":\"apiteam@wordnik.com\",\"license\":\"Apache 2.0\",\"licenseUrl\":\"http://www.apache.org/licenses/LICENSE-2.0.html\"}}";
		Element root = Json.jsonToElement(str);
		System.out.println("TC4: "+str);
		System.out.println("TC4: "+root.toString());
		System.out.println("TC4: "+Json.elementToJson(root));
	}
	
	public static void nvpair2json(){
		NVList list = new NVList();
		list.add(new NVPair("name1", "value1"));
		list.add(new NVPair("name1", "value2"));
		list.add(new NVPair("name2", "value3"));
		list.add(new NVPair("name3", "value4"));
		
		String str = Json.NVPairsToJson(list);
		System.out.println(str);
		System.out.println(Json.NVPairsToJson(Json.JsonToNVPairs(str)));
	}
}
