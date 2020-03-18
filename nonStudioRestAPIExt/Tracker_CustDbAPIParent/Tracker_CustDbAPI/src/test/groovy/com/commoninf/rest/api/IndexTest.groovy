package com.commoninf.rest.api;

import groovy.json.JsonSlurper

import javax.servlet.http.HttpServletRequest

import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder

import spock.lang.Specification

import com.bonitasoft.web.extension.rest.RestAPIContext

/**
 * @see http://spockframework.github.io/spock/docs/1.0/index.html
 */
class IndexTest extends Specification {

    // Declare mocks here
    // Mocks are used to simulate external dependencies behavior
    def httpRequest = Mock(HttpServletRequest)
    def resourceProvider = Mock(ResourceProvider)
    def context = Mock(RestAPIContext)

    /**
     * You can configure mocks before each tests in the setup method
     */
    def setup(){
        // Simulate access to configuration.properties resource
        context.resourceProvider >> resourceProvider
        resourceProvider.getResourceAsStream("configuration.properties") >> IndexTest.class.classLoader.getResourceAsStream("testConfiguration.properties")
    }

    def should_return_a_json_representation_as_result() {
        given: "a RestAPIController"
        def index = new Tracker_CustGet()
        // Simulate a request with a value for each parameter
        httpRequest.getParameter("p") >> "0"
        httpRequest.getParameter("c") >> "test"
        httpRequest.getParameter("db_name") >> "aValue5"
		httpRequest.getParameter("req") >> "aValue3"

        when: "Invoking the REST API"
        def apiResponse = index.doHandle(httpRequest, new RestApiResponseBuilder(), context)

        then: "A JSON representation is returned in response body"
        def jsonResponse = new JsonSlurper().parseText(apiResponse.response)
        // Validate returned response
        apiResponse.httpStatus == 200
        jsonResponse.p == "0"
        jsonResponse.c == "test"
        jsonResponse.db_name == "aValue5"
		jsonResponse.req == "aValue3"
    }

    def should_return_an_error_response_if_p_is_not_set() {
        given: "a request without p"
        def index = new Tracker_CustGet()
        httpRequest.getParameter("p") >> null
        // Other parameters return a valid value
        httpRequest.getParameter("c") >> "aValue2"
        httpRequest.getParameter("db_name") >> "aValue5"
		httpRequest.getParameter("req") >> "aValue3"

        when: "Invoking the REST API"
        def apiResponse = index.doHandle(httpRequest, new RestApiResponseBuilder(), context)

        then: "A JSON response is returned with a HTTP Bad Request Status (400) and an error message in body"
        def jsonResponse = new JsonSlurper().parseText(apiResponse.response)
        // Validate returned response
        apiResponse.httpStatus == 400
        jsonResponse.error == "the parameter p is missing"
    }

    def should_return_an_error_response_if_c_is_not_set() {
        given: "a request without c"
        def index = new Tracker_CustGet()
        httpRequest.getParameter("c") >> null
        // Other parameters return a valid value
        httpRequest.getParameter("p") >> "aValue1"
        httpRequest.getParameter("db_name") >> "aValue5"
		httpRequest.getParameter("req") >> "aValue3"

        when: "Invoking the REST API"
        def apiResponse = index.doHandle(httpRequest, new RestApiResponseBuilder(), context)

        then: "A JSON response is returned with a HTTP Bad Request Status (400) and an error message in body"
        def jsonResponse = new JsonSlurper().parseText(apiResponse.response)
        // Validate returned response
        apiResponse.httpStatus == 400
        jsonResponse.error == "the parameter c is missing"
    }

    def should_return_an_error_response_if_db_name_is_not_set() {
        given: "a request without db_name"
        def index = new Tracker_CustGet()
        httpRequest.getParameter("db_name") >> null
        // Other parameters return a valid value
        httpRequest.getParameter("p") >> "aValue1"
        httpRequest.getParameter("c") >> "aValue2"
		httpRequest.getParameter("req") >> "aValue3"

        when: "Invoking the REST API"
        def apiResponse = index.doHandle(httpRequest, new RestApiResponseBuilder(), context)

        then: "A JSON response is returned with a HTTP Bad Request Status (400) and an error message in body"
        def jsonResponse = new JsonSlurper().parseText(apiResponse.response)
        // Validate returned response
        apiResponse.httpStatus == 400
        jsonResponse.error == "the parameter db_name is missing"
    }
	
	def should_return_an_error_response_if_req_is_not_set() {
		given: "a request without req"
		def index = new Tracker_CustGet()
		httpRequest.getParameter("req") >> null
		// Other parameters return a valid value
		httpRequest.getParameter("p") >> "aValue1"
		httpRequest.getParameter("c") >> "aValue2"
		httpRequest.getParameter("db_name") >> "aValue5"

		when: "Invoking the REST API"
		def apiResponse = index.doHandle(httpRequest, new RestApiResponseBuilder(), context)

		then: "A JSON response is returned with a HTTP Bad Request Status (400) and an error message in body"
		def jsonResponse = new JsonSlurper().parseText(apiResponse.response)
		// Validate returned response
		apiResponse.httpStatus == 400
		jsonResponse.error == "the parameter req is missing"
	}
}