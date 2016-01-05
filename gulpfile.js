var cp = require('child_process');
var fs = require('fs');
var gulp = require('gulp');
var path = require('path');
var q = require('q');
var root_dir = __dirname

var out_bible_dir = path.join(root_dir, 'out/bible');

function log_with_prefix(prefix, buff) {
    if(buff.length > 0) {
        console.log(prefix + ": " + buff.toString())
    }
}

function exec(name, command, cb) {
    cp.exec(command, { cwd: root_dir }, function(err, stdout, stderr) {
        log_with_prefix(name, stdout);
        log_with_prefix(name + " err", stderr);
        cb(err);
    });
}

function mkdir(dir, cb) {
    fs.stat(dir, function(err, stats) {
        if(err || stats) {
            cb(err);
        } else {
            fs.mkdir(dir, function(err) {
                cb(err);
            });
        }
    });
}

gulp.task('compile_dbg', function(cb) {
    exec('lein', 'lein cljsbuild once dbg', cb);
});

gulp.task('bible_resources_dir', function(cb) {
    mkdir(out_bible_dir, cb);
});

gulp.task('bible_resources', gulp.series(
    'bible_resources_dir',
    function prepare(cb) {
        exec('prepare', 'node biblecli.js prepare staggs ' +
            path.join('kjv-src/www.staggs.pair.com-kjbp/kjv.txt') + ' out/bible', cb);
    }));

gulp.task('node_tests', function(cb) {
    exec('node', 'node nodetest.js', cb);
});

gulp.task('default',
   gulp.series('compile_dbg', 'node_tests', 'bible_resources'));