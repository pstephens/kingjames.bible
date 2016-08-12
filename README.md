# The King James Bible
Source for https://kingjames.bible, one of many copies of the King James Bible on the web.

This is a labor of love with the end goal being a clean, high quality implementation without distracting fluff.

## Roadmap
Here is the current thinking and relative order of planned features:
* Single Page App (SPA) but retaining SEO features
* Full text searching
* Built-in dictionary
* Copy + Paste functionality to aid in copying bible content to blogs and web sites with configurable styling
* Embeddable "verse of the day"
* Embeddable Bible search
* A flash card style memorization site

## Contact
Please add feature suggestions to the issue tracker and I'll see what I can do.

## Tools
### ![BrowserStack](doc/browserstacklogo.png)
Cross browser testing is provided by [BrowserStack](https://www.browserstack.com/).

## Building and Deploying

To build this software you will need several tools pre-installed:

* Leiningen - Tested with 2.6.1
* Node.js - 6.3.1
* NPM - 3.10.3
* gulp-cli - 0.4.0 - install with ```npm install gulp-cli -g```

The build steps are:

1. Resolve all Clojure, ClojureScript, and Node.js dependencies with ```lein deps```.
2. Compile the ClojureScript code with ```lein cljsbuild once```.
3. Build the static web page resources with ```gulp build --config prod```. This will place the resulting files in ```./out/prod```.
4. Publish the static web page resources to an S3 bucket with ```gulp bucketsync --config prod```.