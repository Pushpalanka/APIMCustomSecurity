/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.example.token.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Base64;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/token")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TokenService {

    private static final Log log = LogFactory.getLog(TokenService.class);

    @GET
    public Response hello(@QueryParam("clientId") String clientId, @QueryParam("clientSecret") String clientSecret) {
        
        String originalInput = clientId + "--" + clientSecret;
        String token = Base64.getEncoder().withoutPadding().encodeToString(originalInput.getBytes());
        return Response.status(Response.Status.OK)
                .entity("{\"token\" : \"" + token + "\"}").build();
    }
}
