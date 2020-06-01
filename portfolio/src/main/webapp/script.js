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

/**
 * Adds a random personal fact to the page.
 */
function addRandomFact() {
    const FACTS =
        ['Dogs are (usually) better than cats.', 'Brooklyn 99 is one of the best shows of all time.', 'Milk is underrated.', 'I\'m a ravenclaw.'];

    // Pick a random fact.
    const FACT = FACTS[Math.floor(Math.random() * FACTS.length)];

    // Add it to the page.
    const FACT_CONTAINER = document.getElementById("fact-container");

    FACT_CONTAINER.innerText = FACT;
}

function getRandomGreeting() {
    fetch("/data")
        .then(greetingProm => {
            return greetingProm.text();
        })
        .then(text => {
            document.querySelector("#greeting-container").innerHTML = text;
        })
}

/**
 * Fills all <header> and <footer> tags with the content in header.html and footer.html, respectively
 */
fetch("header.html")
    .then(response => {
        return response.text();
    })
    .then(data => {
        document.querySelector("header").innerHTML = data;
    });

fetch("footer.html")
    .then(response => {
        return response.text();
    })
    .then(data => {
        document.querySelector("footer").innerHTML = data;
    });
