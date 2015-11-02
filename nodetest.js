try {
    require("source-map-support").install();
} catch(err) {
}
require('./out/test/goog/bootstrap/nodejs.js')
require('./out/test/nodetests.js')
goog.require("test.node.unittests");
goog.require("cljs.nodejscli");