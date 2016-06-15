var _ =       require('lodash');
var cp =      require('child_process');
var del =     require('del');
var fs =      require('fs');
var gulp =    require('gulp');
var uglify =  require('gulp-uglify');
var jasmine = require('jasmine-core').files;
var mkdirp =  require('mkdirp');
var os =      require('os');
var path =    require('path');
var phantom = require('phantomjs2');
var q =       require('q');

var config = (function parse_commandline() {
    var argv_opts = {
        boolean: ['prod'],
        alias: {prod: 'p'}
    };
    var argv = require("minimist")(process.argv.slice(2), argv_opts);
    var default_config = {
        profile: "default",
        region: "us-east-1",
        bucket: "kingjames-beta",
        bible_src: 'kjv-src/www.staggs.pair.com-kjbp/kjv.txt',
        bible_parser: 'staggs',
    };
    return _.merge(default_config, argv);
})();

var build_config = config.prod ? "prod" : "beta";
var root_dir = __dirname
var node_exec_path = process.execPath;
var out_bible_dir = path.join(root_dir, 'out/bible');
var build_dir = path.join(root_dir, 'out/' + build_config);
var temp_dir = path.join(root_dir, 'out/temp');
var votd_dir = path.join(build_dir, 'votd');
var votd_jasmine_dir = path.join(votd_dir, 'jasmine');
var temp_votd_dir = path.join(temp_dir, 'votd');

function output_to(name, stream) {
    return function output_to(buff) {
        _(buff.toString().split(/[\r\n]/))
            .filter(function(line) {
                return !(/^\s*$/.test(line)); })
            .forEach(function(line) {
                stream.write(name + ': ' + line + os.EOL);
            });
    }
}

function spawn(name, command, args, options) {
    var deferred = q.defer();
    args = args || [];
    options = Object.assign({ cwd: root_dir }, options || {});

    var proc = cp.spawn(command, args, options);
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
    return spawn(name, command, args, { shell: true });
}

function biblecli(cmd, args) {
    return spawn('biblecli-' + cmd, node_exec_path,
        _.concat(['biblecli.js', cmd], _.slice(arguments, 1))).promise
}

function biblecli_task(cmd, args) {
    args = _.slice(arguments);
    return function biblecli_task() {
        return biblecli.apply(null, args);
    }
}

function mkdir_task(dir) {
    return function mkdir_task(cb) {
        mkdirp(dir, cb);
    }
}

gulp.task('compile_dbg', function() {
    return exec('lein', 'lein', ['cljsbuild', 'auto', 'dbg']).promise;
});

gulp.task('bible_resources_dir', mkdir_task(out_bible_dir));

gulp.task('bible_resources', gulp.series(
    'bible_resources_dir',
    biblecli_task('prepare', config.bible_parser, config.bible_src, 'out/bible')
    ));

gulp.task('run_node_tests', function() {
    return spawn('node', node_exec_path, ['nodetest.js']).promise;
});

gulp.task('run_phantom_tests', function() {
    // launch the web server
    var server = biblecli('serve');

    // launch phantom.js
    return spawn('phantom', phantom.path, ['phantomtest.js', 'http://localhost:7490/phantomtest.html']).promise
        .finally(function() {
            console.log('Stopping the web test server...');
            server.proc.kill();
        });
});

gulp.task('run_tests',
    gulp.series(
        'run_node_tests',
        'bible_resources',
        'run_phantom_tests'));

gulp.task('watch_tests', function() {
    return gulp.watch("out/dbg/last-compiled.txt",
        gulp.series('run_tests'));
});

gulp.task('make_build_dir', mkdir_task(build_dir));

gulp.task('make_temp_votd_dir', mkdir_task(temp_votd_dir));

gulp.task('clean', function() {
    return del([path.join(build_dir, '**'), '!' + build_dir], { cwd: root_dir });
});

gulp.task('copy_votd_jasmine',
    gulp.parallel(
        function copy_jasmine_files() {
            var files = _.concat(jasmine.cssFiles, jasmine.jsFiles);
            return gulp.src(files, { cwd: jasmine.path })
                    .pipe(gulp.dest(votd_jasmine_dir));
        },
        function copy_jasmine_boot() {
            return gulp.src(jasmine.bootFiles, { cwd: jasmine.bootDir })
                    .pipe(gulp.dest(votd_jasmine_dir));
        },
        function copy_jasmine_images() {
            return gulp.src('jasmine_favicon.png', { cwd: jasmine.imagesDir })
                    .pipe(gulp.dest(votd_jasmine_dir));
        }));

gulp.task('copy_votd_tests', function copy_votd_tests() {
    return gulp.src('src/votd/test/**')
            .pipe(gulp.dest(votd_dir));
});

gulp.task('build_votd_js',
    gulp.series(
        'make_temp_votd_dir',
        biblecli_task('verseoftheday', config.bible_parser, config.bible_src, 'src/votd/verse-list.md', temp_votd_dir),
        gulp.parallel(
            function copy_client_html() {
                return gulp.src(path.join(temp_votd_dir, 'client.html'))
                        .pipe(gulp.dest(votd_dir));
            },
            function uglify_votd_js() {
                return gulp.src(path.join(temp_votd_dir, 'votd.js'))
                        .pipe(uglify())
                        .pipe(gulp.dest(votd_dir));
            })));

gulp.task('build',
    gulp.series(
        'clean',
        'make_build_dir',
        gulp.parallel(
            biblecli_task('static', config.bible_parser, config.bible_src, build_dir),
            'copy_votd_jasmine',
            'copy_votd_tests',
            'build_votd_js')));

gulp.task('default',
   gulp.parallel(
    'compile_dbg',
    'watch_tests'));