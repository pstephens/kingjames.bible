try {
    require("source-map-support").install();
} catch(err) {
}
require('./out/dbg/goog/bootstrap/nodejs.js')
require('./out/dbg/debug_refs')
goog.require("biblecli.main.core");
goog.require("biblecli.main.utility");
biblecli.main.utility.set_root_path_BANG_(__dirname);
goog.require("cljs.nodejscli");