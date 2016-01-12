var page = require('webpage').create();
var url = require('system').args[1];

page.onConsoleMessage = function(message) {
    var exitCode = /^~~EXIT\((\d+)\)~~$/.exec(message);
    if(exitCode) {
        phantom.exit(parseInt(exitCode[1]));
    }
    else {
        console.log(message);
    }
};

page.open(url, function(status) {
    page.evaluate(function() {
        test.browser.core.run();
    });
});