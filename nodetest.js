try {
    require("source-map-support").install();
} catch(err) {
}
require('./out/dbg/goog/bootstrap/nodejs.js')
require('./out/dbg/debug_refs.js')
goog.require("test.node.unittests");
goog.require("cljs.nodejscli");