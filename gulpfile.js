var _ =      require('lodash');
var cp =     require('child_process');
var fs =     require('fs');
var gulp =   require('gulp');
var mkdirp = require('mkdirp');
var os =     require('os');
var path =   require('path');
var q =      require('q');

var root_dir = __dirname
var node_exec = process.execPath;
var out_bible_dir = path.join(root_dir, 'out/bible');

function output_to(name, stream) {
    return function output_to(buff) {
        _(buff.toString().split(/[\r\n]/))
            .drop(function(line) { return line.match(/^\s*$/); })
            .forEach(function(line) {
                stream.write(name + ': ' + line + os.EOL);
            });
    }
}

function spawn(name, command, args, cb) {
    args = args || [];
    var proc = cp.spawn(command, args, { cwd: root_dir });
    proc.stdout.on('data', output_to(name, process.stdout));
    proc.stderr.on('data', output_to(name, process.stderr));
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
    // Seems like shell execution on windows while streaming to stdout and stderr is not yet supported.
    // https://github.com/nodejs/node/issues/1009
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
    exec('lein', 'lein', ['cljsbuild', 'auto', 'dbg'], cb);
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

gulp.task('run_tests',
    gulp.series('node_tests', 'bible_resources'));

gulp.task('watch_tests', function() {
    gulp.watch("out/dbg/last-compiled.txt",
        gulp.series('run_tests'));
});

gulp.task('default',
   gulp.parallel(
    'compile_dbg',
    'watch_tests'));