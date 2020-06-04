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

/** Adds a random personal fact to the page. */
function addRandomFact() {
    const FACTS =
        ['Dogs are (usually) better than cats.', 'Brooklyn 99 is one of the best shows of all time.', 'Milk is underrated.', 'I\'m a ravenclaw.'];
    const FACT = FACTS[Math.floor(Math.random() * FACTS.length)];
    var factContainer = document.getElementById("fact-container");

    factContainer.innerText = FACT;
}

/** Fetches user comment and comment history to be displayed */
async function getComments() {
    var inputResponse = await fetch("/comments");
    var servletJson = await inputResponse.json();
    var commentContainer = document.getElementById("old-comments");
    servletJson.forEach(line => {
        commentContainer.appendChild(createListElement(line));
    });
}

/** Creates a <li> element containing text. (helper method borrowed from example file) */
function createListElement(text) {
    var liElement = document.createElement('li');
    liElement.innerText = text;
    return liElement;
}

/** Fills all <header> and <footer> tags with the content in header.html and footer.html, respectively */
async function loadHeaderFooter() {
    var headerResponse = await fetch("header.html");
    var headerData = await headerResponse.text();
    document.querySelector("header").innerHTML = headerData;

    var footerResponse = await fetch("footer.html");
    var footerData = await footerResponse.text();
    document.querySelector("footer").innerHTML = footerData;
}

loadHeaderFooter();
