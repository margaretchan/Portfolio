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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/location-data")
public class LocationDataServlet extends HttpServlet{
    
    private static final String CSV_FILE_PATH = "/WEB-INF/location-data-ithaca.csv";

    // This list is thread-safe since it is only written to once in init() when the servlet is created.
    // All subsequent reads can only occour after the init() had completed execution.
    private List<Location> locations;

    @Override
    public void init () {
        locations = new ArrayList<>();
        Scanner scanner = new Scanner(getServletContext().getResourceAsStream("/WEB-INF/location-data-ithaca.csv"));
        
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] values = line.split(",");
            locations.add(new Location(Double.parseDouble(values[0]), Double.parseDouble(values[1]), values[2], values[3]));
        }
        scanner.close();
    }

    @Override
    public void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        Gson gson = new Gson();

        response.getWriter().println(gson.toJson(locations));
    }
}
