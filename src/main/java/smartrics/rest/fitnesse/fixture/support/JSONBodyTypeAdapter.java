/*  Copyright 2008 Fabrizio Cannizzo
 *
 *  This file is part of RestFixture.
 *
 *  RestFixture (http://code.google.com/p/rest-fixture/) is free software:
 *  you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *
 *  RestFixture is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with RestFixture.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  If you want to contact the author please leave a comment here
 *  http://smartrics.blogspot.com/2008/08/get-fitnesse-with-some-rest.html
 */
package smartrics.rest.fitnesse.fixture.support;

import java.util.List;
import org.skyscreamer.jsonassert.*;

/**
 * Type adapted for cells containing JSON content.
 * 
 * @author smartrics
 * 
 */
public class JSONBodyTypeAdapter extends XPathBodyTypeAdapter {
    private boolean forceJsEvaluation = false;
    private JavascriptWrapper wrapper = new JavascriptWrapper();

    /**
     * def ctor
     */
    public JSONBodyTypeAdapter() {
    }

    @Override
    protected boolean eval(String expr, String json) {
        // for backward compatibility we should keep for now xpath expectations
        if (!forceJsEvaluation && Tools.isValidXPath(getContext(), expr) && !wrapper.looksLikeAJsExpression(expr)) {
            throw new IllegalArgumentException("XPath expectations in JSON content are not supported anymore. Please use JavaScript expressions.");
        }
	if(expr.indexOf("json:")==0){
	
	 try {
	      
	JSONCompareResult result = JSONCompare.compareJSON(expr.replace("json:",""), json, JSONCompareMode.LENIENT);
        if (result.failed()) {
	 addError(result.getMessage());
       		return false;
        }

		//JSONAssert.assertEquals(expr.replace("json:",""), json, false);
      		return true;
    	  } 
	  catch (org.json.JSONException e) {
		addError(e.getMessage());
      		return false;
    	 }

	}
        Object exprResult = wrapper.evaluateExpression(json, expr);
	 

        if (exprResult == null) {
            return false;
        }
        return Boolean.parseBoolean(exprResult.toString());
    }

    @Override
    public Object parse(String possibleJsContent) throws Exception {
        if (possibleJsContent == null || possibleJsContent.trim().indexOf("/* javascript */") < 0) {
            forceJsEvaluation = false;
            return super.parse(possibleJsContent);
        }
        forceJsEvaluation = true;
        String content = Tools.fromHtml(possibleJsContent.trim());
        return content;
    }

    @Override
    public boolean equals(Object expected, Object actual) {
        if (checkNoBody(expected)) {
            return checkNoBody(actual);
        }
        if (checkNoBody(actual)) {
            return checkNoBody(expected);
        }
        if (expected instanceof List<?>) {
            return super.equals(expected, actual);
        }
        boolean result = false;
        if (expected instanceof String) {
            result = eval(expected.toString(), actual.toString());
            if (!result) {
		if( getErrors().size()==0){
                	addError("not equal: '" + expected.toString() + "'");
		}
            }
        }
        return result;
    }

    @Override
    public String toString(Object obj) {
        if (obj == null || obj.toString().trim().equals("")) {
            return "no-body";
        }
        // the actual value is passed as an xml string
        // TODO: pretty print?
        return obj.toString();
    }

    @Override
    public String toXmlString(String content) {
        return Tools.fromJSONtoXML(content);
    }

}
