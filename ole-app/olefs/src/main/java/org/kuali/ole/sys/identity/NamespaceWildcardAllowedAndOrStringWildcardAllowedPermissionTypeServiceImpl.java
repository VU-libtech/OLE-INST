/*
 * Copyright 2007-2009 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.ole.sys.identity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.uif.RemotableAttributeError;
import org.kuali.rice.coreservice.api.namespace.Namespace;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.permission.Permission;
import org.kuali.rice.kim.api.type.KimType;
import org.kuali.rice.krad.kim.NamespacePermissionTypeServiceImpl;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;

/**
 * This is a permission type service with attributes for wildcard matching on the namespace and/or
 * wildcard matching on another attribute (set in the spring config).
 *
 * The permissions returned are the most exact matches found and namespace exact matching takes
 * priority over string attribute exact matching. There is no difference in priority between two attributes
 * which both match partially (i.e. staging/* and staging/sys/*)
 *
 * Priority Namespace   String attribute
 * 1.       Exact       Exact
 * 2.       Exact       Partial
 * 3.       Exact       Blank
 * 4.       Partial     Exact
 * 5.       Partial     Partial
 * 6.       Partial     Blank
 * 7.       Blank       Exact
 * 8.       Blank       Partial
 */
public class NamespaceWildcardAllowedAndOrStringWildcardAllowedPermissionTypeServiceImpl
		extends NamespacePermissionTypeServiceImpl {
	protected static final String NAMESPACE_CODE = KimConstants.AttributeConstants.NAMESPACE_CODE;

	protected String wildcardMatchStringAttributeName;
	protected boolean namespaceRequiredOnStoredAttributeSet;

    /**
     * Check for entries that match the namespace or the string attribute identified in 'wildcardMatchStringAttributeName'.
     * Return the ones which are the most specific.
     *
     * I.e., matches best. KFS-SYS will have priority over KFS-*
     *
     * @see org.kuali.rice.kim.service.support.impl.KimPermissionTypeServiceBase#performPermissionMatches(org.kuali.rice.kim.bo.types.dto.AttributeSet, java.util.List)
     */
	@Override
	protected List<Permission> performPermissionMatches(Map<String, String> requestedDetails, List<Permission> permissionsList) {
		List<Permission> matchingPermissions = new ArrayList<Permission>();

		List<Permission> exactNamespacePermissions = new ArrayList<Permission>();
		List<Permission> partialNamespacePermissions = new ArrayList<Permission>();
        List<Permission> blankNamespacePermissions = new ArrayList<Permission>();

	    String requestedNamespaceAttributeValue = requestedDetails.get(NAMESPACE_CODE);


	    for ( Permission kpi : permissionsList ) {
	        String permissionNamespaceAttributeValue = kpi.getAttributes().get(NAMESPACE_CODE);
	        if ( matchExact(requestedNamespaceAttributeValue, permissionNamespaceAttributeValue) ) {
	            exactNamespacePermissions.add(kpi);
	        }
	        else if ( matchPartial(requestedNamespaceAttributeValue, permissionNamespaceAttributeValue) ) {
	        	partialNamespacePermissions.add(kpi);
	        }
	        else if ( matchBlank(permissionNamespaceAttributeValue) ) {
	            blankNamespacePermissions.add(kpi);
	        }
	    }

	    // if the exact match worked, use those when checking the string attribute
	    if ( !exactNamespacePermissions.isEmpty() ) {
	    	matchingPermissions = performStringAttributeMatching(requestedDetails, getWildcardMatchStringAttributeName(), exactNamespacePermissions);

	    	//found exact namespace match and exact, partial, or blank string attribute match
		    if (!matchingPermissions.isEmpty()) {
		    	return matchingPermissions;
		    }
	    }

	    // if the partial match worked, use those when checking the string attribute
	    if ( !partialNamespacePermissions.isEmpty() ) {
	    	matchingPermissions = performStringAttributeMatching(requestedDetails, getWildcardMatchStringAttributeName(), partialNamespacePermissions);

	    	//found partial namespace match and exact, partial, or blank string attribute match
		    if (!matchingPermissions.isEmpty()) {
		    	return matchingPermissions;
		    }
	    }

	    //don't match on a null namespace when it is required
	    if (!getNamespaceRequiredOnStoredAttributeSet()) {
		    // if the blank match worked, use those when checking the string attribute
		    if ( !blankNamespacePermissions.isEmpty() ) {
		    	matchingPermissions = performStringAttributeMatching(requestedDetails, getWildcardMatchStringAttributeName(), blankNamespacePermissions);

		    	//found blank namespace match and exact, partial, or blank string attribute match
			    if (!matchingPermissions.isEmpty()) {
			    	return matchingPermissions;
			    }
		    }
	    }

	    return matchingPermissions; // empty list
	}

    /**
     * Determines which existing permissions apply in the case which best matches the requested details
     * @param requestedDetails the details to find the best permission matches for
     * @param attributeNameForMatching the attribute name of the wildcard string to match against
     * @param permissionsList the list of permissions for this permission type
     * @return the list of matching permissions
     */
	private List<Permission> performStringAttributeMatching(Map<String, String> requestedDetails, String attributeNameForMatching, List<Permission> permissionsList) {
		String requestedAttributeValue = requestedDetails.get(attributeNameForMatching);

		List<Permission> exactMatchingPermissions = new ArrayList<Permission>();
		List<Permission> partialMatchingPermissions = new ArrayList<Permission>();
		List<Permission> blankMatchingPermissions = new ArrayList<Permission>();

		//check attribute matching
    	for ( Permission kpi : permissionsList ) {
    		String permissionAttributeValue = kpi.getAttributes().get(attributeNameForMatching);

    		if ( matchExact(requestedAttributeValue, permissionAttributeValue) ) {
    			exactMatchingPermissions.add(kpi);
    		}
    		else if ( matchPartial(requestedAttributeValue, permissionAttributeValue) ) {
    			partialMatchingPermissions.add(kpi);
    		}
    		else if ( matchBlank(permissionAttributeValue) ) {
    			blankMatchingPermissions.add(kpi);
    		}
    	}

	    if (!exactMatchingPermissions.isEmpty()) {
	    	return exactMatchingPermissions;
	    }
	    if (!partialMatchingPermissions.isEmpty()) {
	    	return partialMatchingPermissions;
	    }
	    if (!blankMatchingPermissions.isEmpty()) {
	    	return blankMatchingPermissions;
	    }

	    return exactMatchingPermissions; // empty list
	}


	/**
	 * Returns true if the attributes match exactly
	 *
	 * @param requestedAttributeValue
	 * @param permissionAttributeValue
	 * @return
	 */
	protected boolean matchExact(String requestedAttributeValue, String permissionAttributeValue) {
		return StringUtils.equals(requestedAttributeValue, permissionAttributeValue);
	}

	/**
	 * Returns true if the permissionAttribute matches part of the requestedAttribute
	 *
	 * @param requestedAttributeValue
	 * @param permissionAttributeValue
	 * @return
	 */
	protected boolean matchPartial(String requestedAttributeValue, String permissionAttributeValue) {
		if (requestedAttributeValue != null && permissionAttributeValue != null) {
			//replace any '*' with '.*' for regex matching
			permissionAttributeValue = permissionAttributeValue.replaceAll("\\*", ".*");
	        return requestedAttributeValue.matches(permissionAttributeValue);
		}
		return false;
	}

	/**
	 * Returns true if the permissionAttribute is blank
	 *
	 * @param permissionAttributeValue
	 * @return
	 */
	protected boolean matchBlank(String permissionAttributeValue) {
		return StringUtils.isBlank(permissionAttributeValue);
	}

    /**
     * @return the attribute to match wildcards against
     */
    protected String getWildcardMatchStringAttributeName() {
        return this.wildcardMatchStringAttributeName;
    }

    /**
     * @return true if the namespace is required in saved attribute sets using this permission type, false otherwise
     */
	protected boolean getNamespaceRequiredOnStoredAttributeSet() {
		return this.namespaceRequiredOnStoredAttributeSet;
	}

	public void setWildcardMatchStringAttributeName(String wildcardMatchStringAttributeName) {
		this.wildcardMatchStringAttributeName = wildcardMatchStringAttributeName;
	}

	public void setNamespaceRequiredOnStoredAttributeSet(boolean namespaceRequiredOnStoredAttributeSet) {
		this.namespaceRequiredOnStoredAttributeSet = namespaceRequiredOnStoredAttributeSet;
	}

	/**
	 * Overrides the superclass's version of this method in order to account for "namespaceCode" permission detail values containing wildcards.
	 */
	@Override
	protected List<RemotableAttributeError> validateReferencesExistAndActive(KimType kimType, Map<String, String> attributes, List<RemotableAttributeError> previousValidationErrors) {
	    List<RemotableAttributeError> errors = new ArrayList<RemotableAttributeError>();
		Map<String, String> nonNamespaceCodeAttributes = new HashMap<String, String>(attributes);

		// Check if "namespaceCode" is one of the permission detail values.
		if (attributes.containsKey(NAMESPACE_CODE)) {
			nonNamespaceCodeAttributes.remove(NAMESPACE_CODE);
			Collection<Namespace> namespaces = KRADServiceLocatorWeb.getLookupService().findCollectionBySearchUnbounded(
					Namespace.class, Collections.singletonMap("code", attributes.get(NAMESPACE_CODE)));
			// If the lookup service found at least one namespace, perform exists-and-active checks on each one.
			if (namespaces != null && !namespaces.isEmpty()) {
				for (Namespace namespace : namespaces) {
				    Map<String, String> attr = new HashMap<String, String>();
				    attr.put(NAMESPACE_CODE, namespace.getCode());
					errors.addAll(super.validateReferencesExistAndActive(kimType, attr, previousValidationErrors));
				}
			} else {
				// If no namespaces were found, let the superclass generate an appropriate error.
			    Map<String, String> attr = new HashMap<String, String>();
			    attr.put(NAMESPACE_CODE, attributes.get(NAMESPACE_CODE));
				errors.addAll(super.validateReferencesExistAndActive(kimType, attr, previousValidationErrors));
			}
		}

		// Validate all non-namespaceCode attributes.
		errors.addAll(super.validateReferencesExistAndActive(kimType, nonNamespaceCodeAttributes, previousValidationErrors));
		return errors;
	}
}
