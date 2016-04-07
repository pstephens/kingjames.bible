var _ =       require('lodash');
var cp =      require('child_process');
var fs =      require('fs');
var gulp =    require('gulp');
var mkdirp =  require('mkdirp');
var os =      require('os');
var path =    require('path');
var phantom = require('phantomjs2');
var q =       require('q');

var root_dir = __dirname
var node_exec = process.execPath;
var out_bible_dir = path.join(root_dir, 'out/bible');

function output_to(name, stream) {
    return function output_to(buff) {
        _(buff.toString().split(/[\r\n]/))
            .filter(function(line) {
                return !(/^\s*$/.test(line)); })
            .forEach(function(line) {
                stream.write(name + ': ' + line + os.EOL);
            })
            .value();
    }
}

function spawn(name, command, args) {
    var deferred = q.defer();
    args = args || [];
    var proc = cp.spawn(command, args, { cwd: root_dir });
    proc.stdout.on('data', output_to(name, process.stdout));
    proc.stderr.on('data', output_to(name, process.stderr));
    proc.on('close', function(code) {
        if(code) {
            deferred.reject(new Error(name + ': failed with exit code ' + code));
        }
        else {
            deferred.resolve();
        }
    });
    proc.on('error', function(err) {
        deferred.reject(err);
    });

    return {
        proc: proc,
        promise: deferred.promise
    };
}

function exec(name, command, args) {
    // Seems like shell execution on windows while streaming to stdout and stderr is not yet supported.
    // https://github.com/nodejs/node/issues/1009
    args = args || [];
    if(process.platform === 'win32')
    {
        return spawn(name, 'cmd.exe', [ '/c', command ].concat(args));
    }
    else {
        return spawn(name, command, args);
    }
}

gulp.task('compile_dbg', function() {
    return exec('lein', 'lein', ['cljsbuild', 'auto']).promise;
});

gulp.task('bible_resources_dir', function(cb) {
    mkdirp(out_bible_dir, cb);
});

gulp.task('bible_resources', gulp.series(
    'bible_resources_dir',
    function prepare() {
        return spawn('prepare', node_exec, ['biblecli.js', 'prepare', 'staggs',
            path.join('kjv-src/www.staggs.pair.com-kjbp/kjv.txt'), 'out/bible' ]).promise;
    }));

gulp.task('run_node_tests', function() {
    return spawn('node', node_exec, ['nodetest.js']).promise;
});

gulp.task('run_phantom_tests', function() {
    // launch the web server
    var server = spawn('serve', node_exec, ['biblecli.js', 'serve']);

    // launch phantom.js
    return spawn('phantom', phantom.path, ['phantomtest.js', 'http://localhost:7490/phantomtest.html']).promise
        .finally(function() {
            server.proc.kill();
        });
});

gulp.task('run_tests',
    gulp.series(
        'run_node_tests',
        'bible_resources',
        'run_phantom_tests'));

gulp.task('watch_tests', function() {
    return gulp.watch("out/dbg_browser/last-compiled.txt",
        gulp.series('run_tests'));
});

gulp.task('default',
   gulp.parallel(
    'compile_dbg',
    'watch_tests'));