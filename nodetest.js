try {
    require("source-map-support").install();
} catch(err) {
}
require('./out/goog/bootstrap/nodejs.js')
require('./out/nodetests.js')
goog.require("test.node.unittests");
goog.require("cljs.nodejscli");