var cp = require('child_process');
var fs = require('fs');
var gulp = require('gulp');
var mkdirp = require('mkdirp');
var path = require('path');
var q = require('q');
var root_dir = __dirname

var node_exec = process.execPath;
var out_bible_dir = path.join(root_dir, 'out/bible');

function spawn(name, command, args, cb) {
    args = args || [];
    var proc = cp.spawn(command, args, { cwd: root_dir });
    proc.stdout.on('data', function(buff) { console.log(buff.toString()) });
    proc.stderr.on('data', function(buff) { console.error(buff.toString()) });
    proc.on('close', function(code) {
        if(code) {
            cb(new Error(name + ': failed with exit code ' + code));
        }
        else {
            cb(null);
        }
    });
    proc.on('error', function(err) {
        cb(err);
    });
}

function exec(name, command, args, cb) {
    args = args || [];
    if(process.platform === 'win32')
    {
        spawn(name, 'cmd.exe', [ '/c', command ].concat(args), cb);
    }
    else {
        spawn(name, command, args, cb);
    }
}

gulp.task('compile_dbg', function(cb) {
    exec('lein', 'lein', ['cljsbuild', 'once', 'dbg'], cb);
});

gulp.task('bible_resources_dir', function(cb) {
    mkdirp(out_bible_dir, cb);
});

gulp.task('bible_resources', gulp.series(
    'bible_resources_dir',
    function prepare(cb) {
        spawn('prepare', node_exec, ['biblecli.js', 'prepare', 'staggs',
            path.join('kjv-src/www.staggs.pair.com-kjbp/kjv.txt'), 'out/bible' ], cb);
    }));

gulp.task('node_tests', function(cb) {
    spawn('node', node_exec, ['nodetest.js'], cb);
});

gulp.task('default',
   gulp.series('compile_dbg', 'node_tests', 'bible_resources'));