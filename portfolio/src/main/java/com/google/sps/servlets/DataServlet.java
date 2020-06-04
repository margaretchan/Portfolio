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
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for listing comments */
@WebServlet("/comments")
public class DataServlet extends HttpServlet {

    /** GET request pulls comments from datastore and prints on /comments page */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery results = datastore.prepare(query);

        // new arraylist everytime to keep thread safe
        ArrayList<String> comments = new ArrayList<>();
        List<Entity> resultsList = (List) results.asList(FetchOptions.Builder.withLimit(100)); 

        for (int i = getBoundedMaxComments(request, resultsList.size()); i >= 0; i--) {
            Entity entity = resultsList.get(i);
            String comment = (String) entity.getProperty("text");
            comments.add(comment);
        }

        // convert self object to json and print on /comment page
        Gson gson = new Gson();
        response.setContentType("application/json;");
        response.getWriter().println(gson.toJson(comments));
    }

    /** Parses user-requested max number of comments, returned num is valid and positive */
    private int getBoundedMaxComments(HttpServletRequest request, int numComments) {
        int requestedComments = 5;
        String inputMaxComments = request.getParameter("max-comments");
        try {
            requestedComments = Integer.parseInt(inputMaxComments);
        } catch (Exception e) { /* ignore */ }

        if (requestedComments >= 0 && requestedComments < numComments) {
            return requestedComments;
        }
        return numComments;
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
