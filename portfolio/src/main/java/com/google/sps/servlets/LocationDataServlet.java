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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/location-data")
public class LocationDataServlet extends HttpServlet{
    
    private static final String CSV_FILE_PATH = "/WEB-INF/location-data-ithaca.csv";

    private List<Location> locations;

    @Override
    public void init () {
        locations = new ArrayList<>();

        BufferedReader csvReader = new BufferedReader(new FileReader(CSV_FILE_PATH));
        while (line = csvReader.readLine() != null) {
            String[] values = line.split(",");
            locations.add(new UfoSighting(Double.parseDouble(values[0]), Double.parseDouble(values[1]), values[2], values[3]));
        }
        csvReader.close();
    }

    @Override
    public void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        Gson gson = new Gson();

        // This read should be thread safe since the write only happens once in init() when the servlet is created 
        response.getWriter().println(gson.toJson(locations));
    }
}
