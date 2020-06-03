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

    // Pick a random fact.
    const FACT = FACTS[Math.floor(Math.random() * FACTS.length)];

    // Add it to the page.
    const factContainer = document.getElementById("fact-container");

    factContainer.innerText = FACT;
}

/** Fetches user comment and comment history to be displayed */
async function getComments() {
    const inputResponse = await fetch("/comments");
    const servletJson = await inputResponse.json();
    const commentContainer = document.getElementById("old-comments");
    servletJson.comments.forEach(line => {
        commentContainer.appendChild(createListElement(line));
    });
}

/** Creates a <li> element containing text. (helper method borrowed from example file) */
function createListElement(text) {
    const liElement = document.createElement('li');
    liElement.innerText = text;
    return liElement;
}

/** combines loading header, footer, and comments for blog page */
function loadBlogPage() {
    loadHeaderFooter();
    getComments();
}

/** Fills all <header> and <footer> tags with the content in header.html and footer.html, respectively */
async function loadHeaderFooter() {
    const headerResponse = await fetch("header.html");
    const headerData = await headerResponse.text();
    document.querySelector("header").innerHTML = headerData;

    const footerResponse = await fetch("footer.html");
    const footerData = await footerResponse.text();
    document.querySelector("footer").innerHTML = footerData;
}
