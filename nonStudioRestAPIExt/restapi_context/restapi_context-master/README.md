This REST API can access in one call all your process variables, from a taskId or from a caseId.
Use http://../API/extension/context?caseId={{caseId}} or http://../API/extension/context?taskId={{taskId}}

See the tutorial for a step by step information
-------------------------------- People who can read this document --------------------------------

All person using the UI Designer to create form.

-------------------------------- Difference: --------------------------------

The difference with the standard REST API to access variables and BDM are: 
 1/ Perimeter: using http://../API/bpm/activityVariable/[activity_id]/[variable_name] or http://../API/bpm/caseVariable/[caseId]/[variableName], you need to know where is the variable (case Variable?, activity?)
 2/ theses REST API can't handle HashMap, List or Datatype, only basic type (String,Long,Date)
 3/ one variable at a call
 4/ no way to control security: using a RESTAPI, you can access all variables included “CommentOfTheBoss”
 5/ one call per BDM variable
 6/ if the BDM has a children "load when needed" (lazy), the standard RestAPI does not include it, and you have to deal with Javascript / RestAPI to get the first level of child, and there are no simple way to get a grand child
 7/ no way to protect the header.CommentOfTheManagerEmployeeMustNotAccess in anyway
 8/ case is archive? REST API to call are different ( ! ) . So, building an overview to access 10 variables? You have to call 2 * 10 REST API, to get the result, and use Javascript to manage the display (value may come from API/bpm/caseVariable or API/bpm/archiveVariable)

-------------------------------- Installation guide --------------------------------

Run the BonitaPortal, and connect as an Administrator. 
1. Then, the profile Administrator should be accessible.
2. In the profile Administrator, select "Resources".
3. Click on the button ADD, and give the file “ContextAccess-x.y.zip"
4. that's it : the RestContextExtention is installed and deployed

Nota: to install the Process Demonstration and the Source environment, see the PDF document in the release.

-------------------------------- Data managed --------------------------------
These type of variable are managed
* Simple variable : STRING, 
Example on the returned JSON: 
  "message": "Hello the word"

* Numerical value  INTEGER, FLOAT 
Example on the returned JSON: 
    "numberOfLines": 54
	
* Boolean value 

Example on the returned JSON: 
	"allowNegativeAmount" : true

* Date
A date is returned in the JSON format to be used on the browser
	
Example on the returned JSON: 
    "dateOfRegistration": "2016-05-23T14:24:16.683Z"


* Map of object 
The Java class Map is managed, and return a JSON set of data
 "address": {
        "country": "USA",
		"city" : "San Francisco",
		"zipcode" : 94702
		"validfrom" : "2016-05-23T14:24:16.683Z"
		}
		
* List of object (list of String, list of Integer...)	
The list is a JSON list, each content can be whatever is manage as a value (String, Integer, Date, Bdm...)

Example on the returned JSON: 
    "listOfMessage": [
        "Hello",
        "the",
        "world"
    ],
	
* Java Enumerate.
If the data is an Java Enumerate (nota : the ListOfOption in Bonita is not an Enumerate, it's a String in fact)
The value is return as a String and a new value, same name but finish by "_list" return the list of possible value.
	"aPetsAnimalEnum" : "TURTLE",
	"aPetsAnimalEnum_list" : [
		"CAT",
		"DOG",
		"CROCODILE",
		"TURTLE",
		"FROGGY"
	]
	
* Data Type
Any data type generated by the studio, or Serializable, can be handle.
The return is a map of information, where all attributes are recursively exploited

Example on the returned JSON: 

    "invoice": {
        "lines": [
            {
                "product": "BPAD",
                "lineNumber": "100"
            },
            {
                "product": "AI",
                "lineNumber": "200"
            }
        ],
        "details": {
            "address": "44 Tahoha St",
            "country": "USA",
            "city": "San Francisco"
        },
        "headerNumber": 0,
        "name": "TheDatatype Name"
    }
	
* Business Data Model
The business Data Model (Multiple or not) are displayed, and are recursively exploited (list of line of an invoice for example). if you BDM has some aggregation (the Invoice reference a Customer), then the information on the customer can be returned : you have to use the Pilot for that.

Example on the returned JSON: 
 "summerOrder": {
        "aFloat": 6.334,
        "multInteger": [
            553,
            54,
            null,
            56
        ],
        "aBoolean": false,
        "multDate": [
            "2016-08-17T19:25:32+0000",
            "2016-08-17T19:25:32+0000",
            null,
            "2016-08-17T19:25:32+0000"
        ],
        "aInteger": 444,
        "numheader": "1471461932737",
        "aLong": 45334545,
		"persistenceId": 24,
        "ticket": {
            "persistenceId": 57,
            "solicitante": "Name of the Solicitante",
            "idTicket": 12
        }
      };
-------------------------------- first step --------------------------------

 When you use some processes variables (process variable, or local variable), use this REST API to access all variables in one call. All variables are delivered on a JSON format (even HashMap , List, and Datatype) and you don't need to care if the variable is a process variable or a local variable.
So, you can use
 ../API/extension/context?caseId={{caseId}}
 or
 ../API/extension/context?taskId={{taskId}}

-------------------------------- How to pilot the result? --------------------------------

To pilot which variable has to be retrieved and return, you can define a local variable "context", or a process variable "globalcontext" which contains some JSON information.
 For example, set as localvariable "context" the following information:
 { "isMyInvoiceIsReady":"data",
 "dateInvoice":"data",
 "personWhoOrderMap":"data",
 "invoiceNumber":"data",
 "listOfProducts":"data",
 "completeValueAsLong":"data",
 "shortDescString":"data",
 "thisIsALocalVariable":"data",
 "completeAddressDataType":"datatype" }

then you get as a result :
 { "isMyInvoiceIsReady ":true,
"dateInvoice":”2016-04-27T22:39:32+0000",
" personWhoOrderMap ":{"age":34,"lastname":"Bates","firstname":"Walter"},
"invoiceNumber": 3342,
 "listOfProducts": [ “AI Training”, “BPAD Training”, “Advanced Training”],
 "completeValueAsLong": 9921,
 "shortDescString":"Hello the word",
 "thisIsALocalVariable":" yes, Local variable too ",
 "completeAddressDataType": { "city”: “San Francisco”, “country”:”USA”, "lines":
 [ {"product":"BPAD","lineNumber":"100"},
 {"product":"AI","lineNumber":"200"}
 ] },
 
 
 -------------------------------- access the BDM--------------------------------
 
 When you have a BDM variable like "summerOrder", ask in the pilot:
 "summerOrder" : {
 "name": "data",
 "ticket": "",
 "lines" : { "linename" : "data",
 "ticket" : { "solicitante" : "data" },
 "price":"data"
 }
 }
Saying that, you will get the BDM variable, and the children "ticket" and all fields in “ticket” (if ticket is a MULTIPLE, then you get a LIST of RECORD). Then you ask to get lines, and in lines, subchild "linename" and "price".
 Then, if the BDM has more attributes (like in the lines an attribute "for_the_manager_only", then this information is not part of the result (except if you ask "*")

-------------------------------- all variables --------------------------------

 Get it simple, get all ! And in fact, if you don't specify a pilot, that what you have.
 if you want all variables (process, activity, BDM) :
 { "*":"all" }
 Nota : if you ask with a parameter "taskId" you get all the LOCAL variable, the PROCESS variables and the BDM variables. With a parameter caseId, you get only the process variable and the BDM variables.

-------------------------------- case is archived ? --------------------------------

 If the case 9002 is now archived, just continue to ask the case 9002 : the REST API get all the information even if the case is archived. Enjoy a simple way to build the Overview page!

-------------------------------- log --------------------------------

 in the URL, ask "&log=true" and then you get additional result in the JSON : source of data, performance to fetch the result. And on the server too, you will have more information in the log

--------------------------------------- Security --------------------------------

All the REST API activityVariable/ and caseVariable are based on the permission access task_visualization. So, if your process contains a sensitive information like "managerComment", at any moment, the employee can access this information via one of this API.
 To avoid that:
 * change the permission access of this two REST API to "onlyadministratorway"
 * in each activity, define a local variable "context" where you define what the user can see.
 Doing that, the REST API will deliver only what it's define in the local variable. And because the variable is on the server, user has no way to change it.

 -------------------------------- Tutorial --------------------------------
 
 To demonstrate the usage, install the RestApiContext (see the Installation part).
 * load the BOS file "DemontrasteRestApiContext", 
 * access the Business Data Model (Development / Business Data Model / Manage) and click on Finish to deploy the data model
 
Use ContextCall:
  Select the Context Call process, and click on Run to deploy it
  Click on Start to create a case.
  On the portal, you should see three different tasks: ContextUse, allContext,GlobalContext. Click on the task ContextUse and look the URL:
  http://localhost:8080/bonita/portal/homepage#?_p=tasklistinguser&_pf=1&_f=available&_id=100003
the taskId is under "id" : is this URL, taskId is 100003. To get the caseId, on the portal, the id is visible after the "Case: xxxx" (example : 5004)
  
  Run a Firefox Browser. Connect as Walter.Bates.
  
  BY A REST CLIENT
  On Firefox, run the Rest Client Extension (a Rest Client module on Firefox). The browser shares the cookie, so now your rest client is connected
   
   * * *  Set the REST URL :
	Method : POST
	URL : http://localhost:8080/bonita/API/extension/context?caseId=5004
   Click on SEND, result is 
	Status : 200
	Response Body:
	{
    "aBoolean": true,
    "globalcontext": "{ \"*\":\"all\" }",
    "aHashMap": {
        "age": 34,
        "lastname": "Bates",
        "firstname": "Walter"
    },
    "aString": "Hello the word",
    "aInteger": 54,
    "aList": [
        "Hello",
        "the",
        "world"
    ],
    "aDataType": {
        "lines": [
            {
                "product": "BPAD",
                "lineNumber": "100"
            },
            {
                "product": "AI",
                "lineNumber": "200"
            }
        ],
        "details": {
            "address": "44 Tahoha St",
            "country": "USA",
            "city": "San Francisco"
        },
        "headerNumber": 0,
        "name": "TheDatatype Name"
    },
    "aLong": 12,
    "aDate": "2016-05-23T14:24:16.683Z"
}
* * *  Set the REST URL TASK (the answer will contain the 'aLocalVariable') :
	 Method : POST
	URL : http://localhost:8080/bonita/API/extension/context?taskId=100003
   Click on SEND, result is 
	Status : 200
	Response Body:
{
    "aBoolean": true,
    "aLocalVariable": 43,
    "globalcontext": "{ \"*\":\"all\" }",
    "aHashMap": {
        "age": 34,
        "lastname": "Bates",
        "firstname": "Walter"
    },
    "aString": "Hello the word",
    "aInteger": 54,
    "aList": [
        "Hello",
        "the",
        "world"
    ],
	"anPetsAnimalEnum" : "TURTLE",
	"anPetsAnimalEnum_list" : [
		"CAT",
		"DOG",
		"CROCODILE",
		"TURTLE",
		"FROGGY"
	],	
    "aDataType": {
        "lines": [
            {
                "product": "BPAD",
                "lineNumber": "100"
            },
            {
                "product": "AI",
                "lineNumber": "200"
            }
        ],
        "details": {
            "address": "44 Tahoha St",
            "country": "USA",
            "city": "San Francisco"
        },
        "headerNumber": 0,
        "name": "TheDatatype Name"
    },
    "aLong": 12,
    "aDate": "2016-05-23T14:24:16.683Z"
}

   BY THE UIDESIGNER
	click on the different task, and then the REST API is called and get the information. Access the task definition with the UIDesigner to see how to use it.
  
----------- List of update

 2.0 Access :
			•BDM variables
			•when the case is archived, access data
			•thanks to Kilian, modify the Date format output

 2.1 Remove the BDM dependency in POM.XML

 2.2 This version works in a BonitaCommunity / Fix the boolean field in a BDM

 2.3 An Java numerate is handle and the list of value is returned When a BDM is null, Rest Context handle it and return the value with a null.
		Only limit : if you have a List of BDM and in the list, a null :
		List[ 0 ] = ClientDAO.newInstance()
		List[ 1 ] = null
		List[ 2 ] = ClientDAO.newInstance()
		List[ 3 ] = ClientDAO.newInstance()
		In this situation, the engine return an exception (when RestAPIContext ask the list of BDM) and this situation is not handle by the RestAPI Context

 2.4 manage dates as timestamp to manage the TimeZone for the DatePicker
		Correct a bug on "Templist"

 2.5 New parameters the dateformat parameters : 
 		dateformat=DATEJSON to return a date in JSON (to be usefull with the Widget DateTime),
 		dateformat=DATETIME return a JSON date + time
      dateformat=DATELONG return a the date as a long (TimeStamp) to be compatible with the widget DatePicker[UIDesigner]. This is the default value  
 return document
 add context in the result (context group caseid, taskid, userid, username, isAdministrator, processdefinitionid,isProcessInstanciation)


 2.6 Accept url parameter
      return context.isTask / context.isOverview based on the URL parameter value
      in case of instantiation, return the document variable empty (if context ask for anyvalue like '*').
      return parameters (+ value) in the result
      
      
 return context.isTask / context.isOverview based on the URL parameter value
 in case of instantiation, return the document variable empty (if context ask for anyvalue like '*').
 return parameters (+ value) in the result
--------------------------------- Url Parameter
To set the URL in the RestAPi Context, do the following:
- Create a Javascript variable "getUrl"
- give the Javascript : 
        var urlPath = window.location.pathname;
        var urlPathEncode = encodeURI(urlPath);
        return urlPathEncode;

- in the RESTAPI CONTEXT, give this information:
   ../API/extension/context?taskId={{taskId}}&processId={{taskId}}&caseId={{taskId}}&url={{getUrl}}

 (assuming taskId is a variable "URL parameter" / value= "id"
 Nota : BonitaPortal give in the URL parameter ID different ID : this is the TASK ID of the task on the task execution, the caseId in the overviewcase, and the processId in the process instantiation
 The result is then
 "context": {
        "isTaskExecution": false,
        "isProcessInstanciation": true,
        "isProcessOverview": false,
        "isAdministrator": true,
 
 ------------------------------ Document
 In the instantiation form, when a document exist, return it empty. This information is needed for the FileUploadWidgetPlus
 
 ------------------------------ Parameters
 If requested (context required all like '*') then the process parameters are returned with the value.
 Else, return can be done only when requested

 
    DateTime is now new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")


2.7  TODO
	globalContext : comment y acceder en process instanctiation ? 
				Parametre dans globalcontext : ne marche pas
				