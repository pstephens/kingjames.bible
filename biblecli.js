try {
    require("source-map-support").install();
} catch(err) {
}
require('./out/cli/goog/bootstrap/nodejs.js')
require('./out/cli/biblecli.js')
goog.require("biblecli.main.core");
goog.require("cljs.nodejscli");