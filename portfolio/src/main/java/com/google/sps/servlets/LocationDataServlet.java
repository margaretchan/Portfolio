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

import com.google.gson.Gson;
import com.google.sps.data.Location;
import java.io.IOException;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

@WebServlet("/location-data")
public class LocationDataServlet extends HttpServlet{
    
    private static final String CSV_FILE_PATH = "/WEB-INF/location-data-ithaca.csv";

    // This list is thread-safe since it is only written to once in init() when the servlet is created.
    // All subsequent reads can only occour after the init() had completed execution.
    private ImmutableList<Location> locations;

    @Override
    public void init () {
        ImmutableList.Builder builder = new ImmutableList.Builder<>();
        Scanner scanner = new Scanner(getServletContext().getResourceAsStream("/WEB-INF/location-data-ithaca.csv"));
        
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] values = line.split(",");

            double lat = Double.parseDouble(values[0]);
            double lng = Double.parseDouble(values[1]);
            String name = values[2];
            String description = values[3];
            builder.add(new Location(lat, lng, name, description));
        }
        locations = builder.build();
        scanner.close();
    }

    @Override
    public void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        Gson gson = new Gson();

        response.getWriter().println(gson.toJson(locations));
    }
}
