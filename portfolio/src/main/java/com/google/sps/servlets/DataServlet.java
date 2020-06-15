// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.gson.Gson;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for listing comments */
@WebServlet("/comments")
public class DataServlet extends HttpServlet {

    private static final int DEFAULT_NUM_COMMENTS = 5;

    /** GET request pulls comments from datastore and prints on /comments page */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery results = datastore.prepare(query);

        // new arraylist every time to keep thread-safe
        List<String> comments = new ArrayList<>();
        List<Entity> resultsList = results.asList(FetchOptions.Builder.withLimit(100)); 
        
        for (int i = Math.min(getMaxComments(request), resultsList.size()) - 1; i >= 0; i--) {
            comments.add((String) resultsList.get(i).getProperty("text"));
        }

        // convert comments to json and print on /comment page
        Gson gson = new Gson();
        response.setContentType("application/json;");
        response.getWriter().println(gson.toJson(comments));
    }

    /** Parses user-requested max number of comments, defaults to DEFAULT_NUM_COMMENTS */
    private int getMaxComments(HttpServletRequest request) {
        int requestedComments = DEFAULT_NUM_COMMENTS;
        try {
            requestedComments = Integer.parseInt(request.getParameter("max-comments"));
        } catch (NumberFormatException e) { 
            System.out.println("Invalid input for number of comments requested: " + e.getMessage());
            // TODO(margaret): display error message to user
        }
        return (requestedComments >= 0) ? requestedComments : DEFAULT_NUM_COMMENTS;
    }

    /** POST request sends new user-inputted comment to datastore */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String newComment = request.getParameter("user-comment");
        long currTime = System.currentTimeMillis();

        Entity commentEntity = new Entity("Comment");
        commentEntity.setProperty("text", newComment);

        // add timestamp for sorting
        commentEntity.setProperty("timestamp", currTime);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(commentEntity);

        response.sendRedirect("/blog.html");
    }
}
