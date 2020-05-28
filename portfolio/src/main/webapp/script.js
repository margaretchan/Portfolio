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
        ['Dogs are (usually) better than cats', 'Brooklyn 99 is a great TV show', 'Milk is underrated', 'I\'m a ravenclaw'];

    // Pick a random fact.
    const FACT = FACTS[Math.floor(Math.random() * FACTS.length)];

    // Add it to the page.
    const FACT_CONTAINER = document.getElementById("fact-container");

    FACT_CONTAINER.innerText = FACT;
}
