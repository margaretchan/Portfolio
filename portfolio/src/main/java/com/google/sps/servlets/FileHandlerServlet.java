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

package com.google.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * When the user submits the form, Blobstore processes the file upload and then forwards the request
 * to this servlet. This servlet can then process the request using the file URL we get from
 * Blobstore.
 */
@WebServlet("/file-handler")
public class FileHandlerServlet extends HttpServlet {

    private static final String IMAGE_DATASTORE_KEY = "Image";
    private static final String ACCEPTABLE_CONTENT_TYPE = "image";

    /**
     * Attempts to put image uploaded to blobstore into datastore. Removes blob from blobstore 
     * if blob is empty or is not an image file type.
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity imageEntity = new Entity(IMAGE_DATASTORE_KEY);

        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
        List<BlobKey> blobKeys = blobs.get(IMAGE_DATASTORE_KEY);

        // Following pattern in docs https://cloud.google.com/appengine/docs/standard/java/blobstore#3_implement_upload_handler
        if (blobKeys != null && !blobKeys.isEmpty()) {
            BlobKey blobKey = blobKeys.get(0);
            BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);

            if (blobInfo.getSize() == 0 || !blobInfo.getContentType().startsWith(ACCEPTABLE_CONTENT_TYPE)) {
                // Delete blob immediately to prevent it from being orphaned
                blobstoreService.delete(blobKey);
            } else {
                String imageUrl = getUrlfromKey(blobKey).getPath();
                imageEntity.setProperty("caption", request.getParameter("caption"));
                imageEntity.setProperty("url", imageUrl);
                datastore.put(imageEntity);
            }
        }

        response.sendRedirect("/blog.html");
    }

    /**
     * Gets all blobstore image urls from datastore and prints json containing urls to response
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Query query = new Query(IMAGE_DATASTORE_KEY);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery results = datastore.prepare(query);

        List<String> imageUrls = new ArrayList<>();
        for (Entity entity : results.asIterable()) {
            String url = (String) entity.getProperty("url");
            imageUrls.add(url);
        }

        Gson gson = new Gson();
        response.setContentType("application/json;");
        response.getWriter().println(gson.toJson(imageUrls));
    }

    /** 
     * Returns a URL that points to the uploaded file key
     * precondition: the blobKey exists (is non-null)
     */
    private URL getUrlfromKey(BlobKey blobKey) throws MalformedURLException {
        ImagesService imagesService = ImagesServiceFactory.getImagesService();
        ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);

        return new URL(imagesService.getServingUrl(options));
    }
}
