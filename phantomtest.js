var page = require('webpage').create();
var url = require('system').args[1];

page.onConsoleMessage = function(message) {
    console.log(message);
};

page.open(url, function(status) {
    page.evaluate(function() {
        test.browser.core.run();
    });
});